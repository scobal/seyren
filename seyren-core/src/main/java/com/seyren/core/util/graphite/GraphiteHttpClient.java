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
package com.seyren.core.util.graphite;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.security.KeyStore;

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
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.seyren.core.service.checker.JsonNodeResponseHandler;
import com.seyren.core.util.config.GraphiteInstanceConfig;

// No longer component-scanning this. GraphiteManager creates GraphiteHttpClients based on the SeyrenConfig.
// [williewheeler]
//@Named
public class GraphiteHttpClient {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphiteHttpClient.class);
    private static final String THRESHOLD_TARGET = "alias(dashed(color(constantLine(%s),\"%s\")),\"%s\")";
    private static final int MAX_CONNECTIONS_PER_ROUTE = 20;
    
    private final JsonNodeResponseHandler jsonNodeHandler = new JsonNodeResponseHandler();
    private final ByteArrayResponseHandler chartBytesHandler = new ByteArrayResponseHandler();
    private final GraphiteInstanceConfig graphiteInstanceConfig;
    private final HttpClient client;
    private final HttpContext context;
    
    public GraphiteHttpClient(GraphiteInstanceConfig graphiteInstanceConfig) {
    	this.graphiteInstanceConfig = graphiteInstanceConfig;
        this.context = new BasicHttpContext();
        this.client = createHttpClient();
    }
    
    public JsonNode getTargetJson(String target) throws Exception {
    	URI baseUri = new URI(graphiteInstanceConfig.getBaseUrl() + "/render/");
        URI uri = new URIBuilder(baseUri)
                .addParameter("from", "-11minutes")
                .addParameter("until", "-1minutes")
                .addParameter("uniq", String.valueOf(new DateTime().getMillis()))
                .addParameter("format", "json")
                .addParameter("target", target).build();
        
        HttpGet get = new HttpGet(uri);
        
        try {
            return client.execute(get, jsonNodeHandler, context);
        } catch (Exception e) {
            throw new GraphiteReadException("Failed to read from Graphite", e);
        } finally {
            get.releaseConnection();
        }
    }
    
    public byte[] getChart(String target, int width, int height, String from, String to, LegendState legendState, AxesState axesState) throws Exception {
        return getChart(target, width, height, from, to, legendState, axesState, null, null);
    }
    
    public byte[] getChart(String target, int width, int height, String from, String to, LegendState legendState, AxesState axesState,
            BigDecimal warnThreshold, BigDecimal errorThreshold) throws Exception {
    	URI baseUri = new URI(graphiteInstanceConfig.getBaseUrl() + "/render/");
        URIBuilder uriBuilder = new URIBuilder(baseUri)
                .addParameter("target", target)
                .addParameter("from", from)
                .addParameter("width", String.valueOf(width))
                .addParameter("height", String.valueOf(height))
                .addParameter("uniq", String.valueOf(new DateTime().getMillis()))
                .addParameter("hideLegend", legendState == LegendState.HIDE ? "true" : "false")
                .addParameter("hideAxes", axesState == AxesState.HIDE ? "true" : "false");
        
        if (warnThreshold != null) {
            uriBuilder.addParameter("target", String.format(THRESHOLD_TARGET, warnThreshold.toString(), "yellow", "warn level"));
        }
        
        if (errorThreshold != null) {
            uriBuilder.addParameter("target", String.format(THRESHOLD_TARGET, errorThreshold.toString(), "red", "error level"));
        }
        
        HttpGet get = new HttpGet(uriBuilder.build());
        
        try {
            return client.execute(get, chartBytesHandler, context);
        } catch (Exception e) {
            throw new GraphiteReadException("Failed to read from Graphite", e);
        } finally {
            get.releaseConnection();
        }
    }
    
    private HttpClient createHttpClient() {
        DefaultHttpClient client = new DefaultHttpClient(createConnectionManager());
        
        String graphiteBaseUrl = graphiteInstanceConfig.getBaseUrl();
        String graphiteUsername = graphiteInstanceConfig.getUsername();
        String graphitePassword = graphiteInstanceConfig.getPassword();
        String graphiteKeyStore = graphiteInstanceConfig.getKeyStore();
        String graphiteKeyStorePassword = graphiteInstanceConfig.getKeyStorePassword();
        String graphiteTrustStore = graphiteInstanceConfig.getTrustStore();
        
        boolean usingSSL = graphiteBaseUrl.startsWith("https:");
        
        // Set auth header for graphite if username and password are provided
        if (!StringUtils.isEmpty(graphiteUsername) && !StringUtils.isEmpty(graphitePassword)) {
            client.getCredentialsProvider().setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(graphiteUsername, graphitePassword));
            context.setAttribute("preemptive-auth", new BasicScheme());
            client.addRequestInterceptor(new PreemptiveAuth(), 0);
        }
        
        // Set SSL configuration if keystore and truststore are provided
        if (usingSSL && !StringUtils.isEmpty(graphiteKeyStore) && !StringUtils.isEmpty(graphiteKeyStorePassword) && !StringUtils.isEmpty(graphiteTrustStore)) {
            try {
            	URI graphiteBaseUri = new URI(graphiteBaseUrl);
            	String graphiteScheme = graphiteBaseUri.getScheme();
            	int graphiteSSLPort = graphiteBaseUri.getPort();
            	
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
                sslContext.init(keyManagers, trustManagers, null);
                
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
                } catch (IOException e) {
                }
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
            HttpHost targetHost = (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
            Credentials credentials = credsProvider.getCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()));
            authState.update(authScheme, credentials);
        }
    }
    
}
