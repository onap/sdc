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

package org.openecomp.sdc.be.auditing.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Formatter;
import java.util.Locale;

import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class AuditingLogFormatUtil {

	// When adding any new fields here, please keep the convention <fieldName>=
	// <value>, with the space between them.
	private static Logger log = LoggerFactory.getLogger(AuditingLogFormatUtil.class.getName());

	// This is the key by which audit marker is recognized in logback.xml
	private static String AUDIT_MARKER_STR = "AUDIT_MARKER";

	public static final Marker auditMarker = MarkerFactory.getMarker(AUDIT_MARKER_STR);

	protected static void logAuditEvent(EnumMap<AuditingFieldsKeysEnum, Object> auditingFields) {

		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb, Locale.US);
		log.trace("logAuditEvent - start");

		try {

			// Common fields
			String modifier = getModifier((String) auditingFields.get(AuditingFieldsKeysEnum.AUDIT_MODIFIER_NAME), (String) auditingFields.get(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID));
			Object statusObj = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_STATUS);
			String status = null;
			if (statusObj != null) {
				status = String.valueOf(statusObj);
			}
			String desc = (String) auditingFields.get(AuditingFieldsKeysEnum.AUDIT_DESC);
			String action = (String) auditingFields.get(AuditingFieldsKeysEnum.AUDIT_ACTION);

			AuditingActionEnum auditEventType = AuditingActionEnum.getActionByName(action);
			StringBuilder formattedEvent = getFormattedEvent(auditingFields, modifier, status, desc, action, auditEventType);
			String formattedString = formattedEvent.toString();

			// This is the only way to fix DE166225 without major refactoring,
			// after it was previously agreed with Ella that activity type will
			// be the method name.

			if (auditEventType.equals(AuditingActionEnum.AUTH_REQUEST)) {
				HttpRequestAuthentication(formattedString);
			} else {
				log.info(auditMarker, formattedString);
			}
		} catch (Exception e) {
			log.debug("unexpected error occurred: {}", e.getMessage(), e);

		} finally {
			formatter.close();
			log.trace("logAuditEvent - end");
		}

	}

	private static void HttpRequestAuthentication(String formattedString) {
		log.info(auditMarker, formattedString);
	}

	private static StringBuilder getFormattedEvent(EnumMap<AuditingFieldsKeysEnum, Object> auditingFields, String modifier, String status, String desc, String action, AuditingActionEnum auditEventType) {

		StringBuilder formattedString = new StringBuilder();

		switch (auditEventType) {
		case ADD_USER:
		case DELETE_USER:
		case UPDATE_USER:
			formattedString = buildStringAccrodingToArray(AuditingLogFormatConstants.USER_ADMIN_TEMPLATE_ARRAY, auditingFields);

			break;
		case USER_ACCESS:
			formattedString = buildStringAccrodingToArray(AuditingLogFormatConstants.USER_ACCESS_TEMPLATE_ARRAY, auditingFields);
			break;
		case DISTRIBUTION_REGISTER:
		case DISTRIBUTION_UN_REGISTER:
			formattedString = buildStringAccrodingToArray(AuditingLogFormatConstants.DISTRIBUTION_REGISTRATION_TEMPLATE_ARRAY, auditingFields);
			break;
		case UPDATE_RESOURCE_METADATA:
		case CREATE_RESOURCE:
		case IMPORT_RESOURCE:
			ArrayList<AuditingFieldsKeysEnum> createResourceList = new ArrayList(Arrays.asList(AuditingLogFormatConstants.CREATE_RESOURCE_TEMPLATE_PREFIX_ARRAY));
			createResourceList.addAll(Arrays.asList(AuditingLogFormatConstants.CREATE_RESOURCE_TEMPLATE_SUFFIX_ARRAY));
			if (auditEventType == AuditingActionEnum.IMPORT_RESOURCE) {
				createResourceList.add(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TOSCA_NODE_TYPE);
			}
			AuditingFieldsKeysEnum[] createResourceArray = new AuditingFieldsKeysEnum[100];
			createResourceArray = createResourceList.toArray(createResourceArray);
			formattedString = buildStringAccrodingToArray(createResourceArray, auditingFields);
			break;
		case CHECKIN_RESOURCE:
		case CHECKOUT_RESOURCE:
		case UNDO_CHECKOUT_RESOURCE:
		case CERTIFICATION_REQUEST_RESOURCE:
		case START_CERTIFICATION_RESOURCE:
		case CERTIFICATION_SUCCESS_RESOURCE:
		case FAIL_CERTIFICATION_RESOURCE:
		case CANCEL_CERTIFICATION_RESOURCE:
			ArrayList<AuditingFieldsKeysEnum> checkinFieldsList = new ArrayList(Arrays.asList(AuditingLogFormatConstants.CREATE_RESOURCE_TEMPLATE_PREFIX_ARRAY));
			checkinFieldsList.add(AuditingFieldsKeysEnum.AUDIT_RESOURCE_COMMENT);
			checkinFieldsList.addAll(Arrays.asList(AuditingLogFormatConstants.CREATE_RESOURCE_TEMPLATE_SUFFIX_ARRAY));
			AuditingFieldsKeysEnum[] checkinFieldsArray = new AuditingFieldsKeysEnum[100];
			checkinFieldsArray = checkinFieldsList.toArray(checkinFieldsArray);
			String comment = (String) auditingFields.get(AuditingFieldsKeysEnum.AUDIT_RESOURCE_COMMENT);
			if (comment == null || comment.equals(Constants.NULL_STRING)) {
				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_COMMENT, Constants.EMPTY_STRING);
			}
			formattedString = buildStringAccrodingToArray(checkinFieldsArray, auditingFields);
			break;
		case ARTIFACT_UPLOAD:
		case ARTIFACT_DELETE:
		case ARTIFACT_METADATA_UPDATE:
		case ARTIFACT_PAYLOAD_UPDATE:
		case ARTIFACT_DOWNLOAD:
			ArrayList<AuditingFieldsKeysEnum> artifactFieldsSet = new ArrayList<>(Arrays.asList(AuditingLogFormatConstants.CREATE_RESOURCE_TEMPLATE_PREFIX_ARRAY)) ;
			artifactFieldsSet.add(AuditingFieldsKeysEnum.AUDIT_PREV_ARTIFACT_UUID);
			artifactFieldsSet.add(AuditingFieldsKeysEnum.AUDIT_CURR_ARTIFACT_UUID);
			artifactFieldsSet.add(AuditingFieldsKeysEnum.AUDIT_ARTIFACT_DATA);
			artifactFieldsSet.addAll(Arrays.asList(AuditingLogFormatConstants.EXTERNAL_DOWNLOAD_ARTIFACT_ARRAY));
			AuditingFieldsKeysEnum[] artifactFieldsArray = new AuditingFieldsKeysEnum[100];
			artifactFieldsArray = artifactFieldsSet.toArray(artifactFieldsArray);
			formattedString = buildStringAccrodingToArray(artifactFieldsArray, auditingFields);
			break;
		case DOWNLOAD_ARTIFACT:
			ArrayList<AuditingFieldsKeysEnum> downloadArtifactFieldsList = new ArrayList(Arrays.asList(AuditingLogFormatConstants.EXTERNAL_DOWNLOAD_ARTIFACT_ARRAY));
			AuditingFieldsKeysEnum[] downloadArtifactFieldsArray = new AuditingFieldsKeysEnum[100];
			artifactFieldsArray = downloadArtifactFieldsList.toArray(downloadArtifactFieldsArray);
			formattedString = buildStringAccrodingToArray(artifactFieldsArray, auditingFields);
			break;
		case DISTRIBUTION_STATE_CHANGE_REQUEST:
			formattedString = buildStringAccrodingToArray(AuditingLogFormatConstants.ACTIVATE_DISTRIBUTION_ARRAY, auditingFields);
			break;
		case DISTRIBUTION_STATE_CHANGE_APPROV:
		case DISTRIBUTION_STATE_CHANGE_REJECT:
			formattedString = buildStringAccrodingToArray(AuditingLogFormatConstants.CHANGE_DISTRIBUTION_STATUS_ARRAY, auditingFields);
			break;
		case CREATE_DISTRIBUTION_TOPIC:
			formattedString = buildStringAccrodingToArray(AuditingLogFormatConstants.CREATE_TOPIC_TEMPLATE_ARRAY, auditingFields);
			break;
		case ADD_KEY_TO_TOPIC_ACL:
		case REMOVE_KEY_FROM_TOPIC_ACL:
			formattedString = buildStringAccrodingToArray(AuditingLogFormatConstants.ADD_REMOVE_TOPIC_KEY_ACL_TEMPLATE_ARRAY, auditingFields);
			break;
		case DISTRIBUTION_STATUS:
			formattedString = buildStringAccrodingToArray(AuditingLogFormatConstants.DISTRIBUTION_STATUS_TEMPLATE_ARRAY, auditingFields);
			break;
		case DISTRIBUTION_NOTIFY:
			formattedString = buildStringAccrodingToArray(AuditingLogFormatConstants.DISTRIBUTION_NOTIFY_ARRAY, auditingFields);
			break;
		case DISTRIBUTION_DEPLOY:
			formattedString = buildStringAccrodingToArray(AuditingLogFormatConstants.DISTRIBUTION_DEPLOY_ARRAY, auditingFields);
			break;
		case GET_UEB_CLUSTER:
			formattedString = buildStringAccrodingToArray(AuditingLogFormatConstants.GET_UEB_CLUSTER_ARRAY, auditingFields);
			break;
		case DISTRIBUTION_ARTIFACT_DOWNLOAD:
			formattedString = buildStringAccrodingToArray(AuditingLogFormatConstants.DISTRIBUTION_DOWNLOAD_TEMPLATE_ARRAY, auditingFields);
			break;
		case AUTH_REQUEST:
			formattedString = buildStringAccrodingToArray(AuditingLogFormatConstants.AUTH_TEMPLATE_ARRAY, auditingFields);
			break;
		case ADD_ECOMP_USER_CREDENTIALS:
		case GET_ECOMP_USER_CREDENTIALS:
		case DELETE_ECOMP_USER_CREDENTIALS:
		case UPDATE_ECOMP_USER_CREDENTIALS:
			formattedString = buildStringAccrodingToArray(AuditingLogFormatConstants.ECOMP_USER_TEMPLATE_ARRAY, auditingFields);
			break;
		case ADD_CATEGORY:
		case ADD_SUB_CATEGORY:
		case ADD_GROUPING:
			formattedString = buildStringAccrodingToArray(AuditingLogFormatConstants.CATEGORY_TEMPLATE_ARRAY, auditingFields);
			break;
		case GET_USERS_LIST:
			formattedString = buildStringAccrodingToArray(AuditingLogFormatConstants.GET_USERS_LIST_TEMPLATE_ARRAY, auditingFields);
			break;
		case GET_CATEGORY_HIERARCHY:
			formattedString = buildStringAccrodingToArray(AuditingLogFormatConstants.GET_CATEGORY_HIERARCHY_TEMPLATE_ARRAY, auditingFields);
			break;
		case GET_ASSET_LIST:
		case GET_FILTERED_ASSET_LIST:
			formattedString = buildStringAccrodingToArray(AuditingLogFormatConstants.EXTERNAL_GET_ASSET_LIST_TEMPLATE_ARRAY, auditingFields);
			break;
		case GET_ASSET_METADATA:
		case GET_TOSCA_MODEL:
			formattedString = buildStringAccrodingToArray(AuditingLogFormatConstants.EXTERNAL_GET_ASSET_TEMPLATE_ARRAY, auditingFields);
			break;
		case ARTIFACT_UPLOAD_BY_API:
		case ARTIFACT_DELETE_BY_API:
		case ARTIFACT_UPDATE_BY_API:
			ArrayList<AuditingFieldsKeysEnum> uploadArtifactFieldsList = new ArrayList(Arrays.asList(AuditingLogFormatConstants.EXTERNAL_CRUD_API_ARTIFACT_ARRAY));
			AuditingFieldsKeysEnum[] uploadArtifactFieldsArray = new AuditingFieldsKeysEnum[100];
			artifactFieldsArray = uploadArtifactFieldsList.toArray(uploadArtifactFieldsArray);
			formattedString = buildStringAccrodingToArray(artifactFieldsArray, auditingFields);
			break;
		case CREATE_RESOURCE_BY_API:
			formattedString = buildStringAccrodingToArray(AuditingLogFormatConstants.EXTERNAL_CRUD_API_ARRAY, auditingFields);
			break;
		case CHANGE_LIFECYCLE_BY_API:
			formattedString = buildStringAccrodingToArray(AuditingLogFormatConstants.EXTERNAL_LYFECYCLE_API_ARRAY, auditingFields);
			break;
		default:
			break;
		}

		return formattedString;
	}

	private static StringBuilder buildStringAccrodingToArray(AuditingFieldsKeysEnum[] sortedFieldsArray, EnumMap<AuditingFieldsKeysEnum, Object> auditingFields) {
		StringBuilder formattedString = new StringBuilder();
		for (int i = 0; i < sortedFieldsArray.length; i++) {
			AuditingFieldsKeysEnum key = sortedFieldsArray[i];

			Object fieldVal = auditingFields.get(key);
			if (fieldVal != null) {
				formattedString.append(key.getDisplayName()).append(" = \"").append(fieldVal).append("\"");
				if (i < sortedFieldsArray.length - 1) {
					formattedString.append(" ");
				}
			}
		}
		return formattedString;
	}

	protected static String getModifier(String modifierName, String modifierUid) {
		if (modifierUid == null || modifierUid.equals(Constants.EMPTY_STRING)) {
			return Constants.EMPTY_STRING;
		}
		StringBuilder sb = new StringBuilder();
		if (modifierName != null) {
			sb.append(modifierName);
		}
		sb.append("(").append(modifierUid).append(")");
		return sb.toString();
	}

	protected static String getUser(String userData) {
		StringBuilder sb = new StringBuilder();
		sb.append(userData);
		return sb.toString();
	}
}
