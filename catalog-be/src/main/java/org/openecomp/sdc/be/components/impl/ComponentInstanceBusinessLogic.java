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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.info.CreateAndAssotiateInfo;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceAttribute;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.IComponentInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IComponentOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ComponentOperation;
import org.openecomp.sdc.be.model.operations.impl.GroupOperation;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.model.operations.utils.ComponentValidationUtils;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fj.data.Either;

public abstract class ComponentInstanceBusinessLogic extends BaseBusinessLogic {

	private static final String ARTIFACT_PLACEHOLDER_FILE_EXTENSION = "fileExtension";

	static final String HEAT_ENV_NAME = "heatEnv";
	private static final String HEAT_ENV_SUFFIX = "env";

	private static Logger log = LoggerFactory.getLogger(ComponentInstanceBusinessLogic.class.getName());

	@Autowired
	private IComponentInstanceOperation componentInstanceOperation;

	@Autowired
	private PropertyOperation propertyOperation;

	@Autowired
	private ArtifactsBusinessLogic artifactBusinessLogic;

	@Autowired
	private GroupOperation groupOperation;

	public ComponentInstanceBusinessLogic() {
	}

	public Either<ComponentInstance, ResponseFormat> createComponentInstance(String containerComponentParam, String containerComponentId, String userId, ComponentInstance resourceInstance) {
		return createComponentInstance(containerComponentParam, containerComponentId, userId, resourceInstance, true, true, true);
	}

	public Either<ComponentInstance, ResponseFormat> createComponentInstance(String containerComponentParam, String containerComponentId, String userId, ComponentInstance resourceInstance, boolean inTransaction, boolean needLock,
			boolean createNewTransaction) {

		Either<User, ResponseFormat> resp = validateUserExists(userId, "create Component Instance", inTransaction);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

		Either<Boolean, ResponseFormat> validateValidJson = validateJsonBody(resourceInstance, ComponentInstance.class);
		if (validateValidJson.isRight()) {
			return Either.right(validateValidJson.right().value());
		}

		Either<ComponentTypeEnum, ResponseFormat> validateComponentType = validateComponentType(containerComponentParam);
		if (validateComponentType.isRight()) {
			return Either.right(validateComponentType.right().value());
		}

		final ComponentTypeEnum containerComponentType = validateComponentType.left().value();
		final ComponentOperation containerOperation = getComponentOperation(containerComponentType);

		Either<org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponentExists = validateComponentExists(containerComponentId, containerComponentType, inTransaction, createNewTransaction);
		if (validateComponentExists.isRight()) {
			return Either.right(validateComponentExists.right().value());
		}
		org.openecomp.sdc.be.model.Component containerComponent = validateComponentExists.left().value();

		Either<Boolean, ResponseFormat> validateAllowedToContainCompInstances = validateAllowedToContainCompInstances(containerComponent);
		if (validateAllowedToContainCompInstances.isRight()) {
			return Either.right(validateAllowedToContainCompInstances.right().value());
		}

		Either<Boolean, ResponseFormat> validateCanWorkOnComponent = validateCanWorkOnComponent(containerComponent, userId);
		if (validateCanWorkOnComponent.isRight()) {
			return Either.right(validateCanWorkOnComponent.right().value());
		}
		if (resourceInstance != null && containerComponentType != null) {
			Either<Boolean, ResponseFormat> validateComponentInstanceParentState = validateComponentInstanceParentState(containerComponentType, resourceInstance);
			if (validateComponentInstanceParentState.isRight()) {
				return Either.right(validateComponentInstanceParentState.right().value());
			}
		}
		if (needLock) {

			Either<Boolean, ResponseFormat> lockComponent = lockComponent(containerComponent, "createComponentInstance");

			if (lockComponent.isRight()) {
				return Either.right(lockComponent.right().value());
			}
		}

		Either<ComponentInstance, ResponseFormat> resultOp = null;
		try {
			log.debug("Try to create entry on graph");
			Either<Component, ResponseFormat> eitherResourceName = getOriginComponentNameFromComponentInstance(resourceInstance, inTransaction);

			if (eitherResourceName.isRight()) {
				resultOp = Either.right(eitherResourceName.right().value());
				return resultOp;
			}
			Component origComponent = eitherResourceName.left().value();

			resultOp = createComponentInstanceOnGraph(containerComponent, origComponent, resourceInstance, userId, containerOperation, inTransaction);
			return resultOp;

		} finally {
			if (needLock)
				unlockComponent(resultOp, containerComponent);
		}
	}

