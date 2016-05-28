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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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
    private static final String USERNAME = "Seyren";
    private static final String CONTENT_ENCODING = "ISO-8859-1";

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
        when(mockSeyrenConfig.getSlackUsername()).thenReturn(USERNAME);
        notificationService = new SlackNotificationService(mockSeyrenConfig, clientDriver.getBaseUrl());
    }

    @After
    public void after() {
        System.setProperty("SLACK_USERNAME", "");
    }

    @Test
    public void notificationServiceCanOnlyHandleSlackSubscription() {
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
        // Given
        Check check = givenCheck();
        Subscription subscription = givenSlackSubscriptionWithTarget("target");
        Alert alert = givenAlert();

        List<Alert> alerts = Arrays.asList(alert);

        StringBodyCapture bodyCapture = new StringBodyCapture();

        clientDriver.addExpectation(
                onRequestTo("/api/chat.postMessage")
                        .withMethod(ClientDriverRequest.Method.POST)
                        .capturingBodyIn(bodyCapture)
                        .withHeader("accept", "application/json"),
                giveEmptyResponse());

        // When
        notificationService.sendNotification(check, subscription, alerts);

        // Then
        String content = bodyCapture.getContent();
        System.out.println(decode(content));

        assertContent(content, check, subscription);
        assertThat(content, containsString("&channel=" + subscription.getTarget()));
        assertThat(content, not(containsString(encode("<!channel>"))));

        verify(mockSeyrenConfig).getSlackEmojis();
        verify(mockSeyrenConfig).getSlackIconUrl();
        verify(mockSeyrenConfig).getSlackToken();
        verify(mockSeyrenConfig).getSlackUsername();
        verify(mockSeyrenConfig).getBaseUrl();
    }

    @Test
    public void mentionChannelWhenTargetContainsExclamationTest() {
        //Given
        Check check = givenCheck();
        Subscription subscription = givenSlackSubscriptionWithTarget("target!");
        Alert alert = givenAlert();

        List<Alert> alerts = Arrays.asList(alert);

        StringBodyCapture bodyCapture = new StringBodyCapture();

        clientDriver.addExpectation(
                onRequestTo("/api/chat.postMessage")
                        .withMethod(ClientDriverRequest.Method.POST)
                        .capturingBodyIn(bodyCapture)
                        .withHeader("accept", "application/json"),
                giveEmptyResponse());

        // When
        notificationService.sendNotification(check, subscription, alerts);

        // Then
        String content = bodyCapture.getContent();
        System.out.println(decode(content));

        assertContent(content, check, subscription);
        assertThat(content, containsString("&channel=" + StringUtils.removeEnd(subscription.getTarget(), "!")));
        assertThat(content, containsString(encode("<!channel>")));

        verify(mockSeyrenConfig).getSlackEmojis();
        verify(mockSeyrenConfig).getSlackIconUrl();
        verify(mockSeyrenConfig).getSlackToken();
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

    Subscription givenSlackSubscriptionWithTarget(String target) {
	Subscription subscription = new Subscription()
                .withEnabled(true)
                .withType(SubscriptionType.SLACK)
                .withTarget(target);
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

    private void assertContent(String content, Check check, Subscription subscription) {
      assertThat(content, containsString("token="));
      assertThat(content, containsString(encode("*" + check.getState().name() + "* " + check.getName())));
      assertThat(content, containsString(encode("/#/checks/" + check.getId())));
      assertThat(content, containsString("&username=" + USERNAME));
      assertThat(content, containsString("&icon_url="));
    }

    String encode(String data) {
        try {
            return URLEncoder.encode(data, CONTENT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    String decode(String data) {
        try {
            return URLDecoder.decode(data, CONTENT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

}
