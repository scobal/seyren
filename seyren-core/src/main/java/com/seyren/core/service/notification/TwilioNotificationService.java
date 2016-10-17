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
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seyren.core.domain.Alert;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.util.config.SeyrenConfig;

@Named
public class TwilioNotificationService implements NotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TwilioNotificationService.class);
    
    private final SeyrenConfig seyrenConfig;
    
    @Inject
    public TwilioNotificationService(SeyrenConfig seyrenConfig) {
        this.seyrenConfig = seyrenConfig;
    }
    
    @Override
    public void sendNotification(Check check, Subscription subscription, List<Alert> alerts) throws NotificationFailedException {
       String twilioUrl = StringUtils.trimToNull(seyrenConfig.getTwilioUrl());
        
        if (twilioUrl == null) {
            LOGGER.warn("Twilio URL needs to be set before sending notifications to Twilio");
            return;
        }
        
        String body;
        if (check.getState() == AlertType.ERROR) {
            body = "ERROR Check " + check.getName() + " has exceeded its threshold.";
        } else if (check.getState() == AlertType.OK) {
            body = "OK Check " + check.getName() + " has been resolved.";
        } else if (check.getState() == AlertType.WARN) {
            body = "WARN Check " + check.getName() + " has exceeded its threshold.";
        } else {
            LOGGER.warn("Did not send notification to Twilio for check in state: {}", check.getState());
            body = null;
        }        
        
        List<NameValuePair> params=new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("To", subscription.getTarget()));
        params.add(new BasicNameValuePair("From", seyrenConfig.getTwilioPhoneNumber()));
        params.add(new BasicNameValuePair("Body", body));

        HttpClient client = HttpClientBuilder.create().useSystemProperties().build();
        
        HttpPost post = new HttpPost(twilioUrl + "/"+seyrenConfig.getTwilioAccountSid()+"/Messages");
        try {
            String credentials=seyrenConfig.getTwilioAccountSid()+":"+seyrenConfig.getTwilioAuthToken();
            post.setHeader(new BasicHeader("Authorization", "Basic "+Base64.encodeBase64String(credentials.getBytes("UTF-8"))));
            
            HttpEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
            post.setEntity(entity);
            
            HttpResponse response=client.execute(post);
            if(response.getStatusLine().getStatusCode()/100 != 2)
                throw new IOException("API request failed: "+response.getStatusLine());
        } catch (IOException e) {
            throw new NotificationFailedException("Sending notification to Twilio at " + twilioUrl + " failed.", e);
        } finally {
            HttpClientUtils.closeQuietly(client);
        }
    }
    
    @Override
    public boolean canHandle(SubscriptionType subscriptionType) {
        return subscriptionType == SubscriptionType.TWILIO;
    }
}
