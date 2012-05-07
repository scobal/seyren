package com.seyren.mongo;

import static com.seyren.mongo.NiceDBObject.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import com.seyren.core.domain.Subscription;
import com.seyren.core.store.AlertsStore;
import com.seyren.core.store.ChecksStore;
import com.seyren.core.store.SubscriptionsStore;

@Named
public class MongoStore implements ChecksStore, AlertsStore, SubscriptionsStore {

	private static final String DEFAULT_MONGO_URL = "mongodb://localhost:27017/seyren";
	private MongoMapper mapper = new MongoMapper();
	private DB mongo;

	public MongoStore() {
		try {
			MongoURI mongoUri = new MongoURI(getMongoUri());
			DB mongo = mongoUri.connectDB();
			if (mongoUri.getUsername() != null) {
				mongo.authenticate(mongoUri.getUsername(), mongoUri.getPassword());
			}
			this.mongo = mongo;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private String getMongoUri() {
		String uri = System.getenv("MONGOHQ_URL");
		if (uri == null) {
			uri = DEFAULT_MONGO_URL;
		}
		return uri;
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
	public List<Check> getChecks() {
		List<Check> result =  new ArrayList<Check>();
		for (DBObject dbo : getChecksCollection().find().toArray()) {
			result.add(mapper.checkFrom(dbo));
		}
		return result;
	}
	
	@Override
	public List<Check> getChecksByState(Set<String> states) {
		List<Check> result =  new ArrayList<Check>();
		DBCursor dbc = getChecksCollection().find(new BasicDBObject("state", new BasicDBObject("$in", states.toArray())));
		for (DBObject dbo : dbc.toArray()) {
			result.add(mapper.checkFrom(dbo));
		}
		return result;
	}

	@Override
	public Check getCheck(String checkId) {
		DBObject dbo = getChecksCollection().findOne(new BasicDBObject("_id", checkId));
		if (dbo == null) {
			return null;
		}
		return mapper.checkFrom(dbo);
	}

	@Override
	public void deleteCheck(String checkId) {
		getChecksCollection().remove(basicDBObjectById(checkId));
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
	    
	    DBObject updateObject = object("name", check.getName())
    	    .with("target", check.getTarget())
    	    .with("warn", check.getWarn())
    	    .with("error", check.getError())
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
	public List<Alert> getAlerts(String checkId) {
		DBCursor dbc = getAlertsCollection().find(new BasicDBObject("checkId", checkId));
		List<Alert> alerts = new ArrayList<Alert>();
		for (DBObject dbo : dbc.toArray()) {
			alerts.add(mapper.alertFrom(dbo));
		}
		return alerts;
	}

	@Override
	public Subscription createSubscription(String checkId, Subscription subscription) {
		subscription.setId(ObjectId.get().toString());
		DBObject check = basicDBObjectById(checkId);
		DBObject query = new BasicDBObject("$push", new BasicDBObject("subscriptions", mapper.subscriptionToDBObject(subscription)));
		getChecksCollection().update(check, query);
		return subscription;
	}
	
	@Override
	public void deleteSubscription(String checkId, String subscriptionId) {
		DBObject check = basicDBObjectById(checkId);
		BasicDBObject subscription = new BasicDBObject("$pull", new BasicDBObject("subscriptions", basicDBObjectById(subscriptionId)));
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
	
	private DBObject basicDBObjectById(String id) {
	    return new BasicDBObject("_id", id);
	}
	
}
