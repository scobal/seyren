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
package com.seyren.core.service.checker;

import com.google.common.base.Optional;
import com.seyren.core.domain.Check;
import com.seyren.core.util.http.HttpUrlFetcher;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.junit.Before;
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Map;

import static org.junit.Assert.assertEquals;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by ohad on 7/18/15.
 */
public class JenkinsTargetCheckerTest {

    private static final String JOB_NAME = "JobName";
    private static final String JENKINS_SERVER = "server:8080";
    private static final String SUCCESS_RESPONSE = "{\"actions\":[{\"causes\":[{\"shortDescription\":\"Started by timer\"}]},{\"parameters\":[{\"name\":\"EMAIL_FAILURES\",\"value\":true}]},{},{\"buildsByBranchName\":{\"refs/remotes/origin/master\":{\"buildNumber\":20576,\"buildResult\":null,\"marked\":{\"SHA1\":\"7b393d9640d9affd00e85099bffc723f4d46f262\",\"branch\":[{\"SHA1\":\"7b393d9640d9affd00e85099bffc723f4d46f262\",\"name\":\"refs/remotes/origin/master\"}]},\"revision\":{\"SHA1\":\"7b393d9640d9affd00e85099bffc723f4d46f262\",\"branch\":[{\"SHA1\":\"7b393d9640d9affd00e85099bffc723f4d46f262\",\"name\":\"refs/remotes/origin/master\"}]}}},\"lastBuiltRevision\":{\"SHA1\":\"7b393d9640d9affd00e85099bffc723f4d46f262\",\"branch\":[{\"SHA1\":\"7b393d9640d9affd00e85099bffc723f4d46f262\",\"name\":\"refs/remotes/origin/master\"}]},\"remoteUrls\":[\"git@github.com:windward-ltd/monitoring.git\"],\"scmName\":\"\"},{},{},{\"failCount\":0,\"skipCount\":1,\"totalCount\":59,\"urlName\":\"testReport\"},{},{}],\"artifacts\":[],\"building\":false,\"description\":null,\"duration\":131345,\"estimatedDuration\":149638,\"executor\":null,\"fullDisplayName\":\"monitoring_prod #20576\",\"id\":\"2015-07-19_08-12-00\",\"keepLog\":false,\"number\":20576,\"result\":\"SUCCESS\",\"timestamp\":1437293520404,\"url\":\"http://jenkins.windward.eu:8080/job/monitoring_prod/20576/\",\"builtOn\":\"slave-3\",\"changeSet\":{\"items\":[],\"kind\":\"git\"},\"culprits\":[],\"mavenArtifacts\":{},\"mavenVersionUsed\":\"3.2.1\"}";
    private static final String ERROR_RESPONSE = "{\"actions\":[{\"causes\":[{\"shortDescription\":\"Started by timer\"}]},{\"parameters\":[{\"name\":\"EMAIL_FAILURES\",\"value\":true}]},{},{\"buildsByBranchName\":{\"refs/remotes/origin/master\":{\"buildNumber\":20576,\"buildResult\":null,\"marked\":{\"SHA1\":\"7b393d9640d9affd00e85099bffc723f4d46f262\",\"branch\":[{\"SHA1\":\"7b393d9640d9affd00e85099bffc723f4d46f262\",\"name\":\"refs/remotes/origin/master\"}]},\"revision\":{\"SHA1\":\"7b393d9640d9affd00e85099bffc723f4d46f262\",\"branch\":[{\"SHA1\":\"7b393d9640d9affd00e85099bffc723f4d46f262\",\"name\":\"refs/remotes/origin/master\"}]}}},\"lastBuiltRevision\":{\"SHA1\":\"7b393d9640d9affd00e85099bffc723f4d46f262\",\"branch\":[{\"SHA1\":\"7b393d9640d9affd00e85099bffc723f4d46f262\",\"name\":\"refs/remotes/origin/master\"}]},\"remoteUrls\":[\"git@github.com:windward-ltd/monitoring.git\"],\"scmName\":\"\"},{},{},{\"failCount\":0,\"skipCount\":1,\"totalCount\":59,\"urlName\":\"testReport\"},{},{}],\"artifacts\":[],\"building\":false,\"description\":null,\"duration\":131345,\"estimatedDuration\":149638,\"executor\":null,\"fullDisplayName\":\"monitoring_prod #20576\",\"id\":\"2015-07-19_08-12-00\",\"keepLog\":false,\"number\":20576,\"result\":\"ERROR\",\"timestamp\":1437293520404,\"url\":\"http://jenkins.windward.eu:8080/job/monitoring_prod/20576/\",\"builtOn\":\"slave-3\",\"changeSet\":{\"items\":[],\"kind\":\"git\"},\"culprits\":[],\"mavenArtifacts\":{},\"mavenVersionUsed\":\"3.2.1\"}";
    private static final String UNSTABLE_RESPONSE = "{\"actions\":[{\"causes\":[{\"shortDescription\":\"Started by timer\"}]},{\"parameters\":[{\"name\":\"EMAIL_FAILURES\",\"value\":true}]},{},{\"buildsByBranchName\":{\"refs/remotes/origin/master\":{\"buildNumber\":20576,\"buildResult\":null,\"marked\":{\"SHA1\":\"7b393d9640d9affd00e85099bffc723f4d46f262\",\"branch\":[{\"SHA1\":\"7b393d9640d9affd00e85099bffc723f4d46f262\",\"name\":\"refs/remotes/origin/master\"}]},\"revision\":{\"SHA1\":\"7b393d9640d9affd00e85099bffc723f4d46f262\",\"branch\":[{\"SHA1\":\"7b393d9640d9affd00e85099bffc723f4d46f262\",\"name\":\"refs/remotes/origin/master\"}]}}},\"lastBuiltRevision\":{\"SHA1\":\"7b393d9640d9affd00e85099bffc723f4d46f262\",\"branch\":[{\"SHA1\":\"7b393d9640d9affd00e85099bffc723f4d46f262\",\"name\":\"refs/remotes/origin/master\"}]},\"remoteUrls\":[\"git@github.com:windward-ltd/monitoring.git\"],\"scmName\":\"\"},{},{},{\"failCount\":0,\"skipCount\":1,\"totalCount\":59,\"urlName\":\"testReport\"},{},{}],\"artifacts\":[],\"building\":false,\"description\":null,\"duration\":131345,\"estimatedDuration\":149638,\"executor\":null,\"fullDisplayName\":\"monitoring_prod #20576\",\"id\":\"2015-07-19_08-12-00\",\"keepLog\":false,\"number\":20576,\"result\":\"UNSTABLE\",\"timestamp\":1437293520404,\"url\":\"http://jenkins.windward.eu:8080/job/monitoring_prod/20576/\",\"builtOn\":\"slave-3\",\"changeSet\":{\"items\":[],\"kind\":\"git\"},\"culprits\":[],\"mavenArtifacts\":{},\"mavenVersionUsed\":\"3.2.1\"}";

