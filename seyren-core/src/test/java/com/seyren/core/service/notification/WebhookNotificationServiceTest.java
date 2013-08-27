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

import com.fasterxml.jackson.databind.JsonNode;
import static com.github.restdriver.Matchers.hasJsonPath;
import com.github.restdriver.clientdriver.ClientDriverRequest.Method;
import com.github.restdriver.clientdriver.ClientDriverRule;
import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import com.github.restdriver.clientdriver.capture.BodyCapture;
import com.github.restdriver.clientdriver.capture.JsonBodyCapture;
import com.seyren.core.domain.*;
import com.seyren.core.util.config.SeyrenConfig;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class WebhookNotificationServiceTest {
    
    private SeyrenConfig mockSeyrenConfig;
    private NotificationService service;
    
    @Rule
    public ClientDriverRule clientDriver = new ClientDriverRule();
    
    @Before
    public void before() {
        mockSeyrenConfig = mock(SeyrenConfig.class);
        service = new WebhookNotificationService(mockSeyrenConfig);
    }
    
    @Test
    public void notifcationServiceCanOnlyHandleWebHookSubscription() {
        assertThat(service.canHandle(SubscriptionType.WEBHOOK), is(true));
        for (SubscriptionType type : SubscriptionType.values()) {
            if (type == SubscriptionType.WEBHOOK) {
                continue;
            }
            assertThat(service.canHandle(type), is(false));
        }
    }
    
    @Test
    public void checkingOutTheHappyPath() {
        
        String seyrenUrl = clientDriver.getBaseUrl() + "/seyren";
        
        when(mockSeyrenConfig.getGraphiteUrl()).thenReturn(clientDriver.getBaseUrl() + "/graphite");
        when(mockSeyrenConfig.getBaseUrl()).thenReturn(seyrenUrl);
        
        Check check = new Check()
                .withEnabled(true)
                .withName("check-name")
                .withTarget("statsd.metric.name")
                .withState(AlertType.ERROR)
                .withWarn(BigDecimal.ONE)
                .withError(BigDecimal.TEN);
        
        Subscription subscription = new Subscription()
                .withType(SubscriptionType.WEBHOOK)
                .withTarget(clientDriver.getBaseUrl() + "/myWebHook/thatdoesstuff");
        
        Alert alert = new Alert()
                .withTarget("the.target.name")
                .withValue(BigDecimal.valueOf(12))
                .withWarn(BigDecimal.valueOf(5))
                .withError(BigDecimal.valueOf(10))
                .withFromType(AlertType.WARN)
                .withToType(AlertType.ERROR);
        
        List<Alert> alerts = Arrays.asList(alert);
        
        BodyCapture<JsonNode> bodyCapture = new JsonBodyCapture();
        
        clientDriver.addExpectation(
                onRequestTo("/myWebHook/thatdoesstuff")
                        .withMethod(Method.POST)
                        .capturingBodyIn(bodyCapture),
                giveResponse("success", "text/plain"));
        
        service.sendNotification(check, subscription, alerts);
        
        JsonNode node = bodyCapture.getContent();
        
        
        assertThat(node, hasJsonPath("$.seyrenUrl", is(seyrenUrl)));
        assertThat(node, hasJsonPath("$.check.name", is("check-name")));
        assertThat(node, hasJsonPath("$.check.state", is("ERROR")));
        assertThat(node, hasJsonPath("$.alerts", hasSize(1)));
        assertThat(node, hasJsonPath("$.alerts[0].target", is("the.target.name")));
        assertThat(node, hasJsonPath("$.alerts[0].value", is(12)));
        assertThat(node, hasJsonPath("$.alerts[0].warn", is(5)));
        assertThat(node, hasJsonPath("$.alerts[0].error", is(10)));
        assertThat(node, hasJsonPath("$.alerts[0].fromType", is("WARN")));
        assertThat(node, hasJsonPath("$.alerts[0].toType", is("ERROR")));
        assertThat(node, hasJsonPath("$.preview", startsWith("<br />")));
        assertThat(node, hasJsonPath("$.preview", containsString(check.getTarget())));
        
        verify(mockSeyrenConfig).getGraphiteUrl();
        verify(mockSeyrenConfig).getBaseUrl();
        
    }
    
}