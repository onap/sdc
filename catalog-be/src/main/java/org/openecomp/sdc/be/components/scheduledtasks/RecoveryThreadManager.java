/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import com.google.common.annotations.VisibleForTesting;
import fj.data.Either;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.openecomp.sdc.be.components.distribution.engine.EnvironmentsEngine;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.cassandra.OperationalEnvironmentDao;
import org.openecomp.sdc.be.datatypes.enums.EnvironmentStatusEnum;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.openecomp.sdc.common.datastructure.FunctionalInterfaces.convertToFunction;

@Component("recoveryThreadManager")
public class RecoveryThreadManager extends AbstractScheduleTaskRunner {

    private static final Logger log = Logger.getLogger(RecoveryThreadManager.class);
    @VisibleForTesting
    FixEnvironmentTask task = new FixEnvironmentTask();

    @Resource
    private OperationalEnvironmentDao operationalEnvironmentDao;

    @Autowired
    private EnvironmentsEngine environmentsEngine;

    private ScheduledExecutorService scheduledService = Executors.newScheduledThreadPool(NumberUtils.INTEGER_ONE,
            new BasicThreadFactory.Builder().namingPattern("EnvironmentCleanThread-%d").build());
    @VisibleForTesting
    Integer allowedTimeBeforeStaleSec;

    @PostConstruct
    public void init() {
        log.debug("Enter init method of RecoveryThreadManager");
        final DistributionEngineConfiguration distributionEngineConfiguration = ConfigurationManager
                .getConfigurationManager().getDistributionEngineConfiguration();
        Integer opEnvRecoveryIntervalSec = distributionEngineConfiguration.getOpEnvRecoveryIntervalSec();
        scheduledService.scheduleAtFixedRate(task, NumberUtils.INTEGER_ZERO, opEnvRecoveryIntervalSec,
                TimeUnit.SECONDS);
        this.allowedTimeBeforeStaleSec = distributionEngineConfiguration.getAllowedTimeBeforeStaleSec();
        log.debug("End init method of AsdcComponentsCleaner");
    }

    @PreDestroy
    public void destroy() {
        shutdownExecutor();
    }

    protected class FixEnvironmentTask implements Runnable {
        @Override
        public void run() {
            try {
                // Failed Envs
                Either<List<OperationalEnvironmentEntry>, CassandraOperationStatus> eitherFailedEnv = operationalEnvironmentDao
                        .getByEnvironmentsStatus(EnvironmentStatusEnum.FAILED);
                eitherFailedEnv.bimap(convertToFunction(this::handleFailedeEnvironmentsRecords), convertToFunction(
                        cassandraError -> logFailedRetrieveRecord(EnvironmentStatusEnum.FAILED, cassandraError)));

                // In-Progress Envs
                Either<List<OperationalEnvironmentEntry>, CassandraOperationStatus> eitherInProgressEnv = operationalEnvironmentDao
                        .getByEnvironmentsStatus(EnvironmentStatusEnum.IN_PROGRESS);
                eitherInProgressEnv.bimap(convertToFunction(this::handleInProgressEnvironmentsRecords),
                        convertToFunction(cassandraError -> logFailedRetrieveRecord(EnvironmentStatusEnum.IN_PROGRESS,
                                cassandraError)));

                // Envs To Connect to UEB topics
                Either<List<OperationalEnvironmentEntry>, CassandraOperationStatus> eitherCompleteEnv = operationalEnvironmentDao
                        .getByEnvironmentsStatus(EnvironmentStatusEnum.COMPLETED);
                eitherCompleteEnv.bimap(convertToFunction(this::handleCompleteEnvironmentsRecords), convertToFunction(
                        cassandraError -> logFailedRetrieveRecord(EnvironmentStatusEnum.COMPLETED, cassandraError)));

            } catch (Exception e) {
                log.debug("error while handling operational environments to be fixed :{}", e.getMessage(), e);
            }
        }

        private void handleCompleteEnvironmentsRecords(List<OperationalEnvironmentEntry> completeEnvironmentsRecords) {
            if (!isEmpty(completeEnvironmentsRecords)) {
                completeEnvironmentsRecords.stream().filter(env -> !environmentsEngine.isInMap(env))
                        .forEach(opEnvEntry -> {
                            environmentsEngine.createUebTopicsForEnvironment(opEnvEntry);
                            environmentsEngine.addToMap(opEnvEntry);
                        });
            }

        }

        private void handleFailedeEnvironmentsRecords(List<OperationalEnvironmentEntry> failedEnvironmentsRecords) {
            if (!isEmpty(failedEnvironmentsRecords)) {
                failedEnvironmentsRecords.parallelStream()
                        .forEach(env -> environmentsEngine.buildOpEnv(new Wrapper<>(), env));
            }

        }

        private void handleInProgressEnvironmentsRecords(List<OperationalEnvironmentEntry> inProgressEnvList) {
            if (!isEmpty(inProgressEnvList)) {

                long currentTimeMillis = System.currentTimeMillis();
                if (!isEmpty(inProgressEnvList)) {
                    List<OperationalEnvironmentEntry> staleInProgressEnvList = inProgressEnvList.stream()
                            .filter(record -> (record.getLastModified().getTime() + (allowedTimeBeforeStaleSec * 1000)) < currentTimeMillis)
                            .collect(Collectors.toList());
                    staleInProgressEnvList.parallelStream()
                            .forEach(env -> environmentsEngine.buildOpEnv(new Wrapper<>(), env));
                }

            }

        }

        private void logFailedRetrieveRecord(EnvironmentStatusEnum recordStatus, CassandraOperationStatus error) {
            log.debug("error: {} while retrieving operational environments with status: {}", error, recordStatus);
        }


    }

    @Override
    public ExecutorService getExecutorService() {
        return scheduledService;
    }

}
