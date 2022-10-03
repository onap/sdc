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

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.ArgumentMatchers;
import org.openecomp.sdc.be.components.impl.CapabilitiesBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.validation.CapabilitiesValidation;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.SpringConfig;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.CapabilitiesOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
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

@TestInstance(Lifecycle.PER_CLASS)
class CapabilityServletTest extends JerseyTest {

    private static final String componentId = "dac65869-dfb4-40d2-aa20-084324659ec1";
    private static final String USER_ID = "cs0008";
    private static final String INVALID_ID = "InvalidId";
    private static final String INVALID_CONTENT = "InvalidContent";

    private HttpServletRequest request;
    private HttpSession session;
    private ServletContext servletContext;
    private WebAppContextWrapper webAppContextWrapper;
    private WebApplicationContext webApplicationContext;
    private UserBusinessLogic userBusinessLogic;
    private ComponentInstanceBusinessLogic componentInstanceBusinessLogic;
    private ComponentsUtils componentsUtils;
    private ServletUtils servletUtils;
    private ResourceImportManager resourceImportManager;
    private CapabilitiesBusinessLogic capabilitiesBusinessLogic;
    private CapabilitiesValidation capabilitiesValidation;
    private ResponseFormat responseFormat;
    private UserValidations userValidations;
    private ToscaOperationFacade toscaOperationFacade;
    private CapabilitiesOperation capabilitiesOperation;
    private Component component;
    private User user;
    private JSONObject inputJson;
    private CapabilityDefinition capabilityDefinition;
    private UiComponentDataTransfer uiComponentDataTransfer;
    private CapabilityServlet capabilityServlet;
    private Response response;


