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

package org.openecomp.sdc.be.ui.model;

import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.AdditionalInformationDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.category.CategoryDefinition;

public class UiComponentDataTransfer {




	private Map<String, ArtifactDefinition> artifacts;
	private Map<String, ArtifactDefinition> deploymentArtifacts;
	private Map<String, ArtifactDefinition> toscaArtifacts;

	private List<CategoryDefinition> categories;

	// User
	private String creatorUserId;
	private String creatorFullName;
	private String lastUpdaterUserId;
	private String lastUpdaterFullName;

	protected ComponentTypeEnum componentType;

	private List<ComponentInstance> componentInstances;

	private List<RequirementCapabilityRelDef> componentInstancesRelations;

	private Map<String, List<ComponentInstanceInput>> componentInstancesInputs;

	private Map<String, List<ComponentInstanceProperty>> componentInstancesProperties;

	private Map<String, List<ComponentInstanceProperty>> componentInstancesAttributes;

	private Map<String, List<CapabilityDefinition>> capabilities;

	private Map<String, List<RequirementDefinition>> requirements;

	private List<InputDefinition> inputs;

	private List<GroupDefinition> groups;
	
	protected List<AdditionalInformationDefinition> additionalInformation;

	public UiComponentDataTransfer(){}

	public Map<String, ArtifactDefinition> getArtifacts() {
		return artifacts;
	}


	public void setArtifacts(Map<String, ArtifactDefinition> artifacts) {
		this.artifacts = artifacts;
	}


	public Map<String, ArtifactDefinition> getDeploymentArtifacts() {
		return deploymentArtifacts;
	}


	public void setDeploymentArtifacts(Map<String, ArtifactDefinition> deploymentArtifacts) {
		this.deploymentArtifacts = deploymentArtifacts;
	}


	public Map<String, ArtifactDefinition> getToscaArtifacts() {
		return toscaArtifacts;
	}


	public void setToscaArtifacts(Map<String, ArtifactDefinition> toscaArtifacts) {
		this.toscaArtifacts = toscaArtifacts;
	}


	public List<CategoryDefinition> getCategories() {
		return categories;
	}


	public void setCategories(List<CategoryDefinition> categories) {
		this.categories = categories;
	}


	public String getCreatorUserId() {
		return creatorUserId;
	}


	public void setCreatorUserId(String creatorUserId) {
		this.creatorUserId = creatorUserId;
	}


	public String getCreatorFullName() {
		return creatorFullName;
	}


	public void setCreatorFullName(String creatorFullName) {
		this.creatorFullName = creatorFullName;
	}


	public String getLastUpdaterUserId() {
		return lastUpdaterUserId;
	}


	public void setLastUpdaterUserId(String lastUpdaterUserId) {
		this.lastUpdaterUserId = lastUpdaterUserId;
	}


	public String getLastUpdaterFullName() {
		return lastUpdaterFullName;
	}


	public void setLastUpdaterFullName(String lastUpdaterFullName) {
		this.lastUpdaterFullName = lastUpdaterFullName;
	}


	public ComponentTypeEnum getComponentType() {
		return componentType;
	}


	public void setComponentType(ComponentTypeEnum componentType) {
		this.componentType = componentType;
	}


	public List<ComponentInstance> getComponentInstances() {
		return componentInstances;
	}


	public void setComponentInstances(List<ComponentInstance> componentInstances) {
		this.componentInstances = componentInstances;
	}


	public List<RequirementCapabilityRelDef> getComponentInstancesRelations() {
		return componentInstancesRelations;
	}


	public void setComponentInstancesRelations(List<RequirementCapabilityRelDef> componentInstancesRelations) {
		this.componentInstancesRelations = componentInstancesRelations;
	}


	public Map<String, List<ComponentInstanceInput>> getComponentInstancesInputs() {
		return componentInstancesInputs;
	}


	public void setComponentInstancesInputs(Map<String, List<ComponentInstanceInput>> componentInstancesInputs) {
		this.componentInstancesInputs = componentInstancesInputs;
	}


	public Map<String, List<ComponentInstanceProperty>> getComponentInstancesProperties() {
		return componentInstancesProperties;
	}


	public void setComponentInstancesProperties(Map<String, List<ComponentInstanceProperty>> componentInstancesProperties) {
		this.componentInstancesProperties = componentInstancesProperties;
	}


	public Map<String, List<ComponentInstanceProperty>> getComponentInstancesAttributes() {
		return componentInstancesAttributes;
	}


	public void setComponentInstancesAttributes(
			Map<String, List<ComponentInstanceProperty>> componentInstancesAttributes) {
		this.componentInstancesAttributes = componentInstancesAttributes;
	}


	public Map<String, List<CapabilityDefinition>> getCapabilities() {
		return capabilities;
	}


	public void setCapabilities(Map<String, List<CapabilityDefinition>> capabilities) {
		this.capabilities = capabilities;
	}


	public Map<String, List<RequirementDefinition>> getRequirements() {
		return requirements;
	}


	public void setRequirements(Map<String, List<RequirementDefinition>> requirements) {
		this.requirements = requirements;
	}


	public List<InputDefinition> getInputs() {
		return inputs;
	}


	public void setInputs(List<InputDefinition> inputs) {
		this.inputs = inputs;
	}


	public List<GroupDefinition> getGroups() {
		return groups;
	}


	public void setGroups(List<GroupDefinition> groups) {
		this.groups = groups;
	}


	public List<AdditionalInformationDefinition> getAdditionalInformation() {
		return additionalInformation;
	}


	public void setAdditionalInformation(List<AdditionalInformationDefinition> additionalInformation) {
		this.additionalInformation = additionalInformation;
	}
	
}
