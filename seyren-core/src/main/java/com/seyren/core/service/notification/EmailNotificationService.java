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
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.springframework.mail.javamail.JavaMailSender;

import com.seyren.core.domain.Alert;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.util.config.SeyrenConfig;
import com.seyren.core.util.email.Email;
import com.seyren.core.util.email.EmailHelper;

@Named
public class EmailNotificationService implements NotificationService {
    
    private final JavaMailSender mailSender;
    private final SeyrenConfig seyrenConfig;
    private final EmailHelper emailHelper;
    
    @Inject
    public EmailNotificationService(JavaMailSender mailSender, SeyrenConfig seyrenConfig, EmailHelper emailHelper) {
        this.mailSender = mailSender;
        this.seyrenConfig = seyrenConfig;
        this.emailHelper = emailHelper;
    }
    
    @Override
    public void sendNotification(Check check, Subscription subscription, List<Alert> alerts) {
        
        try {
            Email email = new Email()
                    .withTo(subscription.getTarget())
                    .withFrom(seyrenConfig.getSmtpFrom())
                    .withSubject(emailHelper.createSubject(check))
                    .withMessage(emailHelper.createBody(check, subscription, alerts));
            
            mailSender.send(createMimeMessage(email));
            
        } catch (Exception e) {
            throw new NotificationFailedException("Failed to send notification to " + subscription.getTarget() + " from " + seyrenConfig.getSmtpFrom(), e);
        }
        
    }
    
    private MimeMessage createMimeMessage(Email email) throws AddressException, MessagingException {
        
        MimeMessage mail = mailSender.createMimeMessage();
        InternetAddress senderAddress = new InternetAddress(email.getFrom());
        mail.addRecipient(RecipientType.TO, new InternetAddress(email.getTo()));
        mail.setSender(senderAddress);
        mail.setFrom(senderAddress);
        mail.setText(email.getMessage());
        mail.setSubject(email.getSubject());
        mail.addHeader("Content-Type", "text/html; charset=UTF-8");
        
        return mail;
    }
    
    @Override
    public boolean canHandle(SubscriptionType subscriptionType) {
        return subscriptionType == SubscriptionType.EMAIL;
    }
    
}
