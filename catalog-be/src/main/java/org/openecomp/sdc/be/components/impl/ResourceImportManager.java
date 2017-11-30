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
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.auditing.api.IAuditingManager;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum;
import org.openecomp.sdc.be.components.impl.ImportUtils.Constants;
import org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import org.openecomp.sdc.be.components.impl.ImportUtils.ToscaTagNamesEnum;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.CsarInfo;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.CapabilityTypeOperation;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.yaml.snakeyaml.Yaml;

import fj.data.Either;

@Component("resourceImportManager")
public class ResourceImportManager {

	private ServletContext servletContext;

	@Autowired
	private IAuditingManager auditingManager;

	@Autowired
	private ResourceBusinessLogic resourceBusinessLogic;

	@Autowired
	private IGraphLockOperation graphLockOperation;

	@Autowired
	protected ComponentsUtils componentsUtils;

	public final static Pattern PROPERTY_NAME_PATTERN_IGNORE_LENGTH = Pattern
			.compile("[\\w\\-\\_\\d\\:]+");
	@Autowired
	protected CapabilityTypeOperation capabilityTypeOperation;
	@Autowired
	protected ToscaOperationFacade toscaOperationFacade; 

	private ResponseFormatManager responseFormatManager;

	private static Logger log = LoggerFactory.getLogger(ResourceImportManager.class.getName());

	public void setToscaOperationFacade(ToscaOperationFacade toscaOperationFacade) {
		this.toscaOperationFacade = toscaOperationFacade;
	}

	public Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> importNormativeResource(String resourceYml, UploadResourceInfo resourceMetaData, User creator, boolean createNewVersion, boolean needLock) {

		LifecycleChangeInfoWithAction lifecycleChangeInfo = new LifecycleChangeInfoWithAction();
		lifecycleChangeInfo.setUserRemarks("certification on import");
		Function<Resource, Either<Boolean, ResponseFormat>> validator = (resource) -> resourceBusinessLogic.validatePropertiesDefaultValues(resource);

		return importCertifiedResource(resourceYml, resourceMetaData, creator, validator, lifecycleChangeInfo, false, createNewVersion, needLock, null, null, false, null, null, false);
	}
	
	public Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> importNormativeResourceFromCsar(String resourceYml, UploadResourceInfo resourceMetaData, User creator, boolean createNewVersion, boolean needLock) {

		LifecycleChangeInfoWithAction lifecycleChangeInfo = new LifecycleChangeInfoWithAction();
		lifecycleChangeInfo.setUserRemarks("certification on import");
		Function<Resource, Either<Boolean, ResponseFormat>> validator = (resource) -> resourceBusinessLogic.validatePropertiesDefaultValues(resource);

		return importCertifiedResource(resourceYml, resourceMetaData, creator, validator, lifecycleChangeInfo, false, createNewVersion, needLock, null, null, false, null, null, false);
	}

	public Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> importCertifiedResource(String resourceYml, UploadResourceInfo resourceMetaData, User creator, Function<Resource, Either<Boolean, ResponseFormat>> validationFunction,
			LifecycleChangeInfoWithAction lifecycleChangeInfo, boolean isInTransaction, boolean createNewVersion, boolean needLock, Map<ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle, List<ArtifactDefinition> nodeTypesNewCreatedArtifacts, boolean forceCertificationAllowed, CsarInfo csarInfo, String nodeName, boolean isNested) {
		Resource resource = new Resource();
		ImmutablePair<Resource, ActionStatus> responsePair = new ImmutablePair<>(resource, ActionStatus.CREATED);
		Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> response = Either.left(responsePair);

		String latestCertifiedResourceId = null;
		try {
			boolean shouldBeCertified = nodeTypeArtifactsToHandle == null || nodeTypeArtifactsToHandle.isEmpty();
			setConstantMetaData(resource, shouldBeCertified);
			setMetaDataFromJson(resourceMetaData, resource);

			Either<Boolean, ResponseFormat> validateResourceFromYaml = populateResourceFromYaml(resourceYml, resource, isInTransaction);
			if (validateResourceFromYaml.isRight()) {
				ResponseFormat validationErrorResponse = validateResourceFromYaml.right().value();
				auditErrorImport(resourceMetaData, creator, validationErrorResponse, true);
				return Either.right(validationErrorResponse);

			}

			Either<Boolean, ResponseFormat> isValidResource = validationFunction.apply(resource);
			if (isValidResource.isLeft()) {
				// The flag createNewVersion if false doesn't create new version
				if (!createNewVersion) {
					Either<Resource, StorageOperationStatus> latestByName = toscaOperationFacade.getLatestByName(resource.getName());
					if (latestByName.isLeft()) {
						return Either.right(componentsUtils.getResponseFormatByResource(ActionStatus.COMPONENT_NAME_ALREADY_EXIST, resource));
					}
				}

				response = resourceBusinessLogic.createOrUpdateResourceByImport(resource, creator, true, isInTransaction, needLock, csarInfo, nodeName, isNested);
				Either<Resource, ResponseFormat> changeStateResponse;
				if (response.isLeft()) {
					resource = response.left().value().left;
					
					if(nodeTypeArtifactsToHandle !=null && !nodeTypeArtifactsToHandle.isEmpty()){
						Either<List<ArtifactDefinition>, ResponseFormat> handleNodeTypeArtifactsRes = 
								resourceBusinessLogic.handleNodeTypeArtifacts(resource, nodeTypeArtifactsToHandle, nodeTypesNewCreatedArtifacts, creator, isInTransaction, false);
						if(handleNodeTypeArtifactsRes.isRight()){
							return Either.right(handleNodeTypeArtifactsRes.right().value());
						}
					}
					latestCertifiedResourceId = getLatestCertifiedResourceId(resource);
					changeStateResponse = resourceBusinessLogic.propagateStateToCertified(creator, resource, lifecycleChangeInfo, isInTransaction, needLock, forceCertificationAllowed);
					if (changeStateResponse.isRight()) {
						response = Either.right(changeStateResponse.right().value());
					} else {
						responsePair = new ImmutablePair<>(changeStateResponse.left().value(), response.left().value().right);
						response = Either.left(responsePair);
					}
				}
			} else {
				ResponseFormat validationErrorResponse = isValidResource.right().value();
				auditErrorImport(resourceMetaData, creator, validationErrorResponse, true);
				response = Either.right(validationErrorResponse);
			}

		} catch (RuntimeException e) {
			ResponseFormat exceptionResponse = handleImportResourceExecption(resourceMetaData, creator, true, e);
			response = Either.right(exceptionResponse);
		} finally {
			if (latestCertifiedResourceId != null && needLock) {
				log.debug("unlock resource {}", latestCertifiedResourceId);
				graphLockOperation.unlockComponent(latestCertifiedResourceId, NodeTypeEnum.Resource);
			}
		}

		return response;
	}

