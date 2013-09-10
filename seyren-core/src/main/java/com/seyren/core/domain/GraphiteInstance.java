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
package com.seyren.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * <p>
 * Represents a Graphite instance.
 * </p>
 * <p>
 * The intent is to provide a way to consume multiple Graphite instances from a single Seyren instance.
 * </p>
 * 
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphiteInstance {
    private String id;
    private String name;
    private String baseUrl;
    private String username;
    private String password;
    private String keyStore;
    private String keyStorePassword;
    private String trustStore;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public GraphiteInstance withId(String id) {
        setId(id);
        return this;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public GraphiteInstance withName(String name) {
        setName(name);
        return this;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public GraphiteInstance withBaseUrl(String baseUrl) {
        setBaseUrl(baseUrl);
        return this;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public GraphiteInstance withUsername(String username) {
        setUsername(username);
        return this;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public GraphiteInstance withPassword(String password) {
        setPassword(password);
        return this;
    }

    public String getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }
    
    public GraphiteInstance withKeyStore(String keyStore) {
        setKeyStore(keyStore);
        return this;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }
    
    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }
    
    public GraphiteInstance withKeyStorePassword(String keyStorePassword) {
        setKeyStorePassword(keyStorePassword);
        return this;
    }

    public String getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }
    
    public GraphiteInstance withTrustStore(String trustStore) {
        setTrustStore(trustStore);
        return this;
    }

}
