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
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.vendorlicense.VendorLicenseUtil;
import org.openecomp.sdc.vendorlicense.dao.types.xml.LimitForXml;
import org.openecomp.sdc.vendorlicense.dao.types.xml.LimitXml;
import org.openecomp.sdc.vendorlicense.dao.types.xml.OperationalScopeForXml;
import org.openecomp.sdc.vendorlicense.dao.types.xml.ThresholdForXml;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionableEntity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Table(keyspace = "dox", name = "entitlement_pool")
public class EntitlementPoolEntity implements VersionableEntity {

  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private static final String ENTITY_TYPE = "Entitlement Pool";

  @PartitionKey
  @Column(name = "vlm_id")
  private String vendorLicenseModelId;

  @PartitionKey(value = 1)
  @Frozen
  private Version version;

  @ClusteringColumn
  @Column(name = "ep_id")
  private String id;
  private String name;
  private String description;

  @Column(name = "threshold")
  private Integer thresholdValue;

  @Column(name = "threshold_unit")
  @Enumerated
  private ThresholdUnit thresholdUnit;

  private String increments;

  @Column(name = "operational_scope")
  @Frozen
  private MultiChoiceOrOther<OperationalScope> operationalScope;

  @Column(name = "ref_fg_ids")
  private Set<String> referencingFeatureGroups = new HashSet<>();

  @Column(name = "version_uuid")
  private String versionUuId;


  private String startDate;
  private String expiryDate;

  private Collection<LimitEntity> limits;

  //Defined and used only for License Artifcat XMLs
  private String manufacturerReferenceNumber;

  public EntitlementPoolEntity() {
  }

