/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
 *  Modification Copyright (C) 2021 Nokia.
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
    // @formatter:off
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
    VNF_SOFTWARE_VERSION("vnf_software_version"),
    VNFM_INFO("vnfm_info"),
    VNFD_ID("vnfd_id"),
    PNFD_NAME("pnfd_name"),
    PNFD_PROVIDER("pnfd_provider"),
    PNFD_ARCHIVE_VERSION("pnfd_archive_version"),
    PNFD_RELEASE_DATE_TIME("pnfd_release_date_time"),
    SIGNATURE("Signature"),
    CERTIFICATE("Certificate"),
    COMPATIBLE_SPECIFICATION_VERSIONS("compatible_specification_versions"),
    APPLICATION_NAME("application_name"),
    APPLICATION_PROVIDER("application_provider"),
    REALEASE_DATE_TIME("release_date_time"),
    ENTRY_DEFINITION_TYPE("entry_definition_type"),
    VENDOR_NAME("vendor_name"),
    ARTIFACT_TYPE("artifact_type");
    // @formatter:on

    private final String token;

    ManifestTokenType(final String token) {
        this.token = token;
    }

    public static Optional<ManifestTokenType> parse(final String token) {
        return Arrays.stream(values()).filter(it -> it.getToken() != null && it.getToken().equalsIgnoreCase(token)).findFirst();
    }

    public String getToken() {
        return token;
    }

    public boolean isMetadataEntry() {
        return isMetadataVnfEntry() || isMetadataPnfEntry() || isMetadataAsdEntry();
    }

    public boolean isMetadataVnfEntry() {
        switch (this) {
            case VNF_PRODUCT_NAME:
            case VNF_PROVIDER_ID:
            case VNF_PACKAGE_VERSION:
            case VNF_RELEASE_DATE_TIME:
            case VNFD_ID:
            case VNFM_INFO:
            case VNF_SOFTWARE_VERSION:
            case COMPATIBLE_SPECIFICATION_VERSIONS:
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
            case COMPATIBLE_SPECIFICATION_VERSIONS:
                return true;
            default:
                return false;
        }
    }

    public boolean isMetadataAsdEntry() {
        switch (this) {
            case APPLICATION_NAME:
            case APPLICATION_PROVIDER:
            case REALEASE_DATE_TIME:
            case ENTRY_DEFINITION_TYPE:
                return true;
            default:
                return false;
        }
    }
}
