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
package com.seyren.awsmanager.entity;

/**
 * Created by akharbanda on 31/08/17.
 */
public class AWSInstanceDetail
{
    private final String instancePrivateIP;
    private final String instanceId ;
    private final String autoScalingGroup ;

    public AWSInstanceDetail(String instancePrivateIP, String instanceId, String autoScalingGroup)
    {
        this.instancePrivateIP = instancePrivateIP;
        this.instanceId = instanceId;
        this.autoScalingGroup = autoScalingGroup;
    }

    public String getInstancePrivateIP()
    {
        return instancePrivateIP;
    }

    public String getInstanceId()
    {
        return instanceId;
    }

    public String getAutoScalingGroup()
    {
        return autoScalingGroup;
    }
}
