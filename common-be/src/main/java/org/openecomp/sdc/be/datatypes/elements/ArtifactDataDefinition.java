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

package org.openecomp.sdc.be.datatypes.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;

public class ArtifactDataDefinition extends ToscaDataDefinition {

    public ArtifactDataDefinition() {
        setArtifactVersion("0");
    }

    public ArtifactDataDefinition(Map<String, Object> art) {
        super(art);
        setArtifactVersion("0");
    }


    public ArtifactDataDefinition(ArtifactDataDefinition a) {
        this.setUniqueId(a.getUniqueId());
        this.setArtifactType(a.getArtifactType());
        this.setArtifactRef(a.getArtifactRef());
        this.setArtifactName(a.getArtifactName());
        this.setArtifactRepository(a.getArtifactRepository());
        this.setArtifactChecksum(a.getArtifactChecksum());
        this.setUserIdCreator(a.getUserIdCreator());
        this.setUserIdLastUpdater(a.getUserIdLastUpdater());
        this.setCreatorFullName(a.getCreatorFullName());
        this.setUpdaterFullName(a.getUpdaterFullName());
        this.setCreationDate(a.getCreationDate());
        this.setLastUpdateDate(a.getLastUpdateDate());
        this.setDescription(a.getDescription());
        this.setEsId(a.getEsId());
        this.setArtifactLabel(a.getArtifactLabel());
        this.setArtifactCreator(a.getArtifactCreator());
        this.setMandatory(a.getMandatory());
        this.setArtifactDisplayName(a.getArtifactDisplayName());
        this.setApiUrl(a.getApiUrl());
        this.setServiceApi(a.getServiceApi());
        this.setArtifactGroupType(a.getArtifactGroupType());
        this.setTimeout(a.getTimeout());
        this.setArtifactVersion(a.getArtifactVersion());
        this.setArtifactUUID(a.getArtifactUUID());
        this.setPayloadUpdateDate(a.getPayloadUpdateDate());
        this.setHeatParamsUpdateDate(a.getHeatParamsUpdateDate());
        this.setGenerated(a.getGenerated());
        this.setDuplicated(a.getDuplicated());
        if (a.getRequiredArtifacts() != null) {
            this.setRequiredArtifacts(new ArrayList<>(a.getRequiredArtifacts()));
        }
        if (a.getHeatParameters() != null) {
            this.setHeatParameters(new ArrayList<>(a.getHeatParameters()));
        }
        this.setGeneratedFromId(a.getGeneratedFromId());
        this.setIsFromCsar(a.getIsFromCsar());
        if (a.getProperties() != null) {
            this.setProperties(new ArrayList<>(a.getProperties()));
        }

    }

    public String getArtifactName() {
        return (String) getToscaPresentationValue(JsonPresentationFields.NAME);

    }

    public String getArtifactType() {
        return (String) getToscaPresentationValue(JsonPresentationFields.ARTIFACT_TYPE);
    }

    public boolean isHeatEnvType() {
        return ArtifactTypeEnum.HEAT_ENV.getType().equals(getArtifactType());
    }

    public void setArtifactType(String artifactType) {
        setToscaPresentationValue(JsonPresentationFields.ARTIFACT_TYPE, artifactType);

    }

    public String getArtifactRef() {
        return (String) getToscaPresentationValue(JsonPresentationFields.ARTIFACT_REF);

    }

    public void setArtifactRef(String artifactRef) {
        setToscaPresentationValue(JsonPresentationFields.ARTIFACT_REF, artifactRef);
    }

    public String getArtifactRepository() {
        return (String) getToscaPresentationValue(JsonPresentationFields.ARTIFACT_REPOSITORY);

    }

    public void setArtifactRepository(String artifactRepository) {
        setToscaPresentationValue(JsonPresentationFields.ARTIFACT_REPOSITORY, artifactRepository);
    }

    public void setArtifactName(String artifactName) {
        setToscaPresentationValue(JsonPresentationFields.NAME, artifactName);

    }

