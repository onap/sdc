/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdc.versioning.dao.impl;

import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.dao.impl.CassandraBaseDao;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.sdc.versioning.dao.VersionInfoDao;
import org.openecomp.sdc.versioning.dao.types.VersionInfoEntity;

import java.util.Collection;

public class VersionInfoDaoImpl extends CassandraBaseDao<VersionInfoEntity>
    implements VersionInfoDao {


  private final NoSqlDb noSqlDb;
  private final Mapper<VersionInfoEntity> mapper;
  private final VersionInfoAccessor accessor;


  public VersionInfoDaoImpl(NoSqlDb noSqlDb) {
    this.noSqlDb = noSqlDb;
    this.mapper = this.noSqlDb.getMappingManager().mapper(VersionInfoEntity.class);
    this.accessor = this.noSqlDb.getMappingManager().createAccessor(VersionInfoAccessor.class);
  }

  @Override
  protected Mapper<VersionInfoEntity> getMapper() {
    return mapper;
  }

  @Override
  protected Object[] getKeys(VersionInfoEntity entity) {
    return new Object[]{entity.getEntityType(), entity.getEntityId()};
  }

  @Override
  public Collection<VersionInfoEntity> list(VersionInfoEntity entity) {
    return accessor.getAll(entity.getEntityType()).all();
  }

  @Accessor
  interface VersionInfoAccessor {
    @Query("select * from version_info where entity_type=?")
    Result<VersionInfoEntity> getAll(String entityType);
  }
}
