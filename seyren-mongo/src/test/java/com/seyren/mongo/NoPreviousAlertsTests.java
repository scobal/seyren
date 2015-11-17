package com.seyren.mongo;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.junit.Test;

import com.seyren.core.domain.Alert;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;

public class NoPreviousAlertsTests extends AbstractCheckRunTest{

	ArrayList<BigDecimal> values = new ArrayList<BigDecimal>();
	
	@Override
	protected Check getCheck() {
		Check check = new Check();
	    check.setId("");
	    check.setName("");
	    check.setDescription("");
	    check.setTarget("");
	    check.setFrom("");
	    check.setUntil("");
	    check.setGraphiteBaseUrl("");
	    check.setWarn(new BigDecimal(80.0));
	    check.setError(new BigDecimal(90.0));
	    check.setEnabled(true);
	    check.setLive(false);
	    check.setAllowNoData(false);
	    check.setState(AlertType.UNKNOWN);
	    check.setLastCheck(new DateTime());
		return check;
	}

	@Override
	protected Alert getPreviousAlert() {
		return null;
	}

	@Override
	protected Subscription getSubscription() {
		Subscription subscription = new MockSubscription();
		subscription.setEnabled(true);
		subscription.setIgnoreError(false);
		subscription.setIgnoreWarn(false);
		subscription.setIgnoreOk(true);
		subscription.setMo(true);
		subscription.setTu(true);
		subscription.setWe(true);
		subscription.setTh(true);
		subscription.setFr(true);
		subscription.setSa(true);
		subscription.setSu(true);
		subscription.setFromTime(new LocalTime("0:00"));
		subscription.setToTime(new LocalTime("23:59"));
		return subscription;
	}

	@Override
	protected List<BigDecimal> getValues() {
		return values;
	}
	
	private void setOKValues(){
		values.clear();
		values.add(new BigDecimal(50.0));
	}
	
	private void setWarnValues(){
		values.clear();
		values.add(new BigDecimal(85.0));
	}
	
	private void setErrorValues(){
		values.clear();
		values.add(new BigDecimal(100.0));
	}

	@Test
	public void testNewOKSubscription(){
		setOKValues();
		initialize();
		runner.run();
		assertTrue(!((MockSubscription)subscription).notificationSent());
	}
	
	@Test
	public void testNewWarnSubscription(){
		setWarnValues();
		initialize();
		runner.run();
		assertTrue(((MockSubscription)subscription).notificationSent());
	}
	
	@Test
	public void testNewErrorSubscription(){
		setErrorValues();
		initialize();
		runner.run();
		assertTrue(((MockSubscription)subscription).notificationSent());
	}
	

}
