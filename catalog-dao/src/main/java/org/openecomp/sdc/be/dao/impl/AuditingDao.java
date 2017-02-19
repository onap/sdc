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

package org.openecomp.sdc.be.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;
import org.openecomp.sdc.be.resources.data.auditing.ResourceAdminEvent;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.common.datastructure.ESTimeBasedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component("auditingDao")
public class AuditingDao extends ESTimeBasedDao {

	private static final String SERVICE_INSTANCE_ID_FIELD = AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID
			.getDisplayName(); // "serviceInstanceId";
	private static final String STATUS_FIELD = AuditingFieldsKeysEnum.AUDIT_STATUS.getDisplayName(); // "status";
	private static final String ACTION_FIELD = AuditingFieldsKeysEnum.AUDIT_ACTION.getDisplayName(); // "action";
	private static final String DISTRIBUTION_ID_FIELD = AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ID.getDisplayName(); // "distributionId";
	private static Logger log = LoggerFactory.getLogger(AuditingDao.class.getName());
	public static final String AUDITING_INDEX = "auditingevents";

	@PostConstruct
	private void init() {
		AuditingActionEnum[] values = AuditingActionEnum.values();
		for (AuditingActionEnum value : values) {
			typesToClasses.put(value.getAuditingEsType(), ESTimeBasedEvent.class);
		}
	}

	@Override
	public String getIndexPrefix() {
		return AUDITING_INDEX;
	}

	public ActionStatus addRecord(Map<AuditingFieldsKeysEnum, Object> params, String type) {

		// TODO rhalili - remove? check debugEnabled?
		Map<String, Object> displayFields = new HashMap<String, Object>();
		StringBuilder sb = new StringBuilder();
		for (Entry<AuditingFieldsKeysEnum, Object> entry : params.entrySet()) {
			displayFields.put(entry.getKey().getDisplayName(), entry.getValue());
			sb.append(entry.getKey().getDisplayName()).append(" = ").append(entry.getValue()).append(",");
		}

		// Persisiting
		// String type = clazz.getSimpleName().toLowerCase();
		AuditingGenericEvent auditingGenericEvent = new AuditingGenericEvent();
		populateCommonFields(params, auditingGenericEvent);
		auditingGenericEvent.getFields().putAll(displayFields);

		log.debug("Auditing: Persisting object of type {}, fields: {}", type, sb.toString());

		return write(type, auditingGenericEvent);
	}

