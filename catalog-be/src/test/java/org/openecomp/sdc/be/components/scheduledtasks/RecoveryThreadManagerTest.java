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
import org.apache.commons.lang.math.NumberUtils;
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
import org.openecomp.sdc.common.datastructure.Wrapper;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
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
        MockitoAnnotations.initMocks(this);
        reset(operationalEnvironmentDao, environmentsEngine, scheduledService);
        recoveryThreadManager.init();
    }

    @Test
    public void testInit() {
        verify(scheduledService, Mockito.times(1)).scheduleAtFixedRate(recoveryThreadManager.task,
                NumberUtils.LONG_ZERO, 180L, TimeUnit.SECONDS);

    }
    @SuppressWarnings("unchecked")
    @Test
    public void testTaskNoRecords() {
        FixEnvironmentTask fixEnvironmentTask = recoveryThreadManager.task;
        Either<List<OperationalEnvironmentEntry>, CassandraOperationStatus> emptyList = Either.left(Arrays.asList());
        when(operationalEnvironmentDao.getByEnvironmentsStatus(EnvironmentStatusEnum.FAILED)).thenReturn(emptyList);
        fixEnvironmentTask.run();
        verify(environmentsEngine, Mockito.times(0)).buildOpEnv(Mockito.any(Wrapper.class), Mockito.any(OperationalEnvironmentEntry.class));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testTaskCassandraCrash() {
        FixEnvironmentTask fixEnvironmentTask = recoveryThreadManager.task;
        Either<List<OperationalEnvironmentEntry>, CassandraOperationStatus> eitherResult = Either.right(CassandraOperationStatus.GENERAL_ERROR);
        when(operationalEnvironmentDao.getByEnvironmentsStatus(EnvironmentStatusEnum.FAILED)).thenReturn(eitherResult);
        fixEnvironmentTask.run();
        verify(environmentsEngine, Mockito.times(0)).buildOpEnv(Mockito.any(Wrapper.class), Mockito.any(OperationalEnvironmentEntry.class));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testTaskMultipleFailedRecords() {
        FixEnvironmentTask fixEnvironmentTask = recoveryThreadManager.task;
        OperationalEnvironmentEntry mockEntry = Mockito.mock(OperationalEnvironmentEntry.class);
        Either<List<OperationalEnvironmentEntry>, CassandraOperationStatus> nonEmptyList = Either.left(Arrays.asList(mockEntry, mockEntry, mockEntry));
        Either<List<OperationalEnvironmentEntry>, CassandraOperationStatus> emptyList = Either.left(Arrays.asList());
        when(operationalEnvironmentDao.getByEnvironmentsStatus(EnvironmentStatusEnum.FAILED)).thenReturn(nonEmptyList);
        when(operationalEnvironmentDao.getByEnvironmentsStatus(EnvironmentStatusEnum.IN_PROGRESS)).thenReturn(emptyList);
        fixEnvironmentTask.run();
        verify(environmentsEngine, Mockito.times(3)).buildOpEnv(Mockito.any(Wrapper.class), Mockito.any(OperationalEnvironmentEntry.class));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testTaskStaleRecords() {
        FixEnvironmentTask fixEnvironmentTask = recoveryThreadManager.task;
        OperationalEnvironmentEntry mockFailedEntryFirst = Mockito.mock(OperationalEnvironmentEntry.class);
        OperationalEnvironmentEntry mockFailedEntrySecond = Mockito.mock(OperationalEnvironmentEntry.class);
        Either<List<OperationalEnvironmentEntry>, CassandraOperationStatus> failedRecordsList = Either.left(Arrays.asList(mockFailedEntryFirst, mockFailedEntrySecond));
        when(operationalEnvironmentDao.getByEnvironmentsStatus(EnvironmentStatusEnum.FAILED)).thenReturn(failedRecordsList);

        OperationalEnvironmentEntry mockInProgressNonStaleEntry = Mockito.mock(OperationalEnvironmentEntry.class);
        OperationalEnvironmentEntry mockInProgressStaleEntry = Mockito.mock(OperationalEnvironmentEntry.class);
        doReturn(new Date(System.currentTimeMillis() - recoveryThreadManager.allowedTimeBeforeStaleSec * 1000 /2)).when(mockInProgressNonStaleEntry).getLastModified();
        doReturn(new Date(System.currentTimeMillis() - recoveryThreadManager.allowedTimeBeforeStaleSec * 1000 * 2)).when(mockInProgressStaleEntry).getLastModified();
        Either<List<OperationalEnvironmentEntry>, CassandraOperationStatus> inProgressList = Either.left(Arrays.asList(mockInProgressNonStaleEntry, mockInProgressStaleEntry));
        when(operationalEnvironmentDao.getByEnvironmentsStatus(EnvironmentStatusEnum.IN_PROGRESS)).thenReturn(inProgressList);
        fixEnvironmentTask.run();

        verify(environmentsEngine).buildOpEnv(Mockito.any(Wrapper.class), Mockito.eq(mockFailedEntryFirst));
        verify(environmentsEngine).buildOpEnv(Mockito.any(Wrapper.class), Mockito.eq(mockFailedEntrySecond));
        verify(environmentsEngine).buildOpEnv(Mockito.any(Wrapper.class), Mockito.eq(mockInProgressStaleEntry));
        verify(environmentsEngine, times(0)).buildOpEnv(Mockito.any(Wrapper.class), Mockito.eq(mockInProgressNonStaleEntry));

        verify(environmentsEngine, times(3)).buildOpEnv(Mockito.any(Wrapper.class), Mockito.any(OperationalEnvironmentEntry.class));

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
        when(operationalEnvironmentDao.getByEnvironmentsStatus(EnvironmentStatusEnum.FAILED)).thenReturn(Either.left(Arrays.asList()));
        when(operationalEnvironmentDao.getByEnvironmentsStatus(EnvironmentStatusEnum.IN_PROGRESS)).thenReturn(Either.left(Arrays.asList()));
        fixEnvironmentTask.run();

        verify(environmentsEngine).createUebTopicsForEnvironment(mockCompleteUnconnected);
        verify(environmentsEngine, times(0)).createUebTopicsForEnvironment(mockCompleteConnected);
        verify(environmentsEngine).addToMap(mockCompleteUnconnected);
        verify(environmentsEngine, times(0)).addToMap(mockCompleteConnected);
        verify(environmentsEngine, times(1)).createUebTopicsForEnvironment(Mockito.any(OperationalEnvironmentEntry.class));

    }
}
