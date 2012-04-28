package com.graphite.siren.core.domain;

import org.joda.time.DateTime;

public class Alert {

	private String id;
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
