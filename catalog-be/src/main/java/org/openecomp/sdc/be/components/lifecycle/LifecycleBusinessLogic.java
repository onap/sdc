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

package org.openecomp.sdc.be.components.lifecycle;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.openecomp.sdc.be.components.distribution.engine.ServiceDistributionArtifactsBuilder;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogic;
import org.openecomp.sdc.be.components.impl.ProductBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.ICacheMangerOperation;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.ILifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.CapabilityOperation;
import org.openecomp.sdc.be.model.operations.impl.ProductOperation;
import org.openecomp.sdc.be.model.operations.impl.ResourceOperation;
import org.openecomp.sdc.be.model.operations.impl.ServiceOperation;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fj.data.Either;

@org.springframework.stereotype.Component("lifecycleBusinessLogic")
public class LifecycleBusinessLogic {

	private static final String COMMENT = "comment";

	@Autowired
	private IGraphLockOperation graphLockOperation = null;

	@Autowired
	private ArtifactsBusinessLogic artifactsBusinessLogic;

	@Autowired
	private ResourceOperation resourceOperation;

	@Autowired
	private ServiceOperation serviceOperation;

	@Autowired
	private ProductOperation productOperation;

	@Autowired
	private CapabilityOperation capabilityOperation;

	private static Logger log = LoggerFactory.getLogger(LifecycleBusinessLogic.class.getName());

	@javax.annotation.Resource
	private ComponentsUtils componentUtils;

	@javax.annotation.Resource
	private ILifecycleOperation lifecycleOperation;
	@javax.annotation.Resource
	ArtifactsBusinessLogic artifactsManager;

	@javax.annotation.Resource
	private ServiceDistributionArtifactsBuilder serviceDistributionArtifactsBuilder;

	@javax.annotation.Resource
	private ServiceBusinessLogic serviceBusinessLogic;

	@javax.annotation.Resource
	private ResourceBusinessLogic resourceBusinessLogic;

	@javax.annotation.Resource
	private ProductBusinessLogic productBusinessLogic;

	@Autowired
	private ToscaExportHandler toscaExportUtils;

	@Autowired
	ICacheMangerOperation cacheManagerOperation;

	private Map<String, LifeCycleTransition> stateTransitions;
	private static volatile boolean isInitialized = false;

	@PostConstruct
	public void init() {
		// init parameters
		if (!isInitialized) {
			synchronized (this) {
				if (!isInitialized) {
					initStateOperations();
					isInitialized = true;
				}
			}
		}
	}

	private void initStateOperations() {
		stateTransitions = new HashMap<String, LifeCycleTransition>();

		LifeCycleTransition checkoutOp = new CheckoutTransition(componentUtils, lifecycleOperation);
		stateTransitions.put(checkoutOp.getName().name(), checkoutOp);

		UndoCheckoutTransition undoCheckoutOp = new UndoCheckoutTransition(componentUtils, lifecycleOperation);
		undoCheckoutOp.setArtifactsBusinessLogic(artifactsBusinessLogic);
		stateTransitions.put(undoCheckoutOp.getName().name(), undoCheckoutOp);

		LifeCycleTransition checkinOp = new CheckinTransition(componentUtils, lifecycleOperation);
		stateTransitions.put(checkinOp.getName().name(), checkinOp);

		LifeCycleTransition certificationRequest = new CertificationRequestTransition(componentUtils, lifecycleOperation, serviceDistributionArtifactsBuilder, serviceBusinessLogic, capabilityOperation, toscaExportUtils);
		stateTransitions.put(certificationRequest.getName().name(), certificationRequest);

		LifeCycleTransition startCertification = new StartCertificationTransition(componentUtils, lifecycleOperation);
		stateTransitions.put(startCertification.getName().name(), startCertification);

		LifeCycleTransition failCertification = new CertificationChangeTransition(LifeCycleTransitionEnum.FAIL_CERTIFICATION, componentUtils, lifecycleOperation);
		stateTransitions.put(failCertification.getName().name(), failCertification);

		LifeCycleTransition cancelCertification = new CertificationChangeTransition(LifeCycleTransitionEnum.CANCEL_CERTIFICATION, componentUtils, lifecycleOperation);
		stateTransitions.put(cancelCertification.getName().name(), cancelCertification);

		CertificationChangeTransition successCertification = new CertificationChangeTransition(LifeCycleTransitionEnum.CERTIFY, componentUtils, lifecycleOperation);
		successCertification.setArtifactsManager(artifactsBusinessLogic);
		stateTransitions.put(successCertification.getName().name(), successCertification);
	}

