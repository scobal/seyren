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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.neustar.pagerduty.PagerDutyClient;

import com.seyren.core.domain.Alert;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.util.config.SeyrenConfig;

@Named
public class PagerDutyNotificationService implements NotificationService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PagerDutyNotificationService.class);

    private final SeyrenConfig seyrenConfig;
    
    @Inject
    public PagerDutyNotificationService(SeyrenConfig seyrenConfig) {
        this.seyrenConfig = seyrenConfig;
    }
    
    @Override
    public void sendNotification(Check check, Subscription subscription, List<Alert> alerts) throws NotificationFailedException {
                
    	PagerDutyClient client = new PagerDutyClient(seyrenConfig.getPagerDutyDomain(), "username", "password");
        try {        
            Map<String, Object> details = createNotificationDetails(check, alerts);
            
            if (check.getState() == AlertType.ERROR) {
                client.trigger(subscription.getTarget(), "Check " + check.getName() + " has exceeded its threshold.  " + seyrenConfig.getBaseUrl() + "/#/checks/" + check.getId(), "MonitoringAlerts_" + check.getId(), details);
            } else if (check.getState() == AlertType.OK) {
                client.resolve(subscription.getTarget(), "Check " + check.getName() + " has been resolved. " + seyrenConfig.getBaseUrl() + "/#/checks/" + check.getId(), "MonitoringAlerts_" + check.getId(), details);
            } else {
            	LOGGER.warn("Did not send notification to PagerDuty for check in state: " + check.getState());
            }
        } catch (Exception e) {
            throw new NotificationFailedException("Failed to send notification to PagerDuty", e);
        }         
    }
    
	@Override
	public boolean canHandle(SubscriptionType subscriptionType) {
		return subscriptionType == SubscriptionType.PAGERDUTY;
	}
    
    private Map<String, Object> createNotificationDetails(Check check, List<Alert> alerts) {
        Map<String, Object> details = new HashMap<String, Object>();
        details.put("CHECK", check);
        details.put("ALERTS", alerts);
        details.put("SEYREN_URL", seyrenConfig.getBaseUrl());
        return details;
    }

}