	private String getLatestCertifiedResourceId(Resource resource) {
		Map<String, String> allVersions = resource.getAllVersions();
		Double latestCertifiedVersion = 0.0;
		if (allVersions != null) {
			for (String version : allVersions.keySet()) {
				Double dVersion = Double.valueOf(version);
				if ((dVersion > latestCertifiedVersion) && (version.endsWith(".0"))) {
					latestCertifiedVersion = dVersion;
				}
			}
			return allVersions.get(String.valueOf(latestCertifiedVersion));
		} else {
			return null;
		}
	}

	public Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> importUserDefinedResource(String resourceYml, UploadResourceInfo resourceMetaData, User creator, boolean isReusable, boolean isInTransaction) {

		Resource resource = new Resource();
		ImmutablePair<Resource, ActionStatus> responsePair = new ImmutablePair<Resource, ActionStatus>(resource, ActionStatus.CREATED);
		Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> response = Either.left(responsePair);

		try {
			setMetaDataFromJson(resourceMetaData, resource);

			Either<Boolean, ResponseFormat> validateResourceFromYaml = populateResourceFromYaml(resourceYml, resource, isInTransaction);
			if (validateResourceFromYaml.isRight()) {
				ResponseFormat validationErrorResponse = validateResourceFromYaml.right().value();
				auditErrorImport(resourceMetaData, creator, validationErrorResponse, false);
				return Either.right(validationErrorResponse);

			}

			// currently import VF isn't supported. In future will be supported
			// import VF only with CSER file!!
			if (ResourceTypeEnum.VF.equals(resource.getResourceType())) {
				log.debug("Now import VF isn't supported. It will be supported in future with CSER file only");
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
			}

			Either<Boolean, ResponseFormat> validateDerivedFromNotEmpty = resourceBusinessLogic.validateDerivedFromNotEmpty(creator, resource, AuditingActionEnum.CREATE_RESOURCE);
			if (validateDerivedFromNotEmpty.isRight()) {
				return Either.right(validateDerivedFromNotEmpty.right().value());
			}

			Either<Boolean, ResponseFormat> validatePropertiesTypes = resourceBusinessLogic.validatePropertiesDefaultValues(resource);

			if (validatePropertiesTypes.isLeft()) {
				response = resourceBusinessLogic.createOrUpdateResourceByImport(resource, creator, false, isInTransaction, true, null, null, false);
			} else {
				ResponseFormat validationErrorResponse = validatePropertiesTypes.right().value();
				auditErrorImport(resourceMetaData, creator, validationErrorResponse, false);
				response = Either.right(validationErrorResponse);
			}

		} catch (RuntimeException e) {
			ResponseFormat exceptionResponse = handleImportResourceExecption(resourceMetaData, creator, false, e);
			response = Either.right(exceptionResponse);
		}

		return response;

	}

	Either<Boolean, ResponseFormat> populateResourceFromYaml(String resourceYml, Resource resource, boolean inTransaction) {
		@SuppressWarnings("unchecked")
		Either<Boolean, ResponseFormat> eitherResult = Either.left(true);
		Map<String, Object> toscaJsonAll = (Map<String, Object>) new Yaml().load(resourceYml);
		Map<String, Object> toscaJson = toscaJsonAll;

		// Checks if exist and builds the node_types map
		if (toscaJsonAll.containsKey(ToscaTagNamesEnum.NODE_TYPES.getElementName()) && resource.getResourceType()!=ResourceTypeEnum.CVFC) {
			toscaJson = new HashMap<String, Object>();
			toscaJson.put(ToscaTagNamesEnum.NODE_TYPES.getElementName(), toscaJsonAll.get(ToscaTagNamesEnum.NODE_TYPES.getElementName()));
		}
		// Derived From
		Either<Resource, ResponseFormat> setDerivedFrom = setDerivedFrom(toscaJson, resource, inTransaction);
		if (setDerivedFrom.isRight()) {
			return Either.right(setDerivedFrom.right().value());
		}
		Resource parentResource = setDerivedFrom.left().value();
		if(StringUtils.isEmpty(resource.getToscaResourceName()))
			setToscaResourceName(toscaJson, resource);
		setAttributes(toscaJson, resource);
		eitherResult = setCapabilities(toscaJson, resource, parentResource);
		if (eitherResult.isRight())
			return eitherResult;
		eitherResult = setProperties(toscaJson, resource);
		if (eitherResult.isRight())
			return eitherResult;
		eitherResult = setRequirements(toscaJson, resource, parentResource);
		if (eitherResult.isRight())
			return eitherResult;
		setInterfaceLifecycle(toscaJson, resource);

		return eitherResult;
	}

