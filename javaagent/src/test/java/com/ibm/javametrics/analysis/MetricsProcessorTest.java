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

package com.ibm.javametrics.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.ibm.javametrics.Javametrics;
import com.ibm.javametrics.client.HttpDataAggregator.HttpUrlData;

/**
 * Tests for com.ibm.javametrics.Javametrics
 *
 */
public class MetricsProcessorTest {

    MetricsProcessor mp = MetricsProcessor.getInstance();

    // This MUST be the first test as testing the state of the singleton instance
    @Test
    public void testInitialState() {
        MetricsData md = mp.getMetricsData(0);
        assertNull("Expected no contexts but found one", md);
        
        md = mp.getMetricsData(1);
        assertNull("Context 1 should not exist", md);              
    }

    @Test
    public void testenvironment() {
        String env = mp.getEnvironment();
        assertEquals(env, "[]");
        List<String> jsonData = new ArrayList<String>();
        String fakeEnv = "Any old rubbish";
        jsonData.add("{\"topic\":\"env\",\"payload\":[" + fakeEnv  + "]}");
        mp.processData(jsonData);
        env = mp.getEnvironment();

        assertEquals(env, "[" + fakeEnv + "]");
    }
    
    @Test
    public void addDeleteContextTest() {
        int contextId = mp.addContext();
      
        MetricsData md = mp.getMetricsData(contextId);
        assertNotNull("Added context should exist", md);
        
        mp.removeContext(contextId);
        md = mp.getMetricsData(contextId);
        assertNull("Removed context should not exist", md);
        
    }

    @Test
    public void getContextIdsTest() {
        
        Integer ids[] = mp.getContextIds();
        int size = ids.length;

        int contextId = mp.addContext();        

        ids = mp.getContextIds();
        assertEquals(size + 1, ids.length);
        
        boolean found = false;
        for(Integer i : ids) {
            if (i == contextId) {
                found = true;
            }
        }
        assertTrue("New context should exist", found);
        
        mp.removeContext(contextId);
        ids = mp.getContextIds();
        assertEquals(size, ids.length);
        found = false;
        for(Integer i : ids) {
            if (i == contextId) {
                found = true;
            }
        }
        assertFalse("Deleted context should not exist", found);
        
    }
    
    @Test
    public void testResetContext() {

        int contextId = mp.addContext();
        
        // Add some HTTP data
        List<String> jsonData = new ArrayList<String>();
        jsonData.add("{\"topic\":\"http\",\"payload\":{\"time\":" + System.currentTimeMillis()
                + ",\"duration\":150,\"url\":\"http://localhost:9080/testy/v1/example1\",\"method\":\"GET\",\"status\":200,\"contentType\":\"null\",\"header\":{},\"requestHeader\":{}}}");
        jsonData.add("{\"topic\":\"http\",\"payload\":{\"time\":" + System.currentTimeMillis()
                + ",\"duration\":150,\"url\":\"http://localhost:9080/testy/v1/example\",\"method\":\"GET\",\"status\":200,\"contentType\":\"null\",\"header\":{},\"requestHeader\":{}}}");
        jsonData.add("{\"topic\":\"http\",\"payload\":{\"time\":" + System.currentTimeMillis()
                + ",\"duration\":600,\"url\":\"http://localhost:9080/testy/v1/example2\",\"method\":\"GET\",\"status\":200,\"contentType\":\"null\",\"header\":{},\"requestHeader\":{}}}");

        jsonData.add("{\"topic\":\"http\",\"payload\":{\"time\":" + System.currentTimeMillis()
                + ",\"duration\":150,\"url\":\"http://localhost:9080/testy/v1/example2\",\"method\":\"GET\",\"status\":200,\"contentType\":\"null\",\"header\":{},\"requestHeader\":{}}}");
        jsonData.add("{\"topic\":\"http\",\"payload\":{\"time\":" + System.currentTimeMillis()
                + ",\"duration\":0,\"url\":\"http://localhost:9080/testy/v1/example\",\"method\":\"GET\",\"status\":200,\"contentType\":\"null\",\"header\":{},\"requestHeader\":{}}}");
        
        mp.processData(jsonData);
        jsonData.clear();
        MetricsData md = mp.getMetricsData(contextId);
        assertNotNull("Added context should exist", md);
        
        checkUrlData(md);
        boolean exists = mp.resetMetricsData(contextId);
        assertTrue("context should exist", exists);
        
        md = mp.getMetricsData(contextId);
        assertNotNull("Added context should exist", md);
        Collection<HttpUrlData> urlData = md.getUrlData();
        assertEquals(0, urlData.size());
        
        mp.removeContext(contextId);
    }
    
