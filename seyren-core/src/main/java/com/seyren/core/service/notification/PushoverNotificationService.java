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

import static java.lang.String.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seyren.core.domain.Alert;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.util.config.SeyrenConfig;

@Named
public class PushoverNotificationService implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushoverNotificationService.class);
    private final SeyrenConfig seyrenConfig;

    @Inject
    public PushoverNotificationService(SeyrenConfig seyrenConfig) {
        this.seyrenConfig = seyrenConfig;
    }

    @Override
    public void sendNotification(Check check, Subscription subscription, List<Alert> alerts) throws NotificationFailedException {
        String pushoverAppApiToken = StringUtils.trimToNull(seyrenConfig.getPushoverAppApiToken());
        String pushoverUserKey = StringUtils.trimToNull(subscription.getTarget());
        String pushoverMsgTitle = formatMsgTitle(check);
        String pushoverMsgBody = "Check details : " + seyrenConfig.getBaseUrl() + "/#/checks/" + check.getId();
        String pushoverMsgPriority = getMsgPriority(check);

        if (pushoverAppApiToken == null) {
            LOGGER.warn("Pushover App API Token must be provided");
            return;
        }

        if ( pushoverUserKey == null || pushoverUserKey.length() != 30 ) {
            LOGGER.warn("Invalid or missing Pushover user key");
            return;
        }


        HttpClient client = HttpClientBuilder.create().useSystemProperties().build();
        HttpPost post = new HttpPost("https://api.pushover.net/1/messages.json");

        try {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("token", pushoverAppApiToken));
            nameValuePairs.add(new BasicNameValuePair("user", pushoverUserKey));
            nameValuePairs.add(new BasicNameValuePair("title", pushoverMsgTitle));
            nameValuePairs.add(new BasicNameValuePair("message", pushoverMsgBody));
            nameValuePairs.add(new BasicNameValuePair("priority", pushoverMsgPriority));

            post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            client.execute(post);
        } catch (IOException e) {
            throw new NotificationFailedException("Sending notification to Pushover failed.", e);
        } finally {
            HttpClientUtils.closeQuietly(client);
        }
    }

    private String formatMsgTitle(Check check) {
        if (check.getState() == AlertType.ERROR) {
            return format("[CRIT] %s", check.getName());
        }
        if (check.getState() == AlertType.WARN) {
            return format("[WARN] %s", check.getName());
        }
        if (check.getState() == AlertType.OK) {
            return format("[OK] %s", check.getName());
        }

        LOGGER.info("Unmanaged check state [%s] for check [%s]", check.getState(), check.getName());
        return "";
    }

    private String getMsgPriority(Check check) {
        if (check.getState() == AlertType.WARN || check.getState() == AlertType.ERROR) {
            return "1";
        } else {
            return "0";
        }
    }

    @Override
    public boolean canHandle(SubscriptionType subscriptionType) {
        return subscriptionType == SubscriptionType.PUSHOVER;
    }
}
