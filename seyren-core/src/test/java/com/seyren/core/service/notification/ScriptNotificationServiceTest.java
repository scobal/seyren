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

import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang.SystemUtils;
import org.junit.Before;
import org.junit.Test;

import com.seyren.core.domain.Alert;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.util.config.SeyrenConfig;

public class ScriptNotificationServiceTest {
    
    private SeyrenConfig mockSeyrenConfig;
    private NotificationService notificationService;
        
    @Before
    public void before() {
        mockSeyrenConfig = mock(SeyrenConfig.class);
        when(mockSeyrenConfig.getScriptType()).thenReturn("python");
        when(mockSeyrenConfig.getScriptPath()).thenReturn(Thread.currentThread().getContextClassLoader().getResource("script.py").toString());
        when(mockSeyrenConfig.getBaseUrl()).thenReturn("http://somefakehostname.int");
        
        notificationService = new ScriptNotificationService(mockSeyrenConfig);
    }
    
    @Test
    public void notificationServiceCanHandleScriptSubscription() {
        assertThat(notificationService.canHandle(SubscriptionType.SCRIPT), is(SystemUtils.IS_OS_LINUX));
    }
    
    @Test
    public void basicHappyPathTest() throws Exception {
        Check check = new Check()
                .withEnabled(true)
                .withId("test-id")
                .withName("test-check")
                .withState(AlertType.ERROR);
        Subscription subscription = new Subscription()
                .withEnabled(true)
                .withType(SubscriptionType.SCRIPT)
                .withTarget("eos.test.expedia.com/isactive")
                .withPosition(4);
        Alert alert = new Alert()
                .withFromType(AlertType.OK)
				.withTarget("some.complex.specificationof.machinename.for.you")
                .withToType(AlertType.ERROR);
        List<Alert> alerts = Arrays.asList(alert);
        
        notificationService.sendNotification(check, subscription, alerts);
        
        
    }
    
}
