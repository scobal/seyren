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
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.restdriver.Matchers.hasJsonPath;
import static com.github.restdriver.clientdriver.RestClientDriver.giveEmptyResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PagerDutyNotificationServiceTest {
    private NotificationService notificationService;
    private SeyrenConfig mockSeyrenConfig;
    @Rule
    public ClientDriverRule clientDriver = new ClientDriverRule();

    @Before
    public void before() {
        mockSeyrenConfig = mock(SeyrenConfig.class);
        when(mockSeyrenConfig.getBaseUrl()).thenReturn(clientDriver.getBaseUrl() + "/pagerduty");
        notificationService = new PagerDutyNotificationService(mockSeyrenConfig, clientDriver.getBaseUrl());
    }
    
    @After
    public void after() {
    }
    
    @Test
    public void notifcationServiceCanOnlyHandlePagerDutySubscription() {
        assertThat(notificationService.canHandle(SubscriptionType.PAGERDUTY), is(true));
        for (SubscriptionType type : SubscriptionType.values()) {
            if (type == SubscriptionType.PAGERDUTY) {
                continue;
            }
            assertThat(notificationService.canHandle(type), is(false));
        }
    }
    
    @Test
    public void triggerPagerDutyTest() {
        Check check = new Check()
                .withId("123")
                .withEnabled(true)
                .withName("test-check")
                .withState(AlertType.ERROR);
        Subscription subscription = new Subscription()
                .withEnabled(true)
                .withType(SubscriptionType.PAGERDUTY)
                .withTarget("servicekey123");
        Alert alert = new Alert()
                .withId("890")
                .withValue(new BigDecimal("1.0"))
                .withTimestamp(new DateTime())
                .withFromType(AlertType.OK)
                .withToType(AlertType.ERROR);
        List<Alert> alerts = Arrays.asList(alert);

        BodyCapture<JsonNode> bodyCapture = new JsonBodyCapture();

        clientDriver.addExpectation(
                onRequestTo("/generic/2010-04-15/create_event.json")
                        .withMethod(ClientDriverRequest.Method.POST)
                        .capturingBodyIn(bodyCapture)
                        .withHeader("content-Type", "application/json"),
                giveEmptyResponse());

        notificationService.sendNotification(check, subscription, alerts);

        JsonNode node = bodyCapture.getContent();

        assertThat(node, hasJsonPath("$.service_key", is("servicekey123")));
        assertThat(node, hasJsonPath("$.incident_key", is("MonitoringAlerts_123")));
        assertThat(node, hasJsonPath("$.event_type", is("trigger")));
        assertThat(node, hasJsonPath("$.description", is("Check 'test-check' has exceeded its threshold.")));
        assertThat(node, hasJsonPath("$.client", is("Seyren")));
        assertThat(node, hasJsonPath("$.client_url", containsString("/#/checks/123")));
        assertThat(node, hasJsonPath("$.details.CHECK", containsString("{\"id\":\"123\",\"name\":\"test-check\",")));
        assertThat(node, hasJsonPath("$.details.ALERTS", containsString("{\"id\":\"890\",")));
        assertThat(node, hasJsonPath("$.details.SEYREN_URL", containsString("/pagerduty")));
    }

    @Test
    public void resolutionPagerDutyTest() {
        Check check = new Check()
                .withId("123")
                .withEnabled(true)
                .withName("test-check")
                .withState(AlertType.OK);
        Subscription subscription = new Subscription()
                .withEnabled(true)
                .withType(SubscriptionType.PAGERDUTY)
                .withTarget("servicekey123");
        List<Alert> alerts = new ArrayList<Alert>();

        BodyCapture<JsonNode> bodyCapture = new JsonBodyCapture();

        clientDriver.addExpectation(
                onRequestTo("/generic/2010-04-15/create_event.json")
                        .withMethod(ClientDriverRequest.Method.POST)
                        .capturingBodyIn(bodyCapture)
                        .withHeader("content-Type", "application/json"),
                giveEmptyResponse());

        notificationService.sendNotification(check, subscription, alerts);

        JsonNode node = bodyCapture.getContent();

        assertThat(node, hasJsonPath("$.service_key", is("servicekey123")));
        assertThat(node, hasJsonPath("$.incident_key", is("MonitoringAlerts_123")));
        assertThat(node, hasJsonPath("$.event_type", is("resolve")));
        assertThat(node, hasJsonPath("$.description", is("Check 'test-check' has been resolved.")));
        assertThat(node, hasJsonPath("$.details.CHECK", containsString("{\"id\":\"123\",\"name\":\"test-check\",")));
        assertThat(node, hasJsonPath("$.details.SEYREN_URL", containsString("/pagerduty")));
    }
}
