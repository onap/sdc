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

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RelationshipTypeDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

public class TypeCompareUtilsTest {

    @Test
    public void checkTypeAlreadyExists() {
        assertEquals(TypeCompareUtils.typeAlreadyExists().right().value(), StorageOperationStatus.OK);
    }

    @Test
    public void checkGroupTypesEquality() {
        GroupTypeDefinition object1 = new GroupTypeDefinition();
        GroupTypeDefinition object2 = new GroupTypeDefinition();
        assertTrue(TypeCompareUtils.isGroupTypesEquals(object1, object1));
        assertTrue(TypeCompareUtils.isGroupTypesEquals(object1, object2));
        assertFalse(TypeCompareUtils.isGroupTypesEquals(object1, null));
    }

    @Test
    public void checkCapabilityTypesEquality() {
        CapabilityTypeDefinition object1 = new CapabilityTypeDefinition();
        CapabilityTypeDefinition object2 = new CapabilityTypeDefinition();
        assertTrue(TypeCompareUtils.isCapabilityTypesEquals(object1, object1));
        assertTrue(TypeCompareUtils.isCapabilityTypesEquals(object1, object2));
        assertFalse(TypeCompareUtils.isCapabilityTypesEquals(object1, null));
    }

    @Test
    public void checkRelationshipTypesEquality() {
        RelationshipTypeDefinition object1 = new RelationshipTypeDefinition();
        RelationshipTypeDefinition object2 = new RelationshipTypeDefinition();
        assertTrue(TypeCompareUtils.isRelationshipTypesEquals(object1, object1));
        assertTrue(TypeCompareUtils.isRelationshipTypesEquals(object1, object2));
        assertFalse(TypeCompareUtils.isRelationshipTypesEquals(object1, null));
    }

    @Test
    public void checkPropertyDefinitionEquality() {
        PropertyDefinition object1 = new PropertyDefinition();
        PropertyDefinition object2 = new PropertyDefinition();
        assertTrue(TypeCompareUtils.propertiesEquals(
            Collections.singletonList(object1), Collections.singletonList(object1)));
        assertTrue(TypeCompareUtils.propertiesEquals(
            Collections.singletonList(object1), Collections.singletonList(object2)));
        assertFalse(TypeCompareUtils.propertiesEquals(
            Collections.singletonList(object1), null));
    }
}