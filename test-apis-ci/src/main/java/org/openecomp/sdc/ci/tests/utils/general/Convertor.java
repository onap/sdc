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

package org.openecomp.sdc.ci.tests.utils.general;

import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_CREATED;
import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_SUCCESS;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.ci.tests.datatypes.ComponentReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ProductReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceRespJavaObject;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ErrorInfo;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedProductAudit;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedResourceAuditJavaObject;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedUserCRUDAudit;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.common.api.Constants;

public class Convertor {
	// ***** resource *****
	public static ResourceRespJavaObject constructFieldsForRespValidation(ResourceReqDetails resourceDetails,
			String resourceVersion) {
		return convertToRespObject(resourceDetails, resourceVersion, UserRoleEnum.ADMIN.getUserId(),
				UserRoleEnum.ADMIN.getUserName());

	}

	public static ResourceRespJavaObject constructFieldsForRespValidation(ResourceReqDetails resourceDetails) {
		return convertToRespObject(resourceDetails, resourceDetails.getVersion(), UserRoleEnum.ADMIN.getUserId(),
				UserRoleEnum.ADMIN.getUserName());

	}

	public static ResourceRespJavaObject constructFieldsForRespValidation(ResourceReqDetails resourceDetails,
			String resourceVersion, User user) {
		return convertToRespObject(resourceDetails, resourceVersion, user.getUserId(), user.getFullName());

	}

	private static ResourceRespJavaObject convertToRespObject(ResourceReqDetails resourceDetails,
			String resourceVersion, String userId, String userName) {
		ResourceRespJavaObject resourceRespJavaObject = new ResourceRespJavaObject();

		resourceRespJavaObject.setUniqueId(resourceDetails.getUniqueId());
		resourceRespJavaObject.setName(resourceDetails.getName());
		resourceRespJavaObject.setCreatorUserId(resourceDetails.getCreatorUserId());
		resourceRespJavaObject.setCreatorFullName(resourceDetails.getCreatorFullName());
		resourceRespJavaObject.setLastUpdaterUserId(userId);
		resourceRespJavaObject.setLastUpdaterFullName(userName);
		resourceRespJavaObject.setDescription(resourceDetails.getDescription());
		resourceRespJavaObject.setIcon(resourceDetails.getIcon());
		resourceRespJavaObject.setTags(resourceDetails.getTags());
		resourceRespJavaObject.setIsHighestVersion("true");
		resourceRespJavaObject.setCategories(resourceDetails.getCategories());
		resourceRespJavaObject.setLifecycleState(
				resourceDetails.getLifecycleState() != null ? resourceDetails.getLifecycleState().toString()
						: LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.toString());
		// resourceRespJavaObject.setLifecycleState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		resourceRespJavaObject.setDerivedFrom(resourceDetails.getDerivedFrom());
		resourceRespJavaObject.setVendorName(resourceDetails.getVendorName());
		resourceRespJavaObject.setVendorRelease(resourceDetails.getVendorRelease());
		resourceRespJavaObject.setContactId(resourceDetails.getContactId());
		resourceRespJavaObject.setAbstractt("false");
		resourceRespJavaObject.setVersion(resourceVersion);
		resourceRespJavaObject.setCost(resourceDetails.getCost());
		resourceRespJavaObject.setLicenseType(resourceDetails.getLicenseType());
		resourceRespJavaObject.setResourceType(resourceDetails.getResourceType());

		return resourceRespJavaObject;

	}

	// ********** service **************

