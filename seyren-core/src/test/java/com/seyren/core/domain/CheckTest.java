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

import org.junit.Test;

public class CheckTest {

	@Test
	public void testIsBeyondWarnThresholdWhenHigherIsBad() {
		Check check = new Check().withWarn(10d).withError(20d);
		assertThat(check.isBeyondWarnThreshold(9d), is(equalTo(false)));
		assertThat(check.isBeyondWarnThreshold(10d), is(equalTo(true)));
		assertThat(check.isBeyondWarnThreshold(11d), is(equalTo(true)));
	}
	
	@Test
	public void testIsBeyondWarnThresholdWhenLowerIsBad() {
		Check check = new Check().withWarn(10d).withError(0d);
		assertThat(check.isBeyondWarnThreshold(9d), is(equalTo(true)));
		assertThat(check.isBeyondWarnThreshold(10d), is(equalTo(true)));
		assertThat(check.isBeyondWarnThreshold(11d), is(equalTo(false)));
	}
	
	@Test
	public void testIsBeyondErrorThresholdWhenHigherIsBad() {
		Check check = new Check().withWarn(10d).withError(20d);
		assertThat(check.isBeyondErrorThreshold(19d), is(equalTo(false)));
		assertThat(check.isBeyondErrorThreshold(20d), is(equalTo(true)));
		assertThat(check.isBeyondErrorThreshold(21d), is(equalTo(true)));
	}
	
	@Test
	public void testIsBeyondErrorThresholdWhenLowerIsBad() {
		Check check = new Check().withWarn(10d).withError(5d);
		assertThat(check.isBeyondErrorThreshold(4d), is(equalTo(true)));
		assertThat(check.isBeyondErrorThreshold(5d), is(equalTo(true)));
		assertThat(check.isBeyondErrorThreshold(6d), is(equalTo(false)));
	}
	
}
