package org.openecomp.sdc.vendorsoftwareproduct.dao.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ProcessDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.ProcessDaoZusammenImpl;

class ProcessDaoFactoryImplTest {

    @Test
    void createInterface() {
        final ProcessDao testSubject = ProcessDaoFactoryImpl.getInstance().createInterface();
        assertNotNull(testSubject);
        assertSame(ProcessDaoZusammenImpl.class, testSubject.getClass());
    }
}