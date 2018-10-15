package org.openecomp.sdc.vendorlicense.dao.types;


import org.junit.Before;
import org.junit.Test;

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
        assertEquals(licenseKeyGroupEntity.getThresholdValue(), new Integer(1));
    }

    @Test
    public void testVersionUuid() {
        assertEquals(licenseKeyGroupEntity.getVersionUuId(), "7733-9998");
    }
}