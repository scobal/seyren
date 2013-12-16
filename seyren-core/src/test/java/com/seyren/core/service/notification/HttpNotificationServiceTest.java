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

import static com.github.restdriver.Matchers.*;
import static com.github.restdriver.clientdriver.RestClientDriver.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.restdriver.clientdriver.ClientDriverRequest.Method;
import com.github.restdriver.clientdriver.ClientDriverRule;
import com.github.restdriver.clientdriver.capture.BodyCapture;
import com.github.restdriver.clientdriver.capture.JsonBodyCapture;
import com.seyren.core.domain.Alert;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.util.config.SeyrenConfig;

public class HttpNotificationServiceTest {
    
    private SeyrenConfig mockSeyrenConfig;
    private NotificationService service;
    
    @Rule
    public ClientDriverRule clientDriver = new ClientDriverRule();
    
    @Before
    public void before() {
        mockSeyrenConfig = mock(SeyrenConfig.class);
        service = new HttpNotificationService(mockSeyrenConfig);
    }
    
    @Test
    public void notifcationServiceCanOnlyHandleHttpSubscription() {
        assertThat(service.canHandle(SubscriptionType.HTTP), is(true));
        for (SubscriptionType type : SubscriptionType.values()) {
            if (type == SubscriptionType.HTTP) {
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
                .withType(SubscriptionType.HTTP)
                .withTarget(clientDriver.getBaseUrl() + "/myendpoint/thatdoesstuff");
        
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
                onRequestTo("/myendpoint/thatdoesstuff")
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
        assertThat(node, hasJsonPath("$.preview", Matchers.startsWith("<br />")));
        assertThat(node, hasJsonPath("$.preview", containsString(check.getTarget())));
        
        verify(mockSeyrenConfig).getGraphiteUrl();
        verify(mockSeyrenConfig).getBaseUrl();
        
    }
    
}