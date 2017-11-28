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

import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by akharbanda on 10/11/17.
 */
public class AWSManagerTest
{
    private final AWSManager awsManager ;
    private final AmazonEC2Client amazonEC2Client = Mockito.mock(AmazonEC2Client.class);
    private final AmazonAutoScalingClient amazonAutoScalingClient = Mockito.mock(AmazonAutoScalingClient.class);

    public AWSManagerTest()
    {
        awsManager = new AWSManager(amazonEC2Client,amazonAutoScalingClient);
    }

    @Test
    public void testMaxFilterListNotMultiple200Size()
    {
        String baseIp = "10.9.8.";
        List<String> ipAddressList = new ArrayList<String>();

        for(Integer i =0 ; i < 450 ; i++)
        {
            String ip = baseIp + i.toString();
            ipAddressList.add(ip);
        }

        awsManager.getInstanceDetail(ipAddressList);
        Mockito.verify(amazonEC2Client, Mockito.times(3)).describeInstances(Matchers.any(DescribeInstancesRequest.class));
    }

    @Test
    public void testMaxFilterListMultiple200Size()
    {
        String baseIp = "10.9.8.";
        List<String> ipAddressList = new ArrayList<String>();

        for(Integer i =0 ; i < 400 ; i++)
        {
            String ip = baseIp + i.toString();
            ipAddressList.add(ip);
        }

        awsManager.getInstanceDetail(ipAddressList);
        Mockito.verify(amazonEC2Client, Mockito.times(2)).describeInstances(Matchers.any(DescribeInstancesRequest.class));
    }

    @Test
    public void testMaxFilterListLessThan200Size()
    {
        String baseIp = "10.9.8.";
        List<String> ipAddressList = new ArrayList<String>();

        for(Integer i =0 ; i < 100 ; i++)
        {
            String ip = baseIp + i.toString();
            ipAddressList.add(ip);
        }

        awsManager.getInstanceDetail(ipAddressList);
        Mockito.verify(amazonEC2Client, Mockito.times(1)).describeInstances(Matchers.any(DescribeInstancesRequest.class));
    }
}
