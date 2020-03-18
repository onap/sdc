package org.openecomp.sdc.be.model;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class UploadArtifactInfoTest {

    private UploadArtifactInfo createTestSubject() {
        return new UploadArtifactInfo();
    }

    @Test
    public void testUploadArtifactInfo() {
        UploadArtifactInfo testSubject;

        // default test
        testSubject = createTestSubject();
        assertNotNull(testSubject);
    }
}