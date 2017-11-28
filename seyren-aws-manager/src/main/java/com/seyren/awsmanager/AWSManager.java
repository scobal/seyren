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
package com.seyren.awsmanager;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.AutoScalingInstanceDetails;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingInstancesRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingInstancesResult;
import com.amazonaws.services.autoscaling.model.SetInstanceHealthRequest;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import com.google.common.cache.CacheLoader;
import com.seyren.awsmanager.cache.AWSInstanceDetailsCache;
import org.apache.commons.collections4.CollectionUtils;
import com.seyren.awsmanager.entity.AWSInstanceDetail;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by akharbanda on 30/08/17.
 */

@Named
public class AWSManager
{
    private final AmazonEC2Client amazonEC2Client;
    private final AmazonAutoScalingClient amazonAutoScalingClient;
    private static final String IPADDRESS_PATTERN =
            "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\-){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

    private final Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
    private final AWSInstanceDetailsCache awsInstanceDetailsCache;
    private final static Long CACHE_MAX_SIZE = 1000l;
    private final static Long CACHE_EXPIRY_IN_MILLIS = 10 * 60 * 1000l; //10 mins
    private static final Logger LOGGER = LoggerFactory.getLogger(AWSManager.class);
    private static final int MAX_FILTER_LIST_SIZE = 200; //AWS throws error for larger lists
    private static final String DEFAULT_AWS_REGION = "us-west-2";

    @Inject
    public AWSManager(AmazonEC2Client amazonEC2Client, AmazonAutoScalingClient amazonAutoScalingClient)
    {
        this.amazonEC2Client = amazonEC2Client;
        this.amazonAutoScalingClient = amazonAutoScalingClient;
        Region region = Region.getRegion(Regions.fromName(DEFAULT_AWS_REGION));
        amazonEC2Client.setEndpoint(region.getServiceEndpoint("ec2"));
        amazonAutoScalingClient.setEndpoint(region.getServiceEndpoint("autoscaling"));
        CacheLoader cacheLoader = new CacheLoader<String, AWSInstanceDetail>()
        {
            @Override
            public AWSInstanceDetail load(String key) throws Exception
            {
                throw new Exception("No entry in cache"); // throw exception because we want to send one batch request to AWS for instance details
            }
        };
        awsInstanceDetailsCache = new AWSInstanceDetailsCache(cacheLoader, CACHE_MAX_SIZE, CACHE_EXPIRY_IN_MILLIS);
    }

