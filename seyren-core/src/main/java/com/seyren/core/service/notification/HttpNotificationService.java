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


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    
    @Inject
    public HttpNotificationService(SeyrenConfig seyrenConfig) {
        this.seyrenConfig = seyrenConfig;
    }
    
    @Override
    public void sendNotification(Check check, Subscription subscription, List<Alert> alerts) throws NotificationFailedException {
        
        String httpUrl = StringUtils.trimToNull(subscription.getTarget());
        
        if (httpUrl == null) {
            LOGGER.warn("URL needs to be set before sending notifications to HTTP");
            return;
        }
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("seyrenUrl", seyrenConfig.getBaseUrl());
        body.put("check", check);
        body.put("subscription", subscription);
        body.put("alerts", alerts);        
        body.put("preview", getPreviewImage(check)); 
        
        HttpClient client = HttpClientBuilder.create().useSystemProperties().build();
        HttpPost post;

        if(StringUtils.isNotBlank(seyrenConfig.getHttpNotificationUrl())) {
            post = new HttpPost(seyrenConfig.getHttpNotificationUrl());
        } else {
            post = new HttpPost(subscription.getTarget());
        }

        try {
            HttpEntity entity = new StringEntity(MAPPER.writeValueAsString(body), ContentType.APPLICATION_JSON);
            post.setEntity(entity);
            HttpResponse response = client.execute(post);
            HttpEntity responseEntity = response.getEntity();
            if(responseEntity!=null) {
                LOGGER.info("Response : {} ", EntityUtils.toString(responseEntity));
            }
        } catch (Exception e) {
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
   
    private String getPreviewImage(Check check)
    {
        return "<br /><img src=" + seyrenConfig.getGraphiteUrl() + "/render/?target=" + check.getTarget() + getTimeFromUntilString(new Date()) +
                         "&target=alias(dashed(color(constantLine(" + check.getWarn().toString() + "),%22yellow%22)),%22warn%20level%22)&target=alias(dashed(color(constantLine(" + check.getError().toString() 
                        + "),%22red%22)),%22error%20level%22)&width=500&height=225></img>"; 
                
    }
       
    private String getTimeFromUntilString(Date date)
    {        
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm_yyyyMMdd");
        cal.setTime(date);
        cal.add(Calendar.HOUR, -1);
        String from = format.format(cal.getTime());
        cal.add(Calendar.HOUR, 1);
        String until = format.format(cal.getTime());

        return "&from=" + until + "&until=" + from;
    }
    
}
