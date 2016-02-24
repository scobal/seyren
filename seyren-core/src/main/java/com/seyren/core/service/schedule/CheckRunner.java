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
package com.seyren.core.service.schedule;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.seyren.core.domain.Alert;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.service.checker.TargetChecker;
import com.seyren.core.service.checker.ValueChecker;
import com.seyren.core.service.notification.NotificationService;
import com.seyren.core.store.AlertsStore;
import com.seyren.core.store.ChecksStore;

public class CheckRunner implements Runnable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckRunner.class);
    
    private final Check check;
    private final AlertsStore alertsStore;
    private final ChecksStore checksStore;
    private final TargetChecker targetChecker;
    private final ValueChecker valueChecker;
    private final Iterable<NotificationService> notificationServices;
    
    public CheckRunner(Check check, AlertsStore alertsStore, ChecksStore checksStore, TargetChecker targetChecker, ValueChecker valueChecker,
            Iterable<NotificationService> notificationServices) {
        this.check = check;
        this.alertsStore = alertsStore;
        this.checksStore = checksStore;
        this.targetChecker = targetChecker;
        this.valueChecker = valueChecker;
        this.notificationServices = notificationServices;
    }
    
    @Override
    public final void run() {
        if (!check.isEnabled()) {
            return;
        }
        
        try {
            Map<String, Optional<BigDecimal>> targetValues = targetChecker.check(check);
            
            DateTime now = new DateTime();
            BigDecimal warn = check.getWarn();
            BigDecimal error = check.getError();
            
            AlertType worstState;
            
            if (check.isAllowNoData()) {
                worstState = AlertType.OK;
            } else {
                worstState = AlertType.UNKNOWN;
            }
            
            List<Alert> interestingAlerts = new ArrayList<Alert>();
            
            for (Entry<String, Optional<BigDecimal>> entry : targetValues.entrySet()) {
                
                String target = entry.getKey();
                Optional<BigDecimal> value = entry.getValue();

                if (!value.isPresent()) {
                    if (!check.isAllowNoData()) {
                        LOGGER.warn("No value present for {} and check must have data", target);
                    }
                    continue;
                }
                
                BigDecimal currentValue = value.get();
                
                Alert lastAlert = alertsStore.getLastAlertForTargetOfCheck(target, check.getId());
                
                AlertType lastState;
                
                if (lastAlert == null) {
                    lastState = AlertType.OK;
                } else {
                    lastState = lastAlert.getToType();
                }
                
                AlertType currentState = valueChecker.checkValue(currentValue, warn, error);
                
                if (currentState.isWorseThan(worstState)) {
                    worstState = currentState;
                }
                
                if (isStillOk(lastState, currentState)) {
                    continue;
                }
                
                Alert alert = createAlert(target, currentValue, warn, error, lastState, currentState, now);
                
                alertsStore.createAlert(check.getId(), alert);
                
                // Only notify if the alert has changed state
                if (stateIsTheSame(lastState, currentState)) {
                    continue;
                }
                
                interestingAlerts.add(alert);
                
            }

            Check updatedCheck = checksStore.updateStateAndLastCheck(check.getId(), worstState, DateTime.now());

            if (interestingAlerts.isEmpty()) {
                return;
            }
            
            for (Subscription subscription : updatedCheck.getSubscriptions()) {
                if (!subscription.shouldNotify(now, worstState)) {
                    continue;
                }
                
                for (NotificationService notificationService : notificationServices) {
                    if (notificationService.canHandle(subscription.getType())) {
                        try {
                            notificationService.sendNotification(updatedCheck, subscription, interestingAlerts);
                        } catch (Exception e) {
                            LOGGER.warn("Notifying {} by {} failed.", subscription.getTarget(), subscription.getType(), e);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            LOGGER.warn("{} failed", check.getName(), e);
        }
    }
    
    private boolean isStillOk(AlertType last, AlertType current) {
        return last == AlertType.OK && current == AlertType.OK;
    }
    
    private boolean stateIsTheSame(AlertType last, AlertType current) {
        return last == current;
    }
    
    private Alert createAlert(String target, BigDecimal value, BigDecimal warn, BigDecimal error, AlertType from, AlertType to, DateTime now) {
        return new Alert()
                .withTarget(target)
                .withValue(value)
                .withWarn(warn)
                .withError(error)
                .withFromType(from)
                .withToType(to)
                .withTimestamp(now);
    }
    
}
