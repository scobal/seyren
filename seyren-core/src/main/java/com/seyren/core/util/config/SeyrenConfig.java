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

import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.seyren.core.util.velocity.Slf4jLogChute;

@Named
public class SeyrenConfig {
    
    private final String baseUrl;
    private final String mongoUrl;
    private final String graphiteUrl;
    private final String graphiteUsername;
    private final String graphitePassword;
    private final String graphiteKeyStore;
    private final String graphiteKeyStorePassword;
    private final String graphiteTrustStore;
    private final String graphiteCarbonPickleEnable;
    private final String graphiteCarbonPicklePort;
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
    private final String flowdockExternalUsername;
    private final String flowdockTags;
    // Icon mapped check sate (AlertType) see http://apps.timwhitlock.info/emoji/tables/unicode
    // question, sunny, cloud, voltage exclamation should be: \u2753,\u2600,\u2601,\u26A1,\u2757
    private final String flowdockEmojis;

    public SeyrenConfig() {
        
        // Base
        this.baseUrl = stripEnd(configOrDefault("SEYREN_URL", "http://localhost:8080/seyren"), "/");
        this.mongoUrl = configOrDefault("MONGO_URL", "mongodb://localhost:27017/seyren");
        
        // Graphite
        this.graphiteUrl = stripEnd(configOrDefault("GRAPHITE_URL", "http://localhost:80"), "/");
        this.graphiteUsername = configOrDefault("GRAPHITE_USERNAME", "");
        this.graphitePassword = configOrDefault("GRAPHITE_PASSWORD", "");
        this.graphiteKeyStore = configOrDefault("GRAPHITE_KEYSTORE", "");
        this.graphiteKeyStorePassword = configOrDefault("GRAPHITE_KEYSTORE_PASSWORD", "");
        this.graphiteTrustStore = configOrDefault("GRAPHITE_TRUSTSTORE", "");
        this.graphiteCarbonPickleEnable = configOrDefault("GRAPHITE_CARBON_PICKLE_ENABLE", "false");
        this.graphiteCarbonPicklePort = configOrDefault("GRAPHITE_CARBON_PICKLE_PORT", "2004");
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
        
        // Flowdock
        this.flowdockExternalUsername = configOrDefault("FLOWDOCK_EXTERNAL_USERNAME", "Seyren");
        this.flowdockTags = configOrDefault("FLOWDOCK_TAGS", "");
        this.flowdockEmojis = configOrDefault("FLOWDOCK_EMOJIS", "");
        
    }
    
    @PostConstruct
    public void init() {
        Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new Slf4jLogChute());
        Velocity.init();
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    @JsonIgnore
    public String getMongoUrl() {
        return mongoUrl;
    }
    
    @JsonIgnore
    public String getPagerDutyDomain() {
        return pagerDutyDomain;
    }
    
    @JsonIgnore
    public String getPagerDutyToken() {
        return pagerDutyToken;
    }
    
    @JsonIgnore
    public String getPagerDutyUsername() {
        return pagerDutyUsername;
    }
    
    @JsonIgnore
    public String getPagerDutyPassword() {
        return pagerDutyPassword;
    }
    
    @JsonIgnore
    public String getHipChatAuthToken() {
        return hipChatAuthToken;
    }
    
    @JsonIgnore
    public String getHipChatUsername() {
        return hipChatUsername;
    }
    
    @JsonIgnore
    public String getHubotUrl() {
        return hubotUrl;
    }
    
    @JsonIgnore
    public String getFlowdockExternalUsername() {
        return flowdockExternalUsername;
    }
    
    @JsonIgnore
    public String getFlowdockTags() {
        return flowdockTags;
    }
    
    @JsonIgnore
    public String getFlowdockEmojis() {
        return flowdockEmojis;
    }
    
    @JsonIgnore
    public String getSmtpFrom() {
        return smtpFrom;
    }
    
    @JsonIgnore
    public String getSmtpUsername() {
        return smtpUsername;
    }
    
    @JsonIgnore
    public String getSmtpPassword() {
        return smtpPassword;
    }
    
    @JsonIgnore
    public String getSmtpHost() {
        return smtpHost;
    }
    
    @JsonIgnore
    public String getSmtpProtocol() {
        return smtpProtocol;
    }
    
    @JsonIgnore
    public Integer getSmtpPort() {
        return smtpPort;
    }
    
    @JsonIgnore
    public String getGraphiteUrl() {
        return graphiteUrl;
    }
    
    @JsonIgnore
    public String getGraphiteUsername() {
        return graphiteUsername;
    }
    
    @JsonIgnore
    public String getGraphitePassword() {
        return graphitePassword;
    }
    
    @JsonIgnore
    public String getGraphiteScheme() {
        return splitBaseUrl(graphiteUrl)[0];
    }
    
    @JsonIgnore
    public int getGraphiteSSLPort() {
        return Integer.valueOf(splitBaseUrl(graphiteUrl)[1]);
    }
    
    @JsonIgnore
    public String getGraphiteHost() {
        return splitBaseUrl(graphiteUrl)[2];
    }
    
    @JsonIgnore
    public String getGraphitePath() {
        return splitBaseUrl(graphiteUrl)[3];
    }
    
    @JsonIgnore
    public String getGraphiteKeyStore() {
        return graphiteKeyStore;
    }
    
    @JsonIgnore
    public String getGraphiteKeyStorePassword() {
        return graphiteKeyStorePassword;
    }

    @JsonIgnore
    public String getGraphiteTrustStore() {
        return graphiteTrustStore;
    }

    @JsonIgnore
    public int getGraphiteCarbonPicklePort() {
        return Integer.valueOf(graphiteCarbonPicklePort);
    }

    @JsonIgnore
    public boolean getGraphiteCarbonPickleEnable() {
        return Boolean.valueOf(graphiteCarbonPickleEnable);
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
        String[] baseParts = new String[4];
        
        if (baseUrl.toString().contains("://")) {
            baseParts[0] = baseUrl.toString().split("://")[0];
            baseUrl = baseUrl.toString().split("://")[1];
        } else {
            baseParts[0] = "http";
        }
        
        if (baseUrl.contains(":")) {
            baseParts[1] = baseUrl.split(":")[1];
        } else {
            baseParts[1] = "443";
        }
        
        if (baseUrl.contains("/")) {
            baseParts[2] = baseUrl.split("/")[0];
            baseParts[3] = "/" + baseUrl.split("/", 2)[1];
        } else {
            baseParts[2] = baseUrl;
            baseParts[3] = "";
        }
        
        return baseParts;
    }
}
