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


import org.joda.time.Seconds;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bson.types.ObjectId;

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
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.MongoURI;
import com.seyren.core.util.config.SeyrenConfig;

public class CheckRunner implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckRunner.class);

    private DB mongo;
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
            SeyrenConfig config;
            config = new SeyrenConfig();
            String uri = config.getMongoUrl();
            MongoURI mongoUri = new MongoURI(uri);
            DB mongo = mongoUri.connectDB();
            this.mongo = mongo;
            if (mongoUri.getUsername() != null) {
               mongo.authenticate(mongoUri.getUsername(), mongoUri.getPassword());
            }

        } catch (Exception e) {
           throw new RuntimeException(e);
        }

	BasicDBObject query = new BasicDBObject();
        query.put("name", check.getName());
        DBObject dbo  = mongo.getCollection("checks").findOne(query);

        try {
            Map<String, Optional<BigDecimal>> targetValues = targetChecker.check(check);

            if (targetValues.isEmpty()) {
                return;
            }

            DateTime now = new DateTime();
            BigDecimal warn = check.getWarn();
            BigDecimal error = check.getError();

            AlertType worstState = AlertType.UNKNOWN;

            List<Alert> interestingAlerts = new ArrayList<Alert>();
            for (Entry<String, Optional<BigDecimal>> entry : targetValues.entrySet()) {

                String target = entry.getKey();
                Optional<BigDecimal> value = entry.getValue();

                if (!value.isPresent()) {
                    LOGGER.warn("No value present for {}", target);
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
		if(dbo.get("errorTimeStamp")!=null){
		 DateTime err_ts = new DateTime(dbo.get("errorTimeStamp"));
		 DateTime lastCheck = check.getLastCheck();
                      if(lastCheck!=null){
                          int   diff  =  Seconds.secondsBetween(err_ts,lastCheck).getSeconds()/(60);
                          diff += 1;
		          mongo.getCollection("checks").update(dbo,new BasicDBObject("$set",new BasicDBObject("diff",diff)));
                          int timeThreshold = check.getTimeThreshold().intValue();
                          if(diff < timeThreshold) {
                               continue;
                        }
                      }
                    }
                Alert alert = createAlert(target, currentValue, warn, error, lastState, currentState, now);

                alertsStore.createAlert(check.getId(), alert);

                if (stateIsTheSame(lastState, currentState)) {
                         continue;
                }

                interestingAlerts.add(alert);
          }

            check.setState(worstState);
            check.setLastCheck(DateTime.now());
            checksStore.saveCheck(check);

            if (interestingAlerts.isEmpty()) {
                return;
            }
            for (Subscription subscription : check.getSubscriptions()) {
                if (!subscription.shouldNotify(now, worstState)) {
                    continue;
                }

                for (NotificationService notificationService : notificationServices) {
                    if (notificationService.canHandle(subscription.getType())) {
                        try {
                            notificationService.sendNotification(check, subscription, interestingAlerts);
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
