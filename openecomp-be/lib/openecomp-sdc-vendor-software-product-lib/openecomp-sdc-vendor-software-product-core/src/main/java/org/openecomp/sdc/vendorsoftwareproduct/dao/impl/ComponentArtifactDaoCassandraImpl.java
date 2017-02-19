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

package org.openecomp.sdc.vendorsoftwareproduct.dao.impl;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.UDTMapper;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.dao.impl.CassandraBaseDao;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentArtifactDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentArtifactEntity;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;


import java.nio.ByteBuffer;
import java.util.Collection;

public class ComponentArtifactDaoCassandraImpl extends CassandraBaseDao<ComponentArtifactEntity>
    implements ComponentArtifactDao {

  private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static final ComponentArtifactAccessor accessor =
      noSqlDb.getMappingManager().createAccessor(ComponentArtifactAccessor.class);
  private static final Mapper<ComponentArtifactEntity> mapper =
      noSqlDb.getMappingManager().mapper(ComponentArtifactEntity.class);
  private static final UDTMapper<Version> versionMapper =
      noSqlDb.getMappingManager().udtMapper(Version.class);


  @Override
  public void registerVersioning(String versionableEntityType) {
    VersionableEntityMetadata metadata = new VersionableEntityMetadata(
        mapper.getTableMetadata().getName(),
        mapper.getTableMetadata().getPartitionKey().get(0).getName(),
        mapper.getTableMetadata().getPartitionKey().get(1).getName());

    // metadata.setUniqueValuesMetadata(Collections.singletonList(new UniqueValueMetadata
    // (VendorSoftwareProductConstants.UniqueValues.COMPONENT_ARTIFACT_NAME,
    // Arrays.asList(mapper.getTableMetadata().getPartitionKey().get(0).getName(), mapper
    // .getTableMetadata().getPartitionKey().get(1).getName(), "name"))));

    VersioningManagerFactory.getInstance().createInterface()
        .register(versionableEntityType, metadata);
  }

  @Override
  public ComponentArtifactEntity getArtifactByType(ComponentArtifactEntity entity) {
    return accessor.listByType(entity.getVspId(), versionMapper.toUDT(entity.getVersion()),
        entity.getComponentId(), entity.getType().toString()).one();
  }

  @Override
  protected Mapper<ComponentArtifactEntity> getMapper() {
    return mapper;
  }

  @Override
  protected Object[] getKeys(ComponentArtifactEntity entity) {
    return new Object[]{entity.getVspId(), versionMapper.toUDT(entity.getVersion()),
        entity.getComponentId(), entity.getType().toString(), entity.getId()};
  }

  @Override
  public Collection<ComponentArtifactEntity> list(ComponentArtifactEntity entity) {
    return accessor.listByType(entity.getVspId(), versionMapper.toUDT(entity.getVersion()),
        entity.getComponentId(), entity.getType().toString()).all();
  }

  @Override
  public void update(ComponentArtifactEntity entity) {
    accessor.update(entity.getVspId(), versionMapper.toUDT(entity.getVersion()),
        entity.getComponentId(), entity.getId(), entity.getArtifactName(), entity.getArtifact(),
        entity.getType().toString());

  }

  @Override
  public void delete(ComponentArtifactEntity entity) {
    accessor.delete(entity.getVspId(), versionMapper.toUDT(entity.getVersion()),
        entity.getComponentId(), entity.getType().toString());
  }

  @Override
  public Collection<ComponentArtifactEntity> getArtifactNamesAndTypesForComponent(
      ComponentArtifactEntity entity) {
    return accessor.getArtifactNamesAndTypesForComponent(entity.getVspId(),
        versionMapper.toUDT(entity.getVersion()), entity.getComponentId()).all();
  }

  @Override
  public void deleteAll(ComponentArtifactEntity entity) {
    accessor.deleteAll(entity.getVspId(), versionMapper.toUDT(entity.getVersion()));
  }


  @Accessor
  interface ComponentArtifactAccessor {

    @Query(
        "insert into vsp_component_artifact (vsp_id, version, component_id, artifact_id, name, "
            + "artifact, artifact_type) values (?,?,?,?,?,?,?)")
    ResultSet update(String vspId, UDTValue version, String componentId, String id,
                     String artifactName, ByteBuffer artifact, String type);

    @Query(
        "select vsp_id, version, component_id, artifact_type, artifact_id, name, artifact from "
            + "vsp_component_artifact where vsp_id=? and version=? and component_id=? and "
            + "artifact_type =?")
    Result<ComponentArtifactEntity> listByType(String vspId, UDTValue version, String componentId,
                                               String type);

    @Query(
        "delete name, artifact from vsp_component_artifact where vsp_id=? and version=? and "
            + "component_id=? and artifact_type=? and artifact_id=?")
    ResultSet delete(String vspId, UDTValue version, String componentId, String type,
                     String artifactId);

    @Query(
        "delete from vsp_component_artifact where vsp_id=? and version=? and component_id=? "
            + "and artifact_type=?")
    ResultSet delete(String vspId, UDTValue version, String componentId, String artifactType);

    @Query("delete from vsp_component_artifact where vsp_id=? and version=?")
    ResultSet deleteAll(String vspId, UDTValue version);

    @Query(
        "select name, artifact_type from vsp_component_artifact where vsp_id=? and version=? "
            + "and component_id=?")
    Result<ComponentArtifactEntity> getArtifactNamesAndTypesForComponent(String vspId,
                                                                         UDTValue version,
                                                                         String componentId);

  }
}
