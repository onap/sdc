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
package org.openecomp.sdc.common.api;

import org.junit.Test;
import org.openecomp.sdc.common.api.ResponseInfo.ResponseStatusEnum;

public class ResponseInfoTest {

    private ResponseInfo createTestSubject() {
        return new ResponseInfo(null, "");
    }

    @Test
    public void testGetApplicativeStatus() throws Exception {
        ResponseInfo testSubject;
        ResponseStatusEnum result;
        // default test
        testSubject = createTestSubject();
        result = testSubject.getApplicativeStatus();
    }

    @Test
    public void testSetApplicativeStatus() throws Exception {
        ResponseInfo testSubject;
        ResponseStatusEnum applicativeStatus = null;
        // default test
        testSubject = createTestSubject();
        testSubject.setApplicativeStatus(applicativeStatus);
    }

    @Test
    public void testGetDescription() throws Exception {
        ResponseInfo testSubject;
        String result;
        // default test
        testSubject = createTestSubject();
        result = testSubject.getDescription();
    }

    @Test
    public void testSetDescription() throws Exception {
        ResponseInfo testSubject;
        String description = "";
        // default test
        testSubject = createTestSubject();
        testSubject.setDescription(description);
    }
}
