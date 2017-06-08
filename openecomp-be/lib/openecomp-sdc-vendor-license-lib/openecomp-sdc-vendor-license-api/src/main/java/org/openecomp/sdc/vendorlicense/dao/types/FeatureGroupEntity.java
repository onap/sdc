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

@Table(name = "feature_group", keyspace = "dox")
public class FeatureGroupEntity implements VersionableEntity {
  private static final String ENTITY_TYPE = "Feature Group";

  @PartitionKey
  @Column(name = "vlm_id")
  private String vendorLicenseModelId;
  @PartitionKey(value = 1)
  @Frozen
  private Version version;
  @ClusteringColumn
  @Column(name = "fg_id")
  private String id;
  private String name;
  private String description;
  @Column(name = "part_num")
  private String partNumber;
  @Column(name = "lkg_ids")
  private Set<String> licenseKeyGroupIds = new HashSet<>();
  @Column(name = "ep_ids")
  private Set<String> entitlementPoolIds = new HashSet<>();
  @Column(name = "ref_la_ids")
  private Set<String> referencingLicenseAgreements = new HashSet<>();

  public FeatureGroupEntity() {
  }

  /**
   * Instantiates a new Feature group entity.
   *
   * @param vendorLicenseModelId the vendor license model id
   * @param version              the version
   * @param id                   the id
   */
  public FeatureGroupEntity(String vendorLicenseModelId, Version version, String id) {
    this.vendorLicenseModelId = vendorLicenseModelId;
    this.version = version;
    this.id = id;
  }

  @Override
  public String getEntityType() {
    return ENTITY_TYPE;
  }

  @Override
  public String getFirstClassCitizenId() {
    return getVendorLicenseModelId();
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public Version getVersion() {
    return version;
  }

  @Override
  public void setVersion(Version version) {
    this.version = version;
  }

  public String getVendorLicenseModelId() {
    return vendorLicenseModelId;
  }

  public void setVendorLicenseModelId(String vendorLicenseModelId) {
    this.vendorLicenseModelId = vendorLicenseModelId;
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

  public String getPartNumber() {
    return partNumber;
  }

  public void setPartNumber(String partNumber) {
    this.partNumber = partNumber;
  }

  public Set<String> getLicenseKeyGroupIds() {
    return licenseKeyGroupIds;
  }

  public void setLicenseKeyGroupIds(Set<String> licenseKeyGroupIds) {
    this.licenseKeyGroupIds = licenseKeyGroupIds;
  }

  public Set<String> getEntitlementPoolIds() {
    return entitlementPoolIds;
  }

  public void setEntitlementPoolIds(Set<String> entitlementPoolIds) {
    this.entitlementPoolIds = entitlementPoolIds;
  }

  public Set<String> getReferencingLicenseAgreements() {
    return referencingLicenseAgreements;
  }

  public void setReferencingLicenseAgreements(Set<String> referencingLicenseAgreements) {
    this.referencingLicenseAgreements = referencingLicenseAgreements;
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(vendorLicenseModelId, version, id, name, description, partNumber, licenseKeyGroupIds,
            entitlementPoolIds, referencingLicenseAgreements);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    FeatureGroupEntity that = (FeatureGroupEntity) obj;
    return Objects.equals(vendorLicenseModelId, that.vendorLicenseModelId)
        && Objects.equals(version, that.version)
        && Objects.equals(id, that.id)
        && Objects.equals(name, that.name)
        && Objects.equals(description, that.description)
        && Objects.equals(partNumber, that.partNumber)
        && Objects.equals(licenseKeyGroupIds, that.licenseKeyGroupIds)
        && Objects.equals(entitlementPoolIds, that.entitlementPoolIds)
        && Objects.equals(referencingLicenseAgreements, that.referencingLicenseAgreements);
  }

  @Override
  public String toString() {
    return "FeatureGroupEntity{"
        + "vendorLicenseModelId='" + vendorLicenseModelId + '\''
        + ", version=" + version
        + ", id='" + id + '\''
        + ", name='" + name + '\''
        + ", description='" + description + '\''
        + ", partNumber='" + partNumber + '\''
        + ", licenseKeyGroupIds=" + licenseKeyGroupIds
        + ", entitlementPoolIds=" + entitlementPoolIds
        + ", referencingLicenseAgreements=" + referencingLicenseAgreements
        + '}';
  }
}
