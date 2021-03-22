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
package org.openecomp.sdc.itempermissions.errors;

/**
 * Created by ayalaben on 6/28/2017
 */
public enum PermissionsErrorMessages {

    NO_PERMISSION_FOR_USER("The user is not permitted to edit this item"),
    USER_NOT_OWNER_SUBMIT("The user must be the owner to submit the item"),
    INVALID_PERMISSION_TYPE("Invalid permission type"),
    INVALID_ACTION_TYPE("Invalid action type");

    private String errorMessage;

    PermissionsErrorMessages(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
