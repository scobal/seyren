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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.seyren.core.domain.Check;
import com.seyren.core.util.http.HttpUrlFetcher;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ohad on 7/18/15.
 */
public class JenkinsTargetChecker implements TargetChecker{

    private static final Logger LOGGER = LoggerFactory.getLogger(JenkinsTargetChecker.class);
    private static final String JENKINS_JOB_DETAILS_URL = "http://%s/job/%s/lastCompletedBuild/api/json";
    private final HttpUrlFetcher httpUrlFetcher;
    private final String username;
    private final String password;
    private final ObjectMapper objectMapper;
    private final String jenkinsServer;

    public JenkinsTargetChecker(HttpUrlFetcher httpUrlFetcher,String jenkinsServer,String username,String password) {
        this.httpUrlFetcher = httpUrlFetcher;
        this.jenkinsServer = jenkinsServer;
        this.username = username;
        this.password = password;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Map<String, Optional<BigDecimal>> check(Check check) throws Exception {

        Map<String, Optional<BigDecimal>> targetValues = new HashMap<String, Optional<BigDecimal>>();
        String url = String.format(JENKINS_JOB_DETAILS_URL,jenkinsServer,check.getTarget());
        LOGGER.debug("Check jenkins job: {}",url);
        HttpResponse response = httpUrlFetcher.sendUrl(url,username,password);
        JenkinsJobStatus jobStatus = objectMapper.readValue(response.getEntity().getContent(),JenkinsJobStatus.class);
        BigDecimal jobResult = new BigDecimal(jobStatus.getJobStatus());
        targetValues.put(check.getTarget(),Optional.of(jobResult));
        return targetValues;
    }
}
