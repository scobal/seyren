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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.script.ScriptEngineManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seyren.core.util.config.SeyrenConfig;

/**
 * ServerSocket listening Carbon Relay Pickle protocol (aka python serialization).
 */
@Named
public class CarbonPickleListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(CarbonPickleListener.class);

    private SeyrenConfig seyrenConfig;
    private PickleHandlerFactory pickleHandlerFactory;

    @Inject
    public CarbonPickleListener(SeyrenConfig seyrenConfig, PickleHandlerFactory pickleHandlerFactory) {
        this.seyrenConfig = seyrenConfig;
        this.pickleHandlerFactory = pickleHandlerFactory;
    }

    @PostConstruct
    public void initialize() throws Exception {
        if (seyrenConfig.getGraphiteCarbonPickleEnable()) {
            new ScriptEngineManager().getEngineByName("python");
            bootstrap();
        } else {
            LOGGER.info("Carbon Pickle Listener disabled.");
        }
    }

    public void bootstrap() throws Exception {
        LOGGER.info("Carbon Pickle Listener enabled.");
        Thread thread = new Thread("Carbon Pickle Listener") {
            @Override
            public void run() {
                ServerSocket serverSocket = null;
                ExecutorService tasks = Executors.newFixedThreadPool(2);
                try {
                    serverSocket = new ServerSocket(seyrenConfig.getGraphiteCarbonPicklePort());
                    serverSocket.setReuseAddress(true);
                    serverSocket.setReceiveBufferSize(1024 * 1024);

                    while (true) {
                        LOGGER.debug("Accepting...");
                        Socket socket = serverSocket.accept();
                        socket.setKeepAlive(true);
                        socket.setTcpNoDelay(true);
                        socket.setReceiveBufferSize(1024 * 1024);
                        tasks.execute(pickleHandlerFactory.create(socket));
                    }
                } catch (IOException e) {
                    LOGGER.warn("Error: ", e);
                } finally {
                    if (serverSocket != null) {
                        try {
                            serverSocket.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
        };
        thread.start();
    }

}
