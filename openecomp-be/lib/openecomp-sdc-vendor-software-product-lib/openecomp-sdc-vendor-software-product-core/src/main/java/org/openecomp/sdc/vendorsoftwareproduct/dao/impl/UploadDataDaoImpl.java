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

import com.datastax.driver.core.UDTValue;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.UDTMapper;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.dao.impl.CassandraBaseDao;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.UploadDataDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.UploadDataEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.nio.ByteBuffer;
import java.util.Collection;

public class UploadDataDaoImpl extends CassandraBaseDao<UploadDataEntity> implements UploadDataDao {

  private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static final Mapper<UploadDataEntity> mapper =
      noSqlDb.getMappingManager().mapper(UploadDataEntity.class);
  private static final UploadDataAccessor accessor =
      noSqlDb.getMappingManager().createAccessor(UploadDataAccessor.class);
  private static final UDTMapper<Version> versionMapper =
      noSqlDb.getMappingManager().udtMapper(Version.class);

  @Override
  protected Mapper<UploadDataEntity> getMapper() {
    return mapper;
  }

  @Override
  protected Object[] getKeys(UploadDataEntity entity) {
    return new Object[]{entity.getId(), versionMapper.toUDT(entity.getVersion())};
  }

  @Override
  public Collection<UploadDataEntity> list(UploadDataEntity entity) {
    return accessor.listAll().all();
  }

  @Override
  public void deleteContentDataAndValidationData(String vspId, Version version) {
    accessor.deleteContentDataAndValidationData(vspId, versionMapper.toUDT(version));
  }

  @Override
  public ByteBuffer getContentData(String vspId, Version version) {
    return accessor.getContentData(vspId, version).one().getContentData();
  }


  @Accessor
  interface UploadDataAccessor {

    @Query(
        "SELECT package_name, package_version, content_data, validation_data FROM vsp_information")
    Result<UploadDataEntity> listAll();

    @Query(
        "DELETE package_name, package_version, content_data, validation_data FROM vsp_information "
            + "WHERE vsp_id=? and version=?")
    Result<VspDetails> deleteContentDataAndValidationData(String vspId, UDTValue udtValue);

    @Query("SELECT CONTENT_DATA FROM vsp_information WHERE vsp_id=? and version=?")
    Result<UploadDataEntity> getContentData(String vspId, Version version);

  }
}