	public LifeCycleTransition getLifecycleTransition(LifeCycleTransitionEnum transitionEnum) {
		return stateTransitions.get(transitionEnum.name());
	}

	public Either<Service, ResponseFormat> changeServiceState(String serviceId, User modifier, LifeCycleTransitionEnum transitionEnum, LifecycleChangeInfoWithAction changeInfo, boolean inTransaction, boolean needLock) {
		return (Either<Service, ResponseFormat>) changeComponentState(ComponentTypeEnum.SERVICE, serviceId, modifier, transitionEnum, changeInfo, inTransaction, needLock);
	}

	// TODO: rhalili - should use changeComponentState when possible
	public Either<Resource, ResponseFormat> changeState(String resourceId, User modifier, LifeCycleTransitionEnum transitionEnum, LifecycleChangeInfoWithAction changeInfo, boolean inTransaction, boolean needLock) {
		return (Either<Resource, ResponseFormat>) changeComponentState(ComponentTypeEnum.RESOURCE, resourceId, modifier, transitionEnum, changeInfo, inTransaction, needLock);

		// LifeCycleTransition lifeCycleTransition =
		// stateTransitions.get(transitionEnum.name());
		// if (lifeCycleTransition == null) {
		// log.debug("state operation is not valid. operations allowed are: {}",
		// LifeCycleTransitionEnum.valuesAsString());
		// ResponseFormat error =
		// componentUtils.getInvalidContentErrorAndAudit(modifier,
		// AuditingActionEnum.CHECKOUT_RESOURCE);
		// return Either.right(error);
		// }
		//
		// Either<Resource, ResponseFormat> operationResult;
		// Resource resource = null;
		// boolean needToUnlockResource = false;
		//
		// log.debug("get resource from graph");
		// ResponseFormat errorResponse;
		// Either<Resource, ResponseFormat> eitherResourceResponse =
		// getResourceForChange(resourceId, modifier, lifeCycleTransition);
		// if (eitherResourceResponse.isRight()) {
		// return eitherResourceResponse;
		// }
		// resource = eitherResourceResponse.left().value();
		// String resourceCurrVersion = resource.getResourceVersion();
		// LifecycleStateEnum resourceCurrState = resource.getLifecycleState();
		//
		// if (inTransaction == false) {
		// // lock resource
		// Either<Boolean, ResponseFormat> eitherLockResource =
		// lockResource(resource);
		// if (eitherLockResource.isRight()) {
		// errorResponse = eitherLockResource.right().value();
		// componentUtils.auditResource(errorResponse, modifier, resource,
		// resourceCurrState.name(), resourceCurrVersion,
		// lifeCycleTransition.getAuditingAction(), null);
		// return Either.right(errorResponse);
		// }
		// needToUnlockResource = true;
		// }
		//
		// try {
		// Either<Boolean, ResponseFormat> resourceNotDeleted =
		// validateResourceNotDeleted(modifier, lifeCycleTransition, resource,
		// resourceCurrVersion);
		// if (resourceNotDeleted.isRight()) {
		// return Either.right(resourceNotDeleted.right().value());
		// }
		//
		// Either<Boolean, ResponseFormat> validateHighestVersion =
		// validateHighestVersion(modifier, lifeCycleTransition, resource,
		// resourceCurrVersion);
		// if (validateHighestVersion.isRight()) {
		// return Either.right(validateHighestVersion.right().value());
		// }
		//
		// Either<User, ResponseFormat> ownerResult =
		// lifeCycleTransition.getResourceOwner(resource);
		// if (ownerResult.isRight()) {
		// return Either.right(ownerResult.right().value());
		// }
		// User owner = ownerResult.left().value();
		// log.debug("owner of resource {} is {}", resource.getUniqueId(),
		// owner.getUserId());
		//
		// LifecycleStateEnum oldState = resource.getLifecycleState();
		//
		// Either<String, ResponseFormat> commentValidationResult =
		// validateComment(changeInfo, transitionEnum);
		// if (commentValidationResult.isRight()) {
		// errorResponse = commentValidationResult.right().value();
		// EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new
		// EnumMap<AuditingFieldsKeysEnum,
		// Object>(AuditingFieldsKeysEnum.class);
		// auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_COMMENT,
		// changeInfo.getUserRemarks());
		// componentUtils.auditResource(errorResponse, modifier, resource,
		// resourceCurrState.name(), resourceCurrVersion,
		// lifeCycleTransition.getAuditingAction(), auditingFields);
		// return Either.right(errorResponse);
		// }
		// changeInfo.setUserRemarks(commentValidationResult.left().value());
		//
		// Either<Boolean, ResponseFormat> stateValidationResult =
		// lifeCycleTransition.validateResourceBeforeTransition(resource.getResourceName(),
		// ComponentTypeEnum.RESOURCE, modifier, owner, oldState);
		// if (stateValidationResult.isRight()) {
		// errorResponse = stateValidationResult.right().value();
		// componentUtils.auditResource(errorResponse, modifier, resource,
		// resourceCurrState.name(), resourceCurrVersion,
		// lifeCycleTransition.getAuditingAction(), null);
		// return Either.right(errorResponse);
		// }
		//
		// operationResult = lifeCycleTransition.changeStateOperation(resource,
		// modifier, owner, inTransaction);
		//
		// if (operationResult.isRight()) {
		// errorResponse = operationResult.right().value();
		// log.debug("audit before sending response");
		// componentUtils.auditResource(errorResponse, modifier, resource,
		// resourceCurrState.name(), resourceCurrVersion,
		// lifeCycleTransition.getAuditingAction(), null);
		//
		// return Either.right(errorResponse);
		// }
		// Resource resourceAfterOperation = operationResult.left().value();
		// EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new
		// EnumMap<AuditingFieldsKeysEnum,
		// Object>(AuditingFieldsKeysEnum.class);
		// auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_COMMENT,
		// changeInfo.getUserRemarks());
		// componentUtils.auditResource(componentUtils.getResponseFormat(ActionStatus.OK),
		// modifier, resourceAfterOperation, resourceCurrState.name(),
		// resourceCurrVersion, lifeCycleTransition.getAuditingAction(),
		// auditingFields);
		// return operationResult;
		//
		// } finally {
		// log.debug("unlock resource {}", resourceId);
		// if (needToUnlockResource && resource != null) {
		// resource.setUniqueId(resourceId);
		// graphLockOperation.unlockResource(resource);
		// }
		// }

	}

