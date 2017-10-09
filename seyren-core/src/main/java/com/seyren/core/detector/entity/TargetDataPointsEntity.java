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
