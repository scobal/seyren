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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.seyren.core.util.config.SeyrenConfig;

@Named
public class SeyrenMailSender extends JavaMailSenderImpl {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SeyrenMailSender.class);
    
    @Inject
    public SeyrenMailSender(SeyrenConfig seyrenConfig) {
        
        int port = seyrenConfig.getSmtpPort();
        String host = seyrenConfig.getSmtpHost();
        String username = seyrenConfig.getSmtpUsername();
        String password = seyrenConfig.getSmtpPassword();
        String protocol = seyrenConfig.getSmtpProtocol();
        
        setPort(port);
        setHost(host);
        
        Properties props = new Properties();
        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            props.setProperty("mail.smtp.auth", "true");
            setUsername(username);
            setPassword(password);
        }
        
        if (getPort() == 587) {
            props.put("mail.smtp.starttls.enable", "true");
        }
        
        if (props.size() > 0) {
            setJavaMailProperties(props);
        }
        
        setProtocol(protocol);
        
        LOGGER.info("{}:{}@{}", username, password, host);
        
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
