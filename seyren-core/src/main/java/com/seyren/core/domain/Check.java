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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers.BigDecimalDeserializer;
import com.fasterxml.jackson.datatype.joda.ser.DateTimeSerializer;
import com.seyren.core.util.math.BigDecimalSerializer;

/**
 * This class represents a graphite target that needs to be monitored.
 * 
 * It stores current subscriptions
 * 
 * @author mark
 * 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Check {
    
    private String id;
    private String name;
    private String description;
    private String target;
    private String from;
    private String until;
    private BigDecimal warn;
    private BigDecimal error;
    private boolean enabled;
    private boolean live;
    private boolean allowNoData;
    private AlertType state;
    private DateTime lastCheck;
    private List<Subscription> subscriptions = new ArrayList<Subscription>();
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Check withId(String id) {
        setId(id);
        return this;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Check withName(String name) {
        setName(name);
        return this;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Check withDescription(String description) {
        this.description = description;
        return this;
    }
    
    public String getTarget() {
        return target;
    }
    
    public void setTarget(String target) {
        this.target = target;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Check withFrom(String from) {
        setFrom(from);
        return this;
    }

    public String getUntil() {
        return until;
    }

    public void setUntil(String until) {
        this.until = until;
    }

    public Check withUntil(String until) {
        setUntil(until);
        return this;
    }

    public Check withTarget(String target) {
        setTarget(target);
        return this;
    }

    @JsonSerialize(using = BigDecimalSerializer.class)
    public BigDecimal getWarn() {
        return warn;
    }
    
    @JsonDeserialize(using = BigDecimalDeserializer.class)
    public void setWarn(BigDecimal warn) {
        this.warn = warn;
    }
    
    public Check withWarn(BigDecimal warn) {
        setWarn(warn);
        return this;
    }
    
    @JsonSerialize(using = BigDecimalSerializer.class)
    public BigDecimal getError() {
        return error;
    }
    
    @JsonDeserialize(using = BigDecimalDeserializer.class)
    public void setError(BigDecimal error) {
        this.error = error;
    }
    
    public Check withError(BigDecimal error) {
        setError(error);
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Check withEnabled(boolean enabled) {
        setEnabled(enabled);
        return this;
    }

    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }

    public Check withLive(boolean live) {
        setLive(live);
        return this;
    }
    
    public boolean isAllowNoData() {
        return allowNoData;
    }
    
    public void setAllowNoData(boolean allowNoData) {
        this.allowNoData = allowNoData;
    }
    
    public Check withAllowNoData(boolean allowNoData) {
        setAllowNoData(allowNoData);
        return this;
    }
    
    public AlertType getState() {
        return state;
    }
    
    public void setState(AlertType state) {
        this.state = state;
    }
    
    @JsonSerialize(using = DateTimeSerializer.class)
    public DateTime getLastCheck() {
        return lastCheck;
    }
    
    public void setLastCheck(DateTime lastCheck) {
        this.lastCheck = lastCheck;
    }
    
    public Check withLastCheck(DateTime lastCheck) {
        setLastCheck(lastCheck);
        return this;
    }
    
    public Check withState(AlertType state) {
        setState(state);
        return this;
    }
    
    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }
    
    public void setSubscriptions(List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }
    
    public Check withSubscriptions(List<Subscription> subscriptions) {
        setSubscriptions(subscriptions);
        return this;
    }
    
}
