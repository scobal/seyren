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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.*;

public class SeyrenConfigTest {
    
    private SeyrenConfig config;
    
    @Before
    public void before() {
        config = new SeyrenConfig();
    }
    
    // FIXME This fails when SEYREN_URL is set to something else. [WLW]
    @Test
    public void defaultBaseUrlIsCorrect() {
        if (config.isBaseUrlSetToDefault()) {
            assertThat(config.getBaseUrl(), is("http://localhost:8080/seyren"));
        }
    }
    
    // FIXME This fails when MONGO_URL is set to something else. [WLW]
    @Test
    public void defaultMongoUrlIsCorrect() {
        assertThat(config.getMongoUrl(), is("mongodb://localhost:27017/seyren"));
    }
    
    @Test
    public void defaultSmtpFromIsCorrect() {
        assertThat(config.getSmtpFrom(), is("alert@seyren"));
    }
    
    @Test
    public void defaultSmtpUsernameIsCorrect() {
        assertThat(config.getSmtpUsername(), is(""));
    }
    
    @Test
    public void defaultSmtpPasswordIsCorrect() {
        assertThat(config.getSmtpPassword(), is(""));
    }
    
    @Test
    public void defaultSmtpHostIsCorrect() {
        assertThat(config.getSmtpHost(), is("localhost"));
    }
    
    @Test
    public void defaultSmtpProtocolIsCorrect() {
        assertThat(config.getSmtpProtocol(), is("smtp"));
    }
    
    @Test
    public void defaultSmtpPortIsCorrect() {
        assertThat(config.getSmtpPort(), is(25));
    }
    
    @Test
    public void defaultHipChatAuthTokenIsCorrect() {
        assertThat(config.getHipChatAuthToken(), is(""));
    }
    
    @Test
    public void defaultHipChatUsernameIsCorrect() {
        assertThat(config.getHipChatUsername(), is("Seyren Alert"));
    }
    
    @Test
    public void defaultPagerDutyDomainIsCorrect() {
        assertThat(config.getPagerDutyDomain(), is(""));
    }
    
    @Test
    public void defaultHubotUrlIsCorrect() {
        assertThat(config.getHubotUrl(), is(""));
    }
    
    @Test
    public void defaultFlowdockExternalUsernameIsCorrect() {
        assertThat(config.getFlowdockExternalUsername(), is("Seyren"));
    }
    
    @Test
    public void defaultFlowdockTagsIsCorrect() {
        assertThat(config.getFlowdockTags(), is(""));
    }
    
    @Test
    public void defaultFlowdockEmojisIsCorrect() {
        assertThat(config.getFlowdockEmojis(), is(""));
    }
    
}
