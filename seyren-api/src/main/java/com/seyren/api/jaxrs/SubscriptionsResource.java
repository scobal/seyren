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

import com.seyren.core.domain.Subscription;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public interface SubscriptionsResource {
    
    @POST
    @Path("/checks/{checkId}/subscriptions")
    @Consumes(MediaType.APPLICATION_JSON)
    Response createSubscription(@PathParam("checkId") String checkId, Subscription subscription, @Context HttpHeaders headers);

    @PUT
    @Path("/checks/{checkId}/subscriptions/{subscriptionId}")
    Response updateSubscription(@PathParam("checkId") String checkId, Subscription subscription, @Context HttpHeaders headers);

    @DELETE
    @Path("/checks/{checkId}/subscriptions/{subscriptionId}")
    Response deleteSubscription(@PathParam("checkId") String checkId, @PathParam("subscriptionId") String subscriptionId, @Context HttpHeaders headers);

    @PUT
    @Path("/checks/{checkId}/subscriptions/{subscriptionId}/test")
    Response testSubscription(@PathParam("checkId") String checkId, @PathParam("subscriptionId") String subscriptionId);
}
