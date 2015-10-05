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
    private static final int MAX_CHECK_VALUES = 65536; // 4 unsigned hex digits, values range 0 - 16 ^ 4 - 1

    private final ScheduledExecutorService executor;
    private final ChecksStore checksStore;
    private final CheckRunnerFactory checkRunnerFactory;
    private final int instanceIndex;
    private final int totalWorkers;

    @Inject
    public CheckScheduler(ChecksStore checksStore, CheckRunnerFactory checkRunnerFactory, SeyrenConfig seyrenConfig) {
        this.checksStore = checksStore;
        this.checkRunnerFactory = checkRunnerFactory;
        this.executor = Executors.newScheduledThreadPool(seyrenConfig.getNoOfThreads(), new ThreadFactoryBuilder().setNameFormat("seyren.check-scheduler-%s")
                        .setDaemon(false).build());
        this.instanceIndex = seyrenConfig.getCheckExecutorInstanceIndex();
        this.totalWorkers = seyrenConfig.getCheckExecutorTotalInstances();
    }

    @Scheduled(fixedRateString = "${GRAPHITE_REFRESH:60000}")
    public void performChecks() {
        List<Check> checks = checksStore.getChecks(true, false).getValues();
        for (final Check check : checks) {
    		// Skip any not in this instance's workload
        	if (!isMyWork(check)) {
        		continue;
        	}

        	executor.execute(checkRunnerFactory.create(check));
        }
    }

    private boolean isMyWork(Check check) {
    	if (totalWorkers > 1) {
    		// More than 1 worker; split work on range of characters 30-33 of check id
    		int checkIndex = Integer.parseInt(check.getId().substring(20,24), 16);
    		if ((int)(MAX_CHECK_VALUES * (instanceIndex - 1) / totalWorkers) <= checkIndex && checkIndex < (int)(MAX_CHECK_VALUES * instanceIndex / totalWorkers)) {
    			return true;
    		}

    		// Not in range for this worker instance
    		return false;
    	}

    	return true;
    }

    @PreDestroy
    public void preDestroy() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(500, TimeUnit.MILLISECONDS);
    }

}
