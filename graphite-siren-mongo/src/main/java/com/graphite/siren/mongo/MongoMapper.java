package com.graphite.siren.mongo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.graphite.siren.core.domain.Alert;
import com.graphite.siren.core.domain.Check;
import com.graphite.siren.core.domain.Subscription;
import com.graphite.siren.core.domain.SubscriptionType;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class MongoMapper {

	public Check checkFrom(DBObject dbo) {
		String id = dbo.get("_id").toString();
		String name = getString(dbo, "name");
		String target = getString(dbo, "target");
		
		List<Alert> alerts = new ArrayList<Alert>();
		BasicDBList list = getBasicDBList(dbo, "alerts");
		for (Object o : list) {
			alerts.add(alertFrom((DBObject) o));
		}
		
		List<Subscription> subscriptions = new ArrayList<Subscription>();
		list = getBasicDBList(dbo, "subscriptions");
		for (Object o : list) {
			subscriptions.add(subscriptionFrom((DBObject) o));
		}

		return new Check().withId(id)
				.withName(name)
				.withTarget(target)
				.withAlerts(alerts)
				.withSubscriptions(subscriptions);
	}

	private Subscription subscriptionFrom(DBObject dbo) {
		String id = dbo.get("_id").toString();
		String target = getString(dbo, "target");
		SubscriptionType type = SubscriptionType.valueOf(getString(dbo, "type"));
		
		return new Subscription()
				.withId(id)
				.withTarget(target)
				.withType(type);
	}

	public Alert alertFrom(DBObject dbo) {
		String id = dbo.get("_id").toString();
		DateTime timestamp = getDateTime(dbo, "timestamp");
		
		return new Alert()
				.withId(id)
				.withTimestamp(timestamp);
	}

	public DBObject checkToDBObject(Check check) {
		return new BasicDBObject(propertiesToMap(check));
	}
	
	public DBObject subscriptionToDBObject(Subscription subscription) {
		return new BasicDBObject(propertiesToMap(subscription));
	}
	
	public DBObject alertToDBObject(Alert alert) {
		return new BasicDBObject(propertiesToMap(alert));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map propertiesToMap(Check check) {
		Map map = new HashMap();
		map.put("_id", check.getId());
		map.put("name", check.getName());
		map.put("target", check.getTarget());
		return map;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map propertiesToMap(Subscription subscription) {
		Map map = new HashMap();
		map.put("_id", subscription.getId());
		map.put("target", subscription.getTarget());
		map.put("type", subscription.getType().toString());
		return map;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map propertiesToMap(Alert alert) {
		Map map = new HashMap();
		map.put("_id", alert.getId());
		map.put("timestamp", new Date(alert.getTimestamp().getMillis()));
		return map;
	}

	private DateTime getDateTime(DBObject dbo, String key) {
		Date date = (Date)dbo.get(key);
		if (date != null) {
			return new DateTime(date.getTime());
		}
		return null;
	}
	
	private String getString(DBObject dbo, String key) {
		return (String) dbo.get(key);
	}
	
	private BasicDBList getBasicDBList(DBObject dbo, String key) {
		BasicDBList result = (BasicDBList) dbo.get(key);
		if (result == null) {
			result = new BasicDBList();
		}
		return result;
	}
	
}
