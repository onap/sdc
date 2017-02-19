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
import com.datastax.driver.mapping.UDTMapper;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ProcessArtifactDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessArtifactEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.nio.ByteBuffer;

public class ProcessArtifactDaoCassandraImpl implements ProcessArtifactDao {

  private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static final ProcessArtifactAccessor accessor =
      noSqlDb.getMappingManager().createAccessor(ProcessArtifactAccessor.class);
  private static final UDTMapper<Version> versionMapper =
      noSqlDb.getMappingManager().udtMapper(Version.class);

  @Override
  public void update(ProcessArtifactEntity entity) {
    accessor.update(entity.getVspId(), versionMapper.toUDT(entity.getVersion()),
        entity.getComponentId(), entity.getId(), entity.getArtifactName(), entity.getArtifact());
  }

  @Override
  public ProcessArtifactEntity get(ProcessArtifactEntity entity) {
    return accessor
        .get(entity.getVspId(), versionMapper.toUDT(entity.getVersion()), entity.getComponentId(),
            entity.getId());
  }

  @Override
  public void delete(ProcessArtifactEntity entity) {
    accessor.delete(entity.getVspId(), versionMapper.toUDT(entity.getVersion()),
        entity.getComponentId(), entity.getId());
  }

  @Accessor
  interface ProcessArtifactAccessor {

    @Query(
        "insert into vsp_process (vsp_id, version, component_id, process_id, artifact_name,"
            + " artifact) values (?,?,?,?,?,?)")
    ResultSet update(String vspId, UDTValue version, String componentId, String id,
                     String artifactName, ByteBuffer artifact);

    @Query(
        "select vsp_id, version, component_id, process_id, artifact_name, artifact "
            + "from vsp_process where vsp_id=? and version=? and component_id=? and process_id=?")
    ProcessArtifactEntity get(String vspId, UDTValue version, String componentId, String id);

    @Query(
        "delete artifact_name, artifact from vsp_process where vsp_id=? and version=? and"
            + " component_id=? and process_id=?")
    ResultSet delete(String vspId, UDTValue version, String componentId, String id);
  }
}
