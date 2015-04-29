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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seyren.core.domain.*;
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.util.config.SeyrenConfig;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

@Named
public class VictorOpsNotificationService implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VictorOpsNotificationService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final SeyrenConfig seyrenConfig;

    @Inject
    public VictorOpsNotificationService(SeyrenConfig seyrenConfig) {
        this.seyrenConfig = seyrenConfig;
    }

    @Override
    public void sendNotification(Check check, Subscription subscription, List<Alert> alerts) throws NotificationFailedException {

        String httpUrl = StringUtils.trimToNull(subscription.getTarget());

        if (httpUrl == null) {
            LOGGER.warn("VictorOps REST API endpoint needs to be set before sending notifications");
            return;
        }

        Map<String, Object> body = new HashMap<String, Object>();
        body.put("entity_id", check.getId());
        body.put("message_type", MessageType.fromAlertType(check.getState()).name());
        body.put("state_message", getDescription(check));

        HttpClient client = HttpClientBuilder.create().build();

        HttpPost post = new HttpPost(subscription.getTarget());
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
        return subscriptionType == SubscriptionType.VICTOROPS;
    }

    private String getDescription(Check check) {
        String message = "Check <a href=" + seyrenConfig.getBaseUrl() + "/#/checks/" + check.getId() + ">" + check.getName() + "</a> has entered its " + check.getState().toString() + " state.";
        return message;
    }

    @VisibleForTesting
    protected enum MessageType {
        INFO, WARNING, ACKNOWLEDGEMENT, CRITICAL, RECOVERY;

        public static MessageType fromAlertType(AlertType alert) {
            switch (alert) {
                case OK:
                    return RECOVERY;
                case WARN:
                case UNKNOWN:
                    return WARNING;
                case ERROR:
                case EXCEPTION:
                    return CRITICAL;
            }
            return INFO;
        }
    }
}