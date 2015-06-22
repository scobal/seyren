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
import java.util.List;

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
			if (check.getState() == AlertType.ERROR || check.getState() == AlertType.WARN) {
		    	CreateAlertRequest request = new CreateAlertRequest();
		    	request.setApiKey(apiKey);
		    	request.setMessage(getMessage(check));
		    	request.setSource("Seyren");
		    	request.setTeams(config.getOpsGenieTeams());
		
		    	CreateAlertResponse response = client.alert().createAlert(request);
		    	assert response.isSuccess();
            } else if (check.getState() == AlertType.OK) {
            	com.ifountain.opsgenie.client.model.beans.Alert alert = findAlert(client, apiKey, getMessage(check));
            
            	CloseAlertRequest request = new CloseAlertRequest();
            	request.setId(alert.getId());
            	request.setApiKey(apiKey);
            	CloseAlertResponse response = client.alert().closeAlert(request);
            	assert response.isSuccess();
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

	private com.ifountain.opsgenie.client.model.beans.Alert findAlert(OpsGenieClient client, String apiKey, String message) throws OpsGenieClientException, IOException, ParseException {
		ListAlertsRequest request = new ListAlertsRequest();
		request.setApiKey(apiKey);

		ListAlertsResponse response = client.alert().listAlerts(request);
		List<com.ifountain.opsgenie.client.model.beans.Alert> alerts = response.getAlerts();
		for (com.ifountain.opsgenie.client.model.beans.Alert alert: alerts) {
			if (alert.getMessage().equals(message)) {
				return alert;
			}
		}
		return null;
	}

	private String getMessage(Check check) {
		return "Check '" + check.getName() + "' has exceeded its threshold.";
	}

    @Override
    public boolean canHandle(SubscriptionType subscriptionType) {
        return subscriptionType == SubscriptionType.OPSGENIE;
    }

}
