package com.graphite.siren.core.domain;

import com.graphite.siren.core.service.NotificationService;
import com.graphite.siren.core.value.Email;
import com.graphite.siren.core.value.EmailAddress;

public class EmailSubscription extends Subscription {

    /** @{inheritDoc}
     */
    @Override
    public void report(Alert alert, NotificationService notificationService) {
        notificationService.sendNotification(
                new Email(new EmailAddress(getTarget()),
                        new EmailAddress("alerts@seyren"),
                        alert.toString(),
                        "Alert from seyren"));

    }
}
