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
