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
    private static final String TYPE = "outlier";

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

    public String getType()
    {
        return OutlierCheck.TYPE;
    }

    public String getAsgName()
    {
        return asgName;
    }

    public void setAsgName(String asgName)
    {
        this.asgName = asgName;
    }

    public Check withAsgName(String asgName)
    {
        setAsgName(asgName);
        return this;
    }

}
