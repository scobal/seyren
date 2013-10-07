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
package com.seyren.core.service.live.server;

import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.seyren.core.service.schedule.CheckRunnerFactory;
import com.seyren.core.store.ChecksStore;
import com.seyren.core.util.config.SeyrenConfig;

@Named
public class PickleHandlerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(PickleHandlerFactory.class);

    private SeyrenConfig seyrenConfig;
    private ChecksStore checksStore;
    private CheckRunnerFactory checkRunnerFactory;
    private ThreadPoolExecutor executor;

    @Inject
    public PickleHandlerFactory(SeyrenConfig seyrenConfig, ChecksStore checksStore, CheckRunnerFactory checkRunnerFactory) {
        this.seyrenConfig = seyrenConfig;
        this.checksStore = checksStore;
        this.checkRunnerFactory = checkRunnerFactory;
    }

    @PostConstruct
    public void initialize() {
        if (seyrenConfig.getGraphiteCarbonPickleEnable()) {
            executor = new ThreadPoolExecutor(
                2,
                8,
                500, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(1000),
                new ThreadFactoryBuilder().setNameFormat("seyren.check-live-%s").build()
            );
            executor.prestartCoreThread();
        } else {
            LOGGER.info("Carbon Pickle Listener disabled.");
        }
    }

    @PreDestroy
    public void preDestroy() throws InterruptedException {
        if (seyrenConfig.getGraphiteCarbonPickleEnable()) {
            executor.shutdown();
            while (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                LOGGER.info("Awaiting completion of threads...");
            }
        }
    }

    public PickleHandler create(Socket socket) {
        return new PickleHandler(socket, executor, checksStore, checkRunnerFactory);
    }

}
