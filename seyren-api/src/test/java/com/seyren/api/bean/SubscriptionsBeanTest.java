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


import com.seyren.api.util.RequestValidator;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.service.notification.NotificationService;
import com.seyren.core.store.ChecksStore;
import com.seyren.core.store.SubscriptionsStore;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SubscriptionsBeanTest {

    RequestValidator validator = mock(RequestValidator.class);
    ChecksStore checkStore = mock(ChecksStore.class);
    SubscriptionsStore subscriptionsStore = mock(SubscriptionsStore.class);
    NotificationService notificationService = mock(NotificationService.class);
    HttpHeaders httpHeaders = mock(HttpHeaders.class);
    Response response = mock(Response.class);
    private SubscriptionsBean subscriptionsBean;
    @Before
    public void setUp() {
        subscriptionsBean = new SubscriptionsBean(checkStore, subscriptionsStore, Arrays.asList(notificationService), validator);
    }
    @Test
    public void notAuthenticatedSubscriptionCreation() {
        response = Response.status(Response.Status.UNAUTHORIZED).build();
        Subscription subscription = new Subscription();
        subscription.setType(SubscriptionType.HIPCHAT);
        when(validator.unauthorizedSubscription(httpHeaders, subscription.getType())).thenReturn(response);
        Response unauthorizedResponse = subscriptionsBean.createSubscription("1", subscription, httpHeaders);
        assertEquals(unauthorizedResponse.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void isAuthenticatedSubscriptionCreation() {
        Subscription subscription = new Subscription();
        subscription.setType(SubscriptionType.FLOWDOCK);
        subscription.setId("1");
        when(subscriptionsStore.createSubscription("1", subscription)).thenReturn(subscription);
        when(validator.unauthorizedSubscription(httpHeaders, subscription.getType())).thenReturn(null);
        Response goodResponse = subscriptionsBean.createSubscription("1", subscription, httpHeaders);
        assertEquals(goodResponse.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void notAuthenticatedSubscriptionUpdate() {
        response = Response.status(Response.Status.UNAUTHORIZED).build();
        Subscription subscription = new Subscription();
        subscription.setType(SubscriptionType.LOGGER);
        when(validator.unauthorizedSubscription(httpHeaders, subscription.getType())).thenReturn(response);
        Response unauthorizedResponse = subscriptionsBean.updateSubscription("1", subscription, httpHeaders);
        assertEquals(unauthorizedResponse.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void isAuthenticatedSubscriptonUpdated() {
        Subscription subscription = new Subscription();
        subscription.setType(SubscriptionType.EMAIL);
        subscription.setId("1");
        when(subscriptionsStore.createSubscription("1", subscription)).thenReturn(subscription);
        when(validator.unauthorizedSubscription(httpHeaders, subscription.getType())).thenReturn(null);
        Response goodResponse = subscriptionsBean.updateSubscription("1", subscription, httpHeaders);
        assertEquals(goodResponse.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
    }
}