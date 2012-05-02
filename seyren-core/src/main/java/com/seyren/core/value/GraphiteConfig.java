package com.seyren.core.value;


public class GraphiteConfig {
	
	private final String host;
	private final String path;

	public GraphiteConfig(String host, String path) {
		this.host = host;
		this.path = path;
	}

	public String getHost() {
		return host;
	}

	public String getPath() {
		return path;
	}
	
	public String getUri() {
		return getHost() + getPath();
	}
	
}
