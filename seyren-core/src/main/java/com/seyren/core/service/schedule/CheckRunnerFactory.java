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

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.seyren.core.domain.Check;
import com.seyren.core.service.checker.NoopTargetCheck;
import com.seyren.core.service.checker.TargetChecker;
import com.seyren.core.service.checker.ValueChecker;
import com.seyren.core.service.notification.NotificationService;
import com.seyren.core.store.AlertsStore;
import com.seyren.core.store.ChecksStore;

@Named
public class CheckRunnerFactory {
    
    private final AlertsStore alertsStore;
    private final ChecksStore checksStore;
    private final TargetChecker targetChecker;
    private final ValueChecker valueChecker;
    private final Iterable<NotificationService> notificationServices;
    
    @Inject
    public CheckRunnerFactory(AlertsStore alertsStore, ChecksStore checksStore, TargetChecker targetChecker, ValueChecker valueChecker,
            List<NotificationService> notificationServices) {
        this.alertsStore = alertsStore;
        this.checksStore = checksStore;
        this.targetChecker = targetChecker;
        this.valueChecker = valueChecker;
        this.notificationServices = notificationServices;
    }

    public CheckRunner create(Check check) {
        return new CheckRunner(check, alertsStore, checksStore, targetChecker, valueChecker, notificationServices);
    }

    public CheckRunner create(Check check, BigDecimal value) {
        return new CheckRunner(check, alertsStore, checksStore, new NoopTargetCheck(value), valueChecker, notificationServices);
    }

}
