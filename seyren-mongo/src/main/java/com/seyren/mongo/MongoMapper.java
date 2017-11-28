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
import java.util.*;

import com.google.common.base.Strings;
import com.seyren.core.domain.*;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class MongoMapper {
    
    public Check checkFrom(DBObject dbo) {
        String id = dbo.get("_id").toString();
        String name = getString(dbo, "name");
        String description = getString(dbo, "description");
        String graphiteBaseUrl = getString(dbo, "graphiteBaseUrl");
        String target = getString(dbo, "target");
        String graphiteUrl = getString(dbo, "graphiteBaseUrl");
        String from = Strings.emptyToNull(getString(dbo, "from"));
        String until = Strings.emptyToNull(getString(dbo, "until"));
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
        Boolean enableConsecutiveChecks = getBooleanValue(dbo, "enableConsecutiveChecks");
        Integer consecutiveChecks = getInteger(dbo, "consecutiveChecks");
        Integer consecutiveChecksTolerance = getInteger(dbo, "consecutiveChecksTolerance");
        Boolean consecutiveChecksTriggered = getBooleanValue(dbo, "consecutiveChecksTriggered");
        String asgName = getString(dbo,"asgName");

        String checkType = getString(dbo,"checkType");
        Check check = null;
        if(checkType == null || checkType.equalsIgnoreCase("threshold"))
        {
            BigDecimal warn = getBigDecimal(dbo, "warn");
            BigDecimal error = getBigDecimal(dbo, "error");
            check =  new ThresholdCheck()
                    .withWarn(warn)
                    .withError(error);

        }

        else
        {
            BigDecimal absoluteDiff = getBigDecimal(dbo, "absoluteDiff");
            Double relativeDiff = getDouble(dbo, "relativeDiff");
            Integer minConsecutiveViolations = getInteger(dbo, "minConsecutiveViolations");
            check = new OutlierCheck()
                    .withAbsoluteDiff(absoluteDiff)
                    .withRelativeDiff(relativeDiff)
                    .withMinConsecutiveViolations(minConsecutiveViolations)
                    .withAsgName(asgName);
        }

        check = check.withId(id)
                .withName(name)
                .withDescription(description)
                .withGraphiteBaseUrl(graphiteBaseUrl)
                .withTarget(target)
                .withGraphiteBaseUrl(graphiteUrl)
                .withFrom(from)
                .withUntil(until)
                .withEnabled(enabled)
                .withLive(live)
                .withAllowNoData(allowNoData)
                .withState(state)
                .withLastCheck(lastCheck)
                .withSubscriptions(subscriptions)
                .withEnableConsecutiveChecks(enableConsecutiveChecks)
                .withConsecutiveChecks(consecutiveChecks)
                .withConsecutiveChecksTolerance(consecutiveChecksTolerance)
                .withConsecutiveChecksTriggered(consecutiveChecksTriggered);
        return check;
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
        String position = getString(dbo, "position");
        
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
                .withEnabled(enabled)
                .withPosition(position);
    }
    
    public Alert alertFrom(DBObject dbo) {
        String id = dbo.get("_id").toString();
        String checkId = getString(dbo, "checkId");
        BigDecimal value = getBigDecimal(dbo, "value");
        String target = getString(dbo, "target");
        AlertType fromType = AlertType.valueOf(getString(dbo, "fromType"));
        AlertType toType = AlertType.valueOf(getString(dbo, "toType"));
        DateTime timestamp = getDateTime(dbo, "timestamp");

        String alertType = getString(dbo, "alertType");
        Alert alert = null;
        if (alertType == null || alertType.equalsIgnoreCase("threshold"))
        {
            BigDecimal warn = getBigDecimal(dbo, "warn");
            BigDecimal error = getBigDecimal(dbo, "error");
            alert = new ThresholdAlert()
                    .withWarn(warn)
                    .withError(error);
        }

        else
        {
                BigDecimal absoluteDiff = getBigDecimal(dbo, "absoluteDiff");
                Double relativeDiff = getDouble(dbo, "relativeDiff");
                Integer consecutiveAlertCount = getInteger(dbo, "consecutiveAlertCount");

            alert =  new OutlierAlert()
                    .withAbsoluteDiff(absoluteDiff)
                    .withRelativeDiff(relativeDiff)
                    .withConsecutiveAlertCount(consecutiveAlertCount);

        }

        alert = alert.withId(id)
                .withCheckId(checkId)
                .withValue(value)
                .withTarget(target)
                .withFromType(fromType)
                .withToType(toType)
                .withTimestamp(timestamp);
        return alert;
    }

    public SubscriptionPermissions permissionsFrom(DBObject dbo) {
        String name = dbo.get("_id").toString();
        String write = getString(dbo, "write");
        SubscriptionPermissions permissions = new SubscriptionPermissions();
        permissions.setName(name);
        permissions.setWriteTypes(write.split(";"));
        return permissions;
    }

    public User userFrom(DBObject dbo) {
        String userId = dbo.get("_id").toString();
        String username = dbo.get("username").toString();
        String passwordEncoded = dbo.get("password").toString();
        Set<String> roles = new HashSet<String>(Arrays.asList(dbo.get("roles").toString().split(";")));
        User user = new User(username, passwordEncoded);
        user.setId(userId);
        user.setRoles(roles);
        return user;
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

    public DBObject permissionToDBObject(SubscriptionPermissions permissions) {
        return new BasicDBObject(propertiesToMap(permissions));
    }

     public DBObject userToDBObject(User user) {
         return new BasicDBObject(propertiesToMap(user));
     }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Map propertiesToMap(Check check) {
        Map map = new HashMap();
        map.put("_id", check.getId());
        map.put("name", check.getName());
        map.put("description", check.getDescription());
        map.put("graphiteBaseUrl", check.getGraphiteBaseUrl());
        map.put("target", check.getTarget());
        map.put("graphiteBaseUrl", check.getGraphiteBaseUrl());
        map.put("from", check.getFrom());
        map.put("until", check.getUntil());

        if(check instanceof ThresholdCheck)
        {
            map.put("checkType","threshold");
            ThresholdCheck thresholdCheck = (ThresholdCheck)check;
            if (thresholdCheck.getWarn() != null) {
                map.put("warn", thresholdCheck.getWarn().toPlainString());
            }
            if (thresholdCheck.getError() != null) {
                map.put("error", thresholdCheck.getError().toPlainString());
            }
        }

        else if(check instanceof OutlierCheck)
        {
            OutlierCheck outlierCheck = (OutlierCheck)check;
            map.put("checkType","outlier");
            if (outlierCheck.getAbsoluteDiff() != null) {
                map.put("absoluteDiff", outlierCheck.getAbsoluteDiff().toPlainString());
            }

            if(outlierCheck.getRelativeDiff() !=null )
            {
                map.put("relativeDiff",outlierCheck.getRelativeDiff());
            }

            if(outlierCheck.getMinConsecutiveViolations()!=null)
            {
                map.put("minConsecutiveViolations",outlierCheck.getMinConsecutiveViolations());
            }

            if(outlierCheck.getAsgName()!=null)
            {
                map.put("asgName", outlierCheck.getAsgName());
            }

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
        map.put("enableConsecutiveChecks",check.isEnableConsecutiveChecks());
        map.put("consecutiveChecks",check.getConsecutiveChecks());
        map.put("consecutiveChecksTolerance", check.getConsecutiveChecksTolerance());
        map.put("consecutiveChecksTriggered", check.isConsecutiveChecksTriggered());
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
        if (subscription.getPosition() != null) {
        	map.put("position", subscription.getPosition());
		}
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
        if(alert instanceof ThresholdAlert)
        {
            ThresholdAlert thresholdAlert = (ThresholdAlert)alert;

            map.put("alertType","threshold");
            if (thresholdAlert.getWarn() != null) {
                map.put("warn", thresholdAlert.getWarn().toPlainString());
            }
            if (thresholdAlert.getError() != null) {
                map.put("error", thresholdAlert.getError().toPlainString());
            }
        }

        else if(alert instanceof OutlierAlert)
        {
            OutlierAlert outlierAlert = (OutlierAlert)alert;
            map.put("alertType","outlier");
            if (outlierAlert.getAbsoluteDiff() != null) {
                map.put("absoluteDiff", outlierAlert.getAbsoluteDiff().toPlainString());
            }

            if(outlierAlert.getRelativeDiff() !=null )
            {
                map.put("relativeDiff",outlierAlert.getRelativeDiff());
            }

            map.put("consecutiveAlertCount",outlierAlert.getConsecutiveAlertCount());

        }
        map.put("fromType", alert.getFromType().toString());
        map.put("toType", alert.getToType().toString());
        map.put("timestamp", new Date(alert.getTimestamp().getMillis()));
        return map;
    }

    private Map propertiesToMap(SubscriptionPermissions permissions) {
        Map map = new HashMap();
        map.put("_id", permissions.getName());
        map.put("write", permissions.getWriteTypesDelimited());
        return map;
    }

     private Map propertiesToMap(User user) {
         Map map = new HashMap();
         map.put("_id", user.getId());
         map.put("username", user.getUsername());
         map.put("password", user.getPassword());
         map.put("roles", user.getRolesDelimited());
         return map;
     }

    
    private boolean getBoolean(DBObject dbo, String key) {
        return (Boolean) dbo.get(key);
    }

    private Boolean getBooleanValue(DBObject dbo, String key) {
        if(null != dbo.get(key)) {
            return (Boolean) dbo.get(key);
        }
        return null;
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

    private Double getDouble(DBObject dbo, String key) {
        return (Double) dbo.get(key);
    }
}
