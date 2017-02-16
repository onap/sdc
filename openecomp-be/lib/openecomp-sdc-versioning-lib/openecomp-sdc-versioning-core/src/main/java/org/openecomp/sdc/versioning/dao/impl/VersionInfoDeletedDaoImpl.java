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

import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.dao.impl.CassandraBaseDao;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.versioning.dao.VersionInfoDeletedDao;
import org.openecomp.sdc.versioning.dao.types.VersionInfoDeletedEntity;

import java.util.Collection;

public class VersionInfoDeletedDaoImpl extends CassandraBaseDao<VersionInfoDeletedEntity>
    implements VersionInfoDeletedDao {

  private static NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static Mapper<VersionInfoDeletedEntity> mapper =
      noSqlDb.getMappingManager().mapper(VersionInfoDeletedEntity.class);
  private static VersionInfoAccessor accessor =
      noSqlDb.getMappingManager().createAccessor(VersionInfoAccessor.class);


  @Override
  protected Mapper<VersionInfoDeletedEntity> getMapper() {
    return mapper;
  }

  @Override
  protected Object[] getKeys(VersionInfoDeletedEntity entity) {
    return new Object[]{entity.getEntityType(), entity.getEntityId()};
  }

  @Override
  public Collection<VersionInfoDeletedEntity> list(VersionInfoDeletedEntity entity) {
    return accessor.getAll(entity.getEntityType()).all();
  }

  @Accessor
  interface VersionInfoAccessor {
    @Query("select * from version_info_deleted where entity_type=?")
    Result<VersionInfoDeletedEntity> getAll(String entityType);
  }
}
