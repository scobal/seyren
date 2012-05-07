package com.seyren.api.bean;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;

import com.seyren.api.jaxrs.SubscriptionsResource;
import com.seyren.core.domain.Subscription;
import com.seyren.core.store.SubscriptionsStore;

@Named
public class SubscriptionsBean implements SubscriptionsResource {

	private SubscriptionsStore subscriptionsStore;

	@Inject
	public SubscriptionsBean(SubscriptionsStore subscriptionsStore) {
		this.subscriptionsStore = subscriptionsStore;
	}
	
	@Override
	public Response createSubscription(String checkId, Subscription subscription) {
		Subscription stored = subscriptionsStore.createSubscription(checkId, subscription);
		return Response.created(uri(checkId, stored.getId())).build();
	}

	@Override
	public Response deleteSubscription(String checkId, String subscriptionId) {
		subscriptionsStore.deleteSubscription(checkId, subscriptionId);
		return Response.noContent().build();
	}
	
	private URI uri(String checkId, String subscriptionId) {
		try {
			return new URI("checks/" + checkId + "/subscriptions/" + subscriptionId);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

}
