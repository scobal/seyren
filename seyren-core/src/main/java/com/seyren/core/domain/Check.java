package com.seyren.core.domain;

import java.util.ArrayList;
import java.util.List;

import com.seyren.core.service.NotificationService;

/**
 * This class represents a graphite target that needs to be monitored.
 * 
 * It stores historical alerts and current subscriptions
 * 
 * @author mark
 *
 */
public class Check {

	private String id;
	private String name;
	private String target;
	private Double warn;
	private Double error;
	private boolean enabled;
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

	public Double getWarn() {
		return warn;
	}

	public void setWarn(Double warn) {
		this.warn = warn;
	}
	
	public Check withWarn(Double warn) {
		setWarn(warn);
		return this;
	}

	public Double getError() {
		return error;
	}

	public void setError(Double error) {
		this.error = error;
	}
	
	public Check withError(Double error) {
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

    /**
     * Report to interested subscribers that an alert has fired
     * @param alert The alert
     * @param notificationService
     */
    public void notify(Alert alert, NotificationService notificationService) {
        for (Subscription subscription : subscriptions) {
        	if (subscription.shouldNotify(alert)) {
        		subscription.notify(alert, notificationService);
        	} 
        }
    }
    
	public boolean isBeyondWarnThreshold(Double value) {
		if (isTheValueBeingHighBad()) {
			return value >= getWarn();
		}
		return value <= getWarn();
	}

	public boolean isBeyondErrorThreshold(Double value) {
		if (isTheValueBeingHighBad()) {
			return value >= getError();
		}
		return value <= getError();
	}
	
	private boolean isTheValueBeingHighBad() {
		return getWarn() <= getError();
	}
}
