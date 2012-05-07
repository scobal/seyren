package com.seyren.core.util.email;

import static org.apache.commons.lang.StringUtils.*;

import javax.inject.Named;

import org.springframework.mail.javamail.JavaMailSenderImpl;

@Named
public class SeyrenMailSender extends JavaMailSenderImpl {

    public static final String DEFAULT_SMTP_HOST = "localhost";
    public static final String DEFAULT_SMTP_PORT = "25";

    public SeyrenMailSender() {
        setHost(environmentOrDefault("SMTP_HOST", DEFAULT_SMTP_HOST));
        setPort(Integer.parseInt(environmentOrDefault("SMTP_PORT", DEFAULT_SMTP_PORT)));
        setProtocol("smtp");
    }
    
    public SeyrenMailSender withHost(String host) {
    	setHost(host);
    	return this;
    }
    
    public SeyrenMailSender withPort(int port) {
    	setPort(port);
    	return this;
    }

	private static String environmentOrDefault(String propertyName, String defaultValue) {
	    String value = System.getenv(propertyName);
	    if (isEmpty(value)) {
	        return defaultValue;
	    }
	    return value;
	}

}
