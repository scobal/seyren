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
package com.seyren.core.service.notification;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.seyren.core.domain.Alert;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.util.config.SeyrenConfig;

public class TwilioNotificationServiceTest {
    /**
     * Any phone number works with test credentials when sending SMS, according
     * to Twilio's "magic phone number" rules. We could use anything, but we use
     * the dummy From phone number here.
     * 
     * @see https://www.twilio.com/docs/api/rest/test-credentials#test-sms-messages-parameters-To
     */
    public static final String TWILIO_MAGIC_SUCCESS_PHONE_NUMBER="+15005550006";
    
    /**
     * This is one of Twilio's "magic" phone numbers that always returns a
     * failing code when sending an SMS message. This allows us to test sending
     * text messages without actually sending text messages.
     * 
     * @see https://www.twilio.com/docs/api/rest/test-credentials#test-sms-messages-parameters-To
     */
    public static final String TWILIO_MAGIC_FAILURE_PHONE_NUMBER="+15005550001";
    
    /**
     * This is one of Twilio's "magic" phone numbers that always succeeds as
     * a "from" number.
     * 
     * @see https://www.twilio.com/docs/api/rest/test-credentials#test-sms-messages-parameters-From
     */
    public static final String TWILIO_TESTING_ACCOUNT_PHONE_NUMBER="+15005550006";
    
    /**
     * This is the test Account SID associated with our Twilio testing account.
     */
    public static final String TWILIO_TESTING_ACCOUNT_SID="AC2d7846930afd1dba5188c6fab4a460a4";
    
    /**
     * This is the test Auth Token associated with our Twilio testing account.
     */
    public static final String TWILIO_TESTING_AUTH_TOKEN="bc62abadd790c9800ebb117f5fbc7d63";
    
    private SeyrenConfig mockSeyrenConfig;
    private NotificationService service;
    
    @Before
    public void before() {
        mockSeyrenConfig = mock(SeyrenConfig.class);
        service = new TwilioNotificationService(mockSeyrenConfig);
    }
    
    @Test
    public void notifcationServiceCanOnlyHandleHubotSubscription() {
        assertThat(service.canHandle(SubscriptionType.TWILIO), is(true));
        for (SubscriptionType type : SubscriptionType.values()) {
            if (type == SubscriptionType.TWILIO) {
                continue;
            }
            assertThat(service.canHandle(type), is(false));
        }
    }
    
    @Test
    public void checkingOutTheHappyPath() {
        when(mockSeyrenConfig.getTwilioAccountSid()).thenReturn(TWILIO_TESTING_ACCOUNT_SID);
        when(mockSeyrenConfig.getTwilioAuthToken()).thenReturn(TWILIO_TESTING_AUTH_TOKEN);
        when(mockSeyrenConfig.getTwilioPhoneNumber()).thenReturn(TWILIO_TESTING_ACCOUNT_PHONE_NUMBER);
        
        Check check = new Check()
                .withEnabled(true)
                .withName("check-name")
                .withState(AlertType.ERROR);
        
        Subscription subscription = new Subscription()
                .withType(SubscriptionType.TWILIO)
                .withTarget(TWILIO_MAGIC_SUCCESS_PHONE_NUMBER);
        
        Alert alert = new Alert()
                .withTarget("the.target.name")
                .withValue(BigDecimal.valueOf(12))
                .withWarn(BigDecimal.valueOf(5))
                .withError(BigDecimal.valueOf(10))
                .withFromType(AlertType.WARN)
                .withToType(AlertType.ERROR);
        
        List<Alert> alerts = Arrays.asList(alert);
        
        // This will fail with an NotificationFailedException if anything goes wrong.
        service.sendNotification(check, subscription, alerts);
    }
    
    @Test
    public void checkingOutTheSadPath() {
        when(mockSeyrenConfig.getTwilioAccountSid()).thenReturn(TWILIO_TESTING_ACCOUNT_SID);
        when(mockSeyrenConfig.getTwilioAuthToken()).thenReturn(TWILIO_TESTING_AUTH_TOKEN);
        when(mockSeyrenConfig.getTwilioPhoneNumber()).thenReturn(TWILIO_TESTING_ACCOUNT_PHONE_NUMBER);
        
        Check check = new Check()
                .withEnabled(true)
                .withName("check-name")
                .withState(AlertType.ERROR);
        
        Subscription subscription = new Subscription()
                .withType(SubscriptionType.TWILIO)
                .withTarget(TWILIO_MAGIC_FAILURE_PHONE_NUMBER);
        
        Alert alert = new Alert()
                .withTarget("the.target.name")
                .withValue(BigDecimal.valueOf(12))
                .withWarn(BigDecimal.valueOf(5))
                .withError(BigDecimal.valueOf(10))
                .withFromType(AlertType.WARN)
                .withToType(AlertType.ERROR);
        
        List<Alert> alerts = Arrays.asList(alert);
        
        // This will fail with a NotificationFailedException if anything goes wrong.
        NotificationFailedException cause;
        try {
            service.sendNotification(check, subscription, alerts);
            cause = null;
        }
        catch(NotificationFailedException e) {
            cause = e;
        }
        
        assertThat(cause, notNullValue());
        assertThat(cause.getMessage(), containsString("(code=21211)"));
    }
}
