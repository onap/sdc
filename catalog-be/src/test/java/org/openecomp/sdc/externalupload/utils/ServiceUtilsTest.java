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
package org.openecomp.sdc.externalupload.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;

public class ServiceUtilsTest {

    private static final String INVALID_MODEL = "Invalid model";
    private static final String OBJ_1 = "obj1";
    private static final String PROP = "prop";

    @Test
    public void shouldCreateObjectUsingSetters() throws Exception {
        TestModel testModel = getTestModel();
        Optional<TestModel> objectUsingSetters = ServiceUtils.createObjectUsingSetters(testModel, TestModel.class);
        assertNotEquals(objectUsingSetters.orElseThrow(() -> new Exception(INVALID_MODEL)), testModel);
        assertEquals(objectUsingSetters.orElseThrow(() -> new Exception(INVALID_MODEL)).getProp(), testModel.getProp());
    }

    @Test
    public void shouldGetObjectAsMap() {
        TestModel testModel = getTestModel();
        Map<String, Object> objectAsMap = ServiceUtils.getObjectAsMap(testModel);
        assertEquals(objectAsMap.size(), 1);
        assertEquals(objectAsMap.get(PROP), OBJ_1);
    }

    @Test
    public void shouldGetClassFieldNames() {
        Set<String> classFieldNames = ServiceUtils.getClassFieldNames(TestModel.class);
        assertTrue(classFieldNames.contains(PROP));
    }

    private TestModel getTestModel() {
        TestModel testModel = new TestModel();
        testModel.setProp(OBJ_1);
        return testModel;
    }

}