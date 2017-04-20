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

public class SubscriptionFiringTests extends AbstractCheckRunTest{
	/** The values to be used for the target checker */
	ArrayList<BigDecimal> values = new ArrayList<BigDecimal>();
	
	@Override
	protected Check getCheck() {
		return this.getDefaultCheck();
	}
	
	protected Alert getDefaultErrorAlert(){
		Alert alert = this.getDefaultAlert();
		alert.setToType(AlertType.ERROR);
		return alert;
	}
	
	protected Alert getDefaultWarnAlert(){
		Alert alert = this.getDefaultAlert();
		alert.setToType(AlertType.WARN);
		return alert;
	}
	
	protected Alert getDefaultOKAlert(){
		Alert alert = this.getDefaultAlert();
		alert.setToType(AlertType.OK);
		return alert;
	}

	@Override
	protected Alert getPreviousAlert() {
		return previousAlert;
	}

	@Override
	protected Subscription getSubscription() {
		return this.getDefaultSubscription();
	}

	@Override
	protected List<BigDecimal> getValues() {
		return values;
	}
	
	protected void setOKValues(){
		values.clear();
		values.add(new BigDecimal(50.0));
	}
	
	protected void setWarnValues(){
		values.clear();
		values.add(new BigDecimal(85.0));
	}
	
	protected void setErrorValues(){
		values.clear();
		values.add(new BigDecimal(100.0));
	}

	@Test
	public void testNewOKAlert(){
		setOKValues();
		initialize();
		runner.run();
		assertTrue(!((MockSubscription)subscription).notificationSent());
	}
	
	@Test
	public void testNewWarnAlert(){
		setWarnValues();
		initialize();
		runner.run();
		assertTrue(((MockSubscription)subscription).notificationSent());
	}
	
	@Test
	public void testNewErrorAlert(){
		setErrorValues();
		initialize();
		runner.run();
		assertTrue(((MockSubscription)subscription).notificationSent());
	}
	
	@Test
	public void testOKAfterOKAlert(){
		setOKValues();
		this.previousAlert = this.getDefaultOKAlert();
		initialize();
		runner.run();
		assertTrue(!((MockSubscription)subscription).notificationSent());
	}
	
	@Test
	public void testWarnAfterOKAlert(){
		setWarnValues();
		this.previousAlert = this.getDefaultOKAlert();
		initialize();
		runner.run();
		assertTrue(((MockSubscription)subscription).notificationSent());
	}
	
	@Test
	public void testErrorAfterOKAlert(){
		setErrorValues();
		this.previousAlert = this.getDefaultOKAlert();
		initialize();
		runner.run();
		assertTrue(((MockSubscription)subscription).notificationSent());
	}
	
	@Test
	public void testOKAfterWarnAlert(){
		setOKValues();
		this.previousAlert = this.getDefaultWarnAlert();
		initialize();
		runner.run();
		assertTrue(((MockSubscription)subscription).notificationSent());
	}
	
	@Test
	public void testWarnAfterWarnAlert(){
		setWarnValues();
		this.previousAlert = this.getDefaultWarnAlert();
		initialize();
		runner.run();
		assertTrue(!((MockSubscription)subscription).notificationSent());
	}
	
	@Test
	public void testErrorAfterWarnAlert(){
		setErrorValues();
		this.previousAlert = this.getDefaultWarnAlert();
		initialize();
		runner.run();
		assertTrue(((MockSubscription)subscription).notificationSent());
	}
	
	@Test
	public void testOKAfterErrorAlert(){
		setOKValues();
		this.previousAlert = this.getDefaultErrorAlert();
		initialize();
		runner.run();
		assertTrue(((MockSubscription)subscription).notificationSent());
	}
	
	@Test
	public void testWarnAfterErrorAlert(){
		setWarnValues();
		this.previousAlert = this.getDefaultErrorAlert();
		initialize();
		runner.run();
		assertTrue(((MockSubscription)subscription).notificationSent());
	}
	
	@Test
	public void testErrorAfterErrorAlert(){
		setErrorValues();
		this.previousAlert = this.getDefaultErrorAlert();
		initialize();
		runner.run();
		assertTrue(!((MockSubscription)subscription).notificationSent());
	}
}