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

package org.openecomp.core.dao.impl;

import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.dao.UniqueValueDao;
import org.openecomp.core.dao.types.UniqueValueEntity;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;

import java.util.Collection;

public class UniqueValueCassandraDaoImpl extends CassandraBaseDao<UniqueValueEntity> implements
    UniqueValueDao {

  private static NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static Mapper<UniqueValueEntity> mapper =
      noSqlDb.getMappingManager().mapper(UniqueValueEntity.class);
  private static UniqueValueAccessor accessor =
      noSqlDb.getMappingManager().createAccessor(UniqueValueAccessor.class);


  @Override
  protected Mapper<UniqueValueEntity> getMapper() {
    return mapper;
  }

  @Override
  protected Object[] getKeys(UniqueValueEntity entity) {
    return new Object[]{entity.getType(), entity.getValue()};
  }

  @Override
  public Collection<UniqueValueEntity> list(UniqueValueEntity entity) {
    return accessor.listAll().all();
  }

  @Accessor
  interface UniqueValueAccessor {

    @Query("select * from unique_value")
    Result<UniqueValueEntity> listAll();
  }
}
