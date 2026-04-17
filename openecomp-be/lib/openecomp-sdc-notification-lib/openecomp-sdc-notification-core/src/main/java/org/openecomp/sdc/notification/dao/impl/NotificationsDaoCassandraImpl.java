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
package org.openecomp.sdc.notification.dao.impl;

import static java.util.Objects.isNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.uuid.Uuids;

import org.apache.commons.collections.CollectionUtils;
import org.openecomp.core.dao.impl.CassandraBaseDao;
import org.openecomp.sdc.notification.dao.NotificationsDao;
import org.openecomp.sdc.notification.dao.types.NotificationEntity;
import org.openecomp.sdc.notification.dtos.NotificationsStatus;

public class NotificationsDaoCassandraImpl extends CassandraBaseDao<NotificationEntity> implements NotificationsDao {

    private final CqlSession session;

    public NotificationsDaoCassandraImpl(CqlSession session) {
        super(session);
        this.session = session;
    }

    @Override
    protected Object[] getKeys(NotificationEntity entity) {
        return new Object[]{entity.getOwnerId(), entity.getEventId()};
    }

    @Override
    public List<NotificationEntity> list(NotificationEntity entity) {
        String query = "SELECT * FROM notifications WHERE owner_id = ?";
        ResultSet rs = session.execute(SimpleStatement.newInstance(query, entity.getOwnerId()));
        List<NotificationEntity> list = new ArrayList<>();
        for (Row row : rs) {
            list.add(mapRow(row));
        }
        return list;
    }

    @Override
    public List<NotificationEntity> getNotificationsByOwnerId(String ownerId, int limit) {
        String query = "SELECT * FROM notifications WHERE owner_id = ? LIMIT ?";
        ResultSet rs = session.execute(SimpleStatement.newInstance(query, ownerId, limit));
        List<NotificationEntity> list = new ArrayList<>();
        for (Row row : rs) {
            list.add(mapRow(row));
        }
        return list;
    }

    @Override
    public List<NotificationEntity> getNewNotificationsByOwnerId(String ownerId, UUID eventId) {
        return getNewNotificationsByOwnerId(ownerId, eventId, DEFAULT_LIMIT_OF_RESULTS_FOR_OWNER_NOTIFICATIONS);
    }

    @Override
    public List<NotificationEntity> getNewNotificationsByOwnerId(String ownerId, UUID eventId, int limit) {
        String query = (eventId == null)
                ? "SELECT * FROM notifications WHERE owner_id = ? LIMIT ?"
                : "SELECT * FROM notifications WHERE owner_id = ? AND event_id > ? LIMIT ?";

        ResultSet rs = (eventId == null)
                ? session.execute(SimpleStatement.newInstance(query, ownerId, limit))
                : session.execute(SimpleStatement.newInstance(query, ownerId, eventId, limit));

        List<NotificationEntity> list = new ArrayList<>();
        for (Row row : rs) {
            list.add(mapRow(row));
        }
        return list;
    }

    @Override
    public void markNotificationAsRead(String ownerId, Collection<UUID> eventIds) {
        String query = "UPDATE notifications SET read = true WHERE owner_id = ? AND event_id = ?";
        for (UUID eventId : eventIds) {
            session.execute(SimpleStatement.newInstance(query, ownerId, eventId));
        }
    }

    @Override
    public NotificationsStatus getNotificationsStatus(String ownerId, UUID lastScannedEventId, int numOfRecordsToReturn) {
        NotificationsStatusImpl notificationsStatus = new NotificationsStatusImpl();
        List<NotificationEntity> entities = getNotificationsByOwnerId(ownerId, numOfRecordsToReturn);
        if (CollectionUtils.isNotEmpty(entities)) {
            long lastSeen = Uuids.unixTimestamp(lastScannedEventId);
            populateNewNotifications(notificationsStatus, entities, lastSeen);
            UUID firstScannedEventId = entities.get(0).getEventId();
            notificationsStatus.setLastScanned(firstScannedEventId);

            // count unseen
            String countQuery = "SELECT count(*) FROM notifications WHERE owner_id = ? AND event_id > ? AND event_id <= ?";
            ResultSet rs = session.execute(SimpleStatement.newInstance(countQuery, ownerId, lastScannedEventId, firstScannedEventId));
            Row row = rs.one();
            if (!isNull(row)) {
                notificationsStatus.setNumOfNotSeenNotifications(row.getLong(0));
            }
        }
        return notificationsStatus;
    }

