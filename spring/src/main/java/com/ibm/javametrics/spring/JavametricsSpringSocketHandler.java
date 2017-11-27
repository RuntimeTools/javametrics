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
import java.util.HashSet;
import java.util.Set;

import com.ibm.javametrics.web.DataHandler;
import com.ibm.javametrics.web.Emitter;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class JavametricsSpringSocketHandler extends TextWebSocketHandler implements Emitter {

	private Set<WebSocketSession> openSessions = new HashSet<>();

	public JavametricsSpringSocketHandler() {
		super();
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		openSessions.add(session);
		emit("{\"topic\": \"title\", \"payload\": {\"title\":\"Application Metrics for Java\", \"docs\": \"http://github.com/RuntimeTools/javametrics\"}}");
		DataHandler.registerEmitter(this);
	}
	
	public void emit(String message) {
		for (WebSocketSession webSocketSession : openSessions) {
			try {
				webSocketSession.sendMessage(new TextMessage(message));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
