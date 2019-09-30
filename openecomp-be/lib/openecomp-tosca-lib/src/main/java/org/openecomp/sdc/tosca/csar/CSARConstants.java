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
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.PNFD_ARCHIVE_VERSION;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.PNFD_NAME;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.PNFD_PROVIDER;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.PNFD_RELEASE_DATE_TIME;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.VNF_PACKAGE_VERSION;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.VNF_PRODUCT_NAME;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.VNF_PROVIDER_ID;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.VNF_RELEASE_DATE_TIME;

import com.google.common.collect.ImmutableSet;

public class CSARConstants {

    public static final ImmutableSet<String> ELIGBLE_FOLDERS = of("Artifacts/","Definitions/",
            "Licenses/", "TOSCA-Metadata/");
    public static final String ARTIFACTS_FOLDER = "Artifacts";
    public static final String MAIN_SERVICE_TEMPLATE_MF_FILE_NAME = "MainServiceTemplate.mf";
    public static final String MAIN_SERVICE_TEMPLATE_YAML_FILE_NAME = "MainServiceTemplate.yaml";
    public static final ImmutableSet<String> ELIGIBLE_FILES =
            of(MAIN_SERVICE_TEMPLATE_MF_FILE_NAME,MAIN_SERVICE_TEMPLATE_YAML_FILE_NAME);
    public static final ImmutableSet<String> MANIFEST_PNF_METADATA =
        of(PNFD_PROVIDER.getToken(), PNFD_NAME.getToken(), PNFD_RELEASE_DATE_TIME.getToken(),
            PNFD_ARCHIVE_VERSION.getToken());
    public static final ImmutableSet<String> MANIFEST_VNF_METADATA =
            of(VNF_PROVIDER_ID.getToken(), VNF_PRODUCT_NAME.getToken(), VNF_RELEASE_DATE_TIME.getToken(),
                VNF_PACKAGE_VERSION.getToken());
    public static final int MANIFEST_METADATA_LIMIT = 4;
    public static final String TOSCA_META_ORIG_PATH_FILE_NAME = "TOSCA-Metadata/TOSCA.meta.original";
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
