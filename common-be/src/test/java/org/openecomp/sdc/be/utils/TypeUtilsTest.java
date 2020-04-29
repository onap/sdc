/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.utils;

import org.junit.Test;
import org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum;

import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


public class TypeUtilsTest {

    private static final String ANY_GROUP = "anyGroup";

    private class DummyClass {
        private String field;

        public void setField(String field) { this.field = field; }
        public String getField() { return this.field; }
    }

    @Test
    public void testSetFieldShouldConsumeForJSONContainingParam() {
        DummyClass dummyObject = new DummyClass();
        Map<String, Object> toscaJson = new HashMap<>();
        toscaJson.put(ToscaTagNamesEnum.GROUPS.getElementName(), ANY_GROUP);
        TypeUtils.setField(toscaJson, ToscaTagNamesEnum.GROUPS, dummyObject::setField);
        assertEquals(ANY_GROUP, dummyObject.getField());
    }

    @Test
    public void testSetFieldShouldDoNothingForJSONNotContainingParam() {
        DummyClass dummyObject = new DummyClass();
        Map<String, Object> toscaJson = new HashMap<>();
        toscaJson.put(ToscaTagNamesEnum.GROUPS.getElementName(), ANY_GROUP);
        TypeUtils.setField(toscaJson, ToscaTagNamesEnum.INPUTS, dummyObject::setField);
        assertNull(dummyObject.getField());
    }

}