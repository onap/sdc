package org.openecomp.sdc.vendorsoftwareproduct.dao.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.ComponentDependencyModelDaoZusammenImpl;
import org.openecomp.sdc.versioning.dao.VersionableDao;

class ComponentDependencyModelDaoFactoryImplTest {

    @Test
    void createInterface() {
        final VersionableDao testSubject = ComponentDependencyModelDaoFactoryImpl.getInstance().createInterface();
        assertNotNull(testSubject);
        assertSame(ComponentDependencyModelDaoZusammenImpl.class, testSubject.getClass());
    }
}