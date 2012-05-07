package com.seyren.core.service.notification;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import com.seyren.core.domain.Alert;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.util.email.Email;

@Named
public class EmailNotificationService implements NotificationService {

    private final MailSender mailSender;

    @Inject
    public EmailNotificationService(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendNotification(Check check, Subscription subscription, Alert alert) {
    	
    	Email email = new Email()
    				.withTo(subscription.getTarget())
    				.withFrom("alerts@seyren")
    				.withSubject(alert.getTarget())
    				.withMessage("Alert from seyren");

        try {
            SimpleMailMessage mailMessage = createEmail(email.getFrom(), email.getTo(), email.getMessage(), email.getSubject());
            mailSender.send(mailMessage);

        } catch (Exception e) {
            throw new NotificationFailedException("Failed to send notification to " + email.getTo(), e);
        }
    }

    private SimpleMailMessage createEmail(String from, String to, String message, String subject) {

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setFrom(from);
        mail.setText(message);
        mail.setSubject(subject);

        return mail;
    }
}
