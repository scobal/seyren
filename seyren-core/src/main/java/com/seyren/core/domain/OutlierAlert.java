package com.seyren.core.domain;

import java.math.BigDecimal;

/**
 * Created by akharbanda on 20/08/17.
 */
public class OutlierAlert extends Alert
{
    private BigDecimal absoluteDiff;
    private Double relativeDiff;
    private Integer consecutiveAlertCount;
    private AlertType alertType;

    public BigDecimal getAbsoluteDiff() {
        return absoluteDiff;
    }

    public void setAbsoluteDiff(BigDecimal absoluteDiff) {
        this.absoluteDiff = absoluteDiff;
    }

    public OutlierAlert withAbsoluteDiff(BigDecimal absoluteDiff) {
        setAbsoluteDiff(absoluteDiff);
        return this;
    }

    public Double getRelativeDiff() {
        return relativeDiff;
    }

    public void setRelativeDiff(Double relativeDiff) {
        this.relativeDiff = relativeDiff;
    }

    public OutlierAlert withRelativeDiff(Double relativeDiff) {
        setRelativeDiff(relativeDiff);
        return this;
    }

    public Integer getConsecutiveAlertCount()
    {
        return consecutiveAlertCount;
    }

    public void setConsecutiveAlertCount(Integer consecutiveAlertCount)
    {
        this.consecutiveAlertCount = consecutiveAlertCount;
    }

    public OutlierAlert withConsecutiveAlertCount(Integer consecutiveAlertCount)
    {
        setConsecutiveAlertCount(consecutiveAlertCount);
        return this;
    }

    public AlertType getAlertType()
    {
        return alertType;
    }

    public void setAlertType(AlertType alertType)
    {
        this.alertType = alertType;
    }

    public OutlierAlert withAlertType(AlertType alertType)
    {
        setAlertType(alertType);
        return this;
    }
}
