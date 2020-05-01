package org.openecomp.sdc.vendorsoftwareproduct.dao.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.DeploymentFlavorDaoZusammenImpl;
import org.openecomp.sdc.versioning.dao.VersionableDao;

class DeploymentFlavorDaoFactoryImplTest {

    @Test
    void createInterface() {
        final VersionableDao testSubject = DeploymentFlavorDaoFactoryImpl.getInstance().createInterface();
        assertNotNull(testSubject);
        assertSame(DeploymentFlavorDaoZusammenImpl.class, testSubject.getClass());
    }
}