    @Override
    public NotificationsStatus getNotificationsStatus(String ownerId, UUID lastSeenNotification, int numOfRecordsToReturn, UUID prevLastScannedEventId) {
        NotificationsStatusImpl notificationsStatus = new NotificationsStatusImpl();
        String query = "SELECT * FROM notifications WHERE owner_id = ? AND event_id < ? LIMIT ?";
        ResultSet rs = session.execute(SimpleStatement.newInstance(query, ownerId, prevLastScannedEventId, numOfRecordsToReturn));

        List<NotificationEntity> entities = new ArrayList<>();
        for (Row row : rs) {
            entities.add(mapRow(row));
        }

        if (CollectionUtils.isNotEmpty(entities)) {
            long lastSeen = Uuids.unixTimestamp(lastSeenNotification);
            populateNewNotifications(notificationsStatus, entities, lastSeen);
        }
        return notificationsStatus;
    }

    @Override
    public void createBatch(List<NotificationEntity> notificationEntities) {
        BatchStatementBuilder batchBuilder = new BatchStatementBuilder(BatchType.LOGGED);
        String query = "INSERT INTO notifications (owner_id, event_id, read, event_type, event_attributes, originator_id) VALUES (?, ?, ?, ?, ?, ?)";
        for (NotificationEntity entity : notificationEntities) {
            batchBuilder.addStatement(SimpleStatement.newInstance(query,
                    entity.getOwnerId(),
                    entity.getEventId(),
                    entity.isRead(),
                    entity.getEventType(),
                    entity.getEventAttributes(),
                    entity.getOriginatorId()
            ));
        }
        session.execute(batchBuilder.build());
    }

    private void populateNewNotifications(NotificationsStatusImpl notificationsStatus, List<NotificationEntity> entities, long lastSeen) {
        for (NotificationEntity entity : entities) {
            UUID eventId = entity.getEventId();
            notificationsStatus.addNotification(entity);
            if (Uuids.unixTimestamp(eventId) > lastSeen) {
                notificationsStatus.addNewNotificationUUID(eventId);
            }
        }
    }

    private NotificationEntity mapRow(Row row) {
        if (row == null) {
            return null;
        }
        return new NotificationEntity(
                row.getString("owner_id"),
                row.getBoolean("read"),
                row.getUuid("event_id"),
                row.getString("event_type"),
                row.getString("event_attributes"),
                row.getString("originator_id")
        );
    }

    private class NotificationsStatusImpl implements NotificationsStatus {
        private final List<NotificationEntity> notifications = new ArrayList<>();
        private final List<UUID> newEntries = new ArrayList<>();
        private UUID lastScanned;
        private UUID endOfPage;
        private long numOfNotSeenNotifications = 0;

        void addNotification(NotificationEntity notification) {
            notifications.add(notification);
            endOfPage = notification.getEventId();
        }

        void addNewNotificationUUID(UUID notificationUuid) {
            newEntries.add(notificationUuid);
        }

        @Override
        public List<NotificationEntity> getNotifications() {
            return Collections.unmodifiableList(notifications);
        }

        @Override
        public List<UUID> getNewEntries() {
            return Collections.unmodifiableList(newEntries);
        }

        @Override
        public UUID getLastScanned() {
            return lastScanned;
        }

        void setLastScanned(UUID lastScanned) {
            this.lastScanned = lastScanned;
        }

        @Override
        public UUID getEndOfPage() {
            return endOfPage;
        }

        @Override
        public long getNumOfNotSeenNotifications() {
            return numOfNotSeenNotifications;
        }

        void setNumOfNotSeenNotifications(long numOfNotSeenNotifications) {
            this.numOfNotSeenNotifications = numOfNotSeenNotifications;
        }
    }

    @Override
    protected String getTableName() {
        return "notifications";
    }

    @Override
    protected String[] getColumns(NotificationEntity entity) {
        return new String[]{
                "owner_id", "read", "event_id", "event_type", "event_attributes", "originator_id"
        };
    }

    @Override
    protected Object[] getValues(NotificationEntity entity) {
        return new Object[]{
                entity.getOwnerId(),
                entity.isRead(),
                entity.getEventId(),
                entity.getEventType(),
                entity.getEventAttributes(),
                entity.getOriginatorId()
        };
    }
}
