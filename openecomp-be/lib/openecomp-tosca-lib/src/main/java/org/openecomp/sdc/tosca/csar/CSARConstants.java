/*
 * Copyright © 2016-2017 European Support Limited
 * Copyright (C) 2019 Nordix Foundation.
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

import static com.google.common.collect.ImmutableSet.of;

import com.google.common.collect.ImmutableSet;

public class CSARConstants {

    public static final ImmutableSet<String> ELIGBLE_FOLDERS = of("Artifacts/","Definitions/",
            "Licenses/", "TOSCA-Metadata/");
    public static final String ARTIFACTS_FOLDER = "Artifacts";

    public static final String MAIN_SERVICE_TEMPLATE_MF_FILE_NAME = "MainServiceTemplate.mf";
    public static final String MAIN_SERVICE_TEMPLATE_YAML_FILE_NAME = "MainServiceTemplate.yaml";
    public static final String TOSCA_META_PATH_FILE_NAME = "TOSCA-Metadata/TOSCA.meta";
    public static final String TOSCA_META_FILE_VERSION_ENTRY = "TOSCA-Meta-File-Version";
    public static final String TOSCA_META_CSAR_VERSION_ENTRY = "CSAR-Version";
    public static final String TOSCA_META_CREATED_BY_ENTRY = "Created-By";
    public static final String TOSCA_META_ENTRY_DEFINITIONS ="Entry-Definitions";
    public static final String TOSCA_META_ETSI_ENTRY_MANIFEST ="ETSI-Entry-Manifest";
    public static final String TOSCA_META_ETSI_ENTRY_CHANGE_LOG ="ETSI-Entry-Change-Log";
    public static final String TOSCA_META_ETSI_ENTRY_TESTS = "ETSI-Entry-Tests";
    public static final String TOSCA_META_ETSI_ENTRY_LICENSES = "ETSI-Entry-Licenses";
    public static final String TOSCA_META_ETSI_ENTRY_CERTIFICATE = "ETSI-Entry-Certificate";
    public static final ImmutableSet<String> ELIGIBLE_FILES =
            of(MAIN_SERVICE_TEMPLATE_MF_FILE_NAME,MAIN_SERVICE_TEMPLATE_YAML_FILE_NAME);

    public static final String PNFD_PROVIDER = "pnfd_provider";
    public static final String PNFD_NAME = "pnfd_name";
    public static final String PNFD_RELEASE_DATE_TIME = "pnfd_release_date_time";
    public static final String PNFD_ARCHIVE_VERSION = "pnfd_archive_version";
    public static final ImmutableSet<String> MANIFEST_PNF_METADATA =
        of(PNFD_PROVIDER, PNFD_NAME, PNFD_RELEASE_DATE_TIME, PNFD_ARCHIVE_VERSION);

    public static final String VNF_PROVIDER_ID = "vnf_provider_id";
    public static final String VNF_PRODUCT_NAME = "vnf_product_name";
    public static final String VNF_RELEASE_DATE_TIME = "vnf_release_date_time";
    public static final String VNF_PACKAGE_VERSION = "vnf_package_version";
    public static final ImmutableSet<String> MANIFEST_VNF_METADATA =
            of(VNF_PROVIDER_ID, VNF_PRODUCT_NAME, VNF_RELEASE_DATE_TIME, VNF_PACKAGE_VERSION);

    public static final int MANIFEST_METADATA_LIMIT = 4;
    public static final String METADATA_MF_ATTRIBUTE = "metadata";
    public static final String SOURCE_MF_ATTRIBUTE = "Source";
    public static final String ALGORITHM_MF_ATTRIBUTE = "Algorithm";
    public static final String HASH_MF_ATTRIBUTE = "Hash";
    public static final String CMS_BEGIN = "----BEGIN CMS-----";
    public static final String CMD_END = "----END CMS-----";
    public static final String SEPARATOR_MF_ATTRIBUTE = ":";
    public static final String NON_MANO_MF_ATTRIBUTE = "non_mano_artifact_sets";
    public static final String TOSCA_META_ORIG_PATH_FILE_NAME = "TOSCA-Metadata/TOSCA.meta.original";

    public static final String TOSCA_META_FILE_VERSION = "1.0";
    public static final String CSAR_VERSION_1_0 = "1.0";
    public static final String CSAR_VERSION_1_1 = "1.1";
    public static final ImmutableSet<String> NON_FILE_IMPORT_ATTRIBUTES =
            ImmutableSet.of("repository", "namespace_uri", "namespace_prefix");
    public static final String TOSCA_TYPE_PNF = "pnf";
    public static final String TOSCA_TYPE_VNF = "vnf";
    public static final String TOSCA_MANIFEST_FILE_EXT = "mf";

    private CSARConstants() {

    }
}
