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
package com.seyren.core.util.config;

import static org.apache.commons.lang.StringUtils.*;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class SeyrenConfig {
    
    private final GraphiteConfig graphite;
    private final String baseUrl;
    private final String mongoUrl;
    private final String pagerDutyDomain;
    private final String hipChatAuthToken;
    private final String hipChatUsername;
    private final String hubotUrl;
    private final String smtpFrom;
	private final String smtpUsername;
	private final String smtpPassword;
	private final String smtpHost;
	private final String smtpProtocol;
	private final Integer smtpPort;
    
	@Inject
    public SeyrenConfig(GraphiteConfig graphite) {
        this.graphite = graphite;
        
        // Base
        this.baseUrl = stripEnd(environmentOrDefault("SEYREN_URL", "http://localhost:8080/seyren"), "/");
        this.mongoUrl = environmentOrDefault("MONGO_URL", "mongodb://localhost:27017/seyren");
        // TODO GRAPHITE_URL, GRAPHITE_USERNAME, GRAPHITE_PASSWORD
        
        // SMTP
        this.smtpFrom = environmentOrDefault("SEYREN_FROM_EMAIL", "alert@seyren");
        this.smtpUsername = environmentOrDefault("SMTP_USERNAME", "");
        this.smtpPassword = environmentOrDefault("SMTP_PASSWORD", "");
        this.smtpHost = environmentOrDefault("SMTP_HOST", "localhost");
        this.smtpProtocol = environmentOrDefault("SMTP_PROTOCOL", "smtp");
        this.smtpPort = Integer.parseInt(environmentOrDefault("SMTP_PORT", "25"));

        // HipChat
        this.hipChatAuthToken = environmentOrDefault("HIPCHAT_AUTH_TOKEN", "");
        this.hipChatUsername = environmentOrDefault("HIPCHAT_USER_NAME", "Seyren Alert");
        
        // PagerDuty
        this.pagerDutyDomain = environmentOrDefault("PAGERDUTY_DOMAIN", "");
        
        // Hubot
        this.hubotUrl = environmentOrDefault("SEYREN_HUBOT_URL", "");
    }
    
	public String getBaseUrl() {
        return baseUrl;
    }
    
    public String getMongoUrl() {
		return mongoUrl;
	}
    
    public String getPagerDutyDomain() {
        return pagerDutyDomain;
    }
    
    public String getHipChatAuthToken() {
        return hipChatAuthToken;
    }
    
    public String getHipChatUsername() {
        return hipChatUsername;
    }
    
    public String getHubotUrl() {
        return hubotUrl;
    }
    
    public String getSmtpFrom() {
        return smtpFrom;
    }
    
    public String getSmtpUsername() {
		return smtpUsername;
	}

	public String getSmtpPassword() {
		return smtpPassword;
	}

	public String getSmtpHost() {
		return smtpHost;
	}

	public String getSmtpProtocol() {
		return smtpProtocol;
	}

	public Integer getSmtpPort() {
		return smtpPort;
	}
	
	public String getGraphiteScheme() {
		return graphite.getScheme();
	}
	
	public String getGraphiteHost() {
		return graphite.getHost();
	}
	
	public String getGraphitePath() {
		return graphite.getPath();
	}
	
	public String getGraphiteUsername() {
		return graphite.getUsername();
	}
	
	public String getGraphitePassword() {
		return graphite.getPassword();
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
