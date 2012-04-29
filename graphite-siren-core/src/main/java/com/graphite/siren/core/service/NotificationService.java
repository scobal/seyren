package com.graphite.siren.core.service;

import com.graphite.siren.core.value.Email;
import com.graphite.siren.core.exception.NotificationFailedException;

public interface NotificationService {
    void sendNotification(Email email) throws NotificationFailedException;
}
