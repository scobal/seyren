package com.seyren.core.value;

public class Email {
    private final EmailAddress to;
    private final EmailAddress from;

    public EmailAddress getTo() {
        return to;
    }

    public EmailAddress getFrom() {
        return from;
    }

    public String getMessage() {
        return message;
    }

    public String getSubject() {
        return subject;
    }

    private final String message;
    private final String subject;

    public Email(EmailAddress to, EmailAddress from, String message, String subject) {
        this.to = to;
        this.from = from;
        this.message = message;
        this.subject = subject;
    }
}
