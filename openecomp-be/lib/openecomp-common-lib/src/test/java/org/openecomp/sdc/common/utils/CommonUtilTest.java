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

import static org.onap.sdc.tosca.services.CommonUtil.DEFAULT;
import static org.onap.sdc.tosca.services.CommonUtil.UNDERSCORE_DEFAULT;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.testng.annotations.Test;

public class CommonUtilTest {

    @Test
    public void testGetObjectAsMap() {
        final Map<String, String> obj = new HashMap<>(1);
        obj.put(DEFAULT, "");
        assertTrue(CommonUtil.getObjectAsMap(obj).containsKey(UNDERSCORE_DEFAULT));
    }

}
