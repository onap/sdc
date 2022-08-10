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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.common.api.Constants.USER_ID_HEADER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedList;
import java.util.List;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentNodeFilterBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.exceptions.BusinessLogicException;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.SpringConfig;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.utils.ConstraintConvertor;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementNodeFilterCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeFilterConstraintType;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.dto.FilterConstraintDto;
import org.openecomp.sdc.be.ui.mapper.FilterConstraintMapper;
import org.openecomp.sdc.be.ui.model.UIConstraint;
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

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
class ComponentNodeFilterServletTest extends JerseyTest {

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
    private static final String V_1_CATALOG_S_S_COMPONENT_INSTANCE_S_S_S_NODE_FILTER = "/v1/catalog/%s/%s/componentInstance/%s/%s/%s/nodeFilter";
    private static final String V_1_CATALOG_S_S_COMPONENT_INSTANCE_S_S_NODE_FILTER = "/v1/catalog/%s/%s/componentInstance/%s/%s/nodeFilter";
    private final UIConstraint uiConstraint = new UIConstraint("resourceType", "equal", "static", "static", "resourceTypeValue");
    private final String constraint = new ConstraintConvertor().convert(uiConstraint);
    private final FilterConstraintDto filterConstraintDto = new FilterConstraintMapper().mapFrom(uiConstraint);
    private final String inputJson = buildConstraintDataJson(uiConstraint);
    private final User user = new User("", "", USER_ID, "", Role.ADMIN.name(), null);
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
    private ComponentNodeFilterBusinessLogic componentNodeFilterBusinessLogic;
    @Mock
    private ResponseFormat responseFormat;
    @Mock
    private UserValidations userValidations;

    private CINodeFilterDataDefinition ciNodeFilterDataDefinition;

    public ComponentNodeFilterServletTest() throws JsonProcessingException {
    }

