/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.vendorlicense.errors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.errors.ErrorCode;

public class LimitErrorBuilderTest {

    @Test
    public void testGetInvalidValueErrorBuilder() {
        ErrorCode res = LimitErrorBuilder.getInvalidValueErrorBuilder("attrebute", "404");
        assertEquals("The attrebute value doesn't meet the expected attribute value.", res.message());
        assertEquals("404", res.id());
    }

    @Test
    public void testGetDuplicateNameErrorbuilder() {
        ErrorCode res = LimitErrorBuilder.getDuplicateNameErrorbuilder("name", "type");
        assertEquals("Invalid request, Limit with name name already exists for type type.", res.message());
        assertEquals("DUPLICATE_LIMIT_NAME_NOT_ALLOWED", res.id());
    }
}
