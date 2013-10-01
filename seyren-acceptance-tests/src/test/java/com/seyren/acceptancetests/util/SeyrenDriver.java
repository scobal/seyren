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
package com.seyren.acceptancetests.util;

import com.github.restdriver.serverdriver.http.Header;
import com.github.restdriver.serverdriver.http.Url;

public final class SeyrenDriver {
    
    private SeyrenDriver() {
    }
    
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "8080";
    private static final String DEFAULT_CONTEXT_ROOT = "seyren";
    private static final int DEFAULT_REST_DRIVER_PORT = 8081;
    
    public static Url config() {
        return baseUri().withPath("config");
    }
    
    public static Url checks() {
        return baseUri().withPath("checks");
    }
    
    public static Url check(String checkId) {
        return checks().withPath("/" + checkId);
    }
    
    public static Url alerts(String checkId) {
        return check(checkId).withPath("/alerts");
    }
    
    public static Url subscriptions(String checkId) {
        return check(checkId).withPath("/subscriptions");
    }
    
    public static Url subscriptions(Header checkLocation) {
        return new Url(checkLocation.getValue()).withPath("/subscriptions");
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
