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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentFieldsEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;

@Getter
@Setter
@NoArgsConstructor
public class ComponentParametersView {

    private boolean ignoreUsers = false;
    private boolean ignoreGroups = false;
    private boolean ignoreComponentInstances = false;
    private boolean ignoreComponentInstancesProperties = false;
    private boolean ignoreComponentInstancesAttributes = false;
    private boolean ignoreProperties = false;
    private boolean ignoreAttributes = false;
    private boolean ignoreCapabilities = false;
    private boolean ignoreRequirements = false;
    private boolean ignoreCategories = false;
    private boolean ignoreAllVersions = false;
    private boolean ignoreAdditionalInformation = false;
    private boolean ignoreArtifacts = false;
    private boolean ignoreInterfaces = false;
    private boolean ignoreInterfaceInstances = false;
    private boolean ignoreComponentInstancesInterfaces = false;
    private boolean ignoreDerivedFrom = false;
    private boolean ignoreInputs = false;
    private boolean ignoreOutputs = false;
    private boolean ignoreComponentInstancesInputs = false;
    private boolean ignoreComponentInstancesOutputs = false;
    private boolean ignoreCapabiltyProperties = false;
    private boolean ignoreServicePath = true;
    private boolean ignorePolicies = false;
    private boolean ignoreNodeFilterRequirements = false;
    private boolean ignoreNodeFilter = false;
    private boolean ignoreSubstitutionFilter = false;
    private boolean ignoreDataType = false;

    public ComponentParametersView(boolean setAllToIgnore) {
        this();
        if (setAllToIgnore) {
            this.disableAll();
        }
    }

    public ComponentParametersView(List<String> filters) {
        this(true);
        for (String fieldName : filters) {
            switch (ComponentFieldsEnum.findByValue(fieldName)) {
                case PROPERTIES:
                    this.setIgnoreProperties(false);
                    break;
                case INPUTS:
                    this.setIgnoreInputs(false);
                    break;
                case OUTPUTS:
                    this.setIgnoreOutputs(false);
                    break;
                case USERS:
                    this.setIgnoreUsers(false);
                    break;
                case CATEGORIES:
                    this.setIgnoreCategories(false);
                    break;
                case METADATA:
                    this.setIgnoreUsers(false);
                    this.setIgnoreCategories(false);
                    this.setIgnoreAllVersions(false);
                    this.setIgnoreDerivedFrom(false);
                    break;
                case GROUPS:
                case NON_EXCLUDED_GROUPS:
                    this.setIgnoreGroups(false);
                    break;
                case COMPONENT_INSTANCES:
                    this.setIgnoreComponentInstances(false);
                    this.setIgnoreCapabilities(false);
                    this.setIgnoreRequirements(false);
                    this.setIgnoreNodeFilter(false);
                    this.setIgnoreSubstitutionFilter(false);
                    this.setIgnoreCapabiltyProperties(false);
                    this.setIgnoreInterfaces(false);
                    this.setIgnoreComponentInstancesInterfaces(false);
                    break;
                case COMPONENT_INSTANCES_PROPERTIES:
                    this.setIgnoreComponentInstances(false); //we need this in order to get the calculate capabilities requirements
                    this.setIgnoreComponentInstancesProperties(false);
                    break;
                case CAPABILITIES:
                    this.setIgnoreComponentInstances(false);//we need this in order to get the calculate capabilities requirements
                    this.setIgnoreCapabilities(false);
                    this.setIgnoreCapabiltyProperties(false);
                    break;
                case REQUIREMENTS:
                    this.setIgnoreComponentInstances(false);
                    this.setIgnoreRequirements(false);
                    break;
                case ALL_VERSIONS:
                    this.setIgnoreAllVersions(false);
                    break;
                case ADDITIONAL_INFORMATION:
                    this.setIgnoreAdditionalInformation(false);
                    break;
                case ARTIFACTS:
                case DEPLOYMENT_ARTIFACTS:
                case TOSCA_ARTIFACTS:
                case SERVICE_API_ARTIFACTS:
                    this.setIgnoreArtifacts(false);
                    break;
                case INTERFACES:
                    this.setIgnoreInterfaces(false);
                    break;
                case DERIVED_FROM:
                    this.setIgnoreDerivedFrom(false);
                    break;
                case ATTRIBUTES:
                    this.setIgnoreAttributes(false);
                    break;
                case COMPONENT_INSTANCES_ATTRIBUTES:
                    this.setIgnoreComponentInstances(false);
                    this.setIgnoreComponentInstancesAttributes(false);
                    break;
                case COMPONENT_INSTANCE_INPUTS:
                    this.setIgnoreComponentInstances(false);
                    this.setIgnoreComponentInstancesInputs(false);
                    break;
                case COMPONENT_INSTANCE_OUTPUTS:
                    this.setIgnoreComponentInstances(false);
                    this.setIgnoreComponentInstancesOutputs(false);
                    break;
                case INSTANCE_CAPABILTY_PROPERTIES:
                    this.setIgnoreCapabiltyProperties(false);
                    break;
                case FORWARDING_PATHS:
                    this.setIgnoreServicePath(false);
                    break;
                case POLICIES:
                case NON_EXCLUDED_POLICIES:
                    this.setIgnorePolicies(false);
                    break;
                case NODE_FILTER:
                    this.setIgnoreNodeFilterRequirements(false);
                    this.setIgnoreNodeFilter(false);
                    break;
                case SUBSTITUTION_FILTER:
                    this.setIgnoreSubstitutionFilter(false);
                    break;
                case COMPONENT_INSTANCES_INTERFACES:
                    this.setIgnoreComponentInstances(false);
                    this.setIgnoreInterfaces(false);
                    this.setIgnoreComponentInstancesInterfaces(false);
                    break;
                case DATA_TYPES:
                    this.setIgnoreDataType(false);
                    break;
                default:
                    break;
            }
        }
    }
    ///////////////////////////////////////////////////////////////

