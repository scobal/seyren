package com.seyren.core.detector.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by akharbanda on 04/09/17.
 */
public class ASGDataPoints
{
    private List<ASGDataPoint> asgDataPointList ;

    public ASGDataPoints()
    {
        asgDataPointList = new ArrayList<ASGDataPoint>();
    }

    public void addDataPoint(String targetName , BigDecimal value)
    {
        asgDataPointList.add(new ASGDataPoint(targetName,value));
    }

    public List<ASGDataPoint> getAsgDataPointList()
    {
        return asgDataPointList;
    }

}
