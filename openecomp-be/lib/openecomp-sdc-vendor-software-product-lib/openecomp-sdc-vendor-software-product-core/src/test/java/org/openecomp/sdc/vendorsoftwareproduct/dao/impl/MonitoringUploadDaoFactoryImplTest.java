package org.openecomp.sdc.vendorsoftwareproduct.dao.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.ComponentArtifactDaoZusammenImpl;
import org.openecomp.sdc.versioning.dao.VersionableDao;

class MonitoringUploadDaoFactoryImplTest {

    @Test
    void createInterface() {
        final VersionableDao testSubject = MonitoringUploadDaoFactoryImpl.getInstance().createInterface();
        assertNotNull(testSubject);
        assertSame(ComponentArtifactDaoZusammenImpl.class, testSubject.getClass());
    }
}