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

import java.util.Map;

import com.ibm.javametrics.client.HttpDataAggregator.HttpUrlData;

public class MetricsData {

    long startTime;
    long endTime;

    double gcTime;

    double cpuSystemMean;
    double cpuSystemPeak;
    double cpuProcessMean;
    double cpuProcessPeak;

    long usedHeapAfterGCPeak;
    long usedNativePeak;

    Map<String, HttpUrlData> urlData;

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

    public Map<String, HttpUrlData> getUrlData() {
        return urlData;
    }

}