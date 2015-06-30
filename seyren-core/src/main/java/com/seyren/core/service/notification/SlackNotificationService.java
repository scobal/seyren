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

import javax.inject.Inject;
import javax.inject.Named;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.seyren.core.domain.Alert;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.util.config.SeyrenConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
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

import static com.google.common.collect.Iterables.transform;

@Named
public class SlackNotificationService implements NotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SlackNotificationService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final SeyrenConfig seyrenConfig;
    private String baseUrl;

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
        String targetUrl = subscription.getTarget();

        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        try {
            URL target = new URL(targetUrl);
            String query = target.getQuery();
            this.baseUrl = targetUrl.replace(query, "");
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        String channel = query_pairs.get("channel");
        String username = query_pairs.get("username");

        String url = this.baseUrl;
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);
        post.addHeader("accept", "application/json");

        Map<String, Object> body = new HashMap<String, Object>();
        body.put("channel", "#" + StringUtils.removeEnd(channel == null ? "dev-ops" : "datascience-ops", "!"));
        body.put("text", formatContent(check, subscription, alerts));
        body.put("username", username == null ? "Seyren" : username);
        body.put("icon_emoji", ":seyren:");

        try {
            HttpEntity entity = new StringEntity(MAPPER.writeValueAsString(body), ContentType.APPLICATION_JSON);
            post.setEntity(entity);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.info("> parameters: {}", MAPPER.writeValueAsBytes(body));
            }
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

    private String formatContent(Check check, Subscription subscription, List<Alert> alerts) {
        List<String> emojis = new ArrayList<String>();
        String url = String.format("%s/#/checks/%s", seyrenConfig.getBaseUrl(), check.getId());
        String alertsString = Joiner.on("\n").join(transform(alerts, new Function<Alert, String>() {
            @Override
            public String apply(Alert input) {
                return String.format("%s = %s (%s to %s)", input.getTarget(), input.getValue().toString(), input.getFromType(), input.getToType());
            }
        }));

        String channel = subscription.getTarget().contains("!") ? "<!channel>" : "";

        String description;
        if (StringUtils.isNotBlank(check.getDescription())) {
            description = String.format("\n> %s", check.getDescription());
        } else {
            description = "";
        }

        final String state = check.getState().toString();

        return String.format("%s*%s* %s [%s]%s\n```\n%s\n```\n#%s %s",
                Iterables.get(emojis, check.getState().ordinal(), ""),
                state,
                check.getName(),
                url,
                description,
                alertsString,
                state.toLowerCase(Locale.getDefault()),
                channel
        );
    }
}
