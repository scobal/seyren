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
package com.seyren.core.service.notification;

import com.amazonaws.services.ec2.model.DescribeAddressesRequest;
import com.amazonaws.services.ec2.model.Filter;
import com.seyren.awsmanager.AWSManager;
import com.seyren.awsmanager.entity.AWSInstanceDetail;
import com.seyren.core.domain.*;
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.util.config.SeyrenConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by akharbanda on 20/08/17.
 */
@Named
public class AWSUnhealthyInstanceNotificationService implements NotificationService
{
    private final SeyrenConfig seyrenConfig;
    private static final String IPADDRESS_PATTERN =
            "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\-){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

    private final Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
    private final AWSManager awsManager;

    @Inject
    public AWSUnhealthyInstanceNotificationService(AWSManager awsManager, SeyrenConfig seyrenConfig)
    {
        this.awsManager = awsManager;
        this.seyrenConfig = seyrenConfig;
    }

    @Override
    public void sendNotification(Check check, Subscription subscription, List<Alert> alerts) throws NotificationFailedException
    {
        if (CollectionUtils.isNotEmpty(alerts))
        {
            String asgName = subscription.getTarget();
            Iterator<Alert> iter = alerts.iterator();
            while (iter.hasNext())
            {
                if (iter.next().getToType() != AlertType.ERROR)
                {
                    iter.remove();
                }
            }
            List<String> convictedIPs = getConvictedIPs(alerts);
            if (CollectionUtils.isNotEmpty(convictedIPs))
            {
                Map<String, AWSInstanceDetail> awsInstanceDetailMap = awsManager.getInstanceDetail(convictedIPs);
                List<String> instanceIdList = filterOnAsg(awsInstanceDetailMap, asgName);
                if (CollectionUtils.isNotEmpty(instanceIdList))
                {
                    awsManager.convictInstance(instanceIdList);
                }

            }
        }

    }

    private List<String> filterOnAsg(Map<String, AWSInstanceDetail> awsInstanceDetailMap, String asgName)
    {
        List<String> instanceIdList = new ArrayList<String>();
        if (MapUtils.isNotEmpty(awsInstanceDetailMap))
        {
            for (AWSInstanceDetail awsInstanceDetail : awsInstanceDetailMap.values())
            {
                if (awsInstanceDetail != null && StringUtils.isNotEmpty(asgName) && awsInstanceDetail.getAutoScalingGroup().contains(asgName))
                {
                    instanceIdList.add(awsInstanceDetail.getInstanceId());
                }

            }
        }
        return instanceIdList;
    }

    private List<String> getConvictedIPs(List<Alert> alerts)
    {
        List<String> convictedIPList = new ArrayList<String>();
        for (Alert alert : alerts)
        {
            if (alert != null)
            {
                String target = alert.getTarget();
                Matcher matcher = pattern.matcher(target);
                if (matcher.find())
                {
                    String ip = matcher.group();
                    ip = ip.replace("-", ".");
                    convictedIPList.add(ip);
                }
            }
        }
        return convictedIPList;
    }

    private DescribeAddressesRequest buildDescribeAddressesRequest(List<String> ipAddress)
    {
        DescribeAddressesRequest describeAddressesRequest = new DescribeAddressesRequest();
        describeAddressesRequest = describeAddressesRequest.withFilters(new Filter("private-ip-address", ipAddress));

        return describeAddressesRequest;
    }

    @Override
    public boolean canHandle(SubscriptionType subscriptionType)
    {
        return subscriptionType == SubscriptionType.AWS_UNHEALTHY_INSTANCE;
    }

}
