package com.graphite.siren.api.jaxrs;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.graphite.siren.core.domain.Subscription;

@Path("/checks/{checkId}/subscriptions")
public interface SubscriptionsResource {
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	Response createSubscription(@PathParam("checkId") String checkId, Subscription subscription);

}
