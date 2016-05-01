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
package com.seyren.mongo;

import static com.seyren.mongo.NiceDBObject.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.base.Strings;
import org.apache.commons.lang.Validate;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.seyren.core.domain.Alert;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.SeyrenResponse;
import com.seyren.core.domain.Subscription;
import com.seyren.core.store.AlertsStore;
import com.seyren.core.store.ChecksStore;
import com.seyren.core.store.SubscriptionsStore;
import com.seyren.core.util.config.SeyrenConfig;
import com.seyren.core.util.hashing.TargetHash;

@Named
public class MongoStore implements ChecksStore, AlertsStore, SubscriptionsStore {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoStore.class);
    
    private MongoMapper mapper = new MongoMapper();
    private DB mongo;
    
    @Inject
    public MongoStore(SeyrenConfig seyrenConfig) {
        try {
            String uri = seyrenConfig.getMongoUrl();
            MongoClientURI mongoClientUri = new MongoClientURI(uri);
            MongoClient mongoClient = new MongoClient(mongoClientUri);
            DB mongoDB = mongoClient.getDB(mongoClientUri.getDatabase());
            mongoDB.setWriteConcern(WriteConcern.ACKNOWLEDGED);
            this.mongo = mongoDB;
            bootstrapMongo();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private void bootstrapMongo() {
        LOGGER.info("Bootstrapping Mongo indexes. Depending on the number of checks and alerts you've got it may take a little while.");
        try {
            createIndices();
            removeOldIndices();
            addTargetHashToAlerts();
        } catch (MongoException e) {
            LOGGER.error("Failure while bootstrapping Mongo indexes.\n"
                    + "If you've hit this problem it's possible that you have two checks which are named the same and violate an index which we've tried to add.\n"
                    + "Please correct the problem by removing the clash. If it's something else, please let us know on Github!", e);
            throw new RuntimeException("Failed to bootstrap Mongo indexes. Please refer to the logs for more information.", e);
        }
        LOGGER.info("Done bootstrapping Mongo indexes.");
    }

    private void createIndices() {
        LOGGER.info("Ensuring that we have all the indices we need");
        getChecksCollection().createIndex(new BasicDBObject("name", 1), new BasicDBObject("unique", true));
        getChecksCollection().createIndex(new BasicDBObject("enabled", 1).append("live", 1));
        getAlertsCollection().createIndex(new BasicDBObject("timestamp", -1));
        getAlertsCollection().createIndex(new BasicDBObject("checkId", 1).append("targetHash", 1));
    }

    private void removeOldIndices() {
        LOGGER.info("Dropping old indices");
        try {
            getAlertsCollection().dropIndex(new BasicDBObject("checkId", 1).append("target", 1));
        } catch (MongoCommandException e) {
            if (e.getCode() != 27) {
                // 27 is the code which appears when the index doesn't exist (which we're happy with, anything else is bad news)
                throw e;
            }
        }
    }

    private void addTargetHashToAlerts() {
        LOGGER.info("Adding targetHash field to any alerts which don't have it");
        DBCursor alerts = getAlertsCollection().find(new BasicDBObject("targetHash", new BasicDBObject("$exists", false)));
        alerts.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
        int alertCount = alerts.count();
        if (alertCount > 0) {
            LOGGER.info("Found {} alert(s) which need updating", alertCount);
        }
        while (alerts.hasNext()) {
            DBObject alertObject = alerts.next();
            Alert alert = mapper.alertFrom(alertObject);
            getAlertsCollection().save(mapper.alertToDBObject(alert));
        }
    }
    
    private DBCollection getChecksCollection() {
        return mongo.getCollection("checks");
    }
    
    private DBCollection getAlertsCollection() {
        return mongo.getCollection("alerts");
    }

    protected SeyrenResponse executeQueryAndCollectResponse(DBObject query) {
        List<Check> checks = new ArrayList<Check>();
        DBCursor dbc = getChecksCollection().find(query);
        while (dbc.hasNext()) {
            checks.add(mapper.checkFrom(dbc.next()));
        }
        dbc.close();

        return new SeyrenResponse<Check>()
                .withValues(checks)
                .withTotal(dbc.count());
    }
    
    @Override
    public SeyrenResponse<Check> getChecks(Boolean enabled, Boolean live) {
        List<Check> checks = new ArrayList<Check>();
        DBObject query = new BasicDBObject();
        if (enabled != null) {
            query.put("enabled", enabled);
        }
        if (live != null) {
            query.put("live", live);
        }
        DBCursor dbc = getChecksCollection().find(query);
        while (dbc.hasNext()) {
            checks.add(mapper.checkFrom(dbc.next()));
        }
        return new SeyrenResponse<Check>()
                .withValues(checks)
                .withTotal(dbc.count());
    }
    
    @Override
    public SeyrenResponse<Check> getChecksByState(Set<String> states, Boolean enabled) {
        List<Check> checks = new ArrayList<Check>();
        
        DBObject query = new BasicDBObject();
        query.put("state", object("$in", states.toArray()));
        if (enabled != null) {
            query.put("enabled", enabled);
        }
        DBCursor dbc = getChecksCollection().find(query);
        
        while (dbc.hasNext()) {
            checks.add(mapper.checkFrom(dbc.next()));
        }
        dbc.close();
        
        return new SeyrenResponse<Check>()
                .withValues(checks)
                .withTotal(dbc.count());
    }

    @Override
    public SeyrenResponse getChecksByPattern(List<String> checkFields, List<Pattern> patterns, Boolean enabled) {
        Validate.notNull(checkFields, "Fields may not be null.");
        Validate.notNull(patterns, "Patterns may not be null.");
        Validate.notEmpty(checkFields, "Fields may not be empty");
        Validate.notEmpty(patterns, "Patterns may not be empty");
        Validate.isTrue(checkFields.size() == patterns.size(), String.format("Fields[%s] have same number of elements as patterns[%s].  " +
                "fieldsSize[%d] != fieldsSize[%d]", checkFields, patterns, checkFields.size(), patterns.size()));

        DBObject query = new BasicDBObject();
        for (int i = 0; i < checkFields.size(); i++) {
            query.put(checkFields.get(i), patterns.get(i));
        }
        if (enabled != null) {
            query.put("enabled", enabled);
        }

        return executeQueryAndCollectResponse(query);
    }

    @Override
    public Check getCheck(String checkId) {
        DBObject dbo = getChecksCollection().findOne(object("_id", checkId));
        if (dbo == null) {
            return null;
        }
        return mapper.checkFrom(dbo);
    }
    
    @Override
    public void deleteCheck(String checkId) {
        getChecksCollection().remove(forId(checkId));
        deleteAlerts(checkId, null);
    }
    
    @Override
    public Check createCheck(Check check) {
        check.setId(ObjectId.get().toString());
        getChecksCollection().insert(mapper.checkToDBObject(check));
        return check;
    }
    
    @Override
    public Check saveCheck(Check check) {
        DBObject findObject = forId(check.getId());
        
        DateTime lastCheck = check.getLastCheck();
        
        DBObject partialObject = object("name", check.getName())
                .with("description", check.getDescription())
                .with("target", check.getTarget())
                .with("from", Strings.emptyToNull(check.getFrom()))
                .with("until", Strings.emptyToNull(check.getUntil()))
                .with("warn", check.getWarn().toPlainString())
                .with("error", check.getError().toPlainString())
                .with("enabled", check.isEnabled())
                .with("live", check.isLive())
                .with("allowNoData", check.isAllowNoData())
                .with("lastCheck", lastCheck == null ? null : new Date(lastCheck.getMillis()))
                .with("state", check.getState().toString());
        
        DBObject setObject = object("$set", partialObject);
        
        getChecksCollection().update(findObject, setObject);
        
        return check;
    }
    
    @Override
    public Check updateStateAndLastCheck(String checkId, AlertType state, DateTime lastCheck) {
        DBObject findObject = forId(checkId);
        
        DBObject partialObject = object("lastCheck", new Date(lastCheck.getMillis()))
                .with("state", state.toString());
        
        DBObject setObject = object("$set", partialObject);
        
        getChecksCollection().update(findObject, setObject);

        return getCheck(checkId);
    }
    
    @Override
    public Alert createAlert(String checkId, Alert alert) {
        alert.setId(ObjectId.get().toString());
        alert.setCheckId(checkId);
        getAlertsCollection().insert(mapper.alertToDBObject(alert));
        return alert;
    }
    
    @Override
    public SeyrenResponse<Alert> getAlerts(String checkId, int start, int items) {
        DBCursor dbc = getAlertsCollection().find(object("checkId", checkId)).sort(object("timestamp", -1)).skip(start).limit(items);
        List<Alert> alerts = new ArrayList<Alert>();
        while (dbc.hasNext()) {
            alerts.add(mapper.alertFrom(dbc.next()));
        }
        dbc.close();
        return new SeyrenResponse<Alert>()
                .withValues(alerts)
                .withItems(items)
                .withStart(start)
                .withTotal(dbc.count());
    }
    
    @Override
    public SeyrenResponse<Alert> getAlerts(int start, int items) {
        DBCursor dbc = getAlertsCollection().find().sort(object("timestamp", -1)).skip(start).limit(items);
        List<Alert> alerts = new ArrayList<Alert>();
        while (dbc.hasNext()) {
            alerts.add(mapper.alertFrom(dbc.next()));
        }
        dbc.close();
        return new SeyrenResponse<Alert>()
                .withValues(alerts)
                .withItems(items)
                .withStart(start)
                .withTotal(dbc.count());
    }
    
    @Override
    public void deleteAlerts(String checkId, DateTime before) {
        DBObject query = object("checkId", checkId);
        
        if (before != null) {
            query.put("timestamp", object("$lt", new Date(before.getMillis())));
        }
        
        getAlertsCollection().remove(query);
    }
    
    @Override
    public Alert getLastAlertForTargetOfCheck(String target, String checkId) {
        DBObject query = object("checkId", checkId).with("targetHash", TargetHash.create(target));
        DBCursor cursor = getAlertsCollection().find(query).sort(object("timestamp", -1)).limit(1);
        try {
            while (cursor.hasNext()) {
                return mapper.alertFrom(cursor.next());
            }
        } finally {
            cursor.close();
        }
        return null;
    }
    
    @Override
    public Subscription createSubscription(String checkId, Subscription subscription) {
        subscription.setId(ObjectId.get().toString());
        DBObject check = forId(checkId);
        DBObject query = object("$push", object("subscriptions", mapper.subscriptionToDBObject(subscription)));
        getChecksCollection().update(check, query);
        return subscription;
    }
    
    @Override
    public void deleteSubscription(String checkId, String subscriptionId) {
        DBObject check = forId(checkId);
        BasicDBObject subscription = object("$pull", object("subscriptions", forId(subscriptionId)));
        getChecksCollection().update(check, subscription);
    }
    
    @Override
    public void updateSubscription(String checkId, Subscription subscription) {
        DBObject subscriptionObject = mapper.subscriptionToDBObject(subscription);
        DBObject subscriptionFindObject = forId(subscription.getId());
        DBObject checkFindObject = forId(checkId).with("subscriptions", object("$elemMatch", subscriptionFindObject));
        DBObject updateObject = object("$set", object("subscriptions.$", subscriptionObject));
        getChecksCollection().update(checkFindObject, updateObject);
    }
    
}
