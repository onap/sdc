/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.notification.websocket;

import com.google.gson.Gson;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.notification.types.NotificationsStatusDto;
import org.openecomp.sdc.notification.workers.NotificationWorker;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class NotificationWebsocketHandler extends TextWebSocketHandler {

	private static final String USER_ID_HEADER_PARAM = "USER_ID";
	private static final String LAST_DELIVERED_QUERY_PARAM = "LAST_DELIVERED_EVENT_ID";
	private static final String COOKIE = "Cookie";
	private static Logger LOGGER = LoggerFactory.getLogger(NotificationWebsocketHandler.class);
	private NotificationWorker worker;

	public NotificationWebsocketHandler(NotificationWorker worker) {
		super();
		this.worker = Objects.requireNonNull(worker, "NotificationWorker object is not initialized.");
	}

	@Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);

		String ownerId = getOwnerId(session);
		if (ownerId == null) {
			return;
		}
		UUID lastDelivered = getLastEventId(session);

		Consumer<NotificationsStatusDto> notesProcessor = (notes) -> notifyReceiver(session, notes);

		worker.register(ownerId, lastDelivered, notesProcessor);
    }

	@Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		String ownerId = getOwnerId(session);
		if (ownerId != null) {
			worker.unregister(ownerId);
		}
	    super.afterConnectionClosed(session, status);
    }

	private void notifyReceiver(WebSocketSession session, NotificationsStatusDto notificationsStatusDto) {

		try {
			session.sendMessage(new TextMessage(new Gson().toJson(notificationsStatusDto)));
		} catch (IOException e) {
			LOGGER.error("IO Exception during Receiver notification.", e);
		}
	}

    private String getOwnerId(WebSocketSession session) {

        HttpHeaders handshakeHeaders = session.getHandshakeHeaders();
        if (handshakeHeaders.containsKey(COOKIE)) {
			String[] cookies = handshakeHeaders.get(COOKIE).get(0).split("; ");
			Optional<String> cookie = extractValue(cookies, USER_ID_HEADER_PARAM);
			if (cookie.isPresent()) {
				return cookie.get();
			}
		}

		LOGGER.error("No " + USER_ID_HEADER_PARAM + " specified in the session cookies.");
		return null;
    }

    private UUID getLastEventId(WebSocketSession session) {

        String uriQuery = session.getUri().getQuery();
        if (uriQuery != null) {

            String[] queries = uriQuery.split("; ");
            Optional<String> paramValue = extractValue(queries, LAST_DELIVERED_QUERY_PARAM);
            if (paramValue.isPresent()) {
                return UUID.fromString(paramValue.get());
            }
        }

        LOGGER.warn("No " + LAST_DELIVERED_QUERY_PARAM + " specified in the request URI.");
        return null;
    }

	private Optional<String> extractValue(String[] pairs, String name) {

		for (String nameValuePair : pairs) {

            String[] value = nameValuePair.split("=");
            if (value[0].equals(name)) {
                return Optional.of(value[1]);
            }
        }

		return Optional.empty();
	}

}
