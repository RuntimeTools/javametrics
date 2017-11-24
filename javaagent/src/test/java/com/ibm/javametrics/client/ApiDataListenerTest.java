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

package com.ibm.javametrics.client;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.ibm.javametrics.Javametrics;
import com.ibm.javametrics.client.ApiDataListener;

/**
 * Tests for com.ibm.javametrics.Javametrics
 *
 */
public class ApiDataListenerTest {

    @Test
    public void procesDataTest() {

        class Receiver extends ApiDataListener {
            public boolean received = false;

            @Override
            public void processData(List<String> jsonData) {
                for (String data : jsonData) {
                    // The individual message should be received as part of a batch
                    if (data.equals("{\"topic\":\"myTopic\",\"payload\":{\"message\": \"hello\"}}")) {
                        received = true;
                    }
                }
            }
        }
        ;

        Receiver rcvr = new Receiver();
        Javametrics.getInstance().addListener(rcvr);

        try {
            // Sleep to allow some data to be generated so our message will be in a batch
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // do nothing
            e.printStackTrace();
        }
        Javametrics.getInstance().sendJSON("myTopic", "{\"message\": \"hello\"}");
        int timeout = 6000;
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - timeout < startTime && !rcvr.received) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // do nothing
                e.printStackTrace();
            }
        }
        assertTrue("Listener should have received our message", rcvr.received);
    }

}
