/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.cassandra.OperationalEnvironmentDao;
import org.openecomp.sdc.be.datatypes.enums.EnvironmentStatusEnum;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;

class EnvironmentsEngineTest {

    private final OperationalEnvironmentDao dao = mock(OperationalEnvironmentDao.class);
    private final ConfigurationManager configurationManager = mock(ConfigurationManager.class);
    private final DistributionEngineConfiguration deConfig = new DistributionEngineConfiguration();

    @BeforeEach
    void setUp() {
        deConfig.setEnvironments(List.of("AUTO"));
        when(configurationManager.getDistributionEngineConfiguration()).thenReturn(deConfig);
        when(dao.getByEnvironmentsStatus(EnvironmentStatusEnum.COMPLETED)).thenReturn(Either.left(List.of()));
    }

    @Test
    void configEnvIsKnownWithoutUebServers() {
        EnvironmentsEngine engine = new EnvironmentsEngine(dao, configurationManager);
        engine.init();
        OperationalEnvironmentEntry env = engine.getEnvironmentById("AUTO");
        assertNotNull(env);
        assertTrue(env.getIsProduction());
        verify(dao, never()).save(any());
    }

    @Test
    void completedCassandraEnvIsStillKnown() {
        OperationalEnvironmentEntry tenant = new OperationalEnvironmentEntry();
        tenant.setEnvironmentId("tenant-1");
        tenant.setStatus(EnvironmentStatusEnum.COMPLETED);
        when(dao.getByEnvironmentsStatus(EnvironmentStatusEnum.COMPLETED)).thenReturn(Either.left(List.of(tenant)));
        EnvironmentsEngine engine = new EnvironmentsEngine(dao, configurationManager);
        engine.init();
        assertNotNull(engine.getEnvironmentById("tenant-1"));
        assertNotNull(engine.getEnvironmentById("AUTO"));
    }

    @Test
    void cassandraFailureStillYieldsConfigEnv() {
        when(dao.getByEnvironmentsStatus(EnvironmentStatusEnum.COMPLETED))
            .thenReturn(Either.right(CassandraOperationStatus.GENERAL_ERROR));
        EnvironmentsEngine engine = new EnvironmentsEngine(dao, configurationManager);
        engine.init();
        assertNotNull(engine.getEnvironmentById("AUTO"));
    }

    @Test
    void cassandraExceptionStillYieldsConfigEnv() {
        when(dao.getByEnvironmentsStatus(EnvironmentStatusEnum.COMPLETED)).thenThrow(new NullPointerException());
        EnvironmentsEngine engine = new EnvironmentsEngine(dao, configurationManager);
        engine.init();
        assertNotNull(engine.getEnvironmentById("AUTO"));
    }
}
