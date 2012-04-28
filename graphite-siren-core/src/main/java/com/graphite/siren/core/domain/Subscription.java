package com.graphite.siren.core.domain;

public class Subscription {

	private SubscriptionType type;
	private String target;
	
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
	
}
