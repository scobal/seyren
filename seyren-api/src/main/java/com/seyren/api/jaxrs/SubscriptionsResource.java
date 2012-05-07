package com.seyren.api.jaxrs;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.seyren.core.domain.Subscription;

@Path("/checks/{checkId}/subscriptions")
public interface SubscriptionsResource {
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	Response createSubscription(@PathParam("checkId") String checkId, Subscription subscription);
	
	@DELETE
	@Path("/{subscriptionId}")
	Response deleteSubscription(@PathParam("checkId") String checkId, @PathParam("subscriptionId") String subscriptionId);

}
