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

import com.seyren.core.domain.*;
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.util.config.SeyrenConfig;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.LoggerFactory;

@Named
public class HipChatNotificationService implements NotificationService {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(HipChatNotificationService.class);
    private final SeyrenConfig seyrenConfig;

    private String host = "api.hipchat.com";     
    private String from;

    @Inject
    public HipChatNotificationService(SeyrenConfig seyrenConfig) {
        this.seyrenConfig = seyrenConfig;
    }

    @Override
    public void sendNotification(Check check, Subscription subscription, List<Alert> alerts) throws NotificationFailedException {
        String token = seyrenConfig.getHipChatAuthToken();
        String from = seyrenConfig.getHipChatUserName();
        String[] roomIds = subscription.getTarget().split(",");
        try {          
            if (check.getState() == AlertType.ERROR) {
                String message = "Check <a href="+ seyrenConfig.getBaseUrl() + "/#/checks/" + check.getId() + ">" + check.getName() + "</a> has exceeded its error threshold value. Please investigate.";
                SendMessage(message, MessageColor.RED, roomIds, from, token, true);
            } else if (check.getState() == AlertType.OK) {
                String message = "Check <a href="+ seyrenConfig.getBaseUrl() + "/#/checks/" + check.getId() + ">" + check.getName() + "</a> is back up.";
                SendMessage(message, MessageColor.GREEN, roomIds, from, token, true);
            } else {
                LOGGER.warn("Did not send notification to HipChat for check in state: " + check.getState());
            }
        } catch (Exception e) {
            throw new NotificationFailedException("Failed to send notification to HipChat", e);
        }
    }      
        
    public void SendMessage(String message, MessageColor color, String[] roomIds, String from, String authToken, boolean notify) {
        for (String roomId : roomIds) {
            LOGGER.info("Posting: " + from + " to " + roomId + ": " + message + " " + color);
            HttpClient client = new HttpClient();
            String url = "https://" + host + "/v1/rooms/message?auth_token=" + authToken;
            PostMethod post = new PostMethod(url);

            try {
                post.addParameter("from", from);
                post.addParameter("room_id", roomId);
                post.addParameter("message", message);
                post.addParameter("color", color.name().toLowerCase());
                if (notify)
                {
                    post.addParameter("notify", "1");
                }
                post.getParams().setContentCharset("UTF-8");
                client.executeMethod(post);
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
    
    public enum MessageColor
    {
        YELLOW, RED, GREEN, PURPLE, RANDOM;
    }
}


