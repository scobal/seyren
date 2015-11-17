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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.seyren.core.domain.Check;
import com.seyren.core.store.ChecksStore;

@Named
public class CheckScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckScheduler.class);

    private static final int GUID_MAX_CHECK_VALUES = 65536; // 4 unsigned hex digits, values range 0 - 16 ^ 4 - 1
    
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
    	int checksInScope = 0;
    	int checksWereRun = 0;
        List<Check> checks = checksStore.getChecks(true, false).getValues();
        for (final Check check : checks) {
    		// Skip any not in this instance's workload
        	if (!isMyWork(check)) {
        		continue;
        	}
        	checksInScope++;
        	// See if this check is currently running, if so, return and log the 
        	// missed cycle
        	if (!CheckConcurrencyGovernor.instance().isCheckRunning(check)){
        		checksWereRun++;
            	// Notify the Check Governor that the check is now running
            	CheckConcurrencyGovernor.instance().notifiyCheckIsRunning(check);
            	executor.execute(checkRunnerFactory.create(check));
        	}
        	else {
        		CheckConcurrencyGovernor.instance().logCheckSkipped(check);
        		continue;
        	}
        }
        // Log basic information about worker instance and its work
        LOGGER.debug(String.format("Worker %d of %d is responsible for %d of %d checks, of which %d were run.", instanceIndex, totalWorkers, checksInScope, checks.size(), checksWereRun));
    }

    private boolean isMyWork(Check check) {
    	if (totalWorkers > 1) {
    		// More than 1 worker; split work on range of characters 30-33 of check id for guid-based id
    		// or modulus-based of counter-based portion of check id for a mongodb ObjectId-based id;
    		// Note: Determination of ID-type is based on length (36 for Guid, 24 for MongoDB ObjectId)
    		String id = check.getId();
    		if (id.length() == 36) {
    			// Guid-based id work sharding
        		int checkIndex = Integer.parseInt(id.substring(30,34), 16);
    			int low = (int)(GUID_MAX_CHECK_VALUES * (instanceIndex - 1) / totalWorkers);
    			int high = (int)(GUID_MAX_CHECK_VALUES * instanceIndex / totalWorkers);
        		if (low <= checkIndex && checkIndex < high) {
        			return true;
        		}    		
    		}
    		else if (id.length() == 24) {
    			// ObjectId-based id work sharding; get the last two hex characters of the timestamp portion
    			// which is the first 4 bytes or 8 characters
        		int checkIndex = Integer.parseInt(id.substring(6,8), 16);
        		
        		if ((checkIndex % totalWorkers) == (instanceIndex - 1)) {
        			return true;
        		}
    		}
    		else {
    			throw new UnsupportedOperationException("Unsupported id format; expected formats are 36 or 24 characters in length");
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
