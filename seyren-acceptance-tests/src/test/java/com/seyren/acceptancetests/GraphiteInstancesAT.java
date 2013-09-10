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
import static org.hamcrest.Matchers.*;

import org.junit.Test;

import com.github.restdriver.serverdriver.http.response.Response;

/**
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 */
public class GraphiteInstancesAT {
    
    @Test
    public void testGetGraphiteInstancesReturnsOk() {
        Response response = get(graphiteInstances());
        assertThat(response, hasStatusCode(200));
        assertThat(response.asJson(), hasJsonPath("$.values", hasSize(0)));
    }
    
    @Test
    public void testGetGraphiteInstancesReturnsResultsOk() {
        Response createResponse = createGraphiteInstance("{ }");
        Response response = get(graphiteInstances());
        assertThat(response, hasStatusCode(200));
        assertThat(response.asJson(), hasJsonPath("$.values", hasSize(1)));
        deleteLocation(createResponse.getHeader("Location").getValue());
    }
    
    @Test
    public void testCreateGraphiteInstanceReturnsCreated() {
        Response response = createGraphiteInstance("{ }");
        assertThat(response, hasStatusCode(201));
        deleteLocation(response.getHeader("Location").getValue());
    }
    
    @Test
    public void testCreateGraphiteInstanceWithName() {
        Response response = createGraphiteInstance("{ \"name\" : \"Graphite - Lab\" }");
        assertThat(response, hasStatusCode(201));
        String location = response.getHeader("Location").getValue();
        assertThat(get(location).asJson(), hasJsonPath("$.name", is("Graphite - Lab")));
        deleteLocation(location);
    }
    
    private Response createGraphiteInstance(String body) {
        Response response = post(graphiteInstances(), body(body, "application/json"));
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
