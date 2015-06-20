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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.seyren.core.domain.Alert;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.util.config.SeyrenConfig;
import com.seyren.core.util.email.EmailHelper;

public class VelocityEmailHelperTest {
    
    private EmailHelper emailHelper;
    
    @Before
    public void before() {
        emailHelper = new VelocityEmailHelper(new SeyrenConfig());
    }
    
    @Test
    public void bodyContainsRightSortsOfThings() {
        
        Check check = new Check()
                .withId("123")
                .withEnabled(true)
                .withName("test-check")
                .withDescription("Some great description")
                .withWarn(new BigDecimal("2.0"))
                .withError(new BigDecimal("3.0"))
                .withState(AlertType.ERROR);
        Subscription subscription = new Subscription()
                .withEnabled(true)
                .withType(SubscriptionType.EMAIL)
                .withTarget("some@email.com");
        Alert alert = new Alert()
                .withTarget("some.value")
                .withValue(new BigDecimal("4.0"))
                .withTimestamp(new DateTime())
                .withFromType(AlertType.OK)
                .withToType(AlertType.ERROR);
        List<Alert> alerts = Arrays.asList(alert);
        
        String body = emailHelper.createBody(check, subscription, alerts);
        
        assertThat(body, containsString("test-check"));
        assertThat(body, containsString("Some great description"));
        assertThat(body, containsString("some.value"));
        assertThat(body, containsString("2.0"));
        assertThat(body, containsString("3.0"));
        assertThat(body, containsString("4.0"));
        
    }
    
    @Test
    public void descriptionIsNotIncludedIfEmpty() {
        
        Check check = new Check()
                .withId("123")
                .withEnabled(true)
                .withName("test-check")
                .withDescription("")
                .withWarn(new BigDecimal("2.0"))
                .withError(new BigDecimal("3.0"))
                .withState(AlertType.ERROR);
        Subscription subscription = new Subscription()
                .withEnabled(true)
                .withType(SubscriptionType.EMAIL)
                .withTarget("some@email.com");
        Alert alert = new Alert()
                .withTarget("some.value")
                .withValue(new BigDecimal("4.0"))
                .withTimestamp(new DateTime())
                .withFromType(AlertType.OK)
                .withToType(AlertType.ERROR);
        List<Alert> alerts = Arrays.asList(alert);
        
        String body = emailHelper.createBody(check, subscription, alerts);
        
        assertThat(body, not(containsString("<p></p>")));
        
    }
    
    @Test
    public void bodyDoesNotContainScientificNotationOfNumber() {
        
        Check check = new Check()
                .withId("123")
                .withEnabled(true)
                .withName("test-check")
                .withWarn(new BigDecimal("2.0"))
                .withError(new BigDecimal("3.0"))
                .withState(AlertType.ERROR);
        Subscription subscription = new Subscription()
                .withEnabled(true)
                .withType(SubscriptionType.EMAIL)
                .withTarget("some@email.com");
        Alert alert = new Alert()
                .withTarget("some.value")
                .withValue(new BigDecimal("138362880"))
                .withTimestamp(new DateTime())
                .withFromType(AlertType.OK)
                .withToType(AlertType.ERROR);
        List<Alert> alerts = Arrays.asList(alert);
        
        String body = emailHelper.createBody(check, subscription, alerts);
        
        assertThat(body, containsString("138362880"));
        
    }

    @Test
    public void templateLocationShouldBeConfigurable() {
        SeyrenConfig mockConfiguration = mock(SeyrenConfig.class);
        when(mockConfiguration.getEmailTemplateFileName()).thenReturn("test-email-template.vm");
        EmailHelper emailHelper = new VelocityEmailHelper(mockConfiguration);
        String body = emailHelper.createBody(null, null, null);
        assertThat(body, containsString("Test content."));
    }

    @Test
    public void bodyContainsItemsFromModel() {

        Check check = new Check()
                .withId("123")
                .withEnabled(true)
                .withName("test-check")
                .withDescription("Some great description")
                .withWarn(new BigDecimal("2.0"))
                .withError(new BigDecimal("3.0"))
                .withState(AlertType.ERROR);
        Subscription subscription = new Subscription()
                .withEnabled(true)
                .withType(SubscriptionType.EMAIL)
                .withTarget("some@email.com");
        Alert alert = new Alert()
                .withTarget("some.value")
                .withValue(new BigDecimal("4.0"))
                .withTimestamp(new DateTime())
                .withFromType(AlertType.OK)
                .withToType(AlertType.ERROR);
        List<Alert> alerts = Arrays.asList(alert);

        String subject = emailHelper.createSubject(check, subscription, alerts);

        assertThat(subject, is("Seyren alert: test-check"));
    }

    @Test
    public void subjectTemplateLocationShouldBeConfigurable() {
        SeyrenConfig mockConfiguration = mock(SeyrenConfig.class);
        when(mockConfiguration.getEmailSubjectTemplateFileName()).thenReturn("test-email-template.vm");
        EmailHelper emailHelper = new VelocityEmailHelper(mockConfiguration);
        String subject = emailHelper.createSubject(null, null, null);
        assertThat(subject, containsString("Test content."));
    }
}
