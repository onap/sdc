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

package org.openecomp.sdc.versioning.dao.impl;

import com.datastax.driver.core.UDTValue;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.UDTMapper;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.dao.impl.CassandraBaseDao;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.versioning.dao.VersionHistoryDao;
import org.openecomp.sdc.versioning.dao.types.VersionHistoryEntity;
import org.openecomp.sdc.versioning.dao.types.VersionableEntityId;

import java.util.Collection;

public class VersionHistoryCassandraDaoImpl extends CassandraBaseDao<VersionHistoryEntity>
    implements VersionHistoryDao {

  private static NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static Mapper<VersionHistoryEntity> mapper =
      noSqlDb.getMappingManager().mapper(VersionHistoryEntity.class);
  private static VersionHistoryAccessor accessor =
      noSqlDb.getMappingManager().createAccessor(VersionHistoryAccessor.class);
  private static UDTMapper<VersionableEntityId> versionedEntityIdMapper =
      noSqlDb.getMappingManager().udtMapper(VersionableEntityId.class);

  @Override
  protected Mapper<VersionHistoryEntity> getMapper() {
    return mapper;
  }

  @Override
  protected Object[] getKeys(VersionHistoryEntity entity) {
    return new Object[]{versionedEntityIdMapper.toUDT(entity.getEntityId())};
  }

  @Override
  public Collection<VersionHistoryEntity> list(VersionHistoryEntity entity) {
    return accessor.getAll(versionedEntityIdMapper.toUDT(entity.getEntityId())).all();
  }

  @Accessor
  interface VersionHistoryAccessor {
    @Query("select * from version_history where entity_id=?")
    Result<VersionHistoryEntity> getAll(UDTValue entityId);
  }
}
