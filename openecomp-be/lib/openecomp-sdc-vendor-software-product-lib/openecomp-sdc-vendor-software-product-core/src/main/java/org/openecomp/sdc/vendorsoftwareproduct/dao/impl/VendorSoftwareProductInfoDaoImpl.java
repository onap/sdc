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

import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.UDTMapper;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.dao.impl.CassandraBaseDao;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspQuestionnaireEntity;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;

import java.util.Collection;

public class VendorSoftwareProductInfoDaoImpl extends CassandraBaseDao<VspDetails>
    implements VendorSoftwareProductInfoDao {

  private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static final Mapper<VspDetails> mapper =
      noSqlDb.getMappingManager().mapper(VspDetails.class);
  private static final VendorSoftwareProductInfoAccessor accessor =
      noSqlDb.getMappingManager().createAccessor(VendorSoftwareProductInfoAccessor.class);
  private static final UDTMapper<Version> versionMapper =
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
  protected Mapper<VspDetails> getMapper() {
    return mapper;
  }

  @Override
  protected Object[] getKeys(VspDetails entity) {
    return new Object[]{entity.getId(), versionMapper.toUDT(entity.getVersion())};
  }

  @Override
  public Collection<VspDetails> list(VspDetails entity) {
    return accessor.listAll().all();
  }

  @Override
  public void updateQuestionnaireData(String vspId, Version version,
                                      String questionnaireData) {

  }

  @Override
  public VspQuestionnaireEntity getQuestionnaire(String vspId, Version version) {
    return null;
  }

  @Override
  public boolean isManual(String vspId, Version version) {
    return false;
  }


  @Accessor
  interface VendorSoftwareProductInfoAccessor {

    @Query(
        "SELECT vsp_id,version,name,description,icon,category,sub_category,vendor_id,vlm_version,"
            + "license_agreement,feature_groups, is_old_version FROM vsp_information")
    Result<VspDetails> listAll();
  }
}
