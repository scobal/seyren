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

import com.fasterxml.jackson.databind.JsonNode;
import com.github.restdriver.serverdriver.http.response.Response;
import org.junit.Test;

import static com.github.restdriver.serverdriver.Matchers.hasJsonPath;
import static com.github.restdriver.serverdriver.Matchers.hasStatusCode;
import static com.github.restdriver.serverdriver.RestServerDriver.*;
import static com.seyren.acceptancetests.util.SeyrenDriver.checks;
import static com.seyren.acceptancetests.util.SeyrenDriver.filters;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class FiltersAT {

    @Test
    public void testFiltersShouldBeSavedAndRetrievedAndDeleted() {
        String body = "{ \"name\" : \"TestFilter\", \"filter\" : \"app.mx.test.rate5\" }";
        Response response = post(filters(), body(body, "application/json"));
        assertThat(response, hasStatusCode(201));

        body = "{ \"name\" : \"TestFilter2\", \"filter\" : \"app.ar.test.rate5\" }";
        response = post(filters(), body(body, "application/json"));
        assertThat(response, hasStatusCode(201));

        Response newResponse = get(filters());
        assertThat(newResponse, hasStatusCode(200));
        JsonNode responseJson = newResponse.asJson();
        assertThat(responseJson.findValues("id").size(), greaterThanOrEqualTo(2));

        String idToBeDeleted = responseJson.findValues("id").get(0).asText();
        response = delete(new StringBuilder().append(filters()).append("/").append(idToBeDeleted));
        assertThat(response, hasStatusCode(204));

        newResponse = get(filters());
        responseJson = newResponse.asJson();
        assertThat(newResponse, hasStatusCode(200));
        assertThat(responseJson.findValues("id").size(), is(1));
    }


}
