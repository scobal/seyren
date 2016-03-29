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

import static com.github.restdriver.clientdriver.RestClientDriver.giveEmptyResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.restdriver.clientdriver.ClientDriverRequest;
import com.github.restdriver.clientdriver.ClientDriverRule;
import com.github.restdriver.clientdriver.capture.StringBodyCapture;
import com.seyren.core.domain.Alert;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.util.config.SeyrenConfig;

public class SlackNotificationServiceTest {
    private static final String USERNAME = "Seyren";
    private static final String SLACK_URI_TO_POST = "/services/SOMETHING/ANOTHERTHING/FINALTHING";

    private NotificationService notificationService;
    private SeyrenConfig mockSeyrenConfig;

    @Rule
    public ClientDriverRule clientDriver = new ClientDriverRule();

    @Before
    public void before() {
        mockSeyrenConfig = mock(SeyrenConfig.class);
        when(mockSeyrenConfig.getBaseUrl()).thenReturn(clientDriver.getBaseUrl() + "/slack");
        when(mockSeyrenConfig.getSlackWebhook()).thenReturn(clientDriver.getBaseUrl() + SLACK_URI_TO_POST);
        when(mockSeyrenConfig.getSlackEmojis()).thenReturn("");
        when(mockSeyrenConfig.getSlackIconUrl()).thenReturn("");
        when(mockSeyrenConfig.getSlackUsername()).thenReturn(USERNAME);
        notificationService = new SlackNotificationService(mockSeyrenConfig);
    }

    @After
    public void after() {
        System.setProperty("SLACK_USERNAME", "");
    }

    @Test
    public void notifcationServiceCanOnlyHandleSlackSubscription() {
        assertThat(notificationService.canHandle(SubscriptionType.SLACK), is(true));
        for (SubscriptionType type : SubscriptionType.values()) {
            if (type == SubscriptionType.SLACK) {
                continue;
            }
            assertThat(notificationService.canHandle(type), is(false));
        }
    }

    @Test
    public void basicSlackTest() throws JsonParseException, JsonMappingException, IOException {
	// Given
        Check check = givenCheck();

        Subscription subscription = givenSubsciption();

        Alert alert = givenAlert();
        List<Alert> alerts = Arrays.asList(alert);

        StringBodyCapture bodyCapture = new StringBodyCapture();

        clientDriver.addExpectation(
                onRequestTo(SLACK_URI_TO_POST)
                        .withMethod(ClientDriverRequest.Method.POST)
                        .capturingBodyIn(bodyCapture)
                        .withHeader("accept", "application/json"),
                giveEmptyResponse());

        // When
        notificationService.sendNotification(check, subscription, alerts);

        // Then
        String content = bodyCapture.getContent();

        Map<String,String> map = new HashMap<String,String>();
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<HashMap<String,Object>> typeRef = new TypeReference<HashMap<String,Object>>() {};
        map = mapper.readValue(content, typeRef);

        assertThat(map.get("channel"), Matchers.is(subscription.getTarget()));
        assertThat(map.get("text"), Matchers.containsString("*" + check.getState().name() + "* "));
        assertThat(map.get("text"), Matchers.containsString("/#/checks/" + check.getId()));
        assertThat(map.get("text"), Matchers.containsString(check.getName()));
        assertThat(map.get("username"), Matchers.is(USERNAME));
        assertThat(map.get("icon_url"), Matchers.isEmptyString());

        verify(mockSeyrenConfig).getSlackWebhook();
        verify(mockSeyrenConfig).getSlackEmojis();
        verify(mockSeyrenConfig).getSlackIconUrl();
        verify(mockSeyrenConfig).getSlackUsername();
        verify(mockSeyrenConfig).getBaseUrl();
    }

    Check givenCheck() {
        Check check = new Check()
                .withId("123")
                .withEnabled(true)
                .withName("test-check")
                .withState(AlertType.ERROR);
        return check;
    }

    Subscription givenSubsciption() {
	Subscription subscription = new Subscription()
                .withEnabled(true)
                .withType(SubscriptionType.SLACK)
                .withTarget("target");
	return subscription;
    }

    Alert givenAlert() {
	Alert alert = new Alert()
                .withValue(new BigDecimal("1.0"))
                .withTimestamp(new DateTime())
                .withFromType(AlertType.OK)
                .withToType(AlertType.ERROR);
	return alert;
    }

}
