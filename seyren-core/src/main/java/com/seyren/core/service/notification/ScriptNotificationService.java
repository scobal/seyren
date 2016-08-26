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


import com.google.gson.Gson;
import com.seyren.core.domain.Alert;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.util.config.SeyrenConfig;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@Named
public class ScriptNotificationService implements NotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptNotificationService.class);
    private final SeyrenConfig seyrenConfig;

    @Inject
    public ScriptNotificationService(SeyrenConfig seyrenConfig) {
        this.seyrenConfig = seyrenConfig;
    }
    @Override
    public void sendNotification(Check check, Subscription subscription, List<Alert> alerts) throws NotificationFailedException {
	    if (check.getState() == AlertType.ERROR) {
			String hostPosition = subscription.getPosition();
			
			// Check for a valid position
			if (hostPosition == null) {
				LOGGER.info("No hostname position for subscription: {}", subscription.getId());
				throw new NotificationFailedException("Invalid subscription; script has no hostname position");
			}
			
	        LOGGER.info("Check#{}, Script Location: {}", check.getId(), seyrenConfig.getScriptPath());
	    	for(Alert alert: alerts) {
	    		try {
	    			String hostname = getHostName(alert, hostPosition);
	    			String resourceUrl = subscription.getTarget();
	    			ProcessBuilder pb = new ProcessBuilder(seyrenConfig.getScriptType(), seyrenConfig.getScriptPath(), hostname, new Gson().toJson(check), seyrenConfig.getBaseUrl(), resourceUrl);
	                LOGGER.info("Check#{}, Script Start", check.getId());
	                Process p = pb.start();
	                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
	                String line;
	                while ((line = reader.readLine()) != null) {
	                    LOGGER.info("Check#{}, {}" check.getId(), line);
	                    //System.out.println(line);
	                }
	                LOGGER.info("Check#{}, Script End", check.getId());
	    		}
	    		catch (Exception e) {
	                LOGGER.error("Check#{}, Script could not be sent: {}", check.getId(), e);
	                throw new NotificationFailedException("Could not send message through the script");
	    		}
	    	}
	    }
    }

    private String getHostName(Alert alert,String hostPosition) {
        int pos = Integer.parseInt(hostPosition);
        //LOGGER.info("******* hostPostion found : "+pos);
        String[] target = alert.getTarget().split("\\.");
        String hostname = target[pos-1];
        //LOGGER.info("******* Retuning Hostname " +hostname); 
        return hostname;
    }

    @Override
    public boolean canHandle(SubscriptionType subscriptionType) {
        return (subscriptionType == SubscriptionType.SCRIPT && SystemUtils.IS_OS_LINUX);
    }
}
