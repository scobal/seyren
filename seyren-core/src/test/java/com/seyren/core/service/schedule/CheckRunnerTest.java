/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seyren.core.service.schedule;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seyren.core.domain.*;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.base.Optional;
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.service.checker.TargetChecker;
import com.seyren.core.service.checker.ValueChecker;
import com.seyren.core.service.notification.NotificationService;
import com.seyren.core.store.AlertsStore;
import com.seyren.core.store.ChecksStore;

public class CheckRunnerTest {
    
    private ThresholdCheck mockCheck;
    private AlertsStore mockAlertsStore;
    private ChecksStore mockChecksStore;
    private TargetChecker mockTargetChecker;
    private ValueChecker mockValueChecker;
    private NotificationService mockNotificationService;
    private Iterable<NotificationService> mockNotificationServices;
    private CheckRunner checkRunner;
    
    @Before
    public void before() {
        mockCheck = mock(ThresholdCheck.class);
        mockAlertsStore = mock(AlertsStore.class);
        mockChecksStore = mock(ChecksStore.class);
        mockTargetChecker = mock(TargetChecker.class);
        mockValueChecker = mock(ValueChecker.class);
        mockNotificationService = mock(NotificationService.class);
        mockNotificationServices = Arrays.asList(mockNotificationService);
        checkRunner = new CheckRunner(
                mockCheck,
                mockAlertsStore,
                mockChecksStore,
                mockTargetChecker,
                mockValueChecker,
                mockNotificationServices, "60000");
        
        // Clear all cached values so that initial state is true even between tests
        CheckRunner.flushLastAlerts();
    }
    
    @Test
    public void checkWhichIsNotEnabledDoesNothing() {
        when(mockCheck.isEnabled()).thenReturn(false);
        checkRunner.run();
    }
    
    @Test
    public void noTargetValuesUpdatesLastCheckWithUnknown() throws Exception {
        when(mockCheck.getId()).thenReturn("id");
        when(mockCheck.isEnabled()).thenReturn(true);
        when(mockCheck.isAllowNoData()).thenReturn(false);
        when(mockTargetChecker.check(mockCheck)).thenReturn(new HashMap<String, Optional<BigDecimal>>());
        when(mockChecksStore.updateStateAndLastCheck(eq("id"), eq(AlertType.UNKNOWN), any(DateTime.class))).thenReturn(mockCheck);
        checkRunner.run();
        verify(mockChecksStore).updateStateAndLastCheck(eq("id"),  eq(AlertType.UNKNOWN), any(DateTime.class));
    }
    
    @Test
    public void noTargetValuesButAllowNoDataUpdatesLastCheckWithOk() throws Exception {
        when(mockCheck.getId()).thenReturn("id");
        when(mockCheck.isEnabled()).thenReturn(true);
        when(mockCheck.isAllowNoData()).thenReturn(true);
        when(mockTargetChecker.check(mockCheck)).thenReturn(new HashMap<String, Optional<BigDecimal>>());
        when(mockChecksStore.updateStateAndLastCheck(eq("id"), eq(AlertType.OK), any(DateTime.class))).thenReturn(mockCheck);
        checkRunner.run();
        verify(mockChecksStore).updateStateAndLastCheck(eq("id"),  eq(AlertType.OK), any(DateTime.class));
    }
    
    @Test
    public void anExceptionWhileRunningIsHandled() throws Exception {
        when(mockCheck.isEnabled()).thenReturn(true);
        when(mockTargetChecker.check(mockCheck)).thenThrow(new Exception("Boom!"));
        checkRunner.run();
    }
    
    @Test
    public void emptyTargetValueDoesNothing() throws Exception {
        when(mockCheck.isEnabled()).thenReturn(true);
        Map<String, Optional<BigDecimal>> targetValues = new HashMap<String, Optional<BigDecimal>>();
        targetValues.put("target", Optional.<BigDecimal>absent());
        when(mockTargetChecker.check(mockCheck)).thenReturn(targetValues);
        checkRunner.run();
    }
    
