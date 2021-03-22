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
package org.openecomp.sdc.heat.datatypes;

import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefinedHeatParameterTypesTest {

    @Test
    public void testIsValueIsFromGivenType() {
        assertTrue(DefinedHeatParameterTypes.isValueIsFromGivenType(2, "number"));
        assertTrue(DefinedHeatParameterTypes.isValueIsFromGivenType("2 KB", "number"));

        assertTrue(DefinedHeatParameterTypes.isValueIsFromGivenType(true, "boolean"));
        assertTrue(DefinedHeatParameterTypes.isValueIsFromGivenType("test", "string"));
        assertTrue(DefinedHeatParameterTypes.isValueIsFromGivenType(new LinkedList<>(), "json"));

        assertTrue(DefinedHeatParameterTypes.isValueIsFromGivenType("val1,val2", "comma_delimited_list"));
        assertFalse(DefinedHeatParameterTypes.isValueIsFromGivenType("", "wrong"));
    }

    @Test
    public void testIsNovaServerEnvValueIsFromRightType() {
        assertTrue(DefinedHeatParameterTypes.isNovaServerEnvValueIsFromRightType("test"));
        assertTrue(DefinedHeatParameterTypes.isNovaServerEnvValueIsFromRightType("val1,val2"));
    }

    @Test
    public void testIsEmptyValueInEnv() {
        assertTrue(DefinedHeatParameterTypes.isEmptyValueInEnv(null));
        assertFalse(DefinedHeatParameterTypes.isEmptyValueInEnv(""));
    }
}
