/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.tosca.csar;

import java.util.Arrays;
import java.util.Optional;

public enum ManifestTokenType {
    ALGORITHM("Algorithm"),
    ATTRIBUTE_VALUE_SEPARATOR(":"),
    CMS_BEGIN("-----BEGIN CMS-----"),
    CMS_END("-----END CMS-----"),
    HASH("Hash"),
    METADATA("metadata"),
    NON_MANO_ARTIFACT_SETS("non_mano_artifact_sets"),
    SOURCE("Source"),
    VNF_PRODUCT_NAME("vnf_product_name"),
    VNF_PROVIDER_ID("vnf_provider_id"),
    VNF_PACKAGE_VERSION("vnf_package_version"),
    VNF_RELEASE_DATE_TIME("vnf_release_date_time"),
    PNFD_NAME("pnfd_name"),
    PNFD_PROVIDER("pnfd_provider"),
    PNFD_ARCHIVE_VERSION("pnfd_archive_version"),
    PNFD_RELEASE_DATE_TIME("pnfd_release_date_time");

    private final String token;

    ManifestTokenType(final String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public static Optional<ManifestTokenType> parse(final String token) {
        return Arrays.stream(values()).filter(it -> it.getToken() != null && it.getToken().equals(token)).findFirst();
    }

    public boolean isMetadataEntry() {
        return isMetadataVnfEntry() || isMetadataPnfEntry();
    }

    public boolean isMetadataVnfEntry() {
        switch (this) {
            case VNF_PRODUCT_NAME:
            case VNF_PROVIDER_ID:
            case VNF_PACKAGE_VERSION:
            case VNF_RELEASE_DATE_TIME:
                return true;
            default:
                return false;
        }
    }

    public boolean isMetadataPnfEntry() {
        switch (this) {
            case PNFD_NAME:
            case PNFD_PROVIDER:
            case PNFD_ARCHIVE_VERSION:
            case PNFD_RELEASE_DATE_TIME:
                return true;
            default:
                return false;
        }
    }
}