	public Either<CreateAndAssotiateInfo, ResponseFormat> createAndAssociateRIToRI(String containerComponentParam, String containerComponentId, String userId, CreateAndAssotiateInfo createAndAssotiateInfo) {

		Either<CreateAndAssotiateInfo, ResponseFormat> resultOp = null;
		ComponentInstance resourceInstance = createAndAssotiateInfo.getNode();
		RequirementCapabilityRelDef associationInfo = createAndAssotiateInfo.getAssociate();

		Either<User, ResponseFormat> resp = validateUserExists(userId, "create And Associate RI To RI", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

		Either<ComponentTypeEnum, ResponseFormat> validateComponentType = validateComponentType(containerComponentParam);
		if (validateComponentType.isRight()) {
			return Either.right(validateComponentType.right().value());
		}

		final ComponentTypeEnum containerComponentType = validateComponentType.left().value();
		final ComponentOperation containerOperation = getComponentOperation(containerComponentType);

		Either<org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponentExists = validateComponentExists(containerComponentId, containerComponentType, false, true);
		if (validateComponentExists.isRight()) {
			return Either.right(validateComponentExists.right().value());
		}
		org.openecomp.sdc.be.model.Component containerComponent = validateComponentExists.left().value();

		Either<Boolean, ResponseFormat> validateAllowedToContainCompInstances = validateAllowedToContainCompInstances(containerComponent);
		if (validateAllowedToContainCompInstances.isRight()) {
			return Either.right(validateAllowedToContainCompInstances.right().value());
		}

		Either<Boolean, ResponseFormat> validateCanWorkOnComponent = validateCanWorkOnComponent(containerComponent, userId);
		if (validateCanWorkOnComponent.isRight()) {
			return Either.right(validateCanWorkOnComponent.right().value());
		}

		Either<Boolean, ResponseFormat> lockComponent = lockComponent(containerComponent, "createAndAssociateRIToRI");
		if (lockComponent.isRight()) {
			return Either.right(lockComponent.right().value());
		}

		try {
			log.debug("Try to create entry on graph");
			NodeTypeEnum containerNodeType = containerComponentType.getNodeType();
			Either<Component, ResponseFormat> eitherResourceName = getOriginComponentNameFromComponentInstance(resourceInstance, true);

			if (eitherResourceName.isRight()) {
				resultOp = Either.right(eitherResourceName.right().value());
				return resultOp;
			}
			Component origComponent = eitherResourceName.left().value();

			Either<ComponentInstance, ResponseFormat> result = createComponentInstanceOnGraph(containerComponent, origComponent, resourceInstance, userId, containerOperation, true);
			if (result.isRight()) {
				log.debug("Failed to create resource instance {}", containerComponentId);
				resultOp = Either.right(result.right().value());
				return resultOp;

			}

			log.debug("Entity on graph is created.");
			ComponentInstance resResourceInfo = result.left().value();
			if (associationInfo.getFromNode() == null || associationInfo.getFromNode().isEmpty()) {
				associationInfo.setFromNode(resResourceInfo.getUniqueId());
			} else {
				associationInfo.setToNode(resResourceInfo.getUniqueId());
			}

			RequirementCapabilityRelDef requirementCapabilityRelDef = associationInfo;// createRequirementCapabilityrelDef(associationInfo);

			Either<RequirementCapabilityRelDef, StorageOperationStatus> resultReqCapDef = componentInstanceOperation.associateResourceInstances(containerComponentId, containerNodeType, requirementCapabilityRelDef, true);
			if (resultReqCapDef.isLeft()) {
				log.debug("Enty on graph is created.");
				RequirementCapabilityRelDef resReqCapabilityRelDef = resultReqCapDef.left().value();
				CreateAndAssotiateInfo resInfo = new CreateAndAssotiateInfo(resResourceInfo, resReqCapabilityRelDef);
				resultOp = Either.left(resInfo);
				return resultOp;

			} else {
				log.info("Failed to associate node {} with node {}", associationInfo.getFromNode(), associationInfo.getToNode());
				resultOp = Either.right(componentsUtils.getResponseFormatForResourceInstance(componentsUtils.convertFromStorageResponseForResourceInstance(resultReqCapDef.right().value(), true), "", null));
				return resultOp;
			}

		} finally {
			unlockComponent(resultOp, containerComponent);
		}
	}

	private Either<Component, ResponseFormat> getOriginComponentNameFromComponentInstance(ComponentInstance componentInstance, boolean inTransaction) {
		Either<Component, ResponseFormat> eitherResponse;
		Either<Component, StorageOperationStatus> eitherComponent = getCompInstOriginComponentOperation().getComponent(componentInstance.getComponentUid(), inTransaction);
		if (eitherComponent.isRight()) {
			log.debug("Failed to get origin component with id {} for component instance {} ", componentInstance.getComponentUid(), componentInstance.getName());
			eitherResponse = Either.right(componentsUtils.getResponseFormatForResourceInstance(componentsUtils.convertFromStorageResponse(eitherComponent.right().value(), ComponentTypeEnum.RESOURCE), "", null));
		} else {
			eitherResponse = Either.left(eitherComponent.left().value());
		}
		return eitherResponse;
	}

	private Either<String, ResponseFormat> handleNameLogic(Component origComponent, ComponentInstance componentInstance, ComponentTypeEnum containerComponentType, String containerComponentId, boolean isCreate, boolean inTransaction) {
		Either<String, ResponseFormat> eitherResult;
		final ComponentOperation containerOperation = getComponentOperation(containerComponentType);

		Either<Integer, StorageOperationStatus> componentInNumberStatus = containerOperation.increaseAndGetComponentInstanceCounter(containerComponentId, true);

		if (componentInNumberStatus.isRight()) {
			log.debug("Failed to get component instance number for container component {} ", containerComponentId);
			eitherResult = Either.right(componentsUtils.getResponseFormatForResourceInstance(componentsUtils.convertFromStorageResponseForResourceInstance(componentInNumberStatus.right().value(), true), "", null));
		} else {
			String resourceInNumber = componentInNumberStatus.left().value().toString();
			eitherResult = Either.left(resourceInNumber);
		}

		if (eitherResult.isLeft()) {
			Either<Boolean, ResponseFormat> eitherSpecificLogic;
			if (isCreate) {
				eitherSpecificLogic = handleNameLogicForNewComponentInstance(origComponent, componentInstance, eitherResult.left().value(), containerComponentType, inTransaction);
			} else {
				eitherSpecificLogic = handleNameLogicForUpdatingComponentInstance(origComponent, componentInstance, componentInNumberStatus, containerComponentType, inTransaction);
			}
			if (eitherSpecificLogic.isRight()) {
				eitherResult = Either.right(eitherSpecificLogic.right().value());
			}
		}
		return eitherResult;
	}

	private Either<Boolean, ResponseFormat> handleNameLogicForUpdatingComponentInstance(Component origComponent, ComponentInstance componentInstance, Either<Integer, StorageOperationStatus> componentInNumberStatus,
			ComponentTypeEnum containerComponentType, boolean inTransaction) {
		Either<Boolean, ResponseFormat> eitherResult = Either.left(true);
		if (componentInstance.getName() == null || componentInstance.getName().isEmpty()) {
			if (origComponent == null) {
				Either<Component, ResponseFormat> eitherResourceName = getOriginComponentNameFromComponentInstance(componentInstance, inTransaction);

				if (eitherResourceName.isRight()) {
					eitherResult = Either.right(eitherResourceName.right().value());
					return eitherResult;
				}
				origComponent = eitherResourceName.left().value();

				String resourceName = origComponent.getName();
				String logicalName = componentInstanceOperation.createComponentInstLogicalName(componentInNumberStatus.left().value().toString(), resourceName);
				componentInstance.setName(logicalName);
				if (containerComponentType == ComponentTypeEnum.RESOURCE) {
					Resource resource = (Resource) origComponent;
					componentInstance.setToscaComponentName(resource.getToscaResourceName());
				}

			}
		}

		Either<Boolean, ResponseFormat> eitherValidation = validateComponentInstanceName(componentInstance.getName(), componentInstance, false);
		if (eitherValidation.isRight()) {
			eitherResult = Either.right(eitherValidation.right().value());
		}
		return eitherResult;
	}

	private Either<Boolean, ResponseFormat> handleNameLogicForNewComponentInstance(Component origComponent, ComponentInstance componentInstance, String resourceInNumber, ComponentTypeEnum containerComponentType, boolean inTransaction) {
		Either<Boolean, ResponseFormat> eitherResult = Either.left(true);

		if (origComponent == null) {
			Either<Component, ResponseFormat> eitherResourceName = getOriginComponentNameFromComponentInstance(componentInstance, inTransaction);

			if (eitherResourceName.isRight()) {
				eitherResult = Either.right(eitherResourceName.right().value());
				return eitherResult;
			}

			origComponent = eitherResourceName.left().value();
		}

		String resourceName = origComponent.getName();
		componentInstance.setComponentName(resourceName);
		if (componentInstance.getName() == null || componentInstance.getName().isEmpty())
			componentInstance.setName(resourceName);
		String logicalName = componentInstanceOperation.createComponentInstLogicalName(resourceInNumber, componentInstance.getName());

		Either<Boolean, ResponseFormat> eitherValidation = validateComponentInstanceName(logicalName, componentInstance, true);
		if (eitherValidation.isRight()) {
			eitherResult = Either.right(eitherValidation.right().value());
		}
		if (containerComponentType == ComponentTypeEnum.RESOURCE) {
			Resource resource = (Resource) origComponent;
			componentInstance.setToscaComponentName(resource.getToscaResourceName());
		}

		return eitherResult;
	}

	public Either<ComponentInstance, ResponseFormat> createComponentInstanceOnGraph(String containerComponentParam, org.openecomp.sdc.be.model.Component containerComponent, Component origComponent, ComponentInstance componentInstance, String userId,
			boolean needLock, boolean inTransaction) {

		Either<ComponentTypeEnum, ResponseFormat> validateComponentType = validateComponentType(containerComponentParam);
		if (validateComponentType.isRight()) {
			return Either.right(validateComponentType.right().value());
		}

		final ComponentTypeEnum containerComponentType = validateComponentType.left().value();
		final ComponentOperation containerOperation = getComponentOperation(containerComponentType);
		Either<ComponentInstance, ResponseFormat> resultOp = null;
		if (needLock) {

			Either<Boolean, ResponseFormat> lockComponent = lockComponent(containerComponent, "createComponentInstance");

			if (lockComponent.isRight()) {
				return Either.right(lockComponent.right().value());
			}
		}
		try {
			resultOp = createComponentInstanceOnGraph(containerComponent, origComponent, componentInstance, userId, containerOperation, inTransaction);
			return resultOp;

		} finally {
			if (needLock)
				unlockComponent(resultOp, containerComponent);
		}

	}

	private Map<String, String> getExistingEnvVersions(ComponentInstance componentInstance) {
		if (null == componentInstance.getDeploymentArtifacts())
			return null;
		return componentInstance.getDeploymentArtifacts().values()
				//filter env artifacts
				.stream().filter(p -> p.getArtifactType().equals(ArtifactTypeEnum.HEAT_ENV.getType()))
				//map name to version 
				.collect(Collectors.toMap(a -> a.getArtifactName(), a -> a.getArtifactVersion()));
	}

	private Either<ComponentInstance, ResponseFormat> createComponentInstanceOnGraph(org.openecomp.sdc.be.model.Component containerComponent, Component origComponent, ComponentInstance componentInstance, String userId,
			ComponentOperation containerOperation, boolean inTransaction) {
		Either<ComponentInstance, ResponseFormat> resultOp;
		boolean nameAlreadyExist = true;
		String resourceInNumber = "";
		String containerComponentId = containerComponent.getUniqueId();
		ComponentTypeEnum containerComponentType = containerComponent.getComponentType();
		NodeTypeEnum containerNodeType = containerComponentType.getNodeType();
		NodeTypeEnum compInstNodeType = getNodeTypeOfComponentInstanceOrigin();
		while (nameAlreadyExist) {

			Either<String, ResponseFormat> eitherNameLogic = handleNameLogic(origComponent, componentInstance, containerComponent.getComponentType(), containerComponent.getUniqueId(), true, inTransaction);
			if (eitherNameLogic.isRight()) {
				return Either.right(eitherNameLogic.right().value());
			} else {
				resourceInNumber = eitherNameLogic.left().value();
			}

			Either<Boolean, StorageOperationStatus> isNameExistStatus = componentInstanceOperation.isComponentInstanceNameExist(containerComponentId, containerNodeType, null, componentInstance.getNormalizedName());
			if (isNameExistStatus.isRight()) {
				log.debug("Failed to check if component instance name exists for container component {}", containerComponentId);

				resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_INSTANCE_RELATION_NOT_FOUND, componentInstance.getName(), containerComponentId));
				return resultOp;
			}
			nameAlreadyExist = isNameExistStatus.left().value();

		}

		Either<ComponentInstance, StorageOperationStatus> result = componentInstanceOperation.createComponentInstance(containerComponentId, containerNodeType, resourceInNumber, componentInstance, compInstNodeType, inTransaction);

		if (result.isRight()) {
			log.debug("Failed to create entry on graph for component instance {}", componentInstance.getName());
			resultOp = Either.right(componentsUtils.getResponseFormatForResourceInstance(componentsUtils.convertFromStorageResponseForResourceInstance(result.right().value(), true), "", null));
			return resultOp;
		}

		log.debug("Entity on graph is created.");
		ComponentInstance compInst = result.left().value();

		Map<String, String> existingEnvVersions = getExistingEnvVersions(componentInstance);
		Either<ActionStatus, ResponseFormat> addComponentInstanceArtifacts = addComponentInstanceArtifacts(containerComponent, compInst, userId, inTransaction, existingEnvVersions);
		if (addComponentInstanceArtifacts.isRight()) {
			log.debug("Failed to create component instance {}", componentInstance.getName());
			resultOp = Either.right(addComponentInstanceArtifacts.right().value());
			return resultOp;
		}

