package com.seyren.acceptancetests.util;

import com.github.restdriver.serverdriver.http.Url;

public final class SeyrenDriver {
    
    private SeyrenDriver() {
    }

	private static final String DEFAULT_HOST = "localhost";
	private static final String DEFAULT_PORT = "8080";
	private static final String DEFAULT_CONTEXT_ROOT = "seyren";
    private static final int DEFAULT_REST_DRIVER_PORT = 8081;

	public static Url checks() {
		return baseUri().withPath("checks");
	}
	
	public static Url alerts(String checkId) {
		return checks().withPath("/" + checkId + "/alerts");
	}
    
	private static Url baseUri() {
		return new Url("http://" + host() + ":" + port() + "/" + contextRoot()).withPath("api");
	}

    public static int getRestDriverPort() {
        return DEFAULT_REST_DRIVER_PORT;
    }
	
	private static String host() {
		return System.getProperty("seyren.host", DEFAULT_HOST);
	}

	private static String port() {
		return System.getProperty("seyren.port", DEFAULT_PORT);
	}

	private static String contextRoot() {
		return System.getProperty("seyren.contextRoot", DEFAULT_CONTEXT_ROOT);
	}
	
}
