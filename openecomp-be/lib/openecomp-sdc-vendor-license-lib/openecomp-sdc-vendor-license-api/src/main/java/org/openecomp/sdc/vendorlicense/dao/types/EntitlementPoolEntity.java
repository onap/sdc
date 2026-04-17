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


import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.datastax.oss.driver.api.mapper.annotations.Transient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.vendorlicense.VendorLicenseUtil;
import org.openecomp.sdc.vendorlicense.dao.types.xml.LimitForXml;
import org.openecomp.sdc.vendorlicense.dao.types.xml.LimitXml;
import org.openecomp.sdc.vendorlicense.dao.types.xml.OperationalScopeForXml;
import org.openecomp.sdc.vendorlicense.dao.types.xml.ThresholdForXml;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionableEntity;

@Entity
@CqlName("entitlement_pool")
public class EntitlementPoolEntity implements VersionableEntity {

    private static final String ENTITY_TYPE = "Entitlement Pool";
    @PartitionKey
    @CqlName("vlm_id")
    private String vendorLicenseModelId;
    @PartitionKey(value = 1)
   
    private Version version;
    @ClusteringColumn
    @CqlName("ep_id")
    private String id;
    private String name;
    private String description;
    private EntitlementPoolType type;
    @CqlName("threshold")
    private Integer thresholdValue;
    @CqlName("threshold_unit")
    private ThresholdUnit thresholdUnit;
    private String increments;
    @CqlName("operational_scope")
    private String operationalScope;
    @CqlName("ref_fg_ids")
    private Set<String> referencingFeatureGroups = new HashSet<>();
    @CqlName("version_uuid")
    private String versionUuId;
    private String startDate;
    private String expiryDate;
    @Transient
    private Collection<LimitEntity> limits;
    //Defined and used only for License Artifcat XMLs
    private String manufacturerReferenceNumber;

    private static final ObjectMapper MAPPER = new ObjectMapper();


