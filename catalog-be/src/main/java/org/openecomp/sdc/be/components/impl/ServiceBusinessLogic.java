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

package org.openecomp.sdc.be.components.impl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.components.distribution.engine.IDistributionEngine;
import org.openecomp.sdc.be.components.distribution.engine.INotificationData;
import org.openecomp.sdc.be.components.distribution.engine.VfModuleArtifactPayload;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GroupTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.DistributionTransitionEnum;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.operations.api.ICacheMangerOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IServiceOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ComponentOperation;
import org.openecomp.sdc.be.model.operations.impl.ServiceOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.model.operations.utils.ComponentValidationUtils;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionDeployEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionNotificationEvent;
import org.openecomp.sdc.be.resources.data.auditing.ResourceAdminEvent;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.kpi.api.ASDCKpiApi;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fj.data.Either;

@org.springframework.stereotype.Component("serviceBusinessLogic")
public class ServiceBusinessLogic extends ComponentBusinessLogic {

	private static final String STATUS_SUCCESS_200 = "200";

	private static final String STATUS_DEPLOYED = "DEPLOYED";

	@Autowired
	private IElementOperation elementDao;

	@Autowired
	private IDistributionEngine distributionEngine;

	// @Autowired
	// private AuditingDao auditingDao;

	@Autowired
	private AuditCassandraDao auditCassandraDao;

	@Autowired
	private ServiceComponentInstanceBusinessLogic serviceComponentInstanceBusinessLogic;

	@Autowired
	private ICacheMangerOperation cacheManagerOperation;

	private static Logger log = LoggerFactory.getLogger(ServiceBusinessLogic.class.getName());
	private static final String INITIAL_VERSION = "0.1";

	public ServiceBusinessLogic() {
		log.debug("ServiceBusinessLogic started");
	}

