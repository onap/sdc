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

package org.openecomp.sdc.vendorlicense.dao.types;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Frozen;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionableEntity;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Table(keyspace = "dox", name = "license_agreement")
public class LicenseAgreementEntity implements VersionableEntity {
  public static final String ENTITY_TYPE = "License Agreement";

  @PartitionKey(value = 0)
  @Column(name = "vlm_id")
  private String vendorLicenseModelId;

  @PartitionKey(value = 1)
  @Frozen
  private Version version;

  @ClusteringColumn
  @Column(name = "la_id")
  private String id;
  private String name;
  private String description;

  @Column(name = "lic_term")
  @Frozen
  private ChoiceOrOther<LicenseTerm> licenseTerm;

  @Column(name = "req_const")
  private String requirementsAndConstrains;

  @Column(name = "fg_ids")
  private Set<String> featureGroupIds = new HashSet<>();

  public LicenseAgreementEntity() {
  }

  /**
   * Instantiates a new License agreement entity.
   *
   * @param vlmId   the vlm id
   * @param version the version
   * @param id      the id
   */
  public LicenseAgreementEntity(String vlmId, Version version, String id) {
    this.vendorLicenseModelId = vlmId;
    this.id = id;
    this.version = version;
  }

  @Override
  public String getEntityType() {
    return ENTITY_TYPE;
  }

  @Override
  public String getFirstClassCitizenId() {
    return getVendorLicenseModelId();
  }

  public String getVendorLicenseModelId() {
    return vendorLicenseModelId;
  }

  public void setVendorLicenseModelId(String vendorLicenseModelId) {
    this.vendorLicenseModelId = vendorLicenseModelId;
  }

  @Override
  public Version getVersion() {
    return version;
  }

  @Override
  public void setVersion(Version version) {
    this.version = version;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public ChoiceOrOther<LicenseTerm> getLicenseTerm() {
    return licenseTerm;
  }

  public void setLicenseTerm(ChoiceOrOther<LicenseTerm> licenseTerm) {
    licenseTerm.resolveEnum(LicenseTerm.class);
    this.licenseTerm = licenseTerm;
  }

  public String getRequirementsAndConstrains() {
    return requirementsAndConstrains;
  }

  public void setRequirementsAndConstrains(String requirementsAndConstrains) {
    this.requirementsAndConstrains = requirementsAndConstrains;
  }

  public Set<String> getFeatureGroupIds() {
    return featureGroupIds;
  }

  public void setFeatureGroupIds(Set<String> featureGroupIds) {
    this.featureGroupIds = featureGroupIds;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    LicenseAgreementEntity that = (LicenseAgreementEntity) obj;
    return Objects.equals(vendorLicenseModelId, that.vendorLicenseModelId)
        && Objects.equals(version, that.version)
        && Objects.equals(id, that.id)
        && Objects.equals(name, that.name)
        && Objects.equals(description, that.description)
        && Objects.equals(licenseTerm, that.licenseTerm)
        && Objects.equals(requirementsAndConstrains, that.requirementsAndConstrains)
        && Objects.equals(featureGroupIds, that.featureGroupIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(vendorLicenseModelId, version, id, name, description, licenseTerm,
        requirementsAndConstrains, featureGroupIds);
  }
}
