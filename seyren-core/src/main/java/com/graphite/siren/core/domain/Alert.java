package com.graphite.siren.core.domain;

import org.joda.time.DateTime;

/**
 * An instance of this class represents an occurrence of a check that is found
 * to be out of the normal range.
 * 
 * It stores some of the state of the check at the time it occurred.
 * 
 * @author mark
 * 
 */
public class Alert {

	private String id;
	private String value;
	private String target;
	private String warn;
	private String error;
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
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public Alert withValue(String value) {
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

	public String getWarn() {
		return warn;
	}

	public void setWarn(String warn) {
		this.warn = warn;
	}
	
	public Alert withWarn(String warn) {
		setWarn(warn);
		return this;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
	
	public Alert withError(String error) {
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

	public DateTime getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(DateTime timestamp) {
		this.timestamp = timestamp;
	}
	
	public Alert withTimestamp(DateTime timestamp) {
		setTimestamp(timestamp);
		return this;
	}

}
