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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ModelBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.SpringConfig;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.exception.BusinessException;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.Model;
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

@TestInstance(Lifecycle.PER_CLASS)
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
    @InjectMocks
    private ModelServlet modelServlet;
    @Mock
    private ResponseFormat responseFormat;
    @Mock
    private UserValidations userValidations;

    private Model model;
    private Response response;
    private ModelCreateRequest modelCreateRequest;

    @BeforeAll
    public void initClass() {
        MockitoAnnotations.openMocks(this);
        when(request.getSession()).thenReturn(session);
        when(session.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR))
            .thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webApplicationContext);
        when(webApplicationContext.getBean(ModelBusinessLogic.class)).thenReturn(modelBusinessLogic);
        when(request.getHeader(Constants.USER_ID_HEADER)).thenReturn(USER_ID);
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
    void resetMock() throws Exception {
        super.setUp();
        initTestData();
    }

    @AfterEach
    void after() throws Exception {
        super.tearDown();
    }

    private void initTestData() {
        final String modelName = "MY-INTEGRATION-TEST-MODEL";
        model = new Model(modelName);
        modelCreateRequest = new ModelCreateRequest();
        modelCreateRequest.setName(modelName);
    }

    @Override
    protected ResourceConfig configure() {
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
                }
            })
            .property("contextConfig", context);
    }

    @Test
    void createModelSuccessTest() {
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.CREATED)).thenReturn(responseFormat);
        when(modelBusinessLogic.createModel(any(Model.class))).thenReturn(model);
        response = modelServlet.createModel(modelCreateRequest, USER_ID);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    void createModelFailTest() {
        when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(responseFormat);
        when(modelBusinessLogic.createModel(any(Model.class))).thenReturn(model);
        response = modelServlet.createModel(modelCreateRequest, USER_ID);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    void createModelFailWithModelNameEmptyTest() {
        when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(responseFormat);
        modelCreateRequest.setName(StringUtils.EMPTY);
        final Exception exception = assertThrows(ConstraintViolationException.class, () -> modelServlet.createModel(modelCreateRequest, USER_ID));
        assertThat(exception.getMessage()).isEqualTo("Model name cannot be empty");
    }

    @Test
    void createModelFailWithModelNameNullTest() {
        when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(responseFormat);
        modelCreateRequest.setName(null);
        final Exception exception = assertThrows(ConstraintViolationException.class, () -> modelServlet.createModel(modelCreateRequest, USER_ID));
        assertThat(exception.getMessage()).isEqualTo("Model name cannot be null");
    }

    @Test
    void createModelThrowsBusinessExceptionTest() {
        when(modelBusinessLogic.createModel(model)).thenThrow(new BusinessException() {});
        assertThrows(BusinessException.class, () -> modelServlet.createModel(modelCreateRequest, USER_ID));
    }

}