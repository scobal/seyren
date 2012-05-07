package com.seyren.core.util.email;

public class Email {

	private EmailAddress to;
	private EmailAddress from;
	private String message;
	private String subject;

	public EmailAddress getTo() {
		return to;
	}

	public void setTo(EmailAddress to) {
		this.to = to;
	}
	
	public Email withTo(EmailAddress to) {
		setTo(to);
		return this;
	}

	public EmailAddress getFrom() {
		return from;
	}

	public void setFrom(EmailAddress from) {
		this.from = from;
	}
	
	public Email withFrom(EmailAddress from) {
		setFrom(from);
		return this;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public Email withMessage(String message) {
		setMessage(message);
		return this;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public Email withSubject(String subject) {
		setSubject(subject);
		return this;
	}

}
