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


import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionPermissions;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.service.notification.NotificationService;
import com.seyren.core.store.ChecksStore;
import com.seyren.core.store.PermissionsStore;
import com.seyren.core.store.SubscriptionsStore;
import com.seyren.core.util.config.SeyrenConfig;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.ws.rs.core.Response;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SubscriptionsBeanTest {
    ChecksStore checkStore = mock(ChecksStore.class);
    SubscriptionsStore subscriptionsStore = mock(SubscriptionsStore.class);
    PermissionsStore permissionsStore = mock(PermissionsStore.class);
    NotificationService notificationService = mock(NotificationService.class);
    SeyrenConfig seyrenConfig = mock(SeyrenConfig.class);
    private SubscriptionsBean subscriptionsBean;
    @Before
    public void setUp() {
        subscriptionsBean = new SubscriptionsBean(checkStore, subscriptionsStore, Arrays.asList(notificationService), permissionsStore, seyrenConfig);
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(securityContext.getAuthentication().getName()).thenReturn("test");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void noSecurityThenAllPermissionsSet() {
        when(seyrenConfig.isSecurityEnabled()).thenReturn(false);
        Subscription subscription = new Subscription();
        subscription.setType(SubscriptionType.FLOWDOCK);
        subscription.setId("1");
        when(subscriptionsStore.createSubscription("1", subscription)).thenReturn(subscription);
        Response response = subscriptionsBean.createSubscription("1", subscription);
        assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void securityEnabledAndNoPermissionSet() {
        when(seyrenConfig.isSecurityEnabled()).thenReturn(true);
        Subscription subscription = new Subscription();
        subscription.setType(SubscriptionType.FLOWDOCK);
        subscription.setId("1");
        when(permissionsStore.getPermissions("test")).thenReturn(new SubscriptionPermissions());
        Response response = subscriptionsBean.createSubscription("1", subscription);
        assertEquals(response.getStatus(), Response.Status.FORBIDDEN.getStatusCode());

    }

    @Test
    public void securityEnabledWithPermissionSet() {
        when(seyrenConfig.isSecurityEnabled()).thenReturn(true);
        Subscription subscription = new Subscription();
        subscription.setType(SubscriptionType.FLOWDOCK);
        subscription.setId("1");
        SubscriptionPermissions subscriptionPermissions = new SubscriptionPermissions();
        subscriptionPermissions.setWriteTypes(new String[]{SubscriptionType.FLOWDOCK.name()});
        when(permissionsStore.getPermissions("test")).thenReturn(subscriptionPermissions);
        when(subscriptionsStore.createSubscription("1", subscription)).thenReturn(subscription);
        Response response = subscriptionsBean.createSubscription("1", subscription);
        assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }
}