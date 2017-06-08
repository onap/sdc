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
import com.datastax.driver.mapping.annotations.Enumerated;
import com.datastax.driver.mapping.annotations.Frozen;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import org.openecomp.sdc.vendorlicense.dao.types.xml.LicenseKeyTypeForXml;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionableEntity;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Table(keyspace = "dox", name = "license_key_group")
public class LicenseKeyGroupEntity implements VersionableEntity {
  private static final String ENTITY_TYPE = "License Key Group";

  @PartitionKey
  @Column(name = "vlm_id")
  private String vendorLicenseModelId;
  @PartitionKey(value = 1)
  @Frozen
  private Version version;
  @ClusteringColumn
  @Column(name = "lkg_id")
  private String id;
  private String name;
  private String description;
  @Enumerated
  private LicenseKeyType type;
  @Column(name = "operational_scope")
  @Frozen
  private MultiChoiceOrOther<OperationalScope> operationalScope;
  @Column(name = "ref_fg_ids")
  private Set<String> referencingFeatureGroups = new HashSet<>();
  @Column(name = "version_uuid")
  private String versionUuId;


  public LicenseKeyGroupEntity() {
  }

  /**
   * Instantiates a new License key group entity.
   *
   * @param vendorLicenseModelId the vendor license model id
   * @param version              the version
   * @param id                   the id
   */
  public LicenseKeyGroupEntity(String vendorLicenseModelId, Version version, String id) {
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

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Version getVersion() {
    return version;
  }

  public void setVersion(Version version) {
    this.version = version;
  }

  @Override
  public String getVersionUuId() {
    return versionUuId;
  }

  @Override
  public void setVersionUuId(String uuId) {
    versionUuId = uuId;
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

  public LicenseKeyType getType() {
    return type;
  }

  public void setType(LicenseKeyType type) {
    this.type = type;
  }

  public MultiChoiceOrOther<OperationalScope> getOperationalScope() {
    return operationalScope;
  }

  public void setOperationalScope(MultiChoiceOrOther<OperationalScope> operationalScope) {
    operationalScope.resolveEnum(OperationalScope.class);
    this.operationalScope = operationalScope;
  }

  public Set<String> getReferencingFeatureGroups() {
    return referencingFeatureGroups;
  }

  public void setReferencingFeatureGroups(Set<String> referencingFeatureGroups) {
    this.referencingFeatureGroups = referencingFeatureGroups;
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(vendorLicenseModelId, version, id, name, description, type, operationalScope,
            referencingFeatureGroups);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    LicenseKeyGroupEntity that = (LicenseKeyGroupEntity) obj;
    return Objects.equals(vendorLicenseModelId, that.vendorLicenseModelId)
        && Objects.equals(id, that.id)
        && Objects.equals(name, that.name)
        && Objects.equals(description, that.description)
        && type == that.type
        && Objects.equals(operationalScope, that.operationalScope)
        && Objects.equals(referencingFeatureGroups, that.referencingFeatureGroups);
  }

  @Override
  public String toString() {
    return "LicenseKeyGroupEntity{" + "vendorLicenseModelId='" + vendorLicenseModelId + '\''
        + ", version=" + version
        + ", id='" + id + '\''
        + ", name='" + name + '\''
        + ", description='" + description + '\''
        + ", type=" + type
        + ", operationalScope=" + operationalScope
        + ", referencingFeatureGroups=" + referencingFeatureGroups
        + ", versionUuId='" + versionUuId + '\''
        + '}';
  }

  /**
   * Gets operational scope for artifact.
   *
   * @return the operational scope for artifact
   */
  public Set<String> getOperationalScopeForArtifact() {
    if (operationalScope != null) {
      return operationalScope.getResults();
    } else {
      return null;
    }
  }

  /**
   * Gets version for artifact.
   * @return version in format suitable for artifact
   */
  public String getVersionForArtifact() {
    return version.toString();
  }

  /**
   * Gets type for artifact.
   *
   * @return the type for artifact
   */
  public LicenseKeyTypeForXml getTypeForArtifact() {
    LicenseKeyTypeForXml typeXml = new LicenseKeyTypeForXml();
    if (type != null) {
      typeXml.setValue(type.toString());
    } else {
      typeXml.setValue(null);
    }
    return typeXml;
  }
}
