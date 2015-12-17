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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
import org.bson.types.ObjectId;
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
        String description = getString(dbo, "description");
        String target = getString(dbo, "target");
        String from = Strings.emptyToNull(getString(dbo, "from"));
        String until = Strings.emptyToNull(getString(dbo, "until"));
        BigDecimal warn = getBigDecimal(dbo, "warn");
        BigDecimal error = getBigDecimal(dbo, "error");
        boolean enabled = getBoolean(dbo, "enabled");
        boolean live = getOptionalBoolean(dbo, "live", false);
        boolean allowNoData = getOptionalBoolean(dbo, "allowNoData", false);
        AlertType state = AlertType.valueOf(getString(dbo, "state"));
        DateTime lastCheck = getDateTime(dbo, "lastCheck");
        List<Subscription> subscriptions = new ArrayList<Subscription>();
        BasicDBList list = getBasicDBList(dbo, "subscriptions");
        for (Object o : list) {
            subscriptions.add(subscriptionFrom((DBObject) o));
        }
        
        return new Check().withId(id)
                .withName(name)
                .withDescription(description)
                .withTarget(target)
                .withFrom(from)
                .withUntil(until)
                .withWarn(warn)
                .withError(error)
                .withEnabled(enabled)
                .withLive(live)
                .withAllowNoData(allowNoData)
                .withState(state)
                .withLastCheck(lastCheck)
                .withSubscriptions(subscriptions);
    }
    
    public Subscription subscriptionFrom(DBObject dbo) {
        String id = dbo.get("_id").toString();
        String target = getString(dbo, "target");
        SubscriptionType type = getSubscriptionType(getString(dbo, "type"));
        boolean su = getBoolean(dbo, "su");
        boolean mo = getBoolean(dbo, "mo");
        boolean tu = getBoolean(dbo, "tu");
        boolean we = getBoolean(dbo, "we");
        boolean th = getBoolean(dbo, "th");
        boolean fr = getBoolean(dbo, "fr");
        boolean sa = getBoolean(dbo, "sa");
        boolean ignoreWarn = getOptionalBoolean(dbo, "ignoreWarn", false);
        boolean ignoreError = getOptionalBoolean(dbo, "ignoreError", false);
        boolean ignoreOk = getOptionalBoolean(dbo, "ignoreOk", false);
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
                .withIgnoreWarn(ignoreWarn)
                .withIgnoreError(ignoreError)
                .withIgnoreOk(ignoreOk)
                .withFromTime(fromTime)
                .withToTime(toTime)
                .withEnabled(enabled);
    }
    
    public Alert alertFrom(DBObject dbo) {
        String id = dbo.get("_id").toString();
        String checkId = getString(dbo, "checkId");
        BigDecimal value = getBigDecimal(dbo, "value");
        String target = getString(dbo, "target");
        BigDecimal warn = getBigDecimal(dbo, "warn");
        BigDecimal error = getBigDecimal(dbo, "error");
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
        map.put("description", check.getDescription());
        map.put("target", check.getTarget());
        map.put("from", check.getFrom());
        map.put("until", check.getUntil());
        if (check.getWarn() != null) {
            map.put("warn", check.getWarn().toPlainString());
        }
        if (check.getError() != null) {
            map.put("error", check.getError().toPlainString());
        }
        map.put("enabled", check.isEnabled());
        map.put("live", check.isLive());
        map.put("allowNoData", check.isAllowNoData());
        map.put("state", check.getState().toString());
        if (check.getLastCheck() != null) {
            map.put("lastCheck", new Date(check.getLastCheck().getMillis()));
        }
        if (check.getSubscriptions() != null && !check.getSubscriptions().isEmpty()) {
            final ArrayList<DBObject> dbSubscriptions = new ArrayList<DBObject>();
            for (Subscription s : check.getSubscriptions()) {
                final BasicDBObject dbObject = new BasicDBObject(propertiesToMap(s));
                if (dbObject.get("_id") == null) {
                    dbObject.put("_id", new ObjectId().toHexString());
                }
                dbSubscriptions.add(dbObject);
            }

            map.put("subscriptions", dbSubscriptions);
        }
        return map;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Map propertiesToMap(Subscription subscription) {
        Map map = new HashMap();
        map.put("_id", subscription.getId());
        map.put("target", subscription.getTarget());
        if (subscription.getType() != null) {
            map.put("type", subscription.getType().toString());
        }
        map.put("su", subscription.isSu());
        map.put("mo", subscription.isMo());
        map.put("tu", subscription.isTu());
        map.put("we", subscription.isWe());
        map.put("th", subscription.isTh());
        map.put("fr", subscription.isFr());
        map.put("sa", subscription.isSa());
        map.put("ignoreWarn", subscription.isIgnoreWarn());
        map.put("ignoreError", subscription.isIgnoreError());
        map.put("ignoreOk", subscription.isIgnoreOk());
        if (subscription.getFromTime() != null) {
            map.put("fromHour", subscription.getFromTime().getHourOfDay());
            map.put("fromMin", subscription.getFromTime().getMinuteOfHour());
        }
        if (subscription.getToTime() != null) {
            map.put("toHour", subscription.getToTime().getHourOfDay());
            map.put("toMin", subscription.getToTime().getMinuteOfHour());
        }
        map.put("enabled", subscription.isEnabled());
        return map;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Map propertiesToMap(Alert alert) {
        Map map = new HashMap();
        map.put("_id", alert.getId());
        map.put("checkId", alert.getCheckId());
        map.put("target", alert.getTarget());
        map.put("targetHash", alert.getTargetHash());
        if (alert.getValue() != null) {
            map.put("value", alert.getValue().toPlainString());
        }
        if (alert.getWarn() != null) {
            map.put("warn", alert.getWarn().toPlainString());
        }
        if (alert.getError() != null) {
            map.put("error", alert.getError().toPlainString());
        }
        map.put("fromType", alert.getFromType().toString());
        map.put("toType", alert.getToType().toString());
        map.put("timestamp", new Date(alert.getTimestamp().getMillis()));
        return map;
    }
    
    private boolean getBoolean(DBObject dbo, String key) {
        return (Boolean) dbo.get(key);
    }
    
    private boolean getOptionalBoolean(DBObject dbo, String key, boolean defaultValue) {
        Object value = dbo.get(key);
        if (value == null) {
            return defaultValue;
        }
        return (Boolean) value;
    }
    
    private DateTime getDateTime(DBObject dbo, String key) {
        Date date = (Date) dbo.get(key);
        if (date != null) {
            return new DateTime(date.getTime());
        }
        return null;
    }
    
    private LocalTime getLocalTime(DBObject dbo, String hourKey, String minKey) {
        Integer hour = getInteger(dbo, hourKey);
        Integer min = getInteger(dbo, minKey);
        return (hour == null || min == null) ? null : new LocalTime(hour, min);
    }
    
    private String getString(DBObject dbo, String key) {
        return (String) dbo.get(key);
    }
    
    private BigDecimal getBigDecimal(DBObject dbo, String key) {
        Object result = dbo.get(key);
        if (result == null) {
            return null;
        }
        return new BigDecimal(result.toString());
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
    
    private SubscriptionType getSubscriptionType(String value) {
        return value == null ? null : SubscriptionType.valueOf(value);
    }
    
}
