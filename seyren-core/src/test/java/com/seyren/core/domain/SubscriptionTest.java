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
    public void testShouldNotify() {
        Subscription sub = new Subscription().withEnabled(true).withFromTime(localTime("1000")).withToTime(localTime("1100")).withSu(true);
        assertThat(sub.shouldNotify(dateTime("1030")), is(equalTo(true)));
    }
    
    @Test
    public void testShouldNotifyAfterTime() {
        Subscription sub = new Subscription().withFromTime(localTime("1000")).withToTime(localTime("1100")).withSu(true);
        assertThat(sub.shouldNotify(dateTime("1200")), is(equalTo(false)));
    }
    
    @Test
    public void testShouldNotifyBeforeTime() {
        Subscription sub = new Subscription().withFromTime(localTime("1000")).withToTime(localTime("1100")).withSu(true);
        assertThat(sub.shouldNotify(dateTime("0900")), is(equalTo(false)));
    }
    
    @Test
    public void testShouldNotifyIncorrectDay() {
        Subscription sub = new Subscription();
        assertThat(sub.shouldNotify(dateTime("1015")), is(equalTo(false)));
    }
    
    @Test
    public void shouldNotNotifyWhenNotEnabled() {
        Subscription sub = new Subscription().withEnabled(false);
        assertThat(sub.shouldNotify(dateTime("1015")), is(equalTo(false)));
    }
    
    private DateTime dateTime(String time) {
        return new DateTime(2012, 01, 01, Integer.valueOf(time.substring(0, 2)), Integer.valueOf(time.substring(2)));
    }
    
    private LocalTime localTime(String time) {
        return new LocalTime(Integer.valueOf(time.substring(0, 2)), Integer.valueOf(time.substring(2)));
    }
    
}
