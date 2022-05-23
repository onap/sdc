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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PolicyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PolicyTargetType;
import org.openecomp.sdc.be.datatypes.elements.PropertiesOwner;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SubstitutionFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElementTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.log.api.ILogConfiguration;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class Component implements PropertiesOwner {

    protected List<AdditionalInformationDefinition> additionalInformation;
    protected List<PropertyDefinition> properties;
    protected List<AttributeDefinition> attributes;
    private ComponentMetadataDefinition componentMetadataDefinition;
    private Map<String, ArtifactDefinition> artifacts;
    private Map<String, ArtifactDefinition> deploymentArtifacts;
    private Map<String, ArtifactDefinition> toscaArtifacts;
    @EqualsAndHashCode.Include
    private List<CategoryDefinition> categories;
    private List<ComponentInstance> componentInstances;
    private List<RequirementCapabilityRelDef> componentInstancesRelations;
    private Map<String, List<ComponentInstanceInput>> componentInstancesInputs;
    private Map<String, List<ComponentInstanceOutput>> componentInstancesOutputs;
    private Map<String, List<ComponentInstanceProperty>> componentInstancesProperties;
    private Map<String, List<ComponentInstanceAttribute>> componentInstancesAttributes;
    private Map<String, List<CapabilityDefinition>> capabilities;
    private Map<String, List<RequirementDefinition>> requirements;
    private Map<String, List<ComponentInstanceInterface>> componentInstancesInterfaces;
    private List<InputDefinition> inputs;
    private List<OutputDefinition> outputs;
    private List<GroupDefinition> groups;
    private Map<String, PolicyDefinition> policies;
    private String derivedFromGenericType;
    private String derivedFromGenericVersion;
    private String toscaType;
    private Map<String, CINodeFilterDataDefinition> nodeFilterComponents;
    private Map<String, InterfaceDefinition> interfaces;
    private List<DataTypeDefinition> dataTypes;
    private SubstitutionFilterDataDefinition substitutionFilter;
    private String model;

    protected Component(ComponentMetadataDefinition componentMetadataDefinition) {
        this.componentMetadataDefinition = componentMetadataDefinition;
    }

    public Map<String, String> getCategorySpecificMetadata() {
        final Map<String, String> categorySpecificMetadata = componentMetadataDefinition.getMetadataDataDefinition().getCategorySpecificMetadata();
        return categorySpecificMetadata == null ? Collections.emptyMap() : categorySpecificMetadata;
    }

    public void setCategorySpecificMetadata(final Map<String, String> categorySpecificMetadata) {
        componentMetadataDefinition.getMetadataDataDefinition().setCategorySpecificMetadata(categorySpecificMetadata);
    }

    public String getModel() {
        return getComponentMetadataDefinition().getMetadataDataDefinition().getModel();
    }

    public void setModel(final String model) {
        getComponentMetadataDefinition().getMetadataDataDefinition().setModel(model);
    }

    @JsonIgnore
    public ComponentMetadataDefinition getComponentMetadataDefinition() {
        return componentMetadataDefinition;
    }

    @Override
    @EqualsAndHashCode.Include
    public String getUniqueId() {
        return componentMetadataDefinition.getMetadataDataDefinition().getUniqueId();
    }

    public void setUniqueId(String uniqueId) {
        componentMetadataDefinition.getMetadataDataDefinition().setUniqueId(uniqueId);
    }

    public void setHighestVersion(Boolean isHighestVersion) {
        componentMetadataDefinition.getMetadataDataDefinition().setHighestVersion(isHighestVersion);
    }

    public void setState(LifecycleStateEnum state) {
        componentMetadataDefinition.getMetadataDataDefinition().setState(state.name());
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
    @EqualsAndHashCode.Include
    public String getName() {
        return componentMetadataDefinition.getMetadataDataDefinition().getName();
    }

    public void setName(String name) {
        componentMetadataDefinition.getMetadataDataDefinition().setName(name);
    }

    public String getVersion() {
        return componentMetadataDefinition.getMetadataDataDefinition().getVersion();
    }

    public void setVersion(String version) {
        componentMetadataDefinition.getMetadataDataDefinition().setVersion(version);
    }

    public Boolean isHighestVersion() {
        return componentMetadataDefinition.getMetadataDataDefinition().isHighestVersion();
    }

    public Long getCreationDate() {
        return componentMetadataDefinition.getMetadataDataDefinition().getCreationDate();
    }

    public void setCreationDate(Long creationDate) {
        componentMetadataDefinition.getMetadataDataDefinition().setCreationDate(creationDate);
    }

    public Long getLastUpdateDate() {
        return componentMetadataDefinition.getMetadataDataDefinition().getLastUpdateDate();
    }

    public void setLastUpdateDate(Long lastUpdateDate) {
        componentMetadataDefinition.getMetadataDataDefinition().setLastUpdateDate(lastUpdateDate);
    }

    public String getDescription() {
        return componentMetadataDefinition.getMetadataDataDefinition().getDescription();
    }

    public void setDescription(String description) {
        componentMetadataDefinition.getMetadataDataDefinition().setDescription(description);
    }

    public LifecycleStateEnum getLifecycleState() {
        if (componentMetadataDefinition.getMetadataDataDefinition().getState() != null) {
            return LifecycleStateEnum.valueOf(componentMetadataDefinition.getMetadataDataDefinition().getState());
        } else {
            return null;
        }
    }

    public void setLifecycleState(LifecycleStateEnum state) {
        if (state != null) {
            this.componentMetadataDefinition.getMetadataDataDefinition().setState(state.name());
        }
    }

    public List<String> getTags() {
        return componentMetadataDefinition.getMetadataDataDefinition().getTags();
    }

    public void setTags(List<String> tags) {
        componentMetadataDefinition.getMetadataDataDefinition().setTags(tags);
    }

    public String getConformanceLevel() {
        return componentMetadataDefinition.getMetadataDataDefinition().getConformanceLevel();
    }

    public void setConformanceLevel(String conformanceLevel) {
        componentMetadataDefinition.getMetadataDataDefinition().setConformanceLevel(conformanceLevel);
    }

    public String getIcon() {
        return componentMetadataDefinition.getMetadataDataDefinition().getIcon();
    }

    public void setIcon(String icon) {
        componentMetadataDefinition.getMetadataDataDefinition().setIcon(icon);
    }

    public String getContactId() {
        return componentMetadataDefinition.getMetadataDataDefinition().getContactId();
    }

    public void setContactId(String contactId) {
        componentMetadataDefinition.getMetadataDataDefinition().setContactId(contactId);
    }

    public List<InputDefinition> safeGetInputs() {
        return inputs == null ? new ArrayList<>() : inputs;
    }

    public String getUUID() {
        return componentMetadataDefinition.getMetadataDataDefinition().getUUID();
    }

    public void setUUID(String uUID) {
        componentMetadataDefinition.getMetadataDataDefinition().setUUID(uUID);
    }

    public String getSystemName() {
        return componentMetadataDefinition.getMetadataDataDefinition().getSystemName();
    }

    public void setSystemName(String systemName) {
        componentMetadataDefinition.getMetadataDataDefinition().setSystemName(systemName);
    }

    public Map<String, String> getAllVersions() {
        return componentMetadataDefinition.getMetadataDataDefinition().getAllVersions();
    }

    public void setAllVersions(Map<String, String> allVersions) {
        componentMetadataDefinition.getMetadataDataDefinition().setAllVersions(allVersions);
    }

    public Map<String, ArtifactDefinition> getAllArtifacts() {
        HashMap<String, ArtifactDefinition> allArtifacts = new HashMap<>();
        allArtifacts.putAll(Optional.ofNullable(this.deploymentArtifacts).orElse(emptyMap()));
        allArtifacts.putAll(Optional.ofNullable(this.artifacts).orElse(emptyMap()));
        return allArtifacts;
    }

    @Override
    @EqualsAndHashCode.Include
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

    public boolean isService() {
        return getComponentType() == ComponentTypeEnum.SERVICE;
    }

    public Map<String, List<CapabilityDefinition>> getCapabilities() {
        if (MapUtils.isEmpty(capabilities)) {
            capabilities = Maps.newHashMap();
        }
        return capabilities;
    }

    public List<ComponentInstance> safeGetComponentInstances() {
        if (componentInstances != null) {
            return componentInstances;
        } else {
            return emptyList();
        }
    }

    public Optional<ComponentInstance> fetchInstanceById(String instanceId) {
        return Optional.ofNullable(MapUtil.toMap(componentInstances, ComponentInstance::getUniqueId).get(instanceId));
    }

    public Map<String, ArtifactDefinition> safeGetComponentInstanceDeploymentArtifacts(String componentInstanceId) {
        return getComponentInstanceById(componentInstanceId).map(ComponentInstance::safeGetDeploymentArtifacts).orElse(emptyMap());
    }

    public Map<String, ArtifactDefinition> safeGetComponentInstanceInformationalArtifacts(String componentInstanceId) {
        return getComponentInstanceById(componentInstanceId).map(ComponentInstance::safeGetInformationalArtifacts).orElse(emptyMap());
    }

    public List<ArtifactDefinition> safeGetComponentInstanceHeatArtifacts(String componentInstanceId) {
        return safeGetComponentInstanceDeploymentArtifacts(componentInstanceId).values().stream()
            .filter(artifact -> ArtifactTypeEnum.HEAT_ENV.getType().equals(artifact.getArtifactType())).collect(Collectors.toList());
    }

    public Map<String, List<ComponentInstanceProperty>> safeGetComponentInstancesProperties() {
        return componentInstancesProperties == null ? emptyMap() : componentInstancesProperties;
    }

    public Map<String, List<ComponentInstanceAttribute>> safeGetComponentInstancesAttributes() {
        return componentInstancesAttributes == null ? emptyMap() : componentInstancesAttributes;
    }

    public Map<String, List<ComponentInstanceProperty>> safeGetUiComponentInstancesProperties() {
        return componentInstancesProperties == null ? emptyMap() : findUiComponentInstancesProperties();
    }

    private Map<String, List<ComponentInstanceProperty>> findUiComponentInstancesProperties() {
        List<String> instancesFromUi = componentInstances.stream().filter(i -> !i.isCreatedFromCsar()).map(ComponentInstance::getUniqueId)
            .collect(Collectors.toList());
        return componentInstancesProperties.entrySet().stream().filter(e -> instancesFromUi.contains(e.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, List<ComponentInstanceInput>> safeGetComponentInstancesInputs() {
        return componentInstancesInputs == null ? emptyMap() : componentInstancesInputs;
    }

    public Map<String, List<ComponentInstanceInput>> safeGetUiComponentInstancesInputs() {
        return componentInstancesInputs == null ? emptyMap() : findUiComponentInstancesInputs();
    }

    private Map<String, List<ComponentInstanceInput>> findUiComponentInstancesInputs() {
        List<String> instancesFromUi = componentInstances.stream().filter(i -> !i.isCreatedFromCsar()).map(ComponentInstance::getUniqueId)
            .collect(Collectors.toList());
        return componentInstancesInputs.entrySet().stream().filter(e -> instancesFromUi.contains(e.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public List<ComponentInstanceProperty> safeGetComponentInstanceProperties(String cmptInstacneId) {
        return this.safeGetComponentInstanceEntity(cmptInstacneId, this.componentInstancesProperties);
    }

    public List<ComponentInstanceAttribute> safeGetComponentInstanceAttributes(String cmptInstacneId) {
        return this.safeGetComponentInstanceEntity(cmptInstacneId, this.componentInstancesAttributes);
    }

    public List<ComponentInstanceInput> safeGetComponentInstanceInput(String comptInstanceId) {
        return this.safeGetComponentInstanceEntity(comptInstanceId, this.componentInstancesInputs);
    }

    public List<ComponentInstanceOutput> safeGetComponentInstanceOutput(String comptInstanceId) {
        return this.safeGetComponentInstanceEntity(comptInstanceId, this.componentInstancesOutputs);
    }

    public List<ComponentInstanceInterface> safeGetComponentInstanceInterfaces(String cmptInstacneId) {
        return this.safeGetComponentInstanceEntity(cmptInstacneId, this.componentInstancesInterfaces);
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
        return groups.stream().filter(predicate).findAny();
    }

    public void addGroups(List<GroupDefinition> groupsToAdd) {
        if (groups == null) {
            groups = new ArrayList<>();
        }
        groups.addAll(groupsToAdd);
    }

    public void addPolicy(PolicyDefinition policyDefinition) {
        if (MapUtils.isEmpty(this.policies)) {
            this.policies = new HashMap<>();
        }
        this.policies.put(policyDefinition.getUniqueId(), policyDefinition);
    }

    public void addProperty(PropertyDefinition propertyDefinition) {
        if (org.apache.commons.collections.CollectionUtils.isEmpty(this.properties)) {
            this.properties = new ArrayList<>();
        }
        this.properties.add(propertyDefinition);
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

    public Map<String, List<PropertyDataDefinition>> safeGetGroupsProperties() {
        if (isEmpty(groups)) {
            return emptyMap();
        }
        return groups.stream().filter(gr -> Objects.nonNull(gr.getProperties()))
            .collect(toMap(GroupDataDefinition::getUniqueId, GroupDataDefinition::getProperties));
    }

    public Map<String, List<PropertyDataDefinition>> safeGetPolicyProperties() {
        if (isEmpty(policies)) {
            return emptyMap();
        }
        return policies.values().stream().filter(policy -> Objects.nonNull(policy.getProperties()))
            .collect(toMap(PolicyDataDefinition::getUniqueId, PolicyDataDefinition::getProperties));
    }

    public List<ComponentInstanceInput> safeGetComponentInstanceInputsByName(String cmptInstanceName) {
        List<ComponentInstanceInput> emptyPropsList = emptyList();
        if (this.componentInstancesInputs == null) {
            return emptyPropsList;
        }
        return this.componentInstances.stream().filter(ci -> ci.getName().equals(cmptInstanceName)).map(ComponentInstance::getUniqueId)
            .map(instanceId -> safeGetComponentInstanceEntity(instanceId, this.componentInstancesInputs)).findAny().orElse(emptyPropsList);
    }

    private <T> List<T> safeGetComponentInstanceEntity(String cmptInstanceId, Map<String, List<T>> instanceEntities) {
        List<T> emptyPropsList = emptyList();
        if (instanceEntities == null) {
            return emptyPropsList;
        }
        List<T> cmptInstanceProps = instanceEntities.get(cmptInstanceId);
        return cmptInstanceProps == null ? emptyPropsList : cmptInstanceProps;
    }

    public void setSpecificComponetTypeArtifacts(Map<String, ArtifactDefinition> specificComponentTypeArtifacts) {
        // Implement where needed
    }

    public String fetchGenericTypeToscaNameFromConfig() {
        return ConfigurationManager.getConfigurationManager().getConfiguration().getGenericAssetNodeTypes().get(this.assetType());
    }

    protected <A> Optional<A> getHeadOption(List<A> list) {
        return list == null || list.isEmpty() ? Optional.empty() : Optional.ofNullable(list.get(0));
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
        if (genericType == null) {
            derivedFromGenericType = null;
            derivedFromGenericVersion = null;
            return;
        }
        derivedFromGenericType = genericType.getToscaResourceName();
        derivedFromGenericVersion = genericType.getVersion();
    }

    public boolean isTopologyTemplate() {
        return ToscaElementTypeEnum.TOPOLOGY_TEMPLATE.getValue().equals(toscaType);
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
        return policies.values().stream().filter(policyPredicate).collect(Collectors.toList());
    }

    public List<GroupDefinition> resolveGroupsByMember(String instanceId) {
        if (groups == null) {
            return emptyList();
        }
        return groups.stream().filter(group -> group.containsInstanceAsMember(instanceId)).collect(Collectors.toList());
    }

    public String getActualComponentType() {
        return componentMetadataDefinition.getMetadataDataDefinition().getActualComponentType();
    }

    public Boolean isArchived() {
        return componentMetadataDefinition.getMetadataDataDefinition().isArchived();
    }

    public void setArchived(Boolean archived) {
        componentMetadataDefinition.getMetadataDataDefinition().setArchived(archived);
    }

    public Long getArchiveTime() {
        return componentMetadataDefinition.getMetadataDataDefinition().getArchiveTime();
    }

    public void setArchiveTime(Long archiveTime) {
        componentMetadataDefinition.getMetadataDataDefinition().setArchiveTime(archiveTime);
    }

    public Boolean isVspArchived() {
        return componentMetadataDefinition.getMetadataDataDefinition().isVspArchived();
    }

    public void setVspArchived(Boolean vspArchived) {
        componentMetadataDefinition.getMetadataDataDefinition().setVspArchived(vspArchived);
    }

    // supportability log method return map of component metadata teddy.h
    public Map<String, String> getComponentMetadataForSupportLog() {
        Map<String, String> componentMetadata = new HashMap<>();
        componentMetadata.put(ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_NAME, this.getName());
        componentMetadata.put(ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_VERSION, this.getVersion());
        componentMetadata.put(ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_UUID, this.getUUID());
        componentMetadata.put(ILogConfiguration.MDC_SUPPORTABLITY_CSAR_UUID, this.getCsarUUID());
        componentMetadata.put(ILogConfiguration.MDC_SUPPORTABLITY_CSAR_VERSION, this.getCsarVersion());
        return componentMetadata;
    }
}
