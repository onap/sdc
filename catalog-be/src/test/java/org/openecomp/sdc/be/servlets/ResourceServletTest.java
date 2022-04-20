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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fj.data.Either;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.text.StrSubstitutor;
import org.apache.http.HttpStatus;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.GroupBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.SpringConfig;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.api.HighestFilterEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.DeleteActionEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.WebApplicationContext;

class ResourceServletTest extends JerseyTest {

    private final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    private final ResourceImportManager resourceImportManager = Mockito.mock(ResourceImportManager.class);
    private final HttpSession session = Mockito.mock(HttpSession.class);
    private final ServletContext servletContext = Mockito.mock(ServletContext.class);
    private final WebAppContextWrapper webAppContextWrapper = Mockito.mock(WebAppContextWrapper.class);
    private final WebApplicationContext webApplicationContext = Mockito.mock(WebApplicationContext.class);
    private final ServletUtils servletUtils = Mockito.mock(ServletUtils.class);
    private final ComponentsUtils componentUtils = Mockito.mock(ComponentsUtils.class);
    private final UserBusinessLogic userAdmin = Mockito.mock(UserBusinessLogic.class);
    private final UserBusinessLogic userBusinessLogic = Mockito.mock(UserBusinessLogic.class);
    private final GroupBusinessLogic groupBL = Mockito.mock(GroupBusinessLogic.class);
    private final ComponentInstanceBusinessLogic componentInstanceBL = Mockito.mock(ComponentInstanceBusinessLogic.class);
    private final ResourceBusinessLogic resourceBusinessLogic = Mockito.mock(ResourceBusinessLogic.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final ResponseFormat okResponseFormat = new ResponseFormat(HttpStatus.SC_OK);
    private static final ResponseFormat conflictResponseFormat = new ResponseFormat(HttpStatus.SC_CONFLICT);
    private static final ResponseFormat generalErrorResponseFormat = new ResponseFormat(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    private static final ResponseFormat createdResponseFormat = new ResponseFormat(HttpStatus.SC_CREATED);
    private static final ResponseFormat noContentResponseFormat = new ResponseFormat(HttpStatus.SC_NO_CONTENT);
    private static final ResponseFormat notFoundResponseFormat = new ResponseFormat(HttpStatus.SC_NOT_FOUND);
    private static final ResponseFormat badRequestResponseFormat = new ResponseFormat(HttpStatus.SC_BAD_REQUEST);
    private static final ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(),
        "src/test/resources/config/catalog-be");
    private static final ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
    private static final String RESOURCE_NAME = "resourceName";
    private static final String VERSION = "version";
    private static final String RESOURCE_ID = "resourceId";
    private static final String RESOURCE_VERSION = "resourceVersion";
    private static final String SUBTYPE = "subtype";
    private static final String CSAR_UUID = "csaruuid";
    private static final String EMPTY_JSON = "{}";
    private static final String NON_UI_IMPORT_JSON = "{\n" +
        "  \"node1\": \"value1\",\n" +
        "  \"node2\": {\n" +
        "    \"level21\": \"value21\",\n" +
        "    \"level22\": \"value22\"\n" +
        "  }\n" +
        "}";
    private static User user;

    @BeforeAll
    public static void setup() {
        ExternalConfiguration.setAppName("catalog-be");
    }

    @AfterEach
    void after() throws Exception {
        super.tearDown();
    }

    @BeforeEach
    public void beforeTest() throws Exception {
        super.setUp();
        Mockito.reset(componentUtils);
        Mockito.reset(resourceBusinessLogic);

        when(request.getSession()).thenReturn(session);
        when(session.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webApplicationContext);
        when(webApplicationContext.getBean(ResourceImportManager.class)).thenReturn(resourceImportManager);
        when(webApplicationContext.getBean(ServletUtils.class)).thenReturn(servletUtils);
        when(servletUtils.getComponentsUtils()).thenReturn(componentUtils);
        when(servletUtils.getUserAdmin()).thenReturn(userAdmin);
        String userId = "jh0003";
        user = new User();
        user.setUserId(userId);
        user.setRole(Role.ADMIN.name());
        when(userAdmin.getUser(userId)).thenReturn(user);
        when(request.getHeader(Constants.USER_ID_HEADER)).thenReturn(userId);
        ImmutablePair<Resource, ActionStatus> pair = new ImmutablePair<>(new Resource(), ActionStatus.OK);
        when(resourceImportManager
            .importUserDefinedResource(Mockito.anyString(), Mockito.any(UploadResourceInfo.class), Mockito.any(User.class), Mockito.anyBoolean()))
            .thenReturn(pair);
        when(webApplicationContext.getBean(ResourceBusinessLogic.class)).thenReturn(resourceBusinessLogic);

        when(componentUtils.getResponseFormat(ActionStatus.OK)).thenReturn(okResponseFormat);
        when(componentUtils.getResponseFormat(ActionStatus.CREATED)).thenReturn(createdResponseFormat);
        when(componentUtils.getResponseFormat(ActionStatus.NO_CONTENT)).thenReturn(noContentResponseFormat);
        when(componentUtils.getResponseFormat(ActionStatus.INVALID_CONTENT)).thenReturn(badRequestResponseFormat);
        when(componentUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(generalErrorResponseFormat);
        when(componentUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND)).thenReturn(notFoundResponseFormat);
    }

    @Test
    void testHappyScenarioTest() {
        when(componentUtils.getResponseFormat(ActionStatus.OK)).thenReturn(createdResponseFormat);

        UploadResourceInfo validJson = buildValidJson();
        setMD5OnRequest(true, validJson);
        Response response = target().path("/v1/catalog/resources").request(MediaType.APPLICATION_JSON)
            .post(Entity.json(gson.toJson(validJson)), Response.class);
        Mockito.verify(componentUtils, Mockito.times(1)).getResponseFormat(Mockito.any(ActionStatus.class));
        Mockito.verify(componentUtils, Mockito.times(1)).getResponseFormat(ActionStatus.OK);
        assertEquals(HttpStatus.SC_CREATED, response.getStatus());

    }

    @Test
    void testNonValidMd5Fail() {
        UploadResourceInfo validJson = buildValidJson();

        setMD5OnRequest(false, validJson);

        Response response = target().path("/v1/catalog/resources").request(MediaType.APPLICATION_JSON)
            .post(Entity.json(gson.toJson(validJson)), Response.class);
        Mockito.verify(componentUtils, Mockito.times(1)).getResponseFormat(Mockito.any(ActionStatus.class));
        Mockito.verify(componentUtils, Mockito.times(1)).getResponseFormat(ActionStatus.INVALID_RESOURCE_CHECKSUM);
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatus());

    }

