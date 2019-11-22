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

import java.util.Collection;
import java.util.Iterator;

import com.ibm.javametrics.client.HttpDataAggregator.HttpUrlData;

public class MetricsData {

    // time
    private long startTime;
    private long endTime;
    private static final String TIME_UNIT = "UNIX time (ms)";

    // gc
    private double gcTime;
    private static final String GC_TIME_UNIT = "decimal fraction";

    // cpu
    private double cpuSystemMean;
    private double cpuSystemPeak;
    private double cpuProcessMean;
    private double cpuProcessPeak;
    private static final String CPU_UNIT = "decimal fraction";

    // memory
    private long usedHeapAfterGCPeak;
    private long usedNativePeak;
    private static final String MEMORY_UNIT = "bytes";

    // http
    private Collection<HttpUrlData> urlData;
    private static final String HITS_UNIT = "count";
    private static final String RESPONSE_UNIT = "ms";

    public double getGcTime() {
        return gcTime;
    }

    public void setGcTime(double gcTime) {
        this.gcTime = gcTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public double getCpuSystemMean() {
        return cpuSystemMean;
    }

    public void setCpuSystemMean(double cpuSystemMean) {
        this.cpuSystemMean = cpuSystemMean;
    }

    public double getCpuSystemPeak() {
        return cpuSystemPeak;
    }

    public void setCpuSystemPeak(double cpuSystemPeak) {
        this.cpuSystemPeak = cpuSystemPeak;
    }

    public double getCpuProcessMean() {
        return cpuProcessMean;
    }

    public void setCpuProcessMean(double cpuProcessMean) {
        this.cpuProcessMean = cpuProcessMean;
    }

    public double getCpuProcessPeak() {
        return cpuProcessPeak;
    }

    public void setCpuProcessPeak(double cpuProcessPeak) {
        this.cpuProcessPeak = cpuProcessPeak;
    }

    public long getUsedHeapAfterGCPeak() {
        return usedHeapAfterGCPeak;
    }

    public void setUsedHeapAfterGCPeak(long usedHeapAfterGCPeak) {
        this.usedHeapAfterGCPeak = usedHeapAfterGCPeak;
    }

    public long getUsedNativePeak() {
        return usedNativePeak;
    }

    public void setUsedNativePeak(long usedNativePeak) {
        this.usedNativePeak = usedNativePeak;
    }

    public Collection<HttpUrlData> getUrlData() {
        return urlData;
    }

    public void setUrlData(Collection<HttpUrlData> urlData) {
        this.urlData = urlData;
    }

    public String toJson(int contextId) {

        StringBuilder metricsJson = new StringBuilder("{\"id\":");
        metricsJson.append(contextId);
        metricsJson.append(",\"time\": { \"data\":{");
        metricsJson.append("\"startTime\":");
        metricsJson.append(getStartTime());
        metricsJson.append(",\"endTime\":");
        metricsJson.append(getEndTime());
        metricsJson.append("},\"units\": {");
        metricsJson.append("\"startTime\":");
        metricsJson.append("\"" + TIME_UNIT + "\"");
        metricsJson.append(",\"endTime\":");
        metricsJson.append("\"" + TIME_UNIT + "\"");
        metricsJson.append("}}");

        metricsJson.append(",\"cpu\":{\"data\":{");
        metricsJson.append("\"systemMean\":");
        metricsJson.append(getCpuSystemMean());
        metricsJson.append(",\"systemPeak\":");
        metricsJson.append(getCpuSystemPeak());
        metricsJson.append(",\"processMean\":");
        metricsJson.append(getCpuProcessMean());
        metricsJson.append(",\"processPeak\":");
        metricsJson.append(getCpuProcessPeak());
        metricsJson.append("},\"units\": {");
        metricsJson.append("\"systemMean\":");
        metricsJson.append("\"" + CPU_UNIT + "\"");
        metricsJson.append(",\"systemPeak\":");
        metricsJson.append("\"" + CPU_UNIT + "\"");
        metricsJson.append(",\"processMean\":");
        metricsJson.append("\"" + CPU_UNIT + "\"");
        metricsJson.append(",\"processPeak\":");
        metricsJson.append("\"" + CPU_UNIT + "\"");
        metricsJson.append("}}");

        // gc
        metricsJson.append(",\"gc\":{\"data\":{");
        metricsJson.append("\"gcTime\":");
        metricsJson.append(getGcTime());
        metricsJson.append("},\"units\": {");
        metricsJson.append("\"gcTime\":");
        metricsJson.append("\"" + GC_TIME_UNIT + "\"");
        metricsJson.append("}}");

        // memory
        metricsJson.append(",\"memory\":{\"data\":{");
        metricsJson.append("\"usedHeapAfterGCPeak\":");
        metricsJson.append(getUsedHeapAfterGCPeak());
        metricsJson.append(",\"usedNativePeak\":");
        metricsJson.append(getUsedNativePeak());
        metricsJson.append("},\"units\": {");
        metricsJson.append("\"usedHeapAfterGCPeak\":");
        metricsJson.append("\"" + MEMORY_UNIT + "\"");
        metricsJson.append(",\"usedNativePeak\":");
        metricsJson.append("\"" + MEMORY_UNIT + "\"");
        metricsJson.append("}}");

        metricsJson.append(",\"httpUrls\":{\"data\":[");
        Iterator<HttpUrlData> it = getUrlData().iterator();
        while (it.hasNext()) {
            HttpUrlData hud = it.next();
            metricsJson.append("{\"url\":\"");
            metricsJson.append(hud.getUrl());
            metricsJson.append("\",\"method\":\"");
            metricsJson.append(hud.getMethod());
            metricsJson.append("\",\"hits\":\"");
            metricsJson.append(hud.getHits());
            metricsJson.append("\",\"averageResponseTime\":");
            metricsJson.append(hud.getAverageResponseTime());
            metricsJson.append(",\"longestResponseTime\":");
            metricsJson.append(hud.getLongestResponseTime());
            metricsJson.append('}');
            if (it.hasNext()) {
                metricsJson.append(',');
            }
        }
        metricsJson.append("],\"units\":{");
        metricsJson.append("\"averageResponseTime\":");
        metricsJson.append("\"" + RESPONSE_UNIT + "\"");
        metricsJson.append(",\"longestResponseTime\":");
        metricsJson.append("\"" + RESPONSE_UNIT + "\"");
        metricsJson.append(",\"hits\":");
        metricsJson.append("\"" + HITS_UNIT + "\"");
        metricsJson.append("}}}");

        return metricsJson.toString();
    }
}