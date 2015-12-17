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
package com.seyren.core.util.velocity;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.IOUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.seyren.core.domain.Alert;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.util.config.SeyrenConfig;
import com.seyren.core.util.email.EmailHelper;

@Named
public class VelocityEmailHelper implements EmailHelper {

    // Will first attempt to load from classpath then fall back to loading from the filesystem.
    private final String TEMPLATE_FILE_NAME;
    private final String TEMPLATE_CONTENT;

    private final String TEMPLATE_SUBJECT_FILE_NAME;
    private final String TEMPLATE_SUBJECT_CONTENT;
    
    private final SeyrenConfig seyrenConfig;

    /**
     * Loads content of configurable templated email message at creation time.
     *
     * @param seyrenConfig Used for both email template file name and the seyren URL.
     */
    @Inject
    public VelocityEmailHelper(SeyrenConfig seyrenConfig) {
        this.seyrenConfig = seyrenConfig;
        TEMPLATE_FILE_NAME = seyrenConfig.getEmailTemplateFileName();
        TEMPLATE_CONTENT = getTemplateAsString(TEMPLATE_FILE_NAME);

        TEMPLATE_SUBJECT_FILE_NAME = seyrenConfig.getEmailSubjectTemplateFileName();
        TEMPLATE_SUBJECT_CONTENT = getTemplateAsString(TEMPLATE_SUBJECT_FILE_NAME);
    }
    
    public String createSubject(Check check, Subscription subscription, List<Alert> alerts) {
        return evaluateTemplate(check, subscription, alerts, TEMPLATE_SUBJECT_CONTENT);
    }

    @Override
    public String createBody(Check check, Subscription subscription, List<Alert> alerts) {
        return evaluateTemplate(check, subscription, alerts, TEMPLATE_CONTENT);
    }

    private String evaluateTemplate(Check check, Subscription subscription, List<Alert> alerts, String templateContent) {
        VelocityContext context = createVelocityContext(check, subscription, alerts);
        StringWriter stringWriter = new StringWriter();
        Velocity.evaluate(context, stringWriter, "EmailNotificationService", templateContent);
        return stringWriter.toString();
    }
    
    private VelocityContext createVelocityContext(Check check, Subscription subscription, List<Alert> alerts) {
        VelocityContext result = new VelocityContext();
        result.put("CHECK", check);
        result.put("ALERTS", alerts);
        result.put("SEYREN_URL", seyrenConfig.getBaseUrl());
        return result;
    }
    
    private String getTemplateAsString(String templateFileName) {
        try {
            // Handle the template filename as either a class path resource or an absolute path to the filesystem.
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(templateFileName);
            if (inputStream == null) {
                inputStream = new FileInputStream(templateFileName);
            }
            return IOUtils.toString(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Template file could not be found on classpath at " + templateFileName);
        }
    }
    
}
