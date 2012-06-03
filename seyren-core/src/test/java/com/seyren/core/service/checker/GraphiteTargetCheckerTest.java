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

import static com.github.restdriver.clientdriver.RestClientDriver.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.List;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.restdriver.clientdriver.ClientDriverRule;
import com.seyren.core.domain.Alert;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.util.config.GraphiteConfig;

public class GraphiteTargetCheckerTest {
    
    @Rule
    public ClientDriverRule clientDriver = new ClientDriverRule();
    
    private GraphiteConfig graphiteConfig;
    private GraphiteTargetChecker checker;
    
    @Before
    public void before() {
        graphiteConfig = new GraphiteConfig(clientDriver.getBaseUrl());
        checker = new GraphiteTargetChecker(graphiteConfig);
    }
    
    @Test
    public void checkHasCorrectTarget() throws Exception {
        String response = "[{\"target\": \"service.error.1MinuteRate\", \"datapoints\": [[0.06, 1337453460]]}]";
        
        clientDriver.addExpectation(
                onRequestTo("/render")
                    .withParam("from", "-11minutes")
                    .withParam("until", "-1minutes")
                    .withParam("uniq", Pattern.compile("[0-9]+"))
                    .withParam("format", "json")
                    .withParam("target", "service.error.1MinuteRate"),
                giveResponse(response));
        
        List<Alert> alerts = checker.check(check());
        
        assertThat(alerts.get(0).getTarget(), is("service.error.1MinuteRate"));
    }
    
    @Test
    public void checkComingBackOkWorks() throws Exception {
        String response = "[{\"target\": \"service.error.1MinuteRate\", \"datapoints\": [[0.06, 1337453460]]}]";
        
        clientDriver.addExpectation(
                onRequestTo("/render")
                    .withParam("from", "-11minutes")
                    .withParam("until", "-1minutes")
                    .withParam("uniq", Pattern.compile("[0-9]+"))
                    .withParam("format", "json")
                    .withParam("target", "service.error.1MinuteRate"),
                giveResponse(response));
        
        List<Alert> alerts = checker.check(check());
        
        assertThat(alerts.get(0).getToType(), is(AlertType.OK));
    }
    
    @Test
    public void checkComingBackWarnWorks() throws Exception {
        String response = "[{\"target\": \"service.error.1MinuteRate\", \"datapoints\": [[0.15, 1337453460]]}]";
        
        clientDriver.addExpectation(
                onRequestTo("/render")
                    .withParam("from", "-11minutes")
                    .withParam("until", "-1minutes")
                    .withParam("uniq", Pattern.compile("[0-9]+"))
                    .withParam("format", "json")
                    .withParam("target", "service.error.1MinuteRate"),
                giveResponse(response));
        
        List<Alert> alerts = checker.check(check());
        
        assertThat(alerts.get(0).getToType(), is(AlertType.WARN));
    }
    
    @Test
    public void checkComingBackErrorWorks() throws Exception {
        String response = "[{\"target\": \"service.error.1MinuteRate\", \"datapoints\": [[0.20, 1337453460]]}]";
        
        clientDriver.addExpectation(
                onRequestTo("/render")
                    .withParam("from", "-11minutes")
                    .withParam("until", "-1minutes")
                    .withParam("uniq", Pattern.compile("[0-9]+"))
                    .withParam("format", "json")
                    .withParam("target", "service.error.1MinuteRate"),
                giveResponse(response));
        
        List<Alert> alerts = checker.check(check());
        
        assertThat(alerts.get(0).getToType(), is(AlertType.ERROR));
    }
    
    @Test
    public void checkGoesThroughDatapointsInReverserOrderToDetermineState() throws Exception {
        String response = "[{\"target\": \"service.error.1MinuteRate\", \"datapoints\": [[0.20, 1337453460],[0.01, 1337453463]]}]";
        
        clientDriver.addExpectation(
                onRequestTo("/render")
                    .withParam("from", "-11minutes")
                    .withParam("until", "-1minutes")
                    .withParam("uniq", Pattern.compile("[0-9]+"))
                    .withParam("format", "json")
                    .withParam("target", "service.error.1MinuteRate"),
                giveResponse(response));
        
        List<Alert> alerts = checker.check(check());
        
        assertThat(alerts.get(0).getToType(), is(AlertType.OK));
    }
    
    @Test
    public void checkSkipsNullValuesToDetermineCurrentState() throws Exception {
        String response = "[{\"target\": \"service.error.1MinuteRate\", \"datapoints\": [[0.17, 1337453460],[null, 1337453463]]}]";
        
        clientDriver.addExpectation(
                onRequestTo("/render")
                    .withParam("from", "-11minutes")
                    .withParam("until", "-1minutes")
                    .withParam("uniq", Pattern.compile("[0-9]+"))
                    .withParam("format", "json")
                    .withParam("target", "service.error.1MinuteRate"),
                giveResponse(response));
        
        List<Alert> alerts = checker.check(check());
        
        assertThat(alerts.get(0).getToType(), is(AlertType.WARN));
    }
    
    @Test
    public void checkReturningMultipleMetricsCreatesAlertForEach() throws Exception {
        String response = "[" +
        		    "{\"target\": \"service.error.1MinuteRate\", \"datapoints\": [[0.20, 1337453460],[0.01, 1337453463]]}," +
        		    "{\"target\": \"service.warn.1MinuteRate\", \"datapoints\": [[0.56, 1337453460],[0.78, 1337453463]]}" +
        		"]";
        
        clientDriver.addExpectation(
                onRequestTo("/render")
                    .withParam("from", "-11minutes")
                    .withParam("until", "-1minutes")
                    .withParam("uniq", Pattern.compile("[0-9]+"))
                    .withParam("format", "json")
                    .withParam("target", "service.*.1MinuteRate"),
                giveResponse(response));
        
        List<Alert> alerts = checker.check(check("service.*.1MinuteRate"));
        
        assertThat(alerts, hasSize(2));
        assertThat(alerts.get(0).getTarget(), is("service.error.1MinuteRate"));
        assertThat(alerts.get(0).getToType(), is(AlertType.OK));
        assertThat(alerts.get(1).getTarget(), is("service.warn.1MinuteRate"));
        assertThat(alerts.get(1).getToType(), is(AlertType.ERROR));
    }
    
    private Check check() {
        return check("service.error.1MinuteRate");
    }
    
    private Check check(String target) {
        return new Check()
            .withId("id")
            .withTarget(target)
            .withWarn(0.15)
            .withError(0.20);
    }
    
}
