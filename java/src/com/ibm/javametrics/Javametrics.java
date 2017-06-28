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

import java.util.HashMap;

/**
 * Javametrics public API class. Used to create Topics which can send data to
 * Javametrics. JSON formatted data can also be sent directly using sendJSON.
 */
public class Javametrics {

    /*
     * Connect to the native agent
     */
    private static JavametricsAgentConnector javametricsAgentConnector = new JavametricsAgentConnector();

    /*
     * Start the mbean data providers
     */
    static JavametricsMBeanConnector jmbc = new JavametricsMBeanConnector();
    
	private static HashMap<String, Topic> topics = new HashMap<String, Topic>();

	/**
	 * Get a Topic to send data on. If a topic with the given name already
	 * exists then that will be returned to you
	 * 
	 * @param topicName
	 * @return a {@link Topic} with the given name
	 */
	public static Topic getTopic(String topicName) {
		if (topicName == null || topicName.length() == 0) {
			throw new JavametricsException("Topic names must not be null or 0 length");
		}
		if (topics.containsKey(topicName)) {
			return topics.get(topicName);
		} else {
			Topic topic = new TopicImpl(topicName);
			topics.put(topicName, topic);
			return topic;
		}
	}

	protected static void sendData(String data) {
		if (javametricsAgentConnector != null) {
			javametricsAgentConnector.sendDataToAgent(data);
		}
	}

	/**
	 * Send data to Javametrics
	 * 
	 * @param topicName
	 *            the name of the topic to send data on
	 * @param payload
	 *             A JSON object formatted as a String
	 */
	public static void sendJSON(String topicName, String payload) {
		if (topicName == null || topicName.length() == 0) {
			throw new JavametricsException("Topic names must not be null or 0 length");
		}
		if (payload == null || payload.length() == 0) {
			throw new JavametricsException("Payload must exist");
		}
		getTopic(topicName).sendJSON(payload);
	}

	/**
	 * Returns true if the given topic is enabled
	 * 
	 * @param topicName
	 * @return
	 */
	public static boolean isEnabled(String topicName) {
		if (topicName == null || topicName.length() == 0) {
			throw new JavametricsException("Topic names must not be null or 0 length");
		}
		return getTopic(topicName).isEnabled();
	}

	/**
	 * Add a JavametricsListener, which will be informed of Javametrics events
	 * @param jml the JavametricsListener to be added
	 */
	public static void addListener(JavametricsListener jml) {
		javametricsAgentConnector.addListener(jml);
		
		/*
		 * Request history data so new listeners receive the environment data
		 */
	    javametricsAgentConnector.send("history");
	}

	/**
	 * Remove a JavametricsListener
	 * @param jml the JavametricsListener to be removed
	 * @return true if the listener was registered
	 */
	public static boolean removeListener(JavametricsListener jml) {
		return javametricsAgentConnector.removeListener(jml);
	}

}
