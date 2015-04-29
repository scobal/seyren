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
package com.seyren.core.util.velocity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seyren.core.domain.*;
import com.seyren.core.util.config.SeyrenConfig;
import com.seyren.core.util.http.HttpHelper;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static com.github.restdriver.Matchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Represents tests for {@link com.seyren.core.util.velocity.VelocityHttpHelper}.
 *
 * @author <a href="mailto:tobias.lindenmann@1und1.de">Tobias Lindenmann</a>
 */
public class VelocityHttpHelperTest {

    private HttpHelper httpHelper;

    @Before
    public void before() {
        this.httpHelper = new VelocityHttpHelper(new SeyrenConfig());
    }


    @Test
    public void bodyContainsRightSortsOfThings() throws IOException {

        Check check = new Check()
                .withId("123")
                .withEnabled(true)
                .withName("test-check")
                .withDescription("Some great description")
                .withWarn(new BigDecimal("2.0"))
                .withError(new BigDecimal("3.0"))
                .withState(AlertType.ERROR)
                .withPriority(PriorityType.TRIVIAL)
                .withTarget("the.test-target");
        Subscription subscription = new Subscription()
                .withEnabled(true)
                .withType(SubscriptionType.HTTP)
                .withTarget("some@email.com");
        Alert alert = new Alert()
                .withTarget("some.value")
                .withValue(new BigDecimal("4.0"))
                .withError(new BigDecimal("2.0"))
                .withWarn(new BigDecimal("1.0"))
                .withTimestamp(new DateTime())
                .withFromType(AlertType.OK)
                .withToType(AlertType.ERROR);
        Alert alert2 = new Alert()
                .withTarget("some.value")
                .withValue(new BigDecimal("4.0"))
                .withError(new BigDecimal("2.0"))
                .withWarn(new BigDecimal("1.0"))
                .withTimestamp(new DateTime())
                .withFromType(AlertType.OK)
                .withToType(AlertType.ERROR);

        List<Alert> alerts = Arrays.asList(alert, alert2);

        String body = httpHelper.createHttpContent(check, subscription, alerts);
        ObjectMapper mapper = new ObjectMapper();
        assertThat(body, notNullValue());
        JsonNode node = mapper.readTree(body);

        assertThat(node, hasJsonPath("$.seyrenUrl", is("http://localhost:8080/seyren")));
        assertThat(node, hasJsonPath("$.check.name", is("test-check")));
        assertThat(node, hasJsonPath("$.check.state", is("ERROR")));
        assertThat(node, hasJsonPath("$.alerts", hasSize(2)));
        assertThat(node, hasJsonPath("$.alerts[0].target", is(alert.getTarget())));
        assertThat(node, hasJsonPath("$.alerts[0].value", is(4.0)));
        assertThat(node, hasJsonPath("$.alerts[0].warn", is(1.0)));
        assertThat(node, hasJsonPath("$.alerts[0].error", is(2.0)));
        assertThat(node, hasJsonPath("$.alerts[0].fromType", is("OK")));
        assertThat(node, hasJsonPath("$.alerts[0].toType", is("ERROR")));
        assertThat(node, hasJsonPath("$.alerts[1].target", is(alert2.getTarget())));
        assertThat(node, hasJsonPath("$.alerts[1].value", is(4.0)));
        assertThat(node, hasJsonPath("$.alerts[1].warn", is(1.0)));
        assertThat(node, hasJsonPath("$.alerts[1].error", is(2.0)));
        assertThat(node, hasJsonPath("$.alerts[1].fromType", is("OK")));
        assertThat(node, hasJsonPath("$.alerts[1].toType", is("ERROR")));
        assertThat(node, hasJsonPath("$.preview", Matchers.startsWith("<br />")));
        assertThat(node, hasJsonPath("$.preview", containsString(check.getTarget())));
    }

    @Test
    public void templateLocationShouldBeConfigurable() {
        Check check = new Check()
                .withId("123")
                .withEnabled(true)
                .withName("test-check")
                .withWarn(new BigDecimal("2.0"))
                .withError(new BigDecimal("3.0"))
                .withState(AlertType.ERROR);

        SeyrenConfig mockConfiguration = mock(SeyrenConfig.class);
        when(mockConfiguration.getHttpTemplateFileName()).thenReturn("test-http-template.vm");
        HttpHelper httpHelper = new VelocityHttpHelper(mockConfiguration);
        String body = httpHelper.createHttpContent(check, null, null);
        assertThat(body, containsString("\"ping\": \"pong\""));
    }
}
