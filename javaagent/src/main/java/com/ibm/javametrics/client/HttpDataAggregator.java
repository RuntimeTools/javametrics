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

import java.util.Collection;
import java.util.HashMap;

/**
 * Aggregate HTTP request data
 *
 */
public class HttpDataAggregator {
    int totalHits;
    double total;
    long longest;
    long time;
    String longestUrl;
    String longestMethod;


    public class HttpUrlData {
        private String url;
        private String method;
        private int hits;
        private double totalResponseTime;
        private long longestResponseTime;

        public int getHits() {
            return hits;
        }

        public double getAverageResponseTime() {
            return totalResponseTime / hits;
        }

        public long getLongestResponseTime() {
            return longestResponseTime;
        }

        public String getUrl() {
            return url;
        }

        public String getMethod() {
            return method;
        }

        public HttpUrlData(String url, String method) {
            this.url = url;
            this.method = method;
            hits = 0;
            totalResponseTime = 0;
            longestResponseTime = 0;
        }
    }
    
    private HashMap<String, HttpUrlData> responseTimes = new HashMap<String, HttpUrlData>();

    public HttpDataAggregator() {
        resetSummaryData();
    }

    public void resetSummaryData() {
        totalHits = 0;
        total = 0;
        longest = 0;
        time = 0;
        longestUrl = "";
        longestMethod = "";
    }

    public void clear() {
        resetSummaryData();
        responseTimes.clear();
    }

    /**
     * @param jsonObject
     *            representing the HTTP request
     * @throws JsonException
     */
    public void aggregate(long requestTime, long requestDuration, String requestUrl, String requestMethod) {
        totalHits += 1;
        total += requestDuration;

        if ((totalHits == 1) || (requestDuration > longest)) {
            time = requestTime;
            longest = requestDuration;
            longestUrl = requestUrl;
            longestMethod = requestMethod;
        }
        String urlKey = requestMethod + requestUrl;
        HttpUrlData urlData = responseTimes.getOrDefault(urlKey, new HttpUrlData(requestUrl, requestMethod));
        urlData.hits += 1;
        urlData.totalResponseTime += requestDuration;
        if (requestDuration > urlData.longestResponseTime) {
            urlData.longestResponseTime = requestDuration;
        }
        responseTimes.put(urlKey, urlData);
    }

    public double getAverage() {
        double average = 0;
        if (totalHits > 0) {
            average = total / totalHits;
        }
        return average;
    }

    public long getLongest() {
        return longest;
    }

    public long getTime() {
        return time;
    }

    public int getTotalHits() {
        return totalHits;
    }

    public void setTime(long timeStamp) {
        time = timeStamp;
    }

    public Collection<HttpUrlData> getUrlData() {
        return responseTimes.values();
    }

    public String getLongestUrl() {
        return longestUrl;
    }

    public String getLongestMethod() {
        return longestMethod;
    }


}
