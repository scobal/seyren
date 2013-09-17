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

import static com.seyren.mongo.NiceDBObject.forId;
import static com.seyren.mongo.NiceDBObject.object;
import static org.springframework.util.Assert.notNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoURI;
import com.seyren.core.domain.Alert;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.GraphiteInstance;
import com.seyren.core.domain.SeyrenResponse;
import com.seyren.core.domain.Subscription;
import com.seyren.core.store.AlertsStore;
import com.seyren.core.store.ChecksStore;
import com.seyren.core.store.GraphiteInstancesStore;
import com.seyren.core.store.SubscriptionsStore;
import com.seyren.core.util.config.SeyrenConfig;

@Named
public class MongoStore implements ChecksStore, AlertsStore, SubscriptionsStore, GraphiteInstancesStore {
    
    private MongoMapper mapper = new MongoMapper();
    private DB mongo;
    
    @Inject
    public MongoStore(SeyrenConfig seyrenConfig) {
        try {
            String uri = seyrenConfig.getMongoUrl();
            MongoURI mongoUri = new MongoURI(uri);
            DB mongo = mongoUri.connectDB();
            if (mongoUri.getUsername() != null) {
                mongo.authenticate(mongoUri.getUsername(), mongoUri.getPassword());
            }
            this.mongo = mongo;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public MongoStore(DB mongo) {
        this.mongo = mongo;
    }
    
    private DBCollection getChecksCollection() {
        return mongo.getCollection("checks");
    }
    
    private DBCollection getAlertsCollection() {
        return mongo.getCollection("alerts");
    }
    
    @Override
    public SeyrenResponse<Check> getChecks(Boolean enabled) {
        List<Check> checks = new ArrayList<Check>();
        DBCursor dbc;
        if (enabled != null) {
            dbc = getChecksCollection().find(object("enabled", enabled));
        } else {
            dbc = getChecksCollection().find();
        }
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
    }
    
    @Override
    public Check createCheck(Check check) {
        check.setId(ObjectId.get().toString());
        getChecksCollection().insert(mapper.checkToDBObject(check));
        return check;
    }
    
    @Override
    public Check saveCheck(Check check) {
    	notNull(check, "check can't be null");
    	notNull(check.getGraphiteBaseUrl(), "check.graphiteBaseUrl can't be null");
    	notNull(check.getTarget(), "check.target can't be null");
    	notNull(check.getWarn(), "check.warn can't be null");
    	notNull(check.getError(), "check.error can't be null");
    	notNull(check.getState(), "check.state can't be null");
    	
        DBObject findObject = forId(check.getId());
        
        DBObject updateObject = object("name", check.getName())
                .with("description", check.getDescription())
                .with("graphiteBaseUrl", check.getGraphiteBaseUrl())
                .with("target", check.getTarget())
                .with("warn", check.getWarn().toPlainString())
                .with("error", check.getError().toPlainString())
                .with("enabled", check.isEnabled())
                .with("state", check.getState().toString());
        
        DBObject setObject = object("$set", updateObject);
        
        getChecksCollection().update(findObject, setObject);
        
        return check;
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
    public Alert getLastAlertForTargetOfCheck(String graphiteBaseUrl, String target, String checkId) {
        DBObject query = object("checkId", checkId)
        		.with("graphiteBaseUrl", graphiteBaseUrl)
        		.with("target", target);
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

	@Override
	public SeyrenResponse<GraphiteInstance> getGraphiteInstances() {
        List<GraphiteInstance> insts = new ArrayList<GraphiteInstance>();
        DBCursor dbc = getGraphiteInstancesCollection().find();
        while (dbc.hasNext()) {
            insts.add(mapper.graphiteInstanceFrom(dbc.next()));
        }
        return new SeyrenResponse<GraphiteInstance>()
                .withValues(insts)
                .withTotal(dbc.count());
	}
	
    private DBCollection getGraphiteInstancesCollection() {
        return mongo.getCollection("graphiteInstances");
    }
}
