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

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.base.Optional;
import com.seyren.core.domain.Alert;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.service.checker.TargetChecker;
import com.seyren.core.service.checker.ValueChecker;
import com.seyren.core.service.notification.NotificationService;
import com.seyren.core.service.notification.NotificationServiceSettings;
import com.seyren.core.store.AlertsStore;
import com.seyren.core.store.ChecksStore;
import com.seyren.core.util.config.SeyrenConfig;
import org.joda.time.DateTimeUtils;

public class CheckRunnerTest {
    
    private Check mockCheck;
    private AlertsStore mockAlertsStore;
    private ChecksStore mockChecksStore;
    private TargetChecker mockTargetChecker;
    private ValueChecker mockValueChecker;
    private NotificationService mockNotificationService;
    private NotificationServiceSettings mockNotificationServiceSettings;
    private Iterable<NotificationService> mockNotificationServices;
    private SeyrenConfig mockSeyrenConfig;
    private CheckRunner checkRunner;    
    
    @Before
    public void before() {
        mockCheck = mock(Check.class);
        mockAlertsStore = mock(AlertsStore.class);
        mockChecksStore = mock(ChecksStore.class);
        mockTargetChecker = mock(TargetChecker.class);
        mockValueChecker = mock(ValueChecker.class);
        mockNotificationService = mock(NotificationService.class);
        mockSeyrenConfig = mock(SeyrenConfig.class);
        mockNotificationServices = Arrays.asList(mockNotificationService);
        mockNotificationServiceSettings = mock(NotificationServiceSettings.class);
        checkRunner = new CheckRunner(
                mockCheck,
                mockAlertsStore,
                mockChecksStore,
                mockTargetChecker,
                mockValueChecker,
                mockNotificationServices,
                mockSeyrenConfig,
                mockNotificationServiceSettings);
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

    
    @SuppressWarnings("unchecked")
    @Test
    public void changeOfStateAndSubscriptionWhichShouldNotifyAndCanHandleSendsNotification2() throws Exception {
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
    public void globalDelayNotSetAndSpecificDelayNotSetSendNotificationOnStateChange() throws Exception {
        BigDecimal warn = BigDecimal.valueOf(2);
        BigDecimal error = BigDecimal.valueOf(3);
        
        when(mockCheck.getId()).thenReturn("id");
        when(mockCheck.isEnabled()).thenReturn(true);
        when(mockCheck.getWarn()).thenReturn(warn);
        when(mockCheck.getError()).thenReturn(error);
        when(mockCheck.getNotificationDelay()).thenReturn(null);
        when(mockCheck.getNotificationInterval()).thenReturn(null);
        when(mockSeyrenConfig.getAlertNotificationDelayInSeconds()).thenReturn(0);
        
        when(mockNotificationServiceSettings.applyNotificationDelayAndIntervalProperties(mockCheck, AlertType.OK, AlertType.ERROR, null)).thenReturn(false);
        
        checkRunner.run();

        verifyZeroInteractions(mockNotificationServiceSettings);
    }    

    
//    @SuppressWarnings("unchecked")
//    @Test
//    public void globalDelaySetAndSpecificDelayNotSetSendNotificationEveryIntervalIfStateIsInErrorLongerThanDelay() throws Exception {
//        BigDecimal value = BigDecimal.ONE;
//        BigDecimal warn = BigDecimal.valueOf(2);
//        BigDecimal error = BigDecimal.valueOf(3);
//        DateTime now = new DateTime();
//        
//        Subscription mockSubscription = mock(Subscription.class);
//        when(mockSubscription.getType()).thenReturn(SubscriptionType.EMAIL);
//        
//        when(mockCheck.getId()).thenReturn("id");
//        when(mockCheck.isEnabled()).thenReturn(true);
//        when(mockCheck.getWarn()).thenReturn(warn);
//        when(mockCheck.getError()).thenReturn(error);
//        when(mockCheck.getSubscriptions()).thenReturn(Arrays.asList(mockSubscription));        
//        when(mockCheck.getNotificationDelay()).thenReturn(null);
//        when(mockCheck.getNotificationInterval()).thenReturn(null);
//        when(mockSeyrenConfig.getAlertNotificationDelayInSeconds()).thenReturn(10);
//        when(mockSeyrenConfig.getAlertNotificationIntervalInSeconds()).thenReturn(20);
//        //DateTimeUtils.setCurrentMillisFixed(10L);
//        when(mockNotificationServiceSettings.applyNotificationDelayAndIntervalProperties(eq(mockCheck), eq(AlertType.OK), eq(AlertType.ERROR), any(DateTime.class))).thenReturn(false);
//
//        Map<String, Optional<BigDecimal>> targetValues = new HashMap<String, Optional<BigDecimal>>();
//        targetValues.put("target", Optional.of(value));
//        when(mockTargetChecker.check(mockCheck)).thenReturn(targetValues);
//        when(mockAlertsStore.getLastAlertForTargetOfCheck("target", "id")).thenReturn(new Alert().withToType(AlertType.WARN));
//        when(mockValueChecker.checkValue(value, warn, error)).thenReturn(AlertType.ERROR);
//        
//        Alert alert = new Alert();
//        
//        when(mockAlertsStore.createAlert(eq("id"), any(Alert.class))).thenReturn(alert);
//        when(mockChecksStore.updateStateAndLastCheck(eq("id"), eq(AlertType.ERROR), any(DateTime.class))).thenReturn(mockCheck);
//        when(mockSubscription.shouldNotify(any(DateTime.class), eq(AlertType.ERROR))).thenReturn(true);
//        when(mockNotificationService.canHandle(SubscriptionType.EMAIL)).thenReturn(true);        
//        
//        
//        checkRunner.run();
//
//        verify(mockNotificationServiceSettings).applyNotificationDelayAndIntervalProperties(mockCheck, AlertType.OK, AlertType.ERROR, new DateTime());
//    }
//    
//    @SuppressWarnings("unchecked")
//    @Test
//    public void globalDelayNotSetAndSpecificDelaySetSendNotificationEveryIntervalIfStateIsInErrorLongerThanDelay() throws Exception {
//        
//    }    
//    
//    @SuppressWarnings("unchecked")
//    @Test
//    public void globalDelaySetAndSpecificDelaySetSendNotificationEveryIntervalIfStateIsInErrorLongerThanDelay() throws Exception {
//        
//    }
//    
//    @SuppressWarnings("unchecked")
//    @Test
//    public void createAlertWithStateInErrorShorterThanGlobalDelayWhichShouldNotSendNotification() throws Exception {
//        
//    }    
//    
//    @SuppressWarnings("unchecked")
//    @Test
//    public void createAlertWithStateInErrorShorterThanSpecificDelayWhichShouldNotNotify() throws Exception {
//        
//    }
    
}
