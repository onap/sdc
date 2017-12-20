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

import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import com.google.common.collect.Sets;
import org.openecomp.core.dao.impl.CassandraBaseDao;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.notification.dao.SubscribersDao;
import org.openecomp.sdc.notification.dao.types.SubscribersEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.isNull;

public class SubscribersDaoCassandraImpl extends CassandraBaseDao<SubscribersEntity> implements
        SubscribersDao {

    private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
    private static final Mapper<SubscribersEntity> mapper =
            noSqlDb.getMappingManager().mapper(SubscribersEntity.class);
    private static final SubscribersAccessor accessor =
            noSqlDb.getMappingManager().createAccessor(SubscribersAccessor.class);


    @Override
    protected Object[] getKeys(SubscribersEntity entity) {
        return new Object[]{entity.getEntityId()};
    }

    @Override
    protected Mapper<SubscribersEntity> getMapper() {
        return mapper;
    }

    @Override
    public void subscribe(String ownerId, String entityId) {
        Objects.requireNonNull(ownerId);
        Objects.requireNonNull(entityId);
        accessor.subscribe(Sets.newHashSet(ownerId), entityId);
    }

    @Override
    @Deprecated
    public Collection<SubscribersEntity> list(SubscribersEntity entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unsubscribe(String ownerId, String entityId) {
        Objects.requireNonNull(ownerId);
        Objects.requireNonNull(entityId);
        accessor.unsubscribe(Sets.newHashSet(ownerId), entityId);
    }

    @Override
    public Set<String> getSubscribers(String entityId) {
        Objects.requireNonNull(entityId);
        SubscribersEntity subscribersEntity = accessor.getSubscribers(entityId).one();
        if (isNull(subscribersEntity)) {
            return Collections.emptySet();
        }
        return subscribersEntity.getSubscribers();
    }

    @Accessor
    interface SubscribersAccessor {

        @Query("select * from notification_subscribers where entity_id=?")
        Result<SubscribersEntity> getSubscribers(String entityId);

        @Query("update notification_subscribers set subscribers=subscribers+? WHERE entity_id=?")
        void subscribe(Set<String> ownerId, String entityId);

        @Query("update notification_subscribers set subscribers=subscribers-? WHERE entity_id=?")
        void unsubscribe(Set<String> ownerId, String entityId);
    }

}