    public Map<String, AWSInstanceDetail> getInstanceDetail(List<String> ipAddressList)
    {
        Map<String, AWSInstanceDetail> awsInstanceDetailMap = new HashMap<String, AWSInstanceDetail>();

        if (CollectionUtils.isNotEmpty(ipAddressList))
        {
            Map<String, String> instanceIdToIPAddressMap = new HashMap<String, String>();
            List<String> ipAddressNotInCacheList = buildMapFromCache(awsInstanceDetailMap, ipAddressList);

            if (CollectionUtils.isNotEmpty(ipAddressNotInCacheList))
            {
                int size = ipAddressNotInCacheList.size();

                int batches = size / MAX_FILTER_LIST_SIZE;

                for (int i = 0; i <= batches; i++)
                {

                    int startIndex = i * MAX_FILTER_LIST_SIZE;
                    int endIndex = startIndex + MAX_FILTER_LIST_SIZE;

                    if (i == batches)
                    {
                        endIndex = size;
                    }


                    List<String> ipAddressNotInCacheBatch = ipAddressNotInCacheList.subList(startIndex, endIndex);
                    if (CollectionUtils.isNotEmpty(ipAddressNotInCacheBatch))
                    {
                        DescribeInstancesResult describeInstancesResult = amazonEC2Client.describeInstances(buildDescribeInstanceRequest(ipAddressNotInCacheBatch));
                        if (describeInstancesResult != null)
                        {
                            List<Reservation> reservationList = describeInstancesResult.getReservations();

                            if (CollectionUtils.isNotEmpty(reservationList))
                            {
                                for (String ipAddress : ipAddressNotInCacheBatch)
                                {
                                    boolean found = false;
                                    for (Reservation reservation : reservationList)
                                    {
                                        if (reservation != null && CollectionUtils.isNotEmpty(reservation.getInstances()))
                                        {
                                            List<Instance> instances = reservation.getInstances();
                                            for (Instance instance : instances)
                                            {
                                                if (instance != null)
                                                {
                                                    if (instance.getPrivateIpAddress().equals(ipAddress))
                                                    {
                                                        instanceIdToIPAddressMap.put(instance.getInstanceId(), ipAddress);
                                                        found = true;
                                                        break;
                                                    }
                                                }
                                            }
                                            if (found)
                                            {
                                                break;
                                            }
                                        }

                                    }
                                }

                            }
                        }
                    }

                }

                if (MapUtils.isNotEmpty(instanceIdToIPAddressMap))
                {
                    DescribeAutoScalingInstancesResult describeAutoScalingInstancesResult = amazonAutoScalingClient.describeAutoScalingInstances(buildDescribeAutoScalingInstancesRequest(new ArrayList<String>(instanceIdToIPAddressMap.keySet())));
                    if (describeAutoScalingInstancesResult != null && CollectionUtils.isNotEmpty(describeAutoScalingInstancesResult.getAutoScalingInstances()))
                    {
                        for (AutoScalingInstanceDetails autoScalingInstanceDetails : describeAutoScalingInstancesResult.getAutoScalingInstances())
                        {
                            if (autoScalingInstanceDetails != null && autoScalingInstanceDetails.getInstanceId() != null)
                            {
                                String privateIp = instanceIdToIPAddressMap.get(autoScalingInstanceDetails.getInstanceId());
                                if (privateIp != null)
                                {
                                    AWSInstanceDetail awsInstanceDetail = new AWSInstanceDetail(privateIp, autoScalingInstanceDetails.getInstanceId(), autoScalingInstanceDetails.getAutoScalingGroupName());
                                    awsInstanceDetailMap.put(privateIp, awsInstanceDetail);
                                }

                            }
                        }
                    }
                }
            }
        }
        updateCacheFromMap(awsInstanceDetailMap);
        return awsInstanceDetailMap;
    }

    public void convictInstance(List<String> instanceIdList)
    {
        if (CollectionUtils.isNotEmpty(instanceIdList))
        {
            for (String instanceId : instanceIdList)
            {
                try
                {
                    amazonAutoScalingClient.setInstanceHealth(new SetInstanceHealthRequest().withHealthStatus("Unhealthy").withInstanceId(instanceId));
                }

                catch (Exception e)
                {
                    LOGGER.error(String.format("Error while setting instance state for %s to unhealthy", instanceId), e);
                }
            }
        }
    }

    private List<String> buildMapFromCache(Map<String, AWSInstanceDetail> awsInstanceDetailMap, List<String> ipAddressList)
    {
        List<String> ipAddressNotInCacheList = new ArrayList<String>();
        for (String ipAddress : ipAddressList)
        {
            AWSInstanceDetail awsInstanceDetail = awsInstanceDetailsCache.getAWSInstanceDetails(ipAddress);
            if (awsInstanceDetail != null)
            {
                awsInstanceDetailMap.put(ipAddress, awsInstanceDetail);
            }
            else
            {
                ipAddressNotInCacheList.add(ipAddress);
            }
        }
        return ipAddressNotInCacheList;
    }

    private void updateCacheFromMap(Map<String, AWSInstanceDetail> awsInstanceDetailMap)
    {
        for (Map.Entry<String, AWSInstanceDetail> entry : awsInstanceDetailMap.entrySet())
        {
            awsInstanceDetailsCache.putAWSInstanceDetails(entry.getKey(), entry.getValue());
        }
    }

    private DescribeInstancesRequest buildDescribeInstanceRequest(List<String> ipAddress)
    {
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
        describeInstancesRequest = describeInstancesRequest.withFilters(new Filter("private-ip-address", ipAddress));

        return describeInstancesRequest;
    }

    private DescribeAutoScalingInstancesRequest buildDescribeAutoScalingInstancesRequest(List<String> instanceIdList)
    {
        DescribeAutoScalingInstancesRequest describeAutoScalingInstancesRequest = null;
        if (CollectionUtils.isNotEmpty(instanceIdList))
        {
            describeAutoScalingInstancesRequest = new DescribeAutoScalingInstancesRequest();
            describeAutoScalingInstancesRequest.setInstanceIds(instanceIdList);
        }

        return describeAutoScalingInstancesRequest;
    }
}
