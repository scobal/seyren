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
