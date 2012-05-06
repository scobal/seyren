package com.seyren.api.jaxrs;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.seyren.core.domain.Check;

@Path("/checks")
public interface ChecksResource {
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	Response getChecks(@QueryParam("states") String states);
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	Response createCheck(Check check);
	
	@GET
	@Path("/{checkId}")
	@Produces(MediaType.APPLICATION_JSON)
	Response getCheck(@PathParam("checkId") String checkId);
	
	@PUT
	@Path("/{checkId}")
	@Consumes(MediaType.APPLICATION_JSON)
	Response updateCheck(@PathParam("checkId") String checkId, Check check);
	
	@DELETE
	@Path("/{checkId}")
	Response deleteCheck(@PathParam("checkId") String checkId);

}
