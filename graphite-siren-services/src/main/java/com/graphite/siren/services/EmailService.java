package com.graphite.siren.services;

import javax.inject.Named;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import com.graphite.siren.core.exception.NotificationFailedException;
import com.graphite.siren.core.service.NotificationService;

@Named
public class EmailService implements NotificationService {

    @Override
    public void sendNotification(com.graphite.siren.core.value.Email email) {

        try {
            createEmail(email.getFrom().getAddress(), email.getTo().getAddress(), email.getMessage(), email.getSubject());

        } catch (EmailException e) {
            throw new NotificationFailedException("Failed to send notification", e);
        }
    }

    Email createEmail(String from, String to, String message, String subject) throws EmailException{
        Email apacheEmail = new SimpleEmail();

        apacheEmail.setHostName("localhost");
        apacheEmail.setSocketConnectionTimeout(500);
        apacheEmail.setSmtpPort(1125);
        apacheEmail.setFrom(from);
        apacheEmail.setSubject(subject);
        apacheEmail.setMsg(message);
        apacheEmail.addTo(to);
        apacheEmail.send();

        return apacheEmail;
    }
}