	// public static ServiceRespJavaObject
	// constructFieldsForRespValidation(ServiceReqDetails serviceDetails, String
	// serviceVersion, User user) {
	// return convertToRespObject(serviceDetails, serviceVersion,
	// user.getUserId(), user.getFullName());
	//
	// }
	//
	// private static ServiceRespJavaObject
	// convertToRespObject(ServiceReqDetails serviceDetails, String
	// serviceVersion, String userId, String userName) {
	// ServiceRespJavaObject serviceRespJavaObject = new
	// ServiceRespJavaObject();
	//
	// serviceRespJavaObject.setUniqueId(serviceDetails.getUniqueId());
	// serviceRespJavaObject.setName(serviceDetails.getName());
	// serviceRespJavaObject.setCreatorUserId(userId);
	// serviceRespJavaObject.setCreatorFullName(userName);
	// serviceRespJavaObject.setLastUpdaterUserId(userId);
	// serviceRespJavaObject.setLastUpdaterFullName(userName);
	// serviceRespJavaObject.setDescription(serviceDetails.getDescription());
	// serviceRespJavaObject.setIcon(serviceDetails.getIcon());
	// serviceRespJavaObject.setCategory(serviceDetails.getCategory());
	// serviceRespJavaObject.setLifecycleState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
	// serviceRespJavaObject.setContactId(serviceDetails.getContactId());
	// serviceRespJavaObject.setVersion(serviceVersion);
	//
	// return serviceRespJavaObject;
	// }

	// ********** product **************

	public static Product constructFieldsForRespValidation(ProductReqDetails productDetails, String productVersion,
			User user) {
		return convertToRespObject(productDetails, productVersion, user.getUserId(), user.getFullName());
	}

	private static Product convertToRespObject(ProductReqDetails productDetails, String productVersion, String userId,
			String userName) {
		Product expectedProduct = new Product();

		expectedProduct.setUniqueId(productDetails.getUniqueId());
		expectedProduct.setName(productDetails.getName());
		expectedProduct.setFullName(productDetails.getFullName());
		expectedProduct.setCreatorUserId(productDetails.getCreatorUserId());
		expectedProduct.setCreatorFullName(productDetails.getCreatorFullName());
		expectedProduct.setLastUpdaterUserId(userId);
		expectedProduct.setLastUpdaterFullName(userName);
		expectedProduct.setDescription(productDetails.getDescription());
		// expectedProduct.setIcon(resourceDetails.getIcon());
		expectedProduct.setTags(productDetails.getTags());
		expectedProduct.setHighestVersion(true);
		List<CategoryDefinition> categories = productDetails.getCategories();
		if (categories == null) {
			categories = new ArrayList<>();
		}
		expectedProduct.setCategories(categories);
		expectedProduct.setState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		expectedProduct.setVersion(productVersion);
		expectedProduct.setContacts(productDetails.getContacts());
		return expectedProduct;
	}

	// ***** audit *****

	public static ExpectedResourceAuditJavaObject constructFieldsForAuditValidation(ResourceReqDetails resourceDetails,
			String resourceVersion) {
		return convertToAuditObject(resourceDetails, resourceVersion, UserRoleEnum.ADMIN.getUserId(),
				UserRoleEnum.ADMIN.getUserName());
	}

	public static ExpectedResourceAuditJavaObject constructFieldsForAuditValidation(
			ResourceReqDetails resourceDetails) {
		return convertToAuditObject(resourceDetails, resourceDetails.getVersion(), UserRoleEnum.ADMIN.getUserId(),
				UserRoleEnum.ADMIN.getUserName());
	}

	public static ExpectedResourceAuditJavaObject constructFieldsForAuditValidation(ResourceReqDetails resourceDetails,
			String resourceVersion, User user) {
		return convertToAuditObject(resourceDetails, resourceVersion, user.getUserId(), user.getFullName());
	}

	private static ExpectedResourceAuditJavaObject convertToAuditObject(ResourceReqDetails resourceDetails,
			String resourceVersion, String userId, String userName) {
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = new ExpectedResourceAuditJavaObject();

		expectedResourceAuditJavaObject.setAction("Checkout");
		expectedResourceAuditJavaObject.setModifierName(userName);
		expectedResourceAuditJavaObject.setModifierUid(userId);
		expectedResourceAuditJavaObject.setStatus("200.0");
		expectedResourceAuditJavaObject.setDesc("OK");
		expectedResourceAuditJavaObject.setResourceName(resourceDetails.getName());
		expectedResourceAuditJavaObject.setResourceType("Resource");
		expectedResourceAuditJavaObject.setPrevVersion(String.valueOf(Float.parseFloat(resourceVersion) - 0.1f));
		expectedResourceAuditJavaObject.setCurrVersion(resourceVersion);
		expectedResourceAuditJavaObject.setPrevState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setComment(null);

		return expectedResourceAuditJavaObject;
	}

