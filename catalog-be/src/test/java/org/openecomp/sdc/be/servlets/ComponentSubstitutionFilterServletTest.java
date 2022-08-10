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
import static org.mockito.ArgumentMatchers.anyList;
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
import java.util.Collections;
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
import org.mockito.ArgumentMatchers;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentSubstitutionFilterBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.exceptions.BusinessLogicException;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.SpringConfig;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SubstitutionFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SubstitutionFilterPropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
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

public class ComponentSubstitutionFilterServletTest extends JerseyTest {

    private static final String USER_ID = "jh0003";
    private static final String servicePropertyName = "controller_actor";
    private static final String constraintOperator = "equal";
    private static final String sourceType = "static";
    private static final String sourceName = sourceType;
    private static final String propertyValue = "constraintValue";
    private static final String componentId = "dac65869-dfb4-40d2-aa20-084324659ec1";
    private static final String constraintType = "properties";
    private static final String componentType = "services";

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
    private static ComponentSubstitutionFilterBusinessLogic componentSubstitutionFilterBusinessLogic;
    private static ResponseFormat responseFormat;
    private static UserValidations userValidations;
    private SubstitutionFilterDataDefinition substitutionFilterDataDefinition;
    private UIConstraint uiConstraint;
    private String inputJson;
    private User user;
    private FilterConstraintDto filterConstraintDto;