    public String getArtifactChecksum() {
        return (String) getToscaPresentationValue(JsonPresentationFields.ARTIFACT_CHECKSUM);
    }

    public void setArtifactChecksum(String artifactChecksum) {
        setToscaPresentationValue(JsonPresentationFields.ARTIFACT_CHECKSUM, artifactChecksum);
    }

    public String getUserIdCreator() {

        return (String) getToscaPresentationValue(JsonPresentationFields.USER_ID_CREATOR);
    }

    public void setUserIdCreator(String userIdCreator) {
        setToscaPresentationValue(JsonPresentationFields.USER_ID_CREATOR, userIdCreator);
    }

    public String getUserIdLastUpdater() {
        return (String) getToscaPresentationValue(JsonPresentationFields.USER_ID_LAST_UPDATER);
    }

    public void setUserIdLastUpdater(String userIdLastUpdater) {
        setToscaPresentationValue(JsonPresentationFields.USER_ID_LAST_UPDATER, userIdLastUpdater);
    }

    public String getCreatorFullName() {
        return (String) getToscaPresentationValue(JsonPresentationFields.CREATOR_FULL_NAME);
    }

    public void setCreatorFullName(String creatorFullName) {
        setToscaPresentationValue(JsonPresentationFields.CREATOR_FULL_NAME, creatorFullName);
    }

    public String getUpdaterFullName() {
        return (String) getToscaPresentationValue(JsonPresentationFields.UPDATER_FULL_NAME);
    }

    public void setUpdaterFullName(String updaterFullName) {
        setToscaPresentationValue(JsonPresentationFields.UPDATER_FULL_NAME, updaterFullName);
    }

    public Long getCreationDate() {
        return (Long) getToscaPresentationValue(JsonPresentationFields.CREATION_DATE);
    }

    public void setCreationDate(Long creationDate) {
        setToscaPresentationValue(JsonPresentationFields.CREATION_DATE, creationDate);
    }

    public Long getLastUpdateDate() {
        return (Long) getToscaPresentationValue(JsonPresentationFields.LAST_UPDATE_DATE);
    }

    public void setLastUpdateDate(Long lastUpdateDate) {
        setToscaPresentationValue(JsonPresentationFields.LAST_UPDATE_DATE, lastUpdateDate);
    }

    public String getUniqueId() {
        return (String) getToscaPresentationValue(JsonPresentationFields.UNIQUE_ID);
    }

    public void setUniqueId(String uniqueId) {
        setToscaPresentationValue(JsonPresentationFields.UNIQUE_ID, uniqueId);
    }

    public String getDescription() {
        return (String) getToscaPresentationValue(JsonPresentationFields.DESCRIPTION);
    }

    public void setDescription(String description) {
        setToscaPresentationValue(JsonPresentationFields.DESCRIPTION, description);
    }

    public String getArtifactLabel() {
        return (String) getToscaPresentationValue(JsonPresentationFields.ARTIFACT_LABEL);
    }

    public void setArtifactLabel(String artifactLabel) {
        setToscaPresentationValue(JsonPresentationFields.ARTIFACT_LABEL, artifactLabel);
    }

    public String getEsId() {
        return (String) getToscaPresentationValue(JsonPresentationFields.ES_ID);
    }

    public boolean hasNoMandatoryEsId() {
        return getEsId() == null && getMandatory();
    }

    public void setEsId(String esId) {
        setToscaPresentationValue(JsonPresentationFields.ES_ID, esId);
    }

    public String getArtifactCreator() {
        return (String) getToscaPresentationValue(JsonPresentationFields.ARTIFACT_CREATOR);
    }

    public void setArtifactCreator(String artifactCreator) {
        setToscaPresentationValue(JsonPresentationFields.ARTIFACT_CREATOR, artifactCreator);
    }

    public Boolean getMandatory() {
        Boolean isMandatory = (Boolean) getToscaPresentationValue(JsonPresentationFields.IS_ABSTRACT);
        return isMandatory == null ? Boolean.FALSE : isMandatory;
    }