	public static ExpectedProductAudit constructFieldsForAuditValidation(Product productDetails, String action,
			User user, ActionStatus actionStatus, String prevVersion, String currVersion, LifecycleStateEnum prevState,
			LifecycleStateEnum currState, String uuid, String... errorMessageParams) throws FileNotFoundException {
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(actionStatus.name());
		return convertToAuditObject(productDetails, action, user, errorInfo, prevVersion, currVersion, prevState,
				currState, uuid, errorMessageParams);
	}

	private static ExpectedProductAudit convertToAuditObject(Product productDetails, String action, User user,
			ErrorInfo errorInfo, String prevVersion, String currVersion, LifecycleStateEnum prevState,
			LifecycleStateEnum currState, String uuid, String... errorMessageParams) {
		ExpectedProductAudit expectedProductAudit = new ExpectedProductAudit();

		expectedProductAudit.setACTION(action);
		String userUserId = user.getUserId();
		String userFullName;
		if (StringUtils.isEmpty(user.getFirstName()) && StringUtils.isEmpty(user.getLastName())) {
			userFullName = "";
		} else {
			userFullName = user.getFullName();
		}
		if (StringUtils.isEmpty(userUserId)) {
			userUserId = "UNKNOWN";
		}
		expectedProductAudit.setMODIFIER(
				!StringUtils.isEmpty(userFullName) ? userFullName + "(" + userUserId + ")" : "(" + userUserId + ")");
		expectedProductAudit.setSTATUS(Integer.toString(errorInfo.getCode()));
		expectedProductAudit.setDESC(errorInfo.getAuditDesc((Object[]) (errorMessageParams)));
		expectedProductAudit
				.setRESOURCE_NAME(productDetails != null ? productDetails.getName() : Constants.EMPTY_STRING);
		expectedProductAudit.setRESOURCE_TYPE("Product");
		expectedProductAudit.setPREV_VERSION(prevVersion);
		expectedProductAudit.setCURR_VERSION(currVersion);
		expectedProductAudit.setPREV_STATE(prevState != null ? prevState.name() : Constants.EMPTY_STRING);
		expectedProductAudit.setCURR_STATE(currState != null ? currState.name() : Constants.EMPTY_STRING);
		expectedProductAudit.setSERVICE_INSTANCE_ID(uuid);
		return expectedProductAudit;
	}

	////////////////
	// Convertor.constructFieldsForAuditValidationSuccess(addUser,
	//////////////// sdncAdminUser, mechIdUser, null, STATUS_CODE_CREATED);
	public static ExpectedUserCRUDAudit constructFieldsForAuditValidation(String action, User userModifier,
			ActionStatus actionStatus, User userAfter, User userBefore, Object... variables) throws Exception {
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(actionStatus.name());
		ExpectedUserCRUDAudit expectedAddUserAuditJavaObject = new ExpectedUserCRUDAudit();
		expectedAddUserAuditJavaObject.setAction(action);
		expectedAddUserAuditJavaObject.setModifier(
				userModifier.getFirstName() != null ? userModifier.getFullName() + "(" + userModifier.getUserId() + ")"
						: "(" + userModifier.getUserId() + ")");
		String status = Integer.toString(errorInfo.getCode());
		expectedAddUserAuditJavaObject.setStatus(status);
		if (errorInfo.getCode() == STATUS_CODE_SUCCESS || errorInfo.getCode() == STATUS_CODE_CREATED) {
			expectedAddUserAuditJavaObject.setDesc("OK");
		} else {
			expectedAddUserAuditJavaObject.setDesc(errorInfo.getAuditDesc(variables));
		}
		expectedAddUserAuditJavaObject.setUserBefore(userBefore != null
				? userBefore.getUserId() + ", " + userBefore.getFirstName() + " " + userBefore.getLastName() + ", "
						+ userBefore.getEmail() + ", " + userBefore.getRole()
				: Constants.EMPTY_STRING);
		expectedAddUserAuditJavaObject.setUserAfter(userAfter != null
				? userAfter.getUserId() + ", " + userAfter.getFirstName() + " " + userAfter.getLastName() + ", "
						+ userAfter.getEmail() + ", " + userAfter.getRole()
				: Constants.EMPTY_STRING);
		return expectedAddUserAuditJavaObject;
	}

