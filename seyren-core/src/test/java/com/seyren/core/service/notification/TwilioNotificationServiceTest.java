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

import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.github.restdriver.clientdriver.ClientDriverRequest.Method;
import com.github.restdriver.clientdriver.ClientDriverRule;
import com.github.restdriver.clientdriver.capture.BodyCapture;
import com.github.restdriver.clientdriver.capture.StringBodyCapture;
import com.seyren.core.domain.Alert;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.util.config.SeyrenConfig;

public class TwilioNotificationServiceTest {
    private SeyrenConfig mockSeyrenConfig;
    private NotificationService service;
    private ClientDriverRule clientDriver = new ClientDriverRule();
    
    @Before
    public void before() {
        mockSeyrenConfig = mock(SeyrenConfig.class);
        service = new TwilioNotificationService(mockSeyrenConfig);
    }
    
    @Test
    public void notifcationServiceCanOnlyHandleTwilioSubscription() {
        assertThat(service.canHandle(SubscriptionType.TWILIO), is(true));
        for (SubscriptionType type : SubscriptionType.values()) {
            if (type == SubscriptionType.TWILIO) {
                continue;
            }
            assertThat(service.canHandle(type), is(false));
        }
    }
    
    private static final String ACCOUNT_SID="1234";
    
    @Test
    public void checkingOutTheHappyPath() {
        String seyrenUrl = clientDriver.getBaseUrl() + "/seyren";
        
        when(mockSeyrenConfig.getTwilioUrl()).thenReturn(clientDriver.getBaseUrl() + "/twilio");
        when(mockSeyrenConfig.getTwilioAccountSid()).thenReturn(ACCOUNT_SID);
        when(mockSeyrenConfig.getTwilioAuthToken()).thenReturn("5678");
        when(mockSeyrenConfig.getTwilioPhoneNumber()).thenReturn("+11234567890");
        when(mockSeyrenConfig.getBaseUrl()).thenReturn(seyrenUrl);
        
        Check check = new Check()
                .withEnabled(true)
                .withName("check-name")
                .withState(AlertType.ERROR);
        
        Subscription subscription = new Subscription()
                .withType(SubscriptionType.TWILIO)
                .withTarget("+10987654321");
        
        Alert alert = new Alert()
                .withTarget("the.target.name")
                .withValue(BigDecimal.valueOf(12))
                .withWarn(BigDecimal.valueOf(5))
                .withError(BigDecimal.valueOf(10))
                .withFromType(AlertType.WARN)
                .withToType(AlertType.ERROR);
        
        List<Alert> alerts = Arrays.asList(alert);
        
        BodyCapture<String> bodyCapture = new StringBodyCapture();
        
        clientDriver.addExpectation(
                onRequestTo("/twilio/"+ACCOUNT_SID+"/Messages")
                        .withMethod(Method.POST)
                        .capturingBodyIn(bodyCapture),
                giveResponse("Thanks for letting me know", "text/plain"));
        
        
        // This will fail with an NotificationFailedException if anything goes wrong.
        service.sendNotification(check, subscription, alerts);
    
        String body=bodyCapture.getContent();
        
        assertThat(body, containsString("To=%2B10987654321"));
        assertThat(body, containsString("From=%2B11234567890"));
        assertThat(body, containsString("Body=ERROR+Check+check-name+has+exceeded+its+threshold"));
        
        verify(mockSeyrenConfig).getTwilioUrl();
    }
}
