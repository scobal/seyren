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
package com.seyren.core.security;

import com.seyren.core.domain.User;
import com.seyren.core.util.config.SeyrenConfig;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthenticationTokenFilterTest {
    AuthenticationTokenFilter authenticationTokenFilter;
    UserDetailsService userDetailsService = mock(UserDetailsService.class);
    SeyrenConfig seyrenConfig = mock(SeyrenConfig.class);
    HttpServletRequest servletRequest = mock(HttpServletRequest.class);
    ServletResponse servletResponse = mock(ServletResponse.class);
    FilterChain filterChain = mock(FilterChain.class);
    @Before
    public void setUp() {
        authenticationTokenFilter = new AuthenticationTokenFilter(userDetailsService, seyrenConfig);
    }
    @Test
    public void securityDisabledUserHasPermissionsFilterTest() throws Exception {
        when(seyrenConfig.isSecurityEnabled()).thenReturn(false);
        authenticationTokenFilter.doFilter(servletRequest, servletResponse, filterChain);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(authentication.isAuthenticated(), true);
    }

    @Test
    @Ignore
    public void securityEnabledAndUserHasToken() throws IOException, ServletException {
        User user = new User("test", "password");
        String token = Token.createToken(user);
        when(seyrenConfig.isSecurityEnabled()).thenReturn(true);
        when(servletRequest.getHeader("X-Auth-Token")).thenReturn(token);
        when(userDetailsService.loadUserByUsername("test")).thenReturn(user);
        authenticationTokenFilter.doFilter(servletRequest, servletResponse, filterChain);
        assertEquals(SecurityContextHolder.getContext().getAuthentication().isAuthenticated(), true);
    }
}