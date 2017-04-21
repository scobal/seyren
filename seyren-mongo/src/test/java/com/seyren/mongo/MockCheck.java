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
import java.util.List;

import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers.BigDecimalDeserializer;
import com.fasterxml.jackson.datatype.joda.ser.DateTimeSerializer;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.util.math.BigDecimalSerializer;

public class MockCheck extends Check {

	private final Check check;
	
	private final Subscription subscription;
	
	public MockCheck(Check check, Subscription subscription){
		this.check = check;
		this.subscription = subscription;
	}
	
	@Override
    public List<Subscription> getSubscriptions() {
		ArrayList<Subscription> subList = new ArrayList<Subscription>();
		subList.add(subscription);
        return subList;
    }
	
    public Subscription getSubscription() {
        return subscription;
    }
    
	@Override
    public void setSubscriptions(List<Subscription> subscriptions) {
        this.check.setSubscriptions(subscriptions);
    }
	
	@Override
	public String getId() {
        return check.getId();
    }
    
	@Override
    public void setId(String id) {
        this.check.setId(id);
    }
    
	@Override
    public Check withId(String id) {
        check.setId(id);
        return this;
    }
    
	@Override
    public String getName() {
        return check.getName();
    }
    
	@Override
    public void setName(String name) {
        this.check.setName(name);
    }
    
	@Override
    public Check withName(String name) {
        check.setName(name);
        return this;
    }
    
	@Override
    public String getDescription() {
        return check.getDescription();
    }
    
	@Override
    public void setDescription(String description) {
        this.check.setDescription(description);
    }
    
	@Override
    public Check withDescription(String description) {
        this.check.withDescription(description);
        return this;
    }
    
	@Override
    public String getTarget() {
        return check.getTarget();
    }
    
	@Override
    public void setTarget(String target) {
        this.check.setTarget(target);
    }
    
	@Override
    public String getFrom() {
        return this.check.getFrom();
    }
    
	@Override
    public void setFrom(String from) {
        this.check.setFrom(from);
    }
    
	@Override
    public Check withFrom(String from) {
        this.check.setFrom(from);
        return this;
    }
    
	@Override
    public String getUntil() {
        return this.check.getUntil();
    }
    
	@Override
    public void setUntil(String until) {
        this.check.setUntil(until);
    }
    
	@Override
    public Check withUntil(String until) {
        setUntil(until);
        return this;
    }
    
	@Override
    public Check withTarget(String target) {
        setTarget(target);
        return this;
    }
    
	@Override
    @JsonSerialize(using = BigDecimalSerializer.class)
    public BigDecimal getWarn() {
        return this.check.getWarn();
    }
    
	@Override
    @JsonDeserialize(using = BigDecimalDeserializer.class)
    public void setWarn(BigDecimal warn) {
        this.check.setWarn(warn);
    }
    
	@Override
    public Check withWarn(BigDecimal warn) {
        this.check.setWarn(warn);
        return this;
    }
    
	@Override
    @JsonSerialize(using = BigDecimalSerializer.class)
    public BigDecimal getError() {
        return this.check.getError();
    }
    
	@Override
    @JsonDeserialize(using = BigDecimalDeserializer.class)
    public void setError(BigDecimal error) {
        this.check.setError(error);
    }
    
	@Override
    public Check withError(BigDecimal error) {
        this.check.setError(error);
        return this;
    }
    
	@Override
    public boolean isEnabled() {
        return this.check.isEnabled();
    }
    
	@Override
    public void setEnabled(boolean enabled) {
        this.check.setEnabled(enabled);
    }
    
	@Override
    public Check withEnabled(boolean enabled) {
        this.check.setEnabled(enabled);
        return this;
    }
    
	@Override
    public Check withGraphiteBaseUrl(String graphiteBaseUrl) {
        this.check.setGraphiteBaseUrl(graphiteBaseUrl);
        return this;
    }
    
	@Override
    public boolean isLive() {
        return this.check.isLive();
    }
    
	@Override
    public void setLive(boolean live) {
        this.check.setLive(live);
    }
    
	@Override
    public Check withLive(boolean live) {
        this.check.setLive(live);
        return this;
    }
    
	@Override
    public boolean isAllowNoData() {
        return this.check.isAllowNoData();
    }
    
	@Override
    public void setAllowNoData(boolean allowNoData) {
        this.check.setAllowNoData(allowNoData);
    }
    
	@Override
    public Check withAllowNoData(boolean allowNoData) {
        this.check.setAllowNoData(allowNoData);
        return this;
    }
    
	@Override
    public AlertType getState() {
        return this.check.getState();
    }
    
	@Override
    public void setState(AlertType state) {
        this.check.setState(state);
    }
    
	@Override
    @JsonSerialize(using = DateTimeSerializer.class)
    public DateTime getLastCheck() {
        return this.check.getLastCheck();
    }
    
	@Override
    public void setLastCheck(DateTime lastCheck) {
        this.check.setLastCheck(lastCheck);
    }
    
	@Override
    public Check withLastCheck(DateTime lastCheck) {
        setLastCheck(lastCheck);
        return this;
    }
    
	@Override
    public Check withState(AlertType state) {
        setState(state);
        return this;
    }
    

    
	@Override
    public Check withSubscriptions(List<Subscription> subscriptions) {
        setSubscriptions(subscriptions);
        return this;
    }
    
	@Override
    public String getGraphiteBaseUrl() {
        return this.check.getGraphiteBaseUrl();
    }
    
	@Override
    public void setGraphiteBaseUrl(String graphiteBaseUrl) {
        this.check.setGraphiteBaseUrl(graphiteBaseUrl);
    }
	
}
