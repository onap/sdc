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

package org.openecomp.sdc.common.http.client.api;

import org.apache.commons.lang3.StringUtils;

public class HttpResponse<T> {
    private final T response;
    private final int statusCode;
    private final String description;

    public HttpResponse(T response, int statusCode) {
        this.response = response;
        this.statusCode = statusCode;
        this.description = StringUtils.EMPTY;
    }
    
    public HttpResponse(T response, int statusCode, String description) {
        this.response = response;
        this.statusCode = statusCode;
        this.description = description;
    }

    public T getResponse() {
        return response;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("HttpResponse [response=");
        builder.append(response);
        builder.append(", statusCode=");
        builder.append(statusCode);
        builder.append(", description=");
        builder.append(description);
        builder.append("]");
        return builder.toString();
    }
    
    
}
