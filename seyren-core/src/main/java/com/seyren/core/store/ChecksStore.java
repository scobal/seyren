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
package com.seyren.core.store;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.joda.time.DateTime;

import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.SeyrenResponse;

public interface ChecksStore {

    /**
     * Searches the fields provided using corresponding regular expression pattern.
     *
     * @param checkFields non-null, non-empty order-significant list of fields to search for.
     * @param patterns non-null, non-empty, order-significant, same length list of regex patterns.
     * @param enabled if present, will restrict checks to the value supplied.
     *
     * @return a Seyren response containing all checks that meet the provided criteria
     */
    SeyrenResponse getChecksByPattern(List<String> checkFields, List<Pattern> patterns, Boolean enabled);

    SeyrenResponse<Check> getChecks(Boolean enabled, Boolean live);
    
    SeyrenResponse<Check> getChecksByState(Set<String> states, Boolean enabled);
    
    Check getCheck(String checkId);
    
    void deleteCheck(String checkId);
    
    Check createCheck(Check check);
    
    Check saveCheck(Check check);

    Check updateStateAndLastCheck(String checkId, AlertType state, DateTime lastCheck);
    
}
