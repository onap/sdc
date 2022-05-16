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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents an artifact type that is used for hard-coded type representation.
 * All artifacts must be configured in the SDC configuration file.
 */
@Getter
@AllArgsConstructor
public enum ArtifactTypeEnum {
    // @formatter:off
    AAI_SERVICE_MODEL("AAI_SERVICE_MODEL"),
    AAI_VF_INSTANCE_MODEL("AAI_VF_INSTANCE_MODEL"),
    AAI_VF_MODEL("AAI_VF_MODEL"),
    AAI_VF_MODULE_MODEL("AAI_VF_MODULE_MODEL"),
    ANSIBLE_PLAYBOOK("ANSIBLE_PLAYBOOK"),
    APPC_CONFIG("APPC_CONFIG"),
    BPEL("BPEL"),
    CHEF("CHEF"),
    CLOUD_TECHNOLOGY_SPECIFIC_ARTIFACT("CLOUD_TECHNOLOGY_SPECIFIC_ARTIFACT"),
    CONTROLLER_BLUEPRINT_ARCHIVE("CONTROLLER_BLUEPRINT_ARCHIVE"),
    DCAE_DOC("DCAE_DOC"),
    DCAE_EVENT("DCAE_EVENT"),
    DCAE_INVENTORY_BLUEPRINT("DCAE_INVENTORY_BLUEPRINT"),
    DCAE_INVENTORY_DOC("DCAE_INVENTORY_DOC"),
    DCAE_INVENTORY_EVENT("DCAE_INVENTORY_EVENT"),
    DCAE_INVENTORY_JSON("DCAE_INVENTORY_JSON"),
    DCAE_INVENTORY_POLICY("DCAE_INVENTORY_POLICY"),
    DCAE_INVENTORY_TOSCA("DCAE_INVENTORY_TOSCA"),
    DCAE_JSON("DCAE_JSON"),
    DCAE_POLICY("DCAE_POLICY"),
    DCAE_TOSCA("DCAE_TOSCA"),
    DG_XML("DG_XML"),
    ETSI_PACKAGE("ETSI_PACKAGE"),
    ASD_PACKAGE("ASD_PACKAGE"),
    GUIDE("GUIDE"),
    HEAT_ARTIFACT("HEAT_ARTIFACT"),
    HEAT_ENV("HEAT_ENV"),
    HEAT("HEAT"),
    HEAT_NESTED("HEAT_NESTED"),
    HEAT_NET("HEAT_NET"),
    HEAT_VOL("HEAT_VOL"),
    HELM("HELM"),
    ICON("ICON"),
    LIFECYCLE_OPERATIONS("LIFECYCLE_OPERATIONS"),
    MODEL_INVENTORY_PROFILE("MODEL_INVENTORY_PROFILE"),
    MODEL_QUERY_SPEC("MODEL_QUERY_SPEC"),
    MURANO_PKG("MURANO_PKG"),
    NETWORK_CALL_FLOW("NETWORK_CALL_FLOW"),
    ONBOARDED_PACKAGE("ONBOARDED_PACKAGE"),
    OTHER("OTHER"),
    PERFORMANCE_COUNTER("PERFORMANCE_COUNTER"),
    PLAN("PLAN"),
    PM_DICTIONARY("PM_DICTIONARY"),
    PNF_SW_INFORMATION("PNF_SW_INFORMATION"),
    PUPPET("PUPPET"),
    SHELL_SCRIPT("SHELL_SCRIPT"),
    SHELL("SHELL"),
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
    YANG_MODULE("YANG_MODULE"),
    YANG_XML("YANG_XML"),
    YANG("YANG");
    // @formatter:on

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

    /**
     * Gets the list of mandatory/base SDC artifacts. Those artifacts must be configured.
     *
     * @return A set of base artifact types
     */
    public static Set<ArtifactTypeEnum> getBaseArtifacts() {
        final List<ArtifactTypeEnum> artifactTypeEnums = Arrays
            .asList(AAI_SERVICE_MODEL, AAI_VF_INSTANCE_MODEL, AAI_VF_MODEL, AAI_VF_MODULE_MODEL, ANSIBLE_PLAYBOOK, APPC_CONFIG, BPEL, CHEF,
                CLOUD_TECHNOLOGY_SPECIFIC_ARTIFACT, CONTROLLER_BLUEPRINT_ARCHIVE, DCAE_DOC, DCAE_EVENT, DCAE_INVENTORY_BLUEPRINT, DCAE_INVENTORY_DOC,
                DCAE_INVENTORY_EVENT, DCAE_INVENTORY_JSON, DCAE_INVENTORY_POLICY, DCAE_INVENTORY_TOSCA, DCAE_JSON, DCAE_POLICY, DCAE_TOSCA, DG_XML,
                ETSI_PACKAGE, GUIDE, HEAT, HEAT_ARTIFACT, HEAT_ENV, HEAT_NESTED, HEAT_NET, HEAT_VOL, ICON, LIFECYCLE_OPERATIONS,
                MODEL_INVENTORY_PROFILE, MODEL_QUERY_SPEC, MURANO_PKG, NETWORK_CALL_FLOW, ONBOARDED_PACKAGE, OTHER, PERFORMANCE_COUNTER, PLAN,
                PM_DICTIONARY, PNF_SW_INFORMATION, PUPPET, SHELL, SHELL_SCRIPT, SNMP_POLL, SNMP_TRAP, TOSCA_CSAR, TOSCA_TEMPLATE,
                UCPE_LAYER_2_CONFIGURATION, VENDOR_LICENSE, VES_EVENTS, VF_LICENSE, VF_MODULES_METADATA, VNF_CATALOG, WORKFLOW, YANG, YANG_MODULE,
                YANG_XML);
        return new HashSet<>(artifactTypeEnums);
    }

    public static String findType(final String type) {
        for (final ArtifactTypeEnum ate : ArtifactTypeEnum.values()) {
            if (ate.getType().equalsIgnoreCase(type)) {
                return type;
            }
        }
        return null;
    }

    public static List<String> getAllTypes() {
        final List<String> types = new ArrayList<>();
        for (final ArtifactTypeEnum ate : ArtifactTypeEnum.values()) {
            types.add(ate.getType());
        }
        return types;
    }
}
