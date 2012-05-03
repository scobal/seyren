package com.seyren.core.domain;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

public class CheckTest {

	@Test
	public void testIsBeyondWarnThresholdWhenHigherIsBad() {
		Check check = new Check().withWarn("10").withError("20");
		assertThat(check.isBeyondWarnThreshold(9f), is(equalTo(false)));
		assertThat(check.isBeyondWarnThreshold(10f), is(equalTo(true)));
		assertThat(check.isBeyondWarnThreshold(11f), is(equalTo(true)));
	}
	
	@Test
	public void testIsBeyondWarnThresholdWhenLowerIsBad() {
		Check check = new Check().withWarn("10").withError("0");
		assertThat(check.isBeyondWarnThreshold(9f), is(equalTo(true)));
		assertThat(check.isBeyondWarnThreshold(10f), is(equalTo(true)));
		assertThat(check.isBeyondWarnThreshold(11f), is(equalTo(false)));
	}
	
	@Test
	public void testIsBeyondErrorThresholdWhenHigherIsBad() {
		Check check = new Check().withWarn("10").withError("20");
		assertThat(check.isBeyondErrorThreshold(19f), is(equalTo(false)));
		assertThat(check.isBeyondErrorThreshold(20f), is(equalTo(true)));
		assertThat(check.isBeyondErrorThreshold(21f), is(equalTo(true)));
	}
	
	@Test
	public void testIsBeyondErrorThresholdWhenLowerIsBad() {
		Check check = new Check().withWarn("10").withError("5");
		assertThat(check.isBeyondErrorThreshold(4f), is(equalTo(true)));
		assertThat(check.isBeyondErrorThreshold(5f), is(equalTo(true)));
		assertThat(check.isBeyondErrorThreshold(6f), is(equalTo(false)));
	}
	
}
