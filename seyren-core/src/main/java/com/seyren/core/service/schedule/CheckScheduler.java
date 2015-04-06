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
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import com.seyren.core.util.config.SeyrenConfig;
import org.springframework.scheduling.annotation.Scheduled;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.seyren.core.domain.Check;
import com.seyren.core.store.ChecksStore;

@Named
public class CheckScheduler {
    
    private final ScheduledExecutorService executor;
    private final ChecksStore checksStore;
    private final CheckRunnerFactory checkRunnerFactory;
    
    @Inject
    public CheckScheduler(ChecksStore checksStore, CheckRunnerFactory checkRunnerFactory, SeyrenConfig seyrenConfig) {
        this.checksStore = checksStore;
        this.checkRunnerFactory = checkRunnerFactory;
        this.executor = Executors.newScheduledThreadPool(seyrenConfig.getNoOfThreads(), new ThreadFactoryBuilder().setNameFormat("seyren.check-scheduler-%s")
                        .setDaemon(false).build());
    }
    
    @Scheduled(fixedRateString = "${GRAPHITE_REFRESH:60000}")
    public void performChecks() {
        List<Check> checks = checksStore.getChecks(true, false).getValues();
        for (final Check check : checks) {
            executor.execute(checkRunnerFactory.create(check));
        }
    }
    
    @PreDestroy
    public void preDestroy() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(500, TimeUnit.MILLISECONDS);
    }
    
}