    public void setMandatory(Boolean mandatory) {
        setToscaPresentationValue(JsonPresentationFields.IS_ABSTRACT, mandatory);
    }

    public String getArtifactDisplayName() {
        return (String) getToscaPresentationValue(JsonPresentationFields.ARTIFACT_DISPLAY_NAME);
    }

    public void setArtifactDisplayName(String artifactDisplayName) {
        setToscaPresentationValue(JsonPresentationFields.ARTIFACT_DISPLAY_NAME, artifactDisplayName);
    }

    public String getApiUrl() {
        return (String) getToscaPresentationValue(JsonPresentationFields.API_URL);
    }

    public void setApiUrl(String apiUrl) {
        setToscaPresentationValue(JsonPresentationFields.API_URL, apiUrl);
    }

    public Boolean getServiceApi() {
        Boolean serviceApi = (Boolean) getToscaPresentationValue(JsonPresentationFields.SERVICE_API);
        return serviceApi == null ? Boolean.FALSE : serviceApi;
    }

    public void setServiceApi(Boolean serviceApi) {
        setToscaPresentationValue(JsonPresentationFields.SERVICE_API, serviceApi);
    }

    public ArtifactGroupTypeEnum getArtifactGroupType() {
        ArtifactGroupTypeEnum artifactGroupType = null;
        String groupType = (String) getToscaPresentationValue(JsonPresentationFields.ARTIFACT_GROUP_TYPE);
        if (groupType != null && !groupType.isEmpty()) {
            artifactGroupType = ArtifactGroupTypeEnum.findType(groupType);
        }
        return artifactGroupType;
    }

    public void setArtifactGroupType(ArtifactGroupTypeEnum artifactGroupType) {
        if (artifactGroupType != null) {
            setToscaPresentationValue(JsonPresentationFields.ARTIFACT_GROUP_TYPE, artifactGroupType.getType());
        }
    }

    public Integer getTimeout() {
        return (Integer) getToscaPresentationValue(JsonPresentationFields.ARTIFACT_TIMEOUT);
    }

    public void setTimeout(Integer timeout) {
        setToscaPresentationValue(JsonPresentationFields.ARTIFACT_TIMEOUT, timeout);
    }

    public String getArtifactVersion() {
        return (String) getToscaPresentationValue(JsonPresentationFields.ARTIFACT_VERSION);
    }

    public void setArtifactVersion(String artifactVersion) {
        setToscaPresentationValue(JsonPresentationFields.ARTIFACT_VERSION, artifactVersion);
    }

    public String getArtifactUUID() {
        return (String) getToscaPresentationValue(JsonPresentationFields.ARTIFACT_UUID);
    }

    public void setArtifactUUID(String artifactUUID) {
        setToscaPresentationValue(JsonPresentationFields.ARTIFACT_UUID, artifactUUID);
    }

    public Long getPayloadUpdateDate() {
        return (Long) getToscaPresentationValue(JsonPresentationFields.PAYLOAD_UPDATE_DATE);
    }

    public void setPayloadUpdateDate(Long payloadUpdateDate) {
        setToscaPresentationValue(JsonPresentationFields.PAYLOAD_UPDATE_DATE, payloadUpdateDate);
    }

    public Long getHeatParamsUpdateDate() {
        return (Long) getToscaPresentationValue(JsonPresentationFields.HEAT_PARAMS_UPDATE_DATE);
    }

    public void setHeatParamsUpdateDate(Long heatParamsUpdateDate) {
        setToscaPresentationValue(JsonPresentationFields.HEAT_PARAMS_UPDATE_DATE, heatParamsUpdateDate);
    }

    public List<String> getRequiredArtifacts() {
        return (List<String>) getToscaPresentationValue(JsonPresentationFields.REQUIRED_ARTIFACTS);
    }

    public void setRequiredArtifacts(List<String> requiredArtifacts) {
        setToscaPresentationValue(JsonPresentationFields.REQUIRED_ARTIFACTS, requiredArtifacts);
    }

