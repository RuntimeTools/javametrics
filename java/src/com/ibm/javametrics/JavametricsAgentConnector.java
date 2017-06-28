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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class JavametricsAgentConnector {

    private static native void regListener(JavametricsAgentConnector jm);
    private static native void deregListener();
    private static native void sendMessage(String message, byte[] id);
    private static native void pushDataToAgent(String data);


    /*
     * Set to true when connected to the native agent
     */
    private boolean initialized = false;

    private static final String CLIENT_ID = "localNative";//$NON-NLS-1$
    private static final String COMMA = ","; //$NON-NLS-1$
    private static final String DATASOURCE_TOPIC = "/datasource";//$NON-NLS-1$
    private static final String CONFIGURATION_TOPIC = "configuration/";//$NON-NLS-1$
    private static final String HISTORY_TOPIC = "/history/";//$NON-NLS-1$

    private Set<JavametricsListener> javametricsListeners = new HashSet<JavametricsListener>();
    public JavametricsAgentConnector() {
        try {
            regListener(this);
            initialized = true;
        } catch (UnsatisfiedLinkError ule) {
            System.err.println("Javametrics: Native agent not loaded. Use -agentpath parameter to load Javametrics agent.");
        }
    }

    private void sendMessage(String name, String command, String... params) {
        if (initialized) {
            StringBuffer sb = new StringBuffer();
            sb.append(command);
            for (String parameter : params) {
                sb.append(COMMA).append(parameter);
            }
            sb.trimToSize();
            sendMessage(name, sb.toString().getBytes());
        }
    }

    public void receiveData(String type, byte[] data) {
        final String dataString = new String(data);
        for (Iterator<JavametricsListener> iterator = javametricsListeners.iterator(); iterator.hasNext();) {
            JavametricsListener javametricsListener = iterator.next();
            javametricsListener.receive(type, dataString);
        }
    }

    protected void addListener(JavametricsListener jml) {
        javametricsListeners.add(jml);
    }

    protected boolean removeListener(JavametricsListener jml) {
        return javametricsListeners.remove(jml);
    }

    protected void sendDataToAgent(String data) {
        if (initialized) {
            pushDataToAgent(data);
        }
    }

    protected void send(String message) {
        if (initialized) {
            sendMessage(message, CLIENT_ID);
        }
    }
}
