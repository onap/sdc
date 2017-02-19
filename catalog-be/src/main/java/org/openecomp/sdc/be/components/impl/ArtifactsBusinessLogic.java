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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import org.openecomp.sdc.be.components.impl.ImportUtils.ToscaTagNamesEnum;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction.LifecycleChanceActionEnum;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.Configuration.DeploymentArtifactTypeConfig;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.validation.DeploymentArtifactHeatConfiguration;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ArtifactType;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.heat.HeatParameterType;
import org.openecomp.sdc.be.model.operations.api.IArtifactOperation;
import org.openecomp.sdc.be.model.operations.api.IComponentInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IHeatParametersOperation;
import org.openecomp.sdc.be.model.operations.api.IInterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.IResourceOperation;
import org.openecomp.sdc.be.model.operations.api.IUserAdminOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ComponentOperation;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.model.operations.utils.ComponentValidationUtils;
import org.openecomp.sdc.be.resources.data.ArtifactData;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;
import org.openecomp.sdc.be.servlets.RepresentationUtils;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.be.tosca.ToscaError;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;
import org.openecomp.sdc.be.tosca.ToscaRepresentation;
import org.openecomp.sdc.be.user.IUserBusinessLogic;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.common.util.YamlToObjectConverter;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.org.apache.xerces.internal.parsers.SAXParser;

import fj.data.Either;

@org.springframework.stereotype.Component("artifactBusinessLogic")
public class ArtifactsBusinessLogic extends BaseBusinessLogic {
	private static final String ARTIFACT_TYPE_OTHER = "OTHER";
	private static final String ARTIFACT_DESCRIPTION = "artifact description";
	private static final String ARTIFACT_LABEL = "artifact label";
	private static final String ARTIFACT_URL = "artifact url";
	private static final String ARTIFACT_NAME = "artifact name";
	private static final String ARTIFACT_PAYLOAD = "artifact payload";

	private static final String ARTIFACT_PLACEHOLDER_TYPE = "type";
	private static final String ARTIFACT_PLACEHOLDER_DISPLAY_NAME = "displayName";
	private static final Object ARTIFACT_PLACEHOLDER_DESCRIPTION = "description";

	private static Integer defaultHeatTimeout;
	private static final Integer NON_HEAT_TIMEOUT = 0;
	private static Logger log = LoggerFactory.getLogger(ArtifactsBusinessLogic.class.getName());
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@javax.annotation.Resource
	private IArtifactOperation artifactOperation;

	// @javax.annotation.Resource
	// private IResourceUploader daoUploader;

	@javax.annotation.Resource
	private IInterfaceLifecycleOperation interfaceLifecycleOperation;
	@javax.annotation.Resource
	private IUserAdminOperation userOperaton;

	// @javax.annotation.Resource
	// private ESCatalogDAO esCatalogDao;

	@javax.annotation.Resource
	private IElementOperation elementOperation;

	@javax.annotation.Resource
	private ResourceBusinessLogic resourceBusinessLogic;

	@javax.annotation.Resource
	private ServiceBusinessLogic serviceBusinessLogic;

	@javax.annotation.Resource
	private UserBusinessLogic userAdminManager;

	@javax.annotation.Resource
	private IHeatParametersOperation heatParametersOperation;

	@javax.annotation.Resource
	private IComponentInstanceOperation resourceInstanceOperation;

	@Autowired
	private ArtifactCassandraDao artifactCassandraDao;

	@Autowired
	private ToscaExportHandler toscaExportUtils;

	@Autowired
	private CsarUtils csarUtils;

	@Autowired
	private LifecycleBusinessLogic lifecycleBusinessLogic;

	@Autowired
	private IUserBusinessLogic userBusinessLogic;

	public ArtifactsBusinessLogic() {
		defaultHeatTimeout = ConfigurationManager.getConfigurationManager().getConfiguration().getDefaultHeatArtifactTimeoutMinutes();
		if ((defaultHeatTimeout == null) || (defaultHeatTimeout < 1)) {
			defaultHeatTimeout = 60;
		}
	}

	public static enum ArtifactOperation {

		Create(false), Update(false), Delete(false), Download(false);

		private boolean isExternalApi;

		ArtifactOperation(boolean isExternalApi) {
			this.isExternalApi = isExternalApi;
		}

		public boolean isExternalApi() {
			return isExternalApi;
		}

		public void setExternalApi(boolean isExternalApi) {
			this.isExternalApi = isExternalApi;
		}
	}

	// new flow US556184
	public Either<Either<ArtifactDefinition, Operation>, ResponseFormat> handleArtifactRequest(String componentId, String userId, ComponentTypeEnum componentType, ArtifactOperation operation, String artifactId, ArtifactDefinition artifactInfo,
			String origMd5, String originData, String interfaceName, String operationName, String parentId, String containerComponentType) {
		return handleArtifactRequest(componentId, userId, componentType, operation, artifactId, artifactInfo, origMd5, originData, interfaceName, operationName, parentId, containerComponentType, true, false);
	}

