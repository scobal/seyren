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

import java.math.BigDecimal;

import org.junit.Test;

public class CheckTest {

	@Test
	public void testIsBeyondWarnThresholdWhenHigherIsBad() {
		Check check = new Check().withWarn(bigdecimal(10)).withError(bigdecimal(20));
		assertThat(check.isBeyondWarnThreshold(bigdecimal(9)), is(equalTo(false)));
		assertThat(check.isBeyondWarnThreshold(bigdecimal(10)), is(equalTo(true)));
		assertThat(check.isBeyondWarnThreshold(bigdecimal(11)), is(equalTo(true)));
	}
	
	@Test
	public void testIsBeyondWarnThresholdWhenLowerIsBad() {
		Check check = new Check().withWarn(bigdecimal(10)).withError(bigdecimal(0));
		assertThat(check.isBeyondWarnThreshold(bigdecimal(9)), is(equalTo(true)));
		assertThat(check.isBeyondWarnThreshold(bigdecimal(10)), is(equalTo(true)));
		assertThat(check.isBeyondWarnThreshold(bigdecimal(11)), is(equalTo(false)));
	}
	
	@Test
	public void testIsBeyondErrorThresholdWhenHigherIsBad() {
		Check check = new Check().withWarn(bigdecimal(10)).withError(bigdecimal(20));
		assertThat(check.isBeyondErrorThreshold(bigdecimal(19)), is(equalTo(false)));
		assertThat(check.isBeyondErrorThreshold(bigdecimal(20)), is(equalTo(true)));
		assertThat(check.isBeyondErrorThreshold(bigdecimal(21)), is(equalTo(true)));
	}
	
	@Test
	public void testIsBeyondErrorThresholdWhenLowerIsBad() {
		Check check = new Check().withWarn(bigdecimal(10)).withError(bigdecimal(5));
		assertThat(check.isBeyondErrorThreshold(bigdecimal(4)), is(equalTo(true)));
		assertThat(check.isBeyondErrorThreshold(bigdecimal(5)), is(equalTo(true)));
		assertThat(check.isBeyondErrorThreshold(bigdecimal(6)), is(equalTo(false)));
	}
	
	private BigDecimal bigdecimal(int i) {
		return new BigDecimal(i);
	}
	
}
