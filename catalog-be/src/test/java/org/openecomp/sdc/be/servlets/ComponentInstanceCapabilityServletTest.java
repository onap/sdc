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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fj.data.Either;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.ErrorInfo;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception.OperationException;
import org.openecomp.sdc.be.servlets.builder.ServletResponseBuilder;
import org.openecomp.sdc.be.servlets.exception.OperationExceptionMapper;
import org.openecomp.sdc.be.ui.mapper.CapabilityMapper;
import org.openecomp.sdc.be.ui.model.ComponentInstanceCapabilityUpdateModel;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;
import org.openecomp.sdc.exception.ServiceException;

class ComponentInstanceCapabilityServletTest extends JerseySpringBaseTest {

    private static final String UPDATE_INSTANCE_REQUIREMENT_PATH_FORMAT = "/v1/catalog/%s/%s/componentInstances/%s/capability";
    private static ConfigurationManager configurationManager;

    private final String componentId = "componentId";
    private final String componentInstanceId = "componentInstanceId";
    private final String userId = "userId";

    private CapabilityMapper capabilityMapper;
    private ComponentInstanceBusinessLogic componentInstanceBusinessLogicMock;

    @Override
    protected ResourceConfig configure() {
        componentInstanceBusinessLogicMock = mock(ComponentInstanceBusinessLogic.class);
        capabilityMapper = new CapabilityMapper();
        var servletResponseBuilder = new ServletResponseBuilder();
        var componentInstanceCapabilityServlet =
            new ComponentInstanceCapabilityServlet(componentInstanceBusinessLogicMock, capabilityMapper, servletResponseBuilder);
        return super.configure().register(componentInstanceCapabilityServlet)
            .register(new OperationExceptionMapper(servletResponseBuilder));
    }

    @BeforeAll
    static void beforeAll() {
        setupConfiguration();
    }

    private static void setupConfiguration() {
        final ConfigurationSource configurationSource =
            new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be");
        configurationManager = new ConfigurationManager(configurationSource);
    }

    //workaround for JerseyTest + Junit5
    @BeforeEach
    void beforeEach() throws Exception {
        super.setUp();
    }

    //workaround for JerseyTest + Junit5
    @AfterEach
    void afterEach() throws Exception {
        super.tearDown();
    }

    @Test
    void updateInstanceRequirementSuccessTest() throws JsonProcessingException {
        final var updateModel = createDefaultUpdateMode();
        var expectedCapabilityDefinition = capabilityMapper.mapToCapabilityDefinition(updateModel);

        when(componentInstanceBusinessLogicMock
            .updateInstanceCapability(eq(ComponentTypeEnum.SERVICE), eq(componentId), eq(componentInstanceId), any(CapabilityDefinition.class), eq(userId)))
            .thenReturn(Either.left(expectedCapabilityDefinition));
        final var url =
            String.format(UPDATE_INSTANCE_REQUIREMENT_PATH_FORMAT, ComponentTypeEnum.SERVICE_PARAM_NAME, componentId, componentInstanceId);

        final Response response = target()
            .path(url)
            .request(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, userId)
            .put(Entity.entity(parseToJson(updateModel), MediaType.APPLICATION_JSON));
        var actualCapabilityDefinition = response.readEntity(CapabilityDefinition.class);
        assertEquals(200, response.getStatus(), "The update status should be as expected");
        assertEquals(MediaType.valueOf(MediaType.APPLICATION_JSON), response.getMediaType(), "The content type should be application/json");
        assertCapabilityDefinition(actualCapabilityDefinition, expectedCapabilityDefinition);
    }

    @Test
    void updateInstanceRequirementFailTest() throws JsonProcessingException {
        final var expectedResponseFormat = new ResponseFormat(404);
        final var requestErrorWrapper = expectedResponseFormat.new RequestErrorWrapper();
        final var serviceException = new ServiceException("anErrorCode", "anErrorText", new String[2]);
        requestErrorWrapper.setServiceException(serviceException);
        expectedResponseFormat.setRequestError(requestErrorWrapper);

        when(componentInstanceBusinessLogicMock
            .updateInstanceCapability(eq(ComponentTypeEnum.SERVICE), eq(componentId), eq(componentInstanceId), any(CapabilityDefinition.class), eq(userId)))
            .thenReturn(Either.right(expectedResponseFormat));

        final var url =
            String.format(UPDATE_INSTANCE_REQUIREMENT_PATH_FORMAT, ComponentTypeEnum.SERVICE_PARAM_NAME, componentId, componentInstanceId);
        final Response response = target()
            .path(url)
            .request(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, userId)
            .put(Entity.entity(parseToJson(createDefaultUpdateMode()), MediaType.APPLICATION_JSON));
        var actualResponseFormat = response.readEntity(ResponseFormat.class);

        assertEquals(expectedResponseFormat.getStatus(), response.getStatus(), "The update status should be as expected");
        assertNotNull(actualResponseFormat.getRequestError());
        assertNotNull(actualResponseFormat.getRequestError().getRequestError());
        assertEquals(expectedResponseFormat.getMessageId(), actualResponseFormat.getMessageId());
        assertEquals(expectedResponseFormat.getVariables().length, actualResponseFormat.getVariables().length);
    }