    private void checkUrlData(MetricsData md) {
        Collection<HttpUrlData> urlData = md.getUrlData();
        assertEquals(3, urlData.size());

        HttpUrlData hud = null;
        for (HttpUrlData d : urlData) {
            if ("http://localhost:9080/testy/v1/example".equals(d.getUrl())) {
                hud = d;
            }
        }
        
        assertNotNull("Missing url data for http://localhost:9080/testy/v1/example", hud);
        assertEquals(2, hud.getHits());
        assertEquals(75, hud.getAverageResponseTime(), 0);
        assertEquals(150, hud.getLongestResponseTime(), 0);
    }
    
    @Test
    public void testMetricsProcessor() {

        // This test just drives coverage
        
        
        // Add as a listener to drive real data throuh
        Javametrics.getInstance().addListener(mp);
        int id = mp.addContext();

       // Sleep for 3 seconds to allow some data to arrive via the listener
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }
        
        // Add some HTTP data
        List<String> jsonData = new ArrayList<String>();
        jsonData.add("{\"topic\":\"http\",\"payload\":{\"time\":" + System.currentTimeMillis()
                + ",\"duration\":150,\"url\":\"http://localhost:9080/testy/v1/example1\",\"method\":\"GET\",\"status\":200,\"contentType\":\"null\",\"header\":{},\"requestHeader\":{}}}");
        jsonData.add("{\"topic\":\"http\",\"payload\":{\"time\":" + System.currentTimeMillis()
                + ",\"duration\":150,\"url\":\"http://localhost:9080/testy/v1/example\",\"method\":\"GET\",\"status\":200,\"contentType\":\"null\",\"header\":{},\"requestHeader\":{}}}");
        jsonData.add("{\"topic\":\"http\",\"payload\":{\"time\":" + System.currentTimeMillis()
                + ",\"duration\":600,\"url\":\"http://localhost:9080/testy/v1/example2\",\"method\":\"GET\",\"status\":200,\"contentType\":\"null\",\"header\":{},\"requestHeader\":{}}}");
        mp.processData(jsonData);
        jsonData.clear();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }

        int id2 = mp.addContext();

        jsonData.add("{\"topic\":\"http\",\"payload\":{\"time\":" + System.currentTimeMillis()
                + ",\"duration\":150,\"url\":\"http://localhost:9080/testy/v1/example2\",\"method\":\"GET\",\"status\":200,\"contentType\":\"null\",\"header\":{},\"requestHeader\":{}}}");
        jsonData.add("{\"topic\":\"http\",\"payload\":{\"time\":" + System.currentTimeMillis()
                + ",\"duration\":0,\"url\":\"http://localhost:9080/testy/v1/example\",\"method\":\"GET\",\"status\":200,\"contentType\":\"null\",\"header\":{},\"requestHeader\":{}}}");

        printSummary(id);

        jsonData.add("{\"topic\":\"http\",\"payload\":{\"time\":" + System.currentTimeMillis()
                + ",\"duration\":150,\"url\":\"http://localhost:9080/testy/v1/example\",\"method\":\"GET\",\"status\":200,\"contentType\":\"null\",\"header\":{},\"requestHeader\":{}}}");
        mp.processData(jsonData);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }
        
        System.gc();
        
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }

        printSummary(id);
        printSummary(id2);
    }

    private void printSummary(int id) {
        MetricsData summary = mp.getMetricsData(id);
        assertNotNull("Summary " + id + " does not exist", summary);
        
        System.out.println(summary.toJson(id));
        System.out.println("\nsummary for " + id);
        System.out.println("Start time: " + summary.getStartTime() + " duration : "
                + (summary.getEndTime() - summary.getStartTime()));
        System.out
                .println("cpu: processMean:" + summary.getCpuProcessMean() + " systemMean:" + summary.getCpuSystemMean()
                        + " processPeak:" + summary.getCpuProcessPeak() + " systemPeak:" + summary.getCpuSystemPeak());
        System.out.println("gcTime:" + summary.getGcTime());
        System.out.println("usedHeapAfterGCPeak:" + summary.getUsedHeapAfterGCPeak() + " usedNativePeak:"
                + summary.getUsedNativePeak());
        Iterator<HttpUrlData> it = summary.getUrlData().iterator();
        while (it.hasNext()) {
            HttpUrlData data = it.next();
            System.out.println(data.getUrl() + " hits:" + data.getHits() + " average:"
                    + data.getAverageResponseTime() + " longest:"
                    + data.getLongestResponseTime());
        }
    }
}
