/*
 *
 *  Copyright © 2017-2018 European Support Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 *
 */

package org.openecomp.core.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.core.dao.UniqueValueDao;
import org.openecomp.core.dao.types.UniqueValueEntity;
import org.openecomp.sdc.common.errors.CoreException;

@ExtendWith(MockitoExtension.class)
class UniqueValueUtilTest {

    private static final String ENTITLEMENT_POOL_NAME = "Entitlement Pool name";
    private static final String ORIGINAL_ENTITY_NAME = "originalEntityName";
    private static final String LOWER_CASE_ENTITY_NAME = ORIGINAL_ENTITY_NAME.toLowerCase();
    private static final String NON_EXISTING_NAME = "nonExistingName";
    private static final String LOWER_CASE_NON_EXISTING = NON_EXISTING_NAME.toLowerCase();

    @Mock
    private UniqueValueDao uniqueValueDao;

    private UniqueValueUtil uniqueValueUtil;

    @BeforeEach
    void setUp() {
        uniqueValueUtil = new UniqueValueUtil(uniqueValueDao);
    }

    private void stubDao(String value, boolean exists) {
        Mockito.lenient().when(uniqueValueDao.get(Mockito.eq(ENTITLEMENT_POOL_NAME), Mockito.eq(value)))
                .thenReturn(exists ? Optional.of(new UniqueValueEntity()) : Optional.empty());
    }

    @Test
    void testCreateUniqueValue() {
        stubDao(LOWER_CASE_ENTITY_NAME, false);
        uniqueValueUtil.createUniqueValue(ENTITLEMENT_POOL_NAME, ORIGINAL_ENTITY_NAME);
        Mockito.verify(uniqueValueDao, Mockito.times(1)).create(Mockito.any());
    }

    @Test
    void testCreateUniqueValueNotUnique() {
        stubDao(LOWER_CASE_ENTITY_NAME, true);
        org.junit.jupiter.api.Assertions.assertThrows(CoreException.class, () -> {
            uniqueValueUtil.createUniqueValue(ENTITLEMENT_POOL_NAME, ORIGINAL_ENTITY_NAME);
        });
        Mockito.verify(uniqueValueDao, Mockito.times(1)).get(Mockito.eq(ENTITLEMENT_POOL_NAME), Mockito.eq(LOWER_CASE_ENTITY_NAME));
    }

    @Test
    void testDeleteUniqueValue() {
        Mockito.doNothing().when(uniqueValueDao).delete(Mockito.any());
        uniqueValueUtil.deleteUniqueValue(ENTITLEMENT_POOL_NAME, ORIGINAL_ENTITY_NAME);
        Mockito.verify(uniqueValueDao, Mockito.times(1)).delete(Mockito.any());
    }

    @Test
    void testDeleteUniqueValueNoValue() {
        uniqueValueUtil.deleteUniqueValue(ENTITLEMENT_POOL_NAME);
        Mockito.verify(uniqueValueDao, Mockito.times(0)).delete(Mockito.any());
    }

    @Test
    void testUpdateUniqueValue() {
    // Format: context_newname (last element lowercased)
    String formattedNewValue = "context_newname";
    Mockito.lenient().when(uniqueValueDao.get(
            Mockito.eq(ENTITLEMENT_POOL_NAME),
            Mockito.eq(formattedNewValue)
    )).thenReturn(Optional.empty()); // not occupied

    Mockito.doNothing().when(uniqueValueDao).delete(Mockito.any());

    uniqueValueUtil.updateUniqueValue(ENTITLEMENT_POOL_NAME, "oldName", "newName", "context");

    Mockito.verify(uniqueValueDao, Mockito.times(1)).create(Mockito.any());
    Mockito.verify(uniqueValueDao, Mockito.times(1)).delete(Mockito.any());
}

    @Test
    void testValidateUniqueValue() {
        stubDao(LOWER_CASE_ENTITY_NAME, false);
        uniqueValueUtil.validateUniqueValue(ENTITLEMENT_POOL_NAME, ORIGINAL_ENTITY_NAME);
        Mockito.verify(uniqueValueDao, Mockito.times(1)).get(Mockito.eq(ENTITLEMENT_POOL_NAME), Mockito.eq(LOWER_CASE_ENTITY_NAME));
    }

    @Test
    void testValidateUniqueValueNotUnique() {
        stubDao(LOWER_CASE_ENTITY_NAME, true);
        org.junit.jupiter.api.Assertions.assertThrows(CoreException.class, () -> {
            uniqueValueUtil.createUniqueValue(ENTITLEMENT_POOL_NAME, ORIGINAL_ENTITY_NAME);
        });
        Mockito.verify(uniqueValueDao, Mockito.times(1)).get(Mockito.eq(ENTITLEMENT_POOL_NAME), Mockito.eq(LOWER_CASE_ENTITY_NAME));
    }

    @Test
    void testIsUniqueValueOccupiedTrue() {
        stubDao(LOWER_CASE_ENTITY_NAME, true);
        assertTrue(uniqueValueUtil.isUniqueValueOccupied(ENTITLEMENT_POOL_NAME, ORIGINAL_ENTITY_NAME));
    }

    @Test
    void testIsUniqueValueOccupiedFalse() {
        stubDao(LOWER_CASE_ENTITY_NAME, false);
        assertFalse(uniqueValueUtil.isUniqueValueOccupied(ENTITLEMENT_POOL_NAME, ORIGINAL_ENTITY_NAME));
    }

    @Test
    void testIsUniqueValueOccupiedFalseWithNonExisting() {
        stubDao(LOWER_CASE_NON_EXISTING, false);
        assertFalse(uniqueValueUtil.isUniqueValueOccupied(ENTITLEMENT_POOL_NAME, NON_EXISTING_NAME));
    }
}


