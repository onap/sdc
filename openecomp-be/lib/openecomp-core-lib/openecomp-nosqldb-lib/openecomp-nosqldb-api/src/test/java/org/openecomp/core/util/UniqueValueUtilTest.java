/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.core.dao.UniqueValueDao;
import org.openecomp.core.dao.types.UniqueValueEntity;
import org.openecomp.sdc.common.errors.CoreException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class UniqueValueUtilTest {

    private static final String ENTITLEMENT_POOL_NAME = "Entitlement Pool name";
    private static final String ORIGINAL_ENTITY_NAME = "originalEntityName";

    @Mock
    private UniqueValueDao uniqueValueDao;

    private UniqueValueUtil uniqueValueUtil;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        uniqueValueUtil = new UniqueValueUtil(uniqueValueDao);
    }

    @Test
    public void testCreateUniqueValue() {
        Mockito.when(uniqueValueDao.get(Mockito.any())).thenReturn(null);
        uniqueValueUtil.createUniqueValue(ENTITLEMENT_POOL_NAME, ORIGINAL_ENTITY_NAME);

        Mockito.verify(uniqueValueDao, Mockito.times(1)).create(Mockito.any());
    }

    @Test(expectedExceptions = CoreException.class)
    public void testCreateUniqueValueNotUnique() {
        Mockito.when(uniqueValueDao.get(Mockito.any())).thenReturn(new UniqueValueEntity());
        uniqueValueUtil.createUniqueValue(ENTITLEMENT_POOL_NAME, ORIGINAL_ENTITY_NAME);

        Mockito.verify(uniqueValueDao, Mockito.times(1)).create(Mockito.any());
    }

    @Test
    public void testDeleteUniqueValue() {
        Mockito.doNothing().when(uniqueValueDao).delete(Mockito.any());
        uniqueValueUtil.deleteUniqueValue(ENTITLEMENT_POOL_NAME, ORIGINAL_ENTITY_NAME);

        Mockito.verify(uniqueValueDao, Mockito.times(1)).delete(Mockito.any());
    }

    @Test
    public void testDeleteUniqueValueNoValue() {
        uniqueValueUtil.deleteUniqueValue(ENTITLEMENT_POOL_NAME);
        Mockito.verify(uniqueValueDao, Mockito.times(0)).delete(Mockito.any());
    }

    @Test
    public void testUpdateUniqueValue() {
        Mockito.when(uniqueValueDao.get(Mockito.any())).thenReturn(null);
        Mockito.doNothing().when(uniqueValueDao).delete(Mockito.any());

        uniqueValueUtil.updateUniqueValue(ENTITLEMENT_POOL_NAME, "oldName", "newName", "uniqueContext");

        Mockito.verify(uniqueValueDao, Mockito.times(1)).create(Mockito.any());
        Mockito.verify(uniqueValueDao, Mockito.times(1)).delete(Mockito.any());
    }

    @Test
    public void testValidateUniqueValue() {
        Mockito.when(uniqueValueDao.get(Mockito.any())).thenReturn(null);
        uniqueValueUtil.validateUniqueValue(ENTITLEMENT_POOL_NAME, ORIGINAL_ENTITY_NAME);

        Mockito.verify(uniqueValueDao, Mockito.times(1)).get(Mockito.any());
    }

    @Test(expectedExceptions = CoreException.class)
    public void testValidateUniqueValueNotUnique() {
        Mockito.when(uniqueValueDao.get(Mockito.any())).thenReturn(new UniqueValueEntity());
        uniqueValueUtil.createUniqueValue(ENTITLEMENT_POOL_NAME, ORIGINAL_ENTITY_NAME);

        Mockito.verify(uniqueValueDao, Mockito.times(1)).get(Mockito.any());
    }

    @Test
    public void testIsUniqueValueOccupied() {
        Mockito.when(uniqueValueDao.get(Mockito.any())).thenReturn(new UniqueValueEntity());
        Assert.assertTrue(uniqueValueUtil.isUniqueValueOccupied(ENTITLEMENT_POOL_NAME, ORIGINAL_ENTITY_NAME));
    }

    @Test
    public void testIsUniqueValueOccupiedFalse() {
        Mockito.when(uniqueValueDao.get(Mockito.any())).thenReturn(null);
        Assert.assertFalse(uniqueValueUtil.isUniqueValueOccupied(ENTITLEMENT_POOL_NAME, ORIGINAL_ENTITY_NAME));
    }
}
