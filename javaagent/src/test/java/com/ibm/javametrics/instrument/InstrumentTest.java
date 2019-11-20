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

package com.ibm.javametrics.instrument;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.BeforeClass;

import com.ibm.javametrics.Javametrics;
import com.ibm.javametrics.JavametricsListener;
import com.ibm.javametrics.TestUtils;

/**
 * Test for com.ibm.javametrics.instrument classes
 */
public class InstrumentTest {

    private static final List<String> received = new ArrayList<String>();
    private static Javametrics javametrics = Javametrics.getInstance();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        javametrics.addListener(new JavametricsListener() {

            @Override
            public void receive(String pluginName, String data) {
                List<String> events = TestUtils.splitIntoJSONObjects(data);
                received.addAll(events);
            }
        });

        // Initial setup - turn off other types of data
        javametrics.getTopic("gc").disable();
        javametrics.getTopic("memoryPools").disable();
        javametrics.getTopic("cpu").disable();
        // Wait 3 seconds to make sure all events already sent have come
        // through.
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        received.clear();
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

//    @Test
    public void testInstrumentHttpJspPage() {

        try {
            new MockHttpJspPage()._jspService(new MockHttpServletRequest("http://testURL"),
                    new MockHttpServletResponse());
        } catch (ServletException | IOException e) {
            e.printStackTrace();
        }
        // Wait 3 seconds for data to come through
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean foundHttp = false;
        for (Iterator<String> iterator = received.iterator(); iterator.hasNext();) {
            String message = iterator.next();
            if (message.startsWith("{\"topic\": \"http")) {
                foundHttp = true;
                assertTrue("Test URL should be in message " + message,
                        message.indexOf("\"url\":\"http://testURL\"") != -1);
            }
        }
        assertTrue("Should be emitting Http data", foundHttp);
    }

//    @Test
    public void testInstrumentServlet() {
        try {
            MockServlet servlet = new MockServlet();
            servlet.service(new MockHttpServletRequest("http://testURL/service"),
                        new MockHttpServletResponse());
            servlet.doGet(new MockHttpServletRequest("http://testURL/doGet"),
                        new MockHttpServletResponse());
            servlet.doPost(new MockHttpServletRequest("http://testURL/doPost"),
                    new MockHttpServletResponse());            
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Wait 3 seconds for data to come through
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean instrumentedService = false;
        boolean instrumentedDoGet = false;
        boolean instrumentedDoPost = false;
        for (Iterator<String> iterator = received.iterator(); iterator.hasNext();) {
            String message = iterator.next();
            if (message.startsWith("{\"topic\": \"http")) {
                if(message.indexOf("\"url\":\"http://testURL/service\"") != -1) {
                    instrumentedService = true;
                } else if(message.indexOf("\"url\":\"http://testURL/doGet\"") != -1) {
                    instrumentedDoGet = true;
                } else if(message.indexOf("\"url\":\"http://testURL/doPost\"") != -1) {
                    instrumentedDoPost = true;
                }
            }
        }
        assertTrue("Should be instrumenting HttpServlet.service", instrumentedService);
        assertTrue("Should be instrumenting HttpServlet.doGet", instrumentedDoGet);
        assertTrue("Should be instrumenting HttpServlet.doPost", instrumentedDoPost);
    }

}
