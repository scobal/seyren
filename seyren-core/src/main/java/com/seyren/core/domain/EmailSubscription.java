package com.seyren.core.domain;

import com.seyren.core.service.NotificationService;
import com.seyren.core.value.Email;
import com.seyren.core.value.EmailAddress;

public class EmailSubscription extends Subscription {

    /** @{inheritDoc}
     */
    @Override
    public void notify(Alert alert, NotificationService notificationService) {
        notificationService.sendNotification(
                new Email(new EmailAddress(getTarget()),
                        new EmailAddress("alerts@seyren"),
                        alert.toString(),
                        "Alert from seyren"));

    }
}
