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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.seyren.core.domain.Check;
import com.seyren.core.service.checker.*;
import com.seyren.core.service.notification.NotificationService;
import com.seyren.core.store.AlertsStore;
import com.seyren.core.store.ChecksStore;
import com.seyren.core.util.config.SeyrenConfig;
import com.seyren.core.util.http.HttpUrlFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
public class CheckRunnerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckRunnerFactory.class);
    private final AlertsStore alertsStore;
    private final ChecksStore checksStore;
    private final TargetChecker targetChecker;
    private final ValueChecker valueChecker;
    private final Iterable<NotificationService> notificationServices;
    private final HttpTargetChecker httpTargetChecker;
    private final JenkinsTargetChecker jenkinsTargetChecker;

    @Inject
    public CheckRunnerFactory(AlertsStore alertsStore, ChecksStore checksStore, TargetChecker targetChecker, ValueChecker valueChecker,
                              List<NotificationService> notificationServices,HttpUrlFetcher httpUrlFetcher,SeyrenConfig seyrenConfig) {
        this.alertsStore = alertsStore;
        this.checksStore = checksStore;
        this.targetChecker = targetChecker;
        this.valueChecker = valueChecker;
        this.notificationServices = notificationServices;
        this.httpTargetChecker = new HttpTargetChecker(httpUrlFetcher);
        this.jenkinsTargetChecker = new JenkinsTargetChecker(httpUrlFetcher,seyrenConfig.getJenkinsServerUrl(),seyrenConfig.getJenkinsUser(),seyrenConfig.getJenkinsPassword());
    }

    public CheckRunner create(Check check) {
        return new CheckRunner(check, alertsStore, checksStore, targetChecker, valueChecker, notificationServices);
    }

    public CheckRunner create(Check check, BigDecimal value) {
        return new CheckRunner(check, alertsStore, checksStore, new NoopTargetCheck(value), valueChecker, notificationServices);
    }

    public Runnable createHttpChecker(Check check) {
        return new CheckRunner(check,alertsStore,checksStore,httpTargetChecker,new HttpResponseValueChecker(),notificationServices);
    }

    public Runnable createJenkinsChecker(Check check) {
        return new CheckRunner(check,alertsStore,checksStore,jenkinsTargetChecker,new JenkinsResponseValueChecker(),notificationServices);
    }
}
