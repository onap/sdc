/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
package org.openecomp.sdc.be.components.distribution.engine;

import com.google.common.annotations.VisibleForTesting;
import fj.data.Either;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.cassandra.OperationalEnvironmentDao;
import org.openecomp.sdc.be.datatypes.enums.EnvironmentStatusEnum;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Keeps the operational environments a service can be distributed to: the environment configured for the distribution engine plus the
 * COMPLETED environments stored in Cassandra.
 */
@Service
public class EnvironmentsEngine {

    private static final String UNKNOWN = "Unknown";
    private static final Logger log = Logger.getLogger(EnvironmentsEngine.class.getName());
    private final OperationalEnvironmentDao operationalEnvironmentDao;
    private final ConfigurationManager configurationManager;
    private Map<String, OperationalEnvironmentEntry> environments = new HashMap<>();

    @Autowired
    public EnvironmentsEngine(OperationalEnvironmentDao operationalEnvironmentDao) {
        this(operationalEnvironmentDao, ConfigurationManager.getConfigurationManager());
    }

    public EnvironmentsEngine(OperationalEnvironmentDao operationalEnvironmentDao, ConfigurationManager configurationManager) {
        this.operationalEnvironmentDao = operationalEnvironmentDao;
        this.configurationManager = configurationManager;
    }

    @VisibleForTesting
    @PostConstruct
    void init() {
        Map<String, OperationalEnvironmentEntry> envs = getEnvironmentsFromDb();
        OperationalEnvironmentEntry confEntry = readEnvFromConfig();
        envs.put(confEntry.getEnvironmentId(), confEntry);
        environments = envs;
        log.info("Environments engine has been initialized with environments {}", environments.keySet());
    }

    private OperationalEnvironmentEntry readEnvFromConfig() {
        OperationalEnvironmentEntry entry = new OperationalEnvironmentEntry();
        DistributionEngineConfiguration distributionEngineConfiguration = configurationManager.getDistributionEngineConfiguration();
        String envName =
            distributionEngineConfiguration.getEnvironments().size() == 1 ? distributionEngineConfiguration.getEnvironments().get(0) : UNKNOWN;
        entry.setEnvironmentId(envName);
        entry.setIsProduction(true);
        entry.setStatus(EnvironmentStatusEnum.COMPLETED);
        log.debug("Environment read from configuration: {}", entry);
        return entry;
    }

    private Map<String, OperationalEnvironmentEntry> getEnvironmentsFromDb() {
        Either<List<OperationalEnvironmentEntry>, CassandraOperationStatus> opEnvResult;
        try {
            opEnvResult = operationalEnvironmentDao.getByEnvironmentsStatus(EnvironmentStatusEnum.COMPLETED);
        } catch (RuntimeException e) {
            log.error("Failed to read the operational environments from Cassandra", e);
            return new HashMap<>();
        }
        if (opEnvResult.isLeft()) {
            Map<String, OperationalEnvironmentEntry> resultMap = opEnvResult.left().value().stream()
                .collect(Collectors.toMap(OperationalEnvironmentEntry::getEnvironmentId, Function.identity()));
            resultMap.forEach((key, value) -> log.debug("Environment loaded from DB: {}", value));
            return resultMap;
        } else {
            CassandraOperationStatus status = opEnvResult.right().value();
            log.debug("Failed to populate Operation Environments Map from Cassandra, DB status: {}", status);
            return new HashMap<>();
        }
    }

    public Map<String, OperationalEnvironmentEntry> getEnvironments() {
        return environments;
    }

    public OperationalEnvironmentEntry getEnvironmentById(String envId) {
        return environments.get(envId);
    }

    public boolean isInMap(OperationalEnvironmentEntry env) {
        return isInMap(env.getEnvironmentId());
    }

    public boolean isInMap(String envId) {
        return environments.containsKey(envId);
    }

    public void addToMap(OperationalEnvironmentEntry opEnvEntry) {
        environments.put(opEnvEntry.getEnvironmentId(), opEnvEntry);
    }
}
