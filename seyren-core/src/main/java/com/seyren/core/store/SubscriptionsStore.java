package com.seyren.core.store;

import com.seyren.core.domain.Subscription;

public interface SubscriptionsStore {

	Subscription createSubscription(String checkId, Subscription subscription);

	void deleteSubscription(String checkId, String subscriptionId);
	
	void updateSubscription(String checkId, Subscription subscription);
	
}
