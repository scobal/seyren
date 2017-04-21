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

import java.util.List;

import com.seyren.core.domain.Alert;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.service.notification.NotificationService;

public class MockNotificationService implements NotificationService {
	
	
	@Override
	public void sendNotification(Check check, Subscription subscription, List<Alert> alerts)
			throws NotificationFailedException {
		if (subscription instanceof MockSubscription){
			((MockSubscription)subscription).sendNotification();
		}
	}

	@Override
	public boolean canHandle(SubscriptionType subscriptionType) {
		return true;
	}

}
