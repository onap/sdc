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

package org.openecomp.sdc.be.components.scheduledtasks;

import fj.data.Either;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.BeConfDependentTest;
import org.openecomp.sdc.be.components.distribution.engine.EnvironmentsEngine;
import org.openecomp.sdc.be.components.scheduledtasks.RecoveryThreadManager.FixEnvironmentTask;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.cassandra.OperationalEnvironmentDao;
import org.openecomp.sdc.be.datatypes.enums.EnvironmentStatusEnum;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RecoveryThreadManagerTest extends BeConfDependentTest {

    @InjectMocks
    RecoveryThreadManager recoveryThreadManager = new RecoveryThreadManager();

    private OperationalEnvironmentDao operationalEnvironmentDao = mock(OperationalEnvironmentDao.class);
    private EnvironmentsEngine environmentsEngine = mock(EnvironmentsEngine.class);
    private ScheduledExecutorService scheduledService = mock(ScheduledExecutorService.class);

    @Before
    public void initMocks() {
        MockitoAnnotations.openMocks(this);
        reset(operationalEnvironmentDao, environmentsEngine, scheduledService);
        recoveryThreadManager.init();
    }

    @Test
    public void testInit() {
        verify(scheduledService, Mockito.times(1)).scheduleAtFixedRate(recoveryThreadManager.task,
                NumberUtils.LONG_ZERO, 180L, TimeUnit.SECONDS);
    }

    @Test
    public void testTaskCassandraCrash() {
        FixEnvironmentTask fixEnvironmentTask = recoveryThreadManager.task;
        Either<List<OperationalEnvironmentEntry>, CassandraOperationStatus> eitherResult = Either.right(CassandraOperationStatus.GENERAL_ERROR);
        when(operationalEnvironmentDao.getByEnvironmentsStatus(EnvironmentStatusEnum.COMPLETED)).thenReturn(eitherResult);
        fixEnvironmentTask.run();
        verify(environmentsEngine, never()).addToMap(Mockito.any(OperationalEnvironmentEntry.class));
    }

    @Test
    public void testOnlyCompletedRecordsAreRead() {
        when(operationalEnvironmentDao.getByEnvironmentsStatus(EnvironmentStatusEnum.COMPLETED)).thenReturn(Either.left(Arrays.asList()));
        recoveryThreadManager.task.run();
        verify(operationalEnvironmentDao, never()).getByEnvironmentsStatus(EnvironmentStatusEnum.FAILED);
        verify(operationalEnvironmentDao, never()).getByEnvironmentsStatus(EnvironmentStatusEnum.IN_PROGRESS);
    }

    @Test
    public void testUnconnectedRecords() {
        FixEnvironmentTask fixEnvironmentTask = recoveryThreadManager.task;
        OperationalEnvironmentEntry mockCompleteConnected = Mockito.mock(OperationalEnvironmentEntry.class);
        OperationalEnvironmentEntry mockCompleteUnconnected = Mockito.mock(OperationalEnvironmentEntry.class);
        Either<List<OperationalEnvironmentEntry>, CassandraOperationStatus> completeRecordsList = Either.left(Arrays.asList(mockCompleteConnected, mockCompleteUnconnected));
        when(environmentsEngine.isInMap(mockCompleteConnected)).thenReturn(true);
        when(environmentsEngine.isInMap(mockCompleteUnconnected)).thenReturn(false);
        when(operationalEnvironmentDao.getByEnvironmentsStatus(EnvironmentStatusEnum.COMPLETED)).thenReturn(completeRecordsList);
        fixEnvironmentTask.run();

        verify(environmentsEngine).addToMap(mockCompleteUnconnected);
        verify(environmentsEngine, times(0)).addToMap(mockCompleteConnected);
    }
}
