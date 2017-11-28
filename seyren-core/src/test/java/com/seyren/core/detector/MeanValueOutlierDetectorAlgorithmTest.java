package com.seyren.core.detector;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by akharbanda on 04/11/17.
 */
public class MeanValueOutlierDetectorAlgorithmTest
{
    private final MeanValueOutlierDetectorAlgorithm meanValueOutlierDetectorAlgorithm = new MeanValueOutlierDetectorAlgorithm();
    private final BigDecimal defaultInstanceValue = new BigDecimal(20.0);
    private final Double defaultRelativeDiff = 40.0;
    private final BigDecimal defaultAbsoluteDiff = BigDecimal.TEN;


    @Test
    public void testMinDataPoints()
    {
        Assert.assertFalse("Outlier should be false for 0 data points",meanValueOutlierDetectorAlgorithm.isOutlier(defaultInstanceValue,new ArrayList<BigDecimal>(),defaultRelativeDiff,defaultAbsoluteDiff));
    }

    @Test
    public void testMeanValueZeroRelativeDiff()
    {
        Assert.assertFalse("Outlier should be false when mean value is zero and only relative diff is present",meanValueOutlierDetectorAlgorithm.isOutlier(defaultInstanceValue, Arrays.asList(new BigDecimal[]{BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO}),defaultRelativeDiff,null));
    }

    @Test
    public void testMeanValueZeroAbsoluteDiff()
    {
        Assert.assertTrue("Outlier should be true when mean value is zero and absolute diff is violated",meanValueOutlierDetectorAlgorithm.isOutlier(defaultInstanceValue, Arrays.asList(new BigDecimal[]{BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO}),null,defaultAbsoluteDiff));
    }

    @Test
    public void testBothAbsoluteAndRelativeDiffNull()
    {
        Assert.assertFalse("Outlier should be false when both absolute and relative diff are null",meanValueOutlierDetectorAlgorithm.isOutlier(defaultInstanceValue, Arrays.asList(new BigDecimal[]{BigDecimal.TEN,BigDecimal.TEN,BigDecimal.TEN,BigDecimal.TEN,BigDecimal.TEN}),null,null));
    }

    @Test
    public void testRelativeDiffOutlier()
    {
        Assert.assertTrue("Outlier should be true when relative diff is violated",meanValueOutlierDetectorAlgorithm.isOutlier(new BigDecimal(20), Arrays.asList(new BigDecimal[]{BigDecimal.TEN,BigDecimal.TEN,BigDecimal.TEN,BigDecimal.TEN,BigDecimal.TEN}),50.0,null));
    }

    @Test
    public void testAbsoluteDiffOutlier()
    {
        Assert.assertTrue("Outlier should be true when absolute diff is violated",meanValueOutlierDetectorAlgorithm.isOutlier(new BigDecimal(20), Arrays.asList(new BigDecimal[]{BigDecimal.TEN,BigDecimal.TEN,BigDecimal.TEN,BigDecimal.TEN,BigDecimal.TEN}),null,new BigDecimal(9)));
    }

    @Test
    public void testAbsoluteDiffOutlierRelativeDiffGood()
    {
        Assert.assertTrue("Outlier should be true when absolute diff is violated even when relative diff is good",meanValueOutlierDetectorAlgorithm.isOutlier(new BigDecimal(20), Arrays.asList(new BigDecimal[]{BigDecimal.TEN,BigDecimal.TEN,BigDecimal.TEN,BigDecimal.TEN,BigDecimal.TEN}),100.0,new BigDecimal(9)));
    }

    @Test
    public void testNegativeRelativeDiffOutlier()
    {
        Assert.assertTrue("Outlier should be true when negative relative diff is violated",meanValueOutlierDetectorAlgorithm.isOutlier(new BigDecimal(3), Arrays.asList(new BigDecimal[]{BigDecimal.TEN,BigDecimal.TEN,BigDecimal.TEN,BigDecimal.TEN,BigDecimal.TEN}),-50.0,null));
    }

    @Test
    public void testNegativeAbsoluteDiffOutlier()
    {
        Assert.assertTrue("Outlier should be true when negative absolute diff is violated",meanValueOutlierDetectorAlgorithm.isOutlier(new BigDecimal(4), Arrays.asList(new BigDecimal[]{BigDecimal.TEN,BigDecimal.TEN,BigDecimal.TEN,BigDecimal.TEN,BigDecimal.TEN}),null,new BigDecimal(-5)));
    }


}

