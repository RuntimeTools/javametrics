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
package com.ibm.javametrics.instrument;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * HTTP request data
 */
public class HttpData {

	long requestTime = 0;
	long duration = 0;
	String url = null;
	String method = null;
	String contentType = null;
	HashMap<String, String> headers = null;
	HashMap<String, String> requestHeaders = null;

	public long getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(long requestTime) {
		this.requestTime = requestTime;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * @return JSON string representing response headers
	 */
	private String getHeaders() {

		StringBuilder sb = new StringBuilder("\"header\":{");
		if (headers != null) {
			boolean first = true;
			Iterator<Entry<String, String>> it = headers.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> pair = (Map.Entry<String, String>) it.next();
				if (!first) {
					sb.append(',');
				}
				sb.append('\"');
				sb.append(pair.getKey());
				sb.append("\":\"");
				sb.append(pair.getValue().replace("\"", "\\\""));
				sb.append('\"');
				first = false;
			}
		}

		sb.append('}');
		return sb.toString();
	}

	public void addHeader(String headerName, String header) {
		if (headers == null) {
			headers = new HashMap<String, String>();
		}
		headers.put(headerName, header);
	}

	/**
	 * @return JSON string repreesenting request headers
	 */
	public String getRequestHeaders() {
		StringBuilder sb = new StringBuilder("\"requestHeader\":{");
		if (requestHeaders != null) {
			boolean first = true;
			Iterator<Entry<String, String>> it = requestHeaders.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> pair = (Map.Entry<String, String>) it.next();
				if (!first) {
					sb.append(',');
				}
				sb.append('\"');
				sb.append(pair.getKey());
				sb.append("\":\"");
				sb.append(pair.getValue().replace("\"", "\\\""));
				sb.append('\"');
				first = false;
			}
		}

		sb.append('}');

		return sb.toString();
	}

	public void addRequestHeader(String headerName, String header) {
		if (requestHeaders == null) {
			requestHeaders = new HashMap<String, String>();
		}
		requestHeaders.put(headerName, header);
	}

	/**
	 * @return JSON string representing the HTTP request data
	 */
	public String toJsonString() {
		StringBuilder sb = new StringBuilder("{\"time\":");
		sb.append(requestTime);
		sb.append(",\"duration\":");
		sb.append(duration);
		sb.append(",\"url\":\"");
		sb.append(url);
		sb.append('\"');
		sb.append(",\"method\":\"");
		sb.append(method);
		sb.append('\"');
		sb.append(",\"contentType\":\"");
		sb.append(contentType);
		sb.append('\"');
		sb.append(',');
		sb.append(getHeaders());
		sb.append(',');
		sb.append(getRequestHeaders());
		sb.append('}');

		return sb.toString();
	}

}
