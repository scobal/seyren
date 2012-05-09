/**
 * Copyright © 2010-2011 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
