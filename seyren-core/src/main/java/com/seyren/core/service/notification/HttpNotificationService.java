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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seyren.core.util.http.HttpHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seyren.core.domain.Alert;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.util.config.SeyrenConfig;

@Named
public class HttpNotificationService implements NotificationService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpNotificationService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final SeyrenConfig seyrenConfig;
    private final HttpHelper httpHelper;
    
    @Inject
    public HttpNotificationService(SeyrenConfig seyrenConfig, HttpHelper httpHelper) {
        this.seyrenConfig = seyrenConfig;
        this.httpHelper = httpHelper;
    }
    
    @Override
    public void sendNotification(Check check, Subscription subscription, List<Alert> alerts) throws NotificationFailedException {
        
        String httpUrl = StringUtils.trimToNull(subscription.getTarget());
        
        if (httpUrl == null) {
            LOGGER.warn("URL needs to be set before sending notifications to HTTP");
            return;
        }
        HttpClient client = HttpClientBuilder.create().build();
        
        HttpPost post = new HttpPost(subscription.getTarget());
        try {
            HttpEntity entity = new StringEntity(httpHelper.createHttpContent(check, subscription, alerts), ContentType.APPLICATION_JSON);

            post.setEntity(entity);
            HttpResponse response = client.execute(post);
            HttpEntity responseEntity = response.getEntity();
            if(responseEntity!=null) {
                LOGGER.info("Response : {} ", EntityUtils.toString(responseEntity));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new NotificationFailedException("Failed to send notification to HTTP", e);
        } finally {
            post.releaseConnection();
            HttpClientUtils.closeQuietly(client);
        }
    }
    
    @Override
    public boolean canHandle(SubscriptionType subscriptionType) {
        return subscriptionType == SubscriptionType.HTTP;
    }
   

       

    
}