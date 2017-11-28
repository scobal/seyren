/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seyren.core.detector;

import com.google.common.base.Optional;
import com.seyren.awsmanager.AWSManager;
import com.seyren.awsmanager.entity.AWSInstanceDetail;
import com.seyren.core.detector.entity.ASGDataPoint;
import com.seyren.core.detector.entity.ASGDataPoints;
import com.seyren.core.detector.entity.TargetDataPointsEntity;
import com.seyren.core.domain.OutlierCheck;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by akharbanda on 02/09/17.
 */

@Named
public class AWSOutlierDetector extends AbstractOutlierDetector
{

    private final AWSManager awsManager;

    private static final String IPADDRESS_PATTERN =
            "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\-){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

    private final Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);

    @Inject
    public AWSOutlierDetector(AWSManager awsManager, OutlierDetectionAlgorithm outlierDetectionAlgorithm)
    {
        super(outlierDetectionAlgorithm);
        this.awsManager = awsManager;
    }

    @Override
    public List<String> getUnhealthyTargets(Map<String, Optional<BigDecimal>> targetValues, OutlierCheck check)
    {
        List<String> unHealthyTargets = new ArrayList<String>();

        Map<String, TargetDataPointsEntity> targetDataPointsEntityMap = buildTargetDataPointsEntityMap(targetValues, check);
        if (MapUtils.isNotEmpty(targetDataPointsEntityMap))
        {
            for (Map.Entry<String, TargetDataPointsEntity> entry : targetDataPointsEntityMap.entrySet())
            {
                if (entry.getValue() != null && outlierDetectionAlgorithm.isOutlier(entry.getValue().getCurrentValue(), entry.getValue().getDataPoints(), check.getRelativeDiff(), check.getAbsoluteDiff()))
                {
                    unHealthyTargets.add(entry.getKey());
                }
            }
        }

        return unHealthyTargets;
    }

    //Returns target name to TargetDataPointsEntity Map
    //TargetDataPointsEntity has the Metric value for the instance and list of Metrics for all the other instances in its ASG
    private Map<String, TargetDataPointsEntity> buildTargetDataPointsEntityMap(Map<String, Optional<BigDecimal>> targetValues, OutlierCheck outlierCheck)
    {
        //Map contains the ASG name and all the ASGDataPoint in that ASG
        Map<String, ASGDataPoints> asgNameToDataPointsMap = new HashMap<String, ASGDataPoints>();
        Map<String, String> targetToAsgNameMap = buildTargetToAsgNameMap(new ArrayList<String>(targetValues.keySet()), outlierCheck);


        if (MapUtils.isNotEmpty(targetToAsgNameMap))
        {
            for (Map.Entry<String, String> entry : targetToAsgNameMap.entrySet())
            {
                ASGDataPoints asgDataPoints = asgNameToDataPointsMap.get(entry.getValue());
                if (asgDataPoints != null)
                {
                    asgDataPoints.addDataPoint(entry.getKey(), targetValues.get(entry.getKey()) != null ? targetValues.get(entry.getKey()).get() : null);
                }

                else
                {
                    asgDataPoints = new ASGDataPoints();
                    asgDataPoints.addDataPoint(entry.getKey(), targetValues.get(entry.getKey()) != null ? targetValues.get(entry.getKey()).get() : null);
                    asgNameToDataPointsMap.put(entry.getValue(), asgDataPoints);
                }
            }
        }

        return buildTargetDataPointsEntityMapFromAsgDataPointMap(asgNameToDataPointsMap);

    }

    // Map contains the target name and the corresponding ASG name
    private Map<String, String> buildTargetToAsgNameMap(List<String> targetNames, OutlierCheck outlierCheck)
    {
        Map<String, String> targetAsgNameMap = new HashMap<String, String>();
        Map<String, String> targetNameToIpAddressMap = new HashMap<String, String>();
        if (CollectionUtils.isNotEmpty(targetNames))
        {
            for (String targetName : targetNames)
            {
                String ipAddress = parseIp(targetName);
                if (ipAddress != null)
                {
                    targetNameToIpAddressMap.put(targetName, ipAddress);
                }
            }

            Map<String, AWSInstanceDetail> awsInstanceDetailMap = awsManager.getInstanceDetail(new ArrayList<String>(targetNameToIpAddressMap.values()));

            String targetAsgName = outlierCheck.getAsgName();
            if (StringUtils.isNotEmpty(targetAsgName))
            {
                for (String targetName : targetNames)
                {
                    if (targetNameToIpAddressMap.containsKey(targetName))
                    {
                        String ipAddress = targetNameToIpAddressMap.get(targetName);
                        AWSInstanceDetail awsInstanceDetail = awsInstanceDetailMap.get(ipAddress);

                        if (awsInstanceDetail != null && awsInstanceDetail.getAutoScalingGroup() != null && awsInstanceDetail.getAutoScalingGroup().contains(targetAsgName))
                        {
                            targetAsgNameMap.put(targetName, awsInstanceDetail.getAutoScalingGroup());
                        }
                    }
                }
            }
        }
        return targetAsgNameMap;
    }

    private Map<String, TargetDataPointsEntity> buildTargetDataPointsEntityMapFromAsgDataPointMap(Map<String, ASGDataPoints> asgNameToDataPointsMap)
    {
        Map<String, TargetDataPointsEntity> targetNameToDataPointsEntityMap = new HashMap<String, TargetDataPointsEntity>();

        if (MapUtils.isNotEmpty(asgNameToDataPointsMap))
        {
            for (Map.Entry<String, ASGDataPoints> entry : asgNameToDataPointsMap.entrySet())
            {
                if (entry.getValue() != null && CollectionUtils.isNotEmpty(entry.getValue().getAsgDataPointList()))
                {
                    List<ASGDataPoint> asgDataPointList = entry.getValue().getAsgDataPointList();

                    for (Integer index = 0; index < asgDataPointList.size(); index++)
                    {
                        TargetDataPointsEntity targetDataPointsEntity = new TargetDataPointsEntity();
                        targetDataPointsEntity.setCurrentValue(asgDataPointList.get(index).getValue());
                        targetNameToDataPointsEntityMap.put(asgDataPointList.get(index).getTargetName(), targetDataPointsEntity);

                        for (Integer innerIndex = 0; innerIndex < asgDataPointList.size(); innerIndex++)
                        {
                            if (innerIndex != index)
                            {
                                targetDataPointsEntity.addDataPoint(asgDataPointList.get(innerIndex).getValue());
                            }
                        }
                    }
                }
            }
        }
        return targetNameToDataPointsEntityMap;
    }

    private String parseIp(String target)
    {
        String ip = null;
        Matcher matcher = pattern.matcher(target);
        if (matcher.find())
        {
            ip = matcher.group();
            ip = ip.replace("-", ".");

        }
        return ip;
    }
}
