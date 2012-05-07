package com.seyren.core.util.email;

public class Email {

	private String to;
	private String from;
	private String message;
	private String subject;

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}
	
	public Email withTo(String to) {
		setTo(to);
		return this;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}
	
	public Email withFrom(String from) {
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
