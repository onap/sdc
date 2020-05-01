package org.openecomp.sdc.vendorsoftwareproduct.dao.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.NetworkDaoZusammenImpl;
import org.openecomp.sdc.versioning.dao.VersionableDao;

class NetworkDaoFactoryImplTest {

    @Test
    void createInterface() {
        final VersionableDao testSubject = NetworkDaoFactoryImpl.getInstance().createInterface();
        assertNotNull(testSubject);
        assertSame(NetworkDaoZusammenImpl.class, testSubject.getClass());
    }
}