/* 
 * Copyright (c) 2013 Expedia, Inc. All rights reserved.
 */
package com.seyren.api.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Willie Wheeler (wwheeler@expedia.com)
 */
@Path("/graphite-instances")
public interface GraphiteInstancesResource {
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	Response getGraphiteInstances();
}
