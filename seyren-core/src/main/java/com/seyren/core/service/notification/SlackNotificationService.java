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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
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
    public boolean canHandle(SubscriptionType subscriptionType) {
        return subscriptionType == SubscriptionType.SLACK;
    }

    @Override
    public void sendNotification(Check check, Subscription subscription, List<Alert> alerts) throws NotificationFailedException {
    	String token = seyrenConfig.getSlackToken();
    	String webhookUrl = seyrenConfig.getSlackWebhook();
        
        String url;
        HttpEntity entity;

        if (!webhookUrl.isEmpty()) {
            LOGGER.debug("Publishing notification using configured Webhook");
            url = webhookUrl;
            try {
                entity = createJsonEntity(check, subscription, alerts);
            } catch (JsonProcessingException e) {
                throw new NotificationFailedException("Failed to serialize message alert.", e);
            }
        } else if (!token.isEmpty()){
            LOGGER.debug("Publishing notification using slack web API");
            url = String.format("%s/api/chat.postMessage", baseUrl);
            try {
                entity = createFormEntity(check, subscription, alerts);
            } catch (UnsupportedEncodingException e) {
                throw new NotificationFailedException("Failed to serialize alert.", e);
            }
        } else {
            LOGGER.warn("No SLACK_WEBHOOK_URL or SLACK_TOKEN set. Cannot notify slack.");
            return;
        }
        
        HttpClient client = HttpClientBuilder.create().useSystemProperties().build();
        HttpPost post = new HttpPost(url);
        post.addHeader("accept", "application/json");
        
        try {
            post.setEntity(entity);
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
    
    private HttpEntity createJsonEntity(Check check, Subscription subscription, List<Alert> alerts) throws JsonProcessingException {
        Map<String,String> payload = new HashMap<String, String>();
        payload.put("channel", subscription.getTarget());
        payload.put("username", seyrenConfig.getSlackUsername());
        payload.put("text", formatForWebhook(check, subscription, alerts));
        payload.put("icon_url", seyrenConfig.getSlackIconUrl());

        String message = new ObjectMapper().writeValueAsString(payload);
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.info("> message: {}", message);
        }
        
        return new StringEntity(message, ContentType.APPLICATION_JSON);
    }

    private HttpEntity createFormEntity(Check check, Subscription subscription, List<Alert> alerts) throws UnsupportedEncodingException {
    	List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
        parameters.add(new BasicNameValuePair("token", seyrenConfig.getSlackToken()));
        parameters.add(new BasicNameValuePair("channel", StringUtils.removeEnd(subscription.getTarget(), "!")));
        parameters.add(new BasicNameValuePair("text", formatForWebApi(check, subscription, alerts)));
        parameters.add(new BasicNameValuePair("username", seyrenConfig.getSlackUsername()));
        parameters.add(new BasicNameValuePair("icon_url", seyrenConfig.getSlackIconUrl()));
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.info("> parameters: {}", parameters);
        }
        
        return new UrlEncodedFormEntity(parameters);
    }
    
    private String formatForWebApi(Check check, Subscription subscription, List<Alert> alerts) {
        String url = formatCheckUrl(check);
        String alertsString = formatAlert(alerts);

        String channel = subscription.getTarget().contains("!") ? "<!channel>" : "";

        String description = formatDescription(check);

        final String state = check.getState().toString();

        return String.format("%s*%s* %s [%s]%s\n```\n%s\n```\n#%s %s",
                Iterables.get(extractEmojis(), check.getState().ordinal(), ""),
                state,
                check.getName(),
                url,
                description,
                alertsString,
                state.toLowerCase(Locale.getDefault()),
                channel
        );
    }
    
    private String formatForWebhook(Check check, Subscription subscription, List<Alert> alerts) {
        String url = formatCheckUrl(check);
        String alertsString = formatAlert(alerts);

        String description = formatDescription(check);

        final String state = check.getState().toString();

        return String.format("%s *%s* %s (<%s|Open>)%s\n```\n%s\n```",
                Iterables.get(extractEmojis(), check.getState().ordinal(), ""),
                state,
                check.getName(),
                url,
                description,
                alertsString
        );
    }

    private List<String> extractEmojis() {
      List<String> emojis = Lists.newArrayList(
              Splitter.on(',').omitEmptyStrings().trimResults().split(seyrenConfig.getSlackEmojis())
      );
      return emojis;
    }

    private String formatCheckUrl(Check check) {
      String url = String.format("%s/#/checks/%s", seyrenConfig.getBaseUrl(), check.getId());
      return url;
    }

    private String formatAlert(List<Alert> alerts) {
      String alertsString = Joiner.on("\n").join(transform(alerts, new Function<Alert, String>() {
          @Override
          public String apply(Alert input) {
              return String.format("%s = %s (%s to %s)", input.getTarget(), input.getValue().toString(), input.getFromType(), input.getToType());
          }
      }));
      return alertsString;
    }

    private String formatDescription(Check check) {
      String description;
      if (StringUtils.isNotBlank(check.getDescription())) {
          description = String.format("\n> %s", check.getDescription());
      } else {
          description = "";
      }
      return description;
    }
}
