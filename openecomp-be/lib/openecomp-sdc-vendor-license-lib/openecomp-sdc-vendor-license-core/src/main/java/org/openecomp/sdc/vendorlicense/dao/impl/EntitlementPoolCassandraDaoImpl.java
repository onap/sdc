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
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.vendorlicense.VendorLicenseConstants;
import org.openecomp.sdc.vendorlicense.dao.EntitlementPoolDao;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.UniqueValueMetadata;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class EntitlementPoolCassandraDaoImpl extends CassandraBaseDao<EntitlementPoolEntity>
    implements EntitlementPoolDao {

  private static NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static Mapper<EntitlementPoolEntity> mapper =
      noSqlDb.getMappingManager().mapper(EntitlementPoolEntity.class);
  private static EntitlementPoolAccessor accessor =
      noSqlDb.getMappingManager().createAccessor(EntitlementPoolAccessor.class);
  private static UDTMapper<Version> versionMapper =
      noSqlDb.getMappingManager().udtMapper(Version.class);

  @Override
  public void registerVersioning(String versionableEntityType) {
    VersionableEntityMetadata metadata = new VersionableEntityMetadata(
        mapper.getTableMetadata().getName(),
        mapper.getTableMetadata().getPartitionKey().get(0).getName(),
        mapper.getTableMetadata().getPartitionKey().get(1).getName());

    metadata.setUniqueValuesMetadata(Collections.singletonList(
        new UniqueValueMetadata(VendorLicenseConstants.UniqueValues.ENTITLEMENT_POOL_NAME,
            Arrays.asList(mapper.getTableMetadata().getPartitionKey().get(0).getName(),
                mapper.getTableMetadata().getPartitionKey().get(1).getName(), "name"))));

    VersioningManagerFactory.getInstance().createInterface()
        .register(versionableEntityType, metadata);
  }

  @Override
  protected Mapper<EntitlementPoolEntity> getMapper() {
    return mapper;
  }

  @Override
  protected Object[] getKeys(EntitlementPoolEntity entity) {
    return new Object[]{entity.getVendorLicenseModelId(), versionMapper.toUDT(entity.getVersion()),
        entity.getId()};
  }

  @Override
  public Collection<EntitlementPoolEntity> list(EntitlementPoolEntity entity) {
    return accessor.listByVlmVersion(entity.getVendorLicenseModelId(),
        versionMapper.toUDT(entity.getVersion())).all();
  }

  @Override
  public long count(EntitlementPoolEntity entity) {
    return accessor.countByVlmVersion(entity.getVendorLicenseModelId(),
        versionMapper.toUDT(entity.getVersion())).one().getLong("count");
  }

  @Override
  public void deleteAll(EntitlementPoolEntity entity) {
    accessor.deleteByVlmVersion(entity.getVendorLicenseModelId(),
        versionMapper.toUDT(entity.getVersion())).all();
  }

  @Override
  public void addReferencingFeatureGroup(EntitlementPoolEntity entity,
                                         String referencingFeatureGroupId) {
    accessor
        .addReferencingFeatureGroups(CommonMethods.toSingleElementSet(referencingFeatureGroupId),
            entity.getVendorLicenseModelId(), versionMapper.toUDT(entity.getVersion()),
            entity.getId());
  }

  @Override
  public void removeReferencingFeatureGroup(EntitlementPoolEntity entity,
                                            String referencingFeatureGroupId) {
    accessor
        .removeReferencingFeatureGroups(CommonMethods.toSingleElementSet(referencingFeatureGroupId),
            entity.getVendorLicenseModelId(), versionMapper.toUDT(entity.getVersion()),
            entity.getId());
  }

  @Accessor
  interface EntitlementPoolAccessor {

    @Query("select * from entitlement_pool where vlm_id=? AND version=?")
    Result<EntitlementPoolEntity> listByVlmVersion(String vendorLicenseModelId,
                                                   UDTValue vendorLicenseModelVersion);

    @Query("delete from entitlement_pool where vlm_id=? AND version=?")
    ResultSet deleteByVlmVersion(String vendorLicenseModelId, UDTValue vendorLicenseModelVersion);

    @Query("select count(1) from entitlement_pool where vlm_id=? AND version=?")
    ResultSet countByVlmVersion(String vendorLicenseModelId, UDTValue vendorLicenseModelVersion);

    @Query(
        "UPDATE entitlement_pool SET ref_fg_ids = ref_fg_ids + ? WHERE vlm_id=? AND version=? "
            + " AND ep_id=?")
    ResultSet addReferencingFeatureGroups(Set<String> referencingFeatureGroups,
                                          String vendorLicenseModelId,
                                          UDTValue vendorLicenseModelVersion, String id);

    @Query(
        "UPDATE entitlement_pool SET ref_fg_ids = ref_fg_ids - ? WHERE vlm_id=? AND version=? "
            + "AND ep_id=?")
    ResultSet removeReferencingFeatureGroups(Set<String> referencingFeatureGroups,
                                             String vendorLicenseModelId,
                                             UDTValue vendorLicenseModelVersion, String id);
  }
}
