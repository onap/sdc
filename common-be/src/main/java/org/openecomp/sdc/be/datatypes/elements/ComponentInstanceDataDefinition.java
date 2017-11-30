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

import java.io.Serializable;

import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.common.util.ValidationUtils;

public class ComponentInstanceDataDefinition extends ToscaDataDefinition implements Serializable {

	/**
	 * 
	 */

	private static final long serialVersionUID = 7215033872921497743L;

	public ComponentInstanceDataDefinition() {
		super();
		setPropertyValueCounter(1);
		setAttributeValueCounter(1);
		setInputValueCounter(1);
		setIsProxy(false);
	}

	public ComponentInstanceDataDefinition(ComponentInstanceDataDefinition dataDefinition) {
		setIcon(dataDefinition.getIcon());
		setUniqueId(dataDefinition.getUniqueId());
		setName(dataDefinition.getName());
		setComponentUid(dataDefinition.getComponentUid());
		setCreationTime(dataDefinition.getCreationTime());
		setModificationTime(dataDefinition.getModificationTime());
		setDescription(dataDefinition.getDescription());
		setPosX(dataDefinition.getPosX());
		setPosY(dataDefinition.getPosY());
		setPropertyValueCounter(dataDefinition.getPropertyValueCounter());
		setNormalizedName(dataDefinition.getNormalizedName());
		setOriginType(dataDefinition.getOriginType());
		setCustomizationUUID(dataDefinition.getCustomizationUUID());
		setComponentName(dataDefinition.getComponentName());
		setComponentVersion(dataDefinition.getComponentVersion());
		setToscaComponentName(dataDefinition.getToscaComponentName());
		setInvariantName(dataDefinition.getInvariantName());
		setSourceModelInvariant(dataDefinition.getSourceModelInvariant());
		setSourceModelName(dataDefinition.getSourceModelName());
		setSourceModelUuid(dataDefinition.getSourceModelUuid());
		setSourceModelUid(dataDefinition.getSourceModelUid());
		setIsProxy(dataDefinition.getIsProxy());
	}

	public String getIcon() {
		return (String) getToscaPresentationValue(JsonPresentationFields.CI_ICON);
	}

	public void setIcon(String icon) {
		setToscaPresentationValue(JsonPresentationFields.CI_ICON, icon);
	}

	public String getUniqueId() {
		return (String) getToscaPresentationValue(JsonPresentationFields.UNIQUE_ID);
	}

	public void setUniqueId(String uniqueId) {
		setToscaPresentationValue(JsonPresentationFields.UNIQUE_ID, uniqueId);
	}

	public Long getCreationTime() {
		return (Long) getToscaPresentationValue(JsonPresentationFields.CREATION_TIME);
	}

	public void setCreationTime(Long creationTime) {
		setToscaPresentationValue(JsonPresentationFields.CREATION_TIME, creationTime);
	}

	public Long getModificationTime() {
		return (Long) getToscaPresentationValue(JsonPresentationFields.MODIFICATION_TIME);
	}

	public void setModificationTime(Long modificationTime) {
		setToscaPresentationValue(JsonPresentationFields.MODIFICATION_TIME, modificationTime);
	}

	public String getDescription() {
		return (String) getToscaPresentationValue(JsonPresentationFields.DESCRIPTION);
	}

	public void setDescription(String description) {
		setToscaPresentationValue(JsonPresentationFields.DESCRIPTION, description);
	}

	public String getPosX() {
		return (String) getToscaPresentationValue(JsonPresentationFields.CI_POS_X);
	}

	public void setPosX(String posX) {
		setToscaPresentationValue(JsonPresentationFields.CI_POS_X, posX);
	}

	public String getPosY() {
		return (String) getToscaPresentationValue(JsonPresentationFields.CI_POS_Y);
	}

	public void setPosY(String posY) {
		setToscaPresentationValue(JsonPresentationFields.CI_POS_Y, posY);
	}

	public String getComponentUid() {
		return (String) getToscaPresentationValue(JsonPresentationFields.CI_COMPONENT_UID);
	}

	public void setComponentUid(String resourceUid) {
		setToscaPresentationValue(JsonPresentationFields.CI_COMPONENT_UID, resourceUid);
	}

	public String getName() {
		return (String) getToscaPresentationValue(JsonPresentationFields.NAME);
	}

	public void setName(String name) {
		if (this.getInvariantName() == null) {
			this.setInvariantName(ValidationUtils.normalizeComponentInstanceName(name));
		}
		setToscaPresentationValue(JsonPresentationFields.NAME, name);
	}

	public String getInvariantName() {
		return (String) getToscaPresentationValue(JsonPresentationFields.CI_INVARIANT_NAME);
	}

	public void setInvariantName(String invariantName) {
		setToscaPresentationValue(JsonPresentationFields.CI_INVARIANT_NAME, invariantName);
	}

	public Integer getPropertyValueCounter() {
		return (Integer) getToscaPresentationValue(JsonPresentationFields.CI_PROP_VALUE_COUNTER);
	}

	public void setPropertyValueCounter(Integer propertyValueCounter) {
		setToscaPresentationValue(JsonPresentationFields.CI_PROP_VALUE_COUNTER, propertyValueCounter);
	}

	public String getNormalizedName() {
		return (String) getToscaPresentationValue(JsonPresentationFields.NORMALIZED_NAME);
	}

	public void setNormalizedName(String normalizedName) {
		setToscaPresentationValue(JsonPresentationFields.NORMALIZED_NAME, normalizedName);
	}