    @Test
    public void cachesLastAlertByTarget() throws Exception {
    	BigDecimal warnLevel = new BigDecimal(0.6);
    	BigDecimal errorLevel = new BigDecimal(0.8);
    	Alert initialAlert1 = new ThresholdAlert()
                .withWarn(warnLevel)
                .withError(errorLevel)
                .withCheckId("check1")
    		    .withTarget("target1")
    		    .withValue(new BigDecimal(0.1))
    		    .withFromType(AlertType.WARN)
    		    .withToType(AlertType.OK)
    		    .withTimestamp(DateTime.now());
    	Alert initialAlert2 = null;

    	when(mockCheck.isEnabled()).thenReturn(true);
    	Map<String, Optional<BigDecimal>> targetValues = new HashMap<String, Optional<BigDecimal>>();
    	targetValues.put("target1", Optional.of(new BigDecimal(0.2)));
    	targetValues.put("target2", Optional.of(new BigDecimal(0.4)));
    	when(mockTargetChecker.check(mockCheck)).thenReturn(targetValues);
    	when(mockCheck.hasRemoteServerErrorOccurred()).thenReturn(false);
    	when(mockCheck.getId()).thenReturn("check1");
    	when(mockCheck.getWarn()).thenReturn(warnLevel);
    	when(mockCheck.getError()).thenReturn(errorLevel);
    	when(mockCheck.isAllowNoData()).thenReturn(true);
    	when(mockAlertsStore.getLastAlertForTargetOfCheck("target1", "check1")).thenReturn(initialAlert1);
    	when(mockAlertsStore.getLastAlertForTargetOfCheck("target2", "check1")).thenReturn(initialAlert2);
    	when(mockValueChecker.checkValue(any(BigDecimal.class), any(BigDecimal.class), any(BigDecimal.class))).thenReturn(AlertType.OK);
    	
    	
    	checkRunner.run();
    	
    	// First time through for these targets, it should pull them from persistence tier
    	verify(mockAlertsStore, times(1)).getLastAlertForTargetOfCheck("target1", "check1");
    	verify(mockAlertsStore, times(1)).getLastAlertForTargetOfCheck("target2", "check1");
    
    	// Add a new target
    	targetValues.put("target3", Optional.of(new BigDecimal(0.3)));
    	when(mockAlertsStore.getLastAlertForTargetOfCheck("target3", "check1")).thenReturn(null);
    	
    	checkRunner.run();
    	
    	// Second time through, verify that still only a single pull from persistence has been made per target
    	verify(mockAlertsStore, times(1)).getLastAlertForTargetOfCheck("target1", "check1");
    	verify(mockAlertsStore, times(1)).getLastAlertForTargetOfCheck("target2", "check1");
    	verify(mockAlertsStore, times(1)).getLastAlertForTargetOfCheck("target3", "check1");
    }

    @Test
    public void updatesCacheWhenSavingAlert() throws Exception {
    	Check mockUpdatedCheck = mock(Check.class);
    	BigDecimal warnLevel = new BigDecimal(0.6);
    	BigDecimal errorLevel = new BigDecimal(0.8);
    	Alert initialAlert = new ThresholdAlert()
                .withWarn(warnLevel)
                .withError(errorLevel)
                .withCheckId("check1")
    		    .withTarget("target1")
    		    .withValue(new BigDecimal(0.1))
    		    .withFromType(AlertType.WARN)
    		    .withToType(AlertType.OK)
    		    .withTimestamp(DateTime.now());

    	when(mockCheck.isEnabled()).thenReturn(true);
    	Map<String, Optional<BigDecimal>> targetValues = new HashMap<String, Optional<BigDecimal>>();
    	targetValues.put("target1", Optional.of(new BigDecimal(0.7)));
    	when(mockTargetChecker.check(mockCheck)).thenReturn(targetValues);
    	when(mockCheck.hasRemoteServerErrorOccurred()).thenReturn(false);
    	when(mockCheck.getId()).thenReturn("check1");
    	when(mockCheck.getWarn()).thenReturn(warnLevel);
    	when(mockCheck.getError()).thenReturn(errorLevel);
    	when(mockCheck.isAllowNoData()).thenReturn(true);
    	when(mockAlertsStore.getLastAlertForTargetOfCheck("target1", "check1")).thenReturn(initialAlert);
    	when(mockValueChecker.checkValue(any(BigDecimal.class), any(BigDecimal.class), any(BigDecimal.class))).thenReturn(AlertType.WARN);
    	when(mockCheck.getSubscriptions()).thenReturn(null);
    	when(mockAlertsStore.createAlert(anyString(), any(Alert.class))).thenReturn(null);
    	when(mockChecksStore.updateStateAndLastCheck(anyString(), any(AlertType.class), any(DateTime.class))).thenReturn(mockUpdatedCheck);
    	
    	checkRunner.run();
    	
    	// First time through for these targets, it should pull from persistence tier and receive OK
    	verify(mockAlertsStore, times(1)).getLastAlertForTargetOfCheck("target1", "check1");
    	
    	// And it should find that state transitions from OK to WARN, so it should create an alert and getSubscriptions should be called
    	verify(mockAlertsStore, times(1)).createAlert(anyString(), any(Alert.class));
    	verify(mockUpdatedCheck, times(1)).getSubscriptions();
    	
    	
    	checkRunner.run();
    	
    	// Second time through, it should pull from cache and it should move from WARN to WARN, so new alert
    	// but no new attempt to retrieve subscriptions
    	verify(mockAlertsStore, times(1)).getLastAlertForTargetOfCheck("target1", "check1");
    	verify(mockAlertsStore, times(2)).createAlert(anyString(), any(Alert.class));
    	verify(mockUpdatedCheck, times(1)).getSubscriptions();
    }

