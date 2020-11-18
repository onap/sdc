/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
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
 */

package org.openecomp.sdc.be.tosca.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class InterfaceTypesNameUtilTest {

    @Test
    void testBuildInterfaceShortNameSuccess() {
        String interfaceShortName = InterfaceTypesNameUtil.buildShortName("an.interface.name");
        assertEquals("name", interfaceShortName);

        interfaceShortName = InterfaceTypesNameUtil.buildShortName("name");
        assertEquals("name", interfaceShortName);

        interfaceShortName = InterfaceTypesNameUtil.buildShortName("");
        assertEquals("", interfaceShortName);

        interfaceShortName = InterfaceTypesNameUtil.buildShortName("an.");
        assertEquals("an.", interfaceShortName);

        interfaceShortName = InterfaceTypesNameUtil.buildShortName(".");
        assertEquals(".", interfaceShortName);

        interfaceShortName = InterfaceTypesNameUtil.buildShortName(".");
        assertEquals(".", interfaceShortName);
    }

    @Test
    void testBuildInterfaceShortNameNullArgument() {
        assertThrows(IllegalArgumentException.class, () -> InterfaceTypesNameUtil.buildShortName(null));
    }
}