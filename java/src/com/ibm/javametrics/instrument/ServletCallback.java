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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Enumeration;

import com.ibm.javametrics.Javametrics;

/**
 * Class containing static methods to be called from injected code
 *
 */
public class ServletCallback {

	private static final String HTTP_TOPIC = "http";
	private static final String GET_REQUEST_URL = "getRequestURL";
	private static final String GET_METHOD = "getMethod";
	private static final String GET_CONTENT_TYPE = "getContentType";
	private static final String GET_HEADER_NAMES = "getHeaderNames";
	private static final String GET_HEADER = "getHeader";
	private static final String GET_ATTRIBUTE = "getAttribute";
	private static final String SET_ATTRIBUTE = "setAttribute";
	private static final Object TRACKER_ATTRIBUTE = "com.ibm.javametrics.tracker";

	/**
	 * Called on method entry for HTTP/JSP requests public static void
	 * 
	 * True method signature: void before(long requestTime, HttpServletRequest
	 * request, HttpServletResponse response)
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 */
	public static void before(Object request, Object response) {
		/*
		 * Use reflection to access the HttpServletRequest/Response as using the
		 * true method signature caused ClassLoader issues.
		 */
		Class<?> reqClass = request.getClass();
		try {
			/*
			 * Retrieve the tracker from the request and increment nesting level
			 */
			HttpRequestTracker tracker;
			Method getAttribute = reqClass.getMethod(GET_ATTRIBUTE, String.class);
			tracker = (HttpRequestTracker) (getAttribute.invoke(request, TRACKER_ATTRIBUTE));
			if (tracker == null) {
				tracker = new HttpRequestTracker();
			}
			tracker.increment();
			Method setAttribute = reqClass.getMethod(SET_ATTRIBUTE, String.class, Object.class);
			setAttribute.invoke(request, TRACKER_ATTRIBUTE, tracker);
		} catch (Exception e) {
			// Log any exception caused by our injected code
			System.err.println("Javametrics: Servlet callback exception: " + e.toString());
		}
	}

	/**
	 * Called on method exit for HTTP/JSP requests public static void
	 * 
	 * True method signature: void after(long requestTime, HttpServletRequest
	 * request, HttpServletResponse response)
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 */
	@SuppressWarnings("unchecked")
	public static void after(Object request, Object response) {

		HttpData data = new HttpData();

		/*
		 * Use reflection to access the HttpServletRequest/Response as using the
		 * true method signature caused ClassLoader issues.
		 */
		Class<?> reqClass = request.getClass();
		Class<?> respClass = response.getClass();
		try {
			/*
			 * Retrieve the request tracker
			 */
			Method getAttribute = reqClass.getMethod(GET_ATTRIBUTE, String.class);
			Method setAttribute = reqClass.getMethod(SET_ATTRIBUTE, String.class, Object.class);
			HttpRequestTracker tracker = (HttpRequestTracker) (getAttribute.invoke(request, TRACKER_ATTRIBUTE));
			if (tracker == null) {
				/*
				 * should never happen
				 */
				return;
			}

			/*
			 * Decrement the nesting level and return if still nested
			 */
			if (tracker.decrement()) {
				setAttribute.invoke(request, TRACKER_ATTRIBUTE, tracker);
				return;
			}

			/*
			 * Clear the tracker
			 */
			setAttribute.invoke(request, TRACKER_ATTRIBUTE, null);

			data.setRequestTime(tracker.getRequestTime());

			Method getRequestURL = reqClass.getMethod(GET_REQUEST_URL);
			data.setUrl(((StringBuffer) getRequestURL.invoke(request)).toString());

			Method getMethod = reqClass.getMethod(GET_METHOD);
			data.setMethod((String) getMethod.invoke(request));

			Method getContentType = respClass.getMethod(GET_CONTENT_TYPE);
			data.setContentType((String) getContentType.invoke(response));

			Method getHeaders = respClass.getMethod(GET_HEADER_NAMES);
			Method getHeader = respClass.getMethod(GET_HEADER, String.class);
			Collection<String> headers = (Collection<String>) getHeaders.invoke(response);
			if (headers != null) {
				for (String headerName : headers) {
					String header = (String) getHeader.invoke(response, headerName);
					if (header != null) {
						data.addHeader(headerName, header);
					}
				}
			}

			Method getReqHeaders = reqClass.getMethod(GET_HEADER_NAMES);
			Method getReqHeader = reqClass.getMethod(GET_HEADER, String.class);
			Enumeration<String> reqHeaders = (Enumeration<String>) getReqHeaders.invoke(request);
			if (reqHeaders != null) {
				while (reqHeaders.hasMoreElements()) {
					String headerName = reqHeaders.nextElement();
					String header = (String) getReqHeader.invoke(request, headerName);
					if (header != null) {
						data.addRequestHeader(headerName, header);
					}
				}
			}

			data.setDuration(System.currentTimeMillis() - tracker.getRequestTime());

			if (Agent.debug) {
				System.err.println("Javametrics: Sending {\"http\" : " + data.toJsonString() + "}");
			}

			/*
			 * Send the http request data to the Javametrics agent
			 */
			Javametrics.sendJSON(HTTP_TOPIC, data.toJsonString());

		} catch (Exception e) {
			// Log any exception caused by our injected code
			System.err.println("Javametrics: Servlet callback exception: " + e.toString());
		}

	}

}