    public Boolean getGenerated() {
        Boolean generated = (Boolean) getToscaPresentationValue(JsonPresentationFields.GENERATED);
        return generated == null ? Boolean.FALSE : generated;
    }

    public void setGenerated(Boolean generated) {
        setToscaPresentationValue(JsonPresentationFields.GENERATED, generated);
    }

    public Boolean getDuplicated() {
        Boolean duplicated = (Boolean) getToscaPresentationValue(JsonPresentationFields.DUPLICATED);
        return duplicated == null ? Boolean.FALSE : duplicated;
    }

    public void setDuplicated(Boolean duplicated) {
        setToscaPresentationValue(JsonPresentationFields.DUPLICATED, duplicated);
    }

    public List<HeatParameterDataDefinition> getHeatParameters() {
        return (List<HeatParameterDataDefinition>) getToscaPresentationValue(JsonPresentationFields.HEAT_PARAMETERS);
    }

    public void setHeatParameters(List<HeatParameterDataDefinition> properties) {
        setToscaPresentationValue(JsonPresentationFields.HEAT_PARAMETERS, properties);
    }

    public String getGeneratedFromId() {
        return (String) getToscaPresentationValue(JsonPresentationFields.GENERATED_FROM_ID);
    }

    public boolean getIsFromCsar() {
        Boolean isFromCsar = (Boolean) getToscaPresentationValue(JsonPresentationFields.IS_FROM_CSAR);
        return isFromCsar == null ? Boolean.FALSE : isFromCsar;
    }

    public void setIsFromCsar(Boolean isFromCsar) {
        setToscaPresentationValue(JsonPresentationFields.IS_FROM_CSAR, isFromCsar);
    }

    public void setGeneratedFromId(String generatedFromId) {
        setToscaPresentationValue(JsonPresentationFields.GENERATED_FROM_ID, generatedFromId);
    }


    public List<PropertyDataDefinition> getProperties() {
        return (List<PropertyDataDefinition>) getToscaPresentationValue(JsonPresentationFields.PROPERTIES);
    }
    
    public void addProperty(final PropertyDataDefinition property) {
        List<PropertyDataDefinition> properties =  (List<PropertyDataDefinition>) getToscaPresentationValue(JsonPresentationFields.PROPERTIES);
        if (properties == null) {
            properties = new ArrayList<>();
            setProperties(properties);
        }
        properties.add(property);
    }

    private void setProperties(final List<PropertyDataDefinition> properties) {
        setToscaPresentationValue(JsonPresentationFields.PROPERTIES, properties);
    }

