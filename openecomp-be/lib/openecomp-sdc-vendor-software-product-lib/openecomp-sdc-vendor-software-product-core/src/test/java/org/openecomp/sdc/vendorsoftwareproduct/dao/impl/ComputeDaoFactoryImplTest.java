package org.openecomp.sdc.vendorsoftwareproduct.dao.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.ComputeDaoZusammenImpl;
import org.openecomp.sdc.versioning.dao.VersionableDao;

class ComputeDaoFactoryImplTest {

    @Test
    void createInterface() {
        final VersionableDao testSubject = ComputeDaoFactoryImpl.getInstance().createInterface();
        assertNotNull(testSubject);
        assertSame(ComputeDaoZusammenImpl.class, testSubject.getClass());
    }
}