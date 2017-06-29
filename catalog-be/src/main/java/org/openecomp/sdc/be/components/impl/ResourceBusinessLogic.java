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

import static org.openecomp.sdc.be.tosca.CsarUtils.ARTIFACTS_PATH;
import static org.openecomp.sdc.be.tosca.CsarUtils.VF_NODE_TYPE_ARTIFACTS_PATH_PATTERN;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationInfo;
import org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import org.openecomp.sdc.be.components.impl.ImportUtils.ToscaElementTypeEnum;
import org.openecomp.sdc.be.components.impl.ImportUtils.ToscaTagNamesEnum;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction.LifecycleChanceActionEnum;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.config.Configuration.VfModuleProperty;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datamodel.api.HighestFilterEnum;
import org.openecomp.sdc.be.datamodel.utils.ArtifactUtils;
import org.openecomp.sdc.be.datamodel.utils.UiComponentDataConverter;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.info.ArtifactTemplateInfo;
import org.openecomp.sdc.be.info.MergedArtifactInfo;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.ParsedToscaYamlInfo;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RelationshipImpl;
import org.openecomp.sdc.be.model.RequirementAndRelationshipPair;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.UploadCapInfo;
import org.openecomp.sdc.be.model.UploadComponentInstanceInfo;
import org.openecomp.sdc.be.model.UploadPropInfo;
import org.openecomp.sdc.be.model.UploadReqInfo;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.heat.HeatParameterType;
import org.openecomp.sdc.be.model.operations.api.ICacheMangerOperation;
import org.openecomp.sdc.be.model.operations.api.ICapabilityTypeOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IHeatParametersOperation;
import org.openecomp.sdc.be.model.operations.api.IInterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.IPropertyOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.CsarOperation;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.InputsOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.model.operations.utils.ComponentValidationUtils;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.servlets.RepresentationUtils;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.be.tosca.CsarUtils.NonMetaArtifactInfo;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.be.user.IUserBusinessLogic;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.be.utils.CommonBeUtils;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.common.datastructure.FunctionalInterfaces;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.kpi.api.ASDCKpiApi;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fj.data.Either;

@org.springframework.stereotype.Component("resourceBusinessLogic")
public class ResourceBusinessLogic extends ComponentBusinessLogic {

	private static final String PLACE_HOLDER_RESOURCE_TYPES = "validForResourceTypes";
	public static final String INITIAL_VERSION = "0.1";

	private Pattern STR_REPLACE_PATTERN = Pattern.compile("^[ ]*\\{[ ]*" + "str_replace" + "=");
	private Pattern TOKEN_PATTERN = Pattern.compile("[ ]*\\{[ ]*" + "token" + "=");
	private Pattern GET_PROPERTY_PATTERN = Pattern.compile("[ ]*\\{[ ]*" + "get_property" + "=");
	private Pattern CONCAT_PATTERN = Pattern.compile("[ ]*\\{[ ]*" + "concat" + "=");

	private static Logger log = LoggerFactory.getLogger(ResourceBusinessLogic.class.getName());

	private static Pattern pattern = Pattern.compile("\\..(.*?)\\..");

	/**
	 * Default constructor
	 */
	public ResourceBusinessLogic() {
		log.debug("ResourceBusinessLogic started");
	}

	@Autowired
	private ICapabilityTypeOperation capabilityTypeOperation = null;

	@Autowired
	private IInterfaceLifecycleOperation interfaceTypeOperation = null;

	@Autowired
	private LifecycleBusinessLogic lifecycleBusinessLogic;

	@Autowired
	private IPropertyOperation propertyOperation;

	@Autowired
	private CsarOperation csarOperation;

	@Autowired
	private VFComponentInstanceBusinessLogic vfComponentInstanceBusinessLogic;

	@Autowired
	private ResourceImportManager resourceImportManager;

	@Autowired
	private GroupBusinessLogic groupBusinessLogic;

	@Autowired
	private InputsBusinessLogic inputsBusinessLogic;

	@javax.annotation.Resource
	private InputsOperation inputOperation;

	// @Autowired
	// private GroupOperation groupOperation;

	@Autowired
	private IHeatParametersOperation heatParametersOperation;

	// @Autowired
	// private IArtifactOperation artifactOperation;

	@Autowired
	private CompositionBusinessLogic compositionBusinessLogic;

	@Autowired
	private ICacheMangerOperation cacheManagerOperation;

	@Autowired
	private ApplicationDataTypeCache dataTypeCache;

	private Gson gson = new Gson();

	public CsarOperation getCsarOperation() {
		return csarOperation;
	}

	public void setCsarOperation(CsarOperation csarOperation) {
		this.csarOperation = csarOperation;
	}

	public LifecycleBusinessLogic getLifecycleBusinessLogic() {
		return lifecycleBusinessLogic;
	}

	public void setLifecycleManager(LifecycleBusinessLogic lifecycleBusinessLogic) {
		this.lifecycleBusinessLogic = lifecycleBusinessLogic;
	}

	public IElementOperation getElementDao() {
		return elementDao;
	}

	public void setElementDao(IElementOperation elementDao) {
		this.elementDao = elementDao;
	}

	public IUserBusinessLogic getUserAdmin() {
		return this.userAdmin;
	}

	public void setUserAdmin(UserBusinessLogic userAdmin) {
		this.userAdmin = userAdmin;
	}

	public ComponentsUtils getComponentsUtils() {
		return this.componentsUtils;
	}

	public void setComponentsUtils(ComponentsUtils componentsUtils) {
		this.componentsUtils = componentsUtils;
	}

	public ArtifactsBusinessLogic getArtifactsManager() {
		return artifactsBusinessLogic;
	}

	public void setArtifactsManager(ArtifactsBusinessLogic artifactsManager) {
		this.artifactsBusinessLogic = artifactsManager;
	}

	public void setPropertyOperation(IPropertyOperation propertyOperation) {
		this.propertyOperation = propertyOperation;
	}

	public ApplicationDataTypeCache getApplicationDataTypeCache() {
		return applicationDataTypeCache;
	}

	public void setApplicationDataTypeCache(ApplicationDataTypeCache applicationDataTypeCache) {
		this.applicationDataTypeCache = applicationDataTypeCache;
	}

	/**
	 * the method returns a list of all the resources that are certified, the returned resources are only abstract or only none abstract according to the given param
	 *
	 * @param getAbstract
	 * @param userId
	 *            TODO
	 * @return
	 */
	public Either<List<Resource>, ResponseFormat> getAllCertifiedResources(boolean getAbstract, HighestFilterEnum highestFilter, String userId) {
		Either<User, ResponseFormat> resp = validateUserExists(userId, "get All Certified Resources", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}
		Boolean isHighest = null;
		switch (highestFilter) {
			case ALL:
				break;
			case HIGHEST_ONLY:
				isHighest = true;
				break;
			case NON_HIGHEST_ONLY:
				isHighest = false;
				break;
			default:
				break;
		}
		Either<List<Resource>, StorageOperationStatus> getResponse = toscaOperationFacade.getAllCertifiedResources(getAbstract, isHighest);

		if (getResponse.isRight()) {
			return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(getResponse.right().value())));
		}

		return Either.left(getResponse.left().value());
	}

	public Either<Map<String, Boolean>, ResponseFormat> validateResourceNameExists(String resourceName, ResourceTypeEnum resourceTypeEnum, String userId) {

		Either<User, ResponseFormat> resp = validateUserExists(userId, "validate Resource Name Exists", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

		Either<Boolean, StorageOperationStatus> dataModelResponse = toscaOperationFacade.validateComponentNameUniqueness(resourceName, resourceTypeEnum, ComponentTypeEnum.RESOURCE);
		// DE242223
		titanDao.commit();

		if (dataModelResponse.isLeft()) {
			Map<String, Boolean> result = new HashMap<>();
			result.put("isValid", dataModelResponse.left().value());
			log.debug("validation was successfully performed.");
			return Either.left(result);
		}

		ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(dataModelResponse.right().value()));

		return Either.right(responseFormat);
	}

	public Either<Resource, ResponseFormat> createResource(Resource resource, AuditingActionEnum auditingAction, User user, Map<String, byte[]> csarUIPayload, String payloadName) {
		Either<Resource, ResponseFormat> createResourceResponse = validateResourceBeforeCreate(resource, user, false);
		if (createResourceResponse.isRight()) {
			return createResourceResponse;
		}

		// Creating resource either by DAO or from CSAR
		String csarUUID = null;
		if (payloadName == null) {
			csarUUID = resource.getCsarUUID();
		} else {
			csarUUID = payloadName;
		}
		if (csarUUID != null && !csarUUID.isEmpty()) {
			// check if VF with the same Csar UUID or with he same name already
			// exists
			Either<Integer, StorageOperationStatus> validateCsarUuidUniquenessRes = toscaOperationFacade.validateCsarUuidUniqueness(csarUUID);
			if (validateCsarUuidUniquenessRes.isRight()) {
				log.debug("Failed to validate uniqueness of CsarUUID {} for resource", csarUUID, resource.getSystemName());
				return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(validateCsarUuidUniquenessRes.right().value())));
			}

			Integer existingResourceRes = validateCsarUuidUniquenessRes.left().value();
			if (existingResourceRes.intValue() > 0) {
				log.debug("Failed to create resource {}, csarUUID {} already exist for a different VF ", resource.getSystemName(), csarUUID);
				ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.VSP_ALREADY_EXISTS, csarUUID);
				componentsUtils.auditResource(errorResponse, user, resource, "", "", auditingAction, null);
				return Either.right(errorResponse);
			}

			log.debug("CsarUUID is {} - going to create resource from CSAR", csarUUID);
			createResourceResponse = createResourceFromCsar(resource, user, Either.left(csarUIPayload), csarUUID);
			return createResourceResponse;
		}

		return createResourceByDao(resource, user, auditingAction, false, false, null);
	}

	public Either<Resource, ResponseFormat> validateAndUpdateResourceFromCsar(Resource resource, User user, Map<String, byte[]> csarUIPayload, String payloadName, String resourceUniqueId) {
		Either<Resource, ResponseFormat> updateResourceResponse = null;
		Either<Resource, ResponseFormat> validateResourceResponse = null;
		Wrapper<ResponseFormat> responseWrapper = new Wrapper<ResponseFormat>();
		String csarUUID = null;
		String csarVersion = null;
		if (payloadName == null) {
			csarUUID = resource.getCsarUUID();
			csarVersion = resource.getCsarVersion();
		} else {
			csarUUID = payloadName;
		}
		if (csarUUID != null && !csarUUID.isEmpty()) {
			Resource oldResource = getResourceByUniqueId(responseWrapper, resourceUniqueId);
			if (responseWrapper.isEmpty()) {
				validateCsarUuidMatching(responseWrapper, oldResource, resource, csarUUID, resourceUniqueId, user);
			}
			if (responseWrapper.isEmpty()) {
				validateCsarIsNotAlreadyUsed(responseWrapper, oldResource, resource, csarUUID, user);
			}
			if (responseWrapper.isEmpty()) {
				if (oldResource != null && ValidationUtils.hasBeenCertified(oldResource.getVersion())) {
					overrideImmutableMetadata(oldResource, resource);
				}
				validateResourceResponse = validateResourceBeforeCreate(resource, user, false);
				if (validateResourceResponse.isRight()) {
					responseWrapper.setInnerElement(validateResourceResponse.right().value());
				}
			}
			if (responseWrapper.isEmpty()) {
				String oldCsarVersion = oldResource.getCsarVersion();
				log.debug("CsarUUID is {} - going to update resource with UniqueId {} from CSAR", csarUUID, resourceUniqueId);
				// (on boarding flow): If the update includes same csarUUID and
				// same csarVersion as already in the VF - no need to import the
				// csar (do only metadata changes if there are).
				if (csarVersion != null && oldCsarVersion != null && oldCsarVersion.equals(csarVersion)) {
					updateResourceResponse = updateResourceMetadata(resourceUniqueId, resource, oldResource, user, false);
				} else {
					updateResourceResponse = updateResourceFromCsar(oldResource, resource, user, AuditingActionEnum.UPDATE_RESOURCE_METADATA, false, Either.left(csarUIPayload), csarUUID);
				}
			}
		} else {
			log.debug("Failed to update resource {}, csarUUID or payload name is missing", resource.getSystemName());
			ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.MISSING_CSAR_UUID, resource.getName());
			componentsUtils.auditResource(errorResponse, user, resource, "", "", AuditingActionEnum.CREATE_RESOURCE, null);
			responseWrapper.setInnerElement(errorResponse);
		}
		if (responseWrapper.isEmpty()) {
			return updateResourceResponse;
		}
		return Either.right(responseWrapper.getInnerElement());
	}

	private void validateCsarIsNotAlreadyUsed(Wrapper<ResponseFormat> responseWrapper, Resource oldResource, Resource resource, String csarUUID, User user) {
		// (on boarding flow): If the update includes a csarUUID: verify this
		// csarUUID is not in use by another VF, If it is - use same error as
		// above:
		// "Error: The VSP with UUID %1 was already imported for VF %2. Please
		// select another or update the existing VF." %1 - csarUUID, %2 - VF
		// name
		Either<Resource, StorageOperationStatus> resourceLinkedToCsarRes = toscaOperationFacade.getLatestComponentByCsarOrName(ComponentTypeEnum.RESOURCE, csarUUID, resource.getSystemName());
		if (resourceLinkedToCsarRes.isRight()) {
			if (!StorageOperationStatus.NOT_FOUND.equals(resourceLinkedToCsarRes.right().value())) {
				log.debug("Failed to find previous resource by CSAR {} and system name {}", csarUUID, resource.getSystemName());
				responseWrapper.setInnerElement(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(resourceLinkedToCsarRes.right().value())));
			}
		} else if (!resourceLinkedToCsarRes.left().value().getUniqueId().equals(oldResource.getUniqueId()) && !resourceLinkedToCsarRes.left().value().getName().equals(oldResource.getName())) {
			ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.VSP_ALREADY_EXISTS, csarUUID, resourceLinkedToCsarRes.left().value().getName());
			componentsUtils.auditResource(errorResponse, user, resource, "", "", AuditingActionEnum.UPDATE_RESOURCE_METADATA, null);
			responseWrapper.setInnerElement(errorResponse);
		}
	}

	private void validateCsarUuidMatching(Wrapper<ResponseFormat> responseWrapper, Resource resource, Resource oldResource, String csarUUID, String resourceUniqueId, User user) {
		// (on boarding flow): If the update includes csarUUID which is
		// different from the csarUUID of the VF - fail with
		// error: "Error: Resource %1 cannot be updated using since it is linked
		// to a different VSP" %1 - VF name
		String oldCsarUUID = oldResource.getCsarUUID();
		if (oldCsarUUID != null && !oldCsarUUID.isEmpty() && !csarUUID.equals(oldCsarUUID)) {
			log.debug("Failed to update resource with UniqueId {} using Csar {}, since the resource is linked to a different VSP {}", resourceUniqueId, csarUUID, oldCsarUUID);
			ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.RESOURCE_LINKED_TO_DIFFERENT_VSP, resource.getName(), csarUUID, oldCsarUUID);
			componentsUtils.auditResource(errorResponse, user, resource, "", "", AuditingActionEnum.UPDATE_RESOURCE_METADATA, null);
			responseWrapper.setInnerElement(errorResponse);
		}
	}

	private Resource getResourceByUniqueId(Wrapper<ResponseFormat> responseWrapper, String resourceUniqueId) {
		Either<Resource, StorageOperationStatus> oldResourceRes = toscaOperationFacade.getToscaElement(resourceUniqueId);
		if (oldResourceRes.isRight()) {
			log.debug("Failed to find previous resource by UniqueId {}, status: {}", resourceUniqueId, oldResourceRes.right().value());
			responseWrapper.setInnerElement(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(oldResourceRes.right().value())));
			return null;
		}
		return oldResourceRes.left().value();
	}

	private void overrideImmutableMetadata(Resource oldRresource, Resource resource) {
		resource.setName(oldRresource.getName());
		resource.setIcon(oldRresource.getIcon());
		resource.setTags(oldRresource.getTags());
		resource.setVendorName(oldRresource.getVendorName());
		resource.setCategories(oldRresource.getCategories());
		resource.setDerivedFrom(oldRresource.getDerivedFrom());
	}

	private Either<Resource, ResponseFormat> updateResourceFromCsar(Resource oldRresource, Resource newRresource, User user, AuditingActionEnum updateResource, boolean inTransaction, Either<Map<String, byte[]>, StorageOperationStatus> csarUIPayload,
																	String csarUUID) {

		// check state
		if (LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.equals(oldRresource.getLifecycleState())) {
			if (!oldRresource.getLastUpdaterUserId().equals(user.getUserId())) {
				log.debug("Current user is not last updater, last updater userId: {}, current user userId: {}", oldRresource.getLastUpdaterUserId(), user.getUserId());
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
			}
		}
		String lockedResourceId = oldRresource.getUniqueId();
		List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
		List<ArtifactDefinition> nodeTypesNewCreatedArtifacts = new ArrayList<>();

		Either<Map<String, byte[]>, StorageOperationStatus> csar = null;
		if (csarUIPayload != null && csarUIPayload.left() != null && csarUIPayload.left().value() != null) {
			csar = csarUIPayload;
		} else {
			csar = csarOperation.getCsar(csarUUID, user);
		}
		if (csar.isRight()) {
			log.debug("Failed to get csar for casrUUID{} ", csarUUID);
			return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(csar.right().value())));
		}

		Either<ImmutablePair<String, String>, ResponseFormat> toscaYamlCsarStatus = validateAndParseCsar(newRresource, user, csarUUID, csar);
		if (toscaYamlCsarStatus.isRight()) {
			return Either.right(toscaYamlCsarStatus.right().value());
		}
		Either<String, ResponseFormat> checksum = CsarValidationUtils.getToscaYamlChecksum(csar.left().value(), csarUUID, componentsUtils);
		if (checksum.isRight()) {
			log.debug("Failed to calculate checksum for casrUUID{} error {} ", csarUUID, checksum.right().value());
			return Either.right(checksum.right().value());
		}
		boolean isUpdateYaml = true;
		if (checksum.left().value().equals(oldRresource.getComponentMetadataDefinition().getMetadataDataDefinition().getImportedToscaChecksum())) {
			log.debug("The checksums are equals for csarUUID {}, existing checsum is {}, new one is {} ", csarUUID, oldRresource.getComponentMetadataDefinition().getMetadataDataDefinition().getImportedToscaChecksum(), checksum.left().value());
			if (oldRresource.getLifecycleState().equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT))
				isUpdateYaml = false;
		} else {
			oldRresource.getComponentMetadataDefinition().getMetadataDataDefinition().setImportedToscaChecksum(checksum.left().value());
		}

		Either<Boolean, ResponseFormat> lockResult = lockComponent(lockedResourceId, oldRresource, "update Resource From Csar");
		if (lockResult.isRight()) {
			return Either.right(lockResult.right().value());
		}

		Either<Resource, ResponseFormat> result = null;
		String yamlFileName = toscaYamlCsarStatus.left().value().getKey();
		Either<Map<String, Resource>, ResponseFormat> parseNodeTypeInfoYamlEither = null;
		try {
			Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> prepareForUpdate = null;
			Resource preparedResource = null;
			Either<ParsedToscaYamlInfo, ResponseFormat> uploadComponentInstanceInfoMap = parseResourceInfoFromYaml(yamlFileName, newRresource, toscaYamlCsarStatus.left().value().getValue(), user);
			if (uploadComponentInstanceInfoMap.isRight()) {
				ResponseFormat responseFormat = uploadComponentInstanceInfoMap.right().value();
				componentsUtils.auditResource(responseFormat, user, newRresource, "", "", updateResource, null);
				result = Either.right(responseFormat);
				return result;
			}
			Map<String, UploadComponentInstanceInfo> instances = uploadComponentInstanceInfoMap.left().value().getInstances();
			Either<Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>>, ResponseFormat> findNodeTypesArtifactsToHandleRes = findNodeTypesArtifactsToHandle(csar.left().value(), csarUUID, yamlFileName, oldRresource, user, true, instances);
			if (findNodeTypesArtifactsToHandleRes.isRight()) {
				log.debug("failed to find node types for update with artifacts during import csar {}. ", csarUUID);
				result = Either.right(findNodeTypesArtifactsToHandleRes.right().value());
				return result;
			}
			Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = findNodeTypesArtifactsToHandleRes.left().value();
			if (isUpdateYaml || !nodeTypesArtifactsToHandle.isEmpty()) {

				prepareForUpdate = updateExistingResourceByImport(newRresource, oldRresource, user, true, false);
				if (prepareForUpdate.isRight()) {
					log.debug("Failed to prepare resource for update : {}", prepareForUpdate.right().value());
					result = Either.right(prepareForUpdate.right().value());
					return result;
				}
				preparedResource = prepareForUpdate.left().value().left;

				String yamlFileContents = toscaYamlCsarStatus.left().value().getValue();
				log.trace("YAML topology file found in CSAR, file name: {}, contents: {}", yamlFileName, yamlFileContents);

				parseNodeTypeInfoYamlEither = this.handleNodeTypes(yamlFileName, preparedResource, user, yamlFileContents, csar.left().value(), false, nodeTypesArtifactsToHandle, nodeTypesNewCreatedArtifacts);
				if (parseNodeTypeInfoYamlEither.isRight()) {
					ResponseFormat responseFormat = parseNodeTypeInfoYamlEither.right().value();
					componentsUtils.auditResource(responseFormat, user, preparedResource, "", "", updateResource, null);
					result = Either.right(responseFormat);
					return result;
				}

				Map<String, InputDefinition> inputs = uploadComponentInstanceInfoMap.left().value().getInputs();
				Either<Resource, ResponseFormat> createInputsOnResource = createInputsOnResource(preparedResource, user, inputs, true);
				if (createInputsOnResource.isRight()) {
					log.debug("failed to create resource inputs status is {}", createInputsOnResource.right().value());
					ResponseFormat responseFormat = createInputsOnResource.right().value();
					componentsUtils.auditResource(createInputsOnResource.right().value(), user, preparedResource, "", "", updateResource, null);
					result = Either.right(responseFormat);
					return result;
				}
				preparedResource = createInputsOnResource.left().value();

				Either<Resource, ResponseFormat> createResourcesInstancesEither = createResourceInstances(user, yamlFileName, preparedResource, instances, true, false, parseNodeTypeInfoYamlEither.left().value());
				if (createResourcesInstancesEither.isRight()) {
					log.debug("failed to create resource instances status is {}", createResourcesInstancesEither.right().value());
					ResponseFormat responseFormat = createResourcesInstancesEither.right().value();
					componentsUtils.auditResource(createResourcesInstancesEither.right().value(), user, preparedResource, "", "", updateResource, null);
					result = Either.right(responseFormat);
					return result;
				}
				preparedResource = createResourcesInstancesEither.left().value();

				createResourcesInstancesEither = createResourceInstancesRelations(user, yamlFileName, preparedResource, instances, true, false);
				if (createResourcesInstancesEither.isRight()) {
					log.debug("failed to create relation between resource instances status is {}", createResourcesInstancesEither.right().value());
					result = Either.right(createResourcesInstancesEither.right().value());
					return result;
				}

				preparedResource = createResourcesInstancesEither.left().value();

				Either<Map<String, GroupDefinition>, ResponseFormat> validateUpdateVfGroupNamesRes = groupBusinessLogic.validateUpdateVfGroupNames(uploadComponentInstanceInfoMap.left().value().getGroups(), preparedResource.getSystemName());
				if (validateUpdateVfGroupNamesRes.isRight()) {

					return Either.right(validateUpdateVfGroupNamesRes.right().value());
				}
				// add groups to resource
				Map<String, GroupDefinition> groups;

				if (!validateUpdateVfGroupNamesRes.left().value().isEmpty()) {
					groups = validateUpdateVfGroupNamesRes.left().value();
				} else {
					groups = uploadComponentInstanceInfoMap.left().value().getGroups();
				}
				Either<Resource, ResponseFormat> updatedGroupsOnResource = updateGroupsOnResource(preparedResource, user, groups);
				if (updatedGroupsOnResource.isRight()) {

					return updatedGroupsOnResource;
				}
				preparedResource = updatedGroupsOnResource.left().value();

			} else {
				Either<Resource, ResponseFormat> dataModelResponse = updateResourceMetadata(oldRresource.getUniqueId(), newRresource, user, oldRresource, false, true);
				if (dataModelResponse.isRight()) {
					log.debug("failed to update resource metadata {}", dataModelResponse.right().value());
					result = Either.right(dataModelResponse.right().value());
					return result;
				}
				preparedResource = dataModelResponse.left().value();
			}

			Either<Resource, ResponseFormat> createdCsarArtifactsEither = handleCsarArtifacts(preparedResource, user, csarUUID, csar.left().value(), createdArtifacts,
					artifactsBusinessLogic.new ArtifactOperationInfo(false, false, ArtifactOperationEnum.Update), false, true);
			if (createdCsarArtifactsEither.isRight()) {

				return createdCsarArtifactsEither;
			}
			preparedResource = createdCsarArtifactsEither.left().value();

			Either<List<ComponentInstance>, ResponseFormat> eitherSetPosition = compositionBusinessLogic.setPositionsForComponentInstances(preparedResource, user.getUserId());
			result = eitherSetPosition.isRight() ? Either.right(eitherSetPosition.right().value()) : Either.left(preparedResource);

			return result;

		} finally {
			if (result == null || result.isRight()) {
				log.warn("operation failed. do rollback");
				titanDao.rollback();
				if (!createdArtifacts.isEmpty() || !nodeTypesNewCreatedArtifacts.isEmpty()) {
					createdArtifacts.addAll(nodeTypesNewCreatedArtifacts);
					StorageOperationStatus deleteFromEsRes = artifactsBusinessLogic.deleteAllComponentArtifactsIfNotOnGraph(createdArtifacts);
					if (!deleteFromEsRes.equals(StorageOperationStatus.OK)) {
						ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(deleteFromEsRes);
						ResponseFormat responseFormat = componentsUtils.getResponseFormat(actionStatus, oldRresource.getName());
					}
					log.debug("component and all its artifacts were deleted, id = {}", oldRresource.getName());
				}
			} else {
				log.debug("operation success. do commit");
				titanDao.commit();
			}
			log.debug("unlock resource {}", lockedResourceId);
			graphLockOperation.unlockComponent(lockedResourceId, NodeTypeEnum.Resource);
		}

	}

	private Either<Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>>, ResponseFormat> findNodeTypesArtifactsToHandle(Map<String, byte[]> csar, String csarUUID, String yamlFileName, Resource oldResource, User user,
																																		 boolean inTransaction, Map<String, UploadComponentInstanceInfo> uploadComponentInstanceInfoMap) {

		Map<String, List<ArtifactDefinition>> extractedVfcsArtifacts = CsarUtils.extractVfcsArtifactsFromCsar(csar);
		Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
		Either<Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>>, ResponseFormat> nodeTypesArtifactsToHandleRes;

		try {
			nodeTypesArtifactsToHandleRes = Either.left(nodeTypesArtifactsToHandle);
			List<ImmutablePair<ImmutablePair<String, List<String>>, String>> extractedVfcToscaNames = extractVfcToscaNames(csar, yamlFileName, oldResource.getSystemName(), uploadComponentInstanceInfoMap);
			validateNodeTypeIdentifiers(extractedVfcsArtifacts, extractedVfcToscaNames);
			Either<EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>, ResponseFormat> curNodeTypeArtifactsToHandleRes = null;
			EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>> curNodeTypeArtifactsToHandle = null;
			log.debug("Going to fetch node types for resource with name {} during import csar with UUID {}. ", oldResource.getName(), csarUUID);

			for (ImmutablePair<ImmutablePair<String, List<String>>, String> currVfcToscaNameEntry : extractedVfcToscaNames) {
				String currVfcToscaName = currVfcToscaNameEntry.getValue();
				log.debug("Going to fetch node type with tosca name {}. ", currVfcToscaName);

				Either<Resource, StorageOperationStatus> curVfcRes = toscaOperationFacade.getLatestByToscaResourceName(currVfcToscaName);
				Resource curNodeType = null;
				if (curVfcRes.isRight() && curVfcRes.right().value() != StorageOperationStatus.NOT_FOUND) {
					log.debug("Error occured during fetching node type with tosca name {}, error: {}", currVfcToscaName, curVfcRes.right().value());
					ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(curVfcRes.right().value()), csarUUID);
					componentsUtils.auditResource(responseFormat, user, oldResource, "", "", AuditingActionEnum.CREATE_RESOURCE, null);
					nodeTypesArtifactsToHandleRes = Either.right(responseFormat);
					break;
				} else if (curVfcRes.isLeft()) {
					curNodeType = curVfcRes.left().value();
				}
				if (!MapUtils.isEmpty(extractedVfcsArtifacts)) {
					List<ArtifactDefinition> currArtifacts = new ArrayList<>();
					for (String currNamespace : currVfcToscaNameEntry.getKey().getValue()) {
						if (extractedVfcsArtifacts.containsKey(currNamespace)) {
							handleAndAddExtractedVfcsArtifacts(currArtifacts, extractedVfcsArtifacts.get(currNamespace));
						}
					}
					curNodeTypeArtifactsToHandleRes = findNodeTypeArtifactsToHandle(curNodeType, currArtifacts);
					if (curNodeTypeArtifactsToHandleRes.isRight()) {
						nodeTypesArtifactsToHandleRes = Either.right(curNodeTypeArtifactsToHandleRes.right().value());
						break;
					}
					curNodeTypeArtifactsToHandle = curNodeTypeArtifactsToHandleRes.left().value();

				} else if (curNodeType != null) {
					// delete all artifacts if have not received artifacts from csar
					curNodeTypeArtifactsToHandle = new EnumMap<>(ArtifactOperationEnum.class);
					List<ArtifactDefinition> artifactsToDelete = new ArrayList<>();
					// delete all informational artifacts
					artifactsToDelete.addAll(curNodeType.getArtifacts().values().stream().filter(a -> a.getArtifactGroupType() == ArtifactGroupTypeEnum.INFORMATIONAL).collect(Collectors.toList()));
					// delete all deployment artifacts
					artifactsToDelete.addAll(curNodeType.getDeploymentArtifacts().values());
					if (!artifactsToDelete.isEmpty()) {
						curNodeTypeArtifactsToHandle.put(ArtifactOperationEnum.Delete, artifactsToDelete);
					}
				}
				if (MapUtils.isNotEmpty(curNodeTypeArtifactsToHandle)) {
					nodeTypesArtifactsToHandle.put(currVfcToscaNameEntry.getKey().getKey(), curNodeTypeArtifactsToHandle);
				}
			}
		} catch (Exception e) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
			nodeTypesArtifactsToHandleRes = Either.right(responseFormat);
			log.debug("Exception occured when findNodeTypesUpdatedArtifacts, error is:{}", e.getMessage(), e);
		}
		return nodeTypesArtifactsToHandleRes;
	}

	private void validateNodeTypeIdentifiers(Map<String, List<ArtifactDefinition>> extractedVfcsArtifacts, List<ImmutablePair<ImmutablePair<String, List<String>>, String>> extractedVfcToscaNames) {
		if (extractedVfcsArtifacts != null) {
			List<String> validIdentifiers = new ArrayList<>();
			if (extractedVfcToscaNames != null) {
				extractedVfcToscaNames.stream().forEach(pair -> {
					validIdentifiers.addAll(pair.getKey().getValue());
					validIdentifiers.add(pair.getKey().getKey());
				});
			}
			for (String curIdentifier : extractedVfcsArtifacts.keySet()) {
				if (validIdentifiers != null && !validIdentifiers.contains(curIdentifier))
					log.warn("Warning - VFC identification {} provided in the Artifacts folder of the CSAR is not valid. ", curIdentifier);
			}
		}
	}

	private Either<EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>, ResponseFormat> findNodeTypeArtifactsToHandle(Resource curNodeType, List<ArtifactDefinition> extractedArtifacts) {

		Either<EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>, ResponseFormat> nodeTypeArtifactsToHandleRes = null;
		EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle = null;
		Wrapper<ResponseFormat> responseWrapper = new Wrapper<>();
		try {
			List<ArtifactDefinition> artifactsToUpload = new ArrayList<>(extractedArtifacts);
			List<ArtifactDefinition> artifactsToUpdate = new ArrayList<>();
			List<ArtifactDefinition> artifactsToDelete = new ArrayList<>();
			if (curNodeType != null) {
				Map<String, ArtifactDefinition> existingArtifacts = new HashMap<>();
				if (curNodeType.getDeploymentArtifacts() != null) {
					existingArtifacts.putAll(curNodeType.getDeploymentArtifacts());
				}
				if (curNodeType.getArtifacts() != null) {
					existingArtifacts.putAll(curNodeType.getArtifacts().entrySet().stream().filter(e -> e.getValue().getArtifactGroupType() == ArtifactGroupTypeEnum.INFORMATIONAL).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
				}
				for (ArtifactDefinition currNewArtifact : extractedArtifacts) {
					ArtifactDefinition foundArtifact;

					if (!existingArtifacts.isEmpty()) {
						foundArtifact = existingArtifacts.values().stream().filter(a -> a.getArtifactName().equals(currNewArtifact.getArtifactName())).findFirst().orElse(null);
						if (foundArtifact != null) {
							if (foundArtifact.getArtifactType().equals(currNewArtifact.getArtifactType())) {
								if (!foundArtifact.getArtifactChecksum().equals(currNewArtifact.getArtifactChecksum())) {
									foundArtifact.setPayload(currNewArtifact.getPayloadData());
									foundArtifact.setPayloadData(Base64.encodeBase64String(currNewArtifact.getPayloadData()));
									foundArtifact.setArtifactChecksum(GeneralUtility.calculateMD5ByByteArray(currNewArtifact.getPayloadData()));
									artifactsToUpdate.add(foundArtifact);
								}
								existingArtifacts.remove(foundArtifact.getArtifactLabel());
								artifactsToUpload.remove(currNewArtifact);
							} else {
								log.debug("Can't upload two artifact with the same name {}.", currNewArtifact.getArtifactName());
								ResponseFormat responseFormat = ResponseFormatManager.getInstance().getResponseFormat(ActionStatus.ARTIFACT_ALRADY_EXIST_IN_DIFFERENT_TYPE_IN_CSAR, currNewArtifact.getArtifactName(), currNewArtifact.getArtifactType(),
										foundArtifact.getArtifactType());
								responseWrapper.setInnerElement(responseFormat);
								break;
							}
						}
					}
				}
				if (responseWrapper.isEmpty()) {
					artifactsToDelete.addAll(existingArtifacts.values());
				}
			}
			if (responseWrapper.isEmpty()) {
				if (!artifactsToUpload.isEmpty() || !artifactsToUpdate.isEmpty() || !artifactsToDelete.isEmpty()) {
					nodeTypeArtifactsToHandle = new EnumMap<>(ArtifactOperationEnum.class);
					if (!artifactsToUpload.isEmpty())
						nodeTypeArtifactsToHandle.put(ArtifactOperationEnum.Create, artifactsToUpload);
					if (!artifactsToUpdate.isEmpty())
						nodeTypeArtifactsToHandle.put(ArtifactOperationEnum.Update, artifactsToUpdate);
					if (!artifactsToDelete.isEmpty())
						nodeTypeArtifactsToHandle.put(ArtifactOperationEnum.Delete, artifactsToDelete);
				}
				nodeTypeArtifactsToHandleRes = Either.left(nodeTypeArtifactsToHandle);
			}
			if (!responseWrapper.isEmpty()) {
				nodeTypeArtifactsToHandleRes = Either.right(responseWrapper.getInnerElement());
			}
		} catch (Exception e) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
			responseWrapper.setInnerElement(responseFormat);
			log.debug("Exception occured when findNodeTypeArtifactsToHandle, error is:{}", e.getMessage(), e);
		}
		return nodeTypeArtifactsToHandleRes;
	}

	/**
	 * Changes resource life cycle state to checked out
	 *
	 * @param resource
	 * @param user
	 * @param inTransaction
	 * @return
	 */
	private Either<Resource, ResponseFormat> checkoutResource(Resource resource, User user, boolean inTransaction) {
		Either<Resource, ResponseFormat> checkoutResourceRes;
		try {
			if (!resource.getComponentMetadataDefinition().getMetadataDataDefinition().getState().equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
				log.debug("************* Going to change life cycle state of resource {} to not certified checked out. ", resource.getName());
				Either<? extends Component, ResponseFormat> checkoutRes = lifecycleBusinessLogic.changeComponentState(resource.getComponentType(), resource.getUniqueId(), user, LifeCycleTransitionEnum.CHECKOUT,
						new LifecycleChangeInfoWithAction("certification on import", LifecycleChanceActionEnum.CREATE_FROM_CSAR), inTransaction, true);
				if (checkoutRes.isRight()) {
					log.debug("Could not change state of component {} with uid {} to checked out. Status is {}. ", resource.getComponentType().getNodeType(), resource.getUniqueId(), checkoutRes.right().value().getStatus());
					checkoutResourceRes = Either.right(checkoutRes.right().value());
				} else {
					checkoutResourceRes = Either.left((Resource) checkoutRes.left().value());
				}
			} else {
				checkoutResourceRes = Either.left(resource);
			}
		} catch (Exception e) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
			checkoutResourceRes = Either.right(responseFormat);
			log.debug("Exception occured when checkoutResource {} , error is:{}", resource.getName(), e.getMessage(), e);
		}
		return checkoutResourceRes;
	}

	/**
	 * Handles Artifacts of NodeType
	 *
	 * @param nodeTypeResource
	 * @param nodeTypeArtifactsToHandle
	 * @param vfcsNewCreatedArtifacts
	 * @param user
	 * @param inTransaction
	 * @return
	 */
	public Either<List<ArtifactDefinition>, ResponseFormat> handleNodeTypeArtifacts(Resource nodeTypeResource, Map<ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle, List<ArtifactDefinition> vfcsNewCreatedArtifacts,
																					User user, boolean inTransaction) {
		Either<List<ArtifactDefinition>, ResponseFormat> handleNodeTypeArtifactsRequestRes;
		Either<List<ArtifactDefinition>, ResponseFormat> handleNodeTypeArtifactsRes = null;
		Either<Resource, ResponseFormat> changeStateResponse;
		try {
			changeStateResponse = checkoutResource(nodeTypeResource, user, inTransaction);
			if (changeStateResponse.isRight()) {
				return Either.right(changeStateResponse.right().value());
			}
			nodeTypeResource = changeStateResponse.left().value();

			List<ArtifactDefinition> handledNodeTypeArtifacts = new ArrayList<>();
			log.debug("************* Going to handle artifacts of node type resource {}. ", nodeTypeResource.getName());
			for (Entry<ArtifactOperationEnum, List<ArtifactDefinition>> curOperationEntry : nodeTypeArtifactsToHandle.entrySet()) {
				ArtifactOperationEnum curOperation = curOperationEntry.getKey();
				List<ArtifactDefinition> curArtifactsToHandle = curOperationEntry.getValue();
				if (curArtifactsToHandle != null && !curArtifactsToHandle.isEmpty()) {
					log.debug("************* Going to {} artifact to vfc {}", curOperation.name(), nodeTypeResource.getName());
					handleNodeTypeArtifactsRequestRes = artifactsBusinessLogic.handleArtifactsRequestForInnerVfcComponent(curArtifactsToHandle, nodeTypeResource, user, vfcsNewCreatedArtifacts,
							artifactsBusinessLogic.new ArtifactOperationInfo(false, false, curOperation), false, inTransaction);
					if (handleNodeTypeArtifactsRequestRes.isRight()) {
						handleNodeTypeArtifactsRes = Either.right(handleNodeTypeArtifactsRequestRes.right().value());
						break;
					}
					if (curOperation == ArtifactOperationEnum.Create) {
						vfcsNewCreatedArtifacts.addAll(handleNodeTypeArtifactsRequestRes.left().value());
					}
					handledNodeTypeArtifacts.addAll(handleNodeTypeArtifactsRequestRes.left().value());
				}
			}
			if (handleNodeTypeArtifactsRes == null) {
				handleNodeTypeArtifactsRes = Either.left(handledNodeTypeArtifacts);
			}
		} catch (Exception e) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
			handleNodeTypeArtifactsRes = Either.right(responseFormat);
			log.debug("Exception occured when handleVfcArtifacts, error is:{}", e.getMessage(), e);
		}
		return handleNodeTypeArtifactsRes;
	}

	@SuppressWarnings("unchecked")
	private List<ImmutablePair<ImmutablePair<String, List<String>>, String>> extractVfcToscaNames(Map<String, byte[]> csar, String yamlFileName, String vfResourceName, Map<String, UploadComponentInstanceInfo> uploadComponentInstanceInfoMap) {
		List<ImmutablePair<ImmutablePair<String, List<String>>, String>> vfcToscaNames = new ArrayList<>();
		Map<String, Object> nodeTypes;
		if (csar != null) {
			nodeTypes = new HashMap<>();
			putNodeTypesFromYaml(csar, yamlFileName, nodeTypes);
			putNodeTypesFromYaml(csar, Constants.GLOBAL_SUBSTITUTION_TYPES_SERVICE_TEMPLATE, nodeTypes);
			putNodeTypesFromYaml(csar, Constants.ABSTRACT_SUBSTITUTE_GLOBAL_TYPES_SERVICE_TEMPLATE, nodeTypes);
			Map<String,String> nestedServiceTemplatesMap = new HashMap<>();
			for(UploadComponentInstanceInfo ci : uploadComponentInstanceInfoMap.values()){
				if(ci.getProperties() != null && ci.getProperties().containsKey("service_template_filter")){
					String tempName = CsarUtils.DEFINITIONS_PATH + ((Map<String, String>)ci.getProperties().get("service_template_filter").get(0).getValue()).get("substitute_service_template");
					putNodeTypesFromYaml(csar,tempName, nodeTypes);
					nestedServiceTemplatesMap.put(ci.getType(), tempName);
				}
			}

			if (!nodeTypes.isEmpty()) {
				Iterator<Entry<String, Object>> nodesNameEntry = nodeTypes.entrySet().iterator();
				while (nodesNameEntry.hasNext()) {
					Entry<String, Object> nodeType = nodesNameEntry.next();
					addVfcToscaNameFindSubstitutes(csar, vfResourceName, vfcToscaNames, nodeType.getKey(), nestedServiceTemplatesMap);
				}
			}
		}
		return vfcToscaNames;
	}

	@SuppressWarnings("unchecked")
	private void putNodeTypesFromYaml(Map<String, byte[]> csar, String yamlFileName, Map<String, Object> nodeTypes) {

		if (csar.containsKey(yamlFileName)) {
			Map<String, Object> mappedToscaTemplate;
			Either<Map<String, Object>, ResultStatusEnum> eitherNodeTypes;
			mappedToscaTemplate = (Map<String, Object>) new Yaml().load(new String(csar.get(yamlFileName), StandardCharsets.UTF_8));
			eitherNodeTypes = ImportUtils.findFirstToscaMapElement(mappedToscaTemplate, ToscaTagNamesEnum.NODE_TYPES);
			if (eitherNodeTypes.isLeft()) {
				nodeTypes.putAll(eitherNodeTypes.left().value());
			}
		}
	}

	private void addVfcToscaNameFindSubstitutes(Map<String, byte[]> csar, String vfResourceName, List<ImmutablePair<ImmutablePair<String, List<String>>, String>> vfcToscaNames, String nodeTypeFullName, Map<String, String> nestedServiceTemplatesMap) {

		String toscaResourceName = buildNestedVfcToscaResourceName(vfResourceName, nodeTypeFullName);
		String nodeTypeTemplateYamlName =null;
		if(nestedServiceTemplatesMap.containsKey(nodeTypeFullName)){
			nodeTypeTemplateYamlName = nestedServiceTemplatesMap.get(nodeTypeFullName);
		}
		List<String> relatedVfcsToscaNameSpaces = new ArrayList<>();
		relatedVfcsToscaNameSpaces.add(buildNestedVfcToscaNamespace(nodeTypeFullName));
		if (nodeTypeTemplateYamlName!=null && csar.containsKey(nodeTypeTemplateYamlName)) {
			addSubstituteToscaNamespacesRecursively(csar, nodeTypeTemplateYamlName, relatedVfcsToscaNameSpaces, nestedServiceTemplatesMap);
		}
		ImmutablePair<String, List<String>> toscaNameSpacesHierarchy = new ImmutablePair<>(nodeTypeFullName, relatedVfcsToscaNameSpaces);
		vfcToscaNames.add(new ImmutablePair<>(toscaNameSpacesHierarchy, toscaResourceName));
	}

	private void addSubstituteToscaNamespacesRecursively(Map<String, byte[]> csar, String yamlFileName, List<String> toscaNameSpaces, Map<String, String> nestedServiceTemplatesMap) {

		Map<String, Object> nodeTypes = new HashMap<>();

		if (csar.containsKey(yamlFileName)) {
			putNodeTypesFromYaml(csar, yamlFileName, nodeTypes);
		}
		if (!nodeTypes.isEmpty()) {
			Iterator<Entry<String, Object>> nodesNameEntry = nodeTypes.entrySet().iterator();
			while (nodesNameEntry.hasNext()) {
				Entry<String, Object> nodeType = nodesNameEntry.next();
				String nodeTypeFullName = nodeType.getKey();
				String toscaNameSpace = buildNestedVfcToscaNamespace(nodeTypeFullName);
				if (toscaNameSpaces.contains(toscaNameSpace)) {
					break;
				}
				toscaNameSpaces.add(toscaNameSpace);

				String nodeTypeTemplateYamlName =null;
				if(nestedServiceTemplatesMap.containsKey(nodeTypeFullName)){
					nodeTypeTemplateYamlName = nestedServiceTemplatesMap.get(nodeTypeFullName);
				}

				if (nodeTypeTemplateYamlName!=null && csar.containsKey(nodeTypeTemplateYamlName)) {
					addSubstituteToscaNamespacesRecursively(csar, nodeTypeTemplateYamlName, toscaNameSpaces, nestedServiceTemplatesMap);
				}
			}
		}
	}

	public Either<Resource, ResponseFormat> createResourceFromCsar(Resource resource, User user, Either<Map<String, byte[]>, StorageOperationStatus> csarUIPayload, String csarUUID) {
		log.trace("************* created successfully from YAML, resource TOSCA ");

		Either<Map<String, byte[]>, StorageOperationStatus> csar = null;
		if (csarUIPayload != null && csarUIPayload.left() != null && csarUIPayload.left().value() != null) {
			csar = csarUIPayload;
		} else {
			csar = csarOperation.getCsar(csarUUID, user);
		}

		Either<ImmutablePair<String, String>, ResponseFormat> toscaYamlCsarStatus = validateAndParseCsar(resource, user, csarUUID, csar);
		if (toscaYamlCsarStatus.isRight()) {
			return Either.right(toscaYamlCsarStatus.right().value());
		}
		Either<String, ResponseFormat> toscaYamlChecksum = CsarValidationUtils.getToscaYamlChecksum(csar.left().value(), csarUUID, componentsUtils);
		if (toscaYamlChecksum.isRight()) {
			log.debug("Failed to calculate checksum for CSAR {}, error  {}", csarUUID, toscaYamlChecksum.right().value());
			return Either.right(toscaYamlChecksum.right().value());
		}
		resource.getComponentMetadataDefinition().getMetadataDataDefinition().setImportedToscaChecksum(toscaYamlChecksum.left().value());

		String yamlFileName = toscaYamlCsarStatus.left().value().getKey();
		String yamlFileContents = toscaYamlCsarStatus.left().value().getValue();
		log.trace("YAML topology file found in CSAR, file name: {}, contents: {}", yamlFileName, yamlFileContents);
		Either<Resource, ResponseFormat> createResourceFromYaml = createResourceFromYaml(resource, user, yamlFileContents, yamlFileName, csar.left().value(), csarUUID);
		if (createResourceFromYaml.isRight()) {
			log.debug("Couldn't create resource from YAML");
			return Either.right(createResourceFromYaml.right().value());
		}

		Resource vfResource = createResourceFromYaml.left().value();
		log.trace("*************VF Resource created successfully from YAML, resource TOSCA name: {}", vfResource.getToscaResourceName());
		return Either.left(vfResource);
	}

	private Either<ImmutablePair<String, String>, ResponseFormat> validateAndParseCsar(Resource resource, User user, String csarUUID, Either<Map<String, byte[]>, StorageOperationStatus> csar) {
		if (csar.isRight()) {
			StorageOperationStatus value = csar.right().value();
			log.debug("Error when fetching csar with ID {}, error: {}", csarUUID, value);
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Creating resource from CSAR: fetching CSAR with id " + csarUUID + " failed");
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(value), csarUUID);
			componentsUtils.auditResource(responseFormat, user, resource, "", "", AuditingActionEnum.CREATE_RESOURCE, null);
			return Either.right(responseFormat);
		}

		Either<Boolean, ResponseFormat> validateCsarStatus = CsarValidationUtils.validateCsar(csar.left().value(), csarUUID, componentsUtils);
		if (validateCsarStatus.isRight()) {
			ResponseFormat responseFormat = validateCsarStatus.right().value();
			log.debug("Error when validate csar with ID {}, error: {}", csarUUID, responseFormat);
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Creating resource from CSAR: fetching CSAR with id " + csarUUID + " failed");
			componentsUtils.auditResource(responseFormat, user, resource, "", "", AuditingActionEnum.CREATE_RESOURCE, null);
			return Either.right(responseFormat);
		}

		Either<ImmutablePair<String, String>, ResponseFormat> toscaYamlCsarStatus = CsarValidationUtils.getToscaYaml(csar.left().value(), csarUUID, componentsUtils);

		if (toscaYamlCsarStatus.isRight()) {
			ResponseFormat responseFormat = toscaYamlCsarStatus.right().value();
			log.debug("Error when try to get csar toscayamlFile with csar ID {}, error: {}", csarUUID, responseFormat);
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Creating resource from CSAR: fetching CSAR with id " + csarUUID + " failed");
			componentsUtils.auditResource(responseFormat, user, resource, "", "", AuditingActionEnum.CREATE_RESOURCE, null);
			return Either.right(responseFormat);
		}
		return toscaYamlCsarStatus;
	}

	private Either<Resource, ResponseFormat> validateResourceBeforeCreate(Resource resource, User user, boolean inTransaction) {
		log.trace("validating resource before create");
		Either<User, ResponseFormat> eitherCreator = validateUser(user, "Create Resource", resource, AuditingActionEnum.CREATE_RESOURCE, false);
		if (eitherCreator.isRight()) {
			return Either.right(eitherCreator.right().value());
		}
		user.copyData(eitherCreator.left().value());

		// validate user role
		Either<Boolean, ResponseFormat> validateRes = validateUserRole(user, resource, new ArrayList<Role>(), AuditingActionEnum.CREATE_RESOURCE, null);
		if (validateRes.isRight()) {
			return Either.right(validateRes.right().value());
		}
		// VF "derivedFrom" should be null (or ignored)
		if (!resource.getResourceType().equals(ResourceTypeEnum.VF)) {
			Either<Boolean, ResponseFormat> validateDerivedFromNotEmpty = validateDerivedFromNotEmpty(user, resource, AuditingActionEnum.CREATE_RESOURCE);
			if (validateDerivedFromNotEmpty.isRight()) {
				return Either.right(validateDerivedFromNotEmpty.right().value());
			}
		}
		return validateResourceBeforeCreate(resource, user, AuditingActionEnum.CREATE_RESOURCE, inTransaction);

	}

	public Either<Resource, ResponseFormat> createResourceFromYaml(Resource resource, User user, String topologyTemplateYaml, String yamlName, Map<String, byte[]> csar, String csarUUID) {

		List<ArtifactDefinition> createdArtifacts = new ArrayList<ArtifactDefinition>();
		log.trace("************* createResourceFromYaml before parse yaml ");
		Either<ParsedToscaYamlInfo, ResponseFormat> parseResourceInfoFromYamlEither = parseResourceInfoFromYaml(yamlName, resource, topologyTemplateYaml, user);
		if (parseResourceInfoFromYamlEither.isRight()) {
			ResponseFormat responseFormat = parseResourceInfoFromYamlEither.right().value();
			componentsUtils.auditResource(responseFormat, user, resource, "", "", AuditingActionEnum.IMPORT_RESOURCE, null);
			return Either.right(responseFormat);
		}
		log.trace("************* createResourceFromYaml after parse yaml ");
		ParsedToscaYamlInfo parsedToscaYamlInfo = parseResourceInfoFromYamlEither.left().value();
		log.debug("The parsed tosca yaml info is {}", parsedToscaYamlInfo);
		log.trace("************* createResourceFromYaml before create ");
		Either<Resource, ResponseFormat> createdResourceResponse = createResourceAndRIsFromYaml(yamlName, resource, user, parsedToscaYamlInfo, AuditingActionEnum.IMPORT_RESOURCE, false, csarUUID, csar, createdArtifacts, topologyTemplateYaml);
		log.trace("************* createResourceFromYaml after create ");
		if (createdResourceResponse.isRight()) {
			ResponseFormat responseFormat = createdResourceResponse.right().value();
			componentsUtils.auditResource(responseFormat, user, resource, "", "", AuditingActionEnum.IMPORT_RESOURCE, null);
			return Either.right(responseFormat);
		}

		return createdResourceResponse;

	}

	public Either<Map<String, Resource>, ResponseFormat> createResourcesFromYamlNodeTypesList(String yamlName, Resource resource, String resourceYml, User user, boolean needLock,
																							  Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle, List<ArtifactDefinition> nodeTypesNewCreatedArtifacts) {

		Map<String, Object> mappedToscaTemplate = (Map<String, Object>) new Yaml().load(resourceYml);

		Either<String, ResultStatusEnum> tosca_version = ImportUtils.findFirstToscaStringElement(mappedToscaTemplate, ToscaTagNamesEnum.TOSCA_VERSION);
		if (tosca_version.isRight()) {
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_TOSCA_TEMPLATE);
			return Either.right(responseFormat);
		}

		Either<Map<String, Object>, ResultStatusEnum> eitherNodeTypes = ImportUtils.findFirstToscaMapElement(mappedToscaTemplate, ToscaTagNamesEnum.NODE_TYPES);

		Map<String, Resource> nodeTypesResources = new HashMap<>();
		Either<Map<String, Resource>, ResponseFormat> result = Either.left(nodeTypesResources);

		Map<String, Object> mapToConvert = new HashMap<String, Object>();
		mapToConvert.put(ToscaTagNamesEnum.TOSCA_VERSION.getElementName(), tosca_version.left().value());

		if (eitherNodeTypes.isLeft()) {

			Iterator<Entry<String, Object>> nodesNameValue = eitherNodeTypes.left().value().entrySet().iterator();

			while (nodesNameValue.hasNext()) {

				Entry<String, Object> nodeType = nodesNameValue.next();
				Map<ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle = nodeTypesArtifactsToHandle == null || nodeTypesArtifactsToHandle.isEmpty() ? null : nodeTypesArtifactsToHandle.get(nodeType.getKey());
				log.trace("************* Going to create node {}", nodeType.getKey());
				Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> resourceCreated = this.createNodeTypeResourceFromYaml(yamlName, nodeType, user, mapToConvert, resource, needLock, nodeTypeArtifactsToHandle, nodeTypesNewCreatedArtifacts);
				log.trace("************* finished to create node {}", nodeType.getKey());
				if (resourceCreated.isRight()) {
					return Either.right(resourceCreated.right().value());
				}
				Resource vfcCreated = resourceCreated.left().value().getLeft();

				nodeTypesResources.put(nodeType.getKey(), vfcCreated);
				mapToConvert.remove(ToscaTagNamesEnum.NODE_TYPES.getElementName());

			}
		}

		return result;
	}

	private String getNodeTypeActualName(String fullName) {
		String nameWithouNamespacePrefix = fullName.substring(Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX.length());
		String[] findTypes = nameWithouNamespacePrefix.split("\\.");
		String resourceType = findTypes[0];
		return nameWithouNamespacePrefix.substring(resourceType.length());
	}

	private Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> createNodeTypeResourceFromYaml(String yamlName, Entry<String, Object> nodeNameValue, User user, Map<String, Object> mapToConvert, Resource resourceVf, boolean needLock,
																										 Map<ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle, List<ArtifactDefinition> nodeTypesNewCreatedArtifacts) {

		Either<UploadResourceInfo, ResponseFormat> resourceMetaData = fillResourceMetadata(yamlName, resourceVf, nodeNameValue.getKey(), user);
		if (resourceMetaData.isRight()) {
			return Either.right(resourceMetaData.right().value());
		}

		// We need to create a Yaml from each node_types in order to create
		// resource from each node type using import normative flow.
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		Yaml yaml = new Yaml(options);

		Map<String, Object> singleVfc = new HashMap<>();

		String actualName = this.getNodeTypeActualName(nodeNameValue.getKey());
		if (!actualName.startsWith(Constants.ABSTRACT)) {
			actualName = "." + Constants.ABSTRACT + actualName;
		}

		// Setting tosca name
		String toscaResourceName = ImportUtils.Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX + resourceMetaData.left().value().getResourceType().toLowerCase() + '.' + resourceVf.getSystemName() + actualName;
		singleVfc.put(toscaResourceName, nodeNameValue.getValue());
		mapToConvert.put(ToscaTagNamesEnum.NODE_TYPES.getElementName(), singleVfc);

		String singleVfcYaml = yaml.dumpAsMap(mapToConvert);

		Either<User, ResponseFormat> eitherCreator = validateUser(user, "CheckIn Resource", resourceVf, AuditingActionEnum.CHECKIN_RESOURCE, true);
		if (eitherCreator.isRight()) {
			return Either.right(eitherCreator.right().value());
		}
		user = eitherCreator.left().value();

		return this.createResourceFromNodeType(singleVfcYaml, resourceMetaData.left().value(), user, true, needLock, nodeTypeArtifactsToHandle, nodeTypesNewCreatedArtifacts);
	}

	public Either<Boolean, ResponseFormat> validateResourceCreationFromNodeType(Resource resource, User creator) {

		Either<Boolean, ResponseFormat> validateDerivedFromNotEmpty = this.validateDerivedFromNotEmpty(creator, resource, AuditingActionEnum.CREATE_RESOURCE);
		if (validateDerivedFromNotEmpty.isRight()) {
			return Either.right(validateDerivedFromNotEmpty.right().value());
		}
		return Either.left(true);
	}

	public Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> createResourceFromNodeType(String nodeTypeYaml, UploadResourceInfo resourceMetaData, User creator, boolean isInTransaction, boolean needLock,
																									Map<ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle, List<ArtifactDefinition> nodeTypesNewCreatedArtifacts) {

		LifecycleChangeInfoWithAction lifecycleChangeInfo = new LifecycleChangeInfoWithAction("certification on import", LifecycleChanceActionEnum.CREATE_FROM_CSAR);
		Function<Resource, Either<Boolean, ResponseFormat>> validator = (resource) -> this.validateResourceCreationFromNodeType(resource, creator);
		return this.resourceImportManager.importCertifiedResource(nodeTypeYaml, resourceMetaData, creator, validator, lifecycleChangeInfo, isInTransaction, true, needLock, nodeTypeArtifactsToHandle, nodeTypesNewCreatedArtifacts);
	}

	private Either<UploadResourceInfo, ResponseFormat> fillResourceMetadata(String yamlName, Resource resourceVf, String nodeTypeName, User user) {
		UploadResourceInfo resourceMetaData = new UploadResourceInfo();

		// validate nodetype name prefix
		if (!nodeTypeName.startsWith(Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX)) {
			log.debug("invalid nodeTypeName:{} does not start with {}.", nodeTypeName, Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX);
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_NODE_TEMPLATE, yamlName, resourceMetaData.getName(), nodeTypeName);
			return Either.right(responseFormat);
		}

		String actualName = this.getNodeTypeActualName(nodeTypeName);
		String namePrefix = nodeTypeName.replace(actualName, "");
		String resourceType = namePrefix.substring(Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX.length());

		// if we import from csar, the node_type name can be
		// org.openecomp.resource.abstract.node_name - in this case we always
		// create a vfc
		if (resourceType.equals(Constants.ABSTRACT)) {
			resourceType = ResourceTypeEnum.VFC.name().toLowerCase();
		}
		// validating type
		if (!ResourceTypeEnum.containsName(resourceType.toUpperCase())) {
			log.debug("invalid resourceType:{} the type is not one of the valide types:{}.", resourceType.toUpperCase(), ResourceTypeEnum.values());
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_NODE_TEMPLATE, yamlName, resourceMetaData.getName(), nodeTypeName);
			return Either.right(responseFormat);
		}

		// Setting name
		resourceMetaData.setName(resourceVf.getSystemName() + actualName);

		// Setting type from name
		String type = resourceType.toUpperCase();
		resourceMetaData.setResourceType(type);

		resourceMetaData.setDescription(ImportUtils.Constants.INNER_VFC_DESCRIPTION);
		resourceMetaData.setIcon(ImportUtils.Constants.DEFAULT_ICON);
		resourceMetaData.setContactId(user.getUserId());
		resourceMetaData.setVendorName(resourceVf.getVendorName());
		resourceMetaData.setVendorRelease(resourceVf.getVendorRelease());

		// Setting tag
		List<String> tags = new ArrayList<>();
		tags.add(resourceMetaData.getName());
		resourceMetaData.setTags(tags);

		// Setting category
		CategoryDefinition category = new CategoryDefinition();
		category.setName(ImportUtils.Constants.ABSTRACT_CATEGORY_NAME);
		SubCategoryDefinition subCategory = new SubCategoryDefinition();
		subCategory.setName(ImportUtils.Constants.ABSTRACT_SUBCATEGORY);
		category.addSubCategory(subCategory);
		List<CategoryDefinition> categories = new ArrayList<>();
		categories.add(category);
		resourceMetaData.setCategories(categories);

		return Either.left(resourceMetaData);
	}

	private Either<Resource, ResponseFormat> createResourceAndRIsFromYaml(String yamlName, Resource resource, User user, ParsedToscaYamlInfo parsedToscaYamlInfo, AuditingActionEnum actionEnum, boolean isNormative, String csarUUID,
																		  Map<String, byte[]> csar, List<ArtifactDefinition> createdArtifacts, String topologyTemplateYaml) {

		boolean result = true;
		boolean inTransaction = true;
		Map<String, Resource> createdVfcs = new HashMap<>();
		List<ArtifactDefinition> nodeTypesNewCreatedArtifacts = new ArrayList<>();
		Either<Boolean, ResponseFormat> lockResult = lockComponentByName(resource.getSystemName(), resource, "Create Resource");
		if (lockResult.isRight()) {
			ResponseFormat responseFormat = lockResult.right().value();
			return Either.right(responseFormat);
		}
		log.debug("name is locked {} status = {}", resource.getSystemName(), lockResult);

		try {
			log.trace("************* createResourceFromYaml before full create resource {}", yamlName);
			Either<Resource, ResponseFormat> genericResourceEither = fetchAndSetDerivedFromGenericType(resource);
			if (genericResourceEither.isRight()) {
				result = false;
				return genericResourceEither;
			}
			Either<Resource, ResponseFormat> createResourcesEither = createResourceTransaction(resource, user, isNormative, inTransaction);
			log.trace("************* createResourceFromYaml after full create resource {}", yamlName);
			if (createResourcesEither.isRight()) {
				result = false;
				return createResourcesEither;
			}
			resource = createResourcesEither.left().value();
			// add groups to resource
			log.trace("************* Going to add inputs from yaml {}", yamlName);
			if (resource.shouldGenerateInputs())
				generateInputsFromGenericTypeProperties(resource, genericResourceEither.left().value());

			Map<String, InputDefinition> inputs = parsedToscaYamlInfo.getInputs();
			Either<Resource, ResponseFormat> createInputsOnResource = createInputsOnResource(resource, user, inputs, inTransaction);
			if (createInputsOnResource.isRight()) {
				result = false;
				return createInputsOnResource;
			}
			resource = createInputsOnResource.left().value();
			log.trace("************* Finish to add inputs from yaml {}", yamlName);

			Map<String, UploadComponentInstanceInfo> uploadComponentInstanceInfoMap = parsedToscaYamlInfo.getInstances();
			log.trace("************* Going to create nodes, RI's and Relations  from yaml {}", yamlName);
			createResourcesEither = createRIAndRelationsFromYaml(yamlName, resource, user, uploadComponentInstanceInfoMap, actionEnum, topologyTemplateYaml, csar, csarUUID, nodeTypesNewCreatedArtifacts, createdVfcs);
			log.trace("************* Finished to create nodes, RI and Relation  from yaml {}", yamlName);
			if (createResourcesEither.isRight()) {
				result = false;
				return createResourcesEither;
			}

			resource = createResourcesEither.left().value();
			// validate update vf module group names
			Either<Map<String, GroupDefinition>, ResponseFormat> validateUpdateVfGroupNamesRes = groupBusinessLogic.validateUpdateVfGroupNames(parsedToscaYamlInfo.getGroups(), resource.getSystemName());
			if (validateUpdateVfGroupNamesRes.isRight()) {
				result = false;
				return Either.right(validateUpdateVfGroupNamesRes.right().value());
			}
			// add groups to resource
			Map<String, GroupDefinition> groups;
			log.trace("************* Going to add groups from yaml {}", yamlName);

			if (!validateUpdateVfGroupNamesRes.left().value().isEmpty()) {
				groups = validateUpdateVfGroupNamesRes.left().value();
			} else {
				groups = parsedToscaYamlInfo.getGroups();
			}
			Either<Resource, ResponseFormat> createGroupsOnResource = createGroupsOnResource(resource, user, groups);
			if (createGroupsOnResource.isRight()) {
				result = false;
				return createGroupsOnResource;
			}
			resource = createGroupsOnResource.left().value();
			log.trace("************* Finished to add groups from yaml {}", yamlName);

			log.trace("************* Going to add artifacts from yaml {}", yamlName);
			Either<Resource, ResponseFormat> createdCsarArtifactsEither = this.handleCsarArtifacts(resource, user, csarUUID, csar, createdArtifacts, artifactsBusinessLogic.new ArtifactOperationInfo(false, false, ArtifactOperationEnum.Create), false,
					inTransaction);
			log.trace("************* Finished to add artifacts from yaml {}", yamlName);
			if (createdCsarArtifactsEither.isRight()) {
				result = false;
				return createdCsarArtifactsEither;
			}

			resource = createdCsarArtifactsEither.left().value();
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.CREATED);
			componentsUtils.auditResource(responseFormat, user, resource, "", "", actionEnum, null);
			ASDCKpiApi.countCreatedResourcesKPI();
			return Either.left(resource);

		} finally {
			if (!result) {
				log.warn("operation failed. do rollback");
				titanDao.rollback();
				if (!createdArtifacts.isEmpty() || !nodeTypesNewCreatedArtifacts.isEmpty()) {
					createdArtifacts.addAll(nodeTypesNewCreatedArtifacts);
					StorageOperationStatus deleteFromEsRes = artifactsBusinessLogic.deleteAllComponentArtifactsIfNotOnGraph(createdArtifacts);
					if (!deleteFromEsRes.equals(StorageOperationStatus.OK)) {
						ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(deleteFromEsRes);
						ResponseFormat responseFormat = componentsUtils.getResponseFormat(actionStatus, resource.getName());
					}
					log.debug("component and all its artifacts were deleted, id = {}", resource.getName());
				}

			} else {
				log.debug("operation success. do commit");
				titanDao.commit();
			}

			graphLockOperation.unlockComponentByName(resource.getSystemName(), resource.getUniqueId(), NodeTypeEnum.Resource);

		}

	}

	private Either<Resource, ResponseFormat> createGroupsOnResource(Resource resource, User user, Map<String, GroupDefinition> groups) {
		if (groups != null && !groups.isEmpty()) {
			Either<List<GroupDefinition>, ResponseFormat> mergeGroupsUsingResource = updateGroupMembersUsingResource(groups, resource);

			if (mergeGroupsUsingResource.isRight()) {
				log.debug("Failed to prepare groups for creation");
				return Either.right(mergeGroupsUsingResource.right().value());
			}
			List<GroupDefinition> groupsAsList = mergeGroupsUsingResource.left().value();
			Either<List<GroupDefinition>, ResponseFormat> createGroups = groupBusinessLogic.createGroups(resource, user, ComponentTypeEnum.RESOURCE, groupsAsList);
			if (createGroups.isRight()) {
				return Either.right(createGroups.right().value());
			}
		} else {
			return Either.left(resource);
		}
		Either<Resource, StorageOperationStatus> updatedResource = toscaOperationFacade.getToscaElement(resource.getUniqueId());
		if (updatedResource.isRight()) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(updatedResource.right().value()), resource);
			return Either.right(responseFormat);
		}
		return Either.left(updatedResource.left().value());
	}

	private Either<Resource, ResponseFormat> updateGroupsOnResource(Resource resource, User user, Map<String, GroupDefinition> groups) {
		if (groups != null && false == groups.isEmpty()) {
			List<GroupDefinition> groupsFromResource = resource.getGroups();
			Either<List<GroupDefinition>, ResponseFormat> mergeGroupsUsingResource = updateGroupMembersUsingResource(groups, resource);

			if (mergeGroupsUsingResource.isRight()) {
				log.debug("Failed to prepare groups for creation");
				return Either.right(mergeGroupsUsingResource.right().value());
			}
			List<GroupDefinition> groupsAsList = mergeGroupsUsingResource.left().value();
			List<GroupDefinition> groupsToUpdate = new ArrayList<GroupDefinition>();
			List<GroupDefinition> groupsToDelete = new ArrayList<GroupDefinition>();
			List<GroupDefinition> groupsToCreate = new ArrayList<GroupDefinition>();
			if (groupsFromResource != null && !groupsFromResource.isEmpty()) {
				for (GroupDefinition group : groupsAsList) {
					Optional<GroupDefinition> op = groupsFromResource.stream().filter(p -> p.getName().equals(group.getName())).findAny();
					if (op.isPresent()) {
						GroupDefinition groupToUpdate = op.get();
						groupToUpdate.setMembers(group.getMembers());
						groupsToUpdate.add(groupToUpdate);
					} else {
						groupsToCreate.add(group);
					}
				}
				for (GroupDefinition group : groupsFromResource) {
					Optional<GroupDefinition> op = groupsAsList.stream().filter(p -> p.getName().equals(group.getName())).findAny();
					if (!op.isPresent() && (group.getArtifacts() == null || group.getArtifacts().isEmpty())) {

						groupsToDelete.add(group);
					}

				}
			} else
				groupsToCreate.addAll(groupsAsList);
			Either<List<GroupDefinition>, ResponseFormat> prepareGroups = null;
			if (!groupsToCreate.isEmpty()) {

				if (groupsFromResource != null && !groupsFromResource.isEmpty()) {
					prepareGroups = groupBusinessLogic.addGroups(resource, user, ComponentTypeEnum.RESOURCE, groupsToCreate);
				} else {
					prepareGroups = groupBusinessLogic.createGroups(resource, user, ComponentTypeEnum.RESOURCE, groupsToCreate);
				}

				if (prepareGroups.isRight()) {
					return Either.right(prepareGroups.right().value());
				}
			}

			if (!groupsToDelete.isEmpty()) {
				prepareGroups = groupBusinessLogic.deleteGroups(resource, user, ComponentTypeEnum.RESOURCE, groupsToDelete);
				if (prepareGroups.isRight()) {
					return Either.right(prepareGroups.right().value());
				}
			}

			if (groupsToUpdate != null && !groupsToUpdate.isEmpty()) {
				prepareGroups = groupBusinessLogic.updateGroups(resource, ComponentTypeEnum.RESOURCE, groupsToUpdate);
				if (prepareGroups.isRight()) {
					return Either.right(prepareGroups.right().value());
				}
			}

		} else {
			return Either.left(resource);
		}

		Either<Resource, StorageOperationStatus> updatedResource = toscaOperationFacade.getToscaElement(resource.getUniqueId());
		if (updatedResource.isRight()) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(updatedResource.right().value()), resource);
			return Either.right(responseFormat);
		}
		return Either.left(updatedResource.left().value());
	}

	private Either<Resource, ResponseFormat> createInputsOnResource(Resource resource, User user, Map<String, InputDefinition> inputs, boolean inTransaction) {
		if (inputs != null && false == inputs.isEmpty()) {

			Either<List<InputDefinition>, ResponseFormat> createGroups = inputsBusinessLogic.createInputsInGraph(inputs, resource, user, inTransaction);
			if (createGroups.isRight()) {
				return Either.right(createGroups.right().value());
			}
		} else {
			return Either.left(resource);
		}

		Either<Resource, StorageOperationStatus> updatedResource = toscaOperationFacade.getToscaElement(resource.getUniqueId());
		if (updatedResource.isRight()) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(updatedResource.right().value()), resource);
			return Either.right(responseFormat);
		}
		return Either.left(updatedResource.left().value());
	}

	private Either<List<GroupDefinition>, ResponseFormat> updateGroupMembersUsingResource(Map<String, GroupDefinition> groups, Resource component) {

		List<GroupDefinition> result = new ArrayList<>();

		List<ComponentInstance> componentInstances = component.getComponentInstances();

		if (groups != null) {
			Either<Boolean, ResponseFormat> validateCyclicGroupsDependencies = validateCyclicGroupsDependencies(groups);
			if (validateCyclicGroupsDependencies.isRight()) {
				return FunctionalInterfaces.convertEitherRight(validateCyclicGroupsDependencies);
			}
			for (Entry<String, GroupDefinition> entry : groups.entrySet()) {
				String groupName = entry.getKey();

				GroupDefinition groupDefinition = entry.getValue();

				GroupDefinition updatedGroupDefinition = new GroupDefinition(groupDefinition);
				updatedGroupDefinition.setMembers(null);

				// get the members of the group
				Map<String, String> members = groupDefinition.getMembers();
				if (members != null) {
					Set<String> compInstancesNames = members.keySet();

					if (componentInstances == null || true == componentInstances.isEmpty()) {
						String membersAstString = compInstancesNames.stream().collect(Collectors.joining(","));
						log.debug("The members: {}, in group: {}, cannot be found in component {}. There are no component instances.", membersAstString, groupName, component.getNormalizedName());
						return Either.right(componentsUtils.getResponseFormat(ActionStatus.GROUP_INVALID_COMPONENT_INSTANCE, membersAstString, groupName, component.getNormalizedName(), getComponentTypeForResponse(component)));
					}
					// Find all component instances with the member names
					Map<String, String> memberNames = componentInstances.stream().collect(Collectors.toMap(ComponentInstance::getName, ComponentInstance::getUniqueId));
					memberNames.putAll(groups.keySet().stream().collect(Collectors.toMap(g -> g, g -> "")));
					Map<String, String> relevantInstances = memberNames.entrySet().stream().filter(n -> compInstancesNames.contains(n.getKey())).collect(Collectors.toMap(n -> n.getKey(), n -> n.getValue()));

					if (relevantInstances == null || relevantInstances.size() != compInstancesNames.size()) {

						List<String> foundMembers = new ArrayList<>();
						if (relevantInstances != null) {
							foundMembers = relevantInstances.keySet().stream().collect(Collectors.toList());
						}
						compInstancesNames.removeAll(foundMembers);
						String membersAstString = compInstancesNames.stream().collect(Collectors.joining(","));
						log.debug("The members: {}, in group: {}, cannot be found in component: {}", membersAstString, groupName, component.getNormalizedName());
						return Either.right(componentsUtils.getResponseFormat(ActionStatus.GROUP_INVALID_COMPONENT_INSTANCE, membersAstString, groupName, component.getNormalizedName(), getComponentTypeForResponse(component)));
					}

					updatedGroupDefinition.setMembers(relevantInstances);
				}

				result.add(updatedGroupDefinition);
			}
		}
		return Either.left(result);
	}

	/**
	 * This Method validates that there is no cyclic group dependencies. meaning group A as member in group B which is member in group A
	 *
	 * @param allGroups
	 * @return
	 */
	private Either<Boolean, ResponseFormat> validateCyclicGroupsDependencies(Map<String, GroupDefinition> allGroups) {

		Either<Boolean, ResponseFormat> result = Either.left(true);
		try {
			Iterator<Entry<String, GroupDefinition>> allGroupsItr = allGroups.entrySet().iterator();
			while (allGroupsItr.hasNext() && result.isLeft()) {
				Entry<String, GroupDefinition> groupAEntry = allGroupsItr.next();
				// Fetches a group member A
				String groupAName = groupAEntry.getKey();
				// Finds all group members in group A
				Set<String> allGroupAMembersNames = new HashSet<>();
				fillAllGroupMemebersRecursivly(groupAEntry.getKey(), allGroups, allGroupAMembersNames);
				// If A is a group member of itself found cyclic dependency
				if (allGroupAMembersNames.contains(groupAName)) {
					ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GROUP_HAS_CYCLIC_DEPENDENCY, groupAName);
					result = Either.right(responseFormat);
				}
			}
		} catch (Exception e) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
			result = Either.right(responseFormat);
			log.debug("Exception occured when validateCyclicGroupsDependencies, error is:{}", e.getMessage(), e);
		}
		return result;
	}

	/**
	 * This Method fills recursively the set groupMembers with all the members of the given group which are also of type group.
	 *
	 * @param groupName
	 * @param allGroups
	 * @param allGroupMembers
	 * @return
	 */
	private void fillAllGroupMemebersRecursivly(String groupName, Map<String, GroupDefinition> allGroups, Set<String> allGroupMembers) {

		// Found Cyclic dependency
		if (isfillGroupMemebersRecursivlyStopCondition(groupName, allGroups, allGroupMembers)) {
			return;
		}
		GroupDefinition groupDefinition = allGroups.get(groupName);
		// All Members Of Current Group Resource Instances & Other Groups
		Set<String> currGroupMembers = groupDefinition.getMembers().keySet();
		// Filtered Members Of Current Group containing only members which
		// are groups
		List<String> currGroupFilteredMembers = currGroupMembers.stream().
				// Keep Only Elements of type group and not Resource Instances
						filter(innerGroupName -> allGroups.containsKey(innerGroupName)).
				// Add Filtered Elements to main Set
						peek(innerGroupName -> allGroupMembers.add(innerGroupName)).
				// Collect results
						collect(Collectors.toList());

		// Recursively call the method for all the filtered group members
		for (String innerGroupName : currGroupFilteredMembers) {
			fillAllGroupMemebersRecursivly(innerGroupName, allGroups, allGroupMembers);
		}

	}

	private boolean isfillGroupMemebersRecursivlyStopCondition(String groupName, Map<String, GroupDefinition> allGroups, Set<String> allGroupMembers) {

		boolean stop = false;
		// In Case Not Group Stop
		if (!allGroups.containsKey(groupName)) {
			stop = true;
		}
		// In Case Group Has no members stop
		if (!stop) {
			GroupDefinition groupDefinition = allGroups.get(groupName);
			stop = MapUtils.isEmpty(groupDefinition.getMembers());

		}
		// In Case all group members already contained stop
		if (!stop) {
			final Set<String> allMembers = allGroups.get(groupName).getMembers().keySet();
			Set<String> membersOfTypeGroup = allMembers.stream().
					// Filter In Only Group members
							filter(innerGroupName -> allGroups.containsKey(innerGroupName)).
					// Collect
							collect(Collectors.toSet());
			stop = allGroupMembers.containsAll(membersOfTypeGroup);
		}
		return stop;
	}

	private Either<Resource, ResponseFormat> createRIAndRelationsFromYaml(String yamlName, Resource resource, User user, Map<String, UploadComponentInstanceInfo> uploadComponentInstanceInfoMap, AuditingActionEnum actionEnum,
																		  String topologyTemplateYaml, Map<String, byte[]> csar, String csarUUID, List<ArtifactDefinition> nodeTypesNewCreatedArtifacts, Map<String, Resource> createdVfcs) {

		Either<Resource, ResponseFormat> result;
		Either<Resource, ResponseFormat> createResourcesInstancesEither;

		Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate = findNodeTypeArtifactsToCreate(csar, yamlName, resource, uploadComponentInstanceInfoMap);

		log.debug("************* Going to create all nodes {}", yamlName);
		Either<Map<String, Resource>, ResponseFormat> createdResourcesFromdNodeTypeMap = this.handleNodeTypes(yamlName, resource, user, topologyTemplateYaml, csar, false, nodeTypesArtifactsToCreate, nodeTypesNewCreatedArtifacts);
		log.debug("************* Finished to create all nodes {}", yamlName);
		if (createdResourcesFromdNodeTypeMap.isRight()) {
			log.debug("failed to resources from node types status is {}", createdResourcesFromdNodeTypeMap.right().value());
			return Either.right(createdResourcesFromdNodeTypeMap.right().value());
		}

		createdVfcs.putAll(createdResourcesFromdNodeTypeMap.left().value());

		log.debug("************* Going to create all resource instances {}", yamlName);
		createResourcesInstancesEither = createResourceInstances(user, yamlName, resource, uploadComponentInstanceInfoMap, true, false, createdResourcesFromdNodeTypeMap.left().value());

		log.debug("************* Finished to create all resource instances {}", yamlName);
		if (createResourcesInstancesEither.isRight()) {
			log.debug("failed to create resource instances status is {}", createResourcesInstancesEither.right().value());
			result = createResourcesInstancesEither;
			return createResourcesInstancesEither;
		}
		resource = createResourcesInstancesEither.left().value();
		log.debug("************* Going to create all relations {}", yamlName);
		createResourcesInstancesEither = createResourceInstancesRelations(user, yamlName, resource, uploadComponentInstanceInfoMap, true, false);

		log.debug("************* Finished to create all relations {}", yamlName);

		if (createResourcesInstancesEither.isRight()) {
			log.debug("failed to create relation between resource instances status is {}", createResourcesInstancesEither.right().value());
			result = createResourcesInstancesEither;
			return result;
		} else {
			resource = createResourcesInstancesEither.left().value();
		}

		log.debug("************* Going to create positions {}", yamlName);
		Either<List<ComponentInstance>, ResponseFormat> eitherSetPosition = compositionBusinessLogic.setPositionsForComponentInstances(resource, user.getUserId());
		log.debug("************* Finished to set positions {}", yamlName);
		result = eitherSetPosition.isRight() ? Either.right(eitherSetPosition.right().value()) : Either.left(resource);

		return result;
	}

	private Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> findNodeTypeArtifactsToCreate(Map<String, byte[]> csar, String yamlName, Resource resource, Map<String, UploadComponentInstanceInfo> uploadComponentInstanceInfoMap) {

		Map<String, List<ArtifactDefinition>> extractedVfcsArtifacts = CsarUtils.extractVfcsArtifactsFromCsar(csar);
		List<ImmutablePair<ImmutablePair<String, List<String>>, String>> extractedVfcToscaNames = extractVfcToscaNames(csar, yamlName, resource.getSystemName(), uploadComponentInstanceInfoMap);
		validateNodeTypeIdentifiers(extractedVfcsArtifacts, extractedVfcToscaNames);
		Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = null;
		if (!extractedVfcsArtifacts.isEmpty() && !extractedVfcToscaNames.isEmpty()) {
			for (ImmutablePair<ImmutablePair<String, List<String>>, String> currToscaNamePair : extractedVfcToscaNames) {
				EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>> curNodeTypeArtifacts = null;
				String currVfcToscaNamespace = currToscaNamePair.getKey().getKey();
				List<String> relatedVfcs = currToscaNamePair.getKey().getValue();
				List<ArtifactDefinition> currArtifactList = null;

				for (String currSubstitute : relatedVfcs) {
					if (extractedVfcsArtifacts.containsKey(currSubstitute)) {
						if (MapUtils.isEmpty(curNodeTypeArtifacts)) {
							curNodeTypeArtifacts = new EnumMap<>(ArtifactOperationEnum.class);
							currArtifactList = new ArrayList<>();
							curNodeTypeArtifacts.put(ArtifactOperationEnum.Create, currArtifactList);
						} else {
							currArtifactList = curNodeTypeArtifacts.get(ArtifactOperationEnum.Create);
						}
						handleAndAddExtractedVfcsArtifacts(currArtifactList, extractedVfcsArtifacts.get(currSubstitute));
					}
				}

				if (nodeTypesArtifactsToHandle == null) {
					nodeTypesArtifactsToHandle = new HashMap<>();
				}
				nodeTypesArtifactsToHandle.put(currVfcToscaNamespace, curNodeTypeArtifacts);
			}
		}
		return nodeTypesArtifactsToHandle;
	}

	private void handleAndAddExtractedVfcsArtifacts(List<ArtifactDefinition> vfcArtifacts, List<ArtifactDefinition> artifactsToAdd) {
		List<String> vfcArtifactNames = vfcArtifacts.stream().map(a -> a.getArtifactName()).collect(Collectors.toList());
		artifactsToAdd.stream().forEach(a -> {
			if (!vfcArtifactNames.contains(a.getArtifactName())) {
				vfcArtifacts.add(a);
			} else {
				log.error("Can't upload two artifact with the same name {}. ", a.getArtifactName());
			}
		});

	}

	private Either<Map<String, Resource>, ResponseFormat> handleNodeTypes(String yamlName, Resource resource, User user, String topologyTemplateYaml, Map<String, byte[]> csar, boolean needLock,
																		  Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle, List<ArtifactDefinition> nodeTypesNewCreatedArtifacts) {

		Map<String, Resource> createdResourcesFromdNodeTypeMap = new HashMap<>();
		Either<Map<String, Resource>, ResponseFormat> result = Either.left(createdResourcesFromdNodeTypeMap);

		String yamlFileName = Constants.GLOBAL_SUBSTITUTION_TYPES_SERVICE_TEMPLATE;

		if (csar != null && csar.containsKey(yamlFileName)) {
			byte[] yamlFileBytes = csar.get(yamlFileName);
			String globalTypesYaml = new String(yamlFileBytes, StandardCharsets.UTF_8);
			Either<Map<String, Resource>, ResponseFormat> createdNodeTypesFromGlobalTypesTemplateEither = this.createResourcesFromYamlNodeTypesList(yamlFileName, resource, globalTypesYaml, user, needLock, nodeTypesArtifactsToHandle,
					nodeTypesNewCreatedArtifacts);
			if (createdNodeTypesFromGlobalTypesTemplateEither.isRight()) {
				ResponseFormat responseFormat = createdNodeTypesFromGlobalTypesTemplateEither.right().value();
				componentsUtils.auditResource(responseFormat, user, resource, "", "", AuditingActionEnum.IMPORT_RESOURCE, null);
				return Either.right(responseFormat);
			}
			createdResourcesFromdNodeTypeMap.putAll(createdNodeTypesFromGlobalTypesTemplateEither.left().value());
		}

		Either<Map<String, Resource>, ResponseFormat> createdNodeTypeFromMainTemplateEither = createResourcesFromYamlNodeTypesList(yamlName, resource, topologyTemplateYaml, user, needLock, nodeTypesArtifactsToHandle, nodeTypesNewCreatedArtifacts);
		if (createdNodeTypeFromMainTemplateEither.isRight()) {
			ResponseFormat responseFormat = createdNodeTypeFromMainTemplateEither.right().value();
			componentsUtils.auditResource(responseFormat, user, resource, "", "", AuditingActionEnum.IMPORT_RESOURCE, null);
			return Either.right(responseFormat);
		}

		createdResourcesFromdNodeTypeMap.putAll(createdNodeTypeFromMainTemplateEither.left().value());

		// add the created node types to the cache although they are not in the
		// graph.
		createdResourcesFromdNodeTypeMap.values().stream().forEach(p -> cacheManagerOperation.storeComponentInCache(p, NodeTypeEnum.Resource));

		return result;
	}

	private Either<Resource, ResponseFormat> handleCsarArtifacts(Resource resource, User user, String csarUUID, Map<String, byte[]> csar, List<ArtifactDefinition> createdArtifacts, ArtifactOperationInfo artifactOperation, boolean shouldLock,
																 boolean inTransaction) {

		if (csar != null) {
			String vendorLicenseModelId = null;
			String vfLicenseModelId = null;

			if (artifactOperation.getArtifactOperationEnum() == ArtifactOperationEnum.Update) {
				Map<String, ArtifactDefinition> deploymentArtifactsMap = resource.getDeploymentArtifacts();
				if (deploymentArtifactsMap != null && !deploymentArtifactsMap.isEmpty()) {
					for (Entry<String, ArtifactDefinition> artifactEntry : deploymentArtifactsMap.entrySet()) {
						if (artifactEntry.getValue().getArtifactName().equalsIgnoreCase(Constants.VENDOR_LICENSE_MODEL))
							vendorLicenseModelId = artifactEntry.getValue().getUniqueId();
						if (artifactEntry.getValue().getArtifactName().equalsIgnoreCase(Constants.VF_LICENSE_MODEL))
							vfLicenseModelId = artifactEntry.getValue().getUniqueId();
					}
				}

			}
			// Specific Behavior for license artifacts
			createOrUpdateSingleNonMetaArtifact(resource, user, csarUUID, csar, CsarUtils.ARTIFACTS_PATH + Constants.VENDOR_LICENSE_MODEL, Constants.VENDOR_LICENSE_MODEL, ArtifactTypeEnum.VENDOR_LICENSE.getType(), ArtifactGroupTypeEnum.DEPLOYMENT,
					Constants.VENDOR_LICENSE_LABEL, Constants.VENDOR_LICENSE_DISPLAY_NAME, Constants.VENDOR_LICENSE_DESCRIPTION, vendorLicenseModelId, artifactOperation, null, shouldLock, inTransaction);
			createOrUpdateSingleNonMetaArtifact(resource, user, csarUUID, csar, CsarUtils.ARTIFACTS_PATH + Constants.VF_LICENSE_MODEL, Constants.VF_LICENSE_MODEL, ArtifactTypeEnum.VF_LICENSE.getType(), ArtifactGroupTypeEnum.DEPLOYMENT,
					Constants.VF_LICENSE_LABEL, Constants.VF_LICENSE_DISPLAY_NAME, Constants.VF_LICENSE_DESCRIPTION, vfLicenseModelId, artifactOperation, null, shouldLock, inTransaction);

			Either<Resource, ResponseFormat> eitherCreateResult = createOrUpdateNonMetaArtifacts(csarUUID, csar, resource, user, createdArtifacts, shouldLock, inTransaction, artifactOperation);
			if (eitherCreateResult.isRight()) {
				return Either.right(eitherCreateResult.right().value());
			}

			Either<ImmutablePair<String, String>, ResponseFormat> artifacsMetaCsarStatus = CsarValidationUtils.getArtifactsMeta(csar, csarUUID, componentsUtils);
			if (artifacsMetaCsarStatus.isLeft()) {

				String artifactsFileName = artifacsMetaCsarStatus.left().value().getKey();
				String artifactsContents = artifacsMetaCsarStatus.left().value().getValue();
				Either<Resource, ResponseFormat> createArtifactsFromCsar = Either.left(resource);
				if (artifactOperation.getArtifactOperationEnum() == ArtifactOperationEnum.Create)
					createArtifactsFromCsar = createResourceArtifactsFromCsar(csarUUID, csar, resource, user, artifactsContents, artifactsFileName, createdArtifacts, shouldLock, inTransaction);
				else
					createArtifactsFromCsar = updateResourceArtifactsFromCsar(csarUUID, csar, resource, user, artifactsContents, artifactsFileName, createdArtifacts, shouldLock, inTransaction);
				if (createArtifactsFromCsar.isRight()) {
					log.debug("Couldn't create artifacts from artifacts.meta");
					return Either.right(createArtifactsFromCsar.right().value());
				}

				resource = createArtifactsFromCsar.left().value();
			} else {
				List<GroupDefinition> groupsToDelete = resource.getGroups();

				if (groupsToDelete != null && !groupsToDelete.isEmpty()) {
					Set<String> artifactsToDelete = new HashSet<String>();
					/*
					 * for (GroupDefinition group : groupsToDelete) { List<String> artifacts = group.getArtifacts(); if (artifacts != null) { artifactsToDelete.addAll(artifacts); Either<GroupDefinition, StorageOperationStatus> deleteGroupEither =
					 * groupOperation.deleteGroup(group.getUniqueId(), inTransaction); if (deleteGroupEither.isRight()) { StorageOperationStatus storageOperationStatus = deleteGroupEither.right().value(); ActionStatus actionStatus =
					 * componentsUtils.convertFromStorageResponse(storageOperationStatus); log.debug("Failed to delete group {} under component {}, error: {}", group.getUniqueId(), resource.getNormalizedName(), actionStatus.name()); return
					 * Either.right(componentsUtils.getResponseFormat(actionStatus)); } } }
					 */
					for (String artifactId : artifactsToDelete) {
						Either<Either<ArtifactDefinition, Operation>, ResponseFormat> handleDelete = artifactsBusinessLogic.handleDelete(resource.getUniqueId(), artifactId, user, AuditingActionEnum.ARTIFACT_DELETE, ComponentTypeEnum.RESOURCE,
								resource, null, null, shouldLock, inTransaction);
						if (handleDelete.isRight()) {
							log.debug("Couldn't delete  artifact {}", artifactId);
							return Either.right(handleDelete.right().value());
						}
					}
					Either<Resource, StorageOperationStatus> eitherGetResource = toscaOperationFacade.getToscaElement(resource.getUniqueId());
					if (eitherGetResource.isRight()) {
						ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(eitherGetResource.right().value()), resource);

						return Either.right(responseFormat);

					}
					resource = eitherGetResource.left().value();
				}
			}
		}
		return Either.left(resource);
	}

	private Either<Boolean, ResponseFormat> createOrUpdateSingleNonMetaArtifact(Resource resource, User user, String csarUUID, Map<String, byte[]> csar, String artifactPath, String artifactFileName, String artifactType,
																				ArtifactGroupTypeEnum artifactGroupType, String artifactLabel, String artifactDisplayName, String artifactDescription, String artifactId, ArtifactOperationInfo operation, List<ArtifactDefinition> createdArtifacts, boolean shouldLock,
																				boolean inTransaction) {
		byte[] artifactFileBytes = null;

		if (csar.containsKey(artifactPath)) {
			artifactFileBytes = csar.get(artifactPath);
		}
		Either<Boolean, ResponseFormat> result = Either.left(true);
		if (operation.getArtifactOperationEnum() == ArtifactOperationEnum.Update || operation.getArtifactOperationEnum() == ArtifactOperationEnum.Delete) {
			if (artifactId != null && !artifactId.isEmpty() && artifactFileBytes == null) {
				Either<Either<ArtifactDefinition, Operation>, ResponseFormat> handleDelete = artifactsBusinessLogic.handleDelete(resource.getUniqueId(), artifactId, user, AuditingActionEnum.ARTIFACT_DELETE, ComponentTypeEnum.RESOURCE, resource, null,
						null, shouldLock, inTransaction);
				if (handleDelete.isRight()) {
					result = Either.right(handleDelete.right().value());
				}
				return result;
			}

			if ((artifactId == null || artifactId.isEmpty()) && artifactFileBytes != null) {
				operation = artifactsBusinessLogic.new ArtifactOperationInfo(false, false, ArtifactOperationEnum.Create);
			}

		}
		if (artifactFileBytes != null) {
			Map<String, Object> vendorLicenseModelJson = buildJsonForUpdateArtifact(artifactId, artifactFileName, artifactType, artifactGroupType, artifactLabel, artifactDisplayName, artifactDescription, artifactFileBytes, null);
			Either<Either<ArtifactDefinition, Operation>, ResponseFormat> eitherNonMetaArtifacts = createOrUpdateCsarArtifactFromJson(resource, user, vendorLicenseModelJson, operation, shouldLock, inTransaction);
			addNonMetaCreatedArtifactsToSupportRollback(operation, createdArtifacts, eitherNonMetaArtifacts);
			if (eitherNonMetaArtifacts.isRight()) {
				BeEcompErrorManager.getInstance().logInternalFlowError("UploadLicenseArtifact", "Failed to upload license artifact: " + artifactFileName + "With csar uuid: " + csarUUID, ErrorSeverity.WARNING);
				return Either.right(eitherNonMetaArtifacts.right().value());
			}
		}
		return result;
	}

	private void addNonMetaCreatedArtifactsToSupportRollback(ArtifactOperationInfo operation, List<ArtifactDefinition> createdArtifacts, Either<Either<ArtifactDefinition, Operation>, ResponseFormat> eitherNonMetaArtifacts) {
		if (operation.getArtifactOperationEnum() == ArtifactOperationEnum.Create && createdArtifacts != null && eitherNonMetaArtifacts.isLeft()) {
			Either<ArtifactDefinition, Operation> eitherResult = eitherNonMetaArtifacts.left().value();
			if (eitherResult.isLeft()) {
				createdArtifacts.add(eitherResult.left().value());
			}
		}
	}

	private Either<Either<ArtifactDefinition, Operation>, ResponseFormat> createOrUpdateCsarArtifactFromJson(Resource resource, User user, Map<String, Object> json, ArtifactOperationInfo operation, boolean shoudLock, boolean inTransaction) {

		String jsonStr = gson.toJson(json);

		String origMd5 = GeneralUtility.calculateMD5ByString(jsonStr);
		ArtifactDefinition artifactDefinitionFromJson = RepresentationUtils.convertJsonToArtifactDefinition(jsonStr, ArtifactDefinition.class);
		String artifactUniqueId = artifactDefinitionFromJson == null ? null : artifactDefinitionFromJson.getUniqueId();
		Either<Either<ArtifactDefinition, Operation>, ResponseFormat> uploadArtifactToService = artifactsBusinessLogic.validateAndHandleArtifact(resource.getUniqueId(), ComponentTypeEnum.RESOURCE, operation, artifactUniqueId,
				artifactDefinitionFromJson, origMd5, jsonStr, null, null, null, user, resource, false, true, false);
		if (uploadArtifactToService.isRight())
			return Either.right(uploadArtifactToService.right().value());

		return Either.left(uploadArtifactToService.left().value());
	}

	public Either<Resource, ResponseFormat> updateResourceArtifactsFromCsar(String csarUUID, Map<String, byte[]> csar, Resource resource, User user, String artifactsMetaFile, String artifactsMetaFileName, List<ArtifactDefinition> createdNewArtifacts,
																			boolean shouldLock, boolean inTransaction) {

		Either<Map<String, List<ArtifactTemplateInfo>>, ResponseFormat> parseResourceInfoFromYamlEither = parseResourceArtifactsInfoFromFile(resource, artifactsMetaFile, artifactsMetaFileName, user);
		if (parseResourceInfoFromYamlEither.isRight()) {
			ResponseFormat responseFormat = parseResourceInfoFromYamlEither.right().value();
			componentsUtils.auditResource(responseFormat, user, resource, "", "", AuditingActionEnum.IMPORT_RESOURCE, null);
			return Either.right(responseFormat);
		}

		List<GroupDefinition> groups = resource.getGroups();
		Map<String, ArtifactDefinition> deplymentArtifact = resource.getDeploymentArtifacts();
		List<ArtifactDefinition> createdDeplymentArtifactsAfterDelete = new ArrayList<ArtifactDefinition>();
		if (deplymentArtifact != null && !deplymentArtifact.isEmpty()) {
			for (Entry<String, ArtifactDefinition> entry : deplymentArtifact.entrySet()) {
				createdDeplymentArtifactsAfterDelete.add(entry.getValue());
			}
		}
		int labelCounter = createdDeplymentArtifactsAfterDelete.size();

		if (deplymentArtifact == null || deplymentArtifact.isEmpty()) {
			if (groups != null && !groups.isEmpty()) {
				for (GroupDefinition group : groups) {
					if (group.getArtifacts() != null && !group.getArtifacts().isEmpty()) {
						log.debug("failed to update artifacts from csar. List of emty but group not empty");
						ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT);
						return Either.right(responseFormat);
					}
				}
			}
			return createResourceArtifacts(csarUUID, csar, resource, user, parseResourceInfoFromYamlEither.left().value(), AuditingActionEnum.CREATE_RESOURCE, createdNewArtifacts, shouldLock, inTransaction);
		}

		// find master in group
		Map<GroupDefinition, Map<ArtifactDefinition, List<ArtifactDefinition>>> groupArtifact = findMasterArtifactInGroup(groups, deplymentArtifact);

		////////////////////////////////////// create set parsed
		////////////////////////////////////// artifacts///////////////////////////////////////////
		Map<String, List<ArtifactTemplateInfo>> parsedArtifactsMap = parseResourceInfoFromYamlEither.left().value();
		Collection<List<ArtifactTemplateInfo>> parsedArifactsCollection = parsedArtifactsMap.values();
		Map<ArtifactTemplateInfo, Set<ArtifactTemplateInfo>> parsedGroup = new HashMap<ArtifactTemplateInfo, Set<ArtifactTemplateInfo>>();

		for (List<ArtifactTemplateInfo> parsedGroupTemplateList : parsedArifactsCollection) {
			for (ArtifactTemplateInfo parsedGroupTemplate : parsedGroupTemplateList) {
				parsedGroupTemplate.setGroupName("");
				Set<ArtifactTemplateInfo> parsedArtifactsNames = new HashSet<ArtifactTemplateInfo>();
				parsedArtifactsNames.add(parsedGroupTemplate);
				List<ArtifactTemplateInfo> relatedGroupTemplateList = parsedGroupTemplate.getRelatedArtifactsInfo();
				if (relatedGroupTemplateList != null && !relatedGroupTemplateList.isEmpty()) {
					createArtifactsGroupSet(parsedGroupTemplateList, parsedArtifactsNames);
				}
				parsedGroup.put(parsedGroupTemplate, parsedArtifactsNames);
			}
		}

		///////////////////////////////// find artifacts to
		///////////////////////////////// delete////////////////////////////////////////////////////

		Set<ArtifactDefinition> artifactsToDelete = new HashSet<ArtifactDefinition>();
		Map<String, List<ArtifactDefinition>> groupToDelete = new HashMap<String, List<ArtifactDefinition>>();
		Map<String, List<String>> dissocArtifactFromGroup = new HashMap<String, List<String>>();

		Set<ArtifactTemplateInfo> jsonMasterArtifacts = parsedGroup.keySet();
		Map<GroupDefinition, MergedArtifactInfo> mergedgroup = mergeGroupInUpdateFlow(groupArtifact, parsedGroup, artifactsToDelete, groupToDelete, jsonMasterArtifacts, createdDeplymentArtifactsAfterDelete);

		// Set<String> deletedArtifactsName = new HashSet<String>();
		Either<List<ArtifactDefinition>, ResponseFormat> deletedArtifactsEither = deleteArtifactsInUpdateCsarFlow(resource, user, shouldLock, inTransaction, artifactsToDelete, groupToDelete);
		if (deletedArtifactsEither.isRight()) {
			log.debug("Failed to delete artifacts. Status is {} ", deletedArtifactsEither.right().value());

			return Either.right(deletedArtifactsEither.right().value());

		}
		List<ArtifactDefinition> deletedArtifacts = deletedArtifactsEither.left().value();

		// need to update resource if we updated artifacts
		if (deletedArtifacts != null && !deletedArtifacts.isEmpty()) {
			for (ArtifactDefinition deletedArtifact : deletedArtifacts) {
				ArtifactDefinition artToRemove = null;
				for (ArtifactDefinition artFromResource : createdDeplymentArtifactsAfterDelete) {
					if (deletedArtifact.getUniqueId().equalsIgnoreCase(artFromResource.getUniqueId())) {
						artToRemove = artFromResource;
						break;
					}
				}
				if (artToRemove != null)
					createdDeplymentArtifactsAfterDelete.remove(artToRemove);

			}
		}

		////////////// dissociate, associate or create
		////////////// artifacts////////////////////////////
		Either<Resource, ResponseFormat> assDissotiateEither = associateAndDissociateArtifactsToGroup(csarUUID, csar, resource, user, createdNewArtifacts, labelCounter, shouldLock, inTransaction, createdDeplymentArtifactsAfterDelete,
				 mergedgroup, deletedArtifacts);

		if (assDissotiateEither.isRight()) {
			log.debug("Failed to delete artifacts. Status is {} ", assDissotiateEither.right().value());

			return Either.right(assDissotiateEither.right().value());

		}
		resource = assDissotiateEither.left().value();
		deplymentArtifact = resource.getDeploymentArtifacts();
		createdDeplymentArtifactsAfterDelete.clear();
		if (deplymentArtifact != null && !deplymentArtifact.isEmpty()) {
			for (Entry<String, ArtifactDefinition> entry : deplymentArtifact.entrySet()) {
				createdDeplymentArtifactsAfterDelete.add(entry.getValue());
			}
		}

		groups = resource.getGroups();
		List<GroupDefinition> groupToUpdate = new ArrayList<>();
		// update vfModule names
		Set<GroupDefinition> groupForAssociateWithMembers = mergedgroup.keySet();
		if (groups != null && !groups.isEmpty()) {
			Either<List<GroupDefinition>, ResponseFormat> validateUpdateVfGroupNamesRes = groupBusinessLogic.validateUpdateVfGroupNamesOnGraph(groups, resource, inTransaction);
			if (validateUpdateVfGroupNamesRes.isRight()) {
				return Either.right(validateUpdateVfGroupNamesRes.right().value());
			}
			List<GroupDefinition> heatGroups = null;

			heatGroups = groups.stream().filter(e -> e.getMembers() != null).collect(Collectors.toList());
			;

			for (GroupDefinition updatedGroupDef : groupForAssociateWithMembers) {
				GroupDefinition group = null;
				Optional<GroupDefinition> opGr = groups.stream().filter(p -> p.getUniqueId().equals(updatedGroupDef.getUniqueId())).findAny();
				if (opGr.isPresent()) {
					group = opGr.get();
					groupToUpdate.add(group);
				}
				if (group != null) {
					Map<String, String> members = new HashMap<String, String>();
					Set<String> artifactsGroup = new HashSet<String>();
					artifactsGroup.addAll(group.getArtifacts());
					associateMembersToArtifacts(createdNewArtifacts, createdDeplymentArtifactsAfterDelete, heatGroups, artifactsGroup, members);
					if (!members.isEmpty()) {
						group.setMembers(members);

					}
				}

			}
			/*
			 * if (!groupToUpdate.isEmpty()) { Either<List<GroupDefinition>, ResponseFormat> assotiateGroupEither = groupBusinessLogic.associateMembersToGroup(resource.getUniqueId(), user.getUserId(), ComponentTypeEnum.RESOURCE, groupToUpdate, false,
			 * true); if (assotiateGroupEither.isRight()) { log.debug("Failed to associate artifacts to groups. Status is {} ", assotiateGroupEither.right().value()); return Either.right(assotiateGroupEither.right().value());
			 *
			 * } }
			 */

		}

		//////////////// create new artifacts in update
		//////////////// flow////////////////////////////
		List<ArtifactTemplateInfo> newArtifactsGroup = new ArrayList<ArtifactTemplateInfo>();

		for (Entry<ArtifactTemplateInfo, Set<ArtifactTemplateInfo>> parsedGroupSetEntry : parsedGroup.entrySet()) {
			ArtifactTemplateInfo parsedArtifactMaster = parsedGroupSetEntry.getKey();
			boolean isNewGroup = true;
			for (Entry<GroupDefinition, Map<ArtifactDefinition, List<ArtifactDefinition>>> groupListEntry : groupArtifact.entrySet()) {
				Map<ArtifactDefinition, List<ArtifactDefinition>> groupArtifacts = groupListEntry.getValue();
				Set<ArtifactDefinition> group = groupArtifacts.keySet();
				for (ArtifactDefinition artifactInfo : group) {
					if (parsedArtifactMaster.getFileName().equalsIgnoreCase(artifactInfo.getArtifactName())) {
						parsedArtifactMaster.setGroupName(groupListEntry.getKey().getName());
						isNewGroup = false;
					}
				}
			}
			if (isNewGroup)
				newArtifactsGroup.add(parsedArtifactMaster);

		}
		if (!newArtifactsGroup.isEmpty()) {
			Collections.sort(newArtifactsGroup, (art1, art2) -> ArtifactTemplateInfo.compareByGroupName(art1, art2));
			int startGroupCounter = groupBusinessLogic.getNextVfModuleNameCounter(groups);
			Either<Boolean, ResponseFormat> validateGroupNamesRes = groupBusinessLogic.validateGenerateVfModuleGroupNames(newArtifactsGroup, resource.getSystemName(), startGroupCounter);
			if (validateGroupNamesRes.isRight()) {
				return Either.right(validateGroupNamesRes.right().value());
			}
			Either<Resource, ResponseFormat> resStatus = createGroupDeploymentArtifactsFromCsar(csarUUID, csar, resource, user, newArtifactsGroup, createdNewArtifacts, createdDeplymentArtifactsAfterDelete, labelCounter, shouldLock, inTransaction);
			if (resStatus.isRight())
				return resStatus;
		}

		// updatedGroup
		if (!groupForAssociateWithMembers.isEmpty()) {

			List<GroupDefinition> groupsId = groupForAssociateWithMembers.stream().map(e -> e).collect(Collectors.toList());

			Either<List<GroupDefinition>, ResponseFormat> updateVersionEither = groupBusinessLogic.updateGroups(resource, ComponentTypeEnum.RESOURCE, groupsId);
			if (updateVersionEither.isRight()) {
				log.debug("Failed to update groups version. Status is {} ", updateVersionEither.right().value());

				return Either.right(updateVersionEither.right().value());

			}
		}

		Either<Resource, StorageOperationStatus> eitherGerResource = toscaOperationFacade.getToscaElement(resource.getUniqueId());
		if (eitherGerResource.isRight()) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(eitherGerResource.right().value()), resource);

			return Either.right(responseFormat);

		}
		return Either.left(eitherGerResource.left().value());

	}

	private Either<List<ArtifactDefinition>, ResponseFormat> deleteArtifactsInUpdateCsarFlow(Resource resource, User user, boolean shouldLock, boolean inTransaction, Set<ArtifactDefinition> artifactsToDelete,
																							 Map<String, List<ArtifactDefinition>> groupToDelete) {
		List<ArtifactDefinition> deletedArtifacts = new ArrayList<ArtifactDefinition>();
		String resourceId = resource.getUniqueId();
		if (!artifactsToDelete.isEmpty()) {
			for (ArtifactDefinition artifact : artifactsToDelete) {
				String artifactType = artifact.getArtifactType();
				ArtifactTypeEnum artifactTypeEnum = ArtifactTypeEnum.findType(artifactType);
				if (artifactTypeEnum == ArtifactTypeEnum.HEAT_ENV) {

					/*
					 * Either<ArtifactDefinition, StorageOperationStatus> removeArifactFromGraph = artifactOperation.removeArifactFromResource(resourceId, artifact.getUniqueId(), NodeTypeEnum.Resource, true, true); if
					 * (removeArifactFromGraph.isRight()) { StorageOperationStatus status = removeArifactFromGraph.right().value(); log.debug("Failed to delete heat env artifact  {} . status is {}", artifact.getUniqueId(), status); ActionStatus
					 * actionStatus = componentsUtils.convertFromStorageResponse(status); return Either.right(componentsUtils.getResponseFormat(actionStatus)); }
					 *
					 * deletedArtifacts.add(removeArifactFromGraph.left().value());
					 */

				}

				else {
					Either<Either<ArtifactDefinition, Operation>, ResponseFormat> handleDelete = artifactsBusinessLogic.handleDelete(resourceId, artifact.getUniqueId(), user, AuditingActionEnum.ARTIFACT_DELETE, ComponentTypeEnum.RESOURCE, resource,
							null, null, shouldLock, inTransaction);
					if (handleDelete.isRight()) {
						return Either.right(handleDelete.right().value());
					}

					deletedArtifacts.add(handleDelete.left().value().left().value());
				}

			}
		}
		if (!groupToDelete.isEmpty()) {
			log.debug("try to delete group");
			List<GroupDefinition> groupDefinitionstoDelete = new ArrayList<>();
			List<GroupDefinition> groups = resource.getGroups();
			for (Entry<String, List<ArtifactDefinition>> deleteGroup : groupToDelete.entrySet()) {
				Optional<GroupDefinition> op = groups.stream().filter(gr -> gr.getUniqueId().equals(deleteGroup.getKey())).findAny();
				if (op.isPresent()) {
					groupDefinitionstoDelete.add(op.get());
				}

			}
			if (!groupDefinitionstoDelete.isEmpty()) {
				Either<List<GroupDefinition>, ResponseFormat> prepareGroups = groupBusinessLogic.deleteGroups(resource, user, ComponentTypeEnum.RESOURCE, groupDefinitionstoDelete);
				if (prepareGroups.isRight()) {
					return Either.right(prepareGroups.right().value());
				}
			}
		}
		return Either.left(deletedArtifacts);
	}

	private Either<Resource, ResponseFormat> associateAndDissociateArtifactsToGroup(String csarUUID, Map<String, byte[]> csar, Resource resource, User user, List<ArtifactDefinition> createdNewArtifacts, int labelCounter, boolean shouldLock,
																					boolean inTransaction, List<ArtifactDefinition> createdDeplymentArtifactsAfterDelete, Map<GroupDefinition, MergedArtifactInfo> mergedgroup, List<ArtifactDefinition> deletedArtifacts) {
		Map<GroupDefinition, List<ArtifactTemplateInfo>> artifactsToAssotiate = new HashMap<GroupDefinition, List<ArtifactTemplateInfo>>();
		Map<GroupDefinition, List<ImmutablePair<ArtifactDefinition, ArtifactTemplateInfo>>> artifactsToUpdateMap = new HashMap<GroupDefinition, List<ImmutablePair<ArtifactDefinition, ArtifactTemplateInfo>>>();
		Either<Resource, ResponseFormat> resEither = Either.left(resource);
		for (Entry<GroupDefinition, MergedArtifactInfo> entry : mergedgroup.entrySet()) {
			List<ArtifactDefinition> dissArtifactsInGroup = entry.getValue().getListToDissotiateArtifactFromGroup(deletedArtifacts);
			GroupDefinition grDef = entry.getKey();
			if (dissArtifactsInGroup != null && !dissArtifactsInGroup.isEmpty()) {
				for (ArtifactDefinition art : dissArtifactsInGroup) {
					grDef.getArtifacts().remove(art.getUniqueId());
					grDef.getArtifactsUuid().remove(art.getArtifactUUID());
				}
			}

			List<ArtifactTemplateInfo> newArtifactsInGroup = entry.getValue().getListToAssociateArtifactToGroup();
			if (newArtifactsInGroup != null && !newArtifactsInGroup.isEmpty())
				artifactsToAssotiate.put(entry.getKey(), newArtifactsInGroup);
			

			List<ImmutablePair<ArtifactDefinition, ArtifactTemplateInfo>> artifactsToUpdate = entry.getValue().getListToUpdateArtifactInGroup();
			if (artifactsToUpdate != null && !artifactsToUpdate.isEmpty())
				artifactsToUpdateMap.put(entry.getKey(), artifactsToUpdate);
		}

		if (!artifactsToUpdateMap.isEmpty()) {
			List<ArtifactDefinition> updatedArtifacts = new ArrayList<ArtifactDefinition>();
			for (Entry<GroupDefinition, List<ImmutablePair<ArtifactDefinition, ArtifactTemplateInfo>>> artifactsToUpdateEntry : artifactsToUpdateMap.entrySet()) {
				List<ImmutablePair<ArtifactDefinition, ArtifactTemplateInfo>> artifactsToUpdateList = artifactsToUpdateEntry.getValue();
				GroupDefinition groupToUpdate = artifactsToUpdateEntry.getKey();
				
				for (ImmutablePair<ArtifactDefinition, ArtifactTemplateInfo> artifact : artifactsToUpdateList) {
					String prevUUID = artifact.getKey().getArtifactUUID();
					String prevId = artifact.getKey().getUniqueId();
					Either<ArtifactDefinition, ResponseFormat> updateArtifactEither = updateDeploymentArtifactsFromCsar(csarUUID, csar, resource, user, artifact.getKey(), artifact.getValue(), updatedArtifacts,
							artifact.getRight().getRelatedArtifactsInfo(), shouldLock, inTransaction);
					if (updateArtifactEither.isRight()) {
						log.debug("failed to update artifacts. status is {}", updateArtifactEither.right().value());
						resEither = Either.right(updateArtifactEither.right().value());
						return resEither;
					}
					ArtifactDefinition artAfterUpdate = updateArtifactEither.left().value();
					if ( !prevUUID.equals(artAfterUpdate.getArtifactUUID()) ||  !prevId.equals(artAfterUpdate.getUniqueId()) ){
						groupToUpdate.getArtifacts().remove(prevId);
						groupToUpdate.getArtifactsUuid().remove(prevUUID);
						groupToUpdate.getArtifacts().add(artAfterUpdate.getUniqueId());
						groupToUpdate.getArtifactsUuid().add(artAfterUpdate.getArtifactUUID());
					}
				}
			}
		}

		List<GroupDefinition> associateArtifactGroup = new ArrayList<GroupDefinition>();

		for (Entry<GroupDefinition, List<ArtifactTemplateInfo>> associateEntry : artifactsToAssotiate.entrySet()) {
			List<ArtifactTemplateInfo> associatedArtifact = associateEntry.getValue();
			Set<String> arifactsUids = new HashSet<String>();
			for (ArtifactTemplateInfo artifactTemplate : associatedArtifact) { // try
				// to
				// find
				// artifact
				// in
				// resource
				boolean isCreate = true;
				for (ArtifactDefinition createdArtifact : createdDeplymentArtifactsAfterDelete) {
					if (artifactTemplate.getFileName().equalsIgnoreCase(createdArtifact.getArtifactName())) {
						arifactsUids.add(createdArtifact.getUniqueId());
						isCreate = false;
						String heatEnvId = checkAndGetHeatEnvId(createdArtifact);
						if (!heatEnvId.isEmpty()) {
							arifactsUids.add(heatEnvId);
							Optional<ArtifactDefinition> op = createdDeplymentArtifactsAfterDelete.stream().filter(p -> p.getUniqueId().equals(heatEnvId)).findAny();
							if (op.isPresent()) {
								this.artifactToscaOperation.updateHeatEnvPlaceholder(op.get(), resource.getUniqueId(), resource.getComponentType().getNodeType());

							}
						}

						break;
					}

				}
				if (isCreate) { // check if already created
					for (ArtifactDefinition createdNewArtifact : createdNewArtifacts) {
						if (artifactTemplate.getFileName().equalsIgnoreCase(createdNewArtifact.getArtifactName())) {
							arifactsUids.add(createdNewArtifact.getUniqueId());
							isCreate = false;
							String heatEnvId = checkAndGetHeatEnvId(createdNewArtifact);
							if (!heatEnvId.isEmpty()) {
								arifactsUids.add(heatEnvId);
							}
							break;
						}
					}
				}

				if (isCreate) {
					Either<ArtifactDefinition, ResponseFormat> createArtifactEither = createDeploymentArtifact(csarUUID, csar, resource, user, ARTIFACTS_PATH, artifactTemplate, createdNewArtifacts, labelCounter, shouldLock, inTransaction);
					if (createArtifactEither.isRight()) {
						resEither = Either.right(createArtifactEither.right().value());
						return resEither;
					}
					ArtifactDefinition createdArtifact = createArtifactEither.left().value();
					arifactsUids.add(createdArtifact.getUniqueId());
					ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(createdArtifact.getArtifactType());
					if (artifactType == ArtifactTypeEnum.HEAT || artifactType == ArtifactTypeEnum.HEAT_NET || artifactType == ArtifactTypeEnum.HEAT_VOL) {
						Either<ArtifactDefinition, ResponseFormat> createHeatEnvPlaceHolder = artifactsBusinessLogic.createHeatEnvPlaceHolder(createdArtifact, ArtifactsBusinessLogic.HEAT_VF_ENV_NAME, resource.getUniqueId(), NodeTypeEnum.Resource,
								resource.getName(), user, resource, null);
						if (createHeatEnvPlaceHolder.isRight()) {
							return Either.right(createHeatEnvPlaceHolder.right().value());
						}
						String heatEnvId = createHeatEnvPlaceHolder.left().value().getUniqueId();
						arifactsUids.add(heatEnvId);
					}
				}

			}
			if (arifactsUids.size() > 0) {
				List<String> artifactsToAssociate = new ArrayList<String>();
				artifactsToAssociate.addAll(arifactsUids);
				GroupDefinition assotiateGroup = new GroupDefinition();
				assotiateGroup.setUniqueId(associateEntry.getKey().getUniqueId());
				assotiateGroup.setArtifacts(artifactsToAssociate);
				associateArtifactGroup.add(assotiateGroup);

			}
		}

		/*
		 * if (!associateArtifactGroup.isEmpty()) {
		 *
		 * log.debug("Try to associate artifacts to groups.");
		 *
		 * Either<List<GroupDefinition>, ResponseFormat> assotiateGroupEither = groupBusinessLogic.associateArtifactsToGroup(resource.getUniqueId(), user.getUserId(), ComponentTypeEnum.RESOURCE, associateArtifactGroup, shouldLock, inTransaction); if
		 * (assotiateGroupEither.isRight()) { log.debug("Failed to associate artifacts to groups. Status is {} ", assotiateGroupEither.right().value()); resEither = Either.right(assotiateGroupEither.right().value()); return resEither;
		 *
		 * } }
		 */

		ComponentParametersView parametersView = new ComponentParametersView();
		parametersView.disableAll();
		parametersView.setIgnoreComponentInstances(false);
		parametersView.setIgnoreUsers(false);
		parametersView.setIgnoreArtifacts(false);
		parametersView.setIgnoreGroups(false);

		Either<Resource, StorageOperationStatus> eitherGerResource = toscaOperationFacade.getToscaElement(resource.getUniqueId(), parametersView);

		if (eitherGerResource.isRight()) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(eitherGerResource.right().value()), resource);

			resEither = Either.right(responseFormat);
			return resEither;

		}
		resEither = Either.left(eitherGerResource.left().value());
		return resEither;
	}

	private Map<GroupDefinition, MergedArtifactInfo> mergeGroupInUpdateFlow(Map<GroupDefinition, Map<ArtifactDefinition, List<ArtifactDefinition>>> groupArtifact, Map<ArtifactTemplateInfo, Set<ArtifactTemplateInfo>> parsedGroup,
																			Set<ArtifactDefinition> artifactsToDelete, Map<String, List<ArtifactDefinition>> groupToDelete, Set<ArtifactTemplateInfo> jsonMasterArtifacts, List<ArtifactDefinition> createdDeplymentArtifacts) {
		Map<GroupDefinition, MergedArtifactInfo> mergedgroup = new HashMap<GroupDefinition, MergedArtifactInfo>();
		for (Entry<GroupDefinition, Map<ArtifactDefinition, List<ArtifactDefinition>>> groupListEntry : groupArtifact.entrySet()) {
			Map<ArtifactDefinition, List<ArtifactDefinition>> createdArtifactMap = groupListEntry.getValue();
			boolean isNeedToDeleteGroup = true;
			List<ArtifactDefinition> listToDelete = null;
			for (ArtifactDefinition maserArtifact : createdArtifactMap.keySet()) {
				listToDelete = createdArtifactMap.get(maserArtifact);
				for (ArtifactDefinition artToDelete : listToDelete) {
					findArtifactToDelete(parsedGroup, artifactsToDelete, groupListEntry.getKey().getUniqueId(), artToDelete, createdDeplymentArtifacts);
				}
				if(artifactsToDelete != null && !artifactsToDelete.isEmpty()){
					GroupDefinition group = groupListEntry.getKey();
					for(ArtifactDefinition artifactDefinition: artifactsToDelete){
						if (CollectionUtils.isNotEmpty(group.getArtifacts()) && group.getArtifacts().contains(artifactDefinition.getUniqueId())) {
							group.getArtifacts().remove(artifactDefinition.getUniqueId());
							
						}
						if (CollectionUtils.isNotEmpty(group.getArtifactsUuid()) && group.getArtifactsUuid().contains(artifactDefinition.getArtifactUUID())) {
							group.getArtifactsUuid().remove(artifactDefinition.getArtifactUUID());
							
						}
					}
					
				}
			
				for (ArtifactTemplateInfo jsonMasterArtifact : jsonMasterArtifacts) {
					if (maserArtifact.getArtifactName().equalsIgnoreCase(jsonMasterArtifact.getFileName())) {
						MergedArtifactInfo mergedGroup = new MergedArtifactInfo();
						mergedGroup.setJsonArtifactTemplate(jsonMasterArtifact);
						mergedGroup.setCreatedArtifact(createdArtifactMap.get(maserArtifact));
						mergedgroup.put(groupListEntry.getKey(), mergedGroup);
						isNeedToDeleteGroup = false;

					}
				}

			}
			if (isNeedToDeleteGroup) {
				groupToDelete.put(groupListEntry.getKey().getUniqueId(), listToDelete);
			}

		}
		return mergedgroup;
	}

	private Set<String> findArtifactsNotInGroupToDelete(List<GroupDefinition> groups, List<ArtifactDefinition> createdDeplymentArtifactsAfterDelete) {
		Set<String> artifactNotInGroupSet = new HashSet<String>();
		for (ArtifactDefinition artifact : createdDeplymentArtifactsAfterDelete) {
			boolean needToDelete = true;
			if (artifact.getArtifactName().equalsIgnoreCase(Constants.VENDOR_LICENSE_MODEL) || artifact.getArtifactName().equalsIgnoreCase(Constants.VF_LICENSE_MODEL))
				continue;
			if (groups != null) {
				for (GroupDefinition group : groups) {
					List<String> groupArtifactIds = group.getArtifacts();
					if (groupArtifactIds == null || groupArtifactIds.isEmpty()) {
						continue;
					}
					for (String groupArtifactid : groupArtifactIds) {
						if (groupArtifactid.equalsIgnoreCase(artifact.getUniqueId()))
							needToDelete = false;

					}

				}
			}
			if (needToDelete)
				artifactNotInGroupSet.add(artifact.getUniqueId());
		}
		return artifactNotInGroupSet;
	}

	private void findArtifactToDelete(Map<ArtifactTemplateInfo, Set<ArtifactTemplateInfo>> parsedGroup, Set<ArtifactDefinition> artifactsToDelete, String deleteGroupId, ArtifactDefinition artifact,
									  List<ArtifactDefinition> createdDeplymentArtifacts) {
		boolean isNeedToDeleteArtifact = true;
		String artifactType = artifact.getArtifactType();
		ArtifactDefinition generatedFromArt = null;
		if (artifact.getGeneratedFromId() != null && !artifact.getGeneratedFromId().isEmpty()) {
			Optional<ArtifactDefinition> op = createdDeplymentArtifacts.stream().filter(p -> p.getUniqueId().equals(artifact.getGeneratedFromId())).findAny();
			if (op.isPresent())
				generatedFromArt = op.get();

		}

		for (Entry<ArtifactTemplateInfo, Set<ArtifactTemplateInfo>> parsedGroupSetEntry : parsedGroup.entrySet()) {
			Set<ArtifactTemplateInfo> artifactsNames = parsedGroupSetEntry.getValue();
			for (ArtifactTemplateInfo template : artifactsNames) {
				if (artifact.getArtifactName().equalsIgnoreCase(template.getFileName()) && artifactType.equalsIgnoreCase(template.getType())) {
					isNeedToDeleteArtifact = false;
					break;

				} else {
					if (generatedFromArt != null) {
						if (generatedFromArt.getArtifactName().equalsIgnoreCase(template.getFileName()) && generatedFromArt.getArtifactType().equalsIgnoreCase(template.getType())) {
							isNeedToDeleteArtifact = false;
							break;
						}
					}
				}
			}

		}
		if (isNeedToDeleteArtifact) {
			artifactsToDelete.add(artifact);

		}
	}

	private Map<GroupDefinition, Map<ArtifactDefinition, List<ArtifactDefinition>>> findMasterArtifactInGroup(List<GroupDefinition> groups, Map<String, ArtifactDefinition> deplymentArtifact) {
		Map<GroupDefinition, Map<ArtifactDefinition, List<ArtifactDefinition>>> groupArtifact = new HashMap<GroupDefinition, Map<ArtifactDefinition, List<ArtifactDefinition>>>();

		for (GroupDefinition group : groups) {
			Map<ArtifactDefinition, List<ArtifactDefinition>> gupsMap = new HashMap<ArtifactDefinition, List<ArtifactDefinition>>();
			List<ArtifactDefinition> artifacts = new ArrayList<ArtifactDefinition>();
			List<String> artifactsList = group.getArtifacts();
			if (artifactsList != null && !artifactsList.isEmpty()) {

				ArtifactDefinition masterArtifact = ArtifactUtils.findMasterArtifact(deplymentArtifact, artifacts, artifactsList);
				if (masterArtifact != null)
					gupsMap.put(masterArtifact, artifacts);
				groupArtifact.put(group, gupsMap);

			}
		}
		return groupArtifact;
	}

	private void createArtifactsGroupSet(List<ArtifactTemplateInfo> parsedGroupTemplateList, Set<ArtifactTemplateInfo> parsedArtifactsName) {

		for (ArtifactTemplateInfo parsedGroupTemplate : parsedGroupTemplateList) {
			parsedArtifactsName.add(parsedGroupTemplate);
			List<ArtifactTemplateInfo> relatedArtifacts = parsedGroupTemplate.getRelatedArtifactsInfo();
			if (relatedArtifacts != null && !relatedArtifacts.isEmpty()) {
				createArtifactsGroupSet(relatedArtifacts, parsedArtifactsName);
			}
		}
	}

	public Either<Resource, ResponseFormat> createResourceArtifactsFromCsar(String csarUUID, Map<String, byte[]> csar, Resource resource, User user, String artifactsMetaFile, String artifactsMetaFileName, List<ArtifactDefinition> createdArtifacts,
																			boolean shouldLock, boolean inTransaction) {

		log.debug("parseResourceArtifactsInfoFromFile start");
		Either<Map<String, List<ArtifactTemplateInfo>>, ResponseFormat> parseResourceInfoFromYamlEither = parseResourceArtifactsInfoFromFile(resource, artifactsMetaFile, artifactsMetaFileName, user);
		if (parseResourceInfoFromYamlEither.isRight()) {
			ResponseFormat responseFormat = parseResourceInfoFromYamlEither.right().value();
			componentsUtils.auditResource(responseFormat, user, resource, "", "", AuditingActionEnum.IMPORT_RESOURCE, null);
			return Either.right(responseFormat);
		}
		log.debug("parseResourceArtifactsInfoFromFile end");

		log.debug("createResourceArtifacts start");
		Either<Resource, ResponseFormat> respStatus = createResourceArtifacts(csarUUID, csar, resource, user, parseResourceInfoFromYamlEither.left().value(), AuditingActionEnum.CREATE_RESOURCE, createdArtifacts, shouldLock, inTransaction);
		if (respStatus.isRight()) {
			return respStatus;
		}
		log.debug("createResourceArtifacts end");
		log.debug("getResource start");
		Either<Resource, StorageOperationStatus> eitherGerResource = toscaOperationFacade.getToscaElement(resource.getUniqueId());
		log.debug("getResource end");
		if (eitherGerResource.isRight()) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(eitherGerResource.right().value()), resource);

			return Either.right(responseFormat);

		}
		return Either.left(eitherGerResource.left().value());

	}

	private Either<Resource, ResponseFormat> createGroupDeploymentArtifactsFromCsar(String csarUUID, Map<String, byte[]> csar, Resource resource, User user, List<ArtifactTemplateInfo> artifactsTemplateList,
																					List<ArtifactDefinition> createdNewArtifacts, List<ArtifactDefinition> artifactsFromResource, int labelCounter, boolean shouldLock, boolean inTransaction) {
		Either<Resource, ResponseFormat> resStatus = Either.left(resource);
		List<GroupDefinition> createdGroups = resource.getGroups();
		List<GroupDefinition> heatGroups = null;
		if (createdGroups != null && !createdGroups.isEmpty()) {
			heatGroups = createdGroups.stream().filter(e -> e.getMembers() != null).collect(Collectors.toList());
		}

		List<GroupDefinition> needToAdd = new ArrayList<>();
		for (ArtifactTemplateInfo groupTemplateInfo : artifactsTemplateList) {
			String groupName = groupTemplateInfo.getGroupName();
			Set<String> artifactsGroup = new HashSet<String>();
			Set<String> artifactsUUIDGroup = new HashSet<String>();

			resStatus = createDeploymentArtifactsFromCsar(csarUUID, csar, resource, user, artifactsGroup, artifactsUUIDGroup, groupTemplateInfo, createdNewArtifacts, artifactsFromResource, labelCounter, shouldLock, inTransaction);
			if (resStatus.isRight())
				return resStatus;

			Map<String, String> members = new HashMap<String, String>();
			associateMembersToArtifacts(createdNewArtifacts, artifactsFromResource, heatGroups, artifactsGroup, members);

			List<String> artifactsList = new ArrayList<String>(artifactsGroup);
			List<String> artifactsUUIDList = new ArrayList<String>(artifactsUUIDGroup);

			GroupDefinition groupDefinition = new GroupDefinition();
			groupDefinition.setName(groupName);
			groupDefinition.setType(Constants.DEFAULT_GROUP_VF_MODULE);
			groupDefinition.setArtifacts(artifactsList);
			groupDefinition.setArtifactsUuid(artifactsUUIDList);

			if (!members.isEmpty())
				groupDefinition.setMembers(members);

			List<GroupProperty> properties = new ArrayList<GroupProperty>();
			GroupProperty prop = new GroupProperty();
			prop.setName(Constants.IS_BASE);
			prop.setValue(Boolean.toString(groupTemplateInfo.isBase()));
			properties.add(prop);
			
			List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
			createdArtifacts.addAll(createdNewArtifacts);
			createdArtifacts.addAll(artifactsFromResource);
			Either<GroupTypeDefinition, StorageOperationStatus> getLatestGroupTypeRes = groupTypeOperation.getLatestGroupTypeByType(Constants.DEFAULT_GROUP_VF_MODULE, true);
			if (getLatestGroupTypeRes.isRight()) {
				return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(getLatestGroupTypeRes.right().value())));
			}
			properties = createVfModuleAdditionalProperties(groupTemplateInfo.isBase(), groupName, properties, createdArtifacts, artifactsList, getLatestGroupTypeRes.left().value());
			groupDefinition.convertFromGroupProperties(properties);

			// Either<GroupDefinition, ResponseFormat> createGroup = groupBusinessLogic.createGroup(resource.getUniqueId(), user.getUserId(), ComponentTypeEnum.RESOURCE, groupDefinition, inTransaction);
			// if (createGroup.isRight())
			// return Either.right(createGroup.right().value());
			needToAdd.add(groupDefinition);
		}
		ComponentParametersView componentParametersView = new ComponentParametersView();
		componentParametersView.disableAll();
		componentParametersView.setIgnoreArtifacts(false);
		componentParametersView.setIgnoreGroups(false);
		componentParametersView.setIgnoreComponentInstances(false);

		Either<Resource, StorageOperationStatus> component = toscaOperationFacade.getToscaElement(resource.getUniqueId(), componentParametersView);
		if (component.isRight()) {
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
		}
		resource = component.left().value();

		Either<List<GroupDefinition>, ResponseFormat> addGroups = groupBusinessLogic.addGroups(resource, user, ComponentTypeEnum.RESOURCE, needToAdd);
		if (addGroups.isRight())
			return Either.right(addGroups.right().value());

		return resStatus;
	}

	private Either<Resource, ResponseFormat> createDeploymentArtifactsFromCsar(String csarUUID, Map<String, byte[]> csar, Resource resource, User user, Set<String> artifactsGroup, Set<String> artifactsUUIDGroup,
																			   ArtifactTemplateInfo artifactTemplateInfo, List<ArtifactDefinition> createdArtifacts, List<ArtifactDefinition> artifactsFromResource, int labelCounter, boolean shoudLock, boolean inTransaction) {
		Either<Resource, ResponseFormat> resStatus = Either.left(resource);
		String artifactFileName = artifactTemplateInfo.getFileName();
		String artifactUid = "";
		String artifactUUID = "";
		String artifactEnvUid = "";
		boolean alreadyExist = false;

		// check if artifacts already exist
		if (artifactsFromResource != null && !artifactsFromResource.isEmpty()) {
			for (ArtifactDefinition artifactFromResource : artifactsFromResource) {
				if (artifactFromResource.getArtifactName().equals(artifactFileName)) {
					artifactUid = artifactFromResource.getUniqueId();
					artifactUUID = artifactFromResource.getArtifactUUID();
					if (!artifactFromResource.getArtifactType().equalsIgnoreCase(artifactTemplateInfo.getType())) {
						log.debug("Artifact with name {} and type {} already exist with type  {}", artifactFileName, artifactTemplateInfo.getType(), artifactFromResource.getArtifactType());
						BeEcompErrorManager.getInstance().logInternalDataError("Artifact  file is not in expected formatr, fileName " + artifactFileName, "Artifact internals are invalid", ErrorSeverity.ERROR);
						return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_ALRADY_EXIST_IN_DIFFERENT_TYPE_IN_CSAR, artifactFileName, artifactTemplateInfo.getType(), artifactFromResource.getArtifactType()));
					}
					alreadyExist = true;
					artifactEnvUid = checkAndGetHeatEnvId(artifactFromResource);
					break;
				}

			}

		}
		if (!alreadyExist) {
			for (ArtifactDefinition createdArtifact : createdArtifacts) {
				if (createdArtifact.getArtifactName().equals(artifactFileName)) {
					artifactUid = createdArtifact.getUniqueId();
					artifactUUID = createdArtifact.getArtifactUUID();

					if (!createdArtifact.getArtifactType().equalsIgnoreCase(artifactTemplateInfo.getType())) {
						log.debug("Artifact with name {} and type {} already exist with type  {}", artifactFileName, artifactTemplateInfo.getType(), createdArtifact.getArtifactType());
						BeEcompErrorManager.getInstance().logInternalDataError("Artifact  file is not in expected formatr, fileName " + artifactFileName, "Artifact internals are invalid", ErrorSeverity.ERROR);
						return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_ALRADY_EXIST_IN_DIFFERENT_TYPE_IN_CSAR, artifactFileName, artifactTemplateInfo.getType(), createdArtifact.getArtifactType()));
					}
					alreadyExist = true;
					artifactEnvUid = checkAndGetHeatEnvId(createdArtifact);
					break;
				}

			}
		}
		// if not exist need to create
		if (!alreadyExist) {

			Either<ArtifactDefinition, ResponseFormat> newArtifactEither = createDeploymentArtifact(csarUUID, csar, resource, user, ARTIFACTS_PATH, artifactTemplateInfo, createdArtifacts, labelCounter, shoudLock, inTransaction);
			if (newArtifactEither.isRight()) {
				resStatus = Either.right(newArtifactEither.right().value());
				return resStatus;
			}
			ArtifactDefinition newArtifact = newArtifactEither.left().value();
			artifactUid = newArtifact.getUniqueId();
			artifactUUID = newArtifact.getArtifactUUID();
			ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(newArtifact.getArtifactType());
			if (artifactType == ArtifactTypeEnum.HEAT || artifactType == ArtifactTypeEnum.HEAT_NET || artifactType == ArtifactTypeEnum.HEAT_VOL) {
				Either<ArtifactDefinition, ResponseFormat> createHeatEnvPlaceHolder = artifactsBusinessLogic.createHeatEnvPlaceHolder(newArtifact, ArtifactsBusinessLogic.HEAT_VF_ENV_NAME, resource.getUniqueId(), NodeTypeEnum.Resource,
						resource.getName(), user, resource, null);
				if (createHeatEnvPlaceHolder.isRight()) {
					return Either.right(createHeatEnvPlaceHolder.right().value());
				}
				artifactEnvUid = createHeatEnvPlaceHolder.left().value().getUniqueId();
			}
		}

		artifactsGroup.add(artifactUid);
		artifactsUUIDGroup.add(artifactUUID);
		if (!artifactEnvUid.isEmpty()) {
			artifactsGroup.add(artifactEnvUid);
		}

		List<ArtifactTemplateInfo> relatedArtifacts = artifactTemplateInfo.getRelatedArtifactsInfo();
		if (relatedArtifacts != null) {
			for (ArtifactTemplateInfo relatedArtifactTemplateInfo : relatedArtifacts) {
				resStatus = createDeploymentArtifactsFromCsar(csarUUID, csar, resource, user, artifactsGroup, artifactsUUIDGroup, relatedArtifactTemplateInfo, createdArtifacts, artifactsFromResource, labelCounter, shoudLock, inTransaction);
				if (resStatus.isRight())
					return resStatus;
			}
		}
		return resStatus;
	}

	private Either<Resource, ResponseFormat> createResourceArtifacts(String csarUUID, Map<String, byte[]> csar, Resource resource, User user, Map<String, List<ArtifactTemplateInfo>> artifactsMap, AuditingActionEnum createResource,
																	 List<ArtifactDefinition> createdArtifacts, boolean shouldLock, boolean inTransaction) {

		Either<Resource, ResponseFormat> resStatus = Either.left(resource);

		Collection<List<ArtifactTemplateInfo>> arifactsCollection = artifactsMap.values();

		for (List<ArtifactTemplateInfo> groupTemplateList : arifactsCollection) {
			if (groupTemplateList != null) {
				resStatus = createGroupDeploymentArtifactsFromCsar(csarUUID, csar, resource, user, groupTemplateList, createdArtifacts, 0, shouldLock, inTransaction);
				if (resStatus.isRight())
					return resStatus;
			}
		}

		return resStatus;

	}

	private Either<Resource, ResponseFormat> createOrUpdateNonMetaArtifacts(String csarUUID, Map<String, byte[]> csar, Resource resource, User user, List<ArtifactDefinition> createdArtifacts, boolean shouldLock, boolean inTransaction,
																			ArtifactOperationInfo artifactOperation) {

		Either<Resource, ResponseFormat> resStatus = null;
		Map<String, Set<List<String>>> collectedWarningMessages = new HashMap<>();

		try {
			List<NonMetaArtifactInfo> artifactPathAndNameList =
					// Stream of file paths contained in csar
					csar.entrySet().stream()
							// Filter in only VF artifact path location
							.filter(e -> Pattern.compile(VF_NODE_TYPE_ARTIFACTS_PATH_PATTERN).matcher(e.getKey()).matches())
							// Validate and add warnings
							.map(e -> CsarUtils.validateNonMetaArtifact(e.getKey(), e.getValue(), collectedWarningMessages))
							// Filter in Non Warnings
							.filter(e -> e.isLeft())
							// Convert from Either to NonMetaArtifactInfo
							.map(e -> e.left().value())
							// collect to List
							.collect(Collectors.toList());

			EnumMap<ArtifactOperationEnum, List<NonMetaArtifactInfo>> vfCsarArtifactsToHandle = null;

			if (artifactOperation.getArtifactOperationEnum() == ArtifactOperationEnum.Create) {
				vfCsarArtifactsToHandle = new EnumMap<>(ArtifactOperationEnum.class);
				vfCsarArtifactsToHandle.put(artifactOperation.getArtifactOperationEnum(), artifactPathAndNameList);
			} else {
				Either<EnumMap<ArtifactOperationEnum, List<NonMetaArtifactInfo>>, ResponseFormat> findVfCsarArtifactsToHandleRes = findVfCsarArtifactsToHandle(resource, artifactPathAndNameList, user);

				if (findVfCsarArtifactsToHandleRes.isRight()) {
					resStatus = Either.right(findVfCsarArtifactsToHandleRes.right().value());
				}
				if (resStatus == null) {
					vfCsarArtifactsToHandle = findVfCsarArtifactsToHandleRes.left().value();
				}
			}
			if (resStatus == null && vfCsarArtifactsToHandle != null) {
				for (Entry<ArtifactOperationEnum, List<NonMetaArtifactInfo>> currArtifactOperationPair : vfCsarArtifactsToHandle.entrySet()) {

					Optional<ResponseFormat> optionalCreateInDBError =
							// Stream of artifacts to be created
							currArtifactOperationPair.getValue().stream()
									// create each artifact
									.map(e -> createOrUpdateSingleNonMetaArtifact(resource, user, csarUUID, csar, e.getPath(), e.getArtifactName(), e.getArtifactType().getType(), e.getArtifactGroupType(), e.getArtifactLabel(), e.getDisplayName(),
											CsarUtils.ARTIFACT_CREATED_FROM_CSAR, e.getArtifactUniqueId(), artifactsBusinessLogic.new ArtifactOperationInfo(false, false, currArtifactOperationPair.getKey()), createdArtifacts, shouldLock,
											inTransaction))
									// filter in only error
									.filter(e -> e.isRight()).
									// Convert the error from either to ResponseFormat
											map(e -> e.right().value()).
									// Check if an error occurred
											findAny();
					// Error found on artifact Creation
					if (optionalCreateInDBError.isPresent()) {
						resStatus = Either.right(optionalCreateInDBError.get());
						break;
					}
				}
			}
			if (resStatus == null) {
				resStatus = Either.left(resource);
			}
		} catch (Exception e) {
			resStatus = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
			log.debug("Exception occured in createNonMetaArtifacts, message:{}", e.getMessage(), e);
		} finally {
			CsarUtils.handleWarningMessages(collectedWarningMessages);
		}
		return resStatus;
	}

	private Either<EnumMap<ArtifactOperationEnum, List<NonMetaArtifactInfo>>, ResponseFormat> findVfCsarArtifactsToHandle(Resource resource, List<NonMetaArtifactInfo> artifactPathAndNameList, User user) {

		List<ArtifactDefinition> existingArtifacts = new ArrayList<>();
		// collect all Deployment and Informational artifacts of VF
		if (resource.getDeploymentArtifacts() != null && !resource.getDeploymentArtifacts().isEmpty()) {
			existingArtifacts.addAll(resource.getDeploymentArtifacts().values());
		}
		if (resource.getArtifacts() != null && !resource.getArtifacts().isEmpty()) {
			existingArtifacts.addAll(resource.getArtifacts().values());
		}
		existingArtifacts = existingArtifacts.stream()
				// filter MANDATORY artifacts, LICENSE artifacts and artifacts was created from HEAT.meta
				.filter(this::isNonMetaArtifact).collect(Collectors.toList());

		List<String> artifactsToIgnore = new ArrayList<>();
		// collect IDs of Artifacts of VF which belongs to any group
		if (resource.getGroups() != null) {
			resource.getGroups().stream().forEach(g -> {
				if (g.getArtifacts() != null && !g.getArtifacts().isEmpty())
					artifactsToIgnore.addAll(g.getArtifacts());
			});
		}
		existingArtifacts = existingArtifacts.stream()
				// filter artifacts which belongs to any group
				.filter(a -> !artifactsToIgnore.contains(a.getUniqueId())).collect(Collectors.toList());
		return organizeVfCsarArtifactsByArtifactOperation(artifactPathAndNameList, existingArtifacts, resource, user);
	}

	private boolean isNonMetaArtifact(ArtifactDefinition artifact) {
		boolean result = true;
		if (artifact.getMandatory() || artifact.getArtifactName() == null || !isValidArtifactType(artifact)) {
			result = false;
		}
		return result;
	}

	private boolean isValidArtifactType(ArtifactDefinition artifact) {
		boolean result = true;
		if (artifact.getArtifactType() == null || ArtifactTypeEnum.findType(artifact.getArtifactType()) == ArtifactTypeEnum.VENDOR_LICENSE || ArtifactTypeEnum.findType(artifact.getArtifactType()) == ArtifactTypeEnum.VF_LICENSE) {
			result = false;
		}
		return result;
	}

	private Either<Resource, ResponseFormat> createGroupDeploymentArtifactsFromCsar(String csarUUID, Map<String, byte[]> csar, Resource resource, User user, List<ArtifactTemplateInfo> artifactsTemplateList, List<ArtifactDefinition> createdArtifacts,
																					int labelCounter, boolean shouldLock, boolean inTransaction) {
		Either<Resource, ResponseFormat> resStatus = Either.left(resource);
		List<GroupDefinition> createdGroups = resource.getGroups();
		List<GroupDefinition> heatGroups = null;
		if (createdGroups != null && !createdGroups.isEmpty()) {

			// List<IArtifactInfo> collect = resources.stream().flatMap( e ->
			// e.getArtifacts().stream()).filter(p ->
			// relevantArtifactTypes.contains(p.getArtifactType()
			// )).collect(Collectors.toList());
			// List<GroupDefinition> heatGroups = createdGroups.stream().filter(
			// e -> e.getProperties().stream().filter(p ->
			// p.getName().contains(Constants.HEAT_FILE_PROPS))).collect(Collectors.toList());
			heatGroups = createdGroups.stream().filter(e -> e.getMembers() != null).collect(Collectors.toList());
			;
		}
		List<GroupDefinition> needToCreate = new ArrayList<>();
		for (ArtifactTemplateInfo groupTemplateInfo : artifactsTemplateList) {
			String groupName = groupTemplateInfo.getGroupName();
			Set<String> artifactsGroup = new HashSet<String>();
			Set<String> artifactsUUIDGroup = new HashSet<String>();

			log.debug("createDeploymentArtifactsFromCsar start");
			resStatus = createDeploymentArtifactFromCsar(csarUUID, ARTIFACTS_PATH, csar, resource, user, artifactsGroup, artifactsUUIDGroup, groupTemplateInfo, createdArtifacts, labelCounter, shouldLock, inTransaction);
			log.debug("createDeploymentArtifactsFromCsar end");
			if (resStatus.isRight())
				return resStatus;

			Map<String, String> members = new HashMap<String, String>();
			associateMembersToArtifacts(createdArtifacts, null, heatGroups, artifactsGroup, members);

			List<String> artifactsList = new ArrayList<String>(artifactsGroup);
			List<String> artifactsUUIDList = new ArrayList<String>(artifactsUUIDGroup);

			GroupDefinition groupDefinition = new GroupDefinition();
			groupDefinition.setName(groupName);
			groupDefinition.setType(Constants.DEFAULT_GROUP_VF_MODULE);
			groupDefinition.setArtifacts(artifactsList);
			groupDefinition.setArtifactsUuid(artifactsUUIDList);

			if (!members.isEmpty())
				groupDefinition.setMembers(members);
			List<GroupProperty> properties = new ArrayList<GroupProperty>();
			GroupProperty prop = new GroupProperty();
			prop.setName(Constants.IS_BASE);
			prop.setValue(Boolean.toString(groupTemplateInfo.isBase()));
			properties.add(prop);
			Either<GroupTypeDefinition, StorageOperationStatus> getLatestGroupTypeRes = groupTypeOperation.getLatestGroupTypeByType(Constants.DEFAULT_GROUP_VF_MODULE, true);
			if (getLatestGroupTypeRes.isRight()) {
				return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(getLatestGroupTypeRes.right().value())));
			}
			properties = createVfModuleAdditionalProperties(groupTemplateInfo.isBase(), groupName, properties, createdArtifacts, artifactsList, getLatestGroupTypeRes.left().value());
			groupDefinition.convertFromGroupProperties(properties);
			log.debug("createGroup start");

			// Since in these groups we handle only artifacts, then no need to
			// fetch component instances

			// Either<GroupDefinition, ResponseFormat> createGroup = groupBusinessLogic.createGroup(comp, user, ComponentTypeEnum.RESOURCE, groupDefinition, inTransaction);
			// log.debug("createGroup end");
			// if (createGroup.isRight())
			// return Either.right(createGroup.right().value());
			needToCreate.add(groupDefinition);
		}

		ComponentParametersView componentParametersView = new ComponentParametersView();
		componentParametersView.disableAll();
		componentParametersView.setIgnoreUsers(false);
		componentParametersView.setIgnoreArtifacts(false);
		componentParametersView.setIgnoreGroups(false);

		componentParametersView.setIgnoreComponentInstances(false);

		Either<Resource, StorageOperationStatus> component = toscaOperationFacade.getToscaElement(resource.getUniqueId(), componentParametersView);
		if (component.isRight()) {
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
		}

		Either<List<GroupDefinition>, ResponseFormat> createGroups = groupBusinessLogic.addGroups(component.left().value(), user, ComponentTypeEnum.RESOURCE, needToCreate);
		if (createGroups.isRight()) {
			return Either.right(createGroups.right().value());
		}

		return resStatus;
	}

	public List<GroupProperty> createVfModuleAdditionalProperties(boolean isBase, String moduleName, List<GroupProperty> properties, List<ArtifactDefinition> deploymentArtifacts, List<String> artifactsInGroup, GroupTypeDefinition groupType) {
		Map<String, VfModuleProperty> vfModuleProperties = ConfigurationManager.getConfigurationManager().getConfiguration().getVfModuleProperties();
		vfModuleProperties.entrySet().forEach(p -> {
			GroupProperty prop = new GroupProperty();
			prop.setName(p.getKey());
			if (isBase) {
				prop.setValue(p.getValue().getForBaseModule());
				prop.setDefaultValue(p.getValue().getForBaseModule());
			} else {
				prop.setValue(p.getValue().getForNonBaseModule());
				prop.setDefaultValue(p.getValue().getForNonBaseModule());
			}
			properties.add(prop);

		});
		GroupProperty proplabel = new GroupProperty();
		proplabel.setName("vf_module_label");

		Matcher matcher = pattern.matcher(moduleName);

		if (matcher.find()) {
			proplabel.setValue(matcher.group(1));
			proplabel.setDefaultValue(matcher.group(1));
		} else {
			proplabel.setValue(moduleName);
			proplabel.setDefaultValue(moduleName);
		}
		properties.add(proplabel);

		GroupProperty propvolume = new GroupProperty();
		propvolume.setName("volume_group");
		boolean isVolume = false;
		for (String artifactId : artifactsInGroup) {
			ArtifactDefinition artifactDef = null;
			artifactDef = findArtifactInList(deploymentArtifacts, artifactId);
			if (artifactDef != null && artifactDef.getArtifactType().equalsIgnoreCase(ArtifactTypeEnum.HEAT_VOL.getType())) {
				isVolume = true;
				break;
			}
		}
		propvolume.setValue(String.valueOf(isVolume));
		propvolume.setDefaultValue(String.valueOf(isVolume));
		properties.add(propvolume);
		mergeWithGroupTypeProperties(properties, groupType.getProperties());
		return properties;
	}

	private void mergeWithGroupTypeProperties(List<GroupProperty> properties, List<PropertyDefinition> groupTypeProperties) {

		Map<String, GroupProperty> propertiesMap = properties.stream().collect(Collectors.toMap(p -> p.getName(), p -> p));
		for (PropertyDefinition groupTypeProperty : groupTypeProperties) {
			if (!propertiesMap.containsKey(groupTypeProperty.getName())) {
				properties.add(new GroupProperty(groupTypeProperty));
			}
		}
	}

	private ArtifactDefinition findArtifactInList(List<ArtifactDefinition> createdArtifacts, String artifactId) {
		for (ArtifactDefinition artifact : createdArtifacts) {
			if (artifact.getUniqueId().equals(artifactId)) {
				return artifact;
			}
		}
		return null;
	}

	private void associateMembersToArtifacts(List<ArtifactDefinition> createdArtifacts, List<ArtifactDefinition> artifactsFromResource, List<GroupDefinition> heatGroups, Set<String> artifactsGroup, Map<String, String> members) {
		if (heatGroups != null && !heatGroups.isEmpty()) {
			for (GroupDefinition heatGroup : heatGroups) {
				List<GroupProperty> grpoupProps = heatGroup.convertToGroupProperties();
				if (grpoupProps != null) {
					Optional<GroupProperty> op = grpoupProps.stream().filter(p -> p.getName().equals(Constants.HEAT_FILE_PROPS)).findAny();
					if (op.isPresent()) {
						GroupProperty prop = op.get();
						String heatFileNAme = prop.getValue();
						if (null == heatFileNAme || heatFileNAme.isEmpty())
							continue;
						List<ArtifactDefinition> artifacts = new ArrayList();
						for (String artifactId : artifactsGroup) {
							Optional<ArtifactDefinition> opArt = createdArtifacts.stream().filter(p -> p.getUniqueId().equals(artifactId)).findAny();
							if (opArt.isPresent()) {
								artifacts.add(opArt.get());
							}
							if (artifactsFromResource != null) {
								opArt = artifactsFromResource.stream().filter(p -> p.getUniqueId().equals(artifactId)).findAny();
								if (opArt.isPresent()) {
									artifacts.add(opArt.get());
								}
							}
						}
						Optional<ArtifactDefinition> resOp = artifacts.stream().filter(p -> heatFileNAme.contains(p.getArtifactName())).findAny();
						if (resOp.isPresent()) {
							members.putAll(heatGroup.getMembers());
						}
					}
				}
			}

		}
	}

	private Either<Resource, ResponseFormat> createDeploymentArtifactFromCsar(String csarUUID, String artifactPath, Map<String, byte[]> csar, Resource resource, User user, Set<String> artifactsGroup, Set<String> artifactsUUIDGroup,
																			  ArtifactTemplateInfo artifactTemplateInfo, List<ArtifactDefinition> createdArtifacts, int labelCounter, boolean shoudLock, boolean inTransaction) {
		Either<Resource, ResponseFormat> resStatus = Either.left(resource);
		String artifactFileName = artifactTemplateInfo.getFileName();
		String artifactUid = "";
		String artifactEnvUid = "";
		String artifactUUID = "";
		boolean alreadyExist = false;

		// check if artifacts already exist
		for (ArtifactDefinition createdArtifact : createdArtifacts) {
			if (createdArtifact.getArtifactName().equals(artifactFileName)) {
				artifactUid = createdArtifact.getUniqueId();
				artifactUUID = createdArtifact.getArtifactUUID();
				if (!createdArtifact.getArtifactType().equalsIgnoreCase(artifactTemplateInfo.getType())) {
					log.debug("Artifact with name {} and type {} already exist with type  {}", artifactFileName, artifactTemplateInfo.getType(), createdArtifact.getArtifactType());
					BeEcompErrorManager.getInstance().logInternalDataError("Artifact  file is not in expected formatr, fileName " + artifactFileName, "Artifact internals are invalid", ErrorSeverity.ERROR);
					return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_ALRADY_EXIST_IN_DIFFERENT_TYPE_IN_CSAR, artifactFileName, artifactTemplateInfo.getType(), createdArtifact.getArtifactType()));
				}
				alreadyExist = true;
				artifactEnvUid = checkAndGetHeatEnvId(createdArtifact);
				break;
			}
		}
		// if not exist need to create
		if (!alreadyExist) {

			Either<ArtifactDefinition, ResponseFormat> newArtifactEither = createDeploymentArtifact(csarUUID, csar, resource, user, artifactPath, artifactTemplateInfo, createdArtifacts, labelCounter, shoudLock, inTransaction);
			if (newArtifactEither.isRight()) {
				resStatus = Either.right(newArtifactEither.right().value());
				return resStatus;
			}
			ArtifactDefinition newArtifact = newArtifactEither.left().value();
			artifactUid = newArtifact.getUniqueId();
			artifactUUID = newArtifact.getArtifactUUID();

			ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(newArtifact.getArtifactType());
			if (artifactType == ArtifactTypeEnum.HEAT || artifactType == ArtifactTypeEnum.HEAT_NET || artifactType == ArtifactTypeEnum.HEAT_VOL) {
				Either<ArtifactDefinition, ResponseFormat> createHeatEnvPlaceHolder = artifactsBusinessLogic.createHeatEnvPlaceHolder(newArtifact, ArtifactsBusinessLogic.HEAT_VF_ENV_NAME, resource.getUniqueId(), NodeTypeEnum.Resource,
						resource.getName(), user, resource, null);
				if (createHeatEnvPlaceHolder.isRight()) {
					return Either.right(createHeatEnvPlaceHolder.right().value());
				}
				artifactEnvUid = createHeatEnvPlaceHolder.left().value().getUniqueId();
			}
		}

		artifactsGroup.add(artifactUid);
		artifactsUUIDGroup.add(artifactUUID);
		if (!artifactEnvUid.isEmpty()) {
			artifactsGroup.add(artifactEnvUid);
		}

		List<ArtifactTemplateInfo> relatedArtifacts = artifactTemplateInfo.getRelatedArtifactsInfo();
		if (relatedArtifacts != null) {
			for (ArtifactTemplateInfo relatedArtifactTemplateInfo : relatedArtifacts) {
				resStatus = createDeploymentArtifactFromCsar(csarUUID, artifactPath, csar, resource, user, artifactsGroup, artifactsUUIDGroup, relatedArtifactTemplateInfo, createdArtifacts, labelCounter, shoudLock, inTransaction);
				if (resStatus.isRight())
					return resStatus;
			}
		}
		return resStatus;
	}

	private String checkAndGetHeatEnvId(ArtifactDefinition createdArtifact) {
		String artifactEnvUid = "";
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(createdArtifact.getArtifactType());
		if (artifactType == ArtifactTypeEnum.HEAT || artifactType == ArtifactTypeEnum.HEAT_NET || artifactType == ArtifactTypeEnum.HEAT_VOL) {
			artifactEnvUid = createdArtifact.getUniqueId() + ArtifactsBusinessLogic.HEAT_ENV_SUFFIX;
		}
		return artifactEnvUid;
	}

	private Either<ArtifactDefinition, ResponseFormat> createDeploymentArtifact(String csarUUID, Map<String, byte[]> csar, Resource resource, User user, String artifactPath, ArtifactTemplateInfo artifactTemplateInfo,
																				List<ArtifactDefinition> createdArtifacts, int labelCounter, boolean shoudLock, boolean inTransaction) {
		final String artifactFileName = artifactTemplateInfo.getFileName();
		Either<ImmutablePair<String, byte[]>, ResponseFormat> artifactContententStatus = CsarValidationUtils.getArtifactsContent(csarUUID, csar, artifactPath + artifactFileName, artifactFileName, componentsUtils);
		if (artifactContententStatus.isRight()) {
			return Either.right(artifactContententStatus.right().value());
		}
		labelCounter += createdArtifacts.size();

		Map<String, Object> json = buildJsonForArtifact(artifactTemplateInfo, artifactContententStatus.left().value().getValue(), labelCounter);

		Either<Either<ArtifactDefinition, Operation>, ResponseFormat> uploadArtifactToService = createOrUpdateCsarArtifactFromJson(resource, user, json, artifactsBusinessLogic.new ArtifactOperationInfo(false, false, ArtifactOperationEnum.Create),
				shoudLock, inTransaction);

		if (uploadArtifactToService.isRight())
			return Either.right(uploadArtifactToService.right().value());

		ArtifactDefinition currentInfo = uploadArtifactToService.left().value().left().value();
		if (currentInfo.getHeatParameters() != null) {

			Either<ArtifactDefinition, ResponseFormat> updateEnvEither = updateHeatParamsFromCsar(resource, csarUUID, csar, artifactTemplateInfo, currentInfo, false);
			if (updateEnvEither.isRight()) {
				log.debug("failed to update parameters to artifact {}", artifactFileName);
				return Either.right(updateEnvEither.right().value());

			}
			currentInfo = updateEnvEither.left().value();

		}

		createdArtifacts.add(currentInfo);

		return Either.left(currentInfo);

	}

	private Either<ArtifactDefinition, ResponseFormat> createInformationalArtifact(String csarUUID, Map<String, byte[]> csar, Resource resource, User user, ArtifactTemplateInfo artifactTemplateInfo, int labelCounter, boolean shoudLock,
																				   boolean inTransaction) {
		final String artifactFileName = artifactTemplateInfo.getFileName();
		String artifactPath = CsarUtils.ARTIFACTS_PATH + CsarUtils.INFORMATIONAL_ARTIFACTS + artifactFileName;
		Either<ImmutablePair<String, byte[]>, ResponseFormat> artifactContententStatus = CsarValidationUtils.getArtifactsContent(csarUUID, csar, artifactPath, artifactFileName, componentsUtils);
		if (artifactContententStatus.isRight())
			return Either.right(artifactContententStatus.right().value());

		Map<String, Object> json = buildJsonForArtifact(artifactTemplateInfo, artifactContententStatus.left().value().getValue(), labelCounter);

		Either<Either<ArtifactDefinition, Operation>, ResponseFormat> uploadArtifactToService = createOrUpdateCsarArtifactFromJson(resource, user, json, artifactsBusinessLogic.new ArtifactOperationInfo(false, false, ArtifactOperationEnum.Create),
				shoudLock, inTransaction);

		if (uploadArtifactToService.isRight())
			return Either.right(uploadArtifactToService.right().value());

		ArtifactDefinition currentInfo = uploadArtifactToService.left().value().left().value();

		return Either.left(currentInfo);

	}

	private Either<ArtifactDefinition, ResponseFormat> updateDeploymentArtifactsFromCsar(String csarUUID, Map<String, byte[]> csar, Resource resource, User user, ArtifactDefinition oldArtifact, ArtifactTemplateInfo artifactTemplateInfo,
																						 List<ArtifactDefinition> updatedArtifacts, List<ArtifactTemplateInfo> updatedRequiredArtifacts, boolean shouldLock, boolean inTransaction) {

		Either<ArtifactDefinition, ResponseFormat> resStatus = null;
		String artifactFileName = artifactTemplateInfo.getFileName();
		String artifactUid = "";

		// check if artifacts already exist
		for (ArtifactDefinition updatedArtifact : updatedArtifacts) {
			if (updatedArtifact.getArtifactName().equals(artifactFileName)) {
				artifactUid = updatedArtifact.getUniqueId();
				if (!updatedArtifact.getArtifactType().equalsIgnoreCase(artifactTemplateInfo.getType())) {
					log.debug("Artifact with name {} and type {} already updated with type  {}", artifactFileName, artifactTemplateInfo.getType(), updatedArtifact.getArtifactType());
					BeEcompErrorManager.getInstance().logInternalDataError("Artifact  file is not in expected formatr, fileName " + artifactFileName, "Artifact internals are invalid", ErrorSeverity.ERROR);
					resStatus = Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_ALRADY_EXIST_IN_DIFFERENT_TYPE_IN_CSAR, artifactFileName, artifactTemplateInfo.getType(), updatedArtifact.getArtifactType()));
					return resStatus;
				}
				resStatus = Either.left(updatedArtifact);
				return resStatus;
			}

		}

		Either<ImmutablePair<String, byte[]>, ResponseFormat> artifactContententStatus = CsarValidationUtils.getArtifactsContent(csarUUID, csar, CsarUtils.ARTIFACTS_PATH + artifactFileName, artifactFileName, componentsUtils);
		if (artifactContententStatus.isRight()) {
			resStatus = Either.right(artifactContententStatus.right().value());
			return resStatus;
		}

		Map<String, Object> json = buildJsonForUpdateArtifact(oldArtifact.getUniqueId(), artifactFileName, oldArtifact.getArtifactType(), ArtifactGroupTypeEnum.DEPLOYMENT, oldArtifact.getArtifactLabel(), oldArtifact.getArtifactDisplayName(),
				oldArtifact.getDescription(), artifactContententStatus.left().value().getRight(), updatedRequiredArtifacts);

		Either<Either<ArtifactDefinition, Operation>, ResponseFormat> uploadArtifactToService = createOrUpdateCsarArtifactFromJson(resource, user, json, artifactsBusinessLogic.new ArtifactOperationInfo(false, false, ArtifactOperationEnum.Update),
				shouldLock, inTransaction);

		if (uploadArtifactToService.isRight()) {
			resStatus = Either.right(uploadArtifactToService.right().value());
			return resStatus;
		}
		ArtifactDefinition currentInfo = uploadArtifactToService.left().value().left().value();

		Either<ArtifactDefinition, ResponseFormat> updateEnvEither = updateHeatParamsFromCsar(resource, csarUUID, csar, artifactTemplateInfo, currentInfo, true);
		if (updateEnvEither.isRight()) {
			log.debug("failed to update parameters to artifact {}", artifactFileName);
			resStatus = Either.right(updateEnvEither.right().value());
			return resStatus;
		}
		// TODO evg update env time ( must be separate US for this!!!!)

		artifactUid = updateEnvEither.left().value().getUniqueId();
		updatedArtifacts.add(updateEnvEither.left().value());
		resStatus = Either.left(updateEnvEither.left().value());

		return resStatus;

	}

	private Either<ArtifactDefinition, ResponseFormat> updateHeatParamsFromCsar(Resource resource, String csarUUID, Map<String, byte[]> csar, ArtifactTemplateInfo artifactTemplateInfo, ArtifactDefinition currentInfo, boolean isUpdateEnv) {
		Either<ArtifactDefinition, ResponseFormat> resStatus = Either.left(currentInfo);
		if (artifactTemplateInfo.getEnv() != null && !artifactTemplateInfo.getEnv().isEmpty()) {

			Either<ImmutablePair<String, byte[]>, ResponseFormat> artifactparamsStatus = CsarValidationUtils.getArtifactsContent(csarUUID, csar, CsarUtils.ARTIFACTS_PATH + artifactTemplateInfo.getEnv(), artifactTemplateInfo.getEnv(),
					componentsUtils);
			if (artifactparamsStatus.isRight()) {
				resStatus = Either.right(artifactparamsStatus.right().value());
				return resStatus;
			}
			Either<List<HeatParameterDefinition>, ResponseFormat> propsStatus = extractHeatParameters(ArtifactTypeEnum.HEAT_ENV.getType(), artifactTemplateInfo.getEnv(), artifactparamsStatus.left().value().getValue(), false);
			/*
			 * if (propsStatus.isRight()) {
			 *
			 * resStatus = Either.right(propsStatus.right().value()); return resStatus; }
			 */
			if (propsStatus.isLeft()) {
				List<HeatParameterDefinition> updatedHeatEnvParams = propsStatus.left().value();
				List<HeatParameterDefinition> currentHeatEnvParams = currentInfo.getListHeatParameters();
				// List<HeatParameterDefinition> newHeatEnvParams = new ArrayList<HeatParameterDefinition>();

				if (updatedHeatEnvParams != null && !updatedHeatEnvParams.isEmpty() && currentHeatEnvParams != null && !currentHeatEnvParams.isEmpty()) {

					String paramName;
					for (HeatParameterDefinition heatEnvParam : updatedHeatEnvParams) {

						paramName = heatEnvParam.getName();
						for (HeatParameterDefinition currHeatParam : currentHeatEnvParams) {
							if (paramName.equalsIgnoreCase(currHeatParam.getName())) {

								String updatedParamValue = heatEnvParam.getCurrentValue();
								if (updatedParamValue == null)
									updatedParamValue = heatEnvParam.getDefaultValue();
								HeatParameterType paramType = HeatParameterType.isValidType(currHeatParam.getType());
								if (!paramType.getValidator().isValid(updatedParamValue, null)) {
									ActionStatus status = ActionStatus.INVALID_HEAT_PARAMETER_VALUE;
									ResponseFormat responseFormat = componentsUtils.getResponseFormat(status, ArtifactTypeEnum.HEAT_ENV.getType(), paramType.getType(), paramName);
									resStatus = Either.right(responseFormat);
									return resStatus;
								}
								currHeatParam.setCurrentValue(HeatParameterType.isValidType(currHeatParam.getType()).getConverter().convert(updatedParamValue, null, null));
								// newHeatEnvParams.add(currHeatParam);
								break;
							}
						}
					}
					currentInfo.setListHeatParameters(currentHeatEnvParams);
					Either<ArtifactDefinition, StorageOperationStatus> updateArifactOnResource = artifactToscaOperation.updateArifactOnResource(currentInfo, resource.getUniqueId(), currentInfo.getUniqueId(), null, null);
					if (updateArifactOnResource.isRight()) {
						log.debug("Failed to update heat paratemers of heat on CSAR flow for component {} artifact {} label {}", resource.getUniqueId(), currentInfo.getUniqueId(), currentInfo.getArtifactLabel());
						return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(updateArifactOnResource.right().value())));
					}
					resStatus = Either.left(updateArifactOnResource.left().value());
				}
			}
		}
		if (isUpdateEnv) {
			Map<String, ArtifactDefinition> artifacts = resource.getDeploymentArtifacts();
			Optional<ArtifactDefinition> op = artifacts.values().stream().filter(p -> p.getGeneratedFromId() != null && p.getGeneratedFromId().equals(currentInfo.getUniqueId())).findAny();
			if (op.isPresent()) {
				ArtifactDefinition artifactInfoHeatEnv = op.get();
				Either<ArtifactDefinition, StorageOperationStatus> updateArifactOnResource = artifactToscaOperation.updateArifactOnResource(artifactInfoHeatEnv, resource.getUniqueId(), artifactInfoHeatEnv.getUniqueId(), null, null);
				if (updateArifactOnResource.isRight()) {
					log.debug("Failed to update heat env on CSAR flow for component {} artifact {} label {}", resource.getUniqueId(), artifactInfoHeatEnv.getUniqueId(), artifactInfoHeatEnv.getArtifactLabel());
					return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(updateArifactOnResource.right().value())));
				}
			}
		}
		return resStatus;
	}

	private Either<List<HeatParameterDefinition>, ResponseFormat> extractHeatParameters(String artifactType, String fileName, byte[] content, boolean is64Encoded) {
		// extract heat parameters
		String heatDecodedPayload = is64Encoded ? new String(Base64.decodeBase64(content)) : new String(content);
		Either<List<HeatParameterDefinition>, ResultStatusEnum> heatParameters = ImportUtils.getHeatParamsWithoutImplicitTypes(heatDecodedPayload, artifactType);
		if (heatParameters.isRight()) {
			log.debug("File {} is not in expected key-value form in csar ", fileName);
			BeEcompErrorManager.getInstance().logInternalDataError("File " + fileName + " is not in expected key-value form in csar ", "CSAR internals are invalid", ErrorSeverity.ERROR);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_DEPLOYMENT_ARTIFACT_HEAT, fileName));

		}
		return Either.left(heatParameters.left().value());

	}

	private Map<String, Object> buildJsonForArtifact(ArtifactTemplateInfo artifactTemplateInfo, byte[] artifactContentent, int atrifactLabelCounter) {

		Map<String, Object> json = new HashMap<String, Object>();
		String artifactName = artifactTemplateInfo.getFileName();

		json.put(Constants.ARTIFACT_NAME, artifactTemplateInfo.getFileName());
		json.put(Constants.ARTIFACT_TYPE, artifactTemplateInfo.getType());
		json.put(Constants.ARTIFACT_DESCRIPTION, "created from csar");

		// DE250204: There is no need to check if base64 encoding.

		// String encodedPayload = new String(artifactContentent);
		// boolean isEncoded = GeneralUtility.isBase64Encoded(artifactContentent);
		// if (!isEncoded) {
		String encodedPayload = Base64.encodeBase64String(artifactContentent);
		// }
		json.put(Constants.ARTIFACT_PAYLOAD_DATA, encodedPayload);
		String displayName = artifactName;
		if (artifactName.lastIndexOf(".") > 0)
			displayName = artifactName.substring(0, artifactName.lastIndexOf("."));
		json.put(Constants.ARTIFACT_DISPLAY_NAME, displayName);
		String label = ValidationUtils.normalizeArtifactLabel(artifactTemplateInfo.getType() + atrifactLabelCounter);
		json.put(Constants.ARTIFACT_LABEL, label);
		json.put(Constants.ARTIFACT_GROUP_TYPE, ArtifactGroupTypeEnum.DEPLOYMENT.getType());
		List<ArtifactTemplateInfo> requiredArtifacts = artifactTemplateInfo.getRelatedArtifactsInfo();
		json.put(Constants.REQUIRED_ARTIFACTS, (requiredArtifacts == null || requiredArtifacts.isEmpty()) ? new ArrayList<>()
				: requiredArtifacts.stream().filter(e -> e.getType().equals(ArtifactTypeEnum.HEAT_ARTIFACT.getType()) || e.getType().equals(ArtifactTypeEnum.HEAT_NESTED.getType())).map(e -> e.getFileName()).collect(Collectors.toList()));
		return json;
	}

	private Map<String, Object> buildJsonForUpdateArtifact(String artifactId, String artifactName, String artifactType, ArtifactGroupTypeEnum artifactGroupType, String label, String displayName, String description, byte[] artifactContentent,
														   List<ArtifactTemplateInfo> updatedRequiredArtifacts) {

		Map<String, Object> json = new HashMap<String, Object>();
		if (artifactId != null && !artifactId.isEmpty())
			json.put(Constants.ARTIFACT_ID, artifactId);

		json.put(Constants.ARTIFACT_NAME, artifactName);
		json.put(Constants.ARTIFACT_TYPE, artifactType);
		json.put(Constants.ARTIFACT_DESCRIPTION, description);

		String encodedPayload = new String(artifactContentent);

		// boolean isEncoded = GeneralUtility.isBase64Encoded(artifactContentent);
		// if (!isEncoded) {
		log.debug("payload is encoded. perform decode");
		encodedPayload = Base64.encodeBase64String(artifactContentent);
		// }

		json.put(Constants.ARTIFACT_PAYLOAD_DATA, encodedPayload);
		json.put(Constants.ARTIFACT_DISPLAY_NAME, displayName);
		json.put(Constants.ARTIFACT_LABEL, label);
		json.put(Constants.ARTIFACT_GROUP_TYPE, artifactGroupType.getType());
		json.put(Constants.REQUIRED_ARTIFACTS, (updatedRequiredArtifacts == null || updatedRequiredArtifacts.isEmpty()) ? new ArrayList<>()
				: updatedRequiredArtifacts.stream().filter(e -> e.getType().equals(ArtifactTypeEnum.HEAT_ARTIFACT.getType()) || e.getType().equals(ArtifactTypeEnum.HEAT_NESTED.getType())).map(e -> e.getFileName()).collect(Collectors.toList()));
		return json;
	}

	private Either<Map<String, List<ArtifactTemplateInfo>>, ResponseFormat> parseResourceArtifactsInfoFromFile(Resource resource, String artifactsMetaFile, String artifactFileName, User user) {

		try {
			JsonObject jsonElement = new JsonObject();
			jsonElement = gson.fromJson(artifactsMetaFile, jsonElement.getClass());

			JsonElement importStructureElement = jsonElement.get(Constants.IMPORT_STRUCTURE);
			if (importStructureElement == null || importStructureElement.isJsonNull()) {
				log.debug("Artifact  file is not in expected formatr, fileName  {}", artifactFileName);
				BeEcompErrorManager.getInstance().logInternalDataError("Artifact  file is not in expected formatr, fileName " + artifactFileName, "Artifact internals are invalid", ErrorSeverity.ERROR);
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID_FORMAT, artifactFileName));
			}

			Map<String, List<Map<String, Object>>> artifactTemplateMap = new HashMap<String, List<Map<String, Object>>>();
			artifactTemplateMap = componentsUtils.parseJsonToObject(importStructureElement.toString(), HashMap.class);
			if (artifactTemplateMap.isEmpty()) {
				log.debug("Artifact  file is not in expected formatr, fileName  {}", artifactFileName);
				BeEcompErrorManager.getInstance().logInternalDataError("Artifact  file is not in expected formatr, fileName " + artifactFileName, "Artifact internals are invalid", ErrorSeverity.ERROR);
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID_FORMAT, artifactFileName));
			}

			Set<String> artifactsTypeKeys = artifactTemplateMap.keySet();
			Map<String, List<ArtifactTemplateInfo>> artifactsMap = new HashMap<String, List<ArtifactTemplateInfo>>();
			List<ArtifactTemplateInfo> allGroups = new ArrayList<>();
			for (String artifactsTypeKey : artifactsTypeKeys) {

				List<Map<String, Object>> o = artifactTemplateMap.get(artifactsTypeKey);
				Either<List<ArtifactTemplateInfo>, ResponseFormat> artifactTemplateInfoListPairStatus = createArtifactTemplateInfoModule(artifactsTypeKey, o);
				if (artifactTemplateInfoListPairStatus.isRight()) {
					log.debug("Artifact  file is not in expected formatr, fileName  {}", artifactFileName);
					BeEcompErrorManager.getInstance().logInternalDataError("Artifact  file is not in expected format, fileName " + artifactFileName, "Artifact internals are invalid", ErrorSeverity.ERROR);
					return Either.right(artifactTemplateInfoListPairStatus.right().value());
				}
				List<ArtifactTemplateInfo> artifactTemplateInfoList = artifactTemplateInfoListPairStatus.left().value();
				if (artifactTemplateInfoList == null) {
					log.debug("Artifact  file is not in expected formatr, fileName  {}", artifactFileName);
					BeEcompErrorManager.getInstance().logInternalDataError("Artifact  file is not in expected format, fileName " + artifactFileName, "Artifact internals are invalid", ErrorSeverity.ERROR);
					return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID_FORMAT, artifactFileName));

				}
				allGroups.addAll(artifactTemplateInfoList);
				artifactsMap.put(artifactsTypeKey, artifactTemplateInfoList);
			}
			int counter = groupBusinessLogic.getNextVfModuleNameCounter(resource.getGroups());
			Either<Boolean, ResponseFormat> validateGroupNamesRes = groupBusinessLogic.validateGenerateVfModuleGroupNames(allGroups, resource.getSystemName(), counter);
			if (validateGroupNamesRes.isRight()) {
				return Either.right(validateGroupNamesRes.right().value());
			}
			return Either.left(artifactsMap);
		} catch (Exception e) {
			log.debug("Artifact  file is not in expected format, fileName  {}", artifactFileName);
			log.debug("failed with exception.", e);
			BeEcompErrorManager.getInstance().logInternalDataError("Artifact  file is not in expected format, fileName " + artifactFileName, "Artifact internals are invalid", ErrorSeverity.ERROR);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID_FORMAT, artifactFileName));
		}

	}

	private Either<List<ArtifactTemplateInfo>, ResponseFormat> createArtifactTemplateInfoModule(String artifactsTypeKey, List<Map<String, Object>> jsonObject) {
		List<ArtifactTemplateInfo> artifactTemplateInfoList = new ArrayList<ArtifactTemplateInfo>();
		for (Map<String, Object> o : jsonObject) {
			Either<ArtifactTemplateInfo, ResponseFormat> artifacttemplateInfoStatus = ArtifactTemplateInfo.createArtifactTemplateInfoFromJson(componentsUtils, artifactsTypeKey, o, artifactTemplateInfoList, null);
			if (artifacttemplateInfoStatus.isRight()) {
				return Either.right(artifacttemplateInfoStatus.right().value());
			}

			ArtifactTemplateInfo artifacttemplateInfo = artifacttemplateInfoStatus.left().value();
			if (artifacttemplateInfo != null) {
				artifactTemplateInfoList.add(artifacttemplateInfo);
			}

		}
		return Either.left(artifactTemplateInfoList);
	}

	private Either<Resource, ResponseFormat> createResourceInstancesRelations(User user, String yamlName, Resource resource, Map<String, UploadComponentInstanceInfo> uploadResInstancesMap, boolean inTransaction, boolean needLock) {
		log.debug("createResourceInstancesRelations try to create relations ");
		List<ComponentInstance> componentInstancesList = resource.getComponentInstances();
		if (uploadResInstancesMap == null) {
			log.debug("UploadComponentInstanceInfo is empty, fileName  {}", yamlName);
			BeEcompErrorManager.getInstance().logInternalDataError("UploadComponentInstanceInfo is emty, fileName  {}", yamlName, ErrorSeverity.ERROR);

			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
			return Either.right(responseFormat);
		}

		if (componentInstancesList == null || componentInstancesList.isEmpty()) {
			log.debug("componentInstancesList is empty in resource {} ", resource.getUniqueId());
			BeEcompErrorManager.getInstance().logInternalDataError("componentInstancesList is empty in resource {} ", resource.getUniqueId(), ErrorSeverity.ERROR);

			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
			return Either.right(responseFormat);
		}

		log.debug("Before validateAndUpdatePropertyValue");
		Either<Map<String, DataTypeDefinition>, TitanOperationStatus> allDataTypes = dataTypeCache.getAll();
		if (allDataTypes.isRight()) {
			TitanOperationStatus status = allDataTypes.right().value();
			BeEcompErrorManager.getInstance().logInternalFlowError("UpdatePropertyValueOnComponentInstance", "Failed to update property value on instance. Status is " + status, ErrorSeverity.ERROR);
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(DaoStatusConverter.convertTitanStatusToStorageStatus(status)), yamlName);

		}

		Map<String, List<ComponentInstanceProperty>> instProperties = new HashMap<>();
		Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilties = new HashMap<>();
		Map<ComponentInstance, Map<String, List<RequirementDefinition>>> instRequirements = new HashMap<>();
		Map<String, Map<String, ArtifactDefinition>> instArtifacts = new HashMap<>();
		Map<String, List<PropertyDefinition>> instAttributes = new HashMap<>();
		Map<String, Resource> originCompMap = new HashMap<>();
		List<RequirementCapabilityRelDef> relations = new ArrayList<>();

		for (Entry<String, UploadComponentInstanceInfo> entry : uploadResInstancesMap.entrySet()) {
			UploadComponentInstanceInfo uploadComponentInstanceInfo = entry.getValue();
			ComponentInstance currentCompInstance = null;
			for (ComponentInstance compInstance : componentInstancesList) {

				if (compInstance.getName().equals(uploadComponentInstanceInfo.getName())) {
					currentCompInstance = compInstance;
					break;
				}

			}
			if (currentCompInstance == null) {
				log.debug("component instance with name {}  in resource {} ", uploadComponentInstanceInfo.getName(), resource.getUniqueId());
				BeEcompErrorManager.getInstance().logInternalDataError("component instance with name " + uploadComponentInstanceInfo.getName() + "  in resource {} ", resource.getUniqueId(), ErrorSeverity.ERROR);
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
				return Either.right(responseFormat);
			}
			String resourceInstanceId = currentCompInstance.getUniqueId();
			Resource originResource = null;
			if (!originCompMap.containsKey(currentCompInstance.getComponentUid())) {
				Either<Resource, StorageOperationStatus> getPropertyRes = toscaOperationFacade.getToscaFullElement(currentCompInstance.getComponentUid());
				if (getPropertyRes.isRight()) {
					log.debug("failed to find properties of resource {} status is {}", currentCompInstance.getComponentUid(), getPropertyRes);
					ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(getPropertyRes.right().value()), yamlName);
					return Either.right(responseFormat);
				}
				originResource = getPropertyRes.left().value();
				originCompMap.put(originResource.getUniqueId(), originResource);
			} else {
				originResource = originCompMap.get(currentCompInstance.getComponentUid());
			}
			if (originResource.getCapabilities() != null && !originResource.getCapabilities().isEmpty())
				instCapabilties.put(currentCompInstance, originResource.getCapabilities());
			if (originResource.getRequirements() != null && !originResource.getRequirements().isEmpty())
				instRequirements.put(currentCompInstance, originResource.getRequirements());
			if (originResource.getDeploymentArtifacts() != null && !originResource.getDeploymentArtifacts().isEmpty())
				instArtifacts.put(resourceInstanceId, originResource.getDeploymentArtifacts());
			if (originResource.getAttributes() != null && !originResource.getAttributes().isEmpty())
				instAttributes.put(resourceInstanceId, originResource.getAttributes());

			ResponseFormat addPropertiesValueToRiRes = addPropertyValuesToRi(uploadComponentInstanceInfo, resource, originResource, currentCompInstance, yamlName, instProperties, allDataTypes.left().value());
			if (addPropertiesValueToRiRes.getStatus() != 200) {
				return Either.right(addPropertiesValueToRiRes);
			}

		}

		Either<Map<String, List<ComponentInstanceProperty>>, StorageOperationStatus> addPropToInst = toscaOperationFacade.associateComponentInstancePropertiesToComponent(instProperties, resource.getUniqueId());
		if (addPropToInst.isRight()) {
			log.debug("failed to associate properties of resource {} status is {}", resource.getUniqueId(), addPropToInst.right().value());
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(addPropToInst.right().value()), yamlName);
			return Either.right(responseFormat);
		}

		StorageOperationStatus addArtToInst = toscaOperationFacade.associateArtifactToInstances(instArtifacts, resource.getUniqueId(), user);
		if (addArtToInst != StorageOperationStatus.OK && addArtToInst != StorageOperationStatus.NOT_FOUND) {
			log.debug("failed to associate artifact of resource {} status is {}", resource.getUniqueId(), addArtToInst);
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(addArtToInst), yamlName);
			return Either.right(responseFormat);
		}

		addArtToInst = toscaOperationFacade.associateCalculatedCapReq(instCapabilties, instRequirements, resource.getUniqueId());
		if (addArtToInst != StorageOperationStatus.OK && addArtToInst != StorageOperationStatus.NOT_FOUND) {
			log.debug("failed to associate cap and req of resource {} status is {}", resource.getUniqueId(), addArtToInst);
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(addArtToInst), yamlName);
			return Either.right(responseFormat);
		}

		addArtToInst = toscaOperationFacade.associateInstAttributeToComponentToInstances(instAttributes, resource.getUniqueId());
		if (addArtToInst != StorageOperationStatus.OK && addArtToInst != StorageOperationStatus.NOT_FOUND) {
			log.debug("failed to associate attributes of resource {} status is {}", resource.getUniqueId(), addArtToInst);
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(addArtToInst), yamlName);
			return Either.right(responseFormat);
		}

		ComponentParametersView parametersView = new ComponentParametersView();
		parametersView.disableAll();
		parametersView.setIgnoreComponentInstances(false);
		parametersView.setIgnoreComponentInstancesProperties(false);
		parametersView.setIgnoreCapabilities(false);
		parametersView.setIgnoreRequirements(false);

		Either<Resource, StorageOperationStatus> eitherGerResource = toscaOperationFacade.getToscaElement(resource.getUniqueId(), parametersView);

		if (eitherGerResource.isRight()) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(eitherGerResource.right().value()), resource);

			return Either.right(responseFormat);

		}

		resource = eitherGerResource.left().value();

		for (Entry<String, UploadComponentInstanceInfo> entry : uploadResInstancesMap.entrySet()) {
			UploadComponentInstanceInfo uploadComponentInstanceInfo = entry.getValue();
			ComponentInstance currentCompInstance = null;
			for (ComponentInstance compInstance : componentInstancesList) {

				if (compInstance.getName().equals(uploadComponentInstanceInfo.getName())) {
					currentCompInstance = compInstance;
					break;
				}

			}
			if (currentCompInstance == null) {
				log.debug("component instance with name {}  in resource {} ", uploadComponentInstanceInfo.getName(), resource.getUniqueId());
				BeEcompErrorManager.getInstance().logInternalDataError("component instance with name " + uploadComponentInstanceInfo.getName() + "  in resource {} ", resource.getUniqueId(), ErrorSeverity.ERROR);
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
				return Either.right(responseFormat);
			}

			ResponseFormat addRelationToRiRes = addRelationToRI(yamlName, resource, entry.getValue(), relations);
			if (addRelationToRiRes.getStatus() != 200) {
				return Either.right(addRelationToRiRes);
			}
		}

		addArtToInst = toscaOperationFacade.associateResourceInstances(resource.getUniqueId(), relations);
		if (addArtToInst != StorageOperationStatus.OK && addArtToInst != StorageOperationStatus.NOT_FOUND) {
			log.debug("failed to associate instances of resource {} status is {}", resource.getUniqueId(), addArtToInst);
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(addArtToInst), yamlName);
			return Either.right(responseFormat);
		}

		log.debug("************* in create relations, getResource start");

		eitherGerResource = toscaOperationFacade.getToscaElement(resource.getUniqueId());
		log.debug("************* in create relations, getResource end");
		if (eitherGerResource.isRight()) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(eitherGerResource.right().value()), resource);

			return Either.right(responseFormat);

		}
		return Either.left(eitherGerResource.left().value());
	}

	private ResponseFormat addRelationToRI(String yamlName, Resource resource, UploadComponentInstanceInfo nodesInfoValue, List<RequirementCapabilityRelDef> relations) {
		List<ComponentInstance> componentInstancesList = resource.getComponentInstances();
		long totalCreateRel = 0;
		long totalCreatePropVal = 0;

		UploadComponentInstanceInfo uploadComponentInstanceInfo = nodesInfoValue;

		ComponentInstance currentCompInstance = null;

		for (ComponentInstance compInstance : componentInstancesList) {

			if (compInstance.getName().equals(uploadComponentInstanceInfo.getName())) {
				currentCompInstance = compInstance;
				break;
			}

		}

		if (currentCompInstance == null) {
			log.debug("component instance with name {}  in resource {} ", uploadComponentInstanceInfo.getName(), resource.getUniqueId());
			BeEcompErrorManager.getInstance().logInternalDataError("component instance with name " + uploadComponentInstanceInfo.getName() + "  in resource {} ", resource.getUniqueId(), ErrorSeverity.ERROR);
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
			return responseFormat;
		}
		String resourceInstanceId = currentCompInstance.getUniqueId();

		log.debug("************* addPropertyValuesToRi start");
		long startAddProperty = System.currentTimeMillis();
		log.debug("************* addPropertyValuesToRi end");
		totalCreatePropVal += (System.currentTimeMillis() - startAddProperty);
		Map<String, List<UploadReqInfo>> regMap = uploadComponentInstanceInfo.getRequirements();
		if (regMap == null) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK, yamlName);
			return responseFormat;
		}
		Iterator<Entry<String, List<UploadReqInfo>>> nodesRegValue = regMap.entrySet().iterator();

		long startAddRelation = System.currentTimeMillis();

		while (nodesRegValue.hasNext()) {
			Entry<String, List<UploadReqInfo>> nodesRegInfoEntry = nodesRegValue.next();

			List<UploadReqInfo> uploadRegInfoList = nodesRegInfoEntry.getValue();
			for (UploadReqInfo uploadRegInfo : uploadRegInfoList) {
				log.debug("Going to create  relation {}", uploadRegInfo.getName());
				String regName = uploadRegInfo.getName();
				String nodeCapName = uploadRegInfo.getNode();
				RequirementCapabilityRelDef regCapRelDef = new RequirementCapabilityRelDef();
				regCapRelDef.setFromNode(resourceInstanceId);
				log.debug("try to find available requirement {} ", regName);
				Either<RequirementDefinition, ResponseFormat> eitherReqStatus = findAviableRequiremen(regName, yamlName, uploadComponentInstanceInfo, currentCompInstance, uploadRegInfo.getCapabilityName());
				if (eitherReqStatus.isRight()) {
					log.debug("failed to find available requirement {} status is {}", regName, eitherReqStatus.right().value());
					return eitherReqStatus.right().value();
				}

				RequirementDefinition validReq = eitherReqStatus.left().value();
				List<RequirementAndRelationshipPair> reqAndRelationshipPairList = regCapRelDef.getRelationships();
				if (reqAndRelationshipPairList == null)
					reqAndRelationshipPairList = new ArrayList<RequirementAndRelationshipPair>();
				RequirementAndRelationshipPair reqAndRelationshipPair = new RequirementAndRelationshipPair();
				reqAndRelationshipPair.setRequirement(regName);
				reqAndRelationshipPair.setRequirementOwnerId(validReq.getOwnerId());
				reqAndRelationshipPair.setRequirementUid(validReq.getUniqueId());
				RelationshipImpl relationship = new RelationshipImpl();
				relationship.setType(validReq.getCapability());
				reqAndRelationshipPair.setRelationships(relationship);

				ComponentInstance currentCapCompInstance = null;
				for (ComponentInstance compInstance : componentInstancesList) {
					if (compInstance.getName().equals(uploadRegInfo.getNode())) {
						currentCapCompInstance = compInstance;
						break;
					}
				}

				if (currentCapCompInstance == null) {
					log.debug("component instance  with name {}  in resource {} ", uploadRegInfo.getNode(), resource.getUniqueId());
					BeEcompErrorManager.getInstance().logInternalDataError("component instance with name " + uploadRegInfo.getNode() + "  in resource {} ", resource.getUniqueId(), ErrorSeverity.ERROR);
					ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
					return responseFormat;
				}
				regCapRelDef.setToNode(currentCapCompInstance.getUniqueId());
				log.debug("try to find aviable Capability  req name is {} ", validReq.getName());
				CapabilityDefinition aviableCapForRel = findAvailableCapabilityByTypeOrName(validReq, currentCapCompInstance, uploadRegInfo);
				if (aviableCapForRel == null) {
					log.debug("aviable capability was not found. req name is {} component instance is {}", validReq.getName(), currentCapCompInstance.getUniqueId());
					BeEcompErrorManager.getInstance().logInternalDataError("aviable capability was not found. req name is " + validReq.getName() + " component instance is " + currentCapCompInstance.getUniqueId(), resource.getUniqueId(),
							ErrorSeverity.ERROR);
					ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
					return responseFormat;
				}
				reqAndRelationshipPair.setCapability(aviableCapForRel.getName());
				reqAndRelationshipPair.setCapabilityUid(aviableCapForRel.getUniqueId());
				reqAndRelationshipPair.setCapabilityOwnerId(aviableCapForRel.getOwnerId());
				reqAndRelationshipPairList.add(reqAndRelationshipPair);
				regCapRelDef.setRelationships(reqAndRelationshipPairList);
				relations.add(regCapRelDef);

			}

		}
		totalCreateRel += (System.currentTimeMillis() - startAddRelation);

		return componentsUtils.getResponseFormat(ActionStatus.OK);
	}

	private ResponseFormat addPropertyValuesToRi(UploadComponentInstanceInfo uploadComponentInstanceInfo, Resource resource, Resource originResource, ComponentInstance currentCompInstance, String yamlName,
												 Map<String, List<ComponentInstanceProperty>> instProperties, Map<String, DataTypeDefinition> allDataTypes) {

		Map<String, List<UploadPropInfo>> propMap = uploadComponentInstanceInfo.getProperties();
		if (propMap != null && propMap.size() > 0) {
			Map<String, PropertyDefinition> currPropertiesMap = new HashMap<String, PropertyDefinition>();

			int index = 0;
			List<PropertyDefinition> listFromMap = originResource.getProperties();
			if (listFromMap == null || listFromMap.isEmpty()) {
				log.debug("failed to find properties ");
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND);
				return responseFormat;
			}
			for (PropertyDefinition prop : listFromMap) {
				String propName = prop.getName();
				if (!currPropertiesMap.containsKey(propName)) {
					currPropertiesMap.put(propName, prop);
				}
			}
			List<ComponentInstanceProperty> instPropList = new ArrayList<>();
			for (List<UploadPropInfo> propertyList : propMap.values()) {

				UploadPropInfo propertyInfo = propertyList.get(0);
				String propName = propertyInfo.getName();
				if (!currPropertiesMap.containsKey(propName)) {
					log.debug("failed to find property {} ", propName);
					ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND, propName);
					return responseFormat;
				}
				PropertyDefinition curPropertyDef = currPropertiesMap.get(propName);
				ComponentInstanceProperty property = null;

				String value = null;
				List<GetInputValueDataDefinition> getInputs = null;
				boolean isValidate = true;
				if (propertyInfo.getValue() != null) {
					getInputs = propertyInfo.getGet_input();
					isValidate = getInputs == null || getInputs.isEmpty();
					if (isValidate) {
						value = ImportUtils.getPropertyJsonStringValue(propertyInfo.getValue(), curPropertyDef.getType());
					} else
						value = ImportUtils.getPropertyJsonStringValue(propertyInfo.getValue(), ToscaTagNamesEnum.GET_INPUT.getElementName());
				}
				String innerType = null;
				property = new ComponentInstanceProperty(curPropertyDef, value, null);

				Either<String, StorageOperationStatus> validatevalueEiter = validatePropValueBeforeCreate(property, value, isValidate, innerType, allDataTypes);
				if (validatevalueEiter.isRight()) {
					return componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(validatevalueEiter.right().value()));
				}

				// String uniqueId = UniqueIdBuilder.buildResourceInstancePropertyValueUid(currentCompInstance.getComponentUid(), index++);
				// property.setUniqueId(uniqueId);
				property.setValue(validatevalueEiter.left().value());

				if (getInputs != null && !getInputs.isEmpty()) {
					List<GetInputValueDataDefinition> getInputValues = new ArrayList<>();
					for (GetInputValueDataDefinition getInput : getInputs) {
						List<InputDefinition> inputs = resource.getInputs();
						if (inputs == null || inputs.isEmpty()) {
							log.debug("Failed to add property {} to resource instance {}. Inputs list is empty ", property, currentCompInstance.getUniqueId());
							return componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT);
						}

						Optional<InputDefinition> optional = inputs.stream().filter(p -> p.getName().equals(getInput.getInputName())).findAny();
						if (!optional.isPresent()) {
							log.debug("Failed to find input {} ", getInput.getInputName());
							// @@TODO error message
							return componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT);
						}
						InputDefinition input = optional.get();
						getInput.setInputId(input.getUniqueId());
						getInputValues.add(getInput);

						GetInputValueDataDefinition getInputIndex = getInput.getGetInputIndex();
						if (getInputIndex != null) {
							optional = inputs.stream().filter(p -> p.getName().equals(getInputIndex.getInputName())).findAny();
							if (!optional.isPresent()) {
								log.debug("Failed to find input {} ", getInputIndex.getInputName());
								// @@TODO error message
								return componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT);
							}
							InputDefinition inputIndex = optional.get();
							getInputIndex.setInputId(inputIndex.getUniqueId());
							getInputValues.add(getInputIndex);

						}

					}
					property.setGetInputValues(getInputValues);
				}
				instPropList.add(property);
				// delete overriden property
				currPropertiesMap.remove(property.getName());
			}
			// add rest of properties
			if (!currPropertiesMap.isEmpty()) {
				for (PropertyDefinition value : currPropertiesMap.values()) {
					instPropList.add(new ComponentInstanceProperty(value));
				}
			}
			instProperties.put(currentCompInstance.getUniqueId(), instPropList);
		}
		return componentsUtils.getResponseFormat(ActionStatus.OK);
	}

	// US740820 Relate RIs according to capability name
	private CapabilityDefinition findAvailableCapabilityByTypeOrName(RequirementDefinition validReq, ComponentInstance currentCapCompInstance, UploadReqInfo uploadReqInfo) {
		if (null == uploadReqInfo.getCapabilityName() || validReq.getCapability().equals(uploadReqInfo.getCapabilityName())) {// get
			// by
			// capability
			// type
			return findAviableCapability(validReq, currentCapCompInstance);
		}
		return findAvailableCapability(validReq, currentCapCompInstance, uploadReqInfo);
	}

	private CapabilityDefinition findAvailableCapability(RequirementDefinition validReq, ComponentInstance currentCapCompInstance, UploadReqInfo uploadReqInfo) {
		CapabilityDefinition cap = null;
		Map<String, List<CapabilityDefinition>> capMap = currentCapCompInstance.getCapabilities();
		if (!capMap.containsKey(validReq.getCapability())) {
			return null;
		}
		Optional<CapabilityDefinition> capByName = capMap.get(validReq.getCapability()).stream().filter(p -> p.getName().equals(uploadReqInfo.getCapabilityName())).findAny();
		if (!capByName.isPresent()) {
			return null;
		}
		cap = capByName.get();

		if (cap.getMaxOccurrences() != null && !cap.getMaxOccurrences().equals(CapabilityDataDefinition.MAX_OCCURRENCES)) {
			String leftOccurrences = cap.getLeftOccurrences();
			int left = Integer.parseInt(leftOccurrences);
			if (left > 0) {
				--left;
				cap.setLeftOccurrences(String.valueOf(left));

			}

		}

		// TODO temporary fix - remove specific capability node validation -
		// String reqNode = validReq.getNode();
		// if (reqNode != null && !reqNode.isEmpty() &&
		// !cap.getCapabilitySources().contains(reqNode)) {
		// return null;
		// }
		// RequirementAndRelationshipPair relationPair = getReqRelPair(cap);
		// Either<Boolean, StorageOperationStatus> eitherStatus = componentInstanceOperation.isAvailableCapabilty(currentCapCompInstance, relationPair);
		// if (eitherStatus.isRight() || eitherStatus.left().value() == false) {
		// return null;
		// }
		return cap;
	}

	private RequirementAndRelationshipPair getReqRelPair(CapabilityDefinition cap) {
		RequirementAndRelationshipPair relationPair = new RequirementAndRelationshipPair();
		relationPair.setCapabilityUid(cap.getUniqueId());
		relationPair.setCapability(cap.getName());
		relationPair.setCapabilityOwnerId(cap.getOwnerId());
		return relationPair;
	}

	private CapabilityDefinition findAviableCapability(RequirementDefinition validReq, ComponentInstance currentCapCompInstance) {
		CapabilityDefinition aviableCapForRel = null;
		Map<String, List<CapabilityDefinition>> capMap = currentCapCompInstance.getCapabilities();
		if (capMap.containsKey(validReq.getCapability())) {
			List<CapabilityDefinition> capList = capMap.get(validReq.getCapability());

			for (CapabilityDefinition cap : capList) {
				// TODO temporary fix - remove specific capability node
				// String reqNode = validReq.getNode();
				// if (reqNode != null && !reqNode.isEmpty()) {
				// if (!cap.getCapabilitySources().contains(reqNode)) {
				// continue;
				// }
				// }
				if (cap.getMaxOccurrences() != null && !cap.getMaxOccurrences().equals(CapabilityDataDefinition.MAX_OCCURRENCES)) {
					String leftOccurrences = cap.getLeftOccurrences();
					if (leftOccurrences == null) {
						leftOccurrences = cap.getMaxOccurrences();
					}
					int left = Integer.parseInt(leftOccurrences);
					if (left > 0) {
						--left;
						cap.setLeftOccurrences(String.valueOf(left));
						aviableCapForRel = cap;
						break;
					} else {
						continue;
					}
				} else {
					aviableCapForRel = cap;
					break;
				}
			}
		}
		return aviableCapForRel;
	}

	private Either<RequirementDefinition, ResponseFormat> findAviableRequiremen(String regName, String yamlName, UploadComponentInstanceInfo uploadComponentInstanceInfo, ComponentInstance currentCompInstance, String capName) {
		Map<String, List<RequirementDefinition>> comInstRegDefMap = currentCompInstance.getRequirements();
		List<RequirementDefinition> list = comInstRegDefMap.get(capName);
		RequirementDefinition validRegDef = null;
		if (list == null) {
			for (Entry<String, List<RequirementDefinition>> entry : comInstRegDefMap.entrySet()) {
				for (RequirementDefinition reqDef : entry.getValue()) {
					if (reqDef.getName().equals(regName)) {
						if (reqDef.getMaxOccurrences() != null && !reqDef.getMaxOccurrences().equals(RequirementDataDefinition.MAX_OCCURRENCES)) {
							String leftOccurrences = reqDef.getLeftOccurrences();
							if (leftOccurrences == null) {
								leftOccurrences = reqDef.getMaxOccurrences();
							}
							int left = Integer.parseInt(leftOccurrences);
							if (left > 0) {
								--left;
								reqDef.setLeftOccurrences(String.valueOf(left));
								validRegDef = reqDef;
								break;
							} else {
								continue;
							}
						} else {
							validRegDef = reqDef;
							break;
						}

					}
				}
				if (validRegDef != null) {
					break;
				}
			}
		} else {
			for (RequirementDefinition reqDef : list) {
				if (reqDef.getName().equals(regName)) {
					if (reqDef.getMaxOccurrences() != null && !reqDef.getMaxOccurrences().equals(RequirementDataDefinition.MAX_OCCURRENCES)) {
						String leftOccurrences = reqDef.getLeftOccurrences();
						if (leftOccurrences == null) {
							leftOccurrences = reqDef.getMaxOccurrences();
						}
						int left = Integer.parseInt(leftOccurrences);
						if (left > 0) {
							--left;
							reqDef.setLeftOccurrences(String.valueOf(left));
							validRegDef = reqDef;
							break;
						} else {
							continue;
						}
					} else {
						validRegDef = reqDef;
						break;
					}
				}
			}
		}
		if (validRegDef == null) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_NODE_TEMPLATE, yamlName, uploadComponentInstanceInfo.getName(), uploadComponentInstanceInfo.getType());
			return Either.right(responseFormat);
		}
		return Either.left(validRegDef);
	}

	@SuppressWarnings("unchecked")
	public Either<ParsedToscaYamlInfo, ResponseFormat> parseResourceInfoFromYaml(String yamlFileName, Resource resource, String resourceYml, User user) {

		Map<String, Object> mappedToscaTemplate = (Map<String, Object>) new Yaml().load(resourceYml);
		Either<Object, ResultStatusEnum> toscaElementEither = ImportUtils.findToscaElement(mappedToscaTemplate, ToscaTagNamesEnum.TOPOLOGY_TEMPLATE, ToscaElementTypeEnum.ALL);
		if (toscaElementEither.isRight()) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE);
			return Either.right(responseFormat);
		}

		Either<Map<String, InputDefinition>, ResponseFormat> createInputsEither = createInputsFromYaml(yamlFileName, mappedToscaTemplate, resource);
		if (createInputsEither.isRight()) {
			ResponseFormat responseFormat = createInputsEither.right().value();
			return Either.right(responseFormat);
		}

		Either<Map<String, UploadComponentInstanceInfo>, ResponseFormat> uploadResInstancesEither = createResourcesInstanceInfoFromYaml(yamlFileName, mappedToscaTemplate, resource);
		if (uploadResInstancesEither.isRight()) {
			ResponseFormat responseFormat = uploadResInstancesEither.right().value();
			return Either.right(responseFormat);
		}

		Either<Map<String, GroupDefinition>, ResponseFormat> createGroupsFromYaml = createGroupsFromYaml(yamlFileName, mappedToscaTemplate, resource);
		if (createGroupsFromYaml.isRight()) {
			ResponseFormat responseFormat = createGroupsFromYaml.right().value();
			return Either.right(responseFormat);
		}

		ParsedToscaYamlInfo parsedToscaYamlInfo = new ParsedToscaYamlInfo();
		parsedToscaYamlInfo.setInputs(createInputsEither.left().value());
		parsedToscaYamlInfo.setInstances(uploadResInstancesEither.left().value());
		parsedToscaYamlInfo.setGroups(createGroupsFromYaml.left().value());

		return Either.left(parsedToscaYamlInfo);
	}

	private Either<Resource, ResponseFormat> createResourceInstances(User user, String yamlName, Resource resource, Map<String, UploadComponentInstanceInfo> uploadResInstancesMap, boolean inTransaction, boolean needLock,
																	 Map<String, Resource> nodeTypeNamespaceMap) {

		Either<Resource, ResponseFormat> eitherResource = null;
		log.debug("createResourceInstances is {} - going to create resource instanse from CSAR", yamlName);
		if (uploadResInstancesMap == null || uploadResInstancesMap.isEmpty()) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE);

			return Either.right(responseFormat);

		}
		Map<String, Resource> existingnodeTypeMap = new HashMap<>();
		if (nodeTypeNamespaceMap != null && !nodeTypeNamespaceMap.isEmpty()) {
			nodeTypeNamespaceMap.entrySet().stream().forEach(x -> existingnodeTypeMap.put(x.getValue().getToscaResourceName(), x.getValue()));
		}

		Iterator<Entry<String, UploadComponentInstanceInfo>> nodesInfoValue = uploadResInstancesMap.entrySet().iterator();
		Map<ComponentInstance, Resource> resourcesInstancesMap = new HashMap<>();
		while (nodesInfoValue.hasNext()) {
			log.debug("*************Going to create  resource instances {}", yamlName);
			Entry<String, UploadComponentInstanceInfo> uploadComponentInstanceInfoEntry = nodesInfoValue.next();
			UploadComponentInstanceInfo uploadComponentInstanceInfo = uploadComponentInstanceInfoEntry.getValue();

			// updating type if the type is node type name - we need to take the
			// updated name
			log.debug("*************Going to create  resource instances {}", uploadComponentInstanceInfo.getName());
			if (nodeTypeNamespaceMap.containsKey(uploadComponentInstanceInfo.getType())) {
				uploadComponentInstanceInfo.setType(nodeTypeNamespaceMap.get(uploadComponentInstanceInfo.getType()).getToscaResourceName());
			}

			eitherResource = validateResourceInstanceBeforeCreate(yamlName, uploadComponentInstanceInfo, existingnodeTypeMap);
			if (eitherResource.isRight()) {
				return eitherResource;
			}
			Resource refResource = eitherResource.left().value();

			ComponentInstance componentInstance = new ComponentInstance();

			componentInstance.setComponentUid(refResource.getUniqueId());

			ComponentTypeEnum containerComponentType = resource.getComponentType();
			NodeTypeEnum containerNodeType = containerComponentType.getNodeType();

			if (containerNodeType.equals(NodeTypeEnum.Resource) && uploadComponentInstanceInfo.getCapabilities() != null) {
				Either<Map<String, List<CapabilityDefinition>>, ResponseFormat> getValidComponentInstanceCapabilitiesRes = getValidComponentInstanceCapabilities(refResource.getCapabilities(), uploadComponentInstanceInfo.getCapabilities());
				if (getValidComponentInstanceCapabilitiesRes.isRight()) {
					return Either.right(getValidComponentInstanceCapabilitiesRes.right().value());
				} else {
					componentInstance.setCapabilities(getValidComponentInstanceCapabilitiesRes.left().value());
				}
			}
			if (!existingnodeTypeMap.containsKey(uploadComponentInstanceInfo.getType())) {
				log.debug("createResourceInstances - not found lates version for resource instance with name {} and type ", uploadComponentInstanceInfo.getName(), uploadComponentInstanceInfo.getType());
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_NODE_TEMPLATE, yamlName, uploadComponentInstanceInfo.getName(), uploadComponentInstanceInfo.getType());
				return Either.right(responseFormat);
			}
			Resource origResource = existingnodeTypeMap.get(uploadComponentInstanceInfo.getType());
			componentInstance.setName(uploadComponentInstanceInfo.getName());
			componentInstance.setIcon(origResource.getIcon());

			resourcesInstancesMap.put(componentInstance, origResource);

		}
		if (MapUtils.isNotEmpty(resourcesInstancesMap)) {

			StorageOperationStatus status = toscaOperationFacade.associateComponentInstancesToComponent(resource, resourcesInstancesMap, false);
			if (status != null && status != StorageOperationStatus.OK) {
				log.debug("Failed to add component instances to container component {}", resource.getName());
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status));
				eitherResource = Either.right(responseFormat);
				return eitherResource;
			}

		}

		log.debug("*************Going to get resource {}", resource.getUniqueId());
		ComponentParametersView parametersView = new ComponentParametersView();
		parametersView.disableAll();
		parametersView.setIgnoreComponentInstances(false);
		parametersView.setIgnoreUsers(false);
		parametersView.setIgnoreInputs(false); // inputs are read when creating
		// property values on instances
		Either<Resource, StorageOperationStatus> eitherGerResource = toscaOperationFacade.getToscaElement(resource.getUniqueId(), parametersView);
		log.debug("*************finished to get resource {}", resource.getUniqueId());
		if (eitherGerResource.isRight()) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(eitherGerResource.right().value()), resource);

			return Either.right(responseFormat);

		}

		if (eitherGerResource.left().value().getComponentInstances() == null || eitherGerResource.left().value().getComponentInstances().isEmpty()) {

			log.debug("Error when create resource inctanse from csar. ComponentInstances list empty");
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Error when create resource inctanse from csar. ComponentInstances list empty");
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE);
			return Either.right(responseFormat);

		}

		return Either.left(eitherGerResource.left().value());
	}

	private Either<Resource, ResponseFormat> validateResourceInstanceBeforeCreate(String yamlName, UploadComponentInstanceInfo uploadComponentInstanceInfo, Map<String, Resource> nodeTypeNamespaceMap) {
		log.debug("validateResourceInstanceBeforeCreate - going to validate resource instance with name {} and type before create", uploadComponentInstanceInfo.getName(), uploadComponentInstanceInfo.getType());
		Resource refResource = null;
		if (nodeTypeNamespaceMap.containsKey(uploadComponentInstanceInfo.getType())) {
			refResource = nodeTypeNamespaceMap.get(uploadComponentInstanceInfo.getType());
		} else {
			Either<Resource, StorageOperationStatus> findResourceEither = toscaOperationFacade.getLatestCertifiedNodeTypeByToscaResourceName(uploadComponentInstanceInfo.getType());
			if (findResourceEither.isRight()) {
				log.debug("validateResourceInstanceBeforeCreate - not found lates version for resource instance with name {} and type ", uploadComponentInstanceInfo.getName(), uploadComponentInstanceInfo.getType());
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(findResourceEither.right().value()));
				return Either.right(responseFormat);
			}
			refResource = findResourceEither.left().value();
			nodeTypeNamespaceMap.put(refResource.getToscaResourceName(), refResource);
		}
		String componentState = refResource.getComponentMetadataDefinition().getMetadataDataDefinition().getState();
		if (componentState.equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
			log.debug("validateResourceInstanceBeforeCreate - component instance of component {} can not be created because the component is in an illegal state {}.", refResource.getName(), componentState);
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.ILLEGAL_COMPONENT_STATE, refResource.getComponentType().getValue(), refResource.getName(), componentState);
			return Either.right(responseFormat);
		}
		ResourceTypeEnum resourceTypeEnum = refResource.getResourceType();
		if (resourceTypeEnum == ResourceTypeEnum.VF) {
			log.debug("validateResourceInstanceBeforeCreate -  ref resource type is  ", resourceTypeEnum);
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_NODE_TEMPLATE, yamlName, uploadComponentInstanceInfo.getName(), uploadComponentInstanceInfo.getType());
			return Either.right(responseFormat);
		}
		return Either.left(refResource);
	}

	private Either<Map<String, UploadComponentInstanceInfo>, ResponseFormat> createResourcesInstanceInfoFromYaml(String yamlFileName, Map<String, Object> toscaJson, Resource resource) {
		Map<String, UploadComponentInstanceInfo> moduleComponentInstances = new HashMap<String, UploadComponentInstanceInfo>();
		Either<Map<String, UploadComponentInstanceInfo>, ResponseFormat> result = Either.left(moduleComponentInstances);
		Either<Map<String, Object>, ResultStatusEnum> eitherNodesTemlates = ImportUtils.findFirstToscaMapElement(toscaJson, ToscaTagNamesEnum.NODE_TEMPLATES);
		if (eitherNodesTemlates.isLeft()) {
			Map<String, Object> jsonNodeTemplates = eitherNodesTemlates.left().value();

			Iterator<Entry<String, Object>> nodesNameValue = jsonNodeTemplates.entrySet().iterator();
			while (nodesNameValue.hasNext()) {
				Entry<String, Object> nodeNameValue = nodesNameValue.next();
				Either<UploadComponentInstanceInfo, ResponseFormat> eitherNode = createModuleComponentInstanceInfo(nodeNameValue.getValue());
				if (eitherNode.isRight()) {
					log.info("error when creating node template:{}, for resource:{}", nodeNameValue.getKey(), resource.getName());
					return Either.right(eitherNode.right().value());
				} else {
					UploadComponentInstanceInfo uploadComponentInstanceInfo = eitherNode.left().value();
					uploadComponentInstanceInfo.setName(nodeNameValue.getKey());
					moduleComponentInstances.put(nodeNameValue.getKey(), uploadComponentInstanceInfo);
				}

			}

		}
		if (moduleComponentInstances.isEmpty()) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlFileName);
			return Either.right(responseFormat);
		}

		return result;
	}

	private Either<UploadComponentInstanceInfo, ResponseFormat> createModuleComponentInstanceInfo(Object nodeTemplateJson) {

		UploadComponentInstanceInfo nodeTemplateInfo = new UploadComponentInstanceInfo();
		Either<UploadComponentInstanceInfo, ResponseFormat> result = Either.left(nodeTemplateInfo);

		try {
			if (nodeTemplateJson instanceof String) {
				String nodeTemplateJsonString = (String) nodeTemplateJson;
				nodeTemplateInfo.setType(nodeTemplateJsonString);
			} else if (nodeTemplateJson instanceof Map) {
				Map<String, Object> nodeTemplateJsonMap = (Map<String, Object>) nodeTemplateJson;
				// Type
				if (nodeTemplateJsonMap.containsKey(ToscaTagNamesEnum.TYPE.getElementName())) {
					nodeTemplateInfo.setType((String) nodeTemplateJsonMap.get(ToscaTagNamesEnum.TYPE.getElementName()));
				}

				if (nodeTemplateJsonMap.containsKey(ToscaTagNamesEnum.REQUIREMENTS.getElementName())) {
					Either<Map<String, List<UploadReqInfo>>, ResponseFormat> regResponse = createReqModuleFromYaml(nodeTemplateInfo, nodeTemplateJsonMap);
					if (regResponse.isRight())
						return Either.right(regResponse.right().value());
					if (regResponse.left().value().size() > 0) {
						nodeTemplateInfo.setRequirements(regResponse.left().value());
					}
				}

				if (nodeTemplateJsonMap.containsKey(ToscaTagNamesEnum.CAPABILITIES.getElementName())) {
					Either<Map<String, List<UploadCapInfo>>, ResponseFormat> eitherCapRes = createCapModuleFromYaml(nodeTemplateInfo, nodeTemplateJsonMap);
					if (eitherCapRes.isRight())
						return Either.right(eitherCapRes.right().value());
					if (eitherCapRes.left().value().size() > 0) {
						nodeTemplateInfo.setCapabilities(eitherCapRes.left().value());
					}
				}
				if (nodeTemplateJsonMap.containsKey(ToscaTagNamesEnum.PROPERTIES.getElementName())) {
					Either<Map<String, List<UploadPropInfo>>, ResponseFormat> regResponse = createPropModuleFromYaml(nodeTemplateJsonMap);
					if (regResponse.isRight())
						return Either.right(regResponse.right().value());
					if (regResponse.left().value().size() > 0) {
						nodeTemplateInfo.setProperties(regResponse.left().value());
					}
				}
			} else {

				result = Either.right(componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE));

			}
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeSystemError, "Import Resource - create capability");
			BeEcompErrorManager.getInstance().logBeSystemError("Import Resource - create capability");
			log.debug("error when creating capability, message:{}", e.getMessage(), e);
			result = Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_YAML));
		}

		return result;
	}

	private Either<Map<String, List<UploadPropInfo>>, ResponseFormat> createPropModuleFromYaml(Map<String, Object> nodeTemplateJsonMap) {
		Map<String, List<UploadPropInfo>> moduleProp = new HashMap<String, List<UploadPropInfo>>();
		Either<Map<String, List<UploadPropInfo>>, ResponseFormat> response = Either.left(moduleProp);
		Either<Map<String, Object>, ResultStatusEnum> toscaProperties = ImportUtils.findFirstToscaMapElement(nodeTemplateJsonMap, ToscaTagNamesEnum.PROPERTIES);
		if (toscaProperties.isLeft()) {
			Map<String, Object> jsonProperties = toscaProperties.left().value();
			for (Entry<String, Object> jsonPropObj : jsonProperties.entrySet()) {
				// Property
				String propName = jsonPropObj.getKey();
				Object propValue = jsonPropObj.getValue();

				if (valueContainsPattern(STR_REPLACE_PATTERN, propValue)) {
					log.debug("Ignore property value {}.", propName);
					continue;
				}

				if (valueContainsPattern(TOKEN_PATTERN, propValue)) {
					log.debug("Ignore property value {}.", propName);
					continue;
				}
				if (valueContainsPattern(GET_PROPERTY_PATTERN, propValue)) {
					log.debug("Ignore property value {}.", propName);
					continue;
				}

				if (valueContainsPattern(CONCAT_PATTERN, propValue)) {
					log.debug("Ignore property value {}.", propName);
					continue;
				}

				UploadPropInfo propertyDef = new UploadPropInfo();
				propertyDef.setValue(propValue);
				propertyDef.setName(propName);
				if (propValue instanceof Map) {
					if (((Map<String, Object>) propValue).containsKey(ToscaTagNamesEnum.TYPE.getElementName())) {
						propertyDef.setType(((Map<String, Object>) propValue).get(ToscaTagNamesEnum.TYPE.getElementName()).toString());
					}

					if (((Map<String, Object>) propValue).containsKey(ToscaTagNamesEnum.GET_INPUT.getElementName())
							|| ImportUtils.getPropertyJsonStringValue(propValue, ToscaPropertyType.MAP.getType()).contains(ToscaTagNamesEnum.GET_INPUT.getElementName())) {
						createGetInputModuleFromMap(propName, (Map<String, Object>) propValue, propertyDef);
					}

					if (((Map<String, Object>) propValue).containsKey(ToscaTagNamesEnum.DESCRIPTION.getElementName())) {
						propertyDef.setDescription(((Map<String, Object>) propValue).get(ToscaTagNamesEnum.DESCRIPTION.getElementName()).toString());
					}
					if (((Map<String, Object>) propValue).containsKey(ToscaTagNamesEnum.DEFAULT_VALUE.getElementName())) {
						propertyDef.setValue(((Map<String, Object>) propValue).get(ToscaTagNamesEnum.DEFAULT_VALUE.getElementName()));
					}
					if (((Map<String, Object>) propValue).containsKey(ToscaTagNamesEnum.IS_PASSWORD.getElementName())) {
						propertyDef.setPassword(Boolean.getBoolean(((Map<String, Object>) propValue).get(ToscaTagNamesEnum.IS_PASSWORD.getElementName()).toString()));
					} else {
						propertyDef.setValue(propValue);
					}
				} else if (propValue instanceof List) {
					List<Object> propValueList = (List<Object>) propValue;

					createInputPropList(propertyDef, propValueList);
					propertyDef.setValue(propValue);
				}

				if (moduleProp.containsKey(propName)) {
					moduleProp.get(propName).add(propertyDef);
				} else {
					List<UploadPropInfo> list = new ArrayList<UploadPropInfo>();
					list.add(propertyDef);
					moduleProp.put(propName, list);
				}
			}
		}
		return response;
	}

	private void createInputPropList(UploadPropInfo propertyDef, List<Object> propValueList) {
		for (Object objValue : propValueList) {

			if (objValue instanceof Map) {
				Map<String, Object> objMap = (Map<String, Object>) objValue;
				if (objMap.containsKey(ToscaTagNamesEnum.GET_INPUT.getElementName()))
					createGetInputModuleFromMap(propertyDef.getName(), objMap, propertyDef);
				else {
					Set<String> keys = objMap.keySet();
					for (String key : keys) {
						Object value = objMap.get(key);
						if (value instanceof Map) {
							createGetInputModuleFromMap(key, (Map<String, Object>) value, propertyDef);

						} else if (value instanceof List) {
							List<Object> propSubValueList = (List<Object>) value;

							createInputPropList(propertyDef, propSubValueList);
						}

					}
				}

			} else if (objValue instanceof List) {
				List<Object> propSubValueList = (List<Object>) objValue;

				createInputPropList(propertyDef, propSubValueList);

			}

		}
	}

	private void createGetInputModuleFromMap(String propName, Map<String, Object> propValue, UploadPropInfo propertyDef) {

		if (propValue.containsKey(ToscaTagNamesEnum.GET_INPUT.getElementName())) {
			Object getInput = propValue.get(ToscaTagNamesEnum.GET_INPUT.getElementName());
			GetInputValueDataDefinition getInputInfo = new GetInputValueDataDefinition();
			List<GetInputValueDataDefinition> getInputs = propertyDef.getGet_input();
			if (getInputs == null) {
				getInputs = new ArrayList<GetInputValueDataDefinition>();
			}
			if (getInput instanceof String) {

				getInputInfo.setInputName((String) getInput);
				getInputInfo.setPropName(propName);

			} else if (getInput instanceof List) {
				List<Object> getInputList = (List<Object>) getInput;
				getInputInfo.setPropName(propName);
				getInputInfo.setInputName((String) getInputList.get(0));
				if (getInputList.size() > 1) {
					Object indexObj = getInputList.get(1);
					if (indexObj instanceof Integer) {
						getInputInfo.setIndexValue((Integer) indexObj);
					} else if (indexObj instanceof Float) {
						int index = ((Float) indexObj).intValue();
						getInputInfo.setIndexValue(index);
					} else if (indexObj instanceof Map && ((Map<String, Object>) indexObj).containsKey(ToscaTagNamesEnum.GET_INPUT.getElementName())) {
						Object index = ((Map<String, Object>) indexObj).get(ToscaTagNamesEnum.GET_INPUT.getElementName());
						GetInputValueDataDefinition getInputInfoIndex = new GetInputValueDataDefinition();
						getInputInfoIndex.setInputName((String) index);
						getInputInfoIndex.setPropName(propName);
						getInputInfo.setGetInputIndex(getInputInfoIndex);
					}
					getInputInfo.setList(true);
				}

			}
			getInputs.add(getInputInfo);
			propertyDef.setGet_input(getInputs);
			propertyDef.setValue(propValue);
		} else {
			Set<String> keys = propValue.keySet();
			for (String key : keys) {
				Object value = propValue.get(key);
				if (value instanceof Map) {
					createGetInputModuleFromMap(key, (Map<String, Object>) value, propertyDef);

				} else if (value instanceof List) {
					List<Object> valueList = (List<Object>) value;
					for (Object o : valueList) {
						if (o instanceof Map) {
							createGetInputModuleFromMap(key, (Map<String, Object>) o, propertyDef);

						}
					}

				}

			}

		}
	}

	/*
	 * private boolean valueContainsStrReplace(Object propValue) {
	 *
	 * log.debug("valueContainsStrReplace value is {}", propValue); boolean result = false; if (propValue != null) { log.debug("valueContainsStrReplace value is {}", propValue.getClass()); Matcher matcher =
	 * STR_REPLACE_PATTERN.matcher(propValue.toString()); result = matcher.find(); }
	 *
	 * return result; }
	 *
	 * private boolean valueContainsToken(Object propValue) {
	 *
	 * log.debug("valueContainsToken value is {}", propValue); boolean result = false; if (propValue != null) { log.debug("valueContainsToken value is {}", propValue.getClass()); Matcher matcher = TOKEN_PATTERN.matcher(propValue.toString()); result =
	 * matcher.find(); }
	 *
	 * return result; }
	 */

	private boolean valueContainsPattern(Pattern pattern, Object propValue) {

		log.debug("valueContainsToken value is {}", propValue);
		boolean result = false;
		if (propValue != null) {
			log.debug("valueContainspattern value is {}", propValue.getClass());
			Matcher matcher = pattern.matcher(propValue.toString());
			result = matcher.find();
		}

		return result;

	}

	private Either<Map<String, List<UploadCapInfo>>, ResponseFormat> createCapModuleFromYaml(UploadComponentInstanceInfo nodeTemplateInfo, Map<String, Object> nodeTemplateJsonMap) {
		Map<String, List<UploadCapInfo>> moduleCap = new HashMap<String, List<UploadCapInfo>>();
		Either<Map<String, List<UploadCapInfo>>, ResponseFormat> response = Either.left(moduleCap);
		Either<List<Object>, ResultStatusEnum> toscaRequirements = ImportUtils.findFirstToscaListElement(nodeTemplateJsonMap, ToscaTagNamesEnum.CAPABILITIES);
		if (toscaRequirements.isLeft()) {
			List<Object> jsonCapabilities = toscaRequirements.left().value();

			for (Object jsonCapObj : jsonCapabilities) {
				// Requirement
				Map<String, Object> capJsonWrapper = (Map<String, Object>) jsonCapObj;
				String capName = capJsonWrapper.keySet().iterator().next();
				Either<UploadCapInfo, ResponseFormat> eitherCap = createModuleNodeTemplateCap(capJsonWrapper.get(capName));
				if (eitherCap.isRight()) {
					log.info("error when creating Requirement:{}, for node:{}", capName, nodeTemplateInfo);
					return Either.right(eitherCap.right().value());
				} else {
					UploadCapInfo requirementDef = eitherCap.left().value();
					requirementDef.setName(capName);
					if (moduleCap.containsKey(capName)) {
						moduleCap.get(capName).add(requirementDef);
					} else {
						List<UploadCapInfo> list = new ArrayList<UploadCapInfo>();
						list.add(requirementDef);
						moduleCap.put(capName, list);
					}

				}
			}

		}

		return response;
	}

	private Either<Map<String, List<UploadReqInfo>>, ResponseFormat> createReqModuleFromYaml(UploadComponentInstanceInfo nodeTemplateInfo, Map<String, Object> nodeTemplateJsonMap) {
		Map<String, List<UploadReqInfo>> moduleRequirements = new HashMap<String, List<UploadReqInfo>>();
		Either<Map<String, List<UploadReqInfo>>, ResponseFormat> response = Either.left(moduleRequirements);
		Either<List<Object>, ResultStatusEnum> toscaRequirements = ImportUtils.findFirstToscaListElement(nodeTemplateJsonMap, ToscaTagNamesEnum.REQUIREMENTS);
		if (toscaRequirements.isLeft()) {
			List<Object> jsonRequirements = toscaRequirements.left().value();

			for (Object jsonRequirementObj : jsonRequirements) {
				// Requirement
				Map<String, Object> requirementJsonWrapper = (Map<String, Object>) jsonRequirementObj;
				String requirementName = requirementJsonWrapper.keySet().iterator().next();
				Either<UploadReqInfo, ResponseFormat> eitherRequirement = createModuleNodeTemplateReg(requirementJsonWrapper.get(requirementName));
				if (eitherRequirement.isRight()) {
					log.info("error when creating Requirement:{}, for node:{}", requirementName, nodeTemplateInfo);
					return Either.right(eitherRequirement.right().value());
				} else {
					UploadReqInfo requirementDef = eitherRequirement.left().value();
					requirementDef.setName(requirementName);
					if (moduleRequirements.containsKey(requirementName)) {
						moduleRequirements.get(requirementName).add(requirementDef);
					} else {
						List<UploadReqInfo> list = new ArrayList<UploadReqInfo>();
						list.add(requirementDef);
						moduleRequirements.put(requirementName, list);
					}

				}
			}

		}
		return response;
	}

	private Either<UploadCapInfo, ResponseFormat> createModuleNodeTemplateCap(Object capObject) {
		UploadCapInfo capTemplateInfo = new UploadCapInfo();
		Either<UploadCapInfo, ResponseFormat> result = Either.left(capTemplateInfo);

		if (capObject instanceof String) {
			String nodeTemplateJsonString = (String) capObject;
			capTemplateInfo.setNode(nodeTemplateJsonString);
		} else if (capObject instanceof Map) {
			Map<String, Object> nodeTemplateJsonMap = (Map<String, Object>) capObject;
			// Type
			if (nodeTemplateJsonMap.containsKey(ToscaTagNamesEnum.NODE.getElementName())) {
				capTemplateInfo.setNode((String) nodeTemplateJsonMap.get(ToscaTagNamesEnum.NODE.getElementName()));
			}
			if (nodeTemplateJsonMap.containsKey(ToscaTagNamesEnum.TYPE.getElementName())) {
				capTemplateInfo.setType((String) nodeTemplateJsonMap.get(ToscaTagNamesEnum.TYPE.getElementName()));
			}
			if (nodeTemplateJsonMap.containsKey(ToscaTagNamesEnum.VALID_SOURCE_TYPES.getElementName())) {
				Either<List<Object>, ResultStatusEnum> validSourceTypesRes = ImportUtils.findFirstToscaListElement(nodeTemplateJsonMap, ToscaTagNamesEnum.VALID_SOURCE_TYPES);
				if (validSourceTypesRes.isLeft()) {
					capTemplateInfo.setValidSourceTypes(validSourceTypesRes.left().value().stream().map(o -> o.toString()).collect(Collectors.toList()));
				}
			}
			if (nodeTemplateJsonMap.containsKey(ToscaTagNamesEnum.PROPERTIES.getElementName())) {
				Either<Map<String, List<UploadPropInfo>>, ResponseFormat> regResponse = createPropModuleFromYaml(nodeTemplateJsonMap);
				if (regResponse.isRight())
					return Either.right(regResponse.right().value());
				if (!regResponse.left().value().isEmpty()) {
					List<UploadPropInfo> properties = new ArrayList<UploadPropInfo>();
					regResponse.left().value().values().forEach(list -> properties.addAll(list));
					if (!properties.isEmpty())
						capTemplateInfo.setProperties(properties);
				}
			}
		}

		return result;
	}

	private Either<UploadReqInfo, ResponseFormat> createModuleNodeTemplateReg(Object regObject) {

		UploadReqInfo regTemplateInfo = new UploadReqInfo();
		Either<UploadReqInfo, ResponseFormat> result = Either.left(regTemplateInfo);

		if (regObject instanceof String) {
			String nodeTemplateJsonString = (String) regObject;
			regTemplateInfo.setNode(nodeTemplateJsonString);
		} else if (regObject instanceof Map) {
			Map<String, Object> nodeTemplateJsonMap = (Map<String, Object>) regObject;
			// Type
			if (nodeTemplateJsonMap.containsKey(ToscaTagNamesEnum.NODE.getElementName())) {
				regTemplateInfo.setNode((String) nodeTemplateJsonMap.get(ToscaTagNamesEnum.NODE.getElementName()));
			}
			// US740820 Relate RIs according to capability name
			if (nodeTemplateJsonMap.containsKey(ToscaTagNamesEnum.CAPABILITY.getElementName())) {
				regTemplateInfo.setCapabilityName((String) nodeTemplateJsonMap.get(ToscaTagNamesEnum.CAPABILITY.getElementName()));
			}
		}

		return result;
	}

	public Either<Resource, ResponseFormat> propagateStateToCertified(User user, Resource resource, LifecycleChangeInfoWithAction lifecycleChangeInfo, boolean inTransaction, boolean needLock) {
		Either<Resource, ResponseFormat> result = null;

		// resource updated with checkout. certify the resource
		if (resource.getLifecycleState().equals(LifecycleStateEnum.CERTIFIED)) {
			Either<Either<ArtifactDefinition, Operation>, ResponseFormat> eitherPopulated = populateToscaArtifacts(resource, user, false, inTransaction, needLock);
			result = eitherPopulated.isLeft() ? Either.left(resource) : Either.right(eitherPopulated.right().value());
			return result;
		}
		try {
			result = lifecycleBusinessLogic.changeState(resource.getUniqueId(), user, LifeCycleTransitionEnum.CERTIFICATION_REQUEST, lifecycleChangeInfo, inTransaction, needLock);
			if (result.isLeft()) {
				resource = result.left().value();
				result = lifecycleBusinessLogic.changeState(resource.getUniqueId(), user, LifeCycleTransitionEnum.START_CERTIFICATION, lifecycleChangeInfo, inTransaction, needLock);
			}
			if (result.isLeft()) {
				resource = result.left().value();
				result = lifecycleBusinessLogic.changeState(resource.getUniqueId(), user, LifeCycleTransitionEnum.CERTIFY, lifecycleChangeInfo, inTransaction, needLock);
			}
			return result;
		} finally {
			if (result == null || result.isRight()) {
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeSystemError, "Change LifecycleState - Certify");
				BeEcompErrorManager.getInstance().logBeSystemError("Change LifecycleState - Certify");
				if (inTransaction == false) {
					log.debug("operation failed. do rollback");
					titanDao.rollback();
				}
			} else if (inTransaction == false) {
				log.debug("operation success. do commit");
				titanDao.commit();
			}
		}
	}

	/*
	 * /**
	 *
	 * @deprecated Use {@link #createOrUpdateResourceByImport(Resource,User,boolean, boolean,boolean)} instead
	 */
	/*
	 * public Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> createOrUpdateResourceByImport(Resource resource, User user, AuditingActionEnum auditingEnum, boolean isNormative, boolean needLock) { return
	 * createOrUpdateResourceByImport(resource, user, isNormative, false, needLock); }
	 */

	public Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> createOrUpdateResourceByImport(Resource resource, User user, boolean isNormative, boolean isInTransaction, boolean needLock) {

		// check if resource already exist
		Either<Resource, StorageOperationStatus> latestByName = toscaOperationFacade.getLatestByName(resource.getName());
		Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> result = null;

		// create
		if (latestByName.isRight() && latestByName.right().value().equals(StorageOperationStatus.NOT_FOUND)) {

			Either<Resource, StorageOperationStatus> latestByToscaName = toscaOperationFacade.getLatestByToscaResourceName(resource.getToscaResourceName());
			if (latestByToscaName.isRight() && latestByToscaName.right().value().equals(StorageOperationStatus.NOT_FOUND))
				result = createResourceByImport(resource, user, isNormative, isInTransaction);

			else {
				StorageOperationStatus status = latestByName.right().value();
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeResourceMissingError, "Create / Update resource by import", resource.getName());
				BeEcompErrorManager.getInstance().logBeComponentMissingError("Create / Update resource by import", ComponentTypeEnum.RESOURCE.getValue(), resource.getName());
				log.debug("resource already exist {}. status={}", resource.getName(), status);
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESOURCE_ALREADY_EXISTS);
				componentsUtils.auditResource(responseFormat, user, resource, "", "", AuditingActionEnum.IMPORT_RESOURCE, null);
				result = Either.right(responseFormat);
			}

		}

		// update
		else if (latestByName.isLeft()) {
			result = updateExistingResourceByImport(resource, latestByName.left().value(), user, isNormative, needLock);
		}

		// error
		else {
			StorageOperationStatus status = latestByName.right().value();
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeResourceMissingError, "Create / Update resource by import", resource.getName());
			log.debug("failed to get latest version of resource {}. status={}", resource.getName(), status);
			ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(latestByName.right().value()), resource);
			componentsUtils.auditResource(responseFormat, user, resource, "", "", AuditingActionEnum.IMPORT_RESOURCE, null);
			result = Either.right(responseFormat);
		}
		return result;

	}

	private Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> createResourceByImport(Resource resource, User user, boolean isNormative, boolean isInTransaction) {
		log.debug("resource with name {} does not exist. create new resource", resource.getName());
		Either<Resource, ResponseFormat> response = validateResourceBeforeCreate(resource, user, AuditingActionEnum.IMPORT_RESOURCE, isInTransaction);
		if (response.isRight()) {
			return Either.right(response.right().value());
		}
		Either<Resource, ResponseFormat> createResponse = createResourceByDao(resource, user, AuditingActionEnum.IMPORT_RESOURCE, isNormative, isInTransaction, null);
		if (createResponse.isRight()) {
			return Either.right(createResponse.right().value());
		} else {
			ImmutablePair<Resource, ActionStatus> resourcePair = new ImmutablePair<>(createResponse.left().value(), ActionStatus.CREATED);
			ASDCKpiApi.countImportResourcesKPI();
			return Either.left(resourcePair);

		}
	}

	public boolean isResourceExist(String resourceName) {
		Either<Resource, StorageOperationStatus> latestByName = toscaOperationFacade.getLatestByName(resourceName);
		return latestByName.isLeft();
	}

	private Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> updateExistingResourceByImport(Resource newResource, Resource oldResource, User user, boolean inTransaction, boolean needLock) {
		String lockedResourceId = oldResource.getUniqueId();
		log.debug("found resource: name={}, id={}, version={}, state={}", oldResource.getName(), lockedResourceId, oldResource.getVersion(), oldResource.getLifecycleState());
		Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> result = null;
		try {
			if (needLock) {
				Either<Boolean, ResponseFormat> lockResult = lockComponent(lockedResourceId, oldResource, "Update Resource by Import");
				if (lockResult.isRight()) {
					return Either.right(lockResult.right().value());
				}
			}

			Either<Resource, ResponseFormat> prepareResourceForUpdate = prepareResourceForUpdate(oldResource, user, inTransaction, false);
			if (prepareResourceForUpdate.isRight()) {
				ResponseFormat responseFormat = prepareResourceForUpdate.right().value();
				log.info("resource {} cannot be updated. reason={}", lockedResourceId, responseFormat.getFormattedMessage());
				componentsUtils.auditResource(responseFormat, user, newResource, oldResource.getLifecycleState().name(), oldResource.getVersion(), AuditingActionEnum.IMPORT_RESOURCE, null);
				result = Either.right(prepareResourceForUpdate.right().value());
				return result;
			}
			oldResource = prepareResourceForUpdate.left().value();

			mergeOldResourceMetadataWithNew(oldResource, newResource);

			Either<Boolean, ResponseFormat> validateFieldsResponse = validateResourceFieldsBeforeUpdate(oldResource, newResource, inTransaction);
			if (validateFieldsResponse.isRight()) {
				result = Either.right(validateFieldsResponse.right().value());
				return result;
			}

			// contact info normalization
			newResource.setContactId(newResource.getContactId().toLowerCase());
			// non-updatable fields
			newResource.setCreatorUserId(user.getUserId());
			newResource.setCreatorFullName(user.getFullName());
			newResource.setLastUpdaterUserId(user.getUserId());
			newResource.setLastUpdaterFullName(user.getFullName());
			newResource.setUniqueId(oldResource.getUniqueId());
			newResource.setVersion(oldResource.getVersion());
			newResource.setInvariantUUID(oldResource.getInvariantUUID());
			newResource.setLifecycleState(oldResource.getLifecycleState());
			newResource.setUUID(oldResource.getUUID());
			newResource.setNormalizedName(oldResource.getNormalizedName());
			newResource.setSystemName(oldResource.getSystemName());
			if (oldResource.getCsarUUID() != null) {
				newResource.setCsarUUID(oldResource.getCsarUUID());
			}
			if (oldResource.getImportedToscaChecksum() != null) {
				newResource.setImportedToscaChecksum(oldResource.getImportedToscaChecksum());
			}
			newResource.setAbstract(oldResource.isAbstract());

			if (newResource.getDerivedFrom() == null || newResource.getDerivedFrom().isEmpty()) {
				newResource.setDerivedFrom(oldResource.getDerivedFrom());
			}
			// TODO rhalili: handle artifacts here (delete from old resource and
			// add for new)
			// TODO rbetzer: remove after migration - in case of resources
			// created without tosca artifacts - add the placeholders
			if (newResource.getToscaArtifacts() == null || newResource.getToscaArtifacts().isEmpty()) {
				setToscaArtifactsPlaceHolders(newResource, user);
			}
			Either<Resource, StorageOperationStatus> overrideResource = toscaOperationFacade.overrideComponent(newResource, oldResource);

			if (overrideResource.isRight()) {
				ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(overrideResource.right().value()), newResource);
				componentsUtils.auditResource(responseFormat, user, newResource, newResource.getLifecycleState().name(), newResource.getVersion(), AuditingActionEnum.IMPORT_RESOURCE, null);
				result = Either.right(responseFormat);
				return result;
			}

			log.debug("Resource updated successfully!!!");
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
			componentsUtils.auditResource(responseFormat, user, newResource, oldResource.getLifecycleState().name(), oldResource.getVersion(), AuditingActionEnum.IMPORT_RESOURCE, null);

			ImmutablePair<Resource, ActionStatus> resourcePair = new ImmutablePair<>(overrideResource.left().value(), ActionStatus.OK);
			result = Either.left(resourcePair);
			return result;
		} finally {
			if (result == null || result.isRight()) {
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeSystemError, "Change LifecycleState - Certify");
				BeEcompErrorManager.getInstance().logBeSystemError("Change LifecycleState - Certify");
				log.debug("operation failed. do rollback");
				titanDao.rollback();
			} else if (inTransaction == false) {
				log.debug("operation success. do commit");
				titanDao.commit();
			}
			if (needLock == true) {
				log.debug("unlock resource {}", lockedResourceId);
				graphLockOperation.unlockComponent(lockedResourceId, NodeTypeEnum.Resource);
			}
		}

	}

	/**
	 * Merge old resource with new. Keep old category and vendor name without change
	 *
	 * @param oldResource
	 * @param newResource
	 */
	private void mergeOldResourceMetadataWithNew(Resource oldResource, Resource newResource) {

		// keep old category and vendor name without change
		// merge the rest of the resource metadata
		if (newResource.getTags() == null || newResource.getTags().isEmpty()) {
			newResource.setTags(oldResource.getTags());
		}

		if (newResource.getDescription() == null) {
			newResource.setDescription(oldResource.getDescription());
		}

		if (newResource.getVendorRelease() == null) {
			newResource.setVendorRelease(oldResource.getVendorRelease());
		}

		if (newResource.getContactId() == null) {
			newResource.setContactId(oldResource.getContactId());
		}

		newResource.setCategories(oldResource.getCategories());
		newResource.setVendorName(oldResource.getVendorName());
	}

	private Either<Resource, ResponseFormat> prepareResourceForUpdate(Resource latestResource, User user, boolean inTransaction, boolean needLock) {

		Either<Resource, ResponseFormat> result = Either.left(latestResource);
		// check if user can edit resource
		if (!ComponentValidationUtils.canWorkOnResource(latestResource, user.getUserId())) {
			// checkout
			Either<Resource, ResponseFormat> changeState = lifecycleBusinessLogic.changeState(latestResource.getUniqueId(), user, LifeCycleTransitionEnum.CHECKOUT, new LifecycleChangeInfoWithAction("update by import"), inTransaction, needLock);
			result = changeState;
		}

		return result;
	}

	public Either<Resource, ResponseFormat> validateResourceBeforeCreate(Resource resource, User user, AuditingActionEnum actionEnum, boolean inTransaction) {

		Either<Boolean, ResponseFormat> eitherValidation = validateResourceFieldsBeforeCreate(user, resource, actionEnum, inTransaction);
		if (eitherValidation.isRight()) {
			return Either.right(eitherValidation.right().value());
		}

		eitherValidation = validateCapabilityTypesCreate(user, getCapabilityTypeOperation(), resource, actionEnum, inTransaction);
		if (eitherValidation.isRight()) {
			return Either.right(eitherValidation.right().value());
		}
		eitherValidation = validateLifecycleTypesCreate(user, resource, actionEnum);
		if (eitherValidation.isRight()) {
			return Either.right(eitherValidation.right().value());
		}
		eitherValidation = validateResourceType(user, resource, actionEnum);
		if (eitherValidation.isRight()) {
			return Either.right(eitherValidation.right().value());
		}

		resource.setCreatorUserId(user.getUserId());
		resource.setCreatorFullName(user.getFirstName() + " " + user.getLastName());
		resource.setContactId(resource.getContactId().toLowerCase());
		if (resource.getResourceType().equals(ResourceTypeEnum.VF)) {
			resource.setToscaResourceName(CommonBeUtils.generateToscaResourceName(ResourceTypeEnum.VF.name(), resource.getSystemName()));
		}

		// Generate invariant UUID - must be here and not in operation since it
		// should stay constant during clone
		String invariantUUID = UniqueIdBuilder.buildInvariantUUID();
		resource.setInvariantUUID(invariantUUID);

		return Either.left(resource);
	}

	private Either<Boolean, ResponseFormat> validateResourceType(User user, Resource resource, AuditingActionEnum actionEnum) {
		Either<Boolean, ResponseFormat> eitherResult = Either.left(true);
		if (resource.getResourceType() == null) {
			log.debug("Invalid resource type for resource");
			ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT);
			eitherResult = Either.right(errorResponse);
			componentsUtils.auditResource(errorResponse, user, resource, "", "", actionEnum, null);
		}
		return eitherResult;
	}

	private Either<Boolean, ResponseFormat> validateLifecycleTypesCreate(User user, Resource resource, AuditingActionEnum actionEnum) {
		Either<Boolean, ResponseFormat> eitherResult = Either.left(true);
		if (resource.getInterfaces() != null && resource.getInterfaces().size() > 0) {
			log.debug("validate interface lifecycle Types Exist");
			Iterator<InterfaceDefinition> intItr = resource.getInterfaces().values().iterator();
			while (intItr.hasNext() && eitherResult.isLeft()) {
				InterfaceDefinition interfaceDefinition = intItr.next();
				String intType = interfaceDefinition.getUniqueId();
				Either<InterfaceDefinition, StorageOperationStatus> eitherCapTypeFound = interfaceTypeOperation.getInterface(intType);
				if (eitherCapTypeFound.isRight()) {
					if (eitherCapTypeFound.right().value() == StorageOperationStatus.NOT_FOUND) {
						BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeInterfaceMissingError, "Create Resource - validateLifecycleTypesCreate", intType);
						BeEcompErrorManager.getInstance().logBeGraphObjectMissingError("Create Resource - validateLifecycleTypesCreate", "Interface", intType);
						log.debug("Lifecycle Type: {} is required by resource: {} but does not exist in the DB", intType, resource.getName());
						BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError, "Create Resource - validateLifecycleTypesCreate");
						BeEcompErrorManager.getInstance().logBeDaoSystemError("Create Resource - validateLifecycleTypesCreate");
						log.debug("request to data model failed with error: {}", eitherCapTypeFound.right().value().name());
					}

					ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.MISSING_LIFECYCLE_TYPE, intType);
					eitherResult = Either.right(errorResponse);
					componentsUtils.auditResource(errorResponse, user, resource, "", "", actionEnum, null);
				}

			}
		}
		return eitherResult;
	}

	private Either<Boolean, ResponseFormat> validateCapabilityTypesCreate(User user, ICapabilityTypeOperation capabilityTypeOperation, Resource resource, AuditingActionEnum actionEnum, boolean inTransaction) {

		Either<Boolean, ResponseFormat> eitherResult = Either.left(true);
		if (resource.getCapabilities() != null && resource.getCapabilities().size() > 0) {
			log.debug("validate capability Types Exist - capabilities section");

			for (Entry<String, List<CapabilityDefinition>> typeEntry : resource.getCapabilities().entrySet()) {

				eitherResult = validateCapabilityTypeExists(user, capabilityTypeOperation, resource, actionEnum, eitherResult, typeEntry, inTransaction);
				if (eitherResult.isRight()) {
					return Either.right(eitherResult.right().value());
				}
			}
		}

		if (resource.getRequirements() != null && resource.getRequirements().size() > 0) {
			log.debug("validate capability Types Exist - requirements section");
			for (String type : resource.getRequirements().keySet()) {
				eitherResult = validateCapabilityTypeExists(user, capabilityTypeOperation, resource, resource.getRequirements().get(type) , actionEnum, eitherResult, type, inTransaction);
				if (eitherResult.isRight()) {
					return Either.right(eitherResult.right().value());
				}
			}
		}

		return eitherResult;
	}

	//@param typeObject- the object to which the validation is done
	private Either<Boolean, ResponseFormat> validateCapabilityTypeExists(User user, ICapabilityTypeOperation capabilityTypeOperation, Resource resource, List validationObjects , AuditingActionEnum actionEnum, Either<Boolean, ResponseFormat> eitherResult, String type,
																			 boolean inTransaction) {
		Either<CapabilityTypeDefinition, StorageOperationStatus> eitherCapTypeFound = capabilityTypeOperation.getCapabilityType(type, inTransaction);
		if (eitherCapTypeFound.isRight()) {
			if (eitherCapTypeFound.right().value() == StorageOperationStatus.NOT_FOUND) {
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeCapabilityTypeMissingError, "Create Resource - validateCapabilityTypesCreate", type);
				BeEcompErrorManager.getInstance().logBeGraphObjectMissingError("Create Resource - validateCapabilityTypesCreate", "Capability Type", type);
				log.debug("Capability Type: {} is required by resource: {} but does not exist in the DB", type, resource.getName());
				BeEcompErrorManager.getInstance().logBeDaoSystemError("Create Resource - validateCapabilityTypesCreate");
			}
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError, "Create Resource - validateCapabilityTypesCreate");
			log.debug("Trying to get capability type {} failed with error: {}", type, eitherCapTypeFound.right().value().name());
			ResponseFormat errorResponse =null;
			if (type!=null)
				errorResponse = componentsUtils.getResponseFormat(ActionStatus.MISSING_CAPABILITY_TYPE, type);
			else
				errorResponse = componentsUtils.getResponseFormatByElement(ActionStatus.MISSING_CAPABILITY_TYPE, validationObjects );
			eitherResult = Either.right(errorResponse);
			componentsUtils.auditResource(errorResponse, user, resource, "", "", actionEnum, null);
		}
		return eitherResult;
	}

	private Either<Boolean, ResponseFormat> validateCapabilityTypeExists(User user, ICapabilityTypeOperation capabilityTypeOperation, Resource resource, AuditingActionEnum actionEnum, Either<Boolean, ResponseFormat> eitherResult,
																		 Entry<String, List<CapabilityDefinition>> typeEntry, boolean inTransaction) {
		Either<CapabilityTypeDefinition, StorageOperationStatus> eitherCapTypeFound = capabilityTypeOperation.getCapabilityType(typeEntry.getKey(), inTransaction);
		if (eitherCapTypeFound.isRight()) {
			if (eitherCapTypeFound.right().value() == StorageOperationStatus.NOT_FOUND) {
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeCapabilityTypeMissingError, "Create Resource - validateCapabilityTypesCreate", typeEntry.getKey());
				BeEcompErrorManager.getInstance().logBeGraphObjectMissingError("Create Resource - validateCapabilityTypesCreate", "Capability Type", typeEntry.getKey());
				log.debug("Capability Type: {} is required by resource: {} but does not exist in the DB", typeEntry.getKey(), resource.getName());
				BeEcompErrorManager.getInstance().logBeDaoSystemError("Create Resource - validateCapabilityTypesCreate");
			}
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError, "Create Resource - validateCapabilityTypesCreate");
			log.debug("Trying to get capability type {} failed with error: {}", typeEntry.getKey(), eitherCapTypeFound.right().value().name());
			ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.MISSING_CAPABILITY_TYPE, typeEntry.getKey());
			eitherResult = Either.right(errorResponse);
			componentsUtils.auditResource(errorResponse, user, resource, "", "", actionEnum, null);
		}
		CapabilityTypeDefinition capabilityTypeDefinition = eitherCapTypeFound.left().value();
		if (capabilityTypeDefinition.getProperties() != null) {
			for (CapabilityDefinition capDef : typeEntry.getValue()) {
				List<ComponentInstanceProperty> properties = capDef.getProperties();
				if (properties == null || properties.isEmpty()) {
					properties = new ArrayList<ComponentInstanceProperty>();
					for (Entry<String, PropertyDefinition> prop : capabilityTypeDefinition.getProperties().entrySet()) {
						ComponentInstanceProperty newProp = new ComponentInstanceProperty(prop.getValue());
						properties.add(newProp);
					}
				} else {
					for (Entry<String, PropertyDefinition> prop : capabilityTypeDefinition.getProperties().entrySet()) {
						PropertyDefinition porpFromDef = prop.getValue();
						List<ComponentInstanceProperty> propsToAdd = new ArrayList<>();
						for (ComponentInstanceProperty cip : properties) {
							if (!cip.getName().equals(porpFromDef.getName())) {
								ComponentInstanceProperty newProp = new ComponentInstanceProperty(porpFromDef);
								propsToAdd.add(newProp);
							}
						}
						if (!propsToAdd.isEmpty()) {
							properties.addAll(propsToAdd);
						}
					}
				}
				capDef.setProperties(properties);
			}
		}
		return eitherResult;
	}

	public Either<Resource, ResponseFormat> createResourceByDao(Resource resource, User user, AuditingActionEnum actionEnum, boolean isNormative, boolean inTransaction, EnumMap<AuditingFieldsKeysEnum, Object> additionalParams) {
		// create resource

		// lock new resource name in order to avoid creation resource with same
		// name
		if (inTransaction == false) {
			Either<Boolean, ResponseFormat> lockResult = lockComponentByName(resource.getSystemName(), resource, "Create Resource");
			if (lockResult.isRight()) {
				ResponseFormat responseFormat = lockResult.right().value();
				componentsUtils.auditResource(responseFormat, user, resource, "", "", actionEnum, additionalParams);
				return Either.right(responseFormat);
			}

			log.debug("name is locked {} status = {}", resource.getSystemName(), lockResult);
		}
		try {
			if (resource.deriveFromGeneric()) {
				Either<Resource, ResponseFormat> genericResourceEither = fetchAndSetDerivedFromGenericType(resource);
				if (genericResourceEither.isRight())
					return genericResourceEither;
				if (resource.shouldGenerateInputs())
					generateInputsFromGenericTypeProperties(resource, genericResourceEither.left().value());
			}

			Either<Resource, ResponseFormat> respStatus = createResourceTransaction(resource, user, isNormative, inTransaction);
			if (respStatus.isLeft()) {
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.CREATED);
				componentsUtils.auditResource(responseFormat, user, resource, "", "", actionEnum, additionalParams);
				ASDCKpiApi.countCreatedResourcesKPI();
			} else
				componentsUtils.auditResource(respStatus.right().value(), user, resource, "", "", actionEnum, additionalParams);
			return respStatus;

		} finally {
			if (inTransaction == false) {
				graphLockOperation.unlockComponentByName(resource.getSystemName(), resource.getUniqueId(), NodeTypeEnum.Resource);
			}
		}
	}

	private Either<Resource, ResponseFormat> createResourceTransaction(Resource resource, User user, boolean isNormative, boolean inTransaction) {
		// validate resource name uniqueness
		log.debug("validate resource name");
		Either<Boolean, StorageOperationStatus> eitherValidation = toscaOperationFacade.validateComponentNameExists(resource.getName(), resource.getResourceType(), resource.getComponentType());
		if (eitherValidation.isRight()) {
			log.debug("Failed to validate component name {}. Status is {}. ", resource.getName(), eitherValidation.right().value());
			ResponseFormat errorResponse = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(eitherValidation.right().value()));
			return Either.right(errorResponse);
		}
		if (eitherValidation.left().value()) {
			log.debug("resource with name: {}, already exists", resource.getName());
			ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_NAME_ALREADY_EXIST, ComponentTypeEnum.RESOURCE.getValue(), resource.getName());
			return Either.right(errorResponse);
		}

		log.debug("send resource {} to dao for create", resource.getName());

		createArtifactsPlaceHolderData(resource, user);

		//

		// enrich object
		if (!isNormative) {
			log.debug("enrich resource with creator, version and state");
			resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
			resource.setVersion(INITIAL_VERSION);
			resource.setHighestVersion(true);
			resource.setAbstract(false);
		}

		Either<Resource, StorageOperationStatus> createToscaElement = toscaOperationFacade.createToscaComponent(resource);
		if (createToscaElement.isLeft()) {
			return Either.left(createToscaElement.left().value());
		}

		ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(createToscaElement.right().value()), resource);

		return Either.right(responseFormat);
	}

	private void createArtifactsPlaceHolderData(Resource resource, User user) {
		// create mandatory artifacts

		// TODO it must be removed after that artifact uniqueId creation will be
		// moved to ArtifactOperation
		// String resourceUniqueId =
		// UniqueIdBuilder.buildResourceUniqueId(resource.getResourceName(),
		// resource.getResourceVersion());

		setInformationalArtifactsPlaceHolder(resource, user);
		setDeploymentArtifactsPlaceHolder(resource, user);
		setToscaArtifactsPlaceHolders(resource, user);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setDeploymentArtifactsPlaceHolder(Component component, User user) {
		Resource resource = (Resource) component;
		Map<String, ArtifactDefinition> artifactMap = resource.getDeploymentArtifacts();
		if (artifactMap == null) {
			artifactMap = new HashMap<String, ArtifactDefinition>();
		}
		Map<String, Object> deploymentResourceArtifacts = ConfigurationManager.getConfigurationManager().getConfiguration().getDeploymentResourceArtifacts();
		if (deploymentResourceArtifacts != null) {
			Iterator<Entry<String, Object>> iterator = deploymentResourceArtifacts.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, Object> currEntry = iterator.next();
				boolean shouldCreateArtifact = true;
				Map<String, Object> artifactDetails = (Map<String, Object>) currEntry.getValue();
				Object object = artifactDetails.get(PLACE_HOLDER_RESOURCE_TYPES);
				if (object != null) {
					List<String> artifactTypes = (List<String>) object;
					if (!artifactTypes.contains(resource.getResourceType().name())) {
						shouldCreateArtifact = false;
						continue;
					}
				} else {
					log.info("resource types for artifact placeholder {} were not defined. default is all resources", currEntry.getKey());
				}
				if (shouldCreateArtifact) {
					if (artifactsBusinessLogic != null) {
						ArtifactDefinition artifactDefinition = artifactsBusinessLogic.createArtifactPlaceHolderInfo(resource.getUniqueId(), currEntry.getKey(), (Map<String, Object>) currEntry.getValue(), user, ArtifactGroupTypeEnum.DEPLOYMENT);
						if (artifactDefinition != null && !artifactMap.containsKey(artifactDefinition.getArtifactLabel()))
							artifactMap.put(artifactDefinition.getArtifactLabel(), artifactDefinition);
					}
				}
			}
		}
		resource.setDeploymentArtifacts(artifactMap);
	}

	private void setInformationalArtifactsPlaceHolder(Resource resource, User user) {
		Map<String, ArtifactDefinition> artifactMap = resource.getArtifacts();
		if (artifactMap == null) {
			artifactMap = new HashMap<String, ArtifactDefinition>();
		}
		String resourceUniqueId = resource.getUniqueId();
		List<String> exludeResourceCategory = ConfigurationManager.getConfigurationManager().getConfiguration().getExcludeResourceCategory();
		Map<String, Object> informationalResourceArtifacts = ConfigurationManager.getConfigurationManager().getConfiguration().getInformationalResourceArtifacts();
		List<CategoryDefinition> categories = resource.getCategories();
		boolean isCreateArtifact = true;
		if (exludeResourceCategory != null) {
			String category = categories.get(0).getName();
			for (String exlude : exludeResourceCategory) {
				if (exlude.equalsIgnoreCase(category)) {
					isCreateArtifact = false;
					break;
				}
			}

		}

		if (informationalResourceArtifacts != null && isCreateArtifact) {
			Set<String> keys = informationalResourceArtifacts.keySet();
			for (String informationalResourceArtifactName : keys) {
				Map<String, Object> artifactInfoMap = (Map<String, Object>) informationalResourceArtifacts.get(informationalResourceArtifactName);
				ArtifactDefinition artifactDefinition = artifactsBusinessLogic.createArtifactPlaceHolderInfo(resourceUniqueId, informationalResourceArtifactName, artifactInfoMap, user, ArtifactGroupTypeEnum.INFORMATIONAL);
				artifactMap.put(artifactDefinition.getArtifactLabel(), artifactDefinition);

			}
		}
		resource.setArtifacts(artifactMap);
	}

	/**
	 * deleteResource
	 *
	 * @param resourceId
	 * @param user
	 * @return
	 */
	public ResponseFormat deleteResource(String resourceId, User user) {
		ResponseFormat responseFormat;
		Either<User, ResponseFormat> eitherCreator = validateUserExists(user, "Delete Resource", false);
		if (eitherCreator.isRight()) {
			return eitherCreator.right().value();
		}

		Either<Resource, StorageOperationStatus> resourceStatus = toscaOperationFacade.getToscaElement(resourceId);
		if (resourceStatus.isRight()) {
			log.debug("failed to get resource {}", resourceId);
			return componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(resourceStatus.right().value()), "");
		}

		Resource resource = resourceStatus.left().value();

		StorageOperationStatus result = StorageOperationStatus.OK;
		Either<Boolean, ResponseFormat> lockResult = lockComponent(resourceId, resource, "Mark resource to delete");
		if (lockResult.isRight()) {
			result = StorageOperationStatus.GENERAL_ERROR;
			return componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
		}

		try {

			result = markComponentToDelete(resource);
			if (result.equals(StorageOperationStatus.OK)) {
				responseFormat = componentsUtils.getResponseFormat(ActionStatus.NO_CONTENT);
			} else {
				ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(result);
				responseFormat = componentsUtils.getResponseFormatByResource(actionStatus, resource.getName());
			}
			return responseFormat;

		} finally {
			if (result == null || !result.equals(StorageOperationStatus.OK)) {
				log.warn("operation failed. do rollback");
				titanDao.rollback();
			} else {
				log.debug("operation success. do commit");
				titanDao.commit();
			}
			graphLockOperation.unlockComponent(resourceId, NodeTypeEnum.Resource);
		}

	}

	public ResponseFormat deleteResourceByNameAndVersion(String resourceName, String version, User user) {
		ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.NO_CONTENT);
		Either<User, ResponseFormat> eitherCreator = validateUserExists(user, "Delete Resource", false);
		if (eitherCreator.isRight()) {
			return eitherCreator.right().value();
		}

		// Resource resource = null;
		Resource resource = null;
		StorageOperationStatus result = StorageOperationStatus.OK;
		try {

			Either<Resource, StorageOperationStatus> resourceStatus = toscaOperationFacade.getComponentByNameAndVersion(ComponentTypeEnum.RESOURCE, resourceName, version);
			if (resourceStatus.isRight()) {
				log.debug("failed to get resource {} version {}", resourceName, version);
				return componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(resourceStatus.right().value()), resourceName);
			}

			resource = resourceStatus.left().value();

		} finally {
			if (result == null || !result.equals(StorageOperationStatus.OK)) {
				log.warn("operation failed. do rollback");
				titanDao.rollback();
				ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(result);
				responseFormat = componentsUtils.getResponseFormatByResource(actionStatus, resourceName);
			} else {
				log.debug("operation success. do commit");
				titanDao.commit();
			}
		}
		if (resource != null) {
			Either<Boolean, ResponseFormat> lockResult = lockComponent(resource.getUniqueId(), resource, "Delete Resource");
			if (lockResult.isRight()) {
				result = StorageOperationStatus.GENERAL_ERROR;
				return componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
			}
			try {
				result = markComponentToDelete(resource);
				if (!result.equals(StorageOperationStatus.OK)) {
					ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(result);
					responseFormat = componentsUtils.getResponseFormatByResource(actionStatus, resource.getName());
					return responseFormat;
				}

			} finally {
				if (result == null || !result.equals(StorageOperationStatus.OK)) {
					log.warn("operation failed. do rollback");
					titanDao.rollback();
				} else {
					log.debug("operation success. do commit");
					titanDao.commit();
				}
				graphLockOperation.unlockComponent(resource.getUniqueId(), NodeTypeEnum.Resource);
			}
		}
		return responseFormat;
	}

	public Either<Resource, ResponseFormat> getResource(String resourceId, User user) {

		if (user != null) {
			Either<User, ResponseFormat> eitherCreator = validateUserExists(user, "Create Resource", false);
			if (eitherCreator.isRight()) {
				return Either.right(eitherCreator.right().value());
			}
		}

		// IResourceOperation dataModel = getResourceOperation();
		Either<Resource, StorageOperationStatus> storageStatus = toscaOperationFacade.getToscaElement(resourceId);

		if (storageStatus.isRight()) {
			log.debug("failed to get resource by id {}", resourceId);
			return Either.right(componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(storageStatus.right().value()), ""));
		}
		return Either.left(storageStatus.left().value());

	}

	public Either<Resource, ResponseFormat> getResourceByNameAndVersion(String resourceName, String resourceVersion, String userId) {

		Either<User, ResponseFormat> resp = validateUserExists(userId, "get Resource By Name And Version", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

		Either<Resource, StorageOperationStatus> getResource = toscaOperationFacade.getComponentByNameAndVersion(ComponentTypeEnum.RESOURCE, resourceName, resourceVersion);
		if (getResource.isRight()) {
			log.debug("failed to get resource by name {} and version {}", resourceName, resourceVersion);
			return Either.right(componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(getResource.right().value()), resourceName));
		}
		return Either.left(getResource.left().value());
	}

	/**
	 * updateResourceMetadata
	 *
	 * @param user
	 *            - modifier data (userId)
	 * @param inTransaction
	 *            TODO
	 * @param resourceIdToUpdate
	 *            - the resource identifier
	 * @param newResource
	 *
	 * @return Either<Resource, responseFormat>
	 */
	public Either<Resource, ResponseFormat> updateResourceMetadata(String resourceIdToUpdate, Resource newResource, Resource currentResource, User user, boolean inTransaction) {

		Either<User, ResponseFormat> resp = validateUserExists(user.getUserId(), "update Resource Metadata", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

		// IResourceOperation dataModel = getResourceOperation();
		log.debug("Get resource with id {}", resourceIdToUpdate);
		boolean needToUnlock = false;
		boolean rollbackNeeded = true;

		try {
			// Either<Resource, StorageOperationStatus> storageStatus =
			// dataModel.getResource_tx(resourceIdToUpdate, false);
			if (currentResource == null) {
				Either<Resource, StorageOperationStatus> storageStatus = toscaOperationFacade.getToscaElement(resourceIdToUpdate);
				if (storageStatus.isRight()) {
					return Either.right(componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(storageStatus.right().value()), ""));
				}

				currentResource = storageStatus.left().value();
			}
			// verify that resource is checked-out and the user is the last
			// updater
			if (!ComponentValidationUtils.canWorkOnResource(currentResource, user.getUserId())) {
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
			}

			// lock resource
			StorageOperationStatus lockResult = graphLockOperation.lockComponent(resourceIdToUpdate, NodeTypeEnum.Resource);
			if (!lockResult.equals(StorageOperationStatus.OK)) {
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeFailedLockObjectError, "Upload Artifact - lock " + resourceIdToUpdate + ": " + NodeTypeEnum.Resource);
				BeEcompErrorManager.getInstance().logBeFailedLockObjectError("Upload Artifact - lock ", NodeTypeEnum.Resource.getName(), resourceIdToUpdate);
				log.debug("Failed to lock resource: {}, error - {}", resourceIdToUpdate, lockResult);
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(lockResult));
				return Either.right(responseFormat);
			}

			needToUnlock = true;

			// critical section starts here
			// convert json to object

			// Update and updated resource must have a non-empty "derivedFrom"
			// list
			// This code is not called from import resources, because of root
			// VF "derivedFrom" should be null (or ignored)
			if (!currentResource.getResourceType().equals(ResourceTypeEnum.VF)) {
				Either<Boolean, ResponseFormat> derivedFromNotEmptyEither = validateDerivedFromNotEmpty(null, newResource, null);
				if (derivedFromNotEmptyEither.isRight()) {
					log.debug("for updated resource {}, derived from field is empty", newResource.getName());
					return Either.right(derivedFromNotEmptyEither.right().value());
				}

				derivedFromNotEmptyEither = validateDerivedFromNotEmpty(null, currentResource, null);
				if (derivedFromNotEmptyEither.isRight()) {
					log.debug("for current resource {}, derived from field is empty", currentResource.getName());
					return Either.right(derivedFromNotEmptyEither.right().value());
				}
			} else {
				newResource.setDerivedFrom(null);
			}

			Either<Resource, ResponseFormat> dataModelResponse = updateResourceMetadata(resourceIdToUpdate, newResource, user, currentResource, false, true);
			if (dataModelResponse.isRight()) {
				log.debug("failed to update resource metadata!!!");
				rollbackNeeded = true;
				return Either.right(dataModelResponse.right().value());
			}

			log.debug("Resource metadata updated successfully!!!");
			rollbackNeeded = false;
			return Either.left(dataModelResponse.left().value());

		} finally {
			if (!inTransaction) {
				if (rollbackNeeded) {
					titanDao.rollback();
				} else {
					titanDao.commit();
				}
			}

			if (needToUnlock) {
				graphLockOperation.unlockComponent(resourceIdToUpdate, NodeTypeEnum.Resource);
			}
		}
	}

	private Either<Resource, ResponseFormat> updateResourceMetadata(String resourceIdToUpdate, Resource newResource, User user, Resource currentResource, boolean shouldLock, boolean inTransaction) {

		Either<Boolean, ResponseFormat> validateResourceFields = validateResourceFieldsBeforeUpdate(currentResource, newResource, inTransaction);
		if (validateResourceFields.isRight()) {
			return Either.right(validateResourceFields.right().value());
		}
		// Setting last updater and uniqueId
		newResource.setContactId(newResource.getContactId().toLowerCase());
		newResource.setLastUpdaterUserId(user.getUserId());
		newResource.setUniqueId(resourceIdToUpdate);
		// Cannot set highest version through UI
		newResource.setHighestVersion(currentResource.isHighestVersion());
		newResource.setCreationDate(currentResource.getCreationDate());

		Either<Boolean, ResponseFormat> processUpdateOfDerivedFrom = processUpdateOfDerivedFrom(currentResource, newResource, user.getUserId(), shouldLock, inTransaction);

		if (processUpdateOfDerivedFrom.isRight()) {
			log.debug("Couldn't update derived from for resource {}", resourceIdToUpdate);
			return Either.right(processUpdateOfDerivedFrom.right().value());
		}

		log.debug("send resource {} to dao for update", newResource.getUniqueId());
		Either<Resource, StorageOperationStatus> dataModelResponse = toscaOperationFacade.updateToscaElement(newResource);

		if (dataModelResponse.isRight()) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(dataModelResponse.right().value()), newResource);
			return Either.right(responseFormat);
		} else if (dataModelResponse.left().value() == null) {
			log.debug("No response from updateResource");
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
		}
		return Either.left(dataModelResponse.left().value());
	}

	/**
	 * validateResourceFieldsBeforeCreate
	 *
	 * @param user
	 *            - modifier data (userId)
	 * @param dataModel
	 *            - IResourceOperation for resource crud
	 * @param resource
	 *            - Resource object to validate
	 * @return Either<Boolean, ErrorResponse>
	 */
	private Either<Boolean, ResponseFormat> validateResourceFieldsBeforeCreate(User user, Resource resource, AuditingActionEnum actionEnum, boolean inTransaction) {
		Either<Boolean, ResponseFormat> componentsFieldsValidation = validateComponentFieldsBeforeCreate(user, resource, actionEnum);
		if (componentsFieldsValidation.isRight()) {
			return componentsFieldsValidation;
		}

		// validate name

		/*
		 * log.debug("validate resource name"); Either<Boolean, ResponseFormat> eitherValidation = validateComponentName(user, resource, actionEnum); if (eitherValidation.isRight()) { return eitherValidation; }
		 *
		 * // validate description log.debug("validate description"); eitherValidation = validateDescriptionAndCleanup(user, resource, actionEnum); if (eitherValidation.isRight()) { return eitherValidation; }
		 */

		// validate icon
		/*
		 * log.debug("validate icon"); eitherValidation = validateIcon(user, resource, actionEnum); if (eitherValidation.isRight()) { return eitherValidation; }
		 */

		// validate tags
		/*
		 * log.debug("validate tags"); eitherValidation = validateTagsListAndRemoveDuplicates(user, resource, actionEnum); if (eitherValidation.isRight()) { return eitherValidation; }
		 */

		// validate category
		log.debug("validate category");
		Either<Boolean, ResponseFormat> eitherValidation = validateCategory(user, resource, actionEnum, inTransaction);
		if (eitherValidation.isRight()) {
			return eitherValidation;
		}

		// validate vendor name & release
		log.debug("validate vendor name");
		eitherValidation = validateVendorName(user, resource, actionEnum);
		if (eitherValidation.isRight()) {
			return eitherValidation;
		}

		log.debug("validate vendor release");
		eitherValidation = validateVendorReleaseName(user, resource, actionEnum);
		if (eitherValidation.isRight()) {
			return eitherValidation;
		}

		// validate contact info
		/*
		 * log.debug("validate contact info"); eitherValidation = validateContactIdContactId(user, resource, actionEnum); if (eitherValidation.isRight()) { return eitherValidation; }
		 */

		// validate cost
		log.debug("validate cost");
		eitherValidation = validateCost(user, resource, actionEnum);
		if (eitherValidation.isRight()) {
			return eitherValidation;
		}

		// validate licenseType
		log.debug("validate licenseType");
		eitherValidation = validateLicenseType(user, resource, actionEnum);
		if (eitherValidation.isRight()) {
			return eitherValidation;
		}

		// validate template (derived from)
		log.debug("validate derived from");
		if (resource.getResourceType().equals(ResourceTypeEnum.VF)) {
			resource.setDerivedFrom(null);
		}
		eitherValidation = validateDerivedFromExist(user, resource, actionEnum);
		if (eitherValidation.isRight()) {
			return Either.right(eitherValidation.right().value());
		}

		// warn about non-updatable fields
		checkComponentFieldsForOverrideAttempt(resource);
		String currentCreatorFullName = resource.getCreatorFullName();
		if (currentCreatorFullName != null) {
			log.warn("Resource Creator fullname is automatically set and cannot be updated");
		}

		String currentLastUpdaterFullName = resource.getLastUpdaterFullName();
		if (currentLastUpdaterFullName != null) {
			log.warn("Resource LastUpdater fullname is automatically set and cannot be updated");
		}

		Long currentLastUpdateDate = resource.getLastUpdateDate();
		if (currentLastUpdateDate != null) {
			log.warn("Resource last update date is automatically set and cannot be updated");
		}

		Boolean currentAbstract = resource.isAbstract();
		if (currentAbstract != null) {
			log.warn("Resource abstract is automatically set and cannot be updated");
		}

		return Either.left(true);
	}

	/**
	 * validateResourceFieldsBeforeUpdate
	 *
	 * @param currentResource
	 *            - Resource object to validate
	 * @return Either<Boolean, ErrorResponse>
	 */
	private Either<Boolean, ResponseFormat> validateResourceFieldsBeforeUpdate(Resource currentResource, Resource updateInfoResource, boolean inTransaction) {

		boolean hasBeenCertified = ValidationUtils.hasBeenCertified(currentResource.getVersion());

		// validate resource name
		log.debug("validate resource name before update");
		Either<Boolean, ResponseFormat> eitherValidation = validateResourceName(currentResource, updateInfoResource, hasBeenCertified);
		if (eitherValidation.isRight()) {
			return eitherValidation;
		}

		// validate description
		log.debug("validate description before update");
		eitherValidation = validateDescriptionAndCleanup(null, updateInfoResource, null);
		if (eitherValidation.isRight()) {
			return eitherValidation;
		}

		log.debug("validate icon before update");
		eitherValidation = validateIcon(currentResource, updateInfoResource, hasBeenCertified);
		if (eitherValidation.isRight()) {
			return eitherValidation;
		}

		log.debug("validate tags before update");
		eitherValidation = validateTagsListAndRemoveDuplicates(null, updateInfoResource, null);
		if (eitherValidation.isRight()) {
			return eitherValidation;
		}

		log.debug("validate vendor name before update");
		eitherValidation = validateVendorName(currentResource, updateInfoResource, hasBeenCertified);
		if (eitherValidation.isRight()) {
			return eitherValidation;
		}

		log.debug("validate vendor release before update");
		eitherValidation = validateVendorReleaseName(null, updateInfoResource, null);
		if (eitherValidation.isRight()) {
			return eitherValidation;
		}

		log.debug("validate contact info before update");
		eitherValidation = validateContactId(null, updateInfoResource, null);
		if (eitherValidation.isRight()) {
			return eitherValidation;
		}

		log.debug("validate derived before update");
		eitherValidation = validateDerivedFromDuringUpdate(currentResource, updateInfoResource, hasBeenCertified);
		if (eitherValidation.isRight()) {
			return eitherValidation;
		}

		log.debug("validate category before update");
		eitherValidation = validateCategory(currentResource, updateInfoResource, hasBeenCertified, inTransaction);
		if (eitherValidation.isRight()) {
			return eitherValidation;
		}

		// warn about non-updatable fields
		String currentResourceVersion = currentResource.getVersion();
		String updatedResourceVersion = updateInfoResource.getVersion();

		if ((updatedResourceVersion != null) && (!updatedResourceVersion.equals(currentResourceVersion))) {
			log.warn("Resource version is automatically set and cannot be updated");
		}

		String currentCreatorUserId = currentResource.getCreatorUserId();
		String updatedCreatorUserId = updateInfoResource.getCreatorUserId();

		if ((updatedCreatorUserId != null) && (!updatedCreatorUserId.equals(currentCreatorUserId))) {
			log.warn("Resource Creator UserId is automatically set and cannot be updated");
		}

		String currentCreatorFullName = currentResource.getCreatorFullName();
		String updatedCreatorFullName = updateInfoResource.getCreatorFullName();

		if ((updatedCreatorFullName != null) && (!updatedCreatorFullName.equals(currentCreatorFullName))) {
			log.warn("Resource Creator fullname is automatically set and cannot be updated");
		}

		String currentLastUpdaterUserId = currentResource.getLastUpdaterUserId();
		String updatedLastUpdaterUserId = updateInfoResource.getLastUpdaterUserId();

		if ((updatedLastUpdaterUserId != null) && (!updatedLastUpdaterUserId.equals(currentLastUpdaterUserId))) {
			log.warn("Resource LastUpdater userId is automatically set and cannot be updated");
		}

		String currentLastUpdaterFullName = currentResource.getLastUpdaterFullName();
		String updatedLastUpdaterFullName = updateInfoResource.getLastUpdaterFullName();

		if ((updatedLastUpdaterFullName != null) && (!updatedLastUpdaterFullName.equals(currentLastUpdaterFullName))) {
			log.warn("Resource LastUpdater fullname is automatically set and cannot be updated");
		}

		Long currentCreationDate = currentResource.getCreationDate();
		Long updatedCreationDate = updateInfoResource.getCreationDate();

		if ((updatedCreationDate != null) && (!updatedCreationDate.equals(currentCreationDate))) {
			log.warn("Resource Creation date is automatically set and cannot be updated");
		}

		Long currentLastUpdateDate = currentResource.getLastUpdateDate();
		Long updatedLastUpdateDate = updateInfoResource.getLastUpdateDate();

		if ((updatedLastUpdateDate != null) && (!updatedLastUpdateDate.equals(currentLastUpdateDate))) {
			log.warn("Resource last update date is automatically set and cannot be updated");
		}

		LifecycleStateEnum currentLifecycleState = currentResource.getLifecycleState();
		LifecycleStateEnum updatedLifecycleState = updateInfoResource.getLifecycleState();

		if ((updatedLifecycleState != null) && (!updatedLifecycleState.equals(currentLifecycleState))) {
			log.warn("Resource lifecycle state date is automatically set and cannot be updated");
		}

		Boolean currentAbstract = currentResource.isAbstract();
		Boolean updatedAbstract = updateInfoResource.isAbstract();

		if ((updatedAbstract != null) && (!updatedAbstract.equals(currentAbstract))) {
			log.warn("Resource abstract is automatically set and cannot be updated");
		}

		Boolean currentHighestVersion = currentResource.isHighestVersion();
		Boolean updatedHighestVersion = updateInfoResource.isHighestVersion();

		if ((updatedHighestVersion != null) && (!updatedHighestVersion.equals(currentHighestVersion))) {
			log.warn("Resource highest version is automatically set and cannot be updated");
		}

		String currentUuid = currentResource.getUUID();
		String updatedUuid = updateInfoResource.getUUID();

		if ((updatedUuid != null) && (!updatedUuid.equals(currentUuid))) {
			log.warn("Resource UUID is automatically set and cannot be updated");
		}

		ResourceTypeEnum currentResourceType = currentResource.getResourceType();
		ResourceTypeEnum updatedResourceType = updateInfoResource.getResourceType();

		if ((updatedResourceType != null) && (!updatedResourceType.equals(currentResourceType))) {
			log.warn("Resource Type  cannot be updated");

		}
		updateInfoResource.setResourceType(currentResource.getResourceType());

		String currentInvariantUuid = currentResource.getInvariantUUID();
		String updatedInvariantUuid = updateInfoResource.getInvariantUUID();

		if ((updatedInvariantUuid != null) && (!updatedInvariantUuid.equals(currentInvariantUuid))) {
			log.warn("Resource invariant UUID is automatically set and cannot be updated");
			updateInfoResource.setInvariantUUID(currentInvariantUuid);
		}
		return Either.left(true);
	}

	/*
	 * private Either<Boolean, ResponseFormat> validateResourceName(User user, Resource resource, AuditingActionEnum actionEnum) { log.debug("validate resource name is not empty"); String resourceName = resource.getResourceName();
	 *
	 * if (!ValidationUtils.validateStringNotEmpty(resourceName)) { log.debug("Resource name is empty"); ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_COMPONENT_NAME, ComponentTypeEnum.RESOURCE.getValue());
	 * componentsUtils.auditResource(responseFormat, user, resource, "", "", actionEnum, null); return Either.right(responseFormat); }
	 *
	 * if (!ValidationUtils.validateResourceNameLength(resourceName)) { log.debug("Resource name is exceeds max length {} ", ValidationUtils.RESOURCE_NAME_MAX_LENGTH); ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.
	 * COMPONENT_NAME_EXCEEDS_LIMIT, ComponentTypeEnum.RESOURCE.getValue(), "" + ValidationUtils.RESOURCE_NAME_MAX_LENGTH); componentsUtils.auditResource(responseFormat, user, resource, "", "", actionEnum, null); return Either.right(responseFormat);
	 * }
	 *
	 * if (!ValidationUtils.validateResourceName(resourceName)) { log.debug("Resource name {} has invalid format", resourceName); ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_COMPONENT_NAME,
	 * ComponentTypeEnum.RESOURCE.getValue()); componentsUtils.auditResource(responseFormat, user, resource, "", "", actionEnum, null); return Either.right(responseFormat); } resource.setNormalizedName(ValidationUtils.normaliseComponentName(
	 * resourceName)); resource.setSystemName(ValidationUtils.convertToSystemName(resourceName)) ;
	 *
	 * return Either.left(true); }
	 */

	private Either<Boolean, ResponseFormat> validateResourceName(Resource currentResource, Resource updateInfoResource, boolean hasBeenCertified) {
		String resourceNameUpdated = updateInfoResource.getName();
		String resourceNameCurrent = currentResource.getName();
		if (!resourceNameCurrent.equals(resourceNameUpdated)) {
			if (!hasBeenCertified) {
				Either<Boolean, ResponseFormat> validateResourceNameResponse = validateComponentName(null, updateInfoResource, null);
				if (validateResourceNameResponse.isRight()) {
					ResponseFormat errorResponse = validateResourceNameResponse.right().value();
					return Either.right(errorResponse);
				}
				validateResourceNameResponse = validateResourceNameExists(updateInfoResource);
				if (validateResourceNameResponse.isRight()) {
					ResponseFormat errorResponse = validateResourceNameResponse.right().value();
					return Either.right(errorResponse);
				}
				currentResource.setName(resourceNameUpdated);
				currentResource.setNormalizedName(ValidationUtils.normaliseComponentName(resourceNameUpdated));
				currentResource.setSystemName(ValidationUtils.convertToSystemName(resourceNameUpdated));

			} else {
				log.info("Resource name: {}, cannot be updated once the resource has been certified once.", resourceNameUpdated);
				ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NAME_CANNOT_BE_CHANGED);
				return Either.right(errorResponse);
			}
		}
		return Either.left(true);
	}

	private Either<Boolean, ResponseFormat> validateIcon(Resource currentResource, Resource updateInfoResource, boolean hasBeenCertified) {
		String iconUpdated = updateInfoResource.getIcon();
		String iconCurrent = currentResource.getIcon();
		if (!iconCurrent.equals(iconUpdated)) {
			if (!hasBeenCertified) {
				Either<Boolean, ResponseFormat> validateIcon = validateIcon(null, updateInfoResource, null);
				if (validateIcon.isRight()) {
					ResponseFormat errorResponse = validateIcon.right().value();
					return Either.right(errorResponse);
				}
			} else {
				log.info("Icon {} cannot be updated once the resource has been certified once.", iconUpdated);
				ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.RESOURCE_ICON_CANNOT_BE_CHANGED);
				return Either.right(errorResponse);
			}
		}
		return Either.left(true);
	}

	private Either<Boolean, ResponseFormat> validateVendorName(Resource currentResource, Resource updateInfoResource, boolean hasBeenCertified) {
		String vendorNameUpdated = updateInfoResource.getVendorName();
		String vendorNameCurrent = currentResource.getVendorName();
		if (!vendorNameCurrent.equals(vendorNameUpdated)) {
			if (!hasBeenCertified) {
				Either<Boolean, ResponseFormat> validateVendorName = validateVendorName(null, updateInfoResource, null);
				if (validateVendorName.isRight()) {
					ResponseFormat errorResponse = validateVendorName.right().value();
					return Either.right(errorResponse);
				}
			} else {
				log.info("Vendor name {} cannot be updated once the resource has been certified once.", vendorNameUpdated);
				ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.RESOURCE_VENDOR_NAME_CANNOT_BE_CHANGED);
				return Either.right(errorResponse);
			}
		}
		return Either.left(true);
	}

	private Either<Boolean, ResponseFormat> validateCategory(Resource currentResource, Resource updateInfoResource, boolean hasBeenCertified, boolean inTransaction) {
		Either<Boolean, ResponseFormat> validateCategoryName = validateCategory(null, updateInfoResource, null, inTransaction);
		if (validateCategoryName.isRight()) {
			ResponseFormat errorResponse = validateCategoryName.right().value();
			return Either.right(errorResponse);
		}
		if (hasBeenCertified) {
			CategoryDefinition currentCategory = currentResource.getCategories().get(0);
			SubCategoryDefinition currentSubCategory = currentCategory.getSubcategories().get(0);
			CategoryDefinition updateCategory = updateInfoResource.getCategories().get(0);
			SubCategoryDefinition updtaeSubCategory = updateCategory.getSubcategories().get(0);
			if (!currentCategory.getName().equals(updateCategory.getName()) || !currentSubCategory.getName().equals(updtaeSubCategory.getName())) {
				log.info("Category {} cannot be updated once the resource has been certified once.", currentResource.getCategories());
				ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.RESOURCE_CATEGORY_CANNOT_BE_CHANGED);
				return Either.right(errorResponse);
			}
		}
		return Either.left(true);
	}

	private Either<Boolean, ResponseFormat> validateDerivedFromDuringUpdate(Resource currentResource, Resource updateInfoResource, boolean hasBeenCertified) {

		List<String> currentDerivedFrom = currentResource.getDerivedFrom();
		List<String> updatedDerivedFrom = updateInfoResource.getDerivedFrom();
		if (currentDerivedFrom == null || currentDerivedFrom.isEmpty() || updatedDerivedFrom == null || updatedDerivedFrom.isEmpty()) {
			log.trace("Update normative types");
			return Either.left(true);
		}

		String derivedFromCurrent = currentDerivedFrom.get(0);
		String derivedFromUpdated = updatedDerivedFrom.get(0);

		if (!derivedFromCurrent.equals(derivedFromUpdated)) {
			if (!hasBeenCertified) {
				Either<Boolean, ResponseFormat> validateDerivedFromExistsEither = validateDerivedFromExist(null, updateInfoResource, null);
				if (validateDerivedFromExistsEither.isRight()) {
					return validateDerivedFromExistsEither;
				}
			} else {
				Either<Boolean, ResponseFormat> validateDerivedFromExtending = validateDerivedFromExtending(null, currentResource, updateInfoResource, null);

				if (validateDerivedFromExtending.isRight() || !validateDerivedFromExtending.left().value()) {
					log.debug("Derived from cannot be updated if it doesnt inherits directly or extends inheritance");
					return validateDerivedFromExtending;
				}
			}
		} else {
			// For derived from, we must know whether it was actually changed,
			// otherwise we must do no action.
			// Due to changes it inflicts on data model (remove artifacts,
			// properties...), it's not like a flat field which can be
			// overwritten if not changed.
			// So we must indicate that derived from is not changed
			updateInfoResource.setDerivedFrom(null);
		}
		return Either.left(true);
	}

	private Either<Boolean, ResponseFormat> validateDerivedFromExist(User user, Resource resource, AuditingActionEnum actionEnum) {

		if (resource.getDerivedFrom() == null || resource.getDerivedFrom().isEmpty()) {
			return Either.left(true);
		}

		// IResourceOperation resourceOperation = getResourceOperation();

		String templateName = resource.getDerivedFrom().get(0);

		Either<Boolean, StorageOperationStatus> dataModelResponse = toscaOperationFacade.validateToscaResourceNameExists(templateName);
		if (dataModelResponse.isRight()) {
			StorageOperationStatus storageStatus = dataModelResponse.right().value();
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError, "Create Resource - validateDerivedFromExist");
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Create Resource - validateDerivedFromExist");
			log.debug("request to data model failed with error: {}", storageStatus.name());
			ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(storageStatus), resource);
			log.trace("audit before sending response");
			componentsUtils.auditResource(responseFormat, user, resource, "", "", actionEnum, null);
			return Either.right(responseFormat);
		}

		else if (!dataModelResponse.left().value()) {
			log.info("resource template with name: {}, does not exists", templateName);
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.PARENT_RESOURCE_NOT_FOUND);
			componentsUtils.auditResource(responseFormat, user, resource, "", "", actionEnum, null);

			return Either.right(responseFormat);

		}
		return Either.left(true);
	}

	// Tal G for extending inheritance US815447
	private Either<Boolean, ResponseFormat> validateDerivedFromExtending(User user, Resource currentResource, Resource updateInfoResource, AuditingActionEnum actionEnum) {
		// If updated resource is not deriving, should fail validation
		/*
		 * if (currentResource.getDerivedFrom() == null || currentResource.getDerivedFrom().isEmpty()) { return Either.left(false); }
		 */
		// If current resource is deriving from certain type and it is updated to not deriving, should fail validation
		/*
		 * if (updateInfoResource.getDerivedFrom() == null || updateInfoResource.getDerivedFrom().isEmpty()) { return Either.left(false); }
		 */
		String currentTemplateName = currentResource.getDerivedFrom().get(0);
		String updatedTemplateName = updateInfoResource.getDerivedFrom().get(0);

		Either<Boolean, StorageOperationStatus> dataModelResponse = toscaOperationFacade.validateToscaResourceNameExtends(currentTemplateName, updatedTemplateName);
		if (dataModelResponse.isRight()) {
			StorageOperationStatus storageStatus = dataModelResponse.right().value();
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Create/Update Resource - validateDerivingFromExtendingType");
			log.debug("request to data model failed with error: {}", storageStatus.name());
			ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(storageStatus), currentResource);
			log.trace("audit before sending response");
			componentsUtils.auditResource(responseFormat, user, currentResource, "", "", actionEnum, null);
			return Either.right(responseFormat);
		}

		if (!dataModelResponse.left().value()) {
			log.info("resource template with name {} does not inherit as original {}", updatedTemplateName, currentTemplateName);
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.PARENT_RESOURCE_DOES_NOT_EXTEND);
			componentsUtils.auditResource(responseFormat, user, currentResource, "", "", actionEnum, null);

			return Either.right(responseFormat);

		}
		return Either.left(true);
	}

	public Either<Boolean, ResponseFormat> validateDerivedFromNotEmpty(User user, Resource resource, AuditingActionEnum actionEnum) {
		log.debug("validate resource derivedFrom field");
		if ((resource.getDerivedFrom() == null) || (resource.getDerivedFrom().isEmpty()) || (resource.getDerivedFrom().get(0)) == null || (resource.getDerivedFrom().get(0).trim().isEmpty())) {
			log.info("derived from (template) field is missing for the resource");
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_DERIVED_FROM_TEMPLATE);
			componentsUtils.auditResource(responseFormat, user, resource, "", "", actionEnum, null);

			return Either.right(responseFormat);
		}
		return Either.left(true);
	}

	private Either<Boolean, ResponseFormat> validateResourceNameExists(Resource resource) {

		Either<Boolean, StorageOperationStatus> resourceOperationResponse = toscaOperationFacade.validateComponentNameExists(resource.getName(), resource.getResourceType(), resource.getComponentType());
		if (resourceOperationResponse.isLeft()) {
			if (!resourceOperationResponse.left().value()) {
				return Either.left(false);
			} else {
				log.debug("resource with name: {}, already exists", resource.getName());
				ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_NAME_ALREADY_EXIST, ComponentTypeEnum.RESOURCE.getValue(), resource.getName());
				return Either.right(errorResponse);
			}
		}
		log.debug("error while validateResourceNameExists for resource: {}", resource.getName());
		ResponseFormat errorResponse = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(resourceOperationResponse.right().value()));
		return Either.right(errorResponse);
	}

	/*
	 * private Either<Boolean, ResponseFormat> validateTagsListAndRemoveDuplicates(User user, Resource resource, AuditingActionEnum actionEnum) { List<String> tagsList = resource.getTags();
	 * 
	 * Either<Boolean, ResponseFormat> validateTags = validateResourceTags(tagsList, resource.getResourceName()); if (validateTags.isRight()) { ResponseFormat responseFormat = validateTags.right().value();
	 * componentsUtils.auditResource(responseFormat, user, resource, "", "", actionEnum, null); return Either.right(responseFormat); } ValidationUtils.removeDuplicateFromList(tagsList); return Either.left(true);
	 * 
	 * }
	 * 
	 * private Either<Boolean, ResponseFormat> validateResourceTags(List<String> tags, String resourceName) { log.debug("validate resource tags"); boolean includesResourceName = false; int tagListSize = 0; if (tags != null && !tags.isEmpty()) { for
	 * (String tag : tags) { if (!ValidationUtils.validateTagLength(tag)) { log.debug("tag length exceeds limit {}", ValidationUtils.TAG_MAX_LENGTH); return Either.right(componentsUtils.getResponseFormat(ActionStatus.
	 * COMPONENT_SINGLE_TAG_EXCEED_LIMIT, "" + ValidationUtils.TAG_MAX_LENGTH)); } if (ValidationUtils.validateComponentNamePattern(tag)) { if (!includesResourceName) { includesResourceName = resourceName.equals(tag); } } else {
	 * log.debug("invalid tag {}", tag); return Either.right(componentsUtils.getResponseFormat(ActionStatus. COMPONENT_INVALID_TAG)); } tagListSize += tag.length() + 1; } if (!includesResourceName) { log.debug( "tags must include resource name");
	 * return Either.right(componentsUtils.getResponseFormat(ActionStatus. COMPONENT_INVALID_TAGS_NO_COMP_NAME)); } if (!ValidationUtils.validateTagListLength(tagListSize)) { log.debug( "overall tags length {}, exceeds limit {}", tagListSize,
	 * ValidationUtils.TAG_LIST_MAX_LENGTH); return Either.right(componentsUtils.getResponseFormat(ActionStatus. COMPONENT_TAGS_EXCEED_LIMIT, "" + ValidationUtils.TAG_LIST_MAX_LENGTH)); } return Either.left(true); }
	 * 
	 * return Either.right(componentsUtils.getResponseFormat(ActionStatus. COMPONENT_MISSING_TAGS)); }
	 */

	private Either<Boolean, ResponseFormat> validateCategory(User user, Resource resource, AuditingActionEnum actionEnum, boolean inTransaction) {

		List<CategoryDefinition> categories = resource.getCategories();
		if (categories == null || categories.size() == 0) {
			log.debug("Resource category is empty");
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_MISSING_CATEGORY, ComponentTypeEnum.RESOURCE.getValue());
			componentsUtils.auditResource(responseFormat, user, resource, "", "", actionEnum, null);
			return Either.right(responseFormat);
		}
		if (categories.size() > 1) {
			log.debug("Must be only one category for resource");
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_TOO_MUCH_CATEGORIES, ComponentTypeEnum.RESOURCE.getValue());
			return Either.right(responseFormat);
		}
		CategoryDefinition category = categories.get(0);
		List<SubCategoryDefinition> subcategories = category.getSubcategories();
		if (subcategories == null || subcategories.size() == 0) {
			log.debug("Missinig subcategory for resource");
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_MISSING_SUBCATEGORY);
			return Either.right(responseFormat);
		}
		if (subcategories.size() > 1) {
			log.debug("Must be only one sub ategory for resource");
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESOURCE_TOO_MUCH_SUBCATEGORIES);
			return Either.right(responseFormat);
		}

		SubCategoryDefinition subcategory = subcategories.get(0);

		if (!ValidationUtils.validateStringNotEmpty(category.getName())) {
			log.debug("Resource category is empty");
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_MISSING_CATEGORY, ComponentTypeEnum.RESOURCE.getValue());
			componentsUtils.auditResource(responseFormat, user, resource, "", "", actionEnum, null);
			return Either.right(responseFormat);
		}
		if (!ValidationUtils.validateStringNotEmpty(subcategory.getName())) {
			log.debug("Resource category is empty");
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_MISSING_SUBCATEGORY, ComponentTypeEnum.RESOURCE.getValue());
			componentsUtils.auditResource(responseFormat, user, resource, "", "", actionEnum, null);
			return Either.right(responseFormat);
		}

		Either<Boolean, ResponseFormat> validateCategory = validateCategoryListed(category, subcategory, inTransaction);
		if (validateCategory.isRight()) {
			ResponseFormat responseFormat = validateCategory.right().value();
			componentsUtils.auditResource(responseFormat, user, resource, "", "", actionEnum, null);
			return Either.right(responseFormat);
		}

		return Either.left(true);
	}

	private Either<Boolean, ResponseFormat> validateCategoryListed(CategoryDefinition category, SubCategoryDefinition subcategory, boolean inTransaction) {
		if (category != null && subcategory != null) {
			log.debug("validating resource category {} against valid categories list", category);
			Either<List<CategoryDefinition>, ActionStatus> categories = elementDao.getAllCategories(NodeTypeEnum.ResourceNewCategory, inTransaction);
			if (categories.isRight()) {
				log.debug("failed to retrive resource categories from Titan");
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(categories.right().value());
				return Either.right(responseFormat);
			}
			List<CategoryDefinition> categoryList = categories.left().value();
			for (CategoryDefinition cat : categoryList) {
				if (cat.getName().equals(category.getName())) {
					for (SubCategoryDefinition subcat : cat.getSubcategories()) {
						if (subcat.getName().equals(subcategory.getName())) {
							return Either.left(true);
						}
					}
					log.debug("SubCategory {} is not part of resource category group. Resource subcategory valid values are {}", subcategory, cat.getSubcategories());
					return Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INVALID_CATEGORY, ComponentTypeEnum.RESOURCE.getValue()));
				}
			}
			log.debug("Category {} is not part of resource category group. Resource category valid values are {}", category, categoryList);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INVALID_CATEGORY, ComponentTypeEnum.RESOURCE.getValue()));
		}
		return Either.left(false);
	}

	public Either<Boolean, ResponseFormat> validateVendorReleaseName(User user, Resource resource, AuditingActionEnum actionEnum) {
		String vendorRelease = resource.getVendorRelease();

		log.debug("validate vendor relese name");
		if (!ValidationUtils.validateStringNotEmpty(vendorRelease)) {
			log.info("vendor relese name is missing.");
			ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.MISSING_VENDOR_RELEASE);
			componentsUtils.auditResource(errorResponse, user, resource, "", "", actionEnum, null);
			return Either.right(errorResponse);
		}

		Either<Boolean, ResponseFormat> validateVendorReleaseResponse = validateVendorReleaseName(vendorRelease);
		if (validateVendorReleaseResponse.isRight()) {
			ResponseFormat responseFormat = validateVendorReleaseResponse.right().value();
			componentsUtils.auditResource(responseFormat, user, resource, "", "", actionEnum, null);
		}
		return validateVendorReleaseResponse;
	}

	public Either<Boolean, ResponseFormat> validateVendorReleaseName(String vendorRelease) {
		if (vendorRelease != null) {
			if (!ValidationUtils.validateVendorReleaseLength(vendorRelease)) {
				log.info("vendor release exceds limit.");
				ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.VENDOR_RELEASE_EXCEEDS_LIMIT, "" + ValidationUtils.VENDOR_RELEASE_MAX_LENGTH);
				return Either.right(errorResponse);
			}

			if (!ValidationUtils.validateVendorRelease(vendorRelease)) {
				log.info("vendor release  is not valid.");
				ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.INVALID_VENDOR_RELEASE);
				return Either.right(errorResponse);
			}
			return Either.left(true);
		}
		return Either.left(false);

	}

	private Either<Boolean, ResponseFormat> validateVendorName(User user, Resource resource, AuditingActionEnum actionEnum) {
		String vendorName = resource.getVendorName();
		if (!ValidationUtils.validateStringNotEmpty(vendorName)) {
			log.info("vendor name is missing.");
			ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.MISSING_VENDOR_NAME);
			componentsUtils.auditResource(errorResponse, user, resource, "", "", actionEnum, null);
			return Either.right(errorResponse);
		}

		Either<Boolean, ResponseFormat> validateVendorNameResponse = validateVendorName(vendorName);
		if (validateVendorNameResponse.isRight()) {
			ResponseFormat responseFormat = validateVendorNameResponse.right().value();
			componentsUtils.auditResource(responseFormat, user, resource, "", "", actionEnum, null);
		}
		return validateVendorNameResponse;

	}

	private Either<Boolean, ResponseFormat> validateVendorName(String vendorName) {
		if (vendorName != null) {
			if (!ValidationUtils.validateVendorNameLength(vendorName)) {
				log.info("vendor name exceds limit.");
				ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.VENDOR_NAME_EXCEEDS_LIMIT, "" + ValidationUtils.VENDOR_NAME_MAX_LENGTH);
				return Either.right(errorResponse);
			}

			if (!ValidationUtils.validateVendorName(vendorName)) {
				log.info("vendor name  is not valid.");
				ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.INVALID_VENDOR_NAME);
				return Either.right(errorResponse);
			}
			return Either.left(true);

		}
		return Either.left(false);

	}

	/*
	 * private Either<Boolean, ResponseFormat> validateDescriptionAndCleanup(User user, Resource resource, AuditingActionEnum actionEnum) { String description = resource.getDescription(); if (!ValidationUtils.validateStringNotEmpty(description)) {
	 * log.debug("Resource description is empty"); ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus. COMPONENT_MISSING_DESCRIPTION, ComponentTypeEnum.RESOURCE.getValue()); componentsUtils.auditResource(errorResponse,
	 * user, resource, "", "", actionEnum, null); return Either.right(errorResponse); }
	 * 
	 * description = ValidationUtils.removeNoneUtf8Chars(description); description = ValidationUtils.removeHtmlTags(description); description = ValidationUtils.normaliseWhitespace(description); description = ValidationUtils.stripOctets(description);
	 * 
	 * Either<Boolean, ResponseFormat> validatDescription = validateResourceDescription(description); if (validatDescription.isRight()) { ResponseFormat responseFormat = validatDescription.right().value();
	 * componentsUtils.auditResource(responseFormat, user, resource, "", "", actionEnum, null); return Either.right(responseFormat); } resource.setDescription(description); return Either.left(true); }
	 * 
	 * private Either<Boolean, ResponseFormat> validateResourceDescription(String description) { if (description != null) { if (!ValidationUtils.validateDescriptionLength(description)) { return
	 * Either.right(componentsUtils.getResponseFormat(ActionStatus. COMPONENT_DESCRIPTION_EXCEEDS_LIMIT, ComponentTypeEnum.RESOURCE.getValue(), "" + ValidationUtils.COMPONENT_DESCRIPTION_MAX_LENGTH)); }
	 * 
	 * if (!ValidationUtils.validateIsEnglish(description)) { return Either.right(componentsUtils.getResponseFormat(ActionStatus. COMPONENT_INVALID_DESCRIPTION, ComponentTypeEnum.RESOURCE.getValue())); } return Either.left(true); } return
	 * Either.left(false); }
	 */

	/*
	 * private Either<Boolean, ResponseFormat> validateContactId(User user, Resource resource, AuditingActionEnum actionEnum) { String contactId = resource.getContactId();
	 * 
	 * if (!ValidationUtils.validateStringNotEmpty(contactId)) { log.info("contact info is missing."); ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus. COMPONENT_MISSING_CONTACT, ComponentTypeEnum.RESOURCE.getValue());
	 * componentsUtils.auditResource(errorResponse, user, resource, "", "", actionEnum, null); return Either.right(errorResponse); }
	 * 
	 * Either<Boolean, ResponseFormat> validateContactIdResponse = validateContactId(contactId); if (validateContactIdResponse.isRight()) { ResponseFormat responseFormat = validateContactIdResponse.right().value();
	 * componentsUtils.auditResource(responseFormat, user, resource, "", "", actionEnum, null); } return validateContactIdResponse; }
	 * 
	 * private Either<Boolean, ResponseFormat> validateContactId(String contactId) { if (contactId != null) { if (!ValidationUtils.validateContactId(contactId)) { log.debug("contact {} is invalid.", contactId); ResponseFormat errorResponse =
	 * componentsUtils.getResponseFormat(ActionStatus. COMPONENT_INVALID_CONTACT, ComponentTypeEnum.RESOURCE.getValue()); return Either.right(errorResponse); } return Either.left(true); } return Either.left(false);
	 * 
	 * }
	 */

	/*
	 * private Either<Boolean, ResponseFormat> validateIcon(User user, Resource resource, AuditingActionEnum actionEnum) { String icon = resource.getIcon();
	 * 
	 * if (!ValidationUtils.validateStringNotEmpty(icon)) { log.debug("icon is missing."); ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_MISSING_ICON, ComponentTypeEnum.RESOURCE.getValue());
	 * componentsUtils.auditResource(errorResponse, user, resource, "", "", actionEnum, null); return Either.right(errorResponse); }
	 * 
	 * Either<Boolean, ResponseFormat> validateIcon = validateIcon(icon); if (validateIcon.isRight()) { ResponseFormat responseFormat = validateIcon.right().value(); componentsUtils.auditResource(responseFormat, user, resource, "", "", actionEnum,
	 * null); } return validateIcon;
	 * 
	 * }
	 * 
	 * public Either<Boolean, ResponseFormat> validateIcon(String icon) { if (icon != null) { if (!ValidationUtils.validateIconLength(icon)) { log.debug("icon exceeds max length"); return Either.right(componentsUtils.getResponseFormat(ActionStatus.
	 * COMPONENT_ICON_EXCEEDS_LIMIT, ComponentTypeEnum.RESOURCE.getValue(), "" + ValidationUtils.ICON_MAX_LENGTH)); } if (!ValidationUtils.validateIcon(icon)) { log.debug("icon is invalid." ); ResponseFormat errorResponse =
	 * componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INVALID_ICON, ComponentTypeEnum.RESOURCE.getValue()); return Either.right(errorResponse); } return Either.left(true); } return Either.left(false);
	 * 
	 * }
	 */

	private Either<Boolean, ResponseFormat> validateCost(User user, Resource resource, AuditingActionEnum actionEnum) {
		String cost = resource.getCost();
		if (cost != null) {

			if (!ValidationUtils.validateCost(cost)) {
				log.debug("resource cost is invalid.");
				ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT);
				return Either.right(errorResponse);
			}
		}
		return Either.left(true);
	}

	private Either<Boolean, ResponseFormat> validateLicenseType(User user, Resource resource, AuditingActionEnum actionEnum) {
		log.debug("validate licenseType");
		String licenseType = resource.getLicenseType();
		if (licenseType != null) {
			List<String> licenseTypes = ConfigurationManager.getConfigurationManager().getConfiguration().getLicenseTypes();
			if (!licenseTypes.contains(licenseType)) {
				log.debug("License type {} isn't configured");
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT);
				if (actionEnum != null) {
					// In update case, no audit is required
					componentsUtils.auditResource(responseFormat, user, resource, "", "", actionEnum, null);
				}
				return Either.right(responseFormat);
			}
		}
		return Either.left(true);
	}

	private Either<Boolean, ResponseFormat> processUpdateOfDerivedFrom(Resource currentResource, Resource updatedResource, String userId, boolean shouldLock, boolean inTransaction) {
		Either<Operation, ResponseFormat> deleteArtifactByInterface = null;
		if (updatedResource.getDerivedFrom() != null) {
			log.debug("Starting derived from update for resource {}", updatedResource.getUniqueId());
			log.debug("1. Removing interface artifacts from graph");
			// Remove all interface artifacts of resource
			String resourceId = updatedResource.getUniqueId();
			Map<String, InterfaceDefinition> interfaces = currentResource.getInterfaces();

			if (interfaces != null) {
				Collection<InterfaceDefinition> values = interfaces.values();
				for (InterfaceDefinition interfaceDefinition : values) {
					String interfaceType = interfaceTypeOperation.getShortInterfaceName(interfaceDefinition);

					log.trace("Starting interface artifacts removal for interface type {}", interfaceType);
					Map<String, Operation> operations = interfaceDefinition.getOperationsMap();
					if (operations != null) {
						for (Entry<String, Operation> operationEntry : operations.entrySet()) {
							Operation operation = operationEntry.getValue();
							ArtifactDefinition implementation = operation.getImplementationArtifact();
							if (implementation != null) {
								String uniqueId = implementation.getUniqueId();
								log.debug("Removing interface artifact definition {}, operation {}, interfaceType {}", uniqueId, operationEntry.getKey(), interfaceType);
								// only thing that transacts and locks here
								deleteArtifactByInterface = artifactsBusinessLogic.deleteArtifactByInterface(resourceId, interfaceType, operationEntry.getKey(), userId, uniqueId, null, shouldLock, true);
								if (deleteArtifactByInterface.isRight()) {
									log.debug("Couldn't remove artifact definition with id {}", uniqueId);
									if (!inTransaction) {
										titanDao.rollback();
									}
									return Either.right(deleteArtifactByInterface.right().value());
								}
							} else {
								log.trace("No implementation found for operation {} - nothing to delete", operationEntry.getKey());
							}
						}
					} else {
						log.trace("No operations found for interface type {}", interfaceType);
					}
				}
			}
			log.debug("2. Removing properties");
			Either<Map<String, PropertyDefinition>, StorageOperationStatus> findPropertiesOfNode = propertyOperation.deleteAllPropertiesAssociatedToNode(NodeTypeEnum.Resource, resourceId);

			if (findPropertiesOfNode.isRight() && !findPropertiesOfNode.right().value().equals(StorageOperationStatus.OK)) {
				log.debug("Failed to remove all properties of resource");
				if (!inTransaction)
					titanDao.rollback();
				return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(findPropertiesOfNode.right().value())));
			}

		} else {
			log.debug("Derived from wasn't changed during update");
		}

		if (!inTransaction)
			titanDao.commit();
		return Either.left(true);

	}

	/**** Auditing *******************/

	protected static IElementOperation getElementDao(Class<IElementOperation> class1, ServletContext context) {
		WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);

		WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);

		return webApplicationContext.getBean(class1);
	}

	public ICapabilityTypeOperation getCapabilityTypeOperation() {
		return capabilityTypeOperation;
	}

	public void setCapabilityTypeOperation(ICapabilityTypeOperation capabilityTypeOperation) {
		this.capabilityTypeOperation = capabilityTypeOperation;
	}

	public Either<Boolean, ResponseFormat> validatePropertiesDefaultValues(Resource resource) {
		log.debug("validate resource properties default values");
		Either<Boolean, ResponseFormat> eitherResult = Either.left(true);
		List<PropertyDefinition> properties = resource.getProperties();
		String type = null;
		String innerType = null;
		if (properties != null) {
			for (PropertyDefinition property : properties) {
				if (!propertyOperation.isPropertyTypeValid(property)) {
					log.info("Invalid type for property");
					ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_PROPERTY_TYPE, property.getType(), property.getName());
					eitherResult = Either.right(responseFormat);
					break;
				}

				Either<Map<String, DataTypeDefinition>, ResponseFormat> allDataTypes = getAllDataTypes(applicationDataTypeCache);
				if (allDataTypes.isRight()) {
					return Either.right(allDataTypes.right().value());
				}

				type = property.getType();
				if (type.equals(ToscaPropertyType.LIST.getType()) || type.equals(ToscaPropertyType.MAP.getType())) {
					ImmutablePair<String, Boolean> propertyInnerTypeValid = propertyOperation.isPropertyInnerTypeValid(property, allDataTypes.left().value());
					innerType = propertyInnerTypeValid.getLeft();
					if (!propertyInnerTypeValid.getRight().booleanValue()) {
						log.info("Invalid inner type for property");
						ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_PROPERTY_INNER_TYPE, innerType, property.getName());
						eitherResult = Either.right(responseFormat);
						break;
					}
				}

				if (!propertyOperation.isPropertyDefaultValueValid(property, allDataTypes.left().value())) {
					log.info("Invalid default value for property");
					ResponseFormat responseFormat;
					if (type.equals(ToscaPropertyType.LIST.getType()) || type.equals(ToscaPropertyType.MAP.getType())) {
						responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_COMPLEX_DEFAULT_VALUE, property.getName(), type, innerType, property.getDefaultValue());
					} else {
						responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_DEFAULT_VALUE, property.getName(), type, property.getDefaultValue());
					}
					eitherResult = Either.right(responseFormat);
					break;

				}
			}
		}
		return eitherResult;
	}

	@Override
	public Either<List<String>, ResponseFormat> deleteMarkedComponents() {
		return deleteMarkedComponents(ComponentTypeEnum.RESOURCE);
	}

	@Override
	public ComponentInstanceBusinessLogic getComponentInstanceBL() {
		return vfComponentInstanceBusinessLogic;
	}

	private String getComponentTypeForResponse(Component component) {
		String componentTypeForResponse = "SERVICE";
		if (component instanceof Resource) {
			componentTypeForResponse = ((Resource) component).getResourceType().name();
		}
		return componentTypeForResponse;
	}

	private Either<Map<String, GroupDefinition>, ResponseFormat> createGroupsFromYaml(String yamlFileName, Map<String, Object> toscaJson, Resource resource) {

		Map<String, GroupDefinition> groups = new HashMap<String, GroupDefinition>();
		Either<Map<String, GroupDefinition>, ResponseFormat> result = Either.left(groups);

		Either<Map<String, Object>, ResultStatusEnum> eitherNodesTemlates = ImportUtils.findFirstToscaMapElement(toscaJson, ToscaTagNamesEnum.GROUPS);
		if (eitherNodesTemlates.isLeft()) {
			Map<String, Object> jsonNodeTemplates = eitherNodesTemlates.left().value();

			if (jsonNodeTemplates != null && false == jsonNodeTemplates.isEmpty()) {
				Iterator<Entry<String, Object>> nodesNameValue = jsonNodeTemplates.entrySet().iterator();
				while (nodesNameValue.hasNext()) {
					Entry<String, Object> groupNameValue = nodesNameValue.next();

					String groupName = groupNameValue.getKey();
					Either<GroupDefinition, ResponseFormat> eitherNode = createGroupInfo(groupName, groupNameValue.getValue());
					if (eitherNode.isRight()) {
						String message = "Failed when creating group: " + groupNameValue.getKey() + " for resource:" + resource.getName();
						BeEcompErrorManager.getInstance().logInternalFlowError("ImportResource", message, ErrorSeverity.INFO);
						return Either.right(eitherNode.right().value());
					} else {
						GroupDefinition groupDefinition = eitherNode.left().value();
						groups.put(groupName, groupDefinition);
					}
				}
			}
		}

		return result;
	}

	private Either<Map<String, InputDefinition>, ResponseFormat> createInputsFromYaml(String yamlFileName, Map<String, Object> toscaJson, Resource resource) {

		Either<Map<String, InputDefinition>, ResultStatusEnum> inputs = ImportUtils.getInputs(toscaJson);
		if (inputs.isRight()) {
			String message = "Failed when creating inputs:  for resource:" + resource.getName();
			BeEcompErrorManager.getInstance().logInternalFlowError("ImportResource", message, ErrorSeverity.INFO);
			Map<String, InputDefinition> resultMap = new HashMap();
			return Either.left(resultMap);

		}

		Either<Map<String, InputDefinition>, ResponseFormat> result = Either.left(inputs.left().value());

		return result;
	}

	private Either<GroupDefinition, ResponseFormat> createGroupInfo(String groupName, Object groupTemplateJson) {

		GroupDefinition groupInfo = new GroupDefinition();
		groupInfo.setName(groupName);
		Either<GroupDefinition, ResponseFormat> result = Either.left(groupInfo);

		try {
			if (groupTemplateJson != null && groupTemplateJson instanceof Map) {
				Map<String, Object> groupTemplateJsonMap = (Map<String, Object>) groupTemplateJson;
				// Type
				String groupType = null;
				if (groupTemplateJsonMap.containsKey(ToscaTagNamesEnum.TYPE.getElementName())) {
					groupType = (String) groupTemplateJsonMap.get(ToscaTagNamesEnum.TYPE.getElementName());
					groupInfo.setType(groupType);
				} else {
					log.debug("The 'type' member is not found under group {}", groupName);
					result = Either.right(componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE));
				}

				if (groupTemplateJsonMap.containsKey(ToscaTagNamesEnum.DESCRIPTION.getElementName())) {
					groupInfo.setDescription((String) groupTemplateJsonMap.get(ToscaTagNamesEnum.DESCRIPTION.getElementName()));
				}

				if (groupTemplateJsonMap.containsKey(ToscaTagNamesEnum.MEMBERS.getElementName())) {
					Object members = groupTemplateJsonMap.get(ToscaTagNamesEnum.MEMBERS.getElementName());
					if (members != null) {
						if (members instanceof List) {
							Map<String, String> membersLoaded = new HashMap<>();
							List<?> membersAsList = (List<?>) members;
							for (Object member : membersAsList) {
								membersLoaded.put(member.toString(), "");
							}
							groupInfo.setMembers(membersLoaded);
						} else {
							log.debug("The 'type' member is not found under group {}", groupName);
							result = Either.right(componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE));
						}
					}
				}

				if (groupTemplateJsonMap.containsKey(ToscaTagNamesEnum.PROPERTIES.getElementName())) {
					Object properties = groupTemplateJsonMap.get(ToscaTagNamesEnum.PROPERTIES.getElementName());

					Either<List<GroupProperty>, ResponseFormat> regResponse = createPropertiesValueModuleFromYaml(properties, groupName, groupType);
					if (regResponse.isRight())
						return Either.right(regResponse.right().value());
					if (regResponse.left().value().size() > 0) {
						groupInfo.convertFromGroupProperties(regResponse.left().value());
					}
				}

			} else {
				result = Either.right(componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE));
			}
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().logBeSystemError("Import Resource - create group");
			log.debug("error when creating group, message:{}", e.getMessage(), e);
			result = Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_YAML));
		}

		return result;
	}

	private Either<List<GroupProperty>, ResponseFormat> createPropertiesValueModuleFromYaml(Object properties, String groupName, String groupType) {

		List<GroupProperty> result = new ArrayList<>();

		if (properties == null) {
			return Either.left(result);
		}

		Either<GroupTypeDefinition, StorageOperationStatus> groupTypeRes = groupTypeOperation.getLatestGroupTypeByType(groupType, true);

		if (groupTypeRes.isRight()) {
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.GROUP_MISSING_GROUP_TYPE, groupType));
		}

		Map<String, PropertyDefinition> gtProperties = new HashMap<>();
		GroupTypeDefinition groupTypeDefinition = groupTypeRes.left().value();

		List<PropertyDefinition> propertiesDef = groupTypeDefinition.getProperties();

		if (propertiesDef != null) {
			gtProperties = propertiesDef.stream().collect(Collectors.toMap(p -> p.getName(), p -> p));
		}

		if (properties != null) {

			if (properties instanceof Map) {

				Map<String, Object> props = (Map<String, Object>) properties;
				for (Entry<String, Object> entry : props.entrySet()) {

					String propName = entry.getKey();
					Object value = entry.getValue();

					PropertyDefinition gtDefinition = gtProperties.get(propName);
					if (gtDefinition == null) {
						return Either.right(componentsUtils.getResponseFormat(ActionStatus.GROUP_PROPERTY_NOT_FOUND, propName, groupName, groupType));
					}

					ToscaPropertyType type = ToscaPropertyType.isValidType(gtDefinition.getType());

					String convertedValue = null;
					if (value != null) {
						if (type == null || value instanceof Map || value instanceof List) {
							convertedValue = gson.toJson(value);
						} else {
							convertedValue = value.toString();
						}
					}

					GroupProperty groupProperty = new GroupProperty();
					groupProperty.setValue(convertedValue);
					groupProperty.setName(propName);

					log.trace("After building group property {}", groupProperty);

					result.add(groupProperty);
				}

			}

		}

		return Either.left(result);
	}

	public Either<Resource, ResponseFormat> getLatestResourceFromCsarUuid(String csarUuid, User user) {

		// validate user
		if (user != null) {
			Either<User, ResponseFormat> userValidation = validateUserExists(user, "Get resource from csar UUID", false);
			if (userValidation.isRight()) {
				return Either.right(userValidation.right().value());
			}
		}

		// get resource from csar uuid
		Either<Resource, StorageOperationStatus> either = toscaOperationFacade.getLatestComponentByCsarOrName(ComponentTypeEnum.RESOURCE, csarUuid, "");
		if (either.isRight()) {
			ResponseFormat resp = componentsUtils.getResponseFormat(ActionStatus.RESOURCE_FROM_CSAR_NOT_FOUND, csarUuid);
			return Either.right(resp);
		}

		return Either.left(either.left().value());
	}

	@Override
	public Either<List<ComponentInstance>, ResponseFormat> getComponentInstancesFilteredByPropertiesAndInputs(String componentId, ComponentTypeEnum componentTypeEnum, String userId, String searchText) {
		return null;
	}

	private Either<Map<String, List<CapabilityDefinition>>, ResponseFormat> getValidComponentInstanceCapabilities(Map<String, List<CapabilityDefinition>> defaultCapabilities, Map<String, List<UploadCapInfo>> uploadedCapabilities) {
		ResponseFormat responseFormat;
		Map<String, List<CapabilityDefinition>> validCapabilitiesMap = new HashMap<>();

		for (Entry<String, List<UploadCapInfo>> uploadedCapabilitiesEntry : uploadedCapabilities.entrySet()) {
			String capabilityType = uploadedCapabilitiesEntry.getValue().get(0).getType();
			if (!defaultCapabilities.containsKey(capabilityType)) {
				responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_CAPABILITY_TYPE, capabilityType);
				return Either.right(responseFormat);
			} else {
				CapabilityDefinition delaultCapability = defaultCapabilities.get(capabilityType).get(0);
				Either<Boolean, String> validationRes = validateUniquenessUpdateUploadedComponentInstanceCapability(delaultCapability, uploadedCapabilitiesEntry.getValue().get(0));
				if (validationRes.isRight()) {
					responseFormat = componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NAME_ALREADY_EXISTS, validationRes.right().value());
					return Either.right(responseFormat);
				}
				List<CapabilityDefinition> validCapabilityList = new ArrayList<>();
				validCapabilityList.add(delaultCapability);
				validCapabilitiesMap.put(uploadedCapabilitiesEntry.getKey(), validCapabilityList);
			}
		}
		return Either.left(validCapabilitiesMap);
	}

	private Either<Boolean, String> validateUniquenessUpdateUploadedComponentInstanceCapability(CapabilityDefinition defaultCapability, UploadCapInfo uploadedCapability) {
		List<ComponentInstanceProperty> validProperties = new ArrayList<>();
		Map<String, PropertyDefinition> defaultProperties = defaultCapability.getProperties().stream().collect(Collectors.toMap(PropertyDefinition::getName, Function.identity()));
		List<UploadPropInfo> uploadedProperties = uploadedCapability.getProperties();
		for (UploadPropInfo property : uploadedProperties) {
			String propertyName = property.getName().toLowerCase();
			String propertyType = property.getType();
			ComponentInstanceProperty validProperty;
			if (defaultProperties.containsKey(propertyName)) {
				if (propertyType != null && !defaultProperties.get(propertyName).getType().equals(propertyType)) {
					return Either.right(propertyName);
				}
			}
			validProperty = new ComponentInstanceProperty();
			validProperty.setName(propertyName);
			if (property.getValue() != null)
				validProperty.setValue(property.getValue().toString());
			validProperty.setDescription(property.getDescription());
			validProperty.setPassword(property.isPassword());
			validProperties.add(validProperty);
		}
		defaultCapability.setProperties(validProperties);
		return Either.left(true);
	}

	private Either<EnumMap<ArtifactOperationEnum, List<NonMetaArtifactInfo>>, ResponseFormat> organizeVfCsarArtifactsByArtifactOperation(List<NonMetaArtifactInfo> artifactPathAndNameList, List<ArtifactDefinition> existingArtifactsToHandle,
																																		 Resource resource, User user) {

		EnumMap<ArtifactOperationEnum, List<NonMetaArtifactInfo>> nodeTypeArtifactsToHandle = new EnumMap<>(ArtifactOperationEnum.class);
		Wrapper<ResponseFormat> responseWrapper = new Wrapper<>();
		Either<EnumMap<ArtifactOperationEnum, List<NonMetaArtifactInfo>>, ResponseFormat> nodeTypeArtifactsToHandleRes = Either.left(nodeTypeArtifactsToHandle);
		;
		try {
			// add all found Csar artifacts to list to upload
			List<NonMetaArtifactInfo> artifactsToUpload = new ArrayList<>(artifactPathAndNameList);
			List<NonMetaArtifactInfo> artifactsToUpdate = new ArrayList<>();
			List<NonMetaArtifactInfo> artifactsToDelete = new ArrayList<>();
			for (NonMetaArtifactInfo currNewArtifact : artifactPathAndNameList) {
				ArtifactDefinition foundArtifact;

				if (!existingArtifactsToHandle.isEmpty()) {
					foundArtifact = existingArtifactsToHandle.stream().filter(a -> a.getArtifactName().equals(currNewArtifact.getArtifactName())).findFirst().orElse(null);
					if (foundArtifact != null) {
						if (ArtifactTypeEnum.findType(foundArtifact.getArtifactType()) == currNewArtifact.getArtifactType()) {
							if (!foundArtifact.getArtifactChecksum().equals(currNewArtifact.getArtifactChecksum())) {
								currNewArtifact.setArtifactUniqueId(foundArtifact.getUniqueId());
								// if current artifact already exists, but has different content, add him to the list to update
								artifactsToUpdate.add(currNewArtifact);
							}
							// remove found artifact from the list of existing artifacts to handle, because it was already handled
							existingArtifactsToHandle.remove(foundArtifact);
							// and remove found artifact from the list to upload, because it should either be updated or be ignored
							artifactsToUpload.remove(currNewArtifact);
						} else {
							log.debug("Can't upload two artifact with the same name {}.", currNewArtifact.getArtifactName());
							ResponseFormat responseFormat = ResponseFormatManager.getInstance().getResponseFormat(ActionStatus.ARTIFACT_ALRADY_EXIST_IN_DIFFERENT_TYPE_IN_CSAR, currNewArtifact.getArtifactName(),
									currNewArtifact.getArtifactType().name(), foundArtifact.getArtifactType());
							AuditingActionEnum auditingAction = artifactsBusinessLogic.detectAuditingType(artifactsBusinessLogic.new ArtifactOperationInfo(false, false, ArtifactOperationEnum.Create), foundArtifact.getArtifactChecksum());
							artifactsBusinessLogic.handleAuditing(auditingAction, resource, resource.getUniqueId(), user, null, null, foundArtifact.getUniqueId(), responseFormat, resource.getComponentType(), null);
							responseWrapper.setInnerElement(responseFormat);
							break;
						}
					}
				}
			}
			if (responseWrapper.isEmpty()) {
				existingArtifactsToHandle.stream()
						// add all artifacts which was not detected as artifact to update or to ignore to the list to delete
						.forEach(a -> artifactsToDelete.add(new NonMetaArtifactInfo(a.getArtifactName(), null, ArtifactTypeEnum.findType(a.getArtifactType()), a.getArtifactGroupType(), null, a.getUniqueId())));
			}
			if (responseWrapper.isEmpty()) {
				if (!artifactsToUpload.isEmpty())
					nodeTypeArtifactsToHandle.put(ArtifactOperationEnum.Create, artifactsToUpload);
				if (!artifactsToUpdate.isEmpty())
					nodeTypeArtifactsToHandle.put(ArtifactOperationEnum.Update, artifactsToUpdate);
				if (!artifactsToDelete.isEmpty())
					nodeTypeArtifactsToHandle.put(ArtifactOperationEnum.Delete, artifactsToDelete);
			}
			if (!responseWrapper.isEmpty()) {
				nodeTypeArtifactsToHandleRes = Either.right(responseWrapper.getInnerElement());
			}
		} catch (Exception e) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
			responseWrapper.setInnerElement(responseFormat);
			log.debug("Exception occured when findNodeTypeArtifactsToHandle, error is:{}", e.getMessage(), e);
		}
		return nodeTypeArtifactsToHandleRes;
	}

	private String buildNestedVfcToscaNamespace(String nodeTypeFullName) {

		String actualName = this.getNodeTypeActualName(nodeTypeFullName);
		return ImportUtils.Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX + ResourceTypeEnum.VFC.name().toLowerCase() + actualName;

	}

	private String buildNestedVfcToscaResourceName(String vfResourceName, String nodeTypeFullName) {
		String toscaResourceName;
		String nameWithouNamespacePrefix = getNodeTypeActualName(nodeTypeFullName);
		if (nameWithouNamespacePrefix.startsWith(Constants.ABSTRACT)) {
			toscaResourceName = Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX + ResourceTypeEnum.VFC.name().toLowerCase() + '.' + vfResourceName + '.' + nameWithouNamespacePrefix;
		} else {
			toscaResourceName = Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX + ResourceTypeEnum.VFC.name().toLowerCase() + '.' + vfResourceName + '.' + Constants.ABSTRACT + nameWithouNamespacePrefix;
		}
		return toscaResourceName;
	}

	public ICacheMangerOperation getCacheManagerOperation() {
		return cacheManagerOperation;
	}

	public void setCacheManagerOperation(ICacheMangerOperation cacheManagerOperation) {
		this.cacheManagerOperation = cacheManagerOperation;
	}

	///////////////////////////////////////// DataModel refactoring/////////////////////////////////////////////

	/*
	 * /**
	 * 
	 * @deprecated Use {@link #createOrUpdateResourceByImport(Resource,User,boolean, boolean,boolean)} instead
	 */
	/*
	 * public Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> createOrUpdateResourceByImport(Resource resource, User user, AuditingActionEnum auditingEnum, boolean isNormative, boolean needLock) { return
	 * createOrUpdateResourceByImport(resource, user, isNormative, false, needLock); }
	 */

	public Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> createOrUpdateNodeTypeByImport(Resource resource, User user, boolean isNormative, boolean isInTransaction, boolean needLock) {

		// check if resource already exist
		Either<Resource, StorageOperationStatus> latestByName = toscaOperationFacade.getLatestByName(resource.getName());
		Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> result = null;

		// create
		if (latestByName.isRight() && latestByName.right().value().equals(StorageOperationStatus.NOT_FOUND)) {

			Either<Resource, StorageOperationStatus> latestByToscaName = toscaOperationFacade.getLatestByToscaResourceName(resource.getToscaResourceName());
			if (latestByToscaName.isRight() && latestByToscaName.right().value().equals(StorageOperationStatus.NOT_FOUND))
				result = createNodeTypeByImport(resource, user, isNormative, isInTransaction);

			else {
				StorageOperationStatus status = latestByName.right().value();
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeResourceMissingError, "Create / Update resource by import", resource.getName());
				BeEcompErrorManager.getInstance().logBeComponentMissingError("Create / Update resource by import", ComponentTypeEnum.RESOURCE.getValue(), resource.getName());
				log.debug("resource already exist {}. status={}", resource.getName(), status);
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESOURCE_ALREADY_EXISTS);
				componentsUtils.auditResource(responseFormat, user, resource, "", "", AuditingActionEnum.IMPORT_RESOURCE, null);
				result = Either.right(responseFormat);
			}

		}

		// update
		else if (latestByName.isLeft()) {
			// result = updateExistingNodeTypeByImport(resource, latestByName.left().value(), user, isNormative, needLock);
		}

		// error
		else {
			StorageOperationStatus status = latestByName.right().value();
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeResourceMissingError, "Create / Update resource by import", resource.getName());
			log.debug("failed to get latest version of resource {}. status={}", resource.getName(), status);
			ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(latestByName.right().value()), resource);
			componentsUtils.auditResource(responseFormat, user, resource, "", "", AuditingActionEnum.IMPORT_RESOURCE, null);
			result = Either.right(responseFormat);
		}
		return result;

	}

	private Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> createNodeTypeByImport(Resource resource, User user, boolean isNormative, boolean isInTransaction) {
		log.debug("resource with name {} does not exist. create new resource", resource.getName());
		Either<Resource, ResponseFormat> response = validateResourceBeforeCreate(resource, user, AuditingActionEnum.IMPORT_RESOURCE, isInTransaction);
		if (response.isRight()) {
			return Either.right(response.right().value());
		}

		Either<Resource, ResponseFormat> createResponse = createResourceByDao(resource, user, AuditingActionEnum.IMPORT_RESOURCE, isNormative, isInTransaction, null);
		if (createResponse.isRight()) {
			return Either.right(createResponse.right().value());
		} else {
			ImmutablePair<Resource, ActionStatus> resourcePair = new ImmutablePair<>(createResponse.left().value(), ActionStatus.CREATED);
			ASDCKpiApi.countImportResourcesKPI();
			return Either.left(resourcePair);

		}
	}

	public Either<UiComponentDataTransfer, ResponseFormat> getUiComponentDataTransferByComponentId(String resourceId, List<String> dataParamsToReturn) {

		ComponentParametersView paramsToRetuen = new ComponentParametersView(dataParamsToReturn);
		Either<Resource, StorageOperationStatus> resourceResultEither = toscaOperationFacade.getToscaElement(resourceId, paramsToRetuen);

		if (resourceResultEither.isRight()) {
			if(resourceResultEither.right().value().equals(StorageOperationStatus.NOT_FOUND)) {
				log.debug("Failed to found resource with id {} ", resourceId);
				Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, resourceId));
			}

			log.debug("failed to get resource by id {} with filters {}", resourceId, dataParamsToReturn.toString());
			return Either.right(componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(resourceResultEither.right().value()), ""));
		}

		Resource resource = resourceResultEither.left().value();
		UiComponentDataTransfer dataTransfer = UiComponentDataConverter.getUiDataTransferFromResourceByParams(resource, dataParamsToReturn);
		return Either.left(dataTransfer);
	}

}
