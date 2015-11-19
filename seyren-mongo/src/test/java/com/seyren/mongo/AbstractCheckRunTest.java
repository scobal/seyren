package com.seyren.mongo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.junit.After;
import org.junit.Before;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.mongodb.DB;
import com.mongodb.Mongo;

import com.seyren.core.domain.Alert;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.service.checker.DefaultValueChecker;
import com.seyren.core.service.notification.NotificationService;
import com.seyren.core.service.schedule.CheckRunner;
import com.seyren.core.util.config.SeyrenConfig;
import com.seyren.mongo.MongoStore;

public abstract class AbstractCheckRunTest {
	
	@Mock protected Mongo mongo;
	
	@Mock protected DB db;
	
	@Mock protected MongoStore mongoStore;
	
	@Mock protected SeyrenConfig config;
	
	protected CheckRunner runner;
	
	protected Check check;
	
	protected NotificationService notificationService;

	protected Subscription subscription;
	
	protected Alert currentAlert;
	
	protected Alert previousAlert;
	
	protected abstract Check getCheck();
	
	protected abstract Alert getPreviousAlert();
	
	protected abstract Subscription getSubscription();
	
	protected abstract List<BigDecimal> getValues();
	
	
	@Before
	public void setUp() throws Exception {
		
		config = Mockito.mock(SeyrenConfig.class);

		Mockito.when(config.getMongoUrl()).thenAnswer(new Answer<String>() {
		     public String answer(InvocationOnMock invocation) throws Throwable {
		    	 System.out.println("Getting URL of Mongo DB");
		         return "mongodb://somedomain.com:12345/seyren";
		     }
		});

		DB db = Mockito.mock(DB.class);
		
		mongoStore = Mockito.mock(MongoStore.class);
		mongoStore.setConfig(config);
		
		this.subscription = this.getSubscription();
		this.check = new MockCheck(this.getCheck(), this.subscription);
		
		Mockito.doAnswer(new Answer<Check>(){ 
			public Check answer(InvocationOnMock invocation) throws Throwable {
				return null;
			}})
	    .when(mongoStore).updateStateAndLastCheck(
	          Mockito.any(String.class), Mockito.any(AlertType.class), Mockito.any(DateTime.class));
		
		Mockito.when(mongoStore.updateStateAndLastCheck(Mockito.any(String.class), Mockito.any(AlertType.class), Mockito.any(DateTime.class)))
			.thenReturn(this.check);
		
		Mockito.when(mongoStore.createAlert(Mockito.anyString(),Mockito.any(Alert.class)))
			.thenReturn(new Alert());
		
		Mockito.when(mongoStore.getLastAlertForTargetOfCheck(Mockito.anyString(),Mockito.anyString()))
			.thenReturn(this.getPreviousAlert());
		
	}


	@After
	public void tearDown() throws Exception {
		
	}
	
	protected Check getDefaultCheck(){
		Check check = new Check();
	    check.setId("Check001");
	    check.setName("Testing Check");
	    check.setDescription("A check used simply for unit testing");
	    check.setTarget("com.launch.check.machine1.target1");
	    check.setFrom("");
	    check.setUntil("");
	    check.setGraphiteBaseUrl("http://mygraphite.launch.com:2003");
	    check.setWarn(new BigDecimal(80.0));
	    check.setError(new BigDecimal(90.0));
	    check.setEnabled(true);
	    check.setLive(false);
	    check.setAllowNoData(false);
	    check.setState(AlertType.UNKNOWN);
	    check.setLastCheck(new DateTime());
		return check;
	}
	
	protected Alert getDefaultAlert(){
		Alert alert = new Alert();
		alert.setId("Alert001");
		alert.setCheckId("Check001");
		alert.setTarget("com.launch.check.machine1.target1");
		alert.setWarn(new BigDecimal(80.0));
		alert.setError(new BigDecimal(90.0));
		alert.setTimestamp(new DateTime());
		return alert;
	}
	
	protected Subscription getDefaultSubscription(){
		Subscription subscription = new MockSubscription();
		subscription.setEnabled(true);
		subscription.setIgnoreError(false);
		subscription.setIgnoreWarn(false);
		subscription.setIgnoreOk(false);
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
	
	protected void initialize(){
		MockTargetChecker checker = new MockTargetChecker();
		checker.setValues(this.getValues());
		((MockSubscription)subscription).reset();
		this.notificationService = new MockNotificationService();
		List<NotificationService> notificationServices = new ArrayList<NotificationService>();
		notificationServices.add(this.notificationService);
		runner = new CheckRunner(this.check, mongoStore, mongoStore, checker,  new DefaultValueChecker(),
	            notificationServices);
	}
}
