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
package com.seyren.core.domain;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.junit.Test;

public class SubscriptionTest {
    
    @Test
    public void subscriptionShouldNotifyWhenTimeAndDateAreGood() {
        Subscription sub = new Subscription().withEnabled(true).withFromTime(localTime("1000")).withToTime(localTime("1100")).withSu(true);
        assertThat(sub.shouldNotify(dateTime("1030"), AlertType.ERROR), is(true));
    }
    
    @Test
    public void subscriptionShouldNotNotifyAfterTime() {
        Subscription sub = new Subscription().withEnabled(true).withFromTime(localTime("1000")).withToTime(localTime("1100")).withSu(true);
        assertThat(sub.shouldNotify(dateTime("1200"), AlertType.ERROR), is(false));
    }
    
    @Test
    public void subscriptionShouldNotNotifyBeforeTime() {
        Subscription sub = new Subscription().withEnabled(true).withFromTime(localTime("1000")).withToTime(localTime("1100")).withSu(true);
        assertThat(sub.shouldNotify(dateTime("0900"), AlertType.ERROR), is(false));
    }
    
    @Test
    public void subscriptionShouldNotNotifyOnTheWrongDay() {
        Subscription sub = new Subscription().withEnabled(true);
        assertThat(sub.shouldNotify(dateTime("1015"), AlertType.ERROR), is(false));
    }
    
    @Test
    public void subscriptionShouldNotNotifyWhenNotEnabled() {
        Subscription sub = new Subscription().withEnabled(false);
        assertThat(sub.shouldNotify(dateTime("1015"), AlertType.ERROR), is(false));
    }
    
    @Test
    public void subscriptionShouldNotifyOfErrorWhenNotIgnoringErrorOrWarning() {
        Subscription sub = new Subscription()
                .withEnabled(true)
                .withFromTime(localTime("1000"))
                .withToTime(localTime("1100"))
                .withSu(true)
                .withIgnoreError(false)
                .withIgnoreWarn(false)
                .withIgnoreOk(false);
        assertThat(sub.shouldNotify(dateTime("1015"), AlertType.ERROR), is(true));
    }
    
    @Test
    public void subscriptionShouldNotifyOfWarningWhenNotIgnoringErrorOrWarning() {
        Subscription sub = new Subscription()
                .withEnabled(true)
                .withFromTime(localTime("1000"))
                .withToTime(localTime("1100"))
                .withSu(true)
                .withIgnoreError(false)
                .withIgnoreWarn(false)
                .withIgnoreOk(false);
        assertThat(sub.shouldNotify(dateTime("1015"), AlertType.WARN), is(true));
    }
    
    @Test
    public void subscriptionShouldNotNotifyOfErrorWhenIgnoringError() {
        Subscription sub = new Subscription()
                .withEnabled(true)
                .withFromTime(localTime("1000"))
                .withToTime(localTime("1100"))
                .withSu(true)
                .withIgnoreError(true)
                .withIgnoreWarn(false)
                .withIgnoreOk(false);
        assertThat(sub.shouldNotify(dateTime("1015"), AlertType.ERROR), is(false));
    }
    
    @Test
    public void subscriptionShouldNotNotifyOfWarningWhenIgnoringWarning() {
        Subscription sub = new Subscription()
                .withEnabled(true)
                .withFromTime(localTime("1000"))
                .withToTime(localTime("1100"))
                .withSu(true)
                .withIgnoreError(false)
                .withIgnoreWarn(true)
                .withIgnoreOk(false);
        assertThat(sub.shouldNotify(dateTime("1015"), AlertType.WARN), is(false));
    }
    
    @Test
    public void subscriptionShouldNotNotifyOfOkWhenIgnoringOk() {
        Subscription sub = new Subscription()
                .withEnabled(true)
                .withFromTime(localTime("1000"))
                .withToTime(localTime("1100"))
                .withSu(true)
                .withIgnoreWarn(false)
                .withIgnoreError(false)
                .withIgnoreOk(true);
        assertThat(sub.shouldNotify(dateTime("1015"), AlertType.OK), is(false));
    }
    
    @Test
    public void subscriptionShouldNotifyOfOkWhenNotIgnoringOk() {
        Subscription sub = new Subscription()
                .withEnabled(true)
                .withFromTime(localTime("1000"))
                .withToTime(localTime("1100"))
                .withSu(true)
                .withIgnoreWarn(false)
                .withIgnoreError(false)
                .withIgnoreOk(false);
        assertThat(sub.shouldNotify(dateTime("1015"), AlertType.OK), is(true));
    }
    
    private DateTime dateTime(String time) {
        return new DateTime(2012, 01, 01, Integer.valueOf(time.substring(0, 2)), Integer.valueOf(time.substring(2)));
    }
    
    private LocalTime localTime(String time) {
        return new LocalTime(Integer.valueOf(time.substring(0, 2)), Integer.valueOf(time.substring(2)));
    }
    
}
