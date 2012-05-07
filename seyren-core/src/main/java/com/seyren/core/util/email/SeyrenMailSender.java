package com.seyren.core.util.email;

import javax.inject.Named;

import org.springframework.mail.javamail.JavaMailSenderImpl;

@Named
public class SeyrenMailSender extends JavaMailSenderImpl {

    public static final String DEFAULT_SMTP_HOST = "localhost";
    public static final String DEFAULT_SMTP_PORT = "25";

    public SeyrenMailSender() {
        setHost(getPropertyOrDefault("SMTP_HOST", DEFAULT_SMTP_HOST));
        setPort(Integer.parseInt(getPropertyOrDefault("SMTP_PORT", DEFAULT_SMTP_PORT)));
        setProtocol("smtp");
    }

    private String getPropertyOrDefault(String property, String defaultvalue) {
    	return System.getProperty(property, defaultvalue);
    }

}
