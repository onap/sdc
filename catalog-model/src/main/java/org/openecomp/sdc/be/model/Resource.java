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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.InterfaceInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;

@Getter
@Setter
public class Resource extends Component {

    /**
     * Please note that more than one "derivedFrom" resource is not currently supported by the app. The first list
     * element is always addressed.
     */
    private List<String> derivedFrom;

    /**
     * The derivedList is a chain of derivedFrom. e.g. if resource C is derived from resource B that is derived from
     * resource A - then A, B is the "DerivedList" of resource C
     */
    private List<String> derivedList;

    private List<PropertyDefinition> attributes;

    private Map<String, InterfaceInstanceDataDefinition> instInterfaces;

    private List<String> defaultCapabilities;
    private String toscaVersion;

    public Resource() {
        super(new ResourceMetadataDefinition());
        this.getComponentMetadataDefinition().getMetadataDataDefinition()
                .setComponentType(ComponentTypeEnum.RESOURCE);
    }

    public Resource(ComponentMetadataDefinition componentMetadataDefinition) {
        super(componentMetadataDefinition);
        if (this.getComponentMetadataDefinition().getMetadataDataDefinition() == null) {
            this.getComponentMetadataDefinition().componentMetadataDataDefinition = new ResourceMetadataDataDefinition();
        }
        this.getComponentMetadataDefinition().getMetadataDataDefinition().setComponentType(ComponentTypeEnum.RESOURCE);
    }

    public List<PropertyDefinition> getProperties() {
        return properties;
    }

    public void setProperties(List<PropertyDefinition> properties) {
        this.properties = properties;
    }

    public Boolean isAbstract() {
        return ((ResourceMetadataDataDefinition) getComponentMetadataDefinition()
                .getMetadataDataDefinition())
                .isAbstract();
    }

    public void setAbstract(Boolean isAbstract) {
        ((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition())
                .setAbstract(isAbstract);
    }

    public String getCost() {
        return ((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition())
                .getCost();
    }

    public void setCost(String cost) {
        ((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition()).setCost(cost);
    }

    public String getLicenseType() {
        return ((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition())
                .getLicenseType();
    }

    public void setLicenseType(String licenseType) {
        ((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition())
                .setLicenseType(licenseType);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + super.hashCode();
        result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
        result = prime * result + ((defaultCapabilities == null) ? 0 : defaultCapabilities.hashCode());
        result = prime * result + ((derivedFrom == null) ? 0 : derivedFrom.hashCode());
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
        result = prime * result + ((derivedList == null) ? 0 : derivedList.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Resource resource = (Resource) o;
        return Objects.equals(derivedFrom, resource.derivedFrom) &&
                Objects.equals(derivedList, resource.derivedList) &&
                Objects.equals(properties, resource.properties) &&
                Objects.equals(attributes, resource.attributes) &&
                Objects.equals(defaultCapabilities, resource.defaultCapabilities);
    }


    @Override
    public String toString() {
        return "Resource [derivedFrom=" + derivedFrom + ", properties=" + properties + ", attributes=" + attributes
                + ", defaultCapabilities=" + defaultCapabilities + ", additionalInformation=" + additionalInformation
                + "Metadata [" + getComponentMetadataDefinition().getMetadataDataDefinition().toString() + "]";
    }

    public String getToscaResourceName() {
        return ((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition())
                .getToscaResourceName();
    }

    public void setToscaResourceName(String toscaResourceName) {
        ((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition())
                .setToscaResourceName(toscaResourceName);
    }

    public ResourceTypeEnum getResourceType() {
        return ((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition())
                .getResourceType();
    }

    public void setResourceType(ResourceTypeEnum resourceType) {
        ((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition())
                .setResourceType(resourceType);
    }

    public String getVendorName() {
        return ((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition())
                .getVendorName();
    }

    public void setVendorName(String vendorName) {
        ((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition())
            .setVendorName(vendorName);
    }

    public String getVendorRelease() {
        return ((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition())
                .getVendorRelease();
    }

    public void setVendorRelease(String vendorRelease) {
        ((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition())
            .setVendorRelease(vendorRelease);
    }

    public String getResourceVendorModelNumber() {
        return ((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition())
                .getResourceVendorModelNumber();
    }

    public void setResourceVendorModelNumber(String resourceVendorModelNumber) {
        ((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition()).
            setResourceVendorModelNumber(resourceVendorModelNumber);
    }

    @Override
    public String fetchGenericTypeToscaNameFromConfig() {
        String result = super.fetchGenericTypeToscaNameFromConfig();
        if (null == result)
            result = ConfigurationManager.getConfigurationManager().getConfiguration().getGenericAssetNodeTypes().get(ResourceTypeEnum.VFC.getValue());
        return result;
    }

    @Override
    public String assetType() {
        return this.getResourceType().name();
    }

    @Override
    public boolean shouldGenerateInputs(){
        return !(this.getResourceType().isAtomicType());
    }

    @Override
    public boolean deriveFromGeneric() {
        return this.shouldGenerateInputs() || (derivedFrom != null && derivedFrom.contains(fetchGenericTypeToscaNameFromConfig()));
    }

    public Map<String, List<RequirementCapabilityRelDef>> groupRelationsFromCsarByInstanceName(Resource resource) {
        List<RequirementCapabilityRelDef> componentInstanceRelationsFromCsar = resource.getComponentInstancesRelations().stream().filter(r->!r.isOriginUI()).collect(Collectors.toList());
        Map<String, List<RequirementCapabilityRelDef>> relationsByInstanceId = MapUtil.groupListBy(componentInstanceRelationsFromCsar, RequirementCapabilityRelDef::getFromNode);
        return MapUtil.convertMapKeys(relationsByInstanceId, instId -> getInstanceInvariantNameFromInstanceId(resource, instId));
    }

    private String getInstanceNameFromInstanceId(Resource resource, String instId) {
        Optional<ComponentInstance> componentInstanceById = resource.getComponentInstanceById(instId);
        return componentInstanceById.isPresent() ? componentInstanceById.get().getName() : null;
    }

    private String getInstanceInvariantNameFromInstanceId(Resource resource, String instId) {
        Optional<ComponentInstance> componentInstanceById = resource.getComponentInstanceById(instId);
        return componentInstanceById.isPresent() ? componentInstanceById.get().getInvariantName() : null;
    }
}
