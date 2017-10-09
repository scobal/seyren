package com.seyren.core.detector.entity;

import java.math.BigDecimal;

/**
 * Created by akharbanda on 04/09/17.
 */
public class ASGDataPoint
{
    private String targetName;
    private BigDecimal value;

    public ASGDataPoint(String targetName , BigDecimal value)
    {
        this.targetName = targetName;
        this.value = value;
    }

    public String getTargetName()
    {
        return targetName;
    }

    public void setTargetName(String targetName)
    {
        this.targetName = targetName;
    }

    public BigDecimal getValue()
    {
        return value;
    }

    public void setValue(BigDecimal value)
    {
        this.value = value;
    }
}
