package com.graphite.siren.mongo;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import org.bson.types.ObjectId;

import com.graphite.siren.core.domain.Alert;
import com.graphite.siren.core.domain.Check;
import com.graphite.siren.core.domain.Subscription;
import com.graphite.siren.core.store.AlertsStore;
import com.graphite.siren.core.store.ChecksStore;
import com.graphite.siren.core.store.SubscriptionsStore;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoURI;

@Named
public class MongoStore implements ChecksStore, AlertsStore, SubscriptionsStore {

	private static final String DEFAULT_MONGO_URL = "mongodb://localhost:27017/graphite-siren";
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
	
	@Override
	public List<Check> getChecks() {
		List<Check> result =  new ArrayList<Check>();
		for (DBObject dbo : getChecksCollection().find().toArray()) {
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
		getChecksCollection().remove(new BasicDBObject("_id", checkId));
	}

	@Override
	public Check createCheck(Check check) {
		check.setId(ObjectId.get().toString());
		getChecksCollection().insert(mapper.checkToDBObject(check));
		return check;
	}

	@Override
	public Check saveCheck(Check check) {
		DBObject dbo = basicDBObjectById(check.getId());
		getChecksCollection().update(dbo, basicDBObjectWithSet("name", check.getName()));
		getChecksCollection().update(dbo, basicDBObjectWithSet("target", check.getTarget()));
		getChecksCollection().update(dbo, basicDBObjectWithSet("warn", check.getWarn()));
		getChecksCollection().update(dbo, basicDBObjectWithSet("error", check.getError()));
		return check;
	}

	@Override
	public Alert createAlert(String checkId, Alert alert) {
		alert.setId(ObjectId.get().toString());
		DBObject check = basicDBObjectById(checkId);
		DBObject query = new BasicDBObject("$push", new BasicDBObject("alerts", mapper.alertToDBObject(alert)));
		getChecksCollection().update(check, query);
		return alert;
	}

	@Override
	public Subscription createSubscription(String checkId, Subscription subscription) {
		subscription.setId(ObjectId.get().toString());
		DBObject check = basicDBObjectById(checkId);
		DBObject query = new BasicDBObject("$push", new BasicDBObject("subscriptions", mapper.subscriptionToDBObject(subscription)));
		getChecksCollection().update(check, query);
		return subscription;
	}
	
	private DBObject basicDBObjectById(String id) {
	    return new BasicDBObject("_id", id);
	}
	
	private BasicDBObject basicDBObjectWithSet(String key, Object value) {
		return new BasicDBObject("$set", new BasicDBObject(key, value));
	}

}
