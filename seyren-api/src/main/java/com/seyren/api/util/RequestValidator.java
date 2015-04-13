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
package com.seyren.api.util;


import com.seyren.api.provider.AuthenticationProvider;
import com.seyren.core.domain.SubscriptionPermissions;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.domain.User;
import com.seyren.core.store.PermissionsStore;
import com.seyren.core.util.config.SeyrenConfig;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.nio.charset.Charset;
import java.util.Base64;

@Named
public class RequestValidator {

    private final SeyrenConfig seyrenConfig;
    private final AuthenticationProvider provider;
    private final PermissionsStore permissionsStore;

    @Inject
    public RequestValidator(SeyrenConfig seyrenConfig, AuthenticationProvider provider, PermissionsStore permissionsStore) {
        this.seyrenConfig = seyrenConfig;
        this.provider = provider;
        this.permissionsStore = permissionsStore;
    }
    /**
     * Validates the Request
     * @param headers
     * @param requiresAdminPermission
     * @return
     */
    public boolean isAuthenticatedRequest(HttpHeaders headers, boolean requiresAdminPermission) {
        User credentials = getCredentialsFromRequest(headers.getRequestHeaders());
        if(credentials == null || !provider.isValidCredentials(credentials)) {
            return false;
        }

        if(requiresAdminPermission) {
            return credentials.getUsername().equals(seyrenConfig.getAdminUser())
                    && credentials.getPassword().equals(seyrenConfig.getAdminPass());

        }
        return true;
    }

    /**
     * If an admin is making the request
     * @param requestHeaders
     * @return
     */
    public Boolean requestCommingFromAdmin(MultivaluedMap<String, String> requestHeaders) {
        User credentialsFromRequest = getCredentialsFromRequest(requestHeaders);
        if(credentialsFromRequest == null) {
            return false;
        }
        return credentialsFromRequest.getUsername().equals(seyrenConfig.getAdminUser())
                && credentialsFromRequest.getPassword().equals(seyrenConfig.getAdminPass());
    }

    /**
     * Get BasicAuthentication Credentials From Request Headers
     * @param requestHeaders
     * @return
     */
    private User getCredentialsFromRequest(MultivaluedMap<String, String> requestHeaders) {
        final String authorization = requestHeaders.getFirst("Authorization");
        if (authorization != null && authorization.startsWith("Basic")) {
            String base64Credentials = authorization.substring("Basic".length()).trim();
            String credentials = new String(Base64.getDecoder().decode(base64Credentials),
                    Charset.forName("UTF-8"));
            String[] split = credentials.split(":", 2);
            User user = new User();
            user.setUsername(split[0]);
            user.setPassword(split[1]);
            return user;
        }
        return null;
    }

    /**
     * If authentication is enabled it will verify if the user can modify the subscription type
     * @param headers
     * @param type
     * @return
     */
    public Response unauthorizedSubscription(HttpHeaders headers, SubscriptionType type) {
        if (seyrenConfig.authenticationEnabled()) {
            boolean authenticatedRequest = isAuthenticatedRequest(headers, false);
            if (authenticatedRequest) {
                return handleNotificationType(getCredentialsFromRequest(headers.getRequestHeaders()).getUsername(), type);
            }
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        return null;
    }

    /**
     * Handles users subscription types
     * @param username
     * @param type
     * @return
     */
    private Response handleNotificationType(String username, SubscriptionType type) {
        SubscriptionPermissions permissions = permissionsStore.getPermissions(username);
        for (String s : permissions.getWrite()) {
            if(type.name().equals(s)) {
                return null;
            }
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }
}
