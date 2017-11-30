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

package org.openecomp.sdc.ci.tests.utils.validation;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.javatuples.Pair;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.ConsumerDataDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.GroupingDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.AuditEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.AuditJsonKeysEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ComponentType;
import org.openecomp.sdc.ci.tests.datatypes.enums.ErrorInfo;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedAuthenticationAudit;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedCategoryAudit;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedDistDownloadAudit;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedEcomConsumerAudit;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedExternalAudit;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedGetUserListAudit;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedProductAudit;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedResourceAuditJavaObject;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedUserCRUDAudit;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.run.StartTest;
import org.openecomp.sdc.ci.tests.utils.ArtifactUtils;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.cassandra.CassandraUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.CategoryRestUtils.CategoryAuditJsonKeysEnum;
import org.openecomp.sdc.ci.tests.utils.rest.ConsumerRestUtils.EcompConsumerAuditJsonKeysEnum;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.Row;

public class AuditValidationUtils {
	protected static Logger logger = Logger.getLogger(AuditValidationUtils.class.getName());
	private static final String auditKeySpaceName = "sdcaudit";

	public AuditValidationUtils() {
		super();

		StartTest.enableLogger();
		logger = Logger.getLogger(AuditValidationUtils.class.getName());

	}

	public static String buildAuditDescription(ErrorInfo errorInfo, List<String> errorVariablesList) {
		String auditDesc;
		if(errorInfo.getMessageId() != null){
			auditDesc = errorInfo.getMessageId() + ": " + errorInfo.getMessage();
			}else{
				auditDesc = errorInfo.getMessage();
			}
		
//		if(! variables.isEmpty() && variables.get(0) != null && ! variables.get(0).isEmpty()){
//			for (int i = 0; i < variables.size(); i++) {
//				if (auditDesc.contains("%" + (i + 1))) {
//					auditDesc = auditDesc.replace("%" + (i + 1), variables.get(i));
//				}
//			}
//		}
		if(errorVariablesList != null && ! errorVariablesList.isEmpty() && errorVariablesList.get(0) != null){
			for (int i = 0; i < errorVariablesList.size(); i++) {
				if (auditDesc.contains("%" + (i + 1))) {
					auditDesc = auditDesc.replace("%" + (i + 1), errorVariablesList.get(i));
				}
			}
		}
		return auditDesc;
	}

	public static String getModifierString(String userName, String uid) {

		if (userName.isEmpty() && uid.isEmpty())
			return "(UNKNOWN)";

		StringBuilder sb = new StringBuilder();
		sb.append(userName).append("(").append(uid).append(")");
		return sb.toString();

	}

	public static void validateAuditDownloadExternalAPI(ExpectedResourceAuditJavaObject resourceAuditJavaObject,
			String action, String body, boolean checkAllFields) throws Exception {
		Map<String, Object> actualAuditRecords = new HashMap<String, Object>();
		// Andrey's comment
		// actualAuditRecords = parseAuditResourceByAction(action, body);
		actualAuditRecords = parseAuditResourceByAction(action, null);

		// List<Map<String, Object>> actualAuditRecords = new
		// ArrayList<Map<String, Object>>();
		// actualAuditRecords = parseAuditResourceByActionToList(action, body);

		validateField(actualAuditRecords, AuditJsonKeysEnum.ACTION.getAuditJsonKeyName(), action);
		validateField(actualAuditRecords, AuditJsonKeysEnum.RESOURCE_NAME.getAuditJsonKeyName(),
				resourceAuditJavaObject.getResourceName());
		validateField(actualAuditRecords, AuditJsonKeysEnum.RESOURCE_TYPE.getAuditJsonKeyName(),
				resourceAuditJavaObject.getResourceType());

		validateField(actualAuditRecords, AuditJsonKeysEnum.STATUS.getAuditJsonKeyName(),
				resourceAuditJavaObject.getStatus());
		validateField(actualAuditRecords, AuditJsonKeysEnum.DESCRIPTION.getAuditJsonKeyName(),
				resourceAuditJavaObject.getDesc());

		// validateField(actualAuditRecords,
		// AuditJsonKeysEnum.CONSUMER_ID.getAuditJsonKeyName(),
		// resourceAuditJavaObject.getCONSUMER_ID());
		// validateField(actualAuditRecords,
		// AuditJsonKeysEnum.RESOURCE_URL.getAuditJsonKeyName(),
		// resourceAuditJavaObject.getRESOURCE_URL());

	}

	public static void validateAudit(ExpectedResourceAuditJavaObject resourceAuditJavaObject, String action,
			String body, boolean checkAllFields) throws Exception {
		Map<String, Object> actualAuditRecords = new HashMap<String, Object>();
		// Andrey's comment
		// actualAuditRecords = parseAuditResourceByAction(action, body);
		actualAuditRecords = parseAuditResourceByAction(action, null);

		if ((resourceAuditJavaObject.getModifierName() != null) && (resourceAuditJavaObject.getModifierUid() != null)) {
			resourceAuditJavaObject.setModifierUid(getModifierString(resourceAuditJavaObject.getModifierName(),
					resourceAuditJavaObject.getModifierUid()));
		}

		validateField(actualAuditRecords, AuditJsonKeysEnum.ACTION.getAuditJsonKeyName(), action);
		validateField(actualAuditRecords, AuditJsonKeysEnum.RESOURCE_NAME.getAuditJsonKeyName(),
				resourceAuditJavaObject.getResourceName());
		validateField(actualAuditRecords, AuditJsonKeysEnum.RESOURCE_TYPE.getAuditJsonKeyName(),
				resourceAuditJavaObject.getResourceType());
		validateField(actualAuditRecords, AuditJsonKeysEnum.PREV_VERSION.getAuditJsonKeyName(),
				resourceAuditJavaObject.getPrevVersion());
		validateField(actualAuditRecords, AuditJsonKeysEnum.CURR_VERSION.getAuditJsonKeyName(),
				resourceAuditJavaObject.getCurrVersion());

		validateField(actualAuditRecords, AuditJsonKeysEnum.MODIFIER.getAuditJsonKeyName(),
				resourceAuditJavaObject.getModifierUid());
		validateField(actualAuditRecords, AuditJsonKeysEnum.PREV_STATE.getAuditJsonKeyName(),
				resourceAuditJavaObject.getPrevState());
		validateField(actualAuditRecords, AuditJsonKeysEnum.CURR_STATE.getAuditJsonKeyName(),
				resourceAuditJavaObject.getCurrState());
		validateField(actualAuditRecords, AuditJsonKeysEnum.STATUS.getAuditJsonKeyName(),
				resourceAuditJavaObject.getStatus());
		// validateField(map2, AuditJsonKeysEnum.STATUS.getAuditJsonKeyName(),
		// Double.parseDouble(resourceAuditJavaObject.getStatus()));
		validateField(actualAuditRecords, AuditJsonKeysEnum.DESCRIPTION.getAuditJsonKeyName(),
				resourceAuditJavaObject.getDesc());
		validateField(actualAuditRecords, AuditJsonKeysEnum.COMMENT.getAuditJsonKeyName(),
				resourceAuditJavaObject.getComment());
		// validateField(map2,
		// AuditJsonKeysEnum.ARTIFACT_DATA.getAuditJsonKeyName(),
		// resourceAuditJavaObject.getArtifactData());
		validateField(actualAuditRecords, AuditJsonKeysEnum.TOSCA_NODE_TYPE.getAuditJsonKeyName(),
				resourceAuditJavaObject.getToscaNodeType());
		validateField(actualAuditRecords, AuditJsonKeysEnum.CURR_ARTIFACT_UUID.getAuditJsonKeyName(),
				resourceAuditJavaObject.getCurrArtifactUuid());
		validateField(actualAuditRecords, AuditJsonKeysEnum.PREV_ARTIFACT_UUID.getAuditJsonKeyName(),
				resourceAuditJavaObject.getPrevArtifactUuid());

		validateAtifactDataField(actualAuditRecords, AuditJsonKeysEnum.ARTIFACT_DATA.getAuditJsonKeyName(),
				resourceAuditJavaObject.getArtifactData(), checkAllFields);
	}

	public static void validateExternalAudit(ExpectedExternalAudit externalAuditObject, String action,
			Map<AuditingFieldsKeysEnum, String> body) throws Exception {

		Map<String, Object> actualAuditRecord = new HashMap<String, Object>();
		actualAuditRecord = parseAuditResourceByAction(action, body);

		validateField(actualAuditRecord, AuditJsonKeysEnum.ACTION.getAuditJsonKeyName(), action);
		validateField(actualAuditRecord, AuditJsonKeysEnum.CONSUMER_ID.getAuditJsonKeyName(),
				externalAuditObject.getCONSUMER_ID());
		// TODO
		validateField(actualAuditRecord, AuditJsonKeysEnum.RESOURCE_URL.getAuditJsonKeyName(),
				externalAuditObject.getRESOURCE_URL());
		//TODO
		validateField(actualAuditRecord, AuditJsonKeysEnum.RESOURCE_NAME.getAuditJsonKeyName(),
				externalAuditObject.getRESOURCE_NAME());
		validateField(actualAuditRecord, AuditJsonKeysEnum.SERVICE_INSTANCE_ID.getAuditJsonKeyName(),
				externalAuditObject.getSERVICE_INSTANCE_ID());
		//TODO
		validateField(actualAuditRecord, AuditJsonKeysEnum.RESOURCE_TYPE.getAuditJsonKeyName(),
				externalAuditObject.getRESOURCE_TYPE());
		validateField(actualAuditRecord, AuditJsonKeysEnum.STATUS.getAuditJsonKeyName(),
				externalAuditObject.getSTATUS());
		validateField(actualAuditRecord, AuditJsonKeysEnum.DESCRIPTION.getAuditJsonKeyName(),
				externalAuditObject.getDESC());
		//TODO
//		validateField(actualAuditRecord, AuditJsonKeysEnum.MODIFIER.getAuditJsonKeyName(),
//				externalAuditObject.getMODIFIER());
		validateField(actualAuditRecord, AuditJsonKeysEnum.PREV_ARTIFACT_UUID.getAuditJsonKeyName(),
				externalAuditObject.getPREV_ARTIFACT_UUID());
		validateField(actualAuditRecord, AuditJsonKeysEnum.CURR_ARTIFACT_UUID.getAuditJsonKeyName(),
				externalAuditObject.getCURR_ARTIFACT_UUID());
		//TODO
		validateField(actualAuditRecord, AuditJsonKeysEnum.ARTIFACT_DATA.getAuditJsonKeyName(),
				externalAuditObject.getARTIFACT_DATA());

	}

