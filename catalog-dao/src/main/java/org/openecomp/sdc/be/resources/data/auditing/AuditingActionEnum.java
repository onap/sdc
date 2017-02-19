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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum AuditingActionEnum {

	// User admininstration
	ADD_USER("AddUser", AuditingTypesConstants.USER_ADMIN_EVENT_TYPE), 
	UPDATE_USER("UpdateUser", AuditingTypesConstants.USER_ADMIN_EVENT_TYPE), 
	DELETE_USER("DeleteUser", AuditingTypesConstants.USER_ADMIN_EVENT_TYPE), 
	USER_ACCESS("Access", AuditingTypesConstants.USER_ACCESS_EVENT_TYPE), 
	GET_USERS_LIST("GetUsersList", AuditingTypesConstants.GET_USERS_LIST_EVENT_TYPE),

	// Resource/service administration
	CREATE_RESOURCE("Create", AuditingTypesConstants.RESOURCE_ADMIN_EVENT_TYPE), 
	IMPORT_RESOURCE("ResourceImport", AuditingTypesConstants.RESOURCE_ADMIN_EVENT_TYPE), 
	CHECKOUT_RESOURCE("Checkout", AuditingTypesConstants.RESOURCE_ADMIN_EVENT_TYPE), 
	UNDO_CHECKOUT_RESOURCE("UndoCheckout", AuditingTypesConstants.RESOURCE_ADMIN_EVENT_TYPE), 
	CHECKIN_RESOURCE("Checkin", AuditingTypesConstants.RESOURCE_ADMIN_EVENT_TYPE), 
	CERTIFICATION_REQUEST_RESOURCE("CertificationRequest", AuditingTypesConstants.RESOURCE_ADMIN_EVENT_TYPE), 
	START_CERTIFICATION_RESOURCE("CertificationStart", AuditingTypesConstants.RESOURCE_ADMIN_EVENT_TYPE), 
	FAIL_CERTIFICATION_RESOURCE("CertificationFailure", AuditingTypesConstants.RESOURCE_ADMIN_EVENT_TYPE), 
	CANCEL_CERTIFICATION_RESOURCE("CertificationCancel", AuditingTypesConstants.RESOURCE_ADMIN_EVENT_TYPE), 
	CERTIFICATION_SUCCESS_RESOURCE("CertificationSuccess", AuditingTypesConstants.RESOURCE_ADMIN_EVENT_TYPE), 
	UPDATE_RESOURCE_METADATA("UpdateResourceMetadata", AuditingTypesConstants.RESOURCE_ADMIN_EVENT_TYPE), 
	UPDATE_SERVICE_METADATA("UpdateServiceMetadata", AuditingTypesConstants.RESOURCE_ADMIN_EVENT_TYPE), 
	ARTIFACT_UPLOAD("ArtifactUpload", AuditingTypesConstants.RESOURCE_ADMIN_EVENT_TYPE), 
	ARTIFACT_UPLOAD_BY_API("ArtifactUploadByAPI", AuditingTypesConstants.EXTERNAL_API_EVENT_TYPE), 
	ARTIFACT_UPDATE_BY_API("ArtifactUpdateByAPI", AuditingTypesConstants.EXTERNAL_API_EVENT_TYPE), 
	ARTIFACT_DELETE_BY_API("ArtifactDeleteByAPI", AuditingTypesConstants.EXTERNAL_API_EVENT_TYPE), 
	ARTIFACT_PAYLOAD_UPDATE("ArtifactPayloadUpdate", AuditingTypesConstants.RESOURCE_ADMIN_EVENT_TYPE), 
	ARTIFACT_METADATA_UPDATE("ArtifactMetadataUpdate", AuditingTypesConstants.RESOURCE_ADMIN_EVENT_TYPE), 
	ARTIFACT_DELETE("ArtifactDelete", AuditingTypesConstants.RESOURCE_ADMIN_EVENT_TYPE), 
	ARTIFACT_DOWNLOAD("ArtifactDownload", AuditingTypesConstants.RESOURCE_ADMIN_EVENT_TYPE), 
	DOWNLOAD_ARTIFACT("DownloadArtifact",AuditingTypesConstants.EXTERNAL_API_EVENT_TYPE),

	// Distribution
	DISTRIBUTION_ARTIFACT_DOWNLOAD("DArtifactDownload", AuditingTypesConstants.DISTRIBUTION_DOWNLOAD_EVENT_TYPE), 
	DISTRIBUTION_STATE_CHANGE_REQUEST("DRequest", AuditingTypesConstants.RESOURCE_ADMIN_EVENT_TYPE), 
	DISTRIBUTION_STATE_CHANGE_APPROV("DApprove", AuditingTypesConstants.RESOURCE_ADMIN_EVENT_TYPE), 
	DISTRIBUTION_STATE_CHANGE_REJECT("DReject", AuditingTypesConstants.RESOURCE_ADMIN_EVENT_TYPE), 
	CREATE_DISTRIBUTION_TOPIC("CreateDistributionTopic", AuditingTypesConstants.DISTRIBUTION_ENGINE_EVENT_TYPE), 
	ADD_KEY_TO_TOPIC_ACL("AddKeyToTopicACL", AuditingTypesConstants.DISTRIBUTION_ENGINE_EVENT_TYPE), 
	REMOVE_KEY_FROM_TOPIC_ACL("RemoveKeyFromTopicACL", AuditingTypesConstants.DISTRIBUTION_ENGINE_EVENT_TYPE), 
	DISTRIBUTION_REGISTER("DRegister", AuditingTypesConstants.DISTRIBUTION_ENGINE_EVENT_TYPE), 
	DISTRIBUTION_UN_REGISTER("DUnRegister", AuditingTypesConstants.DISTRIBUTION_ENGINE_EVENT_TYPE),
	DISTRIBUTION_NOTIFY("DNotify", AuditingTypesConstants.DISTRIBUTION_NOTIFICATION_EVENT_TYPE), 
	DISTRIBUTION_STATUS("DStatus", AuditingTypesConstants.DISTRIBUTION_STATUS_EVENT_TYPE), 
	DISTRIBUTION_DEPLOY("DResult",AuditingTypesConstants.DISTRIBUTION_DEPLOY_EVENT_TYPE), 
	GET_UEB_CLUSTER("GetUebCluster", AuditingTypesConstants.DISTRIBUTION_GET_UEB_CLUSTER_EVENT_TYPE), 
	GET_VALID_ARTIFACT_TYPES("GetValidArtifactTypes", AuditingTypesConstants.DISTRIBUTION_GET_VALID_ARTIFACT_TYPES_EVENT_TYPE),
	// ....
	AUTH_REQUEST("HttpAuthentication", AuditingTypesConstants.AUTH_EVENT_TYPE), 
	ADD_ECOMP_USER_CREDENTIALS("AddECOMPUserCredentials", AuditingTypesConstants.CONSUMER_EVENT_TYPE), 
	GET_ECOMP_USER_CREDENTIALS("GetECOMPUserCredentials", AuditingTypesConstants.CONSUMER_EVENT_TYPE), 
	DELETE_ECOMP_USER_CREDENTIALS("DeleteECOMPUserCredentials", AuditingTypesConstants.CONSUMER_EVENT_TYPE), 
	UPDATE_ECOMP_USER_CREDENTIALS("UpdateECOMPUserCredentials", AuditingTypesConstants.CONSUMER_EVENT_TYPE),
	// Category
	ADD_CATEGORY("AddCategory", AuditingTypesConstants.CATEGORY_EVENT_TYPE), 
	ADD_SUB_CATEGORY("AddSubCategory", AuditingTypesConstants.CATEGORY_EVENT_TYPE), 
	ADD_GROUPING("AddGrouping", AuditingTypesConstants.CATEGORY_EVENT_TYPE),
	GET_CATEGORY_HIERARCHY("GetCategoryHierarchy", AuditingTypesConstants.GET_CATEGORY_HIERARCHY_EVENT_TYPE),

	// Assets

	GET_ASSET_LIST("GetAssetList", AuditingTypesConstants.EXTERNAL_API_EVENT_TYPE), 
	GET_FILTERED_ASSET_LIST("GetFilteredAssetList", AuditingTypesConstants.EXTERNAL_API_EVENT_TYPE), 
	GET_ASSET_METADATA("GetAssetMetadata", AuditingTypesConstants.EXTERNAL_API_EVENT_TYPE), 
	GET_TOSCA_MODEL("GetToscaModel", AuditingTypesConstants.EXTERNAL_API_EVENT_TYPE);

	private String name;
	// private Class<? extends AuditingGenericEvent> auditingEsType;
	private String auditingEsType;

	private static Logger log = LoggerFactory.getLogger(AuditingActionEnum.class.getName());

	// AuditingActionEnum(String name, Class<? extends AuditingGenericEvent>
	// auditingEsType){
	// this.name = name;
	// this.auditingEsType = auditingEsType;
	// }

	AuditingActionEnum(String name, String auditingEsType) {
		this.name = name;
		this.auditingEsType = auditingEsType;
	}

	public String getName() {
		return name;
	}

	// public Class<? extends AuditingGenericEvent> getAuditingEsType(){
	// return auditingEsType;
	// }

	public String getAuditingEsType() {
		return auditingEsType;
	}

	public static AuditingActionEnum getActionByName(String name) {
		AuditingActionEnum res = null;
		AuditingActionEnum[] values = values();
		for (AuditingActionEnum value : values) {
			if (value.getName().equals(name)) {
				res = value;
				break;
			}
		}
		if (res == null) {
			log.debug("No auditing action is mapped to name {}", name);
		}
		return res;
	}
}
