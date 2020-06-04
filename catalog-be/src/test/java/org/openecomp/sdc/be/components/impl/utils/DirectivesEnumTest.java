/*
 * -
 *  * ============LICENSE_START=======================================================
 *  *  Copyright (C) 2019  Nordix Foundation.
 *  * ================================================================================
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *  * ============LICENSE_END=========================================================
 *
 */

package org.openecomp.sdc.be.components.impl.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class DirectivesEnumTest {

    private List<String> directives;

    @Before
    public void setup() {
        directives = new ArrayList<>();
    }

    @Test
    public void testGivenValidDirectives_returnsTrue() {
        directives.add(DirectivesEnum.SELECT.getValue());
        directives.add(DirectivesEnum.SELECTABLE.getValue());
        directives.add(DirectivesEnum.SUBSTITUTE.getValue());
        directives.add(DirectivesEnum.SUBSTITUTABLE.getValue());
        assertTrue(DirectivesEnum.isValid(directives));
    }

    @Test
    public void testGivenEmptyDirectives_returnsTrue() {
        assertTrue(DirectivesEnum.isValid(directives));
    }

    @Test
    public void testGivenInvalidDirectives_returnsFalse() {
        directives.add("Invalid");
        assertFalse(DirectivesEnum.isValid(directives));
    }
}