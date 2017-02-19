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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.openecomp.sdc.be.components.distribution.engine.ServiceDistributionArtifactsBuilder;
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogic;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.RequirementAndRelationshipPair;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.ILifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.CapabilityOperation;
import org.openecomp.sdc.be.model.operations.impl.ResourceOperation;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;

public class CertificationRequestTransition extends LifeCycleTransition {

	private static Logger log = LoggerFactory.getLogger(CertificationRequestTransition.class.getName());

	private ServiceDistributionArtifactsBuilder serviceDistributionArtifactsBuilder;
	private ResourceOperation resourceOperation;
	private CapabilityOperation capabilityOperation;
	private ServiceBusinessLogic serviceBusinessLogic;
	private ToscaExportHandler toscaExportUtils;

	public CertificationRequestTransition(ComponentsUtils componentUtils, ILifecycleOperation lifecycleOperation, ServiceDistributionArtifactsBuilder serviceDistributionArtifactsBuilder, ServiceBusinessLogic serviceBusinessLogic,
			CapabilityOperation capabilityOperation, ToscaExportHandler toscaExportUtils) {
		super(componentUtils, lifecycleOperation);

		// authorized roles
		Role[] resourceServiceCheckoutRoles = { Role.ADMIN, Role.DESIGNER };
		// Role[] productCheckoutRoles = {Role.ADMIN, Role.PRODUCT_MANAGER,
		// Role.PRODUCT_STRATEGIST};
		addAuthorizedRoles(ComponentTypeEnum.RESOURCE, Arrays.asList(resourceServiceCheckoutRoles));
		addAuthorizedRoles(ComponentTypeEnum.SERVICE, Arrays.asList(resourceServiceCheckoutRoles));
		// TODO to be later defined for product
		// addAuthorizedRoles(ComponentTypeEnum.PRODUCT,
		// Arrays.asList(productCheckoutRoles));

		this.serviceDistributionArtifactsBuilder = serviceDistributionArtifactsBuilder;
		if (lifeCycleOperation != null)
			this.resourceOperation = lifeCycleOperation.getResourceOperation();
		this.serviceBusinessLogic = serviceBusinessLogic;
		this.capabilityOperation = capabilityOperation;
		this.toscaExportUtils = toscaExportUtils;
	}

	@Override
	public LifeCycleTransitionEnum getName() {
		return LifeCycleTransitionEnum.CERTIFICATION_REQUEST;
	}

	@Override
	public AuditingActionEnum getAuditingAction() {
		return AuditingActionEnum.CERTIFICATION_REQUEST_RESOURCE;
	}

	protected Either<Boolean, ResponseFormat> validateAllResourceInstanceCertified(Component component) {
		Either<Boolean, ResponseFormat> eitherResult = Either.left(true);

		List<ComponentInstance> resourceInstance = component.getComponentInstances();
		if (resourceInstance != null) {
			Optional<ComponentInstance> nonCertifiedRIOptional = resourceInstance.stream().filter(p -> !ValidationUtils.validateCertifiedVersion(p.getComponentVersion())).findAny();
			// Uncertified Resource Found
			if (nonCertifiedRIOptional.isPresent()) {
				ComponentInstance nonCertifiedRI = nonCertifiedRIOptional.get();
				ResponseFormat resFormat = getRelevantResponseFormatUncertifiedRI(nonCertifiedRI, component.getComponentType());
				eitherResult = Either.right(resFormat);
			}
		}
		return eitherResult;
	}

