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
package com.ibm.javametrics.web;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.json.JsonException;
import javax.json.JsonObject;

/**
 * Aggregate HTTP request data
 * 
 */
public class HttpDataAggregator {
	int total;
	long average;
	long longest;
	long time;
	String url;

	private HashMap<String, HttpUrlData> responseTimes = new HashMap<String, HttpUrlData>();

	public HttpDataAggregator() {
		clear();
	}

	private HttpDataAggregator(int total, long average, long longest, long time, String url) {
		this.total = total;
		this.average = average;
		this.longest = longest;
		this.time = time;
		this.url = url;
	}

	HttpDataAggregator getCurrent() {
		return new HttpDataAggregator(total, average, longest, time, url);
	}

	void clear() {
		total = 0;
		average = 0;
		longest = 0;
		time = 0;
		url = "";
	}

	/**
	 * @param jsonObject
	 *            representing the HTTP request
	 * @throws JsonException
	 */
	public void aggregate(JsonObject jsonObject) throws JsonException {

		/*
		 * Extract the fields from the Json up front to avoid exceptions
		 * creating half baked request data
		 */
		long requestTime = jsonObject.getJsonNumber("time").longValue();
		long requestDuration = jsonObject.getJsonNumber("duration").longValue();
		String requestUrl = jsonObject.getString("url", "");
		// Emd of Json processing

		if (total == 0) {
			time = requestTime;
		}

		total += 1;

		if (requestDuration > longest || total == 1) {
			longest = requestDuration;
			url = requestUrl;
		}
		average = ((average * (total - 1)) + requestDuration) / total;

		HttpUrlData urlData = responseTimes.get(requestUrl);
		if (urlData == null) {
			urlData = new HttpUrlData();
		}

		urlData.hits += 1;
		urlData.averageResponseTime = ((urlData.averageResponseTime * (urlData.hits - 1)) + requestDuration)
				/ urlData.hits;
		responseTimes.put(requestUrl, urlData);
	}

	/**
	 * @return JSON String representing aggregated HTTP request data in the
	 *         format expected by the javascript
	 */
	String toJsonString() {
		StringBuilder sb = new StringBuilder("{\"topic\":\"http\",\"payload\":{\"time\":");
		sb.append(time);
		sb.append(",\"total\":");
		sb.append(total);
		sb.append(",\"longest\":");
		sb.append(longest);
		sb.append(",\"average\":");
		sb.append(average);
		sb.append(",\"url\":\"");
		sb.append(url);
		sb.append("\"}}");
		return sb.toString();
	}

	private class HttpUrlData {
		int hits;
		long averageResponseTime;

		public HttpUrlData() {
			hits = 0;
			averageResponseTime = 0;
		}
	}

	/**
	 * @return JSON String representing HTTP request data by URL in the format
	 *         expected by the javascript
	 */
	public String urlDatatoJsonString() {
		StringBuilder sb = new StringBuilder("{\"topic\":\"httpURLs\",\"payload\":[");

		Iterator<Entry<String, HttpUrlData>> it = responseTimes.entrySet().iterator();
		boolean first = true;
		while (it.hasNext()) {
			Entry<String, HttpUrlData> pair = it.next();
			if (!first) {
				sb.append(',');
			}
			first = false;
			sb.append("{\"url\":\"");
			sb.append(pair.getKey());
			sb.append("\",\"averageResponseTime\":");
			sb.append(pair.getValue().averageResponseTime);
			sb.append('}');
		}

		sb.append("]}");
		return sb.toString();
	}
}
