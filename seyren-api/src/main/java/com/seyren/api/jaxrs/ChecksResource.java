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

import java.util.List;
import java.util.Set;

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

@Path("/")
public interface ChecksResource {

    /**
     * Allows the retrieval of a collection of checks via three distinct algorithms:
     * <ul>
     *     <li>states is non-null and non-empty: will filter all checks by both the states supplied as well as
     *     the value of the enabled parameter.</li>
     *     <li>both fields and regexes are non-null and non-empty: will filter all checks by applying the regex
     *     at index i of regexes to the field at index i of fields.  Length of fields and regexes must be
     *     identical.  If the method parameter "enabled" is specified, then it will override any regex value of
     *     field enabled provided.</li>
     *     <li>default: get all checks as filtered by the presence and value of the enabled parameter.</li>
     * </ul>
     *
     * @param states legal check states @see com.seyren.core.domain.AlertType
     * @param enabled optional, if present, will filter any of the three algorithms
     * @param name (Presently unused)
     * @param fields an ordered list of @see com.seyren.core.domain.Check fields.  If present and non-empty, will
     *               be combined with the correspondingly ordered list of regexes in order to restrict the checks
     *               returned.
     * @param regexes an ordered list of regexes that will be compiled into @java.util.regex.Pattern objects.  If
     *                present and non-empty, will be combined with the correspondingly ordered list of fields in
     *                order to filter the objects returned.
     *
     * @return JSON response containing @see com.seyren.core.domain.Check identified by the parameters supplied.
     */
    @GET
    @Path("/checks")
    @Produces(MediaType.APPLICATION_JSON)
    Response getChecks(@QueryParam("state") Set<String> states,
                       @QueryParam("enabled") Boolean enabled,
                       @QueryParam("name") String name,
                       @QueryParam("fields") List<String> fields,
                       @QueryParam("regexes") List<String> regexes);

    @POST
    @Path("/checks")
    @Consumes(MediaType.APPLICATION_JSON)
    Response createCheck(Check check);
    
    @GET
    @Path("/checks/{checkId}")
    @Produces(MediaType.APPLICATION_JSON)
    Response getCheck(@PathParam("checkId") String checkId);
    
    @PUT
    @Path("/checks/{checkId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response updateCheck(@PathParam("checkId") String checkId, Check check);
    
    @DELETE
    @Path("/checks/{checkId}")
    Response deleteCheck(@PathParam("checkId") String checkId);
    
}
