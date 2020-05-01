package org.openecomp.sdc.vendorsoftwareproduct.dao.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.OrchestrationTemplateCandidateDaoZusammenImpl;
import org.openecomp.sdc.versioning.dao.VersionableDao;

class OrchestrationTemplateCandidateDaoFactoryImplTest {

    @Test
    void createInterface() {
        final VersionableDao testSubject = OrchestrationTemplateCandidateDaoFactoryImpl.getInstance().createInterface();
        assertNotNull(testSubject);
        assertSame(OrchestrationTemplateCandidateDaoZusammenImpl.class, testSubject.getClass());
    }
}