    @BeforeAll
    public void initClass() {
        createMocks();
        createTestObject();
        assertThat(capabilityServlet).isNotNull();
        initComponentData();
        when(request.getSession()).thenReturn(session);
        when(session.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR))
            .thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webApplicationContext);
        when(webApplicationContext.getBean(CapabilitiesBusinessLogic.class)).thenReturn(capabilitiesBusinessLogic);
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

    private void createMocks() {
        request = mock(HttpServletRequest.class);
        userBusinessLogic = mock(UserBusinessLogic.class);
        componentsUtils = mock(ComponentsUtils.class);
        servletUtils = mock(ServletUtils.class);
        resourceImportManager = mock(ResourceImportManager.class);
        capabilitiesBusinessLogic = mock(CapabilitiesBusinessLogic.class);
        componentInstanceBusinessLogic = mock(ComponentInstanceBusinessLogic.class);
        session = mock(HttpSession.class);
        servletContext = mock(ServletContext.class);
        webAppContextWrapper = mock(WebAppContextWrapper.class);
        webApplicationContext = mock(WebApplicationContext.class);
        responseFormat = mock(ResponseFormat.class);
        userValidations = mock(UserValidations.class);
        capabilitiesValidation = mock(CapabilitiesValidation.class);
        toscaOperationFacade = mock(ToscaOperationFacade.class);
        capabilitiesOperation = mock(CapabilitiesOperation.class);
    }

    @BeforeEach
    public void resetMock() throws Exception {
        super.setUp();
        reset(capabilitiesBusinessLogic);
    }

    @AfterEach
    void after() throws Exception {
        super.tearDown();
    }

    private void initComponentData() {
        inputJson = new JSONObject();
        inputJson.put("capabilities", "tosca.capabilities.nfv.VirtualLinkable");
        inputJson.put("description", "A node type that includes the VirtualLinkable capability");
        inputJson.put("properties", "");
        user = new User();
        user.setUserId(USER_ID);
        user.setRole(Role.ADMIN.name());

        capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setName("cap" + Math.random());
        capabilityDefinition.setType("tosca.capabilities.nfv.VirtualLinkable");
        capabilityDefinition.setOwnerId("resourceId");
        capabilityDefinition.setUniqueId("capUniqueId");
        final List<String> path = new ArrayList<>();
        path.add("path1");
        capabilityDefinition.setPath(path);

        final Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
        final List<CapabilityDefinition> capabilityDefinitions = new LinkedList<>();
        capabilityDefinitions.add(null);
        capabilityDefinitions.add(capabilityDefinition);
        capabilities.put("Key", capabilityDefinitions);
        component = new Resource();
        component.setCapabilities(capabilities);
        uiComponentDataTransfer = new UiComponentDataTransfer();
        uiComponentDataTransfer.setCapabilities(component.getCapabilities());
    }

    @Override
    protected ResourceConfig configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        final ApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class);
        return new ResourceConfig(CapabilityServlet.class)
            .register(new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(request).to(HttpServletRequest.class);
                    bind(userBusinessLogic).to(UserBusinessLogic.class);
                    bind(componentInstanceBusinessLogic).to(ComponentInstanceBusinessLogic.class);
                    bind(componentsUtils).to(ComponentsUtils.class);
                    bind(servletUtils).to(ServletUtils.class);
                    bind(resourceImportManager).to(ResourceImportManager.class);
                    bind(capabilitiesBusinessLogic).to(CapabilitiesBusinessLogic.class);
                }
            })
            .property("contextConfig", context);
    }

    private void createTestObject() {
        capabilityServlet = new CapabilityServlet(componentInstanceBusinessLogic,
            componentsUtils, servletUtils, resourceImportManager, capabilitiesBusinessLogic);
    }

    @Test
    void createCapabilitiesSuccessTest() {
        when(componentsUtils.convertJsonToObjectUsingObjectMapper(anyString(), any(User.class),
            any(Class.class), ArgumentMatchers.any(AuditingActionEnum.class),
            ArgumentMatchers.any(ComponentTypeEnum.class))).thenReturn(Either.left(uiComponentDataTransfer));

        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(userBusinessLogic.getUser(USER_ID)).thenReturn(user);
        when(capabilitiesValidation.validateCapabilities(singletonList(capabilityDefinition), component, false))
            .thenReturn(Either.left(true));
        when(capabilitiesOperation.addCapabilities(componentId, singletonList(capabilityDefinition)))
            .thenReturn(Either.left(singletonList(capabilityDefinition)));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
            .thenReturn(Either.left(component));
        when(capabilitiesBusinessLogic
            .createCapabilities(anyString(), anyList(), any(User.class), anyString(), anyBoolean()))
            .thenReturn(Either.left(singletonList(capabilityDefinition)));

        assertThat(capabilityServlet).isNotNull();

        response = capabilityServlet
            .createCapabilitiesOnResource(inputJson.toString(), componentId, request, USER_ID);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);

        response = capabilityServlet
            .createCapabilitiesOnService(inputJson.toString(), componentId, request, USER_ID);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    void createCapabilitiesFailTest() {
        when(componentsUtils.convertJsonToObjectUsingObjectMapper(anyString(), any(User.class),
            any(Class.class), ArgumentMatchers.any(AuditingActionEnum.class),
            ArgumentMatchers.any(ComponentTypeEnum.class))).thenReturn(Either.left(uiComponentDataTransfer));

        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(userBusinessLogic.getUser(USER_ID)).thenReturn(user);
        when(capabilitiesValidation.validateCapabilities(singletonList(capabilityDefinition), component, false))
            .thenReturn(Either.left(true));
        when(capabilitiesOperation.addCapabilities(componentId, singletonList(capabilityDefinition)))
            .thenReturn(Either.left(singletonList(capabilityDefinition)));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(responseFormat);
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
            .thenReturn(Either.left(component));
        when(capabilitiesBusinessLogic
            .createCapabilities(anyString(), anyList(), any(User.class), anyString(), anyBoolean()))
            .thenReturn(Either.left(singletonList(capabilityDefinition)));

        response = capabilityServlet
            .createCapabilitiesOnResource(inputJson.toString(), componentId, request, USER_ID);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);

        response = capabilityServlet
            .createCapabilitiesOnService(inputJson.toString(), componentId, request, USER_ID);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    void createOrUpdateCapabilitiesFailWithNoContentTest() {
        when(componentsUtils.convertJsonToObjectUsingObjectMapper(anyString(), any(User.class),
            any(Class.class), ArgumentMatchers.any(AuditingActionEnum.class),
            ArgumentMatchers.any(ComponentTypeEnum.class)))
            .thenReturn(Either.right(new ResponseFormat(HttpStatus.NO_CONTENT_204)));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.NO_CONTENT_204);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(responseFormat);

        response = capabilityServlet
            .createCapabilitiesOnResource(INVALID_CONTENT, componentId, request, USER_ID);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);

        response = capabilityServlet
            .createCapabilitiesOnService(INVALID_CONTENT, componentId, request, USER_ID);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
    }

    @Test
    void updateCapabilitiesSuccessTest() {
        when(componentsUtils.convertJsonToObjectUsingObjectMapper(anyString(), any(User.class),
            any(Class.class), ArgumentMatchers.any(AuditingActionEnum.class),
            ArgumentMatchers.any(ComponentTypeEnum.class))).thenReturn(Either.left(uiComponentDataTransfer));

        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(userBusinessLogic.getUser(USER_ID)).thenReturn(user);
        when(capabilitiesValidation.validateCapabilities(singletonList(capabilityDefinition), component, false))
            .thenReturn(Either.left(true));
        when(capabilitiesOperation.addCapabilities(componentId, singletonList(capabilityDefinition)))
            .thenReturn(Either.left(singletonList(capabilityDefinition)));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
            .thenReturn(Either.left(component));
        when(capabilitiesBusinessLogic
            .updateCapabilities(anyString(), anyList(), any(User.class), anyString(), anyBoolean()))
            .thenReturn(Either.left(singletonList(capabilityDefinition)));

        response = capabilityServlet
            .updateCapabilitiesOnResource(inputJson.toString(), componentId, request, USER_ID);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);

        response = capabilityServlet
            .updateCapabilitiesOnService(inputJson.toString(), componentId, request, USER_ID);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    void getCapabilitySuccessTest() {
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
            .thenReturn(Either.left(component));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
        when(capabilitiesBusinessLogic
            .getCapability(anyString(), anyString(), any(User.class), anyBoolean()))
            .thenReturn(Either.left(capabilityDefinition));

        response = capabilityServlet
            .getCapabilityOnResource(componentId, capabilityDefinition.getUniqueId(), request, USER_ID);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);

        response = capabilityServlet
            .getCapabilityOnService(componentId, capabilityDefinition.getUniqueId(), request, USER_ID);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    void getCapabilityFailTest() {
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
            .thenReturn(Either.left(component));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.NOT_FOUND_404);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(responseFormat);
        when(capabilitiesBusinessLogic
            .getCapability(componentId, capabilityDefinition.getUniqueId(), user, true))
            .thenReturn(Either.left(capabilityDefinition));

        response = capabilityServlet
            .getCapabilityOnResource(componentId, INVALID_ID, request, USER_ID);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND_404);

        response = capabilityServlet
            .getCapabilityOnService(componentId, INVALID_ID, request, USER_ID);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND_404);
    }

    @Test
    void deleteCapabilitySuccessTest() {
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
            .thenReturn(Either.left(component));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
        when(capabilitiesBusinessLogic.deleteCapability(anyString(), anyString(), any(User.class), anyBoolean()))
            .thenReturn(Either.left(capabilityDefinition));

        response = capabilityServlet
            .deleteCapabilityOnResource(componentId, capabilityDefinition.getUniqueId(), request, USER_ID);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);

        response = capabilityServlet
            .deleteCapabilityOnService(componentId, capabilityDefinition.getUniqueId(), request, USER_ID);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    void deleteCapabilityFailTest() {
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
            .thenReturn(Either.left(component));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.NOT_FOUND_404);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(responseFormat);
        when(capabilitiesBusinessLogic.deleteCapability(anyString(), anyString(), any(User.class), anyBoolean()))
            .thenReturn(Either.left(capabilityDefinition));

        response = capabilityServlet
            .deleteCapabilityOnResource(componentId, INVALID_ID, request, USER_ID);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND_404);

        response = capabilityServlet
            .deleteCapabilityOnService(componentId, INVALID_ID, request, USER_ID);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND_404);
    }
}
