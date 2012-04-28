package com.graphite.siren.mongo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.graphite.siren.core.domain.Alert;
import com.graphite.siren.core.domain.Check;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class MongoMapper {

	public Check checkFrom(DBObject dbo) {
		String id = dbo.get("_id").toString();
		String target = getString(dbo, "target");
		
		List<Alert> alerts = new ArrayList<Alert>();
		BasicDBList list = (BasicDBList) dbo.get("alerts");
		if (list != null) {
			for (Object o : list) {
				alerts.add(alertFrom((DBObject) o));
			}
		}

		return new Check().withId(id)
				.withTarget(target)
				.withAlerts(alerts);
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
	
	public DBObject alertToDBObject(Alert alert) {
		return new BasicDBObject(propertiesToMap(alert));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map propertiesToMap(Check check) {
		Map map = new HashMap();
		map.put("_id", check.getId());
		map.put("target", check.getTarget());
		return map;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map propertiesToMap(Alert alert) {
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
	
}
