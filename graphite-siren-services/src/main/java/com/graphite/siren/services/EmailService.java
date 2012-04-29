package com.graphite.siren.services;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import com.graphite.siren.core.exception.NotificationFailedException;
import com.graphite.siren.core.service.NotificationService;

@Named
public class EmailService implements NotificationService {

    private final MailSender mailSender;

    @Inject
    public EmailService(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendNotification(com.graphite.siren.core.value.Email email) {

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
