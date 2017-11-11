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

import com.seyren.awsmanager.AWSManager;
import com.seyren.core.domain.*;
import com.seyren.core.util.config.SeyrenConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Created by akharbanda on 05/11/17.
 */
public class AWSUnhealthyInstanceNotificationServiceTest
{
    private SeyrenConfig mockSeyrenConfig;
    private NotificationService service;
    private AWSManager awsManager = mock(AWSManager.class);

    @Before
    public void before() {
        mockSeyrenConfig = mock(SeyrenConfig.class);
        service = new AWSUnhealthyInstanceNotificationService(awsManager,mockSeyrenConfig);
    }

    @Test
    public void testNotifcationServiceCanOnlyHandleAWSUnhealthySubscription() {
        assertThat(service.canHandle(SubscriptionType.AWS_UNHEALTHY_INSTANCE), is(true));
        for (SubscriptionType type : SubscriptionType.values()) {
            if (type == SubscriptionType.AWS_UNHEALTHY_INSTANCE) {
                continue;
            }
            assertThat(service.canHandle(type), is(false));
        }
    }

    @Test
    public void testNoNotificationForOKAlert() {
        Alert alert = new OutlierAlert();
        alert.setToType(AlertType.OK);
        Subscription subscription = new Subscription();
        subscription.setType(SubscriptionType.AWS_UNHEALTHY_INSTANCE);
        service.sendNotification(new ThresholdCheck(),subscription, new ArrayList<Alert>(Arrays.asList(new Alert[]{alert})));
        verify(awsManager, never()).convictInstance(Mockito.anyList());
    }
}
