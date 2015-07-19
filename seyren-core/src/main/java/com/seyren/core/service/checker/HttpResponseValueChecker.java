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

import com.seyren.core.domain.AlertType;

import java.math.BigDecimal;

/**
 * Created by ohad on 7/17/15.
 */
public class HttpResponseValueChecker implements ValueChecker {

    private static final BigDecimal RESPONSE_OK = new BigDecimal(200);

    @Override
    public AlertType checkValue(BigDecimal value, BigDecimal warn, BigDecimal error) {
        if (value.equals(RESPONSE_OK)){
            return AlertType.OK;
        }

        return AlertType.ERROR;
    }
}
