/*
 * Copyright (C) 2017 Huawei Intellectual Property. All rights reserved.
 *
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
 */

package org.openecomp.sdc.common.utils;

import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertTrue;

public class CommonUtilTest {

    @Test
    public void testGetObjectAsMap() {
        Map<String, String> obj = new HashMap<>(1);
        obj.put(CommonUtil.DEFAULT, "");
        Map<String, Object> newMap = CommonUtil.getObjectAsMap(obj);

        boolean exists = newMap.containsKey(CommonUtil._DEFAULT);

        assertTrue(exists);
    }
}