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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
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
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        MAPPER.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    }

    @Override
    public void sendNotification(Check check, Subscription subscription, List<Alert> alerts) throws NotificationFailedException {

        String victorOpsRestEndpoint = seyrenConfig.getVictorOpsRestEndpoint();
        String victorOpsRoutingKey = StringUtils.defaultIfEmpty(subscription.getTarget(), "default");

        if (victorOpsRestEndpoint == null) {
            LOGGER.warn("VictorOps REST API endpoint needs to be set before sending notifications");
            return;
        }

        URI victorOpsUri = null;
        try {
            victorOpsUri = new URI(victorOpsRestEndpoint).resolve(new URI(victorOpsRoutingKey));
        } catch (URISyntaxException use) {
            LOGGER.warn("Invalid endpoint is given.");
            return;
        }

        HttpClient client = HttpClientBuilder.create().useSystemProperties().build();
        HttpPost post = new HttpPost(victorOpsUri);
        try {
            HttpEntity entity = new StringEntity(getDescription(check, alerts), ContentType.APPLICATION_JSON);
            post.setEntity(entity);
            HttpResponse response = client.execute(post);
            HttpEntity responseEntity = response.getEntity();
            if(responseEntity!=null) {
                LOGGER.info("Response : {} ", EntityUtils.toString(responseEntity));
            }
        } catch (Exception e) {
            throw new NotificationFailedException("Failed to send notification to VictorOps", e);
        } finally {
            post.releaseConnection();
            HttpClientUtils.closeQuietly(client);
        }
    }

    @Override
    public boolean canHandle(SubscriptionType subscriptionType) {
        return subscriptionType == SubscriptionType.VICTOROPS;
    }

    private String getDescription(Check check, List<Alert> alerts) throws JsonProcessingException {
        MAPPER.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        StringBuilder message = new StringBuilder("Check: ").append(check.getDescription())
                .append("\nQuery: ").append(check.getTarget());

        // to keep message small, Only show the first alert in this check.
        if (!alerts.isEmpty()) {
            message.append("\n").append(MAPPER.writeValueAsString(alerts.get(0)));
        }
        message.append("\n").append(url(check));

        Map<String, String> body = ImmutableMap.<String, String>builder().
                put("entity_id", check.getId()).
                put("entity_display_name", check.getName()).
                put("message_type", MessageType.fromAlertType(check.getState()).name()).
                put("state_message", message.toString()).
                put("monitoring_tool", "Seyren").
                build();
        return MAPPER.writeValueAsString(body);
    }

    private String url(Check check) {
        return seyrenConfig.getBaseUrl() + "/#/checks/" + check.getId();
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
                default:
                    return INFO;
            }
        }
    }
}
