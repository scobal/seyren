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

import com.seyren.core.util.config.SeyrenConfig;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


public class AuthenticationTokenFilter implements Filter {
    private final UserDetailsService userService;
    private final SeyrenConfig seyrenConfig;

    @Inject
    public AuthenticationTokenFilter(UserDetailsService userService, SeyrenConfig seyrenConfig) {
        this.userService = userService;
        this.seyrenConfig = seyrenConfig;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!seyrenConfig.isSecurityEnabled()) {
            SecurityContextHolder.getContext().setAuthentication(new SecurityDisabledAuthentication());
        } else {
            HttpServletRequest httpRequest = this.getAsHttpRequest(request);

            String authToken = this.extractAuthTokenFromRequest(httpRequest);
            String userName = Token.getUserNameFromToken(authToken);

            if (userName != null) {
                UserDetails userDetails = this.userService.loadUserByUsername(userName);

                if (Token.validateToken(authToken, userDetails)) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                }
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }

    private HttpServletRequest getAsHttpRequest(ServletRequest request)
    {
        if (!(request instanceof HttpServletRequest)) {
            throw new RuntimeException("Expecting an HTTP request");
        }

        return (HttpServletRequest) request;
    }

    private String extractAuthTokenFromRequest(HttpServletRequest httpRequest)
    {
        return httpRequest.getHeader("X-Auth-Token");
    }
}
