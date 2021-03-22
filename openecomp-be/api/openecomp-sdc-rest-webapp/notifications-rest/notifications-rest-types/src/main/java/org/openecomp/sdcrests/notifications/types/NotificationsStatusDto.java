/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdcrests.notifications.types;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author avrahamg
 * @since June 29, 2017
 */
public class NotificationsStatusDto {

    private List<NotificationEntityDto> notifications;
    private List<UUID> newEntries = new ArrayList<>();
    private UUID lastScanned;
    private UUID endOfPage;
    private long numOfNotSeenNotifications;

    public NotificationsStatusDto() {
    }

    public List<NotificationEntityDto> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<NotificationEntityDto> notifications) {
        this.notifications = notifications;
    }

    public List<UUID> getNewEntries() {
        return newEntries;
    }

    public void setNewEntries(List<UUID> newEntries) {
        this.newEntries = newEntries;
    }

    public UUID getLastScanned() {
        return lastScanned;
    }

    public void setLastScanned(UUID lastScanned) {
        this.lastScanned = lastScanned;
    }

    public UUID getEndOfPage() {
        return endOfPage;
    }

    public void setEndOfPage(UUID endOfPage) {
        this.endOfPage = endOfPage;
    }

    public long getNumOfNotSeenNotifications() {
        return numOfNotSeenNotifications;
    }

    public void setNumOfNotSeenNotifications(long numOfNotSeenNotifications) {
        this.numOfNotSeenNotifications = numOfNotSeenNotifications;
    }
}
