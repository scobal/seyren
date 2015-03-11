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

import com.seyren.core.domain.Alert;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.util.config.SeyrenConfig;
import com.seyren.core.util.http.HttpHelper;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.StringWriter;
import java.util.List;

/**
 * Helper class to building the http content via template.
 *
 * @author <a href="mailto:tobias.lindenmann@1und1.de">Tobias Lindenmann</a>
 */
@Named
public class VelocityHttpHelper extends AbstractHelper implements HttpHelper {

    private final String TEMPLATE_CONTENT;

    /**
     * Loads content of configurable templated email message at creation time.
     *
     * @param seyrenConfig Used for both email template file name and the seyren URL.
     */
    @Inject
    public VelocityHttpHelper(SeyrenConfig seyrenConfig) {
        super(seyrenConfig);
        TEMPLATE_CONTENT = getTemplateAsString(seyrenConfig.getHttpTemplateFileName());
    }


    @Override
    public String createHttpContent(Check check, Subscription subscription, List<Alert> alerts) {
        VelocityContext context = createVelocityContext(check, subscription, alerts);
        StringWriter stringWriter = new StringWriter();
        Velocity.evaluate(context, stringWriter, "HttpNotificationService", TEMPLATE_CONTENT);
        return stringWriter.toString();
    }
}
