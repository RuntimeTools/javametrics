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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * Websocket Endpoint implementation for JavametricsWebSocket
 */

@ServerEndpoint(value = "/", subprotocols = "javametrics-dash")
public class JavametricsWebSocket implements Emitter {

	private Set<Session> openSessions = new HashSet<>();

	public JavametricsWebSocket() {
		super();
	}

	@OnOpen
	public void open(Session session) {
		try {
			session.getBasicRemote().sendText(
					"{\"topic\": \"title\", \"payload\": {\"title\":\"Application Metrics for Java\", \"docs\": \"http://github.com/RuntimeTools/javametrics\"}}");
		} catch (IOException e) {
			e.printStackTrace();
		}
		openSessions.add(session);
        DataHandler.registerEmitter(this);
	}

	@OnClose
	public void close(Session session) {
        DataHandler.deregisterEmitter(this);
		openSessions.remove(session);
	}

	@OnError
	public void onError(Throwable error) {
	}

	@OnMessage
	public void handleMessage(String message, Session session) {
	}

	/* (non-Javadoc)
	 * @see com.ibm.javametrics.MetricsEmitter#emit(java.lang.String)
	 */
	public void emit(String message) {
		openSessions.forEach((session) -> {
			try {
				if (session.isOpen()) {
					session.getBasicRemote().sendText(message);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}


}
