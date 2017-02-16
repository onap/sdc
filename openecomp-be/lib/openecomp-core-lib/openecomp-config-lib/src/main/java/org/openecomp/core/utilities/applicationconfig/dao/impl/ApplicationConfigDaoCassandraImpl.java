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

package org.openecomp.core.utilities.applicationconfig.dao.impl;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.dao.impl.CassandraBaseDao;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.core.utilities.applicationconfig.dao.ApplicationConfigDao;
import org.openecomp.core.utilities.applicationconfig.dao.type.ApplicationConfigEntity;
import org.openecomp.core.utilities.applicationconfig.type.ConfigurationData;

import java.util.Collection;
import java.util.Objects;

public class ApplicationConfigDaoCassandraImpl extends CassandraBaseDao<ApplicationConfigEntity>
    implements ApplicationConfigDao {

  private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static final Mapper<ApplicationConfigEntity> mapper =
      noSqlDb.getMappingManager().mapper(ApplicationConfigEntity.class);
  private static final ApplicationConfigAccessor accessor =
      noSqlDb.getMappingManager().createAccessor(ApplicationConfigAccessor.class);

  @Override
  protected Mapper<ApplicationConfigEntity> getMapper() {
    return mapper;
  }

  @Override
  protected Object[] getKeys(ApplicationConfigEntity entity) {
    return new Object[]{entity.getNamespace(), entity.getKey(), entity.getValue()};
  }

  @Override
  public Collection<ApplicationConfigEntity> list(ApplicationConfigEntity entity) {
    return accessor.list(entity.getNamespace()).all();
  }

  @Override
  public void create(ApplicationConfigEntity entity) {
    accessor.updateApplicationConfigData(entity.getNamespace(), entity.getKey(), entity.getValue());
  }

  @Override
  public void update(ApplicationConfigEntity entity) {
    accessor.updateApplicationConfigData(entity.getNamespace(), entity.getKey(), entity.getValue());
  }

  @Override
  public ApplicationConfigEntity get(ApplicationConfigEntity entity) {
    return accessor.get(entity.getNamespace(), entity.getKey());
  }

  @Override
  public long getValueTimestamp(String namespace, String key) {
    ResultSet resultSet = accessor.getValueAndTimestampOfConfigurationValue(namespace, key);

    return resultSet.one().getLong("writetime(value)");
  }

  @Override
  public ConfigurationData getConfigurationData(String namespace, String key) {
    //String value = accessor.getValue(namespace, key).one().getString("value");
    ResultSet resultSet = accessor.getValueAndTimestampOfConfigurationValue(namespace, key);
    Row one = resultSet.one();

    if (Objects.nonNull(one)) {
      return new ConfigurationData(one.getString("value"), one.getLong("writetime(value)"));
    }

    return null;
  }


  @Accessor
  interface ApplicationConfigAccessor {

    @Query("select namespace, key, value from application_config where namespace=?")
    Result<ApplicationConfigEntity> list(String namespace);

    @Query("insert into application_config (namespace, key, value) values (?,?,?)")
    ResultSet updateApplicationConfigData(String namespace, String key, String value);

    @Query("select namespace, key, value from application_config where namespace=? and key=?")
    ApplicationConfigEntity get(String namespace, String key);

    @Query("select value, writetime(value) from application_config where namespace=? and key=?")
    ResultSet getValueAndTimestampOfConfigurationValue(String namespace, String key);

  }
}
