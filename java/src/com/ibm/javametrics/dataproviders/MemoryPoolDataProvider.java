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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.Iterator;
import java.util.List;

/**
 * Uses MXBeans to provide heap and other memory statistics
 *
 */
public class MemoryPoolDataProvider {

	/**
	 * Get the current heap size in bytes Returns -1 if no data is available
	 */
	public static long getHeapMemory() {
		long total = 0;
		List<MemoryPoolMXBean> memoryPoolBeans = ManagementFactory.getMemoryPoolMXBeans();
		if (memoryPoolBeans.isEmpty()) {
			return -1;
		}
		for (Iterator<MemoryPoolMXBean> iterator = memoryPoolBeans.iterator(); iterator.hasNext();) {
			MemoryPoolMXBean memoryPoolMXBean = iterator.next();
			if (memoryPoolMXBean.getType().equals(MemoryType.HEAP)) {
				total += memoryPoolMXBean.getUsage().getUsed();
			}
		}
		return total;
	}

	/**
	 * Get the most recent heap size immediately after GC in bytes. Returns -1
	 * if no data is available
	 */
	public static long getUsedHeapAfterGC() {
		long total = 0;
		List<MemoryPoolMXBean> memoryPoolBeans = ManagementFactory.getMemoryPoolMXBeans();
		if (memoryPoolBeans.isEmpty()) {
			return -1;
		}
		for (Iterator<MemoryPoolMXBean> iterator = memoryPoolBeans.iterator(); iterator.hasNext();) {
			MemoryPoolMXBean memoryPoolMXBean = iterator.next();
			if (memoryPoolMXBean.getType().equals(MemoryType.HEAP)) {
				total += memoryPoolMXBean.getCollectionUsage().getUsed();
			}
		}
		return total;
	}

	/**
	 * Get the size of native memory used by the JVM in bytes. Returns -1 if no
	 * data is available
	 */
	public static long getNativeMemory() {
		long total = 0;
		List<MemoryPoolMXBean> memoryPoolBeans = ManagementFactory.getMemoryPoolMXBeans();
		if (memoryPoolBeans.isEmpty()) {
			return -1;
		}
		for (Iterator<MemoryPoolMXBean> iterator = memoryPoolBeans.iterator(); iterator.hasNext();) {
			MemoryPoolMXBean memoryPoolMXBean = iterator.next();
			if (memoryPoolMXBean.getType().equals(MemoryType.NON_HEAP)) {
				total += memoryPoolMXBean.getUsage().getUsed();
			}
		}
		return total;
	}

}
