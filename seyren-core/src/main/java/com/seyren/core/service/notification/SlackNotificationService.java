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

import static com.google.common.collect.Iterables.*;
import static org.apache.http.entity.ContentType.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.seyren.core.domain.Alert;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.util.config.SeyrenConfig;

@Named
public class SlackNotificationService implements NotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SlackNotificationService.class);

    private final SeyrenConfig seyrenConfig;
    private final String baseUrl;

    @Inject
    public SlackNotificationService(SeyrenConfig seyrenConfig) {
        this.seyrenConfig = seyrenConfig;
        this.baseUrl = "https://slack.com";
    }

    protected SlackNotificationService(SeyrenConfig seyrenConfig, String baseUrl) {
        this.seyrenConfig = seyrenConfig;
        this.baseUrl = baseUrl;
    }

    @Override
    public void sendNotification(Check check, Subscription subscription, List<Alert> alerts) throws NotificationFailedException {
        String token = seyrenConfig.getSlackToken();
        String channel = subscription.getTarget();
        String username = seyrenConfig.getSlackUsername();
        String iconUrl = seyrenConfig.getSlackIconUrl();

        List<String> emojis = Lists.newArrayList(
                Splitter.on(',').omitEmptyStrings().trimResults().split(seyrenConfig.getSlackEmojis())
                );

        String url = String.format("%s/api/chat.postMessage", baseUrl);
        HttpClient client = HttpClientBuilder.create().build();

        List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
        parameters.add(new BasicNameValuePair("token", token));
        parameters.add(new BasicNameValuePair("channel", channel));
        parameters.add(new BasicNameValuePair("text", formatContent(emojis, check, subscription, alerts)));
        parameters.add(new BasicNameValuePair("username", username));
        parameters.add(new BasicNameValuePair("icon_url", iconUrl));

        HttpPost post = new HttpPost(url+"?"+URLEncodedUtils.format(parameters, "UTF-8"));
        post.addHeader("accept", "application/json");

        try {
            HttpResponse response = client.execute(post);
            if (LOGGER.isDebugEnabled()) {
              LOGGER.debug("Status: {}, Body: {}", response.getStatusLine(), new BasicResponseHandler().handleResponse(response));
            }
        } catch (Exception e) {
            LOGGER.warn("Error posting to Slack", e);
        } finally {
            post.releaseConnection();
            HttpClientUtils.closeQuietly(client);
        }

    }

    @Override
    public boolean canHandle(SubscriptionType subscriptionType) {
        return subscriptionType == SubscriptionType.SLACK;
    }

    private String formatContent(List<String> emojis, Check check, Subscription subscription, List<Alert> alerts) {
        String url = String.format("%s/#/checks/%s", seyrenConfig.getBaseUrl(), check.getId());
        String alertsString = Joiner.on(", ").join(transform(alerts, new Function<Alert, String>() {
            @Override
            public String apply(Alert input) {
                return String.format("%s: %s", input.getTarget(), input.getValue().toString());
            }
        }));
        return String.format("[%s] %s %s has entered its %s state - [%s] - %s",
            Iterables.getFirst(alerts, null).getTimestamp(),
            Iterables.get(emojis, check.getState().ordinal(), ""),
            check.getName(),
            check.getState().toString(),
            alertsString,
            url
        );
    }
}
