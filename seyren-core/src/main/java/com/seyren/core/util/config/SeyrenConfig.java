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

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.stripEnd;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;

@Named
public class SeyrenConfig {

	private final GraphiteConfig graphite;
	private final String baseUrl;
	private final String fromEmail;
	private final String pagerDutyDomain;
	private final Map<String, String> overrides;

	public SeyrenConfig() {
		this(new HashMap<String, String>());
	}

	public SeyrenConfig(Map<String, String> overrides) {
		this.overrides = overrides;
		this.graphite = new GraphiteConfig(getConfigProperty("GRAPHITE_URL", "http://localhost:80"), 
				getConfigProperty("GRAPHITE_USERNAME", ""), 
				getConfigProperty("GRAPHITE_PASSWORD", ""));
		this.baseUrl = stripEnd( getConfigProperty("SEYREN_URL", "http://localhost:8080/seyren"), "/");
		this.fromEmail = getConfigProperty("SEYREN_FROM_EMAIL", "alert@seyren");
		this.pagerDutyDomain = getConfigProperty("PAGERDUTY_DOMAIN", "");

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
    
	public void setProperty(String propertyName, String propertyValue) {
		this.overrides.put(propertyName, propertyValue);
	}
    
    public String getConfigProperty(String propertyName, String defaultValue) {
		String value;
		value = overrides.get(propertyName);
		if (!isEmpty(value)) {
			return value;
		}
		value = System.getenv(propertyName);
		if (!isEmpty(value)) {
			return value;
		}
		return defaultValue;
	}
	
}
