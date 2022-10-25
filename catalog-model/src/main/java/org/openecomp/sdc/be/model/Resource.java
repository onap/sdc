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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class Resource extends Component implements Serializable {

    /**
     * Please note that more than one "derivedFrom" resource is not currently supported by the app. The first list element is always addressed.
     */
    private List<String> derivedFrom;
    /**
     * The derivedList is a chain of derivedFrom. e.g. if resource C is derived from resource B that is derived from resource A - then A, B is the
     * "DerivedList" of resource C
     */
    private List<String> derivedList;
    private Map<String, String> derivedFromMapOfIdToName;
    private String toscaVersion;

    public Resource() {
        super(new ResourceMetadataDefinition());
        this.getComponentMetadataDefinition().getMetadataDataDefinition().setComponentType(ComponentTypeEnum.RESOURCE);
    }

    public Resource(ComponentMetadataDefinition componentMetadataDefinition) {
        super(componentMetadataDefinition);
        if (this.getComponentMetadataDefinition().getMetadataDataDefinition() == null) {
            this.getComponentMetadataDefinition().componentMetadataDataDefinition = new ResourceMetadataDataDefinition();
        }
        this.getComponentMetadataDefinition().getMetadataDataDefinition().setComponentType(ComponentTypeEnum.RESOURCE);
    }

    public Boolean isAbstract() {
        return getResourceMetadataDataDefinition().isAbstract();
    }

    public void setAbstract(Boolean isAbstract) {
        getResourceMetadataDataDefinition().setAbstract(isAbstract);
    }

    public String getCost() {
        return getResourceMetadataDataDefinition().getCost();
    }

    public void setCost(String cost) {
        getResourceMetadataDataDefinition().setCost(cost);
    }

    public String getLicenseType() {
        return getResourceMetadataDataDefinition().getLicenseType();
    }

    public void setLicenseType(String licenseType) {
        getResourceMetadataDataDefinition().setLicenseType(licenseType);
    }

    public String getToscaResourceName() {
        return getResourceMetadataDataDefinition().getToscaResourceName();
    }

    public void setToscaResourceName(String toscaResourceName) {
        getResourceMetadataDataDefinition().setToscaResourceName(toscaResourceName);
    }

    public ResourceTypeEnum getResourceType() {
        return getResourceMetadataDataDefinition().getResourceType();
    }

    public void setResourceType(ResourceTypeEnum resourceType) {
        getResourceMetadataDataDefinition().setResourceType(resourceType);
    }

    public String getVendorName() {
        return getResourceMetadataDataDefinition().getVendorName();
    }

    public void setVendorName(String vendorName) {
        getResourceMetadataDataDefinition().setVendorName(vendorName);
    }


    public String getTenant() {
        return getResourceMetadataDataDefinition().getTenant();
    }

    public void setTenant(String tenant) {
        getResourceMetadataDataDefinition().setTenant(tenant);
    }




    public String getVendorRelease() {
        return getResourceMetadataDataDefinition().getVendorRelease();
    }

    public void setVendorRelease(String vendorRelease) {
        getResourceMetadataDataDefinition().setVendorRelease(vendorRelease);
    }

    public String getResourceVendorModelNumber() {
        return getResourceMetadataDataDefinition().getResourceVendorModelNumber();
    }

    public void setResourceVendorModelNumber(String resourceVendorModelNumber) {
        getResourceMetadataDataDefinition()
            .setResourceVendorModelNumber(resourceVendorModelNumber);
    }

    private ResourceMetadataDataDefinition getResourceMetadataDataDefinition() {
        return (ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition();
    }

    public String getCsarVersionId() {
        return getResourceMetadataDataDefinition().getCsarVersionId();
    }

    public void setCsarVersionId(String csarVersionId) {
        getResourceMetadataDataDefinition().setCsarVersionId(csarVersionId);
    }



    @Override
    public String fetchGenericTypeToscaNameFromConfig() {
        return fetchToscaNameFromConfigBasedOnCategory().orElse(fetchToscaNameFromConfigBasedOnAssetType());
    }

    public String fetchToscaNameFromConfigBasedOnAssetType() {
        String result = super.fetchGenericTypeToscaNameFromConfig();
        if (null == result) {
            result = ConfigurationManager.getConfigurationManager().getConfiguration().getGenericAssetNodeTypes()
                .get(ResourceTypeEnum.VFC.getValue());
        }
        return result;
    }

    private Optional<String> fetchToscaNameFromConfigBasedOnCategory() {
        return getHeadOption(this.getCategories()).flatMap(category -> getHeadOption(category.getSubcategories())
            .map(subCategory -> fetchToscaNameFromConfigBasedOnCategory(category.getName(), subCategory.getName())));
    }

    private String fetchToscaNameFromConfigBasedOnCategory(final String resourceCategory, final String resourceSubCategory) {
        return Optional.ofNullable(ConfigurationManager.getConfigurationManager().getConfiguration().getResourceNodeTypes())
            .map(categoryNames -> categoryNames.get(resourceCategory)).map(subCategoryNames -> subCategoryNames.get(resourceSubCategory))
            .orElse(null);
    }

    @Override
    public String assetType() {
        return this.getResourceType().name();
    }

    @Override
    public boolean shouldGenerateInputs() {
        return !(this.getResourceType().isAtomicType());
    }

    @Override
    public boolean deriveFromGeneric() {
        return this.shouldGenerateInputs() || (derivedFrom != null && derivedFrom.contains(fetchGenericTypeToscaNameFromConfig()));
    }

    public Map<String, List<RequirementCapabilityRelDef>> groupRelationsFromCsarByInstanceName(Resource resource) {
        List<RequirementCapabilityRelDef> componentInstanceRelationsFromCsar = resource.getComponentInstancesRelations().stream()
            .filter(r -> !r.isOriginUI()).collect(Collectors.toList());
        Map<String, List<RequirementCapabilityRelDef>> relationsByInstanceId = MapUtil
            .groupListBy(componentInstanceRelationsFromCsar, RequirementCapabilityRelDef::getFromNode);
        return MapUtil.convertMapKeys(relationsByInstanceId, instId -> getInstanceInvariantNameFromInstanceId(resource, instId));
    }

    private String getInstanceInvariantNameFromInstanceId(Resource resource, String instId) {
        Optional<ComponentInstance> componentInstanceById = resource.getComponentInstanceById(instId);
        return componentInstanceById.isPresent() ? componentInstanceById.get().getInvariantName() : null;
    }
}
