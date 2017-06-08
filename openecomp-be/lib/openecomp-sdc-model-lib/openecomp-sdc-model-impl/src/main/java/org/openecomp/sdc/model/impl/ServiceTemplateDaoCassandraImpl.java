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

package org.openecomp.sdc.model.impl;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.UDTMapper;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.model.dao.ServiceTemplateDao;
import org.openecomp.core.model.types.ServiceTemplate;
import org.openecomp.core.model.types.ServiceTemplateEntity;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceTemplateDaoCassandraImpl implements ServiceTemplateDao {

  private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static final Mapper<ServiceTemplateEntity> mapper = noSqlDb.getMappingManager().mapper(
      ServiceTemplateEntity.class);
  private static final VspServiceTemplateAccessor accessor =
      noSqlDb.getMappingManager().createAccessor(
          VspServiceTemplateAccessor.class);
  private static final UDTMapper<Version> versionMapper =
      noSqlDb.getMappingManager().udtMapper(Version.class);

  @Override
  public void registerVersioning(String versionableEntityType) {
    VersioningManagerFactory.getInstance().createInterface().register(versionableEntityType,
        new VersionableEntityMetadata(mapper.getTableMetadata().getName(),
            mapper.getTableMetadata().getPartitionKey().get(0).getName(),
            mapper.getTableMetadata().getPartitionKey().get(1).getName()));
  }

  @Override
  public void create(ServiceTemplate entity) {
    ServiceTemplateEntity vspServiceTemplateEntity = new ServiceTemplateEntity(entity);
    mapper.save(vspServiceTemplateEntity);
  }

  @Override
  public void update(ServiceTemplate entity) {
    ServiceTemplateEntity vspServiceTemplateEntity = new ServiceTemplateEntity(entity);
    mapper.save(vspServiceTemplateEntity);
  }

  @Override
  public ServiceTemplate get(String vspId, Version version) {
    return (mapper.get(getKeys(vspId, version))).getServiceTemplate();
  }

  @Override
  public void delete(String vspId, Version version) {
    mapper.delete(vspId, version);
  }

  @Override
  public Object[] getKeys(String vspId, Version version) {
    return new Object[]{vspId, versionMapper.toUDT(version)};
  }

  @Override
  public ServiceTemplate getTemplateInfo(String vspId, Version version, String name) {
    ServiceTemplateEntity serviceTemplateEntity =
        accessor.getTemplateInfo(vspId, versionMapper.toUDT(version),
            name).one();
    if (serviceTemplateEntity == null) {
      return null;
    }
    return serviceTemplateEntity.getServiceTemplate();
  }

  @Override
  public Collection<ServiceTemplate> list(String vspId, Version version) {

    List<ServiceTemplateEntity> entityList = accessor.list(vspId, version).all();
    return entityList.stream().map(entity -> entity.getServiceTemplate())
        .collect(Collectors.toList());
  }

  @Override
  public String getBase(String vspId, Version version) {
    Result<ServiceTemplateEntity> element = accessor.getBase(vspId, versionMapper.toUDT(version));
    if (element != null) {
      ServiceTemplateEntity vspServiceTemplateEntity = element.one();
      if (vspServiceTemplateEntity != null) {
        return element.one().getBaseName();
      }
    }
    return null;
  }

  @Override
  public void deleteAll(String vspId, Version version) {
    accessor.deleteAll(vspId, versionMapper.toUDT(version));
  }

  @Accessor
  interface VspServiceTemplateAccessor {

    @Query("SELECT vsp_id, version, name, base_name ,content_data FROM vsp_service_template")
    Result<ServiceTemplateEntity> listAll();

    @Query(
        "SELECT vsp_id, version, name, base_name ,content_data"
            + " FROM vsp_service_template where vsp_id=? and version=?")
    Result<ServiceTemplateEntity> list(String vspId, Version version);

    @Query(
        "SELECT vsp_id, version, name, base_name ,content_data"
            + " FROM vsp_service_template where vsp_id=? and version=? and name=?")
    Result<ServiceTemplateEntity> getTemplateInfo(String vspId, UDTValue version, String name);

    @Query(
        "SELECT vsp_id, version, name, base_name"
            + "  FROM vsp_service_template where vsp_id=? and version=?")
    Result<ServiceTemplateEntity> getBase(String vspId, UDTValue version);

    @Query("DELETE FROM vsp_service_template where vsp_id = ? and version = ?")
    ResultSet deleteAll(String vspId, UDTValue version);
  }

}
