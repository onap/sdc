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

import fj.data.Either;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;
import org.openecomp.sdc.be.resources.data.auditing.ResourceAdminEvent;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKey;
import org.openecomp.sdc.common.datastructure.ESTimeBasedEvent;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.Map.Entry;

@Component("auditingDao")
public class AuditingDao extends ESTimeBasedDao {

	private static final String SERVICE_INSTANCE_ID_FIELD = AuditingFieldsKey.AUDIT_SERVICE_INSTANCE_ID
			.getDisplayName(); // "serviceInstanceId";
	private static final String STATUS_FIELD = AuditingFieldsKey.AUDIT_STATUS.getDisplayName(); // "status";
	private static final String ACTION_FIELD = AuditingFieldsKey.AUDIT_ACTION.getDisplayName(); // "action";
	private static final String DISTRIBUTION_ID_FIELD = AuditingFieldsKey.AUDIT_DISTRIBUTION_ID.getDisplayName(); // "distributionId";
	private static Logger log = Logger.getLogger(AuditingDao.class.getName());
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

	public ActionStatus addRecord(Map<AuditingFieldsKey, Object> params, String type) {

		// TODO rhalili - remove? check debugEnabled?
		Map<String, Object> displayFields = new HashMap<>();
		StringBuilder sb = new StringBuilder();
		for (Entry<AuditingFieldsKey, Object> entry : params.entrySet()) {
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

	public ActionStatus addRecord(AuditingGenericEvent auditEvent, String type) {

		log.debug("Auditing: Persisting object of type {}, fields: {}", type, auditEvent.getAction());
//		auditEvent.fillFields();
		return write(type, auditEvent);
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
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Get DistributionStatuses List");
			log.debug("failed to get distribution statuses for ", e);
			return Either.right(ActionStatus.GENERAL_ERROR);
		}
		if (remainingElements != null && !remainingElements.isEmpty()) {
			return Either.left(remainingElements);
		} else {
			log.debug("not found distribution statuses for did {}", did);
			remainingElements = new ArrayList<>();
			return Either.left(remainingElements);
		}

	}

	public Either<List<ESTimeBasedEvent>, ActionStatus> getServiceDistributionStatusesList(String serviceInstanceId) {

		List<ESTimeBasedEvent> resList = new ArrayList<>();
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
			Map<AuditingFieldsKey, Object> filterMap) {

		Iterator<Entry<AuditingFieldsKey, Object>> filterItr = filterMap.entrySet().iterator();
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		while (filterItr.hasNext()) {
			Entry<AuditingFieldsKey, Object> curr = filterItr.next();
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
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Get DistributionStatuses List");
			log.debug("failed to get distribution statuses for action {}", actionType, e);
			return Either.right(ActionStatus.GENERAL_ERROR);
		}

		return Either.left(remainingElements);

	}

	private void populateCommonFields(Map<AuditingFieldsKey, Object> params,
			AuditingGenericEvent timeBasedIndexedData) {
		String dateStr = (String) params.get(AuditingFieldsKey.AUDIT_TIMESTAMP);
		if (dateStr != null) {
			timeBasedIndexedData.setTimestamp(dateStr);
		}
		timeBasedIndexedData.setAction((String) params.get(AuditingFieldsKey.AUDIT_ACTION));
		Object statusObj = params.get(AuditingFieldsKey.AUDIT_STATUS);
		// For BC. status was Integer and is String
		if (statusObj != null) {
			timeBasedIndexedData.setStatus(String.valueOf(statusObj));
		} else {
			timeBasedIndexedData.setStatus(null);
		}
		// timeBasedIndexedData.setStatus((String)params.get(AuditingFieldsKey.AUDIT_STATUS));
		timeBasedIndexedData.setDesc((String) params.get(AuditingFieldsKey.AUDIT_DESC));
		timeBasedIndexedData
				.setServiceInstanceId((String) params.get(AuditingFieldsKey.AUDIT_SERVICE_INSTANCE_ID));
		timeBasedIndexedData.setRequestId((String) params.get(AuditingFieldsKey.AUDIT_REQUEST_ID));
	}

}