	public Either<Either<ArtifactDefinition, Operation>, ResponseFormat> handleArtifactRequest(String componentId, String userId, ComponentTypeEnum componentType, ArtifactOperation operation, String artifactId, ArtifactDefinition artifactInfo,
			String origMd5, String originData, String interfaceName, String operationName, String parentId, String containerComponentType, boolean shouldLock, boolean inTransaction) {
		// step 1
		// detect auditing type
		AuditingActionEnum auditingAction = detectAuditingType(operation, origMd5);
		Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
		// step 2
		// check header
		if (userId == null) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_INFORMATION);
			log.debug("handleArtifactRequest - no HTTP_CSP_HEADER , component id {}", componentId);
			handleAuditing(auditingAction, null, componentId, null, null, null, artifactId, responseFormat, componentType, null);
			return Either.right(responseFormat);
		}
		// step 3
		// check user existence
		// step 4
		// check user's role
		Either<User, ResponseFormat> userResult = validateUserExists(userId, auditingAction, componentId, artifactId, componentType, inTransaction);
		if (userResult.isRight()) {
			return Either.right(userResult.right().value());
		}

		User user = userResult.left().value();
		Either<Boolean, ResponseFormat> validateUserRole = validateUserRole(user, auditingAction, componentId, artifactId, componentType, operation);
		if (validateUserRole.isRight()) {
			return Either.right(validateUserRole.right().value());
		}

		// steps 5 - 6 - 7
		// 5. check service/resource existence
		// 6. check service/resource check out
		// 7. user is owner of checkout state
		org.openecomp.sdc.be.model.Component component = null;
		// ComponentInstance resourceInstance = null;
		String realComponentId = componentType == ComponentTypeEnum.RESOURCE_INSTANCE ? parentId : componentId;
		Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponent = validateComponentExists(realComponentId, userId, auditingAction, user, artifactId, componentType, containerComponentType, inTransaction);
		if (validateComponent.isRight()) {
			return Either.right(validateComponent.right().value());
		}
		component = validateComponent.left().value();
		Either<Boolean, ResponseFormat> validateWorkOnResource = validateWorkOnComponent(component, userId, auditingAction, user, artifactId, operation, componentType);
		if (validateWorkOnResource.isRight()) {
			return Either.right(validateWorkOnResource.right().value());
		}
		// step 8
		Either<Either<ArtifactDefinition, Operation>, ResponseFormat> result = validateAndHandleArtifact(componentId, componentType, operation, artifactId, artifactInfo, origMd5, originData, interfaceName, operationName, parentId, user, component,
				shouldLock, inTransaction);

		return result;
	}

	/**
	 * This Method validates only the Artifact and does not validate user / role / component ect...<br>
	 * For regular usage use <br>
	 * {@link #handleArtifactRequest(String, String, ComponentTypeEnum, ArtifactOperation, String, ArtifactDefinition, String, String, String, String, String, String)}
	 * 
	 * @return
	 */
	public Either<Either<ArtifactDefinition, Operation>, ResponseFormat> validateAndHandleArtifact(String componentUniqueId, ComponentTypeEnum componentType, ArtifactOperation operation, String artifactUniqueId, ArtifactDefinition artifactDefinition,
			String origMd5, String originData, String interfaceName, String operationName, String parentId, User user, Component component, boolean shouldLock, boolean inTransaction) {
		Component parent = component;
		Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();

		AuditingActionEnum auditingAction = detectAuditingType(operation, origMd5);
		artifactDefinition = validateArtifact(componentUniqueId, componentType, operation, artifactUniqueId, artifactDefinition, parentId, auditingAction, user, component, parent, shouldLock, errorWrapper);

		Either<Either<ArtifactDefinition, Operation>, ResponseFormat> result;
		if (errorWrapper.isEmpty()) {
			// step 10
			result = doAction(componentUniqueId, componentType, operation, artifactUniqueId, artifactDefinition, origMd5, originData, interfaceName, operationName, auditingAction, user, parent, shouldLock, inTransaction);
		} else {
			result = Either.right(errorWrapper.getInnerElement());
		}
		return result;
	}

	private ArtifactDefinition validateArtifact(String componentId, ComponentTypeEnum componentType, ArtifactOperation operation, String artifactId, ArtifactDefinition artifactInfo, String parentId, AuditingActionEnum auditingAction, User user,
			org.openecomp.sdc.be.model.Component component, org.openecomp.sdc.be.model.Component parent, boolean shouldLock, Wrapper<ResponseFormat> errorWrapper) {
		if (operation == ArtifactOperation.Update || operation == ArtifactOperation.Delete || operation == ArtifactOperation.Download) {
			Either<ArtifactDefinition, ResponseFormat> validateArtifact = validateArtifact(componentId, componentType, artifactId, component, auditingAction, parentId);
			if (validateArtifact.isRight()) {
				ResponseFormat responseFormat = validateArtifact.right().value();
				handleAuditing(auditingAction, parent, componentId, user, null, null, artifactId, responseFormat, componentType, null);
				errorWrapper.setInnerElement(validateArtifact.right().value());
			} else if (operation == ArtifactOperation.Download) {
				artifactInfo = validateArtifact.left().value();
				handleHeatEnvDownload(componentId, user, component, validateArtifact, shouldLock, errorWrapper);
			}
		}
		return artifactInfo;
	}

	private void handleHeatEnvDownload(String componentId, User user, org.openecomp.sdc.be.model.Component component, Either<ArtifactDefinition, ResponseFormat> validateArtifact, boolean shouldLock, Wrapper<ResponseFormat> errorWrapper) {
		ArtifactDefinition validatedArtifact = validateArtifact.left().value();

		if (validatedArtifact.getArtifactType().equalsIgnoreCase(ArtifactTypeEnum.HEAT_ENV.getType())) {
			ComponentInstance componentInstance = component.getComponentInstances().stream().filter(p -> p.getUniqueId().equals(componentId)).findAny().get();
			ArtifactDefinition heatEnvWithHeatParams = componentInstance.getDeploymentArtifacts().values().stream().filter(p -> p.getUniqueId().equals(validatedArtifact.getUniqueId())).findAny().get();
			Either<ArtifactDefinition, ResponseFormat> eitherGenerated = generateHeatEnvArtifact(heatEnvWithHeatParams, component, componentInstance.getName(), user, shouldLock);
			if (eitherGenerated.isRight()) {
				errorWrapper.setInnerElement(eitherGenerated.right().value());
			}
		}
	}

	private boolean artifactGenerationRequired(org.openecomp.sdc.be.model.Component component, ArtifactDefinition artifactInfo) {
		return artifactInfo.getArtifactGroupType() == ArtifactGroupTypeEnum.TOSCA && (component.getLifecycleState() == LifecycleStateEnum.NOT_CERTIFIED_CHECKIN || component.getLifecycleState() == LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	public Either<Either<ArtifactDefinition, Operation>, ResponseFormat> generateAndSaveToscaArtifact(ArtifactDefinition artifactDefinition, org.openecomp.sdc.be.model.Component component, User user, boolean isInCertificationRequest,
			boolean shouldLock, boolean inTransaction, boolean fetchTemplatesFromDB) {

		Either<Either<ArtifactDefinition, Operation>, ResponseFormat> generated = generateToscaArtifact(component, artifactDefinition, isInCertificationRequest, fetchTemplatesFromDB, shouldLock, inTransaction);
		if (generated.isRight()) {
			return generated;
		}
		byte[] decodedPayload = artifactDefinition.getPayloadData();
		artifactDefinition.setEsId(artifactDefinition.getUniqueId());
		artifactDefinition.setArtifactChecksum(GeneralUtility.calculateMD5ByByteArray(decodedPayload));
		return lockComponentAndUpdateArtifact(component.getUniqueId(), artifactDefinition, AuditingActionEnum.ARTIFACT_PAYLOAD_UPDATE, artifactDefinition.getUniqueId(), user, component.getComponentType(), component, decodedPayload, null, null,
				shouldLock, inTransaction);

	}

	private Either<Either<ArtifactDefinition, Operation>, ResponseFormat> generateToscaArtifact(Component parent, ArtifactDefinition artifactInfo, boolean isInCertificationRequest, boolean fetchTemplatesFromDB, boolean shouldLock,
			boolean inTransaction) {
		log.debug("tosca artifact generation");
		if (artifactInfo.getArtifactType().equals(ArtifactTypeEnum.TOSCA_CSAR.getType())) {
			Either<byte[], ResponseFormat> generated = csarUtils.createCsar(parent, fetchTemplatesFromDB, isInCertificationRequest, shouldLock, inTransaction);

			if (generated.isRight()) {
				log.debug("Failed to export tosca csar for component {} error {}", parent.getUniqueId(), generated.right().value());

				return Either.right(generated.right().value());
			}
			byte[] value = generated.left().value();
			artifactInfo.setPayload(value);

		} else {
			Either<ToscaRepresentation, ToscaError> exportComponent = toscaExportUtils.exportComponent(parent);
			if (exportComponent.isRight()) {
				log.debug("Failed export tosca yaml for component {} error {}", parent.getUniqueId(), exportComponent.right().value());
				ActionStatus status = componentsUtils.convertFromToscaError(exportComponent.right().value());
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(status);
				return Either.right(responseFormat);
			}
			log.debug("Tosca yaml exported for component {} ", parent.getUniqueId());
			String payload = exportComponent.left().value().getMainYaml();
			artifactInfo.setPayloadData(payload);
		}
		return Either.left(Either.left(artifactInfo));
	}

	private Either<Either<ArtifactDefinition, Operation>, ResponseFormat> doAction(String componentId, ComponentTypeEnum componentType, ArtifactOperation operation, String artifactId, ArtifactDefinition artifactInfo, String origMd5,
			String originData, String interfaceName, String operationName, AuditingActionEnum auditingAction, User user, org.openecomp.sdc.be.model.Component parent, boolean shouldLock, boolean inTransaction) {
		if (interfaceName != null && operationName != null) {
			interfaceName = interfaceName.toLowerCase();
			operationName = operationName.toLowerCase();
		}
		switch (operation) {
		case Download:
			if (artifactGenerationRequired(parent, artifactInfo)) {
				return generateToscaArtifact(parent, artifactInfo, false, false, shouldLock, inTransaction);
			}
			return handleDownload(componentId, artifactId, user, auditingAction, componentType, parent, shouldLock, inTransaction);
		case Delete:
			return handleDelete(componentId, artifactId, user, auditingAction, componentType, parent, interfaceName, operationName, shouldLock, inTransaction);
		case Update:
			ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(artifactInfo.getArtifactType());
			if (componentType.equals(ComponentTypeEnum.RESOURCE_INSTANCE)
					&& (artifactType == ArtifactTypeEnum.HEAT || artifactType == ArtifactTypeEnum.HEAT_VOL || artifactType == ArtifactTypeEnum.HEAT_NET || artifactType == ArtifactTypeEnum.HEAT_ENV)) {
				return handleUpdateHeatEnv(componentId, artifactInfo, auditingAction, artifactId, user, componentType, parent, originData, origMd5, operation, shouldLock, inTransaction);
			}
			return handleUpdate(componentId, artifactInfo, operation, auditingAction, artifactId, user, componentType, parent, origMd5, originData, interfaceName, operationName, shouldLock, inTransaction);
		case Create:
			return handleCreate(componentId, artifactInfo, operation, auditingAction, user, componentType, parent, origMd5, originData, interfaceName, operationName, shouldLock, inTransaction);
		}
		return null;
	}

	/**
	 * 
	 * @param componentId
	 * @param artifactId
	 * @param userId
	 * @param componentType
	 * @param parentId
	 *            TODO
	 * @return
	 */

	public Either<ImmutablePair<String, byte[]>, ResponseFormat> handleDownloadToscaModelRequest(Component component, ArtifactDefinition csarArtifact, boolean shouldLock, boolean inTransaction) {
		if (artifactGenerationRequired(component, csarArtifact)) {
			Either<byte[], ResponseFormat> generated = csarUtils.createCsar(component, false, false, shouldLock, inTransaction);

			if (generated.isRight()) {
				log.debug("Failed to export tosca csar for component {} error {}", component.getUniqueId(), generated.right().value());

				return Either.right(generated.right().value());
			}
			return Either.left(new ImmutablePair<String, byte[]>(csarArtifact.getArtifactName(), generated.left().value()));
		}
		return downloadArtifact(csarArtifact);
	}

	public Either<ImmutablePair<String, byte[]>, ResponseFormat> handleDownloadRequestById(String componentId, String artifactId, String userId, ComponentTypeEnum componentType, String parentId, String containerComponentType) {
		// perform all validation in common flow
		Either<Either<ArtifactDefinition, Operation>, ResponseFormat> result = handleArtifactRequest(componentId, userId, componentType, ArtifactOperation.Download, artifactId, null, null, null, null, null, parentId, containerComponentType);
		if (result.isRight()) {
			return Either.right(result.right().value());
		}
		ArtifactDefinition artifactDefinition;
		Either<ArtifactDefinition, Operation> insideValue = result.left().value();
		if (insideValue.isLeft()) {
			artifactDefinition = insideValue.left().value();
		} else {
			artifactDefinition = insideValue.right().value().getImplementation();
		}
		// for tosca artifacts generated on download without saving
		if (artifactDefinition.getArtifactGroupType() == ArtifactGroupTypeEnum.TOSCA && artifactDefinition.getPayloadData() != null) {
			return Either.left(new ImmutablePair<String, byte[]>(artifactDefinition.getArtifactName(), artifactDefinition.getPayloadData()));
		}
		return downloadArtifact(artifactDefinition);
	}

	private Either<ArtifactDefinition, ResponseFormat> validateArtifact(String componentId, ComponentTypeEnum componentType, String artifactId, org.openecomp.sdc.be.model.Component component, AuditingActionEnum auditingAction, String parentId) {
		// step 9
		// check artifact existence
		Either<ArtifactDefinition, StorageOperationStatus> artifactResult = artifactOperation.getArtifactById(artifactId, false);
		if (artifactResult.isRight()) {
			if (artifactResult.right().value().equals(StorageOperationStatus.ARTIFACT_NOT_FOUND)) {
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, "");
				log.debug("addArtifact - artifact {} not found", artifactId);
				return Either.right(responseFormat);

			} else {
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(artifactResult.right().value()));
				log.debug("addArtifact - failed to fetch artifact {}, error {}", artifactId, artifactResult.right().value());
				return Either.right(responseFormat);
			}
		}
		// step 9.1
		// check artifact belong to component
		boolean found = false;
		switch (componentType) {
		case RESOURCE:
		case SERVICE:
			found = checkArtifactInComponent(component, artifactId);
			break;
		case RESOURCE_INSTANCE:
			found = checkArtifactInResourceInstance(component, componentId, artifactId);
			break;
		default:

		}
		if (!found) {
			// String component =
			// componentType.equals(ComponentTypeEnum.RESOURCE) ? "resource" :
			// "service";
			String componentName = componentType.name().toLowerCase();
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_ARTIFACT_NOT_FOUND, componentName);
			log.debug("addArtifact - Component artifact not found component Id {}, artifact id {}", componentId, artifactId);
			return Either.right(responseFormat);
		}
		return Either.left(artifactResult.left().value());
	}

	private Either<Either<ArtifactDefinition, Operation>, ResponseFormat> handleCreate(String parentId, ArtifactDefinition artifactInfo, ArtifactOperation operation, AuditingActionEnum auditingAction, User user, ComponentTypeEnum componentType,
			org.openecomp.sdc.be.model.Component parent, String origMd5, String originData, String interfaceType, String operationName, boolean shouldLock, boolean inTransaction) {

		String artifactId = null;

		// step 11
		Either<byte[], ResponseFormat> payloadEither = validateInput(parentId, artifactInfo, operation, auditingAction, artifactId, user, componentType, parent, origMd5, originData, interfaceType, operationName, inTransaction);
		if (payloadEither.isRight()) {
			return Either.right(payloadEither.right().value());
		}
		byte[] decodedPayload = payloadEither.left().value();
		NodeTypeEnum parentType = convertParentType(componentType);
		// lock resource

		if (shouldLock) {
			Either<Boolean, ResponseFormat> lockComponent = lockComponent(parent, "Upload Artifact - lock ");
			if (lockComponent.isRight()) {
				handleAuditing(auditingAction, parent, parentId, user, null, null, null, lockComponent.right().value(), componentType, null);
				return Either.right(lockComponent.right().value());
			}
		}
		Either<Either<ArtifactDefinition, Operation>, ResponseFormat> resultOp = null;

		try {
			resultOp = createArtifact(parent, parentId, artifactInfo, decodedPayload, user, componentType, auditingAction, interfaceType, operationName);
			return resultOp;
		} finally {
			if (shouldLock) {
				unlockComponent(resultOp, parent, inTransaction);
			}

		}

	}

	private Either<Either<ArtifactDefinition, Operation>, ResponseFormat> lockComponentAndUpdateArtifact(String parentId, ArtifactDefinition artifactInfo, AuditingActionEnum auditingAction, String artifactId, User user,
			ComponentTypeEnum componentType, org.openecomp.sdc.be.model.Component parent, byte[] decodedPayload, String interfaceType, String operationName, boolean shouldLock, boolean inTransaction) {

		NodeTypeEnum parentType = convertParentType(componentType);

		// lock resource
		if (shouldLock) {
			Either<Boolean, ResponseFormat> lockComponent = lockComponent(parent, "Update Artifact - lock ");
			if (lockComponent.isRight()) {
				handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, lockComponent.right().value(), componentType, null);
				return Either.right(lockComponent.right().value());
			}
		}

		Either<Either<ArtifactDefinition, Operation>, ResponseFormat> resultOp = null;
		try {
			resultOp = updateArtifactFlow(parent, parentId, artifactId, artifactInfo, user, decodedPayload, componentType, auditingAction, interfaceType, operationName);
			return resultOp;

		} finally {
			if (shouldLock) {
				unlockComponent(resultOp, parent, inTransaction);
			}
		}
	}

	private Either<Either<ArtifactDefinition, Operation>, ResponseFormat> handleUpdate(String parentId, ArtifactDefinition artifactInfo, ArtifactOperation operation, AuditingActionEnum auditingAction, String artifactId, User user,
			ComponentTypeEnum componentType, org.openecomp.sdc.be.model.Component parent, String origMd5, String originData, String interfaceType, String operationName, boolean shouldLock, boolean inTransaction) {

		Either<byte[], ResponseFormat> payloadEither = validateInput(parentId, artifactInfo, operation, auditingAction, artifactId, user, componentType, parent, origMd5, originData, interfaceType, operationName, inTransaction);

		if (payloadEither.isRight()) {
			return Either.right(payloadEither.right().value());
		}
		byte[] decodedPayload = payloadEither.left().value();

		return lockComponentAndUpdateArtifact(parentId, artifactInfo, auditingAction, artifactId, user, componentType, parent, decodedPayload, interfaceType, operationName, shouldLock, inTransaction);
	}

	private Either<byte[], ResponseFormat> validateInput(String parentId, ArtifactDefinition artifactInfo, ArtifactOperation operation, AuditingActionEnum auditingAction, String artifactId, User user, ComponentTypeEnum componentType,
			org.openecomp.sdc.be.model.Component parent, String origMd5, String originData, String interfaceType, String operationName, boolean inTransaction) {
		// Md5 validations
		Either<Boolean, ResponseFormat> validateMd5 = validateMd5(origMd5, originData, artifactInfo.getPayloadData(), operation);
		if (validateMd5.isRight()) {
			ResponseFormat responseFormat = validateMd5.right().value();
			handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, responseFormat, componentType, null);
			return Either.right(responseFormat);
		}

		// step 11
		Either<ArtifactDefinition, ResponseFormat> validateResult = validateInput(parentId, artifactInfo, operation, artifactId, user, interfaceType, operationName, componentType, parent, inTransaction);
		if (validateResult.isRight()) {
			ResponseFormat responseFormat = validateResult.right().value();
			handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, responseFormat, componentType, null);
			return Either.right(validateResult.right().value());
		}

		Either<byte[], ResponseFormat> payloadEither = handlePayload(artifactInfo, isArtifactMetadataUpdate(auditingAction));
		if (payloadEither.isRight()) {
			ResponseFormat responseFormat = payloadEither.right().value();
			handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, responseFormat, componentType, null);
			log.debug("Error during handle payload");
			return Either.right(responseFormat);
		}

		// validate heat parameters. this part must be after the parameters are
		// extracted in "handlePayload"
		Either<ArtifactDefinition, ResponseFormat> validateAndConvertHeatParamers = validateAndConvertHeatParamers(artifactInfo, artifactInfo.getArtifactType());
		if (validateAndConvertHeatParamers.isRight()) {
			ResponseFormat responseFormat = validateAndConvertHeatParamers.right().value();
			handleAuditing(auditingAction, parent, parentId, user, artifactInfo, null, artifactId, responseFormat, componentType, null);
			log.debug("Error during handle payload");
			return Either.right(responseFormat);
		}
		return payloadEither;
	}

	public void handleAuditing(AuditingActionEnum auditingActionEnum, Component component, String componentId, User user, ArtifactDefinition artifactDefinition, String prevArtifactUuid, String currentArtifactUuid, ResponseFormat responseFormat,
			ComponentTypeEnum componentTypeEnum, String resourceInstanceName) {

		if (auditingActionEnum.getAuditingEsType().equals(AuditingTypesConstants.EXTERNAL_API_EVENT_TYPE)) {
			return;
		}

		EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = createArtifactAuditingFields(artifactDefinition, prevArtifactUuid, currentArtifactUuid);

		if (user == null) {
			user = new User();
			user.setUserId("UNKNOWN");
		}
		switch (componentTypeEnum) {

		case RESOURCE:
			Resource resource = (Resource) component;
			if (resource == null) {
				// In that case, component ID should be instead of name
				resource = new Resource();
				resource.setName(componentId);
			}
			componentsUtils.auditResource(responseFormat, user, resource, null, null, auditingActionEnum, auditingFields);
			break;

		case SERVICE:
			Service service = (Service) component;
			if (service == null) {
				// In that case, component ID should be instead of name
				service = new Service();
				service.setName(componentId);
			}
			componentsUtils.auditComponent(responseFormat, user, service, null, null, auditingActionEnum, ComponentTypeEnum.SERVICE, auditingFields);
			break;

		case RESOURCE_INSTANCE:
			if (resourceInstanceName == null) {
				resourceInstanceName = getResourceInstanceNameFromComponent(component, componentId);
			}
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceInstanceName);
			componentsUtils.auditComponent(responseFormat, user, component, null, null, auditingActionEnum, ComponentTypeEnum.RESOURCE_INSTANCE, auditingFields);

			break;
		default:
			break;
		}
	}

	private String getResourceInstanceNameFromComponent(Component component, String componentId) {
		ComponentInstance resourceInstance = component.getComponentInstances().stream().filter(p -> p.getUniqueId().equals(componentId)).findFirst().orElse(null);
		String resourceInstanceName = null;
		if (resourceInstance != null) {
			resourceInstanceName = resourceInstance.getName();
		}
		return resourceInstanceName;
	}

	public EnumMap<AuditingFieldsKeysEnum, Object> createArtifactAuditingFields(ArtifactDefinition artifactDefinition, String prevArtifactUuid, String currentArtifactUuid) {
		EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
		// Putting together artifact info
		String artifactData = buildAuditingArtifactData(artifactDefinition);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ARTIFACT_DATA, artifactData);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_PREV_ARTIFACT_UUID, prevArtifactUuid);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_CURR_ARTIFACT_UUID, currentArtifactUuid);
		return auditingFields;
	}

	// -----

	private String buildAuditingArtifactData(ArtifactDefinition artifactDefinition) {
		StringBuilder sb = new StringBuilder();
		if (artifactDefinition != null) {
			sb.append(artifactDefinition.getArtifactGroupType().getType()).append(",").append("'").append(artifactDefinition.getArtifactLabel()).append("'").append(",").append(artifactDefinition.getArtifactType()).append(",")
					.append(artifactDefinition.getArtifactName()).append(",").append(artifactDefinition.getTimeout()).append(",").append(artifactDefinition.getEsId());

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

	private Either<Boolean, ResponseFormat> validateMd5(String origMd5, String originData, byte[] payload, ArtifactOperation operation) {

		if (origMd5 != null) {
			String encodeBase64Str = GeneralUtility.calculateMD5ByString(originData);

			if (false == encodeBase64Str.equals(origMd5)) {
				log.debug("The calculated md5 is different then the received one");
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_INVALID_MD5));
			}
		} else {
			if (operation == ArtifactOperation.Create) {
				log.debug("Missing md5 header during artifact create");
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_INVALID_MD5));
			}
			// Update metadata
			if (payload != null && payload.length != 0) {
				log.debug("Cannot have payload while md5 header is missing");
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
			}
		}
		return Either.left(true);
	}

	private Either<ArtifactDefinition, ResponseFormat> validateInput(String parentId, ArtifactDefinition artifactInfo, ArtifactOperation operation, String artifactId, User user, String interfaceName, String operationName,
			ComponentTypeEnum componentType, Component parentComponent, boolean inTransaction) {

		Either<Boolean, ResponseFormat> validateAndSetArtifactname = validateAndSetArtifactname(artifactInfo);
		if (validateAndSetArtifactname.isRight()) {
			return Either.right(validateAndSetArtifactname.right().value());
		}
		Either<ArtifactDefinition, ResponseFormat> artifactById = fetchCurrentArtifact(operation, artifactId);
		if (artifactById.isRight()) {
			return Either.right(artifactById.right().value());
		}
		ArtifactDefinition currentArtifactInfo = artifactById.left().value();
		if (operationName != null && interfaceName != null) {
			operationName = operationName.toLowerCase();
			interfaceName = interfaceName.toLowerCase();
		}
		Either<ActionStatus, ResponseFormat> logicalNameStatus = handleArtifactLabel(parentId, operation, artifactId, artifactInfo, interfaceName, operationName, currentArtifactInfo, componentType, inTransaction);
		if (logicalNameStatus.isRight()) {
			return Either.right(logicalNameStatus.right().value());
		}
		// This is a patch to block possibility of updating service api fields
		// through other artifacts flow

		if (!operation.equals(ArtifactOperation.Create)) {
			checkAndSetUnUpdatableFields(user, artifactInfo, currentArtifactInfo, (operationName != null ? ArtifactGroupTypeEnum.LIFE_CYCLE : ArtifactGroupTypeEnum.INFORMATIONAL));
		} else {
			checkCreateFields(user, artifactInfo, (operationName != null ? ArtifactGroupTypeEnum.LIFE_CYCLE : ArtifactGroupTypeEnum.INFORMATIONAL));
		}

		composeArtifactId(parentId, artifactId, artifactInfo, interfaceName, operationName);
		if (currentArtifactInfo != null) {
			artifactInfo.setMandatory(currentArtifactInfo.getMandatory());
		}

		// artifactGroupType is not allowed to be updated
		if (!operation.equals(ArtifactOperation.Create)) {
			Either<ArtifactDefinition, ResponseFormat> validateGroupType = validateOrSetArtifactGroupType(artifactInfo, currentArtifactInfo);
			if (validateGroupType.isRight()) {
				return Either.right(validateGroupType.right().value());
			}
		}
		// TODO TEMP !!!
		NodeTypeEnum parentType = convertParentType(componentType);

		// TODO TEMP !!!
		boolean isCreate = operation.equals(ArtifactOperation.Create);

		if (isDeploymentArtifact(artifactInfo)) {
			Either<Boolean, ResponseFormat> deploymentValidationResult = validateDeploymentArtifact(parentComponent, parentId, user.getUserId(), isCreate, artifactInfo, currentArtifactInfo, parentType);
			if (deploymentValidationResult.isRight()) {
				return Either.right(deploymentValidationResult.right().value());
			}
		} else {
			artifactInfo.setTimeout(NON_HEAT_TIMEOUT);

			/*
			 * if (informationDeployedArtifactsBusinessLogic. isInformationDeployedArtifact(artifactInfo)) { Either<Boolean, ResponseFormat> validationResult = informationDeployedArtifactsBusinessLogic.validateArtifact( isCreate, artifactInfo,
			 * parentComponent, parentType); if (validationResult.isRight()) { return Either.right(validationResult.right().value()); } }
			 */
		}

		Either<Boolean, ResponseFormat> descriptionResult = validateAndCleanDescription(artifactInfo);
		if (descriptionResult.isRight()) {
			return Either.right(descriptionResult.right().value());
		}

		if (currentArtifactInfo != null && currentArtifactInfo.getArtifactGroupType().equals(ArtifactGroupTypeEnum.SERVICE_API)) {
			Either<ActionStatus, ResponseFormat> validateServiceApiType = validateArtifactType(user.getUserId(), artifactInfo, parentType);
			if (validateServiceApiType.isRight()) {
				return Either.right(validateServiceApiType.right().value());
			}
			// Change of type is not allowed and should be ignored

			artifactInfo.setArtifactType(ARTIFACT_TYPE_OTHER);

			Either<Boolean, ResponseFormat> validateUrl = validateAndServiceApiUrl(artifactInfo);
			if (validateUrl.isRight()) {
				return Either.right(validateUrl.right().value());
			}

			Either<Boolean, ResponseFormat> validateUpdate = validateFirstUpdateHasPayload(artifactInfo, currentArtifactInfo);
			if (validateUpdate.isRight()) {
				log.debug("serviceApi first update cnnot be without payload.");
				return Either.right(validateUpdate.right().value());
			}
		} else {
			Either<ActionStatus, ResponseFormat> validateArtifactType = validateArtifactType(user.getUserId(), artifactInfo, parentType);
			if (validateArtifactType.isRight()) {
				return Either.right(validateArtifactType.right().value());
			}
			if (artifactInfo.getApiUrl() != null) {
				artifactInfo.setApiUrl(null);
				log.error("Artifact URL cannot be set through this API - ignoring");
			}

			if (artifactInfo.getServiceApi() != null) {
				if (artifactInfo.getServiceApi()) {
					artifactInfo.setServiceApi(false);
					log.error("Artifact service API flag cannot be changed - ignoring");
				}
			}
		}

		return Either.left(artifactInfo);
	}

	private NodeTypeEnum convertParentType(ComponentTypeEnum componentType) {
		if (componentType.equals(ComponentTypeEnum.RESOURCE)) {
			return NodeTypeEnum.Resource;
		} else if (componentType.equals(ComponentTypeEnum.RESOURCE_INSTANCE)) {
			return NodeTypeEnum.ResourceInstance;
		} else {
			return NodeTypeEnum.Service;
		}
	}

	public Either<Either<ArtifactDefinition, Operation>, ResponseFormat> handleDelete(String parentId, String artifactId, User user, AuditingActionEnum auditingAction, ComponentTypeEnum componentType, org.openecomp.sdc.be.model.Component parent,
			String interfaceType, String operationName, boolean shouldLock, boolean inTransaction) {
		NodeTypeEnum parentType = convertParentType(componentType);
		// lock resource
		if (shouldLock) {
			Either<Boolean, ResponseFormat> lockComponent = lockComponent(parent, "Delete Artifact - lock resource: ");
			if (lockComponent.isRight()) {
				handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, lockComponent.right().value(), componentType, null);
				return Either.right(lockComponent.right().value());
			}
		}
		Either<Either<ArtifactDefinition, Operation>, ResponseFormat> resultOp = null;
		Either<ArtifactDefinition, Operation> insideEither = null;
		StorageOperationStatus error = null;
		boolean isLeft = false;
		ArtifactDefinition artifactDefinition = null;
		Integer artifactParentsCount = 1;
		try {
			if (interfaceType != null && operationName != null) {
				log.debug("Try to delete inteface lifecycle artifact {}", artifactId);

				Either<Operation, StorageOperationStatus> result = interfaceLifecycleOperation.deleteInterfaceOperation(parentId, interfaceType, UniqueIdBuilder.buildOperationByInterfaceUniqueId(parentId, interfaceType, operationName),
						inTransaction);
				isLeft = result.isLeft();
				if (isLeft) {
					artifactDefinition = result.left().value().getImplementation();
					insideEither = Either.right(result.left().value());
				}
			} else {
				log.debug("Try to delete artifact, get parents {}", artifactId);

				Either<Integer, StorageOperationStatus> parentsOfArtifact = artifactOperation.getParentsOfArtifact(artifactId, parentType);
				if (parentsOfArtifact.isRight()) {
					log.debug("Failed to delete entry on graph for artifact {}", artifactId);
					ResponseFormat responseFormat = componentsUtils.getResponseFormatByArtifactId(componentsUtils.convertFromStorageResponse(parentsOfArtifact.right().value()), "");
					resultOp = Either.right(responseFormat);
				} else {

					artifactParentsCount = parentsOfArtifact.left().value();
					log.debug("Number of parents nodes on graph for artifact {} is {}", artifactId, artifactParentsCount);

					Either<ArtifactDefinition, StorageOperationStatus> result = artifactOperation.removeArifactFromResource(parentId, artifactId, parentType, false, true);
					isLeft = result.isLeft();
					if (isLeft) {
						log.debug("Artifact removed from graph {}", artifactId);

						artifactDefinition = result.left().value();
						insideEither = Either.left(result.left().value());
					} else {
						error = result.right().value();
					}
				}
			}

			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
			if (isLeft) {
				StorageOperationStatus deleteIfNotOnGraph = StorageOperationStatus.OK;
				if (artifactParentsCount < 2) {
					log.debug("Number of parent nodes is 1. Need to delete from ES {}", artifactId);
					deleteIfNotOnGraph = deleteIfNotOnGraph(artifactId, artifactDefinition.getEsId(), true);
				}
				if (deleteIfNotOnGraph.equals(StorageOperationStatus.OK)) {
					if (artifactDefinition.getMandatory() || artifactDefinition.getServiceApi()) {
						log.debug("Artifact is mandatory or service API. Clean all fields for {}", artifactId);
						artifactDefinition.setEsId("");
						artifactDefinition.setArtifactName("");
						artifactDefinition.setDescription("");
						artifactDefinition.setApiUrl("");
						artifactDefinition.setArtifactChecksum("");
						setDefaultArtifactTimeout(artifactDefinition.getArtifactGroupType(), artifactDefinition);
						artifactDefinition.setArtifactUUID("");
						long time = System.currentTimeMillis();
						artifactDefinition.setPayloadUpdateDate(time);
						artifactDefinition.setHeatParameters(null);
						artifactDefinition.setHeatParamsUpdateDate(null);
						Either<ArtifactDefinition, StorageOperationStatus> resStatus = null;
						if (artifactParentsCount < 2) {
							log.debug("Only one parent , clean existing placeholder for {}", artifactId);
							resStatus = artifactOperation.updateArifactOnResource(artifactDefinition, parentId, artifactId, parentType, true);
						} else {
							log.debug("more than one parent , create new placeholder for {}", artifactId);
							artifactDefinition.setUniqueId(null);
							resStatus = artifactOperation.addArifactToComponent(artifactDefinition, parentId, parentType, true, true);
						}
						if (resStatus.isRight()) {
							log.debug("Failed to clean placeholder for {}", artifactId);
							responseFormat = componentsUtils.getResponseFormatByArtifactId(componentsUtils.convertFromStorageResponse(resStatus.right().value()), artifactDefinition.getArtifactDisplayName());
							resultOp = Either.right(responseFormat);
						} else {
							log.debug("Placeholder was cleaned for {}", artifactId);

							ArtifactDefinition artifactUfterChange = resStatus.left().value();

							insideEither = Either.left(artifactUfterChange);
							resultOp = Either.left(insideEither);
						}
					} else {
						log.debug("Artifact isn't mandatory/service API. Removed. {}", artifactId);
						resultOp = Either.left(insideEither);
					}

				} else {
					log.debug("failed to delete artifact from ES {} status {}", artifactId, deleteIfNotOnGraph);
					responseFormat = componentsUtils.getResponseFormatByArtifactId(componentsUtils.convertFromStorageResponse(deleteIfNotOnGraph), artifactDefinition.getArtifactDisplayName());
					resultOp = Either.right(responseFormat);
				}
			} else {
				log.debug("Failed to delete entry on graph for artifact {}", artifactId);
				responseFormat = componentsUtils.getResponseFormatByArtifactId(componentsUtils.convertFromStorageResponse(error), "");
				resultOp = Either.right(responseFormat);
			}
			handleAuditing(auditingAction, parent, parentId, user, artifactDefinition, null, artifactId, responseFormat, componentType, null);
			return resultOp;
		} finally {
			if (shouldLock) {
				unlockComponent(resultOp, parent, inTransaction);
			}
		}

	}

	private Either<Either<ArtifactDefinition, Operation>, ResponseFormat> handleDownload(String componentId, String artifactId, User user, AuditingActionEnum auditingAction, ComponentTypeEnum componentType,
			org.openecomp.sdc.be.model.Component parent, boolean shouldLock, boolean inTransaction) {
		Either<ArtifactDefinition, StorageOperationStatus> artifactById = artifactOperation.getArtifactById(artifactId, false);
		if (artifactById.isRight()) {
			ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(artifactById.right().value());
			log.debug("Error when getting artifact info by id{}, error: {}", artifactId, actionStatus.name());
			ResponseFormat responseFormat = componentsUtils.getResponseFormatByArtifactId(actionStatus, "");
			handleAuditing(auditingAction, parent, componentId, user, null, null, artifactId, responseFormat, componentType, null);
			return Either.right(responseFormat);
		}
		ArtifactDefinition artifactDefinition = artifactById.left().value();
		if (artifactDefinition == null) {
			log.debug("Empty artifact definition returned from DB by artifact id {}", artifactId);
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, "");
			handleAuditing(auditingAction, parent, componentId, user, null, null, artifactId, responseFormat, componentType, null);
			return Either.right(responseFormat);
		}
		Either<ArtifactDefinition, Operation> insideEither = Either.left(artifactDefinition);
		ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
		handleAuditing(auditingAction, parent, componentId, user, artifactDefinition, null, artifactId, responseFormat, componentType, null);
		return Either.left(insideEither);
	}

	private Either<ArtifactDefinition, ResponseFormat> fetchCurrentArtifact(ArtifactOperation operation, String artifactId) {
		Either<ArtifactDefinition, StorageOperationStatus> artifactById = artifactOperation.getArtifactById(artifactId, true);
		if (!operation.equals(ArtifactOperation.Create) && artifactById.isRight()) {
			// in case of update artifact must be
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeArtifactMissingError, "Artifact Update / Upload", artifactId);
			BeEcompErrorManager.getInstance().logBeArtifactMissingError("Artifact Update / Upload", artifactId);
			log.debug("Failed to fetch artifact {}. error: {}", artifactId, artifactById.right().value());
			return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(artifactById.right().value()), artifactId));
		}
		if (operation.equals(ArtifactOperation.Create) && artifactById.isLeft()) {
			log.debug("Artifact {} already exist", artifactId);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_EXIST, artifactById.left().value().getArtifactLabel()));
		}
		ArtifactDefinition currentArtifactInfo = null;
		if (artifactById.isLeft()) {
			// get previous value
			currentArtifactInfo = artifactById.left().value();
		}
		return Either.left(currentArtifactInfo);
	}

	private Either<ActionStatus, ResponseFormat> handleArtifactLabel(String componentId, ArtifactOperation operation, String artifactId, ArtifactDefinition artifactInfo, String interfaceName, String operationName,
			ArtifactDefinition currentArtifactInfo, ComponentTypeEnum componentType, boolean inTransaction) {
		String artifactLabel = artifactInfo.getArtifactLabel();

		if (operationName == null && (artifactInfo.getArtifactLabel() == null || artifactInfo.getArtifactLabel().isEmpty())) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeMissingArtifactInformationError, "Artifact Update / Upload", "artifactLabel");
			BeEcompErrorManager.getInstance().logBeMissingArtifactInformationError("Artifact Update / Upload", "artifactLabel");
			log.debug("missing artifact logical name for component {}", componentId);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.MISSING_DATA, ARTIFACT_LABEL));
		}
		if (operation.equals(ArtifactOperation.Create) && !artifactInfo.getMandatory()) {

			if (operationName != null) {
				if (artifactInfo.getArtifactLabel() != null && !operationName.equals(artifactInfo.getArtifactLabel())) {
					log.debug("artifact label cannot be set {}", artifactLabel);
					return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_LOGICAL_NAME_CANNOT_BE_CHANGED));
				} else {
					artifactLabel = operationName;
				}
			}
			String displayName = artifactInfo.getArtifactDisplayName();
			if (displayName == null || displayName.isEmpty())
				displayName = artifactLabel;
			displayName = ValidationUtils.cleanArtifactDisplayName(displayName);
			artifactInfo.setArtifactDisplayName(displayName);

			if (!ValidationUtils.validateArtifactLabel(artifactLabel)) {
				log.debug("Invalid format form Artifact label : {}", artifactLabel);
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
			}
			artifactLabel = ValidationUtils.normalizeArtifactLabel(artifactLabel);

			if (artifactLabel.isEmpty()) {
				log.debug("missing normalized artifact logical name for component {}", componentId);
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.MISSING_DATA, ARTIFACT_LABEL));
			}

			if (!ValidationUtils.validateArtifactLabelLength(artifactLabel)) {
				log.debug("Invalid lenght form Artifact label : {}", artifactLabel);
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.EXCEEDS_LIMIT, ARTIFACT_LABEL, String.valueOf(ValidationUtils.ARTIFACT_LABEL_LENGTH)));
			}
			if (!validateLabelUniqueness(componentId, artifactLabel, componentType, inTransaction)) {
				log.debug("Non unique Artifact label : {}", artifactLabel);
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_EXIST, artifactLabel));
			}
		}
		artifactInfo.setArtifactLabel(artifactLabel);

		if (currentArtifactInfo != null && !currentArtifactInfo.getArtifactLabel().equals(artifactInfo.getArtifactLabel())) {
			log.info("Logical artifact's name cannot be changed {}", artifactId);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_LOGICAL_NAME_CANNOT_BE_CHANGED));
		}
		return Either.left(ActionStatus.OK);
	}

	private boolean validateLabelUniqueness(String parentId, String artifactLabel, ComponentTypeEnum componentType, boolean inTransaction) {
		boolean isUnique = true;
		NodeTypeEnum parentType;
		if (componentType.equals(ComponentTypeEnum.RESOURCE)) {
			parentType = NodeTypeEnum.Resource;
		} else {
			parentType = NodeTypeEnum.Service;
		}
		Either<Map<String, ArtifactDefinition>, StorageOperationStatus> artifacts = artifactOperation.getArtifacts(parentId, parentType, inTransaction);
		if (artifacts.isLeft()) {
			for (String label : artifacts.left().value().keySet()) {
				if (label.equals(artifactLabel)) {
					isUnique = false;
					break;
				}
			}
		}
		if (componentType.equals(ComponentTypeEnum.RESOURCE)) {
			Either<Map<String, InterfaceDefinition>, StorageOperationStatus> allInterfacesOfResource = interfaceLifecycleOperation.getAllInterfacesOfResource(parentId, true, inTransaction);
			if (allInterfacesOfResource.isLeft()) {
				for (InterfaceDefinition interace : allInterfacesOfResource.left().value().values()) {
					for (Operation operation : interace.getOperations().values()) {
						if (operation.getImplementation() != null && operation.getImplementation().getArtifactLabel().equals(artifactLabel)) {
							isUnique = false;
							break;
						}
					}
				}
			}
		}
		return isUnique;
	}

	// ***************************************************************

	private Either<Either<ArtifactDefinition, Operation>, ResponseFormat> createArtifact(org.openecomp.sdc.be.model.Component parent, String parentId, ArtifactDefinition artifactInfo, byte[] decodedPayload, User user,
			ComponentTypeEnum componentTypeEnum, AuditingActionEnum auditingActionEnum, String interfaceType, String operationName) {

		ESArtifactData artifactData = createEsArtifactData(artifactInfo, decodedPayload);
		Either<Either<ArtifactDefinition, Operation>, ResponseFormat> resultOp = null;
		Either<ArtifactDefinition, Operation> insideEither = null;

		if (artifactData == null) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError, "Upload Artifact");
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Upload Artifact");
			log.debug("Failed to create artifact object for ES.");
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
			handleAuditing(auditingActionEnum, parent, parentId, user, artifactInfo, null, null, responseFormat, componentTypeEnum, null);
			resultOp = Either.right(responseFormat);
			return resultOp;

		}
		// set on graph object id of artifact in ES!
		artifactInfo.setEsId(artifactData.getId());

		boolean isLeft = false;
		String artifactUniqueId = null;
		ArtifactDefinition artifactDefinition = null;
		StorageOperationStatus error = null;
		if (interfaceType != null && operationName != null) {
			// lifecycle artifact
			Operation operation = convertToOperation(artifactInfo, operationName);

			Either<Operation, StorageOperationStatus> result = interfaceLifecycleOperation.updateInterfaceOperation(parentId, interfaceType, operationName, operation);

			isLeft = result.isLeft();
			if (isLeft) {
				artifactUniqueId = result.left().value().getImplementation().getUniqueId();
				artifactDefinition = result.left().value().getImplementation();

				insideEither = Either.right(result.left().value());
				resultOp = Either.left(insideEither);
			} else {
				error = result.right().value();
			}
		} else {
			// information/deployment/api aritfacts
			log.debug("Try to create entry on graph");
			NodeTypeEnum nodeType = convertParentType(componentTypeEnum);
			Either<ArtifactDefinition, StorageOperationStatus> result = artifactOperation.addArifactToComponent(artifactInfo, parentId, nodeType, true, true);

			isLeft = result.isLeft();
			if (isLeft) {
				artifactUniqueId = result.left().value().getUniqueId();
				artifactDefinition = result.left().value();

				insideEither = Either.left(result.left().value());
				resultOp = Either.left(insideEither);
			} else {
				error = result.right().value();
			}
		}
		if (isLeft) {
			boolean res = saveArtifacts(artifactData, parentId, false);
			// String uniqueId = artifactDefinition.getUniqueId();

			if (res) {
				log.debug("Artifact saved into ES - {}", artifactUniqueId);

				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
				handleAuditing(auditingActionEnum, parent, parentId, user, artifactInfo, artifactUniqueId, artifactUniqueId, responseFormat, componentTypeEnum, null);
				return resultOp;
			} else {
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError, "Upload Artifact");
				BeEcompErrorManager.getInstance().logBeDaoSystemError("Upload Artifact");
				log.debug("Failed to save the artifact.");
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
				handleAuditing(auditingActionEnum, parent, parentId, user, artifactInfo, null, artifactUniqueId, responseFormat, componentTypeEnum, null);

				resultOp = Either.right(responseFormat);
				return resultOp;
			}
		} else {
			log.debug("Failed to create entry on graph for artifact {}", artifactInfo.getArtifactName());
			ResponseFormat responseFormat = componentsUtils.getResponseFormatByArtifactId(componentsUtils.convertFromStorageResponse(error), artifactInfo.getArtifactDisplayName());
			handleAuditing(auditingActionEnum, parent, parentId, user, artifactInfo, null, null, responseFormat, componentTypeEnum, null);
			resultOp = Either.right(responseFormat);
			return resultOp;
		}

	}

	private Either<Boolean, ResponseFormat> validateDeploymentArtifact(Component parentComponent, String parentId, String userId, boolean isCreate, ArtifactDefinition artifactInfo, ArtifactDefinition currentArtifact, NodeTypeEnum parentType) {

		Either<Boolean, ResponseFormat> result = Either.left(true);
		Wrapper<ResponseFormat> responseWrapper = new Wrapper<ResponseFormat>();

		validateArtifactTypeExists(responseWrapper, artifactInfo);

		ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(artifactInfo.getArtifactType());

		Map<String, DeploymentArtifactTypeConfig> resourceDeploymentArtifacts = fillDeploymentArtifactTypeConf(parentType);

		if (responseWrapper.isEmpty()) {
			validateDeploymentArtifactConf(artifactInfo, responseWrapper, artifactType, resourceDeploymentArtifacts);
		}

		if (responseWrapper.isEmpty()) {
			// Common code for all types
			// not allowed to change artifactType
			if (!isCreate) {
				Either<Boolean, ResponseFormat> validateServiceApiType = validateArtifactTypeNotChanged(artifactInfo, currentArtifact);
				if (validateServiceApiType.isRight()) {
					responseWrapper.setInnerElement(validateServiceApiType.right().value());
				}
			}
		}
		if (responseWrapper.isEmpty()) {
			if (parentType.equals(NodeTypeEnum.Resource)) {
				// if (parentComponent instanceof Resource) {
				Resource resource = (Resource) parentComponent;
				ResourceTypeEnum resourceType = resource.getResourceType();
				DeploymentArtifactTypeConfig config = resourceDeploymentArtifacts.get(artifactType.getType());
				if (config == null) {
					responseWrapper.setInnerElement(ResponseFormatManager.getInstance().getResponseFormat(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED, artifactInfo.getArtifactType()));
				} else {
					List<String> myList = config.getValidForResourceTypes();
					Either<Boolean, ResponseFormat> either = validateResourceType(resourceType, artifactInfo, myList);
					if (either.isRight()) {
						responseWrapper.setInnerElement(either.right().value());
					}
				}
			}
		}
		if (responseWrapper.isEmpty()) {
			validateFileExtension(responseWrapper, () -> getDeploymentArtifactTypeConfig(parentType, artifactType), artifactInfo, parentType, artifactType);
		}

		if (responseWrapper.isEmpty() && !NodeTypeEnum.ResourceInstance.equals(parentType)) {
			String artifactName = artifactInfo.getArtifactName();
			if (isCreate || !artifactName.equalsIgnoreCase(currentArtifact.getArtifactName())) {
				validateSingleDeploymentArtifactName(responseWrapper, artifactName, parentComponent, parentType);
			}
		}

		if (responseWrapper.isEmpty()) {
			switch (artifactType) {
			case HEAT:
			case HEAT_VOL:
			case HEAT_NET: {
				result = validateHeatDeploymentArtifact(parentComponent, userId, isCreate, artifactInfo, currentArtifact, parentType);
				break;
			}
			case HEAT_ENV: {
				result = validateHeatEnvDeploymentArtifact(parentComponent, parentId, userId, isCreate, artifactInfo, parentType);
				artifactInfo.setTimeout(NON_HEAT_TIMEOUT);
				break;
			}
			case DCAE_INVENTORY_TOSCA:
			case DCAE_INVENTORY_JSON:
			case DCAE_INVENTORY_POLICY:
				// Validation is done in handle payload.
			case DCAE_INVENTORY_DOC:
			case DCAE_INVENTORY_BLUEPRINT:
			case DCAE_INVENTORY_EVENT:
				// No specific validation
			default: {
				artifactInfo.setTimeout(NON_HEAT_TIMEOUT);
			}
			}

		}

		if (!responseWrapper.isEmpty()) {
			result = Either.right(responseWrapper.getInnerElement());
		}
		return result;
	}

	private void validateDeploymentArtifactConf(ArtifactDefinition artifactInfo, Wrapper<ResponseFormat> responseWrapper, ArtifactTypeEnum artifactType, Map<String, DeploymentArtifactTypeConfig> resourceDeploymentArtifacts) {
		if ((resourceDeploymentArtifacts == null) || !resourceDeploymentArtifacts.containsKey(artifactType.name())) {
			ResponseFormat responseFormat = ResponseFormatManager.getInstance().getResponseFormat(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED, artifactInfo.getArtifactType());
			responseWrapper.setInnerElement(responseFormat);
			log.debug("Artifact Type: {} Not found !", artifactInfo.getArtifactType());
		}
	}

	private Map<String, DeploymentArtifactTypeConfig> fillDeploymentArtifactTypeConf(NodeTypeEnum parentType) {
		Map<String, DeploymentArtifactTypeConfig> resourceDeploymentArtifacts = null;
		if (parentType.equals(NodeTypeEnum.Resource)) {
			resourceDeploymentArtifacts = ConfigurationManager.getConfigurationManager().getConfiguration().getResourceDeploymentArtifacts();
		} else if (parentType.equals(NodeTypeEnum.ResourceInstance)) {
			resourceDeploymentArtifacts = ConfigurationManager.getConfigurationManager().getConfiguration().getResourceInstanceDeploymentArtifacts();
		} else {
			resourceDeploymentArtifacts = ConfigurationManager.getConfigurationManager().getConfiguration().getServiceDeploymentArtifacts();
		}
		return resourceDeploymentArtifacts;
	}

	public void validateArtifactTypeExists(Wrapper<ResponseFormat> responseWrapper, ArtifactDefinition artifactInfo) {
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(artifactInfo.getArtifactType());
		if (artifactType == null) {
			ResponseFormat responseFormat = ResponseFormatManager.getInstance().getResponseFormat(ActionStatus.GENERAL_ERROR);
			responseWrapper.setInnerElement(responseFormat);
			log.debug("Artifact Type: {} Not found !", artifactInfo.getArtifactType());
		}
	}

	private DeploymentArtifactTypeConfig getDeploymentArtifactTypeConfig(NodeTypeEnum parentType, ArtifactTypeEnum artifactType) {
		DeploymentArtifactTypeConfig retConfig = null;
		String fileType = artifactType.getType();
		if (parentType.equals(NodeTypeEnum.Resource)) {
			retConfig = ConfigurationManager.getConfigurationManager().getConfiguration().getResourceDeploymentArtifacts().get(fileType);
		} else if (parentType.equals(NodeTypeEnum.Service)) {
			retConfig = ConfigurationManager.getConfigurationManager().getConfiguration().getServiceDeploymentArtifacts().get(fileType);
		} else if (parentType.equals(NodeTypeEnum.ResourceInstance)) {
			retConfig = ConfigurationManager.getConfigurationManager().getConfiguration().getResourceInstanceDeploymentArtifacts().get(fileType);
		}
		return retConfig;
	}

	private Either<Boolean, ResponseFormat> extractHeatParameters(ArtifactDefinition artifactInfo) {
		// extract heat parameters
		if (artifactInfo.getPayloadData() != null) {
			String heatDecodedPayload = GeneralUtility.isBase64Encoded(artifactInfo.getPayloadData()) ? new String(Base64.decodeBase64(artifactInfo.getPayloadData())) : new String(artifactInfo.getPayloadData());
			Either<List<HeatParameterDefinition>, ResultStatusEnum> heatParameters = ImportUtils.getHeatParamsWithoutImplicitTypes(heatDecodedPayload, artifactInfo.getArtifactType());
			if (heatParameters.isRight() && (!heatParameters.right().value().equals(ResultStatusEnum.ELEMENT_NOT_FOUND))) {
				log.info("failed to parse heat parameters ");
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_DEPLOYMENT_ARTIFACT_HEAT, artifactInfo.getArtifactType());
				return Either.right(responseFormat);
			} else if (heatParameters.isLeft() && heatParameters.left().value() != null) {
				artifactInfo.setHeatParameters(heatParameters.left().value());
			}
		}
		return Either.left(true);

	}

	// Valid extension
	public void validateFileExtension(Wrapper<ResponseFormat> responseWrapper, IDeploymentArtifactTypeConfigGetter deploymentConfigGetter, ArtifactDefinition artifactInfo, NodeTypeEnum parentType, ArtifactTypeEnum artifactType) {
		String fileType = artifactType.getType();
		List<String> acceptedTypes = null;
		DeploymentArtifactTypeConfig deploymentAcceptedTypes = deploymentConfigGetter.getDeploymentArtifactConfig();
		if (!parentType.equals(NodeTypeEnum.Resource) && !parentType.equals(NodeTypeEnum.Service) && !parentType.equals(NodeTypeEnum.ResourceInstance)) {
			log.debug("parent type of artifact can be either resource or service");
			responseWrapper.setInnerElement(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
			return;
		}

		if (deploymentAcceptedTypes == null) {
			log.debug("parent type of artifact can be either resource or service");
			responseWrapper.setInnerElement(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED, artifactInfo.getArtifactType()));
			return;
		} else {
			acceptedTypes = deploymentAcceptedTypes.getAcceptedTypes();
		}
		/*
		 * No need to check specific types. In case there are no acceptedTypes in configuration, then any type is accepted.
		 * 
		 * if ((!artifactType.equals(ArtifactTypeEnum.OTHER) && !artifactType.equals(ArtifactTypeEnum.HEAT_ARTIFACT )) && (acceptedTypes == null || acceptedTypes.isEmpty()) ) { log.debug( "No accepted types found for type {}, parent type {}",
		 * fileType, parentType.getName()); String methodName = new Object() { }.getClass().getEnclosingMethod().getName(); String configEntryMissing = (parentType.equals(NodeTypeEnum.Resource)) ? "resourceDeploymentArtifacts:" + fileType :
		 * "serviceDeploymentArtifacts:" + fileType; BeEcompErrorManager.getInstance().processEcompError(EcompErrorName. BeMissingConfigurationError, methodName, configEntryMissing); BeEcompErrorManager.getInstance().logBeMissingConfigurationError(
		 * methodName, configEntryMissing); responseWrapper.setInnerElement(componentsUtils.getResponseFormat( ActionStatus.GENERAL_ERROR)); return; }
		 */

		String artifactName = artifactInfo.getArtifactName();
		String fileExtension = GeneralUtility.getFilenameExtension(artifactName);
		// Pavel - File extension validation is case-insensitive - Ella,
		// 21/02/2016
		if (acceptedTypes != null && !acceptedTypes.isEmpty() && !acceptedTypes.contains(fileExtension.toLowerCase())) {
			log.debug("File extension \"{}\" is not allowed for {} which is of type:{}", fileExtension, artifactName, fileType);
			responseWrapper.setInnerElement(componentsUtils.getResponseFormat(ActionStatus.WRONG_ARTIFACT_FILE_EXTENSION, fileType));
			return;
		}
	}

	private Either<Boolean, ResponseFormat> validateHeatEnvDeploymentArtifact(Component parentComponent, String parentId, String userId, boolean isCreate, ArtifactDefinition artifactInfo, NodeTypeEnum parentType) {

		Wrapper<ResponseFormat> errorWrapper = new Wrapper<ResponseFormat>();
		Wrapper<ArtifactDefinition> heatMDWrapper = new Wrapper<ArtifactDefinition>();
		Wrapper<byte[]> payloadWrapper = new Wrapper<>();

		if (errorWrapper.isEmpty()) {
			validateValidYaml(errorWrapper, artifactInfo);
		}

		if (errorWrapper.isEmpty()) {
			// Validate Heat Exist
			validateHeatExist(artifactInfo.getUniqueId(), errorWrapper, heatMDWrapper, getDeploymentArtifacts(parentComponent, parentType, parentId));
		}

		if (errorWrapper.isEmpty() && isCreate) {
			// Validate Only Single HeatEnv Artifact
			validateSingleArtifactType(errorWrapper, ArtifactTypeEnum.HEAT_ENV, parentComponent, parentType, parentId);
		}

		if (errorWrapper.isEmpty() && !heatMDWrapper.isEmpty()) {
			fillArtifactPayloadValidation(errorWrapper, payloadWrapper, heatMDWrapper.getInnerElement());
		}

		if (errorWrapper.isEmpty() && !heatMDWrapper.isEmpty()) {
			validateEnvVsHeat(errorWrapper, artifactInfo, heatMDWrapper.getInnerElement(), payloadWrapper.getInnerElement());
		}

		// Init Response
		Either<Boolean, ResponseFormat> eitherResponse;
		if (errorWrapper.isEmpty()) {
			eitherResponse = Either.left(true);
		} else {
			eitherResponse = Either.right(errorWrapper.getInnerElement());
		}
		return eitherResponse;
	}

	public void fillArtifactPayloadValidation(Wrapper<ResponseFormat> errorWrapper, Wrapper<byte[]> payloadWrapper, ArtifactDefinition artifactDefinition) {
		if (artifactDefinition.getPayloadData() == null || artifactDefinition.getPayloadData().length == 0) {
			Either<Boolean, ResponseFormat> fillArtifactPayload = fillArtifactPayload(payloadWrapper, artifactDefinition);
			if (fillArtifactPayload.isRight()) {
				errorWrapper.setInnerElement(fillArtifactPayload.right().value());
				log.debug("Error getting payload for artifact:{}", artifactDefinition.getArtifactName());
			}
		} else {
			payloadWrapper.setInnerElement(artifactDefinition.getPayloadData());
		}
	}

	public Either<Boolean, ResponseFormat> fillArtifactPayload(Wrapper<byte[]> payloadWrapper, ArtifactDefinition artifactMD) {
		Either<Boolean, ResponseFormat> result = Either.left(true);
		Either<ESArtifactData, CassandraOperationStatus> eitherArtifactData = artifactCassandraDao.getArtifact(artifactMD.getEsId());
		// Either<ESArtifactData, ResourceUploadStatus> eitherArtifactData =
		// esCatalogDao.getArtifact(artifactMD.getEsId());
		if (eitherArtifactData.isLeft()) {
			byte[] data = eitherArtifactData.left().value().getDataAsArray();
			if (!GeneralUtility.isBase64Encoded(data)) {
				data = Base64.encodeBase64(data);
			}
			payloadWrapper.setInnerElement(data);
		} else {
			StorageOperationStatus storageStatus = DaoStatusConverter.convertCassandraStatusToStorageStatus(eitherArtifactData.right().value());
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(storageStatus));
			result = Either.right(responseFormat);
		}
		return result;

	}

	@SuppressWarnings("unchecked")
	private void validateEnvVsHeat(Wrapper<ResponseFormat> errorWrapper, ArtifactDefinition envArtifact, ArtifactDefinition heatArtifact, byte[] heatPayloadData) {

		String envPayload = (GeneralUtility.isBase64Encoded(envArtifact.getPayloadData())) ? new String(Base64.decodeBase64(envArtifact.getPayloadData())) : new String(envArtifact.getPayloadData());
		Map<String, Object> heatEnvToscaJson = (Map<String, Object>) new Yaml().load(envPayload);

		String heatDecodedPayload = (GeneralUtility.isBase64Encoded(heatPayloadData)) ? new String(Base64.decodeBase64(heatPayloadData)) : new String(heatPayloadData);
		Map<String, Object> heatToscaJson = (Map<String, Object>) new Yaml().load(heatDecodedPayload);

		Either<Map<String, Object>, ResultStatusEnum> eitherHeatEnvProperties = ImportUtils.findFirstToscaMapElement(heatEnvToscaJson, ToscaTagNamesEnum.PARAMETERS);
		Either<Map<String, Object>, ResultStatusEnum> eitherHeatProperties = ImportUtils.findFirstToscaMapElement(heatToscaJson, ToscaTagNamesEnum.PARAMETERS);
		if (eitherHeatEnvProperties.isRight()) {
			ResponseFormat responseFormat = ResponseFormatManager.getInstance().getResponseFormat(ActionStatus.CORRUPTED_FORMAT, "Heat Env");
			errorWrapper.setInnerElement(responseFormat);
			log.debug("Invalid heat env format for file:{}", envArtifact.getArtifactName());
		} else if (eitherHeatProperties.isRight()) {
			ResponseFormat responseFormat = ResponseFormatManager.getInstance().getResponseFormat(ActionStatus.MISMATCH_HEAT_VS_HEAT_ENV, envArtifact.getArtifactName(), heatArtifact.getArtifactName());
			errorWrapper.setInnerElement(responseFormat);
			log.debug("Validation of heat_env for artifact:{} vs heat artifact for artifact :{} failed", envArtifact.getArtifactName(), heatArtifact.getArtifactName());
		} else {
			Set<String> heatPropertiesKeys = eitherHeatProperties.left().value().keySet();
			Set<String> heatEnvPropertiesKeys = eitherHeatEnvProperties.left().value().keySet();
			heatEnvPropertiesKeys.removeAll(heatPropertiesKeys);
			if (heatEnvPropertiesKeys.size() > 0) {
				ResponseFormat responseFormat = ResponseFormatManager.getInstance().getResponseFormat(ActionStatus.MISMATCH_HEAT_VS_HEAT_ENV, envArtifact.getArtifactName(), heatArtifact.getArtifactName());
				errorWrapper.setInnerElement(responseFormat);
			}
		}
	}

	private void validateValidYaml(Wrapper<ResponseFormat> errorWrapper, ArtifactDefinition artifactInfo) {
		YamlToObjectConverter yamlConvertor = new YamlToObjectConverter();
		boolean isYamlValid = yamlConvertor.isValidYaml(artifactInfo.getPayloadData());
		if (!isYamlValid) {
			ResponseFormat responseFormat = ResponseFormatManager.getInstance().getResponseFormat(ActionStatus.INVALID_YAML, artifactInfo.getArtifactType());
			errorWrapper.setInnerElement(responseFormat);
			log.debug("Yaml is not valid for artifact : {}", artifactInfo.getArtifactName());
		}
	}

	public boolean isValidXml(byte[] xmlToParse) {
		XMLReader parser = new SAXParser();
		boolean isXmlValid = true;
		try {
			parser.parse(new InputSource(new ByteArrayInputStream(xmlToParse)));
		} catch (IOException | SAXException e) {
			isXmlValid = false;
		}
		return isXmlValid;
	}

	public boolean isValidJson(byte[] jsonToParse) {
		String parsed = new String(jsonToParse);
		try {
			gson.fromJson(parsed, Object.class);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public void validateSingleArtifactType(Wrapper<ResponseFormat> errorWrapper, ArtifactTypeEnum allowedArtifactType, Component parentComponent, NodeTypeEnum parentType, String parentRiId) {
		boolean typeArtifactFound = false;
		// Iterator<ArtifactDefinition> parentDeploymentArtifactsItr =
		// (parentType == NodeTypeEnum.Resource) ?
		// informationDeployedArtifactsBusinessLogic.getAllDeployableArtifacts((Resource)
		// parentComponent).iterator()
		// : getDeploymentArtifacts(parentComponent, parentType).iterator();

		Iterator<ArtifactDefinition> parentDeploymentArtifactsItr = getDeploymentArtifacts(parentComponent, parentType, parentRiId).iterator();

		while (!typeArtifactFound && parentDeploymentArtifactsItr.hasNext()) {
			ArtifactTypeEnum foundArtifactType = ArtifactTypeEnum.findType(parentDeploymentArtifactsItr.next().getArtifactType());
			typeArtifactFound = (foundArtifactType == allowedArtifactType);
		}
		if (typeArtifactFound) {
			String parentName = parentComponent.getName();
			ResponseFormat responseFormat = ResponseFormatManager.getInstance().getResponseFormat(ActionStatus.DEPLOYMENT_ARTIFACT_OF_TYPE_ALREADY_EXISTS, parentType.name(), parentName, allowedArtifactType.getType(), allowedArtifactType.getType());

			errorWrapper.setInnerElement(responseFormat);
			log.debug("Can't upload artifact of type: {}, because another artifact of this type already exist.", allowedArtifactType.getType());

		}
	}

	public void validateSingleDeploymentArtifactName(Wrapper<ResponseFormat> errorWrapper, String artifactName, Component parentComponent, NodeTypeEnum parentType) {
		boolean artifactNameFound = false;
		// Iterator<ArtifactDefinition> parentDeploymentArtifactsItr =
		// (parentType == NodeTypeEnum.Resource) ?
		// informationDeployedArtifactsBusinessLogic.getAllDeployableArtifacts((Resource)
		// parentComponent).iterator()
		// : getDeploymentArtifacts(parentComponent, parentType).iterator();

		Iterator<ArtifactDefinition> parentDeploymentArtifactsItr = getDeploymentArtifacts(parentComponent, parentType, null).iterator();

		while (!artifactNameFound && parentDeploymentArtifactsItr.hasNext()) {
			artifactNameFound = (artifactName.equalsIgnoreCase(parentDeploymentArtifactsItr.next().getArtifactName()));
		}
		if (artifactNameFound) {
			String parentName = parentComponent.getName();
			ResponseFormat responseFormat = ResponseFormatManager.getInstance().getResponseFormat(ActionStatus.DEPLOYMENT_ARTIFACT_NAME_ALREADY_EXISTS, parentType.name(), parentName, artifactName);

			errorWrapper.setInnerElement(responseFormat);
			log.debug("Can't upload artifact: {}, because another artifact with this name already exist.", artifactName);

		}
	}

	private void validateHeatExist(String heatEnvId, Wrapper<ResponseFormat> errorWrapper, Wrapper<ArtifactDefinition> heatArtifactMDWrapper, Collection<ArtifactDefinition> parentDeploymentArtifacts) {
		boolean heatFound = false;
		Either<ArtifactDefinition, StorageOperationStatus> res = artifactOperation.getHeatArtifactByHeatEnvId(heatEnvId, true);
		if (res.isRight()) {
			return;
		}
		ArtifactDefinition heatArtifact = res.left().value();
		Iterator<ArtifactDefinition> parentArtifactsItr = parentDeploymentArtifacts.iterator();
		while (!heatFound && parentArtifactsItr.hasNext()) {
			ArtifactDefinition currArtifact = parentArtifactsItr.next();
			if (heatArtifact.getUniqueId().equals(currArtifact.getUniqueId())) {
				heatFound = true;
				heatArtifactMDWrapper.setInnerElement(currArtifact);
				log.trace("In validateHeatExist found artifact {}", currArtifact);
				/*
				 * ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(currArtifact.getArtifactType()); if(artifactType == ArtifactTypeEnum.HEAT || artifactType == ArtifactTypeEnum.HEAT_VOL || artifactType == ArtifactTypeEnum.HEAT_NET){
				 * heatFound = true; } if (heatFound) { heatArtifactMDWrapper.setInnerElement(currArtifact); }
				 */
			}
		}
		if (!heatFound) {
			ResponseFormat responseFormat = ResponseFormatManager.getInstance().getResponseFormat(ActionStatus.MISSING_HEAT);
			errorWrapper.setInnerElement(responseFormat);
			log.debug("Can't create heat env artifact because No heat Artifact exist.");
		}

	}

	private Either<Boolean, ResponseFormat> validateHeatDeploymentArtifact(Component parentComponent, String userId, boolean isCreate, ArtifactDefinition artifactInfo, ArtifactDefinition currentArtifact, NodeTypeEnum parentType) {
		log.trace("Started HEAT pre-payload validation for artifact {}", artifactInfo.getArtifactLabel());
		// timeout > 0 for HEAT artifacts
		Integer timeout = artifactInfo.getTimeout();
		Integer defaultTimeout = (isCreate) ? defaultHeatTimeout : currentArtifact.getTimeout();
		if (timeout == null) {
			artifactInfo.setTimeout(defaultTimeout);
			// HEAT artifact but timeout is invalid
		} else if (timeout < 1) {
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_INVALID_TIMEOUT));
		}

		// US649856 - Allow several HEAT files on Resource
		/*
		 * if (isCreate) { Wrapper<ResponseFormat> errorWrapper = new Wrapper<>(); validateSingleArtifactType(errorWrapper, ArtifactTypeEnum.findType(artifactInfo.getArtifactType()), parentComponent, parentType); if (!errorWrapper.isEmpty()) { return
		 * Either.right(errorWrapper.getInnerElement()); } }
		 */

		log.trace("Ended HEAT validation for artifact {}", artifactInfo.getArtifactLabel());
		return Either.left(true);
	}

	private Either<Boolean, ResponseFormat> validateResourceType(ResourceTypeEnum resourceType, ArtifactDefinition artifactInfo, List<String> typeList) {
		String listToString = (typeList != null) ? typeList.toString() : "";
		ResponseFormat responseFormat = ResponseFormatManager.getInstance().getResponseFormat(ActionStatus.MISMATCH_BETWEEN_ARTIFACT_TYPE_AND_COMPONENT_TYPE, artifactInfo.getArtifactName(), listToString, resourceType.getValue());
		Either<Boolean, ResponseFormat> either = Either.right(responseFormat);
		String resourceTypeName = resourceType.name();
		if (typeList != null && typeList.contains(resourceTypeName)) {
			either = Either.left(true);
		}
		return either;
	}

	private Either<ArtifactDefinition, ResponseFormat> validateAndConvertHeatParamers(ArtifactDefinition artifactInfo, String artifactType) {
		if (artifactInfo.getHeatParameters() != null) {
			for (HeatParameterDefinition heatParam : artifactInfo.getHeatParameters()) {
				String parameterType = heatParam.getType();
				HeatParameterType heatParameterType = HeatParameterType.isValidType(parameterType);
				String artifactTypeStr = artifactType != null ? artifactType : ArtifactTypeEnum.HEAT.getType();
				if (heatParameterType == null) {
					ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_HEAT_PARAMETER_TYPE, artifactTypeStr, heatParam.getType());
					return Either.right(responseFormat);
				}

				StorageOperationStatus validateAndUpdateProperty = heatParametersOperation.validateAndUpdateProperty(heatParam);
				if (validateAndUpdateProperty != StorageOperationStatus.OK) {
					log.debug("Heat parameter {} is invalid. Status is: {}", heatParam.getName(), validateAndUpdateProperty);
					ActionStatus status = ActionStatus.INVALID_HEAT_PARAMETER_VALUE;
					ResponseFormat responseFormat = componentsUtils.getResponseFormat(status, artifactTypeStr, heatParam.getType(), heatParam.getName());
					return Either.right(responseFormat);
				}
			}
		}
		return Either.left(artifactInfo);
	}

	public List<ArtifactDefinition> getDeploymentArtifacts(Component parentComponent, NodeTypeEnum parentType, String ciId) {
		List<ArtifactDefinition> deploymentArtifacts = new ArrayList<>();
		if (parentComponent.getDeploymentArtifacts() != null && ciId != null) {
			if (NodeTypeEnum.ResourceInstance == parentType) {
				Either<ComponentInstance, ResponseFormat> getRI = getRIFromComponent(parentComponent, ciId, null, null, null);
				if (getRI.isRight()) {
					return deploymentArtifacts;
				}
				ComponentInstance ri = getRI.left().value();
				deploymentArtifacts.addAll(ri.getDeploymentArtifacts().values());
			} else {
				deploymentArtifacts.addAll(parentComponent.getDeploymentArtifacts().values());
			}
		}
		return deploymentArtifacts;
	}

	private void checkCreateFields(User user, ArtifactDefinition artifactInfo, ArtifactGroupTypeEnum type) {
		// on create if null add informational to current
		if (artifactInfo.getArtifactGroupType() == null) {
			artifactInfo.setArtifactGroupType(type);
		}
		if (artifactInfo.getUniqueId() != null) {
			log.error("artifact uniqid cannot be set ignoring");
		}
		artifactInfo.setUniqueId(null);

		if (artifactInfo.getArtifactRef() != null) {
			log.error("artifact ref cannot be set ignoring");
		}
		artifactInfo.setArtifactRef(null);

		if (artifactInfo.getArtifactRepository() != null) {
			log.error("artifact repository cannot be set ignoring");
		}
		artifactInfo.setArtifactRepository(null);

		if (artifactInfo.getUserIdCreator() != null) {
			log.error("creator uuid cannot be set ignoring");
		}
		artifactInfo.setArtifactCreator(user.getUserId());

		if (artifactInfo.getUserIdLastUpdater() != null) {
			log.error("userId of last updater cannot be set ignoring");
		}
		artifactInfo.setUserIdLastUpdater(user.getUserId());

		if (artifactInfo.getCreatorFullName() != null) {
			log.error("creator Full name cannot be set ignoring");
		}
		String fullName = user.getFirstName() + " " + user.getLastName();
		artifactInfo.setUpdaterFullName(fullName);

		if (artifactInfo.getUpdaterFullName() != null) {
			log.error("updater Full name cannot be set ignoring");
		}
		artifactInfo.setUpdaterFullName(fullName);

		if (artifactInfo.getCreationDate() != null) {
			log.error("Creation Date cannot be set ignoring");
		}
		long time = System.currentTimeMillis();
		artifactInfo.setCreationDate(time);

		if (artifactInfo.getLastUpdateDate() != null) {
			log.error("Last Update Date cannot be set ignoring");
		}
		artifactInfo.setLastUpdateDate(time);

		if (artifactInfo.getEsId() != null) {
			log.error("es id cannot be set ignoring");
		}
		artifactInfo.setEsId(null);

	}

	private Either<ArtifactDefinition, ResponseFormat> fetchCurrentArtifact(boolean isCreate, String artifactId) {
		Either<ArtifactDefinition, StorageOperationStatus> artifactById = artifactOperation.getArtifactById(artifactId, true);
		if (isCreate == false && artifactById.isRight()) {
			// in case of update artifact must be
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeArtifactMissingError, "Artifact Update / Upload", artifactId);
			BeEcompErrorManager.getInstance().logBeArtifactMissingError("Artifact Update / Upload", artifactId);
			log.debug("Failed to fetch artifact {}. error: {}", artifactId, artifactById.right().value());
			return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(artifactById.right().value()), artifactId));
		}
		if (isCreate && artifactById.isLeft()) {
			log.debug("Artifact {} already exist", artifactId);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_EXIST, artifactById.left().value().getArtifactLabel()));
		}
		ArtifactDefinition currentArtifactInfo = null;
		if (artifactById.isLeft()) {
			// get previous value
			currentArtifactInfo = artifactById.left().value();
		}
		return Either.left(currentArtifactInfo);
	}

	private Either<ActionStatus, ResponseFormat> handleArtifactLabel(String resourceId, boolean isCreate, String artifactId, ArtifactDefinition artifactInfo, String interfaceName, String operationName, ArtifactDefinition currentArtifactInfo,
			NodeTypeEnum parentType) {
		String artifactLabel = artifactInfo.getArtifactLabel();

		if (operationName == null && (artifactInfo.getArtifactLabel() == null || artifactInfo.getArtifactLabel().isEmpty())) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeMissingArtifactInformationError, "Artifact Update / Upload", "artifactLabel");
			BeEcompErrorManager.getInstance().logBeMissingArtifactInformationError("Artifact Update / Upload", "artifactLabel");
			log.debug("missing artifact logical name for component {}", resourceId);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.MISSING_DATA, ARTIFACT_LABEL));
		}
		if (isCreate && !artifactInfo.getMandatory()) {

			if (operationName != null) {
				if (artifactInfo.getArtifactLabel() != null && !operationName.equals(artifactInfo.getArtifactLabel())) {
					log.debug("artifact label cannot be set {}", artifactLabel);
					return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_LOGICAL_NAME_CANNOT_BE_CHANGED));
				} else {
					artifactLabel = operationName;
				}
			}
			String displayName = ValidationUtils.cleanArtifactDisplayName(artifactLabel);
			artifactInfo.setArtifactDisplayName(displayName);

			if (!ValidationUtils.validateArtifactLabel(artifactLabel)) {
				log.debug("Invalid format form Artifact label : {}", artifactLabel);
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
			}
			artifactLabel = ValidationUtils.normalizeArtifactLabel(artifactLabel);

			if (artifactLabel.isEmpty()) {
				log.debug("missing normalized artifact logical name for component {}", resourceId);
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.MISSING_DATA, ARTIFACT_LABEL));
			}

			if (!ValidationUtils.validateArtifactLabelLength(artifactLabel)) {
				log.debug("Invalid lenght form Artifact label : {}", artifactLabel);
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.EXCEEDS_LIMIT, ARTIFACT_LABEL, String.valueOf(ValidationUtils.ARTIFACT_LABEL_LENGTH)));
			}
			if (!validateLabelUniqueness(resourceId, artifactLabel, parentType)) {
				log.debug("Non unique Artifact label : {}", artifactLabel);
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_EXIST, artifactLabel));
			}
		}
		artifactInfo.setArtifactLabel(artifactLabel);

		if (currentArtifactInfo != null && !currentArtifactInfo.getArtifactLabel().equals(artifactInfo.getArtifactLabel())) {
			log.info("Logical artifact's name cannot be changed {}", artifactId);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_LOGICAL_NAME_CANNOT_BE_CHANGED));
		}
		return Either.left(ActionStatus.OK);
	}

	private boolean validateLabelUniqueness(String parentId, String artifactLabel, NodeTypeEnum parentType) {
		boolean isUnique = true;
		Either<Map<String, ArtifactDefinition>, StorageOperationStatus> artifacts = artifactOperation.getArtifacts(parentId, parentType, true);
		if (artifacts.isLeft()) {
			for (String label : artifacts.left().value().keySet()) {
				if (label.equals(artifactLabel)) {
					isUnique = false;
					break;
				}
			}
		}
		if (parentType.equals(NodeTypeEnum.Resource)) {
			Either<Map<String, InterfaceDefinition>, StorageOperationStatus> allInterfacesOfResource = interfaceLifecycleOperation.getAllInterfacesOfResource(parentId, true);
			if (allInterfacesOfResource.isLeft()) {
				for (InterfaceDefinition interace : allInterfacesOfResource.left().value().values()) {
					for (Operation operation : interace.getOperations().values()) {
						if (operation.getImplementation() != null && operation.getImplementation().getArtifactLabel().equals(artifactLabel)) {
							isUnique = false;
							break;
						}
					}
				}
			}
		}
		return isUnique;
	}

	private String composeArtifactId(String resourceId, String artifactId, ArtifactDefinition artifactInfo, String interfaceName, String operationName) {
		String id = artifactId;
		if (artifactId == null || artifactId.isEmpty()) {
			String uniqueId = null;
			if (interfaceName != null && operationName != null) {
				uniqueId = UniqueIdBuilder.buildArtifactByInterfaceUniqueId(resourceId, interfaceName, operationName, artifactInfo.getArtifactLabel());
			} else {
				uniqueId = UniqueIdBuilder.buildPropertyUniqueId(resourceId, artifactInfo.getArtifactLabel());
			}
			artifactInfo.setUniqueId(uniqueId);
			artifactInfo.setEsId(uniqueId);
			id = uniqueId;
		} else {
			artifactInfo.setUniqueId(artifactId);
			artifactInfo.setEsId(artifactId);
		}
		return id;
	}

	private Either<ActionStatus, ResponseFormat> validateArtifactType(String userId, ArtifactDefinition artifactInfo, NodeTypeEnum parentType) {
		if (artifactInfo.getArtifactType() == null || artifactInfo.getArtifactType().isEmpty()) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeMissingArtifactInformationError, "Artifact Upload / Update");
			BeEcompErrorManager.getInstance().logBeMissingArtifactInformationError("Artifact Update / Upload", "artifactLabel");
			log.debug("Missing artifact type for artifact {}", artifactInfo.getArtifactName());
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.MISSING_ARTIFACT_TYPE));
		}

		boolean artifactTypeExist = false;
		Either<List<ArtifactType>, ActionStatus> allArtifactTypes = null;
		ArtifactGroupTypeEnum artifactGroupType = artifactInfo.getArtifactGroupType();

		if ((artifactGroupType != null) && artifactGroupType.equals(ArtifactGroupTypeEnum.DEPLOYMENT)) {
			allArtifactTypes = getDeploymentArtifactTypes(userId, artifactInfo, parentType);
		} else {

			allArtifactTypes = elementOperation.getAllArtifactTypes();
		}
		if (allArtifactTypes.isRight()) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeInvalidConfigurationError, "Artifact Upload / Update", "artifactTypes", allArtifactTypes.right().value().name());
			BeEcompErrorManager.getInstance().logBeInvalidConfigurationError("Artifact Upload / Update", "artifactTypes", allArtifactTypes.right().value().name());
			log.debug("Failed to retrieve list of suported artifact types. error: {}", allArtifactTypes.right().value());
			return Either.right(componentsUtils.getResponseFormatByUserId(allArtifactTypes.right().value(), userId));
		}

		for (ArtifactType type : allArtifactTypes.left().value()) {
			if (type.getName().equalsIgnoreCase(artifactInfo.getArtifactType())) {
				artifactInfo.setArtifactType(artifactInfo.getArtifactType().toUpperCase());
				artifactTypeExist = true;
				break;
			}
		}

		if (!artifactTypeExist) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeInvalidTypeError, "Artifact Upload / Delete / Update - Not supported artifact type", artifactInfo.getArtifactType(), "Artifact " + artifactInfo.getArtifactName());
			BeEcompErrorManager.getInstance().logBeInvalidTypeError("Artifact Upload / Delete / Update - Not supported artifact type", artifactInfo.getArtifactType(), "Artifact " + artifactInfo.getArtifactName());
			log.debug("Not supported artifact type = {}", artifactInfo.getArtifactType());
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED, artifactInfo.getArtifactType()));
		}

		return Either.left(ActionStatus.OK);
	}

	private Either<List<ArtifactType>, ActionStatus> getDeploymentArtifactTypes(String userId, ArtifactDefinition artifactInfo, NodeTypeEnum parentType) {

		Map<String, DeploymentArtifactTypeConfig> deploymentArtifacts = null;
		List<ArtifactType> artifactTypes = new ArrayList<ArtifactType>();

		if (parentType.equals(NodeTypeEnum.Service)) {
			deploymentArtifacts = ConfigurationManager.getConfigurationManager().getConfiguration().getServiceDeploymentArtifacts();
		} else if (parentType.equals(NodeTypeEnum.ResourceInstance)) {
			deploymentArtifacts = ConfigurationManager.getConfigurationManager().getConfiguration().getResourceInstanceDeploymentArtifacts();
		} else {
			deploymentArtifacts = ConfigurationManager.getConfigurationManager().getConfiguration().getResourceDeploymentArtifacts();
		}
		if (deploymentArtifacts != null) {
			for (String artifactType : deploymentArtifacts.keySet()) {
				ArtifactType artifactT = new ArtifactType();
				artifactT.setName(artifactType);
				artifactTypes.add(artifactT);
			}
			return Either.left(artifactTypes);
		} else {
			return Either.right(ActionStatus.GENERAL_ERROR);
		}

	}

	private Either<Boolean, ResponseFormat> validateFirstUpdateHasPayload(ArtifactDefinition artifactInfo, ArtifactDefinition currentArtifact) {
		if (currentArtifact.getEsId() == null && (artifactInfo.getPayloadData() == null || artifactInfo.getPayloadData().length == 0)) {
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.MISSING_DATA, ARTIFACT_PAYLOAD));
		}
		return Either.left(true);

	}

	private Either<Boolean, ResponseFormat> validateAndSetArtifactname(ArtifactDefinition artifactInfo) {
		if (artifactInfo.getArtifactName() == null || artifactInfo.getArtifactName().isEmpty()) {
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.MISSING_ARTIFACT_NAME));
		}

		String normalizeFileName = ValidationUtils.normalizeFileName(artifactInfo.getArtifactName());
		if (normalizeFileName == null || normalizeFileName.isEmpty()) {
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.MISSING_ARTIFACT_NAME));
		}
		artifactInfo.setArtifactName(normalizeFileName);

		if (!ValidationUtils.validateArtifactNameLength(artifactInfo.getArtifactName())) {
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.EXCEEDS_LIMIT, ARTIFACT_NAME, String.valueOf(ValidationUtils.ARTIFACT_NAME_LENGTH)));
		}

		return Either.left(true);
	}

	private Either<Boolean, ResponseFormat> validateArtifactTypeNotChanged(ArtifactDefinition artifactInfo, ArtifactDefinition currentArtifact) {
		if (artifactInfo.getArtifactType() == null || artifactInfo.getArtifactType().isEmpty()) {
			log.info("artifact type is missing operation ignored");
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.MISSING_ARTIFACT_TYPE));
		}

		if (!currentArtifact.getArtifactType().equalsIgnoreCase(artifactInfo.getArtifactType())) {
			log.info("artifact type cannot be changed operation ignored");
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
		}
		return Either.left(true);
	}

	private Either<ArtifactDefinition, ResponseFormat> validateOrSetArtifactGroupType(ArtifactDefinition artifactInfo, ArtifactDefinition currentArtifact) {
		if (artifactInfo.getArtifactGroupType() == null) {
			artifactInfo.setArtifactGroupType(currentArtifact.getArtifactGroupType());
		}

		else if (!currentArtifact.getArtifactGroupType().getType().equalsIgnoreCase(artifactInfo.getArtifactGroupType().getType())) {
			log.info("artifact group type cannot be changed. operation failed");
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
		}
		return Either.left(artifactInfo);
	}

	private void checkAndSetUnUpdatableFields(User user, ArtifactDefinition artifactInfo, ArtifactDefinition currentArtifact, ArtifactGroupTypeEnum type) {

		// on update if null add informational to current
		if (currentArtifact.getArtifactGroupType() == null && type != null) {
			currentArtifact.setArtifactGroupType(type);
		}

		if (artifactInfo.getUniqueId() != null && !currentArtifact.getUniqueId().equals(artifactInfo.getUniqueId())) {
			log.error("artifact uniqid cannot be set ignoring");
		}
		artifactInfo.setUniqueId(currentArtifact.getUniqueId());

		if (artifactInfo.getArtifactRef() != null && !currentArtifact.getArtifactRef().equals(artifactInfo.getArtifactRef())) {
			log.error("artifact ref cannot be set ignoring");
		}
		artifactInfo.setArtifactRef(currentArtifact.getArtifactRef());

		if (artifactInfo.getArtifactRepository() != null && !currentArtifact.getArtifactRepository().equals(artifactInfo.getArtifactRepository())) {
			log.error("artifact repository cannot be set ignoring");
		}
		artifactInfo.setArtifactRepository(currentArtifact.getArtifactRepository());

		if (artifactInfo.getUserIdCreator() != null && !currentArtifact.getUserIdCreator().equals(artifactInfo.getUserIdCreator())) {
			log.error("creator uuid cannot be set ignoring");
		}
		artifactInfo.setUserIdCreator(currentArtifact.getUserIdCreator());

		if (artifactInfo.getArtifactCreator() != null && !currentArtifact.getArtifactCreator().equals(artifactInfo.getArtifactCreator())) {
			log.error("artifact creator cannot be set ignoring");
		}
		artifactInfo.setArtifactCreator(currentArtifact.getArtifactCreator());

		if (artifactInfo.getUserIdLastUpdater() != null && !currentArtifact.getUserIdLastUpdater().equals(artifactInfo.getUserIdLastUpdater())) {
			log.error("userId of last updater cannot be set ignoring");
		}
		artifactInfo.setUserIdLastUpdater(user.getUserId());

		if (artifactInfo.getCreatorFullName() != null && !currentArtifact.getCreatorFullName().equals(artifactInfo.getCreatorFullName())) {
			log.error("creator Full name cannot be set ignoring");
		}
		artifactInfo.setCreatorFullName(currentArtifact.getCreatorFullName());

		if (artifactInfo.getUpdaterFullName() != null && !currentArtifact.getUpdaterFullName().equals(artifactInfo.getUpdaterFullName())) {
			log.error("updater Full name cannot be set ignoring");
		}
		String fullName = user.getFirstName() + " " + user.getLastName();
		artifactInfo.setUpdaterFullName(fullName);

		if (artifactInfo.getCreationDate() != null && !currentArtifact.getCreationDate().equals(artifactInfo.getCreationDate())) {
			log.error("Creation Date cannot be set ignoring");
		}
		artifactInfo.setCreationDate(currentArtifact.getCreationDate());

		if (artifactInfo.getLastUpdateDate() != null && !currentArtifact.getLastUpdateDate().equals(artifactInfo.getLastUpdateDate())) {
			log.error("Last Update Date cannot be set ignoring");
		}
		long time = System.currentTimeMillis();
		artifactInfo.setLastUpdateDate(time);

		if (artifactInfo.getEsId() != null && !currentArtifact.getEsId().equals(artifactInfo.getEsId())) {
			log.error("es id cannot be set ignoring");
		}
		artifactInfo.setEsId(currentArtifact.getUniqueId());

		if (artifactInfo.getArtifactDisplayName() != null && !currentArtifact.getArtifactDisplayName().equals(artifactInfo.getArtifactDisplayName())) {
			log.error(" Artifact Display Name cannot be set ignoring");
		}
		artifactInfo.setArtifactDisplayName(currentArtifact.getArtifactDisplayName());

		if (artifactInfo.getServiceApi() != null && !currentArtifact.getServiceApi().equals(artifactInfo.getServiceApi())) {
			log.debug("serviceApi cannot be set. ignoring.");
		}
		artifactInfo.setServiceApi(currentArtifact.getServiceApi());

		if (artifactInfo.getArtifactGroupType() != null && !currentArtifact.getArtifactGroupType().equals(artifactInfo.getArtifactGroupType())) {
			log.debug("artifact group cannot be set. ignoring.");
		}
		artifactInfo.setArtifactGroupType(currentArtifact.getArtifactGroupType());

		artifactInfo.setArtifactVersion(currentArtifact.getArtifactVersion());

		if (artifactInfo.getArtifactUUID() != null && !artifactInfo.getArtifactUUID().isEmpty() && !currentArtifact.getArtifactUUID().equals(artifactInfo.getArtifactUUID())) {
			log.debug("artifact UUID cannot be set. ignoring.");
		}
		artifactInfo.setArtifactUUID(currentArtifact.getArtifactUUID());

		if ((artifactInfo.getHeatParameters() != null) && (currentArtifact.getHeatParameters() != null) && !artifactInfo.getHeatParameters().isEmpty() && !currentArtifact.getHeatParameters().isEmpty()) {
			checkAndSetUnupdatableHeatParams(artifactInfo.getHeatParameters(), currentArtifact.getHeatParameters());
		}
	}

	private void checkAndSetUnupdatableHeatParams(List<HeatParameterDefinition> heatParameters, List<HeatParameterDefinition> currentParameters) {

		Map<String, HeatParameterDefinition> currentParametersMap = getMapOfParameters(currentParameters);
		for (HeatParameterDefinition parameter : heatParameters) {
			HeatParameterDefinition currentParam = currentParametersMap.get(parameter.getUniqueId());

			if (currentParam != null) {

				if (parameter.getName() != null && !parameter.getName().equalsIgnoreCase(currentParam.getName())) {
					log.debug("heat parameter name cannot be updated  ({}). ignoring.", parameter.getName());
					parameter.setName(currentParam.getName());
				}
				if (parameter.getDefaultValue() != null && !parameter.getDefaultValue().equalsIgnoreCase(currentParam.getDefaultValue())) {
					log.debug("heat parameter defaultValue cannot be updated  ({}). ignoring.", parameter.getDefaultValue());
					parameter.setDefaultValue(currentParam.getDefaultValue());
				}
				if (parameter.getType() != null && !parameter.getType().equalsIgnoreCase(currentParam.getType())) {
					log.debug("heat parameter type cannot be updated  ({}). ignoring.", parameter.getType());
					parameter.setType(currentParam.getType());
				}
				if (parameter.getDescription() != null && !parameter.getDescription().equalsIgnoreCase(currentParam.getDescription())) {
					log.debug("heat parameter description cannot be updated  ({}). ignoring.", parameter.getDescription());
					parameter.setDescription(currentParam.getDescription());
				}

				// check and set current value
				if ((parameter.getCurrentValue() == null) && (currentParam.getDefaultValue() != null)) {
					log.debug("heat parameter current value is null. set it to default value {}). ignoring.", parameter.getDefaultValue());
					parameter.setCurrentValue(currentParam.getDefaultValue());
				}
			}
		}
	}

	private Map<String, HeatParameterDefinition> getMapOfParameters(List<HeatParameterDefinition> currentParameters) {

		Map<String, HeatParameterDefinition> currentParamsMap = new HashMap<String, HeatParameterDefinition>();
		for (HeatParameterDefinition param : currentParameters) {
			currentParamsMap.put(param.getUniqueId(), param);
		}
		return currentParamsMap;
	}

	private Either<Boolean, ResponseFormat> validateAndServiceApiUrl(ArtifactDefinition artifactInfo) {
		if (!ValidationUtils.validateStringNotEmpty(artifactInfo.getApiUrl())) {
			log.debug("Artifact url cannot be empty.");
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.MISSING_DATA, ARTIFACT_URL));
		}
		artifactInfo.setApiUrl(artifactInfo.getApiUrl().toLowerCase());

		if (!ValidationUtils.validateUrl(artifactInfo.getApiUrl())) {
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_SERVICE_API_URL));
		}
		if (!ValidationUtils.validateUrlLength(artifactInfo.getApiUrl())) {
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.EXCEEDS_LIMIT, ARTIFACT_URL, String.valueOf(ValidationUtils.API_URL_LENGTH)));
		}

		return Either.left(true);
	}

	private Either<Boolean, ResponseFormat> validateAndCleanDescription(ArtifactDefinition artifactInfo) {
		if (artifactInfo.getDescription() == null || artifactInfo.getDescription().isEmpty()) {
			log.debug("Artifact description cannot be empty.");
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.MISSING_DATA, ARTIFACT_DESCRIPTION));
		}
		String description = artifactInfo.getDescription();
		description = ValidationUtils.removeNoneUtf8Chars(description);
		description = ValidationUtils.normaliseWhitespace(description);
		description = ValidationUtils.stripOctets(description);
		description = ValidationUtils.removeHtmlTagsOnly(description);
		if (!ValidationUtils.validateIsEnglish(description)) {
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
		}
		if (!ValidationUtils.validateLength(description, ValidationUtils.ARTIFACT_DESCRIPTION_MAX_LENGTH)) {
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.EXCEEDS_LIMIT, ARTIFACT_DESCRIPTION, String.valueOf(ValidationUtils.ARTIFACT_DESCRIPTION_MAX_LENGTH)));
		}
		artifactInfo.setDescription(description);
		return Either.left(true);
	}

	private Either<Either<ArtifactDefinition, Operation>, ResponseFormat> updateArtifactFlow(org.openecomp.sdc.be.model.Component parent, String parentId, String artifactId, ArtifactDefinition artifactInfo, User user, byte[] decodedPayload,
			ComponentTypeEnum componentType, AuditingActionEnum auditingAction, String interfaceType, String operationName) {
		ESArtifactData artifactData = createEsArtifactData(artifactInfo, decodedPayload);
		String prevArtifactId = null;
		String currArtifactId = artifactId;

		Either<Either<ArtifactDefinition, Operation>, ResponseFormat> resultOp = null;
		Either<ArtifactDefinition, Operation> insideEither = null;

		if (artifactData == null) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError, "Update Artifact");
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Update Artifact");
			log.debug("Failed to create artifact object for ES.");
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
			handleAuditing(auditingAction, parent, parentId, user, artifactInfo, prevArtifactId, currArtifactId, responseFormat, componentType, null);
			resultOp = Either.right(responseFormat);
			return resultOp;
		}
		log.debug("Try to update entry on graph");
		String artifactUniqueId = null;
		ArtifactDefinition artifactDefinition = null;
		StorageOperationStatus error = null;

		boolean isLeft = false;
		if (interfaceType != null && operationName != null) {
			// lifecycle artifact
			Operation operation = convertToOperation(artifactInfo, operationName);

			Either<Operation, StorageOperationStatus> result = interfaceLifecycleOperation.updateInterfaceOperation(parentId, interfaceType, operationName, operation);

			isLeft = result.isLeft();
			if (isLeft) {
				artifactUniqueId = result.left().value().getUniqueId();
				artifactDefinition = result.left().value().getImplementation();

				insideEither = Either.right(result.left().value());
				resultOp = Either.left(insideEither);
			} else {
				error = result.right().value();
			}
		} else {

			NodeTypeEnum convertParentType = convertParentType(componentType);
			Either<ArtifactDefinition, StorageOperationStatus> result = artifactOperation.updateArifactOnResource(artifactInfo, parentId, artifactId, convertParentType, true);
			isLeft = result.isLeft();
			if (isLeft) {
				artifactUniqueId = result.left().value().getUniqueId();
				artifactDefinition = result.left().value();

				insideEither = Either.left(result.left().value());
				resultOp = Either.left(insideEither);
			} else {
				error = result.right().value();
			}
		}
		if (isLeft) {
			log.debug("Enty on graph is updated. Update artifact in ES");
			boolean res;
			// Changing previous and current artifactId for auditing
			prevArtifactId = currArtifactId;
			currArtifactId = artifactDefinition.getUniqueId();

			if (!artifactDefinition.getUniqueId().equals(artifactId)) {
				// different ids ==> artifact node was cloned .
				// if no data in request get from ES
				if (decodedPayload == null) {
					if (!artifactDefinition.getMandatory() || artifactDefinition.getEsId() != null) {
						Either<ESArtifactData, CassandraOperationStatus> artifactFromCassandra = artifactCassandraDao.getArtifact(artifactId);
						// Either<ESArtifactData, ResourceUploadStatus>
						// artifactfromES = daoUploader.getArtifact(artifactId);
						if (artifactFromCassandra.isRight()) {
							log.debug("Failed to get artifact data from ES for artifact id  {}", artifactId);
							StorageOperationStatus storageStatus = DaoStatusConverter.convertCassandraStatusToStorageStatus(artifactFromCassandra.right().value());
							ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(storageStatus));
							handleAuditing(auditingAction, parent, parentId, user, artifactInfo, prevArtifactId, currArtifactId, responseFormat, componentType, null);
							resultOp = Either.right(responseFormat);
							return resultOp;
						}
						// clone data to new artifact
						artifactData.setData(artifactFromCassandra.left().value().getData());
					}
				}
				artifactData.setId(artifactDefinition.getUniqueId());
				// create new entry in ES
				res = true;
				if (artifactData.getData() != null) {
					res = saveArtifacts(artifactData, parentId, false);
					// set on graph object id of artifact in ES!
					if (res) {
						artifactInfo.setEsId(artifactData.getId());
						Either<ArtifactDefinition, StorageOperationStatus> updateArifactRes = artifactOperation.updateArifactDefinition(artifactInfo, true);
						if (updateArifactRes.isRight()) {
							log.debug("Failed to update artifact on graph  - {}", artifactId);
							ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(updateArifactRes.right().value()));
							handleAuditing(auditingAction, parent, parentId, user, artifactInfo, prevArtifactId, currArtifactId, responseFormat, componentType, null);
							resultOp = Either.right(responseFormat);
							return resultOp;

						} else {
							insideEither = Either.left(updateArifactRes.left().value());
							resultOp = Either.left(insideEither);
						}
					}
				}
			} else {
				res = true;
				if (artifactData.getData() != null) {
					// override artifact in ES
					res = saveArtifacts(artifactData, parentId, true);
					if (res && artifactDefinition.getMandatory()) {
						artifactInfo.setEsId(artifactData.getId());
						Either<ArtifactDefinition, StorageOperationStatus> updateArifactRes = artifactOperation.updateArifactDefinition(artifactInfo, true);
						if (updateArifactRes.isRight()) {
							log.debug("Failed to update artifact on graph  - {}", artifactId);
							ResponseFormat responseFormat = componentsUtils.getResponseFormatByArtifactId(componentsUtils.convertFromStorageResponse(updateArifactRes.right().value()), artifactId);
							handleAuditing(auditingAction, parent, parentId, user, artifactInfo, prevArtifactId, currArtifactId, responseFormat, componentType, null);
							resultOp = Either.right(responseFormat);
							return resultOp;

						}
					}
				}
			}
			if (res) {
				log.debug("Artifact saved into ES - {}", artifactUniqueId);
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
				handleAuditing(auditingAction, parent, parentId, user, artifactInfo, prevArtifactId, currArtifactId, responseFormat, componentType, null);
				// resultOp = Either.left(result.left().value());
				// return resultOp;
			} else {
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError, "Update Artifact");
				BeEcompErrorManager.getInstance().logBeDaoSystemError("Update Artifact");
				log.debug("Failed to save the artifact.");
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
				handleAuditing(auditingAction, parent, parentId, user, artifactInfo, prevArtifactId, currArtifactId, responseFormat, componentType, null);
				resultOp = Either.right(responseFormat);
				// return resultOp;
			}
		} else {
			log.debug("Failed to create entry on graph for artifact {}", artifactInfo.getArtifactDisplayName());
			ResponseFormat responseFormat = componentsUtils.getResponseFormatByArtifactId(componentsUtils.convertFromStorageResponse(error), artifactInfo.getArtifactDisplayName());
			handleAuditing(auditingAction, parent, parentId, user, artifactInfo, prevArtifactId, currArtifactId, responseFormat, componentType, null);
			resultOp = Either.right(responseFormat);
			// return resultOp;
		}

		return resultOp;
	}

	private Either<byte[], ResponseFormat> handlePayload(ArtifactDefinition artifactInfo, boolean isArtifactMetadataUpdate) {
		log.trace("Starting payload handling");
		byte[] payload = artifactInfo.getPayloadData();
		byte[] decodedPayload = null;

		if (payload != null && payload.length != 0) {

			decodedPayload = Base64.decodeBase64(payload);
			if (decodedPayload.length == 0) {
				log.debug("Failed to decode the payload.");
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT);
				return Either.right(responseFormat);
			}

			String checkSum = GeneralUtility.calculateMD5ByByteArray(decodedPayload);
			artifactInfo.setArtifactChecksum(checkSum);
			log.trace("Calculated checksum, base64 payload: {},  checksum: {}", payload, checkSum);

			// Specific payload validations of different types
			Either<Boolean, ResponseFormat> isValidPayload = Either.left(true);
			if (isDeploymentArtifact(artifactInfo)) {
				log.trace("Starting deployment artifacts payload validation");
				String artifactType = artifactInfo.getArtifactType();
				if (ArtifactTypeEnum.HEAT.getType().equalsIgnoreCase(artifactType) || ArtifactTypeEnum.HEAT_VOL.getType().equalsIgnoreCase(artifactType) || ArtifactTypeEnum.HEAT_NET.getType().equalsIgnoreCase(artifactType)
						|| ArtifactTypeEnum.HEAT_ENV.getType().equalsIgnoreCase(artifactType)) {
					isValidPayload = validateDeploymentHeatPayload(decodedPayload, artifactType);
					if (isValidPayload.isLeft()) {
						isValidPayload = extractHeatParameters(artifactInfo);
					}
				} else if (ArtifactTypeEnum.YANG_XML.getType().equalsIgnoreCase(artifactType) || ArtifactTypeEnum.VNF_CATALOG.getType().equalsIgnoreCase(artifactType) || ArtifactTypeEnum.VF_LICENSE.getType().equalsIgnoreCase(artifactType)
						|| ArtifactTypeEnum.VENDOR_LICENSE.getType().equalsIgnoreCase(artifactType) || ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType().equalsIgnoreCase(artifactType)
						|| ArtifactTypeEnum.MODEL_QUERY_SPEC.getType().equalsIgnoreCase(artifactType)) {
					isValidPayload = validateYangPayload(decodedPayload, artifactType);
					// else
					// if(ArtifactTypeEnum.APPC_CONFIG.getType().equalsIgnoreCase(artifactType)
					// || ){
				} else if (ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType().equalsIgnoreCase(artifactType) || ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType().equalsIgnoreCase(artifactType)) {
					String artifactFileName = artifactInfo.getArtifactName();
					String fileExtension = GeneralUtility.getFilenameExtension(artifactFileName).toLowerCase();
					switch (fileExtension) {
					case "xml":
						isValidPayload = validateYangPayload(decodedPayload, artifactType);
						break;
					case "json":
						isValidPayload = validateJsonPayload(decodedPayload, artifactType);
						break;
					case "yml":
					case "yaml":
						isValidPayload = validateYmlPayload(decodedPayload, artifactType);
						break;
					}
				}
			}
			if (isValidPayload.isRight()) {
				ResponseFormat responseFormat = isValidPayload.right().value();
				return Either.right(responseFormat);
			}

		} // null/empty payload is normal if called from metadata update ONLY.
			// The validation of whether this is metadata/payload update case is
			// currently done separately
		else {
			if (!isArtifactMetadataUpdate) {
				log.debug("Payload is missing.");
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_DATA, ARTIFACT_PAYLOAD);
				return Either.right(responseFormat);
			}
		}
		log.trace("Ended payload handling");
		return Either.left(decodedPayload);
	}

	private Either<Boolean, ResponseFormat> validateDeploymentHeatPayload(byte[] payload, String artifactType) {
		// Basic YAML validation
		YamlToObjectConverter yamlToObjectConverter = new YamlToObjectConverter();
		if (!yamlToObjectConverter.isValidYaml(payload)) {
			log.debug("Invalid YAML format");
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_YAML, artifactType);
			return Either.right(responseFormat);
		}
		if (!ArtifactTypeEnum.HEAT_ENV.getType().equalsIgnoreCase(artifactType)) {
			// HEAT specific YAML validation
			DeploymentArtifactHeatConfiguration heatConfiguration = yamlToObjectConverter.convert(payload, DeploymentArtifactHeatConfiguration.class);
			if (heatConfiguration == null || heatConfiguration.getHeat_template_version() == null || heatConfiguration.getResources() == null) {
				log.debug("HEAT doesn't contain required \"heat_template_version\" and \"resources\" sections ");
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_DEPLOYMENT_ARTIFACT_HEAT, artifactType);
				return Either.right(responseFormat);
			}
		}

		return Either.left(true);
	}

	private Either<Boolean, ResponseFormat> validateYmlPayload(byte[] decodedPayload, String artifactType) {
		Either<Boolean, ResponseFormat> res = Either.left(true);
		YamlToObjectConverter yamlToObjectConverter = new YamlToObjectConverter();
		if (!yamlToObjectConverter.isValidYaml(decodedPayload)) {
			log.debug("Invalid YAML format");
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_YAML, artifactType);
			res = Either.right(responseFormat);
		}

		return res;
	}

	private Either<Boolean, ResponseFormat> validateYangPayload(byte[] payload, String artifactType) {
		boolean isXmlValid = isValidXml(payload);
		if (!isXmlValid) {
			ResponseFormat responseFormat = ResponseFormatManager.getInstance().getResponseFormat(ActionStatus.INVALID_XML, artifactType);
			log.debug("Invalid XML content");
			return Either.right(responseFormat);
		}
		return Either.left(true);
	}

	private Either<Boolean, ResponseFormat> validateJsonPayload(byte[] payload, String type) {
		boolean isJsonValid = isValidJson(payload);
		if (!isJsonValid) {
			ResponseFormat responseFormat = ResponseFormatManager.getInstance().getResponseFormat(ActionStatus.INVALID_JSON, type);
			log.debug("Invalid JSON content");
			return Either.right(responseFormat);
		}
		return Either.left(true);
	}

	public void handleTransaction(Either<Operation, ResponseFormat> opState) {
		if (opState == null || opState.isRight()) {
			titanGenericDao.rollback();
		} else {
			titanGenericDao.commit();
		}
	}

	public Either<Operation, ResponseFormat> deleteArtifactByInterface(String resourceId, String interfaceType, String operationName, String userId, String artifactId, ImmutablePair<User, Resource> userResourceAuditPair, boolean shouldLock,
			boolean inTransaction) {
		User user = new User();
		user.setUserId(userId);
		Either<Resource, StorageOperationStatus> parent = resourceOperation.getResource(resourceId);
		if (parent.isRight()) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(parent.right().value()));
			return Either.right(responseFormat);
		}
		Either<Either<ArtifactDefinition, Operation>, ResponseFormat> handleDelete = handleDelete(resourceId, artifactId, user, AuditingActionEnum.ARTIFACT_DELETE, ComponentTypeEnum.RESOURCE, parent.left().value(), interfaceType, operationName,
				false, inTransaction);
		if (handleDelete.isRight()) {
			return Either.right(handleDelete.right().value());
		}
		Either<ArtifactDefinition, Operation> result = handleDelete.left().value();
		return Either.left(result.right().value());

	}

	public StorageOperationStatus deleteAllComponentArtifactsIfNotOnGraph(List<ArtifactDefinition> artifacts) {

		if (artifacts != null && !artifacts.isEmpty()) {
			for (ArtifactDefinition artifactDefinition : artifacts) {
				String esId = artifactDefinition.getEsId();
				if (esId != null && !esId.isEmpty()) {
					StorageOperationStatus deleteIfNotOnGraph = deleteIfNotOnGraph(artifactDefinition.getUniqueId(), esId, false);
					if (!deleteIfNotOnGraph.equals(StorageOperationStatus.OK)) {
						return deleteIfNotOnGraph;
					}
				}
			}
		}
		return StorageOperationStatus.OK;
	}

	private Operation convertToOperation(ArtifactDefinition artifactInfo, String operationName) {
		Operation op = new Operation();
		long time = System.currentTimeMillis();
		op.setCreationDate(time);

		String artifactName = artifactInfo.getArtifactName();
		artifactInfo.setArtifactName(createInterfaceArtifactNameFromOperation(operationName, artifactName));

		op.setImplementation(artifactInfo);
		op.setLastUpdateDate(time);
		return op;
	}

	private String createInterfaceArtifactNameFromOperation(String operationName, String artifactName) {
		String newArtifactName = operationName + "_" + artifactName;
		log.trace("converting artifact name {} to {}", artifactName, newArtifactName);
		return newArtifactName;
	}

	public StorageOperationStatus deleteIfNotOnGraph(String artifactId, String artifactEsId, boolean deleteOnlyPayload) {
		log.debug("deleteIfNotOnGraph: delete only payload = {}", deleteOnlyPayload);
		Either<ArtifactData, TitanOperationStatus> checkArtifactNode = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ArtifactRef), artifactId, ArtifactData.class);
		if ((artifactEsId != null && !artifactEsId.isEmpty())) {
			boolean isNotExistOnGraph = checkArtifactNode.isRight() && checkArtifactNode.right().value().equals(TitanOperationStatus.NOT_FOUND);

			if ((isNotExistOnGraph) || (checkArtifactNode.left().value().getArtifactDataDefinition().getMandatory() && deleteOnlyPayload)
					|| (ArtifactGroupTypeEnum.SERVICE_API.equals(checkArtifactNode.left().value().getArtifactDataDefinition().getArtifactGroupType()) && deleteOnlyPayload)) {
				// last one. need to delete in ES
				log.debug("Entry on graph is deleted. Delete artifact in ES for id = {}", artifactEsId);
				artifactCassandraDao.deleteArtifact(artifactEsId);
				return StorageOperationStatus.OK;
				// return
				// componentsUtils.getResponseFormatByResourceId(ActionStatus.OK,
				// resourceId);

			} else {
				log.debug("Entry on graph is deleted. Exist more connections on this artifact. Don't delete artifact in ES for id = {}", artifactEsId);
				return StorageOperationStatus.OK;
			}

		}
		return StorageOperationStatus.OK;
	}

	// download by MSO
	public Either<byte[], ResponseFormat> downloadRsrcArtifactByNames(String serviceName, String serviceVersion, String resourceName, String resourceVersion, String artifactName) {

		// General validation
		if (serviceName == null || serviceVersion == null || resourceName == null || resourceVersion == null || artifactName == null) {
			log.debug("One of the function parameteres is null");
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
		}

		// Normalizing artifact name
		artifactName = ValidationUtils.normalizeFileName(artifactName);

		// Resource validation
		Either<Resource, ResponseFormat> validateResourceNameAndVersion = validateResourceNameAndVersion(resourceName, resourceVersion);
		if (validateResourceNameAndVersion.isRight()) {
			return Either.right(validateResourceNameAndVersion.right().value());
		}

		Resource resource = validateResourceNameAndVersion.left().value();
		String resourceId = resource.getUniqueId();

		// Service validation
		Either<Service, ResponseFormat> validateServiceNameAndVersion = validateServiceNameAndVersion(serviceName, serviceVersion);
		if (validateServiceNameAndVersion.isRight()) {
			return Either.right(validateServiceNameAndVersion.right().value());
		}

		Map<String, ArtifactDefinition> artifacts = resource.getDeploymentArtifacts();
		if (artifacts == null || artifacts.isEmpty()) {
			log.debug("Deployment artifacts of resource {} are not found", resourceId);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, artifactName));
		}

		ArtifactDefinition deploymentArtifact = null;

		for (ArtifactDefinition artifactDefinition : artifacts.values()) {
			if (artifactDefinition.getArtifactName() != null && artifactDefinition.getArtifactName().equals(artifactName)) {
				log.debug("Found deployment artifact {}", artifactName);
				deploymentArtifact = artifactDefinition;
				break;
			}
		}

		if (deploymentArtifact == null) {
			log.debug("No deployment artifact {} was found for resource {}", artifactName, resourceId);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, artifactName));
		}

		// Downloading the artifact
		Either<ImmutablePair<String, byte[]>, ResponseFormat> downloadArtifactEither = downloadArtifact(deploymentArtifact);
		if (downloadArtifactEither.isRight()) {
			log.debug("Download artifact {} failed", artifactName);
			return Either.right(downloadArtifactEither.right().value());
		}
		log.trace("Download of resource artifact succeeded, uniqueId {}", deploymentArtifact.getUniqueId());
		return Either.left(downloadArtifactEither.left().value().getRight());
	}

	// download by MSO
	public Either<byte[], ResponseFormat> downloadRsrcInstArtifactByNames(String serviceName, String serviceVersion, String resourceInstanceName, String artifactName) {

		// General validation
		if (serviceName == null || serviceVersion == null || resourceInstanceName == null || artifactName == null) {
			log.debug("One of the function parameteres is null");
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
		}

		// Normalizing artifact name
		artifactName = ValidationUtils.normalizeFileName(artifactName);

		// Resource validation
		/*
		 * Either<Resource, ResponseFormat> validateResourceNameAndVersion = validateResourceNameAndVersion(resourceName, resourceVersion); if (validateResourceNameAndVersion.isRight()) { return
		 * Either.right(validateResourceNameAndVersion.right().value()); }
		 * 
		 * Resource resource = validateResourceNameAndVersion.left().value(); String resourceId = resource.getUniqueId();
		 */

		// Service validation
		Either<Service, ResponseFormat> validateServiceNameAndVersion = validateServiceNameAndVersion(serviceName, serviceVersion);
		if (validateServiceNameAndVersion.isRight()) {
			return Either.right(validateServiceNameAndVersion.right().value());
		}

		Service service = validateServiceNameAndVersion.left().value();

		// ResourceInstance validation
		Either<ComponentInstance, ResponseFormat> validateResourceInstance = validateResourceInstance(service, resourceInstanceName);
		if (validateResourceInstance.isRight()) {
			return Either.right(validateResourceInstance.right().value());
		}

		ComponentInstance resourceInstance = validateResourceInstance.left().value();

		Map<String, ArtifactDefinition> artifacts = resourceInstance.getDeploymentArtifacts();

		final String finalArtifactName = artifactName;
		Predicate<ArtifactDefinition> filterArtifactByName = p -> p.getArtifactName().equals(finalArtifactName);

		boolean hasDeploymentArtifacts = artifacts != null && artifacts.values().stream().anyMatch(filterArtifactByName);
		ArtifactDefinition deployableArtifact;

		if (!hasDeploymentArtifacts) {
			log.debug("Deployment artifact with name {} not found", artifactName);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, artifactName));
		}

		log.debug("Found deployment artifact {}", artifactName);
		deployableArtifact = artifacts.values().stream().filter(filterArtifactByName).findFirst().get();
		// Downloading the artifact
		Either<ImmutablePair<String, byte[]>, ResponseFormat> downloadArtifactEither = downloadArtifact(deployableArtifact);

		if (downloadArtifactEither.isRight()) {
			log.debug("Download artifact {} failed", artifactName);
			return Either.right(downloadArtifactEither.right().value());
		}
		log.trace("Download of resource artifact succeeded, uniqueId {}", deployableArtifact.getUniqueId());
		return Either.left(downloadArtifactEither.left().value().getRight());
	}

	private Either<ComponentInstance, ResponseFormat> validateResourceInstance(Service service, String resourceInstanceName) {

		List<ComponentInstance> riList = service.getComponentInstances();
		for (ComponentInstance ri : riList) {
			if (ri.getNormalizedName().equals(resourceInstanceName))
				return Either.left(ri);
		}

		return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_INSTANCE_NOT_FOUND, resourceInstanceName));
	}

	private Either<Service, ResponseFormat> validateServiceNameAndVersion(String serviceName, String serviceVersion) {

		Either<List<Service>, StorageOperationStatus> serviceListBySystemName = serviceOperation.getServiceListBySystemName(serviceName, false);
		if (serviceListBySystemName.isRight()) {
			log.debug("Couldn't fetch any service with name {}", serviceName);
			return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(serviceListBySystemName.right().value(), ComponentTypeEnum.SERVICE), serviceName));
		}
		List<Service> serviceList = serviceListBySystemName.left().value();
		if (serviceList == null || serviceList.isEmpty()) {
			log.debug("Couldn't fetch any service with name {}", serviceName);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.SERVICE_NOT_FOUND, serviceName));
		}

		Service foundService = null;
		for (Service service : serviceList) {
			if (service.getVersion().equals(serviceVersion)) {
				log.trace("Found service with version {}", serviceVersion);
				foundService = service;
				break;
			}
		}

		if (foundService == null) {
			log.debug("Couldn't find version {} for service {}", serviceVersion, serviceName);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_VERSION_NOT_FOUND, ComponentTypeEnum.SERVICE.getValue(), serviceVersion));
		}
		return Either.left(foundService);
	}

	private Either<Resource, ResponseFormat> validateResourceNameAndVersion(String resourceName, String resourceVersion) {
		Either<List<Resource>, StorageOperationStatus> resourceListBySystemName = resourceOperation.getResourceListBySystemName(resourceName, false);
		if (resourceListBySystemName.isRight()) {
			log.debug("Couldn't fetch any resource with name {}", resourceName);
			return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(resourceListBySystemName.right().value()), resourceName));
		}
		List<Resource> resourceList = resourceListBySystemName.left().value();
		if (resourceList == null || resourceList.isEmpty()) {
			log.debug("Couldn't fetch any resource with name {}", resourceName);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, resourceName));
		}

		Resource foundResource = null;
		for (Resource resource : resourceList) {
			if (resource.getVersion().equals(resourceVersion)) {
				log.trace("Found resource with version {}", resourceVersion);
				foundResource = resource;
				break;
			}
		}

		if (foundResource == null) {
			log.debug("Couldn't find version {} for resource {}", resourceVersion, resourceName);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_VERSION_NOT_FOUND, ComponentTypeEnum.RESOURCE.getValue(), resourceVersion));
		}
		return Either.left(foundResource);
	}

	public Either<byte[], ResponseFormat> downloadServiceArtifactByNames(String serviceName, String serviceVersion, String artifactName) {
		// Validation
		log.trace("Starting download of service interface artifact, serviceName {}, serviceVersion {}, artifact name {}", serviceName, serviceVersion, artifactName);
		if (serviceName == null || serviceVersion == null || artifactName == null) {
			log.debug("One of the function parameteres is null");
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
		}

		// Normalizing artifact name
		artifactName = ValidationUtils.normalizeFileName(artifactName);

		// Service validation
		Either<Service, ResponseFormat> validateServiceNameAndVersion = validateServiceNameAndVersion(serviceName, serviceVersion);
		if (validateServiceNameAndVersion.isRight()) {
			return Either.right(validateServiceNameAndVersion.right().value());
		}

		String serviceId = validateServiceNameAndVersion.left().value().getUniqueId();

		// Looking for deployment artifacts
		Service service = validateServiceNameAndVersion.left().value();
		Map<String, ArtifactDefinition> artifacts = service.getDeploymentArtifacts();
		if (artifacts == null || artifacts.isEmpty()) {
			log.debug("Deployment artifacts of service {} are not found", serviceId);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, artifactName));
		}

		ArtifactDefinition deploymentArtifact = null;

		for (ArtifactDefinition artifactDefinition : artifacts.values()) {
			if (artifactDefinition.getArtifactName().equals(artifactName)) {
				log.debug("Found deployment artifact {}", artifactName);
				deploymentArtifact = artifactDefinition;
			}
		}

		if (deploymentArtifact == null) {
			log.debug("No deployment artifact {} was found for service {}", artifactName, serviceId);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, artifactName));
		}

		// Downloading the artifact
		Either<ImmutablePair<String, byte[]>, ResponseFormat> downloadArtifactEither = downloadArtifact(deploymentArtifact);
		if (downloadArtifactEither.isRight()) {
			log.debug("Download artifact {} failed", artifactName);
			return Either.right(downloadArtifactEither.right().value());
		}
		log.trace("Download of service artifact succeeded, uniqueId {}", deploymentArtifact.getUniqueId());
		return Either.left(downloadArtifactEither.left().value().getRight());
	}

	public Either<ImmutablePair<String, byte[]>, ResponseFormat> downloadArtifact(String artifactUniqueId) {
		log.trace("Starting download of artifact, uniqueId {}", artifactUniqueId);
		Either<ArtifactDefinition, StorageOperationStatus> artifactById = artifactOperation.getArtifactById(artifactUniqueId, false);
		if (artifactById.isRight()) {
			ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(artifactById.right().value());
			log.debug("Error when getting artifact info by id{}, error: {}", artifactUniqueId, actionStatus.name());
			return Either.right(componentsUtils.getResponseFormatByArtifactId(actionStatus, ""));
		}
		ArtifactDefinition artifactDefinition = artifactById.left().value();
		if (artifactDefinition == null) {
			log.debug("Empty artifact definition returned from DB by artifact id {}", artifactUniqueId);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, ""));
		}

		return downloadArtifact(artifactDefinition);
	}

	private boolean checkArtifactInComponent(org.openecomp.sdc.be.model.Component component, String artifactId) {
		boolean found = false;
		Map<String, ArtifactDefinition> artifactsS = component.getArtifacts();
		if (artifactsS != null) {
			for (Map.Entry<String, ArtifactDefinition> entry : artifactsS.entrySet()) {
				if (entry.getValue().getUniqueId().equals(artifactId)) {
					found = true;
					break;
				}
			}
		}
		Map<String, ArtifactDefinition> deploymentArtifactsS = component.getDeploymentArtifacts();
		if (!found && deploymentArtifactsS != null) {
			for (Map.Entry<String, ArtifactDefinition> entry : deploymentArtifactsS.entrySet()) {
				if (entry.getValue().getUniqueId().equals(artifactId)) {
					found = true;
					break;
				}
			}
		}
		Map<String, ArtifactDefinition> toscaArtifactsS = component.getToscaArtifacts();
		if (!found && toscaArtifactsS != null) {
			for (Map.Entry<String, ArtifactDefinition> entry : toscaArtifactsS.entrySet()) {
				if (entry.getValue().getUniqueId().equals(artifactId)) {
					found = true;
					break;
				}
			}
		}
		switch (component.getComponentType()) {
		case RESOURCE:
			Map<String, InterfaceDefinition> interfaces = ((Resource) component).getInterfaces();
			if (!found && interfaces != null) {
				for (Map.Entry<String, InterfaceDefinition> entry : interfaces.entrySet()) {
					Map<String, Operation> operations = entry.getValue().getOperations();
					for (Map.Entry<String, Operation> entryOp : operations.entrySet()) {
						if (entryOp.getValue().getImplementation() != null && entryOp.getValue().getImplementation().getUniqueId().equals(artifactId)) {
							found = true;
							break;
						}
					}
				}
			}
			break;
		case SERVICE:
			Map<String, ArtifactDefinition> apiArtifacts = ((Service) component).getServiceApiArtifacts();
			if (!found && apiArtifacts != null) {
				for (Map.Entry<String, ArtifactDefinition> entry : apiArtifacts.entrySet()) {
					if (entry.getValue().getUniqueId().equals(artifactId)) {
						found = true;
						break;
					}
				}
			}
			break;
		default:

		}

		return found;
	}

	private boolean checkArtifactInResourceInstance(Component component, String resourceInstanceId, String artifactId) {

		boolean found = false;
		List<ComponentInstance> resourceInstances = component.getComponentInstances();
		ComponentInstance resourceInstance = null;
		for (ComponentInstance ri : resourceInstances) {
			if (ri.getUniqueId().equals(resourceInstanceId)) {
				resourceInstance = ri;
				break;
			}
		}
		if (resourceInstance != null) {
			Map<String, ArtifactDefinition> artifacts = resourceInstance.getDeploymentArtifacts();
			if (artifacts != null) {
				for (Map.Entry<String, ArtifactDefinition> entry : artifacts.entrySet()) {
					if (entry.getValue().getUniqueId().equals(artifactId)) {
						found = true;
						break;
					}
				}
			}
		}
		return found;
	}

	private Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponentExists(String componentId, String userId, AuditingActionEnum auditingAction, User user, String artifactId, ComponentTypeEnum componentType,
			String containerComponentType, boolean inTransaction) {

		NodeTypeEnum nodeType = componentType.getNodeType();
		if (containerComponentType != null && NodeTypeEnum.ResourceInstance == nodeType) {
			nodeType = (ComponentTypeEnum.findByParamName(containerComponentType)).getNodeType();
		}
		ComponentOperation componentOperation = getComponentOperation(nodeType);

		if (componentOperation == null) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
			log.debug("addArtifact - not supported component type {}", componentType);
			handleAuditing(auditingAction, null, componentId, user, null, null, artifactId, responseFormat, componentType, null);
			return Either.right(responseFormat);
		}
		Either<? extends org.openecomp.sdc.be.model.Component, StorageOperationStatus> componentResult = componentOperation.getComponent(componentId, inTransaction);

		if (componentResult.isRight()) {
			ActionStatus status = (componentType.equals(ComponentTypeEnum.RESOURCE)) ? ActionStatus.RESOURCE_NOT_FOUND : ActionStatus.SERVICE_NOT_FOUND;
			ComponentTypeEnum componentForAudit = (componentType.equals(ComponentTypeEnum.RESOURCE)) ? ComponentTypeEnum.RESOURCE : ComponentTypeEnum.SERVICE;

			ResponseFormat responseFormat = componentsUtils.getResponseFormat(status, componentId);
			log.debug("Service not found, serviceId {}", componentId);
			handleAuditing(auditingAction, null, componentId, user, null, null, artifactId, responseFormat, componentForAudit, null);
			return Either.right(responseFormat);
		}
		return Either.left(componentResult.left().value());
	}

	private Either<Boolean, ResponseFormat> validateWorkOnComponent(org.openecomp.sdc.be.model.Component component, String userId, AuditingActionEnum auditingAction, User user, String artifactId, ArtifactOperation operation,
			ComponentTypeEnum componentType) {
		if (operation != ArtifactOperation.Download) {
			Either<Boolean, ResponseFormat> canWork = validateCanWorkOnComponent(component, userId);
			if (canWork.isRight()) {
				String uniqueId = component.getUniqueId();
				log.debug("Service status isn't  CHECKOUT or user isn't owner, serviceId {}", uniqueId);
				handleAuditing(auditingAction, component, uniqueId, user, null, null, artifactId, canWork.right().value(), component.getComponentType(), null);
				return Either.right(canWork.right().value());
			}
		}
		return Either.left(true);
	}

	private Either<Boolean, ResponseFormat> validateWorkOnResource(Resource resource, String userId, AuditingActionEnum auditingAction, User user, String artifactId, ArtifactOperation operation) {
		if (operation != ArtifactOperation.Download) {
			if (!ComponentValidationUtils.canWorkOnResource(resource, userId)) {
				String uniqueId = resource.getUniqueId();
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
				log.debug("Resource status isn't  CHECKOUT or user isn't owner, resourceId {}", uniqueId);
				handleAuditing(auditingAction, resource, uniqueId, user, null, null, artifactId, responseFormat, ComponentTypeEnum.RESOURCE, null);
				return Either.right(responseFormat);
			}
		}
		return Either.left(true);
	}

	private Either<Boolean, ResponseFormat> validateUserRole(User user, AuditingActionEnum auditingAction, String componentId, String artifactId, ComponentTypeEnum componentType, ArtifactOperation operation) {

		if (operation != ArtifactOperation.Download) {
			String role = user.getRole();
			if (!role.equals(Role.ADMIN.name()) && !role.equals(Role.DESIGNER.name())) {
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
				log.debug("addArtifact - user isn't permitted to perform operation, userId {}, role {}", user.getUserId(), role);
				handleAuditing(auditingAction, null, componentId, user, null, null, artifactId, responseFormat, componentType, null);
				return Either.right(responseFormat);
			}
		}
		return Either.left(true);
	}

	private Either<User, ResponseFormat> validateUserExists(String userId, AuditingActionEnum auditingAction, String componentId, String artifactId, ComponentTypeEnum componentType) {

		return validateUserExists(userId, auditingAction, componentId, artifactId, componentType, false);

	}

	private Either<User, ResponseFormat> validateUserExists(String userId, AuditingActionEnum auditingAction, String componentId, String artifactId, ComponentTypeEnum componentType, boolean inTransaction) {
		Either<User, ResponseFormat> validateUserExists = validateUserExists(userId, auditingAction.getName(), inTransaction);

		if (validateUserExists.isRight()) {
			User user = new User();
			user.setUserId(userId);
			handleAuditing(auditingAction, null, componentId, user, null, null, artifactId, validateUserExists.right().value(), componentType, null);
			return Either.right(validateUserExists.right().value());
		}
		return Either.left(validateUserExists.left().value());
	}

	private AuditingActionEnum detectAuditingType(ArtifactOperation operation, String origMd5) {
		AuditingActionEnum auditingAction = null;
		switch (operation) {
		case Create:
			auditingAction = operation.isExternalApi() ? AuditingActionEnum.ARTIFACT_UPLOAD_BY_API : AuditingActionEnum.ARTIFACT_UPLOAD;
			break;
		case Update:
			auditingAction = operation.isExternalApi() ? AuditingActionEnum.ARTIFACT_UPLOAD_BY_API : origMd5 == null ? AuditingActionEnum.ARTIFACT_METADATA_UPDATE : AuditingActionEnum.ARTIFACT_PAYLOAD_UPDATE;
			break;
		case Delete:
			auditingAction = operation.isExternalApi() ? AuditingActionEnum.ARTIFACT_DELETE_BY_API : AuditingActionEnum.ARTIFACT_DELETE;
			break;
		case Download:
			auditingAction = operation.isExternalApi() ? AuditingActionEnum.DOWNLOAD_ARTIFACT : AuditingActionEnum.ARTIFACT_DOWNLOAD;
			break;
		default:
			break;
		}
		return auditingAction;
	}

	private Either<ImmutablePair<String, byte[]>, ResponseFormat> downloadArtifact(ArtifactDefinition artifactDefinition) {
		String esArtifactId = artifactDefinition.getEsId();
		Either<ESArtifactData, CassandraOperationStatus> artifactfromES = artifactCassandraDao.getArtifact(esArtifactId);
		if (artifactfromES.isRight()) {
			CassandraOperationStatus resourceUploadStatus = artifactfromES.right().value();
			StorageOperationStatus storageResponse = DaoStatusConverter.convertCassandraStatusToStorageStatus(resourceUploadStatus);
			ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(storageResponse);
			log.debug("Error when getting artifact from ES, error: {}", actionStatus.name());
			return Either.right(componentsUtils.getResponseFormatByArtifactId(actionStatus, artifactDefinition.getArtifactDisplayName()));
		}

		ESArtifactData esArtifactData = artifactfromES.left().value();
		byte[] data = esArtifactData.getDataAsArray();
		if (data == null) {
			log.debug("Artifact data from ES is null");
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, artifactDefinition.getArtifactDisplayName()));
		}
		String artifactName = artifactDefinition.getArtifactName();
		log.trace("Download of artifact succeeded, uniqueId {}, artifact file name {}", artifactDefinition.getUniqueId(), artifactName);
		return Either.left(new ImmutablePair<String, byte[]>(artifactName, data));
	}

	public ESArtifactData createEsArtifactData(ArtifactDataDefinition artifactInfo, byte[] artifactPayload) {
		ESArtifactData artifactData = new ESArtifactData(artifactInfo.getEsId(), artifactPayload);
		return artifactData;
	}

	private boolean saveArtifacts(ESArtifactData artifactData, String resourceId, boolean reload) {

		CassandraOperationStatus resourceUploadStatus = artifactCassandraDao.saveArtifact(artifactData);

		if (resourceUploadStatus.equals(CassandraOperationStatus.OK)) {
			log.debug("Artifact {} was saved in component {}.", artifactData.getId(), resourceId);
		} else {
			log.info("Failed to save artifact {}.", artifactData.getId());
			return false;
		}
		return true;
	}

	private boolean isArtifactMetadataUpdate(AuditingActionEnum auditingActionEnum) {
		return (auditingActionEnum.equals(AuditingActionEnum.ARTIFACT_METADATA_UPDATE));
	}

	private boolean isDeploymentArtifact(ArtifactDefinition artifactInfo) {
		return (ArtifactGroupTypeEnum.DEPLOYMENT.equals(artifactInfo.getArtifactGroupType()));
	}

	public IResourceOperation getResourceOperation() {
		return this.resourceOperation;
	}

	public Either<ArtifactDefinition, ResponseFormat> createArtifactPlaceHolderInfo(String resourceId, String logicalName, Map<String, Object> artifactInfoMap, String userId, ArtifactGroupTypeEnum groupType, boolean inTransaction) {
		Either<User, ActionStatus> user = userAdminManager.getUser(userId, inTransaction);
		if (user.isRight()) {
			ResponseFormat responseFormat;
			if (user.right().value().equals(ActionStatus.USER_NOT_FOUND)) {
				log.debug("create artifact placeholder - not authorized user, userId {}", userId);
				responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
			} else {
				log.debug("create artifact placeholder - failed to authorize user, userId {}", userId);
				responseFormat = componentsUtils.getResponseFormat(user.right().value());
			}
			return Either.right(responseFormat);
		}

		ArtifactDefinition artifactDefinition = createArtifactPlaceHolderInfo(resourceId, logicalName, artifactInfoMap, user.left().value(), groupType);
		return Either.left(artifactDefinition);
	}

	public ArtifactDefinition createArtifactPlaceHolderInfo(String resourceId, String logicalName, Map<String, Object> artifactInfoMap, User user, ArtifactGroupTypeEnum groupType) {
		ArtifactDefinition artifactInfo = new ArtifactDefinition();

		String artifactName = (String) artifactInfoMap.get(ARTIFACT_PLACEHOLDER_DISPLAY_NAME);
		String artifactType = (String) artifactInfoMap.get(ARTIFACT_PLACEHOLDER_TYPE);
		String artifactDescription = (String) artifactInfoMap.get(ARTIFACT_PLACEHOLDER_DESCRIPTION);

		artifactInfo.setArtifactDisplayName(artifactName);
		artifactInfo.setArtifactLabel(logicalName.toLowerCase());
		artifactInfo.setArtifactType(artifactType);
		artifactInfo.setDescription(artifactDescription);
		artifactInfo.setArtifactGroupType(groupType);
		setDefaultArtifactTimeout(groupType, artifactInfo);

		setArtifactPlaceholderCommonFields(resourceId, user, artifactInfo);

		return artifactInfo;
	}

	private void setDefaultArtifactTimeout(ArtifactGroupTypeEnum groupType, ArtifactDefinition artifactInfo) {
		if (groupType.equals(ArtifactGroupTypeEnum.DEPLOYMENT)) {
			artifactInfo.setTimeout(defaultHeatTimeout);
		} else {
			artifactInfo.setTimeout(NON_HEAT_TIMEOUT);
		}
	}

	private void setArtifactPlaceholderCommonFields(String resourceId, User user, ArtifactDefinition artifactInfo) {
		String uniqueId = null;

		if (resourceId != null) {
			uniqueId = UniqueIdBuilder.buildPropertyUniqueId(resourceId.toLowerCase(), artifactInfo.getArtifactLabel().toLowerCase());
			artifactInfo.setUniqueId(uniqueId);
		}
		artifactInfo.setUserIdCreator(user.getUserId());
		String fullName = user.getFullName();
		artifactInfo.setUpdaterFullName(fullName);

		long time = System.currentTimeMillis();

		artifactInfo.setCreatorFullName(fullName);
		artifactInfo.setCreationDate(time);

		artifactInfo.setLastUpdateDate(time);
		artifactInfo.setUserIdLastUpdater(user.getUserId());

		artifactInfo.setMandatory(true);
	}

	public Either<Map<String, ArtifactDefinition>, StorageOperationStatus> getArtifacts(String parentId, NodeTypeEnum parentType, boolean inTransaction, ArtifactGroupTypeEnum groupType) {
		return artifactOperation.getArtifacts(parentId, parentType, inTransaction, groupType.getType());
	}

	public Either<ArtifactDefinition, StorageOperationStatus> addHeatEnvArtifact(ArtifactDefinition artifactHeatEnv, ArtifactDefinition artifact, String parentId, NodeTypeEnum parentType, boolean inTransaction) {
		return artifactOperation.addHeatEnvArtifact(artifactHeatEnv, artifact, parentId, parentType, inTransaction);

	}

	private Either<ESArtifactData, ResponseFormat> createEsHeatEnvArtifactDataFromString(ArtifactDefinition artifactDefinition, String parameters) {
		StringBuilder sb = new StringBuilder();

		sb.append(ConfigurationManager.getConfigurationManager().getConfiguration().getHeatEnvArtifactHeader());
		sb.append(parameters);
		sb.append(ConfigurationManager.getConfigurationManager().getConfiguration().getHeatEnvArtifactFooter());
		byte[] payload = sb.toString().getBytes();

		YamlToObjectConverter yamlToObjectConverter = new YamlToObjectConverter();

		/*
		 * if (!yamlToObjectConverter.isValidYaml(payload)) { log.debug("Invalid YAML format"); ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_YAML, ArtifactTypeEnum.HEAT_ENV.getType()); return
		 * Either.right(responseFormat); }
		 */

		ESArtifactData artifactData = createEsArtifactData(artifactDefinition, payload);
		return Either.left(artifactData);
	}

	/**
	 * 
	 * @param artifactDefinition
	 * @return
	 */
	public Either<ArtifactDefinition, ResponseFormat> generateHeatEnvArtifact(ArtifactDefinition artifactDefinition, org.openecomp.sdc.be.model.Component component, String resourceInstanceName, User modifier, boolean shouldLock) {
		List<HeatParameterDefinition> heatParameters = artifactDefinition.getHeatParameters();
		heatParameters.sort(Comparator.comparing(e -> e.getName()));
		StringBuilder sb = new StringBuilder("parameters:\n");
		if (heatParameters != null) {

			for (HeatParameterDefinition heatParameterDefinition : heatParameters) {
				if (heatParameterDefinition.getCurrentValue() != null) {
					HeatParameterType type = HeatParameterType.isValidType(heatParameterDefinition.getType());
					if (type != null) {
						switch (type) {
						case BOOLEAN:
							sb.append("  ").append(heatParameterDefinition.getName()).append(":").append(" ").append(Boolean.parseBoolean(heatParameterDefinition.getCurrentValue())).append("\n");
							break;
						case NUMBER:
							// if
							// (ValidationUtils.isFloatNumber(heatParameterDefinition.getCurrentValue()))
							// {
							// sb.append("
							// ").append(heatParameterDefinition.getName()).append(":").append("
							// ").append(Float.parseFloat(heatParameterDefinition.getCurrentValue())).append("\n");
							// } else {
							// sb.append("
							// ").append(heatParameterDefinition.getName()).append(":").append("
							// ").append(Integer.parseInt(heatParameterDefinition.getCurrentValue())).append("\n");
							// }
							sb.append("  ").append(heatParameterDefinition.getName()).append(":").append(" ").append(new BigDecimal(heatParameterDefinition.getCurrentValue()).toPlainString()).append("\n");
							break;
						case COMMA_DELIMITED_LIST:
						case JSON:
							sb.append("  ").append(heatParameterDefinition.getName()).append(":").append(" ").append(heatParameterDefinition.getCurrentValue()).append("\n");
							break;
						default:
							String value = heatParameterDefinition.getCurrentValue();
							boolean starts = value.startsWith("\"");
							boolean ends = value.endsWith("\"");
							if (!(starts && ends)) {
								starts = value.startsWith("'");
								ends = value.endsWith("'");
								if (!(starts && ends)) {
									value = "\"" + value + "\"";
								}
							}
							sb.append("  ").append(heatParameterDefinition.getName()).append(":").append(" ").append(value);
							sb.append("\n");
							break;

						}
					}
				}
			}
		}
		return generateAndSaveHeatEnvArtifact(artifactDefinition, sb.toString(), component, resourceInstanceName, modifier, shouldLock);

	}

	/**
	 * 
	 * @param artifactDefinition
	 * @param payload
	 * @return
	 */
	public Either<ArtifactDefinition, ResponseFormat> generateAndSaveHeatEnvArtifact(ArtifactDefinition artifactDefinition, String payload, org.openecomp.sdc.be.model.Component component, String resourceInstanceName, User modifier,
			boolean shouldLock) {
		return generateArtifactPayload(artifactDefinition, component, resourceInstanceName, modifier, shouldLock, () -> artifactDefinition.getHeatParamsUpdateDate(), () -> createEsHeatEnvArtifactDataFromString(artifactDefinition, payload));

	}

	protected Either<ArtifactDefinition, ResponseFormat> generateArtifactPayload(ArtifactDefinition artifactDefinition, org.openecomp.sdc.be.model.Component component, String resourceInstanceName, User modifier, boolean shouldLock,
			Supplier<Long> payloadUpdateDateGen, Supplier<Either<ESArtifactData, ResponseFormat>> esDataCreator) {

		if (artifactDefinition.getPayloadUpdateDate() == null || artifactDefinition.getPayloadUpdateDate() == 0 || artifactDefinition.getPayloadUpdateDate() < payloadUpdateDateGen.get()) {

			log.trace("Generaing payload for {} artifact {}", artifactDefinition.getArtifactType(), artifactDefinition.getEsId());
			Either<ESArtifactData, ResponseFormat> artifactDataRes = esDataCreator.get();
			ESArtifactData artifactData = null;

			if (artifactDataRes.isLeft()) {
				artifactData = artifactDataRes.left().value();
			} else {
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
				handleAuditing(AuditingActionEnum.ARTIFACT_PAYLOAD_UPDATE, component, component.getUniqueId(), modifier, artifactDefinition, artifactDefinition.getUniqueId(), artifactDefinition.getUniqueId(), responseFormat,
						ComponentTypeEnum.RESOURCE_INSTANCE, resourceInstanceName);

				return Either.right(artifactDataRes.right().value());
			}
			String newCheckSum = GeneralUtility.calculateMD5ByByteArray(artifactData.getDataAsArray());
			String oldCheckSum;
			String esArtifactId = artifactDefinition.getEsId();
			Either<ESArtifactData, CassandraOperationStatus> artifactfromES;
			ESArtifactData esArtifactData;
			if (esArtifactId != null && !esArtifactId.isEmpty()) {
				artifactfromES = artifactCassandraDao.getArtifact(esArtifactId);
				if (artifactfromES.isRight()) {
					CassandraOperationStatus resourceUploadStatus = artifactfromES.right().value();
					StorageOperationStatus storageResponse = DaoStatusConverter.convertCassandraStatusToStorageStatus(resourceUploadStatus);
					ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(storageResponse);
					log.debug("Error when getting artifact from ES, error: {}", actionStatus.name());
					return Either.right(componentsUtils.getResponseFormatByArtifactId(actionStatus, artifactDefinition.getArtifactDisplayName()));
				}
				esArtifactData = artifactfromES.left().value();
				oldCheckSum = GeneralUtility.calculateMD5ByByteArray(esArtifactData.getDataAsArray());
			} else {
				oldCheckSum = artifactDefinition.getArtifactChecksum();
			}
			Either<ArtifactDefinition, StorageOperationStatus> updateArifactDefinitionStatus;

			if (shouldLock) {
				Either<Boolean, ResponseFormat> lockComponent = lockComponent(component, "Update Artifact - lock resource: ");
				if (lockComponent.isRight()) {
					handleAuditing(AuditingActionEnum.ARTIFACT_METADATA_UPDATE, component, component.getUniqueId(), modifier, null, null, artifactDefinition.getUniqueId(), lockComponent.right().value(), component.getComponentType(), null);
					return Either.right(lockComponent.right().value());
				}
			}
			try {
				if (oldCheckSum != null && oldCheckSum.equals(newCheckSum)) {

					artifactDefinition.setPayloadUpdateDate(payloadUpdateDateGen.get());
					updateArifactDefinitionStatus = artifactOperation.updateArifactDefinition(artifactDefinition, false);
					log.trace("No real update done in payload for {} artifact, updating payloadUpdateDate {}", artifactDefinition.getArtifactType(), artifactDefinition.getEsId());
					if (updateArifactDefinitionStatus.isRight()) {
						ResponseFormat responseFormat = componentsUtils.getResponseFormatByArtifactId(componentsUtils.convertFromStorageResponse(updateArifactDefinitionStatus.right().value()), artifactDefinition.getArtifactDisplayName());
						log.trace("Failed to update payloadUpdateDate ", artifactDefinition.getEsId());
						handleAuditing(AuditingActionEnum.ARTIFACT_PAYLOAD_UPDATE, component, component.getUniqueId(), modifier, artifactDefinition, artifactDefinition.getUniqueId(), artifactDefinition.getUniqueId(), responseFormat,
								ComponentTypeEnum.RESOURCE_INSTANCE, resourceInstanceName);

						return Either.right(responseFormat);
					}
				} else {

					oldCheckSum = artifactDefinition.getArtifactChecksum();
					artifactDefinition.setArtifactChecksum(newCheckSum);
					artifactOperation.updateUUID(artifactDefinition, oldCheckSum, artifactDefinition.getArtifactVersion());
					artifactDefinition.setEsId(artifactDefinition.getUniqueId());
					updateArifactDefinitionStatus = artifactOperation.updateArifactDefinition(artifactDefinition, true);

					log.trace("Update Payload  ", artifactDefinition.getEsId());

					if (updateArifactDefinitionStatus.isLeft()) {

						artifactData.setId(artifactDefinition.getUniqueId());
						CassandraOperationStatus saveArtifactStatus = artifactCassandraDao.saveArtifact(artifactData);

						if (saveArtifactStatus.equals(CassandraOperationStatus.OK)) {
							titanGenericDao.commit();
							log.debug("Artifact Saved In ES {}", artifactData.getId());
							ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
							handleAuditing(AuditingActionEnum.ARTIFACT_PAYLOAD_UPDATE, component, component.getUniqueId(), modifier, artifactDefinition, artifactDefinition.getUniqueId(), artifactDefinition.getUniqueId(), responseFormat,
									ComponentTypeEnum.RESOURCE_INSTANCE, resourceInstanceName);

						} else {
							titanGenericDao.rollback();
							log.info("Failed to save artifact {}.", artifactData.getId());
							ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
							handleAuditing(AuditingActionEnum.ARTIFACT_PAYLOAD_UPDATE, component, component.getUniqueId(), modifier, artifactDefinition, artifactDefinition.getUniqueId(), artifactDefinition.getUniqueId(), responseFormat,
									ComponentTypeEnum.RESOURCE_INSTANCE, resourceInstanceName);

							return Either.right(responseFormat);
						}
					} else {
						ResponseFormat responseFormat = componentsUtils.getResponseFormatByArtifactId(componentsUtils.convertFromStorageResponse(updateArifactDefinitionStatus.right().value()), artifactDefinition.getArtifactDisplayName());
						log.debug("Failed To update artifact {}", artifactData.getId());
						handleAuditing(AuditingActionEnum.ARTIFACT_PAYLOAD_UPDATE, component, component.getUniqueId(), modifier, artifactDefinition, artifactDefinition.getUniqueId(), artifactDefinition.getUniqueId(), responseFormat,
								ComponentTypeEnum.RESOURCE_INSTANCE, resourceInstanceName);

						return Either.right(responseFormat);

					}
				}
			} finally {
				if (shouldLock) {
					graphLockOperation.unlockComponent(component.getUniqueId(), component.getComponentType().getNodeType());
				}
			}
		}

		return Either.left(artifactDefinition);
	}

	private Either<Either<ArtifactDefinition, Operation>, ResponseFormat> handleUpdateHeatEnv(String componentId, ArtifactDefinition artifactInfo, AuditingActionEnum auditingAction, String artifactId, User user, ComponentTypeEnum componentType,
			org.openecomp.sdc.be.model.Component parent, String originData, String origMd5, ArtifactOperation operation, boolean shouldLock, boolean inTransaction) {
		NodeTypeEnum parentType = convertParentType(componentType);
		String parentId = parent.getUniqueId();
		Either<ArtifactDefinition, StorageOperationStatus> artifactRes = artifactOperation.getArtifactById(artifactId, false);
		ArtifactDefinition currArtifact = artifactRes.left().value();

		if (origMd5 != null) {
			Either<Boolean, ResponseFormat> validateMd5 = validateMd5(origMd5, originData, artifactInfo.getPayloadData(), operation);
			if (validateMd5.isRight()) {
				ResponseFormat responseFormat = validateMd5.right().value();
				handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, responseFormat, componentType, null);
				return Either.right(responseFormat);
			}

			if (artifactInfo.getPayloadData() != null && artifactInfo.getPayloadData().length != 0) {
				Either<Boolean, ResponseFormat> deploymentValidationResult = validateDeploymentArtifact(parent, componentId, user.getUserId(), false, artifactInfo, currArtifact, NodeTypeEnum.ResourceInstance);
				if (deploymentValidationResult.isRight()) {
					ResponseFormat responseFormat = deploymentValidationResult.right().value();
					handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, responseFormat, componentType, null);
					return Either.right(responseFormat);
				}

				Either<byte[], ResponseFormat> payloadEither = handlePayload(artifactInfo, isArtifactMetadataUpdate(auditingAction));
				if (payloadEither.isRight()) {
					ResponseFormat responseFormat = payloadEither.right().value();
					handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, responseFormat, componentType, null);
					return Either.right(responseFormat);
				}
			} else { // duplicate
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_DATA, ARTIFACT_PAYLOAD);
				handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, responseFormat, componentType, null);
				return Either.right(responseFormat);
			}
		}

		// lock resource
		if (shouldLock) {
			Either<Boolean, ResponseFormat> lockComponent = lockComponent(parent, "Update Artifact - lock ");
			if (lockComponent.isRight()) {
				handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, lockComponent.right().value(), componentType, null);
				return Either.right(lockComponent.right().value());
			}
		}
		Either<Either<ArtifactDefinition, Operation>, ResponseFormat> resultOp = null;
		try {
			resultOp = updateHeatEnvParams(componentId, artifactId, artifactInfo, user, auditingAction, parent, componentType, currArtifact, origMd5, inTransaction);
			return resultOp;

		} finally {
			// unlock resource
			if (resultOp == null || resultOp.isRight()) {
				log.debug("all changes rollback");
				if (false == inTransaction)
					titanGenericDao.rollback();
			} else {
				log.debug("all changes committed");
				if (false == inTransaction)
					titanGenericDao.commit();
			}
			if (shouldLock)
				componentType = parent.getComponentType();
			NodeTypeEnum nodeType = componentType.getNodeType();
			graphLockOperation.unlockComponent(parent.getUniqueId(), nodeType);
			// graphLockOperation.unlockComponent(parentId, parentType);
		}
	}

	private Either<Either<ArtifactDefinition, Operation>, ResponseFormat> updateHeatEnvParams(String componentId, String artifactId, ArtifactDefinition artifactInfo, User user, AuditingActionEnum auditingAction, Component parent,
			ComponentTypeEnum componentType, ArtifactDefinition currArtifact1, String origMd5, boolean inTransaction) {

		Either<Either<ArtifactDefinition, Operation>, ResponseFormat> resultOp = null;
		Either<ArtifactDefinition, Operation> insideEither = null;
		/*
		 * currently getArtifactById does not retrieve heatParameters Either<ArtifactDefinition, StorageOperationStatus> artifactRes = artifactOperation.getArtifactById(artifactId, false); ArtifactDefinition currArtifact = artifactRes.left().value();
		 */
		Either<ComponentInstance, ResponseFormat> getRI = getRIFromComponent(parent, componentId, artifactId, auditingAction, user);
		if (getRI.isRight()) {
			return Either.right(getRI.right().value());
		}
		ComponentInstance ri = getRI.left().value();
		Either<ArtifactDefinition, ResponseFormat> getArtifactRes = getArtifactFromRI(parent, ri, componentId, artifactId, auditingAction, user);
		if (getArtifactRes.isRight()) {
			return Either.right(getArtifactRes.right().value());
		}
		ArtifactDefinition currArtifact = getArtifactRes.left().value();

		if (currArtifact.getArtifactType().equals(ArtifactTypeEnum.HEAT.getType()) || currArtifact.getArtifactType().equals(ArtifactTypeEnum.HEAT_VOL.getType()) || currArtifact.getArtifactType().equals(ArtifactTypeEnum.HEAT_NET.getType())) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
			handleAuditing(auditingAction, parent, parent.getUniqueId(), user, artifactInfo, null, artifactId, responseFormat, componentType, ri.getName());
			return Either.right(responseFormat);
		}
		List<HeatParameterDefinition> currentHeatEnvParams = currArtifact.getHeatParameters();
		List<HeatParameterDefinition> updatedHeatEnvParams = artifactInfo.getHeatParameters();
		List<HeatParameterDefinition> reducedHeatEnvParams = new ArrayList<HeatParameterDefinition>();

		// upload
		if (origMd5 != null) {
			Either<List<HeatParameterDefinition>, ResponseFormat> uploadParamsValidationResult = validateUploadParamsFromEnvFile(auditingAction, parent, user, artifactInfo, artifactId, componentType, ri.getName(), currentHeatEnvParams,
					updatedHeatEnvParams, currArtifact.getArtifactName());
			if (uploadParamsValidationResult.isRight()) {
				ResponseFormat responseFormat = uploadParamsValidationResult.right().value();
				handleAuditing(auditingAction, parent, parent.getUniqueId(), user, artifactInfo, null, artifactId, responseFormat, componentType, ri.getName());
				return Either.right(responseFormat);
			}
			artifactInfo.setHeatParameters(updatedHeatEnvParams);
		}

		Either<ArtifactDefinition, ResponseFormat> validateAndConvertHeatParamers = validateAndConvertHeatParamers(artifactInfo, ArtifactTypeEnum.HEAT_ENV.getType());
		if (validateAndConvertHeatParamers.isRight()) {
			ResponseFormat responseFormat = validateAndConvertHeatParamers.right().value();
			handleAuditing(auditingAction, parent, parent.getUniqueId(), user, artifactInfo, null, artifactId, responseFormat, componentType, ri.getName());
			return Either.right(responseFormat);
		}

		if (updatedHeatEnvParams != null && !updatedHeatEnvParams.isEmpty()) {
			String paramName;
			// fill reduced heat env parameters List for updating
			for (HeatParameterDefinition heatEnvParam : updatedHeatEnvParams) {
				paramName = heatEnvParam.getName();
				for (HeatParameterDefinition currHeatParam : currentHeatEnvParams) {
					if (paramName.equalsIgnoreCase(currHeatParam.getName())) {
						String updatedParamValue = heatEnvParam.getCurrentValue();
						if (updatedParamValue != null && updatedParamValue.equals("")) { // reset
							heatEnvParam.setCurrentValue(heatEnvParam.getDefaultValue());
							reducedHeatEnvParams.add(heatEnvParam);
						} else if (updatedParamValue != null) {
							reducedHeatEnvParams.add(heatEnvParam);
						}
					}
				}
			}
			if (reducedHeatEnvParams.size() > 0) {
				ArtifactDefinition reducedArtifactInfo = new ArtifactDefinition(artifactInfo);
				reducedArtifactInfo.setHeatParameters(reducedHeatEnvParams);
				Either<ArtifactDefinition, StorageOperationStatus> updateArtifactResult = artifactOperation.updateArifactOnResource(reducedArtifactInfo, componentId, artifactInfo.getUniqueId(), componentType.getNodeType(), inTransaction);

				if (updateArtifactResult.isRight()) {
					log.debug("Failed to update artifact on graph  - {}", artifactId);
					ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(updateArtifactResult.right().value()));
					handleAuditing(auditingAction, parent, parent.getUniqueId(), user, artifactInfo, null, artifactId, responseFormat, componentType, ri.getName());
					return Either.right(responseFormat);
				}
				ArtifactDefinition updatedArtifact = updateArtifactResult.left().value();
				String updatedArtifactId = updatedArtifact.getUniqueId();
				if (!currArtifact.getUniqueId().equals(updatedArtifactId)) {
					currArtifact.setUniqueId(updatedArtifactId);
					currArtifact.setPayloadUpdateDate(updatedArtifact.getPayloadUpdateDate());
					currArtifact.setCreationDate(updatedArtifact.getCreationDate());
					currArtifact.setLastUpdateDate(updatedArtifact.getLastUpdateDate());
					currArtifact.setEsId(updatedArtifact.getEsId());
				}
				currArtifact.setHeatParamsUpdateDate(System.currentTimeMillis());

				Either<List<HeatParameterDefinition>, StorageOperationStatus> heatParamsForEnv = ((org.openecomp.sdc.be.model.operations.impl.ArtifactOperation) artifactOperation).getHeatParamsForEnv(currArtifact);
				if (heatParamsForEnv.isRight()) {
					log.debug("failed to get heat parameters values for heat artifact {}", updatedArtifact.getUniqueId());
					ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(heatParamsForEnv.right().value()));
					handleAuditing(auditingAction, parent, parent.getUniqueId(), user, artifactInfo, null, artifactId, responseFormat, componentType, ri.getName());
					return Either.right(responseFormat);
				}
				List<HeatParameterDefinition> updatedHeatParaetersList = heatParamsForEnv.left().value();
				currArtifact.setHeatParameters(updatedHeatParaetersList);

				Either<ArtifactDefinition, StorageOperationStatus> updateArifactRes = artifactOperation.updateArifactDefinition(currArtifact, true);
				if (updateArifactRes.isRight()) {
					log.debug("Failed to update artifact on graph  - {}", artifactId);
					ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(updateArifactRes.right().value()));
					handleAuditing(auditingAction, parent, parent.getUniqueId(), user, artifactInfo, null, artifactId, responseFormat, componentType, ri.getName());
					return Either.right(responseFormat);
				}
			}
		}

		insideEither = Either.left(currArtifact);
		resultOp = Either.left(insideEither);
		ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
		handleAuditing(auditingAction, parent, parent.getUniqueId(), user, currArtifact, null, artifactId, responseFormat, componentType, ri.getName());
		return resultOp;
	}

	public Either<List<HeatParameterDefinition>, ResponseFormat> validateUploadParamsFromEnvFile(AuditingActionEnum auditingAction, Component parent, User user, ArtifactDefinition artifactInfo, String artifactId, ComponentTypeEnum componentType,
			String riName, List<HeatParameterDefinition> currentHeatEnvParams, List<HeatParameterDefinition> updatedHeatEnvParams, String currArtifactName) {

		if (updatedHeatEnvParams == null || updatedHeatEnvParams.isEmpty()) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_DEPLOYMENT_ARTIFACT_HEAT, artifactInfo.getArtifactName(), currArtifactName);
			handleAuditing(auditingAction, parent, parent.getUniqueId(), user, artifactInfo, null, artifactId, responseFormat, componentType, riName);
			return Either.right(responseFormat);
		}

		for (HeatParameterDefinition uploadedHeatParam : updatedHeatEnvParams) {
			String paramName = uploadedHeatParam.getName();
			boolean isExistsInHeat = false;
			for (HeatParameterDefinition currHeatParam : currentHeatEnvParams) {
				if (paramName.equalsIgnoreCase(currHeatParam.getName())) {

					isExistsInHeat = true;
					uploadedHeatParam.setType(currHeatParam.getType());
					uploadedHeatParam.setCurrentValue(uploadedHeatParam.getDefaultValue());
					uploadedHeatParam.setDefaultValue(currHeatParam.getDefaultValue());
					uploadedHeatParam.setUniqueId(currHeatParam.getUniqueId());
					break;
				}
			}
			if (!isExistsInHeat) {
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISMATCH_HEAT_VS_HEAT_ENV, currArtifactName);
				handleAuditing(auditingAction, parent, parent.getUniqueId(), user, artifactInfo, null, artifactId, responseFormat, componentType, riName);
				return Either.right(responseFormat);
			}
		}
		return Either.left(updatedHeatEnvParams);
	}

	private Either<ComponentInstance, ResponseFormat> getRIFromComponent(Component component, String riID, String artifactId, AuditingActionEnum auditingAction, User user) {
		ResponseFormat responseFormat = null;
		List<ComponentInstance> ris = component.getComponentInstances();
		for (ComponentInstance ri : ris) {
			if (riID.equals(ri.getUniqueId())) {
				return Either.left(ri);
			}
		}
		responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESOURCE_INSTANCE_NOT_FOUND_ON_SERVICE, riID);
		log.debug("Resource Instance not found, resourceInstanceId {}", riID);
		handleAuditing(auditingAction, null, riID, user, null, null, artifactId, responseFormat, ComponentTypeEnum.RESOURCE_INSTANCE, null);
		return Either.right(responseFormat);
	}

	private Either<ArtifactDefinition, ResponseFormat> getArtifactFromRI(Component component, ComponentInstance ri, String riID, String artifactId, AuditingActionEnum auditingAction, User user) {
		ResponseFormat responseFormat = null;
		Map<String, ArtifactDefinition> rtifactsMap = ri.getDeploymentArtifacts();
		for (ArtifactDefinition artifact : rtifactsMap.values()) {
			if (artifactId.equals(artifact.getUniqueId())) {
				return Either.left(artifact);
			}
		}
		responseFormat = componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, riID, component.getUniqueId());
		handleAuditing(auditingAction, component, riID, user, null, null, artifactId, responseFormat, ComponentTypeEnum.RESOURCE_INSTANCE, ri.getName());
		return Either.right(responseFormat);
	}

	public ComponentOperation getComponentOperation(NodeTypeEnum componentType) {

		switch (componentType) {
		case Service:
		case ResourceInstance:
			return serviceOperation;
		case Resource:
			return resourceOperation;
		default:
			return null;
		}
	}

	public ArtifactDefinition extractArtifactDefinition(Either<ArtifactDefinition, Operation> eitherArtifact) {
		ArtifactDefinition ret;
		if (eitherArtifact.isLeft()) {
			ret = eitherArtifact.left().value();
		} else {
			ret = eitherArtifact.right().value().getImplementation();
		}
		return ret;
	}

	/**
	 * downloads artifact of component by UUIDs
	 * 
	 * @param componentType
	 * @param componentUuid
	 * @param artifactUUID
	 * @param auditAdditionalParam
	 * @return
	 */
	public Either<byte[], ResponseFormat> downloadComponentArtifactByUUIDs(ComponentTypeEnum componentType, String componentUuid, String artifactUUID, Map<AuditingFieldsKeysEnum, Object> auditAdditionalParam) {
		Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
		Either<byte[], ResponseFormat> result;
		byte[] downloadedArtifact = null;
		Component component = getLatestComponentByUuid(componentType, componentUuid, errorWrapper);
		if (errorWrapper.isEmpty()) {
			auditAdditionalParam.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, component.getName());
			downloadedArtifact = downloadArtifact(component.getDeploymentArtifacts(), artifactUUID, errorWrapper, component.getName());
		}
		if (errorWrapper.isEmpty()) {
			result = Either.left(downloadedArtifact);
		} else {
			result = Either.right(errorWrapper.getInnerElement());
		}
		return result;
	}

	/**
	 * downloads an artifact of resource instance of component by UUIDs
	 * 
	 * @param componentType
	 * @param componentUuid
	 * @param resourceName
	 * @param artifactUUID
	 * @param auditAdditionalParam
	 * @return
	 */
	public Either<byte[], ResponseFormat> downloadResourceInstanceArtifactByUUIDs(ComponentTypeEnum componentType, String componentUuid, String resourceInstanceName, String artifactUUID, Map<AuditingFieldsKeysEnum, Object> auditAdditionalParam) {
		Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
		Either<byte[], ResponseFormat> result;
		byte[] downloadedArtifact = null;
		ComponentInstance resourceInstance = getRelatedComponentInstance(componentType, componentUuid, resourceInstanceName, errorWrapper);
		if (errorWrapper.isEmpty()) {
			auditAdditionalParam.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceInstance.getName());
			downloadedArtifact = downloadArtifact(resourceInstance.getDeploymentArtifacts(), artifactUUID, errorWrapper, resourceInstance.getName());
		}
		if (errorWrapper.isEmpty()) {
			result = Either.left(downloadedArtifact);
		} else {
			result = Either.right(errorWrapper.getInnerElement());
		}
		return result;
	}

	/**
	 * uploads an artifact to a component by UUID
	 * 
	 * @param data
	 * @param request
	 * @param componentType
	 * @param componentUuid
	 * @param additionalParams
	 * @return
	 */
	public Either<ArtifactDefinition, ResponseFormat> uploadArtifactToComponentByUUID(String data, HttpServletRequest request, ComponentTypeEnum componentType, String componentUuid, Map<AuditingFieldsKeysEnum, Object> additionalParams) {
		Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
		Either<Either<ArtifactDefinition, Operation>, ResponseFormat> actionResult = null;
		Either<ArtifactDefinition, ResponseFormat> uploadArtifactResult;
		ArtifactDefinition uploadArtifact;
		Component component = null;
		String componentId = null;
		ArtifactOperation operation = ArtifactOperation.Create;
		operation.setExternalApi(true);
		ArtifactDefinition artifactInfo = RepresentationUtils.convertJsonToArtifactDefinition(data, ArtifactDefinition.class);
		String origMd5 = request.getHeader(Constants.MD5_HEADER);
		String userId = request.getHeader(Constants.USER_ID_HEADER);

		Either<ComponentMetadataData, StorageOperationStatus> getComponentRes = getComponentOperation(componentType).getLatestComponentMetadataByUuid(componentType.getNodeType(), componentUuid);
		if (getComponentRes.isRight()) {
			StorageOperationStatus status = getComponentRes.right().value();
			log.debug("Could not fetch component with type {} and uuid {}. Status is {}. ", componentType, componentUuid, status);
			errorWrapper.setInnerElement(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status)));
		}
		if (errorWrapper.isEmpty() && !getComponentRes.left().value().getMetadataDataDefinition().getState().equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
			component = checkoutParentComponent(componentType, getComponentRes.left().value().getMetadataDataDefinition().getUniqueId(), userId, errorWrapper);
		}
		if (errorWrapper.isEmpty()) {
			if (component != null) {
				componentId = component.getUniqueId();
			} else {
				componentId = getComponentRes.left().value().getMetadataDataDefinition().getUniqueId();
			}
		}
		if (errorWrapper.isEmpty()) {
			additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, getComponentRes.left().value().getMetadataDataDefinition().getName());
			actionResult = handleArtifactRequest(componentId, userId, componentType, operation, null, artifactInfo, origMd5, data, null, null, null, null);
			if (actionResult.isRight()) {
				log.debug("Failed to upload artifact to component with type {} and uuid {}. Status is {}. ", componentType, componentUuid, actionResult.right().value());
				errorWrapper.setInnerElement(actionResult.right().value());
			}
		}
		if (errorWrapper.isEmpty()) {
			uploadArtifact = actionResult.left().value().left().value();
			updateAuditParametersWithArtifactDefinition(additionalParams, uploadArtifact);
			uploadArtifactResult = Either.left(uploadArtifact);
		} else {
			uploadArtifactResult = Either.right(errorWrapper.getInnerElement());
		}
		return uploadArtifactResult;
	}

	/**
	 * upload an artifact to a resource instance by UUID
	 * 
	 * @param data
	 * @param request
	 * @param componentType
	 * @param componentUuid
	 * @param resourceInstanceName
	 * @param additionalParams
	 * @return
	 */
	public Either<ArtifactDefinition, ResponseFormat> uploadArtifactToRiByUUID(String data, HttpServletRequest request, ComponentTypeEnum componentType, String componentUuid, String resourceInstanceName,
			Map<AuditingFieldsKeysEnum, Object> additionalParams) {
		Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
		Either<ArtifactDefinition, ResponseFormat> uploadArtifactResult;
		Either<Either<ArtifactDefinition, Operation>, ResponseFormat> actionResult = null;
		ArtifactDefinition uploadArtifact;
		Component component = null;
		String componentInstanceId;
		String componentId;
		String origMd5 = request.getHeader(Constants.MD5_HEADER);
		String userId = request.getHeader(Constants.USER_ID_HEADER);

		ImmutablePair<Component, ComponentInstance> componentRiPair = null;
		Either<ComponentMetadataData, StorageOperationStatus> getComponentRes = getComponentOperation(componentType).getLatestComponentMetadataByUuid(componentType.getNodeType(), componentUuid);
		if (getComponentRes.isRight()) {
			StorageOperationStatus status = getComponentRes.right().value();
			log.debug("Could not fetch component with type {} and uuid {}. Status is {}. ", componentType, componentUuid, status);
			errorWrapper.setInnerElement(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status)));
		}
		if (errorWrapper.isEmpty() && !getComponentRes.left().value().getMetadataDataDefinition().getState().equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
			component = checkoutParentComponent(componentType, getComponentRes.left().value().getMetadataDataDefinition().getUniqueId(), userId, errorWrapper);
		}
		if (errorWrapper.isEmpty()) {
			if (component == null) {
				componentRiPair = getRelatedComponentComponentInstance(componentType, componentUuid, resourceInstanceName, errorWrapper);
			} else {
				componentRiPair = getRelatedComponentComponentInstance(component, resourceInstanceName, errorWrapper);
			}
		}
		if (errorWrapper.isEmpty()) {
			componentInstanceId = componentRiPair.getRight().getUniqueId();
			componentId = componentRiPair.getLeft().getUniqueId();
			additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceInstanceName);
			ArtifactOperation operation = ArtifactOperation.Create;
			operation.setExternalApi(true);
			ArtifactDefinition artifactInfo = RepresentationUtils.convertJsonToArtifactDefinition(data, ArtifactDefinition.class);

			actionResult = handleArtifactRequest(componentInstanceId, userId, ComponentTypeEnum.RESOURCE_INSTANCE, operation, null, artifactInfo, origMd5, data, null, null, componentId, ComponentTypeEnum.findParamByType(componentType));
			if (actionResult.isRight()) {
				log.debug("Failed to upload artifact to component instance {} of component with type {} and uuid {}. Status is {}. ", resourceInstanceName, componentType, componentUuid, actionResult.right().value());
				errorWrapper.setInnerElement(actionResult.right().value());
			}
		}
		if (errorWrapper.isEmpty()) {
			uploadArtifact = actionResult.left().value().left().value();
			updateAuditParametersWithArtifactDefinition(additionalParams, uploadArtifact);
			uploadArtifactResult = Either.left(uploadArtifact);
		} else {
			uploadArtifactResult = Either.right(errorWrapper.getInnerElement());
		}
		return uploadArtifactResult;
	}

	/**
	 * updates an artifact on a component by UUID
	 * 
	 * @param data
	 * @param request
	 * @param componentType
	 * @param componentUuid
	 * @param artifactUUID
	 * @param additionalParams
	 * @return
	 */
	public Either<ArtifactDefinition, ResponseFormat> updateArtifactOnComponentByUUID(String data, HttpServletRequest request, ComponentTypeEnum componentType, String componentUuid, String artifactUUID,
			Map<AuditingFieldsKeysEnum, Object> additionalParams) {
		Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
		Either<ArtifactDefinition, ResponseFormat> updateArtifactResult;
		Either<Either<ArtifactDefinition, Operation>, ResponseFormat> actionResult = null;
		ArtifactDefinition updateArtifact;
		Component component = null;
		String componentId = null;
		String artifactId = null;
		ArtifactOperation operation = ArtifactOperation.Update;
		operation.setExternalApi(true);
		ArtifactDefinition artifactInfo = RepresentationUtils.convertJsonToArtifactDefinition(data, ArtifactDefinition.class);
		String origMd5 = request.getHeader(Constants.MD5_HEADER);
		String userId = request.getHeader(Constants.USER_ID_HEADER);

		Either<ComponentMetadataData, StorageOperationStatus> getComponentRes = getComponentOperation(componentType).getLatestComponentMetadataByUuid(componentType.getNodeType(), componentUuid);
		if (getComponentRes.isRight()) {
			StorageOperationStatus status = getComponentRes.right().value();
			log.debug("Could not fetch component with type {} and uuid {}. Status is {}. ", componentType, componentUuid, status);
			errorWrapper.setInnerElement(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status)));
		}
		if (errorWrapper.isEmpty() && !getComponentRes.left().value().getMetadataDataDefinition().getState().equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
			component = checkoutParentComponent(componentType, getComponentRes.left().value().getMetadataDataDefinition().getUniqueId(), userId, errorWrapper);
		}
		if (errorWrapper.isEmpty()) {
			if (component != null) {
				componentId = component.getUniqueId();
			} else {
				componentId = getComponentRes.left().value().getMetadataDataDefinition().getUniqueId();
			}
		}
		if (errorWrapper.isEmpty()) {
			artifactId = getLatestParentArtifactDataIdByArtifactUUID(artifactUUID, errorWrapper, componentId, componentType);
		}
		if (errorWrapper.isEmpty()) {
			additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, getComponentRes.left().value().getMetadataDataDefinition().getName());

			actionResult = handleArtifactRequest(componentId, userId, componentType, operation, artifactId, artifactInfo, origMd5, data, null, null, null, null);
			if (actionResult.isRight()) {
				log.debug("Failed to upload artifact to component with type {} and uuid {}. Status is {}. ", componentType, componentUuid, actionResult.right().value());
				errorWrapper.setInnerElement(actionResult.right().value());
			}
		}
		if (errorWrapper.isEmpty()) {
			updateArtifact = actionResult.left().value().left().value();
			updateAuditParametersWithArtifactDefinition(additionalParams, updateArtifact);
			updateArtifactResult = Either.left(updateArtifact);

		} else {
			updateArtifactResult = Either.right(errorWrapper.getInnerElement());
		}
		return updateArtifactResult;
	}

	/**
	 * updates an artifact on a resource instance by UUID
	 * 
	 * @param data
	 * @param request
	 * @param componentType
	 * @param componentUuid
	 * @param resourceInstanceName
	 * @param artifactUUID
	 * @param additionalParams
	 * @return
	 */
	public Either<ArtifactDefinition, ResponseFormat> updateArtifactOnRiByUUID(String data, HttpServletRequest request, ComponentTypeEnum componentType, String componentUuid, String resourceInstanceName, String artifactUUID,
			Map<AuditingFieldsKeysEnum, Object> additionalParams) {

		Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
		Either<ArtifactDefinition, ResponseFormat> updateArtifactResult;
		Either<Either<ArtifactDefinition, Operation>, ResponseFormat> actionResult = null;
		ArtifactDefinition updateArtifact;
		Component component = null;
		String componentInstanceId = null;
		String componentId = null;
		String artifactId = null;
		String origMd5 = request.getHeader(Constants.MD5_HEADER);
		String userId = request.getHeader(Constants.USER_ID_HEADER);

		ImmutablePair<Component, ComponentInstance> componentRiPair = null;
		Either<ComponentMetadataData, StorageOperationStatus> getComponentRes = getComponentOperation(componentType).getLatestComponentMetadataByUuid(componentType.getNodeType(), componentUuid);
		if (getComponentRes.isRight()) {
			StorageOperationStatus status = getComponentRes.right().value();
			log.debug("Could not fetch component with type {} and uuid {}. Status is {}. ", componentType, componentUuid, status);
			errorWrapper.setInnerElement(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status)));
		}
		if (errorWrapper.isEmpty() && !getComponentRes.left().value().getMetadataDataDefinition().getState().equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
			component = checkoutParentComponent(componentType, getComponentRes.left().value().getMetadataDataDefinition().getUniqueId(), userId, errorWrapper);
		}
		if (errorWrapper.isEmpty()) {
			if (component == null) {
				componentRiPair = getRelatedComponentComponentInstance(componentType, componentUuid, resourceInstanceName, errorWrapper);
			} else {
				componentRiPair = getRelatedComponentComponentInstance(component, resourceInstanceName, errorWrapper);
			}
		}
		if (errorWrapper.isEmpty()) {
			componentInstanceId = componentRiPair.getRight().getUniqueId();
			componentId = componentRiPair.getLeft().getUniqueId();
			artifactId = getLatestParentArtifactDataIdByArtifactUUID(artifactUUID, errorWrapper, componentInstanceId, ComponentTypeEnum.RESOURCE_INSTANCE);
		}
		if (errorWrapper.isEmpty()) {
			additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceInstanceName);
			ArtifactOperation operation = ArtifactOperation.Update;
			operation.setExternalApi(true);
			ArtifactDefinition artifactInfo = RepresentationUtils.convertJsonToArtifactDefinition(data, ArtifactDefinition.class);

			actionResult = handleArtifactRequest(componentInstanceId, userId, ComponentTypeEnum.RESOURCE_INSTANCE, operation, artifactId, artifactInfo, origMd5, data, null, null, componentId, ComponentTypeEnum.findParamByType(componentType));
			if (actionResult.isRight()) {
				log.debug("Failed to upload artifact to component instance {} of component with type {} and uuid {}. Status is {}. ", resourceInstanceName, componentType, componentUuid, actionResult.right().value());
				errorWrapper.setInnerElement(actionResult.right().value());
			}
		}
		if (errorWrapper.isEmpty()) {
			updateArtifact = actionResult.left().value().left().value();
			updateAuditParametersWithArtifactDefinition(additionalParams, updateArtifact);
			updateArtifactResult = Either.left(updateArtifact);
		} else {
			updateArtifactResult = Either.right(errorWrapper.getInnerElement());
		}
		return updateArtifactResult;

	}

	/**
	 * deletes an artifact on a component by UUID
	 * 
	 * @param request
	 * @param componentType
	 * @param componentUuid
	 * @param artifactUUID
	 * @param additionalParams
	 * @return
	 */
	public Either<ArtifactDefinition, ResponseFormat> deleteArtifactOnComponentByUUID(HttpServletRequest request, ComponentTypeEnum componentType, String componentUuid, String artifactUUID, Map<AuditingFieldsKeysEnum, Object> additionalParams) {

		Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
		Either<ArtifactDefinition, ResponseFormat> deleteArtifactResult;
		Either<Either<ArtifactDefinition, Operation>, ResponseFormat> actionResult = null;
		ArtifactDefinition deleteArtifact;
		Component component = null;
		String componentId = null;
		String artifactId = null;
		ArtifactOperation operation = ArtifactOperation.Delete;
		operation.setExternalApi(true);
		String origMd5 = request.getHeader(Constants.MD5_HEADER);
		String userId = request.getHeader(Constants.USER_ID_HEADER);

		Either<ComponentMetadataData, StorageOperationStatus> getComponentRes = getComponentOperation(componentType).getLatestComponentMetadataByUuid(componentType.getNodeType(), componentUuid);
		if (getComponentRes.isRight()) {
			StorageOperationStatus status = getComponentRes.right().value();
			log.debug("Could not fetch component with type {} and uuid {}. Status is {}. ", componentType, componentUuid, status);
			errorWrapper.setInnerElement(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status)));
		}
		if (errorWrapper.isEmpty() && !getComponentRes.left().value().getMetadataDataDefinition().getState().equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
			component = checkoutParentComponent(componentType, getComponentRes.left().value().getMetadataDataDefinition().getUniqueId(), userId, errorWrapper);
		}
		if (errorWrapper.isEmpty()) {
			if (component != null) {
				componentId = component.getUniqueId();
			} else {
				componentId = getComponentRes.left().value().getMetadataDataDefinition().getUniqueId();
			}
		}
		if (errorWrapper.isEmpty()) {
			artifactId = getLatestParentArtifactDataIdByArtifactUUID(artifactUUID, errorWrapper, componentId, componentType);
		}
		if (errorWrapper.isEmpty()) {
			additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, getComponentRes.left().value().getMetadataDataDefinition().getName());

			actionResult = handleArtifactRequest(componentId, userId, componentType, operation, artifactId, null, origMd5, null, null, null, null, null);
			if (actionResult.isRight()) {
				log.debug("Failed to upload artifact to component with type {} and uuid {}. Status is {}. ", componentType, componentUuid, actionResult.right().value());
				errorWrapper.setInnerElement(actionResult.right().value());
			}
		}
		if (errorWrapper.isEmpty()) {
			deleteArtifact = actionResult.left().value().left().value();
			updateAuditParametersWithArtifactDefinition(additionalParams, deleteArtifact);
			deleteArtifactResult = Either.left(deleteArtifact);
		} else {
			deleteArtifactResult = Either.right(errorWrapper.getInnerElement());
		}
		return deleteArtifactResult;
	}

	/**
	 * deletes an artifact an a resource instance by UUID
	 * 
	 * @param request
	 * @param componentType
	 * @param componentUuid
	 * @param resourceInstanceName
	 * @param artifactUUID
	 * @param additionalParams
	 * @return
	 */
	public Either<ArtifactDefinition, ResponseFormat> deleteArtifactOnRiByUUID(HttpServletRequest request, ComponentTypeEnum componentType, String componentUuid, String resourceInstanceName, String artifactUUID,
			Map<AuditingFieldsKeysEnum, Object> additionalParams) {

		Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
		Either<ArtifactDefinition, ResponseFormat> deleteArtifactResult;
		Either<Either<ArtifactDefinition, Operation>, ResponseFormat> actionResult = null;
		ArtifactDefinition deleteArtifact;
		Component component = null;
		String componentInstanceId = null;
		String componentId = null;
		String artifactId = null;
		String origMd5 = request.getHeader(Constants.MD5_HEADER);
		String userId = request.getHeader(Constants.USER_ID_HEADER);
		ImmutablePair<Component, ComponentInstance> componentRiPair = null;
		Either<ComponentMetadataData, StorageOperationStatus> getComponentRes = getComponentOperation(componentType).getLatestComponentMetadataByUuid(componentType.getNodeType(), componentUuid);
		if (getComponentRes.isRight()) {
			StorageOperationStatus status = getComponentRes.right().value();
			log.debug("Could not fetch component with type {} and uuid {}. Status is {}. ", componentType, componentUuid, status);
			errorWrapper.setInnerElement(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status)));
		}
		if (errorWrapper.isEmpty() && !getComponentRes.left().value().getMetadataDataDefinition().getState().equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
			component = checkoutParentComponent(componentType, getComponentRes.left().value().getMetadataDataDefinition().getUniqueId(), userId, errorWrapper);
		}
		if (errorWrapper.isEmpty()) {
			if (component == null) {
				componentRiPair = getRelatedComponentComponentInstance(componentType, componentUuid, resourceInstanceName, errorWrapper);
			} else {
				componentRiPair = getRelatedComponentComponentInstance(component, resourceInstanceName, errorWrapper);
			}
		}
		if (errorWrapper.isEmpty()) {
			componentInstanceId = componentRiPair.getRight().getUniqueId();
			componentId = componentRiPair.getLeft().getUniqueId();
			artifactId = getLatestParentArtifactDataIdByArtifactUUID(artifactUUID, errorWrapper, componentInstanceId, ComponentTypeEnum.RESOURCE_INSTANCE);
		}
		if (errorWrapper.isEmpty()) {

			additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceInstanceName);
			ArtifactOperation operation = ArtifactOperation.Delete;
			operation.setExternalApi(true);

			actionResult = handleArtifactRequest(componentInstanceId, userId, ComponentTypeEnum.RESOURCE_INSTANCE, operation, artifactId, null, origMd5, null, null, null, componentId, ComponentTypeEnum.findParamByType(componentType));

			if (actionResult.isRight()) {
				log.debug("Failed to upload artifact to component instance {} of component with type {} and uuid {}. Status is {}. ", resourceInstanceName, componentType, componentUuid, actionResult.right().value());
				errorWrapper.setInnerElement(actionResult.right().value());
			}
		}
		if (errorWrapper.isEmpty()) {
			deleteArtifact = actionResult.left().value().left().value();
			updateAuditParametersWithArtifactDefinition(additionalParams, deleteArtifact);
			deleteArtifactResult = Either.left(deleteArtifact);
		} else {
			deleteArtifactResult = Either.right(errorWrapper.getInnerElement());
		}
		return deleteArtifactResult;
	}

	private ComponentInstance getRelatedComponentInstance(ComponentTypeEnum componentType, String componentUuid, String resourceInstanceName, Wrapper<ResponseFormat> errorWrapper) {
		ComponentInstance componentInstance = null;
		StorageOperationStatus status;
		Either<ComponentInstance, StorageOperationStatus> getResourceInstanceRes = null;
		Component component = getLatestComponentByUuid(componentType, componentUuid, errorWrapper);
		if (errorWrapper.isEmpty()) {
			componentInstance = component.getComponentInstances().stream().filter(ci -> ci.getNormalizedName().equals(resourceInstanceName)).findAny().get();
			if (componentInstance == null) {
				errorWrapper.setInnerElement(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER, resourceInstanceName, "resource instance", component.getComponentType().getValue(), component.getName()));
				log.debug("Component instance {} was not found for component {}", resourceInstanceName, component.getName());
			}
		}
		if (errorWrapper.isEmpty()) {
			getResourceInstanceRes = resourceInstanceOperation.getResourceInstanceById(componentInstance.getUniqueId());
			if (getResourceInstanceRes.isRight()) {
				status = getResourceInstanceRes.right().value();
				errorWrapper.setInnerElement(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status)));
			}
		}
		if (errorWrapper.isEmpty()) {
			componentInstance = getResourceInstanceRes.left().value();
			getResourceInstanceRes = resourceInstanceOperation.getFullComponentInstance(componentInstance, componentType.getNodeType());
			if (getResourceInstanceRes.isRight()) {
				status = getResourceInstanceRes.right().value();
				errorWrapper.setInnerElement(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status)));
			} else {
				componentInstance = getResourceInstanceRes.left().value();
			}
		}
		return componentInstance;
	}

	private ImmutablePair<Component, ComponentInstance> getRelatedComponentComponentInstance(Component component, String resourceInstanceName, Wrapper<ResponseFormat> errorWrapper) {

		ImmutablePair<Component, ComponentInstance> relatedComponentComponentInstancePair = null;
		ComponentInstance componentInstance = component.getComponentInstances().stream().filter(ci -> ci.getNormalizedName().equals(resourceInstanceName)).findAny().get();
		if (componentInstance == null) {
			errorWrapper.setInnerElement(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER, resourceInstanceName, "resource instance", component.getComponentType().getValue(), component.getName()));
			log.debug("Component instance {} was not found for component {}", resourceInstanceName, component.getName());
		} else {
			relatedComponentComponentInstancePair = new ImmutablePair<>(component, componentInstance);
		}
		return relatedComponentComponentInstancePair;
	}

	private ImmutablePair<Component, ComponentInstance> getRelatedComponentComponentInstance(ComponentTypeEnum componentType, String componentUuid, String resourceInstanceName, Wrapper<ResponseFormat> errorWrapper) {
		ComponentInstance componentInstance;
		ImmutablePair<Component, ComponentInstance> relatedComponentComponentInstancePair = null;
		Component component = getLatestComponentByUuid(componentType, componentUuid, errorWrapper);
		if (errorWrapper.isEmpty()) {
			componentInstance = component.getComponentInstances().stream().filter(ci -> ci.getNormalizedName().equals(resourceInstanceName)).findAny().get();
			if (componentInstance == null) {
				errorWrapper.setInnerElement(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER, resourceInstanceName, "resource instance", component.getComponentType().getValue(), component.getName()));
				log.debug("Component instance {} was not found for component {}", resourceInstanceName, component.getName());
			} else {
				relatedComponentComponentInstancePair = new ImmutablePair<>(component, componentInstance);
			}
		}
		return relatedComponentComponentInstancePair;
	}

	private byte[] downloadArtifact(Map<String, ArtifactDefinition> artifacts, String artifactUUID, Wrapper<ResponseFormat> errorWrapper, String componentName) {

		byte[] downloadedArtifact = null;
		Either<ImmutablePair<String, byte[]>, ResponseFormat> downloadArtifactEither = null;
		List<ArtifactDefinition> deploymentArtifacts = null;
		ArtifactDefinition deploymentArtifact = null;
		if (artifacts != null && !artifacts.isEmpty()) {
			deploymentArtifacts = artifacts.values().stream().filter(art -> art.getArtifactUUID() != null && art.getArtifactUUID().equals(artifactUUID)).collect(Collectors.toList());
		}
		if (deploymentArtifacts == null || deploymentArtifacts.isEmpty()) {
			log.debug("Deployment artifact with uuid {} was not found for component {}", artifactUUID, componentName);
			errorWrapper.setInnerElement(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, artifactUUID));
		}
		if (errorWrapper.isEmpty()) {
			deploymentArtifact = deploymentArtifacts.get(0);
			downloadArtifactEither = downloadArtifact(deploymentArtifact);
			if (downloadArtifactEither.isRight()) {
				log.debug("Failed to download artifact {}. ", deploymentArtifact.getArtifactName());
				errorWrapper.setInnerElement(downloadArtifactEither.right().value());
			}
		}
		if (errorWrapper.isEmpty()) {
			log.trace("Succeeded to download artifact with uniqueId {}", deploymentArtifact.getUniqueId());
			downloadedArtifact = downloadArtifactEither.left().value().getRight();
		}
		return downloadedArtifact;
	}

	private Component getLatestComponentByUuid(ComponentTypeEnum componentType, String componentUuid, Wrapper<ResponseFormat> errorWrapper) {
		Component component = null;
		Either<Component, StorageOperationStatus> getComponentRes = getComponentOperation(componentType).getLatestComponentByUuid(componentType.getNodeType(), componentUuid);
		if (getComponentRes.isRight()) {
			StorageOperationStatus status = getComponentRes.right().value();
			log.debug("Could not fetch component with type {} and uuid {}. Status is {}. ", componentType, componentUuid, status);
			errorWrapper.setInnerElement(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status)));
		} else {
			component = getComponentRes.left().value();
		}
		return component;
	}

	private String getLatestParentArtifactDataIdByArtifactUUID(String artifactUUID, Wrapper<ResponseFormat> errorWrapper, String parentId, ComponentTypeEnum componentType) {
		String artifactId = null;
		ActionStatus actionStatus = ActionStatus.ARTIFACT_NOT_FOUND;
		StorageOperationStatus storageStatus;
		ArtifactDefinition latestArtifact = null;
		List<ArtifactDefinition> artifacts = null;
		NodeTypeEnum parentType;
		if (componentType.equals(ComponentTypeEnum.RESOURCE)) {
			parentType = NodeTypeEnum.Resource;
		} else {
			parentType = NodeTypeEnum.Service;
		}
		Either<Map<String, ArtifactDefinition>, StorageOperationStatus> getArtifactsRes = artifactOperation.getArtifacts(parentId, parentType, false);
		if (getArtifactsRes.isRight()) {
			storageStatus = getArtifactsRes.right().value();
			log.debug("Couldn't fetch artifacts data for parent component {} with uid {}, error: {}", componentType.name(), parentId, storageStatus);
			if (!storageStatus.equals(StorageOperationStatus.NOT_FOUND)) {
				actionStatus = componentsUtils.convertFromStorageResponse(storageStatus);
			}
			errorWrapper.setInnerElement(componentsUtils.getResponseFormat(actionStatus, artifactUUID));
		}
		if (errorWrapper.isEmpty()) {
			artifacts = getArtifactsRes.left().value().values().stream().filter(a -> a.getArtifactUUID() != null && a.getArtifactUUID().equals(artifactUUID)).collect(Collectors.toList());
			if (artifacts == null || artifacts.isEmpty()) {
				log.debug("Couldn't fetch artifact with UUID {} data for parent component {} with uid {}, error: {}", artifactUUID, componentType.name(), parentId, actionStatus);
				errorWrapper.setInnerElement(componentsUtils.getResponseFormat(actionStatus, artifactUUID));
			}
		}
		if (errorWrapper.isEmpty()) {
			latestArtifact = artifacts.stream().max((a1, a2) -> {
				int compareRes = Double.compare(Double.parseDouble(a1.getArtifactVersion()), Double.parseDouble(a2.getArtifactVersion()));
				if (compareRes == 0) {
					compareRes = Long.compare(a1.getLastUpdateDate() == null ? 0 : a1.getLastUpdateDate(), a2.getLastUpdateDate() == null ? 0 : a2.getLastUpdateDate());
				}
				return compareRes;
			}).get();
			if (latestArtifact == null) {
				log.debug("Couldn't fetch latest artifact with UUID {} data for parent component {} with uid {}, error: {}", artifactUUID, componentType.name(), parentId, actionStatus);
				errorWrapper.setInnerElement(componentsUtils.getResponseFormat(actionStatus, artifactUUID));
			}
		}
		if (errorWrapper.isEmpty()) {
			artifactId = latestArtifact.getUniqueId();
		}
		return artifactId;
	}

	private Component checkoutParentComponent(ComponentTypeEnum componentType, String parentId, String userId, Wrapper<ResponseFormat> errorWrapper) {

		Component component = null;
		Either<User, ActionStatus> getUserRes = userBusinessLogic.getUser(userId, false);
		if (getUserRes.isRight()) {
			log.debug("Could not fetch User of component {} with uid {} to checked out. Status is {}. ", componentType.getNodeType(), parentId, getUserRes.right().value());
			errorWrapper.setInnerElement(componentsUtils.getResponseFormat(getUserRes.right().value()));
		}
		if (errorWrapper.isEmpty()) {
			User modifier = getUserRes.left().value();
			LifecycleChangeInfoWithAction changeInfo = new LifecycleChangeInfoWithAction("External API checkout", LifecycleChanceActionEnum.UPDATE_FROM_EXTERNAL_API);
			Either<? extends Component, ResponseFormat> checkoutRes = lifecycleBusinessLogic.changeComponentState(componentType, parentId, modifier, LifeCycleTransitionEnum.CHECKOUT, changeInfo, false, true);
			if (checkoutRes.isRight()) {
				log.debug("Could not change state of component {} with uid {} to checked out. Status is {}. ", componentType.getNodeType(), parentId, checkoutRes.right().value().getStatus());
				errorWrapper.setInnerElement(checkoutRes.right().value());
			} else {
				component = checkoutRes.left().value();
			}
		}
		return component;
	}

	private void updateAuditParametersWithArtifactDefinition(Map<AuditingFieldsKeysEnum, Object> additionalParams, ArtifactDefinition artifact) {
		additionalParams.put(AuditingFieldsKeysEnum.AUDIT_CURR_ARTIFACT_UUID, artifact.getArtifactUUID());
		additionalParams.put(AuditingFieldsKeysEnum.AUDIT_ARTIFACT_DATA, buildAuditingArtifactData(artifact));
		additionalParams.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_NAME, artifact.getUpdaterFullName());
	}
}
