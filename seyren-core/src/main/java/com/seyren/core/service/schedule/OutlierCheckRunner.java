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

import com.google.common.base.Optional;
import com.seyren.core.detector.OutlierDetector;
import com.seyren.core.domain.*;
import com.seyren.core.service.checker.TargetChecker;
import com.seyren.core.service.checker.ValueChecker;
import com.seyren.core.service.notification.NotificationService;
import com.seyren.core.store.AlertsStore;
import com.seyren.core.store.ChecksStore;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by akharbanda on 20/08/17.
 */
public class OutlierCheckRunner extends CheckRunner
{
    private static final Logger LOGGER = LoggerFactory.getLogger(OutlierCheckRunner.class);
    private static final BigDecimal IGNORE_THRESHOLD = BigDecimal.ZERO;
    private final OutlierDetector outlierDetector;
    private static final HashMap<String, Alert> lastOutlierAlerts = new HashMap<String, Alert>();

    public OutlierCheckRunner(Check check, AlertsStore alertsStore, ChecksStore checksStore, TargetChecker targetChecker, ValueChecker valueChecker,
                              Iterable<NotificationService> notificationServices, OutlierDetector outlierDetector, String graphiteRefreshRate)
    {
        super(check, alertsStore, checksStore, targetChecker, valueChecker, notificationServices, graphiteRefreshRate);
        this.outlierDetector = outlierDetector;
    }

