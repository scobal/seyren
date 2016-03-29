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

import static com.google.common.collect.Iterables.transform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
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

    @Inject
    public SlackNotificationService(SeyrenConfig seyrenConfig) {
        this.seyrenConfig = seyrenConfig;
    }

    @Override
    public void sendNotification(Check check, Subscription subscription, List<Alert> alerts) throws NotificationFailedException {
        String webhookUrl = seyrenConfig.getSlackWebhook();

        List<String> emojis = Lists.newArrayList(
                Splitter.on(',').omitEmptyStrings().trimResults().split(seyrenConfig.getSlackEmojis())
        );

        HttpClient client = HttpClientBuilder.create().useSystemProperties().build();
        HttpPost post = new HttpPost(webhookUrl);
        post.addHeader("accept", "application/json");

        try {
            String message = generateMessage(emojis, check, subscription, alerts);
            post.setEntity(new StringEntity(message, ContentType.APPLICATION_JSON));
            if (LOGGER.isDebugEnabled()) {
                LOGGER.info("> message: {}", message);
            }
            HttpResponse response = client.execute(post);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.info("> message: {}", message);
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

    private String generateMessage(List<String> emojis, Check check, Subscription subscription, List<Alert> alerts) throws JsonProcessingException {
	Map<String,String> payload = new HashMap<String, String>();
	payload.put("channel", subscription.getTarget());
	payload.put("username", seyrenConfig.getSlackUsername());
	payload.put("text", formatContent(emojis, check, subscription, alerts));
	payload.put("icon_url", seyrenConfig.getSlackIconUrl());

	String message = new ObjectMapper().writeValueAsString(payload);
	return message;
    }

    private String formatContent(List<String> emojis, Check check, Subscription subscription, List<Alert> alerts) {
        String url = String.format("%s/#/checks/%s", seyrenConfig.getBaseUrl(), check.getId());
        String alertsString = Joiner.on("\n").join(transform(alerts, new Function<Alert, String>() {
            @Override
            public String apply(Alert input) {
                return String.format("%s = %s (%s to %s)", input.getTarget(), input.getValue().toString(), input.getFromType(), input.getToType());
            }
        }));

        String description;
        if (StringUtils.isNotBlank(check.getDescription())) {
            description = String.format("\n> %s", check.getDescription());
        } else {
            description = "";
        }

        final String state = check.getState().toString();

        return String.format("%s *%s* %s (<%s|Open>)%s\n```\n%s\n```",
                Iterables.get(emojis, check.getState().ordinal(), ""),
                state,
                check.getName(),
                url,
                description,
                alertsString
        );
    }
}
