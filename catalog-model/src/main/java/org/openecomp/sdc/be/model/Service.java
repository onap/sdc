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

import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.config.CategoryBaseTypeConfig;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.datatypes.components.ComponentMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.components.ServiceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.InstantiationTypes;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElementTypeEnum;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class Service extends Component {

    private Map<String, ArtifactDefinition> serviceApiArtifacts;
    private Map<String, ForwardingPathDataDefinition> forwardingPaths;
    private String toscaServiceName;

    public Service() {
        super(new ServiceMetadataDefinition());
        this.getComponentMetadataDefinition().getMetadataDataDefinition().setComponentType(ComponentTypeEnum.SERVICE);
        this.setToscaType(ToscaElementTypeEnum.TOPOLOGY_TEMPLATE.getValue());
    }

    public Service(ComponentMetadataDefinition serviceMetadataDefinition) {
        super(serviceMetadataDefinition);
        ComponentMetadataDataDefinition metadataDataDefinition = this.getComponentMetadataDefinition().getMetadataDataDefinition();
        if (metadataDataDefinition != null) {
            metadataDataDefinition.setComponentType(ComponentTypeEnum.SERVICE);
        }
        this.setToscaType(ToscaElementTypeEnum.TOPOLOGY_TEMPLATE.getValue());
    }

    @Override
    public String getProjectCode() {
        return getServiceMetadataDefinition().getProjectCode();
    }

    @Override
    public void setProjectCode(String projectName) {
        getServiceMetadataDefinition().setProjectCode(projectName);
    }

    public ForwardingPathDataDefinition addForwardingPath(ForwardingPathDataDefinition forwardingPathDataDefinition) {
        if (forwardingPaths == null) {
            forwardingPaths = new HashMap<>();
        }
        return forwardingPaths.put(forwardingPathDataDefinition.getUniqueId(), forwardingPathDataDefinition);
    }

    public DistributionStatusEnum getDistributionStatus() {
        String distributionStatus = getServiceMetadataDefinition().getDistributionStatus();
        if (distributionStatus != null) {
            return DistributionStatusEnum.valueOf(distributionStatus);
        } else {
            return null;
        }
    }

    public void setDistributionStatus(DistributionStatusEnum distributionStatus) {
        if (distributionStatus != null) {
            getServiceMetadataDefinition().setDistributionStatus(distributionStatus.name());
        }
    }

    public void setEcompGeneratedNaming(Boolean ecompGeneratedNaming) {
        getServiceMetadataDefinition().setEcompGeneratedNaming(ecompGeneratedNaming);
    }

    public Boolean isEcompGeneratedNaming() {
        return getServiceMetadataDefinition().isEcompGeneratedNaming();
    }

    public String getNamingPolicy() {
        return getServiceMetadataDefinition().getNamingPolicy();
    }

    public void setNamingPolicy(String namingPolicy) {
        getServiceMetadataDefinition().setNamingPolicy(namingPolicy);
    }

    public String getEnvironmentContext() {
        return getServiceMetadataDefinition().getEnvironmentContext();
    }

    public void setEnvironmentContext(String environmentContext) {
        getServiceMetadataDefinition().setEnvironmentContext(environmentContext);
    }

    public String getServiceType() {
        return getServiceMetadataDefinition().getServiceType();
    }

    public void setServiceType(String serviceType) {
        getServiceMetadataDefinition().setServiceType(serviceType);
    }

    public String getServiceRole() {
        return getServiceMetadataDefinition().getServiceRole();
    }

    public void setServiceRole(String serviceRole) {
        getServiceMetadataDefinition().setServiceRole(serviceRole);
    }

    public String getInstantiationType() {
        return getServiceMetadataDefinition().getInstantiationType();
    }

    public void setInstantiationType(String instantiationType) {
        getServiceMetadataDefinition().setInstantiationType(instantiationType);
    }

    private ServiceMetadataDataDefinition getServiceMetadataDefinition() {
        return getMetadataDefinition();
    }

    public String getServiceFunction() {
        return getServiceMetadataDefinition().getServiceFunction();
    }

    public void setServiceFunction(String serviceFunction) {
        getServiceMetadataDefinition().setServiceFunction(serviceFunction);
    }

    public void validateAndSetInstantiationType() {
        if (this.getInstantiationType().equals(StringUtils.EMPTY)) {
            this.setInstantiationType(InstantiationTypes.A_LA_CARTE.getValue());
        }
    }

    @Override
    public String fetchGenericTypeToscaNameFromConfig() {
        return getHeadOption(this.getCategories()).map(category -> fetchToscaNameFromConfigBasedOnService(category.getName()))
            .orElse(super.fetchGenericTypeToscaNameFromConfig());
    }

    private String fetchToscaNameFromConfigBasedOnService(final String serviceCategory) {
        final Map<String, CategoryBaseTypeConfig> serviceNodeTypesConfig =
            ConfigurationManager.getConfigurationManager().getConfiguration().getServiceBaseNodeTypes();
        if (serviceNodeTypesConfig == null) {
            return null;
        }

        final CategoryBaseTypeConfig categoryBaseTypeConfig = serviceNodeTypesConfig.get(serviceCategory);
        if (categoryBaseTypeConfig == null || CollectionUtils.isEmpty(categoryBaseTypeConfig.getBaseTypes())) {
            return null;
        }

        return categoryBaseTypeConfig.getBaseTypes().get(0);
    }

    @Override
    public void setSpecificComponetTypeArtifacts(Map<String, ArtifactDefinition> specificComponentTypeArtifacts) {
        setServiceApiArtifacts(specificComponentTypeArtifacts);
    }

    public void setServiceVendorModelNumber(String serviceVendorModelNumber) {
        getServiceMetadataDefinition().setServiceVendorModelNumber(serviceVendorModelNumber);
    }

    public void setAbstract(Boolean isAbstract) {
        getMetadataDefinition().setIsAbstract(isAbstract);
    }

    public void setVendorName(String vendorName) {
        getMetadataDefinition().setVendorName(vendorName);
    }


    public void setTenant(String tenant) {
        getMetadataDefinition().setTenant(tenant);
    }

    public void setVendorRelease(String vendorRelease) {
        getMetadataDefinition().setVendorRelease(vendorRelease);
    }

    public boolean isSubstituteCandidate() {
        return getDerivedFromGenericType() != null;
    }

    private ServiceMetadataDataDefinition getMetadataDefinition() {
        return (ServiceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition();
    }
}
