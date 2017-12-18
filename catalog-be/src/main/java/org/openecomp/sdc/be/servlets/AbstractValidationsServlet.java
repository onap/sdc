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

package org.openecomp.sdc.be.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.zip.ZipInputStream;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.codehaus.jackson.map.ObjectMapper;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.CsarValidationUtils;
import org.openecomp.sdc.be.components.impl.ImportUtils;
import org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import org.openecomp.sdc.be.components.impl.ImportUtils.ToscaElementTypeEnum;
import org.openecomp.sdc.be.components.impl.ImportUtils.ToscaTagNamesEnum;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.servlets.ResourceUploadServlet.ResourceAuthorityTypeEnum;
import org.openecomp.sdc.be.user.IUserBusinessLogic;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.api.UploadArtifactInfo;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.common.util.YamlToObjectConverter;
import org.openecomp.sdc.common.util.ZipUtil;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import fj.data.Either;

public abstract class AbstractValidationsServlet extends BeGenericServlet {

	@Resource
	private ServletUtils servletUtils;

	@Resource
	private ResourceImportManager resourceImportManager;

	@Autowired
	protected ComponentsUtils componentsUtils;

	private Logger log = null;

	protected void init(Logger log) {
		initLog(log);
		initSpringFromContext();

	}

	protected synchronized void initLog(Logger log) {
		if (this.log == null) {
			this.log = log;
		}
	}

	private synchronized void initSpringFromContext() {
		if (servletUtils == null) {
			ServletContext context = servletRequest.getSession().getServletContext();
			WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context
					.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
			WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
			servletUtils = webApplicationContext.getBean(ServletUtils.class);
			resourceImportManager = webApplicationContext.getBean(ResourceImportManager.class);
		}
	}

