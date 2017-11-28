package com.seyren.core.detector;

import org.apache.commons.collections4.CollectionUtils;

import javax.inject.Named;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Created by akharbanda on 11/09/17.
 */

@Named
public class MeanValueOutlierDetectorAlgorithm implements OutlierDetectionAlgorithm
{

    private final static Integer MIN_DATA_POINTS = 5;

    public MeanValueOutlierDetectorAlgorithm()
    {
    }

    @Override
    public boolean isOutlier(BigDecimal instanceValue, List<BigDecimal> clusterValues, Double relativeDiff, BigDecimal absoluteDiff)
    {
        Boolean isOutlier = false;

        if (clusterValues.size() >= MIN_DATA_POINTS)
        {
            BigDecimal comparisonMeanValue = computeMeanValue(clusterValues);

            if (relativeDiff != null && comparisonMeanValue.compareTo(BigDecimal.ZERO) != 0)
            {
                BigDecimal computedRelativeDiff = instanceValue.subtract(comparisonMeanValue).divide(comparisonMeanValue, 20, RoundingMode.HALF_EVEN).multiply(new BigDecimal(100));

                if (relativeDiff > 0 && computedRelativeDiff.compareTo(new BigDecimal(relativeDiff)) > 0)
                {
                    isOutlier = true;
                }

                else if (relativeDiff < 0 && computedRelativeDiff.compareTo(new BigDecimal(relativeDiff)) < 0)
                {
                    isOutlier = true;
                }
            }
            if (absoluteDiff != null)
            {
                BigDecimal computedAbsoluteDiff = instanceValue.subtract(comparisonMeanValue);
                if (absoluteDiff.compareTo(BigDecimal.ZERO) > 0 && computedAbsoluteDiff.compareTo(absoluteDiff) > 0)
                {
                    isOutlier = true;
                }
                else if (absoluteDiff.compareTo(BigDecimal.ZERO) < 0 && computedAbsoluteDiff.compareTo(absoluteDiff) < 0)
                {
                    isOutlier = true;
                }
            }
        }

        return isOutlier;
    }

    private BigDecimal computeMeanValue(List<BigDecimal> clusterValues)
    {
        BigDecimal meanValue = new BigDecimal(0);
        if (CollectionUtils.isNotEmpty(clusterValues))
        {
            BigDecimal sum = new BigDecimal(0);

            for (BigDecimal value : clusterValues)
            {
                sum = sum.add(value);
            }

            meanValue = sum.divide(new BigDecimal(clusterValues.size()), 20, RoundingMode.HALF_EVEN);
        }
        return meanValue;
    }
}
