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

import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.stripEnd;

import java.util.ArrayList;
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
    
    private static final String DEFAULT_BASE_URL = "http://localhost:8080/seyren";

    private final String baseUrl;
    private final String mongoUrl;
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
    private final List<GraphiteInstanceConfig> graphiteInstanceConfigs = new ArrayList<GraphiteInstanceConfig>();
    private final String ircCatHost;
    private final String ircCatPort;

    public SeyrenConfig() {
        
        // Base
        this.baseUrl = stripEnd(configOrDefault("SEYREN_URL", DEFAULT_BASE_URL), "/");
        this.mongoUrl = configOrDefault("MONGO_URL", "mongodb://localhost:27017/seyren");
        
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

        // IrcCat
        this.ircCatHost = configOrDefault("IRCCAT_HOST", "localhost");
        this.ircCatPort = configOrDefault("IRCCAT_PORT", "12345");
        
        buildGraphiteInstanceConfigs();
    }
    
    private void buildGraphiteInstanceConfigs() {
    	
    	// For now, just read in a single Graphite instance so Mark and Neil can decide how they want to handle
    	// configuration generally. Once they have that, we can read in the multiple Graphite instance configs.
    	// [williewheeler]
    	GraphiteInstanceConfig graphiteInstanceConfig = new GraphiteInstanceConfig();
    	graphiteInstanceConfig.setId("the-one-graphite-instance");
    	graphiteInstanceConfig.setName("The One Graphite Instance");
    	graphiteInstanceConfig.setBaseUrl(stripEnd(configOrDefault("GRAPHITE_URL", "http://localhost:80"), "/"));
        graphiteInstanceConfig.setUsername(configOrDefault("GRAPHITE_USERNAME", ""));
        graphiteInstanceConfig.setPassword(configOrDefault("GRAPHITE_PASSWORD", ""));
        graphiteInstanceConfig.setKeyStore(configOrDefault("GRAPHITE_KEYSTORE", ""));
        graphiteInstanceConfig.setKeyStorePassword(configOrDefault("GRAPHITE_KEYSTORE_PASSWORD", ""));
        graphiteInstanceConfig.setTrustStore(configOrDefault("GRAPHITE_TRUSTSTORE", ""));
        graphiteInstanceConfig.setCarbonPickleEnable(Boolean.parseBoolean(configOrDefault("GRAPHITE_CARBON_PICKLE_ENABLE", "false")));
        graphiteInstanceConfig.setCarbonPicklePort(Integer.parseInt(configOrDefault("GRAPHITE_CARBON_PICKLE_PORT", "2004")));
        
        graphiteInstanceConfigs.add(graphiteInstanceConfig);
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
    public boolean isBaseUrlSetToDefault() {
        return getBaseUrl().equals(DEFAULT_BASE_URL);
    }
    
    public List<GraphiteInstanceConfig> getGraphiteInstanceConfigs() {
    	return graphiteInstanceConfigs;
    }
    
    public GraphiteInstanceConfig getGraphiteInstanceConfig(String graphiteInstanceId) {
    	for (GraphiteInstanceConfig graphiteInstanceConfig : graphiteInstanceConfigs) {
    		if (graphiteInstanceId.equals(graphiteInstanceConfig.getId())) {
    			return graphiteInstanceConfig;
    		}
    	}
    	throw new RuntimeException("No such Graphite instance: id=" + graphiteInstanceId);
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
    public String getIrcCatHost() {
        return this.ircCatHost;
    }

    @JsonIgnore
    public int getIrcCatPort() {
        return Integer.valueOf(this.ircCatPort);
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
}
