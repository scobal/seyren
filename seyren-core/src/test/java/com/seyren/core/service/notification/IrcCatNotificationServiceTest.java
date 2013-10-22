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
package com.seyren.core.service.notification;

import com.google.common.io.*;
import com.seyren.core.domain.*;
import com.seyren.core.util.config.*;
import org.junit.*;

import java.io.*;
import java.math.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class IrcCatNotificationServiceTest {

    private static final int IRCCAT_PORT = 12345;
    private SeyrenConfig mockSeyrenConfig;
    private NotificationService service;

    @Before
    public void configureService() {
        mockSeyrenConfig = mock(SeyrenConfig.class);
        service = new IrcCatNotificationService(mockSeyrenConfig);
    }

    @Test
    public void notifcationServiceCanOnlyHandleHubotSubscription() {
        assertThat(service.canHandle(SubscriptionType.IRCCAT), is(true));
        for (SubscriptionType type : SubscriptionType.values()) {
            if (type == SubscriptionType.IRCCAT) {
                continue;
            }
            assertThat(service.canHandle(type), is(false));
        }
    }

    @Test
    public void sendNotification() throws Exception {
        when(mockSeyrenConfig.getBaseUrl()).thenReturn("http://localhost");
        when(mockSeyrenConfig.getIrcCatHost()).thenReturn("localhost");
        when(mockSeyrenConfig.getIrcCatPort()).thenReturn(IRCCAT_PORT);
        TcpServer tcpServer = new TcpServer(IRCCAT_PORT).start();

        Check check = new Check().withEnabled(true).withName("check-name")
                .withState(AlertType.ERROR);

        Subscription subscription = new Subscription().withType(
                SubscriptionType.IRCCAT).withTarget("#mychannel");

        Alert alert = new Alert().withTarget("the.target.name")
                .withValue(BigDecimal.valueOf(12))
                .withWarn(BigDecimal.valueOf(5))
                .withError(BigDecimal.valueOf(10)).withFromType(AlertType.WARN)
                .withToType(AlertType.ERROR);

        List<Alert> alerts = Arrays.asList(alert);

        service.sendNotification(check, subscription, alerts);

        tcpServer.waitForNumberOfMessage(1, 1000);

        assertThat(tcpServer.getMessages().size(), is(1));

        verify(mockSeyrenConfig).getIrcCatHost();
        verify(mockSeyrenConfig).getIrcCatPort();
        tcpServer.stop();
    }

    private static class TcpServer {

        private final int port;

        private volatile boolean shutdown = false;
        private final List<String> messages = new CopyOnWriteArrayList<String>();
        private Thread serverThread;

        public TcpServer(int port) {
            this.port = port;
        }

        public TcpServer start() {
            serverThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    ServerSocket serverSocket = null;
                    try {
                        serverSocket = new ServerSocket(port);
                        while (!shutdown) {
                            Socket socket = serverSocket.accept();
                            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(
                                            socket.getInputStream()));
                            try {
                                String message = in.readLine();
                                messages.add(message);
                                synchronized (this) {
                                    this.notifyAll();
                                }
                            } catch (IOException ioe) {
                                Closeables.closeQuietly(in);
                                socket.close();
                            }
                        }
                    } catch (IOException ioe) {
                    } finally {
                        Closeables.closeQuietly(serverSocket);
                    }
                }
            });
            serverThread.start();
            return this;
        }

        public void waitForNumberOfMessage(int n, long timeout)
                throws InterruptedException {
            long startTime = System.currentTimeMillis();
            while (messages.size() < n) {
                synchronized (this) {
                    this.wait(timeout);
                }
                if (System.currentTimeMillis() - startTime > timeout) {
                    return;
                }
            }
        }

        public void stop() {
            shutdown = true;
        }

        public List<String> getMessages() {
            return this.messages;
        }

    }

}