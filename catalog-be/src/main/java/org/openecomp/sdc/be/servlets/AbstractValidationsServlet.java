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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import fj.data.Either;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.CsarValidationUtils;
import org.openecomp.sdc.be.components.impl.ImportUtils;
import org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import org.openecomp.sdc.be.components.impl.ImportUtils.ToscaElementTypeEnum;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.ServiceImportManager;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.be.model.UploadServiceInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.servlets.ResourceUploadServlet.ResourceAuthorityTypeEnum;
import org.openecomp.sdc.be.servlets.ServiceUploadServlet.ServiceAuthorityTypeEnum;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.api.UploadArtifactInfo;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.common.util.YamlToObjectConverter;
import org.openecomp.sdc.common.zip.ZipUtils;
import org.openecomp.sdc.common.zip.exception.ZipException;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.web.context.WebApplicationContext;
import org.yaml.snakeyaml.Yaml;

public abstract class AbstractValidationsServlet extends BeGenericServlet {

    private static final Logger log = Logger.getLogger(AbstractValidationsServlet.class);
    private static final String TOSCA_SIMPLE_YAML_PREFIX = "tosca_simple_yaml_";
    private static final List<String> TOSCA_DEFINITION_VERSIONS = Arrays
        .asList(TOSCA_SIMPLE_YAML_PREFIX + "1_0_0", TOSCA_SIMPLE_YAML_PREFIX + "1_1_0", "tosca_simple_profile_for_nfv_1_0_0",
            TOSCA_SIMPLE_YAML_PREFIX + "1_0", TOSCA_SIMPLE_YAML_PREFIX + "1_1", TOSCA_SIMPLE_YAML_PREFIX + "1_2", TOSCA_SIMPLE_YAML_PREFIX + "1_3");
    private static final List<String> TOSCA_YML_CSAR_VALID_SUFFIX = Arrays.asList(".yml", ".yaml", ".csar", ".meta");
    protected final ComponentInstanceBusinessLogic componentInstanceBusinessLogic;
    protected ServletUtils servletUtils;
    protected ResourceImportManager resourceImportManager;
    protected ServiceImportManager serviceImportManager;

    public AbstractValidationsServlet(UserBusinessLogic userBusinessLogic, ComponentInstanceBusinessLogic componentInstanceBL,
                                      ComponentsUtils componentsUtils, ServletUtils servletUtils, ResourceImportManager resourceImportManager) {
        super(userBusinessLogic, componentsUtils);
        this.servletUtils = servletUtils;
        this.resourceImportManager = resourceImportManager;
        this.componentInstanceBusinessLogic = componentInstanceBL;
    }

    public static void extractZipContents(Wrapper<String> yamlStringWrapper, File file) throws ZipException {
        final Map<String, byte[]> unzippedFolder = ZipUtils.readZip(file, false);
        String ymlName = unzippedFolder.keySet().iterator().next();
        fillToscaTemplateFromZip(yamlStringWrapper, ymlName, file);
    }

    private static void fillToscaTemplateFromZip(final Wrapper<String> yamlStringWrapper, final String payloadName, final File file)
        throws ZipException {
        final Map<String, byte[]> unzippedFolder = ZipUtils.readZip(file, false);
        final byte[] yamlFileInBytes = unzippedFolder.get(payloadName);
        final String yamlAsString = new String(yamlFileInBytes, StandardCharsets.UTF_8);
        log.debug("received yaml: {}", yamlAsString);
        yamlStringWrapper.setInnerElement(yamlAsString);
    }

    protected void init() {
    }

    protected synchronized void initSpringFromContext() {
        if (serviceImportManager == null) {
            ServletContext context = servletRequest.getSession().getServletContext();
            WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context
                .getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
            WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
            serviceImportManager = webApplicationContext.getBean(ServiceImportManager.class);
        }
    }

