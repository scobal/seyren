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
package com.seyren.core.util.config;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.lang.reflect.Field;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.log.Log;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import com.seyren.core.util.velocity.Slf4jLogChute;

public class SeyrenConfigTest {
    
    @Test
    public void test_default_seyren_config() throws IllegalAccessException {
        SeyrenConfig config = new SeyrenConfig();
        
        // Base
        assertThat(config.getBaseUrl(), is("http://localhost:8080/seyren"));
        assertThat(config.getMongoUrl(), is("mongodb://localhost:27017/seyren"));
        
        // Graphite
        assertThat(config.getGraphiteUrl(), is("http://localhost:80"));
        assertThat(config.getGraphiteUsername(), is(""));
        assertThat(config.getGraphitePassword(), is(""));
        assertThat(config.getGraphiteScheme(), is("http"));
        assertThat(config.getGraphiteHost(), is("localhost:80"));
        assertThat(config.getGraphitePath(), is(""));
        assertThat(config.getGraphiteKeyStore(), is(""));
        assertThat(config.getGraphiteKeyStorePassword(), is(""));
        assertThat(config.getGraphiteTrustStore(), is(""));
        
        // SMTP
        assertThat(config.getSmtpFrom(), is("alert@seyren"));
        assertThat(config.getSmtpUsername(), is(""));
        assertThat(config.getSmtpPassword(), is(""));
        assertThat(config.getSmtpHost(), is("localhost"));
        assertThat(config.getSmtpProtocol(), is("smtp"));
        assertThat(config.getSmtpPort(), is(25));
        
        // HipChat
        assertThat(config.getHipChatAuthToken(), is(""));
        assertThat(config.getHipChatUsername(), is("Seyren Alert"));
        
        // PagerDuty
        assertThat(config.getPagerDutyDomain(), is(""));
        
        // Hubot
        assertThat(config.getHubotUrl(), is(""));
        
        // Flowdock
        assertThat(config.getFlowdockExternalUsername(), is("Seyren"));
        assertThat(config.getFlowdockTags(), is(""));
        assertThat(config.getFlowdockEmojis(), is(""));

        //Velocity logging
        config.init();
        Field chuteField = ReflectionUtils.findField(Log.class, "chute");
        chuteField.setAccessible(true);
        assertThat(chuteField.get(Velocity.getLog()) , is(instanceOf(Slf4jLogChute.class)));
    }
    
}