		resultOp = Either.left(compInst);
		return resultOp;
	}

	/**
	 * addResourceInstanceArtifacts - add artifacts (HEAT_ENV) to resource instance The instance artifacts are generated from the resource's artifacts
	 * 
	 * @param componentInstance
	 * @param userId
	 * @param existingEnvVersions
	 * @param containerComponentId
	 * 
	 * @return
	 */
	protected Either<ActionStatus, ResponseFormat> addComponentInstanceArtifacts(org.openecomp.sdc.be.model.Component containerComponent, ComponentInstance componentInstance, String userId, boolean inTransaction,
			Map<String, String> existingEnvVersions) {
		log.debug("add artifacts to resource instance");

		ActionStatus status = setResourceArtifactsOnResourceInstance(componentInstance);
		if (!ActionStatus.OK.equals(status)) {
			ResponseFormat resultOp = componentsUtils.getResponseFormatForResourceInstance(status, "", null);
			return Either.right(resultOp);
		}

		// generate heat_env if necessary
		Map<String, ArtifactDefinition> componentDeploymentArtifacts = componentInstance.getDeploymentArtifacts();
		if (componentDeploymentArtifacts == null) {
			return Either.left(ActionStatus.OK);
		}
		Map<String, ArtifactDefinition> finalDeploymentArtifacts = new HashMap<String, ArtifactDefinition>(componentDeploymentArtifacts);
		for (ArtifactDefinition artifact : componentDeploymentArtifacts.values()) {
			// if (artifact.getArtifactType().equalsIgnoreCase(
			// ArtifactTypeEnum.HEAT.getType())) {
			String type = artifact.getArtifactType();

			if (!(type.equalsIgnoreCase(ArtifactTypeEnum.HEAT.getType()) || type.equalsIgnoreCase(ArtifactTypeEnum.HEAT_NET.getType()) || type.equalsIgnoreCase(ArtifactTypeEnum.HEAT_VOL.getType()))) {
				continue;
			}

			if (artifact.checkEsIdExist()) {
				Map<String, Object> deploymentResourceArtifacts = ConfigurationManager.getConfigurationManager().getConfiguration().getDeploymentResourceInstanceArtifacts();
				if (deploymentResourceArtifacts == null) {
					log.debug("no deployment artifacts are configured for resource instance");
					break;
				}
				Map<String, Object> placeHolderData = (Map<String, Object>) deploymentResourceArtifacts.get(HEAT_ENV_NAME);

				String envLabel = (artifact.getArtifactLabel() + HEAT_ENV_SUFFIX).toLowerCase();
				Either<ArtifactDefinition, ResponseFormat> createArtifactPlaceHolder = artifactBusinessLogic.createArtifactPlaceHolderInfo(componentInstance.getUniqueId(), envLabel, placeHolderData, userId, ArtifactGroupTypeEnum.DEPLOYMENT,
						inTransaction);
				if (createArtifactPlaceHolder.isRight()) {
					return Either.right(createArtifactPlaceHolder.right().value());
				}
				ArtifactDefinition artifactHeatEnv = createArtifactPlaceHolder.left().value();

				artifactHeatEnv.setHeatParamsUpdateDate(System.currentTimeMillis());
				artifactHeatEnv.setTimeout(0);
				buildHeatEnvFileName(artifact, artifactHeatEnv, placeHolderData);

				// rbetzer - keep env artifactVersion - changeComponentInstanceVersion flow
				handleEnvArtifactVersion(artifactHeatEnv, existingEnvVersions);
				Either<ArtifactDefinition, StorageOperationStatus> addHeatEnvArtifact = artifactBusinessLogic.addHeatEnvArtifact(artifactHeatEnv, artifact, componentInstance.getUniqueId(), NodeTypeEnum.ResourceInstance, true);
				if (addHeatEnvArtifact.isRight()) {
					log.debug("failed to create heat env artifact on resource instance");
					return Either.right(componentsUtils.getResponseFormatForResourceInstance(componentsUtils.convertFromStorageResponseForResourceInstance(addHeatEnvArtifact.right().value(), false), "", null));
				}

				ArtifactDefinition artifactDefinition = addHeatEnvArtifact.left().value();
				if (artifact.getHeatParameters() != null) {
					List<HeatParameterDefinition> heatEnvParameters = new ArrayList<HeatParameterDefinition>();
					for (HeatParameterDefinition parameter : artifact.getHeatParameters()) {
						HeatParameterDefinition heatEnvParameter = new HeatParameterDefinition(parameter);
						heatEnvParameter.setDefaultValue(parameter.getCurrentValue());
						heatEnvParameters.add(heatEnvParameter);
					}
					artifactDefinition.setHeatParameters(heatEnvParameters);
				}
				finalDeploymentArtifacts.put(envLabel, artifactDefinition);

				// audit
				EnumMap<AuditingFieldsKeysEnum, Object> artifactAuditingFields = artifactBusinessLogic.createArtifactAuditingFields(artifactDefinition, "", artifactDefinition.getUniqueId());
				artifactAuditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, componentInstance.getName());
				handleAuditing(AuditingActionEnum.ARTIFACT_UPLOAD, containerComponent, userId, artifactAuditingFields, inTransaction);
			}
			// }
		}
		componentInstance.setDeploymentArtifacts(finalDeploymentArtifacts);
		return Either.left(ActionStatus.OK);
	}

	private void handleAuditing(AuditingActionEnum artifactUpload, org.openecomp.sdc.be.model.Component containerComponent, String userId, EnumMap<AuditingFieldsKeysEnum, Object> artifactAuditingFields, boolean inTransaction) {

		Either<User, ActionStatus> user = userAdmin.getUser(userId, inTransaction);
		if (user.isRight()) {
			log.debug("failed to get user properties from graph for audit");
			return;
		}

		componentsUtils.auditComponent(componentsUtils.getResponseFormat(ActionStatus.OK), user.left().value(), containerComponent, "", "", AuditingActionEnum.ARTIFACT_UPLOAD, ComponentTypeEnum.RESOURCE_INSTANCE, artifactAuditingFields);

	}

	private void handleEnvArtifactVersion(ArtifactDefinition heatEnvArtifact, Map<String, String> existingEnvVersions) {
		if (null != existingEnvVersions) {
			String prevVersion = existingEnvVersions.get(heatEnvArtifact.getArtifactName());
			if (null != prevVersion) {
				heatEnvArtifact.setArtifactVersion(prevVersion);
			}
		}
	}

	private void buildHeatEnvFileName(ArtifactDefinition heatArtifact, ArtifactDefinition heatEnvArtifact, Map<String, Object> placeHolderData) {
		String heatExtension = GeneralUtility.getFilenameExtension(heatArtifact.getArtifactName());
		String envExtension = (String) placeHolderData.get(ARTIFACT_PLACEHOLDER_FILE_EXTENSION);
		String fileName = heatArtifact.getArtifactName().replaceAll("." + heatExtension, "." + envExtension);
		heatEnvArtifact.setArtifactName(fileName);
	}

	private ActionStatus setResourceArtifactsOnResourceInstance(ComponentInstance resourceInstance) {
		Either<Map<String, ArtifactDefinition>, StorageOperationStatus> getResourceDeploymentArtifacts = artifactBusinessLogic.getArtifacts(resourceInstance.getComponentUid(), NodeTypeEnum.Resource, true, ArtifactGroupTypeEnum.DEPLOYMENT);

		Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap<String, ArtifactDefinition>();
		if (getResourceDeploymentArtifacts.isRight()) {
			StorageOperationStatus status = getResourceDeploymentArtifacts.right().value();
			if (!status.equals(StorageOperationStatus.NOT_FOUND)) {
				log.debug("Failed to fetch resource artifacts. status is {}", status);
				return componentsUtils.convertFromStorageResponseForResourceInstance(status, true);
			}
		} else {
			deploymentArtifacts = getResourceDeploymentArtifacts.left().value();
		}

		if (!deploymentArtifacts.isEmpty()) {
			Map<String, ArtifactDefinition> tempDeploymentArtifacts = new HashMap<String, ArtifactDefinition>(deploymentArtifacts);
			for (Entry<String, ArtifactDefinition> artifact : deploymentArtifacts.entrySet()) {
				if (!artifact.getValue().checkEsIdExist()) {
					tempDeploymentArtifacts.remove(artifact.getKey());
				}
			}

			resourceInstance.setDeploymentArtifacts(tempDeploymentArtifacts);
		}

		return ActionStatus.OK;
	}

	public Either<ComponentInstance, ResponseFormat> updateComponentInstance(String containerComponentParam, String containerComponentId, String componentInstanceId, String userId, ComponentInstance componentInstance) {
		return updateComponentInstance(containerComponentParam, containerComponentId, componentInstanceId, userId, componentInstance, false, true, true);
	}

	public Either<ComponentInstance, ResponseFormat> updateComponentInstance(String containerComponentParam, String containerComponentId, String componentInstanceId, String userId, ComponentInstance componentInstance, boolean inTransaction,
			boolean needLock, boolean createNewTransaction) {

		Either<User, ResponseFormat> resp = validateUserExists(userId, "update Component Instance", inTransaction);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

		Either<ComponentInstance, ResponseFormat> resultOp = null;

		Either<ComponentTypeEnum, ResponseFormat> validateComponentType = validateComponentType(containerComponentParam);
		if (validateComponentType.isRight()) {
			return Either.right(validateComponentType.right().value());
		}

		final ComponentTypeEnum containerComponentType = validateComponentType.left().value();

		Either<org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponentExists = validateComponentExists(containerComponentId, containerComponentType, inTransaction, createNewTransaction);
		if (validateComponentExists.isRight()) {
			return Either.right(validateComponentExists.right().value());
		}
		org.openecomp.sdc.be.model.Component containerComponent = validateComponentExists.left().value();

		Either<Boolean, ResponseFormat> validateCanWorkOnComponent = validateCanWorkOnComponent(containerComponent, userId);
		if (validateCanWorkOnComponent.isRight()) {
			return Either.right(validateCanWorkOnComponent.right().value());
		}
		ComponentTypeEnum instanceType = getComponentType(containerComponentType);
		Either<Boolean, StorageOperationStatus> validateParentStatus = componentInstanceOperation.validateParent(containerComponentId, componentInstanceId, inTransaction);
		if (validateParentStatus.isRight()) {
			log.debug("Failed to get component instance {} on service {}", componentInstanceId, containerComponentId);
			resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND, componentInstance.getName(), instanceType.getValue().toLowerCase()));
			return resultOp;
		}
		Boolean isPrentValid = validateParentStatus.left().value();
		if (!isPrentValid) {
			resultOp = Either.right(
					componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER, componentInstance.getName(), instanceType.getValue().toLowerCase(), containerComponentType.getValue().toLowerCase(), containerComponentId));
			return resultOp;

		}

		if (needLock) {

			Either<Boolean, ResponseFormat> lockComponent = lockComponent(containerComponent, "updateComponentInstance");
			if (lockComponent.isRight()) {
				return Either.right(lockComponent.right().value());
			}
		}

		try {

			Either<Component, ResponseFormat> eitherResourceName = getOriginComponentNameFromComponentInstance(componentInstance, inTransaction);

			if (eitherResourceName.isRight()) {
				resultOp = Either.right(eitherResourceName.right().value());
				return resultOp;
			}
			Component origComponent = eitherResourceName.left().value();

			resultOp = updateComponentInstance(containerComponentId, containerComponentType, origComponent, componentInstanceId, componentInstance, inTransaction);
			return resultOp;

		} finally {
			if (needLock)
				unlockComponent(resultOp, containerComponent);
		}
	}

	// New Multiple Instance Update API
	public Either<List<ComponentInstance>, ResponseFormat> updateComponentInstance(String containerComponentParam, String containerComponentId, String userId, List<ComponentInstance> componentInstanceList, boolean needLock,
			boolean createNewTransaction) {

		Either<List<ComponentInstance>, ResponseFormat> resultOp = null;
		org.openecomp.sdc.be.model.Component containerComponent = null;
		try {
			Either<User, ResponseFormat> resp = validateUserExists(userId, "update Component Instance", true);
			if (resp.isRight()) {
				return Either.right(resp.right().value());
			}

			Either<ComponentTypeEnum, ResponseFormat> validateComponentType = validateComponentType(containerComponentParam);
			if (validateComponentType.isRight()) {
				return Either.right(validateComponentType.right().value());
			}

			final ComponentTypeEnum containerComponentType = validateComponentType.left().value();

			ComponentParametersView componentFilter = new ComponentParametersView();
			componentFilter.disableAll();
			componentFilter.setIgnoreUsers(false);
			Either<org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponentExists = validateComponentExistsByFilter(containerComponentId, containerComponentType, componentFilter, true);
			if (validateComponentExists.isRight()) {
				return Either.right(validateComponentExists.right().value());
			}

			containerComponent = validateComponentExists.left().value();

			Either<Boolean, ResponseFormat> validateCanWorkOnComponent = validateCanWorkOnComponent(containerComponent, userId);
			if (validateCanWorkOnComponent.isRight()) {
				return Either.right(validateCanWorkOnComponent.right().value());
			}

			ComponentTypeEnum instanceType = getComponentType(containerComponentType);

			for (ComponentInstance componentInstance : componentInstanceList) {
				Either<Boolean, StorageOperationStatus> validateParentStatus = componentInstanceOperation.validateParent(containerComponentId, componentInstance.getUniqueId(), true);
				if (validateParentStatus.isRight()) {
					log.debug("Failed to get component instance {} on service {}", componentInstance.getUniqueId(), containerComponentId);
					resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND, componentInstance.getName(), instanceType.getValue().toLowerCase()));
					return resultOp;
				}
				Boolean isPrentValid = validateParentStatus.left().value();
				if (!isPrentValid) {
					resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER, componentInstance.getName(), instanceType.getValue().toLowerCase(), containerComponentType.getValue().toLowerCase(),
							containerComponentId));
					return resultOp;
				}
			}

			if (needLock) {

				Either<Boolean, ResponseFormat> lockComponent = lockComponent(containerComponent, "updateComponentInstance");
				if (lockComponent.isRight()) {
					return Either.right(lockComponent.right().value());
				}
			}

			List<ComponentInstance> updatedList = new ArrayList<>();
			for (ComponentInstance componentInstance : componentInstanceList) {

				Either<Component, ResponseFormat> eitherResourceName = getOriginComponentNameFromComponentInstance(componentInstance, true);

				if (eitherResourceName.isRight()) {
					resultOp = Either.right(eitherResourceName.right().value());
					return resultOp;
				}
				Component origComponent = eitherResourceName.left().value();

				Either<ComponentInstance, ResponseFormat> resultSingleUpdate = updateComponentInstance(containerComponentId, containerComponentType, origComponent, componentInstance.getUniqueId(), componentInstance, true);

				if (resultSingleUpdate.isRight()) {
					resultOp = Either.right(resultSingleUpdate.right().value());
					return resultOp;
				}
				updatedList.add(resultSingleUpdate.left().value());
			}

			resultOp = Either.left(updatedList);
			return resultOp;

		} finally {
			if (needLock) {
				unlockComponent(resultOp, containerComponent);
			}
		}
	}

	private ComponentTypeEnum getComponentType(ComponentTypeEnum containerComponentType) {
		if (ComponentTypeEnum.PRODUCT.equals(containerComponentType)) {
			return ComponentTypeEnum.SERVICE_INSTANCE;
		} else {
			return ComponentTypeEnum.RESOURCE_INSTANCE;
		}
	}

	public Either<ComponentInstance, ResponseFormat> updateComponentInstance(String containerComponentParam, org.openecomp.sdc.be.model.Component containerComponent, org.openecomp.sdc.be.model.Component origComponent, String componentInstanceId,
			ComponentInstance componentInstance, boolean needLock, boolean inTransaction) {
		Either<ComponentInstance, ResponseFormat> resultOp = null;
		Either<ComponentTypeEnum, ResponseFormat> validateComponentType = validateComponentType(containerComponentParam);
		if (validateComponentType.isRight()) {
			return Either.right(validateComponentType.right().value());
		}

		final ComponentTypeEnum containerComponentType = validateComponentType.left().value();
		if (needLock) {

			Either<Boolean, ResponseFormat> lockComponent = lockComponent(containerComponent, "updateComponentInstance");
			if (lockComponent.isRight()) {
				return Either.right(lockComponent.right().value());
			}
		}

		try {

			resultOp = updateComponentInstance(containerComponent.getUniqueId(), containerComponentType, origComponent, componentInstanceId, componentInstance, inTransaction);
			return resultOp;

		} finally {
			if (needLock)
				unlockComponent(resultOp, containerComponent);
		}

	}

	private Either<ComponentInstance, ResponseFormat> updateComponentInstance(String containerComponentId, ComponentTypeEnum containerComponentType, org.openecomp.sdc.be.model.Component origComponent, String componentInstanceId,
			ComponentInstance componentInstance, boolean inTransaction) {
		Either<ComponentInstance, ResponseFormat> resultOp;

		Either<String, ResponseFormat> eitherNameLogic = handleNameLogic(origComponent, componentInstance, containerComponentType, containerComponentId, false, inTransaction);
		if (eitherNameLogic.isRight()) {
			return Either.right(eitherNameLogic.right().value());
		}
		NodeTypeEnum containerNodeType = containerComponentType.getNodeType();

		Either<Boolean, StorageOperationStatus> isNameExistStatus = componentInstanceOperation.isComponentInstanceNameExist(containerComponentId, containerNodeType, componentInstanceId, componentInstance.getNormalizedName());
		if (isNameExistStatus.isRight()) {
			log.debug("Failed to get resource instance names for service {}", containerComponentId);

			resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_INSTANCE_RELATION_NOT_FOUND, componentInstance.getName(), containerComponentId));
			return resultOp;
		}
		Boolean isNameExist = isNameExistStatus.left().value();
		if (isNameExist) {
			containerComponentType = getComponentTypeOfComponentInstance();
			resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_NAME_ALREADY_EXIST, containerComponentType.getValue(), componentInstance.getName()));
			return resultOp;

		}

		log.debug("Try to update entry on graph");
		Either<ComponentInstance, StorageOperationStatus> result = componentInstanceOperation.updateResourceInstance(containerComponentId, containerNodeType, componentInstanceId, componentInstance, inTransaction);

		if (result.isLeft()) {
			log.debug("Enty on graph is updated.");
			ComponentInstance resResourceInfo = result.left().value();
			resultOp = Either.left(resResourceInfo);
			return resultOp;

		} else {
			log.debug("Failed to update entry on graph for resource instance {}", componentInstance.getName());
			resultOp = Either.right(componentsUtils.getResponseFormatForResourceInstance(componentsUtils.convertFromStorageResponseForResourceInstance(result.right().value(), false), "", componentInstance.getName()));
			return resultOp;
		}

	}

	public Either<ComponentInstance, ResponseFormat> deleteComponentInstance(String containerComponentParam, String containerComponentId, String resourceInstanceId, String userId) {

		Either<User, ResponseFormat> resp = validateUserExists(userId, "delete Component Instance", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

		Either<ComponentTypeEnum, ResponseFormat> validateComponentType = validateComponentType(containerComponentParam);
		if (validateComponentType.isRight()) {
			return Either.right(validateComponentType.right().value());
		}

		final ComponentTypeEnum containerComponentType = validateComponentType.left().value();
		Either<org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponentExists = validateComponentExists(containerComponentId, containerComponentType, false, true);
		if (validateComponentExists.isRight()) {
			return Either.right(validateComponentExists.right().value());
		}
		org.openecomp.sdc.be.model.Component containerComponent = validateComponentExists.left().value();
		Either<Boolean, ResponseFormat> validateCanWorkOnComponent = validateCanWorkOnComponent(containerComponent, userId);
		if (validateCanWorkOnComponent.isRight()) {
			return Either.right(validateCanWorkOnComponent.right().value());
		}

		Either<Boolean, ResponseFormat> lockComponent = lockComponent(containerComponent, "deleteComponentInstance");
		if (lockComponent.isRight()) {
			return Either.right(lockComponent.right().value());
		}
		// validate resource
		/*
		 * if (!ComponentValidationUtils.canWorkOnComponent(containerComponentId, serviceOperation, userId)) { log.info( "Restricted operation for user {} on service {}", userId, containerComponentId); return Either.right(componentsUtils
		 * .getResponseFormat(ActionStatus.RESTRICTED_OPERATION)); } // lock resource StorageOperationStatus lockStatus = graphLockOperation.lockComponent( containerComponentId, NodeTypeEnum.Service); if (lockStatus != StorageOperationStatus.OK) {
		 * log.debug("Failed to lock service {}", containerComponentId); resultOp = Either.right(componentsUtils .getResponseFormat(componentsUtils .convertFromStorageResponse(lockStatus))); return resultOp; }
		 */
		Either<ComponentInstance, ResponseFormat> resultOp = null;
		try {
			resultOp = deleteComponentInstance(containerComponentId, resourceInstanceId, containerComponentType);
			return resultOp;

		} finally {
			/*
			 * if (resultOp == null || resultOp.isRight()) { titanGenericDao.rollback(); } else { titanGenericDao.commit(); } graphLockOperation.unlockComponent(containerComponentId, NodeTypeEnum.Service);
			 */
			unlockComponent(resultOp, containerComponent);
		}
	}

	private Either<ComponentInstance, ResponseFormat> deleteComponentInstance(String containerComponentId, String resourceInstanceId, ComponentTypeEnum containerComponentType) {
		Either<ComponentInstance, ResponseFormat> resultOp;
		NodeTypeEnum containerNodeType = containerComponentType.getNodeType();
		Either<ComponentInstance, StorageOperationStatus> result = componentInstanceOperation.deleteComponentInstance(containerNodeType, containerComponentId, resourceInstanceId, true);

		if (result.isRight()) {
			log.debug("Failed to delete entry on graph for resourceInstance {}", resourceInstanceId);
			ActionStatus status = componentsUtils.convertFromStorageResponse(result.right().value(), containerComponentType);
			// TODO check
			/*
			 * if (ActionStatus.SERVICE_NOT_FOUND.equals(status)) { resultOp = Either .right(componentsUtils .getResponseFormat(ActionStatus.RESOURCE_INSTANCE_NOT_FOUND)); } else {
			 */
			resultOp = Either.right(componentsUtils.getResponseFormat(status, resourceInstanceId));
			// }
			return resultOp;
		}
		ComponentInstance resResourceInfo = result.left().value();
		resultOp = Either.left(resResourceInfo);

		log.debug("Entry on graph is deleted. Exist more connections on this artifact.");

		Map<String, ArtifactDefinition> deploymentArtifacts = resResourceInfo.getDeploymentArtifacts();
		if (deploymentArtifacts != null && !deploymentArtifacts.isEmpty()) {
			StorageOperationStatus deleteArtifactsIfNotOnGraph = artifactBusinessLogic.deleteAllComponentArtifactsIfNotOnGraph(new ArrayList<ArtifactDefinition>(deploymentArtifacts.values()));
			if (!deleteArtifactsIfNotOnGraph.equals(StorageOperationStatus.OK)) {
				log.debug("failed to delete artifact payload. status={}", deleteArtifactsIfNotOnGraph.name());
				resultOp = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(result.right().value()), resourceInstanceId));
			}

		}
		return resultOp;
	}

	public Either<RequirementCapabilityRelDef, ResponseFormat> associateRIToRI(String componentId, String userId, RequirementCapabilityRelDef requirementDef, ComponentTypeEnum componentTypeEnum) {
		return associateRIToRI(componentId, userId, requirementDef, componentTypeEnum, false, true, true);
	}

	public Either<RequirementCapabilityRelDef, ResponseFormat> associateRIToRI(String componentId, String userId, RequirementCapabilityRelDef requirementDef, ComponentTypeEnum componentTypeEnum, boolean inTransaction, boolean needLock,
			boolean createNewTransaction) {

		Either<User, ResponseFormat> resp = validateUserExists(userId, "associate Ri To RI", inTransaction);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

		Either<RequirementCapabilityRelDef, ResponseFormat> resultOp = null;

		Either<org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponentExists = validateComponentExists(componentId, componentTypeEnum, inTransaction, createNewTransaction);
		if (validateComponentExists.isRight()) {
			return Either.right(validateComponentExists.right().value());
		}
		org.openecomp.sdc.be.model.Component containerComponent = validateComponentExists.left().value();

		Either<Boolean, ResponseFormat> validateCanWorkOnComponent = validateCanWorkOnComponent(containerComponent, userId);
		if (validateCanWorkOnComponent.isRight()) {
			return Either.right(validateCanWorkOnComponent.right().value());
		}
		if (needLock) {
			Either<Boolean, ResponseFormat> lockComponent = lockComponent(containerComponent, "associateRIToRI");

			if (lockComponent.isRight()) {
				return Either.right(lockComponent.right().value());
			}
		}

		try {

			resultOp = associateRIToRIOnGraph(componentId, requirementDef, componentTypeEnum, inTransaction);

			return resultOp;

		} finally {
			if (needLock)
				unlockComponent(resultOp, containerComponent);
		}
	}

	public Either<RequirementCapabilityRelDef, ResponseFormat> associateRIToRIOnGraph(String componentId, RequirementCapabilityRelDef requirementDef, ComponentTypeEnum componentTypeEnum, boolean inTransaction) {

		log.debug("Try to create entry on graph");
		Either<RequirementCapabilityRelDef, ResponseFormat> resultOp = null;

		Either<RequirementCapabilityRelDef, StorageOperationStatus> result = componentInstanceOperation.associateResourceInstances(componentId, componentTypeEnum.getNodeType(), requirementDef, inTransaction);

		if (result.isLeft()) {
			log.debug("Enty on graph is created.");
			RequirementCapabilityRelDef requirementCapabilityRelDef = result.left().value();
			resultOp = Either.left(requirementCapabilityRelDef);
			return resultOp;

		} else {
			log.debug("Failed to associate node {} with node {}", requirementDef.getFromNode(), requirementDef.getToNode());
			String fromNameOrId = "";
			String toNameOrId = "";
			Either<ComponentInstance, StorageOperationStatus> fromResult = componentInstanceOperation.getResourceInstanceById(requirementDef.getFromNode());
			Either<ComponentInstance, StorageOperationStatus> toResult = componentInstanceOperation.getResourceInstanceById(requirementDef.getToNode());

			toNameOrId = requirementDef.getFromNode();
			fromNameOrId = requirementDef.getFromNode();
			if (fromResult.isLeft()) {
				fromNameOrId = fromResult.left().value().getName();
			}
			if (toResult.isLeft()) {
				toNameOrId = toResult.left().value().getName();
			}

			resultOp = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponseForResourceInstance(result.right().value(), true), fromNameOrId, toNameOrId, requirementDef.getRelationships().get(0).getRequirement()));

			return resultOp;
		}

	}

	public Either<RequirementCapabilityRelDef, ResponseFormat> dissociateRIFromRI(String componentId, String userId, RequirementCapabilityRelDef requirementDef, ComponentTypeEnum componentTypeEnum) {
		Either<User, ResponseFormat> resp = validateUserExists(userId, "dissociate RI From RI", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

		Either<RequirementCapabilityRelDef, ResponseFormat> resultOp = null;
		Either<org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponentExists = validateComponentExists(componentId, componentTypeEnum, false, true);
		if (validateComponentExists.isRight()) {
			return Either.right(validateComponentExists.right().value());
		}
		org.openecomp.sdc.be.model.Component containerComponent = validateComponentExists.left().value();

		Either<Boolean, ResponseFormat> validateCanWorkOnComponent = validateCanWorkOnComponent(containerComponent, userId);
		if (validateCanWorkOnComponent.isRight()) {
			return Either.right(validateCanWorkOnComponent.right().value());
		}
		Either<Boolean, ResponseFormat> lockComponent = lockComponent(containerComponent, "associateRIToRI");

		if (lockComponent.isRight()) {
			return Either.right(lockComponent.right().value());
		}
		try {
			log.debug("Try to create entry on graph");
			Either<RequirementCapabilityRelDef, StorageOperationStatus> result = componentInstanceOperation.dissociateResourceInstances(componentId, componentTypeEnum.getNodeType(), requirementDef, true);
			if (result.isLeft()) {
				log.debug("Enty on graph is created.");
				RequirementCapabilityRelDef requirementCapabilityRelDef = result.left().value();
				resultOp = Either.left(requirementCapabilityRelDef);
				return resultOp;

			} else {

				log.debug("Failed to dissocaite node {} from node {}", requirementDef.getFromNode(), requirementDef.getToNode());
				String fromNameOrId = "";
				String toNameOrId = "";
				Either<ComponentInstance, StorageOperationStatus> fromResult = componentInstanceOperation.getResourceInstanceById(requirementDef.getFromNode());
				Either<ComponentInstance, StorageOperationStatus> toResult = componentInstanceOperation.getResourceInstanceById(requirementDef.getToNode());

				toNameOrId = requirementDef.getFromNode();
				fromNameOrId = requirementDef.getFromNode();
				if (fromResult.isLeft()) {
					fromNameOrId = fromResult.left().value().getName();
				}
				if (toResult.isLeft()) {
					toNameOrId = toResult.left().value().getName();
				}

				resultOp = Either
						.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponseForResourceInstance(result.right().value(), true), fromNameOrId, toNameOrId, requirementDef.getRelationships().get(0).getRequirement()));
				return resultOp;
			}
		} finally {
			unlockComponent(resultOp, containerComponent);
		}
	}

	private Either<ComponentInstanceAttribute, ResponseFormat> updateAttributeValue(ComponentInstanceAttribute attribute, String resourceInstanceId) {
		Either<ComponentInstanceAttribute, StorageOperationStatus> eitherAttribute = componentInstanceOperation.updateAttributeValueInResourceInstance(attribute, resourceInstanceId, true);
		Either<ComponentInstanceAttribute, ResponseFormat> result;
		if (eitherAttribute.isLeft()) {
			log.debug("Attribute value {} was updated on graph.", attribute.getValueUniqueUid());
			ComponentInstanceAttribute instanceAttribute = eitherAttribute.left().value();

			result = Either.left(instanceAttribute);

		} else {
			log.debug("Failed to update attribute value {} in resource instance {}", attribute, resourceInstanceId);

			ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(eitherAttribute.right().value());

			result = Either.right(componentsUtils.getResponseFormat(actionStatus, ""));

		}
		return result;
	}

	private Either<ComponentInstanceInput, ResponseFormat> updateInputValue(ComponentInstanceInput input, String resourceInstanceId) {
		Either<ComponentInstanceInput, StorageOperationStatus> eitherInput = componentInstanceOperation.updateInputValueInResourceInstance(input, resourceInstanceId, true);
		Either<ComponentInstanceInput, ResponseFormat> result;
		if (eitherInput.isLeft()) {
			log.debug("Input value {} was updated on graph.", input.getValueUniqueUid());
			ComponentInstanceInput instanceInput = eitherInput.left().value();

			result = Either.left(instanceInput);

		} else {
			log.debug("Failed to update input value {} in resource instance {}", input, resourceInstanceId);

			ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(eitherInput.right().value());

			result = Either.right(componentsUtils.getResponseFormat(actionStatus, ""));

		}
		return result;
	}

	private Either<ComponentInstanceAttribute, ResponseFormat> createAttributeValue(ComponentInstanceAttribute attribute, String resourceInstanceId) {

		Either<ComponentInstanceAttribute, ResponseFormat> result;

		Wrapper<Integer> indexCounterWrapper = new Wrapper<>();
		Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
		validateIncrementCounter(resourceInstanceId, GraphPropertiesDictionary.ATTRIBUTE_COUNTER, indexCounterWrapper, errorWrapper);

		if (!errorWrapper.isEmpty()) {
			result = Either.right(errorWrapper.getInnerElement());
		} else {
			Either<ComponentInstanceAttribute, StorageOperationStatus> eitherAttribute = componentInstanceOperation.addAttributeValueToResourceInstance(attribute, resourceInstanceId, indexCounterWrapper.getInnerElement(), true);
			if (eitherAttribute.isLeft()) {
				log.debug("Attribute value was added to resource instance {}", resourceInstanceId);
				ComponentInstanceAttribute instanceAttribute = eitherAttribute.left().value();
				result = Either.left(instanceAttribute);

			} else {
				log.debug("Failed to add attribute value {}  to resource instance {}", attribute, resourceInstanceId);

				ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(eitherAttribute.right().value());
				result = Either.right(componentsUtils.getResponseFormatForResourceInstanceProperty(actionStatus, ""));

			}
		}
		return result;
	}

	/**
	 * Create Or Updates Attribute Instance
	 * 
	 * @param componentTypeEnum
	 * @param componentId
	 * @param resourceInstanceId
	 * @param attribute
	 * @param userId
	 * @return
	 */
	public Either<ComponentInstanceAttribute, ResponseFormat> createOrUpdateAttributeValue(ComponentTypeEnum componentTypeEnum, String componentId, String resourceInstanceId, ComponentInstanceAttribute attribute, String userId) {
		Either<ComponentInstanceAttribute, ResponseFormat> result = null;
		Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();

		validateUserExist(userId, "create Or Update Attribute Value", errorWrapper);
		if (errorWrapper.isEmpty()) {
			validateComponentTypeEnum(componentTypeEnum, "CreateOrUpdateAttributeValue", errorWrapper);
		}
		if (errorWrapper.isEmpty()) {
			validateCanWorkOnComponent(componentId, componentTypeEnum, userId, errorWrapper);
		}
		if (errorWrapper.isEmpty()) {
			validateComponentLock(componentId, componentTypeEnum, errorWrapper);
		}

		try {
			if (errorWrapper.isEmpty()) {
				final boolean isCreate = Objects.isNull(attribute.getValueUniqueUid());
				if (isCreate) {
					result = createAttributeValue(attribute, resourceInstanceId);
				} else {
					result = updateAttributeValue(attribute, resourceInstanceId);
				}
			} else {
				result = Either.right(errorWrapper.getInnerElement());
			}
			return result;
		}

		finally {
			if (result == null || result.isRight()) {
				titanGenericDao.rollback();
			} else {
				titanGenericDao.commit();
			}
			// unlock resource
			graphLockOperation.unlockComponent(componentId, componentTypeEnum.getNodeType());
		}
	}

	public Either<ComponentInstanceProperty, ResponseFormat> createOrUpdatePropertyValue(ComponentTypeEnum componentTypeEnum, String componentId, String resourceInstanceId, ComponentInstanceProperty property, String userId) {

		Either<ComponentInstanceProperty, ResponseFormat> resultOp = null;

		Either<User, ResponseFormat> resp = validateUserExists(userId, "create Or Update Property Value", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

		if (componentTypeEnum == null) {
			BeEcompErrorManager.getInstance().logInvalidInputError("CreateOrUpdatePropertyValue", "invalid component type", ErrorSeverity.INFO);
			resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.NOT_ALLOWED));
			return resultOp;
		}

		IComponentOperation componentOperation = getIComponentOperation(componentTypeEnum);

		if (!ComponentValidationUtils.canWorkOnComponent(componentId, componentOperation, userId)) {
			log.info("Restricted operation for user {} on service {}", userId, componentId);
			resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
			return resultOp;
		}
		// lock resource
		StorageOperationStatus lockStatus = graphLockOperation.lockComponent(componentId, componentTypeEnum.getNodeType());
		if (lockStatus != StorageOperationStatus.OK) {
			log.debug("Failed to lock service {}", componentId);
			resultOp = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(lockStatus)));
			return resultOp;
		}
		try {
			String propertyValueUid = property.getValueUniqueUid();
			if (propertyValueUid == null) {

				Either<Integer, StorageOperationStatus> counterRes = componentInstanceOperation.increaseAndGetResourceInstanceSpecificCounter(resourceInstanceId, GraphPropertiesDictionary.PROPERTY_COUNTER, true);

				if (counterRes.isRight()) {
					log.debug("increaseAndGetResourcePropertyCounter failed resource instance {} property {}", resourceInstanceId, property);
					StorageOperationStatus status = counterRes.right().value();
					ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(status);
					resultOp = Either.right(componentsUtils.getResponseFormat(actionStatus));
				}
				Integer index = counterRes.left().value();
				Either<ComponentInstanceProperty, StorageOperationStatus> result = componentInstanceOperation.addPropertyValueToResourceInstance(property, resourceInstanceId, index, true);

				if (result.isLeft()) {
					log.debug("Property value was added to resource instance {}", resourceInstanceId);
					ComponentInstanceProperty instanceProperty = result.left().value();

					resultOp = Either.left(instanceProperty);
					return resultOp;

				} else {
					log.debug("Failed to add property value {} to resource instance {}", property, resourceInstanceId);
					// TODO: esofer add error

					ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(result.right().value());

					resultOp = Either.right(componentsUtils.getResponseFormatForResourceInstanceProperty(actionStatus, ""));

					return resultOp;
				}

			} else {
				Either<ComponentInstanceProperty, StorageOperationStatus> result = componentInstanceOperation.updatePropertyValueInResourceInstance(property, resourceInstanceId, true);

				if (result.isLeft()) {
					log.debug("Property value {} was updated on graph.", property.getValueUniqueUid());
					ComponentInstanceProperty instanceProperty = result.left().value();

					resultOp = Either.left(instanceProperty);
					return resultOp;

				} else {
					log.debug("Failed to update property value {} in resource instance {}", property, resourceInstanceId);

					ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(result.right().value());

					resultOp = Either.right(componentsUtils.getResponseFormatForResourceInstanceProperty(actionStatus, ""));

					return resultOp;
				}
			}

		} finally {
			if (resultOp == null || resultOp.isRight()) {
				titanGenericDao.rollback();
			} else {
				titanGenericDao.commit();
			}
			// unlock resource
			graphLockOperation.unlockComponent(componentId, componentTypeEnum.getNodeType());
		}

	}

	public Either<ComponentInstanceInput, ResponseFormat> createOrUpdateInputValue(ComponentTypeEnum componentTypeEnum, String componentId, String resourceInstanceId, ComponentInstanceInput inputProperty, String userId) {

		Either<ComponentInstanceInput, ResponseFormat> resultOp = null;

		Either<User, ResponseFormat> resp = validateUserExists(userId, "create Or Update Input Value", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

		if (componentTypeEnum == null) {
			BeEcompErrorManager.getInstance().logInvalidInputError("createOrUpdateInputValue", "invalid component type", ErrorSeverity.INFO);
			resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.NOT_ALLOWED));
			return resultOp;
		}

		IComponentOperation componentOperation = getIComponentOperation(componentTypeEnum);

		if (!ComponentValidationUtils.canWorkOnComponent(componentId, componentOperation, userId)) {
			log.info("Restricted operation for user {} on service {}", userId, componentId);
			resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
			return resultOp;
		}
		// lock resource
		StorageOperationStatus lockStatus = graphLockOperation.lockComponent(componentId, componentTypeEnum.getNodeType());
		if (lockStatus != StorageOperationStatus.OK) {
			log.debug("Failed to lock service {}", componentId);
			resultOp = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(lockStatus)));
			return resultOp;
		}
		try {
			String propertyValueUid = inputProperty.getValueUniqueUid();
			if (propertyValueUid == null) {

				Either<Integer, StorageOperationStatus> counterRes = componentInstanceOperation.increaseAndGetResourceInstanceSpecificCounter(resourceInstanceId, GraphPropertiesDictionary.INPUT_COUNTER, true);

				if (counterRes.isRight()) {
					log.debug("increaseAndGetResourceInputCounter failed resource instance {} inputProperty {}", resourceInstanceId, inputProperty);
					StorageOperationStatus status = counterRes.right().value();
					ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(status);
					resultOp = Either.right(componentsUtils.getResponseFormat(actionStatus));
				}
				Integer index = counterRes.left().value();
				Either<ComponentInstanceInput, StorageOperationStatus> result = componentInstanceOperation.addInputValueToResourceInstance(inputProperty, resourceInstanceId, index, true);

				if (result.isLeft()) {
					log.debug("Property value was added to resource instance {}", resourceInstanceId);
					ComponentInstanceInput instanceProperty = result.left().value();

					resultOp = Either.left(instanceProperty);
					return resultOp;

				} else {
					log.debug("Failed to add input value {} to resource instance {}", inputProperty, resourceInstanceId);
					// TODO: esofer add error

					ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(result.right().value());

					resultOp = Either.right(componentsUtils.getResponseFormatForResourceInstanceProperty(actionStatus, ""));

					return resultOp;
				}

			} else {
				Either<ComponentInstanceInput, StorageOperationStatus> result = componentInstanceOperation.updateInputValueInResourceInstance(inputProperty, resourceInstanceId, true);

				if (result.isLeft()) {
					log.debug("Input value {} was updated on graph.", inputProperty.getValueUniqueUid());
					ComponentInstanceInput instanceProperty = result.left().value();

					resultOp = Either.left(instanceProperty);
					return resultOp;

				} else {
					log.debug("Failed to update property value {} in reosurce instance {}", inputProperty, resourceInstanceId);

					ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(result.right().value());

					resultOp = Either.right(componentsUtils.getResponseFormatForResourceInstanceProperty(actionStatus, ""));

					return resultOp;
				}
			}

		} finally {
			if (resultOp == null || resultOp.isRight()) {
				titanGenericDao.rollback();
			} else {
				titanGenericDao.commit();
			}
			// unlock resource
			graphLockOperation.unlockComponent(componentId, componentTypeEnum.getNodeType());
		}

	}

	public Either<ComponentInstanceProperty, ResponseFormat> deletePropertyValue(ComponentTypeEnum componentTypeEnum, String serviceId, String resourceInstanceId, String propertyValueId, String userId) {

		Either<User, ResponseFormat> resp = validateUserExists(userId, "delete Property Value", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

		Either<ComponentInstanceProperty, ResponseFormat> resultOp = null;

		if (componentTypeEnum == null) {
			BeEcompErrorManager.getInstance().logInvalidInputError("CreateOrUpdatePropertyValue", "invalid component type", ErrorSeverity.INFO);
			resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.NOT_ALLOWED));
			return resultOp;
		}

		IComponentOperation componentOperation = getIComponentOperation(componentTypeEnum);

		if (!ComponentValidationUtils.canWorkOnComponent(serviceId, componentOperation, userId)) {
			log.info("Restricted operation for user {} on service {}", userId, serviceId);
			resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
			return resultOp;
		}
		// lock resource
		StorageOperationStatus lockStatus = graphLockOperation.lockComponent(serviceId, componentTypeEnum.getNodeType());
		if (lockStatus != StorageOperationStatus.OK) {
			log.debug("Failed to lock service {}", serviceId);
			resultOp = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(lockStatus)));
			return resultOp;
		}
		try {
			Either<ComponentInstanceProperty, StorageOperationStatus> result = propertyOperation.removePropertyValueFromResourceInstance(propertyValueId, resourceInstanceId, true);

			if (result.isLeft()) {
				log.debug("Property value {} was removed from graph.", propertyValueId);
				ComponentInstanceProperty instanceProperty = result.left().value();

				resultOp = Either.left(instanceProperty);
				return resultOp;

			} else {
				log.debug("Failed to remove property value {} in resource instance {}", propertyValueId, resourceInstanceId);

				ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(result.right().value());

				resultOp = Either.right(componentsUtils.getResponseFormatForResourceInstanceProperty(actionStatus, ""));

				return resultOp;
			}

		} finally {
			if (resultOp == null || resultOp.isRight()) {
				titanGenericDao.rollback();
			} else {
				titanGenericDao.commit();
			}
			// unlock resource
			graphLockOperation.unlockComponent(serviceId, componentTypeEnum.getNodeType());
		}

	}

	private Either<Boolean, ResponseFormat> validateComponentInstanceName(String resourceInstanceName, ComponentInstance resourceInstance, boolean isCreate) {
		ComponentTypeEnum containerComponentType = getComponentTypeOfComponentInstance();
		if (!isCreate) {
			if (resourceInstanceName == null)
				return Either.left(true);
		}

		if (!ValidationUtils.validateStringNotEmpty(resourceInstanceName)) {
			ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.MISSING_COMPONENT_NAME, containerComponentType.getValue());

			return Either.right(errorResponse);
		}
		resourceInstance.setNormalizedName(ValidationUtils.normaliseComponentInstanceName(resourceInstanceName));
		if (!isCreate) {
			if (!ValidationUtils.validateResourceInstanceNameLength(resourceInstanceName)) {
				ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_NAME_EXCEEDS_LIMIT, containerComponentType.getValue(), "" + ValidationUtils.COMPONENT_NAME_MAX_LENGTH);

				return Either.right(errorResponse);
			}
			if (!ValidationUtils.validateResourceInstanceName(resourceInstanceName)) {
				ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.INVALID_COMPONENT_NAME, containerComponentType.getValue());

				return Either.right(errorResponse);
			}
		}

		return Either.left(true);

	}

	private Either<Boolean, ResponseFormat> validateComponentInstanceParentState(ComponentTypeEnum containerComponentType, ComponentInstance resourceInstance) {
		String componentId = resourceInstance.getComponentUid();
		Either<? extends Component, StorageOperationStatus> eitherResourceResponse = Either.right(StorageOperationStatus.GENERAL_ERROR);

		ComponentTypeEnum componentType = getComponentTypeByParentComponentType(containerComponentType);
		ComponentOperation componentOperation = getComponentOperation(componentType);
		if (componentOperation != null)
			eitherResourceResponse = componentOperation.getComponent(componentId, true);

		Component component = null;
		ResponseFormat errorResponse = null;
		if (eitherResourceResponse.isRight()) {
			ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(eitherResourceResponse.right().value(), componentType);
			errorResponse = componentsUtils.getResponseFormat(actionStatus, Constants.EMPTY_STRING);
			return Either.right(errorResponse);
		}
		component = eitherResourceResponse.left().value();
		LifecycleStateEnum resourceCurrState = component.getLifecycleState();
		if (resourceCurrState == LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT) {
			ActionStatus actionStatus = ActionStatus.ILLEGAL_COMPONENT_STATE;
			errorResponse = componentsUtils.getResponseFormat(actionStatus, component.getComponentType().toString(), component.getName(), resourceCurrState.toString());
			return Either.right(errorResponse);
		}
		return Either.left(true);
	}

	public Either<ComponentInstance, ResponseFormat> changeComponentInstanceVersion(String containerComponentParam, String containerComponentId, String componentInstanceId, String userId, ComponentInstance newComponentInstance) {

		Either<User, ResponseFormat> resp = validateUserExists(userId, "change Component Instance Version", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

		Either<ComponentInstance, ResponseFormat> resultOp = null;

		Either<ComponentTypeEnum, ResponseFormat> validateComponentType = validateComponentType(containerComponentParam);
		if (validateComponentType.isRight()) {
			return Either.right(validateComponentType.right().value());
		}

		final ComponentTypeEnum containerComponentType = validateComponentType.left().value();
		final ComponentOperation containerOperation = getComponentOperation(containerComponentType);

		Either<org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponentExists = validateComponentExists(containerComponentId, containerComponentType, false, true);
		if (validateComponentExists.isRight()) {
			return Either.right(validateComponentExists.right().value());
		}
		org.openecomp.sdc.be.model.Component containerComponent = validateComponentExists.left().value();

		Either<Boolean, ResponseFormat> validateCanWorkOnComponent = validateCanWorkOnComponent(containerComponent, userId);
		if (validateCanWorkOnComponent.isRight()) {
			return Either.right(validateCanWorkOnComponent.right().value());
		}

		Either<Boolean, StorageOperationStatus> validateParentStatus = componentInstanceOperation.validateParent(containerComponentId, componentInstanceId, false);
		if (validateParentStatus.isRight()) {
			log.debug("Failed to get resource instance {} on service {}", componentInstanceId, containerComponentId);
			resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_INSTANCE_NOT_FOUND, componentInstanceId));
			return resultOp;
		}
		Boolean isPrentValid = validateParentStatus.left().value();
		if (!isPrentValid) {
			resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_INSTANCE_NOT_FOUND_ON_SERVICE, componentInstanceId, containerComponentId));
			return resultOp;

		}

		Either<ComponentInstance, StorageOperationStatus> resourceInstanceStatus = componentInstanceOperation.getResourceInstanceById(componentInstanceId);
		if (resourceInstanceStatus.isRight()) {
			log.debug("Failed to get resource instance {} on service {}", componentInstanceId, containerComponentId);
			resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_INSTANCE_NOT_FOUND, componentInstanceId));
			return resultOp;
		}
		ComponentInstance currentResourceInstance = resourceInstanceStatus.left().value();

		Either<Boolean, ResponseFormat> lockComponent = lockComponent(containerComponent, "changeComponentInstanceVersion");
		if (lockComponent.isRight()) {
			return Either.right(lockComponent.right().value());
		}

		try {
			if (currentResourceInstance.getComponentUid().equals(newComponentInstance.getComponentUid())) {
				resultOp = Either.left(currentResourceInstance);
				return resultOp;

			}
			String resourceId = newComponentInstance.getComponentUid();
			if (!getCompInstOriginComponentOperation().isComponentExist(resourceId)) {
				log.debug("resource {} not found.", resourceId);
				resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, ""));
				return resultOp;
			}

			// esofer - before deleting component instance, we should keep the
			// groups which holds this instance
			List<String> groupsToRevert = new ArrayList<>();
			Either<List<String>, StorageOperationStatus> associatedGroups = groupOperation.getAssociatedGroupsToComponentInstance(componentInstanceId, true);
			if (associatedGroups.isRight()) {
				StorageOperationStatus status = associatedGroups.right().value();
				if (status != StorageOperationStatus.OK) {
					BeEcompErrorManager.getInstance().logInternalFlowError("ChangeComponentInstanceVersion", "Failed to getch groups of current component instance", ErrorSeverity.ERROR);
					resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
					return resultOp;
				}
			} else {
				List<String> groups = associatedGroups.left().value();
				groupsToRevert.addAll(groups);
			}
			// rbetzer - before deleting component instance, retrieve env artifacts to keep track of artifactVersion

			Either<Map<String, ArtifactDefinition>, StorageOperationStatus> retrieveEnvArtifacts = componentInstanceOperation.fetchCIEnvArtifacts(componentInstanceId);
			if (retrieveEnvArtifacts.isLeft())
				newComponentInstance.setDeploymentArtifacts(retrieveEnvArtifacts.left().value());
			else if (retrieveEnvArtifacts.right().value() != StorageOperationStatus.OK) {
				log.debug("falied to fetch instance deployment artifacts {}", componentInstanceId );
			}

			resultOp = deleteComponentInstance(containerComponentId, componentInstanceId, containerComponentType);
			if (resultOp.isRight()) {

				log.debug("failed to delete resource instance {}", resourceId);

				return resultOp;

			}

			Either<Component, ResponseFormat> eitherResourceName = getOriginComponentNameFromComponentInstance(newComponentInstance, true);

			if (eitherResourceName.isRight()) {
				resultOp = Either.right(eitherResourceName.right().value());
				return resultOp;
			}

			Component origComponent = eitherResourceName.left().value();

			ComponentInstance resResourceInfo = resultOp.left().value();
			newComponentInstance.setName(resResourceInfo.getName());
			newComponentInstance.setPosX(resResourceInfo.getPosX());
			newComponentInstance.setPosY(resResourceInfo.getPosY());
			newComponentInstance.setDescription(resResourceInfo.getDescription());

			resultOp = createComponentInstanceOnGraph(containerComponent, origComponent, newComponentInstance, userId, containerOperation, true);

			if (resultOp.isRight()) {

				log.debug("failed to create resource instance {}", resourceId);

				return resultOp;

			}

			newComponentInstance = resultOp.left().value();
			newComponentInstance.setName(resResourceInfo.getName());
			resultOp = updateComponentInstance(containerComponentId, containerComponentType, origComponent, newComponentInstance.getUniqueId(), newComponentInstance, true);

			ComponentInstance updatedComponentInstance = resultOp.left().value();
			if (resultOp.isRight()) {
				log.debug("failed to create resource instance {}", resourceId);
				return resultOp;
			}

			if (false == groupsToRevert.isEmpty()) {
				StorageOperationStatus associatedGroupsToComponentInstance = groupOperation.associateGroupsToComponentInstance(groupsToRevert, updatedComponentInstance.getUniqueId(), updatedComponentInstance.getName(), true);
				if (associatedGroupsToComponentInstance != StorageOperationStatus.OK) {
					BeEcompErrorManager.getInstance().logInternalFlowError("ChangeComponentInstanceVersion", "Failed to associate groups to new component instance", ErrorSeverity.ERROR);
					resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
					return resultOp;
				}
			}

			Either<ComponentInstance, StorageOperationStatus> fullResourceInstance = componentInstanceOperation.getFullComponentInstance(resultOp.left().value(), getNodeTypeOfComponentInstanceOrigin());
			if (fullResourceInstance.isRight()) {
				resultOp = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(fullResourceInstance.right().value()), resourceId));
				return resultOp;
			}
			resultOp = Either.left(fullResourceInstance.left().value());
			return resultOp;

		} finally {
			unlockComponent(resultOp, containerComponent);
		}
	}

	protected abstract Either<Boolean, ResponseFormat> validateAllowedToContainCompInstances(org.openecomp.sdc.be.model.Component containerComponent);

	protected abstract NodeTypeEnum getNodeTypeOfComponentInstanceOrigin();

	protected abstract ComponentTypeEnum getComponentTypeOfComponentInstance();

	protected abstract ComponentOperation getContainerComponentOperation();

	protected abstract ComponentOperation getCompInstOriginComponentOperation();

	protected void validateIncrementCounter(String resourceInstanceId, GraphPropertiesDictionary counterType, Wrapper<Integer> instaceCounterWrapper, Wrapper<ResponseFormat> errorWrapper) {
		Either<Integer, StorageOperationStatus> counterRes = componentInstanceOperation.increaseAndGetResourceInstanceSpecificCounter(resourceInstanceId, counterType, true);

		if (counterRes.isRight()) {
			log.debug("increase And Get {} failed resource instance {}", counterType.name(), resourceInstanceId);
			StorageOperationStatus status = counterRes.right().value();
			ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(status);
			errorWrapper.setInnerElement(componentsUtils.getResponseFormat(actionStatus));
		} else {
			instaceCounterWrapper.setInnerElement(counterRes.left().value());
		}

	}

}