	public enum ArtifactDataFieldEnum {
		attGroup, artLable, artType, artName, artTimeout, artPayloadUUID, artVersion, artUUID
	}

	private static void validateAtifactDataField(Map<String, Object> map, String auditJsonKeyName,
			String expectedArtifactData, boolean checkAllFields) {
		Map<ArtifactDataFieldEnum, String> expectedArtifactDataFileds = new HashMap<ArtifactDataFieldEnum, String>();
		Map<ArtifactDataFieldEnum, String> actualAtifactDataFileds = new HashMap<ArtifactDataFieldEnum, String>();
		if (expectedArtifactData != null) {
			String[] expected = expectedArtifactData.split(",");

			assertTrue("Audit field " + auditJsonKeyName + " not found", map.containsKey(auditJsonKeyName));
			String actualValue = (String) map.get(auditJsonKeyName);
			String[] actual = actualValue.split(",");

			if (expected.length == 1 && actual.length == 1) {
				assertEquals(expectedArtifactData, actualValue);
				return;
			}

			assertEquals(ArtifactDataFieldEnum.values().length, expected.length);
			assertEquals(ArtifactDataFieldEnum.values().length, actual.length);

			for (ArtifactDataFieldEnum field : ArtifactDataFieldEnum.values()) {

				expectedArtifactDataFileds.put(field, expected[field.ordinal()]);
				actualAtifactDataFileds.put(field, actual[field.ordinal()]);
			}
			for (Map.Entry<ArtifactDataFieldEnum, String> entry : expectedArtifactDataFileds.entrySet()) {
				ArtifactDataFieldEnum field = entry.getKey();
				if (checkAllFields || (!field.equals(ArtifactDataFieldEnum.artVersion)
						&& !field.equals(ArtifactDataFieldEnum.artUUID))) {
					assertTrue("Audit field ArtifactData dosn't containt " + field,
							actualAtifactDataFileds.containsKey(field));
					assertEquals("Audit field ArtifactData dosn't equal " + field,
							expectedArtifactDataFileds.get(field), actualAtifactDataFileds.get(field));
				}

			}
		}
	}

	// //Benny
	public static void validateEcompConsumerAudit(ExpectedEcomConsumerAudit ecompConsumerAuditJavaObject, String action)
			throws Exception {

		String fixedAction = BaseRestUtils.encodeUrlForDownload(action);
		Map<String, Object> map2 = new HashMap<String, Object>();
		map2 = parseAuditResourceByAction(fixedAction, null);

		validateField(map2, EcompConsumerAuditJsonKeysEnum.MODIFIER.getAuditJsonKeyName(),
				ecompConsumerAuditJavaObject.getModifier());
		validateField(map2, EcompConsumerAuditJsonKeysEnum.ECOMP_USER.getAuditJsonKeyName(),
				ecompConsumerAuditJavaObject.getEcomUser());
		validateField(map2, EcompConsumerAuditJsonKeysEnum.STATUS.getAuditJsonKeyName(),
				ecompConsumerAuditJavaObject.getStatus());
		validateField(map2, EcompConsumerAuditJsonKeysEnum.DESC.getAuditJsonKeyName(),
				ecompConsumerAuditJavaObject.getDesc());
		validateField(map2, EcompConsumerAuditJsonKeysEnum.ACTION.getAuditJsonKeyName(), action);
	}

	public static void ecompConsumerAuditSuccess(String action, ConsumerDataDefinition consumerDataDefinition,
			User user, int status) throws Exception {
		ExpectedEcomConsumerAudit expectedEcomConsumerAuditJavaObject = new ExpectedEcomConsumerAudit();
		expectedEcomConsumerAuditJavaObject.setAction(action);
		expectedEcomConsumerAuditJavaObject.setEcomUser(
				consumerDataDefinition.getConsumerName() + "," + consumerDataDefinition.getConsumerSalt().toLowerCase()
						+ "," + consumerDataDefinition.getConsumerPassword().toLowerCase());
		expectedEcomConsumerAuditJavaObject.setStatus(String.valueOf(status));
		expectedEcomConsumerAuditJavaObject.setDesc("OK");
		expectedEcomConsumerAuditJavaObject.setModifier(user.getFullName() + "(" + user.getUserId() + ")");
		AuditValidationUtils.validateEcompConsumerAudit(expectedEcomConsumerAuditJavaObject, action);
	}

	public static void createEcompConsumerAuditFailure(String action, ConsumerDataDefinition consumerDataDefinition,
			User user, ActionStatus errorMessage, Object... variables) throws Exception {
		// validate audit
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(errorMessage.name());
		ExpectedEcomConsumerAudit expectedEcomConsumerAuditJavaObject = new ExpectedEcomConsumerAudit();
		expectedEcomConsumerAuditJavaObject.setAction(action);
		expectedEcomConsumerAuditJavaObject.setEcomUser(
				consumerDataDefinition.getConsumerName() + "," + consumerDataDefinition.getConsumerSalt().toLowerCase()
						+ "," + consumerDataDefinition.getConsumerPassword().toLowerCase());
		expectedEcomConsumerAuditJavaObject.setStatus(errorInfo.getCode().toString());
		expectedEcomConsumerAuditJavaObject.setDesc(errorInfo.getAuditDesc(variables));
		expectedEcomConsumerAuditJavaObject.setModifier(user.getFullName() + "(" + user.getUserId() + ")");
		AuditValidationUtils.validateEcompConsumerAudit(expectedEcomConsumerAuditJavaObject, action);
	}

	public static void deleteEcompConsumerAuditFailure(String action, ConsumerDataDefinition consumerDataDefinition,
			User user, ActionStatus errorMessage, Object... variables) throws Exception {
		// validate audit
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(errorMessage.name());
		ExpectedEcomConsumerAudit expectedEcomConsumerAuditJavaObject = new ExpectedEcomConsumerAudit();
		// String auditAction = ADD_ECOMP_USER_CREDENTIALS_AUDIT_ACTION;
		expectedEcomConsumerAuditJavaObject.setAction(action);
		expectedEcomConsumerAuditJavaObject.setEcomUser(consumerDataDefinition.getConsumerName());
		expectedEcomConsumerAuditJavaObject.setStatus(errorInfo.getCode().toString());
		expectedEcomConsumerAuditJavaObject.setDesc(errorInfo.getAuditDesc(variables));
		expectedEcomConsumerAuditJavaObject.setModifier(user.getFullName() + "(" + user.getUserId() + ")");
		AuditValidationUtils.validateEcompConsumerAudit(expectedEcomConsumerAuditJavaObject, action);
	}

	////////////////////// US571255
	public static void GetListOfUsersByRolesAuditFailure(String action, String roles, int status, User userModifier,
			ActionStatus errorMessage, Object... variables) throws Exception {
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(errorMessage.name());
		ExpectedGetUserListAudit expectedGetListOfUsersAuditJavaObject = new ExpectedGetUserListAudit(); // String
																											// auditAction
																											// =
																											// ADD_ECOMP_USER_CREDENTIALS_AUDIT_ACTION;
		expectedGetListOfUsersAuditJavaObject.setAction(action);
		expectedGetListOfUsersAuditJavaObject.setStatus(String.valueOf(status));
		expectedGetListOfUsersAuditJavaObject.setDesc(errorInfo.getAuditDesc(variables));

		expectedGetListOfUsersAuditJavaObject.setDetails(roles);
		if (errorMessage == ActionStatus.USER_INACTIVE || errorMessage == ActionStatus.MISSING_INFORMATION) {
			expectedGetListOfUsersAuditJavaObject.setModifier("(UNKNOWN)");
		} else {
			expectedGetListOfUsersAuditJavaObject
					.setModifier(userModifier.getFullName() + "(" + userModifier.getUserId() + ")");
		}
		AuditValidationUtils.validateAuditGetListOfUsersByRoles(expectedGetListOfUsersAuditJavaObject, action);
	}

	public static void GetListOfUsersByRolesAuditSuccess(String action, String roles, User user, int status)
			throws Exception {
		ExpectedGetUserListAudit expectedGetListOfUsersAuditJavaObject = new ExpectedGetUserListAudit();
		expectedGetListOfUsersAuditJavaObject.setAction(action);
		expectedGetListOfUsersAuditJavaObject.setStatus(String.valueOf(status));
		expectedGetListOfUsersAuditJavaObject.setDesc("OK");
		expectedGetListOfUsersAuditJavaObject.setModifier(user.getFullName() + "(" + user.getUserId() + ")");
		expectedGetListOfUsersAuditJavaObject.setDetails(roles);
		validateAuditGetListOfUsersByRoles(expectedGetListOfUsersAuditJavaObject, action);
	}

