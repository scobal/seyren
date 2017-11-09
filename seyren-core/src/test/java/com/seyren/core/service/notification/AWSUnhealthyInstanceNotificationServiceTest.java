package com.seyren.core.service.notification;

import com.seyren.awsmanager.AWSManager;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.util.config.SeyrenConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

/**
 * Created by akharbanda on 05/11/17.
 */
public class AWSUnhealthyInstanceNotificationServiceTest
{
    private SeyrenConfig mockSeyrenConfig;
    private NotificationService service;
    private AWSManager awsManager = new AWSManager();

    @Before
    public void before() {
        mockSeyrenConfig = mock(SeyrenConfig.class);
        service = new AWSUnhealthyInstanceNotificationService(awsManager,mockSeyrenConfig);
    }

    @Test
    public void notifcationServiceCanOnlyHandleAWSUnhealthySubscription() {
        assertThat(service.canHandle(SubscriptionType.AWS_UNHEALTHY_INSTANCE), is(true));
        for (SubscriptionType type : SubscriptionType.values()) {
            if (type == SubscriptionType.AWS_UNHEALTHY_INSTANCE) {
                continue;
            }
            assertThat(service.canHandle(type), is(false));
        }
    }

}
