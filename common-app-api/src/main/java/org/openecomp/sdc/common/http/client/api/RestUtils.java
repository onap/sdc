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

import java.util.Base64;
import org.apache.http.HttpHeaders;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class RestUtils {

    public static void addBasicAuthHeader(Properties headers, String username, String password) {
        byte[] credentials = Base64.getEncoder().encode((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        headers.setProperty(HttpHeaders.AUTHORIZATION, "Basic " + new String(credentials, StandardCharsets.UTF_8));
    }

}
