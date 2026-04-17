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
package org.openecomp.sdc.be.dao.cassandra;


import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.PagingIterable;

import fj.data.Either;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.schema.Table;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;
import org.openecomp.sdc.be.resources.data.auditing.DistributionDeployEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionNotificationEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionStatusEvent;
import org.openecomp.sdc.be.resources.data.auditing.ResourceAdminEvent;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("audit-cassandra-dao")
public class AuditCassandraDao extends CassandraDao {

    private static Logger logger = Logger.getLogger(AuditCassandraDao.class.getName());
    private CqlSession session;
    private AuditDao auditDao;

    @Autowired
    public AuditCassandraDao(CassandraClient cassandraClient) {
        super(cassandraClient);
    }

    @PostConstruct
public void init() {
    String keyspace = AuditingTypesConstants.AUDIT_KEYSPACE;
    System.out.println("SDC_DEBUG_DISTRIBUTION AuditCassandraDao.init starting. clientConnected=" + client.isConnected() + " keyspace=" + keyspace);
    if (client.isConnected()) {

        Either<CqlSession, CassandraOperationStatus> result = client.connect(keyspace);
        if (result.isLeft()) {

            session = result.left().value();
            System.out.println("SDC_DEBUG_DISTRIBUTION AuditCassandraDao.init connected. sessionKeyspace=" + session.getKeyspace().map(k -> k.asInternal()).orElse("<none>"));

            try {
                AuditDaoMapper auditDaoMapper =
                        new AuditDaoMapperBuilder(session).build();
                auditDao = auditDaoMapper.auditDao(keyspace);
                logger.info("** AuditCassandraDao created");
                System.out.println("SDC_DEBUG_DISTRIBUTION AuditCassandraDao.init mapper created. auditDaoNull=" + (auditDao == null));
            } catch (Exception e) {
                System.out.println("SDC_DEBUG_DISTRIBUTION AuditCassandraDao.init mapper creation failed: " + e);
                e.printStackTrace(System.out);
                throw e;
            }

        } else {
            throw new RuntimeException(
                    "Audit keyspace [" + keyspace + "] failed to connect with error : "
                    + result.right().value());
        }
    } else {
        logger.info("** Cassandra client isn't connected");
        logger.info("** AuditCassandraDao created, but not connected");
        System.out.println("SDC_DEBUG_DISTRIBUTION AuditCassandraDao.init Cassandra client isn't connected");
    }
}


    @SuppressWarnings("unchecked")
    public <T extends AuditingGenericEvent> CassandraOperationStatus saveRecord(T entity) {
        return client.save(entity, (Class<T>) entity.getClass());
    }

    /**
     * @param did
     * @return
     */
    public Either<List<DistributionStatusEvent>, ActionStatus> getListOfDistributionStatuses(String did) {
        List<DistributionStatusEvent> remainingElements = new ArrayList<>();
        try {
            PagingIterable<DistributionStatusEvent> events = auditDao.getListOfDistributionStatuses(did);
            if (events == null) {
                logger.debug("not found distribution statuses for did {}", did);
                return Either.left(remainingElements);
            }
            events.forEach(event -> {
                event.fillFields();
                remainingElements.add(event);
                logger.debug(event.toString());
            });
            return Either.left(remainingElements);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeDaoSystemError("Get DistributionStatuses List");
            logger.error("Failed to get distribution statuses for did {}", did, e);
            return Either.right(ActionStatus.GENERAL_ERROR);
        }
    }

    public Either<List<DistributionDeployEvent>, ActionStatus> getDistributionDeployByStatus(String did, String action, String status) {
        List<DistributionDeployEvent> remainingElements = new ArrayList<>();
        try {
            PagingIterable<DistributionDeployEvent> events = auditDao.getDistributionDeployByStatus(did, action, status);
            if (events == null) {
                logger.debug("not found distribution statuses for did {}", did);
                return Either.left(remainingElements);
            }
            events.forEach(event -> {
                event.fillFields();
                remainingElements.add(event);
                logger.debug(event.toString());
            });
            return Either.left(remainingElements);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeDaoSystemError("get Distribution Deploy By Status");
            logger.error("Failed to get distribution deploy by status did {} action {} status {}", did, action, status, e);
            return Either.right(ActionStatus.GENERAL_ERROR);
        }
    }

