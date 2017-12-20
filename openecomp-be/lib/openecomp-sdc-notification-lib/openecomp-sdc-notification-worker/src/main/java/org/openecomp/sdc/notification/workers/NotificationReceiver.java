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

import org.openecomp.sdc.notification.types.NotificationsStatusDto;

import java.util.UUID;
import java.util.function.Consumer;



public class NotificationReceiver {

	public String ownerId = null;
	public Consumer<NotificationsStatusDto> notesProcessor = null;
	public UUID lastEventId = null;

	NotificationReceiver(String ownerId, UUID lastEventId, Consumer<NotificationsStatusDto> notesProcessor) {
		this.ownerId = ownerId;
		this.lastEventId = lastEventId;
		this.notesProcessor = notesProcessor;
	}

	NotificationReceiver(String ownerId, Consumer<NotificationsStatusDto> notesProcessor) {
		this(ownerId, null, notesProcessor);
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public String getOwnerId() {
		return this.ownerId;
	}

	public void setNotesProcessor(Consumer<NotificationsStatusDto> notesProcessor) {
		this.notesProcessor = notesProcessor;
	}

	public Consumer<NotificationsStatusDto> getNotesProcessor() {
		return this.notesProcessor;
	}

	public void setLastEventId(UUID lastEventId) {
		this.lastEventId = lastEventId;
	}

	public UUID getlastEventId() {
		return this.lastEventId;
	}
}
