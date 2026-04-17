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


import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionableEntity;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.datastax.oss.driver.api.mapper.annotations.Transient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
@CqlName("license_agreement")
public class LicenseAgreementEntity implements VersionableEntity {

    public static final String ENTITY_TYPE = "License Agreement";
    @PartitionKey(value = 0)
    @CqlName("vlm_id")
    private String vendorLicenseModelId;
    @PartitionKey(value = 1)

    private Version version;
    @ClusteringColumn
    @CqlName("la_id")
    private String id;
    private String name;
    private String description;
    @CqlName("lic_term")

    private String licenseTerm;
    @CqlName("req_const")
    private String requirementsAndConstrains;
    @CqlName("fg_ids")
    private Set<String> featureGroupIds = new HashSet<>();

    @Transient
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Every entity class must have a default constructor according to
     * <a href="http://docs.datastax.com/en/developer/java-driver/2.1/manual/object_mapper/creating/">
     * Definition of mapped classes</a>.
     */
    public LicenseAgreementEntity() {
        // Don't delete! Default constructor is required by DataStax driver
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

    @Transient
    public ChoiceOrOther<LicenseTerm> getLicenseTerm() {
    if (licenseTerm == null) {
        return null;
    }
    try {
        return MAPPER.readValue(
            licenseTerm,
            new TypeReference<ChoiceOrOther<LicenseTerm>>() {}
        );
    } catch (Exception e) {
        throw new RuntimeException("Failed to deserialize licenseTerm", e);
    }
}

@Transient
public void setLicenseTerm(ChoiceOrOther<LicenseTerm> term) {
    if (term != null) {
        term.resolveEnum(LicenseTerm.class);
        try {
            this.licenseTerm = MAPPER.writeValueAsString(term);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize licenseTerm", e);
        }
    } else {
        this.licenseTerm = null;
    }
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
    public int hashCode() {
        return Objects.hash(vendorLicenseModelId, version, id, name, description, licenseTerm, requirementsAndConstrains, featureGroupIds);
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
        return Objects.equals(vendorLicenseModelId, that.vendorLicenseModelId) && Objects.equals(version, that.version) && Objects.equals(id, that.id)
            && Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(licenseTerm, that.licenseTerm)
            && Objects.equals(requirementsAndConstrains, that.requirementsAndConstrains) && Objects.equals(featureGroupIds, that.featureGroupIds);
    }

    
}
