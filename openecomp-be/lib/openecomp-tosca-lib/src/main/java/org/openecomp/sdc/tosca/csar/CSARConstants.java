/*
 * Copyright © 2016-2017 European Support Limited
 *
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

package org.openecomp.sdc.tosca.csar;

import com.google.common.collect.ImmutableSet;

import static com.google.common.collect.ImmutableSet.of;
public class CSARConstants {

    public static final ImmutableSet<String> ELIGBLE_FOLDERS = of("Artifacts/","Definitions/",
            "Licenses/", "TOSCA-Metadata/");

    public static final String MAIN_SERVICE_TEMPLATE_MF_FILE_NAME = "MainServiceTemplate.mf";
    public static final String MAIN_SERVICE_TEMPLATE_YAML_FILE_NAME = "MainServiceTemplate.yaml";
    public static final String TOSCA_META_PATH_FILE_NAME="TOSCA-Metadata/TOSCA.meta";
    public static final String TOSCA_META_ENTRY_DEFINITIONS="Entry-Definitions";
    public static final String TOSCA_META_ENTRY_MANIFEST="Entry-Manifest";
    public static final String TOSCA_META_ENTRY_CHANGE_LOG="Entry-Change-Log";
    public static final ImmutableSet<String> ELIGIBLE_FILES =
            of(MAIN_SERVICE_TEMPLATE_MF_FILE_NAME,MAIN_SERVICE_TEMPLATE_YAML_FILE_NAME);

    public static final String METADATA_MF_ATTRIBUTE = "metadata";
    public static final String SOURCE_MF_ATTRIBUTE = "Source";
    public static final String SEPERATOR_MF_ATTRIBUTE = ":";
    public static final String NON_MANO_MF_ATTRIBUTE = "non_mano_artifact_sets";
    public static final String TOSCA_META_ORIG_PATH_FILE_NAME="TOSCA-Metadata/TOSCA.meta.original";

    private CSARConstants() {

    }
}
