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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.tuple.ImmutablePair;
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
import org.openecomp.sdc.common.config.EcompErrorName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;

import fj.data.Either;

@Component("audit-cassandra-dao")
public class AuditCassandraDao extends CassandraDao {

	private AuditAccessor auditAccessor;

	private static Logger logger = LoggerFactory.getLogger(AuditCassandraDao.class.getName());

	public AuditCassandraDao() {
		super();
	}

	@PostConstruct
	public void init() {
		String keyspace = AuditingTypesConstants.AUDIT_KEYSPACE;
		if (client.isConnected()) {
			Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> result = client.connect(keyspace);
			if (result.isLeft()) {
				session = result.left().value().left;
				manager = result.left().value().right;
				auditAccessor = manager.createAccessor(AuditAccessor.class);
				logger.info("** AuditCassandraDao created");
			} else {
				logger.info("** AuditCassandraDao failed");
				throw new RuntimeException(
						"Audit keyspace [" + keyspace + "] failed to connect with error : " + result.right().value());
			}
		} else {
			logger.info("** Cassandra client isn't connected");
			logger.info("** AuditCassandraDao created, but not connected");
		}

	}

	@SuppressWarnings("unchecked")
	public <T extends AuditingGenericEvent> CassandraOperationStatus saveRecord(T entity) {
		return client.save(entity, (Class<T>) entity.getClass(), manager);
	}

