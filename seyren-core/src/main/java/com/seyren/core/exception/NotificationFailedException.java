package com.seyren.core.exception;

public class NotificationFailedException extends RuntimeException {
	
	private static final long serialVersionUID = 7693258582485183478L;

	public NotificationFailedException(String s, Throwable throwable) {
		super(s, throwable);
	}

}