    @Test
    public void noPreviousAlertAndHappyCurrentValueDoesNothing() throws Exception {
        BigDecimal value = BigDecimal.ONE;
        BigDecimal warn = BigDecimal.valueOf(2);
        BigDecimal error = BigDecimal.valueOf(3);
        
        when(mockCheck.getId()).thenReturn("id");
        when(mockCheck.isEnabled()).thenReturn(true);
        when(mockCheck.getWarn()).thenReturn(warn);
        when(mockCheck.getError()).thenReturn(error);
        
        Map<String, Optional<BigDecimal>> targetValues = new HashMap<String, Optional<BigDecimal>>();
        targetValues.put("target", Optional.of(value));
        when(mockTargetChecker.check(mockCheck)).thenReturn(targetValues);
        when(mockAlertsStore.getLastAlertForTargetOfCheck("target", "id")).thenReturn(null);
        when(mockValueChecker.checkValue(value, warn, error)).thenReturn(AlertType.OK);
        checkRunner.run();
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void createAlertButDoNotNotifyIfStateIsTheSame() throws Exception {
        BigDecimal value = BigDecimal.ONE;
        BigDecimal warn = BigDecimal.valueOf(2);
        BigDecimal error = BigDecimal.valueOf(3);
        
        when(mockCheck.getId()).thenReturn("id");
        when(mockCheck.isEnabled()).thenReturn(true);
        when(mockCheck.getWarn()).thenReturn(warn);
        when(mockCheck.getError()).thenReturn(error);
        
        Map<String, Optional<BigDecimal>> targetValues = new HashMap<String, Optional<BigDecimal>>();
        targetValues.put("target", Optional.of(value));
        when(mockTargetChecker.check(mockCheck)).thenReturn(targetValues);
        when(mockAlertsStore.getLastAlertForTargetOfCheck("target", "id")).thenReturn(new Alert().withToType(AlertType.WARN));
        when(mockValueChecker.checkValue(value, warn, error)).thenReturn(AlertType.WARN);
        
        Alert alert = new Alert();
        
        when(mockAlertsStore.createAlert(eq("id"), any(Alert.class))).thenReturn(alert);
        when(mockChecksStore.updateStateAndLastCheck(eq("id"), eq(AlertType.WARN), any(DateTime.class))).thenReturn(mockCheck);
        
        checkRunner.run();
        
        verify(mockAlertsStore).createAlert(eq("id"), any(Alert.class));
        verify(mockNotificationService, times(0)).sendNotification(any(Check.class), any(Subscription.class), any(List.class));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void changeOfStateButNoSubscriptionsDoesNothing() throws Exception {
        BigDecimal value = BigDecimal.ONE;
        BigDecimal warn = BigDecimal.valueOf(2);
        BigDecimal error = BigDecimal.valueOf(3);
        
        when(mockCheck.getId()).thenReturn("id");
        when(mockCheck.isEnabled()).thenReturn(true);
        when(mockCheck.getWarn()).thenReturn(warn);
        when(mockCheck.getError()).thenReturn(error);
        when(mockCheck.getSubscriptions()).thenReturn(new ArrayList<Subscription>());
        
        Map<String, Optional<BigDecimal>> targetValues = new HashMap<String, Optional<BigDecimal>>();
        targetValues.put("target", Optional.of(value));
        when(mockTargetChecker.check(mockCheck)).thenReturn(targetValues);
        when(mockAlertsStore.getLastAlertForTargetOfCheck("target", "id")).thenReturn(new Alert().withToType(AlertType.WARN));
        when(mockValueChecker.checkValue(value, warn, error)).thenReturn(AlertType.ERROR);
        
        Alert alert = new Alert();
        
        when(mockAlertsStore.createAlert(eq("id"), any(Alert.class))).thenReturn(alert);
        when(mockChecksStore.updateStateAndLastCheck(eq("id"), eq(AlertType.ERROR), any(DateTime.class))).thenReturn(mockCheck);
        
        checkRunner.run();
        
        verify(mockAlertsStore).createAlert(eq("id"), any(Alert.class));
        verify(mockNotificationService, times(0)).sendNotification(any(Check.class), any(Subscription.class), any(List.class));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void changeOfStateButSubscriptionWhichShouldNotNotifyDoesNothing() throws Exception {
        BigDecimal value = BigDecimal.ONE;
        BigDecimal warn = BigDecimal.valueOf(2);
        BigDecimal error = BigDecimal.valueOf(3);
        
        Subscription mockSubscription = mock(Subscription.class);
        when(mockSubscription.getType()).thenReturn(SubscriptionType.EMAIL);
        
        when(mockCheck.getId()).thenReturn("id");
        when(mockCheck.isEnabled()).thenReturn(true);
        when(mockCheck.getWarn()).thenReturn(warn);
        when(mockCheck.getError()).thenReturn(error);
        when(mockCheck.getSubscriptions()).thenReturn(Arrays.asList(mockSubscription));
        
        Map<String, Optional<BigDecimal>> targetValues = new HashMap<String, Optional<BigDecimal>>();
        targetValues.put("target", Optional.of(value));
        when(mockTargetChecker.check(mockCheck)).thenReturn(targetValues);
        when(mockAlertsStore.getLastAlertForTargetOfCheck("target", "id")).thenReturn(new Alert().withToType(AlertType.WARN));
        when(mockValueChecker.checkValue(value, warn, error)).thenReturn(AlertType.ERROR);
        
        Alert alert = new Alert();
        
        when(mockAlertsStore.createAlert(eq("id"), any(Alert.class))).thenReturn(alert);
        when(mockChecksStore.updateStateAndLastCheck(eq("id"), eq(AlertType.ERROR), any(DateTime.class))).thenReturn(mockCheck);
        when(mockSubscription.shouldNotify(any(DateTime.class), eq(AlertType.ERROR))).thenReturn(false);
        
        checkRunner.run();
        
        verify(mockAlertsStore).createAlert(eq("id"), any(Alert.class));
        verify(mockNotificationService, times(0)).sendNotification(any(Check.class), any(Subscription.class), any(List.class));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void changeOfStateAndSubscriptionWhichShouldNotifyAndCannotHandleDoesNothing() throws Exception {
        BigDecimal value = BigDecimal.ONE;
        BigDecimal warn = BigDecimal.valueOf(2);
        BigDecimal error = BigDecimal.valueOf(3);
        
        Subscription mockSubscription = mock(Subscription.class);
        when(mockSubscription.getType()).thenReturn(SubscriptionType.EMAIL);
        
        when(mockCheck.getId()).thenReturn("id");
        when(mockCheck.isEnabled()).thenReturn(true);
        when(mockCheck.getWarn()).thenReturn(warn);
        when(mockCheck.getError()).thenReturn(error);
        when(mockCheck.getSubscriptions()).thenReturn(Arrays.asList(mockSubscription));
        
        Map<String, Optional<BigDecimal>> targetValues = new HashMap<String, Optional<BigDecimal>>();
        targetValues.put("target", Optional.of(value));
        when(mockTargetChecker.check(mockCheck)).thenReturn(targetValues);
        when(mockAlertsStore.getLastAlertForTargetOfCheck("target", "id")).thenReturn(new Alert().withToType(AlertType.WARN));
        when(mockValueChecker.checkValue(value, warn, error)).thenReturn(AlertType.ERROR);
        
        Alert alert = new Alert();
        
        when(mockAlertsStore.createAlert(eq("id"), any(Alert.class))).thenReturn(alert);
        when(mockChecksStore.updateStateAndLastCheck(eq("id"), eq(AlertType.ERROR), any(DateTime.class))).thenReturn(mockCheck);
        when(mockSubscription.shouldNotify(any(DateTime.class), eq(AlertType.ERROR))).thenReturn(true);
        when(mockNotificationService.canHandle(SubscriptionType.EMAIL)).thenReturn(false);
        
        checkRunner.run();
        
        verify(mockAlertsStore).createAlert(eq("id"), any(Alert.class));
        verify(mockNotificationService, times(0)).sendNotification(eq(mockCheck), eq(mockSubscription), any(List.class));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void changeOfStateAndSubscriptionWhichShouldNotifyAndCanHandleSendsNotification() throws Exception {
        BigDecimal value = BigDecimal.ONE;
        BigDecimal warn = BigDecimal.valueOf(2);
        BigDecimal error = BigDecimal.valueOf(3);
        
        Subscription mockSubscription = mock(Subscription.class);
        when(mockSubscription.getType()).thenReturn(SubscriptionType.EMAIL);
        
        when(mockCheck.getId()).thenReturn("id");
        when(mockCheck.isEnabled()).thenReturn(true);
        when(mockCheck.getWarn()).thenReturn(warn);
        when(mockCheck.getError()).thenReturn(error);
        when(mockCheck.getSubscriptions()).thenReturn(Arrays.asList(mockSubscription));
        
        Map<String, Optional<BigDecimal>> targetValues = new HashMap<String, Optional<BigDecimal>>();
        targetValues.put("target", Optional.of(value));
        when(mockTargetChecker.check(mockCheck)).thenReturn(targetValues);
        when(mockAlertsStore.getLastAlertForTargetOfCheck("target", "id")).thenReturn(new Alert().withToType(AlertType.WARN));
        when(mockValueChecker.checkValue(value, warn, error)).thenReturn(AlertType.ERROR);
        
        Alert alert = new Alert();
        
        when(mockAlertsStore.createAlert(eq("id"), any(Alert.class))).thenReturn(alert);
        when(mockChecksStore.updateStateAndLastCheck(eq("id"), eq(AlertType.ERROR), any(DateTime.class))).thenReturn(mockCheck);
        when(mockSubscription.shouldNotify(any(DateTime.class), eq(AlertType.ERROR))).thenReturn(true);
        when(mockNotificationService.canHandle(SubscriptionType.EMAIL)).thenReturn(true);
        
        checkRunner.run();
        
        verify(mockAlertsStore).createAlert(eq("id"), any(Alert.class));
        verify(mockNotificationService).sendNotification(eq(mockCheck), eq(mockSubscription), any(List.class));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void exceptionWhileSendingNotificationIsHandled() throws Exception {
        BigDecimal value = BigDecimal.ONE;
        BigDecimal warn = BigDecimal.valueOf(2);
        BigDecimal error = BigDecimal.valueOf(3);
        
        Subscription mockSubscription = mock(Subscription.class);
        when(mockSubscription.getType()).thenReturn(SubscriptionType.EMAIL);
        
        when(mockCheck.getId()).thenReturn("id");
        when(mockCheck.isEnabled()).thenReturn(true);
        when(mockCheck.getWarn()).thenReturn(warn);
        when(mockCheck.getError()).thenReturn(error);
        when(mockCheck.getSubscriptions()).thenReturn(Arrays.asList(mockSubscription));
        
        Map<String, Optional<BigDecimal>> targetValues = new HashMap<String, Optional<BigDecimal>>();
        targetValues.put("target", Optional.of(value));
        when(mockTargetChecker.check(mockCheck)).thenReturn(targetValues);
        when(mockAlertsStore.getLastAlertForTargetOfCheck("target", "id")).thenReturn(new Alert().withToType(AlertType.WARN));
        when(mockValueChecker.checkValue(value, warn, error)).thenReturn(AlertType.ERROR);
        
        Alert alert = new Alert();
        
        when(mockAlertsStore.createAlert(eq("id"), any(Alert.class))).thenReturn(alert);
        when(mockChecksStore.updateStateAndLastCheck(eq("id"), eq(AlertType.ERROR), any(DateTime.class))).thenReturn(mockCheck);
        when(mockSubscription.shouldNotify(any(DateTime.class), eq(AlertType.ERROR))).thenReturn(true);
        when(mockNotificationService.canHandle(SubscriptionType.EMAIL)).thenReturn(true);
        Mockito.doThrow(new NotificationFailedException("Boom!")).when(mockNotificationService).sendNotification(eq(mockCheck), eq(mockSubscription), any(List.class));
        
        checkRunner.run();
        
        verify(mockAlertsStore).createAlert(eq("id"), any(Alert.class));
    }
    
}
