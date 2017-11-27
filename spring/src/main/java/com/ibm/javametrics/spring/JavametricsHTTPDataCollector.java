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
package com.ibm.javametrics.spring;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ibm.javametrics.Javametrics;
import com.ibm.javametrics.instrument.HttpData;

@Component
public class JavametricsHTTPDataCollector extends OncePerRequestFilter { 

    private static final String HTTP_TOPIC = "http";
    
    @Override 
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
                    throws ServletException, IOException {
    	long startTime = System.currentTimeMillis();
    	filterChain.doFilter(request, response);
        long duration = System.currentTimeMillis() - startTime;
        HttpData data = new HttpData();
        data.setRequestTime(startTime);
        data.setUrl(request.getRequestURL().toString());
        data.setMethod(request.getMethod());
        // TODO: make setStatus public
        //data.setStatus(response.getStatus());
        data.setContentType(response.getContentType());
        for (String headerName : response.getHeaderNames()) {
        	for(String header: response.getHeaders(headerName)) {
                data.addHeader(headerName, header);
        	}
		}
        Enumeration<String> reqHeaders = (Enumeration<String>) request.getHeaderNames();
        if (reqHeaders != null) {
            while (reqHeaders.hasMoreElements()) {
                String headerName = reqHeaders.nextElement();
                String header = request.getHeader(headerName);
                if (header != null) {
                    data.addRequestHeader(headerName, header);
                }
            }
        }
        data.setDuration(duration);
        Javametrics.getInstance().sendJSON(HTTP_TOPIC, data.toJsonString());
    	System.out.println("request: " + request.getRequestURL().toString() + " took " + duration + " milliseconds");
    } 
}