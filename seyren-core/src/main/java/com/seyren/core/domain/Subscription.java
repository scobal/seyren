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
package com.seyren.core.domain;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.seyren.core.util.datetime.LocalTimeDeserializer;
import com.seyren.core.util.datetime.LocalTimeSerializer;

/**
 * This class represents something wanting to be notified of an alert
 * 
 * @author mark
 * 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Subscription {
    
    private String id;
    private String target;
    private SubscriptionType type;
    private boolean su, mo, tu, we, th, fr, sa;
    private boolean ignoreWarn, ignoreError, ignoreOk;
    private LocalTime fromTime;
    private LocalTime toTime;
    private boolean enabled;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Subscription withId(String id) {
        setId(id);
        return this;
    }
    
    public String getTarget() {
        return target;
    }
    
    public void setTarget(String target) {
        this.target = target;
    }
    
    public Subscription withTarget(String target) {
        setTarget(target);
        return this;
    }
    
    public SubscriptionType getType() {
        return type;
    }
    
    public void setType(SubscriptionType type) {
        this.type = type;
    }
    
    public Subscription withType(SubscriptionType type) {
        setType(type);
        return this;
    }
    
    public boolean isSu() {
        return su;
    }
    
    public void setSu(boolean su) {
        this.su = su;
    }
    
    public Subscription withSu(boolean su) {
        setSu(su);
        return this;
    }
    
    public boolean isMo() {
        return mo;
    }
    
    public void setMo(boolean mo) {
        this.mo = mo;
    }
    
    public Subscription withMo(boolean mo) {
        setMo(mo);
        return this;
    }
    
    public boolean isTu() {
        return tu;
    }
    
    public void setTu(boolean tu) {
        this.tu = tu;
    }
    
    public Subscription withTu(boolean tu) {
        setTu(tu);
        return this;
    }
    
    public boolean isWe() {
        return we;
    }
    
    public void setWe(boolean we) {
        this.we = we;
    }
    
    public Subscription withWe(boolean we) {
        setWe(we);
        return this;
    }
    
    public boolean isTh() {
        return th;
    }
    
    public void setTh(boolean th) {
        this.th = th;
    }
    
    public Subscription withTh(boolean th) {
        setTh(th);
        return this;
    }
    
    public boolean isFr() {
        return fr;
    }
    
    public void setFr(boolean fr) {
        this.fr = fr;
    }
    
    public Subscription withFr(boolean fr) {
        setFr(fr);
        return this;
    }
    
    public boolean isSa() {
        return sa;
    }
    
    public void setSa(boolean sa) {
        this.sa = sa;
    }
    
    public Subscription withSa(boolean sa) {
        setSa(sa);
        return this;
    }
    
    public boolean isIgnoreWarn() {
        return ignoreWarn;
    }
    
    public void setIgnoreWarn(boolean ignoreWarn) {
        this.ignoreWarn = ignoreWarn;
    }
    
    public Subscription withIgnoreWarn(boolean ignoreWarn) {
        setIgnoreWarn(ignoreWarn);
        return this;
    }
    
    public boolean isIgnoreError() {
        return ignoreError;
    }
    
    public void setIgnoreError(boolean ignoreError) {
        this.ignoreError = ignoreError;
    }
    
    public Subscription withIgnoreError(boolean ignoreError) {
        setIgnoreError(ignoreError);
        return this;
    }
    
    public boolean isIgnoreOk() {
        return ignoreOk;
    }
    
    public void setIgnoreOk(boolean ignoreOk) {
        this.ignoreOk = ignoreOk;
    }
    
    public Subscription withIgnoreOk(boolean ignoreOk) {
        setIgnoreOk(ignoreOk);
        return this;
    }
    
    @JsonSerialize(using = LocalTimeSerializer.class)
    public LocalTime getFromTime() {
        return fromTime;
    }
    
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    public void setFromTime(LocalTime fromTime) {
        this.fromTime = fromTime;
    }
    
    public Subscription withFromTime(LocalTime fromTime) {
        setFromTime(fromTime);
        return this;
    }
    
    @JsonSerialize(using = LocalTimeSerializer.class)
    public LocalTime getToTime() {
        return toTime;
    }
    
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    public void setToTime(LocalTime toTime) {
        this.toTime = toTime;
    }
    
    public Subscription withToTime(LocalTime toTime) {
        setToTime(toTime);
        return this;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public Subscription withEnabled(boolean enabled) {
        setEnabled(enabled);
        return this;
    }
    
    public boolean shouldNotify(DateTime time, AlertType alertType) {
        if (!isEnabled()) {
            return false;
        }
        
        boolean isTimeGood = isCorrectDayOfWeek(time) && isCorrectHourOfDay(time);
        
        if (!isTimeGood) {
            return false;
        }
        
        if (alertType == AlertType.ERROR && ignoreError) {
            return false;
        }
        
        if (alertType == AlertType.WARN && ignoreWarn) {
            return false;
        }
        
        if (alertType == AlertType.OK && ignoreOk) {
            return false;
        }
        
        return true;
    }
    
    private boolean isCorrectHourOfDay(DateTime time) {
        LocalTime alertTime = new LocalTime(time.getHourOfDay(), time.getMinuteOfHour());
        return alertTime.isAfter(getFromTime()) && alertTime.isBefore(getToTime());
    }
    
    private boolean isCorrectDayOfWeek(DateTime time) {
        int day = time.getDayOfWeek();
        if (day == 1 && isMo())
            return true;
        if (day == 2 && isTu())
            return true;
        if (day == 3 && isWe())
            return true;
        if (day == 4 && isTh())
            return true;
        if (day == 5 && isFr())
            return true;
        if (day == 6 && isSa())
            return true;
        if (day == 7 && isSu())
            return true;
        return false;
    }
    
}
