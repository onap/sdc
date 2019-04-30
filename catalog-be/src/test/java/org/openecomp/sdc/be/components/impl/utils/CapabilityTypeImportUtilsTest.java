/*
 * -
 *  * ============LICENSE_START=======================================================
 *  *  Copyright (C) 2019  Nordix Foundation.
 *  * ================================================================================
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *  * ============LICENSE_END=========================================================
 *
 */

package org.openecomp.sdc.be.components.impl.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import java.util.Collections;

import static org.junit.Assert.assertTrue;

public class CapabilityTypeImportUtilsTest {

    private CapabilityTypeDefinition type1;
    private CapabilityTypeDefinition type2;

    @Before
    public void setUp(){
        type1 = new CapabilityTypeDefinition();
        type2 = new CapabilityTypeDefinition();
    }

    @Test
    public void testGivenSameType_returnsTrue(){
        assertTrue(CapabilityTypeImportUtils.isCapabilityTypesEquals(type1, type1));
    }

    @Test
    public void testGivenNullCapability_returnsFalse(){
        Assert.assertFalse(CapabilityTypeImportUtils.isCapabilityTypesEquals(type1,null));
    }

    @Test
    public void testGivenEqualCapabilitiesWithParams_thenReturnsTrue(){
        updateCapabilities(type1);
        updateCapabilities(type2);
        assertTrue(CapabilityTypeImportUtils.isCapabilityTypesEquals(type1, type2));
    }

    private void updateCapabilities(CapabilityTypeDefinition type){
        type.setType("a");
        type.setVersion("1");
        type.setDerivedFrom("none");
        type.setValidSourceTypes(Collections.EMPTY_LIST);
        type.setDescription("Testing capability types");
        type.setProperties(Collections.EMPTY_MAP);
    }
}