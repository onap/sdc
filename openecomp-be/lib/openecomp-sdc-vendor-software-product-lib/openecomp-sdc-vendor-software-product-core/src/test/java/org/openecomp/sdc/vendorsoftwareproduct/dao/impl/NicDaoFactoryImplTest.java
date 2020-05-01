package org.openecomp.sdc.vendorsoftwareproduct.dao.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.NicDaoZusammenImpl;
import org.openecomp.sdc.versioning.dao.VersionableDao;

class NicDaoFactoryImplTest {

    @Test
    void createInterface() {
        final VersionableDao testSubject = NicDaoFactoryImpl.getInstance().createInterface();
        assertNotNull(testSubject);
        assertSame(NicDaoZusammenImpl.class, testSubject.getClass());
    }
}