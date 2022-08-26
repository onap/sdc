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
package org.openecomp.sdc.vendorlicense;

import static org.openecomp.sdc.common.api.Constants.VENDOR_LICENSE_MODEL;
import static org.openecomp.sdc.common.api.Constants.VF_LICENSE_MODEL;

import java.io.File;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

public final class VendorLicenseConstants {

    public static final String VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE = "VendorLicenseModel";
    public static final String EXTERNAL_ARTIFACTS_DIR = "Artifacts";
    //todo change when separating external from internal artifacts
    public static final String VNF_ARTIFACT_NAME_WITH_PATH = EXTERNAL_ARTIFACTS_DIR + File.separator + VF_LICENSE_MODEL;
    public static final String VENDOR_LICENSE_MODEL_ARTIFACT_NAME_WITH_PATH = EXTERNAL_ARTIFACTS_DIR + File.separator + VENDOR_LICENSE_MODEL;
    public static final String VENDOR_LICENSE_MODEL_ARTIFACT_REGEX_REMOVE = " xmlns=\"\"";
    public static final String UNSUPPORTED_OPERATION_ERROR = "An error has occurred: Unsupported operation for 1707 release.";

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class UniqueValues {

        public static final String VENDOR_NAME = "Vendor name";
        public static final String LICENSE_AGREEMENT_NAME = "License Agreement name";
        public static final String FEATURE_GROUP_NAME = "Feature Group name";
        public static final String ENTITLEMENT_POOL_NAME = "Entitlement Pool name";
        public static final String LICENSE_KEY_GROUP_NAME = "License Key Group name";
    }
}