  /**
   * Instantiates a new Entitlement pool entity.
   *
   * @param vlmId   the vlm id
   * @param version the version
   * @param id      the id
   */
  public EntitlementPoolEntity(String vlmId, Version version, String id) {
    this.vendorLicenseModelId = vlmId;
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

  public Set<String> getReferencingFeatureGroups() {
    return referencingFeatureGroups;
  }

  public void setReferencingFeatureGroups(Set<String> referencingFeatureGroups) {
    this.referencingFeatureGroups = referencingFeatureGroups;
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

  public Integer getThresholdValue() {
    return thresholdValue;
  }

  public void setThresholdValue(Integer thresholdValue) {
    this.thresholdValue = thresholdValue;
  }

  public ThresholdUnit getThresholdUnit() {
    return thresholdUnit;
  }

  public void setThresholdUnit(ThresholdUnit thresholdUnits) {
    this.thresholdUnit = thresholdUnits;
  }

  public String getIncrements() {
    return increments;
  }

  public void setIncrements(String increments) {
    this.increments = increments;
  }

  public MultiChoiceOrOther<OperationalScope> getOperationalScope() {
    return operationalScope;
  }

  public void setOperationalScope(MultiChoiceOrOther<OperationalScope> operationalScope) {
    if (operationalScope != null) {
      operationalScope.resolveEnum(OperationalScope.class);
    }
    this.operationalScope = operationalScope;
  }

  /**
   * Gets threshold for artifact.
   *
   * @return the threshold for artifact
   */
  public ThresholdForXml getThresholdForArtifact() {
    ThresholdForXml threshold = new ThresholdForXml();
    threshold.setUnit(getThresholdUnit() == null ? null : getThresholdUnit().name());
    threshold.setValue(getThresholdValue());
    return threshold;
  }

  /**
   * Gets version for artifact.
   *
   * @return version in format suitable for artifact
   */
  public String getVersionForArtifact() {
    return version.toString();
  }

  public String getStartDate() {
    return startDate;
  }

  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  public String getExpiryDate() {
    return expiryDate;
  }

  public void setExpiryDate(String expiryDate) {
    this.expiryDate = expiryDate;
  }

  public Collection<LimitEntity> getLimits() {
    return limits;
  }

  public void setLimits(Collection<LimitEntity> limits) {
    this.limits = limits;
  }

  public LimitForXml getSPLimits() {
    if (limits != null) {
      Set<LimitXml> hs = new HashSet<>();
      for (LimitEntity obj : limits) {
        if (obj.getType().equals(LimitType.ServiceProvider)) {
          LimitXml xmlObj = new LimitXml();
          xmlObj.setDescription(obj.getDescription());
          xmlObj.setMetric(obj.getMetric());
          xmlObj.setValues(obj.getValue());
          xmlObj.setUnit(obj.getUnit());
          xmlObj.setAggregationFunction(
              obj.getAggregationFunction() != null ? obj.getAggregationFunction().name() : null);
          xmlObj.setTime(obj.getTime());
          hs.add(xmlObj);
        }
      }
      LimitForXml spLimitForXml = new LimitForXml();
      spLimitForXml.setLimits(hs);
      return spLimitForXml;
    }

    return null;
  }

  public LimitForXml getVendorLimits() {
    if (limits != null) {
      Set<LimitXml> hs = new HashSet<>();
      for (LimitEntity obj : limits) {
        if (obj.getType().equals(LimitType.Vendor)) {
          LimitXml xmlObj = new LimitXml();
          xmlObj.setDescription(obj.getDescription());
          xmlObj.setMetric(obj.getMetric());
          xmlObj.setValues(obj.getValue());
          xmlObj.setUnit(obj.getUnit());
          xmlObj.setAggregationFunction(
              obj.getAggregationFunction() != null ? obj.getAggregationFunction().name() : null);
          xmlObj.setTime(obj.getTime());
          hs.add(xmlObj);
        }
      }
      LimitForXml vendorLimitForXml = new LimitForXml();
      vendorLimitForXml.setLimits(hs);
      return vendorLimitForXml;
    }

    return null;
  }


  @Override
  public int hashCode() {
    return Objects
        .hash(vendorLicenseModelId, version, id, name, description, thresholdValue, thresholdUnit,
            increments, operationalScope, referencingFeatureGroups, startDate, expiryDate);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    EntitlementPoolEntity that = (EntitlementPoolEntity) obj;
    return Objects.equals(that.thresholdValue, thresholdValue)
        && Objects.equals(vendorLicenseModelId, that.vendorLicenseModelId)
        && Objects.equals(id, that.id)
        && Objects.equals(name, that.name)
        && Objects.equals(description, that.description)
        && Objects.equals(thresholdUnit, that.thresholdUnit)
        && Objects.equals(increments, that.increments)
        && Objects.equals(operationalScope, that.operationalScope)
        && Objects.equals(referencingFeatureGroups, that.referencingFeatureGroups)
        && Objects.equals(startDate, that.startDate)
        && Objects.equals(expiryDate, that.expiryDate)
        && Objects.equals(manufacturerReferenceNumber, that.manufacturerReferenceNumber)
        && Objects.equals(version, that.version);
  }

  @Override
  public String toString() {
    return "EntitlementPoolEntity{"
        + "vendorLicenseModelId='" + vendorLicenseModelId + '\''
        + ", version=" + version
        + ", id='" + id + '\''
        + ", name='" + name + '\''
        + ", description='" + description + '\''
        + ", thresholdValue=" + thresholdValue
        + ", thresholdUnit='" + thresholdUnit + '\''
        + ", increments='" + increments + '\''
        + ", operationalScope=" + operationalScope
        + ", referencingFeatureGroups=" + referencingFeatureGroups
        + ", version_uuid=" + versionUuId
        + ", startDate=" + startDate
        + ", expiryDate=" + expiryDate
        + '}';
  }

  /**
   * Gets operational scope for artifact.
   *
   * @return the operational scope for artifact
   */
  public OperationalScopeForXml getOperationalScopeForArtifact() {
    OperationalScopeForXml obj = new OperationalScopeForXml();
    if (operationalScope != null) {
      if (operationalScope.getResults().size() > 0) {
        obj.setValue(operationalScope.getResults());
      }
    }
    return obj;
  }

  //Defined and used only for License Artifcat XMLs
  public void setManufacturerReferenceNumber(String manufacturerReferenceNumber) {
    this.manufacturerReferenceNumber = manufacturerReferenceNumber;
  }

  public String getManufacturerReferenceNumber() {
    return manufacturerReferenceNumber;
  }

  public String getIsoFormatStartDate() {
    mdcDataDebugMessage.debugEntryMessage("start date", startDate);
    String isoFormatStartDate = null;
    if (!StringUtils.isEmpty(startDate)) {
      isoFormatStartDate = VendorLicenseUtil.getIsoFormatDate(startDate);
      mdcDataDebugMessage.debugExitMessage("start date", "iso format start date", startDate,
          isoFormatStartDate);
    }
    return isoFormatStartDate;
  }


  public String getIsoFormatExpiryDate() {
    mdcDataDebugMessage.debugEntryMessage("expiry date", expiryDate);
    String isoFormatExpDate = null;
    if (!StringUtils.isEmpty(expiryDate)) {
      isoFormatExpDate = VendorLicenseUtil.getIsoFormatDate(expiryDate);
      mdcDataDebugMessage.debugExitMessage("expiry date", "iso format expiry date", expiryDate,
          isoFormatExpDate);
    }
    return isoFormatExpDate;
  }
}
