package com.graphite.siren.services;

import org.apache.commons.mail.EmailException;
import org.junit.Test;

import com.graphite.siren.core.value.Email;
import com.graphite.siren.core.value.EmailAddress;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class EmailServiceTest {

    @Test
    public void sendEmail() throws EmailException {
        StubEmailService emailService = new StubEmailService();

        emailService.sendNotification(new Email(new EmailAddress("andrew@here"), new EmailAddress("andrew@there"), "message", "subject"));

        assertThat(emailService.from, is("andrew@there"));
    }

    private static class StubEmailService extends EmailService {

        public String from;
        public String to;
        public String message;
        public String subject;

        @Override
        org.apache.commons.mail.Email createEmail(String from, String to, String message, String subject) throws EmailException {

            this.from = from;
            this.to = to;
            this.message = message;
            this.subject = subject;

            return new StubEmail();

        }
    }

    private static class StubEmail extends org.apache.commons.mail.Email {

        @Override
        public org.apache.commons.mail.Email setMsg(String s) throws EmailException {
            return null;
        }

        @Override
        public String send() throws EmailException {
            return "Sent";
        }
    }
}
