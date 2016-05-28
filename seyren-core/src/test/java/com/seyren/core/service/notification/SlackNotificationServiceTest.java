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

import static com.github.restdriver.clientdriver.RestClientDriver.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

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
    private NotificationService notificationService;
    private SeyrenConfig mockSeyrenConfig;

    @Rule
    public ClientDriverRule clientDriver = new ClientDriverRule();

    @Before
    public void before() {
        mockSeyrenConfig = mock(SeyrenConfig.class);
        when(mockSeyrenConfig.getBaseUrl()).thenReturn(clientDriver.getBaseUrl() + "/slack");
        when(mockSeyrenConfig.getSlackEmojis()).thenReturn("");
        when(mockSeyrenConfig.getSlackIconUrl()).thenReturn("");
        when(mockSeyrenConfig.getSlackToken()).thenReturn("");
        when(mockSeyrenConfig.getSlackUsername()).thenReturn("Seyren");
        notificationService = new SlackNotificationService(mockSeyrenConfig, clientDriver.getBaseUrl());
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
    public void basicSlackTest() {
        BigDecimal value = new BigDecimal("1.0");

        Check check = new Check()
                .withId("123")
                .withEnabled(true)
                .withName("test-check")
                .withState(AlertType.ERROR);
        Subscription subscription = new Subscription()
                .withEnabled(true)
                .withType(SubscriptionType.SLACK)
                .withTarget("target");
        Alert alert = new Alert()
                .withValue(value)
                .withTimestamp(new DateTime())
                .withFromType(AlertType.OK)
                .withToType(AlertType.ERROR);
        List<Alert> alerts = Arrays.asList(alert);

        StringBodyCapture bodyCapture = new StringBodyCapture();

        clientDriver.addExpectation(
                onRequestTo("/api/chat.postMessage")
                        .withMethod(ClientDriverRequest.Method.POST)
                        .capturingBodyIn(bodyCapture)
                        .withHeader("accept", "application/json"),
                giveEmptyResponse());

        notificationService.sendNotification(check, subscription, alerts);

        String content = bodyCapture.getContent();
        System.out.println(decode(content));

        assertThat(content, Matchers.containsString("token="));
        assertThat(content, Matchers.containsString("&channel=target"));
        assertThat(content, not(Matchers.containsString(encode("<!channel>"))));
        assertThat(content, Matchers.containsString(encode("*ERROR* test-check")));
        assertThat(content, Matchers.containsString(encode("/#/checks/123")));
        assertThat(content, Matchers.containsString("&username=Seyren"));
        assertThat(content, Matchers.containsString("&icon_url="));

        verify(mockSeyrenConfig).getSlackEmojis();
        verify(mockSeyrenConfig).getSlackIconUrl();
        verify(mockSeyrenConfig).getSlackToken();
        verify(mockSeyrenConfig).getSlackUsername();
        verify(mockSeyrenConfig).getBaseUrl();
    }

    @Test
    public void mentionChannelWhenTargetContainsExclamationTest() {
        BigDecimal value = new BigDecimal("1.0");

        Check check = new Check()
                .withId("123")
                .withEnabled(true)
                .withName("test-check")
                .withState(AlertType.ERROR);
        Subscription subscription = new Subscription()
                .withEnabled(true)
                .withType(SubscriptionType.SLACK)
                .withTarget("target!");
        Alert alert = new Alert()
                .withValue(value)
                .withTimestamp(new DateTime())
                .withFromType(AlertType.OK)
                .withToType(AlertType.ERROR);
        List<Alert> alerts = Arrays.asList(alert);

        StringBodyCapture bodyCapture = new StringBodyCapture();

        clientDriver.addExpectation(
                onRequestTo("/api/chat.postMessage")
                        .withMethod(ClientDriverRequest.Method.POST)
                        .capturingBodyIn(bodyCapture)
                        .withHeader("accept", "application/json"),
                giveEmptyResponse());

        notificationService.sendNotification(check, subscription, alerts);

        String content = bodyCapture.getContent();
        System.out.println(decode(content));

        assertThat(content, Matchers.containsString("token="));
        assertThat(content, Matchers.containsString("&channel=target"));
        assertThat(content, Matchers.containsString(encode("<!channel>")));
        assertThat(content, Matchers.containsString(encode("*ERROR* test-check")));
        assertThat(content, Matchers.containsString(encode("/#/checks/123")));
        assertThat(content, Matchers.containsString("&username=Seyren"));
        assertThat(content, Matchers.containsString("&icon_url="));

        verify(mockSeyrenConfig).getSlackEmojis();
        verify(mockSeyrenConfig).getSlackIconUrl();
        verify(mockSeyrenConfig).getSlackToken();
        verify(mockSeyrenConfig).getSlackUsername();
        verify(mockSeyrenConfig).getBaseUrl();
    }

    String encode(String data) {
        try {
            return URLEncoder.encode(data, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    String decode(String data) {
        try {
            return URLDecoder.decode(data, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

}
