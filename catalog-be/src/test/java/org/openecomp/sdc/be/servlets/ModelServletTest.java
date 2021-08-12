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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ModelBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.SpringConfig;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.exception.BusinessException;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.Model;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception.OperationException;
import org.openecomp.sdc.be.servlets.builder.ServletResponseBuilder;
import org.openecomp.sdc.be.servlets.exception.OperationExceptionMapper;
import org.openecomp.sdc.be.ui.model.ModelCreateRequest;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.WebApplicationContext;

class ModelServletTest extends JerseyTest {

    private static final String USER_ID = "cs0008";

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
    private ModelBusinessLogic modelBusinessLogic;
    @Mock
    private ResponseFormat responseFormat;
    @Mock
    private UserValidations userValidations;

    @Mock
    private ResponseFormatManager responseFormatManager;

    private Model model;
    private ModelCreateRequest modelCreateRequest;
    private final Path rootPath = Path.of("/v1/catalog/model");
    private final Path importsPath = rootPath.resolve("imports");

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

    private void initMocks() {
        when(request.getSession()).thenReturn(session);
        when(session.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR))
            .thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webApplicationContext);
        when(webApplicationContext.getBean(ModelBusinessLogic.class)).thenReturn(modelBusinessLogic);
        when(request.getHeader(Constants.USER_ID_HEADER)).thenReturn(USER_ID);
        when(webApplicationContext.getBean(ServletUtils.class)).thenReturn(servletUtils);
        when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);
    }

    private void initConfig() {
        final String appConfigDir = "src/test/resources/config/catalog-be";
        final ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        final ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
        final org.openecomp.sdc.be.config.Configuration configuration = new org.openecomp.sdc.be.config.Configuration();
        configuration.setJanusGraphInMemoryGraph(true);
        configurationManager.setConfiguration(configuration);
        ExternalConfiguration.setAppName("catalog-be");
    }

    private void initTestData() {
        final String modelName = "MY-INTEGRATION-TEST-MODEL";
        model = new Model(modelName);
        modelCreateRequest = new ModelCreateRequest();
        modelCreateRequest.setName(modelName);
    }

    @Override
    protected ResourceConfig configure() {
        MockitoAnnotations.openMocks(this);
        forceSet(TestProperties.CONTAINER_PORT, "0");
        final ApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class);
        return new ResourceConfig(ModelServlet.class)
            .register(new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(request).to(HttpServletRequest.class);
                    bind(userBusinessLogic).to(UserBusinessLogic.class);
                    bind(componentInstanceBusinessLogic).to(ComponentInstanceBusinessLogic.class);
                    bind(componentsUtils).to(ComponentsUtils.class);
                    bind(servletUtils).to(ServletUtils.class);
                    bind(resourceImportManager).to(ResourceImportManager.class);
                    bind(modelBusinessLogic).to(ModelBusinessLogic.class);
                    bind(userValidations).to(UserValidations.class);
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

    @Test
    void createModelSuccessTest() throws JsonProcessingException {
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.CREATED)).thenReturn(responseFormat);
        when(modelBusinessLogic.createModel(any(Model.class))).thenReturn(model);
        final FormDataMultiPart formDataMultiPart = buildCreateFormDataMultiPart(new byte[0], parseToJsonString(modelCreateRequest));
        final var response = target(rootPath.toString()).request(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, USER_ID)
            .post(Entity.entity(formDataMultiPart, MediaType.MULTIPART_FORM_DATA));
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
    }
    
    @Test
    void createModelWithDerivedFromSuccessTest() throws JsonProcessingException {
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.CREATED)).thenReturn(responseFormat);
        when(modelBusinessLogic.createModel(any(Model.class))).thenReturn(model);
        ModelCreateRequest derviedModelCreateRequest = new ModelCreateRequest();
        derviedModelCreateRequest.setName("derivedModel");
        derviedModelCreateRequest.setDerivedFrom(model.getName());
        final FormDataMultiPart formDataMultiPart = buildCreateFormDataMultiPart(new byte[0], parseToJsonString(derviedModelCreateRequest));
        final var response = target(rootPath.toString()).request(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, USER_ID)
            .post(Entity.entity(formDataMultiPart, MediaType.MULTIPART_FORM_DATA));
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void createModelFailTest() throws JsonProcessingException {
        when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(responseFormat);
        when(modelBusinessLogic.createModel(any(Model.class))).thenReturn(model);
        final FormDataMultiPart formDataMultiPart = buildCreateFormDataMultiPart(new byte[0], parseToJsonString(modelCreateRequest));
        final var response = target(rootPath.toString()).request(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, USER_ID)
            .post(Entity.entity(formDataMultiPart, MediaType.MULTIPART_FORM_DATA));
        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void createModelFailWithModelNameEmptyTest() throws JsonProcessingException {
        when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(responseFormat);
        modelCreateRequest.setName(StringUtils.EMPTY);
        final FormDataMultiPart formDataMultiPart = buildCreateFormDataMultiPart(new byte[0], parseToJsonString(modelCreateRequest));
        final var response = target(rootPath.toString()).request(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, USER_ID)
            .post(Entity.entity(formDataMultiPart, MediaType.MULTIPART_FORM_DATA));
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void createModelFailWithModelNameNullTest() throws JsonProcessingException {
        when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(responseFormat);
        modelCreateRequest.setName(null);
        final var modelFile = new byte[0];
        final FormDataMultiPart formDataMultiPart = buildCreateFormDataMultiPart(modelFile, parseToJsonString(modelCreateRequest));
        final var response = target(rootPath.toString()).request(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, USER_ID)
            .post(Entity.entity(formDataMultiPart, MediaType.MULTIPART_FORM_DATA));
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void createModelThrowsBusinessExceptionTest() throws JsonProcessingException {
        final var modelFile = new byte[0];
        final String modelCreateAsJson = parseToJsonString(modelCreateRequest);
        final FormDataMultiPart formDataMultiPart = buildCreateFormDataMultiPart(modelFile, modelCreateAsJson);
        when(modelBusinessLogic.createModel(model)).thenThrow(new BusinessException() {});

        final var response = target(rootPath.toString()).request(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, USER_ID)
            .post(Entity.entity(formDataMultiPart, MediaType.MULTIPART_FORM_DATA));
        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void updateModelImportsSuccessTest() {
        final FormDataMultiPart formDataMultiPart = buildUpdateFormDataMultiPart("model1", new byte[0]);

        final var response = target(importsPath.toString()).request(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, USER_ID)
            .put(Entity.entity(formDataMultiPart, MediaType.MULTIPART_FORM_DATA));
        assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    void updateModelImports_businessException() {
        final var modelId = "model1";
        final FormDataMultiPart formDataMultiPart = buildUpdateFormDataMultiPart(modelId, new byte[0]);
        final OperationException operationException = new OperationException(ActionStatus.INVALID_MODEL, modelId);
        doThrow(operationException).when(modelBusinessLogic).createModelImports(eq(modelId), any(InputStream.class));
        when(responseFormatManager.getResponseFormat(ActionStatus.INVALID_MODEL, modelId))
            .thenReturn(new ResponseFormat(Status.BAD_REQUEST.getStatusCode()));
        final var response = target(importsPath.toString()).request(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, USER_ID)
            .put(Entity.entity(formDataMultiPart, MediaType.MULTIPART_FORM_DATA));
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void updateModelImports_unknownException() {
        final var modelName = "model1";
        final FormDataMultiPart formDataMultiPart = buildUpdateFormDataMultiPart(modelName, new byte[0]);
        doThrow(new RuntimeException()).when(modelBusinessLogic).createModelImports(eq(modelName), any(InputStream.class));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(responseFormat);
        final var response = target(importsPath.toString()).request(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, USER_ID)
            .put(Entity.entity(formDataMultiPart, MediaType.MULTIPART_FORM_DATA));
        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void listModelSuccessTest() throws IOException {
        var model1 = new Model("model1");
        var model2 = new Model("model2");
        var model3 = new Model("model3");
        final List<Model> modelList = List.of(model1, model2, model3);
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
        when(modelBusinessLogic.listModels()).thenReturn(modelList);

        final var response = target(rootPath.toString()).request(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, USER_ID)
            .get();

        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaderString(HttpHeaders.CONTENT_TYPE));
        final String responseBody = response.readEntity(String.class);
        final String toRepresentation = (String) RepresentationUtils.toRepresentation(modelList);
        assertEquals(toRepresentation, responseBody);
    }

    @Test
    void listModelErrorTest() {
        when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(responseFormat);
        doThrow(new RuntimeException()).when(modelBusinessLogic).listModels();

        final var response = target(rootPath.toString()).request(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, USER_ID)
            .get();

        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    private FormDataMultiPart buildUpdateFormDataMultiPart(final String modelName, final byte[] importFilesZip) {
        return new FormDataMultiPart()
            .field("modelName", modelName)
            .field("modelImportsZip", importFilesZip, MediaType.MULTIPART_FORM_DATA_TYPE);
    }

    private FormDataMultiPart buildCreateFormDataMultiPart(final byte[] modelFile, final String modelCreateAsJson) {
        return new FormDataMultiPart()
            .field("model", modelCreateAsJson, MediaType.APPLICATION_JSON_TYPE)
            .field("modelImportsZip", modelFile, MediaType.MULTIPART_FORM_DATA_TYPE);
    }

    private String parseToJsonString(final Object object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }

}