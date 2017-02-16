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
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ProcessDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.UniqueValueMetadata;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class ProcessDaoCassandraImpl implements ProcessDao {

  private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static final Mapper<ProcessEntity> mapper =
      noSqlDb.getMappingManager().mapper(ProcessEntity.class);
  private static final ProcessAccessor accessor =
      noSqlDb.getMappingManager().createAccessor(ProcessAccessor.class);
  private static final UDTMapper<Version> versionMapper =
      noSqlDb.getMappingManager().udtMapper(Version.class);

  @Override
  public void registerVersioning(String versionableEntityType) {
    VersionableEntityMetadata metadata = new VersionableEntityMetadata(
        mapper.getTableMetadata().getName(),
        mapper.getTableMetadata().getPartitionKey().get(0).getName(),
        mapper.getTableMetadata().getPartitionKey().get(1).getName());

    metadata.setUniqueValuesMetadata(Collections.singletonList(
        new UniqueValueMetadata(VendorSoftwareProductConstants.UniqueValues.PROCESS_NAME,
            Arrays.asList("vsp_id", "version", "component_id", "name"))));

    VersioningManagerFactory.getInstance().createInterface()
        .register(versionableEntityType, metadata);
  }

  @Override
  public Collection<ProcessEntity> list(ProcessEntity entity) {
    return accessor
        .list(entity.getVspId(), versionMapper.toUDT(entity.getVersion()), entity.getComponentId())
        .all();
  }

  @Override
  public void create(ProcessEntity entity) {
    accessor.update(entity.getVspId(), versionMapper.toUDT(entity.getVersion()),
        entity.getComponentId(), entity.getId(), entity.getName(), entity.getDescription());
  }

  @Override
  public void update(ProcessEntity entity) {
    accessor.update(entity.getVspId(), versionMapper.toUDT(entity.getVersion()),
        entity.getComponentId(), entity.getId(), entity.getName(), entity.getDescription());
  }

  @Override
  public ProcessEntity get(ProcessEntity entity) {
    return accessor
        .get(entity.getVspId(), versionMapper.toUDT(entity.getVersion()), entity.getComponentId(),
            entity.getId());
  }

  @Override
  public void delete(ProcessEntity entity) {
    if (entity.getId() == null) {
      accessor.deleteAll(entity.getVspId(), versionMapper.toUDT(entity.getVersion()),
          entity.getComponentId());
    } else {
      accessor.delete(entity.getVspId(), versionMapper.toUDT(entity.getVersion()),
          entity.getComponentId(), entity.getId());
    }
  }

  public void deleteAll(ProcessEntity entity) {
    accessor.deleteAll(entity.getVspId(), versionMapper.toUDT(entity.getVersion()));
  }

  @Accessor
  interface ProcessAccessor {

    @Query(
        "insert into vsp_process (vsp_id, version, component_id, process_id, name, description) "
            + "values (?,?,?,?,?,?)")
    ResultSet update(String vspId, UDTValue version, String componentId, String id, String name,
                     String description);

    @Query(
        "select vsp_id, version, component_id, process_id, name, description, artifact_name "
            + "from vsp_process where vsp_id=? and version=? and component_id=? and process_id=?")
    ProcessEntity get(String vspId, UDTValue version, String componentId, String id);

    @Query(
        "select vsp_id, version, component_id, process_id, name, description, artifact_name "
            + "from vsp_process where vsp_id=? and version=? and component_id=?")
    Result<ProcessEntity> list(String vspId, UDTValue version, String componentId);

    @Query(
        "delete from vsp_process where vsp_id=? and version=? and component_id=? and process_id=?")
    ResultSet delete(String vspId, UDTValue version, String componentId, String id);

    @Query("delete from vsp_process where vsp_id=? and version=? and component_id=?")
    ResultSet deleteAll(String vspId, UDTValue version, String componentId);

    @Query("delete from vsp_process where vsp_id=? and version=?")
    ResultSet deleteAll(String vspId, UDTValue version);
  }
}