	public Either<? extends Component, ResponseFormat> changeComponentState(ComponentTypeEnum componentType, String componentId, User modifier, LifeCycleTransitionEnum transitionEnum, LifecycleChangeInfoWithAction changeInfo, boolean inTransaction,
			boolean needLock) {

		LifeCycleTransition lifeCycleTransition = stateTransitions.get(transitionEnum.name());
		if (lifeCycleTransition == null) {
			log.debug("state operation is not valid. operations allowed are: {}", LifeCycleTransitionEnum.valuesAsString());
			ResponseFormat error = componentUtils.getInvalidContentErrorAndAudit(modifier, AuditingActionEnum.CHECKOUT_RESOURCE);
			return Either.right(error);
		}
		ComponentBusinessLogic bl = getComponentBL(componentType);

		Either<? extends Component, ResponseFormat> operationResult = null;
		Component component = null;
		// boolean needToUnlockResource = false;
		log.debug("get resource from graph");
		ResponseFormat errorResponse;

		Either<? extends Component, ResponseFormat> eitherResourceResponse = getComponentForChange(componentType, componentId, modifier, lifeCycleTransition, changeInfo);
		if (eitherResourceResponse.isRight()) {
			return eitherResourceResponse;
		}
		component = eitherResourceResponse.left().value();
		String resourceCurrVersion = component.getVersion();
		LifecycleStateEnum resourceCurrState = component.getLifecycleState();

		// lock resource
		if (inTransaction == false && needLock) {
			Either<Boolean, ResponseFormat> eitherLockResource = lockComponent(componentType, component);
			if (eitherLockResource.isRight()) {
				errorResponse = eitherLockResource.right().value();
				componentUtils.auditComponent(errorResponse, modifier, component, resourceCurrState.name(), resourceCurrVersion, lifeCycleTransition.getAuditingAction(), componentType, null);
				return Either.right(errorResponse);
			}
			// needToUnlockResource = true;
		}
		try {
			Either<String, ResponseFormat> commentValidationResult = validateComment(changeInfo, transitionEnum);
			if (commentValidationResult.isRight()) {
				errorResponse = commentValidationResult.right().value();
				EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_COMMENT, changeInfo.getUserRemarks());
				componentUtils.auditComponent(errorResponse, modifier, component, resourceCurrState.name(), resourceCurrVersion, lifeCycleTransition.getAuditingAction(), componentType, auditingFields);
				return Either.right(errorResponse);
			}
			changeInfo.setUserRemarks(commentValidationResult.left().value());

			Either<Boolean, ResponseFormat> validateHighestVersion = validateHighestVersion(modifier, lifeCycleTransition, component, resourceCurrVersion, componentType);
			if (validateHighestVersion.isRight()) {
				return Either.right(validateHighestVersion.right().value());
			}

			Either<User, ResponseFormat> ownerResult = lifeCycleTransition.getComponentOwner(component, componentType, inTransaction);
			if (ownerResult.isRight()) {
				return Either.right(ownerResult.right().value());
			}
			User owner = ownerResult.left().value();
			log.debug("owner of resource {} is {}", component.getUniqueId(), owner.getUserId());

			LifecycleStateEnum oldState = component.getLifecycleState();

			Either<Boolean, ResponseFormat> stateValidationResult = lifeCycleTransition.validateBeforeTransition(component, componentType, modifier, owner, oldState, changeInfo);
			if (stateValidationResult.isRight()) {
				errorResponse = stateValidationResult.right().value();
				EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_COMMENT, changeInfo.getUserRemarks());
				componentUtils.auditComponent(errorResponse, modifier, component, resourceCurrState.name(), resourceCurrVersion, lifeCycleTransition.getAuditingAction(), componentType, auditingFields);
				return Either.right(errorResponse);

			}