    // When adding new member, please update the filter method.

    ///////////////////////////////////////////////////////////////
    public Component filter(Component component, ComponentTypeEnum componentType) {
        if (ignoreUsers) {
            component.setCreatorUserId(null);
            component.setCreatorFullName(null);
            component.setLastUpdaterUserId(null);
            component.setLastUpdaterFullName(null);
        }
        if (ignoreGroups) {
            component.setGroups(null);
        }
        if (ignoreComponentInstances) {
            component.setComponentInstances(null);
            component.setComponentInstancesRelations(null);
        }
        if (ignoreComponentInstancesProperties) {
            component.setComponentInstancesProperties(null);
        }
        if (ignoreProperties && componentType == ComponentTypeEnum.RESOURCE) {
            ((Resource) component).setProperties(null);
        }
        if (ignoreAttributes) {
            component.setAttributes(null);
        }
        if (ignoreCapabilities) {
            component.setCapabilities(null);
        }
        if (ignoreRequirements) {
            component.setRequirements(null);
        }
        if (ignoreCategories) {
            component.setCategories(null);
        }
        if (ignoreAllVersions) {
            component.setAllVersions(null);
        }
        if (ignoreAdditionalInformation && componentType == ComponentTypeEnum.RESOURCE) {
            ((Resource) component).setAdditionalInformation(null);
        }
        if (ignoreArtifacts) {
            component.setArtifacts(null);
            component.setSpecificComponetTypeArtifacts(null);
            component.setDeploymentArtifacts(null);
            component.setToscaArtifacts(null);
        }
        if (ignoreNodeFilterRequirements) {
            component.setNodeFilterComponents(null);
        }
        if (ignoreInterfaces && ignoreInterfaceInstances && componentType == ComponentTypeEnum.RESOURCE) {
            component.setInterfaces(null);
        }
        if (ignoreDerivedFrom && componentType == ComponentTypeEnum.RESOURCE) {
            ((Resource) component).setDerivedFrom(null);
        }
        if (ignoreComponentInstancesAttributes) {
            component.setComponentInstancesAttributes(null);
        }
        if (ignoreInputs) {
            component.setInputs(null);
        }
        if (ignoreOutputs) {
            component.setOutputs(null);
        }
        if (ignoreComponentInstancesInputs) {
            component.setComponentInstancesInputs(null);
        }
        if (ignoreComponentInstancesOutputs) {
            component.setComponentInstancesOutputs(null);
        }
        if (ignoreServicePath && componentType == ComponentTypeEnum.SERVICE) {
            ((Service) component).setForwardingPaths(null);
        }
        if (ignoreNodeFilter) {
            component.setNodeFilterComponents(null);
        }
        if (ignoreSubstitutionFilter) {
            component.setSubstitutionFilter(null);
        }
        if (ignoreDataType) {
            component.setDataTypes(null);
        }
        return component;
    }

    public void disableAll() {
        ignoreUsers = true;
        ignoreGroups = true;
        ignoreComponentInstances = true;
        ignoreComponentInstancesProperties = true;
        ignoreComponentInstancesAttributes = true;
        ignoreProperties = true;
        ignoreAttributes = true;
        ignoreCapabilities = true;
        ignoreRequirements = true;
        ignoreCategories = true;
        ignoreAllVersions = true;
        ignoreAdditionalInformation = true;
        ignoreArtifacts = true;
        ignoreInterfaces = true;
        ignoreInterfaceInstances = true;
        ignoreComponentInstancesInterfaces = true;
        ignoreDerivedFrom = true;
        ignoreInputs = true;
        ignoreOutputs = true;
        ignoreComponentInstancesInputs = true;
        ignoreComponentInstancesOutputs = true;
        ignoreCapabiltyProperties = true;
        ignoreServicePath = true;
        ignorePolicies = true;
        ignoreNodeFilterRequirements = true;
        ignoreNodeFilter = true;
        ignoreSubstitutionFilter = true;
        ignoreDataType = true;
    }

    public void setIgnoreGroups(boolean ignoreGroups) {
        this.ignoreGroups = ignoreGroups;
        if (!ignoreGroups) {
            this.ignoreCapabiltyProperties = ignoreGroups;
            this.ignoreCapabilities = ignoreGroups;
        }
    }

    public JsonParseFlagEnum detectParseFlag() {
        JsonParseFlagEnum parseFlag;
        if (isIgnoreComponentInstances()) {
            parseFlag = JsonParseFlagEnum.ParseMetadata;
        } else {
            parseFlag = JsonParseFlagEnum.ParseAll;
        }
        return parseFlag;
    }
}
