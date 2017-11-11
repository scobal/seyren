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

import com.seyren.core.domain.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.seyren.core.service.checker.TargetChecker;
import com.seyren.core.service.checker.ValueChecker;
import com.seyren.core.service.notification.NotificationService;
import com.seyren.core.store.AlertsStore;
import com.seyren.core.store.ChecksStore;

public class CheckRunner implements Runnable {

    protected String graphiteRefreshRate;
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckRunner.class);

    protected final Check check;
    protected final AlertsStore alertsStore;
    protected final ChecksStore checksStore;
    protected final TargetChecker targetChecker;
    protected final ValueChecker valueChecker;
    protected final Iterable<NotificationService> notificationServices;

    // A hashmap of last alerts by target/check
	private static final HashMap<String, Alert> lastAlerts = new HashMap<String, Alert>();

    public CheckRunner(Check check, AlertsStore alertsStore, ChecksStore checksStore, TargetChecker targetChecker, ValueChecker valueChecker,
                       Iterable<NotificationService> notificationServices, String graphiteRefreshRate) {
        this.check = check;
        this.alertsStore = alertsStore;
        this.checksStore = checksStore;
        this.targetChecker = targetChecker;
        this.valueChecker = valueChecker;
        this.notificationServices = notificationServices;
        this.graphiteRefreshRate = graphiteRefreshRate;
    }

    @Override
    public void run() {
        // Wrap everything in try so we can clear the concurrency check upon exit in the finally block
    	try {
        	// If the check is not enabled, don't run it, exiting...
            if (!check.isEnabled()) {
                return;
            }

            ThresholdCheck thresholdCheck = (ThresholdCheck)check;
        	// Run the check
            Map<String, Optional<BigDecimal>> targetValues = targetChecker.check(check);
            // If there was a problem retrieving data from graphite, then simply don't continue processing the check
            if (thresholdCheck.hasRemoteServerErrorOccurred()){
            	// TODO Will we always be calling a Graphite server?  Change if you are using another service
            	LOGGER.warn("  *** Check={} :: Message='Will not initiate check, remote server read error occurred when calling' "
            			+ "server located at: GraphiteServer={}", check.getId(), check.getGraphiteBaseUrl());
            	return;
            }
            // Get the current time - to be used for notification and alert time stamps
            DateTime now = new DateTime();
            // Get the threshold values for the check which signify warning and error thresholds
            BigDecimal warn = thresholdCheck.getWarn();
            BigDecimal error = thresholdCheck.getError();
            AlertType worstState;

            // If the check is allowed data, initialized the state as OK, otherwise,
            // it is unknown
            if (thresholdCheck.isAllowNoData()) {
            	LOGGER.info("  *** Check={} :: Message='Initiating check, data is not allowed, setting worst state to 'OK''", thresholdCheck.getId());
                worstState = AlertType.OK;
            } else {
            	LOGGER.info("  *** Check={} :: Message='Initiating check, data is allowed, setting worst state to 'Unknown''", thresholdCheck.getId());
                worstState = AlertType.UNKNOWN;
            }
            // Intialize a list of alerts that represent a change in alert state from
            // the last time that the check was run
            List<Alert> interestingAlerts = new ArrayList<Alert>();
            // Get the measured values for this check from the Graphite/Noop datasource
            // Iterate through them, to check for error/warn values
            for (Entry<String, Optional<BigDecimal>> entry : targetValues.entrySet()) {
                String target = entry.getKey();
            	LOGGER.info("        Check={}, Target={} :: Message='Evaluating value target.''", thresholdCheck.getId(), target);
                Optional<BigDecimal> value = entry.getValue();

                // If there is no value in the entry, move to the next one
                if (!value.isPresent()) {
                    LOGGER.info("        Check={}, Target={} :: Message='No value present.''", thresholdCheck.getId(), target);
                    continue;
                }
                // Get the value of the entry
                BigDecimal currentValue = value.get();

                // Based on the check value retrieved, turn it into an Alert state
                AlertType currentState = valueChecker.checkValue(currentValue, warn, error);

                LOGGER.info("        Check={}, Target={}  Current State is {} :: Message='Value found.''", thresholdCheck.getId(), target, currentState);
                // Get the last alert stored for this check
                Alert lastAlert = getLastAlertForTarget(target , check,lastAlerts,alertsStore);

                AlertType lastState;
                // If no "last alert" is found, then assume that the last state is "OK"
                if (lastAlert == null) {
                	LOGGER.info("        Check={}, Target={} :: Message='Last alert was null, setting to 'OK''", thresholdCheck.getId(), target);
                    lastState = AlertType.OK;
                } else {
                    lastState = lastAlert.getToType();
                    LOGGER.info("        Check={}, Target={} :: Message='Last alert found, state was '{}''", thresholdCheck.getId(), target, lastState );
                }


                // If the Alert state is worse than the last state, set it as the worst state yet
                // encountered
                if (currentState.isWorseThan(worstState)) {
                    worstState = currentState;
                    LOGGER.info("        Check={}, Target={} :: Message='Current state worse than worse state CurrentState:{}, WorstState:{}'", thresholdCheck.getId(), target, currentState, worstState);
                }
                // If the last state and the current state are both OK, move to the next entry
                if (isStillOk(lastState, currentState)) {
                	LOGGER.info("        Check={}, Target={} :: Message='Current alert comparison yields 'Is Still OK''", thresholdCheck.getId(), target );
                    continue;
                }

                Alert alert = createAlert(target, currentValue, warn, error, lastState, currentState, now);
                saveAlert(alert,check,lastAlerts,alertsStore);

                // Only notify if the alert has changed state



                if(null != thresholdCheck.isEnableConsecutiveChecks() && thresholdCheck.isEnableConsecutiveChecks() && null != thresholdCheck.getConsecutiveChecks() && null != thresholdCheck.getConsecutiveChecksTolerance()){
                    LOGGER.info("        Check={} ccIsNowBetter={} , ccIsTriggered={}", thresholdCheck.getId(), lastState.isWorseThan(currentState)  ,thresholdCheck.isConsecutiveChecksTriggered());
                    if(lastState.isWorseThan(currentState) && thresholdCheck.isConsecutiveChecksTriggered()){
                        LOGGER.info("        Check={}, Target={} :: Message='This consecutive alert is now in an ok state'", thresholdCheck.getId(), target );
                        LOGGER.info("        Check={}, Target={}, From={}, To={} :: Message='Adding current alert as an Interesting Alert'", thresholdCheck.getId(), target, lastState, currentState );

                        worstState = currentState;
                        interestingAlerts.add(alert);
                        checksStore.updateConsecutiveChecksTriggered(thresholdCheck.getId(), false);

                    }
                    if (analysePastAlertsAndRaiseAlarm(warn, error, interestingAlerts, alert, target, now)){
                        checksStore.updateConsecutiveChecksTriggered(thresholdCheck.getId(), true);
                    }
                    else{
                        continue;
                    }
                }
                else {
                    if (stateIsTheSame(lastState, currentState)) {
                        LOGGER.info("        Check={}, Target={} :: Message='Current alert comparison reveals state is the same'", thresholdCheck.getId(), target );
                        continue;
                    }
                    // If the state has changed, add the alert to the interesting alerts collection
                    LOGGER.info("        Check={}, Target={}, From={}, To={} :: Message='Adding current alert as an Interesting Alert'", thresholdCheck.getId(), target, lastState, currentState );

                    interestingAlerts.add(alert);
                }

            }
            // Notify the Check Governor that the check has been completed
            LOGGER.info("        Check={} :: Message='Check is now complete'", thresholdCheck.getId() );

            // Update the the check with the worst state encountered in this polling
            Check updatedCheck = checksStore.updateStateAndLastCheck(thresholdCheck.getId(), worstState, DateTime.now());
            LOGGER.info("       Check={} :: Message= 'Updating state to worst state {}'", thresholdCheck.getId(), worstState);
            // If there are no interesting alerts, simply return
            if (interestingAlerts.isEmpty()) {
            	LOGGER.info("        Check={} :: Message='No interesting alerts found.'", thresholdCheck.getId() );
                return;
            }
            LOGGER.info("        Check={} :: Message='Interesting alerts found, looking at check's subscriptions.'", thresholdCheck.getId() );
            // If there are interesting alerts, then evaluate the check's subscriptions
            // to see if notifications are to be sent out
            for (Subscription subscription : updatedCheck.getSubscriptions()) {
            	// If no notification should be sent for this alert state (ERROR, WARN, etc.),
            	// move on
            	LOGGER.info("        Check={} Subscription={} SubscriptionType={} :: Message= 'Subscription being evaluated.'", thresholdCheck.getId(), subscription.getId(), subscription.getType() );
                if (!subscription.shouldNotify(now, worstState)) {
                	LOGGER.info("        Check={} :: Message='Subscription should not fire away.' Subscription={}", thresholdCheck.getId(), subscription.getId() );
                    continue;
                }
                // If a notification should be sent out, poll the notification services and
                // send a notification for each registered service
                for (NotificationService notificationService : notificationServices) {
                    if (notificationService.canHandle(subscription.getType())) {
                    	LOGGER.info("        Check={} :: Message='Subscription firing away.' Subscription={}", thresholdCheck.getId(), subscription.getId() );
                        try {
                            notificationService.sendNotification(updatedCheck, subscription, interestingAlerts);
                            LOGGER.info("        Check={} :: Message='Subscription sent.' Subscription={}", thresholdCheck.getId(), subscription.getId() );
                        } catch (Exception e) {
                            LOGGER.warn("Message='Notifying {} by {} failed.'", subscription.getTarget(), subscription.getType(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Message='{} failed'", check.getName(), e);

        } finally {
        	// Notify the Check Governor that the check has been completed
            CheckConcurrencyGovernor.instance().notifyCheckIsComplete(this.check);
        }
    }

    private boolean analysePastAlertsAndRaiseAlarm(BigDecimal warn, BigDecimal error, List<Alert> interestingAlerts, Alert alert, String target, DateTime nowDate) {
        SeyrenResponse<Alert> previousResponse= alertsStore.getAlerts(check.getId(), 0, check.getConsecutiveChecks());
        if(null != previousResponse) {
            List<Alert> previousAlerts = previousResponse.getValues();
            if(previousAlerts.size() < check.getConsecutiveChecks()){
                LOGGER.info("       Check={}, Message='Not enough checks for previous consecutive alerts number of checks needed {}, current number of checks {}'", check.getId(), check.getConsecutiveChecks(), previousAlerts.size());
                return false;
            }
            else{
                Integer errorCount = 0;
                for(Alert pastAlert : previousAlerts ){
                    AlertType pastErrorState = valueChecker.checkValue(pastAlert.getValue(), warn, error);
                    if(pastErrorState.equals(AlertType.ERROR) && pastAlert.getTimestamp().getMillis() +  check.getConsecutiveChecks()* Integer.parseInt(graphiteRefreshRate) > nowDate.getMillis()){
                        errorCount ++;
                    }
                }
                if(errorCount > (check.getConsecutiveChecks() * check.getConsecutiveChecksTolerance()) / 100){
                    LOGGER.info("        Check={}, Target={}, Errors Count #{}, Tolerance #{} :: Message='Adding current alert as an 'Interesting Alert''", check.getId(), target, check.getConsecutiveChecks(), check.getConsecutiveChecksTolerance());
                    interestingAlerts.add(alert);
                }
                else{
                    LOGGER.info("       Check={}, Message='Error count is not more than Consecutive Check tolerance' ccErrorCount={}, ccTolerance={}", check.getId(), errorCount, (check.getConsecutiveChecks() * check.getConsecutiveChecksTolerance()) / 100);
                    return false;
                }
            }
        }
        return true;
    }

    public static void flushLastAlerts() {
    	lastAlerts.clear();
    }

    protected Alert getLastAlertForTarget(String target , Check check, HashMap<String, Alert> lastAlerts , AlertsStore alertsStore) {
    	String key = String.format("%s|%s", check.getId(), target);

    	if (lastAlerts.containsKey(key)) {
    		return lastAlerts.get(key);
    	}

    	// Last alert has not been loaded for this target/check; load from store
        LOGGER.info("        Check={}, Target={} :: Message='Loading last alert from store'", check.getId(), target);
        Alert lastAlert = alertsStore.getLastAlertForTargetOfCheck(target, check.getId());

        // Cache, even if null
        lastAlerts.put(key, lastAlert);
        return lastAlert;
    }

    protected void saveAlert(Alert alert , Check check , Map<String, Alert> lastAlerts , AlertsStore alertsStore) {
    	// Update cache with latest
    	String key = String.format("%s|%s", check.getId(), alert.getTarget());
        lastAlerts.put(key, alert);

        // Persist in store
        alertsStore.createAlert(check.getId(), alert);
    }

    protected boolean isStillOk(AlertType last, AlertType current) {
        return last == AlertType.OK && current == AlertType.OK;
    }

    protected boolean stateIsTheSame(AlertType last, AlertType current) {
        return last == current;
    }

    private Alert createAlert(String target, BigDecimal value, BigDecimal warn, BigDecimal error, AlertType from, AlertType to, DateTime now) {
        return new ThresholdAlert()
                .withWarn(warn)
                .withError(error)
                .withTarget(target)
                .withValue(value)
                .withFromType(from)
                .withToType(to)
                .withTimestamp(now);
    }
}
