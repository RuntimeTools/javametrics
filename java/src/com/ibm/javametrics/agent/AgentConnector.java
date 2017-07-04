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
package com.ibm.javametrics.agent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.ibm.javametrics.JavametricsListener;
import com.ibm.javametrics.dataproviders.MBeanDataProvider;

public class AgentConnector implements Receiver {

    public static final String HISTORY_MESSAGE = "history";
    
    private Set<JavametricsListener> javametricsListeners = new HashSet<JavametricsListener>();

    private Agent agent;

    private static final int COLLECTION_INTERVAL = 2;

    static MBeanDataProvider mbeanProvider = null;

    private static AgentConnector instance = null;

    public static AgentConnector getConnector() {
        if (instance == null) {
            instance = new AgentConnector();
        }
        return instance;
    }

    private AgentConnector() {
        agent = AgentFactory.getAgent();
        agent.registerReceiver(this);
        
        /*
         * Start the mbean data providers
         */
        mbeanProvider = new MBeanDataProvider(COLLECTION_INTERVAL);
    }

    /* (non-Javadoc)
     * @see com.ibm.javametrics.agent.Receiver#receiveData(java.lang.String, java.lang.String)
     */
    public void receiveData(String type, String data) {
        for (Iterator<JavametricsListener> iterator = javametricsListeners.iterator(); iterator.hasNext();) {
            JavametricsListener javametricsListener = iterator.next();
            javametricsListener.receive(type, data);
        }
    }

    public void addListener(JavametricsListener jml) {
        javametricsListeners.add(jml);

        /*
         * Request history data so new listeners receive the environment data
         */
        sendMessage(HISTORY_MESSAGE);
    }

    public boolean removeListener(JavametricsListener jml) {
        return javametricsListeners.remove(jml);
    }

    public void sendDataToAgent(String type, String data) {
        agent.pushData(type, data);
    }

    public void sendMessage(String command) {
        agent.command(command);
    }
}
