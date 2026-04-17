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

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.openecomp.core.dao.impl.CassandraBaseDao;
import org.openecomp.sdc.notification.dao.LastNotificationDao;
import org.openecomp.sdc.notification.dao.types.LastSeenNotificationEntity;

public class LastNotificationDaoCassandraImpl extends CassandraBaseDao<LastSeenNotificationEntity>
        implements LastNotificationDao {

    private final CqlSession session;

    public LastNotificationDaoCassandraImpl(CqlSession session) {
        super(session);
        this.session = session;
    }

    @Override
    protected Object[] getKeys(LastSeenNotificationEntity entity) {
        return new Object[]{entity.getOwnerId()};
    }

    @Override
    public Collection<LastSeenNotificationEntity> list(LastSeenNotificationEntity entity) {
        Objects.requireNonNull(entity.getOwnerId());
        String query = "SELECT owner_id, event_id FROM last_notification WHERE owner_id = ?";
        ResultSet rs = session.execute(SimpleStatement.builder(query)
                .addPositionalValue(entity.getOwnerId())
                .build());

        List<LastSeenNotificationEntity> result = new ArrayList<>();
        for (Row row : rs) {
            result.add(new LastSeenNotificationEntity(
                    row.getString("owner_id"),
                    row.getUuid("event_id")
            ));
        }
        return result;
    }

    @Override
    public UUID getOwnerLastEventId(String ownerId) {
        Objects.requireNonNull(ownerId);
        String query = "SELECT event_id FROM last_notification WHERE owner_id = ?";
        ResultSet rs = session.execute(SimpleStatement.builder(query)
                .addPositionalValue(ownerId)
                .build());

        Row row = rs.one();
        return row != null ? row.getUuid("event_id") : null;
    }

    @Override
    public void persistOwnerLastEventId(String ownerId, UUID eventId) {
        Objects.requireNonNull(ownerId);
        Objects.requireNonNull(eventId);
        String query = "UPDATE last_notification SET event_id = ? WHERE owner_id = ?";
        session.execute(SimpleStatement.builder(query)
                .addPositionalValues(eventId, ownerId)
                .build());
    }

    @Override
    protected String getTableName() {
        return "last_notification";
    }

    @Override
    protected String[] getColumns(LastSeenNotificationEntity entity) {
        return new String[]{"owner_id", "event_id"};
    }

    @Override
    protected Object[] getValues(LastSeenNotificationEntity entity) {
        return new Object[]{entity.getOwnerId(), entity.getLastEventId()};
    }
}
