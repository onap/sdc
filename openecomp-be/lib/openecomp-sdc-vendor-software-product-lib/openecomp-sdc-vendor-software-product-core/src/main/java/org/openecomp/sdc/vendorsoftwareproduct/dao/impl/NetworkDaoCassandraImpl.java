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
import org.openecomp.sdc.vendorsoftwareproduct.dao.NetworkDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.UniqueValueMetadata;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class NetworkDaoCassandraImpl extends CassandraBaseDao<NetworkEntity> implements NetworkDao {

  private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static final Mapper<NetworkEntity> mapper =
      noSqlDb.getMappingManager().mapper(NetworkEntity.class);
  private static final NetworkAccessor accessor =
      noSqlDb.getMappingManager().createAccessor(NetworkAccessor.class);
  private static final UDTMapper<Version> versionMapper =
      noSqlDb.getMappingManager().udtMapper(Version.class);

  @Override
  public void registerVersioning(String versionableEntityType) {
    VersionableEntityMetadata metadata = new VersionableEntityMetadata(
        mapper.getTableMetadata().getName(),
        mapper.getTableMetadata().getPartitionKey().get(0).getName(),
        mapper.getTableMetadata().getPartitionKey().get(1).getName());


    metadata.setUniqueValuesMetadata(Collections.singletonList(
        new UniqueValueMetadata(VendorSoftwareProductConstants.UniqueValues.NETWORK_NAME,
            Arrays.asList(mapper.getTableMetadata().getPartitionKey().get(0).getName(),
                mapper.getTableMetadata().getPartitionKey().get(1).getName(), "name"))));

    VersioningManagerFactory.getInstance().createInterface()
        .register(versionableEntityType, metadata);
  }

  @Override
  protected Mapper<NetworkEntity> getMapper() {
    return mapper;
  }

  @Override
  protected Object[] getKeys(NetworkEntity entity) {
    return new Object[]{entity.getVspId(), versionMapper.toUDT(entity.getVersion()),
        entity.getId()};
  }

  @Override
  public void update(NetworkEntity entity) {
    accessor.updateCompositionData(entity.getVspId(), versionMapper.toUDT(entity.getVersion()),
        entity.getId(), entity.getCompositionData());
  }

  @Override
  public void updateQuestionnaireData(String vspId, Version version, String id,
                                      String questionnaireData) {
    accessor.updateQuestionnaireData(questionnaireData, vspId, versionMapper.toUDT(version), id);
  }

  @Override
  public Collection<NetworkEntity> list(NetworkEntity entity) {
    return accessor.list(entity.getVspId(), versionMapper.toUDT(entity.getVersion())).all();
  }

  @Override
  public void deleteAll(String vspId, Version version) {
    accessor.deleteAll(vspId, version);
  }

  @Accessor
  interface NetworkAccessor {

    @Query(
        "select vsp_id, version, network_id, composition_data from vsp_network where vsp_id=? "
            + "and version=?")
    Result<NetworkEntity> list(String vspId, UDTValue version);

    @Query(
        "insert into vsp_network (vsp_id, version, network_id, composition_data) values (?,?,?,?)")
    ResultSet updateCompositionData(String vspId, UDTValue version, String id,
                                    String compositionData);

    @Query(
        "update vsp_network set questionnaire_data=? where vsp_id=? and version=? and network_id=?")
    ResultSet updateQuestionnaireData(String questionnaireData, String vspId, UDTValue version,
                                      String id);

    @Query("delete from vsp_network where vsp_id=? and version=?")
    ResultSet deleteAll(String vspId, Version version);

  }
}