	public static void validateAuditGetListOfUsersByRoles(ExpectedGetUserListAudit GetListOfUsersAuditJavaObject,
			String action) throws Exception {

		Map<String, Object> map2 = new HashMap<String, Object>();
		map2 = parseAuditResourceByAction(action, null);
		validateField(map2, AuditJsonKeysEnum.ACTION.getAuditJsonKeyName(), action);
		validateField(map2, AuditJsonKeysEnum.STATUS.getAuditJsonKeyName(), GetListOfUsersAuditJavaObject.getStatus());
		validateField(map2, AuditJsonKeysEnum.MODIFIER.getAuditJsonKeyName(),
				GetListOfUsersAuditJavaObject.getModifier());
		validateField(map2, AuditJsonKeysEnum.DESCRIPTION.getAuditJsonKeyName(),
				GetListOfUsersAuditJavaObject.getDesc());
		validateField(map2, AuditJsonKeysEnum.DETAILS.getAuditJsonKeyName(),
				GetListOfUsersAuditJavaObject.getDetails());
	}

	public static void validateAuditImport(ExpectedResourceAuditJavaObject resourceAuditJavaObject, String action)
			throws Exception {

		Map<String, Object> map2 = new HashMap<String, Object>();
		map2 = parseAuditResourceByAction(action, null);

		resourceAuditJavaObject.setModifierUid(
				getModifierString(resourceAuditJavaObject.getModifierName(), resourceAuditJavaObject.getModifierUid()));

		validateField(map2, AuditJsonKeysEnum.ACTION.getAuditJsonKeyName(), action);
		validateField(map2, AuditJsonKeysEnum.RESOURCE_TYPE.getAuditJsonKeyName(),
				resourceAuditJavaObject.getResourceType());
		validateField(map2, AuditJsonKeysEnum.PREV_VERSION.getAuditJsonKeyName(),
				resourceAuditJavaObject.getPrevVersion());
		validateField(map2, AuditJsonKeysEnum.CURR_VERSION.getAuditJsonKeyName(),
				resourceAuditJavaObject.getCurrVersion());
		validateField(map2, AuditJsonKeysEnum.MODIFIER.getAuditJsonKeyName(), resourceAuditJavaObject.getModifierUid());
		validateField(map2, AuditJsonKeysEnum.PREV_STATE.getAuditJsonKeyName(), resourceAuditJavaObject.getPrevState());
		validateField(map2, AuditJsonKeysEnum.CURR_STATE.getAuditJsonKeyName(), resourceAuditJavaObject.getCurrState());
		validateField(map2, AuditJsonKeysEnum.STATUS.getAuditJsonKeyName(), resourceAuditJavaObject.getStatus());
		validateField(map2, AuditJsonKeysEnum.DESCRIPTION.getAuditJsonKeyName(), resourceAuditJavaObject.getDesc());

	}

	public static void validateAuditDistribution(ExpectedResourceAuditJavaObject resourceAuditJavaObject, String action)
			throws Exception {

		Map<String, Object> map2 = new HashMap<String, Object>();
		map2 = parseAuditResourceByAction(action, null);

		resourceAuditJavaObject.setModifierUid(
				getModifierString(resourceAuditJavaObject.getModifierName(), resourceAuditJavaObject.getModifierUid()));

		validateField(map2, AuditJsonKeysEnum.ACTION.getAuditJsonKeyName(), action);
		validateField(map2, AuditJsonKeysEnum.RESOURCE_NAME.getAuditJsonKeyName(),
				resourceAuditJavaObject.getResourceName());
		validateField(map2, AuditJsonKeysEnum.RESOURCE_TYPE.getAuditJsonKeyName(),
				resourceAuditJavaObject.getResourceType());
		validateField(map2, AuditJsonKeysEnum.CURR_VERSION.getAuditJsonKeyName(),
				resourceAuditJavaObject.getCurrVersion());
		validateField(map2, AuditJsonKeysEnum.MODIFIER.getAuditJsonKeyName(), resourceAuditJavaObject.getModifierUid());
		validateField(map2, AuditJsonKeysEnum.CURR_STATE.getAuditJsonKeyName(), resourceAuditJavaObject.getCurrState());
		validateField(map2, AuditJsonKeysEnum.DPREV_STATUS.getAuditJsonKeyName(),
				resourceAuditJavaObject.getDprevStatus());
		validateField(map2, AuditJsonKeysEnum.DCURR_STATUS.getAuditJsonKeyName(),
				resourceAuditJavaObject.getDcurrStatus());
		validateField(map2, AuditJsonKeysEnum.STATUS.getAuditJsonKeyName(), resourceAuditJavaObject.getStatus());
		validateField(map2, AuditJsonKeysEnum.DESCRIPTION.getAuditJsonKeyName(), resourceAuditJavaObject.getDesc());
		validateField(map2, AuditJsonKeysEnum.COMMENT.getAuditJsonKeyName(), resourceAuditJavaObject.getComment());
		validateField(map2, AuditJsonKeysEnum.DID.getAuditJsonKeyName(), resourceAuditJavaObject.getDistributionId());

	}

	// Benny
	public static void validateAudit_Distribution(ExpectedResourceAuditJavaObject resourceAuditJavaObject,
			String action) throws Exception {

		List<Map<String, Object>> actionToList = getAuditListByAction(resourceAuditJavaObject.getAction(), 1);
		Map<String, Object> map2 = actionToList.get(0);
		validateField(map2, AuditJsonKeysEnum.ACTION.getAuditJsonKeyName(), action);
		validateField(map2, AuditJsonKeysEnum.RESOURCE_NAME.getAuditJsonKeyName(),
				resourceAuditJavaObject.getResourceName());
		validateField(map2, AuditJsonKeysEnum.RESOURCE_TYPE.getAuditJsonKeyName(),
				resourceAuditJavaObject.getResourceType());
		validateField(map2, AuditJsonKeysEnum.CURR_VERSION.getAuditJsonKeyName(),
				resourceAuditJavaObject.getCurrVersion());
		validateField(map2, AuditJsonKeysEnum.MODIFIER.getAuditJsonKeyName(), resourceAuditJavaObject.getMODIFIER());
		validateField(map2, AuditJsonKeysEnum.CURR_STATE.getAuditJsonKeyName(), resourceAuditJavaObject.getCurrState());
		validateField(map2, AuditJsonKeysEnum.DPREV_STATUS.getAuditJsonKeyName(),
				resourceAuditJavaObject.getDprevStatus());
		validateField(map2, AuditJsonKeysEnum.DCURR_STATUS.getAuditJsonKeyName(),
				resourceAuditJavaObject.getDcurrStatus());
		validateField(map2, AuditJsonKeysEnum.STATUS.getAuditJsonKeyName(), resourceAuditJavaObject.getStatus());
		validateField(map2, AuditJsonKeysEnum.DESCRIPTION.getAuditJsonKeyName(), resourceAuditJavaObject.getDesc());
		validateField(map2, AuditJsonKeysEnum.COMMENT.getAuditJsonKeyName(), resourceAuditJavaObject.getComment());

	}

	public void validateAuditNotification(ExpectedResourceAuditJavaObject resourceAuditJavaObject, String action)
			throws Exception {

		Map<String, Object> map2 = new HashMap<String, Object>();
		map2 = parseAuditResourceByAction(action, null);

		resourceAuditJavaObject.setModifierUid(
				getModifierString(resourceAuditJavaObject.getModifierName(), resourceAuditJavaObject.getModifierUid()));

		validateField(map2, AuditJsonKeysEnum.ACTION.getAuditJsonKeyName(), action);
		validateField(map2, AuditJsonKeysEnum.RESOURCE_NAME.getAuditJsonKeyName(),
				resourceAuditJavaObject.getResourceName());
		validateField(map2, AuditJsonKeysEnum.RESOURCE_TYPE.getAuditJsonKeyName(),
				resourceAuditJavaObject.getResourceType());
		validateField(map2, AuditJsonKeysEnum.CURR_VERSION.getAuditJsonKeyName(),
				resourceAuditJavaObject.getCurrVersion());
		validateField(map2, AuditJsonKeysEnum.CURR_STATE.getAuditJsonKeyName(), resourceAuditJavaObject.getCurrState());
		validateField(map2, AuditJsonKeysEnum.STATUS.getAuditJsonKeyName(), resourceAuditJavaObject.getStatus());
		validateField(map2, AuditJsonKeysEnum.DESCRIPTION.getAuditJsonKeyName(), resourceAuditJavaObject.getDesc());
		validateField(map2, AuditJsonKeysEnum.DID.getAuditJsonKeyName(), resourceAuditJavaObject.getDistributionId());
		validateField(map2, AuditJsonKeysEnum.TOPIC_NAME.getAuditJsonKeyName(), resourceAuditJavaObject.getTopicName());

	}

	public static void validateAudit(ExpectedDistDownloadAudit expectedDistDownloadAudit, String action)
			throws Exception {

		Map<String, Object> map2 = new HashMap<String, Object>();
		map2 = parseAuditResourceByAction(action, null);

		validateField(map2, AuditJsonKeysEnum.ACTION.getAuditJsonKeyName(), action);
		validateField(map2, AuditJsonKeysEnum.STATUS.getAuditJsonKeyName(), expectedDistDownloadAudit.getStatus());
		validateField(map2, AuditJsonKeysEnum.DESCRIPTION.getAuditJsonKeyName(), expectedDistDownloadAudit.getDesc());
		validateField(map2, AuditJsonKeysEnum.CONSUMER_ID.getAuditJsonKeyName(),
				expectedDistDownloadAudit.getConsumerId());
		validateField(map2, AuditJsonKeysEnum.RESOURCE_URL.getAuditJsonKeyName(),
				expectedDistDownloadAudit.getResourceUrl());
	}
	
