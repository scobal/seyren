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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.http.NameValuePair;
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
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Account;
import com.twilio.sdk.resource.instance.Message;

@Named
public class TwilioNotificationService implements NotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TwilioNotificationService.class);
    
    private final SeyrenConfig seyrenConfig;
    
    @Inject
    public TwilioNotificationService(SeyrenConfig seyrenConfig) {
        this.seyrenConfig = seyrenConfig;
    }
    
    @Override
    public void sendNotification(Check check, Subscription subscription, List<Alert> alerts) throws NotificationFailedException {
        String body;
        if (check.getState() == AlertType.ERROR) {
            body = "ERROR Check "+check.getName()+" has exceeded its threshold.";
        } else if (check.getState() == AlertType.OK) {
            body = "OK Check "+check.getName()+" has been resolved.";
        } else {
            LOGGER.warn("Did not send notification to Twilio for check in state: {}", check.getState());
            body = null;
        }
        
        if(body != null) {
            TwilioRestClient client = createTwilioClient();
            
            Account account = client.getAccount();
            
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("To", subscription.getTarget()));
            params.add(new BasicNameValuePair("From", seyrenConfig.getTwilioPhoneNumber()));
            params.add(new BasicNameValuePair("Body", body));
            
            try {
                @SuppressWarnings("unused")
                Message sms=account.getMessageFactory().create(params);

                LOGGER.debug("Sent TWILIO notification for alert in state: {}", check.getState());
            }
            catch (TwilioRestException e) {
                throw new NotificationFailedException("Failed to send notification to Twilio (code="+e.getErrorCode()+")", e);
            }
            catch (Exception e) {
                throw new NotificationFailedException("Failed to send notification to Twilio", e);
            }
        }
    }
    
    @Override
    public boolean canHandle(SubscriptionType subscriptionType) {
        return subscriptionType == SubscriptionType.TWILIO;
    }
    
    private TwilioRestClient createTwilioClient() {
        return new TwilioRestClient(seyrenConfig.getTwilioAccountSid(), seyrenConfig.getTwilioAuthToken());
    }
}