    protected void validateResourceDoesNotExist(Wrapper<Response> responseWrapper, User user, String resourceName) {
        if (resourceImportManager.isResourceExist(resourceName)) {
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.RESOURCE_ALREADY_EXISTS);
            Response errorResponse = buildErrorResponse(responseFormat);
            getComponentsUtils().auditResource(responseFormat, user, resourceName, AuditingActionEnum.IMPORT_RESOURCE);
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
        } else {
            UserBusinessLogic userAdmin = getServletUtils().getUserAdmin();
            try {
                User user = userAdmin.getUser(userUserId);
                userWrapper.setInnerElement(user);
            } catch (ComponentException ce) {
                log.info("user is not listed. userId={}", userUserId);
                User user = new User();
                user.setUserId(userUserId);
                Response response = returnMissingInformation(user);
                responseWrapper.setInnerElement(response);
            }
        }
    }

    protected Response returnMissingInformation(User user) {
        ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_INFORMATION);
        getComponentsUtils().auditResource(responseFormat, user, "", AuditingActionEnum.IMPORT_RESOURCE);
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
            getComponentsUtils().auditResource(responseFormat, user, "", AuditingActionEnum.IMPORT_RESOURCE);
            Response response = buildErrorResponse(responseFormat);
            errorResponseWrapper.setInnerElement(response);
        }
    }

    protected void validateZip(final Wrapper<Response> responseWrapper, final File zipFile, final String payloadName) {
        if (StringUtils.isEmpty(payloadName)) {
            log.info("Invalid JSON was received. Payload name is empty");
            final Response errorResponse = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
            responseWrapper.setInnerElement(errorResponse);
            return;
        }
        final Map<String, byte[]> unzippedFolder;
        try {
            unzippedFolder = ZipUtils.readZip(zipFile, false);
        } catch (final ZipException e) {
            log.error("Could not read ZIP file '{}' for validation", zipFile.getName(), e);
            final Response errorResponse = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
            responseWrapper.setInnerElement(errorResponse);
            return;
        }
        if (!unzippedFolder.containsKey(payloadName)) {
            log.info("Could no find payload '{}' in ZIP file '{}'", payloadName, zipFile.getName());
            final Response errorResponse = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
            responseWrapper.setInnerElement(errorResponse);
        }
    }

    protected void validateCsar(final Wrapper<Response> responseWrapper, final File csarFile, final String payloadName) {
        if (StringUtils.isEmpty(payloadName)) {
            log.info("Invalid JSON was received. Payload name is empty");
            Response errorResponse = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
            responseWrapper.setInnerElement(errorResponse);
            return;
        }
        final Map<String, byte[]> unzippedFolder;
        try {
            unzippedFolder = ZipUtils.readZip(csarFile, false);
        } catch (final ZipException e) {
            log.error("Could not read CSAR file '{}' for validation", csarFile.getName(), e);
            final Response errorResponse = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
            responseWrapper.setInnerElement(errorResponse);
            return;
        }
        if (unzippedFolder.isEmpty()) {
            log.info("The CSAR file is empty");
            Response errorResponse = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
            responseWrapper.setInnerElement(errorResponse);
        }
    }

    protected void fillZipContents(Wrapper<String> yamlStringWrapper, File file) throws ZipException {
        extractZipContents(yamlStringWrapper, file);
    }

    protected void fillPayloadDataFromFile(Wrapper<Response> responseWrapper, UploadResourceInfo uploadResourceInfoWrapper, File file) {
        try (InputStream fileInputStream = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            if (fileInputStream.read(data) == -1) {
                log.info("Invalid json was received.");
                ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
                Response errorResp = buildErrorResponse(responseFormat);
                responseWrapper.setInnerElement(errorResp);
            }
            String payloadData = Base64.encodeBase64String(data);
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
                getComponentsUtils().auditResource(responseFormat, user, "", AuditingActionEnum.IMPORT_RESOURCE);
                Response response = buildErrorResponse(responseFormat);
                errorResponseWrapper.setInnerElement(response);
            }
        } else {
            validateUserRole(errorResponseWrapper, user);
        }
    }

    protected void validateAndFillResourceJson(Wrapper<Response> responseWrapper, Wrapper<UploadResourceInfo> uploadResourceInfoWrapper, User user,
                                               ResourceAuthorityTypeEnum resourceAuthorityEnum, String resourceInfo) {
        boolean isValid;
        try {
            log.debug("The received json is {}", resourceInfo);
            UploadResourceInfo resourceInfoObject = gson.fromJson(resourceInfo, UploadResourceInfo.class);
            if (resourceInfoObject == null) {
                isValid = false;
            } else {
                resourceInfoObject.setNormative(!resourceAuthorityEnum.isUserTypeResource());
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
            getComponentsUtils().auditResource(responseFormat, user, "", AuditingActionEnum.IMPORT_RESOURCE);
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
        Either<String, ResultStatusEnum> findFirstToscaStringElement = ImportUtils
            .findFirstToscaStringElement(mappedToscaTemplate, TypeUtils.ToscaTagNamesEnum.TOSCA_VERSION);
        if (findFirstToscaStringElement.isRight()) {
            isValid = false;
        } else {
            String defenitionVersionFound = findFirstToscaStringElement.left().value();
            if (defenitionVersionFound == null || defenitionVersionFound.isEmpty()) {
                isValid = false;
            } else {
                isValid = TOSCA_DEFINITION_VERSIONS.contains(defenitionVersionFound);
            }
        }
        if (!isValid) {
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_TOSCA_TEMPLATE);
            Response errorResponse = buildErrorResponse(responseFormat);
            getComponentsUtils().auditResource(responseFormat, user, uploadResourceInfo.getName(), AuditingActionEnum.IMPORT_RESOURCE);
            responseWrapper.setInnerElement(errorResponse);
        }
    }

    protected void validatePayloadIsYml(Wrapper<Response> responseWrapper, User user, UploadResourceInfo uploadResourceInfo,
                                        String toscaTamplatePayload) {
        log.debug("checking tosca template is valid yml");
        YamlToObjectConverter yamlConvertor = new YamlToObjectConverter();
        boolean isYamlValid = yamlConvertor.isValidYaml(toscaTamplatePayload.getBytes());
        if (!isYamlValid) {
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_YAML_FILE);
            Response errorResponse = buildErrorResponse(responseFormat);
            getComponentsUtils().auditResource(responseFormat, user, uploadResourceInfo.getName(), AuditingActionEnum.IMPORT_RESOURCE);
            responseWrapper.setInnerElement(errorResponse);
        }
    }

    /**
     * Gets the Resource type from the given node type name.
     *
     * @param nodeTypeFullName - Node type Name
     * @return Resource Type name
     */
    private String getResourceType(final String nodeTypeFullName) {
        final Optional<String> nodeTypeNamePrefix = getNodeTypeNamePrefix(nodeTypeFullName);
        if (nodeTypeNamePrefix.isPresent()) {
            final String nameWithouNamespacePrefix = nodeTypeFullName.substring(nodeTypeNamePrefix.get().length());
            final String[] findTypes = nameWithouNamespacePrefix.split("\\.");
            if (findTypes.length > 0) {
                final ResourceTypeEnum resourceType = ResourceTypeEnum.getType(findTypes[0].toUpperCase());
                if (resourceType != null) {
                    return resourceType.name();
                }
            }
        }
        return ResourceTypeEnum.VFC.name();
    }

    /**
     * Extracts the Node Type Name prefix from the given Node Type Name.
     *
     * @param nodeName - Node Type Name
     * @return Node Type Name prefix
     */
    private Optional<String> getNodeTypeNamePrefix(final String nodeName) {
        final List<String> definedNodeTypeNamespaceList = ConfigurationManager.getConfigurationManager().getConfiguration()
            .getDefinedResourceNamespace();
        for (final String validNamespace : definedNodeTypeNamespaceList) {
            if (nodeName.startsWith(validNamespace)) {
                return Optional.of(validNamespace);
            }
        }
        return Optional.empty();
    }

    protected void validatePayloadNameSpace(final Wrapper<Response> responseWrapper, final UploadResourceInfo resourceInfo, final User user,
                                            final String toscaPayload) {
        boolean isValid;
        String namespace = "";
        final Map<String, Object> mappedToscaTemplate = (Map<String, Object>) new Yaml().load(toscaPayload);
        final Either<Map<String, Object>, ResultStatusEnum> toscaElement = ImportUtils
            .findFirstToscaMapElement(mappedToscaTemplate, TypeUtils.ToscaTagNamesEnum.NODE_TYPES);
        if (toscaElement.isRight() || toscaElement.left().value().size() != 1) {
            isValid = false;
        } else {
            namespace = toscaElement.left().value().keySet().iterator().next();
            isValid = getNodeTypeNamePrefix(namespace).isPresent();
        }
        if (!isValid) {
            final ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_RESOURCE_NAMESPACE);
            final Response errorResponse = buildErrorResponse(responseFormat);
            getComponentsUtils().auditResource(responseFormat, user, resourceInfo.getName(), AuditingActionEnum.IMPORT_RESOURCE);
            responseWrapper.setInnerElement(errorResponse);
        } else {
            resourceInfo.setResourceType(getResourceType(namespace));
        }
    }

    private void validatePayloadIsSingleResource(Wrapper<Response> responseWrapper, UploadResourceInfo uploadResourceInfo, User user,
                                                 String toscaPayload) {
        log.debug("checking payload contains single resource");
        boolean isValid;
        Map<String, Object> mappedToscaTemplate = (Map<String, Object>) new Yaml().load(toscaPayload);
        Either<Map<String, Object>, ResultStatusEnum> toscaElement = ImportUtils
            .findFirstToscaMapElement(mappedToscaTemplate, TypeUtils.ToscaTagNamesEnum.NODE_TYPES);
        if (toscaElement.isRight()) {
            isValid = false;
        } else {
            isValid = toscaElement.left().value().size() == 1;
        }
        if (!isValid) {
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.NOT_SINGLE_RESOURCE);
            Response errorResponse = buildErrorResponse(responseFormat);
            getComponentsUtils().auditResource(responseFormat, user, uploadResourceInfo.getName(), AuditingActionEnum.IMPORT_RESOURCE);
            responseWrapper.setInnerElement(errorResponse);
        }
    }

    private void validatePayloadIsNotService(Wrapper<Response> responseWrapper, User user, UploadResourceInfo uploadResourceInfo,
                                             String toscaPayload) {
        log.debug("checking payload is not a tosca service");
        Map<String, Object> mappedToscaTemplate = (Map<String, Object>) new Yaml().load(toscaPayload);
        Either<Object, ResultStatusEnum> toscaElement = ImportUtils
            .findToscaElement(mappedToscaTemplate, TypeUtils.ToscaTagNamesEnum.TOPOLOGY_TEMPLATE, ToscaElementTypeEnum.ALL);
        if (toscaElement.isLeft()) {
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.NOT_RESOURCE_TOSCA_TEMPLATE);
            Response errorResponse = buildErrorResponse(responseFormat);
            getComponentsUtils().auditResource(responseFormat, user, uploadResourceInfo.getName(), AuditingActionEnum.IMPORT_RESOURCE);
            responseWrapper.setInnerElement(errorResponse);
        }
    }

    private void validateToscaTemplatePayloadName(Wrapper<Response> responseWrapper, UploadResourceInfo uploadResourceInfo, User user) {
        String toscaTemplatePayloadName = uploadResourceInfo.getPayloadName();
        boolean isValidSuffix = isToscaTemplatePayloadNameValid(responseWrapper, toscaTemplatePayloadName);
        if (!isValidSuffix) {
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_TOSCA_FILE_EXTENSION);
            Response errorResponse = buildErrorResponse(responseFormat);
            getComponentsUtils().auditResource(responseFormat, user, uploadResourceInfo.getName(), AuditingActionEnum.IMPORT_RESOURCE);
            responseWrapper.setInnerElement(errorResponse);
        }
    }

    private boolean isToscaTemplatePayloadNameValid(Wrapper<Response> responseWrapper, String toscaTemplatePayloadName) {
        boolean isValidSuffix = false;
        if (toscaTemplatePayloadName != null && !toscaTemplatePayloadName.isEmpty()) {
            for (String validSuffix : TOSCA_YML_CSAR_VALID_SUFFIX) {
                isValidSuffix = isValidSuffix || toscaTemplatePayloadName.toLowerCase().endsWith(validSuffix);
            }
        }
        return isValidSuffix;
    }

    private void validateMD5(Wrapper<Response> responseWrapper, User user, UploadResourceInfo resourceInfo, HttpServletRequest request,
                             String resourceInfoJsonString) {
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
            getComponentsUtils().auditResource(responseFormat, user, resourceInfo.getName(), AuditingActionEnum.IMPORT_RESOURCE);
            responseWrapper.setInnerElement(errorResponse);
        }
    }

    ComponentTypeEnum validateComponentType(String componentType) {
        if (componentType == null) {
            throw new ByActionStatusComponentException(ActionStatus.UNSUPPORTED_ERROR);
        }
        if (ComponentTypeEnum.RESOURCE_PARAM_NAME.equalsIgnoreCase(componentType)) {
            return ComponentTypeEnum.RESOURCE;
        }
        if (ComponentTypeEnum.SERVICE_PARAM_NAME.equalsIgnoreCase(componentType)) {
            return ComponentTypeEnum.SERVICE;
        }
        log.debug("Invalid componentType:{}", componentType);
        throw new ByActionStatusComponentException(ActionStatus.UNSUPPORTED_ERROR, componentType);
    }

    ComponentTypeEnum convertToComponentType(String componentType) {
        return validateComponentType(componentType);
    }

    private void fillToscaTemplateFromJson(Wrapper<Response> responseWrapper, Wrapper<String> yamlStringWrapper, User user,
                                           UploadResourceInfo resourceInfo) {
        if (resourceInfo.getPayloadData() == null || resourceInfo.getPayloadData().isEmpty()) {
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_RESOURCE_PAYLOAD);
            Response errorResponse = buildErrorResponse(responseFormat);
            getComponentsUtils().auditResource(responseFormat, user, resourceInfo.getName(), AuditingActionEnum.IMPORT_RESOURCE);
            responseWrapper.setInnerElement(errorResponse);
        } else {
            String toscaPayload = resourceInfo.getPayloadData();
            String decodedPayload = new String(Base64.decodeBase64(toscaPayload));
            yamlStringWrapper.setInnerElement(decodedPayload);
        }
    }

    void fillPayload(Wrapper<Response> responseWrapper, Wrapper<UploadResourceInfo> uploadResourceInfoWrapper, Wrapper<String> yamlStringWrapper,
                     User user, String resourceInfoJsonString, ResourceAuthorityTypeEnum resourceAuthorityEnum, File file) throws ZipException {
        if (responseWrapper.isEmpty()) {
            if (resourceAuthorityEnum.isBackEndImport()) {
                // PrePayload Validations
                if (responseWrapper.isEmpty()) {
                    validateDataNotNull(responseWrapper, file, resourceInfoJsonString);
                }
                if (!resourceAuthorityEnum.equals(ResourceAuthorityTypeEnum.CSAR_TYPE_BE)) {
                    if (responseWrapper.isEmpty()) {
                        validateZip(responseWrapper, file, uploadResourceInfoWrapper.getInnerElement().getPayloadName());
                    }
                    // Fill PayLoad From File
                    if (responseWrapper.isEmpty()) {
                        fillToscaTemplateFromZip(yamlStringWrapper, uploadResourceInfoWrapper.getInnerElement().getPayloadName(), file);
                    }
                } else {
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

    protected void specificResourceAuthorityValidations(final Wrapper<Response> responseWrapper,
                                                        final Wrapper<UploadResourceInfo> uploadResourceInfoWrapper,
                                                        final Wrapper<String> yamlStringWrapper, final User user, final HttpServletRequest request,
                                                        final String resourceInfoJsonString, final ResourceAuthorityTypeEnum resourceAuthorityEnum) {
        if (responseWrapper.isEmpty()) {
            // UI Only Validation
            if (!resourceAuthorityEnum.isBackEndImport()) {
                importUIValidations(responseWrapper, uploadResourceInfoWrapper.getInnerElement(), user, request, resourceInfoJsonString);
            }
            // User Defined Type Resources
            if (resourceAuthorityEnum.isUserTypeResource() && !CsarValidationUtils
                .isCsarPayloadName(uploadResourceInfoWrapper.getInnerElement().getPayloadName()) && responseWrapper.isEmpty()) {
                validatePayloadNameSpace(responseWrapper, uploadResourceInfoWrapper.getInnerElement(), user, yamlStringWrapper.getInnerElement());
            }
        }
    }

    void commonGeneralValidations(Wrapper<Response> responseWrapper, Wrapper<User> userWrapper, Wrapper<UploadResourceInfo> uploadResourceInfoWrapper,
                                  ResourceAuthorityTypeEnum resourceAuthorityEnum, String userId, String resourceInfoJsonString) {
        if (responseWrapper.isEmpty()) {
            validateUserExist(responseWrapper, userWrapper, userId);
        }
        if (responseWrapper.isEmpty()) {
            validateUserRole(responseWrapper, userWrapper.getInnerElement(), resourceAuthorityEnum);
        }
        if (responseWrapper.isEmpty()) {
            validateAndFillResourceJson(responseWrapper, uploadResourceInfoWrapper, userWrapper.getInnerElement(), resourceAuthorityEnum,
                resourceInfoJsonString);
        }
        if (responseWrapper.isEmpty()) {
            validateToscaTemplatePayloadName(responseWrapper, uploadResourceInfoWrapper.getInnerElement(), userWrapper.getInnerElement());
        }
        if (responseWrapper.isEmpty()) {
            validateResourceType(responseWrapper, uploadResourceInfoWrapper.getInnerElement(), userWrapper.getInnerElement());
        }
    }

    private void validateResourceType(Wrapper<Response> responseWrapper, UploadResourceInfo uploadResourceInfo, User user) {
        String resourceType = uploadResourceInfo.getResourceType();
        if (resourceType == null || !ResourceTypeEnum.containsName(resourceType)) {
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
            Response errorResponse = buildErrorResponse(responseFormat);
            getComponentsUtils().auditResource(responseFormat, user, uploadResourceInfo.getName(), AuditingActionEnum.IMPORT_RESOURCE);
            responseWrapper.setInnerElement(errorResponse);
        }
    }

    private void importUIValidations(Wrapper<Response> responseWrapper, UploadResourceInfo resourceInfo, User user, HttpServletRequest request,
                                     String resourceInfoJsonString) {
        if (responseWrapper.isEmpty()) {
            validateMD5(responseWrapper, user, resourceInfo, request, resourceInfoJsonString);
        }
        if (responseWrapper.isEmpty() && request != null && request.getMethod() != null && request.getMethod().equals("POST")) {
            validateResourceDoesNotExist(responseWrapper, user, resourceInfo.getName());
        }
    }

    void commonPayloadValidations(Wrapper<Response> responseWrapper, Wrapper<String> yamlStringWrapper, User user,
                                  UploadResourceInfo uploadResourceInfo) {
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

    void handleImport(Wrapper<Response> responseWrapper, User user, UploadResourceInfo resourceInfoObject, String yamlAsString,
                      ResourceAuthorityTypeEnum authority, boolean createNewVersion, String resourceUniqueId) {
        ImmutablePair<Resource, ActionStatus> createOrUpdateResponse = null;
        Response response = null;
        Object representation = null;
        ImmutablePair<Resource, ActionStatus> importedResourceStatus = null;
        if (CsarValidationUtils.isCsarPayloadName(resourceInfoObject.getPayloadName())) {
            log.debug("import resource from csar");
            importedResourceStatus = importResourceFromUICsar(resourceInfoObject, user, resourceUniqueId);
        } else if (!authority.isUserTypeResource()) {
            log.debug("import normative type resource");
            createOrUpdateResponse =
                resourceImportManager.importNormativeResource(yamlAsString, resourceInfoObject, user, createNewVersion, true, false);
        } else {
            log.debug("import user resource (not normative type)");
            createOrUpdateResponse = resourceImportManager.importUserDefinedResource(yamlAsString, resourceInfoObject, user, false);
        }
        if (createOrUpdateResponse != null) {
            importedResourceStatus = createOrUpdateResponse;
        }
        if (importedResourceStatus != null) {
            try {
                representation = RepresentationUtils.toRepresentation(importedResourceStatus.left);
            } catch (IOException e) {
                log.debug("Error while building resource representation : {}", e.getMessage(), e);
            }
            response = buildOkResponse(getComponentsUtils().getResponseFormat(importedResourceStatus.right), representation);
        }
        responseWrapper.setInnerElement(response);
    }

    private ImmutablePair<Resource, ActionStatus> importResourceFromUICsar(UploadResourceInfo resourceInfoObject, User user,
                                                                           String resourceUniqueId) {
        Resource newResource;
        ActionStatus actionStatus;
        Resource resource = new Resource();
        String payloadName = resourceInfoObject.getPayloadName();
        fillResourceFromResourceInfoObject(resource, resourceInfoObject);
        Map<String, byte[]> csarUIPayload = getCsarFromPayload(resourceInfoObject);
        getAndValidateCsarYaml(csarUIPayload, resource, user, payloadName);
        if (resourceUniqueId == null || resourceUniqueId.isEmpty()) {
            newResource = resourceImportManager.getResourceBusinessLogic()
                .createResource(resource, AuditingActionEnum.CREATE_RESOURCE, user, csarUIPayload, payloadName);
            actionStatus = ActionStatus.CREATED;
        } else {
            newResource = resourceImportManager.getResourceBusinessLogic()
                .validateAndUpdateResourceFromCsar(resource, user, csarUIPayload, payloadName, resourceUniqueId);
            actionStatus = ActionStatus.OK;
        }
        return new ImmutablePair<>(newResource, actionStatus);
    }

    protected Resource throwComponentException(ResponseFormat responseFormat) {
        throw new ByResponseFormatComponentException(responseFormat);
    }

    private void getAndValidateCsarYaml(Map<String, byte[]> csarUIPayload, Resource resource, User user, String csarUUID) {
        getAndValidateComponentCsarYaml(csarUIPayload, resource, user, csarUUID);
    }

    private void getAndValidateComponentCsarYaml(Map<String, byte[]> csarUIPayload, Component component, User user, String csarUUID) {
        Either<ImmutablePair<String, String>, ResponseFormat> getToscaYamlRes = CsarValidationUtils
            .getToscaYaml(csarUIPayload, csarUUID, getComponentsUtils());
        if (getToscaYamlRes.isRight()) {
            ResponseFormat responseFormat = getToscaYamlRes.right().value();
            log.debug("Error when try to get csar toscayamlFile with csar ID {}, error: {}", csarUUID, responseFormat);
            if (component instanceof Resource) {
                BeEcompErrorManager.getInstance().logBeDaoSystemError("Creating resource from CSAR: fetching CSAR with id " + csarUUID + " failed");
                getComponentsUtils().auditResource(responseFormat, user, (Resource) component, AuditingActionEnum.CREATE_RESOURCE);
            } else {
                BeEcompErrorManager.getInstance().logBeDaoSystemError("Creating service from CSAR: fetching CSAR with id " + csarUUID + " failed");
            }
            throwComponentException(responseFormat);
        }
        String toscaYaml = getToscaYamlRes.left().value().getValue();
        log.debug("checking tosca template is valid yml");
        YamlToObjectConverter yamlConvertor = new YamlToObjectConverter();
        boolean isValid = yamlConvertor.isValidYaml(toscaYaml.getBytes());
        if (!isValid) {
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_YAML_FILE);
            if (component instanceof Resource) {
                getComponentsUtils().auditResource(responseFormat, user, (Resource) component, AuditingActionEnum.IMPORT_RESOURCE);
            }
            throwComponentException(responseFormat);
        }
        log.debug("checking payload is valid tosca");
        Map<String, Object> mappedToscaTemplate = (Map<String, Object>) new Yaml().load(toscaYaml);
        Either<String, ResultStatusEnum> findFirstToscaStringElement = ImportUtils
            .findFirstToscaStringElement(mappedToscaTemplate, TypeUtils.ToscaTagNamesEnum.TOSCA_VERSION);
        if (findFirstToscaStringElement.isRight()) {
            isValid = false;
        } else {
            String defenitionVersionFound = findFirstToscaStringElement.left().value();
            if (defenitionVersionFound == null || defenitionVersionFound.isEmpty()) {
                isValid = false;
            } else {
                isValid = TOSCA_DEFINITION_VERSIONS.contains(defenitionVersionFound);
            }
        }
        if (!isValid) {
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_TOSCA_TEMPLATE);
            if (component instanceof Resource) {
                log.debug("enter getAndValidateComponentCsarYaml,component instanceof Resource");
                getComponentsUtils().auditResource(responseFormat, user, (Resource) component, AuditingActionEnum.IMPORT_RESOURCE);
            }
            throwComponentException(responseFormat);
        }
    }

    private void fillResourceFromResourceInfoObject(Resource resource, UploadResourceInfo resourceInfoObject) {
        resourceImportManager.populateResourceMetadata(resourceInfoObject, resource);
        fillArtifacts(resource, resourceInfoObject);
    }

    private void fillArtifacts(Resource resource, UploadResourceInfo resourceInfoObject) {
        if (resource != null && resourceInfoObject != null) {
            List<UploadArtifactInfo> artifactList = resourceInfoObject.getArtifactList();
            if (artifactList != null) {
                Map<String, ArtifactDefinition> artifactsHM = new HashMap<>();
                buildArtifactsHM(artifactList, artifactsHM);
                resource.setArtifacts(artifactsHM);
            }
        }
    }

    private void buildArtifactsHM(List<UploadArtifactInfo> artifactList, Map<String, ArtifactDefinition> artifactsHM) {
        for (UploadArtifactInfo artifact : artifactList) {
            ArtifactDefinition artifactDef = new ArtifactDefinition();
            artifactDef.setArtifactName(artifact.getArtifactName());
            artifactDef.setArtifactType(artifact.getArtifactType().getType());
            artifactDef.setDescription(artifact.getArtifactDescription());
            artifactDef.setPayloadData(artifact.getArtifactData());
            artifactDef.setArtifactRef(artifact.getArtifactPath());
            artifactsHM.put(artifactDef.getArtifactName(), artifactDef);
        }
    }

    private Map<String, byte[]> getCsarFromPayload(UploadResourceInfo innerElement) {
        String csarUUID = innerElement.getPayloadName();
        String payloadData = innerElement.getPayloadData();
        return getComponentCsarFromPayload(csarUUID, payloadData);
    }

    private Map<String, byte[]> getComponentCsarFromPayload(String csarUUID, String payloadData) {
        if (payloadData == null) {
            log.info("Failed to decode received csar {}", csarUUID);
            throw new ByActionStatusComponentException(ActionStatus.CSAR_NOT_FOUND, csarUUID);
        }
        byte[] decodedPayload = Base64.decodeBase64(payloadData.getBytes(StandardCharsets.UTF_8));
        if (decodedPayload == null) {
            log.info("Failed to decode received csar {}", csarUUID);
            throw new ByActionStatusComponentException(ActionStatus.CSAR_NOT_FOUND, csarUUID);
        }
        Map<String, byte[]> csar = null;
        try {
            csar = ZipUtils.readZip(decodedPayload, false);
        } catch (final ZipException e) {
            log.info("Failed to unzip received csar {}", csarUUID, e);
        }
        return csar;
    }

    void validateInputStream(final HttpServletRequest request, Wrapper<String> dataWrapper, Wrapper<ResponseFormat> errorWrapper) throws IOException {
        InputStream inputStream = request.getInputStream();
        byte[] bytes = IOUtils.toByteArray(inputStream);
        if (bytes == null || bytes.length == 0) {
            log.info("Empty body was sent.");
            errorWrapper.setInnerElement(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
        } else {
            dataWrapper.setInnerElement(new String(bytes, StandardCharsets.UTF_8));
        }
    }

    <T> void validateClassParse(String data, Wrapper<T> parsedClassWrapper, Supplier<Class<T>> classGen, Wrapper<ResponseFormat> errorWrapper) {
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

    void validateComponentInstanceBusinessLogic(HttpServletRequest request, String containerComponentType,
                                                Wrapper<ComponentInstanceBusinessLogic> blWrapper, Wrapper<ResponseFormat> errorWrapper) {
        ServletContext context = request.getSession().getServletContext();
        ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context);
        if (componentInstanceLogic == null) {
            log.debug("Unsupported component type {}", containerComponentType);
            errorWrapper.setInnerElement(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
        } else {
            blWrapper.setInnerElement(componentInstanceLogic);
        }
    }

    <T> Response buildResponseFromElement(Wrapper<ResponseFormat> errorWrapper, Wrapper<T> attributeWrapper) throws IOException {
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
        if (StringUtils.isEmpty(instanceIdHeader)) {
            log.debug("Missing X-ECOMP-InstanceID header");
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
            responseWrapper.setInnerElement(responseFormat);
        }
    }

    protected void validateHttpCspUserIdHeader(String header, Wrapper<ResponseFormat> responseWrapper) {
        ResponseFormat responseFormat;
        if (StringUtils.isEmpty(header)) {
            log.debug("MissingUSER_ID");
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_USER_ID);
            responseWrapper.setInnerElement(responseFormat);
        }
    }

    <T> Either<T, ResponseFormat> parseToObject(String json, Supplier<Class<T>> classSupplier) {
        try {
            T object = RepresentationUtils.fromRepresentation(json, classSupplier.get());
            return Either.left(object);
        } catch (Exception e) {
            log.debug("Failed to parse json to {} object", classSupplier.get().getName(), e);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT);
            return Either.right(responseFormat);
        }
    }

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

    protected void validateNotEmptyBody(String data) {
        if (StringUtils.isEmpty(data)) {
            throw new ByActionStatusComponentException(ActionStatus.MISSING_BODY);
        }
    }

    protected void commonServiceGeneralValidations(Wrapper<Response> responseWrapper, Wrapper<User> userWrapper,
                                                   Wrapper<UploadServiceInfo> uploadServiceInfoWrapper, ServiceAuthorityTypeEnum serviceAuthorityEnum,
                                                   String userUserId, String serviceInfoJsonString) {
        if (responseWrapper.isEmpty()) {
            validateUserExist(responseWrapper, userWrapper, userUserId);
        }
        if (responseWrapper.isEmpty()) {
            validateUserRole(responseWrapper, userWrapper.getInnerElement(), serviceAuthorityEnum);
        }
        if (responseWrapper.isEmpty()) {
            validateAndFillServiceJson(responseWrapper, uploadServiceInfoWrapper, userWrapper.getInnerElement(), serviceAuthorityEnum,
                serviceInfoJsonString);
        }
        if (responseWrapper.isEmpty()) {
            validateToscaTemplatePayloadName(responseWrapper, uploadServiceInfoWrapper.getInnerElement(), userWrapper.getInnerElement());
        }
    }

    protected void validateUserRole(Wrapper<Response> errorResponseWrapper, User user, ServiceAuthorityTypeEnum serviceAuthority) {
        log.debug("validate user role");
        if (serviceAuthority == ServiceAuthorityTypeEnum.NORMATIVE_TYPE_BE) {
            if (!user.getRole().equals(Role.ADMIN.name())) {
                log.info("user is not in appropriate role to perform action");
                ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
                log.debug("audit before sending response");
                Response response = buildErrorResponse(responseFormat);
                errorResponseWrapper.setInnerElement(response);
            }
        } else {
            validateUserRole(errorResponseWrapper, user);
        }
    }

    protected void validateAndFillServiceJson(Wrapper<Response> responseWrapper, Wrapper<UploadServiceInfo> uploadServiceInfoWrapper, User user,
                                              ServiceAuthorityTypeEnum serviceAuthorityEnum, String serviceInfo) {
        boolean isValid;
        try {
            log.debug("The received json is {}", serviceInfo);
            UploadServiceInfo serviceInfoObject = gson.fromJson(serviceInfo, UploadServiceInfo.class);
            if (serviceInfoObject == null) {
                isValid = false;
            } else {
                if (!serviceAuthorityEnum.isBackEndImport()) {
                    isValid = serviceInfoObject.getPayloadName() != null && !serviceInfoObject.getPayloadName().isEmpty();
                    //only service name is checked
                } else {
                    isValid = true;
                }
                uploadServiceInfoWrapper.setInnerElement(serviceInfoObject);
                log.debug("get isValid:{},serviceInfoObject get name:{},get tags:{},getContactId:{}," + " getPayloadName:{}", isValid,
                    uploadServiceInfoWrapper.getInnerElement().getName(), uploadServiceInfoWrapper.getInnerElement().getTags(),
                    uploadServiceInfoWrapper.getInnerElement().getContactId(), uploadServiceInfoWrapper.getInnerElement().getPayloadName());
            }
        } catch (JsonSyntaxException e) {
            log.debug("enter validateAndFillServiceJson,Invalid json was received. {}", e.getMessage(), e);
            isValid = false;
        }
        if (!isValid) {
            log.info("Invalid json was received.");
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
            Response errorResp = buildErrorResponse(responseFormat);
            responseWrapper.setInnerElement(errorResp);
        }
    }

    protected void validateToscaTemplatePayloadName(Wrapper<Response> responseWrapper, UploadServiceInfo uploadServiceInfo, User user) {
        String toscaTemplatePayloadName = uploadServiceInfo.getPayloadName();
        boolean isValidSuffix = isToscaTemplatePayloadNameValid(responseWrapper, toscaTemplatePayloadName);
        if (!isValidSuffix) {
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_TOSCA_FILE_EXTENSION);
            Response errorResponse = buildErrorResponse(responseFormat);
            responseWrapper.setInnerElement(errorResponse);
        }
    }

    protected void specificServiceAuthorityValidations(Wrapper<Response> responseWrapper, Wrapper<UploadServiceInfo> uploadServiceInfoWrapper,
                                                       Wrapper<String> yamlStringWrapper, User user, HttpServletRequest request,
                                                       String serviceInfoJsonString, ServiceAuthorityTypeEnum serviceAuthorityEnum)
        throws FileNotFoundException {
        if (responseWrapper.isEmpty()) {
            // UI Only Validation
            if (!serviceAuthorityEnum.isBackEndImport()) {
                importUIValidations(responseWrapper, uploadServiceInfoWrapper.getInnerElement(), user, request, serviceInfoJsonString);
            }
            // User Defined Type Services
            if (serviceAuthorityEnum.isUserTypeService() && !CsarValidationUtils
                .isCsarPayloadName(uploadServiceInfoWrapper.getInnerElement().getPayloadName())) {
                if (responseWrapper.isEmpty()) {
                    validatePayloadNameSpace(responseWrapper, uploadServiceInfoWrapper.getInnerElement(), user, yamlStringWrapper.getInnerElement());
                }
            }
        }
    }

    protected void importUIValidations(Wrapper<Response> responseWrapper, UploadServiceInfo serviceInfo, User user, HttpServletRequest request,
                                       String serviceInfoJsonString) {
        if (responseWrapper.isEmpty()) {
            validateMD5(responseWrapper, user, serviceInfo, request, serviceInfoJsonString);
        }
        if (responseWrapper.isEmpty() && request != null && request.getMethod() != null && request.getMethod().equals("POST")) {
            validateServiceDoesNotExist(responseWrapper, user, serviceInfo.getName());
        }
    }

    protected void validatePayloadNameSpace(Wrapper<Response> responseWrapper, UploadServiceInfo serviceInfo, User user, String toscaPayload) {
        boolean isValid;
        String nameSpace = "";
        Map<String, Object> mappedToscaTemplate = (Map<String, Object>) new Yaml().load(toscaPayload);
        Either<Map<String, Object>, ResultStatusEnum> toscaElement = ImportUtils
            .findFirstToscaMapElement(mappedToscaTemplate, TypeUtils.ToscaTagNamesEnum.NODE_TYPES);
        if (toscaElement.isRight() || toscaElement.left().value().size() != 1) {
            isValid = false;
        } else {
            nameSpace = toscaElement.left().value().keySet().iterator().next();
            isValid = nameSpace.startsWith(Constants.USER_DEFINED_SERVICE_NAMESPACE_PREFIX);
            log.debug("enter validatePayloadNameSpace,get nameSpace:{},get Valid is:{}", nameSpace, isValid);
        }
        if (!isValid) {
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_SERVICE_NAMESPACE);
            Response errorResponse = buildErrorResponse(responseFormat);
            responseWrapper.setInnerElement(errorResponse);
        } else {
            String str1 = nameSpace.substring(Constants.USER_DEFINED_SERVICE_NAMESPACE_PREFIX.length());
            String[] findTypes = str1.split("\\.");
            if (ResourceTypeEnum.containsName(findTypes[0].toUpperCase())) {
                String type = findTypes[0].toUpperCase();
                serviceInfo.setServiceType(type);
            } else {
                serviceInfo.setServiceType(ResourceTypeEnum.SERVICE.name());
            }
        }
    }

    protected void validateMD5(Wrapper<Response> responseWrapper, User user, UploadServiceInfo serviceInfo, HttpServletRequest request,
                               String serviceInfoJsonString) {
        boolean isValid;
        String recievedMD5 = request.getHeader(Constants.MD5_HEADER);
        if (recievedMD5 == null) {
            isValid = false;
        } else {
            String calculateMD5 = GeneralUtility.calculateMD5Base64EncodedByString(serviceInfoJsonString);
            isValid = calculateMD5.equals(recievedMD5);
        }
        if (!isValid) {
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_SERVICE_CHECKSUM);
            Response errorResponse = buildErrorResponse(responseFormat);
            responseWrapper.setInnerElement(errorResponse);
        }
    }

    protected void validateServiceDoesNotExist(Wrapper<Response> responseWrapper, User user, String serviceName) {
        if (serviceImportManager.isServiceExist(serviceName)) {
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.SERVICE_ALREADY_EXISTS);
            Response errorResponse = buildErrorResponse(responseFormat);
            responseWrapper.setInnerElement(errorResponse);
        }
    }

    protected void handleImportService(Wrapper<Response> responseWrapper, User user, UploadServiceInfo serviceInfoObject, String yamlAsString,
                                       ServiceAuthorityTypeEnum authority, boolean createNewVersion, String serviceUniqueId) throws ZipException {
        Response response = null;
        Object representation = null;
        ImmutablePair<Service, ActionStatus> importedServiceStatus = null;
        if (CsarValidationUtils.isCsarPayloadName(serviceInfoObject.getPayloadName())) {
            log.debug("import service from csar");
            importedServiceStatus = importServiceFromUICsar(serviceInfoObject, user, serviceUniqueId);
        }
        if (importedServiceStatus != null) {
            try {
                representation = RepresentationUtils.toRepresentation(importedServiceStatus.left);
            } catch (IOException e) {
                log.debug("Error while building service representation : {}", e.getMessage(), e);
            }
            response = buildOkResponse(getComponentsUtils().getResponseFormat(importedServiceStatus.right), representation);
        }
        responseWrapper.setInnerElement(response);
    }

    private ImmutablePair<Service, ActionStatus> importServiceFromUICsar(UploadServiceInfo serviceInfoObject, User user, String serviceUniqueId)
        throws ZipException {
        Service newService;
        ImmutablePair<Service, ActionStatus> result = null;
        ActionStatus actionStatus;
        Service service = new Service();
        String payloadName = serviceInfoObject.getPayloadName();
        fillServiceFromServiceInfoObject(service, serviceInfoObject);
        Map<String, byte[]> csarUIPayloadRes = getCsarFromPayload(serviceInfoObject);
        getAndValidateCsarYaml(csarUIPayloadRes, service, user, payloadName);
        newService = serviceImportManager.getServiceImportBusinessLogic()
            .createService(service, AuditingActionEnum.CREATE_SERVICE, user, csarUIPayloadRes, payloadName);
        actionStatus = ActionStatus.CREATED;
        return new ImmutablePair<>(newService, actionStatus);
    }

    private void fillServiceFromServiceInfoObject(Service service, UploadServiceInfo serviceInfoObject) {
        serviceImportManager.populateServiceMetadata(serviceInfoObject, service);
        fillArtifacts(service, serviceInfoObject);
    }

    private Map<String, byte[]> getCsarFromPayload(UploadServiceInfo innerElement) throws ZipException {
        String csarUUID = innerElement.getPayloadName();
        String payloadData = innerElement.getPayloadData();
        return getComponentCsarFromPayload(csarUUID, payloadData);
    }

    private void getAndValidateCsarYaml(Map<String, byte[]> csarUIPayload, Service service, User user, String csarUUID) {
        getAndValidateComponentCsarYaml(csarUIPayload, service, user, csarUUID);
    }

    private void fillArtifacts(Service service, UploadServiceInfo serviceInfoObject) {
        if (service != null && serviceInfoObject != null) {
            List<UploadArtifactInfo> artifactList = serviceInfoObject.getArtifactList();
            if (artifactList != null) {
                Map<String, ArtifactDefinition> artifactsHM = new HashMap<>();
                buildArtifactsHM(artifactList, artifactsHM);
                service.setArtifacts(artifactsHM);
            }
        }
    }

    /**
     * import service payload to postman
     *
     * @param responseWrapper
     * @param uploadServiceInfoWrapper
     * @param yamlStringWrapper
     * @param user
     * @param serviceInfoJsonString
     * @param serviceAuthorityEnum
     * @param file
     * @throws ZipException
     */
    protected void fillServicePayload(Wrapper<Response> responseWrapper, Wrapper<UploadServiceInfo> uploadServiceInfoWrapper,
                                      Wrapper<String> yamlStringWrapper, User user, String serviceInfoJsonString,
                                      ServiceAuthorityTypeEnum serviceAuthorityEnum, File file) throws ZipException {
        log.debug("enter fillServicePayload");
        if (responseWrapper.isEmpty()) {
            log.debug("enter fillServicePayload,get responseWrapper is empty");
            if (serviceAuthorityEnum.isBackEndImport()) {
                // PrePayload Validations
                if (responseWrapper.isEmpty()) {
                    validateDataNotNull(responseWrapper, file, serviceInfoJsonString);
                }
                if (responseWrapper.isEmpty()) {
                    log.debug("enter fillServicePayload,responseWrapper is empty");
                }
                if (!serviceAuthorityEnum.equals(ServiceAuthorityTypeEnum.CSAR_TYPE_BE)) {
                    if (responseWrapper.isEmpty()) {
                        validateZip(responseWrapper, file, uploadServiceInfoWrapper.getInnerElement().getPayloadName());
                    }
                    // Fill PayLoad From File
                    if (responseWrapper.isEmpty()) {
                        fillToscaTemplateFromZip(yamlStringWrapper, uploadServiceInfoWrapper.getInnerElement().getPayloadName(), file);
                    }
                } else {
                    log.debug("enter fillServicePayload,ServiceAuthorityTypeEnum is CSAR_TYPE_BE");
                    if (responseWrapper.isEmpty()) {
                        validateCsar(responseWrapper, file, uploadServiceInfoWrapper.getInnerElement().getPayloadName());
                    }
                    if (!responseWrapper.isEmpty()) {
                        log.debug("enter fillServicePayload,get responseWrapper:{}", responseWrapper);
                    }
                    // Fill PayLoad From File
                    if (responseWrapper.isEmpty()) {
                        fillServicePayloadDataFromFile(responseWrapper, uploadServiceInfoWrapper.getInnerElement(), file);
                    }
                }
            } else {
                // Fill PayLoad From JSON
                if (responseWrapper.isEmpty()) {
                    fillServiceToscaTemplateFromJson(responseWrapper, yamlStringWrapper, user, uploadServiceInfoWrapper.getInnerElement());
                }
            }
        }
    }

    protected void fillServicePayloadDataFromFile(Wrapper<Response> responseWrapper, UploadServiceInfo uploadServiceInfoWrapper, File file) {
        try (InputStream fileInputStream = new FileInputStream(file)) {
            log.debug("enter fillServicePayloadDataFromFile");
            byte[] data = new byte[(int) file.length()];
            if (fileInputStream.read(data) == -1) {
                log.info("Invalid json was received.");
                ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
                Response errorResp = buildErrorResponse(responseFormat);
                responseWrapper.setInnerElement(errorResp);
            }
            String payloadData = Base64.encodeBase64String(data);
            uploadServiceInfoWrapper.setPayloadData(payloadData);
            log.debug("enter fillServicePayloadDataFromFile,get payloadData:{}", uploadServiceInfoWrapper.getPayloadData());
            log.debug("enter fillServicePayloadDataFromFile,get uploadService:{}", uploadServiceInfoWrapper);
        } catch (IOException e) {
            log.info("Invalid json was received or Error while closing input Stream.");
            log.debug("Invalid json was received or Error while closing input Stream. {}", e.getMessage(), e);
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
            Response errorResp = buildErrorResponse(responseFormat);
            responseWrapper.setInnerElement(errorResp);
        }
    }

    private void fillServiceToscaTemplateFromJson(Wrapper<Response> responseWrapper, Wrapper<String> yamlStringWrapper, User user,
                                                  UploadServiceInfo serviceInfo) {
        if (serviceInfo.getPayloadData() == null || serviceInfo.getPayloadData().isEmpty()) {
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_RESOURCE_PAYLOAD);
            Response errorResponse = buildErrorResponse(responseFormat);
            getComponentsUtils().auditResource(responseFormat, user, serviceInfo.getName(), AuditingActionEnum.IMPORT_RESOURCE);
            responseWrapper.setInnerElement(errorResponse);
        } else {
            String toscaPayload = serviceInfo.getPayloadData();
            String decodedPayload = new String(Base64.decodeBase64(toscaPayload));
            yamlStringWrapper.setInnerElement(decodedPayload);
        }
    }
}
