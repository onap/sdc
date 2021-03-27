/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.asdctool.enums;

import static org.openecomp.sdc.asdctool.enums.SchemaZipFileEnum.SchemaZipConstants.DATA_IMPORT_LIST;
import static org.openecomp.sdc.asdctool.enums.SchemaZipFileEnum.SchemaZipConstants.EMPTY_IMPORT_LIST;
import static org.openecomp.sdc.asdctool.enums.SchemaZipFileEnum.SchemaZipConstants.RELATIONSHIPS_TYPES_IMPORT_LIST;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SchemaZipFileEnum {
    // @formatter:off
    DATA("data", "data-types", "dataTypes", "data_types", EMPTY_IMPORT_LIST),
    GROUPS("groups", "group-types", "groupTypes", "group_types", DATA_IMPORT_LIST),
    POLICIES("policies", "policy-types", "policyTypes", "policy_types", DATA_IMPORT_LIST),
    ANNOTATIONS("annotations", "annotation-types", "annotationTypes", "annotation_types", DATA_IMPORT_LIST),
    RELATIONSHIPS("relationships", "relationship-types", "relationshipTypes", "relationship_types", RELATIONSHIPS_TYPES_IMPORT_LIST),
    ARTIFACTS("artifacts", "artifact-types", "artifactTypes", "artifact_types", DATA_IMPORT_LIST),
    CAPABILITIES("capabilities", "capability-types", "capabilityTypes", "capability_types", DATA_IMPORT_LIST),
    INTERFACES("interfaces", "interface-lifecycle-types", "interfaceLifecycleTypes", "interface_types", DATA_IMPORT_LIST);
    // @formatter:off

    private final String fileName;
    private final String sourceFolderName;
    private final String sourceFileName;
    private final String collectionTitle;
    private final String[] importFileList;

    static class SchemaZipConstants {

        static final String[] EMPTY_IMPORT_LIST = new String[]{};
        static final String[] DATA_IMPORT_LIST = new String[]{"data.yml"};
        static final String[] RELATIONSHIPS_TYPES_IMPORT_LIST = new String[]{"capabilities.yml", "data.yml", "interfaces.yml"};
    }
}
