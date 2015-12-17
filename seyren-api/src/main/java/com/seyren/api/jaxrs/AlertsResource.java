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

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.seyren.api.util.DateTimeParam;

@Path("/")
public interface AlertsResource {
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/checks/{checkId}/alerts")
    Response getAlertsForCheck(@PathParam("checkId") String checkId,
            @QueryParam("start") @DefaultValue("0") int start,
            @QueryParam("items") @DefaultValue("20") int items);
    
    @DELETE
    @Path("/checks/{checkId}/alerts")
    Response deleteAlertsForCheck(@PathParam("checkId") String checkId,
            @QueryParam("before") @DefaultValue("") DateTimeParam before);
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/alerts")
    Response getAlerts(@QueryParam("start") @DefaultValue("0") int start,
            @QueryParam("items") @DefaultValue("20") int items);
    
}
