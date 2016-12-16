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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
        Map<String,Object> payload = new HashMap<String, Object>();
        payload.put("channel", subscription.getTarget());
        payload.put("username", seyrenConfig.getSlackUsername());
        payload.put("icon_url", seyrenConfig.getSlackIconUrl());
        payload.put("attachments", formatForWebhook(check, subscription, alerts));
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
        String alertsString = formatAlertsForText(alerts);
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
    
    private List<Map<String,Object>> formatForWebhook(Check check, Subscription subscription, List<Alert> alerts) {
    	List<Map<String,Object>> attachments = new ArrayList<Map<String,Object>>();
    	for (Alert alert: alerts) { 
    		Map<String,Object> attachment = new HashMap<String,Object>();
    		attachment.put("mrkdwn_in", Arrays.asList("fields", "text", "pretext"));
    		attachment.put("fallback", String.format("An alert has been triggered for '%s'",check.getName()));
    		attachment.put("color", formatColor(check));
    		attachment.put("title", check.getName());
    		attachment.put("title_link", formatCheckUrl(check));
    		attachment.put("fields", formatAlertForWebhook(check, alert));
    		attachments.add(attachment);
    	}
    	return attachments;
    }

    private String formatAlertsForText(List<Alert> alerts) {
    	return Joiner.on("\n").join(transform(alerts, new Function<Alert, String>() {
			@Override
			public String apply(Alert input) {     
				return String.format("%s = %s (%s to %s)", input.getTarget(), input.getValue().toString(), input.getFromType(), input.getToType());
			}
    	}));
    }
    
    private List<Map<String,Object>> formatAlertForWebhook(Check check, Alert alert) {
		List<Map<String,Object>> fields = new ArrayList<Map<String,Object>>();
		
		if (check.getDescription() != null && !check.getDescription().isEmpty()) {
			Map<String,Object> description = new HashMap<String,Object>();
			description.put("title", "Description");
			description.put("value", check.getDescription());
			description.put("short", false);
			fields.add(description);
		}
		
		Map<String,Object> trigger = new HashMap<String,Object>();
		trigger.put("title", "Trigger");
		trigger.put("value", String.format("`%s = %s`", alert.getTarget(), alert.getValue().toString()));
		trigger.put("short", false);
		fields.add(trigger);
		
		Map<String,Object> from = new HashMap<String,Object>();
		from.put("title", "From");
		from.put("value", alert.getFromType().toString());
		from.put("short", true);
		fields.add(from);
		
		Map<String,Object> to = new HashMap<String,Object>();
		to.put("title", "To");
		to.put("value",  alert.getToType().toString());
		to.put("short",  true);
		fields.add(to);
		
		return fields;
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
    
    private List<String> extractEmojis() {
	    return Lists.newArrayList(
	            Splitter.on(',').omitEmptyStrings().trimResults().split(seyrenConfig.getSlackEmojis())
	    );
	}
	
    private String formatCheckUrl(Check check) {
    	return String.format("%s/#/checks/%s", seyrenConfig.getBaseUrl(), check.getId());
    }
      
    private String formatColor(Check check) {
    	switch(check.getState()) {
		case ERROR:
			return seyrenConfig.getSlackDangerColor();
		case EXCEPTION:
			return seyrenConfig.getSlackExceptionColor();
		case OK:
			return seyrenConfig.getSlackGoodColor();
		case UNKNOWN:
			return seyrenConfig.getSlackUnknownColor();
		case WARN:
			return seyrenConfig.getSlackWarningColor();
		default:
			return seyrenConfig.getSlackUnknownColor();
    	}
    }
}
