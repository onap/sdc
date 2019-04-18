/*-
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 */

package org.openecomp.sdc.be.datatypes.enums;

public enum ComponentFieldsEnum {

    PROPERTIES("properties"),
    INPUTS("inputs"),
    USERS("users"),
    GROUPS("groups"),
    NON_EXCLUDED_GROUPS("nonExcludedGroups"),
    COMPONENT_INSTANCES("componentInstances"),
    COMPONENT_INSTANCES_PROPERTIES("componentInstancesProperties"),
    CAPABILITIES("capabilities"),
    REQUIREMENTS("requirements"),
    ALL_VERSIONS("allVersions"),
    ADDITIONAL_INFORMATION("additionalInformation"),
    ARTIFACTS("artifacts"),
    INTERFACES("interfaces"),
    DERIVED_FROM("derivedFrom"),
    ATTRIBUTES("attributes"),
    COMPONENT_INSTANCES_ATTRIBUTES("componentInstancesAttributes"),
    COMPONENT_INSTANCE_INPUTS("componentInstancesInputs"),
    COMPONENT_INSTANCE_RELATION("componentInstancesRelations"),
    DEPLOYMENT_ARTIFACTS("deploymentArtifacts"),
    TOSCA_ARTIFACTS("toscaArtifacts"),
    SERVICE_API_ARTIFACTS("serviceApiArtifacts"),
    METADATA("metadata"),
    CATEGORIES("categories"),
    INSTANCE_CAPABILTY_PROPERTIES("instanceCapabiltyProperties"),
    FORWARDING_PATHS("forwardingPaths"),
    POLICIES("policies"),
    NON_EXCLUDED_POLICIES("nonExcludedPolicies"),
    NODE_FILTER("nodeFilter"),
    COMPONENT_INSTANCES_INTERFACES("componentInstancesInterfaces"),
    DATA_TYPES("dataTypes")
    ;


    private String value;

    private ComponentFieldsEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }


    public static ComponentFieldsEnum findByValue(String value) {
        ComponentFieldsEnum ret = null;
        for (ComponentFieldsEnum curr : ComponentFieldsEnum.values()) {
            if (curr.getValue().equals(value)) {
                ret = curr;
                return ret;
            }
        }
        return ret;
    }
}
