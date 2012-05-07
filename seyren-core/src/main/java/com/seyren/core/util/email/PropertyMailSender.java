package com.seyren.core.util.email;

import javax.inject.Named;

import org.springframework.mail.javamail.JavaMailSenderImpl;

@Named
public class PropertyMailSender extends JavaMailSenderImpl {

    public static final String DEFAULT_EMAIL_HOST = "localhost";
    public static final String DEFAULT_EMAIL_PORT = "25";
    public static final String DEFAULT_EMAIL_PROTOCOL = "smtp";

    public PropertyMailSender() {
        setHost(getPropertyOrDefault("email.host", DEFAULT_EMAIL_HOST));
        setPort(Integer.parseInt(getPropertyOrDefault("email.port", DEFAULT_EMAIL_PORT)));
        setProtocol(getPropertyOrDefault("email.protocol", DEFAULT_EMAIL_PROTOCOL));
    }

    private String getPropertyOrDefault(String property, String defaultvalue) {
    	return System.getProperty(property, defaultvalue);
    }

}
