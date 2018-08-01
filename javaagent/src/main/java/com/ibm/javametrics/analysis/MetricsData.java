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
import java.util.Map;
import java.util.Map.Entry;

import com.ibm.javametrics.client.HttpDataAggregator.HttpUrlData;

public class MetricsData {

    // time
    long startTime;
    long endTime;
    String startTimeUnit = "UNIX time (ms)";
    String endTimeUnit = "UNIX time (ms)";

    // gc
    double gcTime;
    String gcTimeUnit = "decimal fraction";

    // cpu
    double cpuSystemMean;
    double cpuSystemPeak;
    double cpuProcessMean;
    double cpuProcessPeak;
    String cpuSystemMeanUnit = "decimal fraction";
    String cpuSystemPeakUnit = "decimal fraction";
    String cpuProcessMeanUnit = "decimal fraction";
    String cpuProcessPeakUnit = "decimal fraction";

    // memory
    long usedHeapAfterGCPeak;
    long usedNativePeak;
    String usedHeapAfterGCPeakUnit = "bytes";
    String usedNativePeakUnit = "bytes";

    // http
    Collection<HttpUrlData> urlData;
    String hitsUnit = "count";
    String averageResponseTimeUnit = "ms";
    String longestResponseTimeUnit = "ms";

    public double getGcTime() {
        return gcTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public double getCpuSystemMean() {
        return cpuSystemMean;
    }

    public double getCpuSystemPeak() {
        return cpuSystemPeak;
    }

    public double getCpuProcessMean() {
        return cpuProcessMean;
    }

    public double getCpuProcessPeak() {
        return cpuProcessPeak;
    }

    public long getUsedHeapAfterGCPeak() {
        return usedHeapAfterGCPeak;
    }

    public long getUsedNativePeak() {
        return usedNativePeak;
    }

    public Collection<HttpUrlData> getUrlData() {
        return urlData;
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
        metricsJson.append("\"" + startTimeUnit + "\"");
        metricsJson.append(",\"endTime\":");
        metricsJson.append("\"" + endTimeUnit + "\"");
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
        metricsJson.append("\"" + cpuSystemMeanUnit + "\"");
        metricsJson.append(",\"systemPeak\":");
        metricsJson.append("\"" + cpuSystemPeakUnit + "\"");
        metricsJson.append(",\"processMean\":");
        metricsJson.append("\"" + cpuProcessMeanUnit + "\"");
        metricsJson.append(",\"processPeak\":");
        metricsJson.append("\"" + cpuProcessPeakUnit + "\"");
        metricsJson.append("}}");

        // gc
        metricsJson.append(",\"gc\":{\"data\":{");
        metricsJson.append("\"gcTime\":");
        metricsJson.append(getGcTime());
        metricsJson.append("},\"units\": {");
        metricsJson.append("\"gcTime\":");
        metricsJson.append("\"" + gcTimeUnit + "\"");
        metricsJson.append("}}");

        // memory
        metricsJson.append(",\"memory\":{\"data\":{");
        metricsJson.append("\"usedHeapAfterGCPeak\":");
        metricsJson.append(getUsedHeapAfterGCPeak());
        metricsJson.append(",\"usedNativePeak\":");
        metricsJson.append(getUsedNativePeak());
        metricsJson.append("},\"units\": {");
        metricsJson.append("\"usedHeapAfterGCPeak\":");
        metricsJson.append("\"" + usedHeapAfterGCPeakUnit + "\"");
        metricsJson.append(",\"usedNativePeak\":");
        metricsJson.append("\"" + usedNativePeakUnit + "\"");
        metricsJson.append("}}");

        metricsJson.append(",\"httpUrls\":{\"data\":[");
        Iterator<HttpUrlData> it = getUrlData().iterator();
        while (it.hasNext()) {
            HttpUrlData hud = it.next();
            metricsJson.append("{\"url\":\"");
            metricsJson.append(hud.getUrl());
            metricsJson.append("\",\"method\":\"");
            metricsJson.append(hud.getMethod());
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
        metricsJson.append("\"" + averageResponseTimeUnit + "\"");
        metricsJson.append(",\"longestResponseTime\":");
        metricsJson.append("\"" + longestResponseTimeUnit + "\"");
        metricsJson.append(",\"hits\":");
        metricsJson.append("\"" + hitsUnit + "\"");
        metricsJson.append("}}}");

        return metricsJson.toString();
    }
}