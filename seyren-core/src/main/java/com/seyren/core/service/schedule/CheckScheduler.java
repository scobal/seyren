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
package com.seyren.core.service.schedule;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import com.seyren.core.domain.Alert;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.service.checker.TargetChecker;
import com.seyren.core.service.notification.NotificationService;
import com.seyren.core.store.AlertsStore;
import com.seyren.core.store.ChecksStore;

@Named
public class CheckScheduler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckScheduler.class);

	private final ChecksStore checksStore;
	private final AlertsStore alertsStore;
	private final TargetChecker checker;
	private final NotificationService notificationService;
	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(8);

	@Inject
	public CheckScheduler(ChecksStore checksStore, AlertsStore alertsStore, NotificationService notificationService, TargetChecker checker) {
		this.checksStore = checksStore;
		this.alertsStore = alertsStore;
        this.notificationService = notificationService;
		this.checker = checker;
	}
	
	@Scheduled(fixedRate = 60000)
	public void performChecks() {
	    List<Check> checks = checksStore.getChecks();
		for (final Check check : checks) {
		    executor.execute(new CheckRunner(check));
		}
	}
	
	private class CheckRunner implements Runnable {
	    
	    private final Check check;
	    
	    public CheckRunner(Check check) {
	        this.check = check;
	    }
	    
	    @Override
	    public final void run() {
	        if (check.isEnabled()) {
                try {
                    List<Alert> alerts = checker.check(check);
                    
                    for (Alert alert : alerts) {
                        if (alert.isStillOk()) {
                            continue;
                        }
                        
                        alertsStore.createAlert(check.getId(), alert);
                        check.setState(alert.getToType());
                        checksStore.saveCheck(check);
                        
                        // Only notify if the alert has changed state
                        if (!alert.hasStateChanged()) {
                            continue;
                        }
                        
                        for (Subscription subscription : check.getSubscriptions()) {
                            if (!subscription.shouldNotify(alert)) {
                                continue;
                            }
                            
                            try {
                                notificationService.sendNotification(check, subscription, alert);
                            } catch (Exception e) {
                                LOGGER.warn(subscription.getTarget() + " failed", e);
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warn(check.getName() + " failed", e);
                }
            }
	    }
	    
	}
	
}
