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

package org.openecomp.sdc.be.model.tosca.constraints;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintValueDoNotMatchPropertyTypeException;

class ConstraintUtilTest {

    @Test
    void testCheckStringType() throws Exception {
        assertDoesNotThrow(() -> ConstraintUtil.checkStringType(ToscaType.STRING));
        assertThrows(ConstraintValueDoNotMatchPropertyTypeException.class, () -> ConstraintUtil.checkStringType(ToscaType.SCALAR_UNIT));
    }

    @Test
    void testCheckComparableType() throws Exception {
        assertDoesNotThrow(() -> ConstraintUtil.checkComparableType(ToscaType.INTEGER));
        assertThrows(ConstraintValueDoNotMatchPropertyTypeException.class, () -> ConstraintUtil.checkComparableType(ToscaType.SCALAR_UNIT));
    }

    @Test
    void testConvertToComparable() throws Exception {
        assertTrue(ConstraintUtil.convertToComparable(ToscaType.BOOLEAN, "true") instanceof Comparable);
        assertThrows(IllegalArgumentException.class, () -> ConstraintUtil.convertToComparable(ToscaType.SCALAR_UNIT, "value"));
    }

    @Test
    void testParseToCollection() throws Exception {
        List<Object> list = ConstraintUtil.parseToCollection("[\"color\",\"type\"]", new TypeReference<List<Object>>() {
        });
        assertTrue(list instanceof List);
        assertEquals(2, list.size());

        assertThrows(ConstraintValueDoNotMatchPropertyTypeException.class,
            () -> ConstraintUtil.parseToCollection("", new TypeReference<List<Object>>() {
            }));
    }

}
