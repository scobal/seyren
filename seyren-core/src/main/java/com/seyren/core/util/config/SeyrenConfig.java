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
    private final String fromEmail;
    private final String pagerDutyDomain;
    private final String hipChatAuthToken;
    private final String hipChatUserName;
    private final String hubotUrl;
    
    @Inject
    public SeyrenConfig(GraphiteConfig graphite) {
        this.graphite = graphite;
        this.baseUrl = stripEnd(environmentOrDefault("SEYREN_URL", "http://localhost:8080/seyren"), "/");
        this.fromEmail = environmentOrDefault("SEYREN_FROM_EMAIL", "alert@seyren");
        this.pagerDutyDomain = environmentOrDefault("PAGERDUTY_DOMAIN", "");
        this.hipChatAuthToken = environmentOrDefault("HIPCHAT_AUTH_TOKEN", "");
        this.hipChatUserName = environmentOrDefault("HIPCHAT_USER_NAME", "Seyren Alert");
        this.hubotUrl = environmentOrDefault("SEYREN_HUBOT_URL", "");
    }
    
    public GraphiteConfig getGraphite() {
        return graphite;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public String getFromEmail() {
        return fromEmail;
    }
    
    public String getPagerDutyDomain() {
        return pagerDutyDomain;
    }
    
    public String getHipChatAuthToken() {
        return hipChatAuthToken;
    }
    
    public String getHipChatUserName() {
        return hipChatUserName;
    }
    
    public String getHubotUrl() {
        return hubotUrl;
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
