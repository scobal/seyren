package com.graphite.siren.core.domain;

import com.graphite.siren.core.service.NotificationService;
import com.graphite.siren.core.value.Email;
import com.graphite.siren.core.value.EmailAddress;

/**
 * This class represents something wanting to be notified of an alert
 * 
 * @author mark
 *
 */
public class Subscription {

	private String id;
	private String target;
	private SubscriptionType type;
	
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

    /**
     * Report on this alert
     * @param alert
     * @param notificationService
     */
    public void report(Alert alert, NotificationService notificationService) {

    }
}
