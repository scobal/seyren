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
package com.seyren.mongo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.seyren.core.domain.Check;
import com.seyren.core.service.checker.TargetChecker;

public class MockTargetChecker implements TargetChecker {

	private List<BigDecimal> values = new ArrayList<BigDecimal>();
	
	private int currentIndex = 0;
	
	public void addValue (BigDecimal value){
		values.add(value);
	}
	
	public void setValues(List<BigDecimal> values){
		this.values = values;
	}
	
	@Override
	public Map<String, Optional<BigDecimal>> check(Check check) throws Exception {
		if (currentIndex < values.size()){
			BigDecimal value = values.get(currentIndex);
			currentIndex++;
			return ImmutableMap.of(check.getTarget(), Optional.<BigDecimal>of(value));
		}
		return null;
	}

}
