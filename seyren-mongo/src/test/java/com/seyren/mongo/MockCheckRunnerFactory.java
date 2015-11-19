package com.seyren.mongo;

import java.util.ArrayList;
import java.util.List;

import com.seyren.core.service.checker.TargetChecker;
import com.seyren.core.service.checker.ValueChecker;
import com.seyren.core.service.notification.NotificationService;
import com.seyren.core.service.schedule.CheckRunnerFactory;
import com.seyren.core.store.AlertsStore;
import com.seyren.core.store.ChecksStore;
import com.seyren.mongo.MongoStore;

public class MockCheckRunnerFactory extends CheckRunnerFactory {

	public MockCheckRunnerFactory(MongoStore mongoStore, 
			TargetChecker targetChecker,
			ValueChecker valueChecker, 
			List<NotificationService>  notificationServices) {
		super(mongoStore, mongoStore, targetChecker, valueChecker, notificationServices);
	}

}
