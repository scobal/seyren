package com.seyren.core.util.graphite;

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
