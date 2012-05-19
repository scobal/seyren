/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    public Response updateSubscription(String checkId, Subscription subscription) {
        subscriptionsStore.updateSubscription(checkId, subscription);
        return Response.noContent().build();
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
