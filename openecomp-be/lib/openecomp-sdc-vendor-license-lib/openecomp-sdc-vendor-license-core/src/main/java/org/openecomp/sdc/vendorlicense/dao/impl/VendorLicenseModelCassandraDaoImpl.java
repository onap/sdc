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

package org.openecomp.sdc.vendorlicense.dao.impl;

import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.UDTMapper;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.dao.impl.CassandraBaseDao;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.vendorlicense.dao.VendorLicenseModelDao;
import org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;

import java.util.Collection;

public class VendorLicenseModelCassandraDaoImpl extends CassandraBaseDao<VendorLicenseModelEntity>
    implements VendorLicenseModelDao {

  private static NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static Mapper<VendorLicenseModelEntity> mapper =
      noSqlDb.getMappingManager().mapper(VendorLicenseModelEntity.class);
  private static VendorLicenseModelAccessor accessor =
      noSqlDb.getMappingManager().createAccessor(VendorLicenseModelAccessor.class);
  private static UDTMapper<Version> versionMapper =
      noSqlDb.getMappingManager().udtMapper(Version.class);

  @Override
  public void registerVersioning(String versionableEntityType) {
    VersioningManagerFactory.getInstance().createInterface()
        .register(versionableEntityType, new VersionableEntityMetadata(
            mapper.getTableMetadata().getName(),
            mapper.getTableMetadata().getPartitionKey().get(0).getName(),
            mapper.getTableMetadata().getPartitionKey().get(1).getName()));
  }

  @Override
  protected Mapper<VendorLicenseModelEntity> getMapper() {
    return mapper;
  }

  @Override
  protected Object[] getKeys(VendorLicenseModelEntity entity) {
    return new Object[]{entity.getId(), versionMapper.toUDT(entity.getVersion())};
  }

  @Override
  public Collection<VendorLicenseModelEntity> list(VendorLicenseModelEntity vendorLicenseModel) {
    return accessor.getAll().all();
  }

  //    @Override
  //    public void updateLastModificationTime(VendorLicenseModelEntity vendorLicenseModel){
  //        accessor.updateLastModificationTime(vendorLicenseModel.getLastModificationTime(),
  // vendorLicenseModel.getId(), versionMapper.toUDT(vendorLicenseModel.getVersion()));
  //    }

  @Accessor
  interface VendorLicenseModelAccessor {

    @Query("SELECT * FROM vendor_license_model")
    Result<VendorLicenseModelEntity> getAll();

    //        @Query("UPDATE vendor_license_model set last_modification_time
    // = ? where vlm_id = ? and version = ?")
    //        ResultSet updateLastModificationTime(Date lastModificationTime,
    // String vlmId, UDTValue version);
  }
}
