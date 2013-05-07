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

import java.util.Arrays;
import java.util.List;

import javax.inject.Named;

@Named
public class SeyrenConfig {
    
    private final String baseUrl;
    private final String mongoUrl;
	private final String graphiteUrl;
	private final String graphiteUsername;
	private final String graphitePassword;
    private final String pagerDutyDomain;
    private final String pagerDutyToken;
    private final String pagerDutyUsername;
    private final String pagerDutyPassword;
    private final String hipChatAuthToken;
    private final String hipChatUsername;
    private final String hubotUrl;
    private final String smtpFrom;
	private final String smtpUsername;
	private final String smtpPassword;
	private final String smtpHost;
	private final String smtpProtocol;
	private final Integer smtpPort;
    
    public SeyrenConfig() {
        
        // Base
        this.baseUrl = stripEnd(configOrDefault("SEYREN_URL", "http://localhost:8080/seyren"), "/");
        this.mongoUrl = configOrDefault("MONGO_URL", "mongodb://localhost:27017/seyren");
        
        // Graphite
        this.graphiteUrl = stripEnd(configOrDefault("GRAPHITE_URL", "http://localhost:80"), "/");
        this.graphiteUsername = configOrDefault("GRAPHITE_USERNAME", "");
        this.graphitePassword = configOrDefault("GRAPHITE_PASSWORD", "");
        
        // SMTP
        this.smtpFrom = configOrDefault(list("SMTP_FROM", "SEYREN_FROM_EMAIL"), "alert@seyren");
        this.smtpUsername = configOrDefault("SMTP_USERNAME", "");
        this.smtpPassword = configOrDefault("SMTP_PASSWORD", "");
        this.smtpHost = configOrDefault("SMTP_HOST", "localhost");
        this.smtpProtocol = configOrDefault("SMTP_PROTOCOL", "smtp");
        this.smtpPort = Integer.parseInt(configOrDefault("SMTP_PORT", "25"));

        // HipChat
        this.hipChatAuthToken = configOrDefault(list("HIPCHAT_AUTHTOKEN", "HIPCHAT_AUTH_TOKEN"), "");
        this.hipChatUsername = configOrDefault(list("HIPCHAT_USERNAME", "HIPCHAT_USER_NAME"), "Seyren Alert");
        
        // PagerDuty
        this.pagerDutyDomain = configOrDefault("PAGERDUTY_DOMAIN", "");
        this.pagerDutyToken = configOrDefault("PAGERDUTY_TOKEN", "");
        this.pagerDutyUsername = configOrDefault("PAGERDUTY_USERNAME", "");
        this.pagerDutyPassword = configOrDefault("PAGERDUTY_PASSWORD", "");
        
        // Hubot
        this.hubotUrl = configOrDefault(list("HUBOT_URL", "SEYREN_HUBOT_URL"), "");
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
    
    public String getPagerDutyToken() {
        return pagerDutyToken;
    }

    public String getPagerDutyUsername() {
        return pagerDutyUsername;
    }

    public String getPagerDutyPassword() {
        return pagerDutyPassword;
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
	
	public String getGraphiteUrl() {
		return graphiteUrl;
	}
	
	public String getGraphiteUsername() {
		return graphiteUsername;
	}
	
	public String getGraphitePassword() {
		return graphitePassword;
	}
	
	public String getGraphiteScheme() {
		return splitBaseUrl(graphiteUrl)[0];
	}
	
	public String getGraphiteHost() {
		return splitBaseUrl(graphiteUrl)[1];
	}
	
	public String getGraphitePath() {
		return splitBaseUrl(graphiteUrl)[2];
	}
	
    private static String configOrDefault(String propertyName, String defaultValue) {
    	return configOrDefault(list(propertyName), defaultValue);
    }

	private static String configOrDefault(List<String> propertyNames, String defaultValue) {
		
		for (String propertyName : propertyNames) {
			
			String value = System.getProperty(propertyName);
	        if (isNotEmpty(value)) {
	            return value;
	        }
	        
	        value = System.getenv(propertyName);
	        if (isNotEmpty(value)) {
	            return value;
	        }
		}
		
        return defaultValue;
	}
	
	private static List<String> list(String... propertyNames) {
		return Arrays.asList(propertyNames);
	}
    
    private static String[] splitBaseUrl(String baseUrl) {
        String[] baseParts = new String[3];
        
        if (baseUrl.toString().contains("://")) {
            baseParts[0] = baseUrl.toString().split("://")[0];
            baseUrl = baseUrl.toString().split("://")[1];
        } else {
            baseParts[0] = "http";
        }
        
        if (baseUrl.contains("/")) {
            baseParts[1] = baseUrl.split("/")[0];
            baseParts[2] = "/" + baseUrl.split("/", 2)[1];
        } else {
            baseParts[1] = baseUrl;
            baseParts[2] = "";
        }
        
        return baseParts;
    }
}
