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

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Iterator;
import java.util.List;

/**
 * Uses MXBeans to get GC information
 *
 */
public class GCDataProvider {
	
	private static long previousCollectionTime = 0;
	private static long previousRequestTimeStamp = 0;

	/**
	 * Returns the time spent in GC as a proportion of the time elapsed since this method was last called.
	 * If no data is available -1 is returned.
	 * @return
	 */
	public static double getGCCollectionTime() {
		long now = System.currentTimeMillis();
		List<GarbageCollectorMXBean> sunBeans = ManagementFactory.getGarbageCollectorMXBeans();
		long totalCollectionTime = 0;
		if(sunBeans.size() == 0) {
			return -1;
		}
		for (Iterator<GarbageCollectorMXBean> iterator = sunBeans.iterator(); iterator.hasNext();) {
			GarbageCollectorMXBean garbageCollectorMXBean = iterator.next();
			totalCollectionTime += garbageCollectorMXBean.getCollectionTime();
		}
		if(previousRequestTimeStamp == 0) {
			previousRequestTimeStamp = now;
			previousCollectionTime = totalCollectionTime;
			return -1;
		} else {
			long collectionTime = totalCollectionTime - previousCollectionTime;
			long elapsedTime = now - previousRequestTimeStamp;
			double timeInGc = (double)collectionTime / (double)elapsedTime;
			previousCollectionTime = totalCollectionTime;
			previousRequestTimeStamp = now;
			return timeInGc;
		}
	}

}
