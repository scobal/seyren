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

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.seyren.core.service.schedule.CheckScheduler;
import com.seyren.core.util.config.SeyrenConfig;

@Named
public class SeyrenMailSender extends JavaMailSenderImpl {

	private static final Logger LOGGER = LoggerFactory.getLogger(CheckScheduler.class);
	
    public static final String DEFAULT_SMTP_HOST = "localhost";
    public static final String DEFAULT_SMTP_PORT = "25";

    @Inject
    public SeyrenMailSender(SeyrenConfig seyrenConfig) {
    	
    	String username = seyrenConfig.getConfigProperty("SMTP_USERNAME", "");
    	String password = seyrenConfig.getConfigProperty("SMTP_PASSWORD","");
    	String hostname = seyrenConfig.getConfigProperty("SMTP_HOST", DEFAULT_SMTP_HOST);
        
    	setPort(Integer.parseInt(seyrenConfig.getConfigProperty("SMTP_PORT", DEFAULT_SMTP_PORT)));
    	setHost(hostname);       
        setUsername(username);
        setPassword(password);
        
        if(username != "" && password != "") {
	        Properties props = new Properties();
	        props.setProperty("mail.smtp.auth", "true");
	        setJavaMailProperties(props);
        }
        
        setProtocol("smtp");
        
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

}