    @Test
    void updateInstanceRequirementKnownErrorTest() throws JsonProcessingException {
        when(componentInstanceBusinessLogicMock
            .updateInstanceCapability(eq(ComponentTypeEnum.SERVICE), eq(componentId), eq(componentInstanceId), any(CapabilityDefinition.class), eq(userId)))
            .thenThrow(new OperationException(ActionStatus.COMPONENT_NOT_FOUND, componentId));
        final var url =
            String.format(UPDATE_INSTANCE_REQUIREMENT_PATH_FORMAT, ComponentTypeEnum.SERVICE_PARAM_NAME, componentId, componentInstanceId);
        final Response response = target()
            .path(url)
            .request(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, userId)
            .put(Entity.entity(parseToJson(createDefaultUpdateMode()), MediaType.APPLICATION_JSON));
        var responseFormat = response.readEntity(ResponseFormat.class);

        final ErrorInfo errorInfo = configurationManager.getErrorConfiguration().getErrorInfo(ActionStatus.COMPONENT_NOT_FOUND.name());
        assertNotNull(errorInfo);
        assertEquals(errorInfo.getCode(), response.getStatus(), "The update status should be as expected");
        assertEquals(errorInfo.getMessageId(), responseFormat.getMessageId());
        assertEquals(errorInfo.getMessage(), responseFormat.getText());
    }

    @Test
    void updateInstanceRequirementUnknownErrorTest() throws JsonProcessingException {
        when(componentInstanceBusinessLogicMock
            .updateInstanceCapability(eq(ComponentTypeEnum.SERVICE), eq(componentId), eq(componentInstanceId), any(CapabilityDefinition.class), eq(userId)))
            .thenThrow(new RuntimeException());
        final var url =
            String.format(UPDATE_INSTANCE_REQUIREMENT_PATH_FORMAT, ComponentTypeEnum.SERVICE_PARAM_NAME, componentId, componentInstanceId);
        final Response response = target()
            .path(url)
            .request(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, userId)
            .put(Entity.entity(parseToJson(createDefaultUpdateMode()), MediaType.APPLICATION_JSON));
        var responseFormat = response.readEntity(ResponseFormat.class);

        final ErrorInfo errorInfo = configurationManager.getErrorConfiguration().getErrorInfo(ActionStatus.GENERAL_ERROR.name());
        assertNotNull(errorInfo);
        assertEquals(errorInfo.getCode(), response.getStatus(), "The update status should be as expected");
        assertEquals(errorInfo.getMessageId(), responseFormat.getMessageId());
        assertEquals(errorInfo.getMessage(), responseFormat.getText());
    }

    @Test
    void updateInstanceRequirementIncorrectComponentTypeTest() throws JsonProcessingException {
        final var url =
            String.format(UPDATE_INSTANCE_REQUIREMENT_PATH_FORMAT, "wrongType", componentId, componentInstanceId);
        final Response response = target()
            .path(url)
            .request(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, userId)
            .put(Entity.entity(parseToJson(createDefaultUpdateMode()), MediaType.APPLICATION_JSON));
        var responseFormat = response.readEntity(ResponseFormat.class);

        final ErrorInfo errorInfo = configurationManager.getErrorConfiguration().getErrorInfo(ActionStatus.UNSUPPORTED_ERROR.name());
        assertNotNull(errorInfo);
        assertEquals(errorInfo.getCode(), response.getStatus(), "The update status should be as expected");
        assertEquals(errorInfo.getMessageId(), responseFormat.getMessageId());
        assertEquals(errorInfo.getMessage(), responseFormat.getText());
    }

    private ComponentInstanceCapabilityUpdateModel createDefaultUpdateMode() {
        var capabilityUpdateModel = new ComponentInstanceCapabilityUpdateModel();
        capabilityUpdateModel.setName("name");
        capabilityUpdateModel.setExternal(true);
        capabilityUpdateModel.setOwnerId("ownerId");
        capabilityUpdateModel.setType("type");
        capabilityUpdateModel.setOwnerName("ownerName");
        capabilityUpdateModel.setUniqueId("uniqueId");
        return capabilityUpdateModel;
    }

    private void assertCapabilityDefinition(final CapabilityDefinition actualCapabilityDefinition,
                                            final CapabilityDefinition expectedCapabilityDefinition) {
        assertEquals(expectedCapabilityDefinition.getUniqueId(), actualCapabilityDefinition.getUniqueId());
        assertEquals(expectedCapabilityDefinition.getName(), actualCapabilityDefinition.getName());
        assertEquals(expectedCapabilityDefinition.getOwnerId(), actualCapabilityDefinition.getOwnerId());
        assertEquals(expectedCapabilityDefinition.getOwnerName(), actualCapabilityDefinition.getOwnerName());
        assertEquals(expectedCapabilityDefinition.getType(), actualCapabilityDefinition.getType());
        assertEquals(expectedCapabilityDefinition.isExternal(), actualCapabilityDefinition.isExternal());
    }

    private <T> String parseToJson(final T object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }

}

