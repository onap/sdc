package org.openecomp.sdc.vendorsoftwareproduct.dao.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.common.session.impl.AsdcSessionContextProvider;
import org.openecomp.sdc.vendorsoftwareproduct.dao.PackageInfoDao;

@ExtendWith(MockitoExtension.class)
class PackageInfoDaoFactoryImplTest {

    private static final String USER_ID = "cs0008";

    @InjectMocks
    private AsdcSessionContextProvider asdcSessionContextProvider;

    @BeforeEach
    void setUp() {
        asdcSessionContextProvider.create(USER_ID, "");
    }

    @Disabled
    // TODO - recheck after https://gerrit.onap.org/r/c/sdc/+/106825
    @Test
    void createInterface() {
        final PackageInfoDao testSubject = PackageInfoDaoFactoryImpl.getInstance().createInterface();
        assertNotNull(testSubject);
        assertSame(PackageInfoDaoImpl.class, testSubject.getClass());
    }
}