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
package org.openecomp.sdc.exception;

import org.junit.Test;
import org.openecomp.sdc.exception.ResponseFormat.RequestErrorWrapper;

public class ResponseFormatTest {

    private ResponseFormat createTestSubject() {
        return new ResponseFormat();
    }

    @Test
    public void testSetStatus() throws Exception {
        ResponseFormat testSubject;
        int status = 0;
        // default test
        testSubject = createTestSubject();
        testSubject.setStatus(status);
    }

    @Test
    public void testGetStatus() throws Exception {
        ResponseFormat testSubject;
        Integer result;
        // default test
        testSubject = createTestSubject();
        result = testSubject.getStatus();
    }

    @Test
    public void testGetRequestError() throws Exception {
        ResponseFormat testSubject;
        RequestErrorWrapper result;
        // default test
        testSubject = createTestSubject();
        result = testSubject.getRequestError();
    }

    @Test
    public void testSetRequestError() throws Exception {
        ResponseFormat testSubject;
        RequestErrorWrapper requestErrorWrapper = null;
        // default test
        testSubject = createTestSubject();
        testSubject.setRequestError(requestErrorWrapper);
    }

    @Test
    public void testSetPolicyException() throws Exception {
        ResponseFormat testSubject;
        PolicyException policyException = null;
        // default test
        testSubject = createTestSubject();
        testSubject.setPolicyException(policyException);
    }

    @Test
    public void testSetServiceException() throws Exception {
        ResponseFormat testSubject;
        ServiceException serviceException = null;
        // default test
        testSubject = createTestSubject();
        testSubject.setServiceException(serviceException);
    }

    @Test
    public void testSetOkResponseInfo() throws Exception {
        ResponseFormat testSubject;
        OkResponseInfo okResponseInfo = null;
        // default test
        testSubject = createTestSubject();
        testSubject.setOkResponseInfo(okResponseInfo);
    }

    @Test
    public void testToString() throws Exception {
        ResponseFormat testSubject;
        String result;
        // default test
        testSubject = createTestSubject();
        result = testSubject.toString();
    }
}