	// For RESOURCE and SERVICE same Audit
	public static ExpectedResourceAuditJavaObject constructFieldsForAuditValidation(
			ComponentReqDetails componentDetails, String action, User userModifier, ActionStatus actionStatus,
			String currVersion, String prevVersion, String curState, String prevState, String uuid, String comment,
			Object... variables) throws Exception {
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(actionStatus.name());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = new ExpectedResourceAuditJavaObject();
		expectedResourceAuditJavaObject.setAction(action);
		expectedResourceAuditJavaObject.setMODIFIER(
				userModifier.getFirstName() != null ? userModifier.getFullName() + "(" + userModifier.getUserId() + ")"
						: "(" + userModifier.getUserId() + ")");
		String status = Integer.toString(errorInfo.getCode());
		expectedResourceAuditJavaObject.setStatus(status);
		if (errorInfo.getCode() == STATUS_CODE_SUCCESS || errorInfo.getCode() == STATUS_CODE_CREATED) {
			expectedResourceAuditJavaObject.setDesc("OK");
		} else {
			expectedResourceAuditJavaObject.setDesc(errorInfo.getAuditDesc(variables));
		}
		expectedResourceAuditJavaObject.setCurrState(curState);
		expectedResourceAuditJavaObject.setPrevState(prevState);
		expectedResourceAuditJavaObject.setCurrVersion(currVersion);
		expectedResourceAuditJavaObject.setPrevVersion(prevVersion);
		expectedResourceAuditJavaObject.setComment(comment);
		expectedResourceAuditJavaObject.setSERVICE_INSTANCE_ID(uuid);
		if (componentDetails instanceof ServiceReqDetails) {
			expectedResourceAuditJavaObject.setResourceName(((ServiceReqDetails) componentDetails).getName());
			expectedResourceAuditJavaObject.setResourceType("Service");
		}
		if (componentDetails instanceof ResourceReqDetails) {
			expectedResourceAuditJavaObject.setResourceName(((ResourceReqDetails) componentDetails).getName());
			expectedResourceAuditJavaObject.setResourceType("Resource");
		}
		return expectedResourceAuditJavaObject;
	}

	// Distribution Service Audit
	public static ExpectedResourceAuditJavaObject constructFieldsForDistributionAuditValidation(
			ComponentReqDetails componentDetails, String action, User userModifier, ActionStatus actionStatus,
			String currVersion, String distCurrStatus, String distProvStatus, String curState, String uuid,
			String comment, Object... variables) throws Exception {
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(actionStatus.name());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = new ExpectedResourceAuditJavaObject();
		expectedResourceAuditJavaObject.setAction(action);
		expectedResourceAuditJavaObject.setMODIFIER(
				userModifier.getFirstName() != null ? userModifier.getFullName() + "(" + userModifier.getUserId() + ")"
						: "(" + userModifier.getUserId() + ")");
		String status = Integer.toString(errorInfo.getCode());
		expectedResourceAuditJavaObject.setStatus(status);
		if (errorInfo.getCode() == STATUS_CODE_SUCCESS || errorInfo.getCode() == STATUS_CODE_CREATED) {
			expectedResourceAuditJavaObject.setDesc("OK");
		} else {
			expectedResourceAuditJavaObject.setDesc(errorInfo.getAuditDesc(variables));
		}
		expectedResourceAuditJavaObject.setCurrState(curState);
		expectedResourceAuditJavaObject.setCurrVersion(currVersion);
		expectedResourceAuditJavaObject.setDcurrStatus(distCurrStatus);
		expectedResourceAuditJavaObject.setDprevStatus(distProvStatus);
		expectedResourceAuditJavaObject.setComment(comment);
		expectedResourceAuditJavaObject.setSERVICE_INSTANCE_ID(uuid);
		if (componentDetails instanceof ServiceReqDetails) {
			expectedResourceAuditJavaObject.setResourceName(((ServiceReqDetails) componentDetails).getName());
			expectedResourceAuditJavaObject.setResourceType("Service");
		}
		return expectedResourceAuditJavaObject;
	}

}
