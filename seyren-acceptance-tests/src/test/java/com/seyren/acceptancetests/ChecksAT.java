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

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;

import com.github.restdriver.serverdriver.http.response.Response;

import java.util.HashSet;
import java.util.Set;

public class ChecksAT {
    
    @Test
    public void testGetChecksReturnsOk() {
        Response response = get(checks());
        assertThat(response, hasStatusCode(200));
        assertThat(response.asJson(), hasJsonPath("$.values", hasSize(0)));
    }
    
    @Test
    public void testGetChecksReturnsResultsOk() {
        Response createResponse = createCheck("{ }");
        Response response = get(checks());
        assertThat(response, hasStatusCode(200));
        assertThat(response.asJson(), hasJsonPath("$.values", hasSize(1)));
        deleteLocation(createResponse.getHeader("Location").getValue());
    }
    
    @Test
    public void testGetChecksByErrorStateReturnsOk() {
        Response createResponse = createCheck("{ \"state\" : \"ERROR\" }");
        Response response = get(checks().withParam("state", "ERROR"));
        assertThat(response, hasStatusCode(200));
        assertThat(response.asJson(), hasJsonPath("$.values", hasSize(1)));
        deleteLocation(createResponse.getHeader("Location").getValue());
    }
    
    @Test
    public void testGetChecksByWarnStateReturnsOk() {
        Response createResponse = createCheck("{ \"state\" : \"WARN\" }");
        Response response = get(checks().withParam("state", "WARN"));
        assertThat(response, hasStatusCode(200));
        assertThat(response.asJson(), hasJsonPath("$.values", hasSize(1)));
        deleteLocation(createResponse.getHeader("Location").getValue());
    }
    
    @Test
    public void testCreateCheckReturnsCreated() {
        Response response = createCheck("{ }");
        assertThat(response, hasStatusCode(201));
        deleteLocation(response.getHeader("Location").getValue());
    }
    
    @Test
    public void testCreateCheckWithErrorState() {
        Response response = createCheck("{ \"state\" : \"ERROR\" }");
        assertThat(response, hasStatusCode(201));
        String location = response.getHeader("Location").getValue();
        assertThat(get(location).asJson(), hasJsonPath("$.state", is("ERROR")));
        deleteLocation(location);
    }

    @Test
    public void testCreateCheckWithSubscriptionIncluded() {
        Response response = createCheck("{ \"name\": \"test\", \"warn\": 1.0, \"error\": 0, " +
                "\"subscriptions\": [ { \"target\": \"nobody@test.com\", \"type\":\"EMAIL\" } ] }");
        assertThat(response, hasStatusCode(201));
        String location = response.getHeader("Location").getValue();
        assertThat(get(location).asJson(), hasJsonPath("$.subscriptions", hasSize(1)));
        deleteLocation(location);
    }
    
    @Test
    public void testUpdateHandlesNullLastCheckDate() {
        Response response = createCheck("{ \"name\": \"test\", \"warn\": 1.0, \"error\": 2.0 }");
        assertThat(response, hasStatusCode(201));
        String location = response.getHeader("Location").getValue();
        assertThat(put(location, body(get(location).asText(), "application/json")), hasStatusCode(200));
        deleteLocation(location);
    }

    @Test
    public void testShouldGetOneCheckByIsolatedNamePattern() {
        Set<String> locations =  createStubChecksForPatternMatching();

        Response response = get(checks().withParam("fields", "name").withParam("regexes", "patternName1"));
        assertThat(response, hasStatusCode(200));
        JsonNode responseJson = response.asJson();
        assertThat(responseJson, hasJsonPath("$.values", hasSize(1)));

        cleanupChecks(locations);
    }

    @Test
    public void testShouldGetAllChecksByMutualNamePattern() {
        Set<String> locations =  createStubChecksForPatternMatching();

        Response response = get(checks().withParam("fields", "name").withParam("regexes", "patternName"));
        assertThat(response, hasStatusCode(200));
        JsonNode responseJson = response.asJson();
        assertThat(responseJson, hasJsonPath("$.values", hasSize(3)));

        cleanupChecks(locations);
    }

    @Test
    public void testShouldGetMatchingChecksByRegexCharacterNamePattern() {
        Set<String> locations =  createStubChecksForPatternMatching();

        Response response = get(checks().withParam("fields", "name").withParam("regexes", "patternName\\d"));
        assertThat(response, hasStatusCode(200));
        JsonNode responseJson = response.asJson();
        assertThat(responseJson, hasJsonPath("$.values", hasSize(2)));

        cleanupChecks(locations);
    }

    @Test
    public void testShouldGetNoChecksByNonexistentNamePattern() {
        Set<String> locations =  createStubChecksForPatternMatching();

        Response response = get(checks().withParam("fields", "name").withParam("regexes", "doesnotexist"));
        assertThat(response, hasStatusCode(200));
        JsonNode responseJson = response.asJson();
        assertThat(responseJson, hasJsonPath("$.values", hasSize(0)));

        cleanupChecks(locations);
    }

    private Set<String>  createStubChecksForPatternMatching() {
        Set<String> locations = new HashSet<String>();

        locations.add(
                createCheck("{ \"name\": \"patternName1\", \"warn\": 1.0, \"error\": 2.0 }").
                        getHeader("Location").getValue());

        locations.add(
                createCheck("{ \"name\": \"patternName2\", \"warn\": 1.0, \"error\": 2.0 }").
                        getHeader("Location").getValue());

        locations.add(
                createCheck("{ \"name\": \"patternNameA\", \"warn\": 1.0, \"error\": 2.0 }").
                        getHeader("Location").getValue());

        return locations;
    }

    private void cleanupChecks(Set<String> locations) {
        for (String location : locations) {
            deleteLocation(location);
        }
    }

    private Response createCheck(String body) {
        Response response = post(checks(), body(body, "application/json"));
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
