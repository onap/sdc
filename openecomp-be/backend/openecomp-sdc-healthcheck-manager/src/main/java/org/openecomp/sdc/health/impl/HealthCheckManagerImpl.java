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

package org.openecomp.sdc.health.impl;

import com.amdocs.zusammen.commons.health.data.HealthInfo;
import com.amdocs.zusammen.commons.health.data.HealthStatus;
import com.amdocs.zusammen.datatypes.SessionContext;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenAdaptorFactory;
import org.openecomp.core.zusammen.api.ZusammenUtil;
import org.openecomp.sdc.health.HealthCheckDao;
import org.openecomp.sdc.health.HealthCheckDaoFactory;
import org.openecomp.sdc.health.HealthCheckManager;
import org.openecomp.sdc.health.data.HealthCheckStatus;
import org.openecomp.sdc.health.data.MonitoredModules;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class HealthCheckManagerImpl implements HealthCheckManager {

    private static MdcDataDebugMessage mdcDataDebugMessage;
    private HealthCheckDao healthCheckDao;

    private static final Logger logger;

    static {
        mdcDataDebugMessage = new MdcDataDebugMessage();
        logger = LoggerFactory.getLogger(HealthCheckManagerImpl.class);
    }

    public HealthCheckManagerImpl() {
        healthCheckDao = HealthCheckDaoFactory.getInstance().createInterface();
    }

    public String getBEVersion() {
        return this.getClass().getPackage().getImplementationVersion();
    }

    @Override
    public Collection<org.openecomp.sdc.health.data.HealthInfo> checkHealth() {
        org.openecomp.sdc.health.data.HealthInfo zeHealthInfo = null;
        org.openecomp.sdc.health.data.HealthInfo beHealthInfo = new org.openecomp.sdc.health.data.HealthInfo(
                MonitoredModules.BE, HealthCheckStatus.UP, getBEVersion(), "OK");
        org.openecomp.sdc.health.data.HealthInfo cassandraHealthInfo = null;
        String zVersion = "Unknown";
        try {
            SessionContext context = ZusammenUtil.createSessionContext();
            ZusammenAdaptor zusammenAdaptor = ZusammenAdaptorFactory
                    .getInstance().createInterface();
            Collection<HealthInfo> zeHealthInfos = new ArrayList<>();
            try {
                zeHealthInfos = zusammenAdaptor.checkHealth(context);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
                zeHealthInfo = new org.openecomp.sdc.health.data.HealthInfo(
                        MonitoredModules.ZU, HealthCheckStatus.DOWN,
                        zVersion, ex.getMessage());
            }
            boolean cassandraHealth = false;
            String description = "OK";
            try {
                cassandraHealth = healthCheckDao.checkHealth();
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
                description = ex.getMessage();
                cassandraHealthInfo = new org.openecomp.sdc.health.data.HealthInfo(
                        MonitoredModules.CAS, HealthCheckStatus.DOWN, zVersion, ex.getMessage());
            }
            zVersion = zusammenAdaptor.getVersion(context);
            if (cassandraHealthInfo == null) {
                HealthCheckStatus status = cassandraHealth ? HealthCheckStatus.UP : HealthCheckStatus.DOWN;
                if (!cassandraHealth){
                    description = "Cassandra is not available";
                }
                cassandraHealthInfo = new org.openecomp.sdc.health.data.HealthInfo(MonitoredModules.CAS, status,
                        healthCheckDao.getVersion(), description);
            }
            if (zeHealthInfo == null) {
                List<HealthInfo> downHealth = zeHealthInfos.stream().
                        filter(h -> h.getHealthStatus().equals(HealthStatus.DOWN)).
                        collect(Collectors.toList());

                if (downHealth.isEmpty()) {
                    zeHealthInfo = new org.openecomp.sdc.health.data.HealthInfo(
                            MonitoredModules.ZU, HealthCheckStatus.UP,
                            zVersion, "OK");
                } else {
                    String desc = downHealth.stream().map(healthInfo -> healthInfo.getDescription())
                            .collect(Collectors.joining(" , ", "[", "]"));
                    zeHealthInfo = new org.openecomp.sdc.health.data.HealthInfo(
                            MonitoredModules.ZU, HealthCheckStatus.DOWN,
                            zVersion, desc);
                }

            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            zeHealthInfo = new org.openecomp.sdc.health.data.HealthInfo(
                    MonitoredModules.ZU, HealthCheckStatus.DOWN, zVersion, e.getMessage()
            );
            cassandraHealthInfo = new org.openecomp.sdc.health.data.HealthInfo(
                    MonitoredModules.CAS, HealthCheckStatus.DOWN, zVersion, e.getMessage());
        }
        return Arrays.asList(zeHealthInfo, beHealthInfo, cassandraHealthInfo);
    }


}

