package com.seyren.mongo;

import com.seyren.core.domain.Subscription;

public class MockSubscription extends Subscription {

	private boolean notificationSent = false;
	
	public void sendNotification() {
		this.notificationSent = true;
	}
	
	public boolean notificationSent(){
		return this.notificationSent;
	}
	
	public void reset(){
		this.notificationSent = false;
	}

	
}
