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

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.joda.deser.DateTimeDeserializer;
import com.fasterxml.jackson.datatype.joda.ser.DateTimeSerializer;
import com.seyren.core.util.hashing.TargetHash;

/**
 * An instance of this class represents an occurrence of a check that is found
 * to be out of the normal range.
 * 
 * It stores some of the state of the check at the time it occurred.
 * 
 * @author mark
 * 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Alert {
    
    private String id;
    private String checkId;
    private BigDecimal value;
    private String target;
    private BigDecimal warn;
    private BigDecimal error;
    private AlertType fromType;
    private AlertType toType;
    private DateTime timestamp;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Alert withId(String id) {
        setId(id);
        return this;
    }
    
    public String getCheckId() {
        return checkId;
    }
    
    public void setCheckId(String checkId) {
        this.checkId = checkId;
    }
    
    public Alert withCheckId(String checkId) {
        setCheckId(checkId);
        return this;
    }
    
    public BigDecimal getValue() {
        return value;
    }
    
    public void setValue(BigDecimal value) {
        this.value = value;
    }
    
    public Alert withValue(BigDecimal value) {
        setValue(value);
        return this;
    }
    
    public String getTarget() {
        return target;
    }
    
    public void setTarget(String target) {
        this.target = target;
    }
    
    public Alert withTarget(String target) {
        setTarget(target);
        return this;
    }
    
    public String getTargetHash() {
        return TargetHash.create(target);
    }
    
    public BigDecimal getWarn() {
        return warn;
    }
    
    public void setWarn(BigDecimal warn) {
        this.warn = warn;
    }
    
    public Alert withWarn(BigDecimal warn) {
        setWarn(warn);
        return this;
    }
    
    public BigDecimal getError() {
        return error;
    }
    
    public void setError(BigDecimal error) {
        this.error = error;
    }
    
    public Alert withError(BigDecimal error) {
        setError(error);
        return this;
    }
    
    public AlertType getFromType() {
        return fromType;
    }
    
    public void setFromType(AlertType fromType) {
        this.fromType = fromType;
    }
    
    public Alert withFromType(AlertType fromType) {
        setFromType(fromType);
        return this;
    }
    
    public AlertType getToType() {
        return toType;
    }
    
    public void setToType(AlertType toType) {
        this.toType = toType;
    }
    
    public Alert withToType(AlertType toType) {
        setToType(toType);
        return this;
    }
    
    @JsonSerialize(using = DateTimeSerializer.class)
    public DateTime getTimestamp() {
        return timestamp;
    }
    
    @JsonDeserialize(using = DateTimeDeserializer.class)
    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Alert withTimestamp(DateTime timestamp) {
        setTimestamp(timestamp);
        return this;
    }
    
}
