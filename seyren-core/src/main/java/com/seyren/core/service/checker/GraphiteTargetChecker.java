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
package com.seyren.core.service.checker;

import java.math.BigDecimal;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.codehaus.jackson.JsonNode;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.seyren.core.domain.Check;
import com.seyren.core.exception.InvalidGraphiteValueException;
import com.seyren.core.util.config.GraphiteConfig;

@Named
public class GraphiteTargetChecker implements TargetChecker {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphiteTargetChecker.class);
    private static final String QUERY_STRING = "from=-11minutes&until=-1minutes&uniq=%s&format=json&target=%s";
    private static final int MAX_CONNECTIONS_PER_ROUTE = 20;

	private final HttpClient client;
	private final String graphiteScheme;
	private final String graphiteHost;
	private final String graphitePath;
	private final String graphiteUsername;
	private final String graphitePassword;
	private final JsonNodeResponseHandler handler = new JsonNodeResponseHandler();
	
	@Inject
	public GraphiteTargetChecker(GraphiteConfig graphiteConfig) {
	    this.graphiteScheme = graphiteConfig.getScheme();
	    this.graphiteHost = graphiteConfig.getHost();
	    this.graphitePath = graphiteConfig.getPath();
		this.graphiteUsername = graphiteConfig.getUsername();
		this.graphitePassword = graphiteConfig.getPassword();	    
	    this.client = new DefaultHttpClient(createConnectionManager());
	    setAuthHeadersIfNecessary();
	}
	
	@Override
	public Map<String, Optional<BigDecimal>> check(Check check) throws Exception {
	    
	    String formattedQuery = String.format(QUERY_STRING, new DateTime().getMillis(), check.getTarget());
	    URI uri = new URI(graphiteScheme, graphiteHost, graphitePath + "/render/", formattedQuery, null);
		HttpGet get = new HttpGet(uri);
		Map<String, Optional<BigDecimal>> targetValues = new HashMap<String, Optional<BigDecimal>>();

		try {
		    JsonNode response = client.execute(get, handler);
			for (JsonNode metric : response) {
    			String target = metric.path("target").asText();
    			
    			try {
        			BigDecimal value = getLatestValue(metric);
        			targetValues.put(target, Optional.of(value));
    			} catch (InvalidGraphiteValueException e) {
    			    // Silence these - we don't know what's causing Graphite to return null values
    			    LOGGER.warn(check.getName() + " failed to read from Graphite", e);
    			    targetValues.put(target, Optional.<BigDecimal>absent());
    			}
			}
		} catch (Exception e) {
		    LOGGER.warn(check.getName() + " failed to read from Graphite", e);
		} finally {
			get.releaseConnection();
		}
		
		return targetValues;
		
	}

	/**
	 * Loop through the datapoints in reverse order until we find the latest non-null value
	 */
	private BigDecimal getLatestValue(JsonNode node) throws Exception {
		JsonNode datapoints = node.get("datapoints");
		
		for (int i = datapoints.size() - 1; i >= 0; i--) {
			String value = datapoints.get(i).get(0).asText();
			if (!value.equals("null")) {
				return new BigDecimal(value);
			}
		}

		LOGGER.warn("{}", node);
		throw new InvalidGraphiteValueException("Could not find a valid datapoint for target: " + node.get("target"));
	}

    private ClientConnectionManager createConnectionManager() {
        PoolingClientConnectionManager manager = new PoolingClientConnectionManager();
        manager.setDefaultMaxPerRoute(MAX_CONNECTIONS_PER_ROUTE);
        return manager;
    }
    
    /**
	 * Set auth header for graphite if username and password are provided
	 */
    private void setAuthHeadersIfNecessary(){
    	if (!StringUtils.isEmpty(graphiteUsername) && !StringUtils.isEmpty(graphitePassword)) {
    		CredentialsProvider credsProvider = new BasicCredentialsProvider();
    		credsProvider.setCredentials(
    				new AuthScope(graphiteHost, AuthScope.ANY_PORT), 
    				new UsernamePasswordCredentials(graphiteUsername, graphitePassword));
    		if (client instanceof AbstractHttpClient) {
    			((AbstractHttpClient) client).setCredentialsProvider(credsProvider);
    		}
    	}
    }

}