    @Override
    public final void run()
    {
        if (!check.isEnabled())
        {
            return;
        }

        try
        {
            OutlierCheck outlierCheck = (OutlierCheck) check;
            Map<String, Optional<BigDecimal>> targetValues = targetChecker.check(outlierCheck);
            Map<String, Optional<BigDecimal>> filteredTargetValues = new HashMap<String, Optional<BigDecimal>>();

            if (check.hasRemoteServerErrorOccurred())
            {
                // TODO Will we always be calling a Graphite server?  Change if you are using another service
                LOGGER.warn("  *** Check={} :: Message='Will not initiate check, remote server read error occurred when calling' "
                        + "server located at: GraphiteServer={}", check.getId(), check.getGraphiteBaseUrl());
                return;
            }

            DateTime now = new DateTime();

            Double relativeDiff = outlierCheck.getRelativeDiff();
            BigDecimal absoluteDiff = outlierCheck.getAbsoluteDiff();
            AlertType worstState;

            if (outlierCheck.isAllowNoData())
            {
                LOGGER.info("  *** Check={} :: Message='Initiating check, data is not allowed, setting worst state to 'OK''", outlierCheck.getId());
                worstState = AlertType.OK;
            }
            else
            {
                LOGGER.info("  *** Check={} :: Message='Initiating check, data is allowed, setting worst state to 'Unknown''", outlierCheck.getId());
                worstState = AlertType.UNKNOWN;
            }
            List<Alert> interestingAlerts = new ArrayList<Alert>();

            filteredTargetValues = filterTargetValues(targetValues);

            List<String> unhealthyTargets = outlierDetector.getUnhealthyTargets(filteredTargetValues, outlierCheck);

            for (String target : targetValues.keySet())
            {
                AlertType currentState;
                if (unhealthyTargets.contains(target))
                {
                    currentState = AlertType.ERROR;
                }
                else
                {
                    currentState = AlertType.OK;
                }

                OutlierAlert lastAlert = null;
                try
                {
                    lastAlert = (OutlierAlert) getLastAlertForTarget(target, check, lastOutlierAlerts, alertsStore);
                }
                catch (Exception e)
                {
                    LOGGER.error("Exception while retrieving alert", e);
                }

                AlertType lastState;
                Integer numberOfConsecutiveViolations;

                if (lastAlert == null)
                {
                    LOGGER.info("        Check={}, Target={} :: Message='Last alert was null, setting to 'OK''", outlierCheck.getId(), target);
                    lastState = AlertType.OK;
                    numberOfConsecutiveViolations = 0;
                }
                else
                {
                    lastState = lastAlert.getToType();
                    LOGGER.info("        Check={}, Target={} :: Message='Last alert found, state was '{}''", outlierCheck.getId(), target, lastState);
                    numberOfConsecutiveViolations = lastAlert.getConsecutiveAlertCount();
                }

                // If the last state and the current state are both OK, move to the next entry
                if (isStillOk(lastState, currentState))
                {
                    LOGGER.info("        Check={}, Target={} :: Message='Current alert comparison yields 'Is Still OK''", outlierCheck.getId(), target);
                    continue;
                }

                Alert alert = null;
                if (currentState == AlertType.ERROR)
                {
                    worstState = currentState;
                    numberOfConsecutiveViolations++;
                    alert = createAlert(target, targetValues.get(target).get(), numberOfConsecutiveViolations, absoluteDiff, relativeDiff, lastState, currentState, now);
                    LOGGER.info("        Check={}, Target={} :: Message='Current state worse than worse state CurrentState:{}, WorstState:{}'", outlierCheck.getId(), target, currentState, worstState);
                    if (numberOfConsecutiveViolations >= outlierCheck.getMinConsecutiveViolations())
                    {
                        interestingAlerts.add(alert);
                    }
                }

                else if (currentState == AlertType.OK)
                {
                    alert = createAlert(target, targetValues.get(target).get(), 0, absoluteDiff, relativeDiff, lastState, currentState, now);
                    LOGGER.info("        Check={}, Target={} :: Message='Current state worse than worse state CurrentState:{}, WorstState:{}'", outlierCheck.getId(), target, currentState, worstState);
                    if (!stateIsTheSame(lastState, currentState))
                    {
                        interestingAlerts.add(alert);
                    }
                }

                saveAlert(alert, check, lastOutlierAlerts, alertsStore);

            }

            Check updatedCheck = checksStore.updateStateAndLastCheck(outlierCheck.getId(), worstState, DateTime.now());

            if (interestingAlerts.isEmpty())
            {
                return;
            }

            for (Subscription subscription : updatedCheck.getSubscriptions())
            {
                if (!subscription.shouldNotify(now, worstState))
                {
                    continue;
                }

                for (NotificationService notificationService : notificationServices)
                {
                    if (notificationService.canHandle(subscription.getType()))
                    {
                        try
                        {
                            notificationService.sendNotification(updatedCheck, subscription, interestingAlerts);
                        }
                        catch (Exception e)
                        {
                            LOGGER.warn("Notifying {} by {} failed.", subscription.getTarget(), subscription.getType(), e);
                        }
                    }
                }
            }

        }
        catch (Exception e)
        {
            LOGGER.warn("{} failed", check.getName(), e);
        }
        finally
        {
            // Notify the Check Governor that the check has been completed
            CheckConcurrencyGovernor.instance().notifyCheckIsComplete(this.check);
        }
    }

    public static void flushLastAlerts()
    {
        lastOutlierAlerts.clear();
    }

    private Map<String, Optional<BigDecimal>> filterTargetValues(Map<String, Optional<BigDecimal>> targetValues)
    {
        Map<String, Optional<BigDecimal>> filteredValues = new HashMap<String, Optional<BigDecimal>>();

        for (Map.Entry<String, Optional<BigDecimal>> entry : targetValues.entrySet())
        {
            if (entry.getValue().isPresent() && entry.getValue().get().compareTo(IGNORE_THRESHOLD) > 0)
            {
                filteredValues.put(entry.getKey(), entry.getValue());
            }
        }
        return filteredValues;
    }

    private Alert createAlert(String target, BigDecimal value, Integer consecutiveAlertCount, BigDecimal absoluteDiff, Double relativeDiff, AlertType from, AlertType to, DateTime now)
    {
        return new OutlierAlert()
                .withAbsoluteDiff(absoluteDiff)
                .withConsecutiveAlertCount(consecutiveAlertCount)
                .withRelativeDiff(relativeDiff)
                .withFromType(from)
                .withToType(to)
                .withTarget(target)
                .withValue(value)
                .withTimestamp(now);
    }

}
