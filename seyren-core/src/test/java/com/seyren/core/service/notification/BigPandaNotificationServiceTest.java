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

import org.joda.time.DateTime;

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

public class BigPandaNotificationServiceTest {

    private SeyrenConfig mockSeyrenConfig;
    private NotificationService service;

    @Rule
    public ClientDriverRule clientDriver = new ClientDriverRule();

    @Before
    public void before() {
        mockSeyrenConfig = mock(SeyrenConfig.class);
        service = new BigPandaNotificationService(mockSeyrenConfig);
    }

    @Test
    public void notifcationServiceCanOnlyHandleBigPandaSubscription() {
        assertThat(service.canHandle(SubscriptionType.BIGPANDA), is(true));
        for (SubscriptionType type : SubscriptionType.values()) {
            if (type == SubscriptionType.BIGPANDA) {
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
        when(mockSeyrenConfig.getBigPandaNotificationUrl()).thenReturn(clientDriver.getBaseUrl() + "/bigpanda/test");
        when(mockSeyrenConfig.getBigPandaAuthBearer()).thenReturn("test-auth-bearer");

        Check check = new Check()
                .withEnabled(true)
                .withName("check-name")
                .withDescription("Testing Description")
                .withTarget("the.target.name")
                .withState(AlertType.ERROR)
                .withWarn(BigDecimal.ONE)
                .withError(BigDecimal.TEN)
                .withId("testing");

        Subscription subscription = new Subscription()
                .withType(SubscriptionType.BIGPANDA)
                .withTarget("testing_app_key");

        DateTime timestamp = new DateTime(1420070400000L);

        Alert alert = new Alert()
                .withTarget("the.target.name")
                .withValue(BigDecimal.valueOf(12))
                .withWarn(BigDecimal.valueOf(5))
                .withError(BigDecimal.valueOf(10))
                .withFromType(AlertType.WARN)
                .withToType(AlertType.ERROR)
                .withTimestamp(timestamp);

        List<Alert> alerts = Arrays.asList(alert);

        BodyCapture<JsonNode> bodyCapture = new JsonBodyCapture();

        clientDriver.addExpectation(
                onRequestTo("/bigpanda/test")
                        .withMethod(Method.POST)
                        .capturingBodyIn(bodyCapture),
                giveResponse("success", "text/plain"));

        service.sendNotification(check, subscription, alerts);

        JsonNode node = bodyCapture.getContent();

        assertThat(node, hasJsonPath("$.seyrenCheckUrl", is(seyrenUrl + "/#/checks/" + check.getId())));
        assertThat(node, hasJsonPath("$.app_key", is("testing_app_key")));
        assertThat(node, hasJsonPath("$.check", is("the.target.name")));
        assertThat(node, hasJsonPath("$.status", is("critical")));
        assertThat(node, hasJsonPath("$.service", is("check-name")));
        assertThat(node, hasJsonPath("$.description", is("Testing Description")));
        assertThat(node, hasJsonPath("$.timestamp", is(1420070400L)));
        assertThat(node, hasJsonPath("$.currentValue", is(12)));
        assertThat(node, hasJsonPath("$.thresholdWarning", is(5)));
        assertThat(node, hasJsonPath("$.thresholdCritical", is(10)));
        assertThat(node, hasJsonPath("$.previewGraph", containsString("the.target.name")));
        assertThat(node, hasJsonPath("$.previewGraph", containsString("/graphite")));

    }

}
