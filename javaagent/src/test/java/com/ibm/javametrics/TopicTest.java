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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for Topic
 *
 */
public class TopicTest {

	
	private static final List<String> received = new ArrayList<String>();
	private static Topic testTopic = Javametrics.getInstance().getTopic("testTopic");
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		Javametrics.getInstance().addListener(new JavametricsListener() {
			
			@Override
			public void receive(String pluginName, String data) {
				List<String> events = TestUtils.splitIntoJSONObjects(data);
				for (Iterator<String> iterator = events.iterator(); iterator.hasNext();) {
					String oneEvent = iterator.next();
					// Only store data we sent from this test case
					if (oneEvent.startsWith("{\"topic\": \"testTopic\",")) {
						received.add(oneEvent);
					}
				}
				
			}
		});
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		received.clear();
	}
	
	/**
	 * Test method for
	 * {@link com.ibm.javametrics.impl.TopicImpl#send(java.lang.String)}.
	 */
	@Test
	public void testSendString() {
		testTopic.send("hello from test topic");
		checkForResult("{\"topic\": \"testTopic\", \"payload\": {\"message\":\"hello from test topic\"}}");
	}

	private void checkForResult(String message) {
		int timeout = 3000;
		long startTime = System.currentTimeMillis();
		while (System.currentTimeMillis() - timeout < startTime && received.size() == 0) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// do nothing
			}
		}
		assertTrue("Listener should have received a message", received.size() > 0);
		for (Iterator<String> iterator = received.iterator(); iterator.hasNext();) {
			String data = iterator.next();
			boolean foundMyData = false;
			if (data.startsWith("{\"topic\": \"testTopic\",")) {
				assertEquals(message, data);
				foundMyData = true;
			}
			assertTrue("Should have received the data we sent", foundMyData);
		}
	}

	/**
	 * Test method for
	 * {@link com.ibm.javametrics.impl.TopicImpl#send(long, long, java.lang.String)}.
	 */
	@Test
	public void testSendLongLongString() {
		long endTime = System.currentTimeMillis();
		long startTime = endTime - 3000;
		testTopic.send(startTime, endTime, "hello from test topic");
		checkForResult("{\"topic\": \"testTopic\", \"payload\": {\"time\":\"" + startTime + "\", \"duration\": \"3000\", \"message\": \"hello from test topic\"}}");
	}

	/**
	 * Test method for {@link com.ibm.javametrics.impl.TopicImpl#send(long, long)}.
	 */
	@Test
	public void testSendLongLong() {
		long endTime = System.currentTimeMillis();
		long startTime = endTime - 3000;
		testTopic.send(startTime, endTime);
		checkForResult("{\"topic\": \"testTopic\", \"payload\": {\"time\":\"" + startTime + "\", \"duration\": \"3000\"}}");
	}

	/**
	 * Test method for
	 * {@link com.ibm.javametrics.impl.TopicImpl#sendJSON(java.lang.String)}.
	 */
	@Test
	public void testSendJSON() {
		testTopic.sendJSON("{\"personalizedMessage\":\"hello from test topic\"}");
		checkForResult("{\"topic\": \"testTopic\", \"payload\":{\"personalizedMessage\":\"hello from test topic\"}}");
	}

	/**
	 * Test method for {@link com.ibm.javametrics.impl.TopicImpl#disable()}. Test
	 * method for {@link com.ibm.javametrics.impl.TopicImpl#enable()}. Test method
	 * for {@link com.ibm.javametrics.impl.TopicImpl#isEnabled()}.
	 */
	@Test
	public void testDisableEnable() {
		Topic topic = Javametrics.getInstance().getTopic("hello");
		assertTrue(topic.isEnabled());
		topic.disable();
		assertFalse(topic.isEnabled());
		topic.enable();
		assertTrue(topic.isEnabled());
	}
}