    private Check check;
    private String jobCheckUrl;

    @Before
    public void setUp() throws Exception {
        check = mock(Check.class);
        when(check.getTarget()).thenReturn(JOB_NAME);
        this.jobCheckUrl = "http://server:8080/job/JobName/lastCompletedBuild/api/json";
    }

    @Test
    public void testSuccessfulJob() throws Exception {
        JenkinsTargetChecker jenkinsTaskChecker = init(SUCCESS_RESPONSE);
        Map<String, Optional<BigDecimal>> result = jenkinsTaskChecker.check(check);
        assertEquals("Wrong result",Optional.of(new BigDecimal(1)),result.get(JOB_NAME));
    }

    @Test
    public void testErrorJob() throws Exception {
        JenkinsTargetChecker jenkinsTaskChecker = init(ERROR_RESPONSE);
        Map<String, Optional<BigDecimal>> result = jenkinsTaskChecker.check(check);
        assertEquals("Wrong result",Optional.of(new BigDecimal(-1)),result.get(JOB_NAME));
    }

    @Test
    public void testUnstableJob() throws Exception {
        JenkinsTargetChecker jenkinsTaskChecker = init(UNSTABLE_RESPONSE);
        Map<String, Optional<BigDecimal>> result = jenkinsTaskChecker.check(check);
        assertEquals("Wrong result",Optional.of(new BigDecimal(0)),result.get(JOB_NAME));
    }

    private JenkinsTargetChecker init(String responseMsg) throws IOException {
        InputStream responseContent = new ByteArrayInputStream(responseMsg.getBytes());
        HttpEntity httpEntity = mock(HttpEntity.class);
        when(httpEntity.getContent()).thenReturn(responseContent);
        HttpResponse response = mock(HttpResponse.class);
        when(response.getEntity()).thenReturn(httpEntity);
        HttpUrlFetcher httpUrlFetcher = mock(HttpUrlFetcher.class);
        when(httpUrlFetcher.sendUrl(eq(jobCheckUrl),anyString(),anyString())).thenReturn(response);
        return new JenkinsTargetChecker(httpUrlFetcher,JENKINS_SERVER,null,null);
    }


}
