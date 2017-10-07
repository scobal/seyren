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
package com.seyren.core.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.seyren.core.util.math.BigDecimalDeserializer;
import com.seyren.core.util.math.BigDecimalSerializer;

import java.math.BigDecimal;

/**
 * Created by akharbanda on 20/08/17.
 */
public class OutlierCheck extends Check
{
    private BigDecimal absoluteDiff ;
    private Double relativeDiff;
    private Integer minConsecutiveViolations ;
    private String asgName ;

    @JsonSerialize(using = BigDecimalSerializer.class)
    public BigDecimal getAbsoluteDiff() {
        return absoluteDiff;
    }

    @JsonDeserialize(using = BigDecimalDeserializer.class)
    public void setAbsoluteDiff(BigDecimal absoluteDiff) {
        this.absoluteDiff = absoluteDiff;
    }

    public OutlierCheck withAbsoluteDiff(BigDecimal absoluteDiff) {
        setAbsoluteDiff(absoluteDiff);
        return this;
    }

    public Double getRelativeDiff() {
        return relativeDiff;
    }

    public void setRelativeDiff(Double relativeDiff) {
        this.relativeDiff = relativeDiff;
    }

    public OutlierCheck withRelativeDiff(Double relativeDiff) {
        setRelativeDiff(relativeDiff);
        return this;
    }

    public Integer getMinConsecutiveViolations()
    {
        return minConsecutiveViolations;
    }

    public void setMinConsecutiveViolations(Integer minConsecutiveViolations)
    {
        this.minConsecutiveViolations = minConsecutiveViolations;
    }

    public OutlierCheck withMinConsecutiveViolations(Integer minConsecutiveViolations)
    {
        setMinConsecutiveViolations(minConsecutiveViolations);
        return this;
    }
}
