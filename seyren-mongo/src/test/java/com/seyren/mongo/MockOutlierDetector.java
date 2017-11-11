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

import com.google.common.base.Optional;
import com.seyren.core.detector.AbstractOutlierDetector;
import com.seyren.core.detector.MeanValueOutlierDetectorAlgorithm;
import com.seyren.core.domain.OutlierCheck;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by akharbanda on 09/11/17.
 */
public class MockOutlierDetector extends AbstractOutlierDetector
{
    private List<String> unhealthyTargets ;

    public MockOutlierDetector(List<String> unhealthyTargets)
    {
        super(new MeanValueOutlierDetectorAlgorithm());
        this.unhealthyTargets = unhealthyTargets;
    }

    @Override
    public List<String> getUnhealthyTargets(Map<String, Optional<BigDecimal>> targetValues, OutlierCheck check)
    {
        return this.unhealthyTargets;
    }

    public void setUnhealthyTargets(List<String> unhealthyTargets)
    {
        this.unhealthyTargets = unhealthyTargets;
    }
}
