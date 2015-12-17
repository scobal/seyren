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

import static com.github.restdriver.clientdriver.RestClientDriver.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.restdriver.clientdriver.ClientDriverRequest.Method;
import com.github.restdriver.clientdriver.ClientDriverRule;
import com.seyren.core.domain.Alert;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.util.config.SeyrenConfig;

public class HipChatNotificationServiceTest {
    
    private SeyrenConfig seyrenConfig;
    private NotificationService notificationService;
    
    @Rule
    public ClientDriverRule clientDriver = new ClientDriverRule();
    
    @Before
    public void before() {
        seyrenConfig = new SeyrenConfig();
        notificationService = new HipChatNotificationService(seyrenConfig, clientDriver.getBaseUrl());
    }
    
    @Test
    public void notifcationServiceCanHandleHipChatSubscription() {
        assertThat(notificationService.canHandle(SubscriptionType.HIPCHAT), is(true));
    }
    
    @Test
    public void basicHappyPathTest() throws Exception {
        Check check = new Check()
                .withEnabled(true)
                .withName("test-check")
                .withState(AlertType.ERROR);
        Subscription subscription = new Subscription()
                .withEnabled(true)
                .withType(SubscriptionType.HIPCHAT)
                .withTarget("target");
        Alert alert = new Alert()
                .withFromType(AlertType.OK)
                .withToType(AlertType.ERROR);
        List<Alert> alerts = Arrays.asList(alert);

        String seyrenUrl = URLEncoder.encode(seyrenConfig.getBaseUrl(), "UTF-8");
        clientDriver.addExpectation(
                onRequestTo("/v2/room/target/notification")
                        .withMethod(Method.POST)
                        .withParam("auth_token",seyrenConfig.getHipChatAuthToken())
                        .withBody(is("message=Check+%3Ca+href%3D" + seyrenUrl + "%2F%23%2Fchecks%2Fnull%3Etest-check%3C%2Fa%3E+has+entered+its+ERROR+state."
                                + "&color=red"
                                + "&message_format=html"
                                + "&notify=true"), "application/x-www-form-urlencoded"),
                giveEmptyResponse());
        
        notificationService.sendNotification(check, subscription, alerts);
    }
    
}
