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
package com.seyren.api.jaxrs;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.seyren.core.domain.GraphiteInstance;

/**
 * Resource definition for the Graphite instance endpoints.
 * 
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 */
@Path("/")
public interface GraphiteInstancesResource {
    
    @GET
    @Path("/graphite-instances")
    @Produces(MediaType.APPLICATION_JSON)
    Response getGraphiteInstances();
    
    @POST
    @Path("/graphite-instances")
    @Consumes(MediaType.APPLICATION_JSON)
    Response createGraphiteInstance(GraphiteInstance graphiteInstance);
    
    @GET
    @Path("/graphite-instances/{graphiteInstanceId}")
    @Produces(MediaType.APPLICATION_JSON)
    Response getGraphiteInstance(@PathParam("graphiteInstanceId") String graphiteInstanceId);
    
    @PUT
    @Path("/graphite-instances/{graphiteInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response updateGraphiteInstance(
            @PathParam("graphiteInstanceId") String graphiteInstanceId,
            GraphiteInstance graphiteInstance);
    
    @DELETE
    @Path("/graphite-instances/{graphiteInstanceId}")
    Response deleteGraphiteInstance(@PathParam("graphiteInstanceId") String graphiteInstanceId);
}
