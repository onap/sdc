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
import org.openecomp.sdc.vendorlicense.dao.LicenseAgreementDao;
import org.openecomp.sdc.vendorlicense.dao.types.ChoiceOrOther;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.UniqueValueMetadata;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class LicenseAgreementCassandraDaoImpl extends CassandraBaseDao<LicenseAgreementEntity>
    implements LicenseAgreementDao {
  private static NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static Mapper<LicenseAgreementEntity> mapper =
      noSqlDb.getMappingManager().mapper(LicenseAgreementEntity.class);
  private static LicenseAgreementAccessor accessor =
      noSqlDb.getMappingManager().createAccessor(LicenseAgreementAccessor.class);
  private static UDTMapper<ChoiceOrOther> choiceOrOtherMapper =
      noSqlDb.getMappingManager().udtMapper(ChoiceOrOther.class);
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
        new UniqueValueMetadata(VendorLicenseConstants.UniqueValues.LICENSE_AGREEMENT_NAME,
            Arrays.asList(mapper.getTableMetadata().getPartitionKey().get(0).getName(),
                mapper.getTableMetadata().getPartitionKey().get(1).getName(), "name"))));

    VersioningManagerFactory.getInstance().createInterface()
        .register(versionableEntityType, metadata);
  }

  @Override
  protected Mapper<LicenseAgreementEntity> getMapper() {
    return mapper;
  }

  @Override
  protected Object[] getKeys(LicenseAgreementEntity entity) {
    return new Object[]{entity.getVendorLicenseModelId(), versionMapper.toUDT(entity.getVersion()),
        entity.getId()};
  }

  @Override
  public Collection<LicenseAgreementEntity> list(LicenseAgreementEntity entity) {
    return accessor.listByVlmVersion(entity.getVendorLicenseModelId(),
        versionMapper.toUDT(entity.getVersion())).all();
  }

  @Override
  public long count(LicenseAgreementEntity entity) {
    return accessor.countByVlmVersion(entity.getVendorLicenseModelId(),
        versionMapper.toUDT(entity.getVersion())).one().getLong("count");
  }

  @Override
  public void deleteAll(LicenseAgreementEntity entity) {
    accessor.deleteByVlmVersion(entity.getVendorLicenseModelId(),
        versionMapper.toUDT(entity.getVersion())).all();
  }

  @Override
  public void removeFeatureGroup(LicenseAgreementEntity licenseAgreement, String featureGroupId) {
    accessor.removeFeatureGroup(CommonMethods.toSingleElementSet(featureGroupId),
        licenseAgreement.getVendorLicenseModelId(),
        versionMapper.toUDT(licenseAgreement.getVersion()), licenseAgreement.getId());
  }

  @Override
  public void updateColumnsAndDeltaFeatureGroupIds(LicenseAgreementEntity licenseAgreement,
                                                   Set<String> addedFeatureGroupIds,
                                                   Set<String> removedFeatureGroupIds) {
    accessor.updateColumnsAndDeltaFeatureGroupIds(licenseAgreement.getName(),
        licenseAgreement.getDescription(),
        licenseAgreement.getLicenseTerm() == null ? null
            : choiceOrOtherMapper.toUDT(licenseAgreement.getLicenseTerm()),
        licenseAgreement.getRequirementsAndConstrains(),
        emptyIfNull(addedFeatureGroupIds),
        emptyIfNull(removedFeatureGroupIds),
        licenseAgreement.getVendorLicenseModelId(),
        versionMapper.toUDT(licenseAgreement.getVersion()),
        licenseAgreement.getId());

  }

  @Accessor
  interface LicenseAgreementAccessor {

    @Query("SELECT * FROM license_agreement WHERE vlm_id=? and version=?")
    Result<LicenseAgreementEntity> listByVlmVersion(String vendorLicenseModelId, UDTValue version);

    @Query("select count(1) from license_agreement where vlm_id=? AND version=?")
    ResultSet countByVlmVersion(String vendorLicenseModelId, UDTValue vendorLicenseModelVersion);

    @Query("delete from license_agreement where vlm_id=? AND version=?")
    ResultSet deleteByVlmVersion(String vendorLicenseModelId, UDTValue vendorLicenseModelVersion);

    @Query(
        "UPDATE license_agreement SET name=?, description=?, lic_term=?, req_const=?, "
            + "fg_ids=fg_ids+?, fg_ids=fg_ids-? WHERE vlm_id=? AND version=? AND la_id=?")
    ResultSet updateColumnsAndDeltaFeatureGroupIds(String name, String description,
                                                   UDTValue licenseTerm, String reqAndConst,
                                                   Set<String> addedFeatureGroupIds,
                                                   Set<String> removedFeatureGroupIds,
                                                   String vendorLicenseModelId, UDTValue version,
                                                   String id);

    @Query("UPDATE license_agreement SET fg_ids=fg_ids-? WHERE vlm_id=? AND version=? AND la_id=?")
    ResultSet removeFeatureGroup(Set<String> featureGroupIds, String vendorLicenseModelId,
                                 UDTValue version, String id);
  }
}
