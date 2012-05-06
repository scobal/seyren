package com.seyren.core.domain;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.junit.Test;

public class SubscriptionTest {

	@Test
	public void testShouldNotify() {
		Subscription sub = new Subscription().withFromTime(localTime("1000")).withToTime(localTime("1100")).withSu(true);
		Alert alert = new Alert().withTimestamp(dateTime("1030"));
		assertThat(sub.shouldNotify(alert), is(equalTo(true)));
	}
	
	@Test
	public void testShouldNotifyAfterTime() {
		Subscription sub = new Subscription().withFromTime(localTime("1000")).withToTime(localTime("1100")).withSu(true);
		Alert alert = new Alert().withTimestamp(dateTime("1200"));
		assertThat(sub.shouldNotify(alert), is(equalTo(false)));
	}
	
	@Test
	public void testShouldNotifyBeforeTime() {
		Subscription sub = new Subscription().withFromTime(localTime("1000")).withToTime(localTime("1100")).withSu(true);
		Alert alert = new Alert().withTimestamp(dateTime("0900"));
		assertThat(sub.shouldNotify(alert), is(equalTo(false)));
	}
	
	@Test
	public void testShouldNotifyIncorrectDay() {
		Subscription sub = new Subscription();
		Alert alert = new Alert().withTimestamp(dateTime("1015"));
		assertThat(sub.shouldNotify(alert), is(equalTo(false)));
	}

	private DateTime dateTime(String time) {
		return new DateTime(2012, 01, 01, Integer.valueOf(time.substring(0,2)), Integer.valueOf(time.substring(2)));
	}

	private LocalTime localTime(String time) {
		return new LocalTime(Integer.valueOf(time.substring(0,2)), Integer.valueOf(time.substring(2)));
	}
	
}