	public Either<List<ESTimeBasedEvent>, ActionStatus> getListOfDistributionStatuses(String did) {

		QueryBuilder componentNameMatch = QueryBuilders.matchQuery(DISTRIBUTION_ID_FIELD, did);
		QueryBuilder componentVersionMatch = QueryBuilders.matchQuery(ACTION_FIELD,
				AuditingActionEnum.DISTRIBUTION_STATUS.getName());
		QueryBuilder remainingElementQueryBuilder = QueryBuilders.boolQuery().must(componentNameMatch)
				.must(componentVersionMatch);
		List<ESTimeBasedEvent> remainingElements = null;
		try {
			remainingElements = customFindEvent(AuditingTypesConstants.DISTRIBUTION_STATUS_EVENT_TYPE,
					remainingElementQueryBuilder, null);
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError,
					"Get DistributionStatuses List");
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Get DistributionStatuses List");

			log.debug("failed to get distribution statuses for ", e);
			return Either.right(ActionStatus.GENERAL_ERROR);
		}
		if (remainingElements != null && !remainingElements.isEmpty()) {
			return Either.left(remainingElements);
		} else {
			log.debug("not found distribution statuses for did {}", did);
			remainingElements = new ArrayList<ESTimeBasedEvent>();
			return Either.left(remainingElements);
		}

	}

	public Either<List<ESTimeBasedEvent>, ActionStatus> getServiceDistributionStatusesList(String serviceInstanceId) {

		List<ESTimeBasedEvent> resList = new ArrayList<ESTimeBasedEvent>();
		QueryBuilder componentNameMatch = QueryBuilders.matchQuery(SERVICE_INSTANCE_ID_FIELD, serviceInstanceId);
		QueryBuilder componentVersionMatch = QueryBuilders.matchQuery(ACTION_FIELD,
				AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_REQUEST.getName());
		QueryBuilder remainingElementQueryBuilder = QueryBuilders.boolQuery().must(componentNameMatch)
				.must(componentVersionMatch);
		List<ESTimeBasedEvent> remainingElements = null;
		try {
			remainingElements = customFindEvent(AuditingTypesConstants.RESOURCE_ADMIN_EVENT_TYPE,
					remainingElementQueryBuilder, null);
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError,
					"Get Service DistributionStatuses List");
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Get Service DistributionStatuses List");
			log.debug("failed to get  distribution statuses for action {}",
					AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_REQUEST.getName(), e);
			return Either.right(ActionStatus.GENERAL_ERROR);
		}
		if (remainingElements != null && !remainingElements.isEmpty()) {
			resList.addAll(remainingElements);
		}

		componentVersionMatch = QueryBuilders.matchQuery(ACTION_FIELD,
				AuditingActionEnum.DISTRIBUTION_DEPLOY.getName());
		remainingElementQueryBuilder = QueryBuilders.boolQuery().must(componentNameMatch).must(componentVersionMatch);
		List<ESTimeBasedEvent> dResultElements = null;
		try {
			dResultElements = customFindEvent(AuditingTypesConstants.DISTRIBUTION_DEPLOY_EVENT_TYPE,
					remainingElementQueryBuilder, null);
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError,
					"Get Service DistributionStatuses List");
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Get Service DistributionStatuses List");
			log.debug("failed to get distribution statuses for action {}",
					AuditingActionEnum.DISTRIBUTION_DEPLOY.getName(), e);
			return Either.right(ActionStatus.GENERAL_ERROR);
		}
		if (dResultElements != null && !dResultElements.isEmpty()) {
			resList.addAll(dResultElements);
		}

		componentVersionMatch = QueryBuilders.matchQuery(ACTION_FIELD,
				AuditingActionEnum.DISTRIBUTION_NOTIFY.getName());
		remainingElementQueryBuilder = QueryBuilders.boolQuery().must(componentNameMatch).must(componentVersionMatch);
		List<ESTimeBasedEvent> dNotifyElements = null;
		try {
			dNotifyElements = customFindEvent(AuditingTypesConstants.DISTRIBUTION_NOTIFICATION_EVENT_TYPE,
					remainingElementQueryBuilder, null);
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError,
					"Get Service DistributionStatuses List");
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Get Service DistributionStatuses List");
			log.debug("failed to get distribution statuses for action {}",
					AuditingActionEnum.DISTRIBUTION_NOTIFY.getName(), e);
			return Either.right(ActionStatus.GENERAL_ERROR);
		}
		if (remainingElements != null && !remainingElements.isEmpty()) {
			resList.addAll(dNotifyElements);
		}

		return Either.left(resList);

	}

	public Either<List<ESTimeBasedEvent>, ActionStatus> getFilteredResourceAdminAuditingEvents(
			Map<AuditingFieldsKeysEnum, Object> filterMap) {

		Iterator<Entry<AuditingFieldsKeysEnum, Object>> filterItr = filterMap.entrySet().iterator();
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		while (filterItr.hasNext()) {
			Entry<AuditingFieldsKeysEnum, Object> curr = filterItr.next();
			boolQuery = boolQuery.must(QueryBuilders.termQuery(curr.getKey().getDisplayName(), curr.getValue()));
		}

		try {
			List<ESTimeBasedEvent> customFindEvent = customFindEvent(
					ResourceAdminEvent.class.getSimpleName().toLowerCase(), boolQuery, null);
			return Either.left(customFindEvent);
		} catch (Exception e) {
			log.debug("Failed to query AuditRecords in es");
			return Either.right(ActionStatus.GENERAL_ERROR);
		}

	}

	public Either<List<ESTimeBasedEvent>, ActionStatus> getListOfDistributionByAction(String did, String actionType,
			String requestedStatus, Class<? extends AuditingGenericEvent> clazz) {

		QueryBuilder distributionIdMatch = QueryBuilders.matchQuery(DISTRIBUTION_ID_FIELD, did);
		QueryBuilder distributionActionMatch = QueryBuilders.matchQuery(ACTION_FIELD, actionType);
		QueryBuilder remainingElementQueryBuilder;

		if (requestedStatus != null && !requestedStatus.isEmpty()) {
			QueryBuilder statusMatch = QueryBuilders.matchQuery(STATUS_FIELD, requestedStatus);
			remainingElementQueryBuilder = QueryBuilders.boolQuery().must(distributionIdMatch)
					.must(distributionActionMatch).must(statusMatch);
		} else {
			remainingElementQueryBuilder = QueryBuilders.boolQuery().must(distributionIdMatch)
					.must(distributionActionMatch);
		}

		List<ESTimeBasedEvent> remainingElements = null;
		try {
			remainingElements = customFindEvent(clazz.getSimpleName().toLowerCase(), remainingElementQueryBuilder,
					null);
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError,
					"Get DistributionStatuses List");
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Get DistributionStatuses List");
			log.debug("failed to get distribution statuses for action {}", actionType, e);
			return Either.right(ActionStatus.GENERAL_ERROR);
		}

		return Either.left(remainingElements);

	}

	private void populateCommonFields(Map<AuditingFieldsKeysEnum, Object> params,
			AuditingGenericEvent timeBasedIndexedData) {
		String dateStr = (String) params.get(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP);
		if (dateStr != null) {
			timeBasedIndexedData.setTimestamp(dateStr);
		}
		timeBasedIndexedData.setAction((String) params.get(AuditingFieldsKeysEnum.AUDIT_ACTION));
		Object statusObj = params.get(AuditingFieldsKeysEnum.AUDIT_STATUS);
		// For BC. status was Integer and is String
		if (statusObj != null) {
			timeBasedIndexedData.setStatus(String.valueOf(statusObj));
		} else {
			timeBasedIndexedData.setStatus(null);
		}
		// timeBasedIndexedData.setStatus((String)params.get(AuditingFieldsKeysEnum.AUDIT_STATUS));
		timeBasedIndexedData.setDesc((String) params.get(AuditingFieldsKeysEnum.AUDIT_DESC));
		timeBasedIndexedData
				.setServiceInstanceId((String) params.get(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID));
		timeBasedIndexedData.setRequestId((String) params.get(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID));
	}

}
