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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.seyren.core.service.notification;

import com.seyren.core.domain.AlertType;
import com.seyren.core.util.config.SeyrenConfig;
import javax.inject.Inject;
import javax.inject.Named;
import com.seyren.core.domain.Check;
import com.seyren.core.store.ChecksStore;
import org.joda.time.DateTime;

/**
 *
 * @author DVrede
 */
@Named
public class SeyrenNotificationSettings implements NotificationServiceSettings {
    private final SeyrenConfig seyrenConfig;
    private final ChecksStore checksStore;
    
    @Inject
    public SeyrenNotificationSettings(SeyrenConfig seyrenConfig, ChecksStore checkStore) {
        this.seyrenConfig = seyrenConfig;
        this.checksStore = checkStore;
    }
    
    @Override
    public boolean applyNotificationDelayAndIntervalProperties(Check check, AlertType lastState, AlertType currentState, DateTime now) {
        Boolean notificationShouldBeSent = false;
        long delayInSeconds;
        long seyrenNotificationIntervalInSeconds;
        long timeElapsedSinceFirstErrorOccured;

        // Check if state changed into ERROR save timestamp
        if (!stateIsTheSame(lastState, currentState) && currentState == AlertType.ERROR) {
            check.setTimeFirstErrorOccured(now);
            checksStore.updateTimeFirstErrorOccured(check.getId(), now);
        }
        
        // Set Seyren global delay and interval or Check specific delay and interval
        timeElapsedSinceFirstErrorOccured = (now.getMillis() - check.getTimeFirstErrorOccured().getMillis()) / 1000;
        seyrenNotificationIntervalInSeconds = seyrenConfig.getAlertNotificationIntervalInSeconds();
        delayInSeconds = seyrenConfig.getAlertNotificationDelayInSeconds();

        if (check.getNotificationDelay() != null) {
            delayInSeconds = check.getNotificationDelay().longValue();
        }
        
        if (check.getNotificationInterval() != null) {
            seyrenNotificationIntervalInSeconds = check.getNotificationInterval().longValue();            
        }      
        
        // State is still error and must exist longer than delayInSeconds
        if (stateIsTheSame(lastState, currentState) && currentState == AlertType.ERROR && timeElapsedSinceFirstErrorOccured > delayInSeconds) {    
            long timeSinceLastNotificationInSeconds = check.getTimeLastNotificationSent() == null ? seyrenNotificationIntervalInSeconds : (now.getMillis() - check.getTimeLastNotificationSent().getMillis()) / 1000;

            // Time since the first error is not longer ago than the interval and no notification has been sent
            if (timeElapsedSinceFirstErrorOccured < seyrenNotificationIntervalInSeconds && check.getTimeLastNotificationSent() == null) {
                check.setTimeLastNotificationSent(now);
                checksStore.updateTimeLastNotification(check.getId(), now);
                notificationShouldBeSent = true;
            }
            
            // Last notification is also greater than interval and first notification has been sent
            if (timeSinceLastNotificationInSeconds > seyrenNotificationIntervalInSeconds) {
                check.setTimeLastNotificationSent(now);
                checksStore.updateTimeLastNotification(check.getId(), now);
                notificationShouldBeSent = true;                
            }
        }

        // Also send notification if the state changes from ERROR to OK
        if (currentState == AlertType.OK && lastState == AlertType.ERROR && timeElapsedSinceFirstErrorOccured > delayInSeconds) {
            notificationShouldBeSent = true;
            check.setTimeLastNotificationSent(null);
        }

        return notificationShouldBeSent;                
    }
    
    private boolean stateIsTheSame(AlertType last, AlertType current) {
        return last == current;
    }    
}
