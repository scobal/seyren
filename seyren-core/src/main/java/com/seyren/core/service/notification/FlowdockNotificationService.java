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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.seyren.core.domain.Alert;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.util.config.SeyrenConfig;

@Named
public class FlowdockNotificationService implements NotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlowdockNotificationService.class);
    
    private final SeyrenConfig seyrenConfig;
    private final String baseUrl;
    
    @Inject
    public FlowdockNotificationService(SeyrenConfig seyrenConfig) {
        this.seyrenConfig = seyrenConfig;
        this.baseUrl = "https://api.flowdock.com";
    }
    
    protected FlowdockNotificationService(SeyrenConfig seyrenConfig, String baseUrl) {
        this.seyrenConfig = seyrenConfig;
        this.baseUrl = baseUrl;
    }
    
    @Override
    public void sendNotification(Check check, Subscription subscription, List<Alert> alerts) throws NotificationFailedException {
        String token = subscription.getTarget();
        String externalUsername = seyrenConfig.getFlowdockExternalUsername();
        
        List<String> tags = Lists.newArrayList(
                Splitter.on(',').omitEmptyStrings().trimResults().split(seyrenConfig.getFlowdockTags())
                );
        List<String> emojis = Lists.newArrayList(
                Splitter.on(',').omitEmptyStrings().trimResults().split(seyrenConfig.getFlowdockEmojis())
                );
        
        String url = String.format("%s/v1/messages/chat/%s", baseUrl, token);
        HttpClient client = HttpClientBuilder.create().useSystemProperties().build();
        HttpPost post = new HttpPost(url);
        post.addHeader("Content-Type", "application/json");
        post.addHeader("accept", "application/json");
        
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> dataToSend = new HashMap<String, Object>();
        dataToSend.put("content", formatContent(emojis, check, subscription, alerts));
        dataToSend.put("external_user_name", externalUsername);
        dataToSend.put("tags", formatTags(tags, check, subscription, alerts));
        
        try {
            String data = StringEscapeUtils.unescapeJava(mapper.writeValueAsString(dataToSend));
            post.setEntity(new StringEntity(data, APPLICATION_JSON));
            client.execute(post);
        } catch (Exception e) {
            LOGGER.warn("Error posting to Flowdock", e);
        } finally {
            post.releaseConnection();
            HttpClientUtils.closeQuietly(client);
        }
        
    }
    
    @Override
    public boolean canHandle(SubscriptionType subscriptionType) {
        return subscriptionType == SubscriptionType.FLOWDOCK;
    }
    
    private String formatContent(List<String> emojis, Check check, Subscription subscription, List<Alert> alerts) {
        String url = String.format("%s/#/checks/%s", seyrenConfig.getBaseUrl(), check.getId());
        String alertsString = Joiner.on(", ").join(transform(alerts, new Function<Alert, String>() {
            @Override
            public String apply(Alert input) {
                return String.format("%s: %s", input.getTarget(), input.getValue().toString());
            }
        }));
        return String.format("%s %s has entered its %s state - [%s] - %s - %s",
                Iterables.get(emojis, check.getState().ordinal(), ""),
                check.getName(),
                check.getState().toString(),
                alertsString,
                Iterables.getFirst(alerts, null).getTimestamp(),
                url
                );
    }
    
    private ImmutableList<Object> formatTags(List<String> tags, Check check, Subscription subscription, List<Alert> alerts) {
        return ImmutableList.builder().add(check.getState().toString()).addAll(tags).build();
    }
}
