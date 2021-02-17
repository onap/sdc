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

import static org.junit.Assert.assertEquals;

import java.util.Properties;
import org.apache.http.HttpHeaders;
import org.junit.Test;

public class RestUtilsTest {

    @Test
    public void addBasicAuthHeader() {
        Properties headers = new Properties();
        RestUtils restutil = new RestUtils();
        restutil.addBasicAuthHeader(headers, "uname", "passwd");
        assertEquals(headers.getProperty(HttpHeaders.AUTHORIZATION), "Basic dW5hbWU6cGFzc3dk");
    }
}