	private void setToscaResourceName(Map<String, Object> toscaJson, Resource resource) {
		Either<Map<String, Object>, ResultStatusEnum> toscaElement = ImportUtils.findFirstToscaMapElement(toscaJson, ToscaTagNamesEnum.NODE_TYPES);
		if (toscaElement.isLeft() || toscaElement.left().value().size() == 1) {
			String toscaResourceName = toscaElement.left().value().keySet().iterator().next();
			resource.setToscaResourceName(toscaResourceName);
		}
	}

	private void setInterfaceLifecycle(Map<String, Object> toscaJson, Resource resource) {
		Either<Map<String, Object>, ResultStatusEnum> toscaInterfaces = ImportUtils.findFirstToscaMapElement(toscaJson, ToscaTagNamesEnum.INTERFACES);
		if (toscaInterfaces.isLeft()) {
			Map<String, Object> jsonInterfaces = toscaInterfaces.left().value();
			Map<String, InterfaceDefinition> moduleInterfaces = new HashMap<String, InterfaceDefinition>();
			Iterator<Entry<String, Object>> interfacesNameValue = jsonInterfaces.entrySet().iterator();
			while (interfacesNameValue.hasNext()) {
				Entry<String, Object> interfaceNameValue = interfacesNameValue.next();
				Either<InterfaceDefinition, ResultStatusEnum> eitherInterface = createModuleInterface(interfaceNameValue.getValue());
				if (eitherInterface.isRight()) {
					log.info("error when creating interface:{}, for resource:{}", interfaceNameValue.getKey(), resource.getName());
				} else {
					moduleInterfaces.put(interfaceNameValue.getKey(), eitherInterface.left().value());
				}

			}
			if (moduleInterfaces.size() > 0) {
				resource.setInterfaces(moduleInterfaces);
			}
		}
	}

	private Either<InterfaceDefinition, ResultStatusEnum> createModuleInterface(Object interfaceJson) {
		InterfaceDefinition interf = new InterfaceDefinition();
		Either<InterfaceDefinition, ResultStatusEnum> result = Either.left(interf);

		try {
			if (interfaceJson instanceof String) {
				String requirementJsonString = (String) interfaceJson;
				interf.setType(requirementJsonString);
			} else if (interfaceJson instanceof Map) {
				Map<String, Object> requirementJsonMap = (Map<String, Object>) interfaceJson;
				if (requirementJsonMap.containsKey(ToscaTagNamesEnum.TYPE.getElementName())) {
					String type = (String) requirementJsonMap.get(ToscaTagNamesEnum.TYPE.getElementName());
					interf.setType(type);
					interf.setUniqueId(type.toLowerCase());
				}
			} else {
				result = Either.right(ResultStatusEnum.GENERAL_ERROR);
			}

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeSystemError, "Import Resource- create interface");
			BeEcompErrorManager.getInstance().logBeSystemError("Import Resource- create interface");
			log.debug("error when creating interface, message:{}", e.getMessage(), e);
			result = Either.right(ResultStatusEnum.GENERAL_ERROR);
		}

