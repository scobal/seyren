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

import com.seyren.core.domain.Filter;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public interface FiltersResource {
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/checks/filters")
    Response getFilters();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/checks/filters")
    Response saveFilter(Filter filter);

    @DELETE
    @Path("/checks/filters/{filterId}")
    Response deleteFilter(@PathParam("filterId") String filterId);
    
}
