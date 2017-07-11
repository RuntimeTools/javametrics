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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.javametrics.Javametrics;
import com.ibm.javametrics.JavametricsListener;
import com.ibm.javametrics.TestUtils;

/**
 * Test for built in data providers
 *
 */
public class MBeanDataProviderTest {

	private static final List<String> received = new ArrayList<String>();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		Javametrics.getInstance().addListener(new JavametricsListener() {

			@Override
			public void receive(String pluginName, String data) {
				List<String> events = TestUtils.splitIntoJSONObjects(data);
				received.addAll(events);
			}
		});
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testDefault() {
		// Make sure topics are enabled as this can be affected by testDisableTopics
		Javametrics.getInstance().getTopic("gc").enable();
		Javametrics.getInstance().getTopic("memoryPools").enable();
		Javametrics.getInstance().getTopic("cpu").enable();
		// wait for at least 6 events or 5 seconds
		int timeout = 10000;
		long startTime = System.currentTimeMillis();
		while (System.currentTimeMillis() - timeout < startTime && received.size() < 6) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// do nothing
				e.printStackTrace();
			}
		}
		assertTrue("Listener should have received messages", received.size() >= 6);
		boolean foundGC = false;
		boolean foundMemPools = false;
		boolean foundCPU = false;
		for (Iterator<String> iterator = received.iterator(); iterator.hasNext();) {
			String message = iterator.next();
			if (message.startsWith("{\"topic\": \"gc")) {
				foundGC = true;
			}
			if (message.startsWith("{\"topic\": \"memoryPools")) {
				foundMemPools = true;
			}
			if (message.startsWith("{\"topic\": \"cpu")) {
				foundCPU = true;
			}
		}
		assertTrue("Should be emitting CPU data by default", foundCPU);
		assertTrue("Should be emitting GC data by default", foundGC);
		assertTrue("Should be emitting memory data by default", foundMemPools);
	}

	@Test
	public void testDisableTopics() {
		Javametrics.getInstance().getTopic("gc").disable();
		Javametrics.getInstance().getTopic("memoryPools").disable();
		Javametrics.getInstance().getTopic("cpu").disable();
		// Wait 3 seconds to make sure all events already sent have come through.
		try {
			TimeUnit.SECONDS.sleep(3);
		} catch (InterruptedException e) {
			// do nothing
			e.printStackTrace();
		}
		received.clear();
		// Wait another 3 seconds
		try {
			TimeUnit.SECONDS.sleep(3);
		} catch (InterruptedException e) {
			// do nothing
			e.printStackTrace();
		}
		assertEquals("Should not have received any messages", 0, received.size());
	}

}
