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
import org.openecomp.sdc.vendorlicense.dao.FeatureGroupDao;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.UniqueValueMetadata;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.openecomp.core.utilities.CommonMethods.toSingleElementSet;

public class FeatureGroupCassandraDaoImpl extends CassandraBaseDao<FeatureGroupEntity>
    implements FeatureGroupDao {

  private static NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static Mapper<FeatureGroupEntity> mapper =
      noSqlDb.getMappingManager().mapper(FeatureGroupEntity.class);
  private static FeatureGroupAccessor accessor =
      noSqlDb.getMappingManager().createAccessor(FeatureGroupAccessor.class);
  private static UDTMapper<Version> versionMapper =
      noSqlDb.getMappingManager().udtMapper(Version.class);

  private static Set<String> emptyIfNull(Set<String> set) {
    return set == null ? new HashSet<>() : set;
  }

  @Override
  public void registerVersioning(String versionableEntityType) {
    VersionableEntityMetadata metadata = new VersionableEntityMetadata(
        mapper.getTableMetadata().getName(),
        mapper.getTableMetadata().getPartitionKey().get(0).getName(),
        mapper.getTableMetadata().getPartitionKey().get(1).getName());

    metadata.setUniqueValuesMetadata(Collections.singletonList(
        new UniqueValueMetadata(VendorLicenseConstants.UniqueValues.FEATURE_GROUP_NAME,
            Arrays.asList(mapper.getTableMetadata().getPartitionKey().get(0).getName(),
                mapper.getTableMetadata().getPartitionKey().get(1).getName(), "name"))));

    VersioningManagerFactory.getInstance().createInterface()
        .register(versionableEntityType, metadata);
  }

  @Override
  protected Mapper<FeatureGroupEntity> getMapper() {
    return mapper;
  }

  @Override
  protected Object[] getKeys(FeatureGroupEntity entity) {
    return new Object[]{entity.getVendorLicenseModelId(), versionMapper.toUDT(entity.getVersion()),
        entity.getId()};
  }

  @Override
  public long count(FeatureGroupEntity entity) {
    return accessor.countByVlmVersion(entity.getVendorLicenseModelId(),
        versionMapper.toUDT(entity.getVersion())).one().getLong("count");
  }

  @Override
  public void deleteAll(FeatureGroupEntity entity) {
    accessor.deleteByVlmVersion(entity.getVendorLicenseModelId(),
        versionMapper.toUDT(entity.getVersion())).all();
  }

  @Override
  public void updateFeatureGroup(FeatureGroupEntity entity,
                                 Set<String> addedEntitlementPools,
                                 Set<String> removedEntitlementPools,
                                 Set<String> addedLicenseKeyGroups,
                                 Set<String> removedLicenseKeyGroups) {
    accessor.updateColumnsAndDeltaFeatureGroupIds(
        entity.getName(),
        entity.getDescription(),
        entity.getPartNumber(),
        emptyIfNull(addedEntitlementPools),
        emptyIfNull(removedEntitlementPools),
        emptyIfNull(addedLicenseKeyGroups),
        emptyIfNull(removedLicenseKeyGroups),
        entity.getVendorLicenseModelId(),
        versionMapper.toUDT(entity.getVersion()),
        entity.getId()
    );
  }

  @Override
  public void addReferencingLicenseAgreement(FeatureGroupEntity entity, String licenseAgreementId) {
    accessor.addReferencingLicenseAgreements(CommonMethods.toSingleElementSet(licenseAgreementId),
        entity.getVendorLicenseModelId(), versionMapper.toUDT(entity.getVersion()), entity.getId());
  }

  @Override
  public void removeReferencingLicenseAgreement(FeatureGroupEntity entity,
                                                String licenseAgreementId) {
    accessor
        .removeReferencingLicenseAgreements(CommonMethods.toSingleElementSet(licenseAgreementId),
            entity.getVendorLicenseModelId(), versionMapper.toUDT(entity.getVersion()),
            entity.getId());
  }

  @Override
  public void removeEntitlementPool(FeatureGroupEntity entity, String entitlementPoolId) {
    accessor.removeEntitlementPools(toSingleElementSet(entitlementPoolId),
        entity.getVendorLicenseModelId(), versionMapper.toUDT(entity.getVersion()), entity.getId());
  }

  @Override
  public void removeLicenseKeyGroup(FeatureGroupEntity entity, String licenseKeyGroupId) {
    accessor.removeLicenseKeyGroup(toSingleElementSet(licenseKeyGroupId),
        entity.getVendorLicenseModelId(), versionMapper.toUDT(entity.getVersion()), entity.getId());
  }

  @Override
  public Collection<FeatureGroupEntity> list(FeatureGroupEntity entity) {
    return accessor.listByVlmVersion(entity.getVendorLicenseModelId(),
        versionMapper.toUDT(entity.getVersion())).all();
  }

  @Accessor
  interface FeatureGroupAccessor {

    @Query("select * from feature_group where vlm_id=? AND version=?")
    Result<FeatureGroupEntity> listByVlmVersion(String vendorLicenseModelId, UDTValue version);

    @Query("select count(1) from feature_group where vlm_id=? AND version=?")
    ResultSet countByVlmVersion(String vendorLicenseModelId, UDTValue vendorLicenseModelVersion);

    @Query("delete from feature_group where vlm_id=? AND version=?")
    ResultSet deleteByVlmVersion(String vendorLicenseModelId, UDTValue vendorLicenseModelVersion);

    @Query(
        "update feature_group set name=?,description=?, part_num=?, ep_ids=ep_ids+ ?,"
            + "ep_ids=ep_ids-?, lkg_ids=lkg_ids+?,lkg_ids=lkg_ids-? WHERE vlm_id=? AND version=? "
            + "AND fg_id=?")
    ResultSet updateColumnsAndDeltaFeatureGroupIds(String name, String description,
                                                   String partNumber,
                                                   Set<String> addedEntitlementPools,
                                                   Set<String> removedEntitlementPools,
                                                   Set<String> addedLicenseKeyGroups,
                                                   Set<String> removedLicenseKeyGroups,
                                                   String vendorLicenseModelId, UDTValue version,
                                                   String id);

    @Query(
        "UPDATE feature_group SET ref_la_ids = ref_la_ids + ? WHERE vlm_id=? AND version=? "
            + "AND fg_id=?")
    ResultSet addReferencingLicenseAgreements(Set<String> licenseAgreementIds,
                                              String vendorLicenseModelId, UDTValue version,
                                              String id);

    @Query(
        "UPDATE feature_group SET ref_la_ids = ref_la_ids - ? WHERE vlm_id=? AND version=? AND "
            + "fg_id=?")
    ResultSet removeReferencingLicenseAgreements(Set<String> licenseAgreementIds,
                                                 String vendorLicenseModelId, UDTValue version,
                                                 String id);

    @Query("UPDATE feature_group SET ep_ids = ep_ids - ? WHERE vlm_id=? AND version=? AND fg_id=?")
    ResultSet removeEntitlementPools(Set<String> entitlementPoolIds, String vendorLicenseModelId,
                                     UDTValue version, String id);

    @Query(
        "UPDATE feature_group SET lkg_ids = lkg_ids - ? WHERE vlm_id=? AND version=? AND fg_id=?")
    ResultSet removeLicenseKeyGroup(Set<String> licenseKeyGroupIds, String vendorLicenseModelId,
                                    UDTValue version, String id);

  }
}

