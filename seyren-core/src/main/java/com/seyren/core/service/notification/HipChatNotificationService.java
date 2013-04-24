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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.LoggerFactory;

import com.seyren.core.domain.Alert;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.util.config.SeyrenConfig;

@Named
public class HipChatNotificationService implements NotificationService {
    
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(HipChatNotificationService.class);
    
    private final SeyrenConfig seyrenConfig;
    private final String baseUrl;
    
    @Inject
    public HipChatNotificationService(SeyrenConfig seyrenConfig) {
        this.seyrenConfig = seyrenConfig;
        this.baseUrl = "https://api.hipchat.com";
    }
    
    protected HipChatNotificationService(SeyrenConfig seyrenConfig, String baseUrl) {
        this.seyrenConfig = seyrenConfig;
        this.baseUrl = baseUrl;
    }
    
    @Override
    public void sendNotification(Check check, Subscription subscription, List<Alert> alerts) throws NotificationFailedException {
        String token = seyrenConfig.getHipChatAuthToken();
        String from = seyrenConfig.getHipChatUsername();
        String[] roomIds = subscription.getTarget().split(",");
        try {
            if (check.getState() == AlertType.ERROR) {
                 String message = getHipChatMessage(check);                  
                sendMessage(message, MessageColor.RED, roomIds, from, token, true);
            } else if (check.getState() == AlertType.WARN) {
                String message = getHipChatMessage(check);
                sendMessage(message, MessageColor.YELLOW, roomIds, from, token, true);
            } else if (check.getState() == AlertType.OK) {
                String message = getHipChatMessage(check);
                sendMessage(message, MessageColor.GREEN, roomIds, from, token, true);
            } else {
                LOGGER.warn("Did not send notification to HipChat for check in state: {}", check.getState());
            }
        } catch (Exception e) {
            throw new NotificationFailedException("Failed to send notification to HipChat", e);
        }
    }

    private String getHipChatMessage(Check check) {
        String message = "Check <a href=" + seyrenConfig.getBaseUrl() + "/#/checks/" + check.getId() + ">" + check.getName() + "</a> has entered its " + check.getState().toString() + " state.";
        return message;
    }     
    
    private void sendMessage(String message, MessageColor color, String[] roomIds, String from, String authToken, boolean notify) {
        for (String roomId : roomIds) {
            LOGGER.info("Posting: {} to {}: {} {}", from, roomId, message, color);
            HttpClient client = new DefaultHttpClient();
            String url = baseUrl + "/v1/rooms/message";
            HttpPost post = new HttpPost(url);
            
            try {
                List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
                parameters.add(new BasicNameValuePair("auth_token", authToken));
                parameters.add(new BasicNameValuePair("from", from));
                parameters.add(new BasicNameValuePair("room_id", roomId));
                parameters.add(new BasicNameValuePair("message", message));
                parameters.add(new BasicNameValuePair("color", color.name().toLowerCase()));
                if (notify) {
                    parameters.add(new BasicNameValuePair("notify", "1"));
                }
                post.setEntity(new UrlEncodedFormEntity(parameters));
                client.execute(post);
            } catch (Exception e) {
                LOGGER.warn("Error posting to HipChat", e);
            } finally {
                post.releaseConnection();
            }
        }
    }
    
    @Override
    public boolean canHandle(SubscriptionType subscriptionType) {
        return subscriptionType == SubscriptionType.HIPCHAT;
    }
    
    private enum MessageColor {
        YELLOW, RED, GREEN, PURPLE, RANDOM;
    }
}
