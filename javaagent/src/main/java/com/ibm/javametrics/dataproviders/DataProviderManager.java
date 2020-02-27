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
package com.ibm.javametrics.dataproviders;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.ibm.javametrics.Javametrics;

/**
 * Uses MBean data providers to send data to the Javametrics agent.
 */
public class DataProviderManager {

    private static final String GC_TOPIC = "gc";
    private static final String CPU_TOPIC = "cpu";
    private static final String MEMORYPOOLS_TOPIC = "memoryPools";
    private static final String ENV_TOPIC = "env";

    private ScheduledExecutorService exec;

    private static String escapeStringForJSON(String str) {
        return str.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // So long as we get the CPU samples at a constant interval
    // we can average them out. (Note: the total itself is
    // not meaningful.)
    private double totalSystemCPULoad = 0.0;
    private double totalProcessCPULoad = 0.0;
    private long cpuLoadSamples = 0;

    private long usedHeapAfterGCMax = 0;
    private long usedNativeMax = 0;

    /**
     * Create a JavametricsMBeanConnector
     */
    public DataProviderManager(long interval) {
        exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(this::emitGCData, interval, interval, TimeUnit.SECONDS);
        exec.scheduleAtFixedRate(this::emitCPUUsage, interval, interval, TimeUnit.SECONDS);
        exec.scheduleAtFixedRate(this::emitMemoryPoolUsage, interval, interval, TimeUnit.SECONDS);
    }

    /*
     * Schedule a refresh of any persistent data.
     */
    public void emitPersistentData() {
        // Persistent data provides, env, profiling status etc...
        // Schedule it so it doesn't delay this thread.
        exec.execute(this::emitEnvironmentData);
    }

    private void emitEnvironmentData() {
        String paramFormat = "{\"Parameter\":\"%s\",\"Value\":\"%s\"}";
        StringBuilder message = new StringBuilder("[");
        message.append(String.format(paramFormat, "Hostname",
                escapeStringForJSON(EnvironmentDataProvider.getHostname())));
        message.append(',');
        message.append(String.format(paramFormat, "OS Architecture",
                escapeStringForJSON(EnvironmentDataProvider.getArchitecture())));
        message.append(',');
        message.append(String.format(paramFormat, "Number of Processors", EnvironmentDataProvider.getCPUCount()));
        message.append(',');
        message.append(String.format(paramFormat, "Command Line",
                escapeStringForJSON(EnvironmentDataProvider.getCommandLine())));
        message.append("]");
        Javametrics.getInstance().sendJSON(ENV_TOPIC, message.toString());
    }

    private void emitGCData() {
        long timeStamp = System.currentTimeMillis();
        //double gcFraction = GCDataProvider.getLatestGCPercentage();
        double gcFractionSummary = GCDataProvider.getTotalGCPercentage();

        //if (gcFraction >= 0) { // Don't send -1 'no data' values
        StringBuilder message = new StringBuilder();
        message.append("{\"time\":");
        message.append(timeStamp);
        message.append(",\"gcTime\":");
        /*message.append(gcFraction);
        message.append(",\"gcTimeSummary\":");*/
        message.append(gcFractionSummary);
        message.append("}");
        Javametrics.getInstance().sendJSON(GC_TOPIC, message.toString());
        //}
    }

    private void emitCPUUsage() {
        try{
            long timeStamp = System.currentTimeMillis();
            double process = CPUDataProvider.getProcessCpuLoad();
            double system = CPUDataProvider.getSystemCpuLoad();
            cpuLoadSamples++;
            if (system >= 0 && process >= 0) {
                totalProcessCPULoad += process;
                totalSystemCPULoad += system;

                StringBuilder message = new StringBuilder();
                message.append("{\"time\":");
                message.append(timeStamp);
                message.append(",\"system\":");
                message.append(system);
                message.append(",\"process\":");
                message.append(process);
                message.append(",\"processMean\":");
                message.append(totalProcessCPULoad/cpuLoadSamples);
                message.append(",\"systemMean\":");
                message.append(totalSystemCPULoad/cpuLoadSamples);
                message.append("}");
                Javametrics.getInstance().sendJSON(CPU_TOPIC, message.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void emitMemoryPoolUsage() {
        long timeStamp = System.currentTimeMillis();
        long usedHeapAfterGC = MemoryPoolDataProvider.getUsedHeapAfterGC();
        long usedNative = MemoryPoolDataProvider.getNativeMemory();
        long usedHeap = MemoryPoolDataProvider.getHeapMemory();
        if (usedHeapAfterGC >= 0) { // check that some data is available
            String usedHeapAfterGCStr = Long.toString(usedHeapAfterGC, 10);
            usedHeapAfterGCMax = Math.max(usedHeapAfterGCMax, usedHeapAfterGC);

            String usedNativeStr = Long.toString(usedNative, 10);
            usedNativeMax = Math.max(usedNativeMax, usedNative);

            String usedHeapStr = Long.toString(usedHeap, 10);

            StringBuilder message = new StringBuilder();
            message.append("{\"time\":");
            message.append(timeStamp);
            message.append(",\"usedHeapAfterGC\":");
            message.append(usedHeapAfterGCStr);
            message.append(",\"usedHeap\":");
            message.append(usedHeapStr);
            message.append(",\"usedNative\":");
            message.append(usedNativeStr);

            // Used heap max is not actually that interesting, it ought to get to 100% just before a GC.
            message.append(",\"usedHeapAfterGCMax\":");
            message.append(usedHeapAfterGCMax);
            message.append(",\"usedNativeMax\":");
            message.append(usedNativeMax);
            message.append("}");
            Javametrics.getInstance().sendJSON(MEMORYPOOLS_TOPIC, message.toString());
        }
    }

}
