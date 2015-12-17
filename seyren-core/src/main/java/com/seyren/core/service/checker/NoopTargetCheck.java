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

import java.math.BigDecimal;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.seyren.core.domain.Check;

public class NoopTargetCheck implements TargetChecker {
    private BigDecimal value;

    public NoopTargetCheck(BigDecimal value) {
        this.value = value;
    }

    @Override
    public Map<String, Optional<BigDecimal>> check(Check check) throws Exception {
        return ImmutableMap.of(check.getTarget(), Optional.<BigDecimal>of(value));
    }
}