    @BeforeAll
    public void initClass() {
        MockitoAnnotations.openMocks(this);
        when(request.getSession()).thenReturn(session);
        when(session.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webApplicationContext);
        when(webApplicationContext.getBean(ComponentNodeFilterBusinessLogic.class)).thenReturn(componentNodeFilterBusinessLogic);
        when(request.getHeader("USER_ID")).thenReturn(USER_ID);
        when(webApplicationContext.getBean(ServletUtils.class)).thenReturn(servletUtils);
        when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);
        final String appConfigDir = "src/test/resources/config/catalog-be";
        final ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        final ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
        final org.openecomp.sdc.be.config.Configuration configuration = new org.openecomp.sdc.be.config.Configuration();
        configuration.setJanusGraphInMemoryGraph(true);
        configurationManager.setConfiguration(configuration);
        ExternalConfiguration.setAppName("catalog-be");
    }

    @BeforeEach
    public void resetMock() throws Exception {
        super.setUp();
    }

    @AfterEach
    void after() throws Exception {
        super.tearDown();
    }

    @Test
    void addNodeFilterPropertiesSuccessTest() throws BusinessLogicException {
        initComponentData();
        final String pathFormat = V_1_CATALOG_S_S_COMPONENT_INSTANCE_S_S_NODE_FILTER;
        final String path = String.format(pathFormat, componentType, componentId, componentInstance,
            NodeFilterConstraintType.PROPERTIES_PARAM_NAME);

        doReturn(user).when(componentNodeFilterBusinessLogic).validateUser(USER_ID);
        doReturn(HttpStatus.OK_200).when(responseFormat).getStatus();
        doReturn(responseFormat).when(componentsUtils).getResponseFormat(ActionStatus.OK);
        doReturn(componentsUtils).when(servletUtils).getComponentsUtils();
        assertNotNull(uiConstraint);
        assertThat(servicePropertyName).isEqualToIgnoringCase(uiConstraint.getServicePropertyName());
        assertThat(constraintOperator).isEqualToIgnoringCase(uiConstraint.getConstraintOperator());
        assertThat(sourceType).isEqualToIgnoringCase(uiConstraint.getSourceType());
        assertThat(sourceName).isEqualToIgnoringCase(uiConstraint.getSourceName());
        assertThat(propertyValue).isEqualToIgnoringCase(uiConstraint.getValue().toString());

        doReturn(Optional.of(uiConstraint)).when(componentsUtils)
            .parseToConstraint(anyString(), any(User.class), any(ComponentTypeEnum.class));

        assertNotNull(constraint);
        assertNotNull(ciNodeFilterDataDefinition);
        assertThat(ciNodeFilterDataDefinition.getProperties().getListToscaDataDefinition()).hasSize(1);
        assertThat("resourceType: {equal: resourceTypeValue}\n").isEqualToIgnoringCase(constraint);

        doReturn(Optional.of(ciNodeFilterDataDefinition)).when(componentNodeFilterBusinessLogic)
            .addNodeFilter(componentId, componentInstance, filterConstraintDto, true, ComponentTypeEnum.RESOURCE,
                NodeFilterConstraintType.PROPERTIES, "");

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header(USER_ID_HEADER, USER_ID)
            .post(Entity.entity(inputJson, MediaType.APPLICATION_JSON));

        verify(componentNodeFilterBusinessLogic, times(1))
            .addNodeFilter(anyString(), anyString(), any(FilterConstraintDto.class), anyBoolean(), any(ComponentTypeEnum.class),
                any(NodeFilterConstraintType.class), anyString()
            );

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
        verify(componentNodeFilterBusinessLogic,times(1)).validateUser(USER_ID);
    }

    @Test
    void addNodeFilterCapabilitiesSuccessTest() throws BusinessLogicException, JsonProcessingException {
        initComponentData();
        final String path = String.format(V_1_CATALOG_S_S_COMPONENT_INSTANCE_S_S_NODE_FILTER, componentType, componentId, componentInstance,
            NodeFilterConstraintType.CAPABILITIES_PARAM_NAME);
        final UIConstraint uiConstraint1 = new UIConstraint(uiConstraint.getServicePropertyName(), uiConstraint.getConstraintOperator(),
            uiConstraint.getSourceType(), uiConstraint.getSourceName(), uiConstraint.getValue());
        when(componentNodeFilterBusinessLogic.validateUser(USER_ID)).thenReturn(user);
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
        doReturn(componentsUtils).when(servletUtils).getComponentsUtils();
        uiConstraint1.setCapabilityName(capabilityName);
        final String requestPayload = buildConstraintDataJson(uiConstraint1);
        when(componentsUtils.parseToConstraint(requestPayload, user, ComponentTypeEnum.RESOURCE)).thenReturn(Optional.of(uiConstraint1));

        assertThat(ciNodeFilterDataDefinition.getProperties().getListToscaDataDefinition()).hasSize(1);
        final FilterConstraintDto filterConstraintDto1 = new FilterConstraintMapper().mapFrom(uiConstraint1);
        when(componentNodeFilterBusinessLogic
            .addNodeFilter(componentId, componentInstance, filterConstraintDto1, true, ComponentTypeEnum.RESOURCE,
                NodeFilterConstraintType.CAPABILITIES, capabilityName)
        ).thenReturn(Optional.of(ciNodeFilterDataDefinition));
        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header(USER_ID_HEADER, USER_ID)
            .post(Entity.entity(requestPayload, MediaType.APPLICATION_JSON));

        verify(componentNodeFilterBusinessLogic, times(1))
            .addNodeFilter(componentId, componentInstance, filterConstraintDto1, true, ComponentTypeEnum.RESOURCE,
                NodeFilterConstraintType.CAPABILITIES, capabilityName);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
        verify(componentNodeFilterBusinessLogic,times(1)).validateUser(USER_ID);
    }

    @Test
    void addNodeFilterFailTest() throws BusinessLogicException, JsonProcessingException {
        initComponentData();
        final String pathFormat = V_1_CATALOG_S_S_COMPONENT_INSTANCE_S_S_NODE_FILTER;
        final String path = String.format(pathFormat, componentType, componentId, componentInstance,
            NodeFilterConstraintType.PROPERTIES_PARAM_NAME);

        when(componentNodeFilterBusinessLogic.validateUser(USER_ID)).thenReturn(user);

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header(USER_ID_HEADER, USER_ID)
            .post(Entity.entity(inputJson, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
        verify(componentNodeFilterBusinessLogic,times(1)).validateUser(USER_ID);
    }

    @Test
    void addNodeFilterFailConstraintParseTest() throws JsonProcessingException {
        initComponentData();
        final String pathFormat = V_1_CATALOG_S_S_COMPONENT_INSTANCE_S_S_NODE_FILTER;
        final String path = String.format(pathFormat, componentType, componentId, componentInstance, NodeFilterConstraintType.PROPERTIES_PARAM_NAME);

        when(componentNodeFilterBusinessLogic.validateUser(USER_ID)).thenReturn(user);

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header(USER_ID_HEADER, USER_ID)
            .post(Entity.entity(inputJson, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
        verify(componentNodeFilterBusinessLogic,times(1)).validateUser(USER_ID);
    }

    @Test
    void addNodeFilterFailConvertTest() throws JsonProcessingException {
        initComponentData();
        final String pathFormat = V_1_CATALOG_S_S_COMPONENT_INSTANCE_S_S_NODE_FILTER;
        final String path = String.format(pathFormat, componentType, componentId, componentInstance,
            NodeFilterConstraintType.PROPERTIES.getType());

        when(componentNodeFilterBusinessLogic.validateUser(USER_ID)).thenReturn(user);

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header(USER_ID_HEADER, USER_ID)
            .post(Entity.entity(inputJson, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
        verify(componentNodeFilterBusinessLogic,times(1)).validateUser(USER_ID);
    }

    @Test
    void updateNodeFilterPropertiesSuccessTest() throws BusinessLogicException, JsonProcessingException {
        initComponentData();
        final String pathFormat = V_1_CATALOG_S_S_COMPONENT_INSTANCE_S_S_S_NODE_FILTER;
        final String path = String.format(pathFormat, componentType, componentId, componentInstance,
            NodeFilterConstraintType.PROPERTIES_PARAM_NAME, 0);

        when(componentNodeFilterBusinessLogic.validateUser(USER_ID)).thenReturn(user);

        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
        doReturn(componentsUtils).when(servletUtils).getComponentsUtils();
        doReturn(Optional.of(uiConstraint)).when(componentsUtils)
            .parseToConstraint(anyString(), any(User.class), eq(ComponentTypeEnum.RESOURCE));
        when(componentNodeFilterBusinessLogic
            .updateNodeFilter(componentId, componentInstance, uiConstraint, ComponentTypeEnum.RESOURCE,
                NodeFilterConstraintType.PROPERTIES, 0)).thenReturn(Optional.of(ciNodeFilterDataDefinition));
        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header(USER_ID_HEADER, USER_ID)
            .put(Entity.entity(inputJson, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
        verify(componentNodeFilterBusinessLogic,times(1)).validateUser(USER_ID);
    }

    @Test
    void updateNodeFilterCapabilitiesSuccessTest() throws BusinessLogicException, JsonProcessingException {
        initComponentData();
        final String pathFormat = V_1_CATALOG_S_S_COMPONENT_INSTANCE_S_S_S_NODE_FILTER;
        final String path = String.format(pathFormat, componentType, componentId, componentInstance,
            NodeFilterConstraintType.CAPABILITIES_PARAM_NAME, 0);

        when(componentNodeFilterBusinessLogic.validateUser(USER_ID)).thenReturn(user);

        when(componentsUtils.parseToConstraint(anyString(), any(User.class), any(ComponentTypeEnum.class)))
            .thenReturn(Optional.of(uiConstraint));
        doReturn(componentsUtils).when(servletUtils).getComponentsUtils();
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);

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
            .updateNodeFilter(anyString(), anyString(), any(UIConstraint.class),
                any(ComponentTypeEnum.class), any(NodeFilterConstraintType.class),
                anyInt());

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
        verify(componentNodeFilterBusinessLogic,times(1)).validateUser(USER_ID);
    }

    @Test
    void updateNodeFilterFailTest() throws BusinessLogicException, JsonProcessingException {
        initComponentData();
        final String pathFormat = V_1_CATALOG_S_S_COMPONENT_INSTANCE_S_S_S_NODE_FILTER;
        final String path = String.format(pathFormat, componentType, componentId, componentInstance,
            NodeFilterConstraintType.PROPERTIES_PARAM_NAME, 0);

        when(componentNodeFilterBusinessLogic.validateUser(USER_ID)).thenReturn(user);

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header(USER_ID_HEADER, USER_ID)
            .put(Entity.entity(inputJson, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
        verify(componentNodeFilterBusinessLogic,times(1)).validateUser(USER_ID);
    }

    @Test
    void updateNodeFilterFailConstraintParseTest() throws JsonProcessingException {
        initComponentData();
        final String pathFormat = V_1_CATALOG_S_S_COMPONENT_INSTANCE_S_S_S_NODE_FILTER;
        final String path = String.format(pathFormat, componentType, componentId, componentInstance,
            NodeFilterConstraintType.PROPERTIES_PARAM_NAME, 0);

        when(componentNodeFilterBusinessLogic.validateUser(USER_ID)).thenReturn(user);

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header(USER_ID_HEADER, USER_ID)
            .put(Entity.entity(inputJson, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
        verify(componentNodeFilterBusinessLogic,times(1)).validateUser(USER_ID);
    }

    @Test
    void updateNodeFilterFailConvertTest() throws JsonProcessingException {
        initComponentData();
        final String pathFormat = V_1_CATALOG_S_S_COMPONENT_INSTANCE_S_S_S_NODE_FILTER;
        final String path = String.format(pathFormat, componentType, componentId, componentInstance,
            NodeFilterConstraintType.PROPERTIES_PARAM_NAME, 0);

        when(componentNodeFilterBusinessLogic.validateUser(USER_ID)).thenReturn(user);

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header(USER_ID_HEADER, USER_ID)
            .put(Entity.entity(inputJson, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
        verify(componentNodeFilterBusinessLogic,times(1)).validateUser(USER_ID);
    }

    @Test
    void deleteNodeFilterSuccessTest() throws BusinessLogicException, JsonProcessingException {
        initComponentData();
        final String pathFormat = V_1_CATALOG_S_S_COMPONENT_INSTANCE_S_S_S_NODE_FILTER;
        final String path = String.format(pathFormat, componentType, componentId, componentInstance,
            NodeFilterConstraintType.PROPERTIES_PARAM_NAME, 0);

        when(componentNodeFilterBusinessLogic.validateUser(USER_ID)).thenReturn(user);
        doReturn(componentsUtils).when(servletUtils).getComponentsUtils();
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);

        when(componentNodeFilterBusinessLogic
            .deleteNodeFilter(componentId, componentInstance, 0, true, ComponentTypeEnum.RESOURCE, NodeFilterConstraintType.PROPERTIES))
            .thenReturn(Optional.of(ciNodeFilterDataDefinition));

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(USER_ID_HEADER, USER_ID)
            .delete(Response.class);

        verify(componentNodeFilterBusinessLogic, times(1))
            .deleteNodeFilter(anyString(), anyString(), anyInt(), anyBoolean(), any(ComponentTypeEnum.class), any(NodeFilterConstraintType.class));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
        verify(componentNodeFilterBusinessLogic,times(1)).validateUser(USER_ID);
    }

    @Test
    void deleteNodeFilterFailTest() {
        final String pathFormat = V_1_CATALOG_S_S_COMPONENT_INSTANCE_S_S_S_NODE_FILTER;
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

    private void initComponentData() {
        final PropertyFilterDataDefinition propertyFilterDataDefinition =
            new PropertyFilterDataDefinition();
        propertyFilterDataDefinition.setName(uiConstraint.getServicePropertyName());
        propertyFilterDataDefinition.setConstraints(new LinkedList<>(List.of(new FilterConstraintMapper().mapTo(filterConstraintDto))));

        final ListDataDefinition<PropertyFilterDataDefinition> propertyDataDefinitionList =
            new ListDataDefinition<>(new LinkedList<>(List.of(propertyFilterDataDefinition)));

        final RequirementNodeFilterCapabilityDataDefinition requirementNodeFilterCapabilityDataDefinition =
            new RequirementNodeFilterCapabilityDataDefinition();
        requirementNodeFilterCapabilityDataDefinition.setName(uiConstraint.getServicePropertyName());
        requirementNodeFilterCapabilityDataDefinition.setProperties(propertyDataDefinitionList);

        final ListDataDefinition<RequirementNodeFilterCapabilityDataDefinition> capabilityDataDefinitionList =
            new ListDataDefinition<>(new LinkedList<>(List.of(requirementNodeFilterCapabilityDataDefinition)));

        ciNodeFilterDataDefinition = new CINodeFilterDataDefinition();
        ciNodeFilterDataDefinition.setProperties(propertyDataDefinitionList);
        ciNodeFilterDataDefinition.setCapabilities(capabilityDataDefinitionList);
        ciNodeFilterDataDefinition.setID("NODE_FILTER_UID");

    }

    private String buildConstraintDataJson(final UIConstraint uiConstraint) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(uiConstraint);
    }

}
