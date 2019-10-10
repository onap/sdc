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

package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation;

import java.nio.file.Path;
import java.nio.file.Paths;

class TestConstants {

    public static final String SAMPLE_DEFINITION_IMPORT_FILE_PATH = "Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.yaml";
    public static final String SAMPLE_SOURCE = "Artifacts/Deployment/Events/RadioNode_pnf_v1.yaml";
    public static final Path SAMPLE_DEFINITION_FILE_PATH = Paths.get("validation.files/definition/sampleDefinitionFile.yaml");
    public static final String TOSCA_DEFINITION_FILEPATH = "Definitions/MainServiceTemplate.yaml";
    public static final String TOSCA_MANIFEST_FILEPATH = "Definitions/MainServiceTemplate.mf";
    public static final String TOSCA_CHANGELOG_FILEPATH = "Artifacts/changeLog.text";
    public static final Path EMPTY_YAML_FILE_PATH = Paths.get("validation.files/empty.yaml");
    public static final Path INVALID_YAML_FILE_PATH = Paths.get("validation.files/invalid.yaml");

    private TestConstants() {

    }
}
