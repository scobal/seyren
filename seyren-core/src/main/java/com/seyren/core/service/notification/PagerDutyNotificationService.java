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

import biz.neustar.pagerduty.InternalException;
import biz.neustar.pagerduty.InvalidEventException;
import biz.neustar.pagerduty.PagerDutyClient;
import biz.neustar.pagerduty.model.EventResponse;
import com.seyren.core.domain.Alert;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.util.config.SeyrenConfig;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Named;

@Named
public class PagerDutyNotificationService implements NotificationService {

    private final SeyrenConfig seyrenConfig;
    private PagerDutyClient client;
    
    @Override
    public void sendNotification(Check check, Subscription subscription, List<Alert> alerts) throws NotificationFailedException {
                
        client = new PagerDutyClient(seyrenConfig.getPagerDutyDomain(), "username", "password");
        try {        
            Map details = AddDetailsToNotification(check, alerts);
            EventResponse response = null;        
            if (check.getState()== AlertType.ERROR)
                response = client.trigger(subscription.getTarget(), "Check " + check.getName() + " has exceeded its threshold.  " + seyrenConfig.getBaseUrl() + "/#/checks/" + check.getId(), "MonitoringAlerts_" + check.getId(), details);
            else
                response = client.resolve(subscription.getTarget(), "Check " + check.getName() + " has been resolved. " + seyrenConfig.getBaseUrl() + "/#/checks/" + check.getId(), "MonitoringAlerts_" + check.getId(), details);
            } 
        catch (Exception e) {
            throw new NotificationFailedException("Failed to send notification to PagerDuty", e);
        }         
    }
    
    @Inject
    public PagerDutyNotificationService(SeyrenConfig seyrenConfig) {
        this.seyrenConfig = seyrenConfig;
    }
    
    private Map AddDetailsToNotification(Check check, List<Alert> alerts) {
        Map details = new HashMap();
        details.put("CHECK", check);
        details.put("ALERTS", alerts);
        details.put("SEYREN_URL", seyrenConfig.getBaseUrl());
        return details;
    }

    @Override
    public void sendStatusEmail(List<Check> checks) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
