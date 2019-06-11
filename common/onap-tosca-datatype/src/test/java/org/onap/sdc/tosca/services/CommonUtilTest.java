/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia Intellectual Property. All rights reserved.
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
package org.onap.sdc.tosca.services;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class CommonUtilTest {

    private static final String INT_FIELD_KEY = "field1";
    private static final Integer INT_FIELD_VALUE = 1;
    private static final String STRING_FIELD_KEY = "field2";
    private static final String STRING_FIELD_VALUE = "abc";

    @Test
    public void testPopulateBeanMethod()
        throws InstantiationException, IllegalAccessException, InvocationTargetException {
        Map<String, Object> props = new HashMap<>();
        props.put(INT_FIELD_KEY, INT_FIELD_VALUE);
        props.put(STRING_FIELD_KEY, STRING_FIELD_VALUE);
        TestModel testModel = CommonUtil.populateBean(props, TestModel.class);
        Assert.assertEquals(testModel.getField1(), INT_FIELD_VALUE);
        Assert.assertEquals(testModel.getField2(), STRING_FIELD_VALUE);
    }
}