    @Override
    public String toString() {
        return "ArtifactDataDefinition [uniqueId=" + getUniqueId() + ", artifactType=" + getArtifactType() + ", artifactRef=" + getArtifactRef() + ", artifactName=" + getArtifactName() + ", artifactRepository=" + getArtifactRepository() + ", artifactChecksum="
                + getArtifactChecksum() + ", userIdCreator=" + getUserIdCreator() + ", userIdLastUpdater=" + getUserIdLastUpdater() + ", creatorFullName=" + getCreatorFullName() + ", updaterFullName=" + getUpdaterFullName() + ", creationDate=" + getCreationDate()
                + ", lastUpdateDate=" + getLastUpdateDate() + ", esId=" + getEsId() + ", artifactLabel=" + getArtifactLabel() + ", artifactCreator=" + getArtifactCreator() + ", description=" + getDescription() + ", mandatory=" + getMandatory() + ", artifactDisplayName="
                + getArtifactDisplayName() + ", apiUrl=" + getApiUrl() + ", serviceApi=" + getServiceApi() + ", artifactGroupType=" + getArtifactGroupType() + ", timeout=" + getTimeout() + ", artifactVersion=" + getArtifactVersion() + ", artifactUUID=" + getArtifactUUID()
                + ", payloadUpdateDate=" + getPayloadUpdateDate() + ", heatParamsUpdateDate=" + getHeatParamsUpdateDate() + ", requiredArtifacts=" + getRequiredArtifacts() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        String apiUrl = getApiUrl();
        String artifactChecksum = getArtifactChecksum();
        String artifactCreator = getArtifactCreator();
        String artifactDisplayName = getArtifactDisplayName();
        ArtifactGroupTypeEnum artifactGroupType = getArtifactGroupType();
        String artifactLabel = getArtifactLabel();
        String artifactName = getArtifactName();
        String artifactRef = getArtifactRef();
        String artifactRepository = getArtifactRepository();
        String artifactType = getArtifactType();

        String artifactUUID = getArtifactUUID();
        String artifactVersion = getArtifactVersion();
        String userIdCreator = getUserIdCreator();
        String userIdLastUpdater = getUserIdLastUpdater();
        Long creationDate = getCreationDate();

        String creatorFullName = getCreatorFullName();
        String description = getDescription();
        String esId = getEsId();
        Long heatParamsUpdateDate = getHeatParamsUpdateDate();
        Long lastUpdateDate = getLastUpdateDate();
        Boolean mandatory = getMandatory();

        Long payloadUpdateDate = getPayloadUpdateDate();
        List<String> requiredArtifacts = getRequiredArtifacts();
        Boolean serviceApi = getServiceApi();
        Integer timeout = getTimeout();
        String uniqueId = getUniqueId();
        String updaterFullName = getUpdaterFullName();

        result = prime * result + ((apiUrl == null) ? 0 : apiUrl.hashCode());
        result = prime * result + ((artifactChecksum == null) ? 0 : artifactChecksum.hashCode());
        result = prime * result + ((artifactCreator == null) ? 0 : artifactCreator.hashCode());
        result = prime * result + ((artifactDisplayName == null) ? 0 : artifactDisplayName.hashCode());
        result = prime * result + ((artifactGroupType == null) ? 0 : artifactGroupType.hashCode());
        result = prime * result + ((artifactLabel == null) ? 0 : artifactLabel.hashCode());
        result = prime * result + ((artifactName == null) ? 0 : artifactName.hashCode());
        result = prime * result + ((artifactRef == null) ? 0 : artifactRef.hashCode());
        result = prime * result + ((artifactRepository == null) ? 0 : artifactRepository.hashCode());
        result = prime * result + ((artifactType == null) ? 0 : artifactType.hashCode());
        result = prime * result + ((artifactUUID == null) ? 0 : artifactUUID.hashCode());
        result = prime * result + ((artifactVersion == null) ? 0 : artifactVersion.hashCode());
        result = prime * result + ((userIdCreator == null) ? 0 : userIdCreator.hashCode());
        result = prime * result + ((userIdLastUpdater == null) ? 0 : userIdLastUpdater.hashCode());
        result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
        result = prime * result + ((creatorFullName == null) ? 0 : creatorFullName.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((esId == null) ? 0 : esId.hashCode());
        result = prime * result + ((heatParamsUpdateDate == null) ? 0 : heatParamsUpdateDate.hashCode());
        result = prime * result + ((lastUpdateDate == null) ? 0 : lastUpdateDate.hashCode());
        result = prime * result + ((mandatory == null) ? 0 : mandatory.hashCode());
        result = prime * result + ((payloadUpdateDate == null) ? 0 : payloadUpdateDate.hashCode());
        result = prime * result + ((requiredArtifacts == null) ? 0 : requiredArtifacts.hashCode());
        result = prime * result + ((serviceApi == null) ? 0 : serviceApi.hashCode());
        result = prime * result + ((timeout == null) ? 0 : timeout.hashCode());
        result = prime * result + ((uniqueId == null) ? 0 : uniqueId.hashCode());
        result = prime * result + ((updaterFullName == null) ? 0 : updaterFullName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ArtifactDataDefinition other = (ArtifactDataDefinition) obj;
        if (getApiUrl() == null) {
            if (other.getApiUrl() != null) {
                return false;
            }
        } else if (!getArtifactRepository().equals(other.getArtifactRepository())) {
            return false;
        }
        if (getArtifactName() == null) {
            if (other.getArtifactName() != null) {
                return false;
            }
        } else if (!getArtifactName().equals(other.getArtifactName())) {
            return false;
        }
        if (getArtifactType() == null) {
            if (other.getArtifactType() != null) {
                return false;
            }
        } else if (!getArtifactType().equals(other.getArtifactType())) {
            return false;
        }
        if (getArtifactUUID() == null) {
            if (other.getArtifactUUID() != null) {
                return false;
            }
        } else if (!getArtifactUUID().equals(other.getArtifactUUID())) {
            return false;
        }
        if (getArtifactVersion() == null) {
            if (other.getArtifactVersion() != null) {
                return false;
            }
        } else if (!getArtifactVersion().equals(other.getArtifactVersion())) {
            return false;
        }
        if (getUserIdCreator() == null) {
            if (other.getUserIdCreator() != null) {
                return false;
            }
        } else if (!getUserIdCreator().equals(other.getUserIdCreator())) {
            return false;
        }
        if (getUserIdLastUpdater() == null) {
            if (other.getUserIdLastUpdater() != null) {
                return false;
            }
        } else if (!getUserIdLastUpdater().equals(other.getUserIdLastUpdater())) {
            return false;
        }
        if (getCreationDate() == null) {
            if (other.getCreationDate() != null) {
                return false;
            }
        } else if (!getCreationDate().equals(other.getCreationDate())) {
            return false;
        }
        if (getCreatorFullName() == null) {
            if (other.getCreatorFullName() != null) {
                return false;
            }
        } else if (!getCreatorFullName().equals(other.getCreatorFullName())) {
            return false;
        }
        if (getDescription() == null) {
            if (other.getDescription() != null) {
                return false;
            }
        } else if (!getDescription().equals(other.getDescription())) {
            return false;
        }
        if (getEsId() == null) {
            if (other.getEsId() != null) {
                return false;
            }
        } else if (!getEsId().equals(other.getEsId())) {
            return false;
        }
        if (getHeatParamsUpdateDate() == null) {
            if (other.getHeatParamsUpdateDate() != null) {
                return false;
            }
        } else if (!getHeatParamsUpdateDate().equals(other.getHeatParamsUpdateDate())) {
            return false;
        }
        if (getLastUpdateDate() == null) {
            if (other.getLastUpdateDate() != null) {
                return false;
            }
        } else if (!getLastUpdateDate().equals(other.getLastUpdateDate())) {
            return false;
        }
        if (getMandatory() == null) {
            if (other.getMandatory() != null) {
                return false;
            }
        } else if (!getMandatory().equals(other.getMandatory())) {
            return false;
        }
        if (getPayloadUpdateDate() == null) {
            if (other.getPayloadUpdateDate() != null) {
                return false;
            }
        } else if (!getPayloadUpdateDate().equals(other.getPayloadUpdateDate())) {
            return false;
        }
        if (getRequiredArtifacts() == null) {
            if (other.getRequiredArtifacts() != null) {
                return false;
            }
        } else if (!getRequiredArtifacts().equals(other.getRequiredArtifacts())) {
            return false;
        }
        if (getServiceApi() == null) {
            if (other.getServiceApi() != null) {
                return false;
            }
        } else if (!getServiceApi().equals(other.getServiceApi())) {
            return false;
        }
        if (getTimeout() == null) {
            if (other.getTimeout() != null) {
                return false;
            }
        } else if (!getTimeout().equals(other.getTimeout())) {
            return false;
        }
        if (getUniqueId() == null) {
            if (other.getUniqueId() != null) {
                return false;
            }
        } else if (!getUniqueId().equals(other.getUniqueId())) {
            return false;
        }
        if (getUpdaterFullName() == null) {
            if (other.getUpdaterFullName() != null) {
                return false;
            }
        } else if (!getUpdaterFullName().equals(other.getUpdaterFullName())) {
            return false;
        }
        return true;
    }
}
