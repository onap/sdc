package org.openecomp.sdc.vendorsoftwareproduct.dao.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.ComponentDaoZusammenImpl;
import org.openecomp.sdc.versioning.dao.VersionableDao;

class ComponentDaoFactoryImplTest {

    @Test
    void createInterface() {
        final VersionableDao testSubject = ComponentDaoFactoryImpl.getInstance().createInterface();
        assertNotNull(testSubject);
        assertSame(ComponentDaoZusammenImpl.class, testSubject.getClass());
    }
}