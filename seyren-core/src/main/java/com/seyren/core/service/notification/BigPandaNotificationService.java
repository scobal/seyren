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
public class BigPandaNotificationService implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BigPandaNotificationService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final SeyrenConfig seyrenConfig;

    @Inject
    public BigPandaNotificationService(SeyrenConfig seyrenConfig) {
        this.seyrenConfig = seyrenConfig;
    }

    @Override
    public void sendNotification(Check check, Subscription subscription, List<Alert> alerts) throws NotificationFailedException {

        String bigPandaAppKey = StringUtils.trimToNull(subscription.getTarget());
        if (bigPandaAppKey == null) {
            LOGGER.warn("BigPanda Integration App Key in Subscription Target needs to be set before sending notifications to BigPanda");
            return;
        }

        for (Alert alert : alerts) {

            String level = alert.getToType().toString();
            String bigPandaStatus;
            if(level == "ERROR") {
                bigPandaStatus = "critical";
            } else if(level == "WARN") {
                bigPandaStatus = "warning";
            } else if(level == "OK") {
                bigPandaStatus = "ok";
            } else {
                LOGGER.warn("Unknown level {} specified in alert. Can't send to BigPanda.", level);
                return;
            }

            String checkUrl = seyrenConfig.getBaseUrl() + "/#/checks/" + check.getId();
            Long tstamp = alert.getTimestamp().getMillis() / 1000;

            Map<String, Object> body = new HashMap<String, Object>();
            body.put("app_key", bigPandaAppKey);
            body.put("status", bigPandaStatus);
            body.put("service", check.getName());
            body.put("check", alert.getTarget());
            body.put("description", check.getDescription());
            body.put("timestamp", tstamp);
            body.put("seyrenCheckUrl", checkUrl);
            body.put("currentValue", alert.getValue());
            body.put("thresholdWarning", alert.getWarn());
            body.put("thresholdCritical", alert.getError());
            body.put("previewGraph", getPreviewImageUrl(check));

            HttpClient client = HttpClientBuilder.create().useSystemProperties().build();
            HttpPost post;

            if(StringUtils.isNotBlank(seyrenConfig.getBigPandaNotificationUrl())) {
                post = new HttpPost(seyrenConfig.getBigPandaNotificationUrl());
            } else {
                LOGGER.warn("BigPanda API URL in Seyren Config needs to be set before sending notifications to BigPanda");
                return;
            }

            String authBearer;
            if(StringUtils.isNotBlank(seyrenConfig.getBigPandaAuthBearer())) {
                authBearer = seyrenConfig.getBigPandaAuthBearer();
            } else {
                LOGGER.warn("BigPanda Auth Bearer in Seyren Config needs to be set before sending notifications to BigPanda");
                return;
            }

            try {
                post.addHeader("Authorization", "Bearer " + authBearer);
                HttpEntity entity = new StringEntity(MAPPER.writeValueAsString(body), ContentType.APPLICATION_JSON);
                post.setEntity(entity);
                LOGGER.info("Sending alert to BigPanda (AppKey: {}, Check: {}, Target: {}, Status: {})",
                    bigPandaAppKey,
                    check.getName(),
                    alert.getTarget(),
                    bigPandaStatus);
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
    }

    @Override
    public boolean canHandle(SubscriptionType subscriptionType) {
        return subscriptionType == SubscriptionType.BIGPANDA;
    }

    private String getPreviewImageUrl(Check check)
    {
        return seyrenConfig.getGraphiteUrl() + "/render/?target=" + check.getTarget() + "&from=-1h" +
                         "&target=alias(dashed(color(constantLine(" + check.getWarn().toString() + "),%22yellow%22)),%22warn%20level%22)&target=alias(dashed(color(constantLine(" + check.getError().toString()
                        + "),%22red%22)),%22error%20level%22)&width=500&height=225";

    }

}
