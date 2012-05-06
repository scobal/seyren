package com.seyren.core.service.notification;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.seyren.core.domain.Alert;
import com.seyren.core.domain.Check;
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.util.email.Email;
import com.seyren.core.util.email.EmailAddress;

@Named
public class EmailNotificationService implements NotificationService {

    private final MailSender mailSender;

    @Inject
    public EmailNotificationService(MailSender mailSender) {

        this.mailSender = mailSender;
    }

    @Override
    public void sendNotification(Check check, Alert alert) {
    	
    	Email email = new Email(new EmailAddress(alert.getTarget()),
                new EmailAddress("alerts@seyren"),
                alert.toString(),
                "Alert from seyren");

        try {
            SimpleMailMessage mailMessage = createEmail(email.getFrom().getAddress(), email.getTo().getAddress(), email.getMessage(), email.getSubject());
            mailSender.send(mailMessage);

        } catch (Exception e) {
            throw new NotificationFailedException("Failed to send notification to " + email.getTo().getAddress(), e);
        }
    }

    SimpleMailMessage createEmail(String from, String to, String message, String subject) {

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setFrom(from);
        mail.setText(message);
        mail.setSubject(subject);

        return mail;
    }
}
