package com.seyren.core.detector;

import com.google.common.base.Optional;
import com.seyren.awsmanager.AWSManager;
import com.seyren.awsmanager.entity.AWSInstanceDetail;
import com.seyren.core.detector.entity.TargetDataPointsEntity;
import com.seyren.core.domain.OutlierCheck;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Mockito.*;

/**
 * Created by akharbanda on 04/11/17.
 */
public class AWSOutlierDetectorTest
{
    private AWSOutlierDetector awsOutlierDetector;
    private AWSManager awsManager;

    public AWSOutlierDetectorTest()
    {
        awsManager = mock(AWSManager.class);
        awsOutlierDetector = new AWSOutlierDetector(awsManager,new MeanValueOutlierDetectorAlgorithm());
    }

    private void setupMockAWSManager()
    {
        AWSInstanceDetail awsInstanceDetail = new AWSInstanceDetail("10.0.0.1","i-11111","airpricingservice");
        AWSInstanceDetail awsInstanceDetail1 = new AWSInstanceDetail("10.0.0.2","i-22222","airpricingservice");
        AWSInstanceDetail awsInstanceDetail2 = new AWSInstanceDetail("10.0.0.3","i-33333","airpricingservice");
        AWSInstanceDetail awsInstanceDetail3 = new AWSInstanceDetail("10.0.0.4","i-4444","airpricingservice-incorrect");
        AWSInstanceDetail awsInstanceDetail4 = new AWSInstanceDetail("10.0.0.5","i-55555","airpricingservice");
        AWSInstanceDetail awsInstanceDetail5 = new AWSInstanceDetail("10.0.0.6","i-66666","airpricingservice");
        AWSInstanceDetail awsInstanceDetail6 = new AWSInstanceDetail("10.0.0.7","i-77777","airpricingservice");
        Map<String,AWSInstanceDetail> awsInstanceDetailMap = new HashMap<String, AWSInstanceDetail>();
        awsInstanceDetailMap.put("10.0.0.1",awsInstanceDetail);
        awsInstanceDetailMap.put("10.0.0.2",awsInstanceDetail1);
        awsInstanceDetailMap.put("10.0.0.3",awsInstanceDetail2);
        awsInstanceDetailMap.put("10.0.0.4",awsInstanceDetail3);
        awsInstanceDetailMap.put("10.0.0.5",awsInstanceDetail4);
        awsInstanceDetailMap.put("10.0.0.6",awsInstanceDetail5);
        awsInstanceDetailMap.put("10.0.0.7",awsInstanceDetail6);

        when(awsManager.getInstanceDetail(anyList())).thenReturn(awsInstanceDetailMap);
    }

    private String buildTargetName(String ip)
    {
        return "stats.gauges."+ip+".cpu";
    }

    private Map<String, Optional<BigDecimal>> buildSampleTargetValues()
    {
        Map<String, Optional<BigDecimal>> targetValues = new HashMap<String, Optional<BigDecimal>>();
        targetValues.put(buildTargetName("10-0-0-1"),Optional.of(new BigDecimal(90)));
        targetValues.put(buildTargetName("10-0-0-2"),Optional.of(new BigDecimal(30)));
        targetValues.put(buildTargetName("10-0-0-3"),Optional.of(new BigDecimal(30)));
        targetValues.put(buildTargetName("10-0-0-4"),Optional.of(new BigDecimal(90)));
        targetValues.put(buildTargetName("10-0-0-5"),Optional.of(new BigDecimal(30)));
        targetValues.put(buildTargetName("10-0-0-6"),Optional.of(new BigDecimal(30)));
        targetValues.put(buildTargetName("10-0-0-7"),Optional.of(new BigDecimal(30)));
        return targetValues;
    }

    @Test
    public void testBuildTargetDataPointsEntityMap()
    {
        String theMethodToTest = "buildTargetDataPointsEntityMap";
        OutlierCheck outlierCheck = new OutlierCheck();
        outlierCheck.setAsgName("airpricingservice");
        setupMockAWSManager();
        try
        {
            Map<String,TargetDataPointsEntity> targetDataPointsEntityMap = Whitebox.invokeMethod(awsOutlierDetector, theMethodToTest, buildSampleTargetValues(),outlierCheck);
            Assert.assertEquals("CurrentValue does not match",targetDataPointsEntityMap.get(buildTargetName("10-0-0-1")).getCurrentValue(),new BigDecimal(90));
            Assert.assertEquals("DataPoints does not match",targetDataPointsEntityMap.get(buildTargetName("10-0-0-1")).getDataPoints(), Arrays.asList(new BigDecimal[]{new BigDecimal(30),new BigDecimal(30),new BigDecimal(30),new BigDecimal(30),new BigDecimal(30)}));

            Assert.assertEquals("CurrentValue does not match",targetDataPointsEntityMap.get(buildTargetName("10-0-0-4")).getCurrentValue(),new BigDecimal(90));
            Assert.assertEquals("DataPoints does not match",targetDataPointsEntityMap.get(buildTargetName("10-0-0-4")).getDataPoints(), new ArrayList<BigDecimal>());

            Assert.assertEquals("CurrentValue does not match",targetDataPointsEntityMap.get(buildTargetName("10-0-0-2")).getCurrentValue(),new BigDecimal(30));
            Assert.assertEquals("DataPoints does not match",targetDataPointsEntityMap.get(buildTargetName("10-0-0-2")).getDataPoints(), Arrays.asList(new BigDecimal[]{new BigDecimal(30),new BigDecimal(90),new BigDecimal(30),new BigDecimal(30),new BigDecimal(30)}));

        }
        catch (Exception e)
        {
            Assert.fail("Exception in executing method");
        }

    }

    @Test
    public void testGetUnhealthyTargets()
    {
        OutlierCheck outlierCheck = new OutlierCheck();
        outlierCheck.setAsgName("airpricingservice");
        outlierCheck.setRelativeDiff(40.0);
        outlierCheck.setAbsoluteDiff(null);
        setupMockAWSManager();

        List<String> unhealthyTargets = awsOutlierDetector.getUnhealthyTargets(buildSampleTargetValues(), outlierCheck);
        Assert.assertEquals("Unhealthy target list size should be 1",1,unhealthyTargets.size());
        Assert.assertEquals("Unhealthy target name is incorrect","stats.gauges.10-0-0-1.cpu",unhealthyTargets.get(0));

    }

}
