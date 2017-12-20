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

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.dao.impl.CassandraBaseDao;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.notification.dao.LastNotificationDao;
import org.openecomp.sdc.notification.dao.types.LastSeenNotificationEntity;

import java.util.Collection;
import java.util.UUID;


public class LastNotificationDaoCassandraImpl extends CassandraBaseDao<LastSeenNotificationEntity> implements LastNotificationDao {

  private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static final Mapper<LastSeenNotificationEntity> mapper =
      noSqlDb.getMappingManager().mapper(LastSeenNotificationEntity.class);
  private static final LastNotificationAccessor accessor =
      noSqlDb.getMappingManager().createAccessor(LastNotificationAccessor.class);

  @Override
  protected Mapper<LastSeenNotificationEntity> getMapper() {
    return mapper;
  }

  @Override
  protected Object[] getKeys(LastSeenNotificationEntity entity) {
    return new Object[]{entity.getOwnerId()};
  }

  @Override
  public Collection<LastSeenNotificationEntity> list(LastSeenNotificationEntity entity) {
    return accessor.list(entity.getOwnerId()).all();
  }

  @Override
  public UUID getOwnerLastEventId(String ownerId) {
    ResultSet ownerLastEventId = accessor.getOwnerLastEventId(ownerId);
    Row one = ownerLastEventId.one();
    return one != null ? one.getUUID("event_id") : null;
  }

  @Override
  public void persistOwnerLastEventId(String ownerId, UUID eventId) {
	    accessor.updateOwnerLastEventId(eventId, ownerId);
  }

  @Accessor
  interface LastNotificationAccessor {

    @Query("select * from last_notification where owner_id=?")
    Result<LastSeenNotificationEntity> list(String ownerId);

    @Query("select event_id from last_notification where owner_id=?")
    ResultSet getOwnerLastEventId(String ownerId);

    @Query("update last_notification set event_id=? where owner_id=?")
    ResultSet updateOwnerLastEventId(UUID eventId, String ownerId);
  }

}
