/* 
 * Copyright (c) 2013 Expedia, Inc. All rights reserved.
 */
package com.seyren.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a Graphite instance. Right now all it has is a name and a URL, but we can add
 * credentials and SSL and all that good stuff if we feel like it.
 * 
 * @author Willie Wheeler (wwheeler@expedia.com)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphiteInstance {
	private String id;
	private String name;
	private String baseUrl;
	
	public String getId() { return id; }
	
	public void setId(String id) { this.id = id; }
	
	public GraphiteInstance withId(String id) {
		setId(id);
		return this;
	}
	
	public String getName() { return name; }
	
	public void setName(String name) { this.name = name; }
	
	public GraphiteInstance withName(String name) {
		setName(name);
		return this;
	}
	
	public String getBaseUrl() { return baseUrl; }
	
	public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
	
	public GraphiteInstance withBaseUrl(String baseUrl) {
		setBaseUrl(baseUrl);
		return this;
	}
}
