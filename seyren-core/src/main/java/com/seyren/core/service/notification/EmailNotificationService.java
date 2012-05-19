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
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.util.email.Email;

@Named
public class EmailNotificationService implements NotificationService {

	private static final String TEMPLATE_FILE_NAME = "com/seyren/core/service/notification/email-template.vm";
	
    private final JavaMailSender mailSender;

    @Inject
    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        Velocity.init();
    }
    
    @Override
    public void sendNotification(Check check, Subscription subscription, Alert alert) {

        try {
        	VelocityContext context = createVelocityContext(check, subscription, alert);
        	
        	StringWriter w = new StringWriter();
	    	Velocity.evaluate(context, w, "EmailNotificationService", getTemplateAsString());
	    	
	    	Email email = new Email()
				.withTo(subscription.getTarget())
				.withFrom("seyren-alerts@seyren")
				.withSubject(createSubject(check, alert))
				.withMessage(w.getBuffer().toString());
        	
	    	mailSender.send(createMimeMessage(email));
	    	
        } catch (Exception e) {
            throw new NotificationFailedException("Failed to send notification to " + subscription.getTarget(), e);
        }
    }

	private String createSubject(Check check, Alert alert) {
		return check.getName() + " from " + alert.getFromType() + " to " + alert.getToType();
	}

	private VelocityContext createVelocityContext(Check check, Subscription subscription, Alert alert) {
		VelocityContext result = new VelocityContext();
		result.put("CHECK_NAME", check.getName());
		result.put("ALERT_ERROR", alert.getError());
		result.put("ALERT_FROMTYPE", alert.getFromType());
		result.put("ALERT_TARGET", alert.getTarget());
		result.put("ALERT_TIMESTAMP", alert.getTimestamp().toString("HH:mm d MMM yyyy"));
		result.put("ALERT_TOTYPE", alert.getToType());
		result.put("ALERT_VALUE", alert.getValue());
		result.put("ALERT_WARN", alert.getWarn());
		return result;
	}

	private String getTemplateAsString() throws IOException {
		return IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(TEMPLATE_FILE_NAME));
	}

    private MimeMessage createMimeMessage(Email email) throws AddressException, MessagingException {

    	MimeMessage mail = mailSender.createMimeMessage();
        mail.addRecipient(RecipientType.TO, new InternetAddress(email.getTo()));
        mail.setSender(new InternetAddress(email.getFrom()));
        mail.setText(email.getMessage());
        mail.setSubject(email.getSubject());
        mail.addHeader("Content-Type", "text/html; charset=UTF-8");

        return mail;
    }
}