	public OriginTypeEnum getOriginType() {
		OriginTypeEnum originType = null;
		String origType = (String) getToscaPresentationValue(JsonPresentationFields.CI_ORIGIN_TYPE);
		if (origType != null && !origType.isEmpty()) {

			originType = OriginTypeEnum.findByValue(origType);
		}
		return originType;
	}

	public void setOriginType(OriginTypeEnum originType) {
		if (originType != null)
			setToscaPresentationValue(JsonPresentationFields.CI_ORIGIN_TYPE, originType.getValue());
	}

	public Integer getAttributeValueCounter() {
		return (Integer) getToscaPresentationValue(JsonPresentationFields.CI_ATTR_VALUE_COUNTER);
	}

	public void setAttributeValueCounter(Integer attributeValueCounter) {
		setToscaPresentationValue(JsonPresentationFields.CI_ATTR_VALUE_COUNTER, attributeValueCounter);
	}

	public Integer getInputValueCounter() {
		return (Integer) getToscaPresentationValue(JsonPresentationFields.CI_INPUT_VALUE_COUNTER);
	}

	public void setInputValueCounter(Integer inputValueCounter) {
		setToscaPresentationValue(JsonPresentationFields.CI_INPUT_VALUE_COUNTER, inputValueCounter);
	}

	public String getCustomizationUUID() {
		return (String) getToscaPresentationValue(JsonPresentationFields.CUSTOMIZATION_UUID);
	}

	public void setCustomizationUUID(String customizationUUID) {
		setToscaPresentationValue(JsonPresentationFields.CUSTOMIZATION_UUID, customizationUUID);
	}

	public String getComponentName() {
		return (String) getToscaPresentationValue(JsonPresentationFields.CI_COMPONENT_NAME);
	}

	public void setComponentName(String resourceName) {
		setToscaPresentationValue(JsonPresentationFields.CI_COMPONENT_NAME, resourceName);
	}

	public String getComponentVersion() {
		return (String) getToscaPresentationValue(JsonPresentationFields.CI_COMPONENT_VERSION);
	}

	public String getToscaComponentName() {
		return (String) getToscaPresentationValue(JsonPresentationFields.CI_TOSCA_COMPONENT_NAME);
	}

	public void setToscaComponentName(String toscaComponentName) {
		setToscaPresentationValue(JsonPresentationFields.CI_TOSCA_COMPONENT_NAME, toscaComponentName);
	}

	public void setComponentVersion(String resourceVersion) {
		setToscaPresentationValue(JsonPresentationFields.CI_COMPONENT_VERSION, resourceVersion);
	}

	public void setSourceModelUuid(String targetModelUuid) {
		setToscaPresentationValue(JsonPresentationFields.CI_SOURCE_MODEL_UUID, targetModelUuid);
	}
	public void setSourceModelUid(String targetModelUid) {
		setToscaPresentationValue(JsonPresentationFields.CI_SOURCE_MODEL_UID, targetModelUid);
	}

	public void setSourceModelName(String targetModelName) {
		setToscaPresentationValue(JsonPresentationFields.CI_SOURCE_MODEL_NAME, targetModelName);
	}

	public void setSourceModelInvariant(String targetModelInvariant) {
		setToscaPresentationValue(JsonPresentationFields.CI_SOURCE_MODEL_INVARIANT, targetModelInvariant);
	}

	public String getSourceModelUuid() {
		return (String) getToscaPresentationValue(JsonPresentationFields.CI_SOURCE_MODEL_UUID);
	}
	public String getSourceModelUid() {
		return (String) getToscaPresentationValue(JsonPresentationFields.CI_SOURCE_MODEL_UID);
	}

	public String getSourceModelName() {
		return (String) getToscaPresentationValue(JsonPresentationFields.CI_SOURCE_MODEL_NAME);
	}

	public String getSourceModelInvariant() {
		return (String) getToscaPresentationValue(JsonPresentationFields.CI_SOURCE_MODEL_INVARIANT);
	}

	public void setIsProxy(Boolean isProxy) {
		if (isProxy == null) {
			setToscaPresentationValue(JsonPresentationFields.CI_IS_PROXY, false);
		} else {
			setToscaPresentationValue(JsonPresentationFields.CI_IS_PROXY, isProxy);
		}
	}

	public Boolean getIsProxy() {
		Boolean isProxy = (Boolean) getToscaPresentationValue(JsonPresentationFields.CI_IS_PROXY);
		return ( isProxy != null ) ? isProxy : false;
	}

	@Override
	public String toString() {
		return "ComponentInstanceDataDefinition [icon=" + getIcon() + ", uniqueId=" + getUniqueId() + ", name="
				+ getName() + ", normalizedName=" + getNormalizedName() + ", componentUid=" + getComponentUid()
				+ ", creationTime=" + getCreationTime() + ", modificationTime=" + getModificationTime()
				+ ", description=" + getDescription() + ", posX=" + getPosX() + ", posY=" + getPosY()
				+ ", propertyValueCounter=" + getPropertyValueCounter() + ", attributeValueCounter="
				+ getAttributeValueCounter() + ", inputValueCounter=" + getInputValueCounter() + ", originType="
				+ getOriginType() + ", customizationUUID=" + getCustomizationUUID() + ", componentName="
				+ getComponentName() + ", componentVersion=" + getComponentVersion() + ", toscaComponentName="
				+ getToscaComponentName() + "]";
	}

}
