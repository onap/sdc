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
package org.openecomp.sdc.be.config;

import org.junit.Test;
import org.openecomp.sdc.be.config.ErrorInfo.ErrorInfoType;

public class ErrorInfoTest {

    private ErrorInfo createTestSubject() {
        return new ErrorInfo();
    }

    @Test
    public void testGetCode() throws Exception {
        ErrorInfo testSubject;
        Integer result;
        // default test
        testSubject = createTestSubject();
        result = testSubject.getCode();
    }

    @Test
    public void testSetCode() throws Exception {
        ErrorInfo testSubject;
        Integer code = 0;
        // default test
        testSubject = createTestSubject();
        testSubject.setCode(code);
    }

    @Test
    public void testGetMessage() throws Exception {
        ErrorInfo testSubject;
        String result;
        // default test
        testSubject = createTestSubject();
        result = testSubject.getMessage();
    }

    @Test
    public void testSetMessage() throws Exception {
        ErrorInfo testSubject;
        String message = "";
        // default test
        testSubject = createTestSubject();
        testSubject.setMessage(message);
    }

    @Test
    public void testGetMessageId() throws Exception {
        ErrorInfo testSubject;
        String result;
        // default test
        testSubject = createTestSubject();
        result = testSubject.getMessageId();
    }

    @Test
    public void testSetMessageId() throws Exception {
        ErrorInfo testSubject;
        String messageId = "";
        // test 1
        testSubject = createTestSubject();
        messageId = null;
        testSubject.setMessageId(messageId);
        // test 2
        testSubject = createTestSubject();
        messageId = "";
        testSubject.setMessageId(messageId);
    }

    @Test
    public void testGetErrorInfoType() throws Exception {
        ErrorInfo testSubject;
        ErrorInfoType result;
        // default test
        testSubject = createTestSubject();
        result = testSubject.getErrorInfoType();
    }

    @Test
    public void testToString() throws Exception {
        ErrorInfo testSubject;
        String result;
        // default test
        testSubject = createTestSubject();
        result = testSubject.toString();
    }
}