    @Test
    void testNonValidPayloadNameFail() {
        UploadResourceInfo mdJson = buildValidJson();
        mdJson.setPayloadName("myCompute.xml");

        runAndVerifyActionStatusError(mdJson, ActionStatus.INVALID_TOSCA_FILE_EXTENSION);

    }

    @Test
    void testNullPayloadFail() {
        UploadResourceInfo mdJson = buildValidJson();
        mdJson.setPayloadData(null);
        runAndVerifyActionStatusError(mdJson, ActionStatus.INVALID_RESOURCE_PAYLOAD);

    }

    @Test
    void testNonYmlPayloadFail() {
        UploadResourceInfo mdJson = buildValidJson();
        String payload = "{ json : { isNot : yaml } ";
        encodeAndSetPayload(mdJson, payload);
        runAndVerifyActionStatusError(mdJson, ActionStatus.INVALID_YAML_FILE);

    }

    @Test
    void testNonToscaPayloadFail() {
        UploadResourceInfo mdJson = buildValidJson();

        String payload = "node_types: \r\n" + "  org.openecomp.resource.importResource4test:\r\n" + "    derived_from: tosca.nodes.Root\r\n"
            + "    description: update update";
        encodeAndSetPayload(mdJson, payload);
        runAndVerifyActionStatusError(mdJson, ActionStatus.INVALID_TOSCA_TEMPLATE);

    }

    @Test
    void testServiceToscaPayloadFail() {
        UploadResourceInfo mdJson = buildValidJson();

        String payload =
            "tosca_definitions_version: tosca_simple_yaml_1_0_0\r\n" + "node_types: \r\n" + "  org.openecomp.resource.importResource4test:\r\n"
                + "    derived_from: tosca.nodes.Root\r\n" + "    topology_template: thisIsService\r\n"
                + "    description: update update";

        encodeAndSetPayload(mdJson, payload);
        runAndVerifyActionStatusError(mdJson, ActionStatus.NOT_RESOURCE_TOSCA_TEMPLATE);

    }

