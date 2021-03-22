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
package org.openecomp.sdc.tosca.datatypes;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * TOSCA Simple Profile in YAML Version 1.3.
 * <p>
 * OASIS Standard (26 February 2020).
 * <p>
 * https://docs.oasis-open.org/tosca/TOSCA-Simple-Profile-YAML/v1.3/TOSCA-Simple-Profile-YAML-v1.3.html
 */
@Getter
@AllArgsConstructor
public enum ToscaFunctions {
    // @formatter:off
    // Intrinsic functions, section 4.3 in TOSCA 1.3
    CONCAT("concat"),
    JOIN("join"),   //  since 1.2
    TOKEN("token"),
    // Property functions, section 4.4 in TOSCA 1.3
    GET_INPUT("get_input"), GET_PROPERTY("get_property"),
    // Attribute functions, section 4.5 in TOSCA 1.3
    GET_ATTRIBUTE("get_attribute"),
    // Operation functions, section 4.6 in TOSCA 1.3
    GET_OPERATION_OUTPUT("get_operation_output"),
    // Navigation functions, section 4.7 in TOSCA 1.3
    GET_NODES_OF_TYPE("get_nodes_of_type"),
    // Artifact functions, section 4.8 in TOSCA 1.3
    GET_ARTIFACT("get_artifact"),
    // non TOSCA-compliant function
    GET_POLICY("get_policy");
    // @formatter:of

    private String functionName;
}
