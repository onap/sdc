/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

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
