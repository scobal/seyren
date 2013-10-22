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
import com.seyren.core.exception.*;
import com.seyren.core.util.config.*;
import org.slf4j.*;

import javax.inject.*;
import java.io.*;
import java.net.*;
import java.util.*;

import static java.lang.String.*;

@Named
public class IrcCatNotificationService implements NotificationService {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(IrcCatNotificationService.class);

    private final SeyrenConfig seyrenConfig;

    @Inject
    public IrcCatNotificationService(SeyrenConfig seyrenConfig) {
        this.seyrenConfig = seyrenConfig;
    }

    @Override
    public void sendNotification(Check check, Subscription subscription, List<Alert> alerts) throws NotificationFailedException {
        List<String> channels = Arrays.asList(subscription.getTarget().split(","));
        try {
            for (String channel : channels) {
                String message = createMessage(check);
                sendMessage(seyrenConfig.getIrcCatHost(), seyrenConfig.getIrcCatPort(), message, channel);
            }
        } catch (IOException ioe) {
            throw new NotificationFailedException("Could not send message", ioe);
        }
    }

    private String createMessage(Check check) {
        String checkUrl = seyrenConfig.getBaseUrl() + "/#/checks/" + check.getId();

        if (check.getState() == AlertType.ERROR) {
            return format("%%RED[CRIT]%%NORMAL %s | Please check %s.", check.getName(), checkUrl);
        }
        if (check.getState() == AlertType.WARN) {
            return format("%%YELLOW[WARN]%%NORMAL %s | Please check %s.", check.getName(), checkUrl);
        }
        if (check.getState() == AlertType.OK) {
            return format("%%GREEN[OK]%%NORMAL %s | %s", check.getName(), checkUrl);
        }

        LOGGER.info("Unmanaged check state [%s] for check [%s]", check.getState(), check.getName());
        return "";
    }

    private void sendMessage(String ircCatHost, int ircCatPort, String message, String channel) throws IOException {
        Socket socket = new Socket(ircCatHost, ircCatPort);
        Writer out = new OutputStreamWriter(socket.getOutputStream());
        try {
            out.write(format("%s %s\n", channel, message));
            out.flush();
        } catch (IOException e) {
            Closeables.closeQuietly(out);
            socket.close();
        }
    }

    @Override
    public boolean canHandle(SubscriptionType subscriptionType) {
        return subscriptionType == SubscriptionType.IRCCAT;
    }

}