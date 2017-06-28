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

public class TopicImpl implements Topic {

	private String topicName;
	private boolean enabled = true;

	protected TopicImpl(String topicName) {
		this.topicName = topicName;
	}

	@Override
	public void send(String message) {
		if (enabled) {
			StringBuilder json = new StringBuilder();
			json.append("{\"topic\": \"");
			json.append(topicName);
			json.append("\", \"payload\": {\"message\":\"");
			json.append(message);
			json.append("\"}}");
			Javametrics.sendData(json.toString());
		}
	}

	@Override
	public void send(long startTime, long endTime, String message) {
		if (enabled) {
			long duration = endTime - startTime;
			StringBuilder json = new StringBuilder();
			json.append("{\"topic\": \"");
			json.append(topicName);
			json.append("\", \"payload\": {\"time\":\"");
			json.append(startTime);
			json.append("\", \"duration\": \"");
			json.append(duration);
			json.append("\", \"message\": \"");
			json.append(message);
			json.append("\"}}");
			Javametrics.sendData(json.toString());
		}
	}

	@Override
	public void send(long startTime, long endTime) {
		if (enabled) {
			long duration = endTime - startTime;
			StringBuilder json = new StringBuilder();
			json.append("{\"topic\": \"");
			json.append(topicName);
			json.append("\", \"payload\": {\"time\":\"");
			json.append(startTime);
			json.append("\", \"duration\": \"");
			json.append(duration);
			json.append("\"}}");
			Javametrics.sendData(json.toString());
		}
	}

	@Override
	public void sendJSON(String payload) {
		if (enabled) {
			StringBuilder json = new StringBuilder();
			json.append("{\"topic\": \"");
			json.append(topicName);
			json.append( "\", \"payload\":");
			json.append(payload);
			json.append("}");
			Javametrics.sendData(json.toString());
		}
	}

	@Override
	public void disable() {
		enabled = false;
	}

	@Override
	public void enable() {
		enabled = true;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

}
