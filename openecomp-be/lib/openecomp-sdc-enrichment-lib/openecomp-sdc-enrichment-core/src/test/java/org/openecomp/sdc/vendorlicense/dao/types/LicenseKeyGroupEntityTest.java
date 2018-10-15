/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.vendorlicense.dao.types;


import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class LicenseKeyGroupEntityTest {

    LicenseKeyGroupEntity licenseKeyGroupEntity = new LicenseKeyGroupEntity();

    @Before
    public void setup() {
        licenseKeyGroupEntity.setDescription("new Desc");
        licenseKeyGroupEntity.setId("1234");
        licenseKeyGroupEntity.setName("License");
        licenseKeyGroupEntity.setStartDate("18101993");
        licenseKeyGroupEntity.setExpiryDate("19101993");
        licenseKeyGroupEntity.setIncrements("1");
        licenseKeyGroupEntity.setThresholdValue(10);
        licenseKeyGroupEntity.setVersionUuId("7733-9998");
    }

    @Test
    public void testDesc() {
        assertEquals(licenseKeyGroupEntity.getDescription(), "new Desc");
    }

    @Test
    public void testId() {
        assertEquals(licenseKeyGroupEntity.getId(), "1234");
    }

    @Test
    public void testName() {
        assertEquals(licenseKeyGroupEntity.getName(), "License");
    }

    @Test
    public void testStartDate() {
        assertEquals(licenseKeyGroupEntity.getStartDate(), "18101993");
    }

    @Test
    public void testExpiryDate() {
        assertEquals(licenseKeyGroupEntity.getExpiryDate(), "19101993");
    }

    @Test
    public void testIncrements() {
        assertEquals(licenseKeyGroupEntity.getIncrements(), "1");
    }

    @Test
    public void testThresholdValue() {
        assertEquals(licenseKeyGroupEntity.getThresholdValue(), new Integer(10));
    }

    @Test
    public void testVersionUuid() {
        assertEquals(licenseKeyGroupEntity.getVersionUuId(), "7733-9998");
    }
    @Test
    public void testSPLimits() {
        Collection<LimitEntity> limits = null;
        licenseKeyGroupEntity.getSPLimits();
    }
}