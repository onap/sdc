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

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.MapUtils.isEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PolicyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PolicyTargetType;
import org.openecomp.sdc.be.datatypes.elements.PropertiesOwner;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElementTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;

public abstract class Component implements PropertiesOwner {

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
	private Map<String, List<ComponentInstanceInterface>> componentInstancesInterfaces;
	private List<InputDefinition> inputs;
	private List<GroupDefinition> groups;
	private Map<String, PolicyDefinition> policies;
	private String derivedFromGenericType;
	private String derivedFromGenericVersion;
	private String toscaType;
	protected List<AdditionalInformationDefinition> additionalInformation;
	private Map<String, CINodeFilterDataDefinition> nodeFilterComponents;
	private Map<String, List<UploadNodeFilterInfo>> nodeFilters;
	private Map<String, List<UploadNodeFilterInfo>> serviceFilters;
	protected List<PropertyDefinition> properties;
	private Map<String, InterfaceDefinition> interfaces;

	public Map<String, InterfaceDefinition> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(Map<String, InterfaceDefinition> interfaces) {
		this.interfaces = interfaces;
	}

    public Component(ComponentMetadataDefinition componentMetadataDefinition) {
        this.componentMetadataDefinition = componentMetadataDefinition;
    }

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

	@Override
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

	@Override
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
        }
        else {
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

    public List<InputDefinition> safeGetInputs() {
        return inputs == null ? new ArrayList<>() : inputs;
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
        allArtifacts.putAll(Optional.ofNullable(this.deploymentArtifacts).orElse(emptyMap()));
        allArtifacts.putAll(Optional.ofNullable(this.artifacts).orElse(emptyMap()));
        return allArtifacts;
    }

    public List<CategoryDefinition> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryDefinition> categories) {
        this.categories = categories;
    }

	@Override
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
        if (MapUtils.isEmpty(capabilities)) {
            capabilities = Maps.newHashMap();
        }
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

    public List<ComponentInstance> safeGetComponentInstances() {
    	if(componentInstances != null) {
    		return componentInstances;
    	}else {
    		return emptyList();
    	}
    }

    public Optional<ComponentInstance> fetchInstanceById(String instanceId) {
        return Optional.ofNullable(MapUtil.toMap(componentInstances, ComponentInstance::getUniqueId).get(instanceId));
    }

    public Map<String, ArtifactDefinition> safeGetComponentInstanceDeploymentArtifacts(String componentInstanceId) {
        return getComponentInstanceById(componentInstanceId).map(ComponentInstance::safeGetDeploymentArtifacts)
                                                            .orElse(emptyMap());
    }

    public Map<String, ArtifactDefinition> safeGetComponentInstanceInformationalArtifacts(String componentInstanceId) {
        return getComponentInstanceById(componentInstanceId).map(ComponentInstance::safeGetInformationalArtifacts)
                                                            .orElse(emptyMap());
    }

    public List<ArtifactDefinition> safeGetComponentInstanceHeatArtifacts(String componentInstanceId) {
        return safeGetComponentInstanceDeploymentArtifacts(componentInstanceId)
                .values()
                .stream()
                .filter(artifact -> ArtifactTypeEnum.HEAT_ENV.name().equals(artifact.getArtifactType()))
                .collect(Collectors.toList());
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

    public Map<String, List<ComponentInstanceProperty>> safeGetComponentInstancesProperties() {
        return componentInstancesProperties == null ? emptyMap() : componentInstancesProperties;
    }

    public Map<String, List<ComponentInstanceInput>> safeGetComponentInstancesInputs() {
        return componentInstancesInputs == null ? emptyMap() : componentInstancesInputs;
    }

    public List<ComponentInstanceProperty> safeGetComponentInstanceProperties(String cmptInstacneId) {
        return this.safeGetComponentInstanceEntity(cmptInstacneId, this.componentInstancesProperties);
    }

    public List<ComponentInstanceInput> safeGetComponentInstanceInput(String comptInstanceId) {
        return this.safeGetComponentInstanceEntity(comptInstanceId, this.componentInstancesInputs);
    }

	public List<ComponentInstanceInterface> safeGetComponentInstanceInterfaces(String cmptInstacneId) {
		return this.safeGetComponentInstanceEntity(cmptInstacneId, this.componentInstancesInterfaces);
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
        return getComponentInstanceByPredicate(instance -> id.equals(instance.getUniqueId()));
    }

    public Optional<ComponentInstance> getComponentInstanceByName(String name) {
        return getComponentInstanceByPredicate(instance -> name.equals(instance.getName()));
    }

    private Optional<ComponentInstance> getComponentInstanceByPredicate(Predicate<ComponentInstance> predicate) {
        if (componentInstances == null) {
            return Optional.empty();
        }
        return componentInstances.stream().filter(predicate).findFirst();
    }

    public List<GroupDefinition> getGroups() {
        return groups;
    }

    public List<GroupDefinition> safeGetGroups() {
        return groups == null ? emptyList() : groups;
    }

    public Optional<GroupDefinition> getGroupById(String id) {
        return getGroupByPredicate(group -> group.getUniqueId().equals(id));
    }

    public Optional<GroupDefinition> getGroupByInvariantName(String name) {
        return getGroupByPredicate(group -> name.equals(group.getInvariantName()));
    }

    public boolean containsGroupWithInvariantName(String invariantName) {
        return groups != null && groups.stream().anyMatch(gr -> invariantName.equals(gr.getInvariantName()));
    }

    private Optional<GroupDefinition> getGroupByPredicate(Predicate<GroupDefinition> predicate) {
        if (groups == null) {
            return Optional.empty();
        }
        return groups.stream()
                     .filter(predicate)
                     .findAny();
    }

	public Map<String, List<ComponentInstanceInterface>> getComponentInstancesInterfaces() {
		return componentInstancesInterfaces;
	}

	public void setComponentInstancesInterfaces(Map<String, List<ComponentInstanceInterface>> componentInstancesInterfaces) {
		this.componentInstancesInterfaces = componentInstancesInterfaces;
	}

	public void setGroups(List<GroupDefinition> groups) {
		this.groups = groups;
	}

  public void addGroups(List<GroupDefinition> groupsToAdd) {
    if (groups == null) {
      groups = new ArrayList<>();
    }
    groups.addAll(groupsToAdd);
  }

	public Map<String, PolicyDefinition> getPolicies() {
		return policies;
	}

    public void setPolicies(Map<String, PolicyDefinition> policies) {
        this.policies = policies;
    }

    public void addPolicy(PolicyDefinition policyDefinition) {
	    if(MapUtils.isEmpty(this.policies)) {
	        this.policies = new HashMap<>();
        }

        this.policies.put(policyDefinition.getUniqueId(), policyDefinition);
    }

	public Map<String, CINodeFilterDataDefinition> getNodeFilterComponents() {
		return nodeFilterComponents;
	}

	public void setNodeFilterComponents(Map<String, CINodeFilterDataDefinition> nodeFilter) {
		this.nodeFilterComponents = nodeFilter;
	}



	public Map<String, List<UploadNodeFilterInfo>> getNodeFilters() {
		return nodeFilters;
	}

	public void setNodeFilters(
			Map<String, List<UploadNodeFilterInfo>> nodeFilters) {
		this.nodeFilters = nodeFilters;
	}

	public Map<String, List<UploadNodeFilterInfo>> getServiceFilters() {
		return serviceFilters;
	}

	public void setServiceFilters(
			Map<String, List<UploadNodeFilterInfo>> serviceFilters) {
		this.serviceFilters = serviceFilters;
	}

	public List<PropertyDefinition> getProperties() {
		return properties;
	}

	public void setProperties(List<PropertyDefinition> properties) {
		this.properties = properties;
	}

	public void addProperty(PropertyDefinition propertyDefinition) {
	    if(org.apache.commons.collections.CollectionUtils.isEmpty(this.properties)) {
	        this.properties = new ArrayList<>();
        }

        this.properties.add(propertyDefinition);;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((artifacts == null) ? 0 : artifacts.hashCode());
		result = prime * result + ((categories == null) ? 0 : categories.hashCode());
		result = prime * result + ((componentMetadataDefinition == null) ? 0 : componentMetadataDefinition.hashCode());
		result = prime * result + ((deploymentArtifacts == null) ? 0 : deploymentArtifacts.hashCode());
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
		result = prime * result + ((policies == null) ? 0 : policies.hashCode());
		result = prime * result + ((derivedFromGenericType == null) ? 0 : derivedFromGenericType.hashCode());
		result = prime * result + ((derivedFromGenericVersion == null) ? 0 : derivedFromGenericVersion.hashCode());
		result = prime * result + ((interfaces == null) ? 0 : interfaces.hashCode());
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
        Component other = (Component) obj;
        if (artifacts == null) {
            if (other.artifacts != null) {
                return false;
            }
        }
        else if (!artifacts.equals(other.artifacts)) {
            return false;
        }
        if (categories == null) {
            if (other.categories != null) {
                return false;
            }
        }
        else if (!categories.equals(other.categories)) {
            return false;
        }
        if (componentMetadataDefinition == null) {
            if (other.componentMetadataDefinition != null) {
                return false;
            }
        }
        else if (!componentMetadataDefinition.equals(other.componentMetadataDefinition)) {
            return false;
        }

        if (deploymentArtifacts == null) {
            if (other.deploymentArtifacts != null) {
                return false;
            }
        }
        else if (!deploymentArtifacts.equals(other.deploymentArtifacts)) {
            return false;
        }

        if (componentInstances == null) {
            if (other.componentInstances != null) {
                return false;
            }
        }
        else if (!componentInstances.equals(other.componentInstances)) {
            return false;
        }
        if (componentInstancesProperties == null) {
            if (other.componentInstancesProperties != null) {
                return false;
            }
        }
        else if (!componentInstancesProperties.equals(other.componentInstancesProperties)) {
            return false;
        }

        if (!Objects.equals(componentInstancesAttributes, other.componentInstancesAttributes)) {
            return false;
        }
        if (!Objects.equals(componentInstancesInputs, other.componentInstancesInputs)) {
            return false;
        }
        if (componentInstancesRelations == null) {
            if (other.componentInstancesRelations != null) {
                return false;
            }
        }
        else if (!componentInstancesRelations.equals(other.componentInstancesRelations)) {
            return false;
        }
        if (requirements == null) {
            if (other.requirements != null) {
                return false;
            }
        }
        else if (!requirements.equals(other.requirements)) {
            return false;
        }
        if (capabilities == null) {
            if (other.capabilities != null) {
                return false;
            }
        }
        else if (!capabilities.equals(other.capabilities)) {
            return false;
        }
        if (groups == null) {
            if (other.groups != null) {
                return false;
            }
        }
        else if (!groups.equals(other.groups)) {
            return false;
        }
        if (policies == null) {
            if (other.policies != null) {
                return false;
            }
        }
        else if (!policies.equals(other.policies)) {
            return false;
        }
        if (derivedFromGenericType == null) {
            if (other.derivedFromGenericType != null) {
                return false;
            }
        }
        else if (!derivedFromGenericType.equals(other.derivedFromGenericType)) {
            return false;
        }
        if (derivedFromGenericVersion == null) {
            if (other.derivedFromGenericVersion != null) {
                return false;
            }
        }
        else if (!derivedFromGenericVersion.equals(other.derivedFromGenericVersion)) {
            return false;
        }
        if (interfaces == null) {
            if (other.interfaces != null) {
                return false;
            }
        }
        else if (!interfaces.equals(other.interfaces)) {
            return false;
        }
        else if (!properties.equals(other.properties)) {
            return false;
        }
        else if (!nodeFilterComponents.equals(other.nodeFilterComponents)) {
            return false;
        }
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
                        if (!foundSub) {
                            subcategories.add(subCategory);
                            break;
                        }
                    }
                }
            }
        }
        if (!foundCat) {
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

    public Map<String, List<PropertyDataDefinition>> safeGetGroupsProperties() {
        if (isEmpty(groups)) {
            return emptyMap();
        }
        return groups.stream()
              .filter(gr -> Objects.nonNull(gr.getProperties()))
              .collect(toMap(GroupDataDefinition::getUniqueId,
                             GroupDataDefinition::getProperties));
    }

    public Map<String, List<PropertyDataDefinition>> safeGetPolicyProperties() {
        if (isEmpty(policies)) {
            return emptyMap();
        }
        return policies.values()
                .stream()
                .filter(policy -> Objects.nonNull(policy.getProperties()))
                .collect(toMap(PolicyDataDefinition::getUniqueId,
                               PolicyDataDefinition::getProperties));
    }

    public List<ComponentInstanceInput> safeGetComponentInstanceInputsByName(String cmptInstanceName) {
        List<ComponentInstanceInput> emptyPropsList = emptyList();
        if (this.componentInstancesInputs == null) {
            return emptyPropsList;
        }
        return this.componentInstances.stream()
                                      .filter(ci -> ci.getName().equals(cmptInstanceName))
                                      .map(ComponentInstance::getUniqueId)
                                      .map(instanceId -> safeGetComponentInstanceEntity(instanceId, this.componentInstancesInputs))
                                      .findAny()
                                      .orElse(emptyPropsList);
    }

    private <T> List<T> safeGetComponentInstanceEntity(String cmptInstanceId, Map<String, List<T>> instanceEntities) {
        List<T> emptyPropsList = emptyList();
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

    public String fetchGenericTypeToscaNameFromConfig() {
        // Implement where needed
        return ConfigurationManager.getConfigurationManager()
                                   .getConfiguration()
                                   .getGenericAssetNodeTypes()
                                   .get(this.assetType());
    }

    public String assetType() {
        // Implement where needed
        return this.getComponentType().getValue();
    }

    public boolean shouldGenerateInputs() {
        // Implement where needed
        return true;
    }

    public boolean deriveFromGeneric() {
        // Implement where needed
        return true;
    }

    public void setDerivedFromGenericInfo(Resource genericType) {
        derivedFromGenericType = genericType.getToscaResourceName();
        derivedFromGenericVersion = genericType.getVersion();
    }

    public boolean isTopologyTemplate() {
        return ToscaElementTypeEnum.TOPOLOGY_TEMPLATE.getValue().equals(toscaType);
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

    public PolicyDefinition getPolicyById(String id) {
        return policies != null ? policies.get(id) : null;
    }

    public List<PolicyDefinition> resolvePoliciesList() {
        if (policies == null) {
            return emptyList();
        }
        return new ArrayList<>(policies.values());
    }

    public List<PolicyDefinition> resolvePoliciesContainingTarget(String targetId, PolicyTargetType targetType) {
        Predicate<PolicyDefinition> containsTarget = policy -> policy.containsTarget(targetId, targetType);
        return resolvePoliciesByPredicate(containsTarget);
    }

    private List<PolicyDefinition> resolvePoliciesByPredicate(Predicate<PolicyDefinition> policyPredicate) {
        if (policies == null) {
            return emptyList();
        }
        return policies.values().stream()
                       .filter(policyPredicate)
                       .collect(Collectors.toList());
    }

    public List<GroupDefinition> resolveGroupsByMember(String instanceId) {
        if (groups == null) {
            return emptyList();
        }
        return groups.stream()
                     .filter(group -> group.containsInstanceAsMember(instanceId))
                     .collect(Collectors.toList());
    }

    public String getActualComponentType() {
        return componentMetadataDefinition.getMetadataDataDefinition().getActualComponentType();
    }

    public Boolean isArchived() { return componentMetadataDefinition.getMetadataDataDefinition().isArchived(); }

    public void setArchived(Boolean archived) { componentMetadataDefinition.getMetadataDataDefinition().setArchived(archived); }

    public Long getArchiveTime() { return componentMetadataDefinition.getMetadataDataDefinition().getArchiveTime(); }

    public void setArchiveTime(Long archiveTime) { componentMetadataDefinition.getMetadataDataDefinition().setArchiveTime(archiveTime); }

    public Boolean isVspArchived() { return componentMetadataDefinition.getMetadataDataDefinition().isVspArchived();	}

    public void setVspArchived(Boolean vspArchived) { componentMetadataDefinition.getMetadataDataDefinition().setVspArchived(vspArchived); }

}