	private ResponseFormat getRelevantResponseFormatUncertifiedRI(ComponentInstance nonCertifiedRI, ComponentTypeEnum componentType) {

		ResponseFormat responseFormat;
		Either<Resource, StorageOperationStatus> eitherResource = resourceOperation.getResource(nonCertifiedRI.getComponentUid());
		if (eitherResource.isRight()) {

			responseFormat = componentUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);

		} else {
			ActionStatus actionStatus;
			Resource resource = eitherResource.left().value();
			Either<List<Resource>, StorageOperationStatus> status = resourceOperation.findLastCertifiedResourceByUUID(resource);

			if (ValidationUtils.validateMinorVersion(nonCertifiedRI.getComponentVersion())) {
				if (status.isRight() || status.left().value() == null || status.left().value().isEmpty()) {
					actionStatus = ActionStatus.VALIDATED_RESOURCE_NOT_FOUND;
				} else {
					actionStatus = ActionStatus.FOUND_ALREADY_VALIDATED_RESOURCE;
				}
			} else {
				if (status.isRight() || status.left().value() == null || status.left().value().isEmpty())
					actionStatus = ActionStatus.FOUND_LIST_VALIDATED_RESOURCES;
				else {
					actionStatus = ActionStatus.FOUND_ALREADY_VALIDATED_RESOURCE;
				}

			}
			String compType = (componentType == ComponentTypeEnum.RESOURCE) ? "VF" : "service";
			responseFormat = componentUtils.getResponseFormat(actionStatus, compType, resource.getName());
		}
		return responseFormat;
	}

	private Either<ActionStatus, Map<String, ArtifactDefinition>> validateMandatoryArtifactsSupplied(Map<String, ArtifactDefinition> artifacts) {

		if (artifacts == null || true == artifacts.isEmpty()) {
			return Either.left(ActionStatus.OK);
		}

		Map<String, ArtifactDefinition> invalidArtifacts = new HashMap<String, ArtifactDefinition>();
		for (Entry<String, ArtifactDefinition> artifact : artifacts.entrySet()) {

			ArtifactDefinition artifactDefinition = artifact.getValue();
			if (true == artifactDefinition.getMandatory()) {
				String artifactEsId = artifactDefinition.getEsId();
				if (artifactEsId == null || true == artifactEsId.isEmpty()) {
					invalidArtifacts.put(artifact.getKey(), artifactDefinition);
				}
			}
		}

		if (true == invalidArtifacts.isEmpty()) {
			return Either.left(ActionStatus.OK);
		} else {
			return Either.right(invalidArtifacts);
		}
	}

	@Override
	public Either<? extends Component, ResponseFormat> changeState(ComponentTypeEnum componentType, Component component, ComponentBusinessLogic componentBl, User modifier, User owner, boolean shouldLock, boolean inTransaction) {

		log.debug("start performing certification request for resource {}", component.getUniqueId());

		// Either<ActionStatus, Map<String, ArtifactDefinition>>
		// validateMandatoryArtifacts =
		// validateMandatoryArtifactsSupplied(component.getArtifacts());
		// log.debug("After checking mandatory artifacts were populated. Result
		// is " + validateMandatoryArtifacts);
		// if (validateMandatoryArtifacts.isRight()) {
		// ResponseFormat responseFormat = componentUtils
		// .getResponseFormatByMissingArtifacts(
		// componentType,
		// validateMandatoryArtifacts.right().value());
		// return Either.right(responseFormat);
		// }
		ActionStatus actionStatus = null;
		ResponseFormat responseFormat = null;

		if (componentType == ComponentTypeEnum.SERVICE || (componentType == ComponentTypeEnum.RESOURCE && ((Resource) component).getResourceType() == ResourceTypeEnum.VF)) {

			Either<Boolean, ResponseFormat> statusCert = validateAllResourceInstanceCertified(component);
			if (statusCert.isRight()) {
				return Either.right(statusCert.right().value());
			}

			statusCert = validateConfiguredAtomicReqCapSatisfied(component);
			if (statusCert.isRight()) {
				return Either.right(statusCert.right().value());
			}
		}
		if (componentType == ComponentTypeEnum.SERVICE) {
			Either<Boolean, StorageOperationStatus> status = validateDeloymentArtifactSupplied((Service) component);
			if (status.isRight()) {
				StorageOperationStatus operationStatus = status.right().value();
				actionStatus = componentUtils.convertFromStorageResponse(operationStatus);
			} else {
				Boolean isDeploymentArtifactExists = status.left().value();
				if (isDeploymentArtifactExists == null || isDeploymentArtifactExists.booleanValue() == false) {
					actionStatus = ActionStatus.SERVICE_DEPLOYMENT_ARTIFACT_NOT_FOUND;
				} else {
					Either<Service, ResponseFormat> generateHeatEnvResult = serviceBusinessLogic.generateHeatEnvArtifacts((Service) component, modifier, shouldLock);
					if (generateHeatEnvResult.isRight()) {
						return Either.right(generateHeatEnvResult.right().value());
					}
					Either<Service, ResponseFormat> generateVfModuleResult = serviceBusinessLogic.generateVfModuleArtifacts((Service) component, modifier, shouldLock);
					if (generateVfModuleResult.isRight()) {
						return Either.right(generateVfModuleResult.right().value());
					}
				}
			}

			if (actionStatus != null) {
				responseFormat = componentUtils.getResponseFormatByComponent(actionStatus, component, componentType);
				return Either.right(responseFormat);
			}

		}

		Either<Either<ArtifactDefinition, Operation>, ResponseFormat> eitherPopulated = componentBl.populateToscaArtifacts(component, modifier, true, inTransaction, shouldLock);
		if (eitherPopulated != null && eitherPopulated.isRight()) {
			return Either.right(eitherPopulated.right().value());
		}

		NodeTypeEnum nodeType = (componentType.equals(ComponentTypeEnum.SERVICE)) ? NodeTypeEnum.Service : NodeTypeEnum.Resource;
		Either<? extends Component, StorageOperationStatus> certificationRequestResult = lifeCycleOperation.requestCertificationComponent(nodeType, component, modifier, owner, inTransaction);
		if (certificationRequestResult.isRight()) {
			log.debug("checkout failed on graph");
			StorageOperationStatus response = certificationRequestResult.right().value();
			actionStatus = componentUtils.convertFromStorageResponse(response);

			if (response.equals(StorageOperationStatus.ENTITY_ALREADY_EXISTS)) {
				actionStatus = ActionStatus.COMPONENT_VERSION_ALREADY_EXIST;
			}
			responseFormat = componentUtils.getResponseFormatByComponent(actionStatus, component, componentType);
			return Either.right(responseFormat);
		}

		return Either.left(certificationRequestResult.left().value());
	}

	private Either<Boolean, ResponseFormat> validateConfiguredAtomicReqCapSatisfied(Component component) {
		log.debug("Submit for testing validation - Start validating configured req/cap satisfied for inner atomic instances, component id:{}", component.getUniqueId());
		List<ComponentInstance> componentInstances = component.getComponentInstances();
		if (componentInstances != null) {
			// Prepare relationships data structures
			// Better make it list than set in case we need to count req/cap
			// occurrences in the future
			Map<String, List<String>> reqName2Ids = new HashMap<>();
			Map<String, List<String>> capName2Ids = new HashMap<>();
			parseRelationsForReqCapVerification(component, reqName2Ids, capName2Ids);
			Map<String, Set<String>> requirementsToFulfillBeforeCert = configurationManager.getConfiguration().getRequirementsToFulfillBeforeCert();
			Map<String, Set<String>> capabilitiesToConsumeBeforeCert = configurationManager.getConfiguration().getCapabilitiesToConsumeBeforeCert();
			for (ComponentInstance compInst : componentInstances) {
				String compInstId = compInst.getUniqueId();
				OriginTypeEnum originType = compInst.getOriginType();
				if (originType == null) {
					log.error("Origin type is not set for component instance {} - it shouldn't happen. Skipping this component instance...", compInst.getUniqueId());
					continue;
				}
				String compInstType = originType.getValue();
				// Validating configured requirements fulfilled
				if (null != requirementsToFulfillBeforeCert) {
					Set<String> reqToFulfillForType = requirementsToFulfillBeforeCert.get(compInstType);
					if (reqToFulfillForType != null) {
						for (String reqNameToFulfill : reqToFulfillForType) {
							List<String> reqNameList = reqName2Ids.get(reqNameToFulfill);
							if (reqNameList == null || !reqNameList.contains(compInstId)) {
								log.debug("Requirement {} wasn't fulfilled for component instance {} of type {}", reqNameToFulfill, compInstId, compInstType);
								ComponentTypeEnum componentType = component.getComponentType();
								String compParam = (componentType == ComponentTypeEnum.RESOURCE) ? "VF" : componentType.getValue().toLowerCase();
								ResponseFormat responseFormat = componentUtils.getResponseFormat(ActionStatus.REQ_CAP_NOT_SATISFIED_BEFORE_CERTIFICATION, component.getName(), compParam, originType.getDisplayValue(), compInst.getName(), "requirement",
										reqNameToFulfill, "fulfilled");
								return Either.right(responseFormat);
							}
						}
					}
				}
				// Validating configured capabilities consumed
				if (null != capabilitiesToConsumeBeforeCert) {
					Set<String> capToConsumeForType = capabilitiesToConsumeBeforeCert.get(compInstType);
					if (capToConsumeForType != null) {
						for (String capNameToConsume : capToConsumeForType) {
							List<String> capNameList = capName2Ids.get(capNameToConsume);
							if (capNameList == null || !capNameList.contains(compInstId)) {
								log.debug("Capability {} wasn't consumed for component instance {} of type {}", capNameToConsume, compInstId, compInstType);
								ComponentTypeEnum componentType = component.getComponentType();
								String compParam = (componentType == ComponentTypeEnum.RESOURCE) ? "VF" : componentType.getValue().toLowerCase();
								ResponseFormat responseFormat = componentUtils.getResponseFormat(ActionStatus.REQ_CAP_NOT_SATISFIED_BEFORE_CERTIFICATION, component.getName(), compParam, originType.getDisplayValue(), compInst.getName(), "capability",
										capNameToConsume, "consumed");
								return Either.right(responseFormat);
							}
						}
					}
				}
			}
		}
		log.debug("Submit for testing validation - validating configured req/cap satisfied for inner atomic instances finished successfully, component id:{}", component.getUniqueId());
		return Either.left(true);
	}

	private Either<Boolean, ResponseFormat> parseRelationsForReqCapVerification(Component component, Map<String, List<String>> reqName2Ids, Map<String, List<String>> capName2Ids) {
		log.debug("Submit for testing validation - Preparing relations for inner atomic instances validation");
		List<RequirementCapabilityRelDef> componentInstancesRelations = component.getComponentInstancesRelations();
		if (componentInstancesRelations != null) {
			for (RequirementCapabilityRelDef reqCapRelDef : componentInstancesRelations) {
				List<RequirementAndRelationshipPair> relationships = reqCapRelDef.getRelationships();
				if (relationships != null) {
					for (RequirementAndRelationshipPair reqRelPair : relationships) {
						String capUniqueId = reqRelPair.getCapabilityUid();
						Either<CapabilityDefinition, StorageOperationStatus> capability = capabilityOperation.getCapability(capUniqueId);
						if (capability.isRight()) {
							log.error("Couldn't fetch capability by id {}", capUniqueId);
							return Either.right(componentUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
						}
						String reqCapType = capability.left().value().getType();
						String capabilityOwnerId = reqRelPair.getCapabilityOwnerId();
						String requirementOwnerId = reqRelPair.getRequirementOwnerId();
						// Update req
						List<String> reqIds = reqName2Ids.get(reqCapType);
						if (reqIds == null) {
							reqIds = new ArrayList<>();
							reqName2Ids.put(reqCapType, reqIds);
						}
						reqIds.add(requirementOwnerId);
						// Update cap
						List<String> capIds = capName2Ids.get(reqCapType);
						if (capIds == null) {
							capIds = new ArrayList<>();
							capName2Ids.put(reqCapType, capIds);
						}
						capIds.add(capabilityOwnerId);
					}
				}
			}
			log.debug("Parsed req for validation: {}, parsed cap for validation: {}", reqName2Ids, capName2Ids);
		} else {
			log.debug("There are no relations found for component {}", component.getUniqueId());
		}
		return Either.left(true);
	}

	@Override
	public Either<Boolean, ResponseFormat> validateBeforeTransition(Component component, ComponentTypeEnum componentType, User modifier, User owner, LifecycleStateEnum oldState, LifecycleChangeInfoWithAction lifecycleChangeInfo) {
		String componentName = component.getComponentMetadataDefinition().getMetadataDataDefinition().getName();
		log.debug("validate before certification request. resource name={}, oldState={}, owner userId={}", componentName, oldState, owner.getUserId());

		// validate user
		Either<Boolean, ResponseFormat> userValidationResponse = userRoleValidation(modifier, componentType, lifecycleChangeInfo);
		if (userValidationResponse.isRight()) {
			return userValidationResponse;
		}

		// case of "atomic" checkin and certification request - modifier must be
		// the owner
		if (oldState.equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT) && !modifier.equals(owner) && !modifier.getRole().equals(Role.ADMIN.name())) {
			ResponseFormat error = componentUtils.getResponseFormat(ActionStatus.COMPONENT_CHECKOUT_BY_ANOTHER_USER, componentName, componentType.name().toLowerCase(), owner.getFirstName(), owner.getLastName(), owner.getUserId());
			return Either.right(error);
		}

		// other states
		if (oldState.equals(LifecycleStateEnum.CERTIFIED)) {
			ResponseFormat error = componentUtils.getResponseFormat(ActionStatus.COMPONENT_ALREADY_CERTIFIED, componentName, componentType.name().toLowerCase(), owner.getFirstName(), owner.getLastName(), owner.getUserId());
			return Either.right(error);
		}
		if (oldState.equals(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS)) {
			ResponseFormat error = componentUtils.getResponseFormat(ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE, componentName, componentType.name().toLowerCase(), owner.getFirstName(), owner.getLastName(), owner.getUserId());
			return Either.right(error);
		}
		if (oldState.equals(LifecycleStateEnum.READY_FOR_CERTIFICATION)) {
			ResponseFormat error = componentUtils.getResponseFormat(ActionStatus.COMPONENT_SENT_FOR_CERTIFICATION, componentName, componentType.name().toLowerCase(), owner.getFirstName(), owner.getLastName(), owner.getUserId());
			return Either.right(error);
		}

		return Either.left(true);
	}

	private Either<Boolean, StorageOperationStatus> validateDeloymentArtifactSupplied(Service service) {

		Either<Boolean, StorageOperationStatus> serviceContainsDeploymentArtifacts = this.serviceDistributionArtifactsBuilder.isServiceContainsDeploymentArtifacts(service);

		return serviceContainsDeploymentArtifacts;

	}

}
