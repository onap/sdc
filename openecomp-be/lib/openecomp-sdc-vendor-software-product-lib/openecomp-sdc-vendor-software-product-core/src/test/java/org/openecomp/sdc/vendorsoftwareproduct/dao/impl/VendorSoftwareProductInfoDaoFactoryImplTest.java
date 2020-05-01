package org.openecomp.sdc.vendorsoftwareproduct.dao.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.VendorSoftwareProductInfoDaoZusammenImpl;

class VendorSoftwareProductInfoDaoFactoryImplTest {

    @Test
    void createInterface() {
        final VendorSoftwareProductInfoDao testSubject = VendorSoftwareProductInfoDaoFactoryImpl.getInstance().createInterface();
        assertNotNull(testSubject);
        assertSame(VendorSoftwareProductInfoDaoZusammenImpl.class, testSubject.getClass());
    }
}