    @Test
    void testMultipleResourcesInPayloadFail() {
        UploadResourceInfo mdJson = buildValidJson();

        String payload =
            "tosca_definitions_version: tosca_simple_yaml_1_0_0\r\n" + "node_types: \r\n" + "  org.openecomp.resource.importResource4test2:\r\n"
                + "    derived_from: tosca.nodes.Root\r\n" + "  org.openecomp.resource.importResource4test:\r\n"
                + "    derived_from: tosca.nodes.Root\r\n" + "    description: update update";

        encodeAndSetPayload(mdJson, payload);
        runAndVerifyActionStatusError(mdJson, ActionStatus.NOT_SINGLE_RESOURCE);

    }

    @Test
    void testNonValidNameSpaceInPayloadFail() {
        UploadResourceInfo mdJson = buildValidJson();

        String payload = "tosca_definitions_version: tosca_simple_yaml_1_0_0\r\n" + "node_types: \r\n"
            + "  org.openecomp.resourceX.importResource4test:\r\n" + "    derived_from: tosca.nodes.Root\r\n"
            + "    description: update update";

        encodeAndSetPayload(mdJson, payload);
        runAndVerifyActionStatusError(mdJson, ActionStatus.INVALID_RESOURCE_NAMESPACE);
    }

    @Test
    void deleteResourceTryDeleteNonExistingResourceTest() {
        String resourceId = "resourceId";
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put("resourceId", resourceId);

        String formatEndpoint = "/v1/catalog/resources/{resourceId}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        when(resourceBusinessLogic.deleteResource(any(), any(User.class), any(DeleteActionEnum.class)))
            .thenReturn(notFoundResponseFormat);

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .delete();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void deleteResourceExceptionDuringDeletingTest() {
        String resourceId = RESOURCE_ID;
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(RESOURCE_ID, resourceId);

        String formatEndpoint = "/v1/catalog/resources/{resourceId}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        when(resourceBusinessLogic.deleteResource(any(), any(User.class), any(DeleteActionEnum.class)))
            .thenThrow(new JSONException("Test exception: deleteResource"));

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .delete();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    void deleteResourceCategoryTest() {
        String resourceId = "resourceId";
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put("resourceId", resourceId);

        String formatEndpoint = "/v1/catalog/resources/{resourceId}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        when(resourceBusinessLogic.deleteResource(eq(resourceId.toLowerCase()), any(User.class), any(DeleteActionEnum.class)))
            .thenReturn(noContentResponseFormat);

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .delete();

        assertThat(response.getStatus()).isEqualTo(org.apache.http.HttpStatus.SC_NO_CONTENT);
    }

    @Test
    void deleteResourceByNameAndVersionTryDeleteNonExistingResourceTest() {
        String resourceName = RESOURCE_NAME;
        String version = VERSION;
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(RESOURCE_NAME, resourceName);
        parametersMap.put(VERSION, version);

        String formatEndpoint = "/v1/catalog/resources/{resourceName}/{version}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        when(resourceBusinessLogic.deleteResourceByNameAndVersion(eq(resourceName), eq(version), any(User.class)))
            .thenReturn(notFoundResponseFormat);

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .delete();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void deleteResourceByNameAndVersionExceptionDuringDeletingTest() {
        String resourceName = RESOURCE_NAME;
        String version = VERSION;
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(RESOURCE_NAME, resourceName);
        parametersMap.put(VERSION, version);

        String formatEndpoint = "/v1/catalog/resources/{resourceName}/{version}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        when(resourceBusinessLogic.deleteResourceByNameAndVersion(eq(resourceName), eq(version), any(User.class)))
            .thenThrow(new JSONException("Test exception: deleteResourceByNameAndVersion"));

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .delete();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    void deleteResourceByNameAndVersionCategoryTest() {
        String resourceName = RESOURCE_NAME;
        String version = VERSION;
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(RESOURCE_NAME, resourceName);
        parametersMap.put(VERSION, version);

        String formatEndpoint = "/v1/catalog/resources/{resourceName}/{version}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        when(resourceBusinessLogic.deleteResourceByNameAndVersion(eq(resourceName), eq(version), any(User.class)))
            .thenReturn(noContentResponseFormat);

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .delete();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    void getResourceByIdTryGetNonExistingResourceTest() {
        String resourceId = RESOURCE_ID;
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(RESOURCE_ID, resourceId);

        String formatEndpoint = "/v1/catalog/resources/{resourceId}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        Either<Resource, ResponseFormat> getResourceByIdEither = Either.right(notFoundResponseFormat);
        when(resourceBusinessLogic.getResource(eq(resourceId.toLowerCase()), any(User.class)))
            .thenReturn(getResourceByIdEither);

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void getResourceByIdExceptionDuringSearchingTest() {
        String resourceId = RESOURCE_ID;
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(RESOURCE_ID, resourceId);

        String formatEndpoint = "/v1/catalog/resources/{resourceId}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        given(resourceBusinessLogic.getResource(eq(resourceId.toLowerCase()), any(User.class)))
            .willAnswer(invocation -> {
                throw new IOException("Test exception: getResourceById");
            });

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    void getResourceByIdTest() {
        String resourceId = RESOURCE_ID;
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(RESOURCE_ID, resourceId);

        String formatEndpoint = "/v1/catalog/resources/{resourceId}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        Either<Resource, ResponseFormat> getResourceByIdEither = Either.left(new Resource());
        when(resourceBusinessLogic.getResource(eq(resourceId.toLowerCase()), any(User.class)))
            .thenReturn(getResourceByIdEither);

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    void getResourceByNameAndVersionTryGetNonExistingResourceTest() {
        String resourceName = RESOURCE_NAME;
        String resourceVersion = RESOURCE_VERSION;
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(RESOURCE_NAME, resourceName);
        parametersMap.put(RESOURCE_VERSION, resourceVersion);

        String formatEndpoint = "/v1/catalog/resources/resourceName/{resourceName}/resourceVersion/{resourceVersion}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        Either<Resource, ResponseFormat> getResourceByNameAndVersionEither = Either.right(notFoundResponseFormat);
        when(resourceBusinessLogic.getResourceByNameAndVersion(eq(resourceName), eq(resourceVersion), eq(user.getUserId())))
            .thenReturn(getResourceByNameAndVersionEither);

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void getResourceByNameAndVersionExceptionDuringSearchingTest() {
        String resourceName = RESOURCE_NAME;
        String resourceVersion = RESOURCE_VERSION;
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(RESOURCE_NAME, resourceName);
        parametersMap.put(RESOURCE_VERSION, resourceVersion);

        String formatEndpoint = "/v1/catalog/resources/resourceName/{resourceName}/resourceVersion/{resourceVersion}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        given(resourceBusinessLogic.getResourceByNameAndVersion(eq(resourceName), eq(resourceVersion), eq(user.getUserId())))
            .willAnswer(invocation -> {
                throw new IOException("Test exception: getResourceByNameAndVersion");
            });

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    void getResourceByNameAndVersionTest() {
        String resourceName = RESOURCE_NAME;
        String resourceVersion = RESOURCE_VERSION;
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(RESOURCE_NAME, resourceName);
        parametersMap.put(RESOURCE_VERSION, resourceVersion);

        String formatEndpoint = "/v1/catalog/resources/resourceName/{resourceName}/resourceVersion/{resourceVersion}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        Either<Resource, ResponseFormat> getResourceByNameAndVersionEither = Either.left(new Resource());
        when(resourceBusinessLogic.getResourceByNameAndVersion(eq(resourceName), eq(resourceVersion), eq(user.getUserId())))
            .thenReturn(getResourceByNameAndVersionEither);

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    void validateResourceNameTryValidateNonExistingResourceTest() {
        String resourceName = RESOURCE_NAME;
        String resourceType = "VFC";
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(RESOURCE_NAME, resourceName);

        String formatEndpoint = "/v1/catalog/resources/validate-name/{resourceName}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        Either<Map<String, Boolean>, ResponseFormat> validateResourceNameEither =
            Either.right(notFoundResponseFormat);
        ResourceTypeEnum resourceTypeEnum = ResourceTypeEnum.valueOf(resourceType);
        when(resourceBusinessLogic.validateResourceNameExists(eq(resourceName), eq(resourceTypeEnum), eq(user.getUserId())))
            .thenReturn(validateResourceNameEither);

        Response response = target()
            .path(path)
            .queryParam(SUBTYPE, resourceType)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void validateResourceNameInvalidContentTest() {
        String resourceName = RESOURCE_NAME;
        String resourceType = "ThisIsInvalid";
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(RESOURCE_NAME, resourceName);

        String formatEndpoint = "/v1/catalog/resources/validate-name/{resourceName}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        Response response = target()
            .path(path)
            .queryParam(SUBTYPE, resourceType)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void validateResourceNameTest() {
        String resourceName = RESOURCE_NAME;
        String resourceType = "VFC";
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(RESOURCE_NAME, resourceName);

        String formatEndpoint = "/v1/catalog/resources/validate-name/{resourceName}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        Either<Map<String, Boolean>, ResponseFormat> validateResourceNameEither =
            Either.left(new HashMap<>());
        ResourceTypeEnum resourceTypeEnum = ResourceTypeEnum.valueOf(resourceType);
        when(resourceBusinessLogic.validateResourceNameExists(eq(resourceName), eq(resourceTypeEnum), eq(user.getUserId())))
            .thenReturn(validateResourceNameEither);

        Response response = target()
            .path(path)
            .queryParam(SUBTYPE, resourceType)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    void getCertifiedAbstractResourcesExceptionDuringSearchingTest() {
        String path = "/v1/catalog/resources/certified/abstract";
        given(resourceBusinessLogic.getAllCertifiedResources(eq(true), eq(HighestFilterEnum.HIGHEST_ONLY),
            eq(user.getUserId())))
            .willAnswer(invocation -> {
                throw new IOException("Test exception: getCertifiedAbstractResources");
            });

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    void getCertifiedAbstractResourcesTest() {
        String path = "/v1/catalog/resources/certified/abstract";

        List<Resource> resources = Arrays.asList(new Resource(), new Resource());
        when(resourceBusinessLogic.getAllCertifiedResources(eq(true), eq(HighestFilterEnum.HIGHEST_ONLY),
            eq(user.getUserId())))
            .thenReturn(resources);

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    void getCertifiedNotAbstractResourcesExceptionDuringSearchingTest() {
        String path = "/v1/catalog/resources/certified/notabstract";
        given(resourceBusinessLogic.getAllCertifiedResources(eq(false), eq(HighestFilterEnum.ALL),
            eq(user.getUserId())))
            .willAnswer(invocation -> {
                throw new IOException("Test exception: getCertifiedNotAbstractResources");
            });

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    void getCertifiedNotAbstractResourcesTest() {
        String path = "/v1/catalog/resources/certified/notabstract";

        List<Resource> resources = Arrays.asList(new Resource(), new Resource());
        when(resourceBusinessLogic.getAllCertifiedResources(eq(true), eq(HighestFilterEnum.ALL),
            eq(user.getUserId())))
            .thenReturn(resources);

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    void updateResourceMetadataTryUpdateNonExistingResourceTest() {
        String resourceId = RESOURCE_ID;
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(RESOURCE_ID, resourceId);

        String formatEndpoint = "/v1/catalog/resources/{resourceId}/metadata";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        Either<Resource, ResponseFormat> updateResourceMetadataEither = Either.right(badRequestResponseFormat);

        when(componentUtils.convertJsonToObjectUsingObjectMapper(any(), any(), eq(Resource.class),
            eq(AuditingActionEnum.UPDATE_RESOURCE_METADATA), eq(ComponentTypeEnum.RESOURCE)))
            .thenReturn(updateResourceMetadataEither);

        when(resourceBusinessLogic.updateResourceMetadata(eq(resourceId.toLowerCase()), any(), any(), any(User.class),
            eq(false)))
            .thenReturn(new Resource());

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .put(Entity.json(EMPTY_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void updateResourceMetadataExceptionDuringUpdateTest() {
        String resourceId = RESOURCE_ID;
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(RESOURCE_ID, resourceId);

        String formatEndpoint = "/v1/catalog/resources/{resourceId}/metadata";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        given(componentUtils.convertJsonToObjectUsingObjectMapper(any(), any(), eq(Resource.class),
            eq(AuditingActionEnum.UPDATE_RESOURCE_METADATA), eq(ComponentTypeEnum.RESOURCE)))
            .willAnswer(invocation -> {
                throw new IOException("Test exception: updateResourceMetadata");
            });

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .put(Entity.json(EMPTY_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    void updateResourceMetadataCategoryTest() {
        String resourceId = RESOURCE_ID;
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(RESOURCE_ID, resourceId);

        String formatEndpoint = "/v1/catalog/resources/{resourceId}/metadata";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        Resource initialResource = new Resource();
        Either<Resource, ResponseFormat> updateResourceMetadataEither = Either.left(initialResource);

        when(componentUtils.convertJsonToObjectUsingObjectMapper(any(), any(), eq(Resource.class),
            eq(AuditingActionEnum.UPDATE_RESOURCE_METADATA), eq(ComponentTypeEnum.RESOURCE)))
            .thenReturn(updateResourceMetadataEither);

        when(resourceBusinessLogic.updateResourceMetadata(eq(resourceId.toLowerCase()), eq(initialResource), any(),
            any(User.class), eq(false)))
            .thenReturn(new Resource());

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .put(Entity.json(EMPTY_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    void updateResourceParsingUncussessfulTest() {
        String resourceId = RESOURCE_ID;
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(RESOURCE_ID, resourceId);

        String formatEndpoint = "/v1/catalog/resources/{resourceId}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        Either<Resource, ResponseFormat> updateResourceEither = Either.right(badRequestResponseFormat);

        when(componentUtils.convertJsonToObjectUsingObjectMapper(eq(NON_UI_IMPORT_JSON), any(User.class),
            eq(Resource.class), eq(AuditingActionEnum.UPDATE_RESOURCE_METADATA), eq(ComponentTypeEnum.RESOURCE)))
            .thenReturn(updateResourceEither);

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .put(Entity.json(NON_UI_IMPORT_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void updateResourceExceptionDuringUpdateTest() {
        String resourceId = RESOURCE_ID;
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(RESOURCE_ID, resourceId);

        String formatEndpoint = "/v1/catalog/resources/{resourceId}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        given(componentUtils.convertJsonToObjectUsingObjectMapper(eq(NON_UI_IMPORT_JSON), any(User.class),
            eq(Resource.class), eq(AuditingActionEnum.UPDATE_RESOURCE_METADATA), eq(ComponentTypeEnum.RESOURCE)))
            .willAnswer(invocation -> {
                throw new IOException("Test exception: updateResource");
            });

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .put(Entity.json(NON_UI_IMPORT_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    void updateResourceNonUiImportTest() {
        String resourceId = RESOURCE_ID;
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(RESOURCE_ID, resourceId);

        String formatEndpoint = "/v1/catalog/resources/{resourceId}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        Either<Resource, ResponseFormat> updateResourceEither = Either.left(new Resource());

        when(componentUtils.convertJsonToObjectUsingObjectMapper(eq(NON_UI_IMPORT_JSON), any(User.class), eq(Resource.class),
            eq(AuditingActionEnum.UPDATE_RESOURCE_METADATA), eq(ComponentTypeEnum.RESOURCE)))
            .thenReturn(updateResourceEither);

        when(resourceBusinessLogic.validateAndUpdateResourceFromCsar(any(), any(), any(), any(), eq(resourceId)))
            .thenReturn(new Resource());

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .put(Entity.json(NON_UI_IMPORT_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    void getResourceFromCsarTryGetNonExistingResourceTest() {
        String csarUuid = CSAR_UUID;
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(CSAR_UUID, csarUuid);

        String formatEndpoint = "/v1/catalog/resources/csar/{csaruuid}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        Either<Resource, ResponseFormat> getResourceFromCsarEither = Either.right(notFoundResponseFormat);
        when(resourceBusinessLogic.getLatestResourceFromCsarUuid(eq(csarUuid), any(User.class)))
            .thenReturn(getResourceFromCsarEither);

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void getResourceFromCsarExceptionDuringGettingTest() {
        String csarUuid = CSAR_UUID;
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(CSAR_UUID, csarUuid);

        String formatEndpoint = "/v1/catalog/resources/csar/{csaruuid}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        given(resourceBusinessLogic.getLatestResourceFromCsarUuid(eq(csarUuid), any(User.class)))
            .willAnswer(invocation -> {
                throw new IOException("Test exception: getResourceFromCsar");
            });

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    void getResourceFromCsarTest() {
        String csarUuid = CSAR_UUID;
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(CSAR_UUID, csarUuid);

        String formatEndpoint = "/v1/catalog/resources/csar/{csaruuid}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        Either<Resource, ResponseFormat> getResourceFromCsarEither = Either.left(new Resource());
        when(resourceBusinessLogic.getLatestResourceFromCsarUuid(eq(csarUuid), any(User.class)))
            .thenReturn(getResourceFromCsarEither);

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    void createResourceExceptionDuringCreateTest() {
        String path = "/v1/catalog/resources";

        given(componentUtils.convertJsonToObjectUsingObjectMapper(eq(NON_UI_IMPORT_JSON), any(User.class),
            eq(Resource.class), eq(AuditingActionEnum.CREATE_RESOURCE), eq(ComponentTypeEnum.RESOURCE)))
            .willAnswer(invocation -> {
                throw new IOException("Test exception: createResource");
            });

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .post(Entity.json(NON_UI_IMPORT_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    void createResourceNonUiImportProcessingFailedTest() {
        String path = "/v1/catalog/resources";

        Either<Resource, ResponseFormat> createResourceEither = Either.right(badRequestResponseFormat);

        when(componentUtils.convertJsonToObjectUsingObjectMapper(eq(NON_UI_IMPORT_JSON), any(User.class),
            eq(Resource.class), eq(AuditingActionEnum.CREATE_RESOURCE), eq(ComponentTypeEnum.RESOURCE)))
            .thenReturn(createResourceEither);

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .post(Entity.json(NON_UI_IMPORT_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void createResourceNonUiImportTest() {
        String path = "/v1/catalog/resources";

        Either<Resource, ResponseFormat> createResourceEither = Either.left(new Resource());

        when(componentUtils.convertJsonToObjectUsingObjectMapper(eq(NON_UI_IMPORT_JSON), any(User.class),
            eq(Resource.class), eq(AuditingActionEnum.CREATE_RESOURCE), eq(ComponentTypeEnum.RESOURCE)))
            .thenReturn(createResourceEither);

        when(resourceBusinessLogic.createResource(eq(createResourceEither.left().value()), eq(AuditingActionEnum.CREATE_RESOURCE),
            any(User.class), any(), any()))
            .thenReturn(new Resource());

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, user.getUserId())
            .post(Entity.json(NON_UI_IMPORT_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_CREATED);
    }

    private void encodeAndSetPayload(UploadResourceInfo mdJson, String payload) {
        byte[] encodedBase64Payload = Base64.encodeBase64(payload.getBytes());
        mdJson.setPayloadData(new String(encodedBase64Payload));
    }

    private void runAndVerifyActionStatusError(UploadResourceInfo mdJson, ActionStatus invalidResourcePayload) {
        setMD5OnRequest(true, mdJson);
        Response response = target().path("/v1/catalog/resources").request(MediaType.APPLICATION_JSON)
            .post(Entity.json(gson.toJson(mdJson)), Response.class);
        Mockito.verify(componentUtils, Mockito.times(1)).getResponseFormat(Mockito.any(ActionStatus.class));
        Mockito.verify(componentUtils, Mockito.times(1)).getResponseFormat(invalidResourcePayload);
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatus());
    }

    private void setMD5OnRequest(boolean isValid, UploadResourceInfo json) {
        String md5 = (isValid) ? GeneralUtility.calculateMD5Base64EncodedByString(gson.toJson(json)) : "stam=";
        when(request.getHeader(Constants.MD5_HEADER)).thenReturn(md5);

    }

    private UploadResourceInfo buildValidJson() {
        UploadResourceInfo ret = new UploadResourceInfo();
        ret.setName("ciMyCompute");
        ret.setPayloadName("ciMyCompute.yml");
        ret.addSubCategory("Application Layer 4+", "Application Servers");
        ret.setDescription("ResourceDescription");
        ret.setVendorName("VendorName");
        ret.setVendorRelease("VendorRelease");
        ret.setContactId("AT1234");
        ret.setIcon("router");
        ret.setTags(Collections.singletonList("ciMyCompute"));
        ret.setPayloadData(
            "dG9zY2FfZGVmaW5pdGlvbnNfdmVyc2lvbjogdG9zY2Ffc2ltcGxlX3lhbWxfMV8wXzANCm5vZGVfdHlwZXM6IA0KICBvcmcub3BlbmVjb21wLnJlc291cmNlLk15Q29tcHV0ZToNCiAgICBkZXJpdmVkX2Zyb206IHRvc2NhLm5vZGVzLlJvb3QNCiAgICBhdHRyaWJ1dGVzOg0KICAgICAgcHJpdmF0ZV9hZGRyZXNzOg0KICAgICAgICB0eXBlOiBzdHJpbmcNCiAgICAgIHB1YmxpY19hZGRyZXNzOg0KICAgICAgICB0eXBlOiBzdHJpbmcNCiAgICAgIG5ldHdvcmtzOg0KICAgICAgICB0eXBlOiBtYXANCiAgICAgICAgZW50cnlfc2NoZW1hOg0KICAgICAgICAgIHR5cGU6IHRvc2NhLmRhdGF0eXBlcy5uZXR3b3JrLk5ldHdvcmtJbmZvDQogICAgICBwb3J0czoNCiAgICAgICAgdHlwZTogbWFwDQogICAgICAgIGVudHJ5X3NjaGVtYToNCiAgICAgICAgICB0eXBlOiB0b3NjYS5kYXRhdHlwZXMubmV0d29yay5Qb3J0SW5mbw0KICAgIHJlcXVpcmVtZW50czoNCiAgICAgIC0gbG9jYWxfc3RvcmFnZTogDQogICAgICAgICAgY2FwYWJpbGl0eTogdG9zY2EuY2FwYWJpbGl0aWVzLkF0dGFjaG1lbnQNCiAgICAgICAgICBub2RlOiB0b3NjYS5ub2Rlcy5CbG9ja1N0b3JhZ2UNCiAgICAgICAgICByZWxhdGlvbnNoaXA6IHRvc2NhLnJlbGF0aW9uc2hpcHMuQXR0YWNoZXNUbw0KICAgICAgICAgIG9jY3VycmVuY2VzOiBbMCwgVU5CT1VOREVEXSAgDQogICAgY2FwYWJpbGl0aWVzOg0KICAgICAgaG9zdDogDQogICAgICAgIHR5cGU6IHRvc2NhLmNhcGFiaWxpdGllcy5Db250YWluZXINCiAgICAgICAgdmFsaWRfc291cmNlX3R5cGVzOiBbdG9zY2Eubm9kZXMuU29mdHdhcmVDb21wb25lbnRdIA0KICAgICAgZW5kcG9pbnQgOg0KICAgICAgICB0eXBlOiB0b3NjYS5jYXBhYmlsaXRpZXMuRW5kcG9pbnQuQWRtaW4gDQogICAgICBvczogDQogICAgICAgIHR5cGU6IHRvc2NhLmNhcGFiaWxpdGllcy5PcGVyYXRpbmdTeXN0ZW0NCiAgICAgIHNjYWxhYmxlOg0KICAgICAgICB0eXBlOiB0b3NjYS5jYXBhYmlsaXRpZXMuU2NhbGFibGUNCiAgICAgIGJpbmRpbmc6DQogICAgICAgIHR5cGU6IHRvc2NhLmNhcGFiaWxpdGllcy5uZXR3b3JrLkJpbmRhYmxl");
        return ret;
    }

    @Override
    protected Application configure() {
        ApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class);
        forceSet(TestProperties.CONTAINER_PORT, "0");
        return new ResourceConfig(ResourcesServlet.class)
            .register(new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(request).to(HttpServletRequest.class);
                    bind(servletUtils).to(ServletUtils.class);
                    bind(componentUtils).to(ComponentsUtils.class);
                    bind(userBusinessLogic).to(UserBusinessLogic.class);
                    bind(resourceBusinessLogic).to(ResourceBusinessLogic.class);
                    bind(groupBL).to(GroupBusinessLogic.class);
                    bind(componentInstanceBL).to(ComponentInstanceBusinessLogic.class);
                    bind(resourceImportManager).to(ResourceImportManager.class);
                }
            })
            .register(MultiPartFeature.class)
            .property("contextConfig", context);
    }
}
