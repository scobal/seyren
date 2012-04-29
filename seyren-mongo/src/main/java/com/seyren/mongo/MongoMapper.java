package com.seyren.mongo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.seyren.core.domain.Alert;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.EmailSubscription;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;

public class MongoMapper {

	public Check checkFrom(DBObject dbo) {
		String id = dbo.get("_id").toString();
		String name = getString(dbo, "name");
		String target = getString(dbo, "target");
		String warn = getString(dbo, "warn");
		String error = getString(dbo, "error");
		boolean enabled = getBoolean(dbo, "enabled");
		
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
				.withWarn(warn)
				.withError(error)
				.withEnabled(enabled)
				.withAlerts(alerts)
				.withSubscriptions(subscriptions);
	}

	public Subscription subscriptionFrom(DBObject dbo) {
		String id = dbo.get("_id").toString();
		String target = getString(dbo, "target");
		SubscriptionType type = SubscriptionType.valueOf(getString(dbo, "type"));

        if (type.equals(SubscriptionType.EMAIL)) {
            return new EmailSubscription()
                    .withId(id)
                    .withTarget(target)
                    .withType(type);
        }

		return new Subscription()
				.withId(id)
				.withTarget(target)
				.withType(type);
	}

	public Alert alertFrom(DBObject dbo) {
		String id = dbo.get("_id").toString();
		String value = getString(dbo, "value");
		String target = getString(dbo, "target");
		String warn = getString(dbo, "warn");
		String error = getString(dbo, "error");
		AlertType fromType = AlertType.valueOf(getString(dbo, "fromType"));
		AlertType toType = AlertType.valueOf(getString(dbo, "toType"));
		DateTime timestamp = getDateTime(dbo, "timestamp");
		
		return new Alert()
				.withId(id)
				.withValue(value)
				.withTarget(target)
				.withWarn(warn)
				.withError(error)
				.withFromType(fromType)
				.withToType(toType)
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
		map.put("warn", check.getWarn());
		map.put("error", check.getError());
		map.put("enabled", check.isEnabled());
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
		map.put("value", alert.getValue());
		map.put("target", alert.getTarget());
		map.put("warn", alert.getWarn());
		map.put("error", alert.getError());
		map.put("fromType", alert.getFromType().toString());
		map.put("toType", alert.getToType().toString());
		map.put("timestamp", new Date(alert.getTimestamp().getMillis()));
		return map;
	}

	private boolean getBoolean(DBObject dbo, String key) {
		return (Boolean) dbo.get(key);
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
