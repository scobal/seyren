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
public class ThresholdCheck extends Check
{
    private BigDecimal warn;
    private BigDecimal error;

    @JsonSerialize(using = BigDecimalSerializer.class)
    public BigDecimal getWarn() {
        return warn;
    }

    @JsonDeserialize(using = BigDecimalDeserializer.class)
    public void setWarn(BigDecimal warn) {
        this.warn = warn;
    }

    public ThresholdCheck withWarn(BigDecimal warn) {
        setWarn(warn);
        return this;
    }

    @JsonSerialize(using = BigDecimalSerializer.class)
    public BigDecimal getError() {
        return error;
    }

    @JsonDeserialize(using = BigDecimalDeserializer.class)
    public void setError(BigDecimal error) {
        this.error = error;
    }

    public ThresholdCheck withError(BigDecimal error) {
        setError(error);
        return this;
    }

}
