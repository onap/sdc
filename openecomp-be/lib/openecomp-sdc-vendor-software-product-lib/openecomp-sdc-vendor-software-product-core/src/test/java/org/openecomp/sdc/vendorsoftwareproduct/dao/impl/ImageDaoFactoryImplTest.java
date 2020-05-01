package org.openecomp.sdc.vendorsoftwareproduct.dao.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.ImageDaoZusammenImpl;
import org.openecomp.sdc.versioning.dao.VersionableDao;

class ImageDaoFactoryImplTest {

    @Test
    void createInterface() {
        final VersionableDao testSubject = ImageDaoFactoryImpl.getInstance().createInterface();
        assertNotNull(testSubject);
        assertSame(ImageDaoZusammenImpl.class, testSubject.getClass());
    }
}