	public static void validateAuditExternalSearchAPI(ExpectedExternalAudit expectedDistDownloadAudit, String action, Map<AuditingFieldsKeysEnum, String> body)
			throws Exception {

		Map<String, Object> map2 = new HashMap<String, Object>();
		map2 = parseAuditResourceByAction(action, body);

		validateField(map2, AuditJsonKeysEnum.ACTION.getAuditJsonKeyName(), action);
		validateField(map2, AuditJsonKeysEnum.STATUS.getAuditJsonKeyName(), expectedDistDownloadAudit.getSTATUS());
		validateField(map2, AuditJsonKeysEnum.DESCRIPTION.getAuditJsonKeyName(), expectedDistDownloadAudit.getDESC());
		validateField(map2, AuditJsonKeysEnum.CONSUMER_ID.getAuditJsonKeyName(),
				expectedDistDownloadAudit.getCONSUMER_ID());
		validateField(map2, AuditJsonKeysEnum.RESOURCE_URL.getAuditJsonKeyName(),
				expectedDistDownloadAudit.getRESOURCE_URL());
	}
	
	public static void validateAuditExternalCreateResource(ExpectedResourceAuditJavaObject expectedExternalAudit, String action, Map<AuditingFieldsKeysEnum, String> body) throws Exception {
	Map<String, Object> map2 = new HashMap<String, Object>();
		map2 = parseAuditResourceByAction(action, body);

		validateField(map2, AuditJsonKeysEnum.ACTION.getAuditJsonKeyName(), action);
		validateField(map2, AuditJsonKeysEnum.RESOURCE_NAME.getAuditJsonKeyName(), expectedExternalAudit.getResourceName());
		validateField(map2, AuditJsonKeysEnum.RESOURCE_TYPE.getAuditJsonKeyName(), expectedExternalAudit.getResourceType());
		validateField(map2, AuditJsonKeysEnum.CONSUMER_ID.getAuditJsonKeyName(), expectedExternalAudit.getCONSUMER_ID());
		validateField(map2, AuditJsonKeysEnum.RESOURCE_URL.getAuditJsonKeyName(), expectedExternalAudit.getRESOURCE_URL());
		validateField(map2, AuditJsonKeysEnum.MODIFIER.getAuditJsonKeyName(), expectedExternalAudit.getMODIFIER());
		
		validateField(map2, AuditJsonKeysEnum.PREV_VERSION.getAuditJsonKeyName(), expectedExternalAudit.getPrevVersion());
		validateField(map2, AuditJsonKeysEnum.CURR_VERSION.getAuditJsonKeyName(), expectedExternalAudit.getCurrVersion());
		validateField(map2, AuditJsonKeysEnum.PREV_STATE.getAuditJsonKeyName(), expectedExternalAudit.getPrevState());
		validateField(map2, AuditJsonKeysEnum.CURR_STATE.getAuditJsonKeyName(), expectedExternalAudit.getCurrState());
		
		validateField(map2, AuditJsonKeysEnum.STATUS.getAuditJsonKeyName(), expectedExternalAudit.getStatus());
		validateField(map2, AuditJsonKeysEnum.DESCRIPTION.getAuditJsonKeyName(), expectedExternalAudit.getDesc());
	}
	
	public static void validateAuditExternalChangeAssetLifeCycle(ExpectedResourceAuditJavaObject expectedExternalAudit, String action, Map<AuditingFieldsKeysEnum, String> body) throws Exception {
		Map<String, Object> map2 = new HashMap<String, Object>();
		map2 = parseAuditResourceByAction(action, body);

		validateField(map2, AuditJsonKeysEnum.ACTION.getAuditJsonKeyName(), action);
		validateField(map2, AuditJsonKeysEnum.DESCRIPTION.getAuditJsonKeyName(), expectedExternalAudit.getDesc());
		validateField(map2, AuditJsonKeysEnum.STATUS.getAuditJsonKeyName(), expectedExternalAudit.getStatus());

		validateField(map2, AuditJsonKeysEnum.RESOURCE_TYPE.getAuditJsonKeyName(), expectedExternalAudit.getResourceType());
		validateField(map2, AuditJsonKeysEnum.RESOURCE_NAME.getAuditJsonKeyName(), expectedExternalAudit.getResourceName());
		validateField(map2, AuditJsonKeysEnum.CONSUMER_ID.getAuditJsonKeyName(), expectedExternalAudit.getCONSUMER_ID());
		validateField(map2, AuditJsonKeysEnum.RESOURCE_URL.getAuditJsonKeyName(), expectedExternalAudit.getRESOURCE_URL());
		validateField(map2, AuditJsonKeysEnum.MODIFIER.getAuditJsonKeyName(), expectedExternalAudit.getMODIFIER());
		
		validateField(map2, AuditJsonKeysEnum.PREV_VERSION.getAuditJsonKeyName(), expectedExternalAudit.getPrevVersion());
		validateField(map2, AuditJsonKeysEnum.CURR_VERSION.getAuditJsonKeyName(), expectedExternalAudit.getCurrVersion());
		validateField(map2, AuditJsonKeysEnum.PREV_STATE.getAuditJsonKeyName(), expectedExternalAudit.getPrevState());
		validateField(map2, AuditJsonKeysEnum.CURR_STATE.getAuditJsonKeyName(), expectedExternalAudit.getCurrState());
		
		
		// TODO: Remove comment
//		validateField(map2, AuditJsonKeysEnum.INVARIANT_UUID.getAuditJsonKeyName(), expectedExternalAudit.getINVARIANT_UUID());
	}

	public void validateAuditDeploy(ExpectedResourceAuditJavaObject resourceAuditJavaObject, String action)
			throws Exception {

		Map<String, Object> map2 = new HashMap<String, Object>();
		map2 = parseAuditResourceByAction(action, null);

		resourceAuditJavaObject.setModifierUid(
				getModifierString(resourceAuditJavaObject.getModifierName(), resourceAuditJavaObject.getModifierUid()));

		validateField(map2, AuditJsonKeysEnum.ACTION.getAuditJsonKeyName(), action);
		validateField(map2, AuditJsonKeysEnum.RESOURCE_NAME.getAuditJsonKeyName(),
				resourceAuditJavaObject.getResourceName());
		validateField(map2, AuditJsonKeysEnum.RESOURCE_TYPE.getAuditJsonKeyName(),
				resourceAuditJavaObject.getResourceType());
		validateField(map2, AuditJsonKeysEnum.CURR_VERSION.getAuditJsonKeyName(),
				resourceAuditJavaObject.getCurrVersion());
		validateField(map2, AuditJsonKeysEnum.MODIFIER.getAuditJsonKeyName(), resourceAuditJavaObject.getModifierUid());
		validateField(map2, AuditJsonKeysEnum.STATUS.getAuditJsonKeyName(), resourceAuditJavaObject.getStatus());
		validateField(map2, AuditJsonKeysEnum.DESCRIPTION.getAuditJsonKeyName(), resourceAuditJavaObject.getDesc());
		validateField(map2, AuditJsonKeysEnum.DID.getAuditJsonKeyName(), resourceAuditJavaObject.getDistributionId());

	}

	public static void validateAuditProduct(ExpectedProductAudit productExpectedAudit, String action,
			AuditJsonKeysEnum... additionalFields) throws Exception {

		Map<String, Object> map2 = new HashMap<String, Object>();
		map2 = parseAuditResourceByAction(action, null);

		validateField(map2, AuditJsonKeysEnum.ACTION.getAuditJsonKeyName(), action);
		validateField(map2, AuditJsonKeysEnum.RESOURCE_NAME.getAuditJsonKeyName(),
				productExpectedAudit.getRESOURCE_NAME());
		validateField(map2, AuditJsonKeysEnum.RESOURCE_TYPE.getAuditJsonKeyName(),
				productExpectedAudit.getRESOURCE_TYPE());
		validateField(map2, AuditJsonKeysEnum.PREV_VERSION.getAuditJsonKeyName(),
				productExpectedAudit.getPREV_VERSION());
		validateField(map2, AuditJsonKeysEnum.CURR_VERSION.getAuditJsonKeyName(),
				productExpectedAudit.getCURR_VERSION());
		validateField(map2, AuditJsonKeysEnum.PREV_STATE.getAuditJsonKeyName(), productExpectedAudit.getPREV_STATE());
		validateField(map2, AuditJsonKeysEnum.CURR_STATE.getAuditJsonKeyName(), productExpectedAudit.getCURR_STATE());
		validateField(map2, AuditJsonKeysEnum.MODIFIER.getAuditJsonKeyName(), productExpectedAudit.getMODIFIER());
		validateField(map2, AuditJsonKeysEnum.STATUS.getAuditJsonKeyName(), productExpectedAudit.getSTATUS());
		validateField(map2, AuditJsonKeysEnum.DESCRIPTION.getAuditJsonKeyName(), productExpectedAudit.getDESC());
		validateField(map2, AuditJsonKeysEnum.SERVICE_INSTANCE_ID.getAuditJsonKeyName(),
				productExpectedAudit.getSERVICE_INSTANCE_ID());
		if (additionalFields != null) {
			List<AuditJsonKeysEnum> fieldsList = Arrays.asList(additionalFields);
			if (fieldsList.contains(AuditJsonKeysEnum.COMMENT)) {
				validateField(map2, AuditJsonKeysEnum.COMMENT.getAuditJsonKeyName(), productExpectedAudit.getCOMMENT());
			}
		}
	}

	private static List<Map<String, Object>> getAuditListByAction(String action, int expectedNumOfAudit)
			throws Exception {
		List<Map<String, Object>> actionToList = parseAuditResourceByActionToList(action, null);
		assertEquals("recieved different audits number than expected", expectedNumOfAudit, actionToList.size());
		return actionToList;
	}

