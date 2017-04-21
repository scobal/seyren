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
package com.seyren.core.service.schedule;

import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seyren.core.domain.Check;

/**
 * The Check Concurrency Governor prevents checks being initiated in a given
 * query cycle when that same check has not finished in a former cycle.
 * @author Wayne Warren
 *
 */
public class CheckConcurrencyGovernor {
	/** Singleton instance  */
	private static CheckConcurrencyGovernor instance;
	/** The standard logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(CheckConcurrencyGovernor.class);
	/** A set of the currently running checks, where the check ID is the key and the POJO 
	 * check object is the value */
	private static final HashMap<String, CheckStatus> runningChecks = new HashMap<String, CheckStatus>();
	/**
	 * 
	 */
	public static boolean enabled = true;
	
	/**
	 * Default singleton constructor
	 */
	private CheckConcurrencyGovernor(){
		
	}
	
	/**
	 * Set the enablement of the concurrency governor
	 * @param enabled Whether or not the concurrency governor is enabled
	 */
	public static final void setEnabled(boolean enabled){
		CheckConcurrencyGovernor.enabled = enabled;
	}
	
	/**
	 * Singleton instance request
	 * @return The valid singleton instance
	 */
	public static CheckConcurrencyGovernor instance(){
		if (instance == null){
			instance = new CheckConcurrencyGovernor();
		}
		return instance;
	}
	
	/**
	 * Check to see if a check is currently running
	 * @param check The check in question
	 * @return True if the check is still running, false otherwise
	 */
	public synchronized boolean isCheckRunning(Check check){
		try {
			if (enabled){
				return runningChecks.containsKey(check.getId());
			}
			else {
				return false;
			}
		}
		catch (Exception e){
			LOGGER.error("Exception encountered while checking if check is 'running'", e);
			return false;
		}
	}
	
	/**
	 * Notify this governor that a given check has been initiated and 
	 * is now running.
	 * @param check The check that is now running 
	 * @return True if the check was registered as running properly, false in the case of an exception
	 */
	public synchronized boolean notifyCheckIsRunning(Check check){
		try {
			CheckStatus checkStatus = new CheckStatus(check);
			runningChecks.put(check.getId(), checkStatus);
			return true;
		}
		catch (Exception e){
			LOGGER.error("Exception encountered while setting check to 'running'", e);
			return false;
		}
	}
	
	/**
	 * Notify this governor that a given check has completed
	 * @param check The check in question
	 * @return True if the check was successfully taken out of the running queue
	 */
	public synchronized boolean notifyCheckIsComplete(Check check){
		try {
			String id = check.getId();
			CheckStatus status = runningChecks.get(id);
			if (status != null){
				status.stopTime = new Date().getTime();
			}
			// We're not doing anything yet with the status object... but we might
			// @TODO Look into if archiving the profiling data
			runningChecks.remove(check.getId());
			return true;
		}
		catch (Exception e){
			LOGGER.error("Exception encountered while setting check to 'complete'", e);
			return false;
		}
	}
	
	/**
	 * 
	 * @param check
	 */
	public void logCheckSkipped(Check check){
		
	}
	
	/**
	 * A convenience class that holds the start time of a check, and the 
	 * number of cycles missed, for state and profiling purposes.
	 * @author Wayne Warren
	 *
	 */
	public class CheckStatus {
		/** The start time of the check, in milliseconds */
		private final long startTime;
		/** The stope time of the check, in milliseconds */
		private long stopTime;
		/** The number of query cycles, if any, missed by this check due to its not finishing in time */
		private int missedCycles = 0;
		/** The check whose status this is */
		private final Check check;
		
		/**
		 * Default constructor
		 * @param check The check whose status is reflected by this object
		 */
		public CheckStatus(Check check){
			this.startTime = new Date().getTime();
			this.check = check;
		}
		
		/**
		 * Get the total duration of the check
		 * @return The duration, in milliseconds
		 */
		public long getDuration(){
			return new Date().getTime() - this.startTime;
		}
		
		/**
		 * Let this instance know that a cycle has been missed.  A log message will be issued
		 */
		public void cycleMissed(){
			this.missedCycles++;
			LOGGER.warn("Check cycle missed.  Current check #" + check.getId() + " has "
					+ "missed " + this.missedCycles + " cycles, and has been running for "
							+ this.getDuration() + " milliseconds");
		}
		
		/**
		 * Get the number of query cycles missed by the registered check
		 * @return The number of query cycles missed
		 */
		public int getMissedCycles(){
			return this.missedCycles;
		}
		
		/**
		 * Get the check's name
		 * @return The check's name
		 */
		public String getCheckName(){
			return this.check.getName();
		}
		
		/**
		 * Get the check's ID
		 * @return The check's id
		 */
		public String getCheckId(){
			return this.check.getId();
		}
	}
}