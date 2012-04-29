package com.seyren.core.service;

import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.value.Email;

public interface NotificationService {
    void sendNotification(Email email) throws NotificationFailedException;
}
