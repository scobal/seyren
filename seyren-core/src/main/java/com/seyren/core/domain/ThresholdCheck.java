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
    private static final String TYPE = "threshold";

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

    public String getType()
    {
        return ThresholdCheck.TYPE;
    }
}
