package org.openecomp.sdc.vendorsoftwareproduct.dao.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.OrchestrationTemplateDaoZusammenImpl;
import org.openecomp.sdc.versioning.dao.VersionableDao;

class OrchestrationTemplateDaoFactoryImplTest {

    @Test
    void createInterface() {
        final VersionableDao testSubject = OrchestrationTemplateDaoFactoryImpl.getInstance().createInterface();
        assertNotNull(testSubject);
        assertSame(OrchestrationTemplateDaoZusammenImpl.class, testSubject.getClass());
    }
}