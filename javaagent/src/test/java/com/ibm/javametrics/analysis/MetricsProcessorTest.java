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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Test;

import com.ibm.javametrics.Javametrics;
import com.ibm.javametrics.client.HttpDataAggregator.HttpUrlData;

/**
 * Tests for com.ibm.javametrics.Javametrics
 *
 */
public class MetricsProcessorTest {

    MetricsProcessor sp = MetricsProcessor.getInstance();

    @Test
    public void testSummarizer() {

        Javametrics.getInstance().addListener(sp);
        int id = sp.addContext();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        List<String> jsonData = new ArrayList<String>();
        jsonData.add("{\"topic\":\"http\",\"payload\":{\"time\":" + System.currentTimeMillis()
                + ",\"duration\":150,\"url\":\"http://localhost:9080/testy/v1/example1\",\"method\":\"GET\",\"status\":200,\"contentType\":\"null\",\"header\":{},\"requestHeader\":{}}}");
        jsonData.add("{\"topic\":\"http\",\"payload\":{\"time\":" + System.currentTimeMillis()
                + ",\"duration\":150,\"url\":\"http://localhost:9080/testy/v1/example\",\"method\":\"GET\",\"status\":200,\"contentType\":\"null\",\"header\":{},\"requestHeader\":{}}}");
        jsonData.add("{\"topic\":\"http\",\"payload\":{\"time\":" + System.currentTimeMillis()
                + ",\"duration\":600,\"url\":\"http://localhost:9080/testy/v1/example2\",\"method\":\"GET\",\"status\":200,\"contentType\":\"null\",\"header\":{},\"requestHeader\":{}}}");
        sp.processData(jsonData);
        jsonData.clear();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        int id2 = sp.addContext();

        jsonData.add("{\"topic\":\"http\",\"payload\":{\"time\":" + System.currentTimeMillis()
                + ",\"duration\":150,\"url\":\"http://localhost:9080/testy/v1/example2\",\"method\":\"GET\",\"status\":200,\"contentType\":\"null\",\"header\":{},\"requestHeader\":{}}}");
        jsonData.add("{\"topic\":\"http\",\"payload\":{\"time\":" + System.currentTimeMillis()
                + ",\"duration\":0,\"url\":\"http://localhost:9080/testy/v1/example\",\"method\":\"GET\",\"status\":200,\"contentType\":\"null\",\"header\":{},\"requestHeader\":{}}}");

        printSummary(id);

        jsonData.add("{\"topic\":\"http\",\"payload\":{\"time\":" + System.currentTimeMillis()
                + ",\"duration\":150,\"url\":\"http://localhost:9080/testy/v1/example\",\"method\":\"GET\",\"status\":200,\"contentType\":\"null\",\"header\":{},\"requestHeader\":{}}}");
        sp.processData(jsonData);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.gc();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        printSummary(id);
        printSummary(id2);

        printSummary(0);
    }

    private void printSummary(int id) {
        MetricsData summary = sp.getMetricsData(id);
        
        System.err.println("\nsummary for " + id);
        System.err.println("Start time: " + summary.getStartTime() + " duration : "
                + (summary.getEndTime() - summary.getStartTime()));
        System.err
                .println("cpu: processMean:" + summary.getCpuProcessMean() + " systemMean:" + summary.getCpuSystemMean()
                        + " processPeak:" + summary.getCpuProcessPeak() + " systemPeak:" + summary.getCpuSystemPeak());
        System.err.println("gcTime:" + summary.getGcTime());
        System.err.println("usedHeapAfterGCPeak:" + summary.getUsedHeapAfterGCPeak() + " usedNativePeak:"
                + summary.getUsedNativePeak());
        Iterator<Entry<String, HttpUrlData>> it = summary.getUrlData().entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, HttpUrlData> pair = it.next();
            System.err.println(pair.getKey() + " hits:" + pair.getValue().getHits() + " average:"
                    + pair.getValue().getAverageResponseTime() + " longest:"
                    + pair.getValue().getLongestResponseTime());
        }
    }
}
