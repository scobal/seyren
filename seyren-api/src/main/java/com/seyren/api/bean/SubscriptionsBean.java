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

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.seyren.api.jaxrs.SubscriptionsResource;
import com.seyren.core.domain.Alert;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.service.notification.NotificationService;
import com.seyren.core.store.ChecksStore;
import com.seyren.core.store.SubscriptionsStore;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
public class SubscriptionsBean implements SubscriptionsResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionsBean.class);

    private ChecksStore checksStore;
    private SubscriptionsStore subscriptionsStore;
    private final Iterable<NotificationService> notificationServices;
    
    @Inject
    public SubscriptionsBean(ChecksStore checksStore, SubscriptionsStore subscriptionsStore, List<NotificationService> notificationServices) {
        this.checksStore = checksStore;
        this.subscriptionsStore = subscriptionsStore;
        this.notificationServices = notificationServices;
    }
    
    @Override
    public Response createSubscription(String checkId, Subscription subscription) {
        Subscription stored = subscriptionsStore.createSubscription(checkId, subscription);
        return Response.created(uri(checkId, stored.getId())).build();
    }
    
    @Override
    public Response updateSubscription(String checkId, Subscription subscription) {
        subscriptionsStore.updateSubscription(checkId, subscription);
        return Response.noContent().build();
    }

    @Override
    public Response deleteSubscription(String checkId, String subscriptionId) {
        subscriptionsStore.deleteSubscription(checkId, subscriptionId);
        return Response.noContent().build();
    }

    @Override
    public Response testSubscription(@PathParam("checkId") String checkId, @PathParam("subscriptionId") final String subscriptionId) {
      Check check = checksStore.getCheck(checkId);
      if (check == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      Collection<Subscription> subscriptions = Collections2.filter(check.getSubscriptions(), new Predicate<Subscription>() {
        @Override
        public boolean apply(Subscription subscription) {
          return subscription.getId().equals(subscriptionId);
        }
      });
      if (subscriptions.size() != 1) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      check.setState(AlertType.ERROR);
      Subscription subscription = subscriptions.iterator().next();
      List<Alert> interestingAlerts = new ArrayList<Alert>();
      Alert alert = new Alert()
          .withTarget(check.getTarget())
          .withValue(BigDecimal.valueOf(0.0))
          .withWarn(check.getWarn())
          .withError(check.getError())
          .withFromType(AlertType.OK)
          .withToType(AlertType.ERROR)
          .withTimestamp(new DateTime());
      interestingAlerts.add(alert);
      for (NotificationService notificationService : notificationServices) {
        if (notificationService.canHandle(subscription.getType())) {
          try {
            notificationService.sendNotification(check, subscription, interestingAlerts);
          } catch (Exception e) {
            LOGGER.warn("Notifying {} by {} failed.", subscription.getTarget(), subscription.getType(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(String.format("Notifying failed '%s'", e.getMessage())).type(MediaType.TEXT_PLAIN).build();
          }
        }
      }
      return Response.noContent().build();
    }

    private URI uri(String checkId, String subscriptionId) {
        try {
            return new URI("checks/" + checkId + "/subscriptions/" + subscriptionId);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    
}
