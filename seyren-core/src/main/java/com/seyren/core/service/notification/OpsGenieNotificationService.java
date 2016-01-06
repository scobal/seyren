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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.alert.CloseAlertRequest;
import com.ifountain.opsgenie.client.model.alert.CloseAlertResponse;
import com.ifountain.opsgenie.client.model.alert.CreateAlertRequest;
import com.ifountain.opsgenie.client.model.alert.CreateAlertResponse;
import com.ifountain.opsgenie.client.model.alert.ListAlertsRequest;
import com.ifountain.opsgenie.client.model.alert.ListAlertsResponse;
import com.seyren.core.domain.Alert;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.util.config.SeyrenConfig;

@Named
public class OpsGenieNotificationService implements NotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpsGenieNotificationService.class);
    private SeyrenConfig config;

    @Inject
    public OpsGenieNotificationService(SeyrenConfig config) {
        this.config = config;
    }

    @Override
    public void sendNotification(Check check, Subscription subscription, List<Alert> alerts) throws NotificationFailedException {
        OpsGenieClient client = new OpsGenieClient();
        try {
            String apiKey = subscription.getTarget();
            if (check.getState() == AlertType.WARN || check.getState() == AlertType.ERROR) {
                openAlert(client, apiKey, check);
            } else if (check.getState() == AlertType.OK) {
                closeOpenAlerts(client, apiKey, check);
            }
        } catch (OpsGenieClientException e) {
            LOGGER.warn(
                "Did not send notification to OpsGenie for check in state: {}",
                check.getState(), e);
        } catch (IOException e) {
            LOGGER.warn(
                "Did not send notification to OpsGenie for check in state: {}",
                check.getState(), e);
        } catch (ParseException e) {
            LOGGER.warn(
                "Did not send notification to OpsGenie for check in state: {}",
                check.getState(), e);
        }
    }

    private List<com.ifountain.opsgenie.client.model.beans.Alert> getOpenAlerts(OpsGenieClient client, String apiKey, Check check) throws OpsGenieClientException, IOException, ParseException {
        List<com.ifountain.opsgenie.client.model.beans.Alert> openMatchingAlerts = new ArrayList<com.ifountain.opsgenie.client.model.beans.Alert>();
        ListAlertsRequest request = new ListAlertsRequest();
        request.setApiKey(apiKey);
        ListAlertsResponse response = client.alert().listAlerts(request);
        List<com.ifountain.opsgenie.client.model.beans.Alert> alerts = response.getAlerts();
        //There could be multiple open alerts for a given check
        for (com.ifountain.opsgenie.client.model.beans.Alert alert: alerts) {
            if (String.valueOf(alert.getMessage()).equals(getMessage(check)) &&
                alert.getStatus() == com.ifountain.opsgenie.client.model.beans.Alert.Status.open) {
                openMatchingAlerts.add(alert);
            }
        }
        return openMatchingAlerts;
    }

    private void closeOpenAlerts(OpsGenieClient client, String apiKey, Check check) throws OpsGenieClientException, ParseException, IOException{
        List<com.ifountain.opsgenie.client.model.beans.Alert> opsAlerts = getOpenAlerts(client, apiKey, check);
        for(com.ifountain.opsgenie.client.model.beans.Alert alert : opsAlerts) {
            closeAlert(alert, client,apiKey,check);
        }
    }

    private void closeAlert(com.ifountain.opsgenie.client.model.beans.Alert alert, OpsGenieClient client, String apiKey, Check check) {
        try {
            CloseAlertRequest request = new CloseAlertRequest();
            request.setId(alert.getId());
            request.setApiKey(apiKey);
            CloseAlertResponse response = client.alert().closeAlert(request);
            if(!response.isSuccess())
                LOGGER.warn("Unable to close alert for check: " + check.getName());
        } catch (OpsGenieClientException e) {
            LOGGER.warn(
                "Unable to close alert for check: " + check.getName(),
                check.getState(), e);
        } catch (IOException e) {
            LOGGER.warn(
                "Unable to close alert for check: " + check.getName(),
                check.getState(), e);
        } catch (ParseException e) {
            LOGGER.warn(
                "Unable to close alert for check: " + check.getName(),
                check.getState(), e);
        }

    }

    private void openAlert(OpsGenieClient client, String apiKey, Check check) throws OpsGenieClientException, ParseException, IOException{
        CreateAlertRequest request = new CreateAlertRequest();
        request.setApiKey(apiKey);
        request.setMessage(getMessage(check));
        request.setDetails(getCheckDetails(check));
        request.setSource("Seyren");
        request.setTeams(config.getOpsGenieTeams());
        request.setDescription(String.valueOf(check.getDescription()));
        CreateAlertResponse response = client.alert().createAlert(request);
        assert response.isSuccess();
    }

    private String getMessage(Check check) {
        return "Check " + check.getName();
    }

    private Map<String,String> getCheckDetails(Check check) {
        Map<String, String> details = new HashMap<String,String>();
        details.put("Check State", check.getState().name());
        details.put("Target", check.getTarget());
        return details;
    }

    @Override
    public boolean canHandle(SubscriptionType subscriptionType) {
        return subscriptionType == SubscriptionType.OPSGENIE;
    }

}
