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

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.restdriver.clientdriver.ClientDriverRequest.Method;
import com.github.restdriver.clientdriver.ClientDriverRule;
import com.github.restdriver.clientdriver.capture.BodyCapture;
import com.github.restdriver.clientdriver.capture.JsonBodyCapture;
import com.seyren.core.domain.*;
import com.seyren.core.util.config.SeyrenConfig;

import static com.github.restdriver.Matchers.hasJsonPath;
import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VictorOpsNotificationServiceTest {

    private SeyrenConfig mockSeyrenConfig;
    private NotificationService service;

    @Rule
    public ClientDriverRule clientDriver = new ClientDriverRule();

    @Before
    public void before() {
        mockSeyrenConfig = mock(SeyrenConfig.class);
        when(mockSeyrenConfig.getVictorOpsRestEndpoint()).thenReturn(clientDriver.getBaseUrl() + "/restapi/");
        service = new VictorOpsNotificationService(mockSeyrenConfig);
    }

    @Test
    public void notifcationServiceCanOnlyHandleVictorOpsSubscription() {
        assertThat(service.canHandle(SubscriptionType.VICTOROPS), is(true));
        for (SubscriptionType type : SubscriptionType.values()) {
            if (type == SubscriptionType.VICTOROPS) {
                continue;
            }
            assertThat(service.canHandle(type), is(false));
        }
    }

    @Test
    public void checkingOutTheHappyPath() throws Exception {
        Check check = new Check()
                .withId("test-check-id")
                .withEnabled(true)
                .withName("test-check")
                .withState(AlertType.ERROR);
        Subscription subscription = new Subscription()
                .withEnabled(true)
                .withType(SubscriptionType.VICTOROPS)
                .withTarget("routingurl");
        Alert alert = new Alert()
                .withFromType(AlertType.OK)
                .withToType(AlertType.ERROR);
        List<Alert> alerts = Arrays.asList(alert);

        BodyCapture<JsonNode> bodyCapture = new JsonBodyCapture();
        clientDriver.addExpectation(
                onRequestTo("/restapi/routingurl")
                        .withMethod(Method.POST)
                        .capturingBodyIn(bodyCapture),
                giveResponse("success", "text/plain"));

        service.sendNotification(check, subscription, alerts);

        JsonNode node = bodyCapture.getContent();
        assertThat(node, hasJsonPath("$.entity_id", is(check.getId())));
        assertThat(node, hasJsonPath("$.message_type", is(VictorOpsNotificationService.MessageType.CRITICAL.name())));
    }

}
