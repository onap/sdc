/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 *
 *
 */

package org.openecomp.sdc.be.model.tosca.validators;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimestampValidatorTest {

    private static final TimestampValidator validator = TimestampValidator.getInstance();

    @Test
    void testTimestampValidator() {
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("", null));
        assertTrue(validator.isValid("2001-12-15T02:59:43.1Z", null));
        assertTrue(validator.isValid("2001-12-14t21:59:43.10-05:00", null));
        assertTrue(validator.isValid("2001-12-15 2:59:43.10", null));
        assertTrue(validator.isValid("2002-12-14", null));
        assertTrue(validator.isValid("2001-12-14 21:59:43.10+00", null));
        assertTrue(validator.isValid("2001-12-14 21:59:43.10 -5", null));
    }
}