	public static void validateAuthenticationAudit(ExpectedAuthenticationAudit expectedAudit) throws Exception {
		List<Map<String, Object>> actionToList = getAuditListByAction(expectedAudit.getAction(), 1);
		assertEquals("expected number of ES action is 1", 1, actionToList.size());

		Map<String, Object> map = actionToList.get(0);
		validateField(map, AuditEnum.ACTION.getValue(), expectedAudit.getAction());
		validateField(map, AuditEnum.URL.getValue(), expectedAudit.getUrl());
		validateField(map, AuditEnum.USER.getValue(), expectedAudit.getUser());
		validateField(map, AuditEnum.AUTH_STATUS.getValue(), expectedAudit.getAuthStatus());
		validateField(map, AuditEnum.REALM.getValue(), expectedAudit.getRealm());

	}

	private static void validateField(Map<String, Object> actualAuditRecord, String jsonField, Object expectedValue) {
		if (expectedValue == null) {
			// || changed to &&
			if (actualAuditRecord.containsKey(jsonField)) {
				assertTrue("Audit field " + jsonField + ": expected null, actual " + actualAuditRecord.get(jsonField),
						actualAuditRecord.get(jsonField).toString().equals("null")
								|| actualAuditRecord.get(jsonField).toString().equals(Constants.EMPTY_STRING));
			}

		} else {
			assertTrue("Audit field " + jsonField + " not found in actual", actualAuditRecord.containsKey(jsonField));
			Object foundValue = actualAuditRecord.get(jsonField);
			compareElements(expectedValue, foundValue);
		}
	}

	public static void compareElements(Object expectedValue, Object foundValue) {
		if (expectedValue instanceof String) {
			assertTrue("Actual value " + foundValue + " is not string", foundValue instanceof String);
			assertTrue("Expected " + "[" + expectedValue +"]" + " not equal to actual [" + foundValue + "]", foundValue.equals(expectedValue));
		}
		/*
		 * else if( expectedValue instanceof Number){ assertTrue(foundValue
		 * instanceof Number); assertTrue(foundValue == expectedValue); }
		 */
		else if (expectedValue instanceof Boolean) {
			assertTrue(foundValue instanceof Boolean);
			assertTrue(foundValue == expectedValue);
		} else if (expectedValue instanceof Map) {
			assertTrue(foundValue instanceof Map);
			Map<String, Object> foundMap = (Map<String, Object>) foundValue;
			Map<String, Object> excpectedMap = (Map<String, Object>) expectedValue;
			assertTrue("size of maps is not equel", foundMap.size() == excpectedMap.size());
			Iterator<String> foundkeyItr = foundMap.keySet().iterator();
			while (foundkeyItr.hasNext()) {
				String foundKey = foundkeyItr.next();
				assertTrue(excpectedMap.containsKey(foundKey));
				compareElements(excpectedMap.get(foundKey), foundMap.get(foundKey));
			}

		} else if (expectedValue instanceof List) {
			assertTrue(foundValue instanceof List);
			List<Object> foundList = (List<Object>) foundValue;
			List<Object> excpectedList = (List<Object>) expectedValue;
			assertTrue("size of maps is not equel", foundList.size() == excpectedList.size());
            if( foundList.size() > 0 ){
                final Object elementInList = foundList.get(0);
                if( !(elementInList instanceof List || elementInList instanceof Map) ){
                       List<Object> tempList = new ArrayList<>();
                       tempList.addAll(foundList);
                       for(Object expectedObj : excpectedList){
                              if( tempList.contains(expectedObj)){
                                     tempList.remove(expectedObj);
                              }
                              else{
                            	  assertTrue(false);
                              }
                       }
                       assertTrue("Lists are not equel", tempList.isEmpty());
                }
            }

		} else {
			assertTrue(foundValue.equals(expectedValue));
		}
	}

	// public static Map<String, Object> parseAuditResourceByAction(String
	// action, String body) throws Exception {
	//
	// Map auditingMessage = null;
	// auditingMessage = retrieveAuditMessagesByPattern(action, null);
	//
	// return auditingMessage;
	//
	// }

	public static Map<String, Object> parseAuditResourceByAction(String action, Map<AuditingFieldsKeysEnum, String> body) throws Exception {

		Map auditingMessage = null;
		auditingMessage = retrieveAuditMessagesByPattern(action, body, false);
		return auditingMessage;

	}

	public static List<Map<String, Object>> parseAuditResourceByActionToList(String action,
			Map<AuditingFieldsKeysEnum, String> body) throws Exception {

		Map auditingMessage = null;

		if (body == null || body.isEmpty()) {
			auditingMessage = retrieveAuditMessagesByPattern(action, null, false);
		} else {
			auditingMessage = retrieveAuditMessagesByPattern(action, body, false);
		}

		return ResponseParser.getAuditFromMessage(auditingMessage);
	}

	public JSONObject buildElasticQueryStringObject(String defaultField, String queryValue) throws JSONException {

		JSONObject query_string = new JSONObject();
		JSONObject jSONObject = new JSONObject();
		jSONObject.put("default_field", defaultField);
		jSONObject.put("query", queryValue);

		query_string.put("query_string", jSONObject);

		return query_string;
	}

	public static JSONObject buildElasticQueryBody(List<JSONObject> listObjects) throws JSONException {

		JSONObject query = new JSONObject();
		JSONObject bool = new JSONObject();
		JSONObject must = new JSONObject();
		JSONArray mustA = new JSONArray();

		for (int i = 0; i < listObjects.size(); i++) {
			JSONObject match = new JSONObject();
			match.put("match", listObjects.get(i));
			mustA.put(match);

		}

		must.put("must", mustA);
		bool.put("bool", must);
		query.put("query", bool);

		return query;
	}

	public static String retrieveAuditMessagesUsingBody(String query_string) throws IOException {

		Config config = Utils.getConfig();
		HttpRequest getAuditingMessage = new HttpRequest();
		Map<String, String> headersMap = new HashMap<String, String>();
		String body = query_string;

		String url = String.format(Urls.GET_SEARCH_DATA_FROM_ES, config.getEsHost(), config.getEsPort(), "_search");
		RestResponse restResponse = getAuditingMessage.httpSendPost(url, body, headersMap);

		return restResponse.getResponse();
	}

	public static Map<String, String> retrieveAuditMessagesByPattern(String action, Map<AuditingFieldsKeysEnum, String> body, Boolean retryFlag)
			throws IOException {

		// get cassandra table name by action
		String esType = AuditingActionEnum.getActionByName(action).getAuditingEsType();
		Map<String, String> resultsMap = new HashMap<String, String>();

		List<Pair<AuditingFieldsKeysEnum, String>> myFields = new ArrayList<Pair<AuditingFieldsKeysEnum, String>>();
		Pair<AuditingFieldsKeysEnum, String> myPair = new Pair<AuditingFieldsKeysEnum, String>(
				AuditingFieldsKeysEnum.AUDIT_ACTION, action);
		myFields.add(0, myPair);
		if (body != null && !body.isEmpty()) {
			for (Map.Entry<AuditingFieldsKeysEnum, String> mapElement : body.entrySet()) {
				myFields.add(new Pair<AuditingFieldsKeysEnum, String>(mapElement.getKey(), mapElement.getValue()));
			}
		}

		List<Row> fetchFromTable = CassandraUtils.fetchFromTable(auditKeySpaceName, esType, myFields);
		if(retryFlag){
			if(fetchFromTable.size() == 0){
				return resultsMap;
			}
		}
//		assertTrue("expected on fetching from data base one record only, actual: " + fetchFromTable.size(), fetchFromTable.size() == 1);
		
		
		
		
		Row row =null;
		
		if (fetchFromTable.size() > 1){
			List<Row> fetchFromTable2 = fetchFromTable;
			fetchFromTable2.sort((p1, p2) -> p1.getTimestamp(1).compareTo(p2.getTimestamp(1)));
			row = fetchFromTable2.get(fetchFromTable2.size() - 1);
		}
		else {row = fetchFromTable.get(0);}

		ColumnDefinitions columnDefinitions = row.getColumnDefinitions();

		for (int i = 0; i < columnDefinitions.size(); i++) {
			resultsMap.put(columnDefinitions.getName(i), row.getObject(columnDefinitions.getName(i)) == null ? "null"
					: row.getObject(columnDefinitions.getName(i)).toString());
		}

		return resultsMap;
	}

