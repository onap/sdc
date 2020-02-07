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

package org.openecomp.sdc.be.model.jsonjanusgraph.datamodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapPropertiesDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.NodeTypeOperation;
import org.openecomp.sdc.common.log.api.ILogConfiguration;
import org.slf4j.MDC;

@Getter
@Setter
public abstract class ToscaElement {

    protected Map<String, Object> metadata;
    protected List<CategoryDefinition> categories;
    protected Map<String, ArtifactDataDefinition> toscaArtifacts;
    protected ToscaElementTypeEnum toscaType;
    private Map<String, ArtifactDataDefinition> artifacts;
    private Map<String, ArtifactDataDefinition> deploymentArtifacts;
    private Map<String, AdditionalInfoParameterDataDefinition> additionalInformation;
    private Map<String, PropertyDataDefinition> properties;
    private Map<String, ListCapabilityDataDefinition> capabilities;
    private Map<String, MapPropertiesDataDefinition> capabilitiesProperties;
    private Map<String, ListRequirementDataDefinition> requirements;
    // User
    private String creatorUserId;
    private String creatorFullName;
    private String lastUpdaterUserId;
    private String lastUpdaterFullName;

    private Map<String, String> allVersions;
    private String toscaVersion;

    public ToscaElement(ToscaElementTypeEnum toscaType){
        this.toscaType = toscaType;
    }

    // metadata properties
    // ----------------------------
    public Object getMetadataValue(JsonPresentationFields name) {
        return getMetadataValueOrDefault(name, null);
    }

    public Object getMetadataValueOrDefault(JsonPresentationFields name, Object defaultVal) {
        if (metadata != null) {
            return metadata.getOrDefault(name.getPresentation(), defaultVal);
        }
        return null;
    }

    public void setMetadataValue(JsonPresentationFields name, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(name.getPresentation(), value);

    }
    // --------------------
    public String getUUID() {
        return (String) getMetadataValue(JsonPresentationFields.UUID);
    }

    public void setUUID(String uuid) {
        setMetadataValue(JsonPresentationFields.UUID, uuid);
    }

    public String getVersion() {
        return (String) getMetadataValue(JsonPresentationFields.VERSION);
    }

    public String getNormalizedName() {
        return (String) getMetadataValue(JsonPresentationFields.NORMALIZED_NAME);
    }

    public void setNormalizedName(String normaliseComponentName) {
        setMetadataValue(JsonPresentationFields.NORMALIZED_NAME, normaliseComponentName);
    }

    public String getName() {
        return (String) getMetadataValue(JsonPresentationFields.NAME);
    }

    public String getSystemName() {
        return (String) getMetadataValue(JsonPresentationFields.SYSTEM_NAME);
    }
    public void setSystemName(String systemName) {
        setMetadataValue(JsonPresentationFields.SYSTEM_NAME, systemName);
    }

    public void setLifecycleState(LifecycleStateEnum state) {
        if(state != null)
            setMetadataValue(JsonPresentationFields.LIFECYCLE_STATE, state.name());
    }

    public LifecycleStateEnum getLifecycleState() {
        return LifecycleStateEnum.findState( (String) getMetadataValue(JsonPresentationFields.LIFECYCLE_STATE));
    }

    public Long getCreationDate() {
        return (Long) getMetadataValue(JsonPresentationFields.CREATION_DATE);
    }

    public void setCreationDate(Long currentDate) {
        setMetadataValue(JsonPresentationFields.CREATION_DATE, currentDate);
    }

    public void setLastUpdateDate(Long currentDate) {
        setMetadataValue(JsonPresentationFields.LAST_UPDATE_DATE, currentDate);
    }
    public Long getLastUpdateDate() {
        return (Long) getMetadataValue(JsonPresentationFields.LAST_UPDATE_DATE);
    }

    public String getUniqueId() {
        return (String) getMetadataValue(JsonPresentationFields.UNIQUE_ID);
    }
    public void setUniqueId(String uniqueId) {
         setMetadataValue(JsonPresentationFields.UNIQUE_ID, uniqueId);
    }

    public void setHighestVersion(Boolean isHighest) {
         setMetadataValue(JsonPresentationFields.HIGHEST_VERSION, isHighest);

    }
    public Boolean isHighestVersion() {
        return (Boolean) getMetadataValue(JsonPresentationFields.HIGHEST_VERSION);

    }
    public ResourceTypeEnum getResourceType() {
        String resourceType = (String) getMetadataValue(JsonPresentationFields.RESOURCE_TYPE);
        return resourceType != null ? ResourceTypeEnum.valueOf(resourceType) : null;
    }

    public void setResourceType(ResourceTypeEnum resourceType) {
        if(resourceType != null)
            setMetadataValue(JsonPresentationFields.RESOURCE_TYPE, resourceType.name());
    }

    public ComponentTypeEnum getComponentType() {
        return ComponentTypeEnum.valueOf((String) getMetadataValue(JsonPresentationFields.COMPONENT_TYPE));
    }

    public void setComponentType(ComponentTypeEnum componentType) {
        if(componentType != null)
            setMetadataValue(JsonPresentationFields.COMPONENT_TYPE, componentType.name());
    }

    public String getDerivedFromGenericType(){
        return (String) getMetadataValue(JsonPresentationFields.DERIVED_FROM_GENERIC_TYPE);
    }

    public void setDerivedFromGenericType(String derivedFromGenericType){
        setMetadataValue(JsonPresentationFields.DERIVED_FROM_GENERIC_TYPE, derivedFromGenericType);
    }

    public String getDerivedFromGenericVersion(){
        return (String) getMetadataValue(JsonPresentationFields.DERIVED_FROM_GENERIC_VERSION);
    }

    public void setDerivedFromGenericVersion(String derivedFromGenericVersion){
        setMetadataValue(JsonPresentationFields.DERIVED_FROM_GENERIC_VERSION, derivedFromGenericVersion);
    }

    public Boolean isArchived() { return (Boolean) getMetadataValue(JsonPresentationFields.IS_ARCHIVED); }

    public void setArchived(Boolean archived) { setMetadataValue(JsonPresentationFields.IS_ARCHIVED, archived); }

    public Long getArchiveTime() {
        Object archiveTime = getMetadataValue(JsonPresentationFields.ARCHIVE_TIME);
        if (archiveTime instanceof Integer){
            return new Long((Integer)getMetadataValue(JsonPresentationFields.ARCHIVE_TIME));
        }
        return (Long)archiveTime;
    }

    public void setArchiveTime(Long archiveTime) {
        setMetadataValue(JsonPresentationFields.ARCHIVE_TIME, archiveTime);
    }

    public Boolean isVspArchived() {
        return (Boolean) getMetadataValue(JsonPresentationFields.IS_VSP_ARCHIVED);
    }

    public void setVspArchived(Boolean vspArchived) {
        setMetadataValue(JsonPresentationFields.IS_VSP_ARCHIVED, vspArchived);
    }

    public void generateUUID() {
        String prevUUID = getUUID();
        String version = getVersion();
        if ((prevUUID == null && NodeTypeOperation.uuidNormativeNewVersion.matcher(version).matches()) || NodeTypeOperation.uuidNewVersion.matcher(version).matches()) {
            UUID uuid = UUID.randomUUID();
            setUUID(uuid.toString());
            MDC.put(ILogConfiguration.MDC_SERVICE_INSTANCE_ID, uuid.toString());
        }
    }

}
