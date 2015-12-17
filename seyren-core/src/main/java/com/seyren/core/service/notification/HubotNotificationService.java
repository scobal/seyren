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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seyren.core.domain.Alert;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.util.config.SeyrenConfig;

@Named
public class HubotNotificationService implements NotificationService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HubotNotificationService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    private final SeyrenConfig seyrenConfig;
    
    @Inject
    public HubotNotificationService(SeyrenConfig seyrenConfig) {
        this.seyrenConfig = seyrenConfig;
    }
    
    @Override
    public void sendNotification(Check check, Subscription subscription, List<Alert> alerts) throws NotificationFailedException {
        String hubotUrl = StringUtils.trimToNull(seyrenConfig.getHubotUrl());
        
        if (hubotUrl == null) {
            LOGGER.warn("Hubot URL needs to be set before sending notifications to Hubot");
            return;
        }
        
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("seyrenUrl", seyrenConfig.getBaseUrl());
        body.put("check", check);
        body.put("subscription", subscription);
        body.put("alerts", alerts);
        body.put("rooms", subscription.getTarget().split(","));
        
        HttpClient client = HttpClientBuilder.create().useSystemProperties().build();
        
        HttpPost post = new HttpPost(hubotUrl + "/seyren/alert");
        try {
            HttpEntity entity = new StringEntity(MAPPER.writeValueAsString(body), ContentType.APPLICATION_JSON);
            post.setEntity(entity);
            client.execute(post);
        } catch (IOException e) {
            throw new NotificationFailedException("Sending notification to Hubot at " + hubotUrl + " failed.", e);
        } finally {
            HttpClientUtils.closeQuietly(client);
        }
    }
    
    @Override
    public boolean canHandle(SubscriptionType subscriptionType) {
        return subscriptionType == SubscriptionType.HUBOT;
    }
    
}
