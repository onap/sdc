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
package org.openecomp.sdc.common.errors;

import org.springframework.http.HttpStatus;

/**
 * This class represents an error object to be returned in failed REST instead of just returning one of HTTP fail statuses.
 */
public class ErrorCodeAndMessage {

    /**
     * the HTTP status code.
     */
    private HttpStatus status;
    /**
     * Error code no. if available.
     */
    private String errorCode;
    /**
     * the error message to be displayed.
     */
    private String message;

    /**
     * Instantiates a new Error code and message.
     *
     * @param status    the status
     * @param errorCode the error code
     */
    public ErrorCodeAndMessage(HttpStatus status, ErrorCode errorCode) {
        this.status = status;
        this.message = errorCode.message();
        this.errorCode = errorCode.id();
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
