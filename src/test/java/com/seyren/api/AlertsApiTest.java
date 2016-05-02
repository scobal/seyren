/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.seyren.api;

import com.github.restdriver.serverdriver.http.response.Response;

import org.junit.Test;

import static com.github.restdriver.serverdriver.Matchers.hasJsonPath;
import static com.github.restdriver.serverdriver.Matchers.hasStatusCode;
import static com.github.restdriver.serverdriver.RestServerDriver.get;
import static com.seyren.api.SeyrenDriver.alerts;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class AlertsApiTest {

    @Test
    public void testGetAlertsReturnsOk() {
        Response response = get(alerts("1"));
        assertThat(response, hasStatusCode(200));
        assertThat(response.asJson(), hasJsonPath("$.values", hasSize(0)));
        assertThat(response.asJson(), hasJsonPath("$.items", is(20)));
        assertThat(response.asJson(), hasJsonPath("$.start", is(0)));
        assertThat(response.asJson(), hasJsonPath("$.total", is(0)));
    }

    @Test
    public void testGetAlertsInvalidStart() {
        Response response = get(alerts("1").withParam("start", "-1"));
        assertThat(response, hasStatusCode(400));
    }

    @Test
    public void testGetAlertsInvalidItems() {
        Response response = get(alerts("1").withParam("items", "-1"));
        assertThat(response, hasStatusCode(400));
    }
}
