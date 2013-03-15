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
    
    private final String[] baseParts;
    private final String username;
    private final String password;
    
    public GraphiteConfig() {
        this(stripEnd(SeyrenConfig.configOrDefault("GRAPHITE_URL", "http://localhost:80"), "/"));
    }
    
    public GraphiteConfig(String baseUrl) {
        this.baseParts = splitBaseUrl(baseUrl);
        this.username = SeyrenConfig.configOrDefault("GRAPHITE_USERNAME", "");
        this.password= SeyrenConfig.configOrDefault("GRAPHITE_PASSWORD", "");
    }
    
    String getScheme() {
        return baseParts[0];
    }
    
    String getHost() {
        return baseParts[1];
    }
    
    String getPath() {
        return baseParts[2];
    }
    
    String getUsername() {
        return username;
    }
    
    String getPassword() {
        return password;
    }
    
    private String[] splitBaseUrl(String baseUrl) {
        String[] baseParts = new String[3];
        
        if (baseUrl.toString().contains("://")) {
            baseParts[0] = baseUrl.toString().split("://")[0];
            baseUrl = baseUrl.toString().split("://")[1];
        } else {
            baseParts[0] = "http";
        }
        
        if (baseUrl.contains("/")) {
            baseParts[1] = baseUrl.split("/")[0];
            baseParts[2] = "/" + baseUrl.split("/", 2)[1];
        } else {
            baseParts[1] = baseUrl;
            baseParts[2] = "";
        }
        
        return baseParts;
    }
    
}
