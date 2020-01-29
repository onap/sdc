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

package org.openecomp.sdc.common.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents an artifact type that is used for hard-coded type representation. All artifacts must be configured in the
 * SDC configuration file.
 */
@Getter
@AllArgsConstructor
public enum ArtifactTypeEnum {
    AAI_SERVICE_MODEL("AAI_SERVICE_MODEL"),
    ANSIBLE_PLAYBOOK("ANSIBLE_PLAYBOOK"),
    CLOUD_TECHNOLOGY_SPECIFIC_ARTIFACT("CLOUD_TECHNOLOGY_SPECIFIC_ARTIFACT"),
    DCAE_INVENTORY_JSON("DCAE_INVENTORY_JSON"),
    DCAE_INVENTORY_TOSCA("DCAE_INVENTORY_TOSCA"),
    GUIDE("GUIDE"),
    HEAT("HEAT"),
    HEAT_ARTIFACT("HEAT_ARTIFACT"),
    HEAT_ENV("HEAT_ENV"),
    HEAT_NESTED("HEAT_NESTED"),
    HEAT_NET("HEAT_NET"),
    HEAT_VOL("HEAT_VOL"),
    LIFECYCLE_OPERATIONS("LIFECYCLE_OPERATIONS"),
    MODEL_INVENTORY_PROFILE("MODEL_INVENTORY_PROFILE"),
    MODEL_QUERY_SPEC("MODEL_QUERY_SPEC"),
    OTHER("OTHER"),
    PM_DICTIONARY("PM_DICTIONARY"),
    PUPPET("PUPPET"),
    SNMP_POLL("SNMP_POLL"),
    SNMP_TRAP("SNMP_TRAP"),
    TOSCA_CSAR("TOSCA_CSAR"),
    TOSCA_TEMPLATE("TOSCA_TEMPLATE"),
    UCPE_LAYER_2_CONFIGURATION("UCPE_LAYER_2_CONFIGURATION"),
    VENDOR_LICENSE("VENDOR_LICENSE"),
    VES_EVENTS("VES_EVENTS"),
    VF_LICENSE("VF_LICENSE"),
    VF_MODULES_METADATA("VF_MODULES_METADATA"),
    VNF_CATALOG("VNF_CATALOG"),
    WORKFLOW("WORKFLOW"),
    YANG_XML("YANG_XML"),
    CHEF("CHEF"), YANG("YANG"), SHELL_SCRIPT("SHELL_SCRIPT"), SHELL("SHELL"), ICON("ICON"), UNKNOWN("UNKNOWN"), DG_XML("DG_XML"), MURANO_PKG("MURANO_PKG"),
    NETWORK_CALL_FLOW("NETWORK_CALL_FLOW"), BPEL("BPEL"), APPC_CONFIG("APPC_CONFIG"), PERFORMANCE_COUNTER("PERFORMANCE_COUNTER"), CONTROLLER_BLUEPRINT_ARCHIVE("CONTROLLER_BLUEPRINT_ARCHIVE"),
    DCAE_TOSCA("DCAE_TOSCA"), DCAE_JSON("DCAE_JSON"), DCAE_POLICY("DCAE_POLICY"), DCAE_DOC("DCAE_DOC"), DCAE_EVENT("DCAE_EVENT"),
    DCAE_INVENTORY_POLICY("DCAE_INVENTORY_POLICY"), DCAE_INVENTORY_DOC("DCAE_INVENTORY_DOC"), DCAE_INVENTORY_BLUEPRINT("DCAE_INVENTORY_BLUEPRINT"), DCAE_INVENTORY_EVENT("DCAE_INVENTORY_EVENT"),
    AAI_VF_MODEL("AAI_VF_MODEL"), AAI_VF_MODULE_MODEL("AAI_VF_MODULE_MODEL"), AAI_VF_INSTANCE_MODEL("AAI_VF_INSTANCE_MODEL"),
    PLAN("PLAN"), YANG_MODULE("YANG_MODULE"), ONBOARDED_PACKAGE("ONBOARDED_PACKAGE"), PNF_SW_INFORMATION("PNF_SW_INFORMATION");


    private final String type;

    /**
     * Parse a string to a {@link ArtifactTypeEnum}, ignoring the case.
     *
     * @param type the artifact type
     * @return The artifact type if its represented in the present enum, otherwise {@code null}.
     */
    public static ArtifactTypeEnum parse(final String type) {
        for (final ArtifactTypeEnum artifactType : ArtifactTypeEnum.values()) {
            if (artifactType.getType().equalsIgnoreCase(type)) {
                return artifactType;
            }
        }
        return null;
    }

}
