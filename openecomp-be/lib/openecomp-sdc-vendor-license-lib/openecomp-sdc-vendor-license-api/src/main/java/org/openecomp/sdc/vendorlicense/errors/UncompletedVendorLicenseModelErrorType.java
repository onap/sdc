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
package org.openecomp.sdc.vendorlicense.errors;

/**
 * Created by ayalaben on 5/8/2017
 */
public enum UncompletedVendorLicenseModelErrorType {
    // @formatter:off
    SUBMIT_UNCOMPLETED_VLM_MSG_MISSING_LA("Uncompleted vendor license model - cannot be submitted. \n"
        + "It must contain a license agreement(s)."),
    SUBMIT_UNCOMPLETED_VLM_MSG_LA_MISSING_FG("Uncompleted vendor license model - cannot be submitted. \n"
        + "The license agreement(s) must contain at least one feature group."),
    SUBMIT_UNCOMPLETED_VLM_MSG_FG_MISSING_EP("Uncompleted vendor license model - cannot be submitted. \n"
        + "The feature group(s) must contain at least one entitlement pool.");
    // @formatter:on

    private String errorMessage;

    UncompletedVendorLicenseModelErrorType(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
