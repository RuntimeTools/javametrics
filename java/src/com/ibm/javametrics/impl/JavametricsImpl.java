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
package com.ibm.javametrics.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.ibm.javametrics.Javametrics;
import com.ibm.javametrics.JavametricsException;
import com.ibm.javametrics.JavametricsListener;
import com.ibm.javametrics.Topic;
import com.ibm.javametrics.agent.Agent;
import com.ibm.javametrics.agent.AgentFactory;
import com.ibm.javametrics.agent.Receiver;
import com.ibm.javametrics.dataproviders.MBeanDataProvider;

public class JavametricsImpl implements Javametrics, Receiver {

    public static final String API_TYPE = "api";
    public static final String HISTORY_MESSAGE = "history";

    private Agent agent;
    private static final int COLLECTION_INTERVAL = 2;
    static MBeanDataProvider mbeanProvider = null;

    private Set<JavametricsListener> javametricsListeners = new HashSet<JavametricsListener>();

    private static HashMap<String, Topic> topics = new HashMap<String, Topic>();

    public JavametricsImpl() {
        agent = AgentFactory.getAgent();
        agent.registerReceiver(this);

        initializeProviders();
    }

    private void initializeProviders() {
        /*
         * Start the mbean data providers
         */
        mbeanProvider = new MBeanDataProvider(COLLECTION_INTERVAL);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.javametrics.agent.Receiver#receiveData(java.lang.String,
     * java.lang.String)
     */
    public void receiveData(String type, String data) {
        for (Iterator<JavametricsListener> iterator = javametricsListeners.iterator(); iterator.hasNext();) {
            JavametricsListener javametricsListener = iterator.next();
            javametricsListener.receive(type, data);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.javametrics.Javametrics#getTopic(java.lang.String)
     */
    public synchronized Topic getTopic(String topicName) {
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

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.javametrics.Javametrics#sendJSON(java.lang.String,
     * java.lang.String)
     */
    public void sendJSON(String topicName, String payload) {
        if (topicName == null || topicName.length() == 0) {
            throw new JavametricsException("Topic names must not be null or 0 length");
        }
        if (payload == null || payload.length() == 0) {
            throw new JavametricsException("Payload must exist");
        }
        getTopic(topicName).sendJSON(payload);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.javametrics.Javametrics#isEnabled(java.lang.String)
     */
    public boolean isEnabled(String topicName) {
        if (topicName == null || topicName.length() == 0) {
            throw new JavametricsException("Topic names must not be null or 0 length");
        }
        return getTopic(topicName).isEnabled();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.javametrics.Javametrics#addListener(com.ibm.javametrics.
     * JavametricsListener)
     */
    public void addListener(JavametricsListener jml) {
        javametricsListeners.add(jml);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.javametrics.Javametrics#removeListener(com.ibm.javametrics.
     * JavametricsListener)
     */
    public boolean removeListener(JavametricsListener jml) {
        return javametricsListeners.remove(jml);
    }

    /**
     * @param data
     */
    protected void sendData(String data) {
        pushData(API_TYPE, data);
    }

    /**
     * @param type
     * @param data
     */
    private void pushData(String type, String data) {
        agent.pushData(type, data);
    }

    /**
     * @param command
     */
    public void sendCommand(String command) {
        agent.command(command);
    }
}