	/**
	 * 
	 * @param did
	 * @return
	 */
	public Either<List<DistributionStatusEvent>, ActionStatus> getListOfDistributionStatuses(String did) {
		List<DistributionStatusEvent> remainingElements = new ArrayList<DistributionStatusEvent>();

		try {
			Result<DistributionStatusEvent> events = auditAccessor.getListOfDistributionStatuses(did);
			if (events == null) {
				logger.debug("not found distribution statuses for did {}", did);
				return Either.left(remainingElements);
			}
			events.all().forEach(event -> {
				event.fillFields();
				remainingElements.add(event);
				logger.debug(event.toString());
			});
			return Either.left(remainingElements);
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError,
					"Get DistributionStatuses List");
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Get DistributionStatuses List");

			logger.debug("failed to get distribution statuses for ", e);
			return Either.right(ActionStatus.GENERAL_ERROR);
		}
	}

	public Either<List<DistributionDeployEvent>, ActionStatus> getDistributionDeployByStatus(String did, String action,
			String status) {
		List<DistributionDeployEvent> remainingElements = new ArrayList<DistributionDeployEvent>();

		try {
			Result<DistributionDeployEvent> events = auditAccessor.getDistributionDeployByStatus(did, action, status);
			if (events == null) {
				logger.debug("not found distribution statuses for did {}", did);
				return Either.left(remainingElements);
			}
			events.all().forEach(event -> {
				event.fillFields();
				remainingElements.add(event);
				logger.debug(event.toString());
			});

			return Either.left(remainingElements);
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError,
					"Get DistributionStatuses List");
			BeEcompErrorManager.getInstance().logBeDaoSystemError("get Distribution Deploy By Status");

			logger.debug("failed to get distribution deploy by status for ", e);
			return Either.right(ActionStatus.GENERAL_ERROR);
		}
	}

	public Either<List<ResourceAdminEvent>, ActionStatus> getDistributionRequest(String did, String action) {
		List<ResourceAdminEvent> remainingElements = new ArrayList<ResourceAdminEvent>();

		try {
			Result<ResourceAdminEvent> events = auditAccessor.getDistributionRequest(did, action);
			if (events == null) {
				logger.debug("not found distribution requests for did {}", did);
				return Either.left(remainingElements);
			}
			events.all().forEach(event -> {
				event.fillFields();
				remainingElements.add(event);
				logger.debug(event.toString());
			});
			return Either.left(remainingElements);
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError,
					"Get DistributionStatuses List");
			BeEcompErrorManager.getInstance().logBeDaoSystemError("get Distribution request");

			logger.debug("failed to get distribution request for ", e);
			return Either.right(ActionStatus.GENERAL_ERROR);
		}
	}

	public Either<List<DistributionNotificationEvent>, ActionStatus> getDistributionNotify(String did, String action) {
		List<DistributionNotificationEvent> remainingElements = new ArrayList<DistributionNotificationEvent>();

		try {
			Result<DistributionNotificationEvent> events = auditAccessor.getDistributionNotify(did, action);
			if (events == null) {
				logger.debug("not found distribution notify for did {}", did);
				return Either.left(remainingElements);
			}
			events.all().forEach(event -> {
				event.fillFields();
				remainingElements.add(event);
				logger.debug(event.toString());
			});

			return Either.left(remainingElements);
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError,
					"Get DistributionStatuses List");
			BeEcompErrorManager.getInstance().logBeDaoSystemError("get Distribution notify");

			logger.debug("failed to get distribution notify for ", e);
			return Either.right(ActionStatus.GENERAL_ERROR);
		}
	}

	public Either<List<ResourceAdminEvent>, ActionStatus> getByServiceInstanceId(String serviceInstanceId) {
		List<ResourceAdminEvent> remainingElements = new ArrayList<ResourceAdminEvent>();

		try {
			Result<ResourceAdminEvent> events = auditAccessor.getByServiceInstanceId(serviceInstanceId);
			if (events == null) {
				logger.debug("not found audit records for serviceInstanceId {}", serviceInstanceId);
				return Either.left(remainingElements);
			}
			events.all().forEach(event -> {
				event.fillFields();
				remainingElements.add(event);
				logger.debug(event.toString());
			});
			return Either.left(remainingElements);
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError,
					"Get DistributionStatuses List");
			BeEcompErrorManager.getInstance().logBeDaoSystemError("get Distribution notify");

			logger.debug("failed to get distribution notify for ", e);
			return Either.right(ActionStatus.GENERAL_ERROR);
		}
	}

	/**
	 * 
	 * @param serviceInstanceId
	 * @return
	 */
	public Either<List<? extends AuditingGenericEvent>, ActionStatus> getServiceDistributionStatusesList(
			String serviceInstanceId) {

		List<AuditingGenericEvent> resList = new ArrayList<>();

		try {
			Result<ResourceAdminEvent> resourceAdminEvents = auditAccessor
					.getServiceDistributionStatus(serviceInstanceId);

			if (resourceAdminEvents != null) {
				resourceAdminEvents.all().forEach(event -> {
					event.fillFields();
					resList.add(event);
					logger.debug(event.toString());
				});
			}

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError,
					"Get Service DistributionStatuses List");
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Get Service DistributionStatuses List");
			logger.debug("failed to get  distribution statuses for action {}",
					AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_REQUEST.getName(), e);
			return Either.right(ActionStatus.GENERAL_ERROR);
		}
		try {
			Result<DistributionDeployEvent> distDeployEvents = auditAccessor
					.getServiceDistributionDeploy(serviceInstanceId);
			if (distDeployEvents != null) {
				distDeployEvents.all().forEach(event -> {
					event.fillFields();
					resList.add(event);
					logger.debug(event.toString());
				});
			}
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError,
					"Get Service DistributionStatuses List");
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Get Service DistributionStatuses List");
			logger.debug("failed to get distribution statuses for action {}",
					AuditingActionEnum.DISTRIBUTION_DEPLOY.getName(), e);
			return Either.right(ActionStatus.GENERAL_ERROR);
		}
		try {
			Result<DistributionNotificationEvent> distNotifEvent = auditAccessor
					.getServiceDistributionNotify(serviceInstanceId);
			if (distNotifEvent != null) {
				distNotifEvent.all().forEach(event -> {
					event.fillFields();
					resList.add(event);
					logger.debug(event.toString());
				});
			}
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError,
					"Get Service DistributionStatuses List");
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Get Service DistributionStatuses List");
			logger.debug("failed to get distribution statuses for action {}",
					AuditingActionEnum.DISTRIBUTION_NOTIFY.getName(), e);
			return Either.right(ActionStatus.GENERAL_ERROR);
		}

		return Either.left(resList);
	}

	public Either<List<ResourceAdminEvent>, ActionStatus> getAuditByServiceIdAndPrevVersion(String serviceInstanceId,
			String prevVersion) {
		List<ResourceAdminEvent> remainingElements = new ArrayList<ResourceAdminEvent>();

		try {
			Result<ResourceAdminEvent> events = auditAccessor.getAuditByServiceIdAndPrevVersion(serviceInstanceId,
					prevVersion);
			if (events == null) {
				logger.debug("not found audit records for serviceInstanceId {} andprevVersion {}", serviceInstanceId,
						prevVersion);
				return Either.left(remainingElements);
			}
			events.all().forEach(event -> {
				event.fillFields();
				remainingElements.add(event);
				logger.debug(event.toString());
			});
			return Either.left(remainingElements);
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError,
					"get Audit By ServiceId And PrevVersion");
			BeEcompErrorManager.getInstance().logBeDaoSystemError("get Audit By ServiceId And PrevVersion");

			logger.debug("failed to getAuditByServiceIdAndPrevVersion ", e);
			return Either.right(ActionStatus.GENERAL_ERROR);
		}
	}

	public Either<List<ResourceAdminEvent>, ActionStatus> getAuditByServiceIdAndCurrVersion(String serviceInstanceId,
			String currVersion) {
		List<ResourceAdminEvent> remainingElements = new ArrayList<ResourceAdminEvent>();

		try {
			Result<ResourceAdminEvent> events = auditAccessor.getAuditByServiceIdAndCurrVersion(serviceInstanceId,
					currVersion);
			if (events == null) {
				logger.debug("not found audit records for serviceInstanceId {} andprevVersion {}", serviceInstanceId,
						currVersion);
				return Either.left(remainingElements);
			}
			events.all().forEach(event -> {
				event.fillFields();
				remainingElements.add(event);
				logger.debug(event.toString());
			});
			return Either.left(remainingElements);
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError,
					"get Audit By ServiceId And CurrVersion");
			BeEcompErrorManager.getInstance().logBeDaoSystemError("get Audit By ServiceId And CurrVersion");

			logger.debug("failed to getAuditByServiceIdAndPrevVersion ", e);
			return Either.right(ActionStatus.GENERAL_ERROR);
		}
	}

	/**
	 * the method checks if the given table is empty in the audit keyspace
	 * 
	 * @param tableName
	 *            the name of the table we want to check
	 * @return true if the table is empty
	 */
	public Either<Boolean, CassandraOperationStatus> isTableEmpty(String tableName) {
		return super.isTableEmpty(tableName);
	}

	/**
	 * ---------for use in JUnit only--------------- the method deletes all the
	 * tables in the audit keyspace
	 * 
	 * @return the status of the last failed operation or ok if all the deletes
	 *         were successful
	 */
	public CassandraOperationStatus deleteAllAudit() {
		logger.info("cleaning all audit tables.");
		String query = "truncate " + AuditingTypesConstants.AUDIT_KEYSPACE + ".";
		try {
			for (Table table : Table.values()) {
				if (table.getTableDescription().getKeyspace().equals(AuditingTypesConstants.AUDIT_KEYSPACE)) {
					logger.debug("clean Audit table:{}", table.getTableDescription().getTableName());
					session.execute(query + table.getTableDescription().getTableName() + ";");
					logger.debug("clean Audit table:{} was succsesfull", table.getTableDescription().getTableName());
				}
			}
		} catch (Exception e) {
			logger.error("Failed to clean Audit", e);
			return CassandraOperationStatus.GENERAL_ERROR;
		}
		logger.info("clean all audit finished succsesfully.");
		return CassandraOperationStatus.OK;
	}
}
