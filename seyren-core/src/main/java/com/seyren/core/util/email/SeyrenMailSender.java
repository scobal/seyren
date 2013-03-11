/**
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

import java.util.Properties;

import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.seyren.core.service.schedule.CheckScheduler;

@Named
public class SeyrenMailSender extends JavaMailSenderImpl {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckScheduler.class);
    
    public static final String DEFAULT_SMTP_HOST = "localhost";
    public static final String DEFAULT_SMTP_PORT = "25";
    public static final String DEFAULT_SMTP_PROTOCOL = "smtp";
    
    public SeyrenMailSender() {
        
        String username = environmentOrDefault("SMTP_USERNAME", "");
        String password = environmentOrDefault("SMTP_PASSWORD", "");
        String hostname = environmentOrDefault("SMTP_HOST", DEFAULT_SMTP_HOST);
        String protocol = environmentOrDefault("SMTP_PROTOCOL", DEFAULT_SMTP_PROTOCOL);
        
        setPort(Integer.parseInt(environmentOrDefault("SMTP_PORT", DEFAULT_SMTP_PORT)));
        setHost(hostname);
        setUsername(username);
        setPassword(password);
        
        Properties props = new Properties();
        if (username != "" && password != "") {
            props.setProperty("mail.smtp.auth", "true");
        }
        
        if (getPort() == 587) {
            props.put("mail.smtp.starttls.enable", "true");
        }
        
        if (props.size() > 0) {
            setJavaMailProperties(props);
        }
        
        setProtocol(protocol);
        
        LOGGER.info(username + ":" + password + "@" + hostname);
        
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
        String value = System.getProperty(propertyName);
        if (isNotEmpty(value)) {
            return value;
        }
        value = System.getenv(propertyName);
        if (isNotEmpty(value)) {
            return value;
        }
        return defaultValue;
    }

}
