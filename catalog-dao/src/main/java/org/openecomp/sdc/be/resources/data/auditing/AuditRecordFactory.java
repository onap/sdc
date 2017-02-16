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

package org.openecomp.sdc.be.resources.data.auditing;

import java.util.EnumMap;

import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;

public final class AuditRecordFactory {
	public static AuditingGenericEvent createAuditRecord(EnumMap<AuditingFieldsKeysEnum, Object> auditingFields) {
		AuditingActionEnum actionEnum = AuditingActionEnum
				.getActionByName((String) auditingFields.get(AuditingFieldsKeysEnum.AUDIT_ACTION));
		String tableName = actionEnum.getAuditingEsType();
		AuditingGenericEvent event = null;
		switch (tableName) {
		case AuditingTypesConstants.USER_ADMIN_EVENT_TYPE:
			event = new UserAdminEvent(auditingFields);
			break;
		case AuditingTypesConstants.AUTH_EVENT_TYPE:
			event = new AuthEvent(auditingFields);
			break;
		case AuditingTypesConstants.CATEGORY_EVENT_TYPE:
			event = new CategoryEvent(auditingFields);
			break;
		case AuditingTypesConstants.RESOURCE_ADMIN_EVENT_TYPE:
			event = new ResourceAdminEvent(auditingFields);
			break;
		case AuditingTypesConstants.USER_ACCESS_EVENT_TYPE:
			event = new UserAccessEvent(auditingFields);
			break;
		case AuditingTypesConstants.DISTRIBUTION_STATUS_EVENT_TYPE:
			event = new DistributionStatusEvent(auditingFields);
			break;
		case AuditingTypesConstants.DISTRIBUTION_DOWNLOAD_EVENT_TYPE:
			event = new DistributionDownloadEvent(auditingFields);
			break;
		case AuditingTypesConstants.DISTRIBUTION_ENGINE_EVENT_TYPE:
			event = new DistributionEngineEvent(auditingFields);
			break;
		case AuditingTypesConstants.DISTRIBUTION_NOTIFICATION_EVENT_TYPE:
			event = new DistributionNotificationEvent(auditingFields);
			break;
		case AuditingTypesConstants.DISTRIBUTION_DEPLOY_EVENT_TYPE:
			event = new DistributionDeployEvent(auditingFields);
			break;
		case AuditingTypesConstants.DISTRIBUTION_GET_UEB_CLUSTER_EVENT_TYPE:
			event = new AuditingGetUebClusterEvent(auditingFields);
			break;
		case AuditingTypesConstants.CONSUMER_EVENT_TYPE:
			event = new ConsumerEvent(auditingFields);
			break;
		case AuditingTypesConstants.GET_USERS_LIST_EVENT_TYPE:
			event = new GetUsersListEvent(auditingFields);
			break;
		case AuditingTypesConstants.GET_CATEGORY_HIERARCHY_EVENT_TYPE:
			event = new GetCategoryHierarchyEvent(auditingFields);
			break;
		case AuditingTypesConstants.EXTERNAL_API_EVENT_TYPE:
			event = new ExternalApiEvent(auditingFields);
			break;
		}

		return event;
	}

}
