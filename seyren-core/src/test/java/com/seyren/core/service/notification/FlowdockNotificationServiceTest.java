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
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.restdriver.clientdriver.ClientDriverRequest;
import com.github.restdriver.clientdriver.ClientDriverRule;
import com.github.restdriver.clientdriver.capture.BodyCapture;
import com.github.restdriver.clientdriver.capture.JsonBodyCapture;
import com.seyren.core.domain.Alert;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.util.config.SeyrenConfig;

public class FlowdockNotificationServiceTest {
    private NotificationService notificationService;
    private SeyrenConfig mockSeyrenConfig;
    
    @Rule
    public ClientDriverRule clientDriver = new ClientDriverRule();
    
    @Before
    public void before() {
        mockSeyrenConfig = mock(SeyrenConfig.class);
        when(mockSeyrenConfig.getBaseUrl()).thenReturn(clientDriver.getBaseUrl() + "/flowdock");
        when(mockSeyrenConfig.getFlowdockExternalUsername()).thenReturn("Seyren");
        when(mockSeyrenConfig.getFlowdockEmojis()).thenReturn("");
        when(mockSeyrenConfig.getFlowdockTags()).thenReturn("");
        notificationService = new FlowdockNotificationService(mockSeyrenConfig, clientDriver.getBaseUrl());
    }
    
    @After
    public void after() {
        System.setProperty("FLOWDOCK_EXTERNAL_USERNAME", "");
    }
    
    @Test
    public void notifcationServiceCanOnlyHandleFlowdockSubscription() {
        assertThat(notificationService.canHandle(SubscriptionType.FLOWDOCK), is(true));
        for (SubscriptionType type : SubscriptionType.values()) {
            if (type == SubscriptionType.FLOWDOCK) {
                continue;
            }
            assertThat(notificationService.canHandle(type), is(false));
        }
    }
    
    @Test
    public void basicFlowdockTest() {
        BigDecimal value = new BigDecimal("1.0");

        Check check = new Check()
                .withId("123")
                .withEnabled(true)
                .withName("test-check")
                .withState(AlertType.ERROR);
        Subscription subscription = new Subscription()
                .withEnabled(true)
                .withType(SubscriptionType.FLOWDOCK)
                .withTarget("target");
        Alert alert = new Alert()
                .withValue(value)
                .withTimestamp(new DateTime())
                .withFromType(AlertType.OK)
                .withToType(AlertType.ERROR);
        List<Alert> alerts = Arrays.asList(alert);
        
        BodyCapture<JsonNode> bodyCapture = new JsonBodyCapture();
        
        clientDriver.addExpectation(
                onRequestTo("/v1/messages/chat/target")
                        .withMethod(ClientDriverRequest.Method.POST)
                        .capturingBodyIn(bodyCapture)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("accept", "application/json"),
                giveEmptyResponse());
        
        notificationService.sendNotification(check, subscription, alerts);
        
        JsonNode node = bodyCapture.getContent();
        
        assertThat(node, hasJsonPath("$.content", containsString("test-check")));
        assertThat(node, hasJsonPath("$.content", containsString("ERROR")));
        assertThat(node, hasJsonPath("$.content", containsString(value.toString())));
        assertThat(node, hasJsonPath("$.content", containsString("/#/checks/123")));
        assertThat(node, hasJsonPath("$.external_user_name", is("Seyren")));
        assertThat(node, hasJsonPath("$.tags", hasSize(1)));
        assertThat(node, hasJsonPath("$.tags[0]", is("ERROR")));
        
        verify(mockSeyrenConfig).getFlowdockExternalUsername();
        verify(mockSeyrenConfig).getFlowdockEmojis();
        verify(mockSeyrenConfig).getFlowdockTags();
        verify(mockSeyrenConfig).getBaseUrl();
        
    }
    
}
