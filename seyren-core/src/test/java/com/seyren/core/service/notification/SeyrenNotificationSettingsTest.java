/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.seyren.core.service.notification;

import com.github.restdriver.clientdriver.ClientDriverRule;
import com.seyren.core.domain.AlertType;
import com.seyren.core.util.config.SeyrenConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.seyren.core.domain.Check;
import com.seyren.core.store.ChecksStore;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.junit.Rule;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 *
 * @author DVrede
 */
public class SeyrenNotificationSettingsTest {
    private NotificationServiceSettings nSS;
    private SeyrenConfig mockSeyrenConfig;
    private ChecksStore mockChecksStore;
    
    @Before
    public void before() {
        mockChecksStore = mock(ChecksStore.class);
        mockSeyrenConfig = mock(SeyrenConfig.class);        
        nSS = new SeyrenNotificationSettings(mockSeyrenConfig,mockChecksStore);
    }
    
    @After
    public void after() {

    }
    
    @Test
    public void shouldReturnTrueWhenCheckStateIsErrorLongerThanTheSetDelayAndNoNotificationHasBeenSent() {
        DateTime yesterday = new DateTime(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
        
        Check check = new Check()
                .withId("123")
                .withEnabled(true)
                .withName("test-check")
                .withState(AlertType.OK)
                .withTimeFirstErrorOccured(yesterday)
                .withTimeLastNotificationSent(null);
        
        when(mockSeyrenConfig.getAlertNotificationDelayInSeconds()).thenReturn(20);                
        when(mockSeyrenConfig.getAlertNotificationIntervalInSeconds()).thenReturn(100000);                
        assertThat(nSS.applyNotificationDelayAndIntervalProperties(check, AlertType.ERROR, AlertType.ERROR, new DateTime()), is(true));        System.out.println("Stop heree");
    }
    
    @Test
    public void shouldReturnTrueWhenCheckStateIsErrorLongerThanDelayAndLastNotificationIsPassedTheInterval() {
        DateTime yesterday = new DateTime(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
        
        Check check = new Check()
                .withId("123")
                .withEnabled(true)
                .withName("test-check")
                .withState(AlertType.OK)
                .withTimeFirstErrorOccured(yesterday)
                .withTimeLastNotificationSent(yesterday);
        
        when(mockSeyrenConfig.getAlertNotificationDelayInSeconds()).thenReturn(20);                
        when(mockSeyrenConfig.getAlertNotificationIntervalInSeconds()).thenReturn(10);                
        assertThat(nSS.applyNotificationDelayAndIntervalProperties(check, AlertType.ERROR, AlertType.ERROR, new DateTime()), is(true));        System.out.println("Stop heree");        
    }
    
    @Test
    public void shouldReturnTrueWhenCheckStateChangesFromErrorToOk() {
        DateTime yesterday = new DateTime(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
        
        Check check = new Check()
                .withId("123")
                .withEnabled(true)
                .withName("test-check")
                .withState(AlertType.OK)
                .withTimeFirstErrorOccured(yesterday)
                .withTimeLastNotificationSent(null);
        
        when(mockSeyrenConfig.getAlertNotificationDelayInSeconds()).thenReturn(20);                
        when(mockSeyrenConfig.getAlertNotificationIntervalInSeconds()).thenReturn(100000);                
        assertThat(nSS.applyNotificationDelayAndIntervalProperties(check, AlertType.ERROR, AlertType.OK, new DateTime()), is(true));        System.out.println("Stop heree");        
    }
    
    @Test
    public void shouldUseConfigSettingsWhenCheckSpecificPropertiesAreNotAvailable () {
        DateTime yesterday = new DateTime(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
        
        Check check = new Check()
                .withId("123")
                .withEnabled(true)
                .withName("test-check")
                .withState(AlertType.OK)
                .withTimeFirstErrorOccured(yesterday)
                .withTimeLastNotificationSent(null);
        
        when(mockSeyrenConfig.getAlertNotificationDelayInSeconds()).thenReturn(20);                
        when(mockSeyrenConfig.getAlertNotificationIntervalInSeconds()).thenReturn(100000);                
        assertThat(nSS.applyNotificationDelayAndIntervalProperties(check, AlertType.ERROR, AlertType.ERROR, new DateTime()), is(true));        System.out.println("Stop heree");        
    }
}
