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
package com.seyren.core.util.velocity;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Redirects Velocity's LogChute messages to SLF4J.
 * 
 * <p>
 * To use, first set up SLF4J, then tell Velocity to use this class for logging by adding the following to your velocity.properties:
 * 
 * <code>
 * runtime.log.logsystem.class = org.apache.velocity.slf4j.Slf4jLogChute
 * </code>
 * </p>
 * 
 * <p>
 * You may also set this property to specify what log/name Velocity's messages should be logged to (example below is default). <code>
 * runtime.log.logsystem.slf4j.name = org.apache.velocity
 * </code>
 * </p>
 * 
 * @since 2.0
 * @version $Id$
 */
public class Slf4jLogChute implements LogChute
{
    
    /** Property key for specifying the name for the log instance */
    public static final String LOGCHUTE_SLF4J_NAME =
            "runtime.log.logsystem.slf4j.name";
    
    /** Default name for the commons-logging instance */
    public static final String DEFAULT_LOG_NAME = "org.apache.velocity";
    
    /** the commons-logging Log instance */
    protected Logger log;
    
    /********** LogChute methods *************/
    
    public void init(RuntimeServices rs)
    {
        String name =
                (String) rs.getProperty(LOGCHUTE_SLF4J_NAME);
        
        if (name == null)
        {
            name = DEFAULT_LOG_NAME;
        }
        log = LoggerFactory.getLogger(name);
        log(LogChute.DEBUG_ID, "Slf4jLogChute name is '" + name + "'");
    }
    
    /**
     * Send a log message from Velocity.
     */
    public void log(int level, String message)
    {
        switch (level)
        {
        case LogChute.WARN_ID:
            log.warn(message);
            break;
        case LogChute.INFO_ID:
            log.info(message);
            break;
        case LogChute.TRACE_ID:
            log.trace(message);
            break;
        case LogChute.ERROR_ID:
            log.error(message);
            break;
        case LogChute.DEBUG_ID:
        default:
            log.debug(message);
            break;
        }
    }
    
    /**
     * Send a log message from Velocity with an error.
     */
    public void log(int level, String message, Throwable t)
    {
        switch (level)
        {
        case LogChute.WARN_ID:
            log.warn(message, t);
            break;
        case LogChute.INFO_ID:
            log.info(message, t);
            break;
        case LogChute.TRACE_ID:
            log.trace(message, t);
            break;
        case LogChute.ERROR_ID:
            log.error(message, t);
            break;
        case LogChute.DEBUG_ID:
        default:
            log.debug(message, t);
            break;
        }
    }
    
    /**
     * Checks whether the specified log level is enabled.
     */
    public boolean isLevelEnabled(int level)
    {
        switch (level)
        {
        case LogChute.DEBUG_ID:
            return log.isDebugEnabled();
        case LogChute.INFO_ID:
            return log.isInfoEnabled();
        case LogChute.TRACE_ID:
            return log.isTraceEnabled();
        case LogChute.WARN_ID:
            return log.isWarnEnabled();
        case LogChute.ERROR_ID:
            return log.isErrorEnabled();
        default:
            return true;
        }
    }
}