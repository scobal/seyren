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

import javax.inject.Named;

@Named
public class GraphiteConfig {
	
	private final String baseUrl;

	public GraphiteConfig() {
		this.baseUrl = stripEnd(environmentOrDefault("GRAPHITE_URL", "http://localhost:80"), "/");
	}

	public String getBaseUrl() {
		return baseUrl;
	}
	
	private static String environmentOrDefault(String propertyName, String defaultValue) {
	    String value = System.getenv(propertyName);
	    if (isEmpty(value)) {
	        return defaultValue;
	    }
	    return value;
	}
	
}
