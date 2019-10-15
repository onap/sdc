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

package org.openecomp.sdc.be.model;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class LifeCycleTransitionEnumTest {

    private LifeCycleTransitionEnum createTestSubject() {
        return LifeCycleTransitionEnum.CERTIFY;
    }

    @Test
    public void testGetDisplayName() {

        // default test
        final String displayName = createTestSubject().getDisplayName();
        assertFalse(displayName.isEmpty());
    }

    @Test
    public void testGetFromDisplayName() {

        // default test
        for (final LifeCycleTransitionEnum iterable_element : LifeCycleTransitionEnum.values()) {
            final LifeCycleTransitionEnum displayName = LifeCycleTransitionEnum
                .getFromDisplayName(iterable_element.getDisplayName());
            assertFalse(displayName.getDisplayName().isEmpty());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFromDisplayNameException() {
        // default test
        LifeCycleTransitionEnum.getFromDisplayName("mock");
    }

    @Test
    public void testValuesAsString() {

        // default test
        final String valuesAsString = LifeCycleTransitionEnum.valuesAsString();
    assertFalse(valuesAsString.isEmpty());
    }
}