    public Either<List<ResourceAdminEvent>, ActionStatus> getDistributionRequest(String did, String action) {
        List<ResourceAdminEvent> remainingElements = new ArrayList<>();
        try {
            PagingIterable<ResourceAdminEvent> events = auditDao.getDistributionRequest(did, action);
            if (events == null) {
                logger.debug("not found distribution requests for did {}", did);
                return Either.left(remainingElements);
            }
            events.forEach(event -> {
                event.fillFields();
                remainingElements.add(event);
                logger.debug(event.toString());
            });
            return Either.left(remainingElements);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeDaoSystemError("get Distribution request");
            logger.error("Failed to get distribution request did {} action {}", did, action, e);
            return Either.right(ActionStatus.GENERAL_ERROR);
        }
    }

    public Either<List<DistributionNotificationEvent>, ActionStatus> getDistributionNotify(String did, String action) {
        List<DistributionNotificationEvent> remainingElements = new ArrayList<>();
        try {
            PagingIterable<DistributionNotificationEvent> events = auditDao.getDistributionNotify(did, action);
            if (events == null) {
                logger.debug("not found distribution notify for did {}", did);
                return Either.left(remainingElements);
            }
            events.forEach(event -> {
                event.fillFields();
                remainingElements.add(event);
                logger.debug(event.toString());
            });
            return Either.left(remainingElements);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeDaoSystemError("get Distribution notify");
            logger.error("Failed to get distribution notify did {} action {}", did, action, e);
            return Either.right(ActionStatus.GENERAL_ERROR);
        }
    }

    public Either<List<ResourceAdminEvent>, ActionStatus> getByServiceInstanceId(String serviceInstanceId) {
        List<ResourceAdminEvent> remainingElements = new ArrayList<>();
        try {
            PagingIterable<ResourceAdminEvent> events = auditDao.getByServiceInstanceId(serviceInstanceId);
            if (events == null) {
                logger.debug("not found audit records for serviceInstanceId {}", serviceInstanceId);
                return Either.left(remainingElements);
            }
            events.forEach(event -> {
                event.fillFields();
                remainingElements.add(event);
                logger.debug(event.toString());
            });
            return Either.left(remainingElements);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeDaoSystemError("get Distribution notify");
            logger.error("Failed to get audit records by serviceInstanceId {}", serviceInstanceId, e);
            return Either.right(ActionStatus.GENERAL_ERROR);
        }
    }

