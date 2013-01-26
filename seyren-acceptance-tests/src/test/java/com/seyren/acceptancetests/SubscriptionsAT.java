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
package com.seyren.acceptancetests;

import static com.github.restdriver.serverdriver.Matchers.*;
import static com.github.restdriver.serverdriver.RestServerDriver.*;
import static com.seyren.acceptancetests.util.SeyrenDriver.*;
import static org.hamcrest.MatcherAssert.*;

import org.junit.Test;

import com.github.restdriver.serverdriver.http.Header;
import com.github.restdriver.serverdriver.http.response.Response;

public class SubscriptionsAT {
    
    @Test
    public void testCreateSubscriptionReturnsCreated() {
        Header checkLocation = createCheck("{ }").getHeader("Location");
        Response response = createSubscription(checkLocation, "{ }");
        assertThat(response, hasStatusCode(201));
        deleteLocation(checkLocation.getValue());
    }
    
    private Response createCheck(String body) {
        Response response = post(checks(), body(body, "application/json"));
        assertThat(response, hasStatusCode(201));
        assertThat(response, hasHeader("Location"));
        return response;
    }
    
    private Response createSubscription(Header checkLocation, String body) {
        Response response = post(subscriptions(checkLocation), body(body, "application/json"));
        assertThat(response, hasStatusCode(201));
        assertThat(response, hasHeader("Location"));
        return response;
    }
    
    private void deleteLocation(String location) {
        assertThat(get(location), hasStatusCode(200));
        delete(location);
        assertThat(get(location), hasStatusCode(404));
    }
    
}