	public Either<Service, ResponseFormat> changeServiceDistributionState(String serviceId, String state, LifecycleChangeInfoWithAction commentObj, User user) {

		Either<User, ResponseFormat> resp = validateUserExists(user.getUserId(), "change Service Distribution State", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

		log.debug("check request state");
		Either<DistributionTransitionEnum, ResponseFormat> validateEnum = validateTransitionEnum(state, user);
		if (validateEnum.isRight()) {
			return Either.right(validateEnum.right().value());
		}
		DistributionTransitionEnum distributionTransition = validateEnum.left().value();
		AuditingActionEnum auditAction = (distributionTransition == DistributionTransitionEnum.APPROVE ? AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_APPROV : AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_REJECT);
		Either<String, ResponseFormat> commentResponse = validateComment(commentObj, user, auditAction);
		if (commentResponse.isRight()) {
			return Either.right(commentResponse.right().value());
		}
		String comment = commentResponse.left().value();

		Either<Service, ResponseFormat> validateService = validateServiceDistributionChange(user, serviceId, auditAction, comment);
		if (validateService.isRight()) {
			return Either.right(validateService.right().value());
		}
		Service service = validateService.left().value();
		DistributionStatusEnum initState = service.getDistributionStatus();

		Either<User, ResponseFormat> validateUser = validateUserDistributionChange(user, service, auditAction, comment);
		if (validateUser.isRight()) {
			return Either.right(validateUser.right().value());
		}
		user = validateUser.left().value();

		// lock resource
		/*
		 * StorageOperationStatus lockResult = graphLockOperation.lockComponent(serviceId, NodeTypeEnum.Service); if (!lockResult.equals(StorageOperationStatus.OK)) { BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.
		 * BeFailedLockObjectError, "ChangeServiceDistributionState"); log.debug("Failed to lock service {} error - {}", serviceId, lockResult); ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR,
		 * service.getVersion(), service.getServiceName());
		 * 
		 * createAudit(user, auditAction, comment, service, responseFormat); return Either.right(componentsUtils.getResponseFormat(ActionStatus. GENERAL_ERROR)); }
		 */
		Either<Boolean, ResponseFormat> lockResult = lockComponent(serviceId, service, "ChangeServiceDistributionState");
		if (lockResult.isRight()) {
			ResponseFormat responseFormat = lockResult.right().value();
			createAudit(user, auditAction, comment, service, responseFormat);
			return Either.right(responseFormat);
		}

		try {

			DistributionStatusEnum newState;
			if (distributionTransition == DistributionTransitionEnum.APPROVE) {
				newState = DistributionStatusEnum.DISTRIBUTION_APPROVED;
			} else {
				newState = DistributionStatusEnum.DISTRIBUTION_REJECTED;
			}
			Either<Service, StorageOperationStatus> result = serviceOperation.updateDestributionStatus(service, user, newState);
			if (result.isRight()) {
				titanGenericDao.rollback();
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeSystemError, "ChangeServiceDistributionState");
				BeEcompErrorManager.getInstance().logBeSystemError("ChangeServiceDistributionState");
				log.debug("service {} is  change destribuation status failed", service.getUniqueId());
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR, service.getVersion(), service.getName());
				createAudit(user, auditAction, comment, service, responseFormat);
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
			}
			titanGenericDao.commit();
			Service updatedService = result.left().value();
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
			EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_DCURR_STATUS, updatedService.getDistributionStatus().name());
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_DPREV_STATUS, initState.name());
			createAudit(user, auditAction, comment, updatedService, responseFormat, auditingFields);
			return Either.left(result.left().value());

		} finally {
			graphLockOperation.unlockComponent(serviceId, NodeTypeEnum.Service);
		}

	}

	public Either<List<Map<String, Object>>, ResponseFormat> getComponentAuditRecords(String componentVersion, String componentUUID, String userId) {
		Either<User, ResponseFormat> resp = validateUserExists(userId, "get Component Audit Records", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}
		Either<List<Map<String, Object>>, ActionStatus> result;
		try {

			// Certified Version
			if (componentVersion.endsWith(".0")) {
				Either<List<ResourceAdminEvent>, ActionStatus> eitherAuditingForCertified = auditCassandraDao.getByServiceInstanceId(componentUUID);
				if (eitherAuditingForCertified.isLeft()) {
					result = Either.left(getAuditingFieldsList(eitherAuditingForCertified.left().value()));
				} else {
					result = Either.right(eitherAuditingForCertified.right().value());
				}
			}
			// Uncertified Version
			else {
				result = getAuditRecordsForUncertifiedComponent(componentUUID, componentVersion);
			}
		} catch (Exception e) {
			log.debug("get Audit Records failed with exception {}", e);
			result = Either.right(ActionStatus.GENERAL_ERROR);
		}

		if (result.isRight()) {
			return Either.right(componentsUtils.getResponseFormat(result.right().value()));
		} else {
			return Either.left(result.left().value());
		}

	}

	private Either<List<Map<String, Object>>, ActionStatus> getAuditRecordsForUncertifiedComponent(String componentUUID, String componentVersion) {
		// First Query
		Either<List<ResourceAdminEvent>, ActionStatus> eitherprevVerAudit = auditCassandraDao.getAuditByServiceIdAndPrevVersion(componentUUID, componentVersion);

		if (eitherprevVerAudit.isRight()) {
			return Either.right(eitherprevVerAudit.right().value());
		}

		// Second Query
		Either<List<ResourceAdminEvent>, ActionStatus> eitherCurrVerAudit = auditCassandraDao.getAuditByServiceIdAndCurrVersion(componentUUID, componentVersion);
		if (eitherCurrVerAudit.isRight()) {
			return Either.right(eitherCurrVerAudit.right().value());
		}

		List<Map<String, Object>> prevVerAuditList = getAuditingFieldsList(eitherprevVerAudit.left().value());
		List<Map<String, Object>> currVerAuditList = getAuditingFieldsList(eitherCurrVerAudit.left().value());

		List<Map<String, Object>> duplicateElements = new ArrayList<Map<String, Object>>();
		duplicateElements.addAll(prevVerAuditList);
		duplicateElements.retainAll(currVerAuditList);

		List<Map<String, Object>> joinedNonDuplicatedList = new ArrayList<Map<String, Object>>();
		joinedNonDuplicatedList.addAll(prevVerAuditList);
		joinedNonDuplicatedList.removeAll(duplicateElements);
		joinedNonDuplicatedList.addAll(currVerAuditList);

		return Either.left(joinedNonDuplicatedList);
	}

	private List<Map<String, Object>> getAuditingFieldsList(List<? extends AuditingGenericEvent> prevVerAuditList) {

		List<Map<String, Object>> prevVerAudit = new ArrayList<Map<String, Object>>();
		for (AuditingGenericEvent auditEvent : prevVerAuditList) {
			auditEvent.fillFields();
			prevVerAudit.add(auditEvent.getFields());
		}
		return prevVerAudit;
	}

	/**
	 * createService
	 * 
	 * @param service
	 *            - Service
	 * @param user
	 *            - modifier data (userId)
	 * @return Either<Service, responseFormat>
	 */
	public Either<Service, ResponseFormat> createService(Service service, User user) {

		// get user details
		Either<User, ResponseFormat> eitherCreator = validateUser(user, "Create Service", service, AuditingActionEnum.CREATE_RESOURCE, false);
		if (eitherCreator.isRight()) {
			return Either.right(eitherCreator.right().value());
		}
		user = eitherCreator.left().value();

		// validate user role
		Either<Boolean, ResponseFormat> validateRes = validateUserRole(user, service, new ArrayList<Role>(), AuditingActionEnum.CREATE_RESOURCE, null);
		if (validateRes.isRight()) {
			return Either.right(validateRes.right().value());
		}
		service.setCreatorUserId(user.getUserId());

		// warn on overridden fields
		checkFieldsForOverideAttampt(service);
		// enrich object
		log.debug("enrich service with version and state");
		service.setState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		service.setVersion(INITIAL_VERSION);
		service.setDistributionStatus(DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED);

		Either<Service, ResponseFormat> createServiceResponse = validateServiceBeforeCreate(service, user, AuditingActionEnum.CREATE_RESOURCE);
		if (createServiceResponse.isRight()) {
			return createServiceResponse;
		}
		return createServiceByDao(service, AuditingActionEnum.CREATE_RESOURCE, serviceOperation, user);
	}

	private void checkFieldsForOverideAttampt(Service service) {
		checkComponentFieldsForOverrideAttempt(service);
		if ((service.getDistributionStatus() != null)) {
			log.info("Distribution Status cannot be defined by user. This field will be overridden by the application");
		}
	}

	private Either<Service, ResponseFormat> createServiceByDao(Service service, AuditingActionEnum actionEnum, IServiceOperation dataModel, User user) {
		log.debug("send service {} to dao for create", service.getComponentMetadataDefinition().getMetadataDataDefinition().getName());

		Either<Boolean, ResponseFormat> lockResult = lockComponentByName(service.getSystemName(), service, "Create Service");
		if (lockResult.isRight()) {
			ResponseFormat responseFormat = lockResult.right().value();
			componentsUtils.auditComponentAdmin(responseFormat, user, service, "", "", actionEnum, ComponentTypeEnum.SERVICE);
			return Either.right(responseFormat);
		}

		log.debug("System name locked is {}, status = {}", service.getSystemName(), lockResult);

		try {

			createMandatoryArtifactsData(service, user);
			createServiceApiArtifactsData(service, user);
			setToscaArtifactsPlaceHolders(service, user);

			Either<Service, StorageOperationStatus> dataModelResponse = dataModel.createService(service);

			// service created successfully!!!
			if (dataModelResponse.isLeft()) {
				log.debug("Service created successfully!!!");
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.CREATED);
				componentsUtils.auditComponentAdmin(responseFormat, user, service, "", "", actionEnum, ComponentTypeEnum.SERVICE);
				ASDCKpiApi.countCreatedServicesKPI();

				Service createdService = dataModelResponse.left().value();
				// //add service to cache
				// cacheManagerOperation.updateComponentInCache(createdService.getUniqueId(),
				// createdService.getLastUpdateDate(), NodeTypeEnum.Service);

				return Either.left(dataModelResponse.left().value());
			}

			ResponseFormat responseFormat = componentsUtils.getResponseFormatByComponent(componentsUtils.convertFromStorageResponse(dataModelResponse.right().value()), service, ComponentTypeEnum.SERVICE);
			log.debug("audit before sending response");
			componentsUtils.auditComponentAdmin(responseFormat, user, service, "", "", actionEnum, ComponentTypeEnum.SERVICE);
			return Either.right(responseFormat);

		} finally {
			graphLockOperation.unlockComponentByName(service.getSystemName(), service.getUniqueId(), NodeTypeEnum.Service);
		}
	}

	private void createServiceApiArtifactsData(Service service, User user) {
		// create mandatory artifacts

		// TODO it must be removed after that artifact uniqueId creation will be
		// moved to ArtifactOperation
		// String serviceUniqueId =
		// UniqueIdBuilder.buildServiceUniqueId(service.getComponentMetadataDefinition().getMetadataDataDefinition().getName(),
		// service.getComponentMetadataDefinition().getMetadataDataDefinition().getVersion());
		String serviceUniqueId = service.getUniqueId();
		Map<String, ArtifactDefinition> artifactMap = service.getArtifacts();
		if (artifactMap == null)
			artifactMap = new HashMap<String, ArtifactDefinition>();

		Map<String, Object> serviceApiArtifacts = ConfigurationManager.getConfigurationManager().getConfiguration().getServiceApiArtifacts();
		List<String> exludeServiceCategory = ConfigurationManager.getConfigurationManager().getConfiguration().getExcludeServiceCategory();

		List<CategoryDefinition> categories = service.getCategories();
		boolean isCreateArtifact = true;
		if (categories != null && exludeServiceCategory != null && !exludeServiceCategory.isEmpty()) {
			for (String exlude : exludeServiceCategory) {
				if (exlude.equalsIgnoreCase(categories.get(0).getName())) {
					isCreateArtifact = false;
					break;
				}
			}

		}

		if (serviceApiArtifacts != null && isCreateArtifact) {
			Set<String> keys = serviceApiArtifacts.keySet();
			for (String serviceApiArtifactName : keys) {
				Map<String, Object> artifactInfoMap = (Map<String, Object>) serviceApiArtifacts.get(serviceApiArtifactName);
				ArtifactDefinition artifactDefinition = createArtifactDefinition(serviceUniqueId, serviceApiArtifactName, artifactInfoMap, user, true);
				artifactDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.SERVICE_API);
				artifactMap.put(artifactDefinition.getArtifactLabel(), artifactDefinition);
			}

			service.setArtifacts(artifactMap);
		}
	}

	private Either<Service, ResponseFormat> validateServiceBeforeCreate(Service service, User user, AuditingActionEnum actionEnum) {

		Either<Boolean, ResponseFormat> validationResponse = validateServiceFieldsBeforeCreate(user, service, actionEnum);
		if (validationResponse.isRight()) {
			return Either.right(validationResponse.right().value());
		}
		service.setCreatorFullName(user.getFirstName() + " " + user.getLastName());
		service.setContactId(service.getContactId().toLowerCase());

		// Generate invariant UUID - must be here and not in operation since it
		// should stay constant during clone
		String invariantUUID = UniqueIdBuilder.buildInvariantUUID();
		service.setInvariantUUID(invariantUUID);

		return Either.left(service);
	}

	private Either<Boolean, ResponseFormat> validateServiceFieldsBeforeCreate(User user, Service service, AuditingActionEnum actionEnum) {
		Either<Boolean, ResponseFormat> componentsFieldsValidation = validateComponentFieldsBeforeCreate(user, service, actionEnum);
		if (componentsFieldsValidation.isRight()) {
			return componentsFieldsValidation;
		}

		// validate service name uniqueness
		log.debug("validate service name uniqueness");
		Either<Boolean, ResponseFormat> serviceNameUniquenessValidation = validateComponentNameUnique(user, service, actionEnum);
		if (serviceNameUniquenessValidation.isRight()) {
			return serviceNameUniquenessValidation;
		}

		// validate category
		log.debug("validate category");
		Either<Boolean, ResponseFormat> categoryValidation = validateServiceCategory(user, service, actionEnum);
		if (categoryValidation.isRight()) {
			return categoryValidation;
		}

		log.debug("validate projectName");
		Either<Boolean, ResponseFormat> projectCodeValidation = validateProjectCode(user, service, actionEnum);
		if (projectCodeValidation.isRight()) {
			return projectCodeValidation;
		}

		return Either.left(true);

	}

	private Either<Boolean, ResponseFormat> validateServiceCategory(User user, Service service, AuditingActionEnum actionEnum) {
		log.debug("validate Service category");

		if (service.getCategories() == null || service.getCategories().size() == 0) {
			ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_MISSING_CATEGORY, ComponentTypeEnum.SERVICE.getValue());
			componentsUtils.auditComponentAdmin(errorResponse, user, service, "", "", actionEnum, ComponentTypeEnum.SERVICE);
			return Either.right(errorResponse);
		}

		Either<Boolean, ResponseFormat> validatCategory = validateServiceCategory(service.getCategories());
		if (validatCategory.isRight()) {
			ResponseFormat responseFormat = validatCategory.right().value();
			componentsUtils.auditComponentAdmin(responseFormat, user, service, "", "", actionEnum, ComponentTypeEnum.SERVICE);
			return Either.right(responseFormat);
		}

		return Either.left(true);
	}

	public Either<Map<String, Boolean>, ResponseFormat> validateServiceNameExists(String serviceName, String userId) {

		Either<User, ResponseFormat> resp = validateUserExists(userId, "validate Service Name Exists", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

		Either<Boolean, StorageOperationStatus> dataModelResponse = serviceOperation.validateServiceNameExists(serviceName);

		if (dataModelResponse.isLeft()) {
			Map<String, Boolean> result = new HashMap<>();
			result.put("isValid", dataModelResponse.left().value());
			log.debug("validation was successfully performed.");
			return Either.left(result);
		}

		ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(dataModelResponse.right().value()));

		return Either.right(responseFormat);
	}

	public void setElementDao(IElementOperation elementDao) {
		this.elementDao = elementDao;
	}

	public void setServiceOperation(ServiceOperation serviceOperation) {
		this.serviceOperation = serviceOperation;
	}

	public void setCassandraAuditingDao(AuditCassandraDao auditingDao) {
		this.auditCassandraDao = auditingDao;
	}

	/*
	 * public void setUserAdmin(UserAdminBuisinessLogic userAdmin) { this.userAdmin = userAdmin; }
	 * 
	 * public void setComponentsUtils(ComponentsUtils componentsUtils) { this.componentsUtils = componentsUtils; }
	 * 
	 * public void setGraphLockOperation(IGraphLockOperation graphLockOperation) { this.graphLockOperation = graphLockOperation; }
	 */

	public ArtifactsBusinessLogic getArtifactBl() {
		return artifactsBusinessLogic;
	}

	public void setArtifactBl(ArtifactsBusinessLogic artifactBl) {
		this.artifactsBusinessLogic = artifactBl;
	}

	public Either<Service, ResponseFormat> updateServiceMetadata(String serviceId, Service serviceUpdate, User user) {
		Either<User, ResponseFormat> eitherCreator = validateUser(user, "updateServiceMetadata", serviceUpdate, null, false);
		if (eitherCreator.isRight()) {
			return Either.right(eitherCreator.right().value());
		}
		user = eitherCreator.left().value();

		// validate user role
		Either<Boolean, ResponseFormat> validateRes = validateUserRole(user, serviceUpdate, new ArrayList<Role>(), null, null);
		if (validateRes.isRight()) {
			return Either.right(validateRes.right().value());
		}

		Either<Service, StorageOperationStatus> storageStatus = serviceOperation.getService(serviceId);
		if (storageStatus.isRight()) {
			return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(storageStatus.right().value(), ComponentTypeEnum.SERVICE), ""));
		}

		Service currentService = storageStatus.left().value();

		if (!ComponentValidationUtils.canWorkOnService(currentService.getUniqueId(), serviceOperation, user.getUserId())) {
			log.info("Restricted operation for user {} on service {}", user.getUserId(), currentService.getCreatorUserId());
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
		}

		Either<Service, ResponseFormat> validationRsponse = validateAndUpdateServiceMetadata(user, currentService, serviceUpdate);
		if (validationRsponse.isRight()) {
			log.info("service update metadata: validations field.");
			return validationRsponse;
		}
		Service serviceToUpdate = validationRsponse.left().value();
		// lock resource

		Either<Boolean, ResponseFormat> lockResult = lockComponent(serviceId, currentService, "Update Service Metadata");
		if (lockResult.isRight()) {
			return Either.right(lockResult.right().value());
		}
		try {
			Either<Service, StorageOperationStatus> updateResponse = serviceOperation.updateService(serviceToUpdate, true);
			if (updateResponse.isRight()) {
				titanGenericDao.rollback();
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeSystemError, "Update Service Metadata");
				BeEcompErrorManager.getInstance().logBeSystemError("Update Service Metadata");
				log.debug("failed to update sevice {}", serviceToUpdate.getUniqueId());
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
			}
			titanGenericDao.commit();
			return Either.left(updateResponse.left().value());
		} finally {
			graphLockOperation.unlockComponent(serviceId, NodeTypeEnum.Service);
		}
	}

	private Either<Service, ResponseFormat> validateAndUpdateServiceMetadata(User user, Service currentService, Service serviceUpdate) {

		boolean hasBeenCertified = ValidationUtils.hasBeenCertified(currentService.getVersion());
		Either<Boolean, ResponseFormat> response = validateAndUpdateCategory(user, currentService, serviceUpdate, hasBeenCertified, null);
		if (response.isRight()) {
			ResponseFormat errorResponse = response.right().value();
			return Either.right(errorResponse);
		}

		String creatorUserIdUpdated = serviceUpdate.getCreatorUserId();
		String creatorUserIdCurrent = currentService.getCreatorUserId();
		if (creatorUserIdUpdated != null && !creatorUserIdCurrent.equals(creatorUserIdUpdated)) {
			log.info("update srvice: recived request to update creatorUserId to {} the field is not updatable ignoring.", creatorUserIdUpdated);
		}

		String creatorFullNameUpdated = serviceUpdate.getCreatorFullName();
		String creatorFullNameCurrent = currentService.getCreatorFullName();
		if (creatorFullNameUpdated != null && !creatorFullNameCurrent.equals(creatorFullNameUpdated)) {
			log.info("update srvice: recived request to update creatorFullName to {} the field is not updatable ignoring.", creatorFullNameUpdated);
		}

		String lastUpdaterUserIdUpdated = serviceUpdate.getLastUpdaterUserId();
		String lastUpdaterUserIdCurrent = currentService.getLastUpdaterUserId();
		if (lastUpdaterUserIdUpdated != null && !lastUpdaterUserIdCurrent.equals(lastUpdaterUserIdUpdated)) {
			log.info("update srvice: recived request to update lastUpdaterUserId to {} the field is not updatable ignoring.", lastUpdaterUserIdUpdated);
		}

		String lastUpdaterFullNameUpdated = serviceUpdate.getLastUpdaterFullName();
		String lastUpdaterFullNameCurrent = currentService.getLastUpdaterFullName();
		if (lastUpdaterFullNameUpdated != null && !lastUpdaterFullNameCurrent.equals(lastUpdaterFullNameUpdated)) {
			log.info("update srvice: recived request to update lastUpdaterFullName to {} the field is not updatable ignoring.", lastUpdaterFullNameUpdated );
		}

		response = validateAndUpdateServiceName(user, currentService, serviceUpdate, hasBeenCertified, null);
		if (response.isRight()) {
			ResponseFormat errorResponse = response.right().value();
			return Either.right(errorResponse);
		}

		DistributionStatusEnum distributionStatusUpdated = serviceUpdate.getDistributionStatus();
		DistributionStatusEnum distributionStatusCurrent = currentService.getDistributionStatus();
		if (distributionStatusUpdated != null && !distributionStatusUpdated.name().equals((distributionStatusCurrent != null ? distributionStatusCurrent.name() : null))) {
			log.info("update srvice: recived request to update distributionStatus to {} the field is not updatable ignoring.", distributionStatusUpdated);
		}

		if (serviceUpdate.getProjectCode() != null) {
			response = validateAndUpdateProjectCode(user, currentService, serviceUpdate, null);
			if (response.isRight()) {
				ResponseFormat errorResponse = response.right().value();
				return Either.right(errorResponse);
			}
		}

		response = validateAndUpdateIcon(user, currentService, serviceUpdate, hasBeenCertified, null);
		if (response.isRight()) {
			ResponseFormat errorResponse = response.right().value();
			return Either.right(errorResponse);
		}

		Long creationDateUpdated = serviceUpdate.getCreationDate();
		Long creationDateCurrent = currentService.getCreationDate();
		if (creationDateUpdated != null && !creationDateCurrent.equals(creationDateUpdated)) {
			log.info("update srvice: recived request to update creationDate to {} the field is not updatable ignoring.", creationDateUpdated);
		}

		String versionUpdated = serviceUpdate.getVersion();
		String versionCurrent = currentService.getVersion();
		if (versionUpdated != null && !versionCurrent.equals(versionUpdated)) {
			log.info("update srvice: recived request to update version to {} the field is not updatable ignoring.", versionUpdated);
		}

		response = validateAndUpdateDescription(user, currentService, serviceUpdate, hasBeenCertified, null);
		if (response.isRight()) {
			ResponseFormat errorResponse = response.right().value();
			return Either.right(errorResponse);
		}

		response = validateAndUpdateTags(user, currentService, serviceUpdate, hasBeenCertified, null);
		if (response.isRight()) {
			ResponseFormat errorResponse = response.right().value();
			return Either.right(errorResponse);
		}

		response = validateAndUpdateContactId(user, currentService, serviceUpdate, null);
		if (response.isRight()) {
			ResponseFormat errorResponse = response.right().value();
			return Either.right(errorResponse);
		}

		Long lastUpdateDateUpdated = serviceUpdate.getLastUpdateDate();
		Long lastUpdateDateCurrent = currentService.getLastUpdateDate();
		if (lastUpdateDateUpdated != null && !lastUpdateDateCurrent.equals(lastUpdateDateUpdated)) {
			log.info("update srvice: recived request to update lastUpdateDate to {} the field is not updatable ignoring.", lastUpdateDateUpdated);
		}

		LifecycleStateEnum lifecycleStateUpdated = serviceUpdate.getLifecycleState();
		LifecycleStateEnum lifecycleStateCurrent = currentService.getLifecycleState();
		if (lifecycleStateUpdated != null && !lifecycleStateCurrent.name().equals(lifecycleStateUpdated.name())) {
			log.info("update srvice: recived request to update lifecycleState to {} the field is not updatable ignoring.", lifecycleStateUpdated);
		}

		Boolean isHighestVersionUpdated = serviceUpdate.isHighestVersion();
		Boolean isHighestVersionCurrent = currentService.isHighestVersion();
		if (isHighestVersionUpdated != null && !isHighestVersionCurrent.equals(isHighestVersionUpdated)) {
			log.info("update srvice: recived request to update isHighestVersion to {} the field is not updatable ignoring.", isHighestVersionUpdated);
		}

		String uuidUpdated = serviceUpdate.getUUID();
		String uuidCurrent = currentService.getUUID();
		if (!uuidCurrent.equals(uuidUpdated)) {
			log.info("update srvice: recived request to update uuid to {} the field is not updatable ignoring.", uuidUpdated);
		}

		String currentInvariantUuid = currentService.getInvariantUUID();
		String updatedInvariantUuid = serviceUpdate.getInvariantUUID();

		if ((updatedInvariantUuid != null) && (!updatedInvariantUuid.equals(currentInvariantUuid))) {
			log.warn("Product invariant UUID is automatically set and cannot be updated");
			serviceUpdate.setInvariantUUID(currentInvariantUuid);
		}
		return Either.left(currentService);

	}

	private Either<Boolean, ResponseFormat> validateAndUpdateContactId(User user, Service currentService, Service serviceUpdate, AuditingActionEnum audatingAction) {
		String contactIdUpdated = serviceUpdate.getContactId();
		String contactIdCurrent = currentService.getContactId();
		if (!contactIdCurrent.equals(contactIdUpdated)) {
			Either<Boolean, ResponseFormat> validatContactId = validateContactId(user, serviceUpdate, audatingAction);
			if (validatContactId.isRight()) {
				ResponseFormat errorRespons = validatContactId.right().value();
				return Either.right(errorRespons);
			}
			currentService.setContactId(contactIdUpdated.toLowerCase());
		}
		return Either.left(true);
	}

	private Either<Boolean, ResponseFormat> validateAndUpdateTags(User user, Service currentService, Service serviceUpdate, boolean hasBeenCertified, AuditingActionEnum audatingAction) {
		List<String> tagsUpdated = serviceUpdate.getTags();
		List<String> tagsCurrent = currentService.getTags();
		if (tagsUpdated == null || tagsUpdated.isEmpty()) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_MISSING_TAGS);
			componentsUtils.auditComponentAdmin(responseFormat, user, serviceUpdate, "", "", audatingAction, ComponentTypeEnum.SERVICE);
			return Either.right(responseFormat);
		}

		if (!(tagsCurrent.containsAll(tagsUpdated) && tagsUpdated.containsAll(tagsCurrent))) {
			Either<Boolean, ResponseFormat> validatResponse = validateTagsListAndRemoveDuplicates(user, serviceUpdate, audatingAction);
			if (validatResponse.isRight()) {
				ResponseFormat errorRespons = validatResponse.right().value();
				return Either.right(errorRespons);
			}
			currentService.setTags(tagsUpdated);
		}
		return Either.left(true);
	}

	private Either<Boolean, ResponseFormat> validateAndUpdateDescription(User user, Service currentService, Service serviceUpdate, boolean hasBeenCertified, AuditingActionEnum audatingAction) {
		String descriptionUpdated = serviceUpdate.getDescription();
		String descriptionCurrent = currentService.getDescription();
		if (!descriptionCurrent.equals(descriptionUpdated)) {
			Either<Boolean, ResponseFormat> validateDescriptionResponse = validateDescriptionAndCleanup(user, serviceUpdate, audatingAction);
			if (validateDescriptionResponse.isRight()) {
				ResponseFormat errorRespons = validateDescriptionResponse.right().value();
				return Either.right(errorRespons);
			}
			currentService.setDescription(serviceUpdate.getDescription());
		}
		return Either.left(true);
	}

	private Either<Boolean, ResponseFormat> validateAndUpdateProjectCode(User user, Service currentService, Service serviceUpdate, AuditingActionEnum audatingAction) {
		String projectCodeUpdated = serviceUpdate.getProjectCode();
		String projectCodeCurrent = currentService.getProjectCode();
		if (!projectCodeCurrent.equals(projectCodeUpdated)) {
			Either<Boolean, ResponseFormat> validatProjectCodeResponse = validateProjectCode(user, serviceUpdate, audatingAction);
			if (validatProjectCodeResponse.isRight()) {
				ResponseFormat errorRespons = validatProjectCodeResponse.right().value();
				return Either.right(errorRespons);
			}
			currentService.setProjectCode(projectCodeUpdated);
		}
		return Either.left(true);
	}

	private Either<Boolean, ResponseFormat> validateAndUpdateIcon(User user, Service currentService, Service serviceUpdate, boolean hasBeenCertified, AuditingActionEnum audatingAction) {
		String iconUpdated = serviceUpdate.getIcon();
		String iconCurrent = currentService.getIcon();
		if (!iconCurrent.equals(iconUpdated)) {
			if (!hasBeenCertified) {
				Either<Boolean, ResponseFormat> validatIconResponse = validateIcon(user, serviceUpdate, audatingAction);
				if (validatIconResponse.isRight()) {
					ResponseFormat errorRespons = validatIconResponse.right().value();
					return Either.right(errorRespons);
				}
				currentService.setIcon(iconUpdated);
			} else {
				log.info("icon {} cannot be updated once the service has been certified once.", iconUpdated);
				ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.SERVICE_ICON_CANNOT_BE_CHANGED);
				return Either.right(errorResponse);
			}
		}
		return Either.left(true);
	}

	private Either<Boolean, ResponseFormat> validateAndUpdateServiceName(User user, Service currentService, Service serviceUpdate, boolean hasBeenCertified, AuditingActionEnum audatingAction) {
		String serviceNameUpdated = serviceUpdate.getName();
		String serviceNameCurrent = currentService.getName();
		if (!serviceNameCurrent.equals(serviceNameUpdated)) {
			if (!hasBeenCertified) {
				Either<Boolean, ResponseFormat> validatServiceNameResponse = validateComponentName(user, serviceUpdate, audatingAction);
				if (validatServiceNameResponse.isRight()) {
					ResponseFormat errorRespons = validatServiceNameResponse.right().value();
					return Either.right(errorRespons);
				}

				Either<Boolean, ResponseFormat> serviceNameUniquenessValidation = validateComponentNameUnique(user, serviceUpdate, audatingAction);
				if (serviceNameUniquenessValidation.isRight()) {
					return serviceNameUniquenessValidation;
				}
				currentService.setName(serviceNameUpdated);
				currentService.getComponentMetadataDefinition().getMetadataDataDefinition().setNormalizedName(ValidationUtils.normaliseComponentName(serviceNameUpdated));
				currentService.getComponentMetadataDefinition().getMetadataDataDefinition().setSystemName(ValidationUtils.convertToSystemName(serviceNameUpdated));

			} else {
				log.info("service name {} cannot be updated once the service has been certified once.", serviceNameUpdated);
				ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.SERVICE_NAME_CANNOT_BE_CHANGED);
				return Either.right(errorResponse);
			}
		}
		return Either.left(true);
	}

	private Either<Boolean, ResponseFormat> validateAndUpdateCategory(User user, Service currentService, Service serviceUpdate, boolean hasBeenCertified, AuditingActionEnum audatingAction) {
		List<CategoryDefinition> categoryUpdated = serviceUpdate.getCategories();
		List<CategoryDefinition> categoryCurrent = currentService.getCategories();
		Either<Boolean, ResponseFormat> validatCategoryResponse = validateServiceCategory(user, serviceUpdate, audatingAction);
		if (validatCategoryResponse.isRight()) {
			ResponseFormat errorRespons = validatCategoryResponse.right().value();
			return Either.right(errorRespons);
		}
		if (!categoryCurrent.get(0).getName().equals(categoryUpdated.get(0).getName())) {
			if (!hasBeenCertified) {
				currentService.setCategories(categoryUpdated);
			} else {
				log.info("category {} cannot be updated once the service has been certified once.", categoryUpdated);
				ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.SERVICE_CATEGORY_CANNOT_BE_CHANGED);
				return Either.right(errorResponse);
			}
		}
		return Either.left(true);

	}

	public Either<Boolean, ResponseFormat> validateServiceCategory(List<CategoryDefinition> list) {
		if (list != null) {
			if (list.size() > 1) {
				log.debug("Must be only one category for service");
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_TOO_MUCH_CATEGORIES, ComponentTypeEnum.SERVICE.getValue());
				return Either.right(responseFormat);
			}
			CategoryDefinition category = list.get(0);
			if (category.getSubcategories() != null) {
				log.debug("Subcategories cannot be defined for service");
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.SERVICE_CANNOT_CONTAIN_SUBCATEGORY);
				return Either.right(responseFormat);
			}
			if (!ValidationUtils.validateStringNotEmpty(category.getName())) {
				log.debug("Resource category is empty");
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_MISSING_CATEGORY, ComponentTypeEnum.SERVICE.getValue());
				return Either.right(responseFormat);
			}

			log.debug("validating service category {} against valid categories list", list);
			Either<List<CategoryDefinition>, ActionStatus> categorys = elementDao.getAllServiceCategories();
			if (categorys.isRight()) {
				log.debug("failed to retrive service categories from Titan");
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(categorys.right().value());
				return Either.right(responseFormat);
			}
			List<CategoryDefinition> categoryList = categorys.left().value();
			for (CategoryDefinition value : categoryList) {
				if (value.getName().equals(category.getName())) {
					return Either.left(true);
				}
			}
			log.debug("Category {} is not part of service category group. Service category valid values are {}", list, categoryList);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INVALID_CATEGORY, ComponentTypeEnum.SERVICE.getValue()));
		}
		return Either.left(false);
	}

	public ResponseFormat deleteService(String serviceId, User user) {
		ResponseFormat responseFormat;
		String ecompErrorContext = "delete service";

		Either<User, ResponseFormat> eitherCreator = validateUserExists(user, ecompErrorContext, false);
		if (eitherCreator.isRight()) {
			return eitherCreator.right().value();
		}
		user = eitherCreator.left().value();

		Either<Service, StorageOperationStatus> serviceStatus = serviceOperation.getService(serviceId);
		if (serviceStatus.isRight()) {
			log.debug("failed to get service {}", serviceId);
			return componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(serviceStatus.right().value()), "");
		}

		Service service = serviceStatus.left().value();

		StorageOperationStatus result = StorageOperationStatus.OK;
		Either<Boolean, ResponseFormat> lockResult = lockComponent(service, "Mark service to delete");
		if (lockResult.isRight()) {
			result = StorageOperationStatus.GENERAL_ERROR;
			return componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
		}

		try {

			result = markComponentToDelete(service);
			if (result.equals(StorageOperationStatus.OK)) {
				responseFormat = componentsUtils.getResponseFormat(ActionStatus.NO_CONTENT);
			} else {
				ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(result);
				responseFormat = componentsUtils.getResponseFormatByResource(actionStatus, service.getName());
			}
			return responseFormat;

		} finally {
			if (result == null || !result.equals(StorageOperationStatus.OK)) {
				log.warn("operation failed. do rollback");
				BeEcompErrorManager.getInstance().logBeSystemError("Delete Service");
				titanGenericDao.rollback();
			} else {
				log.debug("operation success. do commit");
				titanGenericDao.commit();
			}
			graphLockOperation.unlockComponent(serviceId, NodeTypeEnum.Service);
		}
	}

	public ResponseFormat deleteServiceByNameAndVersion(String serviceName, String version, User user) {
		ResponseFormat responseFormat;
		String ecompErrorContext = "delete service";
		Either<User, ResponseFormat> validateEmptyResult = validateUserNotEmpty(user, ecompErrorContext);
		if (validateEmptyResult.isRight()) {
			return validateEmptyResult.right().value();
		}

		Either<User, ResponseFormat> eitherCreator = validateUserExists(user, ecompErrorContext, false);
		if (eitherCreator.isRight()) {
			return eitherCreator.right().value();
		}
		user = eitherCreator.left().value();

		Either<Service, ResponseFormat> getResult = getServiceByNameAndVersion(serviceName, version, user.getUserId());
		if (getResult.isRight()) {
			return getResult.right().value();
		}
		Service service = getResult.left().value();

		StorageOperationStatus result = StorageOperationStatus.OK;
		Either<Boolean, ResponseFormat> lockResult = lockComponent(service, "Mark service to delete");
		if (lockResult.isRight()) {
			result = StorageOperationStatus.GENERAL_ERROR;
			return componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
		}

		try {
			result = markComponentToDelete(service);
			if (result.equals(StorageOperationStatus.OK)) {
				responseFormat = componentsUtils.getResponseFormat(ActionStatus.NO_CONTENT);
			} else {
				ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(result);
				responseFormat = componentsUtils.getResponseFormatByResource(actionStatus, service.getName());
			}
			return responseFormat;

		} finally {
			if (result == null || !result.equals(StorageOperationStatus.OK)) {
				log.warn("operation failed. do rollback");
				BeEcompErrorManager.getInstance().logBeSystemError("Delete Service");
				titanGenericDao.rollback();
			} else {
				log.debug("operation success. do commit");
				titanGenericDao.commit();
			}
			graphLockOperation.unlockComponent(service.getUniqueId(), NodeTypeEnum.Service);
		}
	}

	public Either<Service, ResponseFormat> getService(String serviceId, User user) {
		String ecompErrorContext = "Get service";
		Either<User, ResponseFormat> validateEmptyResult = validateUserNotEmpty(user, ecompErrorContext);
		if (validateEmptyResult.isRight()) {
			return Either.right(validateEmptyResult.right().value());
		}

		Either<User, ResponseFormat> eitherCreator = validateUserExists(user, ecompErrorContext, false);
		if (eitherCreator.isRight()) {
			return Either.right(eitherCreator.right().value());
		}
		user = eitherCreator.left().value();

		Either<Service, StorageOperationStatus> storageStatus = serviceOperation.getService(serviceId);
		if (storageStatus.isRight()) {
			log.debug("failed to get service by id {}", serviceId);
			return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(storageStatus.right().value(), ComponentTypeEnum.SERVICE), serviceId));
		}
		// Service service =
		// createServiceApiArtifactLIst(storageStatus.left().value());
		Service service = storageStatus.left().value();
		return Either.left(service);
	}

	public Either<Service, ResponseFormat> getServiceByNameAndVersion(String serviceName, String serviceVersion, String userId) {
		Either<User, ResponseFormat> resp = validateUserExists(userId, "get Service By Name And Version", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}
		Either<Service, StorageOperationStatus> storageStatus = serviceOperation.getServiceByNameAndVersion(serviceName, serviceVersion, null, false);
		if (storageStatus.isRight()) {
			log.debug("failed to get service by name {} and version {}", serviceName, serviceVersion);
			return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(storageStatus.right().value(), ComponentTypeEnum.SERVICE), serviceName));
		}
		Service service = storageStatus.left().value();
		return Either.left(service);
	}

	private void createMandatoryArtifactsData(Service service, User user) {
		// create mandatory artifacts

		// TODO it must be removed after that artifact uniqueId creation will be
		// moved to ArtifactOperation
		// String serviceUniqueId =
		// UniqueIdBuilder.buildServiceUniqueId(service.getComponentMetadataDefinition().getMetadataDataDefinition().getName(),
		// service.getComponentMetadataDefinition().getMetadataDataDefinition().getVersion());
		String serviceUniqueId = service.getUniqueId();
		Map<String, ArtifactDefinition> artifactMap = service.getArtifacts();
		if (artifactMap == null)
			artifactMap = new HashMap<String, ArtifactDefinition>();

		Map<String, Object> informationalServiceArtifacts = ConfigurationManager.getConfigurationManager().getConfiguration().getInformationalServiceArtifacts();
		List<String> exludeServiceCategory = ConfigurationManager.getConfigurationManager().getConfiguration().getExcludeServiceCategory();

		String category = service.getCategories().get(0).getName();
		boolean isCreateArtifact = true;
		if (category != null && exludeServiceCategory != null && !exludeServiceCategory.isEmpty()) {
			for (String exlude : exludeServiceCategory) {
				if (exlude.equalsIgnoreCase(category)) {
					isCreateArtifact = false;
					break;
				}
			}

		}

		if (informationalServiceArtifacts != null && isCreateArtifact) {
			Set<String> keys = informationalServiceArtifacts.keySet();
			for (String informationalServiceArtifactName : keys) {
				Map<String, Object> artifactInfoMap = (Map<String, Object>) informationalServiceArtifacts.get(informationalServiceArtifactName);
				ArtifactDefinition artifactDefinition = createArtifactDefinition(serviceUniqueId, informationalServiceArtifactName, artifactInfoMap, user, false);
				artifactMap.put(artifactDefinition.getArtifactLabel(), artifactDefinition);

			}

			service.setArtifacts(artifactMap);
		}
	}

	private ArtifactDefinition createArtifactDefinition(String serviceId, String logicalName, Map<String, Object> artifactInfoMap, User user, Boolean isServiceApi) {

		ArtifactDefinition artifactInfo = artifactsBusinessLogic.createArtifactPlaceHolderInfo(serviceId, logicalName, artifactInfoMap, user, ArtifactGroupTypeEnum.INFORMATIONAL);

		if (isServiceApi) {
			artifactInfo.setMandatory(false);
			artifactInfo.setServiceApi(true);
		}
		return artifactInfo;
	}

	private Either<DistributionTransitionEnum, ResponseFormat> validateTransitionEnum(String distributionTransition, User user) {
		DistributionTransitionEnum transitionEnum = null;

		transitionEnum = DistributionTransitionEnum.getFromDisplayName(distributionTransition);
		if (transitionEnum == null) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeSystemError, "Change Service Distribution");
			BeEcompErrorManager.getInstance().logBeSystemError("Change Service Distribution");
			log.info("state operation is not valid. operations allowed are: {}", DistributionTransitionEnum.valuesAsString());
			ResponseFormat error = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
			return Either.right(error);
		}

		return Either.left(transitionEnum);
	}

	private Either<String, ResponseFormat> validateComment(LifecycleChangeInfoWithAction comment, User user, AuditingActionEnum auditAction) {
		String data = comment.getUserRemarks();

		if (data == null || data.trim().isEmpty()) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeInvalidJsonInput, "Change Service Distribution");
			BeEcompErrorManager.getInstance().logBeInvalidJsonInput("Change Service Distribution");
			log.debug("user comment cannot be empty or null.");
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
		}
		data = ValidationUtils.removeNoneUtf8Chars(data);
		data = ValidationUtils.removeHtmlTags(data);
		data = ValidationUtils.normaliseWhitespace(data);
		data = ValidationUtils.stripOctets(data);

		if (!ValidationUtils.validateLength(data, ValidationUtils.COMMENT_MAX_LENGTH)) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeInvalidJsonInput, "Change Service Distribution");
			BeEcompErrorManager.getInstance().logBeInvalidJsonInput("Change Service Distribution");
			log.debug("user comment exceeds limit.");
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.EXCEEDS_LIMIT, "comment", String.valueOf(ValidationUtils.COMMENT_MAX_LENGTH)));
		}
		if (!ValidationUtils.validateIsEnglish(data)) {
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
		}
		return Either.left(data);
	}

	private Either<Service, ResponseFormat> validateServiceDistributionChange(User user, String serviceId, AuditingActionEnum auditAction, String comment) {
		Either<Service, StorageOperationStatus> storageStatus = serviceOperation.getService(serviceId);
		if (storageStatus.isRight()) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.SERVICE_NOT_FOUND, serviceId);
			createAudit(user, auditAction, comment, responseFormat);
			return Either.right(responseFormat);
		}
		Service service = storageStatus.left().value();

		if (service.getLifecycleState() != LifecycleStateEnum.CERTIFIED) {
			log.info("service {} is  not available for distribution. Should be in certified state", service.getUniqueId());
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.SERVICE_NOT_AVAILABLE_FOR_DISTRIBUTION, service.getVersion(), service.getName());
			createAudit(user, auditAction, comment, service, responseFormat);
			return Either.right(responseFormat);
		}
		return Either.left(service);
	}

	private Either<User, ResponseFormat> validateUserDistributionChange(User user, Service service, AuditingActionEnum auditAction, String comment) {
		log.debug("get user from DB");
		/*
		 * Either<User, ActionStatus> eitherCreator = userAdmin.getUser(user.getUserId());s if (eitherCreator.isRight() || eitherCreator.left().value() == null) { BeEcompErrorManager.getInstance().processEcompError(EcompErrorName. BeUserMissingError,
		 * "Activate Distribution", user.getUserId()); log. debug("changeServiceDistributionState method - user is not listed. userId=" + user.getUserId()); ResponseFormat responseFormat =
		 * componentsUtils.getResponseFormat(ActionStatus.USER_NOT_FOUND); createAudit(user, auditAction, comment, responseFormat); return Either.right(responseFormat); } user = eitherCreator.left().value(); log.debug("validate user role"); if
		 * (!validateUserTemp(user, Role.ADMIN, Role.GOVERNOR)) { log.info("role {} is not allowed to perform this action", user.getRole()); ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
		 * createAudit(user, auditAction, comment, service, responseFormat); return Either.right(responseFormat); }
		 */
		// get user details
		Either<User, ResponseFormat> eitherCreator = validateUser(user, "Activate Distribution", service, auditAction, false);
		if (eitherCreator.isRight()) {
			return Either.right(eitherCreator.right().value());
		}
		user = eitherCreator.left().value();

		// validate user role
		List<Role> roles = new ArrayList<>();
		roles.add(Role.ADMIN);
		roles.add(Role.GOVERNOR);
		Either<Boolean, ResponseFormat> validateRes = validateUserRole(user, service, roles, auditAction, comment);
		if (validateRes.isRight()) {
			return Either.right(validateRes.right().value());
		}
		return Either.left(user);
	}

	private void createAudit(User user, AuditingActionEnum auditAction, String comment, ResponseFormat responseFormat) {
		EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);

		createAudit(user, auditAction, comment, null, responseFormat, auditingFields);
	}

	private void createAudit(User user, AuditingActionEnum auditAction, String comment, Service component, ResponseFormat responseFormat) {
		EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_DCURR_STATUS, component.getDistributionStatus().name());
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_DPREV_STATUS, component.getDistributionStatus().name());
		createAudit(user, auditAction, comment, component, component.getLifecycleState().name(), component.getVersion(), responseFormat, auditingFields);
	}

	private void createAudit(User user, AuditingActionEnum auditAction, String comment, Service component, ResponseFormat responseFormat, EnumMap<AuditingFieldsKeysEnum, Object> auditingFields) {
		log.debug("audit before sending response");
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_COMMENT, comment);
		componentsUtils.auditComponent(responseFormat, user, component, null, null, auditAction, ComponentTypeEnum.SERVICE, auditingFields);
	}

	private void createAudit(User user, AuditingActionEnum auditAction, String comment, Service component, String prevState, String prevVersion, ResponseFormat responseFormat, EnumMap<AuditingFieldsKeysEnum, Object> auditingFields) {
		log.debug("audit before sending response");
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_COMMENT, comment);
		componentsUtils.auditComponent(responseFormat, user, component, prevState, prevVersion, auditAction, ComponentTypeEnum.SERVICE, auditingFields);
	}

	public Either<Service, ResponseFormat> activateDistribution(String serviceId, String envName, User modifier, HttpServletRequest request) {

		Either<User, ResponseFormat> eitherCreator = validateUserExists(modifier.getUserId(), "activate Distribution", false);
		if (eitherCreator.isRight()) {
			return Either.right(eitherCreator.right().value());
		}

		User user = eitherCreator.left().value();

		Either<Service, ResponseFormat> result = null;
		ResponseFormat response = null;
		Service updatedService = null;
		String did = ThreadLocalsHolder.getUuid();
		EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ID, did);
		// DE194021
		String configuredEnvName = ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration().getEnvironments().get(0);
		if (configuredEnvName != null && false == envName.equals(configuredEnvName)) {
			log.trace("Update environment name to be {} instead of {}", configuredEnvName, envName);
			envName = configuredEnvName;
		}
		// DE194021

		ServletContext servletContext = request.getSession().getServletContext();
		boolean isDistributionEngineUp = getHealthCheckBL(servletContext).isDistributionEngineUp(request.getSession().getServletContext()); // DE
		if (!isDistributionEngineUp) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeSystemError, "Distribution Engine is DOWN");
			BeEcompErrorManager.getInstance().logBeSystemError("Distribution Engine is DOWN");
			log.debug("Distribution Engine is DOWN");
			response = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
			return Either.right(response);
		}

		Either<Service, StorageOperationStatus> serviceRes = serviceOperation.getService(serviceId);
		if (serviceRes.isRight()) {
			log.debug("failed retrieving service");
			response = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(serviceRes.right().value(), ComponentTypeEnum.SERVICE), serviceId);
			componentsUtils.auditComponent(response, user, null, null, null, AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_REQUEST, ComponentTypeEnum.SERVICE, auditingFields);
			return Either.right(response);
		}
		Service service = serviceRes.left().value();
		String dcurrStatus = service.getDistributionStatus().name();
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_DPREV_STATUS, dcurrStatus);

		Either<INotificationData, StorageOperationStatus> readyForDistribution = distributionEngine.isReadyForDistribution(service, did, envName);
		if (readyForDistribution.isLeft()) {
			INotificationData notificationData = readyForDistribution.left().value();
			StorageOperationStatus notifyServiceResponse = distributionEngine.notifyService(did, service, notificationData, envName, user.getUserId(), user.getFullName());
			if (notifyServiceResponse == StorageOperationStatus.OK) {
				Either<Service, ResponseFormat> updateStateRes = updateDistributionStatusForActivation(service, user, DistributionStatusEnum.DISTRIBUTED);
				if (updateStateRes.isLeft() && updateStateRes.left().value() != null) {
					updatedService = updateStateRes.left().value();
					dcurrStatus = updatedService.getDistributionStatus().name();
				} else {
					// The response is not relevant
					updatedService = service;
				}
				ASDCKpiApi.countActivatedDistribution();
				response = componentsUtils.getResponseFormat(ActionStatus.OK);
				result = Either.left(updatedService);
			} else {
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeSystemError, "Activate Distribution - send notification");
				BeEcompErrorManager.getInstance().logBeSystemError("Activate Distribution - send notification");
				log.debug("distributionEngine.notifyService response is: {}", notifyServiceResponse);
				response = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
				result = Either.right(response);
			}
		} else {
			StorageOperationStatus distEngineValidationResponse = readyForDistribution.right().value();
			response = componentsUtils.getResponseFormatByDE(componentsUtils.convertFromStorageResponse(distEngineValidationResponse), service.getName(), envName);
			result = Either.right(response);
		}
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_DCURR_STATUS, dcurrStatus);
		componentsUtils.auditComponent(response, user, service, null, null, AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_REQUEST, ComponentTypeEnum.SERVICE, auditingFields);
		return result;
	}

	// convert to private after deletion of temp url
	public Either<Service, ResponseFormat> updateDistributionStatusForActivation(Service service, User user, DistributionStatusEnum state) {

		Either<User, ResponseFormat> resp = validateUserExists(user.getUserId(), "update Distribution Status For Activation", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

		String serviceId = service.getUniqueId();
		Either<Boolean, ResponseFormat> lockResult = lockComponent(serviceId, service, "updateDistributionStatusForActivation");
		if (lockResult.isRight()) {
			return Either.right(lockResult.right().value());
		}
		try {
			Either<Service, StorageOperationStatus> result = serviceOperation.updateDestributionStatus(service, user, state);
			if (result.isRight()) {
				titanGenericDao.rollback();
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeSystemError, "updateDistributionStatusForActivation");
				BeEcompErrorManager.getInstance().logBeSystemError("updateDistributionStatusForActivation");
				log.debug("service {}  change distribution status failed", serviceId);
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
			}
			titanGenericDao.commit();
			return Either.left(result.left().value());
		} finally {
			graphLockOperation.unlockComponent(serviceId, NodeTypeEnum.Service);
		}
	}

	public Either<Service, ResponseFormat> markDistributionAsDeployed(String serviceId, String did, User user) {

		Either<User, ResponseFormat> resp = validateUserExists(user.getUserId(), "mark Distribution As Deployed", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

		log.debug("mark distribution deployed");

		AuditingActionEnum auditAction = AuditingActionEnum.DISTRIBUTION_DEPLOY;
		Either<Service, StorageOperationStatus> getServiceResponse = serviceOperation.getService(serviceId);
		if (getServiceResponse.isRight()) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeServiceMissingError, "markDistributionAsDeployed", serviceId);
			BeEcompErrorManager.getInstance().logBeComponentMissingError("markDistributionAsDeployed", ComponentTypeEnum.SERVICE.getValue(), serviceId);
			log.debug("service {} not found", serviceId);
			ResponseFormat responseFormat = auditDeployError(did, user, auditAction, null, componentsUtils.convertFromStorageResponse(getServiceResponse.right().value(), ComponentTypeEnum.SERVICE), "");

			return Either.right(responseFormat);
		}

		Service service = getServiceResponse.left().value();

		Either<User, ResponseFormat> validateRoleForDeploy = validateRoleForDeploy(did, user, auditAction, service);
		if (validateRoleForDeploy.isRight()) {
			return Either.right(validateRoleForDeploy.right().value());
		}
		user = validateRoleForDeploy.left().value();

		return checkDistributionAndDeploy(did, user, auditAction, service);

	}

	public Either<Service, ResponseFormat> generateVfModuleArtifacts(Service service, User modifier, boolean shouldLock) {
		Function<ComponentInstance, List<ArtifactGenerator<ArtifactDefinition>>> artifactTaskGeneratorCreator = ri ->
		// Only one VF Module Artifact per instance - add it to a list of one
		Arrays.asList(new VfModuleArtifacGenerator(modifier, ri, service, shouldLock));

		return generateDeploymentArtifacts(service, modifier, artifactTaskGeneratorCreator);

	}

	private List<GroupDefinition> collectGroupsForCompInstance(ComponentInstance currVF, Wrapper<ResponseFormat> responseWrapper) {
		List<GroupDefinition> relevantGroups = new ArrayList<>();
		Either<List<GroupDefinition>, StorageOperationStatus> eitherGroups = groupOperation.getAllGroups(currVF.getComponentUid(), NodeTypeEnum.Resource);

		if (eitherGroups.isRight()) {
			final StorageOperationStatus storageStatus = eitherGroups.right().value();
			if (storageStatus != StorageOperationStatus.NOT_FOUND && storageStatus != StorageOperationStatus.OK) {
				ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(storageStatus);
				responseWrapper.setInnerElement(componentsUtils.getResponseFormat(actionStatus));
			}

		} else {
			relevantGroups = eitherGroups.left().value().stream().filter(p -> GroupTypeEnum.VF_MODULE.getGroupTypeName().equals(p.getType())).collect(Collectors.toList());
		}
		return relevantGroups;
	}

	private ArtifactDefinition getVfModuleArtifactForCompInstance(ComponentInstance currVF, Service service, User modifier, List<GroupDefinition> groupsForCurrVF, Wrapper<String> payloadWrapper, Wrapper<ResponseFormat> responseWrapper) {
		ArtifactDefinition vfModuleAertifact = null;

		Optional<ArtifactDefinition> optionalVfModuleArtifact = currVF.getDeploymentArtifacts().values().stream().filter(p -> p.getArtifactType().equals(ArtifactTypeEnum.VF_MODULES_METADATA.name())).findAny();
		if (optionalVfModuleArtifact.isPresent()) {
			vfModuleAertifact = optionalVfModuleArtifact.get();
		} else {
			Either<ArtifactDefinition, ResponseFormat> createVfModuleArtifact = createVfModuleArtifact(modifier, currVF, service, payloadWrapper.getInnerElement());
			if (createVfModuleArtifact.isLeft()) {
				vfModuleAertifact = createVfModuleArtifact.left().value();
			} else {
				responseWrapper.setInnerElement(createVfModuleArtifact.right().value());
			}
		}
		return vfModuleAertifact;
	}

	private void fillVfModuleHeatEnvPayload(List<GroupDefinition> groupsForCurrVF, ComponentInstance currVFInstance, Wrapper<String> payloadWrapper) {
		// Converts GroupDefinition to VfModuleArtifactPayload which is the
		// format used in the payload

		List<VfModuleArtifactPayload> vfModulePayloadForCurrVF = groupsForCurrVF.stream().map(group -> new VfModuleArtifactPayload(group)).collect(Collectors.toList());
		Collections.sort(vfModulePayloadForCurrVF, (art1, art2) -> VfModuleArtifactPayload.compareByGroupName(art1, art2));
		// Update Payload With Heat Env
		vfModulePayloadForCurrVF.stream().forEach(e -> addHeatEnvArtifactsToVFModulePayload(e, currVFInstance));

		final Gson gson = new GsonBuilder().setPrettyPrinting().create();

		String vfModulePayloadString = gson.toJson(vfModulePayloadForCurrVF);
		payloadWrapper.setInnerElement(vfModulePayloadString);

	}

	private void addHeatEnvArtifactsToVFModulePayload(VfModuleArtifactPayload vfModulePayload, ComponentInstance currVFInstance) {
		List<String> originalModuleArtifacts = vfModulePayload.getArtifacts();
		if (!MapUtils.isEmpty(currVFInstance.getDeploymentArtifacts()) && !CollectionUtils.isEmpty(originalModuleArtifacts)) {

			final Collection<ArtifactDefinition> depInsArtifacts = currVFInstance.getDeploymentArtifacts().values();
			// All Heat_ENV
			List<ArtifactDefinition> heatEnvArtifacts = depInsArtifacts.stream().filter(art -> art.getArtifactType().equals(ArtifactTypeEnum.HEAT_ENV.getType())).collect(Collectors.toList());
			// Unique Id Of Artifacts In the vf module
			List<String> moduleArtUniqueId = depInsArtifacts.stream().filter(art -> originalModuleArtifacts.contains(art.getArtifactUUID())).map(art -> art.getUniqueId()).collect(Collectors.toList());
			// Collect Only Heat Artifatcs that are Generated from artifacts in
			// the module
			List<String> relevantHeatEnvUUID = heatEnvArtifacts.stream().filter(heatEnv -> moduleArtUniqueId.contains(heatEnv.getGeneratedFromId())).map(heatEnv -> heatEnv.getArtifactUUID()).collect(Collectors.toList());

			List<String> fullArtifactList = new ArrayList<>();
			fullArtifactList.addAll(originalModuleArtifacts);
			fullArtifactList.addAll(relevantHeatEnvUUID);

			vfModulePayload.setArtifacts(fullArtifactList);
		}
	}

	private Either<ArtifactDefinition, ResponseFormat> generateVfModuleArtifact(User modifier, ComponentInstance currVFInstance, Service service, boolean shouldLock) {
		ArtifactDefinition vfModuleAertifact = null;
		Wrapper<ResponseFormat> responseWrapper = new Wrapper<>();
		Wrapper<String> payloadWrapper = new Wrapper<>();
		List<GroupDefinition> groupsForCurrVF = collectGroupsForCompInstance(currVFInstance, responseWrapper);
		if (responseWrapper.isEmpty()) {
			fillVfModuleHeatEnvPayload(groupsForCurrVF, currVFInstance, payloadWrapper);
		}
		if (responseWrapper.isEmpty()) {
			vfModuleAertifact = getVfModuleArtifactForCompInstance(currVFInstance, service, modifier, groupsForCurrVF, payloadWrapper, responseWrapper);
		}
		if (responseWrapper.isEmpty() && vfModuleAertifact != null) {
			vfModuleAertifact = fillVfModulePayload(modifier, currVFInstance, vfModuleAertifact, shouldLock, payloadWrapper, responseWrapper);
		}

		Either<ArtifactDefinition, ResponseFormat> result;
		if (responseWrapper.isEmpty()) {
			result = Either.left(vfModuleAertifact);
		} else {
			result = Either.right(responseWrapper.getInnerElement());
		}

		return result;
	}

	private ArtifactDefinition fillVfModulePayload(User modifier, ComponentInstance currVF, ArtifactDefinition vfModuleAertifact, boolean shouldLock, Wrapper<String> payloadWrapper, Wrapper<ResponseFormat> responseWrapper) {
		ArtifactDefinition result = null;
		final Either<Resource, StorageOperationStatus> eitherResource = resourceOperation.getResource(currVF.getComponentUid());
		if (eitherResource.isRight()) {
			responseWrapper.setInnerElement(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(eitherResource.right().value())));
		} else {
			Resource resource = eitherResource.left().value();
			Either<ArtifactDefinition, ResponseFormat> eitherPayload = artifactsBusinessLogic.generateArtifactPayload(vfModuleAertifact, resource, currVF.getName(), modifier, shouldLock, () -> System.currentTimeMillis(),
					() -> Either.left(artifactsBusinessLogic.createEsArtifactData(vfModuleAertifact, payloadWrapper.getInnerElement().getBytes(StandardCharsets.UTF_8))));
			if (eitherPayload.isLeft()) {
				result = eitherPayload.left().value();
			} else {
				responseWrapper.setInnerElement(eitherPayload.right().value());
			}
		}

		return result;
	}

	private Either<ArtifactDefinition, ResponseFormat> createVfModuleArtifact(User modifier, ComponentInstance currVF, Service service, String vfModulePayloadString) {

		ArtifactDefinition vfModuleArtifactDefinition = new ArtifactDefinition();

		vfModuleArtifactDefinition.setDescription("Auto-generated VF Modules information artifact");
		vfModuleArtifactDefinition.setArtifactDisplayName("Vf Modules Metadata");
		vfModuleArtifactDefinition.setArtifactType(ArtifactTypeEnum.VF_MODULES_METADATA.getType());
		vfModuleArtifactDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);
		vfModuleArtifactDefinition.setArtifactLabel("vfModulesMetadata");
		vfModuleArtifactDefinition.setTimeout(0);
		vfModuleArtifactDefinition.setArtifactName(currVF.getNormalizedName() + "_modules.json");
		vfModuleArtifactDefinition.setPayloadData(vfModulePayloadString);

		Either<ArtifactDefinition, StorageOperationStatus> addArifactToComponent = artifactOperation.addArifactToComponent(vfModuleArtifactDefinition, currVF.getUniqueId(), NodeTypeEnum.ResourceInstance, true, true);

		Either<ArtifactDefinition, ResponseFormat> result;
		if (addArifactToComponent.isLeft()) {
			result = Either.left(addArifactToComponent.left().value());
		} else {
			result = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(addArifactToComponent.right().value())));
		}

		return result;
	}

	public Either<Service, ResponseFormat> generateHeatEnvArtifacts(Service service, User modifier, boolean shouldLock) {

		Function<ComponentInstance, List<ArtifactGenerator<ArtifactDefinition>>> artifactTaskGeneratorCreator = resourceInstance ->
		// Get All Deployment Artifacts
		service.getComponentInstances().stream().filter(ri -> ri != null && ri == resourceInstance).filter(ri -> ri.getDeploymentArtifacts() != null).flatMap(ri -> ri.getDeploymentArtifacts().values().stream()).
		// Filter in Only Heat Env
				filter(depArtifact -> ArtifactTypeEnum.HEAT_ENV.getType().equals(depArtifact.getArtifactType())).
				// Create ArtifactGenerator from those Artifacts
				map(depArtifact -> new HeatEnvArtifactGenerator(depArtifact, service, resourceInstance.getName(), modifier, shouldLock)).collect(Collectors.toList());

		return generateDeploymentArtifacts(service, modifier, artifactTaskGeneratorCreator);

	}

	private <CallVal> Either<Service, ResponseFormat> generateDeploymentArtifacts(Service service, User modifier, Function<ComponentInstance, List<ArtifactGenerator<CallVal>>> artifactTaskGeneratorCreator) {

		List<Future<Either<CallVal, ResponseFormat>>> allFutures = new ArrayList<>();

		// Get Flat List of (Callable) ArtifactGenerator for all the RI in the
		// service
		if (service.getComponentInstances() != null) {
			List<ArtifactGenerator<CallVal>> artifactGenList = service.getComponentInstances().stream().flatMap(ri -> artifactTaskGeneratorCreator.apply(ri).stream()).collect(Collectors.toList());
			if (artifactGenList != null && !artifactGenList.isEmpty()) {
				ExecutorService executor = Executors.newFixedThreadPool(artifactGenList.size());

				artifactGenList.stream().forEach(e -> allFutures.add(executor.submit(e)));

				boolean isSuccess = true;
				ResponseFormat firstError = null;
				for (Future<Either<CallVal, ResponseFormat>> entry : allFutures) {
					try {
						Either<CallVal, ResponseFormat> actionStatus = entry.get(20, TimeUnit.SECONDS);
						if (actionStatus.isRight()) {
							isSuccess = false;
							if (firstError == null) {
								firstError = actionStatus.right().value();
							}
							log.debug("Failed to generate artifact error : {}", actionStatus.right().value());
						}
					} catch (Exception e) {
						log.debug("Failed to collect result from artifact generator ", e);
						isSuccess = false;
						firstError = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
					}
				}
				executor.shutdown();
				if (!isSuccess) {
					return Either.right(firstError);
				}
			}

		}
		return Either.left(service);
	}

	abstract class ArtifactGenerator<CallVal> implements Callable<Either<CallVal, ResponseFormat>> {

	}

	class HeatEnvArtifactGenerator extends ArtifactGenerator<ArtifactDefinition> {
		ArtifactDefinition artifactDefinition;
		Service service;
		String resourceInstanceName;
		User modifier;
		boolean shouldLock;

		HeatEnvArtifactGenerator(ArtifactDefinition artifactDefinition, Service service, String resourceInstanceName, User modifier, boolean shouldLock) {
			this.artifactDefinition = artifactDefinition;
			this.service = service;
			this.resourceInstanceName = resourceInstanceName;
			this.modifier = modifier;
			this.shouldLock = shouldLock;
		}

		@Override
		public Either<ArtifactDefinition, ResponseFormat> call() throws Exception {
			return artifactsBusinessLogic.generateHeatEnvArtifact(artifactDefinition, service, resourceInstanceName, modifier, shouldLock);
		}

		public ArtifactDefinition getArtifactDefinition() {
			return artifactDefinition;
		}

	}

	class VfModuleArtifacGenerator extends ArtifactGenerator<ArtifactDefinition> {
		private User user;
		private ComponentInstance componentInstance;
		private Service service;
		boolean shouldLock;

		@Override
		public Either<ArtifactDefinition, ResponseFormat> call() throws Exception {
			return generateVfModuleArtifact(user, componentInstance, service, shouldLock);
		}

		private VfModuleArtifacGenerator(User user, ComponentInstance componentInstance, Service service, boolean shouldLock) {
			super();
			this.user = user;
			this.componentInstance = componentInstance;
			this.service = service;
			this.shouldLock = shouldLock;
		}

	}

	private synchronized Either<Service, ResponseFormat> checkDistributionAndDeploy(String did, User user, AuditingActionEnum auditAction, Service service) {
		boolean isDeployed = isDistributionDeployed(did, service);
		if (isDeployed) {
			return Either.left(service);
		}
		Either<Boolean, ResponseFormat> distributionSuccess = checkDistributionSuccess(did, user, auditAction, service);
		if (distributionSuccess.isRight()) {
			return Either.right(distributionSuccess.right().value());
		}

		log.debug("mark distribution {} as deployed - success", did);
		componentsUtils.auditServiceDistributionDeployed(auditAction, service.getName(), service.getVersion(), service.getUUID(), did, STATUS_DEPLOYED, "OK", user);
		return Either.left(service);
	}

	private boolean isDistributionDeployed(String did, Service service) {
		Either<List<DistributionDeployEvent>, ActionStatus> alreadyDeployed = auditCassandraDao.getDistributionDeployByStatus(did, AuditingActionEnum.DISTRIBUTION_DEPLOY.getName(), STATUS_DEPLOYED);

		boolean isDeployed = false;
		if (alreadyDeployed.isLeft() && !alreadyDeployed.left().value().isEmpty()) {
			// already deployed
			log.debug("distribution {} is already deployed", did);
			isDeployed = true;
		}
		return isDeployed;
	}

	protected Either<Boolean, ResponseFormat> checkDistributionSuccess(String did, User user, AuditingActionEnum auditAction, Service service) {

		log.trace("checkDistributionSuccess");
		// get all "DRequest" records for this distribution
		// Either<List<ESTimeBasedEvent>, ActionStatus> distRequestsResponse =
		// auditingDao.getListOfDistributionByAction(did,
		// AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_REQUEST.getName(), "",
		// ResourceAdminEvent.class);
		Either<List<ResourceAdminEvent>, ActionStatus> distRequestsResponse = auditCassandraDao.getDistributionRequest(did, AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_REQUEST.getName());
		if (distRequestsResponse.isRight()) {
			ResponseFormat error = auditDeployError(did, user, auditAction, service, distRequestsResponse.right().value());
			return Either.right(error);
		}

		List<ResourceAdminEvent> distributionRequests = distRequestsResponse.left().value();
		if (distributionRequests.isEmpty()) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDistributionMissingError, "markDistributionAsDeployed", did);
			BeEcompErrorManager.getInstance().logBeDistributionMissingError("markDistributionAsDeployed", did);
			log.info("distribution {} is not found", did);
			ResponseFormat error = auditDeployError(did, user, auditAction, service, ActionStatus.DISTRIBUTION_REQUESTED_NOT_FOUND);
			return Either.right(error);
		}
		boolean isRequestSucceeded = false;
		for (ResourceAdminEvent event : distributionRequests) {
			String eventStatus = event.getStatus();
			if (eventStatus != null && eventStatus.equals(STATUS_SUCCESS_200)) {
				isRequestSucceeded = true;
				break;
			}
		}

		// get all "DNotify" records for this distribution
		// Either<List<ESTimeBasedEvent>, ActionStatus>
		// distNotificationsResponse =
		// auditingDao.getListOfDistributionByAction(did,
		// AuditingActionEnum.DISTRIBUTION_NOTIFY.getName(), "",
		// DistributionNotificationEvent.class);
		Either<List<DistributionNotificationEvent>, ActionStatus> distNotificationsResponse = auditCassandraDao.getDistributionNotify(did, AuditingActionEnum.DISTRIBUTION_NOTIFY.getName());
		if (distNotificationsResponse.isRight()) {
			ResponseFormat error = auditDeployError(did, user, auditAction, service, distNotificationsResponse.right().value());
			return Either.right(error);
		}

		List<DistributionNotificationEvent> distributionNotifications = distNotificationsResponse.left().value();
		boolean isNotificationsSucceeded = false;
		for (DistributionNotificationEvent event : distributionNotifications) {
			String eventStatus = event.getStatus();
			if (eventStatus != null && eventStatus.equals(STATUS_SUCCESS_200)) {
				isNotificationsSucceeded = true;
				break;
			}
		}

		// if request failed OR there are notifications that failed
		if (!(isRequestSucceeded && isNotificationsSucceeded)) {

			log.info("distribution {} has failed", did);
			ResponseFormat error = componentsUtils.getResponseFormat(ActionStatus.DISTRIBUTION_REQUESTED_FAILED, did);
			auditDeployError(did, user, auditAction, service, ActionStatus.DISTRIBUTION_REQUESTED_FAILED, did);
			return Either.right(error);
		}
		return Either.left(true);
	}

	private ResponseFormat auditDeployError(String did, User user, AuditingActionEnum auditAction, Service service, ActionStatus status, String... params) {

		ResponseFormat error = componentsUtils.getResponseFormat(status, params);
		String message = "";
		if (error.getMessageId() != null) {
			message = error.getMessageId() + ": ";
		}
		message += error.getFormattedMessage();

		if (service != null) {
			componentsUtils.auditServiceDistributionDeployed(auditAction, service.getName(), service.getVersion(), service.getUUID(), did, error.getStatus().toString(), message, user);
		} else {
			componentsUtils.auditServiceDistributionDeployed(auditAction, "", "", "", did, error.getStatus().toString(), message, user);
		}
		return error;
	}

	private Either<User, ResponseFormat> validateRoleForDeploy(String did, User user, AuditingActionEnum auditAction, Service service) {
		Either<User, ActionStatus> eitherCreator = userAdmin.getUser(user.getUserId(), false);
		if (eitherCreator.isRight() || eitherCreator.left().value() == null) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeUserMissingError, "Deploy Service", user.getUserId());
			BeEcompErrorManager.getInstance().logBeUserMissingError("Deploy Service", user.getUserId());
			log.debug("validateRoleForDeploy method - user is not listed. userId={}", user.getUserId());
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.USER_NOT_FOUND, user.getUserId());
			auditDeployError(did, user, auditAction, service, ActionStatus.USER_NOT_FOUND);
			return Either.right(responseFormat);
		}
		user = eitherCreator.left().value();
		log.debug("validate user role");
		List<Role> roles = new ArrayList<>();
		roles.add(Role.ADMIN);
		roles.add(Role.OPS);
		Either<Boolean, ResponseFormat> validateRes = validateUserRole(user, service, roles, auditAction, null);
		if (validateRes.isRight()) {
			log.info("role {} is not allowed to perform this action", user.getRole());
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
			auditDeployError(did, user, auditAction, service, ActionStatus.RESTRICTED_OPERATION);
			return Either.right(responseFormat);
		}
		return Either.left(user);

	}

	@Override
	public void setDeploymentArtifactsPlaceHolder(Component component, User user) {
		// TODO Auto-generated method stub

	}

	@Override
	public Either<List<String>, ResponseFormat> deleteMarkedComponents() {
		return deleteMarkedComponents(ComponentTypeEnum.SERVICE);
	}

	private HealthCheckBusinessLogic getHealthCheckBL(ServletContext context) {
		WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
		WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
		HealthCheckBusinessLogic healthCheckBl = webApplicationContext.getBean(HealthCheckBusinessLogic.class);
		return healthCheckBl;
	}

	@Override
	public ComponentInstanceBusinessLogic getComponentInstanceBL() {
		return serviceComponentInstanceBusinessLogic;
	}

	@Override
	public Either<List<ComponentInstance>, ResponseFormat> getComponentInstancesFilteredByPropertiesAndInputs(String componentId, ComponentTypeEnum componentTypeEnum, String userId, String searchText) {

		Either<User, ResponseFormat> resp = validateUserExists(userId, "Get Component Instances", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

		ComponentOperation componentOperation = getComponentOperation(componentTypeEnum);

		Either<List<ComponentInstance>, StorageOperationStatus> componentInstancesResponse = componentOperation.getAllComponentInstncesMetadata(componentId, componentTypeEnum.getNodeType());
		if (componentInstancesResponse.isRight()) {

			if (componentInstancesResponse.right().value().equals(StorageOperationStatus.NOT_FOUND)) {
				return Either.left(new ArrayList<ComponentInstance>());
			}
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(componentInstancesResponse.right().value()));
			return Either.right(responseFormat);
		}

		List<ComponentInstance> componentInstances = componentInstancesResponse.left().value();
		componentInstances = componentInstances.stream().filter(instance -> instance.getOriginType().equals(OriginTypeEnum.VF)).collect(Collectors.toList());

		return Either.left(componentInstances);
	}

	public ICacheMangerOperation getCacheManagerOperation() {
		return cacheManagerOperation;
	}

	public void setCacheManagerOperation(ICacheMangerOperation cacheManagerOperation) {
		this.cacheManagerOperation = cacheManagerOperation;
	}
}
