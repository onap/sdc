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

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.core.dao.impl.CassandraBaseDao;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.notification.dao.NotificationsDao;
import org.openecomp.sdc.notification.dao.types.NotificationEntity;
import org.openecomp.sdc.notification.dtos.NotificationsStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.openecomp.core.nosqldb.impl.cassandra.CassandraSessionFactory.getSession;

//import org.openecomp.sdc.notification.dao.types.LastSeenNotificationEntity;
//import java.util.Optional;

public class NotificationsDaoCassandraImpl extends CassandraBaseDao<NotificationEntity>
    implements NotificationsDao {

    private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
    private static final Mapper<NotificationEntity> mapper =
        noSqlDb.getMappingManager().mapper(NotificationEntity.class);
    private static final NotificationsAccessor accessor =
        noSqlDb.getMappingManager().createAccessor(NotificationsAccessor.class);

    @Override
    protected Mapper<NotificationEntity> getMapper() {
        return mapper;
    }

    @Override
    protected Object[] getKeys(NotificationEntity entity) {
        return new Object[]{entity.getOwnerId(), entity.getEventId()};
    }

    @Override
    public List<NotificationEntity> list(NotificationEntity entity) {
        return accessor.list(entity.getOwnerId()).all();
    }

    @Override
    public List<NotificationEntity> getNotificationsByOwnerId(String ownerId, int limit) {
        return accessor.getNotifications(ownerId, limit).all();
    }

    @Override
    public List<NotificationEntity> getNewNotificationsByOwnerId(String ownerId, UUID eventId) {
        return getNewNotificationsByOwnerId(ownerId, eventId,
            DEFAULT_LIMIT_OF_RESULTS_FOR_OWNER_NOTIFICATIONS);
    }

    @Override
    public List<NotificationEntity> getNewNotificationsByOwnerId(String ownerId, UUID eventId, int limit) {
        if (Objects.isNull(eventId)) {
            return getNotificationsByOwnerId(ownerId, limit);
        }
        return accessor.getNewNotifications(ownerId, eventId, limit).all();
    }

    @Override
    public void markNotificationAsRead(String ownerId, Collection<UUID> eventIds) {
        eventIds.forEach(eventId -> accessor.markAsRead(ownerId, eventId));
    }

    @Override
    public NotificationsStatus getNotificationsStatus(String ownerId, UUID lastScannedEventId, int numOfRecordsToReturn) {
	NotificationsStatusImpl notificationsStatus = new NotificationsStatusImpl();
	List<NotificationEntity> entities = accessor.getNotifications(ownerId, numOfRecordsToReturn).all();
        if (CollectionUtils.isNotEmpty(entities)) {
            long lastSeen = UUIDs.unixTimestamp(lastScannedEventId);
            populateNewNotifications(notificationsStatus, entities, lastSeen);
            UUID firstScannedEventId = entities.get(0).getEventId();
		notificationsStatus.setLastScanned(firstScannedEventId);
		notificationsStatus.setNumOfNotSeenNotifications(accessor.getNewNotificationsCount(ownerId, lastScannedEventId, firstScannedEventId).one().getLong(0));
        }
	return notificationsStatus;
    }

    private void populateNewNotifications(NotificationsStatusImpl notificationsStatus, List<NotificationEntity> entities, long lastSeen) {
        for (NotificationEntity entity : entities) {
            UUID eventId = entity.getEventId();
            notificationsStatus.addNotification(entity);
            if (UUIDs.unixTimestamp(eventId) > lastSeen) {
                notificationsStatus.addNewNotificationUUID(eventId);
            }
        }
    }

    @Override
    public NotificationsStatus getNotificationsStatus(String ownerId, UUID lastSeenNotification, int numOfRecordsToReturn, UUID prevLastScannedEventId) {
	NotificationsStatusImpl notificationsStatus = new NotificationsStatusImpl();
	List<NotificationEntity> entities = accessor.getPrevNotifications(ownerId, prevLastScannedEventId, numOfRecordsToReturn).all();
        if (CollectionUtils.isNotEmpty(entities)) {
		long lastSeen = UUIDs.unixTimestamp(lastSeenNotification);
            populateNewNotifications(notificationsStatus, entities, lastSeen);
        }
	return notificationsStatus;
    }

/*
    @Override
    public NotificationsStatus getNotificationsStatus(String ownerId,
                                                      LastSeenNotificationEntity lastSeenNotification,
                                                      int numOfRecordsToReturn) {

        List<NotificationEntity> notificationEntities =
            fetchNewNotifications(lastSeenNotification, numOfRecordsToReturn);
        NotificationsStatusImpl notificationsStatus = new NotificationsStatusImpl();
        if (CollectionUtils.isEmpty(notificationEntities)) {
            return notificationsStatus;
        }

        notificationEntities.forEach(notification -> {
            if (isNewNotification(lastSeenNotification, notification)) {
                notificationsStatus.addNewNotificationUUID(notification.getEventId());
            }
            notificationsStatus.addNotification(notification);
        });

        Optional<NotificationEntity> latestNotification = notificationEntities.stream().findFirst();
        latestNotification.ifPresent(e -> notificationsStatus.setLastScanned(e.getEventId()));
        return notificationsStatus;
    }

    private List<NotificationEntity> fetchNewNotifications(
        LastSeenNotificationEntity lastSeenNotification, int numOfRecordsToReturn) {
        String ownerId = lastSeenNotification.getOwnerId();
        UUID lastEventId = lastSeenNotification.getLastEventId();
        List<NotificationEntity> newNotificationsByOwnerId =
            getNewNotificationsByOwnerId(ownerId, lastEventId);
        newNotificationsByOwnerId = fetchMoreIfNeeded(ownerId, newNotificationsByOwnerId,
            numOfRecordsToReturn, lastEventId);
        return newNotificationsByOwnerId;
    }

    private boolean isNewNotification(LastSeenNotificationEntity lastSeenNotification,
                                      NotificationEntity notification) {
        return Objects.isNull(lastSeenNotification.getLastEventId()) ||
            UUIDs.unixTimestamp(notification.getEventId()) >
                UUIDs.unixTimestamp(lastSeenNotification.getLastEventId());
    }
*/

    @Override
    public void createBatch(List<NotificationEntity> notificationEntities) {
        BatchStatement batch = new BatchStatement();
        List<Statement> statements = notificationEntities.stream()
            .map(mapper::saveQuery)
            .collect(Collectors.toList());
        batch.addAll(statements);
        getSession().execute(batch);
    }

    @Accessor
    interface NotificationsAccessor {

        @Query("select * from notifications where owner_id=?")
        Result<NotificationEntity> list(String ownerId);

        @Query("select * from notifications where owner_id=? limit ?")
        Result<NotificationEntity> getNotifications(String ownerId, int limit);

        @Query("select * from notifications where owner_id=? and event_id > ? limit ?")
        Result<NotificationEntity> getNewNotifications(String ownerId, UUID lastScannedEventId, int limit);

        @Query("select * from notifications where owner_id=? and event_id < ? limit ?")
        Result<NotificationEntity> getPrevNotifications(String ownerId, UUID prevLastScannedEventId, int limit);

        @Query("select count(*) from notifications where owner_id=? and event_id > ? and event_id <= ?")
        ResultSet getNewNotificationsCount(String ownerId, UUID lastScannedEventId, UUID firstScannedEventId);

        @Query("update notifications set read=true where owner_id=? and event_id=?")
        ResultSet markAsRead(String ownerId, UUID eventId);
    }

    private class NotificationsStatusImpl implements NotificationsStatus {

        private List<NotificationEntity> notifications = new ArrayList<>();
        private List<UUID> newEntries = new ArrayList<>();
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

/*
    private List<NotificationEntity> fetchMoreIfNeeded(String ownerId,
                                                       List<NotificationEntity> notificationEntities,
                                                       int numOfRecordsToReturn, UUID lastEventId) {

        if (numOfRecordsToReturn <= notificationEntities.size() || Objects.isNull(lastEventId)) {
            return notificationEntities;
        }

        int multiplier = 2;
        while (numOfRecordsToReturn > notificationEntities.size()) {

            int bring = notificationEntities.size() +
                (numOfRecordsToReturn - notificationEntities.size()) * multiplier;
            notificationEntities = getNotificationsByOwnerId(ownerId, bring);

            if (notificationEntities.size() < bring) {
                return notificationEntities;
            }
            multiplier++;
        }
        return notificationEntities;
    }
*/

}
