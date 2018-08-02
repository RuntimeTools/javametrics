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

import com.ibm.javametrics.client.HttpDataAggregator;

public class MetricsContext {

    private long startTime = 0;

    double cpuSystem = 0;
    double cpuSystemPeak = 0;
    double cpuProcess = 0;
    double cpuProcessPeak = 0;
    int cpuEvents = 0;

    double gcTime = 0;
    int gcEvents = 0;

    long usedHeapAfterGCPeak = 0;
    long usedNativePeak = 0;

    private HttpDataAggregator httpData = new HttpDataAggregator();

    public MetricsContext() {
        reset();
    }

    public void reset() {
        startTime = System.currentTimeMillis();

        cpuSystem = 0;
        cpuSystemPeak = 0;
        cpuProcess = 0;
        cpuProcessPeak = 0;
        cpuEvents = 0;

        gcTime = 0;
        gcEvents = 0;

        usedHeapAfterGCPeak = 0;
        usedNativePeak = 0;

        httpData.clear();
    }

    public void aggregateHttp(long timeStamp, long duration, String url, String method) {
        if (timeStamp >= startTime) {
            httpData.aggregate(timeStamp, duration, url, method);
        }
    }

    public void aggregateCpu(long timeStamp, double system, double process) {
        if (timeStamp >= startTime) {
            cpuEvents += 1;
            cpuSystem += system;
            cpuProcess += process;
            if (system > cpuSystemPeak) {
                cpuSystemPeak = system;
            }
            if (system > cpuProcessPeak) {
                cpuProcessPeak = process;
            }
        }
    }

    public void aggregateGc(long timeStamp, double time) {
        if (timeStamp >= startTime) {
            gcEvents += 1;
            gcTime += time;
        }
    }

    public void aggregateMemoryPools(long timeStamp, long usedHeapAfterGC, long usedHeap, long usedNative) {
        if (timeStamp >= startTime) {
            if (usedHeapAfterGC > usedHeapAfterGCPeak) {
                usedHeapAfterGCPeak = usedHeapAfterGC;
            }
            if (usedNative > usedNativePeak) {
                usedNativePeak = usedNative;
            }
        }
    }

    public MetricsData getMetricsData() {
        MetricsData metricsData = new MetricsData();
        metricsData.setStartTime(startTime);
        metricsData.setEndTime(System.currentTimeMillis());

        if (cpuEvents > 0) {
            metricsData.setCpuSystemMean(cpuSystem / cpuEvents);
            metricsData.setCpuProcessMean(cpuProcess / cpuEvents);
            metricsData.setCpuSystemPeak(cpuSystemPeak);
            metricsData.setCpuProcessPeak(cpuProcessPeak);
        }

        if (gcEvents > 0) {
            metricsData.setGcTime(gcTime / gcEvents);
        }

        metricsData.setUsedHeapAfterGCPeak(usedHeapAfterGCPeak);
        metricsData.setUsedNativePeak(usedNativePeak);

        metricsData.setUrlData(httpData.getUrlData());
        return metricsData;
    }

}
