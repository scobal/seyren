package com.seyren.core.service;

import com.seyren.core.domain.Subscription;

public class MockSubscription extends Subscription {

	private boolean notificationSent = false;
	
	public void sendNotification() {
		this.notificationSent = true;
	}
	
	public boolean notificationSent(){
		return this.notificationSent;
	}

	
}