	// public static Map retrieveAuditMessagesByPattern(String pattern) throws
	// IOException {
	//
	//// Config config = Utils.getConfig();
	//// HttpRequest getAuditingMessage = new HttpRequest();
	//// String url = String.format(Urls.GET_SEARCH_DATA_FROM_ES,
	// config.getEsHost(), config.getEsPort(), pattern);
	//// RestResponse restResponse = getAuditingMessage.httpSendGet(url, null);
	//
	//// get cassandra table name by action
	// String esType =
	// AuditingActionEnum.getActionByName(pattern).getAuditingEsType();
	//// AuditingActionEnum actionByName =
	// AuditingActionEnum.getActionByName(pattern);
	//
	//// Map<AuditingFieldsKeysEnum, String> myFields= new
	// HashMap<AuditingFieldsKeysEnum, String>();
	//// myFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION , pattern);
	//
	// List<Pair<AuditingFieldsKeysEnum, String>> myFields = new
	// ArrayList<Pair<AuditingFieldsKeysEnum, String>>();
	// Pair<AuditingFieldsKeysEnum, String> myPair = new
	// Pair<AuditingFieldsKeysEnum, String>(AuditingFieldsKeysEnum.AUDIT_ACTION
	// , pattern);
	// myFields.add(0, myPair);
	//
	//
	// List<Row> fetchFromTable = CassandraUtils.fetchFromTable("sdcaudit",
	// esType, myFields);
	// Row row = fetchFromTable.get(0);
	//
	//
	// ColumnDefinitions columnDefinitions = row.getColumnDefinitions();
	//// String string = row.getString(columnDefinitions.getName(1));
	//
	//// String metaData = row.getColumnDefinitions().toString();
	//// metaData =metaData.replaceAll("\\((.*?)\\)|\\[|\\]|Columns", "");
	//// List<String> metaDataList = new
	// ArrayList<String>(Arrays.asList(metaData.split(", ")));
	//
	//
	//
	// Map<String, String> resultsMap = new HashMap<String, String>();
	//
	//
	// for (int i=0 ; i < columnDefinitions.size() ; i++){
	// resultsMap.put(columnDefinitions.getName(i) ,
	// row.getObject(columnDefinitions.getName(i)) == null ? "null" :
	// row.getObject(columnDefinitions.getName(i)).toString());
	// }
	//// for (String string : metaDataList) {
	//// resultsMap.put(string , row.getString(string));
	//// }
	////
	//
	//// String dataString = fetchFromTable.toString();
	//// dataString = dataString.replaceAll("\\[|\\]|Row", "");
	//// List<String> dataArray = new
	// ArrayList<String>(Arrays.asList(dataString.split(", ")));
	////
	////
	//// Map<String, String> resultsMap = new HashMap<String, String>();
	//// for (int i=0 ; i<metaDataList.size() ; i++) {
	//// resultsMap.put(metaDataList.get(i), dataArray.get(i));
	//// }
	////
	//// return restResponse.getResponse();
	// return resultsMap;
	// }

	public static void categoryAuditSuccess(String action, CategoryDefinition categoryDefinition, User user, int status,
			String resourceType) throws Exception {
		categoryAuditSuccessInternal(action, categoryDefinition, null, null, user, status, resourceType);
	}

	public static void categoryAuditFailure(String action, CategoryDefinition categoryDataDefinition, User user,
			ActionStatus errorMessage, int status, String resourceType, Object... variables) throws Exception {
		categoryAuditFailureInternal(action, categoryDataDefinition, null, null, user, errorMessage, status,
				resourceType, variables);
	}

	public static void subCategoryAuditSuccess(String action, CategoryDefinition categoryDefinition,
			SubCategoryDefinition subCategoryDefinition, User user, int status, String resourceType) throws Exception {
		categoryAuditSuccessInternal(action, categoryDefinition, subCategoryDefinition, null, user, status,
				resourceType);
	}

	public static void groupingAuditSuccess(String action, CategoryDefinition categoryDefinition,
			SubCategoryDefinition subCategoryDefinition, GroupingDefinition groupingDefinition, User user, int status,
			String resourceType) throws Exception {
		categoryAuditSuccessInternal(action, categoryDefinition, subCategoryDefinition, groupingDefinition, user,
				status, resourceType);
	}

	public static void subCategoryAuditFailure(String action, CategoryDefinition categoryDataDefinition,
			SubCategoryDefinition subCategoryDefinition, User user, ActionStatus errorMessage, int status,
			String resourceType, Object... variables) throws Exception {
		categoryAuditFailureInternal(action, categoryDataDefinition, subCategoryDefinition, null, user, errorMessage,
				status, resourceType, variables);
	}

	// NEW Benny
	public static void groupingAuditFailure(String action, CategoryDefinition categoryDefinition,
			SubCategoryDefinition subCategoryDefinition, GroupingDefinition groupingDefinition, User user,
			ActionStatus errorMessage, int status, String resourceType, Object... variables) throws Exception {
		groupingAuditFailureInternal(action, categoryDefinition, subCategoryDefinition, groupingDefinition, user,
				errorMessage, status, resourceType, variables);
	}

	private static void groupingAuditFailureInternal(String action, CategoryDefinition categoryDataDefinition,
			SubCategoryDefinition subCategoryDefinition, GroupingDefinition groupingDefinition, User user,
			ActionStatus errorMessage, int status, String resourceType, Object... variables) throws Exception {
		// validate audit
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(errorMessage.name());
		ExpectedCategoryAudit expectedCatrgoryAuditJavaObject = new ExpectedCategoryAudit();
		expectedCatrgoryAuditJavaObject.setAction(action);
		expectedCatrgoryAuditJavaObject.setModifier(user.getFullName() + "(" + user.getUserId() + ")");
		expectedCatrgoryAuditJavaObject.setCategoryName(categoryDataDefinition.getName());
		String subCategoryName = (subCategoryDefinition != null ? subCategoryDefinition.getName()
				: Constants.EMPTY_STRING);
		expectedCatrgoryAuditJavaObject.setSubCategoryName(subCategoryName);
		String groupingName = (groupingDefinition != null ? groupingDefinition.getName() : Constants.EMPTY_STRING);
		expectedCatrgoryAuditJavaObject.setGroupingName(groupingName);
		expectedCatrgoryAuditJavaObject.setResourceType(resourceType);
		expectedCatrgoryAuditJavaObject.setStatus(String.valueOf(status));
		expectedCatrgoryAuditJavaObject.setDesc(errorInfo.getAuditDesc(variables));
		AuditValidationUtils.validateCategoryAudit(expectedCatrgoryAuditJavaObject, action);
	}

	///
	private static void categoryAuditSuccessInternal(String action, CategoryDefinition categoryDefinition,
			SubCategoryDefinition subCategoryDefinition, GroupingDefinition groupingDefinition, User user, int status,
			String resourceType) throws Exception {
		// resourceType = Service/Resource/Product
		ExpectedCategoryAudit expectedCatrgoryAuditJavaObject = new ExpectedCategoryAudit();
		expectedCatrgoryAuditJavaObject.setAction(action);
		expectedCatrgoryAuditJavaObject.setModifier(user.getFullName() + "(" + user.getUserId() + ")");
		expectedCatrgoryAuditJavaObject.setCategoryName(categoryDefinition.getName());
		String subCategoryName = (subCategoryDefinition != null ? subCategoryDefinition.getName()
				: Constants.EMPTY_STRING);
		expectedCatrgoryAuditJavaObject.setSubCategoryName(subCategoryName);
		String groupingName = (groupingDefinition != null ? groupingDefinition.getName() : Constants.EMPTY_STRING);
		expectedCatrgoryAuditJavaObject.setGroupingName(groupingName);
		expectedCatrgoryAuditJavaObject.setResourceType(resourceType);
		expectedCatrgoryAuditJavaObject.setStatus(String.valueOf(status));
		expectedCatrgoryAuditJavaObject.setDesc("OK");
		AuditValidationUtils.validateCategoryAudit(expectedCatrgoryAuditJavaObject, action);
	}

	///////////////////////////
	///// BENNNNNNNNY
	public enum UserAuditJsonKeysEnum {
		ACTION("ACTION"), MODIFIER("MODIFIER"), STATUS("STATUS"), DESC("DESCRIPTION"), USER_AFTER(
				"USER_AFTER"), USER_BEFORE("USER_BEFORE");
		private String auditJsonKeyName;

		private UserAuditJsonKeysEnum(String auditJsonKeyName) {
			this.auditJsonKeyName = auditJsonKeyName;
		}

		public String getAuditJsonKeyName() {
			return auditJsonKeyName.toLowerCase();
		}
	}

	public static void validateAddUserAudit(ExpectedUserCRUDAudit expectedAddUserAuditJavaObject, String action)
			throws Exception {

		List<Map<String, Object>> actionToList = getAuditListByAction(expectedAddUserAuditJavaObject.getAction(), 1);
		Map<String, Object> map = actionToList.get(0);
		validateField(map, UserAuditJsonKeysEnum.ACTION.getAuditJsonKeyName(), action);
		validateField(map, UserAuditJsonKeysEnum.MODIFIER.getAuditJsonKeyName(),
				expectedAddUserAuditJavaObject.getModifier());
		validateField(map, UserAuditJsonKeysEnum.USER_AFTER.getAuditJsonKeyName(),
				expectedAddUserAuditJavaObject.getUserAfter());
		validateField(map, UserAuditJsonKeysEnum.USER_BEFORE.getAuditJsonKeyName(),
				expectedAddUserAuditJavaObject.getUserBefore());
		validateField(map, UserAuditJsonKeysEnum.STATUS.getAuditJsonKeyName(),
				expectedAddUserAuditJavaObject.getStatus());
		validateField(map, UserAuditJsonKeysEnum.DESC.getAuditJsonKeyName(), expectedAddUserAuditJavaObject.getDesc());

	}

	private static void categoryAuditFailureInternal(String action, CategoryDefinition categoryDataDefinition,
			SubCategoryDefinition subCategoryDefinition, GroupingDefinition groupingDefinition, User user,
			ActionStatus errorMessage, int status, String resourceType, Object... variables) throws Exception {
		// validate audit
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(errorMessage.name());
		ExpectedCategoryAudit expectedCatrgoryAuditJavaObject = new ExpectedCategoryAudit();
		expectedCatrgoryAuditJavaObject.setAction(action);
		expectedCatrgoryAuditJavaObject.setModifier(user.getFullName() + "(" + user.getUserId() + ")");
		expectedCatrgoryAuditJavaObject.setCategoryName(categoryDataDefinition.getName());
		String subCategoryName = (subCategoryDefinition != null ? subCategoryDefinition.getName()
				: Constants.EMPTY_STRING);
		expectedCatrgoryAuditJavaObject.setSubCategoryName(subCategoryName);
		String groupingName = (groupingDefinition != null ? groupingDefinition.getName() : Constants.EMPTY_STRING);
		expectedCatrgoryAuditJavaObject.setGroupingName(groupingName);
		expectedCatrgoryAuditJavaObject.setResourceType(resourceType);
		expectedCatrgoryAuditJavaObject.setStatus(String.valueOf(status));
		expectedCatrgoryAuditJavaObject.setDesc(errorInfo.getAuditDesc(variables));
		AuditValidationUtils.validateCategoryAudit(expectedCatrgoryAuditJavaObject, action);
	}

