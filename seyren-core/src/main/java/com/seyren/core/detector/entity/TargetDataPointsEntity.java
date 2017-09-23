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
package com.seyren.core.detector.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by akharbanda on 04/09/17.
 */
public class TargetDataPointsEntity
{
    private List<BigDecimal> dataPoints = new ArrayList<BigDecimal>();
    private BigDecimal currentValue ;

    public TargetDataPointsEntity()
    {}

    public List<BigDecimal> getDataPoints()
    {
        return dataPoints;
    }

    public void setDataPoints(List<BigDecimal> dataPoints)
    {
        this.dataPoints = dataPoints;
    }

    public BigDecimal getCurrentValue()
    {
        return currentValue;
    }

    public void setCurrentValue(BigDecimal currentValue)
    {
        this.currentValue = currentValue;
    }

    public void addDataPoint(BigDecimal dataPoint)
    {
        dataPoints.add(dataPoint);
    }

    public BigDecimal getMean()
    {
        BigDecimal mean = new BigDecimal(0);
        BigDecimal sum = new BigDecimal(0);
        Long numOfEvents = 0l;
        if(dataPoints.size()>0)
        {
            for(BigDecimal bigDecimal : dataPoints)
            {
                sum = sum.add(bigDecimal);
            }
            mean = sum.divide(new BigDecimal(numOfEvents), 20, RoundingMode.HALF_EVEN);
        }
       return mean;
    }
}
