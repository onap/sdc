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
package org.openecomp.sdc.vendorlicense.errors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VendorLicenseErrorCodes {

    public static final String VENDOR_LICENSE_MODEL_NOT_FOUND = "VENDOR_LICENSE_MODEL_NOT_FOUND";
    public static final String SUBMIT_UNCOMPLETED_LICENSE_MODEL = "SUBMIT_UNCOMPLETED_LICENSE_MODEL";
    public static final String LICENSING_DATA_INVALID = "LICENSING_DATA_INVALID";
    public static final String LIMIT_INVALID_TYPE = "LIMIT_INVALID_TYPE";
    public static final String LIMIT_INVALID_METRIC = "LIMIT_INVALID_METRIC";
    public static final String LIMIT_INVALID_AGGREGATIONFUNCTION = "LIMIT_INVALID_AGGREGATIONFUNCTION";
    public static final String LIMIT_INVALID_TIME = "LIMIT_INVALID_TIME";
    public static final String DUPLICATE_LIMIT_NAME_NOT_ALLOWED = "DUPLICATE_LIMIT_NAME_NOT_ALLOWED";
    public static final String DATE_RANGE_INVALID = "DATE_RANGE_INVALID";
    public static final String VLM_IS_IN_USE_DELETE_ERROR = "VLM_IS_IN_USE_DELETE_ERROR";
    public static final String VLM_IS_CERTIFIED_DELETE_ERROR = "VLM_IS_CERTIFIED_DELETE_ERROR";
}
