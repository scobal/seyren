package com.graphite.siren.core.store;

import com.graphite.siren.core.domain.Subscription;

public interface SubscriptionsStore {

	Subscription createSubscription(String checkId, Subscription subscription);
	
}
