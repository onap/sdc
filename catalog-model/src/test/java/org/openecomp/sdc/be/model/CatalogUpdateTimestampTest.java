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

}
