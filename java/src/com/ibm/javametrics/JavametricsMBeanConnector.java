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
package com.ibm.javametrics;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.ibm.javametrics.dataproviders.CPUDataProvider;
import com.ibm.javametrics.dataproviders.GCDataProvider;
import com.ibm.javametrics.dataproviders.MemoryPoolDataProvider;

/**
 * Uses MBean data providers to send data to the Javametrics agent at regular intervals.
 */
public class JavametricsMBeanConnector {

	private static final String GC_TOPIC = "gc";
    private static final String CPU_TOPIC = "cpu";
    private static final String MEMORYPOOLS_TOPIC = "memoryPools";
    
    private ScheduledExecutorService exec;

	/**
	 * Create a JavametricsMBeanConnector
	 */
	public JavametricsMBeanConnector() {
		exec = Executors.newSingleThreadScheduledExecutor();
		exec.scheduleAtFixedRate(this::emitGCData, 2, 2, TimeUnit.SECONDS);
		exec.scheduleAtFixedRate(this::emitCPUUsage, 2, 2, TimeUnit.SECONDS);
		exec.scheduleAtFixedRate(this::emitMemoryPoolUsage, 2, 2, TimeUnit.SECONDS);
	}

	private void emitGCData() {
		long timeStamp = System.currentTimeMillis();
		double gcTime = GCDataProvider.getGCCollectionTime();
		if (gcTime >= 0) { // Don't send -1 'no data' values
			StringBuilder message = new StringBuilder();
			message.append("{\"time\":\"");
			message.append(timeStamp);
			message.append("\", \"gcTime\": \"");
			message.append(gcTime);
			message.append("\"}}");
            Javametrics.sendJSON(GC_TOPIC, message.toString());
		}
	}

	private void emitCPUUsage() {
		long timeStamp = System.currentTimeMillis();
		double process = CPUDataProvider.getProcessCpuLoad();
		double system = CPUDataProvider.getSystemCpuLoad();
		if (system >= 0 && process >= 0) {
			StringBuilder message = new StringBuilder();
			message.append("{\"time\":\"");
			message.append(timeStamp);
			message.append( "\", \"system\": \"");
			message.append(system);
			message.append("\", \"process\": \"");
			message.append(process);
			message.append("\"}}");
            Javametrics.sendJSON(CPU_TOPIC, message.toString());
		}
	}

	private void emitMemoryPoolUsage() {
		long timeStamp = System.currentTimeMillis();
		long usedHeapAfterGC = MemoryPoolDataProvider.getUsedHeapAfterGC();
		long usedNative = MemoryPoolDataProvider.getNativeMemory();
		long usedHeap = MemoryPoolDataProvider.getHeapMemory();
		if (usedHeapAfterGC >= 0) { // check that some data is available
			StringBuilder message = new StringBuilder();
			message.append("{\"time\":\"");
			message.append( timeStamp);
			message.append("\", \"usedHeapAfterGC\": \"");
			message.append(usedHeapAfterGC);
			message.append("\", \"usedHeap\": \"");
			message.append(usedHeap);
			message.append("\", \"usedNative\": \"");
			message.append(usedNative);
			message.append("\"}}");
	        Javametrics.sendJSON(MEMORYPOOLS_TOPIC, message.toString());
		}
	}
}