    /**
     * @param serviceInstanceId
     * @return
     */
    public Either<List<? extends AuditingGenericEvent>, ActionStatus> getServiceDistributionStatusesList(String serviceInstanceId) {
        List<AuditingGenericEvent> resList = new ArrayList<>();
        System.out.println("SDC_DEBUG_DISTRIBUTION AuditCassandraDao.getServiceDistributionStatusesList serviceInstanceId=" + serviceInstanceId
            + " auditDaoNull=" + (auditDao == null));
        try {
            final int beforeSize = resList.size();
            PagingIterable<ResourceAdminEvent> resourceAdminEvents = auditDao.getServiceDistributionStatus(serviceInstanceId);
            if (resourceAdminEvents != null) {
                resourceAdminEvents.forEach(event -> {
                    event.fillFields();
                    resList.add(event);
                    logger.debug(event.toString());
                });
            }
            System.out.println("SDC_DEBUG_DISTRIBUTION AuditCassandraDao.getServiceDistributionStatusesList DRequest size="
                + (resList.size() - beforeSize));
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeDaoSystemError("Get Service DistributionStatuses List");
            logger.error("Failed to get service distribution statuses for action {} serviceInstanceId {}",
                AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_REQUEST.getName(), serviceInstanceId, e);
            System.out.println("SDC_DEBUG_DISTRIBUTION AuditCassandraDao.getServiceDistributionStatusesList DRequest failed: " + e);
            e.printStackTrace(System.out);
            return Either.right(ActionStatus.GENERAL_ERROR);
        }
        try {
            final int beforeSize = resList.size();
            PagingIterable<DistributionDeployEvent> distDeployEvents = auditDao.getServiceDistributionDeploy(serviceInstanceId);
            if (distDeployEvents != null) {
                distDeployEvents.forEach(event -> {
                    event.fillFields();
                    resList.add(event);
                    logger.debug(event.toString());
                });
            }
            System.out.println("SDC_DEBUG_DISTRIBUTION AuditCassandraDao.getServiceDistributionStatusesList DResult size="
                + (resList.size() - beforeSize));
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeDaoSystemError("Get Service DistributionStatuses List");
            logger.error("Failed to get service distribution statuses for action {} serviceInstanceId {}",
                AuditingActionEnum.DISTRIBUTION_DEPLOY.getName(), serviceInstanceId, e);
            System.out.println("SDC_DEBUG_DISTRIBUTION AuditCassandraDao.getServiceDistributionStatusesList DResult failed: " + e);
            e.printStackTrace(System.out);
            return Either.right(ActionStatus.GENERAL_ERROR);
        }
        try {
            final int beforeSize = resList.size();
            PagingIterable<DistributionNotificationEvent> distNotifEvent = auditDao.getServiceDistributionNotify(serviceInstanceId);
            if (distNotifEvent != null) {
                distNotifEvent.forEach(event -> {
                    event.fillFields();
                    resList.add(event);
                    logger.debug(event.toString());
                });
            }
            System.out.println("SDC_DEBUG_DISTRIBUTION AuditCassandraDao.getServiceDistributionStatusesList DNotify size="
                + (resList.size() - beforeSize));
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeDaoSystemError("Get Service DistributionStatuses List");
            logger.error("Failed to get service distribution statuses for action {} serviceInstanceId {}",
                AuditingActionEnum.DISTRIBUTION_NOTIFY.getName(), serviceInstanceId, e);
            System.out.println("SDC_DEBUG_DISTRIBUTION AuditCassandraDao.getServiceDistributionStatusesList DNotify failed: " + e);
            e.printStackTrace(System.out);
            return Either.right(ActionStatus.GENERAL_ERROR);
        }
        System.out.println("SDC_DEBUG_DISTRIBUTION AuditCassandraDao.getServiceDistributionStatusesList done. totalEvents=" + resList.size());
        return Either.left(resList);
    }

    public Either<List<ResourceAdminEvent>, ActionStatus> getAuditByServiceIdAndPrevVersion(String serviceInstanceId, String prevVersion) {
        List<ResourceAdminEvent> remainingElements = new ArrayList<>();
        try {
            PagingIterable<ResourceAdminEvent> events = auditDao.getAuditByServiceIdAndPrevVersion(serviceInstanceId, prevVersion);
            if (events == null) {
                logger.debug("not found audit records for serviceInstanceId {} andprevVersion {}", serviceInstanceId, prevVersion);
                return Either.left(remainingElements);
            }
            events.forEach(event -> {
                event.fillFields();
                remainingElements.add(event);
                logger.debug(event.toString());
            });
            return Either.left(remainingElements);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeDaoSystemError("get Audit By ServiceId And PrevVersion");
            logger.debug("failed to getAuditByServiceIdAndPrevVersion ", e);
            return Either.right(ActionStatus.GENERAL_ERROR);
        }
    }

