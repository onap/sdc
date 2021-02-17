/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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
import static org.junit.Assert.assertTrue;

import org.apache.http.HttpStatus;
import org.junit.Test;

public class HttpResponseTest {

    @Test
    public void validateNoDescriptionConstructor() {
        final String testResponse = "test response";
        HttpResponse<String> httpResponseTest = new HttpResponse<>(testResponse, HttpStatus.SC_OK);
        assertEquals(httpResponseTest.getStatusCode(), HttpStatus.SC_OK);
        assertEquals(httpResponseTest.getResponse(), testResponse);
        assertEquals(httpResponseTest.getDescription(), "");
    }

    @Test
    public void validateAllArgsConstructor() {
        final String testResponse = "test response";
        final String testDescription = "test description";
        HttpResponse<String> httpResponseTest = new HttpResponse<>(testResponse, HttpStatus.SC_OK, testDescription);
        assertEquals(httpResponseTest.getStatusCode(), HttpStatus.SC_OK);
        assertEquals(httpResponseTest.getResponse(), testResponse);
        assertEquals(httpResponseTest.getDescription(), testDescription);
    }

    @Test
    public void validateToStringConstructor() {
        final String testResponse = "test response";
        final String testDescription = "test description";
        HttpResponse<String> httpResponseTest = new HttpResponse<>(testResponse, HttpStatus.SC_OK, testDescription);
        assertTrue(httpResponseTest.toString().contains(Integer.toString(HttpStatus.SC_OK)));
        assertTrue(httpResponseTest.toString().contains(testResponse));
        assertTrue(httpResponseTest.toString().contains(testDescription));
    }
}
