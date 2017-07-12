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
 * Test for GCDataProvider
 */
public class GCDataProviderTest {

	/**
	 * Test method for {@link com.ibm.javametrics.dataproviders.GCDataProvider#getGCCollectionTime()}.
	 */
	@Test
	public void testGetGCCollectionTime() {
		double gctime = GCDataProvider.getGCCollectionTime();
		int timeout = 3000;
		long startTime = System.currentTimeMillis();
		// may get -1 returned before MXBeans are initialized, allow time for a
		// real value to be returned
		while (System.currentTimeMillis() - timeout < startTime && gctime == -1d) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// Do nothing
				e.printStackTrace();
			}
			gctime = GCDataProvider.getGCCollectionTime();
		}
		assertTrue("GC time should be greater than or equal to 0, was " + gctime, gctime >= 0.0d);
		assertTrue("GC time should be less than 1 (i.e. less than 100%), was " + gctime, gctime <= 1d);
	}

}
