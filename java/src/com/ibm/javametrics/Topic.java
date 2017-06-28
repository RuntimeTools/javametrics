/*******************************************************************************
 * Copyright 2017 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.ibm.javametrics;

/**
 * A Javametrics topic on which data can be emitted.  
 * Create a new topic by calling Javametrics.createTopic(..)
 * Topics created this way are registered with the Javametrics agent and are 'on' by default.
 */
public interface Topic
{

	/**
	 * Send a message (sends if enabled)
	 * @param message the message to be emitted
	 */
	public void send(String message);
	
	/**
	 * Send a message with a start and end time (sends if enabled)
	 * @param startTime start time in milliseconds (see System.currentTimeMillis for definition)
	 * @param endTime end time in milliseconds (see System.currentTimeMillis for definition)
	 * @param message the message to be emitted
	 */
	public void send(long startTime, long endTime, String message);
	
	/**
	 * Send a timed event with a start and end time (sends if enabled)
	 * @param startTime start time in milliseconds (see System.currentTimeMillis for definition)
	 * @param endTime end time in milliseconds (see System.currentTimeMillis for definition)
	 */
	public void send(long startTime, long endTime);
	
	/**
	 * Send a JSON formatted String
	 * @param payload A JSON object formatted as a String
	 */
	public void sendJSON(String payload);

	
	/**
	 * Disable this topic (send methods will do nothing)
	 */
	public void disable();
	
	/**
	 * Enable this topic
	 */
	public void enable();

	/**
	 * @return true if this topic is enabled
	 */
	public boolean isEnabled();

}
