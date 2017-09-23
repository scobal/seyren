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
package com.seyren.core.detector;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by akharbanda on 02/09/17.
 */
public interface OutlierDetectionAlgorithm
{
    public boolean isOutlier(BigDecimal instanceValue, List<BigDecimal> clusterValues, Double relativeDiff, BigDecimal absoluteDiff);
}
