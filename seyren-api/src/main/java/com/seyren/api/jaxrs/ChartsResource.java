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

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/")
public interface ChartsResource {
    
    @GET
    @Produces("image/png")
    @Path("/checks/{checkId}/image")
    Response getChart(@PathParam("checkId") String checkId,
            @QueryParam("width") @DefaultValue("1200") int width,
            @QueryParam("height") @DefaultValue("350") int height,
            @QueryParam("from") @DefaultValue("-24hours") String from,
            @QueryParam("to") String to,
            @QueryParam("hideThresholds") boolean hideThresholds,
            @QueryParam("hideLegend") boolean hideLegend,
            @QueryParam("hideAxes") boolean hideAxes);
    
    @GET
    @Produces("image/png")
    @Path("/chart/{target}")
    Response getCustomChart(@PathParam("target") String target,
            @QueryParam("width") @DefaultValue("1200") int width,
            @QueryParam("height") @DefaultValue("350") int height,
            @QueryParam("from") @DefaultValue("-24hours") String from,
            @QueryParam("to") String to,
            @QueryParam("warn") String warnThreshold,
            @QueryParam("error") String errorThreshold,
            @QueryParam("hideLegend") boolean hideLegend,
            @QueryParam("hideAxes") boolean hideAxes);
    
}