	public static void validateGetCategoryHirarchy(ExpectedCategoryAudit expectedCatrgoryAuditJavaObject, String action)
			throws Exception {

		List<Map<String, Object>> actionToList = getAuditListByAction(expectedCatrgoryAuditJavaObject.getAction(), 1);
		Map<String, Object> map = actionToList.get(0);

		expectedCatrgoryAuditJavaObject.setModifier(getModifierString(expectedCatrgoryAuditJavaObject.getModifierName(),
				expectedCatrgoryAuditJavaObject.getModifierUid()));
		validateField(map, CategoryAuditJsonKeysEnum.ACTION.getAuditJsonKeyName(), action);
		validateField(map, CategoryAuditJsonKeysEnum.MODIFIER.getAuditJsonKeyName(),
				expectedCatrgoryAuditJavaObject.getModifier());
		validateField(map, CategoryAuditJsonKeysEnum.DETAILS.getAuditJsonKeyName(),
				expectedCatrgoryAuditJavaObject.getDetails());
		validateField(map, CategoryAuditJsonKeysEnum.STATUS.getAuditJsonKeyName(),
				expectedCatrgoryAuditJavaObject.getStatus());
		validateField(map, CategoryAuditJsonKeysEnum.DESCRIPTION.getAuditJsonKeyName(),
				expectedCatrgoryAuditJavaObject.getDesc());

	}

	public static void validateCategoryAudit(ExpectedCategoryAudit expectedCatrgoryAuditJavaObject, String action)
			throws Exception {

		List<Map<String, Object>> actionToList = getAuditListByAction(expectedCatrgoryAuditJavaObject.getAction(), 1);
		Map<String, Object> map = actionToList.get(0);
		validateField(map, CategoryAuditJsonKeysEnum.ACTION.getAuditJsonKeyName(), action);
		validateField(map, CategoryAuditJsonKeysEnum.MODIFIER.getAuditJsonKeyName(),
				expectedCatrgoryAuditJavaObject.getModifier());
		validateField(map, CategoryAuditJsonKeysEnum.CATEGORY_NAME.getAuditJsonKeyName(),
				expectedCatrgoryAuditJavaObject.getCategoryName());
		validateField(map, CategoryAuditJsonKeysEnum.SUB_CATEGORY_NAME.getAuditJsonKeyName(),
				expectedCatrgoryAuditJavaObject.getSubCategoryName());
		validateField(map, CategoryAuditJsonKeysEnum.GROUPING_NAME.getAuditJsonKeyName(),
				expectedCatrgoryAuditJavaObject.getGroupingName());
		validateField(map, CategoryAuditJsonKeysEnum.RESOURCE_TYPE.getAuditJsonKeyName(),
				expectedCatrgoryAuditJavaObject.getResourceType());
		validateField(map, CategoryAuditJsonKeysEnum.STATUS.getAuditJsonKeyName(),
				expectedCatrgoryAuditJavaObject.getStatus());
		validateField(map, CategoryAuditJsonKeysEnum.DESCRIPTION.getAuditJsonKeyName(),
				expectedCatrgoryAuditJavaObject.getDesc());
	}

	public static void GetCategoryHierarchyAuditSuccess(String action, String componentType, User user, int status)
			throws Exception {
		ExpectedGetUserListAudit expectedGetListOfUsersAuditJavaObject = new ExpectedGetUserListAudit();
		expectedGetListOfUsersAuditJavaObject.setAction(action);
		expectedGetListOfUsersAuditJavaObject.setStatus(String.valueOf(status));
		expectedGetListOfUsersAuditJavaObject.setDesc("OK");
		expectedGetListOfUsersAuditJavaObject.setModifier(user.getFullName() + "(" + user.getUserId() + ")");
		expectedGetListOfUsersAuditJavaObject.setDetails(componentType.toLowerCase());
		validateAuditGetListOfUsersByRoles(expectedGetListOfUsersAuditJavaObject, action);
	}

	public static String buildArtifactDataAudit(ArtifactDefinition artifactDefinition) {
		StringBuilder sb = new StringBuilder();
		if (artifactDefinition.getTimeout() == null) {
			artifactDefinition.setTimeout(0);
		}
		if (artifactDefinition != null) {
			sb.append(artifactDefinition.getArtifactGroupType() == null ? null
					: artifactDefinition.getArtifactGroupType().getType()).append(",").append("'")
					.append(artifactDefinition.getArtifactLabel()).append("'").append(",")
					.append(artifactDefinition.getArtifactType()).append(",")
					.append(artifactDefinition.getArtifactName()).append(",").append(artifactDefinition.getTimeout())
					.append(",").append(artifactDefinition.getEsId());
			sb.append(",");
			if (artifactDefinition.getArtifactVersion() != null) {
				sb.append(artifactDefinition.getArtifactVersion());
			} else {
				sb.append(" ");
			}
			sb.append(",");
			if (artifactDefinition.getArtifactUUID() != null) {
				sb.append(artifactDefinition.getArtifactUUID());
			} else {
				sb.append(" ");
			}
		}

		return sb.toString();
	}

	public static ExpectedResourceAuditJavaObject expectedMissingInformationAuditObject(String Action,
			String resourceUid, ComponentType resourceType) throws FileNotFoundException {
		ExpectedResourceAuditJavaObject expectedAudit = new ExpectedResourceAuditJavaObject();
		expectedAudit.setAction(Action);
		expectedAudit.setResourceName(resourceUid);
		expectedAudit.setResourceType(resourceType.getValue());
		expectedAudit.setPrevVersion("");
		expectedAudit.setCurrVersion("");
		expectedAudit.setModifierName("");
		expectedAudit.setModifierUid("");
		expectedAudit.setPrevState("");
		expectedAudit.setCurrState("");
		expectedAudit.setPrevArtifactUuid("");
		expectedAudit.setCurrArtifactUuid("");
		expectedAudit.setArtifactData("");
		expectedAudit.setStatus("403");
		expectedAudit.setDesc(buildAuditDescription(
				new ErrorValidationUtils().parseErrorConfigYaml(ActionStatus.MISSING_INFORMATION.name()),
				new ArrayList<String>()));
		return expectedAudit;
	}

	public static ExpectedResourceAuditJavaObject expectedComponentNotFoundAuditObject(String Action,
			String resourceUid, ComponentType resourceType, String artifactUid, User user,
			ArrayList<String> notFoundComponent) throws FileNotFoundException {
		String desc = null;

		ExpectedResourceAuditJavaObject expectedAudit = new ExpectedResourceAuditJavaObject();
		expectedAudit.setAction(Action);
		expectedAudit.setResourceName(resourceUid);
		expectedAudit.setResourceType(resourceType.getValue());
		expectedAudit.setPrevVersion("");
		expectedAudit.setCurrVersion("");
		expectedAudit.setModifierName(user.getFirstName() + " " + user.getLastName());
		expectedAudit.setModifierUid(user.getUserId());
		expectedAudit.setPrevState("");
		expectedAudit.setCurrState("");
		expectedAudit.setPrevArtifactUuid("");
		expectedAudit.setCurrArtifactUuid(artifactUid);
		expectedAudit.setArtifactData("");
		expectedAudit.setStatus("404");

		if (resourceType.getValue() == ComponentType.SERVICE.getValue()) {
			desc = buildAuditDescription(
					new ErrorValidationUtils().parseErrorConfigYaml(ActionStatus.SERVICE_NOT_FOUND.name()),
					notFoundComponent);
		} else if (resourceType.getValue() == ComponentType.RESOURCE.getValue())
			desc = buildAuditDescription(
					new ErrorValidationUtils().parseErrorConfigYaml(ActionStatus.RESOURCE_NOT_FOUND.name()),
					notFoundComponent);

		expectedAudit.setDesc(desc);
		return expectedAudit;
	}

	public static ExpectedResourceAuditJavaObject expectedArtifactNotFoundAuditObject(String Action, String resourceUid,
			ComponentType resourceType, String artifactUid, User user, String currState, String currVersion)
			throws FileNotFoundException {
		String desc = null;

		ExpectedResourceAuditJavaObject expectedAudit = new ExpectedResourceAuditJavaObject();
		expectedAudit.setAction(Action);
		expectedAudit.setResourceName(resourceUid);
		expectedAudit.setResourceType(resourceType.getValue());
		expectedAudit.setPrevVersion("");
		expectedAudit.setCurrVersion(currVersion);
		expectedAudit.setModifierName(user.getFirstName() + " " + user.getLastName());
		expectedAudit.setModifierUid(user.getUserId());
		expectedAudit.setPrevState("");
		expectedAudit.setCurrState(currState);
		expectedAudit.setPrevArtifactUuid("");
		expectedAudit.setCurrArtifactUuid(artifactUid);
		expectedAudit.setArtifactData("");
		expectedAudit.setStatus("404");

		desc = buildAuditDescription(
				new ErrorValidationUtils().parseErrorConfigYaml(ActionStatus.ARTIFACT_NOT_FOUND.name()),
				Arrays.asList(""));

		expectedAudit.setDesc(desc);
		return expectedAudit;
	}

