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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

import com.ibm.javametrics.analysis.HttpDataAggregator.HttpUrlData;

/**
 * Tests for com.ibm.javametrics.Javametrics
 *
 */
public class HttpDataAggregatorTest {
    HttpDataAggregator aggregator = new HttpDataAggregator();

    @Test
    public void testAgregation() {
        
        // Validate initial values
        checkValues(0, 0, 0, 0, "");

        long time = System.currentTimeMillis();      
        aggregator.aggregate(time-300, 500, "myurl-1");  
        aggregator.aggregate(time-400, 200, "myurl-2");  
        aggregator.aggregate(time+666, 300, "myurl-1");  
        aggregator.aggregate(time, 600, "myurl-2");  // Longest
        aggregator.aggregate(time-222, 100, "myurl-1");
        
        checkValues(time, 340, 5, 600, "myurl-2");     
        checkUrlData("myurl-1", 3, 300);
        checkUrlData("myurl-2", 2, 400);
   
        // resetSummaryData should only clear summary and not urlData
        aggregator.resetSummaryData();
        checkValues(0, 0, 0, 0, "");
        checkUrlData("myurl-1", 3, 300);
        checkUrlData("myurl-2", 2, 400);
        
        aggregator.aggregate(time-300, 500, "myurl-1");  
        aggregator.aggregate(time-400, 200, "myurl-2");  
        aggregator.aggregate(time+666, 300, "myurl-1");  
        aggregator.aggregate(time, 600, "myurl-2");  
        aggregator.aggregate(time-222, 100, "myurl-1");  
        
        // clear() should reset everything
        aggregator.clear();
        checkValues(0, 0, 0, 0, "");
        assertTrue("UrlData should be empty", aggregator.getUrlData().isEmpty());     
    }

    private void checkValues(long time, long average, long total, long longest, String url) {

        assertEquals("time value incorrect", time, aggregator.getTime());
        assertEquals("totalHits value incorrect", total, aggregator.getTotalHits());
        assertEquals("average value incorrect", average, aggregator.getAverage());
        assertEquals("longest value incorrect", longest, aggregator.getLongest());
        assertEquals("url value incorrect", url, aggregator.getUrl());
    }
    
    private void checkUrlData(String url, int hits, long average) {
        HashMap<String, HttpUrlData> urlData = aggregator.getUrlData();
        HttpUrlData httpData = urlData.get(url);
        assertNotNull("Expecting http data for url: " +  url, httpData);
        assertEquals("Incorrect number of hits for url: " +  url, hits, httpData.getHits());
        assertEquals("Incorrect average response time for url: " +  url, average, httpData.getAverageResponseTime());
    }
}