    @BeforeAll
    public static void initClass() {
        createMocks();
        when(request.getSession()).thenReturn(session);
        when(session.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR))
            .thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webApplicationContext);
        when(webApplicationContext.getBean(ComponentSubstitutionFilterBusinessLogic.class))
            .thenReturn(componentSubstitutionFilterBusinessLogic);
        when(request.getHeader("USER_ID")).thenReturn(USER_ID);
        when(webApplicationContext.getBean(ServletUtils.class)).thenReturn(servletUtils);
        when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);
        String appConfigDir = "src/test/resources/config/catalog-be";
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
        org.openecomp.sdc.be.config.Configuration configuration = new org.openecomp.sdc.be.config.Configuration();
        configuration.setJanusGraphInMemoryGraph(true);
        configurationManager.setConfiguration(configuration);
        ExternalConfiguration.setAppName("catalog-be");
    }

    @BeforeEach
    public void resetMock() throws Exception {
        super.setUp();
        reset(componentSubstitutionFilterBusinessLogic);
        initComponentData();
    }

    @AfterEach
    void after() throws Exception {
        super.tearDown();
    }

    @Test
    void addSubstitutionFilterTest() throws Exception {
        final String pathFormat = "/v1/catalog/%s/%s/substitutionFilter/%s";
        final String path = String.format(pathFormat, componentType, componentId, constraintType);

        when(userValidations.validateUserExists(user)).thenReturn(user);
        when(componentSubstitutionFilterBusinessLogic.validateUser(USER_ID)).thenReturn(user);
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);

        assertNotNull(uiConstraint);
        assertThat(uiConstraint.getServicePropertyName()).isEqualToIgnoringCase(servicePropertyName);
        assertThat(uiConstraint.getConstraintOperator()).isEqualToIgnoringCase(constraintOperator);
        assertThat(uiConstraint.getSourceType()).isEqualToIgnoringCase(sourceType);
        assertThat(uiConstraint.getSourceName()).isEqualToIgnoringCase(sourceName);
        assertThat(uiConstraint.getValue().toString()).isEqualToIgnoringCase(propertyValue);

        when(componentsUtils.parseToConstraint(anyString(), any(User.class), any(ComponentTypeEnum.class)))
            .thenReturn(Optional.of(uiConstraint));

        assertNotNull(substitutionFilterDataDefinition);
        assertThat(substitutionFilterDataDefinition.getProperties().getListToscaDataDefinition()).hasSize(1);
        when(componentSubstitutionFilterBusinessLogic.addSubstitutionFilter(componentId, filterConstraintDto, true, ComponentTypeEnum.SERVICE))
            .thenReturn(Optional.of(substitutionFilterDataDefinition));

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header(USER_ID_HEADER, USER_ID)
            .post(Entity.entity(inputJson, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);

        verify(componentSubstitutionFilterBusinessLogic, times(1))
                .addSubstitutionFilter(componentId, filterConstraintDto, true, ComponentTypeEnum.SERVICE);
    }

    @Test
    void addSubstitutionFilterFailConstraintParseTest() {
        final String pathFormat = "/v1/catalog/%s/%s/substitutionFilter/%s";
        final String path = String.format(pathFormat, componentType, componentId, constraintType);

        when(userValidations.validateUserExists(user)).thenReturn(user);
        when(componentSubstitutionFilterBusinessLogic.validateUser(USER_ID)).thenReturn(user);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);

        when(componentsUtils.parseToConstraint(anyString(), any(User.class), nullable(ComponentTypeEnum.class)))
            .thenReturn(Optional.empty());

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header(USER_ID_HEADER, USER_ID)
            .post(Entity.entity(inputJson, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    void addSubstitutionFilterFailTest() {
        final String pathFormat = "/v1/catalog/%s/%s/substitutionFilter/%s";
        final String path = String.format(pathFormat, componentType, componentId, constraintType);

        when(userValidations.validateUserExists(user)).thenReturn(user);
        when(componentSubstitutionFilterBusinessLogic.validateUser(USER_ID)).thenReturn(user);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);

        when(componentsUtils.parseToConstraint(anyString(), any(User.class), any(ComponentTypeEnum.class)))
            .thenReturn(Optional.of(uiConstraint));

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header(USER_ID_HEADER, USER_ID)
            .post(Entity.entity(inputJson, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    void updateSubstitutionFilterTest() throws BusinessLogicException {
        final String pathFormat = "/v1/catalog/%s/%s/substitutionFilter/%s";
        final String path = String.format(pathFormat, componentType, componentId, constraintType);

        when(userValidations.validateUserExists(user)).thenReturn(user);
        when(componentSubstitutionFilterBusinessLogic.validateUser(USER_ID)).thenReturn(user);
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);

        when(componentsUtils.validateAndParseConstraint(ArgumentMatchers.any(ComponentTypeEnum.class), anyString(),
            any(User.class))).thenReturn(Collections.singletonList(uiConstraint));

        when(componentSubstitutionFilterBusinessLogic.updateSubstitutionFilter(componentId.toLowerCase(),
            List.of(filterConstraintDto), true, ComponentTypeEnum.SERVICE))
            .thenReturn(Optional.ofNullable(substitutionFilterDataDefinition));

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header(USER_ID_HEADER, USER_ID)
            .put(Entity.entity(inputJson, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);

        verify(componentSubstitutionFilterBusinessLogic, times(1))
                .updateSubstitutionFilter(anyString(), anyList(), anyBoolean(), any(ComponentTypeEnum.class));
    }

    @Test
    void updateSubstitutionFilterFailConstraintParseTest() {
        final String pathFormat = "/v1/catalog/%s/%s/substitutionFilter/%s";
        final String path = String.format(pathFormat, componentType, componentId, constraintType);

        when(userValidations.validateUserExists(user)).thenReturn(user);
        when(componentSubstitutionFilterBusinessLogic.validateUser(USER_ID)).thenReturn(user);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);

        when(componentsUtils.validateAndParseConstraint(ArgumentMatchers.any(ComponentTypeEnum.class), anyString(),
            any(User.class))).thenReturn(Collections.emptyList());

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header(USER_ID_HEADER, USER_ID)
            .put(Entity.entity(inputJson, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    void updateSubstitutionFilterFailTest()  {
        final String pathFormat = "/v1/catalog/%s/%s/substitutionFilter/%s";
        final String path = String.format(pathFormat, componentType, componentId, constraintType);

        when(userValidations.validateUserExists(user)).thenReturn(user);
        when(componentSubstitutionFilterBusinessLogic.validateUser(USER_ID)).thenReturn(user);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);

        when(componentsUtils.validateAndParseConstraint(ArgumentMatchers.any(ComponentTypeEnum.class), anyString(),
            any(User.class))).thenReturn(Collections.singletonList(uiConstraint));

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header(USER_ID_HEADER, USER_ID)
            .put(Entity.entity(inputJson, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    void deleteSubstitutionFilterConstraintTest() throws BusinessLogicException {
        final String pathFormat = "/v1/catalog/%s/%s/substitutionFilter/%s/0";
        final String path = String.format(pathFormat, componentType, componentId, constraintType);

        when(userValidations.validateUserExists(user)).thenReturn(user);
        when(componentSubstitutionFilterBusinessLogic.validateUser(USER_ID)).thenReturn(user);
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);

        when(componentSubstitutionFilterBusinessLogic.deleteSubstitutionFilter(componentId, 0,
                true, ComponentTypeEnum.SERVICE))
            .thenReturn(Optional.ofNullable(substitutionFilterDataDefinition));

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header(USER_ID_HEADER, USER_ID)
            .delete(Response.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);

        verify(componentSubstitutionFilterBusinessLogic, times(1))
                .deleteSubstitutionFilter(anyString(), anyInt(), anyBoolean(), any(ComponentTypeEnum.class));
    }

    @Test
    void deleteSubstitutionFilterConstraintFailTest() {
        final String pathFormat = "/v1/catalog/%s/%s/substitutionFilter/%s/0";
        final String path = String.format(pathFormat, componentType, componentId, constraintType);

        when(userValidations.validateUserExists(user)).thenReturn(user);
        when(componentSubstitutionFilterBusinessLogic.validateUser(USER_ID)).thenReturn(user);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
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
        componentSubstitutionFilterBusinessLogic = mock(ComponentSubstitutionFilterBusinessLogic.class);

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
        return new ResourceConfig(ComponentSubstitutionFilterServlet.class)
            .register(new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(request).to(HttpServletRequest.class);
                    bind(userBusinessLogic).to(UserBusinessLogic.class);
                    bind(componentInstanceBusinessLogic).to(ComponentInstanceBusinessLogic.class);
                    bind(componentsUtils).to(ComponentsUtils.class);
                    bind(servletUtils).to(ServletUtils.class);
                    bind(resourceImportManager).to(ResourceImportManager.class);
                    bind(componentSubstitutionFilterBusinessLogic).to(ComponentSubstitutionFilterBusinessLogic.class);
                }
            })
            .property("contextConfig", context);
    }

    private void initComponentData() throws JsonProcessingException {
        uiConstraint = new UIConstraint(servicePropertyName, constraintOperator, sourceType, sourceName, propertyValue);
        final FilterConstraintMapper filterConstraintMapper = new FilterConstraintMapper();
        filterConstraintDto = filterConstraintMapper.mapFrom(uiConstraint);
        inputJson = buildConstraintDataJson(uiConstraint);

        SubstitutionFilterPropertyDataDefinition substitutionFilterPropertyDataDefinition = new SubstitutionFilterPropertyDataDefinition();
        substitutionFilterPropertyDataDefinition.setName(uiConstraint.getServicePropertyName());
        substitutionFilterPropertyDataDefinition.setConstraints(List.of(filterConstraintMapper.mapTo(filterConstraintDto)));

        final ListDataDefinition<SubstitutionFilterPropertyDataDefinition> listDataDefinition =
            new ListDataDefinition<>(List.of(substitutionFilterPropertyDataDefinition));

        substitutionFilterDataDefinition = new SubstitutionFilterDataDefinition();
        substitutionFilterDataDefinition.setProperties(listDataDefinition);
        substitutionFilterDataDefinition.setID("SUBSTITUTION_FILTER_UID");

        user = new User();
        user.setUserId(USER_ID);
        user.setRole(Role.ADMIN.name());
    }

    private String buildConstraintDataJson(final UIConstraint uiConstraint) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(uiConstraint);
    }

}

