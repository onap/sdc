/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2020 Nordix Foundation
 * ================================================================================
 */
package org.openecomp.sdc.common.log.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LoggerSupportabilityActions {
    IMPORT_CSAR("CREATE RESOURCE FROM CSAR"),
    CREATE_RESOURCE_FROM_YAML("CREATE RESOURCE FROM YAML"),
    CREATE_RI_AND_RELATIONS("CREATE RI AND RELATIONS"),
    CREATE_NODES_AND_CVFCS("ADD NODES AND CVFCS"),
    CREATE_ARTIFACTS("ADD ARTIFACTS"),
    CREATE_SERVICE("CREATE SERVICE"),
    CREATE_RESOURCE("CREATE RESOURCE"),
    CREATE_PROPERTIES("ADD PROPERTIES"),
    CREATE_INPUTS("ADD INPUTS"),
    CREATE_POLICIES("ADD POLICIES"),
    CREATE_RELATIONS("ADD RELATIONS"),
    CREATE_CAPABILITY_REQUIREMENTS("CREATE CAPABILITY REQUIREMENTS"),
    MERGE("MERGE"),
    PROPERTY_ASSIGNMENT("PROPERTY ASSIGNMENT"),
    CREATE_INSTANCE("CREATE INSTANCE"),
    CREATE_RELATION("ADD RELATION"),
    CREATE_GROUP_POLICY("ADD GROUP POLICY"),
    CREATE_GROUPS("ADD GROUPS"),
    UPDATE_PROPERTY_IN_GROUP_POLICY("UPDATE PROPERTY IN GROUP POLICY"),
    UPDATE_CAPABILITY("UPDATE CAPABILITY"),
    PROPERTY("ADD PROPERTY"),
    UPLOAD_DOWNLOAD_ARTIFACT("UPLOAD/DOWNLOAD ARTIFACT"),
    LIFECYCLE("LIFECYCLE"),
    DISTRIBUTION("DISTRIBUTION"),
    UPDATE_CATALOG("UPDATE CATALOG"),
    ARCHIVE("ARCHIVE"),
    TENANT_ISOLATION("TENANT ISOLATION"),
    DOWNLOAD_ARTIFACTS("DOWNLOAD ARTIFACTS"),
    UPDATE_HEAT("UPDATE HEAT"),
    PARAMETERS("PARAMETERS"),
    CHANGELIFECYCLESTATE("CHANGE LIFECYCLE STATE"),
    VALIDATE_NAME("VALIDATE COMPONENT NAME"),
    DELETE_COMPONENT_INSTANCE_ARTIFACT("DELETE COMPONENT INSTANCE ARTIFACT"),
    DELETE_SERVICE("DELETE SERVICE"),
    DELETE_RESOURCE("DELETE RESOURCE"),
    UPDATE_RESOURCE("UPDATE RESOURCE"),
    UPDATE_COMPONENT_INSTANCE("UPDATE COMPONENT INSTANCE"),
    DELETE_COMPONENT_INSTANCE("DELETE COMPONENT INSTANCE"),
    UPDATE_PROPERTIES("UPDATE PROPERTIES"),
    RESTORE_FROM_ARCHIVE("RESTORE FROM ARCHIVE"),
    UPDATE_INPUTS("UPDATE INPUTS"),
    DELETE_INPUTS("DELETE INPUTS"),
    ASSOCIATE_RI_TO_RI("ASSOCIATE RI TO RI"),
    UN_ASSOCIATE_RI_TO_RI("UN ASSOCIATE RI TO RI"),
    UPDATE_ARTIFACT("UPDATE ARTIFACT"),
    GENERATE_CSAR("GENERATE CSAR"),
    GENERATE_TOSCA("GENERATE TOSCA"),
    UPDATE_GROUP_MEMBERS("UPDATE GROUP MEMBERS"),
    UPDATE_INSTANCE_CAPABILITY_PROPERTY("UPDATE INSTANCE CAPABILITY PROPERTY"),
    UPDATE_INSTANCE_REQUIREMENT("UPDATE INSTANCE REQUIREMENT"),
    UPDATE_POLICY_TARGET("UPDATE POLICY TARGET"),
    UPDATE_POLICIES_PROPERTIES("UPDATE POLICIES PROPERTIES");

    private final String name;

}
