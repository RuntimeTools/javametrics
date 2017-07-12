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
 * Tests for MemoryPoolDataProvider
 *
 */
public class MemoryPoolDataProviderTest {

	/**
	 * Test method for {@link com.ibm.javametrics.dataproviders.MemoryPoolDataProvider#getHeapMemory()}.
	 */
	@Test
	public void testGetHeapMemory() {
		long heapMemory = MemoryPoolDataProvider.getHeapMemory();
		assertTrue("Should get a real value for heap memory", heapMemory > 0);
	}

	/**
	 * Test method for {@link com.ibm.javametrics.dataproviders.MemoryPoolDataProvider#getUsedHeapAfterGC()}.
	 */
	@Test
	public void testGetUsedHeapAfterGC() {
		long usedHeap = MemoryPoolDataProvider.getHeapMemory();
		assertTrue("Should get a real value for used heap memory", usedHeap > 0);
	}

	/**
	 * Test method for {@link com.ibm.javametrics.dataproviders.MemoryPoolDataProvider#getNativeMemory()}.
	 */
	@Test
	public void testGetNativeMemory() {
		long nativeMemory = MemoryPoolDataProvider.getNativeMemory();
		assertTrue("Should get a real value for native memory", nativeMemory > 0);
	}

}
