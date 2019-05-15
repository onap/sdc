package org.openecomp.sdc.activitylog.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openecomp.sdc.activitylog.ActivityLogManager;
import org.openecomp.sdc.activitylog.dao.ActivityLogDao;
import org.openecomp.sdc.activitylog.dao.ActivityLogDaoFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ActivityLogDaoFactory.class)
public class ActivityLogManagerFactoryImplTest {

    @Mock
    ActivityLogDaoFactory activityLogDaoFactory;
    @Mock
    ActivityLogDao activityLogDao;

    @Before
    public void setUp(){
        initMocks(this);
        PowerMockito.mockStatic(ActivityLogDaoFactory.class);
        when(ActivityLogDaoFactory.getInstance()).thenReturn(activityLogDaoFactory);
        when(activityLogDaoFactory.createInterface()).thenReturn(activityLogDao);

    }

    @Test
    public void createInterfaceTest(){
        ActivityLogManager activityLogManager = new ActivityLogManagerFactoryImpl().createInterface();
        assertNotNull(activityLogManager);
    }
}
