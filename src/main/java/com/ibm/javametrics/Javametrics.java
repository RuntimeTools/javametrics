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

import com.ibm.javametrics.impl.JavametricsImpl;

/**
 * Javametrics public API class. Used to create Topics which can send data to
 * Javametrics. JSON formatted data can also be sent directly using sendJSON.
 */
public interface Javametrics {

    public static Javametrics instance = new JavametricsImpl();

    public static Javametrics getInstance() {
        return instance;
    }

    /**
     * Get a Topic to send data on. If a topic with the given name already
     * exists then that will be returned to you
     * 
     * @param topicName
     * @return a {@link Topic} with the given name
     */
    public Topic getTopic(String topicName);

    /**
     * Returns true if the given topic is enabled
     * 
     * @param topicName
     * @return
     */
    public boolean isEnabled(String topicName);

    /**
     * Send data to Javametrics
     * 
     * @param topicName
     *            the name of the topic to send data on
     * @param payload
     *            A JSON object formatted as a String
     */
    public void sendJSON(String topicName, String payload);

    /**
     * Add a JavametricsListener, which will be informed of Javametrics events
     * 
     * @param jml
     *            the JavametricsListener to be added
     */
    public void addListener(JavametricsListener jml);

    /**
     * Remove a JavametricsListener
     * 
     * @param jml
     *            the JavametricsListener to be removed
     * @return true if the listener was registered
     */
    public boolean removeListener(JavametricsListener jml);

}
