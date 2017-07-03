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

import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.common.util.ValidationUtils;

public class ComponentInstanceDataDefinition extends ToscaDataDefinition implements Serializable {

	/**
	 * 
	 */
	
	private static final long serialVersionUID = 7215033872921497743L;

	private String icon;
	
	private String uniqueId;

	private String name;
	
	private String normalizedName;

	private String componentUid;

	private Long creationTime;

	private Long modificationTime;

	private String description;

	private String posX;

	private String posY;
	private Integer propertyValueCounter = 1;
	private Integer attributeValueCounter;
	private Integer inputValueCounter = 1;
	private OriginTypeEnum originType;
	private String customizationUUID;
	private String componentName;
	private String componentVersion;
	private String toscaComponentName;
	private String invariantName;
	
	public ComponentInstanceDataDefinition() {
		super();
	}

	public ComponentInstanceDataDefinition(ComponentInstanceDataDefinition dataDefinition) {
		this.icon = dataDefinition.icon;
		this.uniqueId = dataDefinition.uniqueId;
		this.name = dataDefinition.name;
		this.componentUid = dataDefinition.componentUid;
		this.creationTime = dataDefinition.creationTime;
		this.modificationTime = dataDefinition.modificationTime;
		this.description = dataDefinition.description;
		this.posX = dataDefinition.posX;
		this.posY = dataDefinition.posY;
		this.propertyValueCounter = dataDefinition.propertyValueCounter;
		this.normalizedName = dataDefinition.normalizedName;
		this.originType = dataDefinition.originType;
		this.customizationUUID = dataDefinition.customizationUUID;
		this.componentName = dataDefinition.componentName;
		this.componentVersion = dataDefinition.componentVersion;
		this.toscaComponentName = dataDefinition.toscaComponentName;
		this.invariantName = dataDefinition.invariantName;
	}
	
	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public Long getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Long creationTime) {
		this.creationTime = creationTime;
	}

	public Long getModificationTime() {
		return modificationTime;
	}

	public void setModificationTime(Long modificationTime) {
		this.modificationTime = modificationTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPosX() {
		return posX;
	}

	public void setPosX(String posX) {
		this.posX = posX;
	}

	public String getPosY() {
		return posY;
	}

	public void setPosY(String posY) {
		this.posY = posY;
	}

	public String getComponentUid() {
		return componentUid;
	}

	public void setComponentUid(String resourceUid) {
		this.componentUid = resourceUid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if(invariantName == null){
			invariantName = ValidationUtils.normalizeComponentInstanceName(name);
		}
		this.name = name;
	}
	
	public String getInvariantName() {
		return invariantName;
	}

	public void setInvariantName(String invariantName) {
		this.invariantName = invariantName;
	}

	public Integer getPropertyValueCounter() {
		return propertyValueCounter;
	}

	public void setPropertyValueCounter(Integer propertyValueCounter) {
		this.propertyValueCounter = propertyValueCounter;
	}

	public String getNormalizedName() {
		return normalizedName;
	}

	public void setNormalizedName(String normalizedName) {
		this.normalizedName = normalizedName;
	}

	public OriginTypeEnum getOriginType() {
		return originType;
	}

	public void setOriginType(OriginTypeEnum originType) {
		this.originType = originType;
	}

	public Integer getAttributeValueCounter() {
		return attributeValueCounter;
	}

	public void setAttributeValueCounter(Integer attributeValueCounter) {
		this.attributeValueCounter = attributeValueCounter;
	}

	public Integer getInputValueCounter() {
		return inputValueCounter;
	}

	public void setInputValueCounter(Integer inputValueCounter) {
		this.inputValueCounter = inputValueCounter;
	}

	public String getCustomizationUUID() {
		return customizationUUID;
	}

	public void setCustomizationUUID(String customizationUUID) {
		this.customizationUUID = customizationUUID;
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String resourceName) {
		this.componentName = resourceName;
	}

	public String getComponentVersion() {
		return componentVersion;
	}

	public String getToscaComponentName() {
		return toscaComponentName;
	}

	public void setToscaComponentName(String toscaComponentName) {
		this.toscaComponentName = toscaComponentName;
	}

	public void setComponentVersion(String resourceVersion) {
		this.componentVersion = resourceVersion;
	}

	@Override
	public String toString() {
		return "ComponentInstanceDataDefinition [icon=" + icon + ", uniqueId=" + uniqueId + ", name=" + name + ", normalizedName=" + normalizedName + ", componentUid=" + componentUid + ", creationTime=" + creationTime + ", modificationTime="
				+ modificationTime + ", description=" + description + ", posX=" + posX + ", posY=" + posY + ", propertyValueCounter=" + propertyValueCounter + ", attributeValueCounter=" + attributeValueCounter + ", inputValueCounter="
				+ inputValueCounter + ", originType=" + originType + ", customizationUUID=" + customizationUUID + ", componentName=" + componentName + ", componentVersion=" + componentVersion + ", toscaComponentName=" + toscaComponentName + "]";
	}

}
