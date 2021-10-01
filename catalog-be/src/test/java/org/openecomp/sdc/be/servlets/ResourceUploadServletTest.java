/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.servlets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ModelBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.SpringConfig;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.Model;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.servlets.builder.ServletResponseBuilder;
import org.openecomp.sdc.be.servlets.exception.OperationExceptionMapper;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.WebApplicationContext;

class ResourceUploadServletTest extends JerseyTest {
    private static final String USER_ID = "cs0008";
    private static final User USER = new User(USER_ID);

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpSession session;
    @Mock
    private ServletContext servletContext;
    @Mock
    private WebAppContextWrapper webAppContextWrapper;
    @Mock
    private WebApplicationContext webApplicationContext;
    @Mock
    private UserBusinessLogic userBusinessLogic;
    @Mock
    private ComponentInstanceBusinessLogic componentInstanceBusinessLogic;
    @Mock
    private ComponentsUtils componentsUtils;
    @Mock
    private ServletUtils servletUtils;
    @Mock
    private ResourceImportManager resourceImportManager;
    @Mock
    private ResourceBusinessLogic resourceBusinessLogic;
    @Mock
    private ResponseFormat responseFormat;
    @Mock
    private ModelBusinessLogic modelBusinessLogic;
    @Mock
    private ResponseFormatManager responseFormatManager;
    private final String modelName = "ETSI-SOL001-331";

    private final Path rootPath = Path.of("/v1/catalog/upload");
    private final Path bulkImportPath = rootPath.resolve("resource/import");
    private final String multipartPath = "/v1/catalog/upload/multipart";
    private User user;

    @BeforeEach
    void resetMock() throws Exception {
        super.setUp();
        initMocks();
        initConfig();
        initTestData();
    }

    @AfterEach
    void after() throws Exception {
        super.tearDown();
    }