	protected void validateResourceDoesNotExist(Wrapper<Response> responseWrapper, User user, String resourceName) {
		if (resourceImportManager.isResourceExist(resourceName)) {
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.RESOURCE_ALREADY_EXISTS);
			Response errorResponse = buildErrorResponse(responseFormat);
			EnumMap<AuditingFieldsKeysEnum, Object> additionalParam = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
			additionalParam.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceName);
			getComponentsUtils().auditResource(responseFormat, user, null, "", "", AuditingActionEnum.IMPORT_RESOURCE, additionalParam);
			responseWrapper.setInnerElement(errorResponse);
		}
	}

	protected void validateUserExist(Wrapper<Response> responseWrapper, Wrapper<User> userWrapper, String userUserId) {
		log.debug("get user {} from DB", userUserId);
		// get user details
		if (userUserId == null) {
			log.info("user userId is null");
			Response response = returnMissingInformation(new User());
			responseWrapper.setInnerElement(response);
		}

		else {
			IUserBusinessLogic userAdmin = getServletUtils().getUserAdmin();
			Either<User, ActionStatus> eitherCreator = userAdmin.getUser(userUserId, false);
			if (eitherCreator.isRight()) {
				log.info("user is not listed. userId={}", userUserId);
				User user = new User();
				user.setUserId(userUserId);
				Response response = returnMissingInformation(user);
				responseWrapper.setInnerElement(response);
			} else {
				userWrapper.setInnerElement(eitherCreator.left().value());
			}
		}
	}

	protected Response returnMissingInformation(User user) {
		ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_INFORMATION);
		getComponentsUtils().auditResource(responseFormat, user, null, "", "", AuditingActionEnum.IMPORT_RESOURCE, null);
		return buildErrorResponse(responseFormat);
	}

	protected void validateDataNotNull(Wrapper<Response> responseWrapper, Object... dataParams) {
		for (Object dataElement : dataParams) {
			if (dataElement == null) {
				log.info("Invalid body was received.");
				Response response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
				responseWrapper.setInnerElement(response);
				break;
			}
		}

	}

	protected void validateUserRole(Wrapper<Response> errorResponseWrapper, User user) {
		log.debug("validate user role");
		if (!user.getRole().equals(Role.ADMIN.name()) && !user.getRole().equals(Role.DESIGNER.name())) {
			log.info("user is not in appropriate role to perform action");
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
			log.debug("audit before sending response");
			getComponentsUtils().auditResource(responseFormat, user, null, "", "", AuditingActionEnum.IMPORT_RESOURCE, null);

			Response response = buildErrorResponse(responseFormat);
			errorResponseWrapper.setInnerElement(response);
		}

	}

	protected void validateZip(Wrapper<Response> responseWrapper, File file, String payloadName) throws FileNotFoundException {
		InputStream fileInputStream = new FileInputStream(file);
		Map<String, byte[]> unzippedFolder = ZipUtil.readZip(new ZipInputStream(fileInputStream));
		if (payloadName == null || payloadName.isEmpty() || !unzippedFolder.containsKey(payloadName)) {
			log.info("Invalid json was received. payloadName should be yml file name");
			Response errorResponse = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
			responseWrapper.setInnerElement(errorResponse);
		}

	}
	protected void validateCsar(Wrapper<Response> responseWrapper, File file, String payloadName) throws FileNotFoundException {
		InputStream fileInputStream = new FileInputStream(file);
		Map<String, byte[]> unzippedFolder = ZipUtil.readZip(new ZipInputStream(fileInputStream));
		if (payloadName == null || payloadName.isEmpty() || unzippedFolder.isEmpty()) {
			log.info("Invalid json was received. payloadName should be yml file name");
			Response errorResponse = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
			responseWrapper.setInnerElement(errorResponse);
		}

	}

	protected void fillZipContents(Wrapper<String> yamlStringWrapper, File file) throws FileNotFoundException {
		InputStream fileInputStream = new FileInputStream(file);
		Map<String, byte[]> unzippedFolder = ZipUtil.readZip(new ZipInputStream(fileInputStream));
		String ymlName = unzippedFolder.keySet().iterator().next();
		fillToscaTemplateFromZip(yamlStringWrapper, ymlName, file);
	}

	protected void fillToscaTemplateFromZip(Wrapper<String> yamlStringWrapper, String payloadName, File file) throws FileNotFoundException {
		InputStream fileInputStream = new FileInputStream(file);
		Map<String, byte[]> unzippedFolder = ZipUtil.readZip(new ZipInputStream(fileInputStream));
		byte[] yamlFileInBytes = unzippedFolder.get(payloadName);
		String yamlAsString = new String(yamlFileInBytes, StandardCharsets.UTF_8);
		log.debug("received yaml: {}", yamlAsString);
		yamlStringWrapper.setInnerElement(yamlAsString);
	}
	
	protected void fillPayloadDataFromFile(Wrapper<Response> responseWrapper, UploadResourceInfo uploadResourceInfoWrapper, File file)  {
		try(InputStream fileInputStream = new FileInputStream(file);){
			
			byte [] data = new byte[(int)file.length()];
			if( fileInputStream.read(data) == -1){
				log.info("Invalid json was received.");
				ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
				
				Response errorResp = buildErrorResponse(responseFormat);
				responseWrapper.setInnerElement(errorResp);
			}
			String payloadData =  Base64.encodeBase64String(data);
			uploadResourceInfoWrapper.setPayloadData(payloadData);			
			
			
			
		} catch (IOException e) {
			log.info("Invalid json was received or Error while closing input Stream.");
			log.debug("Invalid json was received or Error while closing input Stream. {}", e.getMessage(), e);
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
			
			Response errorResp = buildErrorResponse(responseFormat);
			responseWrapper.setInnerElement(errorResp);
			
		}
	}

	protected void validateUserRole(Wrapper<Response> errorResponseWrapper, User user, ResourceAuthorityTypeEnum resourceAuthority) {
		log.debug("validate user role");
		if (resourceAuthority == ResourceAuthorityTypeEnum.NORMATIVE_TYPE_BE) {
			if (!user.getRole().equals(Role.ADMIN.name())) {
				log.info("user is not in appropriate role to perform action");
				ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
				log.debug("audit before sending response");
				getComponentsUtils().auditResource(responseFormat, user, null, "", "", AuditingActionEnum.IMPORT_RESOURCE, null);

				Response response = buildErrorResponse(responseFormat);
				errorResponseWrapper.setInnerElement(response);
			}
		} else {
			validateUserRole(errorResponseWrapper, user);
		}

	}

	protected void validateAndFillResourceJson(Wrapper<Response> responseWrapper, Wrapper<UploadResourceInfo> uploadResourceInfoWrapper, User user, ResourceAuthorityTypeEnum resourceAuthorityEnum, String resourceInfo) {
		boolean isValid;
		try {
			log.debug("The received json is {}", resourceInfo);
			UploadResourceInfo resourceInfoObject = gson.fromJson(resourceInfo, UploadResourceInfo.class);
			if (resourceInfoObject == null) {
				isValid = false;
			} else {
				if (!resourceAuthorityEnum.isBackEndImport()) {
					isValid = resourceInfoObject.getPayloadName() != null && !resourceInfoObject.getPayloadName().isEmpty();
					//only resource name is checked
				} else {
					isValid = true;
				}
				uploadResourceInfoWrapper.setInnerElement(resourceInfoObject);
			}

		} catch (JsonSyntaxException e) {
			log.debug("Invalid json was received. {}", e.getMessage(), e);
			isValid = false;

		}
		if (!isValid) {
			log.info("Invalid json was received.");
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
			getComponentsUtils().auditResource(responseFormat, user, null, "", "", AuditingActionEnum.IMPORT_RESOURCE, null);
			Response errorResp = buildErrorResponse(responseFormat);
			responseWrapper.setInnerElement(errorResp);
		}
	}

	protected void validateAuthorityType(Wrapper<Response> responseWrapper, String authorityType) {
		log.debug("The received authority type is {}", authorityType);
		ResourceAuthorityTypeEnum authorityTypeEnum = ResourceAuthorityTypeEnum.findByUrlPath(authorityType);
		if (authorityTypeEnum == null) {
			log.info("Invalid authority type was received.");
			Response errorResp = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
			responseWrapper.setInnerElement(errorResp);
		}
	}

	public ServletUtils getServletUtils() {
		initSpringFromContext();
		return servletUtils;
	}

	public Gson getGson() {
		return getServletUtils().getGson();
	}

	public ComponentsUtils getComponentsUtils() {
		return getServletUtils().getComponentsUtils();
	}

	protected void validatePayloadIsTosca(Wrapper<Response> responseWrapper, UploadResourceInfo uploadResourceInfo, User user, String toscaPayload) {
		log.debug("checking payload is valid tosca");
		boolean isValid;
		Map<String, Object> mappedToscaTemplate = (Map<String, Object>) new Yaml().load(toscaPayload);
		Either<String, ResultStatusEnum> findFirstToscaStringElement = ImportUtils.findFirstToscaStringElement(mappedToscaTemplate, ToscaTagNamesEnum.TOSCA_VERSION);

		if (findFirstToscaStringElement.isRight()) {
			isValid = false;
		} else {
			String defenitionVersionFound = findFirstToscaStringElement.left().value();
			if (defenitionVersionFound == null || defenitionVersionFound.isEmpty()) {
				isValid = false;
			} else {
				isValid = ImportUtils.Constants.TOSCA_DEFINITION_VERSIONS.contains(defenitionVersionFound);
			}
		}

		if (!isValid) {
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_TOSCA_TEMPLATE);
			Response errorResponse = buildErrorResponse(responseFormat);
			EnumMap<AuditingFieldsKeysEnum, Object> additionalParam = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
			additionalParam.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, uploadResourceInfo.getName());
			getComponentsUtils().auditResource(responseFormat, user, null, "", "", AuditingActionEnum.IMPORT_RESOURCE, additionalParam);
			responseWrapper.setInnerElement(errorResponse);
		}

	}

	protected void validatePayloadIsYml(Wrapper<Response> responseWrapper, User user, UploadResourceInfo uploadResourceInfo, String toscaTamplatePayload) {
		log.debug("checking tosca template is valid yml");
		YamlToObjectConverter yamlConvertor = new YamlToObjectConverter();
		boolean isYamlValid = yamlConvertor.isValidYaml(toscaTamplatePayload.getBytes());
		if (!isYamlValid) {
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_YAML_FILE);
			Response errorResponse = buildErrorResponse(responseFormat);
			EnumMap<AuditingFieldsKeysEnum, Object> additionalParam = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
			additionalParam.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, uploadResourceInfo.getName());
			getComponentsUtils().auditResource(responseFormat, user, null, "", "", AuditingActionEnum.IMPORT_RESOURCE, additionalParam);
			responseWrapper.setInnerElement(errorResponse);
		}
	}

	protected void validatePayloadNameSpace(Wrapper<Response> responseWrapper, UploadResourceInfo resourceInfo, User user, String toscaPayload) {
		boolean isValid;
		String nameSpace = "";
		Map<String, Object> mappedToscaTemplate = (Map<String, Object>) new Yaml().load(toscaPayload);
		Either<Map<String, Object>, ResultStatusEnum> toscaElement = ImportUtils.findFirstToscaMapElement(mappedToscaTemplate, ToscaTagNamesEnum.NODE_TYPES);
		if (toscaElement.isRight() || toscaElement.left().value().size() != 1) {
			isValid = false;
		} else {
			nameSpace = toscaElement.left().value().keySet().iterator().next();
			isValid = nameSpace.startsWith(Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX);
		}
		if (!isValid) {
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_RESOURCE_NAMESPACE);
			Response errorResponse = buildErrorResponse(responseFormat);
			EnumMap<AuditingFieldsKeysEnum, Object> additionalParam = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
			additionalParam.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceInfo.getName());
			getComponentsUtils().auditResource(responseFormat, user, null, "", "", AuditingActionEnum.IMPORT_RESOURCE, additionalParam);
			responseWrapper.setInnerElement(errorResponse);
		} else {
			String str1 = nameSpace.substring(Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX.length());
			String[] findTypes = str1.split("\\.");
			if (ResourceTypeEnum.containsName(findTypes[0].toUpperCase())) {
				String type = findTypes[0].toUpperCase();
				resourceInfo.setResourceType(type);
			} else {
				resourceInfo.setResourceType(ResourceTypeEnum.VFC.name());
			}
		}

	}

	protected void validatePayloadIsSingleResource(Wrapper<Response> responseWrapper, UploadResourceInfo uploadResourceInfo, User user, String toscaPayload) {
		log.debug("checking payload contains single resource");
		boolean isValid;
		Map<String, Object> mappedToscaTemplate = (Map<String, Object>) new Yaml().load(toscaPayload);
		Either<Map<String, Object>, ResultStatusEnum> toscaElement = ImportUtils.findFirstToscaMapElement(mappedToscaTemplate, ToscaTagNamesEnum.NODE_TYPES);
		if (toscaElement.isRight()) {
			isValid = false;
		} else {
			isValid = toscaElement.left().value().size() == 1;
		}

		if (!isValid) {
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.NOT_SINGLE_RESOURCE);
			Response errorResponse = buildErrorResponse(responseFormat);
			EnumMap<AuditingFieldsKeysEnum, Object> additionalParam = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
			additionalParam.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, uploadResourceInfo.getName());
			getComponentsUtils().auditResource(responseFormat, user, null, "", "", AuditingActionEnum.IMPORT_RESOURCE, additionalParam);
			responseWrapper.setInnerElement(errorResponse);
		}

	}

	protected void validatePayloadIsNotService(Wrapper<Response> responseWrapper, User user, UploadResourceInfo uploadResourceInfo, String toscaPayload) {
		log.debug("checking payload is not a tosca service");
		Map<String, Object> mappedToscaTemplate = (Map<String, Object>) new Yaml().load(toscaPayload);
		Either<Object, ResultStatusEnum> toscaElement = ImportUtils.findToscaElement(mappedToscaTemplate, ToscaTagNamesEnum.TOPOLOGY_TEMPLATE, ToscaElementTypeEnum.ALL);

		if (toscaElement.isLeft()) {
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.NOT_RESOURCE_TOSCA_TEMPLATE);
			Response errorResponse = buildErrorResponse(responseFormat);
			EnumMap<AuditingFieldsKeysEnum, Object> additionalParam = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
			additionalParam.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, uploadResourceInfo.getName());
			getComponentsUtils().auditResource(responseFormat, user, null, "", "", AuditingActionEnum.IMPORT_RESOURCE, additionalParam);
			responseWrapper.setInnerElement(errorResponse);
		}

	}
	
	protected void validateToscaTemplatePayloadName(Wrapper<Response> responseWrapper, UploadResourceInfo uploadResourceInfo, User user) {
		String toscaTemplatePayloadName = uploadResourceInfo.getPayloadName();
		boolean isValidSuffix = false;
		if (toscaTemplatePayloadName != null && !toscaTemplatePayloadName.isEmpty()) {
			for (String validSuffix : ImportUtils.Constants.TOSCA_YML_CSAR_VALID_SUFFIX) {
				isValidSuffix = isValidSuffix || toscaTemplatePayloadName.toLowerCase().endsWith(validSuffix);
			}
		}
		if (!isValidSuffix) {
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_TOSCA_FILE_EXTENSION);
			Response errorResponse = buildErrorResponse(responseFormat);
			EnumMap<AuditingFieldsKeysEnum, Object> additionalParam = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
			additionalParam.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, uploadResourceInfo.getName());
			getComponentsUtils().auditResource(responseFormat, user, null, "", "", AuditingActionEnum.IMPORT_RESOURCE, additionalParam);
			responseWrapper.setInnerElement(errorResponse);
		}

	}

	protected void validateMD5(Wrapper<Response> responseWrapper, User user, UploadResourceInfo resourceInfo, HttpServletRequest request, String resourceInfoJsonString) {
		boolean isValid;
		String recievedMD5 = request.getHeader(Constants.MD5_HEADER);
		if (recievedMD5 == null) {
			isValid = false;
		} else {
			String calculateMD5 = GeneralUtility.calculateMD5Base64EncodedByString(resourceInfoJsonString);
			isValid = calculateMD5.equals(recievedMD5);
		}
		if (!isValid) {
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_RESOURCE_CHECKSUM);
			Response errorResponse = buildErrorResponse(responseFormat);
			EnumMap<AuditingFieldsKeysEnum, Object> additionalParam = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
			additionalParam.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceInfo.getName());
			getComponentsUtils().auditResource(responseFormat, user, null, "", "", AuditingActionEnum.IMPORT_RESOURCE, additionalParam);
			responseWrapper.setInnerElement(errorResponse);
		}
	}

	protected void validateComponentType(Wrapper<Response> responseWrapper, Wrapper<ComponentTypeEnum> componentWrapper, String componentType) {
		boolean isValid;
		if (componentType == null) {
			isValid = false;
		} else {
			if (ComponentTypeEnum.RESOURCE_PARAM_NAME.equalsIgnoreCase(componentType)) {
				isValid = true;
				componentWrapper.setInnerElement(ComponentTypeEnum.RESOURCE);
			} else if (ComponentTypeEnum.SERVICE_PARAM_NAME.equalsIgnoreCase(componentType)) {
				isValid = true;
				componentWrapper.setInnerElement(ComponentTypeEnum.SERVICE);
			} else {
				isValid = false;
			}
		}
		if (!isValid) {
			log.debug("Invalid componentType:{}", componentType);
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
			Response errorResp = buildErrorResponse(responseFormat);
			responseWrapper.setInnerElement(errorResp);
		}
	}

	protected void fillToscaTemplateFromJson(Wrapper<Response> responseWrapper, Wrapper<String> yamlStringWrapper, User user, UploadResourceInfo resourceInfo) {
		if (resourceInfo.getPayloadData() == null || resourceInfo.getPayloadData().isEmpty()) {
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_RESOURCE_PAYLOAD);
			Response errorResponse = buildErrorResponse(responseFormat);
			EnumMap<AuditingFieldsKeysEnum, Object> additionalParam = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
			additionalParam.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceInfo.getName());
			getComponentsUtils().auditResource(responseFormat, user, null, "", "", AuditingActionEnum.IMPORT_RESOURCE, additionalParam);
			responseWrapper.setInnerElement(errorResponse);
		} else {
			String toscaPayload = resourceInfo.getPayloadData();
			String decodedPayload = new String(Base64.decodeBase64(toscaPayload));
			yamlStringWrapper.setInnerElement(decodedPayload);
		}

	}

	protected void fillPayload(Wrapper<Response> responseWrapper, Wrapper<UploadResourceInfo> uploadResourceInfoWrapper, Wrapper<String> yamlStringWrapper, User user, String resourceInfoJsonString, ResourceAuthorityTypeEnum resourceAuthorityEnum,
			File file) throws FileNotFoundException {

		if (responseWrapper.isEmpty()) {
			if (resourceAuthorityEnum.isBackEndImport()) {
				// PrePayload Validations
				if (responseWrapper.isEmpty()) {
					validateDataNotNull(responseWrapper, file, resourceInfoJsonString);
				}
				if(!resourceAuthorityEnum.equals(ResourceAuthorityTypeEnum.CSAR_TYPE_BE)){
					if (responseWrapper.isEmpty()) {
						validateZip(responseWrapper, file, uploadResourceInfoWrapper.getInnerElement().getPayloadName());
					}
	
					// Fill PayLoad From File
					if (responseWrapper.isEmpty()) {
						fillToscaTemplateFromZip(yamlStringWrapper, uploadResourceInfoWrapper.getInnerElement().getPayloadName(), file);
					}
				}else{
					
					if (responseWrapper.isEmpty()) {
						validateCsar(responseWrapper, file, uploadResourceInfoWrapper.getInnerElement().getPayloadName());
					}
	
					// Fill PayLoad From File
					if (responseWrapper.isEmpty()) {
						fillPayloadDataFromFile(responseWrapper, uploadResourceInfoWrapper.getInnerElement(), file);
					}
					
				}

			} else {
				// Fill PayLoad From JSON
				if (responseWrapper.isEmpty()) {
					fillToscaTemplateFromJson(responseWrapper, yamlStringWrapper, user, uploadResourceInfoWrapper.getInnerElement());
				}
			}

		}

	}

	protected void specificResourceAuthorityValidations(Wrapper<Response> responseWrapper, Wrapper<UploadResourceInfo> uploadResourceInfoWrapper, Wrapper<String> yamlStringWrapper, User user, HttpServletRequest request, String resourceInfoJsonString,
			ResourceAuthorityTypeEnum resourceAuthorityEnum) throws FileNotFoundException {

		if (responseWrapper.isEmpty()) {
			// UI Only Validation
			if (!resourceAuthorityEnum.isBackEndImport()) {
				importUIValidations(responseWrapper, uploadResourceInfoWrapper.getInnerElement(), user, request, resourceInfoJsonString);
			}

			// User Defined Type Resources
			if (resourceAuthorityEnum.isUserTypeResource() && !CsarValidationUtils.isCsarPayloadName(uploadResourceInfoWrapper.getInnerElement().getPayloadName())) {
				if (responseWrapper.isEmpty()) {
					validatePayloadNameSpace(responseWrapper, uploadResourceInfoWrapper.getInnerElement(), user, yamlStringWrapper.getInnerElement());
				}

			}
		}
	}
	
	protected void commonGeneralValidations(Wrapper<Response> responseWrapper, Wrapper<User> userWrapper, Wrapper<UploadResourceInfo> uploadResourceInfoWrapper, ResourceAuthorityTypeEnum resourceAuthorityEnum, String userUserId,
			String resourceInfoJsonString) {

		if (responseWrapper.isEmpty()) {
			validateUserExist(responseWrapper, userWrapper, userUserId);
		}

		if (responseWrapper.isEmpty()) {
			validateUserRole(responseWrapper, userWrapper.getInnerElement(), resourceAuthorityEnum);
		}

		if (responseWrapper.isEmpty()) {
			validateAndFillResourceJson(responseWrapper, uploadResourceInfoWrapper, userWrapper.getInnerElement(), resourceAuthorityEnum, resourceInfoJsonString);
		}

		if (responseWrapper.isEmpty()) {
			validateToscaTemplatePayloadName(responseWrapper, uploadResourceInfoWrapper.getInnerElement(), userWrapper.getInnerElement());
		}
		if (responseWrapper.isEmpty()) {
			validateResourceType(responseWrapper, uploadResourceInfoWrapper.getInnerElement(), userWrapper.getInnerElement(), resourceAuthorityEnum);
		}

	}

	private void validateResourceType(Wrapper<Response> responseWrapper, UploadResourceInfo uploadResourceInfo, User user, ResourceAuthorityTypeEnum resourceAuthorityEnum) {
		String resourceType = uploadResourceInfo.getResourceType();
		if (resourceType == null || !ResourceTypeEnum.containsName(resourceType)) {
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
			Response errorResponse = buildErrorResponse(responseFormat);
			EnumMap additionalParam = new EnumMap(AuditingFieldsKeysEnum.class);
			additionalParam.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, uploadResourceInfo.getName());
			getComponentsUtils().auditResource(responseFormat, user, null, "", "", AuditingActionEnum.IMPORT_RESOURCE, additionalParam);
			responseWrapper.setInnerElement(errorResponse);
		}
		if (resourceType.equals(ResourceTypeEnum.getTypeByName("VF").getValue()) && resourceAuthorityEnum == ResourceAuthorityTypeEnum.NORMATIVE_TYPE_BE){
			log.debug("Import of VF resource type is forbidden - VF resource import can be done using onboarding flow only");
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_RESOURCE_TYPE);
			Response errorResponse = buildErrorResponse(responseFormat);
			EnumMap additionalParam = new EnumMap(AuditingFieldsKeysEnum.class);
			additionalParam.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, uploadResourceInfo.getName());
			getComponentsUtils().auditResource(responseFormat, user, null, "", "", AuditingActionEnum.IMPORT_RESOURCE, additionalParam);
			responseWrapper.setInnerElement(errorResponse);
		}
	}

	protected void importUIValidations(Wrapper<Response> responseWrapper, UploadResourceInfo resourceInfo, User user, HttpServletRequest request, String resourceInfoJsonString) {
		if (responseWrapper.isEmpty()) {
			validateMD5(responseWrapper, user, resourceInfo, request, resourceInfoJsonString);
		}
		if (responseWrapper.isEmpty() && request != null && request.getMethod() != null && request.getMethod().equals("POST")) {
			validateResourceDoesNotExist(responseWrapper, user, resourceInfo.getName());
		}
	}

	protected void commonPayloadValidations(Wrapper<Response> responseWrapper, Wrapper<String> yamlStringWrapper, User user, UploadResourceInfo uploadResourceInfo) {

		if (responseWrapper.isEmpty()) {
			validatePayloadIsYml(responseWrapper, user, uploadResourceInfo, yamlStringWrapper.getInnerElement());
		}
		if (responseWrapper.isEmpty()) {
			validatePayloadIsTosca(responseWrapper, uploadResourceInfo, user, yamlStringWrapper.getInnerElement());
		}
		if (responseWrapper.isEmpty()) {
			validatePayloadIsNotService(responseWrapper, user, uploadResourceInfo, yamlStringWrapper.getInnerElement());
		}
		if (responseWrapper.isEmpty()) {
			validatePayloadIsSingleResource(responseWrapper, uploadResourceInfo, user, yamlStringWrapper.getInnerElement());
		}
	}

	/*protected void topologyTemplatePayloadValidations(Wrapper<Response> responseWrapper, Wrapper<String> yamlStringWrapper, User user, UploadResourceInfo uploadResourceInfo) {

		if (responseWrapper.isEmpty()) {
			validatePayloadIsYml(responseWrapper, user, uploadResourceInfo, yamlStringWrapper.getInnerElement());
		}
		if (responseWrapper.isEmpty()) {
			validatePayloadIsTosca(responseWrapper, uploadResourceInfo, user, yamlStringWrapper.getInnerElement());
		}
		if (responseWrapper.isEmpty()) {
			validatePayloadIsTopologyTemplate(responseWrapper, user, uploadResourceInfo, yamlStringWrapper.getInnerElement());
		}

	}*/

	protected void handleImport(Wrapper<Response> responseWrapper, User user, UploadResourceInfo resourceInfoObject, String yamlAsString, ResourceAuthorityTypeEnum authority, boolean createNewVersion, String resourceUniqueId) {
		Either<ImmutablePair<org.openecomp.sdc.be.model.Resource, ActionStatus>, ResponseFormat> createOrUpdateResponse;
		Response response;
		Object representation = null;

		if (CsarValidationUtils.isCsarPayloadName(resourceInfoObject.getPayloadName())) {
			log.debug("import resource from csar");
			
			createOrUpdateResponse = importResourceFromUICsar(resourceInfoObject, user, resourceUniqueId);
			//if (createOrUpdateResponse.isLeft()){
			//	LifecycleChangeInfoWithAction lifecycleChangeInfo = new LifecycleChangeInfoWithAction();
			//	lifecycleChangeInfo.setUserRemarks("certification on import");
			//	Function<Resource, Either<Boolean, ResponseFormat>> validator = (resource) -> resourceImportManager.getResourceBusinessLogic().validatePropertiesDefaultValues(createOrUpdateResponse.left().value().left);
			//}
			
		} else if (!authority.isUserTypeResource()) {
			log.debug("import normative type resource");
			createOrUpdateResponse = resourceImportManager.importNormativeResource(yamlAsString, resourceInfoObject, user, createNewVersion, true);
		} else {
			log.debug("import user resource (not normative type)");
			createOrUpdateResponse = resourceImportManager.importUserDefinedResource(yamlAsString, resourceInfoObject, user, false, false);
		}
		if (createOrUpdateResponse.isRight()) {
			response = buildErrorResponse(createOrUpdateResponse.right().value());
		} else {
			try {
				representation = RepresentationUtils.toRepresentation(createOrUpdateResponse.left().value().getLeft());
			} catch (IOException e) {
				log.debug("Error while building resource representation : {}", e.getMessage(), e);
			}
			ActionStatus successStatus = createOrUpdateResponse.left().value().right;
			response = buildOkResponse(getComponentsUtils().getResponseFormat(successStatus), representation);
		}
		responseWrapper.setInnerElement(response);
	}

	private Either<ImmutablePair<org.openecomp.sdc.be.model.Resource, ActionStatus>, ResponseFormat> importResourceFromUICsar(UploadResourceInfo resourceInfoObject, User user, String resourceUniqueId) {

		Either<org.openecomp.sdc.be.model.Resource, ResponseFormat> createOrUpdateResourceRes;
		ImmutablePair<org.openecomp.sdc.be.model.Resource, ActionStatus> result = null;
		ActionStatus actionStatus;
		org.openecomp.sdc.be.model.Resource resource = new org.openecomp.sdc.be.model.Resource();
		String payloadName = resourceInfoObject.getPayloadName();
		fillResourceFromResourceInfoObject(resource, resourceInfoObject);

		Either<Map<String, byte[]>, ResponseFormat> csarUIPayloadRes = getScarFromPayload(resourceInfoObject);
		if (csarUIPayloadRes.isRight()) {
			return Either.right(csarUIPayloadRes.right().value());
		}
		Map<String, byte[]> csarUIPayload = csarUIPayloadRes.left().value();

		createOrUpdateResourceRes = getAndValidateCsarYaml(csarUIPayload, resource, user, payloadName);
		if (createOrUpdateResourceRes.isRight()) {
			return Either.right(createOrUpdateResourceRes.right().value());
		}
		if (resourceUniqueId == null || resourceUniqueId.isEmpty()) {
			createOrUpdateResourceRes = resourceImportManager.getResourceBusinessLogic().createResource(resource, AuditingActionEnum.CREATE_RESOURCE, user, csarUIPayload, payloadName);
			if (createOrUpdateResourceRes.isRight()) {
				return Either.right(createOrUpdateResourceRes.right().value());
			}
			actionStatus = ActionStatus.CREATED;
		} else {
			createOrUpdateResourceRes = resourceImportManager.getResourceBusinessLogic().validateAndUpdateResourceFromCsar(resource, user, csarUIPayload, payloadName, resourceUniqueId);
			if (createOrUpdateResourceRes.isRight()) {
				return Either.right(createOrUpdateResourceRes.right().value());
			}
			actionStatus = ActionStatus.OK;
		}
		result = new ImmutablePair<org.openecomp.sdc.be.model.Resource, ActionStatus>(createOrUpdateResourceRes.left().value(), actionStatus);
		return Either.left(result);
	}

	private Either<org.openecomp.sdc.be.model.Resource, ResponseFormat> getAndValidateCsarYaml(Map<String, byte[]> csarUIPayload, org.openecomp.sdc.be.model.Resource resource, User user, String csarUUID) {

		Either<ImmutablePair<String, String>, ResponseFormat> getToscaYamlRes = CsarValidationUtils.getToscaYaml(csarUIPayload, csarUUID, getComponentsUtils());

		if (getToscaYamlRes.isRight()) {
			ResponseFormat responseFormat = getToscaYamlRes.right().value();
			log.debug("Error when try to get csar toscayamlFile with csar ID {}, error: {}", csarUUID, responseFormat);
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Creating resource from CSAR: fetching CSAR with id " + csarUUID + " failed");
			getComponentsUtils().auditResource(responseFormat, user, resource, "", "", AuditingActionEnum.CREATE_RESOURCE, null);
			return Either.right(responseFormat);
		}
		String toscaYaml = getToscaYamlRes.left().value().getValue();

		log.debug("checking tosca template is valid yml");
		YamlToObjectConverter yamlConvertor = new YamlToObjectConverter();
		boolean isValid = yamlConvertor.isValidYaml(toscaYaml.getBytes());
		if (!isValid) {
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_YAML_FILE);
			EnumMap<AuditingFieldsKeysEnum, Object> additionalParam = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
			additionalParam.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resource.getName());
			getComponentsUtils().auditResource(responseFormat, user, null, "", "", AuditingActionEnum.IMPORT_RESOURCE, additionalParam);
			return Either.right(responseFormat);
		}

		log.debug("checking payload is valid tosca");
		String heatDecodedPayload = toscaYaml;
		Map<String, Object> mappedToscaTemplate = (Map<String, Object>) new Yaml().load(heatDecodedPayload);
		Either<String, ResultStatusEnum> findFirstToscaStringElement = ImportUtils.findFirstToscaStringElement(mappedToscaTemplate, ToscaTagNamesEnum.TOSCA_VERSION);

		if (findFirstToscaStringElement.isRight()) {
			isValid = false;
		} else {
			String defenitionVersionFound = findFirstToscaStringElement.left().value();
			if (defenitionVersionFound == null || defenitionVersionFound.isEmpty()) {
				isValid = false;
			} else {
				isValid = ImportUtils.Constants.TOSCA_DEFINITION_VERSIONS.contains(defenitionVersionFound);
			}
		}

		if (!isValid) {
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_TOSCA_TEMPLATE);
			EnumMap<AuditingFieldsKeysEnum, Object> additionalParam = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
			additionalParam.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resource.getName());
			getComponentsUtils().auditResource(responseFormat, user, null, "", "", AuditingActionEnum.IMPORT_RESOURCE, additionalParam);
			return Either.right(responseFormat);
		}
		return Either.left(resource);
	}

	private void fillResourceFromResourceInfoObject(org.openecomp.sdc.be.model.Resource resource, UploadResourceInfo resourceInfoObject) {
		if (resource != null && resourceInfoObject != null) {
			resource.setDescription(resourceInfoObject.getDescription());
			resource.setTags(resourceInfoObject.getTags());
			resource.setCategories(resourceInfoObject.getCategories());
			resource.setContactId(resourceInfoObject.getContactId());
			resource.setName(resourceInfoObject.getName());
			resource.setIcon(resourceInfoObject.getResourceIconPath());
			resource.setVendorName(resourceInfoObject.getVendorName());
			resource.setVendorRelease(resourceInfoObject.getVendorRelease());
			resource.setResourceType(ResourceTypeEnum.valueOf(resourceInfoObject.getResourceType()));
			List<UploadArtifactInfo> artifactList = resourceInfoObject.getArtifactList();
			if (artifactList != null) {
				Map<String, ArtifactDefinition> artifactsHM = new HashMap<String, ArtifactDefinition>();
				for (UploadArtifactInfo artifact : artifactList) {
					ArtifactDefinition artifactDef = new ArtifactDefinition();
					artifactDef.setArtifactName(artifact.getArtifactName());
					artifactDef.setArtifactType(artifact.getArtifactType().getType());
					artifactDef.setDescription(artifact.getArtifactDescription());
					artifactDef.setPayloadData(artifact.getArtifactData());
					artifactDef.setArtifactRef(artifact.getArtifactPath());
					artifactsHM.put(artifactDef.getArtifactName(), artifactDef);
				}
				resource.setArtifacts(artifactsHM);
			}
		}

	}

	private Either<Map<String, byte[]>, ResponseFormat> getScarFromPayload(UploadResourceInfo innerElement) {
		String csarUUID = innerElement.getPayloadName();
		String payloadData = innerElement.getPayloadData();
		if (payloadData == null) {
			log.info("Failed to decode received csar", csarUUID);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_NOT_FOUND, csarUUID));
		}
		
		byte[] decodedPayload = Base64.decodeBase64(payloadData.getBytes(StandardCharsets.UTF_8));
		if (decodedPayload == null) {
			log.info("Failed to decode received csar", csarUUID);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_NOT_FOUND, csarUUID));
		}

		Map<String, byte[]> csar = ZipUtil.readZip(decodedPayload);
		if (csar == null) {
			log.info("Failed to unzip received csar", csarUUID);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID, csarUUID));
		}
		return Either.left(csar);
	}

	protected void validateInputStream(final HttpServletRequest request, Wrapper<String> dataWrapper, Wrapper<ResponseFormat> errorWrapper) throws IOException {
		InputStream inputStream = request.getInputStream();
		byte[] bytes = IOUtils.toByteArray(inputStream);
		if (bytes == null || bytes.length == 0) {
			log.info("Empty body was sent.");
			errorWrapper.setInnerElement(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
		} else {
			dataWrapper.setInnerElement(new String(bytes, StandardCharsets.UTF_8));
		}

	}

	protected <T> void validateClassParse(String data, Wrapper<T> parsedClassWrapper, Supplier<Class<T>> classGen, Wrapper<ResponseFormat> errorWrapper) {
		try {
			T parsedClass = gson.fromJson(data, classGen.get());
			if (parsedClass == null) {
				errorWrapper.setInnerElement(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
			} else {
				parsedClassWrapper.setInnerElement(parsedClass);
			}
		} catch (JsonSyntaxException e) {
			log.debug("Failed to decode received {} {} to object.", classGen.get().getName(), data, e);
			errorWrapper.setInnerElement(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
		}
	}

	protected void validateComponentInstanceBusinessLogic(HttpServletRequest request, String containerComponentType, Wrapper<ComponentInstanceBusinessLogic> blWrapper, Wrapper<ResponseFormat> errorWrapper) {
		ServletContext context = request.getSession().getServletContext();

		ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
		ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context, componentTypeEnum);
		if (componentInstanceLogic == null) {
			log.debug("Unsupported component type {}", containerComponentType);
			errorWrapper.setInnerElement(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
		} else {
			blWrapper.setInnerElement(componentInstanceLogic);
		}
	}

	protected <T> Response buildResponseFromElement(Wrapper<ResponseFormat> errorWrapper, Wrapper<T> attributeWrapper) throws IOException {
		Response response;
		if (errorWrapper.isEmpty()) {
			ObjectMapper mapper = new ObjectMapper();
			String result = mapper.writeValueAsString(attributeWrapper.getInnerElement());
			response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);
		} else {
			response = buildErrorResponse(errorWrapper.getInnerElement());
		}
		return response;
	}
	
	protected void validateXECOMPInstanceIDHeader(String instanceIdHeader, Wrapper<ResponseFormat> responseWrapper) {
		ResponseFormat responseFormat;
		if(StringUtils.isEmpty(instanceIdHeader) ){
			log.debug("Missing X-ECOMP-InstanceID header");
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
			responseWrapper.setInnerElement(responseFormat);
		}
	}
	
	protected void validateHttpCspUserIdHeader(String header, Wrapper<ResponseFormat> responseWrapper) {
		ResponseFormat responseFormat;
		if( StringUtils.isEmpty(header)){
			log.debug("MissingUSER_ID");
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_USER_ID);
			responseWrapper.setInnerElement(responseFormat);
		}
	}
	
	/**
	 * Convert json to Object object
	 * @param <T>
	 * @param classSupplier
	 * @param json
	 * @return
	 */
	public <T> Either<T, ResponseFormat> parseToObject(String json, Supplier<Class<T>> classSupplier) {
		
		try {
			T object = RepresentationUtils.fromRepresentation(json, classSupplier.get());
			return Either.left(object);
		} catch (Exception e) {
			log.debug("Failed to parse json to {} object", classSupplier.get().getName(), e);
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT);
			return Either.right(responseFormat);
		}
	}

	/**
	 * Convert json to Object object
	 * @param <T>
	 * @param json
	 * @param type
	 * @return
	 */
	public <T> Either<List<T>, ResponseFormat> parseListOfObjects(String json, Type type) {
		try {
			List<T> listOfObjects = gson.fromJson(json, type);
			return Either.left(listOfObjects);
		} catch (Exception e) {
			log.debug("Failed to parse json to {} object", type.getClass().getName(), e);
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
			return Either.right(responseFormat);
		}
	}
}
