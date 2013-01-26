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

import java.util.Set;

import com.seyren.core.domain.Check;
import com.seyren.core.domain.SeyrenResponse;

public interface ChecksStore {
    
    SeyrenResponse<Check> getChecks(Boolean enabled);
    
    SeyrenResponse<Check> getChecksByState(Set<String> states, Boolean enabled);
    
    Check getCheck(String checkId);
    
    void deleteCheck(String checkId);
    
    Check createCheck(Check check);
    
    Check saveCheck(Check check);
    
}
