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
package com.seyren.api.bean;


import com.seyren.api.jaxrs.AuthenticationResource;
import com.seyren.api.provider.AuthenticationProvider;
import com.seyren.api.util.RequestValidator;
import com.seyren.core.domain.SubscriptionPermissions;
import com.seyren.core.domain.User;
import com.seyren.core.store.PermissionsStore;
import com.seyren.core.util.config.SeyrenConfig;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

@Named
public class AuthenticationBean implements AuthenticationResource {
    private final SeyrenConfig seyrenConfig;
    private PermissionsStore permissionsStore;
    private RequestValidator requestValidator;
    private AuthenticationProvider provider;

    @Inject
    public AuthenticationBean(SeyrenConfig seyrenConfig, PermissionsStore permissionsStore, RequestValidator requestValidator, AuthenticationProvider provider) {
        this.seyrenConfig = seyrenConfig;
        this.permissionsStore = permissionsStore;
        this.requestValidator = requestValidator;
        this.provider = provider;
    }

    @Override
    public Response authenticateUser(User authentication) {
        //seyern admin login
        if (authentication.getUsername().equals(seyrenConfig.getAdminUser()) &&
                authentication.getPassword().equals(seyrenConfig.getAdminPass())) {
            authentication.setAdmin(true);
            authentication.setAuthenticated(true);
            return Response.status(Response.Status.CREATED).entity(authentication).build();
        }

        //provider login implementation
        Boolean validCredentials = provider.isValidCredentials(authentication);

        return Response.status(validCredentials ? Response.Status.CREATED : Response.Status.UNAUTHORIZED).entity(authentication).build();
    }

    @Override
    public Response getUsers(String name, HttpHeaders headers) {
        Boolean adminRequest = requestValidator.requestCommingFromAdmin(headers.getRequestHeaders());

        if(!adminRequest) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.ok().entity(provider.getUsers(name)).build();
    }

    @Override
    public Response getSubscriptionPermissions(String name, HttpHeaders headers) {
        Response invalidResponse = requestIsInvalid(name, headers);

        if(invalidResponse != null) {
            return invalidResponse;
        }

        SubscriptionPermissions checkPermissions = permissionsStore.getPermissions(name);
        return Response.ok().entity(checkPermissions).build();
    }

    @Override
    public Response setSubscriptionPermissions(String name, SubscriptionPermissions permis, HttpHeaders headers) {
        Response invalidResponse = requestIsInvalid(name, headers);

        if(invalidResponse != null) {
            return invalidResponse;
        }

        SubscriptionPermissions checkPermissions = permissionsStore.getPermissions(name);
        if(checkPermissions.getName() == null) {
            permissionsStore.createPermissions(name, permis.getWrite());
        } else {
            permissionsStore.updatePermissions(name, permis.getWrite());
        }

        return Response.noContent().build();
    }

    /**
     * Validates to see if the request is valid or not
     * @param name
     * @param headers
     * @return
     */
    private Response requestIsInvalid(String name, HttpHeaders headers) {
        boolean validRequest =  requestValidator.isAuthenticatedRequest(headers, true);
        if (!validRequest) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        if(!provider.isValidUser(name)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return  null;
    }
}
