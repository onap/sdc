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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.core.dao.UniqueValueDao;
import org.openecomp.core.dao.types.UniqueValueEntity;
import org.openecomp.sdc.errors.CoreException;

@ExtendWith(MockitoExtension.class)
class UniqueValueUtilTest {

    private static final String ENTITLEMENT_POOL_NAME = "Entitlement Pool name";
    private static final String ORIGINAL_ENTITY_NAME = "originalEntityName";

    @Mock
    private UniqueValueDao uniqueValueDao;

    private UniqueValueUtil uniqueValueUtil;

    @BeforeEach
    public void setUp() {
        uniqueValueUtil = new UniqueValueUtil(uniqueValueDao);
    }

    @Test
    void testCreateUniqueValue() {
        Mockito.when(uniqueValueDao.get(Mockito.any())).thenReturn(null);
        uniqueValueUtil.createUniqueValue(ENTITLEMENT_POOL_NAME, ORIGINAL_ENTITY_NAME);

        Mockito.verify(uniqueValueDao, Mockito.times(1)).create(Mockito.any());
    }

    @Test
    void testCreateUniqueValueNotUnique() {
        Mockito.when(uniqueValueDao.get(Mockito.any())).thenReturn(new UniqueValueEntity());
        Assertions.assertThrows(CoreException.class, () -> {
            uniqueValueUtil.createUniqueValue(ENTITLEMENT_POOL_NAME, ORIGINAL_ENTITY_NAME);
        });

        Mockito.verify(uniqueValueDao, Mockito.times(1)).get(Mockito.any());
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
        Mockito.when(uniqueValueDao.get(Mockito.any())).thenReturn(null);
        Mockito.doNothing().when(uniqueValueDao).delete(Mockito.any());

        uniqueValueUtil.updateUniqueValue(ENTITLEMENT_POOL_NAME, "oldName", "newName", "uniqueContext");

        Mockito.verify(uniqueValueDao, Mockito.times(1)).create(Mockito.any());
        Mockito.verify(uniqueValueDao, Mockito.times(1)).delete(Mockito.any());
    }

    @Test
    void testValidateUniqueValue() {
        Mockito.when(uniqueValueDao.get(Mockito.any())).thenReturn(null);
        uniqueValueUtil.validateUniqueValue(ENTITLEMENT_POOL_NAME, ORIGINAL_ENTITY_NAME);

        Mockito.verify(uniqueValueDao, Mockito.times(1)).get(Mockito.any());
    }

    @Test
    void testValidateUniqueValueNotUnique() {
        Mockito.when(uniqueValueDao.get(Mockito.any())).thenReturn(new UniqueValueEntity());
        Assertions.assertThrows(CoreException.class, () -> {
            uniqueValueUtil.createUniqueValue(ENTITLEMENT_POOL_NAME, ORIGINAL_ENTITY_NAME);
        });

        Mockito.verify(uniqueValueDao, Mockito.times(1)).get(Mockito.any());
    }

    @Test
    void testIsUniqueValueOccupied() {
        Mockito.when(uniqueValueDao.get(Mockito.any())).thenReturn(new UniqueValueEntity());
        assertTrue(uniqueValueUtil.isUniqueValueOccupied(ENTITLEMENT_POOL_NAME, ORIGINAL_ENTITY_NAME));
    }

    @Test
    void testIsUniqueValueOccupiedFalse() {
        Mockito.when(uniqueValueDao.get(Mockito.any())).thenReturn(null);
        assertFalse(uniqueValueUtil.isUniqueValueOccupied(ENTITLEMENT_POOL_NAME, ORIGINAL_ENTITY_NAME));
    }
}
