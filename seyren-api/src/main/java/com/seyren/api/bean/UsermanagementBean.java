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

import com.seyren.api.jaxrs.UsermanagementResource;
import com.seyren.core.domain.User;
import com.seyren.core.security.Token;
import com.seyren.core.security.UserManagement;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import javax.annotation.Resource;
import javax.inject.Named;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@Named
public class UsermanagementBean implements UsermanagementResource {

    @Resource(name = "${authentication.service}AuthenticationManager")
    private AuthenticationManager authManager;

    @Resource(name = "${authentication.service}")
    private UserDetailsService userDetailsService;

    @Resource(name = "${authentication.service}UserManagement")
    private UserManagement userManagement;

    @Override
    public Response authenticateUser(User user) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
        Authentication authentication = this.authManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails details = this.userDetailsService.loadUserByUsername(user.getUsername());
        return Response.ok(new com.seyren.core.domain.Token(Token.createToken(details))).build();
    }

    @Override
    public Response getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof String && principal.equals("anonymousUser")) {
            throw new WebApplicationException(401);
        }
        UserDetails userDetails = (UserDetails) principal;

        return Response.ok(userDetails).build();
    }


    @Override
    public Response addUser(User user) {
        boolean userAdded = userManagement.addUser(user);
        if (userAdded) {
            return Response.status(Response.Status.CREATED).entity(user).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }
}
