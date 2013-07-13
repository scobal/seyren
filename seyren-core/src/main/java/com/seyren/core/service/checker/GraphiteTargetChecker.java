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

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;
import com.seyren.core.domain.Check;
import com.seyren.core.exception.InvalidGraphiteValueException;
import com.seyren.core.util.config.SeyrenConfig;

@Named
public class GraphiteTargetChecker implements TargetChecker {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphiteTargetChecker.class);
    private static final String QUERY_STRING = "from=-11minutes&until=-1minutes&uniq=%s&format=json&target=%s";
    private static final int MAX_CONNECTIONS_PER_ROUTE = 20;
    
    private final JsonNodeResponseHandler handler = new JsonNodeResponseHandler();
    private final String graphiteScheme;
    private final String graphiteHost;
    private final String graphitePath;
    private final String graphiteUsername;
    private final String graphitePassword;
    private final String graphiteKeyStore;
    private final String graphiteKeyStorePassword;
    private final String graphiteTrustStore;
    private final int graphiteSSLPort;
    private final HttpClient client;
    private final HttpContext context;
    
    @Inject
    public GraphiteTargetChecker(SeyrenConfig seyrenConfig) {
        this.graphiteScheme = seyrenConfig.getGraphiteScheme();
        this.graphiteHost = seyrenConfig.getGraphiteHost();
        this.graphitePath = seyrenConfig.getGraphitePath();
        this.graphiteUsername = seyrenConfig.getGraphiteUsername();
        this.graphitePassword = seyrenConfig.getGraphitePassword();
        this.graphiteKeyStore = seyrenConfig.getGraphiteKeyStore();
        this.graphiteKeyStorePassword = seyrenConfig.getGraphiteKeyStorePassword();
        this.graphiteTrustStore = seyrenConfig.getGraphiteTrustStore();
        this.graphiteSSLPort = seyrenConfig.getGraphiteSSLPort();
        this.context = new BasicHttpContext();
        this.client = createHttpClient();
    }
    
    @Override
    public Map<String, Optional<BigDecimal>> check(Check check) throws Exception {
        
        String formattedQuery = String.format(QUERY_STRING, new DateTime().getMillis(), check.getTarget());
        URI uri = new URI(graphiteScheme, graphiteHost, graphitePath + "/render/", formattedQuery, null);
        HttpGet get = new HttpGet(uri);
        Map<String, Optional<BigDecimal>> targetValues = new HashMap<String, Optional<BigDecimal>>();
        
        try {
            JsonNode response = client.execute(get, handler, context);
            for (JsonNode metric : response) {
                String target = metric.path("target").asText();
                
                try {
                    BigDecimal value = getLatestValue(metric);
                    targetValues.put(target, Optional.of(value));
                } catch (InvalidGraphiteValueException e) {
                    // Silence these - we don't know what's causing Graphite to return null values
                    LOGGER.warn(check.getName() + " failed to read from Graphite", e);
                    targetValues.put(target, Optional.<BigDecimal> absent());
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
    
    private HttpClient createHttpClient() {
        DefaultHttpClient client = new DefaultHttpClient(createConnectionManager());
        
        // Set auth header for graphite if username and password are provided
        if (!StringUtils.isEmpty(graphiteUsername) && !StringUtils.isEmpty(graphitePassword)) {
            client.getCredentialsProvider().setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(graphiteUsername, graphitePassword));
            context.setAttribute("preemptive-auth", new BasicScheme());
            client.addRequestInterceptor(new PreemptiveAuth(), 0);
        }

        // Set SSL configuration if keystore and truststore are provided
        if ("https".equals(graphiteScheme) && !StringUtils.isEmpty(graphiteKeyStore) && !StringUtils.isEmpty(graphiteKeyStorePassword) && !StringUtils.isEmpty(graphiteTrustStore)) {
            try {
                // Read the keystore and trustore
                KeyStore keyStore = loadKeyStore(graphiteKeyStore, graphiteKeyStorePassword);
                KeyStore trustStore = loadKeyStore(graphiteTrustStore, null);

                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(keyStore, graphiteKeyStorePassword.toCharArray());
                KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(trustStore);
                TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(keyManagers,trustManagers, null);

                SSLSocketFactory socketFactory = new SSLSocketFactory(sslContext);
                Scheme scheme = new Scheme(graphiteScheme, graphiteSSLPort, socketFactory);
                client.getConnectionManager().getSchemeRegistry().register(scheme);
            } catch (Exception e) {
                LOGGER.warn("A problem occurs when building SSLSocketFactory", e);
            }
        }
        return client;
    }

    private KeyStore loadKeyStore(String keyStorePath, String password) throws Exception {
        FileInputStream keyStoreInput = null;
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStoreInput = new FileInputStream(keyStorePath);
            keyStore.load(keyStoreInput, password == null ? null : password.toCharArray());
            return keyStore;
        } catch (Exception e) {
          LOGGER.warn("A problem occurs when loading keystore {}", keyStorePath);
          throw e;
        } finally {
          if (keyStoreInput != null) {
              try {
                keyStoreInput.close();
              } catch (IOException e) {}
            }
        }
    }

  private ClientConnectionManager createConnectionManager() {
        PoolingClientConnectionManager manager = new PoolingClientConnectionManager();
        manager.setDefaultMaxPerRoute(MAX_CONNECTIONS_PER_ROUTE);
        return manager;
    }
    
    /*
     * Adapted from an answer to this question on Stack Overflow:
     * http://stackoverflow.com/questions/2014700/preemptive-basic-authentication-with-apache-httpclient-4
     * Code originally came from here:
     * http://subversion.jfrog.org/jfrog/build-info/trunk/build-info-client/src/main/java/org/jfrog/build/client/PreemptiveHttpClient.java
     */
    private static class PreemptiveAuth implements HttpRequestInterceptor {
        public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
            AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
            if (authState.getAuthScheme() != null) {
                return;
            }
            AuthScheme authScheme = (AuthScheme) context.getAttribute("preemptive-auth");
            if (authScheme == null) {
                return;
            }
            CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(ClientContext.CREDS_PROVIDER);
            HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
            Credentials credentials = credsProvider.getCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()));
            authState.update(authScheme, credentials);
        }
    }
    
}
