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

package org.openecomp.sdc.be.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;

public abstract class Component implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6373756459967949120L;

	private ComponentMetadataDefinition componentMetadataDefinition;
	private Map<String, ArtifactDefinition> artifacts;
	private Map<String, ArtifactDefinition> deploymentArtifacts;
	private Map<String, ArtifactDefinition> toscaArtifacts;

	private List<CategoryDefinition> categories;

	private List<ComponentInstance> componentInstances;

	private List<RequirementCapabilityRelDef> componentInstancesRelations;

	private Map<String, List<ComponentInstanceInput>> componentInstancesInputs;

	private Map<String, List<ComponentInstanceProperty>> componentInstancesProperties;

	private Map<String, List<ComponentInstanceProperty>> componentInstancesAttributes;

	private Map<String, List<CapabilityDefinition>> capabilities;

	private Map<String, List<RequirementDefinition>> requirements;

	private List<InputDefinition> inputs;

	private List<GroupDefinition> groups;
	
	private String derivedFromGenericType;
	private String derivedFromGenericVersion;
	private String toscaType;
	protected List<AdditionalInformationDefinition> additionalInformation;
	
	public String getDerivedFromGenericVersion() {
		return derivedFromGenericVersion;
	}

	public void setDerivedFromGenericVersion(String derivedFromGenericVersion) {
		this.derivedFromGenericVersion = derivedFromGenericVersion;
	}

	public String getDerivedFromGenericType() {
		return derivedFromGenericType;
	}

	public void setDerivedFromGenericType(String derivedFromGenericType) {
		this.derivedFromGenericType = derivedFromGenericType;
	}
	
	public Component(ComponentMetadataDefinition componentMetadataDefinition) {
		this.componentMetadataDefinition = componentMetadataDefinition;
	}

	@JsonIgnore
	public ComponentMetadataDefinition getComponentMetadataDefinition() {
		return componentMetadataDefinition;
	}

	public Map<String, ArtifactDefinition> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(Map<String, ArtifactDefinition> artifacts) {
		this.artifacts = artifacts;
	}

	public Map<String, ArtifactDefinition> getToscaArtifacts() {
		return toscaArtifacts;
	}

	public void setToscaArtifacts(Map<String, ArtifactDefinition> toscaArtifacts) {
		this.toscaArtifacts = toscaArtifacts;
	}

	public String getUniqueId() {
		return componentMetadataDefinition.getMetadataDataDefinition().getUniqueId();
	}

	public void setUniqueId(String uniqueId) {
		componentMetadataDefinition.getMetadataDataDefinition().setUniqueId(uniqueId);
	}

	public void setName(String name) {
		componentMetadataDefinition.getMetadataDataDefinition().setName(name);
	}

	public void setVersion(String version) {
		componentMetadataDefinition.getMetadataDataDefinition().setVersion(version);
	}

	public void setHighestVersion(Boolean isHighestVersion) {
		componentMetadataDefinition.getMetadataDataDefinition().setHighestVersion(isHighestVersion);
	}

	public void setCreationDate(Long creationDate) {
		componentMetadataDefinition.getMetadataDataDefinition().setCreationDate(creationDate);
	}

	public void setLastUpdateDate(Long lastUpdateDate) {
		componentMetadataDefinition.getMetadataDataDefinition().setLastUpdateDate(lastUpdateDate);
	}

	public void setDescription(String description) {
		componentMetadataDefinition.getMetadataDataDefinition().setDescription(description);
	}

	public void setState(LifecycleStateEnum state) {
		componentMetadataDefinition.getMetadataDataDefinition().setState(state.name());
	}

	public void setTags(List<String> tags) {
		componentMetadataDefinition.getMetadataDataDefinition().setTags(tags);
	}

	public void setConformanceLevel(String conformanceLevel) {
		componentMetadataDefinition.getMetadataDataDefinition().setConformanceLevel(conformanceLevel);
	}
	
	public void setIcon(String icon) {
		componentMetadataDefinition.getMetadataDataDefinition().setIcon(icon);
	}

	public void setContactId(String contactId) {
		componentMetadataDefinition.getMetadataDataDefinition().setContactId(contactId);
	}

	public String getCreatorUserId() {
		return this.componentMetadataDefinition.getMetadataDataDefinition().getCreatorUserId();
	}

	public void setCreatorUserId(String creatorUserId) {
		this.componentMetadataDefinition.getMetadataDataDefinition().setCreatorUserId(creatorUserId);
	}

	public String getCreatorFullName() {
		return this.componentMetadataDefinition.getMetadataDataDefinition().getCreatorFullName();
	}

	public void setCreatorFullName(String creatorFullName) {
		this.componentMetadataDefinition.getMetadataDataDefinition().setCreatorFullName(creatorFullName);
	}

	public String getLastUpdaterUserId() {
		return this.componentMetadataDefinition.getMetadataDataDefinition().getLastUpdaterUserId();
	}

	public void setLastUpdaterUserId(String lastUpdaterUserId) {
		this.componentMetadataDefinition.getMetadataDataDefinition().setLastUpdaterUserId(lastUpdaterUserId);
	}

	public String getLastUpdaterFullName() {
		return this.componentMetadataDefinition.getMetadataDataDefinition().getLastUpdaterFullName();
	}

	public void setLastUpdaterFullName(String lastUpdaterFullName) {
		this.componentMetadataDefinition.getMetadataDataDefinition().setLastUpdaterFullName(lastUpdaterFullName);
	}

	public String getName() {
		return componentMetadataDefinition.getMetadataDataDefinition().getName();
	}

	public String getVersion() {
		return componentMetadataDefinition.getMetadataDataDefinition().getVersion();
	}

	public Boolean isHighestVersion() {
		return componentMetadataDefinition.getMetadataDataDefinition().isHighestVersion();
	}

	public Long getCreationDate() {
		return componentMetadataDefinition.getMetadataDataDefinition().getCreationDate();
	}

	public Long getLastUpdateDate() {
		return componentMetadataDefinition.getMetadataDataDefinition().getLastUpdateDate();
	}

	public String getDescription() {
		return componentMetadataDefinition.getMetadataDataDefinition().getDescription();
	}

	public LifecycleStateEnum getLifecycleState() {
		if (componentMetadataDefinition.getMetadataDataDefinition().getState() != null) {
			return LifecycleStateEnum.valueOf(componentMetadataDefinition.getMetadataDataDefinition().getState());
		} else {
			return null;
		}
	}

	public List<String> getTags() {
		return componentMetadataDefinition.getMetadataDataDefinition().getTags();
	}

	public String getConformanceLevel() {
		return componentMetadataDefinition.getMetadataDataDefinition().getConformanceLevel();
	}
	
	public String getIcon() {
		return componentMetadataDefinition.getMetadataDataDefinition().getIcon();
	}

	public String getContactId() {
		return componentMetadataDefinition.getMetadataDataDefinition().getContactId();
	}

	public List<InputDefinition> getInputs() {
		return inputs;
	}

	public void setInputs(List<InputDefinition> inputs) {
		this.inputs = inputs;
	}

	public void setLifecycleState(LifecycleStateEnum state) {
		if (state != null) {
			this.componentMetadataDefinition.getMetadataDataDefinition().setState(state.name());
		}
	}

	public String getUUID() {
		return componentMetadataDefinition.getMetadataDataDefinition().getUUID();
	}

	public void setUUID(String uUID) {
		componentMetadataDefinition.getMetadataDataDefinition().setUUID(uUID);
	}

	public void setSystemName(String systemName) {
		componentMetadataDefinition.getMetadataDataDefinition().setSystemName(systemName);
	}

	public String getSystemName() {
		return componentMetadataDefinition.getMetadataDataDefinition().getSystemName();
	}

	public void setAllVersions(Map<String, String> allVersions) {
		componentMetadataDefinition.getMetadataDataDefinition().setAllVersions(allVersions);
	}

	public Map<String, String> getAllVersions() {
		return componentMetadataDefinition.getMetadataDataDefinition().getAllVersions();
	}

	public Map<String, ArtifactDefinition> getDeploymentArtifacts() {
		return deploymentArtifacts;
	}

	public void setDeploymentArtifacts(Map<String, ArtifactDefinition> deploymentArtifacts) {
		this.deploymentArtifacts = deploymentArtifacts;
	}

	public Map<String, ArtifactDefinition> getAllArtifacts() {
		HashMap<String, ArtifactDefinition> allArtifacts = new HashMap<>();
		allArtifacts.putAll(Optional.ofNullable(this.deploymentArtifacts).orElse(Collections.emptyMap()));
		allArtifacts.putAll(Optional.ofNullable(this.artifacts).orElse(Collections.emptyMap()));
		return allArtifacts;
	}

	public List<CategoryDefinition> getCategories() {
		return categories;
	}

	public void setCategories(List<CategoryDefinition> categories) {
		this.categories = categories;
	}

	public String getNormalizedName() {
		return componentMetadataDefinition.getMetadataDataDefinition().getNormalizedName();
	}

	public void setNormalizedName(String normalizedName) {
		componentMetadataDefinition.getMetadataDataDefinition().setNormalizedName(normalizedName);
	}

	public ComponentTypeEnum getComponentType() {
		return this.componentMetadataDefinition.getMetadataDataDefinition().getComponentType();
	}

	public void setComponentType(ComponentTypeEnum componentType) {
		this.componentMetadataDefinition.getMetadataDataDefinition().setComponentType(componentType);
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

	public List<ComponentInstance> getComponentInstances() {
		return componentInstances;
	}

	public Map<String, ArtifactDefinition> safeGetComponentInstanceDeploymentArtifacts(String componentInstanceId) {
		Optional<ComponentInstance> componentInstanceById = getComponentInstanceById(componentInstanceId);
		Map<String, ArtifactDefinition> instanceDeploymentArtifacts = componentInstanceById.get().safeGetDeploymentArtifacts();
		return instanceDeploymentArtifacts != null ? instanceDeploymentArtifacts : Collections.EMPTY_MAP;
	}

	public Map<String, ArtifactDefinition> safeGetComponentInstanceInformationalArtifacts(String componentInstanceId) {
		Optional<ComponentInstance> componentInstanceById = getComponentInstanceById(componentInstanceId);
		Map<String, ArtifactDefinition> instanceInformationalArtifacts = componentInstanceById.get().safeGetInformationalArtifacts();
		return instanceInformationalArtifacts != null ? instanceInformationalArtifacts : Collections.EMPTY_MAP;
	}

	public List<ArtifactDefinition> safeGetComponentInstanceHeatArtifacts(String componentInstanceId) {
		Optional<ComponentInstance> componentInstanceById = getComponentInstanceById(componentInstanceId);
		List<ArtifactDefinition> instanceHeatEnvArtifacts = Optional.ofNullable(componentInstanceById.get().safeGetDeploymentArtifacts().values()).orElse(new ArrayList<ArtifactDefinition>())
				.stream()
				.filter(artifact -> artifact.getArtifactType() != null && artifact.getArtifactType().equals(ArtifactTypeEnum.HEAT_ENV.name()))
				.collect(Collectors.toList());
		return instanceHeatEnvArtifacts == null ? Collections.EMPTY_LIST : instanceHeatEnvArtifacts;
	}

	public void setComponentInstances(List<ComponentInstance> resourceInstances) {
		this.componentInstances = resourceInstances;
	}

	public List<RequirementCapabilityRelDef> getComponentInstancesRelations() {
		return componentInstancesRelations;
	}

	public void setComponentInstancesRelations(List<RequirementCapabilityRelDef> resourceInstancesRelations) {
		this.componentInstancesRelations = resourceInstancesRelations;
	}

	public Map<String, List<ComponentInstanceProperty>> getComponentInstancesProperties() {
		return componentInstancesProperties;
	}

	public List<ComponentInstanceProperty> safeGetComponentInstanceProperties(String cmptInstacneId) {
		return this.safeGetComponentInstanceEntity(cmptInstacneId, this.componentInstancesProperties);
	}

	public List<ComponentInstanceInput> safeGetComponentInstanceInput(String comptInstanceId) {
		return this.safeGetComponentInstanceEntity(comptInstanceId, this.componentInstancesInputs);
	}

	public void setComponentInstancesProperties(
			Map<String, List<ComponentInstanceProperty>> resourceInstancesProperties) {
		this.componentInstancesProperties = resourceInstancesProperties;
	}

	public Boolean getIsDeleted() {
		return componentMetadataDefinition.getMetadataDataDefinition().isDeleted();
	}

	public void setIsDeleted(Boolean isDeleted) {
		componentMetadataDefinition.getMetadataDataDefinition().setIsDeleted(isDeleted);
	}

	public String getProjectCode() {
		return componentMetadataDefinition.getMetadataDataDefinition().getProjectCode();
	}

	public void setProjectCode(String projectCode) {
		componentMetadataDefinition.getMetadataDataDefinition().setProjectCode(projectCode);
	}

	public String getCsarUUID() {
		return componentMetadataDefinition.getMetadataDataDefinition().getCsarUUID();
	}

	public void setCsarUUID(String csarUUID) {
		componentMetadataDefinition.getMetadataDataDefinition().setCsarUUID(csarUUID);
	}

	public String getCsarVersion() {
		return componentMetadataDefinition.getMetadataDataDefinition().getCsarVersion();
	}

	public void setCsarVersion(String csarVersion) {
		componentMetadataDefinition.getMetadataDataDefinition().setCsarVersion(csarVersion);
	}

	public String getImportedToscaChecksum() {
		return componentMetadataDefinition.getMetadataDataDefinition().getImportedToscaChecksum();
	}

	public void setImportedToscaChecksum(String importedToscaChecksum) {
		componentMetadataDefinition.getMetadataDataDefinition().setImportedToscaChecksum(importedToscaChecksum);
	}

	public String getInvariantUUID() {
		return componentMetadataDefinition.getMetadataDataDefinition().getInvariantUUID();
	}

	public void setInvariantUUID(String invariantUUID) {
		componentMetadataDefinition.getMetadataDataDefinition().setInvariantUUID(invariantUUID);
	}

	public Optional<ComponentInstance> getComponentInstanceById(String id) {
		return componentInstances.stream().filter(instance -> id.equals(instance.getUniqueId())).findFirst();
	}

	public List<GroupDefinition> getGroups() {
		return groups;
	}

	public void setGroups(List<GroupDefinition> groups) {
		this.groups = groups;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((artifacts == null) ? 0 : artifacts.hashCode());
		result = prime * result + ((categories == null) ? 0 : categories.hashCode());
		result = prime * result + ((componentMetadataDefinition == null) ? 0 : componentMetadataDefinition.hashCode());
//		result = prime * result + ((creatorUserId == null) ? 0 : creatorUserId.hashCode());
//		result = prime * result + ((creatorFullName == null) ? 0 : creatorFullName.hashCode());
		result = prime * result + ((deploymentArtifacts == null) ? 0 : deploymentArtifacts.hashCode());
//		result = prime * result + ((lastUpdaterUserId == null) ? 0 : lastUpdaterUserId.hashCode());
//		result = prime * result + ((lastUpdaterFullName == null) ? 0 : lastUpdaterFullName.hashCode());
		result = prime * result + ((capabilities == null) ? 0 : capabilities.hashCode());
		result = prime * result + ((requirements == null) ? 0 : requirements.hashCode());
		result = prime * result + ((componentInstances == null) ? 0 : componentInstances.hashCode());
		result = prime * result
				+ ((componentInstancesProperties == null) ? 0 : componentInstancesProperties.hashCode());
		result = prime * result
				+ ((componentInstancesAttributes == null) ? 0 : componentInstancesAttributes.hashCode());
		result = prime * result + ((componentInstancesInputs == null) ? 0 : componentInstancesInputs.hashCode());
		result = prime * result + ((componentInstancesRelations == null) ? 0 : componentInstancesRelations.hashCode());
		result = prime * result + ((groups == null) ? 0 : groups.hashCode());
		result = prime * result + ((derivedFromGenericType == null) ? 0 : derivedFromGenericType.hashCode());
		result = prime * result + ((derivedFromGenericVersion == null) ? 0 : derivedFromGenericVersion.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Component other = (Component) obj;
		if (artifacts == null) {
			if (other.artifacts != null)
				return false;
		} else if (!artifacts.equals(other.artifacts))
			return false;
		if (categories == null) {
			if (other.categories != null)
				return false;
		} else if (!categories.equals(other.categories))
			return false;
		if (componentMetadataDefinition == null) {
			if (other.componentMetadataDefinition != null)
				return false;
		} else if (!componentMetadataDefinition.equals(other.componentMetadataDefinition))
			return false;
//		if (creatorUserId == null) {
//			if (other.creatorUserId != null)
//				return false;
//		} else if (!creatorUserId.equals(other.creatorUserId))
//			return false;
//		if (creatorFullName == null) {
//			if (other.creatorFullName != null)
//				return false;
//		} else if (!creatorFullName.equals(other.creatorFullName))
//			return false;
		if (deploymentArtifacts == null) {
			if (other.deploymentArtifacts != null)
				return false;
		} else if (!deploymentArtifacts.equals(other.deploymentArtifacts))
			return false;
//		if (lastUpdaterUserId == null) {
//			if (other.lastUpdaterUserId != null)
//				return false;
//		} else if (!lastUpdaterUserId.equals(other.lastUpdaterUserId))
//			return false;
//		if (lastUpdaterFullName == null) {
//			if (other.lastUpdaterFullName != null)
//				return false;
//		} else if (!lastUpdaterFullName.equals(other.lastUpdaterFullName))
//			return false;
		if (componentInstances == null) {
			if (other.componentInstances != null)
				return false;
		} else if (!componentInstances.equals(other.componentInstances))
			return false;
		if (componentInstancesProperties == null) {
			if (other.componentInstancesProperties != null)
				return false;
		} else if (!componentInstancesProperties.equals(other.componentInstancesProperties))
			return false;

		if (!Objects.equals(componentInstancesAttributes, other.componentInstancesAttributes)) {
			return false;
		}
		if (!Objects.equals(componentInstancesInputs, other.componentInstancesInputs)) {
			return false;
		}
		if (componentInstancesRelations == null) {
			if (other.componentInstancesRelations != null)
				return false;
		} else if (!componentInstancesRelations.equals(other.componentInstancesRelations))
			return false;
		if (requirements == null) {
			if (other.requirements != null)
				return false;
		} else if (!requirements.equals(other.requirements))
			return false;
		if (capabilities == null) {
			if (other.capabilities != null)
				return false;
		} else if (!capabilities.equals(other.capabilities))
			return false;
		if (groups == null) {
			if (other.groups != null)
				return false;
		} else if (!groups.equals(other.groups))
			return false;
		if (derivedFromGenericType == null) {
			if (other.derivedFromGenericType != null)
				return false;
		} else if (!derivedFromGenericType.equals(other.derivedFromGenericType))
			return false;
		if (derivedFromGenericVersion == null) {
			if (other.derivedFromGenericVersion != null)
				return false;
		} else if (!derivedFromGenericVersion.equals(other.derivedFromGenericVersion))
			return false;
		return true;
	}

	public void addCategory(String category, String subCategory) {
		if (category != null || subCategory != null) {
			if (categories == null) {
				categories = new ArrayList<>();
			}
			CategoryDefinition selectedCategory = null;
			for (CategoryDefinition categoryDef : categories) {
				if (categoryDef.getName().equals(category)) {
					selectedCategory = categoryDef;
				}
			}
			if (selectedCategory == null) {
				selectedCategory = new CategoryDefinition();
				selectedCategory.setName(category);
				categories.add(selectedCategory);
			}
			List<SubCategoryDefinition> subcategories = selectedCategory.getSubcategories();
			if (subcategories == null) {
				subcategories = new ArrayList<>();
				selectedCategory.setSubcategories(subcategories);
			}
			SubCategoryDefinition selectedSubcategory = null;
			for (SubCategoryDefinition subcategory : subcategories) {
				if (subcategory.getName().equals(subCategory)) {
					selectedSubcategory = subcategory;
				}
			}
			if (selectedSubcategory == null) {
				selectedSubcategory = new SubCategoryDefinition();
				selectedSubcategory.setName(subCategory);
				subcategories.add(selectedSubcategory);
			}
		}
	}

	public void addCategory(CategoryDefinition category) {
		addCategory(category, null);
	}

	public void addCategory(CategoryDefinition category, SubCategoryDefinition subCategory) {
		if (categories == null) {
			categories = new ArrayList<>();
		}
		boolean foundCat = false;
		for (CategoryDefinition cat : categories) {
			if (cat.getName().equals(category.getName())) {
				foundCat = true;
				if (subCategory != null) {
					List<SubCategoryDefinition> subcategories = cat.getSubcategories();
					if (subcategories == null) {
						subcategories = new ArrayList<>();
						cat.setSubcategories(subcategories);
					}
					for (SubCategoryDefinition subcat : subcategories) {
						boolean foundSub = false;
						if (subcat.getName().equals(subCategory.getName())) {
							foundSub = true;
						}
						if (foundSub == false) {
							subcategories.add(subCategory);
							break;
						}
					}
				}
			}
		}
		if (foundCat == false) {
			if (subCategory != null) {
				category.addSubCategory(subCategory);
			}
			categories.add(category);
		}
	}

	public Map<String, List<ComponentInstanceProperty>> getComponentInstancesAttributes() {
		return componentInstancesAttributes;
	}

	public void setComponentInstancesAttributes(
			Map<String, List<ComponentInstanceProperty>> componentInstancesAttributes) {
		this.componentInstancesAttributes = componentInstancesAttributes;
	}

	public Map<String, List<ComponentInstanceInput>> getComponentInstancesInputs() {
		return componentInstancesInputs;
	}

	public List<ComponentInstanceInput> safeGetComponentInstanceInputsByName(String cmptInstanceName) {
		List<ComponentInstanceInput> emptyPropsList = Collections.emptyList();
		if (this.componentInstancesInputs == null) {
			return emptyPropsList;
		}
		Optional<List<ComponentInstanceInput>> instanceInputsByName = this.componentInstances.stream()
				.filter(ci -> ci.getName().equals(cmptInstanceName))
				.map(ComponentInstance::getUniqueId)
				.map(instanceId -> safeGetComponentInstanceEntity(instanceId, this.componentInstancesInputs))
				.findAny();
		return instanceInputsByName.orElse(emptyPropsList);
	}

	private <T> List<T> safeGetComponentInstanceEntity(String cmptInstanceId, Map<String, List<T>> instanceEntities) {
		List<T> emptyPropsList = Collections.emptyList();
		if (instanceEntities == null) {
			return emptyPropsList;
		}
		List<T> cmptInstanceProps = instanceEntities.get(cmptInstanceId);
		return cmptInstanceProps == null ? emptyPropsList : cmptInstanceProps;
	}



	public void setComponentInstancesInputs(Map<String, List<ComponentInstanceInput>> componentInstancesInputs) {
		this.componentInstancesInputs = componentInstancesInputs;
	}

	public void setSpecificComponetTypeArtifacts(Map<String, ArtifactDefinition> specificComponentTypeArtifacts) {
		// Implement where needed
	}
	
	public void setMetadataDefinition(ComponentMetadataDefinition metadataDefinition) {
		this.componentMetadataDefinition = metadataDefinition;
	}
	
	public String fetchGenericTypeToscaNameFromConfig(){
		// Implement where needed
		return ConfigurationManager.getConfigurationManager().getConfiguration().getGenericAssetNodeTypes().get(this.assetType());
	}
	
	public String assetType(){
		// Implement where needed
		return this.getComponentType().getValue();
	}
	
	public boolean shouldGenerateInputs(){
		// Implement where needed
		return true;
	}
	
	public boolean deriveFromGeneric(){
		// Implement where needed
		return true;
	}
	
	public void setDerivedFromGenericInfo(Resource genericType){
		derivedFromGenericType = genericType.getToscaResourceName();
		derivedFromGenericVersion = genericType.getVersion();
	}

	public String getToscaType() {
		return toscaType;
	}

	public void setToscaType(String toscaType) {
		this.toscaType = toscaType;
	}
	public List<AdditionalInformationDefinition> getAdditionalInformation() {
		return additionalInformation;
	}

	public void setAdditionalInformation(List<AdditionalInformationDefinition> additionalInformation) {
		this.additionalInformation = additionalInformation;
	}
	
}
