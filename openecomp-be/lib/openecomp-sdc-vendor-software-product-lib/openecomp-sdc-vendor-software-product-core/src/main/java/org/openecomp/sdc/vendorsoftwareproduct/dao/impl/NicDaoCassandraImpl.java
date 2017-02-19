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
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.UniqueValueMetadata;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class NicDaoCassandraImpl extends CassandraBaseDao<NicEntity> implements NicDao {

  private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static final Mapper<NicEntity> mapper =
      noSqlDb.getMappingManager().mapper(NicEntity.class);
  private static final NicAccessor accessor =
      noSqlDb.getMappingManager().createAccessor(NicAccessor.class);
  private static final UDTMapper<Version> versionMapper =
      noSqlDb.getMappingManager().udtMapper(Version.class);

  @Override
  public void registerVersioning(String versionableEntityType) {
    VersionableEntityMetadata metadata = new VersionableEntityMetadata(
        mapper.getTableMetadata().getName(),
        mapper.getTableMetadata().getPartitionKey().get(0).getName(),
        mapper.getTableMetadata().getPartitionKey().get(1).getName());


    metadata.setUniqueValuesMetadata(Collections
        .singletonList(new UniqueValueMetadata(VendorSoftwareProductConstants.UniqueValues.NIC_NAME,
            Arrays.asList("vsp_id", "version", "component_id", "name"))));

    VersioningManagerFactory.getInstance().createInterface()
        .register(versionableEntityType, metadata);
  }

  @Override
  protected Mapper<NicEntity> getMapper() {
    return mapper;
  }

  @Override
  protected Object[] getKeys(NicEntity entity) {
    return new Object[]{entity.getVspId(), versionMapper.toUDT(entity.getVersion()),
        entity.getComponentId(), entity.getId()};
  }

  @Override
  public void create(NicEntity entity) {
    super.create(entity);
  }

  @Override
  public void update(NicEntity entity) {
    accessor.updateCompositionData(entity.getVspId(), versionMapper.toUDT(entity.getVersion()),
        entity.getComponentId(), entity.getId(), entity.getCompositionData());
  }

  @Override
  public void updateQuestionnaireData(String vspId, Version version, String id, String componentId,
                                      String questionnaireData) {
    accessor.updateQuestionnaireData(questionnaireData, vspId, versionMapper.toUDT(version), id,
        componentId);
  }

  @Override
  public Collection<NicEntity> listByVsp(String vspId, Version version) {
    return accessor.listByVspId(vspId, versionMapper.toUDT(version)).all();
  }

  @Override
  public Collection<NicEntity> list(NicEntity entity) {
    return accessor.listByComponentId(entity.getVspId(), versionMapper.toUDT(entity.getVersion()),
        entity.getComponentId()).all();
  }

  public void deleteByComponentId(String vspId, Version version, String componentId) {
    accessor.deleteByComponentId(vspId, version, componentId);
  }

  public void deleteByVspId(String vspId, Version version) {
    accessor.deleteByVspId(vspId, versionMapper.toUDT(version));
  }

  @Accessor
  interface NicAccessor {

    @Query(
        "select vsp_id, version, component_id, nic_id, composition_data "
            + "from vsp_component_nic where vsp_id=? and version=? and component_id=?")
    Result<NicEntity> listByComponentId(String vspId, UDTValue version, String componentId);

    @Query("select * from vsp_component_nic where vsp_id=? and version=?")
    Result<NicEntity> listByVspId(String vspId, UDTValue version);

    @Query(
        "insert into vsp_component_nic (vsp_id, version, component_id, nic_id, composition_data) "
            + "values (?,?,?,?,?)")
    ResultSet updateCompositionData(String vspId, UDTValue version, String componentId, String id,
                                    String compositionData);

    @Query(
        "update vsp_component_nic set questionnaire_data=? where vsp_id=? and version=? "
            + "and component_id=? and nic_id=?")
    ResultSet updateQuestionnaireData(String questionnaireData, String vspId, UDTValue version,
                                      String componentId, String id);

    @Query("delete from vsp_component_nic where vsp_id=? and version=? and component_id=?")
    ResultSet deleteByComponentId(String vspId, Version version, String componentId);

    @Query("delete from vsp_component_nic where vsp_id=? and version=?")
    ResultSet deleteByVspId(String vspId, UDTValue version);
  }
}
