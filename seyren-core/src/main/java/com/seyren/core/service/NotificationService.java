package com.seyren.core.service;

import com.seyren.core.domain.Alert;
import com.seyren.core.domain.Check;
import com.seyren.core.exception.NotificationFailedException;

public interface NotificationService {
	
    void sendNotification(Check check, Alert alert) throws NotificationFailedException;
}
