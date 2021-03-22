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

package org.openecomp.sdc.common.util;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.common.api.FilterDecisionEnum;
import org.openecomp.sdc.common.datastructure.UserContext;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ThreadLocalsHolderTest {

    @Test
    public void validateSetAngGetMdcProcessReturnsValidThreadLocalBoolean() {
        final boolean testBoolean01 = false;
        final boolean testBoolean02 = true;

        ThreadLocalsHolder.setMdcProcessed(testBoolean01);
        assertEquals(ThreadLocalsHolder.isMdcProcessed(), testBoolean01);
        ThreadLocalsHolder.setMdcProcessed(testBoolean02);
        assertEquals(ThreadLocalsHolder.isMdcProcessed(), testBoolean02);
    }

    @Test
    public void validateSetAngGetUUIDReturnsValidThreadLocalString() {
        final String UUID01 = "testId01";
        final String UUID02 = "testId02";

        ThreadLocalsHolder.setUuid(UUID01);
        assertEquals(ThreadLocalsHolder.getUuid(), UUID01);
        ThreadLocalsHolder.setUuid(UUID02);
        assertEquals(ThreadLocalsHolder.getUuid(), UUID02);
    }

    @Test
    public void validateSetAngGetRequestStartTimeReturnsValidThreadLocalString() {
        final Long requestStartTime01 = 10L;
        final Long requestStartTime02 = 50L;

        ThreadLocalsHolder.setRequestStartTime(requestStartTime01);
        assertEquals(ThreadLocalsHolder.getRequestStartTime(), requestStartTime01);
        ThreadLocalsHolder.setRequestStartTime(requestStartTime02);
        assertEquals(ThreadLocalsHolder.getRequestStartTime(), requestStartTime02);
    }

    @Test
    public void validateCleanupResetsAllParameters() {
        final Long requestStartTime = 10L;
        final String UUID = "testId01";
        final boolean testBoolean = true;

        ThreadLocalsHolder.setMdcProcessed(testBoolean);
        ThreadLocalsHolder.setUuid(UUID);
        ThreadLocalsHolder.setRequestStartTime(requestStartTime);

        ThreadLocalsHolder.cleanup();

        assertNull(ThreadLocalsHolder.getRequestStartTime());
        assertNull(ThreadLocalsHolder.getUuid());
        assertEquals(ThreadLocalsHolder.isMdcProcessed(), false);
    }

    @Test
    public void validateUserContext() {
        UserContext userContext = new UserContext("userId", new HashSet<>(), "firstName", "lastName");

        ThreadLocalsHolder.setUserContext(userContext);
        assertEquals("userId", ThreadLocalsHolder.getUserContext().getUserId());
        assertEquals("firstName", ThreadLocalsHolder.getUserContext().getFirstName());
        assertEquals("lastName", ThreadLocalsHolder.getUserContext().getLastName());
        assertEquals(0, ThreadLocalsHolder.getUserContext().getUserRoles().size());
    }

    @Test
    public void validateApiType() {
        FilterDecisionEnum filterDecision = FilterDecisionEnum.INTERNAL;

        ThreadLocalsHolder.setApiType(filterDecision);
        assertEquals(filterDecision, ThreadLocalsHolder.getApiType());
    }

    @Test
    public void isInternalRequestTest() {
        FilterDecisionEnum filterDecision = FilterDecisionEnum.INTERNAL;
        ThreadLocalsHolder.setApiType(filterDecision);
        assertTrue(ThreadLocalsHolder.isInternalRequest());

        filterDecision = FilterDecisionEnum.EXTERNAL;
        ThreadLocalsHolder.setApiType(filterDecision);
        assertFalse(ThreadLocalsHolder.isInternalRequest());
    }

    @Test
    public void isExternalRequestTest() {
        FilterDecisionEnum filterDecision = FilterDecisionEnum.INTERNAL;
        ThreadLocalsHolder.setApiType(filterDecision);
        assertFalse(ThreadLocalsHolder.isExternalRequest());

        filterDecision = FilterDecisionEnum.EXTERNAL;
        ThreadLocalsHolder.setApiType(filterDecision);
        assertTrue(ThreadLocalsHolder.isExternalRequest());
    }
}
