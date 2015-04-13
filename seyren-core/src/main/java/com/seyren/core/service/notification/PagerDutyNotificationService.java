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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableMap;
import com.seyren.core.domain.Alert;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.util.config.SeyrenConfig;
import com.squareup.pagerduty.incidents.NotifyResult;
import com.squareup.pagerduty.incidents.PagerDuty;
import com.squareup.pagerduty.incidents.Resolution;
import com.squareup.pagerduty.incidents.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit.Endpoints;
import retrofit.RestAdapter;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

@Named
public class PagerDutyNotificationService implements NotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PagerDutyNotificationService.class);

    private final SeyrenConfig seyrenConfig;
    private final String baseUrl;

    @Inject
    public PagerDutyNotificationService(SeyrenConfig seyrenConfig) {
        this.seyrenConfig = seyrenConfig;
        this.baseUrl = PagerDuty.HOST;
    }

    protected PagerDutyNotificationService(SeyrenConfig seyrenConfig, String baseUrl) {
        this.seyrenConfig = seyrenConfig;
        this.baseUrl = baseUrl;
    }

    @Override
    public void sendNotification(Check check, Subscription subscription, List<Alert> alerts) throws NotificationFailedException {
        PagerDuty pagerDuty = PagerDuty.create(subscription.getTarget(), new RestAdapter.Builder()
                .setEndpoint(Endpoints.newFixedEndpoint(baseUrl))
                .build());
        NotifyResult result = null;

        try {
            if (check.getState() == AlertType.ERROR || check.getState() == AlertType.WARN) {
                Trigger trigger = new Trigger.Builder("Check '" + check.getName() + "' has exceeded its threshold.")
                        .withIncidentKey(incidentKey(check))
                        .client("Seyren")
                        .clientUrl(url(check))
                        .addDetails(details(check, alerts))
                        .build();
                result = pagerDuty.notify(trigger);
            } else if (check.getState() == AlertType.OK) {
                Resolution resolution = new Resolution.Builder(incidentKey(check))
                        .withDescription("Check '" + check.getName() + "' has been resolved.")
                        .addDetails(details(check, alerts))
                        .build();
                result = pagerDuty.notify(resolution);
            } else {
                LOGGER.warn("Did not send notification to PagerDuty for check in state: {}", check.getState());
            }
        } catch (Exception e) {
            throw new NotificationFailedException("Failed to send notification to PagerDuty", e);
        }

        if (result != null && !"success".equals(result.status())) {
            throw new NotificationFailedException("Failed to send notification to PagerDuty: '" + result.status() + "', " + result.message());
        }
    }

    private Map<String, String> details(Check check, List<Alert> alerts) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        return ImmutableMap.<String, String>builder().
                put("CHECK", mapper.writeValueAsString(check)).
                put("STATE", check.getState().name()).
                put("ALERTS", mapper.writeValueAsString(alerts)).
                put("SEYREN_URL", seyrenConfig.getBaseUrl()).
                build();
    }

    private String incidentKey(Check check) {
        return "MonitoringAlerts_" + check.getId();
    }

    private String url(Check check) {
        return seyrenConfig.getBaseUrl() + "/#/checks/" + check.getId();
    }

    @Override
    public boolean canHandle(SubscriptionType subscriptionType) {
        return subscriptionType == SubscriptionType.PAGERDUTY;
    }

}
