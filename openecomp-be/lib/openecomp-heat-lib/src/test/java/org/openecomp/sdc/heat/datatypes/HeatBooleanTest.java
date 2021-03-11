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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;

public class HeatBooleanTest {

    @Test
    public void testEval() {
        assertTrue(HeatBoolean.eval("true"));
        assertTrue(HeatBoolean.eval("t"));
        assertTrue(HeatBoolean.eval("on"));
        assertTrue(HeatBoolean.eval("y"));
        assertTrue(HeatBoolean.eval("yes"));
        assertTrue(HeatBoolean.eval(1));
        assertTrue(HeatBoolean.eval(true));

        assertFalse(HeatBoolean.eval("false"));
        assertFalse(HeatBoolean.eval("f"));
        assertFalse(HeatBoolean.eval("off"));
        assertFalse(HeatBoolean.eval("n"));
        assertFalse(HeatBoolean.eval("no"));
        assertFalse(HeatBoolean.eval(0));
        assertFalse(HeatBoolean.eval(false));
    }

    @Test
    public void testIsValueBoolean() {
        assertTrue(HeatBoolean.isValueBoolean("y"));
        assertTrue(HeatBoolean.isValueBoolean("off"));
        assertTrue(HeatBoolean.isValueBoolean(false));
        assertTrue(HeatBoolean.isValueBoolean(1));
        assertTrue(HeatBoolean.isValueBoolean(true));

        assertFalse(HeatBoolean.isValueBoolean("test"));
        assertFalse(HeatBoolean.isValueBoolean(2));
    }
}
