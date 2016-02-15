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
package com.seyren.core.service.checker;

import com.google.common.base.Optional;
import com.seyren.core.domain.Check;
import com.seyren.core.util.http.HttpUrlFetcher;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by ohad on 7/18/15.
 */
public class HttpTargetCheckerTest {

    private static final String URL = "testUrl";
    private Check check;

    @Before
    public void setUp() throws Exception {
        check = mock(Check.class);
        when(check.getTarget()).thenReturn(URL);
    }

    @Test
    public void testSuccessfulRequest() throws Exception {
        HttpTargetChecker taskChecker = init(200);
        Map<String, Optional<BigDecimal>> result = taskChecker.check(check);
        assertEquals("Wrong result",Optional.of(new BigDecimal(200)),result.get(URL));
    }

    @Test
    public void testErrorJob() throws Exception {
        HttpTargetChecker taskChecker = init(501);
        Map<String, Optional<BigDecimal>> result = taskChecker.check(check);
        assertEquals("Wrong result",Optional.of(new BigDecimal(501)),result.get(URL));
    }

    private HttpTargetChecker init(int responseCode) throws IOException {
        StatusLine statusLine = mock(StatusLine.class);
        when(statusLine.getStatusCode()).thenReturn(responseCode);
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        HttpUrlFetcher urlFetcher = mock(HttpUrlFetcher.class);
        when(urlFetcher.sendUrl(URL)).thenReturn(httpResponse);
        return new HttpTargetChecker(urlFetcher);
    }
}