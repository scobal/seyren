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

import com.seyren.api.provider.AuthenticationProvider;
import com.seyren.api.util.RequestValidator;
import com.seyren.core.domain.SubscriptionPermissions;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.domain.User;
import com.seyren.core.store.PermissionsStore;
import com.seyren.core.util.config.SeyrenConfig;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class AuthenticationBeanTest {
    final String ADMIN_NAME = "admin-name";
    final String ADMIN_PASS = "admin-pass";
    private SeyrenConfig seyrenConfig = mock(SeyrenConfig.class);
    private PermissionsStore permissionsStore = mock(PermissionsStore.class);
    private RequestValidator requestValidator = mock(RequestValidator.class);
    private AuthenticationProvider provider = mock(AuthenticationProvider.class);
    private HttpHeaders httpHeaders = mock(HttpHeaders.class);
    private AuthenticationBean authenticationBean;
    private SubscriptionPermissions permissions = new SubscriptionPermissions();
    @Before
    public void setUp() {
        authenticationBean = new AuthenticationBean(seyrenConfig, permissionsStore, requestValidator, provider);
        when(seyrenConfig.getAdminUser()).thenReturn(ADMIN_NAME);
        when(seyrenConfig.getAdminPass()).thenReturn(ADMIN_PASS);
        permissions.setWriteTypes(SubscriptionType.names());
    }

    @Test
    public void isAnAdmin() {
        User user = new User();
        user.setUsername(ADMIN_NAME);
        user.setPassword(ADMIN_PASS);
        Response response = authenticationBean.authenticateUser(user);
        assertEquals(user.isAdmin(), true);
        assertEquals(user.isAuthenticated(), true);
        assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        verify(provider, never()).isValidCredentials(user);
    }

    @Test
    public void isAuthenticatedButNotAdmin() {
        User user = new User();
        user.setUsername("john");
        user.setPassword("123456");
        when(provider.isValidCredentials(user)).thenReturn(true);
        Response response = authenticationBean.authenticateUser(user);
        verify(provider, times(1)).isValidCredentials(user);
        assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void invalidCredentials() {
        User user = new User();
        user.setUsername("sam");
        user.setPassword("qwerty");
        when(provider.isValidCredentials(user)).thenReturn(false);
        Response response = authenticationBean.authenticateUser(user);
        verify(provider, times(1)).isValidCredentials(user);
        assertEquals(response.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void getUsersWithoutAdminAccess() {
        when(requestValidator.requestCommingFromAdmin(httpHeaders.getRequestHeaders())).thenReturn(false);
        Response response = authenticationBean.getUsers("george", httpHeaders);
        assertEquals(response.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode());
        verify(provider, never()).getUsers("george");
    }

    @Test
    public void getUsersWithAdminAccess() {
        when(requestValidator.requestCommingFromAdmin(httpHeaders.getRequestHeaders())).thenReturn(true);
        Response response = authenticationBean.getUsers("player", httpHeaders);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        verify(provider, times(1)).getUsers("player");
    }

    @Test
    public void getSubscriptionPermissionsWithInvalidRequest() {
        when(requestValidator.isAuthenticatedRequest(httpHeaders, true)).thenReturn(false);
        Response response = authenticationBean.getSubscriptionPermissions("roger", httpHeaders);
        assertEquals(response.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode());
        verify(permissionsStore, never()).getPermissions("roger");
    }

    @Test
    public void getSubscriptionPermissionsWithValidRequest() {
        when(requestValidator.isAuthenticatedRequest(httpHeaders, true)).thenReturn(true);
        when(provider.isValidUser("roger")).thenReturn(true);
        Response response = authenticationBean.getSubscriptionPermissions("roger", httpHeaders);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        verify(permissionsStore, times(1)).getPermissions("roger");
    }

    @Test
    public void setSubscriptionPermissionsWithInvalidRequest() {
        when(requestValidator.isAuthenticatedRequest(httpHeaders, true)).thenReturn(false);
        Response response = authenticationBean.setSubscriptionPermissions("roger", permissions, httpHeaders);
        assertEquals(response.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode());
        verify(permissionsStore, never()).getPermissions("roger");
    }

    @Test
    public void setSubscriptionPermissionsWithValidRequest() {
        when(requestValidator.isAuthenticatedRequest(httpHeaders, true)).thenReturn(true);
        when(provider.isValidUser("roger")).thenReturn(true);
        when(permissionsStore.getPermissions("roger")).thenReturn(new SubscriptionPermissions());
        Response response = authenticationBean.setSubscriptionPermissions("roger", permissions, httpHeaders);
        assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
        verify(permissionsStore, times(1)).getPermissions("roger");
        verify( permissionsStore, times(1)).createPermissions("roger", permissions.getWrite());
    }
}