package com.seyren.core.service;

import java.util.List;

import com.seyren.core.domain.Alert;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.service.notification.NotificationService;

public class MockNotificationService implements NotificationService {
	
	
	@Override
	public void sendNotification(Check check, Subscription subscription, List<Alert> alerts)
			throws NotificationFailedException {
		if (subscription instanceof MockSubscription){
			((MockSubscription)subscription).sendNotification();
		}
	}

	@Override
	public boolean canHandle(SubscriptionType subscriptionType) {
		return true;
	}

}