	public static ExpectedResourceAuditJavaObject expectedArtifactNotFoundAuditObject(String Action,
			String resourceName, ComponentType resourceType, String artifactUid, LifecycleStateEnum lifecycle,
			User user, String currVersion) throws FileNotFoundException {
		String desc = null;

		ExpectedResourceAuditJavaObject expectedAudit = new ExpectedResourceAuditJavaObject();
		expectedAudit.setAction(Action);
		expectedAudit.setResourceName(resourceName);
		expectedAudit.setResourceType(resourceType.getValue());
		expectedAudit.setPrevVersion("");
		expectedAudit.setCurrVersion(currVersion);
		expectedAudit.setModifierName(user.getFirstName() + " " + user.getLastName());
		expectedAudit.setModifierUid(user.getUserId());
		expectedAudit.setPrevState("");
		expectedAudit.setCurrState(lifecycle.name());
		expectedAudit.setPrevArtifactUuid("");
		expectedAudit.setCurrArtifactUuid(artifactUid);
		expectedAudit.setArtifactData("");
		expectedAudit.setStatus("404");

		desc = buildAuditDescription(
				new ErrorValidationUtils().parseErrorConfigYaml(ActionStatus.ARTIFACT_NOT_FOUND.name()),
				new ArrayList<String>());

		expectedAudit.setDesc(desc);
		return expectedAudit;
	}

	public static ExpectedResourceAuditJavaObject expectedRestrictedOperationAuditObject(String Action,
			String resourceNameOrUid, ComponentType resourceType, String artifactUid, User user, String currVersion,
			String currState) throws FileNotFoundException {
		String desc = null;

		ExpectedResourceAuditJavaObject expectedAudit = new ExpectedResourceAuditJavaObject();
		expectedAudit.setAction(Action);
		expectedAudit.setResourceName(resourceNameOrUid);
		expectedAudit.setResourceType(resourceType.getValue());
		expectedAudit.setPrevVersion("");
		expectedAudit.setCurrVersion(currVersion);
		expectedAudit.setModifierName(user.getFirstName() + " " + user.getLastName());
		expectedAudit.setModifierUid(user.getUserId());
		expectedAudit.setPrevState("");
		expectedAudit.setCurrState(currState);
		expectedAudit.setPrevArtifactUuid("");
		expectedAudit.setCurrArtifactUuid(artifactUid);
		expectedAudit.setArtifactData("");
		expectedAudit.setStatus("409");

		desc = buildAuditDescription(
				new ErrorValidationUtils().parseErrorConfigYaml(ActionStatus.RESTRICTED_OPERATION.name()),
				new ArrayList<String>());

		expectedAudit.setDesc(desc);
		return expectedAudit;
	}

	public static ExpectedResourceAuditJavaObject expectedInvalidContentAuditObject(String Action, String resourceName,
			ComponentType resourceType, String artifactUid, User user, String currVersion, String currState,
			ArrayList<String> invalidContentList) throws FileNotFoundException {
		return expectedInvalidContentAuditObject(ActionStatus.INVALID_CONTENT, Action, resourceName, resourceType,
				artifactUid, user, currVersion, currState, invalidContentList);
	}

	public static ExpectedResourceAuditJavaObject expectedInvalidContentAuditObject(ActionStatus actionStatus,
			String Action, String resourceName, ComponentType resourceType, String artifactUid, User user,
			String currVersion, String currState, ArrayList<String> invalidContentList) throws FileNotFoundException {
		String desc = null;

		ExpectedResourceAuditJavaObject expectedAudit = new ExpectedResourceAuditJavaObject();
		expectedAudit.setAction(Action);
		expectedAudit.setResourceName(resourceName);
		expectedAudit.setResourceType(resourceType.getValue());
		expectedAudit.setPrevVersion("");
		expectedAudit.setCurrVersion(currVersion);
		expectedAudit.setModifierName(user.getFirstName() + " " + user.getLastName());
		expectedAudit.setModifierUid(user.getUserId());
		expectedAudit.setPrevState("");
		expectedAudit.setCurrState(currState);
		expectedAudit.setPrevArtifactUuid("");
		expectedAudit.setCurrArtifactUuid(artifactUid);
		expectedAudit.setArtifactData("");
		expectedAudit.setStatus("400");

		desc = buildAuditDescription(new ErrorValidationUtils().parseErrorConfigYaml(actionStatus.name()),
				invalidContentList);

		expectedAudit.setDesc(desc);
		return expectedAudit;
	}

	public static ExpectedResourceAuditJavaObject expectedSuccessAuditObject(String Action, String resourceName,
			ComponentType resourceType, ArtifactReqDetails artifactReq, User user, String currVersion, String currState,
			String prevArtifactUuid) throws FileNotFoundException {
		ExpectedResourceAuditJavaObject expectedAudit = new ExpectedResourceAuditJavaObject();
		expectedAudit.setAction(Action);
		expectedAudit.setResourceName(resourceName);
		expectedAudit.setResourceType(resourceType.getValue());
		expectedAudit.setPrevVersion("");
		expectedAudit.setCurrVersion(currVersion);
		expectedAudit.setModifierName(user.getFirstName() + " " + user.getLastName());
		expectedAudit.setModifierUid(user.getUserId());
		expectedAudit.setPrevState("");
		expectedAudit.setCurrState(currState);
		expectedAudit.setPrevArtifactUuid(prevArtifactUuid);
		expectedAudit.setCurrArtifactUuid(artifactReq.getUniqueId());
		expectedAudit
				.setArtifactData(buildArtifactDataAudit(ArtifactUtils.convertArtifactReqToDefinition(artifactReq)));
		expectedAudit.setStatus("200");
		expectedAudit.setDesc("OK");
		return expectedAudit;
	}

	public static JSONObject filterAuditByUuid(String action, String uuid) throws Exception {
		Map<String, String> actionMap = new HashMap<String, String>();
		actionMap.put("ACTION", action);
		JSONObject actionJsonObject = new JSONObject(actionMap);
		Map<String, String> uuidMap = new HashMap<String, String>();
		uuidMap.put("SERVICE_INSTANCE_ID", uuid);
		JSONObject uuidJsonObject = new JSONObject(uuidMap);

		List<JSONObject> filters = new ArrayList<JSONObject>(Arrays.asList(actionJsonObject, uuidJsonObject));
		JSONObject body = buildElasticQueryBody(filters);
		return body;
	}

	public static void validateAudit(ExpectedResourceAuditJavaObject resourceAuditJavaObject, String action)
			throws Exception {
		List<Map<String, Object>> actionToList = getAuditListByAction(resourceAuditJavaObject.getAction(), 1);
		Map<String, Object> map2 = actionToList.get(0);
		validateField(map2, AuditJsonKeysEnum.ACTION.getAuditJsonKeyName(), action);
		validateField(map2, AuditJsonKeysEnum.RESOURCE_NAME.getAuditJsonKeyName(),
				resourceAuditJavaObject.getResourceName());
		validateField(map2, AuditJsonKeysEnum.RESOURCE_TYPE.getAuditJsonKeyName(),
				resourceAuditJavaObject.getResourceType());
		validateField(map2, AuditJsonKeysEnum.PREV_VERSION.getAuditJsonKeyName(),
				resourceAuditJavaObject.getPrevVersion());
		validateField(map2, AuditJsonKeysEnum.CURR_VERSION.getAuditJsonKeyName(),
				resourceAuditJavaObject.getCurrVersion());
		validateField(map2, AuditJsonKeysEnum.MODIFIER.getAuditJsonKeyName(), resourceAuditJavaObject.getMODIFIER());
		validateField(map2, AuditJsonKeysEnum.PREV_STATE.getAuditJsonKeyName(), resourceAuditJavaObject.getPrevState());
		validateField(map2, AuditJsonKeysEnum.CURR_STATE.getAuditJsonKeyName(), resourceAuditJavaObject.getCurrState());
		validateField(map2, AuditJsonKeysEnum.STATUS.getAuditJsonKeyName(), resourceAuditJavaObject.getStatus());
		validateField(map2, AuditJsonKeysEnum.DESCRIPTION.getAuditJsonKeyName(), resourceAuditJavaObject.getDesc());
		validateField(map2, AuditJsonKeysEnum.COMMENT.getAuditJsonKeyName(), resourceAuditJavaObject.getComment());
	}

	////// service audit validation/////////////////////

	public static ExpectedResourceAuditJavaObject constructFieldsForAuditValidation(ServiceReqDetails serviceReqDetails,
			String serviceVersion, User sdncUserDetails) {

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = new ExpectedResourceAuditJavaObject();

		expectedResourceAuditJavaObject.setAction("Create");
		expectedResourceAuditJavaObject.setModifierUid(sdncUserDetails.getUserId());
		String userFirstLastName = sdncUserDetails.getFirstName() + " " + sdncUserDetails.getLastName();
		expectedResourceAuditJavaObject.setModifierName(userFirstLastName);
		expectedResourceAuditJavaObject.setStatus("200");
		expectedResourceAuditJavaObject.setDesc("OK");
		expectedResourceAuditJavaObject.setResourceName(serviceReqDetails.getName());
		expectedResourceAuditJavaObject.setResourceType("Service");
		expectedResourceAuditJavaObject.setPrevVersion(String.valueOf(Float.parseFloat(serviceVersion) - 0.1f));
		expectedResourceAuditJavaObject.setCurrVersion(serviceVersion);
		expectedResourceAuditJavaObject.setPrevState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setComment(null);

		return expectedResourceAuditJavaObject;

	}

	public static ExpectedResourceAuditJavaObject constructFieldsForAuditValidation(ServiceReqDetails serviceReqDetails,
			String serviceVersion, User sdncUserDetails, ActionStatus errorStatus, List<String> variables)
			throws FileNotFoundException {

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = constructFieldsForAuditValidation(
				serviceReqDetails, serviceVersion, sdncUserDetails);
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(errorStatus.name());

		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());
		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);

		return expectedResourceAuditJavaObject;

	}

}
