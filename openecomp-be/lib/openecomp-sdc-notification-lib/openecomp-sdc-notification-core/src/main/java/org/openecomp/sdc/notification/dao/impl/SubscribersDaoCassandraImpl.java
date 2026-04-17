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

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;

import org.openecomp.core.dao.impl.CassandraBaseDao;
import org.openecomp.sdc.notification.dao.SubscribersDao;
import org.openecomp.sdc.notification.dao.types.SubscribersEntity;

public class SubscribersDaoCassandraImpl extends CassandraBaseDao<SubscribersEntity> implements SubscribersDao {

    private final CqlSession session;

    public SubscribersDaoCassandraImpl(CqlSession session) {
        super(session);
        this.session = session;
    }

    @Override
    protected Object[] getKeys(SubscribersEntity entity) {
        return new Object[]{entity.getEntityId()};
    }

    @Override
    public void subscribe(String ownerId, String entityId) {
        Objects.requireNonNull(ownerId, "ownerId must not be null");
        Objects.requireNonNull(entityId, "entityId must not be null");

        String query = "UPDATE notification_subscribers SET subscribers = subscribers + ? WHERE entity_id = ?";
        session.execute(SimpleStatement.newInstance(query, Set.of(ownerId), entityId));
    }

    @Override
    public void unsubscribe(String ownerId, String entityId) {
        Objects.requireNonNull(ownerId, "ownerId must not be null");
        Objects.requireNonNull(entityId, "entityId must not be null");

        String query = "UPDATE notification_subscribers SET subscribers = subscribers - ? WHERE entity_id = ?";
        session.execute(SimpleStatement.newInstance(query, Set.of(ownerId), entityId));
    }

    @Override
    public Set<String> getSubscribers(String entityId) {
        Objects.requireNonNull(entityId, "entityId must not be null");

        String query = "SELECT subscribers FROM notification_subscribers WHERE entity_id = ?";
        ResultSet rs = session.execute(SimpleStatement.newInstance(query, entityId));
        Row row = rs.one();

        if (isNull(row)) {
            return Collections.emptySet();
        }

        Set<String> subscribers = row.getSet("subscribers", String.class);
        return subscribers != null ? subscribers : Collections.emptySet();
    }

    @Override
    @Deprecated
    public Collection<SubscribersEntity> list(SubscribersEntity entity) {
        throw new UnsupportedOperationException("list is not supported");
    }

    @Override
    protected String getTableName() {
        return "notification_subscribers";
    }

    @Override
    protected String[] getColumns(SubscribersEntity entity) {
        return new String[]{"entity_id", "subscribers"};
    }

    @Override
    protected Object[] getValues(SubscribersEntity entity) {
        return new Object[]{entity.getEntityId(), entity.getSubscribers()};
    }
}
