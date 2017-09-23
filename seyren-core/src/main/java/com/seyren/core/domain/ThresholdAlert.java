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

import java.math.BigDecimal;

/**
 * Created by akharbanda on 20/08/17.
 */
public class ThresholdAlert extends Alert
{
    private BigDecimal warn;
    private BigDecimal error;
    private AlertType fromType;
    private AlertType toType;

    public BigDecimal getWarn() {
        return warn;
    }

    public void setWarn(BigDecimal warn) {
        this.warn = warn;
    }

    public ThresholdAlert withWarn(BigDecimal warn) {
        setWarn(warn);
        return this;
    }

    public BigDecimal getError() {
        return error;
    }

    public void setError(BigDecimal error) {
        this.error = error;
    }

    public ThresholdAlert withError(BigDecimal error) {
        setError(error);
        return this;
    }

    public AlertType getFromType() {
        return fromType;
    }

    public void setFromType(AlertType fromType) {
        this.fromType = fromType;
    }

    public ThresholdAlert withFromType(AlertType fromType) {
        setFromType(fromType);
        return this;
    }

    public AlertType getToType() {
        return toType;
    }

    public void setToType(AlertType toType) {
        this.toType = toType;
    }

    public ThresholdAlert withToType(AlertType toType) {
        setToType(toType);
        return this;
    }
}
