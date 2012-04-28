package com.graphite.siren.core.domain;

import java.util.ArrayList;
import java.util.List;

public class Check {

	private String id;
	private String name;
	private String target;
	private String warn;
	private String error;
	private List<Alert> alerts = new ArrayList<Alert>();
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

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}
	
	public Check withTarget(String target) {
		setTarget(target);
		return this;
	}

	public String getWarn() {
		return warn;
	}

	public void setWarn(String warn) {
		this.warn = warn;
	}
	
	public Check withWarn(String warn) {
		setWarn(warn);
		return this;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
	
	public Check withError(String error) {
		setError(error);
		return this;
	}

	public List<Alert> getAlerts() {
		return alerts;
	}

	public void setAlerts(List<Alert> alerts) {
		this.alerts = alerts;
	}
	
	public Check withAlerts(List<Alert> alerts) {
		setAlerts(alerts);
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
