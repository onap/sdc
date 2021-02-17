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
package org.openecomp.sdc.exception;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class OkResponseInfoTest {

    @Test
    public void validateArgConstructorsThrowsCorrectException() {
        final String testMessageId = "test message id";
        final String testMessage = "test error message: %0, %1";
        final String[] testVariables = {"testVariable01", "testVariable02"};
        final String expectedMessage = "test error message: testVariable01, testVariable02";
        OkResponseInfo okResponseInfo = new OkResponseInfo(testMessageId, testMessage, testVariables);
        assertEquals(okResponseInfo.getMessageId(), testMessageId);
        assertEquals(okResponseInfo.getFormattedErrorMessage(), expectedMessage);
        assertArrayEquals(okResponseInfo.getVariables(), testVariables);
    }
}
