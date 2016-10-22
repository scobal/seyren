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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.mail.javamail.JavaMailSender;

import com.seyren.core.domain.Alert;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.util.config.SeyrenConfig;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
public class SnsNotificationService implements NotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SnsNotificationService.class);

    private AmazonSNSClient snsClient;

    @Inject
    public SnsNotificationService(SeyrenConfig seyrenConfig) {
      String snsRegion = seyrenConfig.getSnsRegion();

      this.snsClient = new AmazonSNSClient();
      this.snsClient.setRegion(Region.getRegion(Regions.fromName(snsRegion)));
    }

    @Override
    public void sendNotification(Check check, Subscription subscription, List<Alert> alerts) {
      String topicArn = subscription.getTarget();

      String msg = String.format("Seyren notification '%s' changed state to '%s'",
          check.getName(),
          check.getState().name());

      LOGGER.info("Sending Notification to SNS Topic: " + topicArn + " with message " + msg);

      PublishRequest publishRequest = new PublishRequest(topicArn, msg);
      PublishResult publishResult = snsClient.publish(publishRequest);

      LOGGER.info("Send Notification - " + publishResult.getMessageId());
    }

    @Override
    public boolean canHandle(SubscriptionType subscriptionType) {
        return subscriptionType == SubscriptionType.SNS;
    }
}
