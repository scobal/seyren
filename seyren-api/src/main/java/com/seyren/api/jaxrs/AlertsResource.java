package com.seyren.api.jaxrs;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/checks/{checkId}/alerts")
public interface AlertsResource {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	Response getAlerts(@PathParam("checkId") String checkId,
			@QueryParam("start") @DefaultValue("0") int start,
			@QueryParam("items") @DefaultValue("20") int items);
	
}
