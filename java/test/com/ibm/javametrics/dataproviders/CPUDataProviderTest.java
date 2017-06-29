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

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test CPU Data provider
 *
 */
public class CPUDataProviderTest {

	/**
	 * Test method for
	 * {@link com.ibm.javametrics.dataproviders.CPUDataProvider#getProcessCpuLoad()}
	 */
	@Test
	public void testGetProcessCpuLoad() {
		double load = CPUDataProvider.getProcessCpuLoad();
		int timeout = 3000;
		long startTime = System.currentTimeMillis();
		// may get -1 returned before MXBeans are initialized, allow time for a
		// real value to be returned
		while (System.currentTimeMillis() - timeout < startTime && load == -1d) {
			load = CPUDataProvider.getProcessCpuLoad();
		}
		assertTrue("CPU load should be greater than or equal to 0, was " + load, load >= 0.0d);
		assertTrue("CPU load should be less than 1 (i.e. less than 100%), was " + load, load <= 1d);
	}

	/**
	 * Test method for
	 * {@link com.ibm.javametrics.dataproviders.CPUDataProvider#getSystemCpuLoad()}
	 */
	@Test
	public void testGetSystemCpuLoad() {
		double load = CPUDataProvider.getSystemCpuLoad();
		int timeout = 3000;
		long startTime = System.currentTimeMillis();
		// may get -1 returned before MXBeans are initialized, allow time for a
		// real value to be returned
		while (System.currentTimeMillis() - timeout < startTime && load == -1d) {
			load = CPUDataProvider.getSystemCpuLoad();
		}
		double process = CPUDataProvider.getProcessCpuLoad();
		assertTrue("CPU load should be greater than 0, was " + load, load >= 0.0d);
		assertTrue("CPU load should be less than 1 (i.e. less than 100%), was " + load, load <= 1d);
		assertTrue("System CPU load should greater than or equal to process load", load >= process);
	}

}
