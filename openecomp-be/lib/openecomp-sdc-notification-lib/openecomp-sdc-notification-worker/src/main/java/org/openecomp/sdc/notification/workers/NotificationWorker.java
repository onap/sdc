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

package org.openecomp.sdc.notification.workers;

import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.notification.config.ConfigurationManager;
import org.openecomp.sdc.notification.types.NotificationsStatusDto;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class NotificationWorker {

	private static final int DEFAULT_POLLING_INTERVAL = 2000;
	private static final String POLLING_INTERVAL = "pollingIntervalMsec";
	private static final int DEFAULT_SELECTION_LIMIT = 10;
	private static final String SELECTION_SIZE = "selectionSize";

	private static boolean stopRunning = false;

	private int selectionLimit = DEFAULT_SELECTION_LIMIT;
	private int pollingSleepInterval = DEFAULT_POLLING_INTERVAL;

	private static final Logger LOGGER = LoggerFactory.getLogger(NotificationWorker.class);

	private static Map<String, NotificationReceiver> activeUsers = new ConcurrentHashMap<>();
	private NewNotificationsReader news = null;

	public NotificationWorker(NewNotificationsReader news) {
		ConfigurationManager cm = ConfigurationManager.getInstance();
		pollingSleepInterval = cm.getConfigValue(POLLING_INTERVAL, DEFAULT_POLLING_INTERVAL);
		selectionLimit = cm.getConfigValue(SELECTION_SIZE, DEFAULT_SELECTION_LIMIT);

		Objects.requireNonNull(news, "NotificationNews object is not initialized.");
		this.news = news;

		NotificationWorker.Poller p = new Poller();
		Thread thread = new Thread(p);
		thread.start();
	}

	public Map<String, NotificationReceiver> getActiveUsers() {
		return activeUsers;
	}

	public class Poller extends Thread {
		public void run() {
			try {
				while (!stopRunning) {
					pollNotifications();
					Thread.sleep(pollingSleepInterval);
				}
			}
			catch (InterruptedException e) {
				LOGGER.error("Interrupted Exception during Notification poller launch.", e);
			}
		}

		private void pollNotifications() {

			Map<String, NotificationReceiver> currUsers = new HashMap<>();
			currUsers.putAll(getActiveUsers());

			for (NotificationReceiver receiver : currUsers.values()) {
				String ownerId = receiver.getOwnerId();
				UUID eventId = receiver.getlastEventId();
				NotificationsStatusDto status = news.getNewNotifications(ownerId, eventId, selectionLimit);
				if(Objects.nonNull(status) && CollectionUtils.isNotEmpty(status.getNotifications())) {
					receiver.setLastEventId(status.getLastScanned());
					receiver.getNotesProcessor().accept(status);
				}
			}
		}

	}

	public void register(String ownerId, UUID lastDelivered, Consumer<NotificationsStatusDto> notesProcessor) {
		NotificationReceiver receiver = new NotificationReceiver(ownerId, lastDelivered, notesProcessor);
		activeUsers.put(ownerId, receiver);
		LOGGER.debug("User {} is registered with eventId: {}", ownerId, receiver.getlastEventId());
	}

	public void unregister(String ownerId) {
		activeUsers.remove(ownerId);
		LOGGER.debug("User {} is unregistered.", ownerId);
	}

	public void stopPolling() {
		LOGGER.debug("Stop notification polling.");
		stopRunning = true;
	}

}
