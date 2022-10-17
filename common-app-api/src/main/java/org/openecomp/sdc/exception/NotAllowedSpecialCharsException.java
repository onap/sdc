/*
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2022 Nordix Foundation. All rights reserved.
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

package org.openecomp.sdc.exception;


import lombok.Getter;

@Getter
public class NotAllowedSpecialCharsException extends RuntimeException {

    private static final String ERROR_SPECIAL_CHARACTERS_NOT_ALLOWED = "Error: HTML elements not permitted in field values.";
    private final String errorId;
    private final String message;

    public NotAllowedSpecialCharsException() {
        this.errorId = "NOT_PERMITTED_SPECIAL_CHARS";
        this.message = ERROR_SPECIAL_CHARACTERS_NOT_ALLOWED;
    }

}
