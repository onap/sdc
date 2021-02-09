/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
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
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.common.api.Constants.USER_ID_HEADER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fj.data.Either;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentNodeFilterBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.exceptions.BusinessLogicException;
import org.openecomp.sdc.be.components.impl.utils.NodeFilterConstraintAction;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.SpringConfig;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.utils.ConstraintConvertor;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementNodeFilterCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementNodeFilterPropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeFilterConstraintType;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.ui.model.UIConstraint;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.WebApplicationContext;

public class ComponentNodeFilterServletTest extends JerseyTest {
    private static final String USER_ID = "jh0003";
    private static final String servicePropertyName = "resourceType";
    private static final String constraintOperator = "equal";
    private static final String sourceType = "static";
    private static final String sourceName = sourceType;
    private static final String propertyValue = "resourceTypeValue";
    private static final String componentId = "dac65869-dfb4-40d2-aa20-084324659ec1";
    private static final String componentInstance = "dac65869-dfb4-40d2-aa20-084324659ec1.resource0";
    private static final String componentType = "resources";
    private static final String capabilityName = "MyCapabilityName";

    private static HttpServletRequest request;
    private static HttpSession session;
    private static ServletContext servletContext;
    private static WebAppContextWrapper webAppContextWrapper;
    private static WebApplicationContext webApplicationContext;
    private static UserBusinessLogic userBusinessLogic;
    private static ComponentInstanceBusinessLogic componentInstanceBusinessLogic;
    private static ComponentsUtils componentsUtils;
    private static ServletUtils servletUtils;
    private static ResourceImportManager resourceImportManager;
    private static ComponentNodeFilterBusinessLogic componentNodeFilterBusinessLogic;
    private static ResponseFormat responseFormat;
    private static UserValidations userValidations;
    private static ConfigurationManager configurationManager;
    private CINodeFilterDataDefinition ciNodeFilterDataDefinition;
    private UIConstraint uiConstraint;
    private String constraint;
    private String inputJson;
    private User user;