    public Either<List<ResourceAdminEvent>, ActionStatus> getAuditByServiceIdAndCurrVersion(String serviceInstanceId, String currVersion) {
        List<ResourceAdminEvent> remainingElements = new ArrayList<>();
        try {
            PagingIterable<ResourceAdminEvent> events = auditDao.getAuditByServiceIdAndCurrVersion(serviceInstanceId, currVersion);
            if (events == null) {
                logger.debug("not found audit records for serviceInstanceId {} andprevVersion {}", serviceInstanceId, currVersion);
                return Either.left(remainingElements);
            }
            events.forEach(event -> {
                event.fillFields();
                remainingElements.add(event);
                logger.debug(event.toString());
            });
            return Either.left(remainingElements);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeDaoSystemError("get Audit By ServiceId And CurrVersion");
            logger.debug("failed to getAuditByServiceIdAndPrevVersion ", e);
            return Either.right(ActionStatus.GENERAL_ERROR);
        }
    }

    public Either<List<ResourceAdminEvent>, ActionStatus> getArchiveAuditByServiceInstanceId(String serviceInstanceId) {
        List<ResourceAdminEvent> remainingElements = new ArrayList<>();
        try {
            PagingIterable<ResourceAdminEvent> events = auditDao.getArchiveAuditByServiceInstanceId(serviceInstanceId);
            if (events == null) {
                logger.debug("not found audit records for serviceInstanceId {}", serviceInstanceId);
                return Either.left(remainingElements);
            }
            events.forEach(event -> {
                event.fillFields();
                remainingElements.add(event);
                logger.debug(event.toString());
            });
            return Either.left(remainingElements);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeDaoSystemError("getArchiveAuditByServiceInstanceId");
            logger.debug("failed getArchiveAuditByServiceInstanceId ", e);
            return Either.right(ActionStatus.GENERAL_ERROR);
        }
    }

    public Either<List<ResourceAdminEvent>, ActionStatus> getRestoreAuditByServiceInstanceId(String serviceInstanceId) {
        List<ResourceAdminEvent> remainingElements = new ArrayList<>();
        try {
            PagingIterable<ResourceAdminEvent> events = auditDao.getRestoreAuditByServiceInstanceId(serviceInstanceId);
            if (events == null) {
                logger.debug("not found audit records for serviceInstanceId {}", serviceInstanceId);
                return Either.left(remainingElements);
            }
            events.forEach(event -> {
                event.fillFields();
                remainingElements.add(event);
                logger.debug(event.toString());
            });
            return Either.left(remainingElements);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeDaoSystemError("getRestoreAuditByServiceInstanceId");
            logger.debug("failed getRestoreAuditByServiceInstanceId ", e);
            return Either.right(ActionStatus.GENERAL_ERROR);
        }
    }

    /**
     * the method checks if the given table is empty in the audit keyspace
     *
     * @param tableName the name of the table we want to check
     * @return true if the table is empty
     */
    public Either<Boolean, CassandraOperationStatus> isTableEmpty(String tableName) {
        return super.isTableEmpty(tableName);
    }

    /**
     * ---------for use in JUnit only--------------- the method deletes all the tables in the audit keyspace
     *
     * @return the status of the last failed operation or ok if all the deletes were successful
     */
    public CassandraOperationStatus deleteAllAudit() {
        logger.info("cleaning all audit tables.");
        String query = "truncate " + AuditingTypesConstants.AUDIT_KEYSPACE + ".";
        try {
            for (Table table : Table.values()) {
                if (table.getTableDescription().getKeyspace().equals(AuditingTypesConstants.AUDIT_KEYSPACE)) {
                    logger.debug("clean Audit table:{}", table.getTableDescription().getTableName());
                    session.execute(query + table.getTableDescription().getTableName() + ";");
                    logger.debug("clean Audit table:{} was successful", table.getTableDescription().getTableName());
                }
            }
        } catch (Exception e) {
            logger.error("Failed to clean Audit", e);
            return CassandraOperationStatus.GENERAL_ERROR;
        }
        logger.info("cleaning all audit tables finished successfully.");
        return CassandraOperationStatus.OK;
    }
}
