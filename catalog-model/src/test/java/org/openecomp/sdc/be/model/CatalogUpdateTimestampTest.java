package org.openecomp.sdc.be.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class CatalogUpdateTimestampTest {

    private CatalogUpdateTimestamp createTestSubject() {
        return new CatalogUpdateTimestamp(0L, System.currentTimeMillis());
    }

    @Test
    public void testBuildDummyCatalogUpdateTimestamp() {
        CatalogUpdateTimestamp testSubject;
        CatalogUpdateTimestamp result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.buildDummyCatalogUpdateTimestamp();
        assertNotNull(result);
    }

    @Test
    public void testEquals() {
        CatalogUpdateTimestamp testSubject1;
        CatalogUpdateTimestamp testSubject2;
        boolean result;

        // default test
        testSubject1 = createTestSubject();
        testSubject1.setPreviousUpdateTime(testSubject1.getCurrentUpdateTime());
        testSubject1.setCurrentUpdateTime(System.currentTimeMillis());
        testSubject2 = createTestSubject();
        result = testSubject1.equals(testSubject2);
        assertFalse(result);
    }

    @Test
    public void testHashCode() {
        CatalogUpdateTimestamp testSubject;
        int result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.hashCode();
        assertNotNull(result);
    }

    @Test
    public void testSetPreviousUpdateTime() {
        CatalogUpdateTimestamp testSubject;
        long result;

        // default test
        testSubject = createTestSubject();
        testSubject.setPreviousUpdateTime(System.currentTimeMillis());
        result = testSubject.getPreviousUpdateTime();
        assertNotNull(result);
    }

    @Test
    public void testSetCurrentUpdateTime() {
        CatalogUpdateTimestamp testSubject;
        long result;

        // default test
        testSubject = createTestSubject();
        testSubject.setCurrentUpdateTime(System.currentTimeMillis());
        result = testSubject.getCurrentUpdateTime();
        assertNotNull(result);
    }

    @Test
    public void testToString() {
        CatalogUpdateTimestamp testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.toString();
        assertNotNull(result);
    }

    @Test
    public void testGetPreviousUpdateTime() {
        CatalogUpdateTimestamp testSubject;
        long result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getPreviousUpdateTime();
        assertNotNull(result);
    }

    @Test
    public void testGetCurrentUpdateTime() {
        CatalogUpdateTimestamp testSubject;
        long result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getCurrentUpdateTime();
        assertNotNull(result);
    }

}
