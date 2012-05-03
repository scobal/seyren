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
