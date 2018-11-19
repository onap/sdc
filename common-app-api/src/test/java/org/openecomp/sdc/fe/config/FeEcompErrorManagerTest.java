package org.openecomp.sdc.fe.config;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.common.config.IEcompConfigurationManager;


public class FeEcompErrorManagerTest {

    private FeEcompErrorManager createTestSubject() {
        return FeEcompErrorManager.getInstance();
    }

    @Test
    public void testGetConfigurationManager() {
        FeEcompErrorManager testSubject = createTestSubject();
        IEcompConfigurationManager iEcompConfigurationManager = testSubject.getConfigurationManager();
        Assert.assertNotNull(iEcompConfigurationManager);//assertNotNull(iEcompConfigurationManager);
    }

    @Test
    public void testLogFeHealthCheckRecovery() {
        FeEcompErrorManager testSubject = createTestSubject();
        testSubject.logFeHealthCheckRecovery("test");
    }

    @Test
    public void testLogFeHttpLoggingError() {
        FeEcompErrorManager testSubject = createTestSubject();
        testSubject.logFeHttpLoggingError("test");
    }

    @Test
    public void testLogFePortalServletError() {
        FeEcompErrorManager testSubject = createTestSubject();
        testSubject.logFePortalServletError("test");
    }

    @Test
    public void testLogFeHealthCheckError() {
        FeEcompErrorManager testSubject = createTestSubject();
        testSubject.logFeHealthCheckError("test");
    }

    @Test
    public void testLogFeHealthCheckGeneralError() {
        FeEcompErrorManager testSubject = createTestSubject();
        testSubject.logFeHealthCheckGeneralError("test");
    }

}
