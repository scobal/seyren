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
