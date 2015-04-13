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

import com.google.common.io.BaseEncoding;
import com.seyren.api.provider.AuthenticationProvider;
import com.seyren.core.domain.SubscriptionPermissions;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.domain.User;
import com.seyren.core.store.PermissionsStore;
import com.seyren.core.util.config.SeyrenConfig;
import org.apache.commons.io.Charsets;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


public class RequestValidatorTest {
    final String ADMIN_NAME = "admin-name";
    final String ADMIN_PASS = "admin-pass";
    private final SeyrenConfig seyrenConfig = mock(SeyrenConfig.class);
    private final AuthenticationProvider provider = mock(AuthenticationProvider.class);
    private final PermissionsStore permissionsStore = mock(PermissionsStore.class);
    private RequestValidator requestValidator;
    private HttpHeaders httpHeaders = mock(HttpHeaders.class);
    private MultivaluedMap<String, String> requestHeaders;

    @Before
    public void setUp() {
        requestValidator = new RequestValidator(seyrenConfig, provider, permissionsStore);
        when(seyrenConfig.getAdminUser()).thenReturn(ADMIN_NAME);
        when(seyrenConfig.getAdminPass()).thenReturn(ADMIN_PASS);
        requestHeaders = new MultivaluedHashMap<String, String>();
        when(httpHeaders.getRequestHeaders()).thenReturn(requestHeaders);
    }

    @Test
    public void testBasicAuthenticationDecoding() {
        User user = new User();
        user.setUsername("user");
        user.setPassword("pass");
        String token = "Basic " + BaseEncoding.base64().encode("user:pass".getBytes(Charsets.UTF_8));
        requestHeaders.add("Authorization", token);
        requestValidator.isAuthenticatedRequest(httpHeaders, true);
        verify(provider).isValidCredentials(any(User.class));
    }

    @Test
    public void requestWithAdminPermissions() throws Exception {
        when(provider.isValidCredentials(any(User.class))).thenReturn(true);
        String token = "Basic " + BaseEncoding.base64().encode((ADMIN_NAME + ":" + ADMIN_PASS).getBytes(Charsets.UTF_8));
        requestHeaders.add("Authorization", token);
        boolean authenticatedRequest = requestValidator.isAuthenticatedRequest(httpHeaders, true);
        verify(seyrenConfig, times(1)).getAdminUser();
        verify(seyrenConfig, times(1)).getAdminPass();
        assertEquals(authenticatedRequest, true);
    }

    @Test
    public void requestValidButWithoutAdminPermissions() {
        when(provider.isValidCredentials(any(User.class))).thenReturn(true);
        String token = "Basic " + BaseEncoding.base64().encode(("yeah:boy").getBytes(Charsets.UTF_8));
        requestHeaders.add("Authorization", token);
        boolean authenticatedRequest = requestValidator.isAuthenticatedRequest(httpHeaders, true);
        verify(seyrenConfig, times(1)).getAdminUser();
        assertEquals(authenticatedRequest, false);
    }

    @Test
    public void authenticatedButNotAdmin() {
        when(provider.isValidCredentials(any(User.class))).thenReturn(true);
        String token = "Basic " + BaseEncoding.base64().encode(("yeah:boy").getBytes(Charsets.UTF_8));
        requestHeaders.add("Authorization", token);
        boolean authenticatedRequest = requestValidator.isAuthenticatedRequest(httpHeaders, false);
        verify(seyrenConfig, never()).getAdminUser();
        verify(seyrenConfig, never()).getAdminPass();
        assertEquals(authenticatedRequest, true);
    }

    @Test
    public void testRequestCommingFromAdmin() throws Exception {
        String token = "Basic " + BaseEncoding.base64().encode((ADMIN_NAME + ":" + ADMIN_PASS).getBytes(Charsets.UTF_8));
        requestHeaders.add("Authorization", token);
        Boolean isAdmin = requestValidator.requestCommingFromAdmin(requestHeaders);
        assertEquals(isAdmin, true);
    }

    @Test
    public void testSubscriptionWithoutSecruityEnabled() throws Exception {
        when(seyrenConfig.authenticationEnabled()).thenReturn(false);
        Response response = requestValidator.unauthorizedSubscription(httpHeaders, SubscriptionType.HIPCHAT);
        assertEquals(response, null);
    }

    @Test
    public void authenticatedUserWithPermissions() {
        when(provider.isValidCredentials(any(User.class))).thenReturn(true);
        SubscriptionPermissions subscriptionPermissions = new SubscriptionPermissions();
        subscriptionPermissions.setWriteTypes(SubscriptionType.names());
        String token = "Basic " + BaseEncoding.base64().encode((ADMIN_NAME + ":" + ADMIN_PASS).getBytes(Charsets.UTF_8));
        requestHeaders.add("Authorization", token);
        when(seyrenConfig.authenticationEnabled()).thenReturn(true);
        when(permissionsStore.getPermissions(ADMIN_NAME)).thenReturn(subscriptionPermissions);
        Response response = requestValidator.unauthorizedSubscription(httpHeaders, SubscriptionType.SNMP);
        assertEquals(response, null);
    }

    @Test
    public void authenticatedUserWithoutPermissions() {
        when(provider.isValidCredentials(any(User.class))).thenReturn(true);
        SubscriptionPermissions subscriptionPermissions = new SubscriptionPermissions();
        subscriptionPermissions.setWriteTypes(new String[]{SubscriptionType.EMAIL.name()});
        String token = "Basic " + BaseEncoding.base64().encode((ADMIN_NAME + ":" + ADMIN_PASS).getBytes(Charsets.UTF_8));
        requestHeaders.add("Authorization", token);
        when(seyrenConfig.authenticationEnabled()).thenReturn(true);
        when(permissionsStore.getPermissions(ADMIN_NAME)).thenReturn(subscriptionPermissions);
        Response response = requestValidator.unauthorizedSubscription(httpHeaders, SubscriptionType.HTTP);
        assertEquals(response.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode());
    }
}