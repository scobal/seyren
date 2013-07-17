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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

import com.github.restdriver.serverdriver.http.response.Response;

public class ConfigAT {
    
    @Test
    public void getAcceptanceTestSeyrenConfig() {
        Response response = get(config());
        assertThat(response, hasStatusCode(200));
        
        // Base
        assertThat(response.asJson().size(), is(6));
        assertThat(response.asJson(), hasJsonPath("$.baseUrl", is("http://localhost:8080/seyren")));

        // Graphite
        assertThat(response.asJson(), hasJsonPath("$.graphiteUrl", is("http://localhost:80")));
        assertThat(response.asJson(), hasJsonPath("$.graphiteScheme", is("http")));
        assertThat(response.asJson(), hasJsonPath("$.graphiteHost", is("localhost:80")));
        assertThat(response.asJson(), hasJsonPath("$.graphitePath", is("")));
        assertThat(response.asJson(), hasJsonPath("$.graphiteSSLPort", is(80)));
    }

}