    /**
     * Every entity class must have a default constructor according to
     * <a href="http://docs.datastax.com/en/developer/java-driver/2.1/manual/object_mapper/creating/">
     * Definition of mapped classes</a>.
     */
    public EntitlementPoolEntity() {
        // Don't delete! Default constructor is required by DataStax driver
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


    public String getEntityType() {
        return ENTITY_TYPE;
    }


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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(final String startDate) {
        this.startDate = startDate;
    }

    public void setStartDate(final Object startDate) {
        this.startDate = startDate == null ? null : String.valueOf(startDate);
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(final String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setExpiryDate(final Object expiryDate) {
        this.expiryDate = expiryDate == null ? null : String.valueOf(expiryDate);
    }

    public Collection<LimitEntity> getLimits() {
        return limits;
    }

    public void setLimits(final Collection<LimitEntity> limits) {
        this.limits = limits;
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

    public EntitlementPoolType getType() {
        return type;
    }

    public void setType(EntitlementPoolType type) {
        this.type = type;
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

@Transient
public MultiChoiceOrOther<OperationalScope> getOperationalScope() {
    if (operationalScope == null) {
        return null;
    }
    try {
        MultiChoiceOrOther<OperationalScope> scope =
            MAPPER.readValue(operationalScope,
                new TypeReference<MultiChoiceOrOther<OperationalScope>>() {});

        // Defensive: rebuild results if missing
        if (scope.getResults() == null || scope.getResults().isEmpty()) {
            if (scope.getChoices() != null && !scope.getChoices().isEmpty()) {
                // Copy enum names from choices into results
                Set<String> rebuiltResults = scope.getChoices().stream()
                    .map(Enum::name)
                    .collect(Collectors.toSet());
                scope.setResults(rebuiltResults);
            } else {
                scope.setResults(new HashSet<>()); // safe default
            }
        }

        // Rebuild choices from results if needed
        scope.resolveEnum(OperationalScope.class);

        return scope;
    } catch (Exception e) {
        throw new RuntimeException("Failed to parse operationalScope JSON", e);
    }
}


  @Transient
  public void setOperationalScope(MultiChoiceOrOther<OperationalScope> scope) {
    if (scope != null) {
        scope.resolveEnum(OperationalScope.class);

        // Defensive fix: ensure results is not null
        if (scope.getResults() == null || scope.getResults().isEmpty()) {
            scope.setResults(scope.getChoices() != null
                ? scope.getChoices().stream().map(Enum::name).collect(Collectors.toSet())
                : new HashSet<>());
        }

        try {
            this.operationalScope = MAPPER.writeValueAsString(scope);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize operationalScope", e);
        }
    } else {
        this.operationalScope = null;
    }
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
                    xmlObj.setAggregationFunction(obj.getAggregationFunction() != null ? obj.getAggregationFunction().name() : null);
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
                    xmlObj.setAggregationFunction(obj.getAggregationFunction() != null ? obj.getAggregationFunction().name() : null);
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
        return Objects.hash(vendorLicenseModelId, version, id, name, description, type, thresholdValue, thresholdUnit, increments, operationalScope,
            referencingFeatureGroups, startDate, expiryDate);
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
        return Objects.equals(that.thresholdValue, thresholdValue) && Objects.equals(vendorLicenseModelId, that.vendorLicenseModelId) && Objects
            .equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(description, that.description) && type == that.type && Objects
            .equals(thresholdUnit, that.thresholdUnit) && Objects.equals(increments, that.increments) && Objects
            .equals(that.operationalScope, operationalScope) && Objects.equals(startDate, that.startDate) && Objects
            .equals(expiryDate, that.expiryDate) && Objects.equals(manufacturerReferenceNumber, that.manufacturerReferenceNumber) && Objects
            .equals(version, that.version);
    }

    @Override
    public String toString() {
        return "EntitlementPoolEntity{" + "vendorLicenseModelId='" + vendorLicenseModelId + '\'' + ", version=" + version + ", id='" + id + '\''
            + ", name='" + name + '\'' + ", description='" + description + '\'' + ", type=" + type + ", thresholdValue=" + thresholdValue
            + ", thresholdUnit='" + thresholdUnit + '\'' + ", increments='" + increments + '\'' + ", operationalScope=" + operationalScope
            + ", referencingFeatureGroups=" + referencingFeatureGroups + ", version_uuid=" + versionUuId + ", startDate=" + startDate
            + ", expiryDate=" + expiryDate + '}';
    }

    /**
     * Gets operational scope for artifact.
     *
     * @return the operational scope for artifact
     */
    public OperationalScopeForXml getOperationalScopeForArtifact() {
        OperationalScopeForXml obj = new OperationalScopeForXml();
        MultiChoiceOrOther<OperationalScope> scope = getOperationalScope();

        if (scope == null) {
            obj.setValue(Collections.emptySet());
            return obj;
        }

        scope.resolveEnum(OperationalScope.class);
        Set<String> results = scope.getResults();
        obj.setValue(results == null ? Collections.emptySet() : results);
        return obj;
    }


    public String getManufacturerReferenceNumber() {
        return manufacturerReferenceNumber;
    }

    //Defined and used only for License Artifcat XMLs
    public void setManufacturerReferenceNumber(String manufacturerReferenceNumber) {
        this.manufacturerReferenceNumber = manufacturerReferenceNumber;
    }

    public String getIsoFormatStartDate() {
        String isoFormatStartDate = null;
        if (!StringUtils.isEmpty(startDate)) {
            isoFormatStartDate = VendorLicenseUtil.getIsoFormatDate(startDate);
        }
        return isoFormatStartDate;
    }

    public String getIsoFormatExpiryDate() {
        String isoFormatExpDate = null;
        if (!StringUtils.isEmpty(expiryDate)) {
            isoFormatExpDate = VendorLicenseUtil.getIsoFormatDate(expiryDate);
        }
        return isoFormatExpDate;
    }
}
