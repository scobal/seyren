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
package com.seyren.awsmanager.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.seyren.awsmanager.entity.AWSInstanceDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.jar.Pack200;

/**
 * Created by akharbanda on 04/09/17.
 */
public class AWSInstanceDetailsCache
{
    private final LoadingCache<String,AWSInstanceDetail> awsInstanceDetailLoadingCache ;
    private final CacheLoader<String,AWSInstanceDetail> cacheLoader;
    private final CacheBuilder cacheBuilder ;
    private static final Logger LOGGER = LoggerFactory.getLogger(AWSInstanceDetailsCache.class);

    public AWSInstanceDetailsCache(CacheLoader cacheLoader , Long maxSize , Long expiryTimeoutinMillis)
    {
        this.cacheLoader = cacheLoader;
        cacheBuilder = CacheBuilder.newBuilder();
        if(maxSize!=null)
        {
            cacheBuilder.maximumSize(maxSize);
        }

        if(expiryTimeoutinMillis!=null)
        {
            cacheBuilder.expireAfterWrite(expiryTimeoutinMillis, TimeUnit.MILLISECONDS);
        }

        awsInstanceDetailLoadingCache = cacheBuilder.build(cacheLoader);
    }

    public AWSInstanceDetail getAWSInstanceDetails(String key)
    {
        AWSInstanceDetail awsInstanceDetail = null;

        try{
            awsInstanceDetail = awsInstanceDetailLoadingCache.get(key);
        }
        catch (ExecutionException ee)
        {
        }
        return awsInstanceDetail;
    }

    public void putAWSInstanceDetails(String key, AWSInstanceDetail value)
    {
        if(value!=null)
        {
            awsInstanceDetailLoadingCache.put(key,value);
        }
    }
}
