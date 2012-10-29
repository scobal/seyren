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

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.apache.commons.io.IOUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.mail.javamail.JavaMailSender;

import com.seyren.core.domain.Alert;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.util.config.SeyrenConfig;
import com.seyren.core.util.email.Email;

@Named
public class EmailNotificationService implements NotificationService {

	private static final String TEMPLATE_FILE_NAME = "com/seyren/core/service/notification/email-template.vm";
	
    private final JavaMailSender mailSender;
    private final SeyrenConfig seyrenConfig;

    @Inject
    public EmailNotificationService(JavaMailSender mailSender, SeyrenConfig seyrenConfig) {
        this.mailSender = mailSender;
        this.seyrenConfig = seyrenConfig;
        Velocity.init();
    }
    
    @Override
    public void sendNotification(Check check, Subscription subscription, List<Alert> alerts) {

        try {
        	VelocityContext context = createVelocityContext(check, subscription, alerts);
        	
        	StringWriter w = new StringWriter();
	    	Velocity.evaluate(context, w, "EmailNotificationService", getTemplateAsString());
	    	
	    	Email email = new Email()
				.withTo(subscription.getTarget())
				.withFrom(seyrenConfig.getFromEmail())
				.withSubject(createSubject(check))
				.withMessage(w.getBuffer().toString());
        	
	    	mailSender.send(createMimeMessage(email));
	    	
        } catch (Exception e) {
            throw new NotificationFailedException("Failed to send notification to " + subscription.getTarget() + " from " + seyrenConfig.getFromEmail(), e);
        }
    }

	private String createSubject(Check check) {
		return "Seyren alert: " + check.getName();
	}

	private VelocityContext createVelocityContext(Check check, Subscription subscription, List<Alert> alerts) {
		VelocityContext result = new VelocityContext();
		result.put("CHECK", check);
		result.put("ALERTS", alerts);
		result.put("SEYREN_URL", seyrenConfig.getBaseUrl());
		return result;
	}

	private String getTemplateAsString() throws IOException {
		return IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(TEMPLATE_FILE_NAME));
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
