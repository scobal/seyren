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

import com.seyren.api.jaxrs.AdminResource;
import com.seyren.core.domain.SubscriptionPermissions;
import com.seyren.core.security.UserManagement;
import com.seyren.core.store.PermissionsStore;
import com.seyren.core.util.config.SeyrenConfig;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;

@Named
public class AdminBean implements AdminResource {
    private final SeyrenConfig seyrenConfig;
    private final PermissionsStore permissionsStore;
    @Resource(name = "${authentication.service}UserManagement")
    private UserManagement userManagement;
    @Resource(name = "${authentication.service}")
    private UserDetailsService userDetailsService;

    @Inject
    public AdminBean(SeyrenConfig seyrenConfig, PermissionsStore permissionsStore) {
        this.seyrenConfig = seyrenConfig;
        this.permissionsStore = permissionsStore;
    }

    @Override
    public Response autoCompleteUsers(String pattern) {
        return Response.ok().entity(userManagement.autoCompleteUsers(pattern)).build();
    }

    @Override
    public Response getSubscriptionPermissions(String name) {
        try {
            this.userDetailsService.loadUserByUsername(name);
        } catch (UsernameNotFoundException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        SubscriptionPermissions checkPermissions = permissionsStore.getPermissions(name);
        return Response.ok().entity(checkPermissions).build();
    }

    @Override
    public Response setSubscriptionPermissions(String name, SubscriptionPermissions permis) {
        SubscriptionPermissions checkPermissions = permissionsStore.getPermissions(name);

        if(checkPermissions.getName() == null) {
            permissionsStore.createPermissions(name, permis.getWrite());
        } else {
            permissionsStore.updatePermissions(name, permis.getWrite());
        }

        return Response.noContent().build();
    }


}
