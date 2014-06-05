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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.restdriver.clientdriver.ClientDriverRule;
import com.google.common.base.Optional;
import com.seyren.core.domain.Check;
import com.seyren.core.util.graphite.GraphiteHttpClient;
import com.seyren.core.util.graphite.GraphiteReadException;

public class GraphiteTargetCheckerTest {
    
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    @Rule
    public ClientDriverRule clientDriver = new ClientDriverRule();
    
    private GraphiteHttpClient mockGraphiteHttpClient;
    private GraphiteTargetChecker checker;
    
    @Before
    public void before() {
        mockGraphiteHttpClient = mock(GraphiteHttpClient.class);
        checker = new GraphiteTargetChecker(mockGraphiteHttpClient);
    }
    
    @After
    public void after() {
        System.clearProperty("GRAPHITE_URL");
    }
    
    @Test
    public void singleValidTargetIsPresent() throws Exception {
        JsonNode node = MAPPER.readTree("[{\"target\": \"service.error.1MinuteRate\", \"datapoints\": [[0.06, 1337453460]]}]");
        
        when(mockGraphiteHttpClient.getTargetJson("service.error.1MinuteRate", null, null)).thenReturn(node);
        
        Map<String, Optional<BigDecimal>> values = checker.check(check());
        
        assertThat(values, hasKey("service.error.1MinuteRate"));
    }
    
    @Test
    public void singleValidTargetHasCorrectValue() throws Exception {
        JsonNode node = MAPPER.readTree("[{\"target\": \"service.error.1MinuteRate\", \"datapoints\": [[0.06, 1337453460]]}]");
        
        when(mockGraphiteHttpClient.getTargetJson("service.error.1MinuteRate", null, null)).thenReturn(node);
        
        Map<String, Optional<BigDecimal>> values = checker.check(check());
        
        assertThat(values.get("service.error.1MinuteRate").isPresent(), is(true));
        assertThat(values.get("service.error.1MinuteRate").get(), is(new BigDecimal("0.06")));
    }
    
    @Test
    public void valueIsDeterminedByGoingThroughDatapointsInReverserOrder() throws Exception {
        JsonNode node = MAPPER.readTree("[{\"target\": \"service.error.1MinuteRate\", \"datapoints\": [[0.20, 1337453460],[0.01, 1337453463]]}]");
        
        when(mockGraphiteHttpClient.getTargetJson("service.error.1MinuteRate", null, null)).thenReturn(node);
        
        Map<String, Optional<BigDecimal>> values = checker.check(check());
        
        assertThat(values.get("service.error.1MinuteRate").get(), is(new BigDecimal("0.01")));
    }
    
    @Test
    public void valueIsDeterminedBySkippingNullValues() throws Exception {
        JsonNode node = MAPPER.readTree("[{\"target\": \"service.error.1MinuteRate\", \"datapoints\": [[0.17, 1337453460],[null, 1337453463]]}]");
        
        when(mockGraphiteHttpClient.getTargetJson("service.error.1MinuteRate", null, null)).thenReturn(node);
        
        Map<String, Optional<BigDecimal>> values = checker.check(check());
        
        assertThat(values.get("service.error.1MinuteRate").get(), is(new BigDecimal("0.17")));
    }
    
    @Test
    public void targetWhichOnlyHasNullValuesIsAbsent() throws Exception {
        JsonNode node = MAPPER.readTree("[{\"target\": \"service.error.1MinuteRate\", \"datapoints\": [[null, 1337453460],[null, 1337453463]]}]");
        
        when(mockGraphiteHttpClient.getTargetJson("service.error.1MinuteRate", null, null)).thenReturn(node);
        
        Map<String, Optional<BigDecimal>> values = checker.check(check());
        
        assertThat(values.get("service.error.1MinuteRate").isPresent(), is(false));
    }
    
    @Test
    public void multipleTargetsAreHandledCorrectly() throws Exception {
        JsonNode node = MAPPER.readTree("[" +
                "{\"target\": \"service.error.1MinuteRate\", \"datapoints\": [[0.20, 1337453460],[0.01, 1337453463]]}," +
                "{\"target\": \"service.warn.1MinuteRate\", \"datapoints\": [[0.56, 1337453460],[0.78, 1337453463]]}" +
                "]");
        
        when(mockGraphiteHttpClient.getTargetJson("service.*.1MinuteRate", null, null)).thenReturn(node);
        
        Map<String, Optional<BigDecimal>> values = checker.check(checkWithTarget("service.*.1MinuteRate"));
        
        assertThat(values.entrySet(), hasSize(2));
        assertThat(values.get("service.error.1MinuteRate").get(), is(new BigDecimal("0.01")));
        assertThat(values.get("service.warn.1MinuteRate").get(), is(new BigDecimal("0.78")));
    }
    
    @Test
    public void exceptionGettingDataFromGraphiteIsHandled() throws Exception {
        when(mockGraphiteHttpClient.getTargetJson("service.*.1MinuteRate", null, null)).thenThrow(new GraphiteReadException("Graphite bad times", new RuntimeException("Bad times")));
        
        Map<String, Optional<BigDecimal>> values = checker.check(checkWithTarget("service.*.1MinuteRate"));
        
        assertThat(values.size(), is(0));
    }
    
    private Check check() {
        return checkWithTarget("service.error.1MinuteRate");
    }
    
    private Check checkWithTarget(String target) {
        return new Check()
                .withId("id")
                .withTarget(target)
                .withWarn(new BigDecimal("0.15"))
                .withError(new BigDecimal("0.20"));
    }
    
}
