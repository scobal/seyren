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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.seyren.core.domain.Alert;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;

public class MongoMapper {

	public Check checkFrom(DBObject dbo) {
		String id = dbo.get("_id").toString();
		String name = getString(dbo, "name");
		String target = getString(dbo, "target");
		Double warn = getDouble(dbo, "warn");
		Double error = getDouble(dbo, "error");
		boolean enabled = getBoolean(dbo, "enabled");
		AlertType state = AlertType.valueOf(getString(dbo, "state"));
		
		List<Subscription> subscriptions = new ArrayList<Subscription>();
		BasicDBList list = getBasicDBList(dbo, "subscriptions");
		for (Object o : list) {
			subscriptions.add(subscriptionFrom((DBObject) o));
		}

		return new Check().withId(id)
				.withName(name)
				.withTarget(target)
				.withWarn(warn)
				.withError(error)
				.withEnabled(enabled)
				.withState(state)
				.withSubscriptions(subscriptions);
	}
	
	public Subscription subscriptionFrom(DBObject dbo) {
		String id = dbo.get("_id").toString();
		String target = getString(dbo, "target");
		SubscriptionType type = SubscriptionType.valueOf(getString(dbo, "type"));
		boolean su = getBoolean(dbo, "su");
		boolean mo = getBoolean(dbo, "mo");
		boolean tu = getBoolean(dbo, "tu");
		boolean we = getBoolean(dbo, "we");
		boolean th = getBoolean(dbo, "th");
		boolean fr = getBoolean(dbo, "fr");
		boolean sa = getBoolean(dbo, "sa");
		LocalTime fromTime = getLocalTime(dbo, "fromHour", "fromMin");
		LocalTime toTime = getLocalTime(dbo, "toHour", "toMin");
		boolean enabled = getBoolean(dbo, "enabled");

		return new Subscription()
				.withId(id)
				.withTarget(target)
				.withType(type)
				.withSu(su)
				.withMo(mo)
				.withTu(tu)
				.withWe(we)
				.withTh(th)
				.withFr(fr)
				.withSa(sa)
				.withFromTime(fromTime)
				.withToTime(toTime)
				.withEnabled(enabled);
	}

	public Alert alertFrom(DBObject dbo) {
		String id = dbo.get("_id").toString();
		String checkId = getString(dbo, "checkId");
		Double value = getDouble(dbo, "value");
		String target = getString(dbo, "target");
		Double warn = getDouble(dbo, "warn");
		Double error = getDouble(dbo, "error");
		AlertType fromType = AlertType.valueOf(getString(dbo, "fromType"));
		AlertType toType = AlertType.valueOf(getString(dbo, "toType"));
		DateTime timestamp = getDateTime(dbo, "timestamp");
		
		return new Alert()
				.withId(id)
				.withCheckId(checkId)
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
		map.put("state", check.getState().toString());
		return map;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map propertiesToMap(Subscription subscription) {
		Map map = new HashMap();
		map.put("_id", subscription.getId());
		map.put("target", subscription.getTarget());
		map.put("type", subscription.getType().toString());
		map.put("su", subscription.isSu());
		map.put("mo", subscription.isMo());
		map.put("tu", subscription.isTu());
		map.put("we", subscription.isWe());
		map.put("th", subscription.isTh());
		map.put("fr", subscription.isFr());
		map.put("sa", subscription.isSa());
		map.put("fromHour", subscription.getFromTime().getHourOfDay());
		map.put("fromMin", subscription.getFromTime().getMinuteOfHour());
		map.put("toHour", subscription.getToTime().getHourOfDay());
		map.put("toMin", subscription.getToTime().getMinuteOfHour());
		map.put("enabled", subscription.isEnabled());
		return map;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map propertiesToMap(Alert alert) {
		Map map = new HashMap();
		map.put("_id", alert.getId());
		map.put("checkId", alert.getCheckId());
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
	
	private LocalTime getLocalTime(DBObject dbo, String hourKey, String minKey) {
		return new LocalTime(getInteger(dbo, hourKey), getInteger(dbo, minKey));
	}
	
	private String getString(DBObject dbo, String key) {
		return (String) dbo.get(key);
	}
	
	private Double getDouble(DBObject dbo, String key) {
		return (Double) dbo.get(key);
	}
	
	private Integer getInteger(DBObject dbo, String key) {
		return (Integer) dbo.get(key);
	}
	
	private BasicDBList getBasicDBList(DBObject dbo, String key) {
		BasicDBList result = (BasicDBList) dbo.get(key);
		if (result == null) {
			result = new BasicDBList();
		}
		return result;
	}
	
}
