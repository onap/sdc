/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 * ===================================================================
 *
 */

package org.openecomp.sdc.be.config;

import static org.junit.Assert.assertEquals;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;


public class ErrorConfigurationTest {

    private ErrorConfiguration createTestSubject() {
        return new ErrorConfiguration();
    }


    @Test
    public void testGetErrors() throws Exception {
        ErrorConfiguration testSubject;
        Map<String, ErrorInfo> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getErrors();
    }


    @Test
    public void testSetErrors() throws Exception {
        ErrorConfiguration testSubject;
        Map<String, ErrorInfo> errors = null;

        // default test
        testSubject = createTestSubject();
        testSubject.setErrors(errors);
    }

    @Test
    public void testToString() throws Exception {
        ErrorConfiguration testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.toString();
    }

    @Test
    public void testGetErrorInfo() {
        //given
        Map<String, ErrorInfo> errors = new HashMap<>();
        ErrorInfo errorInfo = new ErrorInfo();
        errorInfo.setCode(100);
        errorInfo.setMessageId("1");
        errorInfo.setMessage("Mock message");
        errors.put("key", errorInfo);
        ErrorConfiguration testSubject;
        testSubject = createTestSubject();
        testSubject.setErrors(errors);
        //when
        ErrorInfo clonedErrorInfo = testSubject.getErrorInfo("key");
        //then
        assertEquals(errorInfo.getErrorInfoType(), clonedErrorInfo.getErrorInfoType());
        assertEquals(errorInfo.getCode(), clonedErrorInfo.getCode());
        assertEquals(errorInfo.getMessageId(), clonedErrorInfo.getMessageId());
        assertEquals(errorInfo.getMessage(), clonedErrorInfo.getMessage());
    }
}