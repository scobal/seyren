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
import java.util.HashMap;
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
    
    // A hashmap of last alerts by target/check
	private static final HashMap<String, Alert> lastAlerts = new HashMap<String, Alert>();
    
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
    	// If the check is not enabled, don't run it, exiting...
        if (!check.isEnabled()) {
            return;
        }
        try {
        	// Run the check
            Map<String, Optional<BigDecimal>> targetValues = targetChecker.check(check);
            // If there was a problem retrieving data from graphite, then simply don't continue processing the check
            if (check.hasRemoteServerErrorOccurred()){
            	// TODO Will we always be calling a Graphite server?  Change if you are using another service
            	LOGGER.warn("  *** Check #{} :: Will not initiate check, remote server read error occurred when calling "
            			+ "server located at: ", check.getId(), check.getGraphiteBaseUrl());
            	return;
            }
            // Get the current time - to be used for notification and alert time stamps 
            DateTime now = new DateTime();
            // Get the threshold values for the check which signify warning and error thresholds
            BigDecimal warn = check.getWarn();
            BigDecimal error = check.getError();
            AlertType worstState;
            // If the check is allowed data, initialized the state as OK, otherwise,
            // it is unknown
            if (check.isAllowNoData()) {
            	LOGGER.info("  *** Check #{} :: Initiating check, data is not allowed, setting worst state to 'OK'", check.getId());
                worstState = AlertType.OK;
            } else {
            	LOGGER.info("  *** Check #{} :: Initiating check, data is allowed, setting worst state to 'Unknown'", check.getId());
                worstState = AlertType.UNKNOWN;
            }
            // Intialize a list of alerts that represent a change in alert state from
            // the last time that the check was run
            List<Alert> interestingAlerts = new ArrayList<Alert>();
            // Get the measured values for this check from the Graphite/Noop datasource 
            // Iterate through them, to check for error/warn values
            for (Entry<String, Optional<BigDecimal>> entry : targetValues.entrySet()) {                
                String target = entry.getKey();
            	LOGGER.info("        Check #{}, Target #{} :: Evaluating value target.", check.getId(), target);
                Optional<BigDecimal> value = entry.getValue();
                // If there is no value in the entry, move to the next one
                if (!value.isPresent()) {
                    LOGGER.warn("        Check #{}, Target #{} :: No value present.", check.getId(), target);
                    continue;
                }
                // Get the value of the entry
                BigDecimal currentValue = value.get();
                LOGGER.info("        Check #{}, Target #{} :: Value found.", check.getId(), target);
                // Get the last alert stored for this check
                Alert lastAlert = getLastAlertForTarget(target);
                
                AlertType lastState;
                // If no "last alert" is found, then assume that the last state is "OK"
                if (lastAlert == null) {
                	LOGGER.info("        Check #{}, Target #{} :: Last alert was null, setting to 'OK'", check.getId(), target);
                    lastState = AlertType.OK;
                } else {
                    lastState = lastAlert.getToType();
                    LOGGER.info("        Check #{}, Target #{} :: Last alert found, state was '{}'", check.getId(), target, lastState );
                }
                // Based on the check value retrieved, turn it into an Alert state
                AlertType currentState = valueChecker.checkValue(currentValue, warn, error);
                // If the Alert state is worse than the last state, set it as the worst state yet
                // encountered
                if (currentState.isWorseThan(worstState)) {
                    worstState = currentState;
                    LOGGER.info("        Check #{}, Target #{} :: Current alert worse than last alert", check.getId(), target );
                }
                // If the last state and the current state are both OK, move to the next entry
                if (isStillOk(lastState, currentState)) {
                	LOGGER.info("        Check #{}, Target #{} :: Current alert comparison yields 'Is Still OK'", check.getId(), target );
                    continue;
                }
                // If the state is not OK, create an alert
                Alert alert = createAlert(target, currentValue, warn, error, lastState, currentState, now);
                saveAlert(alert);
                
                // Only notify if the alert has changed state
                if (stateIsTheSame(lastState, currentState)) {
                	LOGGER.info("        Check #{}, Target #{} :: Current alert comparison reveals state is the same", check.getId(), target );
                    continue;
                }
                // If the state has changed, add the alert to the interesting alerts collection
                LOGGER.info("        Check #{}, Target #{} :: Adding current alert as an 'Interesting Alert'", check.getId(), target );
                interestingAlerts.add(alert);
                
            }
            // Notify the Check Governor that the check has been completed
            LOGGER.info("        Check #{} :: Check is now complete", check.getId() );
            // Update the the check with the worst state encountered in this polling
            Check updatedCheck = checksStore.updateStateAndLastCheck(check.getId(), worstState, DateTime.now());
            // If there are no interesting alerts, simply return
            if (interestingAlerts.isEmpty()) {
            	LOGGER.info("        Check #{} :: No interesting alerts found.", check.getId() );
                return;
            }
            LOGGER.info("        Check #{} :: Interesting alerts found, looking at check's subscriptions.", check.getId() );
            // If there are interesting alerts, then evaluate the check's subscriptions 
            // to see if notifications are to be sent out
            for (Subscription subscription : updatedCheck.getSubscriptions()) {
            	// If no notification should be sent for this alert state (ERROR, WARN, etc.),
            	// move on
            	LOGGER.info("        Check #{} :: Subscription #{} of type '{}' being evaluated.", check.getId(), subscription.getId(), subscription.getType() );
                if (!subscription.shouldNotify(now, worstState)) {
                	LOGGER.info("        Check #{} :: Subscription #{} should not fire away.", check.getId(), subscription.getId() );
                    continue;
                }
                // If a notification should be sent out, poll the notification services and 
                // send a notification for each registered service
                for (NotificationService notificationService : notificationServices) {
                    if (notificationService.canHandle(subscription.getType())) {
                    	LOGGER.info("        Check #{} :: Subscription #{} firing away.", check.getId(), subscription.getId() );
                        try {
                            notificationService.sendNotification(updatedCheck, subscription, interestingAlerts);
                            LOGGER.info("        Check #{} :: Subscription #{} sent.", check.getId(), subscription.getId() );
                        } catch (Exception e) {
                            LOGGER.warn("Notifying {} by {} failed.", subscription.getTarget(), subscription.getType(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("{} failed", check.getName(), e);
            
        } finally {
        	// If we've made it here, it is due either to a typical Exception, or to
        	// a database timeout
        	// Notify the Check Governor that the check has been completed
            CheckConcurrencyGovernor.instance().notifiyCheckIsComplete(this.check);
        }
    }
    
    public static void flushLastAlerts() {
    	lastAlerts.clear();
    }
    
    private Alert getLastAlertForTarget(String target) {
    	String key = String.format("%s|%s", check.getId(), target);
    	
    	if (lastAlerts.containsKey(key)) {
    		return lastAlerts.get(key);
    	}
    	
    	// Last alert has not been loaded for this target/check; load from store
        LOGGER.info("        Check #{}, Target #{} :: Loading last alert from store", check.getId(), target);
        Alert lastAlert = alertsStore.getLastAlertForTargetOfCheck(target, check.getId());
        
        // Cache, even if null
        lastAlerts.put(key, lastAlert);
        return lastAlert;
    }
    
    private void saveAlert(Alert alert) {
    	// Update cache with latest
    	String key = String.format("%s|%s", check.getId(), alert.getTarget());    	
        lastAlerts.put(key, alert);
        
        // Persist in store
        alertsStore.createAlert(check.getId(), alert);
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