		return result;
	}

	private Either<Boolean, ResponseFormat> setRequirements(Map<String, Object> toscaJson, Resource resource, Resource parentResource) {// Note that parentResource can be null
		Either<Boolean, ResponseFormat> eitherResult = Either.left(true);
		Either<List<Object>, ResultStatusEnum> toscaRequirements = ImportUtils.findFirstToscaListElement(toscaJson, ToscaTagNamesEnum.REQUIREMENTS);
		if (toscaRequirements.isLeft()) {
			List<Object> jsonRequirements = toscaRequirements.left().value();
			Map<String, List<RequirementDefinition>> moduleRequirements = new HashMap<String, List<RequirementDefinition>>();
			// Checking for name duplication
			Set<String> reqNames = new HashSet<>();
			// Getting flattened list of capabilities of parent node - cap name
			// to cap type
			Either<Map<String, String>, ResponseFormat> reqName2Type = getReqName2Type(parentResource);
			if (reqName2Type.isRight()) {
				ResponseFormat responseFormat = reqName2Type.right().value();
				log.debug("Error during setting requirements of imported resource: {}", responseFormat);
				return Either.right(responseFormat);
			}
			Map<String, String> reqName2TypeMap = reqName2Type.left().value();
			for (Object jsonRequirementObj : jsonRequirements) {
				// Requirement
				Map<String, Object> requirementJsonWrapper = (Map<String, Object>) jsonRequirementObj;
				String requirementName = requirementJsonWrapper.keySet().iterator().next();
				String reqNameLowerCase = requirementName.toLowerCase();
				if (reqNames.contains(reqNameLowerCase)) {
					log.debug("More than one requirement with same name {} (case-insensitive) in imported TOSCA file is invalid", reqNameLowerCase);
					return Either.right(componentsUtils.getResponseFormat(ActionStatus.IMPORT_DUPLICATE_REQ_CAP_NAME, "requirement", reqNameLowerCase));
				}
				reqNames.add(reqNameLowerCase);
				Either<RequirementDefinition, ResponseFormat> eitherRequirement = createRequirementFromImportFile(requirementJsonWrapper.get(requirementName));
				if (eitherRequirement.isRight()) {
					log.info("error when creating Requirement:{}, for resource:{}", requirementName, resource.getName());
					return Either.right(eitherRequirement.right().value());
				}
				RequirementDefinition requirementDef = eitherRequirement.left().value();
				requirementDef.setName(requirementName);
				if (moduleRequirements.containsKey(requirementDef.getCapability())) {
					moduleRequirements.get(requirementDef.getCapability()).add(requirementDef);
				} else {
					List<RequirementDefinition> list = new ArrayList<RequirementDefinition>();
					list.add(requirementDef);
					moduleRequirements.put(requirementDef.getCapability(), list);
				}

				// Validating against req/cap of "derived from" node
				Either<Boolean, ResponseFormat> validateVsParentCap = validateCapNameVsDerived(reqName2TypeMap, requirementDef.getCapability(), requirementDef.getName());
				if (validateVsParentCap.isRight()) {
					return Either.right(validateVsParentCap.right().value());
				}
				if (!validateVsParentCap.left().value()) {
					log.debug("Requirement with name {} already exists in parent {}", requirementDef.getName(), parentResource.getName());
					ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.IMPORT_REQ_CAP_NAME_EXISTS_IN_DERIVED, "requirement", requirementDef.getName().toLowerCase(), parentResource.getName());
					return Either.right(responseFormat);
				}
			}
			if (moduleRequirements.size() > 0) {
				resource.setRequirements(moduleRequirements);
			}

		}
		return eitherResult;

	}

	private Either<RequirementDefinition, ResponseFormat> createRequirementFromImportFile(Object requirementJson) {
		RequirementDefinition requirement = new RequirementDefinition();
		Either<RequirementDefinition, ResponseFormat> result = Either.left(requirement);

		try {
			if (requirementJson instanceof String) {
				String requirementJsonString = (String) requirementJson;
				requirement.setCapability(requirementJsonString);
			} else if (requirementJson instanceof Map) {
				Map<String, Object> requirementJsonMap = (Map<String, Object>) requirementJson;
				if (requirementJsonMap.containsKey(ToscaTagNamesEnum.CAPABILITY.getElementName())) {
					requirement.setCapability((String) requirementJsonMap.get(ToscaTagNamesEnum.CAPABILITY.getElementName()));
				}

				if (requirementJsonMap.containsKey(ToscaTagNamesEnum.NODE.getElementName())) {
					requirement.setNode((String) requirementJsonMap.get(ToscaTagNamesEnum.NODE.getElementName()));
				}

				if (requirementJsonMap.containsKey(ToscaTagNamesEnum.RELATIONSHIP.getElementName())) {
					requirement.setRelationship((String) requirementJsonMap.get(ToscaTagNamesEnum.RELATIONSHIP.getElementName()));
				}
				if (requirementJsonMap.containsKey(ToscaTagNamesEnum.OCCURRENCES.getElementName())) {
					List<Object> occurrencesList = (List) requirementJsonMap.get(ToscaTagNamesEnum.OCCURRENCES.getElementName());
					Either<Boolean, ResponseFormat> validateAndSetOccurrencesStatus = validateOccurrences(occurrencesList);
					if (validateAndSetOccurrencesStatus.isRight()) {
						result = Either.right(validateAndSetOccurrencesStatus.right().value());
						return result;
					}
					if (validateAndSetOccurrencesStatus.left().value() == true) {
						requirement.setMinOccurrences(occurrencesList.get(0).toString());
						requirement.setMaxOccurrences(occurrencesList.get(1).toString());
					}

				}
			} else {
				result = Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_YAML));
			}

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeSystemError, "Import Resource - create Requirement");
			BeEcompErrorManager.getInstance().logBeSystemError("Import Resource - create Requirement");
			log.debug("error when creating requirement, message:{}", e.getMessage(), e);
			result = Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_YAML));
		}

		return result;
	}

	private Either<Boolean, ResponseFormat> setProperties(Map<String, Object> toscaJson, Resource resource) {
		Map<String, Object> reducedToscaJson = new HashMap<>(toscaJson);
		ImportUtils.removeElementFromJsonMap(reducedToscaJson, "capabilities");
		Either<Boolean, ResponseFormat> result = Either.left(true);
		Either<Map<String, PropertyDefinition>, ResultStatusEnum> properties = ImportUtils.getProperties(reducedToscaJson);
		if (properties.isLeft()) {
			List<PropertyDefinition> propertiesList = new ArrayList<>();
			Map<String, PropertyDefinition> value = properties.left().value();
			if (value != null) {
				for (Entry<String, PropertyDefinition> entry : value.entrySet()) {
					String name = entry.getKey();
					if(!PROPERTY_NAME_PATTERN_IGNORE_LENGTH.matcher(name).matches()){
						log.debug("The property with invalid name {} occured upon import resource {}. ", name, resource.getName());
						result = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromResultStatusEnum(ResultStatusEnum.INVALID_PROPERTY_NAME, JsonPresentationFields.PROPERTY)));
					}
					PropertyDefinition propertyDefinition = entry.getValue();
					propertyDefinition.setName(name);
					propertiesList.add(propertyDefinition);
				}
			}
			resource.setProperties(propertiesList);
		} else if(properties.right().value() != ResultStatusEnum.ELEMENT_NOT_FOUND){
			result = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromResultStatusEnum(properties.right().value(), JsonPresentationFields.PROPERTY)));
		}
		return result;
	}

	private ResultStatusEnum setAttributes(Map<String, Object> toscaJson, Resource resource) {
		ResultStatusEnum result = ResultStatusEnum.OK;
		Either<Map<String, PropertyDefinition>, ResultStatusEnum> attributes = ImportUtils.getAttributes(toscaJson);
		if (attributes.isLeft()) {
			List<PropertyDefinition> attributeList = new ArrayList<>();
			Map<String, PropertyDefinition> value = attributes.left().value();
			if (value != null) {
				for (Entry<String, PropertyDefinition> entry : value.entrySet()) {
					String name = entry.getKey();
					PropertyDefinition attributeDef = entry.getValue();
					attributeDef.setName(name);
					attributeList.add(attributeDef);
				}
			}
			resource.setAttributes(attributeList);
		} else {
			result = attributes.right().value();
		}
		return result;
	}

	private Either<Resource, ResponseFormat> setDerivedFrom(Map<String, Object> toscaJson, Resource resource, boolean inTransaction) {
		Either<String, ResultStatusEnum> toscaDerivedFromElement = ImportUtils.findFirstToscaStringElement(toscaJson, ToscaTagNamesEnum.DERIVED_FROM);
		Resource derivedFromResource = null;
		if (toscaDerivedFromElement.isLeft()) {
			String derivedFrom = toscaDerivedFromElement.left().value();
			log.debug("Derived from TOSCA name is {}", derivedFrom);
			resource.setDerivedFrom(Arrays.asList(new String[] { derivedFrom }));
			Either<Resource, StorageOperationStatus> latestByToscaResourceName = toscaOperationFacade.getLatestByToscaResourceName(derivedFrom);
			
			if (latestByToscaResourceName.isRight()) {
				StorageOperationStatus operationStatus = latestByToscaResourceName.right().value();
				if (operationStatus.equals(StorageOperationStatus.NOT_FOUND)) {
					operationStatus = StorageOperationStatus.PARENT_RESOURCE_NOT_FOUND;
				}
				log.debug("Error when fetching parent resource {}, error: {}", derivedFrom, operationStatus);
				ActionStatus convertFromStorageResponse = componentsUtils.convertFromStorageResponse(operationStatus);
				BeEcompErrorManager.getInstance().logBeComponentMissingError("Import TOSCA YAML", "resource", derivedFrom);
				return Either.right(componentsUtils.getResponseFormat(convertFromStorageResponse, derivedFrom));
			}
			derivedFromResource = latestByToscaResourceName.left().value();
		}
		return Either.left(derivedFromResource);
	}

	private Either<Boolean, ResponseFormat> setCapabilities(Map<String, Object> toscaJson, Resource resource, Resource parentResource) {// Note that parentResource can be null
		Either<Boolean, ResponseFormat> eitherResult = Either.left(true);
		Either<Map<String, Object>, ResultStatusEnum> toscaCapabilities = ImportUtils.findFirstToscaMapElement(toscaJson, ToscaTagNamesEnum.CAPABILITIES);
		if (toscaCapabilities.isLeft()) {
			Map<String, Object> jsonCapabilities = toscaCapabilities.left().value();
			Map<String, List<CapabilityDefinition>> moduleCapabilities = new HashMap<String, List<CapabilityDefinition>>();
			Iterator<Entry<String, Object>> capabilitiesNameValue = jsonCapabilities.entrySet().iterator();
			Set<String> capNames = new HashSet<>();
			// Getting flattened list of capabilities of parent node - cap name
			// to cap type
			Either<Map<String, String>, ResponseFormat> capName2Type = getCapName2Type(parentResource);
			if (capName2Type.isRight()) {
				ResponseFormat responseFormat = capName2Type.right().value();
				log.debug("Error during setting capabilities of imported resource: {}", responseFormat);
				return Either.right(responseFormat);
			}
			Map<String, String> capName2TypeMap = capName2Type.left().value();
			while (capabilitiesNameValue.hasNext()) {
				Entry<String, Object> capabilityNameValue = capabilitiesNameValue.next();

				// Validating that no req/cap duplicates exist in imported YAML
				String capNameLowerCase = capabilityNameValue.getKey().toLowerCase();
				if (capNames.contains(capNameLowerCase)) {
					log.debug("More than one capability with same name {} (case-insensitive) in imported TOSCA file is invalid", capNameLowerCase);
					return Either.right(componentsUtils.getResponseFormat(ActionStatus.IMPORT_DUPLICATE_REQ_CAP_NAME, "capability", capNameLowerCase));
				}
				capNames.add(capNameLowerCase);

				Either<CapabilityDefinition, ResponseFormat> eitherCapability = createCapabilityFromImportFile(capabilityNameValue.getValue());
				if (eitherCapability.isRight()) {
					log.debug("error when creating capability:{}, for resource:{}", capabilityNameValue.getKey(), resource.getName());
					return Either.right(eitherCapability.right().value());
				}

				CapabilityDefinition capabilityDef = eitherCapability.left().value();
				capabilityDef.setName(capabilityNameValue.getKey());
				if (moduleCapabilities.containsKey(capabilityDef.getType())) {
					moduleCapabilities.get(capabilityDef.getType()).add(capabilityDef);
				} else {
					List<CapabilityDefinition> list = new ArrayList<CapabilityDefinition>();
					list.add(capabilityDef);
					moduleCapabilities.put(capabilityDef.getType(), list);
				}

				// Validating against req/cap of "derived from" node
				Either<Boolean, ResponseFormat> validateVsParentCap = validateCapNameVsDerived(capName2TypeMap, capabilityDef.getType(), capabilityDef.getName());
				if (validateVsParentCap.isRight()) {
					return Either.right(validateVsParentCap.right().value());
				}
				if (!validateVsParentCap.left().value()) {
					// Here parentResource is for sure not null, so it's
					// null-safe
					log.debug("Capability with name {} already exists in parent {}", capabilityDef.getName(), parentResource.getName());
					ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.IMPORT_REQ_CAP_NAME_EXISTS_IN_DERIVED, "capability", capabilityDef.getName().toLowerCase(), parentResource.getName());
					return Either.right(responseFormat);
				}
			}
			if (moduleCapabilities.size() > 0) {
				resource.setCapabilities(moduleCapabilities);
			}
		}

		return eitherResult;

	}

	private Either<Map<String, String>, ResponseFormat> getCapName2Type(Resource parentResource) {
		Map<String, String> capName2type = new HashMap<>();
		if (parentResource != null) {
			Map<String, List<CapabilityDefinition>> capabilities = parentResource.getCapabilities();
			if (capabilities != null) {
				for (List<CapabilityDefinition> capDefinitions : capabilities.values()) {
					for (CapabilityDefinition capDefinition : capDefinitions) {
						String nameLowerCase = capDefinition.getName().toLowerCase();
						if (capName2type.get(nameLowerCase) != null) {
							String parentResourceName = parentResource.getName();
							log.debug("Resource with name {} has more than one capability with name {}, ignoring case", parentResourceName, nameLowerCase);
							BeEcompErrorManager.getInstance().logInternalDataError("Import resource", "Parent resource " + parentResourceName + " of imported resource has one or more capabilities with name " + nameLowerCase, ErrorSeverity.ERROR);
							return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
						}
						capName2type.put(nameLowerCase, capDefinition.getType());
					}
				}
			}
		}
		return Either.left(capName2type);
	}

	private Either<Map<String, String>, ResponseFormat> getReqName2Type(Resource parentResource) {
		Map<String, String> reqName2type = new HashMap<>();
		if (parentResource != null) {
			Map<String, List<RequirementDefinition>> requirements = parentResource.getRequirements();
			if (requirements != null) {
				for (List<RequirementDefinition> reqDefinitions : requirements.values()) {
					for (RequirementDefinition reqDefinition : reqDefinitions) {
						String nameLowerCase = reqDefinition.getName().toLowerCase();
						if (reqName2type.get(nameLowerCase) != null) {
							String parentResourceName = parentResource.getName();
							log.debug("Resource with name {} has more than one requirement with name {}, ignoring case", parentResourceName, nameLowerCase);
							BeEcompErrorManager.getInstance().logInternalDataError("Import resource", "Parent resource " + parentResourceName + " of imported resource has one or more requirements with name " + nameLowerCase, ErrorSeverity.ERROR);
							return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
						}
						reqName2type.put(nameLowerCase, reqDefinition.getCapability());
					}
				}
			}
		}
		return Either.left(reqName2type);
	}

	private Either<Boolean, ResponseFormat> validateCapNameVsDerived(Map<String, String> parentCapName2Type, String childCapabilityType, String reqCapName) {
		String capNameLowerCase = reqCapName.toLowerCase();
		log.trace("Validating capability {} vs parent resource", capNameLowerCase);
		String parentCapType = parentCapName2Type.get(capNameLowerCase);
		if (parentCapType != null) {
			if (childCapabilityType.equals(parentCapType)) {
				log.debug("Capability with name {} is of same type {} for imported resource and its parent - this is OK", capNameLowerCase, childCapabilityType);
				return Either.left(true);
			}
			Either<Boolean, StorageOperationStatus> capabilityTypeDerivedFrom = capabilityTypeOperation.isCapabilityTypeDerivedFrom(childCapabilityType, parentCapType);
			if (capabilityTypeDerivedFrom.isRight()) {
				log.debug("Couldn't check whether imported resource capability derives from its parent's capability");
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(capabilityTypeDerivedFrom.right().value()));
				return Either.right(responseFormat);
			}
			return Either.left(capabilityTypeDerivedFrom.left().value());
		}
		return Either.left(true);
	}

	private Either<CapabilityDefinition, ResponseFormat> createCapabilityFromImportFile(Object capabilityJson) {

		CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
		Either<CapabilityDefinition, ResponseFormat> result = Either.left(capabilityDefinition);

		try {
			if (capabilityJson instanceof String) {
				String capabilityJsonString = (String) capabilityJson;
				capabilityDefinition.setType(capabilityJsonString);
			} else if (capabilityJson instanceof Map) {
				Map<String, Object> capabilityJsonMap = (Map<String, Object>) capabilityJson;
				// Type
				if (capabilityJsonMap.containsKey(ToscaTagNamesEnum.TYPE.getElementName())) {
					capabilityDefinition.setType((String) capabilityJsonMap.get(ToscaTagNamesEnum.TYPE.getElementName()));
				}
				// ValidSourceTypes
				if (capabilityJsonMap.containsKey(ToscaTagNamesEnum.VALID_SOURCE_TYPES.getElementName())) {
					capabilityDefinition.setValidSourceTypes((List<String>) capabilityJsonMap.get(ToscaTagNamesEnum.VALID_SOURCE_TYPES.getElementName()));
				}
				// ValidSourceTypes
				if (capabilityJsonMap.containsKey(ToscaTagNamesEnum.DESCRIPTION.getElementName())) {
					capabilityDefinition.setDescription((String) capabilityJsonMap.get(ToscaTagNamesEnum.DESCRIPTION.getElementName()));
				}
				if (capabilityJsonMap.containsKey(ToscaTagNamesEnum.OCCURRENCES.getElementName())) {
					List<Object> occurrencesList = (List) capabilityJsonMap.get(ToscaTagNamesEnum.OCCURRENCES.getElementName());
					Either<Boolean, ResponseFormat> validateAndSetOccurrencesStatus = validateOccurrences(occurrencesList);
					if (validateAndSetOccurrencesStatus.isRight()) {
						result = Either.right(validateAndSetOccurrencesStatus.right().value());
						return result;
					}
					if (validateAndSetOccurrencesStatus.left().value() == true) {
						capabilityDefinition.setMinOccurrences(occurrencesList.get(0).toString());
						capabilityDefinition.setMaxOccurrences(occurrencesList.get(1).toString());
					}
				}
				if (capabilityJsonMap.containsKey(ToscaTagNamesEnum.PROPERTIES.getElementName())) {

					Either<Map<String, PropertyDefinition>, ResultStatusEnum> propertiesRes = ImportUtils.getProperties(capabilityJsonMap);
					if (propertiesRes.isRight()) {
						result = Either.right(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND));
						return result;
					} else {
						propertiesRes.left().value().entrySet().stream().forEach(e -> e.getValue().setName(e.getKey().toLowerCase()));
						List<ComponentInstanceProperty> capabilityProperties = propertiesRes.left().value().values().stream().map(p -> new ComponentInstanceProperty(p, p.getDefaultValue(), null)).collect(Collectors.toList());
						capabilityDefinition.setProperties(capabilityProperties);
					}
				}
			} else if (!(capabilityJson instanceof List)) {

				result = Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_YAML));

			}
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeSystemError, "Import Resource - create capability");
			BeEcompErrorManager.getInstance().logBeSystemError("Import Resource - create capability");
			log.debug("error when creating capability, message:{}", e.getMessage(), e);
			result = Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_YAML));
		}

		return result;
	}

	private ResponseFormat handleImportResourceExecption(UploadResourceInfo resourceMetaData, User user, boolean isNormative, RuntimeException e) {
		String payloadName = (resourceMetaData != null) ? resourceMetaData.getPayloadName() : "";
		BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeSystemError, "Import Resource " + payloadName);
		BeEcompErrorManager.getInstance().logBeSystemError("Import Resource " + payloadName);

		log.debug("Error when importing resource from payload:{} Exception text: {}", payloadName, e.getMessage(), e);
		ResponseFormat errorResponseWrapper = getResponseFormatManager().getResponseFormat(ActionStatus.GENERAL_ERROR);
		auditErrorImport(resourceMetaData, user, errorResponseWrapper, isNormative);
		return errorResponseWrapper;
	}

	private void auditErrorImport(UploadResourceInfo resourceMetaData, User user, ResponseFormat errorResponseWrapper, boolean isNormative) {
		EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<>(AuditingFieldsKeysEnum.class);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, AuditingActionEnum.IMPORT_RESOURCE.getName());
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceMetaData.getName());
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE, ComponentTypeEnum.RESOURCE.getValue());
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_PREV_VERSION, "");
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, user.getUserId());
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_PREV_STATE, "");
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_INVARIANT_UUID, "");
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, errorResponseWrapper.getStatus());
		String message = "";
		if (errorResponseWrapper.getMessageId() != null) {
			message = errorResponseWrapper.getMessageId() + ": ";
		}
		message += errorResponseWrapper.getFormattedMessage();
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, message);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_NAME, user.getFirstName() + " " + user.getLastName());

		String version, lifeCycleState;
		if (isNormative) {
			version = Constants.FIRST_CERTIFIED_VERSION_VERSION;
			lifeCycleState = LifecycleStateEnum.CERTIFIED.name();
		} else {
			version = "";
			lifeCycleState = LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name();

		}
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION, version);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_STATE, lifeCycleState);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TOSCA_NODE_TYPE, "");

		getAuditingManager().auditEvent(auditingFields);
	}

	private void setMetaDataFromJson(UploadResourceInfo resourceMetaData, Resource resource) {
		resource.setTags(resourceMetaData.getTags());
		List<CategoryDefinition> categories = resourceMetaData.getCategories();
		resource.setCategories(categories);
		resource.setDescription(resourceMetaData.getDescription());
		resource.setIcon(resourceMetaData.getResourceIconPath());
		resource.setName(resourceMetaData.getName());
		if (categories != null && !categories.isEmpty()) {
			CategoryDefinition categoryDef = categories.get(0);
			resource.setAbstract(false);
			if (categoryDef != null && categoryDef.getName() != null && categoryDef.getName().equals(Constants.ABSTRACT_CATEGORY_NAME)) {
				SubCategoryDefinition subCategoryDef = categoryDef.getSubcategories().get(0);
				if (subCategoryDef != null && subCategoryDef.getName().equals(Constants.ABSTRACT_SUBCATEGORY)) {
					resource.setAbstract(true);
				}
			}
		}
		resource.setContactId(resourceMetaData.getContactId());
		resource.setCreatorUserId(resourceMetaData.getContactId());

		if (resourceMetaData.getVendorName() != null) {
			resource.setVendorName(resourceMetaData.getVendorName());
		}

		if (resourceMetaData.getVendorRelease() != null) {
			resource.setVendorRelease(resourceMetaData.getVendorRelease());
		}

		resource.setResourceType(ResourceTypeEnum.valueOf(resourceMetaData.getResourceType()));

	}

	private void setConstantMetaData(Resource resource, boolean shouldBeCertified) {
		String version;
		LifecycleStateEnum state;
		if(shouldBeCertified){
			version = ImportUtils.Constants.FIRST_CERTIFIED_VERSION_VERSION;
			state = ImportUtils.Constants.NORMATIVE_TYPE_LIFE_CYCLE;
		}else{
			version = ImportUtils.Constants.FIRST_NON_CERTIFIED_VERSION;
			state = ImportUtils.Constants.NORMATIVE_TYPE_LIFE_CYCLE_NOT_CERTIFIED_CHECKOUT;
		}
		resource.setVersion(version);
		resource.setLifecycleState(state);
		resource.setHighestVersion(ImportUtils.Constants.NORMATIVE_TYPE_HIGHEST_VERSION);
		resource.setVendorName(ImportUtils.Constants.VENDOR_NAME);
		resource.setVendorRelease(ImportUtils.Constants.VENDOR_RELEASE);

	}

	private Either<Boolean, ResponseFormat> validateOccurrences(List<Object> occurrensesList) {

		if (!ValidationUtils.validateListNotEmpty(occurrensesList)) {
			log.debug("Occurrenses list empty");
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_OCCURRENCES);
			return Either.right(responseFormat);
		}

		if (occurrensesList.size() < 2) {
			log.debug("Occurrenses list size not 2");
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_OCCURRENCES);
			return Either.right(responseFormat);
		}
		Object minObj = occurrensesList.get(0);
		Object maxObj = occurrensesList.get(1);
		Integer minOccurrences = null;
		Integer maxOccurrences = null;
		if (minObj instanceof Integer)
			minOccurrences = (Integer) minObj;
		else {
			log.debug("Invalid occurrenses format. low_bound occurrense must be Integer {}", minObj);
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_OCCURRENCES);
			return Either.right(responseFormat);
		}
		if (minOccurrences < 0) {
			log.debug("Invalid occurrenses format.low_bound occurrense negative {}", minOccurrences);
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_OCCURRENCES);
			return Either.right(responseFormat);
		}

		if (maxObj instanceof String) {
			if (maxObj.equals("UNBOUNDED")) {
				return Either.left(true);
			} else {
				log.debug("Invalid occurrenses format. Max occurrence is {}", maxObj);
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_OCCURRENCES);
				return Either.right(responseFormat);
			}
		} else {
			if (maxObj instanceof Integer)
				maxOccurrences = (Integer) maxObj;
			else {
				log.debug("Invalid occurrenses format.  Max occurrence is {}", maxObj);
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_OCCURRENCES);
				return Either.right(responseFormat);
			}

			if (maxOccurrences <= 0 || maxOccurrences < minOccurrences) {
				log.debug("Invalid occurrenses format.  min occurrence is {}, Max occurrence is {}", minOccurrences, maxOccurrences);
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_OCCURRENCES);
				return Either.right(responseFormat);
			}
		}

		return Either.left(true);

	}

	public void init(ServletContext servletContext) {
		if (this.servletContext == null) {
			synchronized (this) {
				if (this.servletContext == null) {
					this.servletContext = servletContext;
					responseFormatManager = ResponseFormatManager.getInstance();
					resourceBusinessLogic = getResourceBL(servletContext);
				}
			}
		}
	}

	public boolean isResourceExist(String resourceName) {
		return resourceBusinessLogic.isResourceExist(resourceName);
	}

	private ResourceBusinessLogic getResourceBL(ServletContext context) {
		WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(org.openecomp.sdc.common.api.Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
		WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
		ResourceBusinessLogic resourceBl = webApplicationContext.getBean(ResourceBusinessLogic.class);
		return resourceBl;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

	public IAuditingManager getAuditingManager() {
		return auditingManager;
	}

	public ResponseFormatManager getResponseFormatManager() {
		return responseFormatManager;
	}

	public void setResponseFormatManager(ResponseFormatManager responseFormatManager) {
		this.responseFormatManager = responseFormatManager;
	}

	public ResourceBusinessLogic getResourceBusinessLogic() {
		return resourceBusinessLogic;
	}

	public void setResourceBusinessLogic(ResourceBusinessLogic resourceBusinessLogic) {
		this.resourceBusinessLogic = resourceBusinessLogic;
	}

	public Logger getLog() {
		return log;
	}

	public static void setLog(Logger log) {
		ResourceImportManager.log = log;
	}

	public IGraphLockOperation getGraphLockOperation() {
		return graphLockOperation;
	}

	public void setGraphLockOperation(IGraphLockOperation graphLockOperation) {
		this.graphLockOperation = graphLockOperation;
	}

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public void setAuditingManager(IAuditingManager auditingManager) {
		this.auditingManager = auditingManager;
	}


}