    @BeforeClass
    public static void initClass() {
        createMocks();
        when(request.getSession()).thenReturn(session);
        when(session.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webApplicationContext);
        when(webApplicationContext.getBean(ComponentNodeFilterBusinessLogic.class)).thenReturn(componentNodeFilterBusinessLogic);
        when(request.getHeader("USER_ID")).thenReturn(USER_ID);
        when(webApplicationContext.getBean(ServletUtils.class)).thenReturn(servletUtils);
        when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);
        String appConfigDir = "src/test/resources/config/catalog-be";
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        configurationManager = new ConfigurationManager(configurationSource);
        org.openecomp.sdc.be.config.Configuration configuration = new org.openecomp.sdc.be.config.Configuration();
        configuration.setJanusGraphInMemoryGraph(true);
        configurationManager.setConfiguration(configuration);
        ExternalConfiguration.setAppName("catalog-be");
    }

    @Before
    public void resetMock() throws Exception {
        reset(componentNodeFilterBusinessLogic);
    }

    @After
    public void after() throws Exception {
        super.tearDown();
    }

    @Test
    public void addNodeFilterPropertiesSuccessTest() throws BusinessLogicException, JsonProcessingException {
        initComponentData();
        final String pathFormat = "/v1/catalog/%s/%s/componentInstance/%s/%s/nodeFilter";
        final String path = String.format(pathFormat, componentType, componentId, componentInstance,
            NodeFilterConstraintType.PROPERTIES_PARAM_NAME);

        when(userValidations.validateUserExists(user)).thenReturn(user);
        when(componentNodeFilterBusinessLogic.validateUser(USER_ID)).thenReturn(user);
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);

        assertNotNull(uiConstraint);
        assertThat(servicePropertyName).isEqualToIgnoringCase(uiConstraint.getServicePropertyName());
        assertThat(constraintOperator).isEqualToIgnoringCase(uiConstraint.getConstraintOperator());
        assertThat(sourceType).isEqualToIgnoringCase(uiConstraint.getSourceType());
        assertThat(sourceName).isEqualToIgnoringCase(uiConstraint.getSourceName());
        assertThat(propertyValue).isEqualToIgnoringCase(uiConstraint.getValue().toString());

        when(componentsUtils.parseToConstraint(anyString(), any(User.class), ArgumentMatchers.any(ComponentTypeEnum.class)))
            .thenReturn(Optional.of(uiConstraint));

        assertNotNull(constraint);
        assertNotNull(ciNodeFilterDataDefinition);
        assertThat(ciNodeFilterDataDefinition.getProperties().getListToscaDataDefinition()).hasSize(1);
        assertThat("resourceType: {equal: resourceTypeValue}\n").isEqualToIgnoringCase(constraint);
        when(componentNodeFilterBusinessLogic
            .addNodeFilter(componentId, componentInstance, NodeFilterConstraintAction.ADD,
                uiConstraint.getServicePropertyName(), constraint, true, ComponentTypeEnum.RESOURCE,
                NodeFilterConstraintType.PROPERTIES, ""))
            .thenReturn(Optional.of(ciNodeFilterDataDefinition));

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header(USER_ID_HEADER, USER_ID)
            .post(Entity.entity(inputJson, MediaType.APPLICATION_JSON));

        verify(componentNodeFilterBusinessLogic, times(1))
            .addNodeFilter(anyString(), anyString(), ArgumentMatchers.any(NodeFilterConstraintAction.class), anyString(),
                anyString(), anyBoolean(), ArgumentMatchers.any(ComponentTypeEnum.class),
                ArgumentMatchers.any(NodeFilterConstraintType.class), anyString());

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    public void addNodeFilterCapabilitiesSuccessTest() throws BusinessLogicException, JsonProcessingException {
        initComponentData();
        final String pathFormat = "/v1/catalog/%s/%s/componentInstance/%s/%s/nodeFilter";
        final String path = String.format(pathFormat, componentType, componentId, componentInstance,
            NodeFilterConstraintType.CAPABILITIES_PARAM_NAME);

        final UIConstraint uiConstraint1 = uiConstraint;
        when(userValidations.validateUserExists(user)).thenReturn(user);
        when(componentNodeFilterBusinessLogic.validateUser(USER_ID)).thenReturn(user);
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
        when(componentsUtils.parseToConstraint(anyString(), any(User.class),ArgumentMatchers.any(ComponentTypeEnum.class)))
            .thenReturn(Optional.of(uiConstraint));

        uiConstraint1.setCapabilityName(capabilityName);
        assertThat(ciNodeFilterDataDefinition.getProperties().getListToscaDataDefinition()).hasSize(1);
        when(componentNodeFilterBusinessLogic
            .addNodeFilter(componentId, componentInstance, NodeFilterConstraintAction.ADD,
                uiConstraint1.getServicePropertyName(), constraint, true, ComponentTypeEnum.RESOURCE,
                NodeFilterConstraintType.CAPABILITIES, capabilityName))
            .thenReturn(Optional.of(ciNodeFilterDataDefinition));

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header(USER_ID_HEADER, USER_ID)
            .post(Entity.entity(inputJson, MediaType.APPLICATION_JSON));

        verify(componentNodeFilterBusinessLogic, times(1))
            .addNodeFilter(anyString(), anyString(), ArgumentMatchers.any(NodeFilterConstraintAction.class), anyString(),
                anyString(), anyBoolean(), ArgumentMatchers.any(ComponentTypeEnum.class),
                ArgumentMatchers.any(NodeFilterConstraintType.class), anyString());

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    public void addNodeFilterFailTest() throws BusinessLogicException, JsonProcessingException {
        initComponentData();
        final String pathFormat = "/v1/catalog/%s/%s/componentInstance/%s/%s/nodeFilter";
        final String path = String.format(pathFormat, componentType, componentId, componentInstance,
            NodeFilterConstraintType.PROPERTIES_PARAM_NAME);

        when(userValidations.validateUserExists(user)).thenReturn(user);
        when(componentNodeFilterBusinessLogic.validateUser(USER_ID)).thenReturn(user);
        when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);

        when(componentsUtils.convertJsonToObjectUsingObjectMapper(anyString(), any(User.class),
            ArgumentMatchers.<Class<UIConstraint>>any(),
            nullable(AuditingActionEnum.class), nullable(ComponentTypeEnum.class))).thenReturn(Either.left(uiConstraint));

        when(componentNodeFilterBusinessLogic
            .addNodeFilter(componentId, componentInstance, NodeFilterConstraintAction.ADD,
                uiConstraint.getServicePropertyName(), constraint, true, ComponentTypeEnum.RESOURCE,
                NodeFilterConstraintType.PROPERTIES, ""))
            .thenReturn(Optional.empty());

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header(USER_ID_HEADER, USER_ID)
            .post(Entity.entity(inputJson, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    public void addNodeFilterFailConstraintParseTest() throws JsonProcessingException {
        initComponentData();
        final String pathFormat = "/v1/catalog/%s/%s/componentInstance/%s/%s/nodeFilter";
        final String path = String.format(pathFormat, componentType, componentId, componentInstance, NodeFilterConstraintType.PROPERTIES_PARAM_NAME);

        when(userValidations.validateUserExists(user)).thenReturn(user);
        when(componentNodeFilterBusinessLogic.validateUser(USER_ID)).thenReturn(user);

        when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);

        when(componentsUtils.convertJsonToObjectUsingObjectMapper(anyString(), any(User.class),
            ArgumentMatchers.<Class<UIConstraint>>any(),
            nullable(AuditingActionEnum.class), nullable(ComponentTypeEnum.class)))
            .thenReturn(Either.right(new ResponseFormat(HttpStatus.INTERNAL_SERVER_ERROR_500)));

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header(USER_ID_HEADER, USER_ID)
            .post(Entity.entity(inputJson, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    public void addNodeFilterFailConvertTest() throws JsonProcessingException {
        initComponentData();
        final String pathFormat = "/v1/catalog/%s/%s/componentInstance/%s/%s/nodeFilter";
        final String path = String.format(pathFormat, componentType, componentId, componentInstance,
            NodeFilterConstraintType.PROPERTIES.getType());

        when(userValidations.validateUserExists(user)).thenReturn(user);
        when(componentNodeFilterBusinessLogic.validateUser(USER_ID)).thenReturn(user);

        when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);

        when(componentsUtils.convertJsonToObjectUsingObjectMapper(anyString(), any(User.class),
            ArgumentMatchers.<Class<UIConstraint>>any(),
            nullable(AuditingActionEnum.class), nullable(ComponentTypeEnum.class)))
            .thenReturn(Either.left(null));

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header(USER_ID_HEADER, USER_ID)
            .post(Entity.entity(inputJson, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    public void updateNodeFilterPropertiesSuccessTest() throws BusinessLogicException, JsonProcessingException {
        initComponentData();
        final String pathFormat = "/v1/catalog/%s/%s/componentInstance/%s/%s/%s/nodeFilter";
        final String path = String.format(pathFormat, componentType, componentId, componentInstance,
            NodeFilterConstraintType.PROPERTIES_PARAM_NAME, 0);

        when(userValidations.validateUserExists(user)).thenReturn(user);
        when(componentNodeFilterBusinessLogic.validateUser(USER_ID)).thenReturn(user);

        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);

        when(componentsUtils.convertJsonToObjectUsingObjectMapper(anyString(), any(User.class),
            ArgumentMatchers.<Class<List>>any(),
            nullable(AuditingActionEnum.class), nullable(ComponentTypeEnum.class)))
            .thenReturn(Either.left(Arrays.asList(new ObjectMapper().convertValue(uiConstraint, Map.class))));

        when(componentNodeFilterBusinessLogic
            .updateNodeFilter(componentId, componentInstance, uiConstraint, ComponentTypeEnum.RESOURCE,
                NodeFilterConstraintType.PROPERTIES, 0)).thenReturn(Optional.of(ciNodeFilterDataDefinition));
        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header(USER_ID_HEADER, USER_ID)
            .put(Entity.entity(inputJson, MediaType.APPLICATION_JSON));

        verify(componentNodeFilterBusinessLogic, times(1))
            .updateNodeFilter(anyString(), anyString(), ArgumentMatchers.any(UIConstraint.class),
                ArgumentMatchers.any(ComponentTypeEnum.class), ArgumentMatchers.any(NodeFilterConstraintType.class),
                anyInt());

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    public void updateNodeFilterCapabilitiesSuccessTest() throws BusinessLogicException, JsonProcessingException {
        initComponentData();
        final String pathFormat = "/v1/catalog/%s/%s/componentInstance/%s/%s/%s/nodeFilter";
        final String path = String.format(pathFormat, componentType, componentId, componentInstance,
            NodeFilterConstraintType.CAPABILITIES_PARAM_NAME, 0);

        when(userValidations.validateUserExists(user)).thenReturn(user);
        when(componentNodeFilterBusinessLogic.validateUser(USER_ID)).thenReturn(user);

        when(componentsUtils.parseToConstraint(anyString(), any(User.class), ArgumentMatchers.any(ComponentTypeEnum.class)))
            .thenReturn(Optional.of(uiConstraint));

        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);

        when(componentsUtils.validateAndParseConstraint(ArgumentMatchers.any(ComponentTypeEnum.class), anyString(), any(User.class)))
            .thenReturn(Collections.singletonList(uiConstraint));

        when(componentsUtils.convertJsonToObjectUsingObjectMapper(anyString(), any(User.class),
            ArgumentMatchers.<Class<List>>any(),
            nullable(AuditingActionEnum.class), nullable(ComponentTypeEnum.class)))
            .thenReturn(Either.left(Arrays.asList(new ObjectMapper().convertValue(uiConstraint, Map.class))));

        when(componentNodeFilterBusinessLogic.deleteNodeFilter(componentId, componentInstance,
            NodeFilterConstraintAction.DELETE, null, 0, true, ComponentTypeEnum.RESOURCE,
            NodeFilterConstraintType.PROPERTIES))
            .thenReturn(Optional.of(ciNodeFilterDataDefinition));

        when(componentNodeFilterBusinessLogic
            .addNodeFilter(componentId, componentInstance, NodeFilterConstraintAction.ADD,
                uiConstraint.getServicePropertyName(), constraint, true, ComponentTypeEnum.RESOURCE,
                NodeFilterConstraintType.PROPERTIES, ""))
            .thenReturn(Optional.of(ciNodeFilterDataDefinition));

        when(componentNodeFilterBusinessLogic
            .updateNodeFilter(componentId, componentInstance, uiConstraint,
                ComponentTypeEnum.RESOURCE, NodeFilterConstraintType.CAPABILITIES, 0))
            .thenReturn(Optional.of(ciNodeFilterDataDefinition));

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header(USER_ID_HEADER, USER_ID)
            .put(Entity.entity(inputJson, MediaType.APPLICATION_JSON));

        verify(componentNodeFilterBusinessLogic, times(1))
            .updateNodeFilter(anyString(), anyString(), ArgumentMatchers.any(UIConstraint.class),
                ArgumentMatchers.any(ComponentTypeEnum.class), ArgumentMatchers.any(NodeFilterConstraintType.class),
                anyInt());

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    public void updateNodeFilterFailTest() throws BusinessLogicException, JsonProcessingException {
        initComponentData();
        final String pathFormat = "/v1/catalog/%s/%s/componentInstance/%s/%s/%s/nodeFilter";
        final String path = String.format(pathFormat, componentType, componentId, componentInstance,
            NodeFilterConstraintType.PROPERTIES_PARAM_NAME, 0);

        when(userValidations.validateUserExists(user)).thenReturn(user);
        when(componentNodeFilterBusinessLogic.validateUser(USER_ID)).thenReturn(user);

        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);

        when(componentsUtils.validateAndParseConstraint(ArgumentMatchers.any(ComponentTypeEnum.class), anyString(), any(User.class)))
            .thenReturn(Collections.singletonList(uiConstraint));

        when(componentNodeFilterBusinessLogic
            .updateNodeFilter(componentId, componentInstance, uiConstraint,
                ComponentTypeEnum.RESOURCE, NodeFilterConstraintType.PROPERTIES, 0))
            .thenReturn(Optional.empty());
        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header(USER_ID_HEADER, USER_ID)
            .put(Entity.entity(inputJson, MediaType.APPLICATION_JSON));

        verify(componentNodeFilterBusinessLogic, times(1))
            .updateNodeFilter(anyString(), anyString(), ArgumentMatchers.any(UIConstraint.class),
                ArgumentMatchers.any(ComponentTypeEnum.class), ArgumentMatchers.any(NodeFilterConstraintType.class),
                anyInt());

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    public void updateNodeFilterFailConstraintParseTest() throws JsonProcessingException {
        initComponentData();
        final String pathFormat = "/v1/catalog/%s/%s/componentInstance/%s/%s/%s/nodeFilter";
        final String path = String.format(pathFormat, componentType, componentId, componentInstance,
            NodeFilterConstraintType.PROPERTIES_PARAM_NAME, 0);

        when(userValidations.validateUserExists(user)).thenReturn(user);
        when(componentNodeFilterBusinessLogic.validateUser(USER_ID)).thenReturn(user);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);

        when(componentsUtils.validateAndParseConstraint(ArgumentMatchers.any(ComponentTypeEnum.class), anyString(), any(User.class)))
            .thenReturn(Collections.emptyList());

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header(USER_ID_HEADER, USER_ID)
            .put(Entity.entity(inputJson, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    public void updateNodeFilterFailConvertTest() throws JsonProcessingException {
        initComponentData();
        final String pathFormat = "/v1/catalog/%s/%s/componentInstance/%s/%s/%s/nodeFilter";
        final String path = String.format(pathFormat, componentType, componentId, componentInstance,
            NodeFilterConstraintType.PROPERTIES_PARAM_NAME, 0);

        when(userValidations.validateUserExists(user)).thenReturn(user);
        when(componentNodeFilterBusinessLogic.validateUser(USER_ID)).thenReturn(user);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);

        when(componentsUtils.convertJsonToObjectUsingObjectMapper(anyString(), any(User.class),
            ArgumentMatchers.<Class<List>>any(),
            nullable(AuditingActionEnum.class), nullable(ComponentTypeEnum.class)))
            .thenReturn(Either.left(null));

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header(USER_ID_HEADER, USER_ID)
            .put(Entity.entity(inputJson, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    public void deleteNodeFilterSuccessTest() throws BusinessLogicException, JsonProcessingException {
        initComponentData();
        final String pathFormat = "/v1/catalog/%s/%s/componentInstance/%s/%s/%s/nodeFilter";
        final String path = String.format(pathFormat, componentType, componentId, componentInstance,
            NodeFilterConstraintType.PROPERTIES_PARAM_NAME, 0);

        when(userValidations.validateUserExists(user)).thenReturn(user);
        when(componentNodeFilterBusinessLogic.validateUser(USER_ID)).thenReturn(user);

        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);

        when(componentNodeFilterBusinessLogic.deleteNodeFilter(componentId, componentInstance,
            NodeFilterConstraintAction.DELETE, null, 0, true, ComponentTypeEnum.RESOURCE,
            NodeFilterConstraintType.PROPERTIES))
            .thenReturn(Optional.of(ciNodeFilterDataDefinition));

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(USER_ID_HEADER, USER_ID)
            .delete(Response.class);

        verify(componentNodeFilterBusinessLogic, times(1))
            .deleteNodeFilter(anyString(), anyString(), ArgumentMatchers.any(NodeFilterConstraintAction.class),
                nullable(String.class), anyInt(), anyBoolean(), ArgumentMatchers.any(ComponentTypeEnum.class),
                ArgumentMatchers.any(NodeFilterConstraintType.class));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    public void deleteNodeFilterFailTest() {
        final String pathFormat = "/v1/catalog/%s/%s/componentInstance/%s/%s/%s/nodeFilter";
        final String path = String.format(pathFormat, componentType, componentId, componentInstance,
            NodeFilterConstraintType.PROPERTIES_PARAM_NAME, 0);
        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(USER_ID_HEADER, USER_ID)
            .delete(Response.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    private static void createMocks() {
        request = mock(HttpServletRequest.class);
        userBusinessLogic = mock(UserBusinessLogic.class);
        componentInstanceBusinessLogic = mock(ComponentInstanceBusinessLogic.class);
        componentsUtils = mock(ComponentsUtils.class);
        servletUtils = mock(ServletUtils.class);
        resourceImportManager = mock(ResourceImportManager.class);
        componentNodeFilterBusinessLogic = mock(ComponentNodeFilterBusinessLogic.class);

        session = mock(HttpSession.class);
        servletContext = mock(ServletContext.class);
        webAppContextWrapper = mock(WebAppContextWrapper.class);
        webApplicationContext = mock(WebApplicationContext.class);
        responseFormat = mock(ResponseFormat.class);
        userValidations = mock(UserValidations.class);
    }

    @Override
    protected ResourceConfig configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        final ApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class);
        return new ResourceConfig(ComponentNodeFilterServlet.class)
            .register(new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(request).to(HttpServletRequest.class);
                    bind(userBusinessLogic).to(UserBusinessLogic.class);
                    bind(componentInstanceBusinessLogic).to(ComponentInstanceBusinessLogic.class);
                    bind(componentsUtils).to(ComponentsUtils.class);
                    bind(servletUtils).to(ServletUtils.class);
                    bind(resourceImportManager).to(ResourceImportManager.class);
                    bind(componentNodeFilterBusinessLogic).to(ComponentNodeFilterBusinessLogic.class);
                }
            })
            .property("contextConfig", context);
    }

    private void initComponentData() throws JsonProcessingException {
        uiConstraint = new UIConstraint("resourceType", "equal", "static", "static", "resourceTypeValue");
        constraint = new ConstraintConvertor().convert(uiConstraint);
        inputJson = buildConstraintDataJson(uiConstraint);

        final RequirementNodeFilterPropertyDataDefinition requirementNodeFilterPropertyDataDefinition =
            new RequirementNodeFilterPropertyDataDefinition();
        requirementNodeFilterPropertyDataDefinition.setName(uiConstraint.getServicePropertyName());
        requirementNodeFilterPropertyDataDefinition.setConstraints(new LinkedList<>(Arrays.asList(constraint)));

        final ListDataDefinition<RequirementNodeFilterPropertyDataDefinition> propertyDataDefinitionList =
            new ListDataDefinition<>(new LinkedList<>(Arrays.asList(requirementNodeFilterPropertyDataDefinition)));

        final RequirementNodeFilterCapabilityDataDefinition requirementNodeFilterCapabilityDataDefinition =
            new RequirementNodeFilterCapabilityDataDefinition();
        requirementNodeFilterCapabilityDataDefinition.setName(uiConstraint.getServicePropertyName());
        requirementNodeFilterCapabilityDataDefinition.setProperties(propertyDataDefinitionList);

        final ListDataDefinition<RequirementNodeFilterCapabilityDataDefinition> capabilityDataDefinitionList =
            new ListDataDefinition<>(new LinkedList<>(Arrays.asList(requirementNodeFilterCapabilityDataDefinition)));

        ciNodeFilterDataDefinition = new CINodeFilterDataDefinition();
        ciNodeFilterDataDefinition.setProperties(propertyDataDefinitionList);
        ciNodeFilterDataDefinition.setCapabilities(capabilityDataDefinitionList);
        ciNodeFilterDataDefinition.setID("NODE_FILTER_UID");

        user = new User();
        user.setUserId(USER_ID);
        user.setRole(Role.ADMIN.name());
    }

    private String buildConstraintDataJson(final UIConstraint uiConstraint) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(uiConstraint);
    }

}