    @Override
    protected ResourceConfig configure() {
        MockitoAnnotations.openMocks(this);
        forceSet(TestProperties.CONTAINER_PORT, "0");
        final ApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class);
        return new ResourceConfig(ResourceUploadServlet.class)
            .register(new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(request).to(HttpServletRequest.class);
                    bind(userBusinessLogic).to(UserBusinessLogic.class);
                    bind(componentInstanceBusinessLogic).to(ComponentInstanceBusinessLogic.class);
                    bind(componentsUtils).to(ComponentsUtils.class);
                    bind(servletUtils).to(ServletUtils.class);
                    bind(resourceImportManager).to(ResourceImportManager.class);
                    bind(resourceBusinessLogic).to(ResourceBusinessLogic.class);
                    bind(modelBusinessLogic).to(ModelBusinessLogic.class);
                }
            })
            .register(new OperationExceptionMapper(new ServletResponseBuilder(), responseFormatManager))
            .register(MultiPartFeature.class)
            .property("contextConfig", context);
    }

    @Override
    protected void configureClient(final ClientConfig config) {
        config.register(MultiPartFeature.class);
    }

    void initMocks() {
        when(request.getSession()).thenReturn(session);
        when(session.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR))
            .thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webApplicationContext);
        when(webApplicationContext.getBean(ModelBusinessLogic.class)).thenReturn(modelBusinessLogic);
        when(webApplicationContext.getBean(UserBusinessLogic.class)).thenReturn(userBusinessLogic);
        when(userBusinessLogic.getUser(USER_ID, false)).thenReturn(USER);
        when(request.getHeader(Constants.USER_ID_HEADER)).thenReturn(USER_ID);
        when(webApplicationContext.getBean(ServletUtils.class)).thenReturn(servletUtils);
        when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);
    }

    void initConfig() {
        final String appConfigDir = "src/test/resources/config/catalog-be";
        final ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        final ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
        final org.openecomp.sdc.be.config.Configuration configuration = new org.openecomp.sdc.be.config.Configuration();
        configuration.setJanusGraphInMemoryGraph(true);
        configurationManager.setConfiguration(configuration);
        ExternalConfiguration.setAppName("catalog-be");
    }

    private void initTestData() {
        user = new User();
        user.setUserId(USER_ID);
        user.setRole(Role.ADMIN.name());
        when(userBusinessLogic.getUser(USER_ID)).thenReturn(user);
    }

    @Test
    void uploadMultipartWithModelSuccessTest() throws IOException, ParseException, URISyntaxException {
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.CREATED)).thenReturn(responseFormat);
        when(servletUtils.getUserAdmin()).thenReturn(userBusinessLogic);
        when(userBusinessLogic.getUser(anyString())).thenReturn(user);
        when(resourceBusinessLogic.validatePropertiesDefaultValues(any())).thenReturn(true);
        when(resourceImportManager.importNormativeResource(anyString(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean()))
            .thenReturn(new ImmutablePair<>(new Resource(), ActionStatus.CREATED));
        when(modelBusinessLogic.findModel(modelName)).thenReturn(Optional.of(new Model(modelName)));
        final var response = target().path(multipartPath).request(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, USER_ID)
            .post(Entity.entity(buildFormDataMultiPart("node-types/TestNodeType001.zip",
                "src/test/resources/node-types/nodeTypeWithModelsField.json"), MediaType.MULTIPART_FORM_DATA), Response.class);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    void uploadMultipartWithoutModelsFieldSuccessTest() throws IOException, ParseException, URISyntaxException {
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.CREATED)).thenReturn(responseFormat);
        when(servletUtils.getUserAdmin()).thenReturn(userBusinessLogic);
        when(userBusinessLogic.getUser(anyString())).thenReturn(user);
        when(resourceBusinessLogic.validatePropertiesDefaultValues(any())).thenReturn(true);
        when(resourceImportManager.importNormativeResource(anyString(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean()))
            .thenReturn(new ImmutablePair<>(new Resource(), ActionStatus.CREATED));
        final var response = target().path(multipartPath).request(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, USER_ID)
            .post(Entity.entity(buildFormDataMultiPart("node-types/TestNodeType002.zip",
                "src/test/resources/node-types/nodeTypeWithoutModelsField.json"), MediaType.MULTIPART_FORM_DATA), Response.class);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    void uploadMultipartFailWithEmptyModelsTest() throws IOException, ParseException, URISyntaxException {
        when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(responseFormat);
        when(servletUtils.getUserAdmin()).thenReturn(userBusinessLogic);
        when(userBusinessLogic.getUser(anyString())).thenReturn(user);
        when(resourceBusinessLogic.validatePropertiesDefaultValues(any())).thenReturn(true);
        when(modelBusinessLogic.findModel("")).thenReturn(Optional.empty());
        final Response response = target().path(multipartPath).request(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, USER_ID)
            .post(Entity.entity(buildFormDataMultiPart("node-types/TestNodeType002.zip",
                "src/test/resources/node-types/nodeTypeWithEmptyModels.json"), MediaType.MULTIPART_FORM_DATA), Response.class);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    void uploadMultipartFailWithModelNotFoundTest() throws IOException, ParseException, URISyntaxException {
        when(servletUtils.getUserAdmin()).thenReturn(userBusinessLogic);
        when(userBusinessLogic.getUser(anyString())).thenReturn(user);
        when(resourceBusinessLogic.validatePropertiesDefaultValues(any())).thenReturn(true);
        when(modelBusinessLogic.findModel(modelName)).thenReturn(Optional.empty());
        final var response = target().path(multipartPath).request(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, USER_ID)
            .post(Entity.entity(buildFormDataMultiPart("node-types/TestNodeType001.zip",
                "src/test/resources/node-types/nodeTypeWithModelsField.json"), MediaType.MULTIPART_FORM_DATA), Response.class);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    void uploadMultipartThrowsBusinessExceptionTest() throws IOException, ParseException, URISyntaxException {
        when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(responseFormat);
        when(servletUtils.getUserAdmin()).thenReturn(userBusinessLogic);
        when(userBusinessLogic.getUser(anyString())).thenReturn(user);
        when(resourceBusinessLogic.validatePropertiesDefaultValues(any())).thenReturn(true);
        final var response = target().path(multipartPath).request(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, USER_ID)
            .post(Entity.entity(buildFormDataMultiPart("node-types/TestNodeType001.zip",
                "src/test/resources/node-types/invalid.json"), MediaType.MULTIPART_FORM_DATA), Response.class);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    void bulkImportSuccessTest() throws IOException, ParseException {
        when(responseFormat.getStatus()).thenReturn(HttpStatus.CREATED_201);
        when(componentsUtils.getResponseFormat(ActionStatus.CREATED)).thenReturn(responseFormat);

        final Response response = doValidCallToBulkImport(USER_ID);

        verify(resourceImportManager).importAllNormativeResource(anyString(), any(), eq(USER), eq(false), eq(false));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED_201);
    }

    @Test
    void bulkImportFailTest_businessException() throws IOException, ParseException {
        doThrow(new ByActionStatusComponentException(ActionStatus.GENERAL_ERROR))
            .when(resourceImportManager)
            .importAllNormativeResource(anyString(), any(), eq(USER), eq(false), eq(false));

        final Response response = doValidCallToBulkImport(USER_ID);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    void bulkImportFailTest_unexpectedException() throws IOException, ParseException {
        when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(responseFormat);

        doThrow(RuntimeException.class)
            .when(resourceImportManager)
            .importAllNormativeResource(anyString(), any(), eq(USER), eq(false), eq(false));

        final Response response = doValidCallToBulkImport(USER_ID);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    void bulkImportFailTest_invalidUser() throws IOException, ParseException {
        var userId = "userId";
        when(userBusinessLogic.getUser(userId, false)).thenThrow(ComponentException.class);
        when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(componentsUtils.getResponseFormat(any(ComponentException.class))).thenReturn(responseFormat);

        final Response response = doValidCallToBulkImport(userId);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    private Response doValidCallToBulkImport(final String userId) throws IOException, ParseException {
        final Path nodeTypesYamlPath = Path.of("src/test/resources/node-types/nodeTypes.yaml");
        final Path nodeTypeMetadataJsonPath = Path.of("src/test/resources/node-types/payload.json");
        return target().path(bulkImportPath.toString()).request(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, userId)
            .post(
                Entity.entity(buildBulkImportMultiPartData(nodeTypesYamlPath, nodeTypeMetadataJsonPath), MediaType.MULTIPART_FORM_DATA),
                Response.class
            );
    }

    private String parseFileToJsonString(final Path jsonFilePath) throws IOException, ParseException {
        final JSONObject inputData = (JSONObject) new JSONParser().parse(
            new FileReader(jsonFilePath.toString()));
        return inputData.toJSONString();
    }

    private File getFile(final String fileName) throws URISyntaxException {
        final URL resource = this.getClass().getClassLoader().getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        }
        return new File(resource.toURI());
    }

    private FormDataMultiPart buildFormDataMultiPart(final String zipFilePath, final String inputJsonData)
        throws IOException, ParseException, URISyntaxException {
        final FileDataBodyPart filePart = new FileDataBodyPart("resourceZip", getFile(zipFilePath));
        final FormDataMultiPart multipartEntity = new FormDataMultiPart();
        multipartEntity.bodyPart(filePart);
        multipartEntity.field("resourceMetadata", parseFileToJsonString(Path.of(inputJsonData)));
        return  multipartEntity;
    }

    private FormDataMultiPart buildBulkImportMultiPartData(final Path nodeTypesYamlPath, final Path payloadJsonPath)
        throws IOException, ParseException {
        final FileDataBodyPart formDataBodyPart = new FileDataBodyPart("nodeTypesYaml", nodeTypesYamlPath.toFile());
        final FormDataMultiPart multipartEntity = new FormDataMultiPart();
        multipartEntity.bodyPart(formDataBodyPart);
        multipartEntity.field("nodeTypeMetadataJson", parseFileToJsonString(payloadJsonPath), MediaType.APPLICATION_JSON_TYPE);
        multipartEntity.field("createNewVersion", String.valueOf(false), MediaType.TEXT_PLAIN_TYPE);
        return multipartEntity;
    }
}
