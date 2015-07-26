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
import org.apache.http.HttpResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ohad on 7/17/15.
 */
public class HttpTargetChecker implements TargetChecker{

    private HttpUrlFetcher urlFetcher;

    public HttpTargetChecker(HttpUrlFetcher urlFetcher) {
        this.urlFetcher = urlFetcher;
    }

    @Override
    public Map<String, Optional<BigDecimal>> check(Check check) throws Exception {

        Map<String, Optional<BigDecimal>> targetValues = new HashMap<String, Optional<BigDecimal>>();
        String url = check.getTarget();
        BigDecimal statusCode = null;
        try{
            HttpResponse response = urlFetcher.sendUrl(url);
            statusCode = new BigDecimal(response.getStatusLine().getStatusCode());
        }catch (Exception e){
            statusCode = new BigDecimal(404);
        }

        targetValues.put(url,Optional.of(statusCode));
        return targetValues;
    }
}
