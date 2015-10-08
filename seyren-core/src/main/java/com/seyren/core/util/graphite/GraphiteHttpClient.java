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
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.seyren.core.service.checker.JsonNodeResponseHandler;
import com.seyren.core.util.config.SeyrenConfig;

@Named
public class GraphiteHttpClient {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphiteHttpClient.class);
    private static final String THRESHOLD_TARGET = "alias(dashed(color(constantLine(%s),\"%s\")),\"%s\")";
    private static final int MAX_CONNECTIONS_PER_ROUTE = 20;
    
    private final JsonNodeResponseHandler jsonNodeHandler = new JsonNodeResponseHandler();
    private final ByteArrayResponseHandler chartBytesHandler = new ByteArrayResponseHandler();
    private final String graphiteScheme;
    private final String graphiteHost;
    private final String graphitePath;
    private final String graphiteUsername;
    private final String graphitePassword;
    private final String graphiteKeyStore;
    private final String graphiteKeyStorePassword;
    private final String graphiteTrustStore;
    private final int graphiteConnectionRequestTimeout;
    private final int graphiteConnectTimeout;
    private final int graphiteSocketTimeout;
    private final HttpClient client;
    private final HttpContext context;
    
    @Inject
    public GraphiteHttpClient(SeyrenConfig seyrenConfig) {
        this.graphiteScheme = seyrenConfig.getGraphiteScheme();
        this.graphiteHost = seyrenConfig.getGraphiteHost();
        this.graphitePath = seyrenConfig.getGraphitePath();
        this.graphiteUsername = seyrenConfig.getGraphiteUsername();
        this.graphitePassword = seyrenConfig.getGraphitePassword();
        this.graphiteKeyStore = seyrenConfig.getGraphiteKeyStore();
        this.graphiteKeyStorePassword = seyrenConfig.getGraphiteKeyStorePassword();
        this.graphiteTrustStore = seyrenConfig.getGraphiteTrustStore();
        this.graphiteConnectionRequestTimeout = seyrenConfig.getGraphiteConnectionRequestTimeout();
        this.graphiteConnectTimeout = seyrenConfig.getGraphiteConnectTimeout();
        this.graphiteSocketTimeout = seyrenConfig.getGraphiteSocketTimeout();
        this.context = new BasicHttpContext();
        this.client = createHttpClient();
    }

    /**
     * @deprecated Use {link}getTargetJson(String target, String from, String until){link} instead.
     */
    @Deprecated
    public JsonNode getTargetJson(String target) throws Exception {
        return getTargetJson(target, null, null);
    }

    public JsonNode getTargetJson(String target, String from, String until) throws Exception {
        // Default values for from/until preserve hard-coded functionality
        // seyren had before from/until were fields that could be specified.
        if (from == null) {
            from = "-11minutes";
        }
        if (until == null) {
            until = "-1minutes";
        }
        URI baseUri = new URI(graphiteScheme, graphiteHost, graphitePath + "/render/", null, null);
        URI uri = new URIBuilder(baseUri)
                .addParameter("from", from)
                .addParameter("until", until)
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
        URI baseUri = new URI(graphiteScheme, graphiteHost, graphitePath + "/render/", null, null);
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
        HttpClientBuilder clientBuilder = HttpClientBuilder.create().useSystemProperties()
                .setConnectionManager(createConnectionManager())
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectionRequestTimeout(graphiteConnectionRequestTimeout)
                        .setConnectTimeout(graphiteConnectTimeout)
                        .setSocketTimeout(graphiteSocketTimeout)
                        .build());
        
        // Set auth header for graphite if username and password are provided
        if (!StringUtils.isEmpty(graphiteUsername) && !StringUtils.isEmpty(graphitePassword)) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(graphiteUsername, graphitePassword));
            clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            context.setAttribute("preemptive-auth", new BasicScheme());
            clientBuilder.addInterceptorFirst(new PreemptiveAuth());
        }
        
        return clientBuilder.build();
    }
    
    private KeyStore loadKeyStore(String keyStorePath, String password) throws Exception {
        FileInputStream keyStoreInput = null;
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStoreInput = new FileInputStream(keyStorePath);
            keyStore.load(keyStoreInput, password == null ? null : password.toCharArray());
            return keyStore;
        } catch (Exception e) {
            LOGGER.warn("A problem occurred when loading keystore {}", keyStorePath);
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
    
    private HttpClientConnectionManager createConnectionManager() {
        PoolingHttpClientConnectionManager manager;
        if ("https".equals(graphiteScheme) && !StringUtils.isEmpty(graphiteKeyStore) && !StringUtils.isEmpty(graphiteKeyStorePassword) && !StringUtils.isEmpty(graphiteTrustStore)) {
            try {
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
                
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
                
                Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
                        .register("https", sslsf).build();
                
                manager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            } catch (Exception e) {
                LOGGER.warn("A problem occurred when building SSLConnectionSocketFactory", e);
                throw new RuntimeException("Error while building SSLConnectionSocketFactory", e);
            }
        } else {
            manager = new PoolingHttpClientConnectionManager();
        }
        
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
            AuthState authState = (AuthState) context.getAttribute(HttpClientContext.TARGET_AUTH_STATE);
            if (authState.getAuthScheme() != null) {
                return;
            }
            AuthScheme authScheme = (AuthScheme) context.getAttribute("preemptive-auth");
            if (authScheme == null) {
                return;
            }
            CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(HttpClientContext.CREDS_PROVIDER);
            HttpHost targetHost = (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
            Credentials credentials = credsProvider.getCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()));
            authState.update(authScheme, credentials);
        }
    }
    
}