			operationResult = lifeCycleTransition.changeState(componentType, component, bl, modifier, owner, false, inTransaction);

			if (operationResult.isRight()) {
				errorResponse = operationResult.right().value();
				log.debug("audit before sending response");
				componentUtils.auditComponentAdmin(errorResponse, modifier, component, resourceCurrState.name(), resourceCurrVersion, lifeCycleTransition.getAuditingAction(), componentType);

				return Either.right(errorResponse);
			}
			Component resourceAfterOperation = operationResult.left().value();
			EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_COMMENT, changeInfo.getUserRemarks());
			componentUtils.auditComponent(componentUtils.getResponseFormat(ActionStatus.OK), modifier, resourceAfterOperation, resourceCurrState.name(), resourceCurrVersion, lifeCycleTransition.getAuditingAction(), componentType, auditingFields);
			return operationResult;

		} finally {
			log.debug("unlock component {}", componentId);
			if (inTransaction == false && needLock && component != null) {
				component.setUniqueId(componentId);
				NodeTypeEnum nodeType = componentType.getNodeType();

				// Handle component change in the cache of the side affect of
				// the operation
				if (operationResult != null && operationResult.isLeft()) {
					Component componentAfterOpertion = operationResult.left().value();
					String uniqueId = componentAfterOpertion.getUniqueId();
					if (false == componentId.equals(uniqueId)) {
						log.debug("During change state, another component {} has been created/updated", uniqueId);
						if (uniqueId != null) {
							cacheManagerOperation.updateComponentInCache(uniqueId, componentAfterOpertion.getLastUpdateDate(), nodeType);
						}
					}
				}

				graphLockOperation.unlockComponent(componentId, nodeType);

			}
		}

	}

	private Either<? extends Component, ResponseFormat> getComponentForChange(ComponentTypeEnum componentType, String componentId, User modifier, LifeCycleTransition lifeCycleTransition, LifecycleChangeInfoWithAction changeInfo) {

		Either<? extends Component, StorageOperationStatus> eitherResourceResponse = Either.right(StorageOperationStatus.GENERAL_ERROR);
		switch (componentType) {
		case SERVICE:
			eitherResourceResponse = serviceOperation.getComponent(componentId, true);
			break;
		case PRODUCT:
			eitherResourceResponse = productOperation.getComponent(componentId, true);
			break;
		case RESOURCE:
			eitherResourceResponse = resourceOperation.getComponent(componentId, true);
			break;
		default:
			break;
		}

		ResponseFormat errorResponse = null;
		if (eitherResourceResponse.isRight()) {
			ActionStatus actionStatus = componentUtils.convertFromStorageResponse(eitherResourceResponse.right().value(), componentType);
			errorResponse = componentUtils.getResponseFormat(actionStatus, Constants.EMPTY_STRING);
			log.debug("audit before sending response");
			EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_COMMENT, changeInfo.getUserRemarks());
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, componentId);
			componentUtils.auditComponent(errorResponse, modifier, null, Constants.EMPTY_STRING, Constants.EMPTY_STRING, lifeCycleTransition.getAuditingAction(), componentType, auditingFields);

			return Either.right(errorResponse);
		}
		return Either.left(eitherResourceResponse.left().value());
	}

	private Either<Boolean, ResponseFormat> validateHighestVersion(User modifier, LifeCycleTransition lifeCycleTransition, Resource resource, String resourceCurrVersion) {
		ResponseFormat errorResponse;
		if (!resource.isHighestVersion()) {
			log.debug("resource version {} is not the last version of resource {}", resource.getVersion(), resource.getName());
			errorResponse = componentUtils.getResponseFormat(ActionStatus.COMPONENT_HAS_NEWER_VERSION, resource.getName(), ComponentTypeEnum.RESOURCE.name().toLowerCase());
			componentUtils.auditResource(errorResponse, modifier, resource, resource.getLifecycleState().name(), resourceCurrVersion, lifeCycleTransition.getAuditingAction(), null);
			return Either.right(errorResponse);
		}
		return Either.left(true);
	}

	private Either<Boolean, ResponseFormat> validateResourceNotDeleted(User modifier, LifeCycleTransition lifeCycleTransition, Resource resource, String resourceCurrVersion) {

		ResponseFormat errorResponse;
		if ((resource.getIsDeleted() != null) && (resource.getIsDeleted() == true)) {
			ActionStatus actionStatus = ActionStatus.RESOURCE_NOT_FOUND;
			errorResponse = componentUtils.getResponseFormatByResource(actionStatus, resource.getName());
			log.debug("resource {} {} is marked for delete", resource.getName(), resource.getVersion());
			componentUtils.auditResource(errorResponse, modifier, null, "", "", lifeCycleTransition.getAuditingAction(), null);

			return Either.right(errorResponse);
		}
		return Either.left(true);
	}

	private Either<Boolean, ResponseFormat> validateHighestVersion(User modifier, LifeCycleTransition lifeCycleTransition, Component component, String resourceCurrVersion, ComponentTypeEnum componentType) {
		ResponseFormat errorResponse;
		if (!component.isHighestVersion()) {
			log.debug("Component version {} is not the last version of component {}", component.getComponentMetadataDefinition().getMetadataDataDefinition().getVersion(),
					component.getComponentMetadataDefinition().getMetadataDataDefinition().getName());
			errorResponse = componentUtils.getResponseFormat(ActionStatus.COMPONENT_HAS_NEWER_VERSION, component.getComponentMetadataDefinition().getMetadataDataDefinition().getName(), componentType.getValue().toLowerCase());
			componentUtils.auditComponentAdmin(errorResponse, modifier, component, component.getLifecycleState().name(), resourceCurrVersion, lifeCycleTransition.getAuditingAction(), componentType);
			return Either.right(errorResponse);
		}
		return Either.left(true);
	}

	private Either<Boolean, ResponseFormat> lockComponent(ComponentTypeEnum componentType, Component component) {
		NodeTypeEnum nodeType = componentType.getNodeType();
		StorageOperationStatus lockResourceStatus = graphLockOperation.lockComponent(component.getUniqueId(), nodeType);

		if (lockResourceStatus.equals(StorageOperationStatus.OK)) {
			return Either.left(true);
		} else {
			ActionStatus actionStatus = componentUtils.convertFromStorageResponse(lockResourceStatus);
			ResponseFormat responseFormat = componentUtils.getResponseFormat(actionStatus, component.getComponentMetadataDefinition().getMetadataDataDefinition().getName());
			return Either.right(responseFormat);
		}

	}

	private Either<Resource, ResponseFormat> getResourceForChange(String resourceId, User modifier, LifeCycleTransition lifeCycleTransition) {
		Either<Resource, StorageOperationStatus> eitherResourceResponse = resourceOperation.getResource(resourceId, true);

		ResponseFormat errorResponse = null;
		if (eitherResourceResponse.isRight()) {
			ActionStatus actionStatus = componentUtils.convertFromStorageResponse(eitherResourceResponse.right().value());
			errorResponse = componentUtils.getResponseFormatByResource(actionStatus, "");
			log.debug("audit before sending response");
			// For audit of not found, resourceName should be uniqueID according
			// to Ella
			EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceId);
			componentUtils.auditResource(errorResponse, modifier, null, "", "", lifeCycleTransition.getAuditingAction(), null);

			return Either.right(errorResponse);
		}
		Resource resource = eitherResourceResponse.left().value();

		return Either.left(resource);

	}

	private Either<String, ResponseFormat> validateComment(LifecycleChangeInfoWithAction changeInfo, LifeCycleTransitionEnum transitionEnum) {
		String comment = changeInfo.getUserRemarks();
		if (LifeCycleTransitionEnum.CANCEL_CERTIFICATION == transitionEnum || LifeCycleTransitionEnum.CERTIFY == transitionEnum || LifeCycleTransitionEnum.FAIL_CERTIFICATION == transitionEnum || LifeCycleTransitionEnum.CHECKIN == transitionEnum
				|| LifeCycleTransitionEnum.CERTIFICATION_REQUEST == transitionEnum
		// import?
		) {

			if (!ValidationUtils.validateStringNotEmpty(comment)) {
				log.debug("user comment cannot be empty or null.");
				ResponseFormat errorResponse = componentUtils.getResponseFormat(ActionStatus.MISSING_DATA, COMMENT);
				return Either.right(errorResponse);
			}

			comment = ValidationUtils.removeNoneUtf8Chars(comment);
			comment = ValidationUtils.removeHtmlTags(comment);
			comment = ValidationUtils.normaliseWhitespace(comment);
			comment = ValidationUtils.stripOctets(comment);

			if (!ValidationUtils.validateLength(comment, ValidationUtils.COMMENT_MAX_LENGTH)) {
				log.debug("user comment exceeds limit.");
				return Either.right(componentUtils.getResponseFormat(ActionStatus.EXCEEDS_LIMIT, COMMENT, String.valueOf(ValidationUtils.COMMENT_MAX_LENGTH)));
			}
			if (!ValidationUtils.validateIsEnglish(comment)) {
				return Either.right(componentUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
			}
		}
		return Either.left(comment);
	}

	private ComponentBusinessLogic getComponentBL(ComponentTypeEnum componentTypeEnum) {
		ComponentBusinessLogic businessLogic;
		switch (componentTypeEnum) {
		case RESOURCE: {
			businessLogic = this.resourceBusinessLogic;
			break;
		}
		case SERVICE: {
			businessLogic = this.serviceBusinessLogic;
			break;
		}
		case PRODUCT: {
			businessLogic = this.productBusinessLogic;
			break;
		}

		default: {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeSystemError, "getComponentBL");
			throw new IllegalArgumentException("Illegal component type:" + componentTypeEnum.getValue());
		}
		}
		return businessLogic;
	}

}
