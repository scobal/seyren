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
    
    private NotificationService notificationService;
    
    @Rule
    public ClientDriverRule clientDriver = new ClientDriverRule();
    
    @Before
    public void before() {
        notificationService = new HipChatNotificationService(new SeyrenConfig(), clientDriver.getBaseUrl());
    }
    
    @Test
    public void notifcationServiceCanHandleHipChatSubscription() {
        assertThat(notificationService.canHandle(SubscriptionType.HIPCHAT), is(true));
    }
    
    @Test
    public void basicHappyPathTest() {
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
        
        clientDriver.addExpectation(
                onRequestTo("/v1/rooms/message")
                        .withMethod(Method.POST)
                        .withParam("auth_token", "")
                        .withParam("from", "Seyren Alert")
                        .withParam("room_id", "target")
                        .withParam("message", "Check <a href=http://localhost:8080/seyren/#/checks/null>test-check</a> has entered its ERROR state.")
                        .withParam("color", "red")
                        .withParam("notify", "1")
                        .withHeader("Content-Type", "application/x-www-form-urlencoded"),
                giveEmptyResponse());
        
        notificationService.sendNotification(check, subscription, alerts);
    }
    
}
