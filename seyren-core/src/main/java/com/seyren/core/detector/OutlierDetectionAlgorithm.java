package com.seyren.core.detector;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by akharbanda on 02/09/17.
 */
public interface OutlierDetectionAlgorithm
{
    public boolean isOutlier(BigDecimal instanceValue, List<BigDecimal> clusterValues, Double relativeDiff, BigDecimal absoluteDiff);
}
