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
package org.openecomp.sdc.be.model.utils;

import static org.junit.Assert.*;
import static org.openecomp.sdc.common.api.Constants.DEFAULT_GROUP_VF_MODULE;
import static org.openecomp.sdc.common.api.Constants.GROUP_TOSCA_HEAT;

import org.junit.Test;

public class GroupUtilsTest {

    @Test
    public void shouldBeVfModule() {
        boolean vfModule = GroupUtils.isVfModule(DEFAULT_GROUP_VF_MODULE);
        assertTrue(vfModule);
    }

    @Test
    public void shouldNotBeVfModule() {
        boolean vfModule = GroupUtils.isVfModule(GROUP_TOSCA_HEAT);
        assertFalse(vfModule);
    }
}