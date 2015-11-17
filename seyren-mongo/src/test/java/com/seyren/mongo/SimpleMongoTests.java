package com.seyren.mongo;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.WriteResult;
import com.seyren.core.domain.Alert;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.util.config.SeyrenConfig;
import com.seyren.core.util.hashing.TargetHash;

public class SimpleMongoTests {
	
	@Mock Mongo mongo;
	
	@Mock DB db;
	
	@Mock MongoStore mongoStore;
	
	@Mock DBCollection alertsCollection;
	
	@Mock DBCollection checksCollection;
	
	@Mock DBCursor alertsCursor;
	
	@Mock SeyrenConfig config;
	
	@Mock WriteResult writeResult;

	private static Subscription subscription;
	
	private static DBObject subscriptionRecord;
	
	private static Alert currentAlert;
	
	private static Alert previousAlert;
	
	private static DBObject previousAlertRecord;
	
	private static Check check;
	
	private static DBObject checkRecord;
	
	private static boolean lastAlertRetrieved;
	
	private Check getCheck(){
		Check check = new Check();
		return check;
	}
	
	private Alert getPreviousAlert(){
		Alert alert = new Alert();
		return alert;
	}
	
	private Alert getCurrentAlert(){
		Alert alert = new Alert();
		return alert;
	}
	
	private Subscription getSubscription(){
		Subscription subscription = new Subscription();
		return subscription;
	}
	
	@Before
	public void setUp() throws Exception {
		
		previousAlertRecord = new BasicDBObject();
		previousAlertRecord.put("_id","0");
		previousAlertRecord.put("warn","120");
		previousAlertRecord.put("toType","OK");
		previousAlertRecord.put("targetHash","?w}`p??t????%?");
		previousAlertRecord.put("fromType","WARN");
		previousAlertRecord.put("error","160");
		previousAlertRecord.put("checkId","-3556770559393616786");
		previousAlertRecord.put("value","40");
		previousAlertRecord.put("target","carbon.agents.HQEXPEDIALinux01-a.creates");
		previousAlertRecord.put("timestamp",new Date());
		
		
		alertsCursor = Mockito.mock(DBCursor.class);
		
		Mockito.when(alertsCursor.next()).thenAnswer(new Answer<DBObject>() {
		     public DBObject answer(InvocationOnMock invocation) throws Throwable {
				return previousAlertRecord;
		     }
		}); 
		
		Mockito.when(alertsCursor.hasNext()).thenAnswer(new Answer<Boolean>() {
		     public Boolean answer(InvocationOnMock invocation) throws Throwable {
		    	 if (!lastAlertRetrieved){
		    		 lastAlertRetrieved = true;
		    		 return true;
		    	 }
		    	 else {
		    		 return false;
		    	 }
		     }
		 });
		
		Mockito.doAnswer(new Answer<DBCursor>(){ 
			public DBCursor answer(InvocationOnMock invocation) throws Throwable {
				return alertsCursor;
			}})
	    .when(alertsCursor).limit(
	          Mockito.any(Integer.class));
		
		Mockito.doAnswer(new Answer<DBCursor>(){ 
			public DBCursor answer(InvocationOnMock invocation) throws Throwable {
				return alertsCursor;
			}})
	    .when(alertsCursor).sort(
	          Mockito.any(BasicDBObject.class));
		
		Mockito.when(alertsCursor.sort(new BasicDBObject())).thenAnswer(new Answer<DBCursor>() {
		     public DBCursor answer(InvocationOnMock invocation) throws Throwable {
		         return alertsCursor;
		     }
		});
		
		alertsCollection = Mockito.mock(DBCollection.class);
		
		
		NiceDBObject query = NiceDBObject.object("checkId", "-3556770559393616786");
		query.append("targetHash" , ";a@cL???iv?8?");

		Mockito.doAnswer(new Answer<DBCursor>(){ 
			public DBCursor answer(InvocationOnMock invocation) throws Throwable {
				return alertsCursor;
			}})
	    .when(alertsCollection).find(
	          Mockito.any(DBObject.class));
		
		
		Mockito.when(alertsCollection.find()).thenAnswer(new Answer<DBCursor>() {
		     public DBCursor answer(InvocationOnMock invocation) throws Throwable {
		    	 return alertsCursor;
		     }
		});
		
		Mockito.doAnswer(new Answer<DBCursor>(){ 
			public DBCursor answer(InvocationOnMock invocation) throws Throwable {
				return alertsCursor;
			}})
	    .when(alertsCollection).find(
	          Mockito.any(NiceDBObject.class));
		
		config = Mockito.mock(SeyrenConfig.class);

		Mockito.when(config.getMongoUrl()).thenAnswer(new Answer<String>() {
		     public String answer(InvocationOnMock invocation) throws Throwable {
		    	 System.out.println("Getting URL of Mongo DB");
		         return "mongodb://somedomain.com:12345/seyren";
		     }
		});

		DB db = Mockito.mock(DB.class);
		Mockito.when(db.getCollection("alerts")).thenAnswer(new Answer<DBCollection>() {
		     public DBCollection answer(InvocationOnMock invocation) throws Throwable {
		         return alertsCollection;
		     }
		});
		
		Mockito.when(db.getCollection("checks")).thenAnswer(new Answer<DBCollection>() {
		     public DBCollection answer(InvocationOnMock invocation) throws Throwable {
		         return checksCollection;
		     }
		});
		
		
		
		mongoStore = new MongoStore(db, config);
		mongoStore.setConfig(config);
		
		
		writeResult = Mockito.mock(WriteResult.class);
		Mockito.when(writeResult.getN()).thenAnswer(new Answer<Integer>() {
		     public Integer answer(InvocationOnMock invocation) throws Throwable {
		         return 1;
		     }
		});
	}


	@After
	public void tearDown() throws Exception {
		
	}
	
	@Test
	public void testAlertRetrieval(){
		Alert alert = mongoStore.getLastAlertForTargetOfCheck("someTarget", "-3556770559393616786");
		assertNotNull(alert);
		assertEquals(alert.getId(),"0");
	}
	
	

}
