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
import org.mockito.Mock;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ActivityLogManagerImplTest {
    private ActivityLogManagerImpl activityLogManager;

    @Mock
    private ActivityLogDaoStub activityLogDao;

    @Before
    public void setUp(){
        initMocks(this);
    }

    @Test
    public void logActivityTest(){
        activityLogManager = new ActivityLogManagerImpl(activityLogDao);
        activityLogManager.logActivity(new ActivityLogEntity());
        verify(activityLogDao, times(1)).create(any());
    }

    @Test
    public void listLoggedActivitiesTest(){
        activityLogManager = new ActivityLogManagerImpl(activityLogDao);
        when(activityLogDao.list(any())).thenCallRealMethod();
        Collection<ActivityLogEntity> collection = activityLogManager.listLoggedActivities("1", new Version());
        assertEquals(collection.size(), 1);
    }
}
