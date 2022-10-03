/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021, Nordix Foundation. All rights reserved.
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
 */
package org.openecomp.sdc.be.servlets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum.RESOURCE;

import fj.data.Either;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import org.assertj.core.api.Assertions;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.OutputsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.SpringConfig;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.ComponentInstOutputsMap;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.servlets.exception.ComponentExceptionMapper;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.WebApplicationContext;

@TestInstance(Lifecycle.PER_CLASS)
class OutputsServletTest extends JerseyTest {

    private static final String USER_ID = "jh0003";
    private static final String COMPONENT_ID = "componentId";
    private static final String RESOURCES = "resources";
    private static final String COMPONENT_INST_OUTPUTS_MAP_OBJ = "componentInstOutputsMapObj";
    private static final String OUTPUT_ID = "outputId";
    private static final String INSTANCE_ID = "instanceId";
    private static final String COMPONENT_UID = "originComponentUid";

    private final UserBusinessLogic userBusinessLogic = mock(UserBusinessLogic.class);
    private final OutputsBusinessLogic outputsBusinessLogic = mock(OutputsBusinessLogic.class);
    private final ComponentInstanceBusinessLogic componentInstanceBL = mock(ComponentInstanceBusinessLogic.class);
    private final HttpSession httpSession = mock(HttpSession.class);
    private final ServletContext servletContext = mock(ServletContext.class);
    private final WebApplicationContext webApplicationContext = mock(WebApplicationContext.class);
    private final ComponentsUtils componentsUtils = mock(ComponentsUtils.class);
    private final ServletUtils servletUtils = mock(ServletUtils.class);
    private final ResourceImportManager resourceImportManager = mock(ResourceImportManager.class);
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final WebAppContextWrapper wrapper = mock(WebAppContextWrapper.class);

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        when(request.getSession()).thenReturn(httpSession);
        when(httpSession.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(wrapper);
        when(wrapper.getWebAppContext(any())).thenReturn(webApplicationContext);
        when(webApplicationContext.getBean(OutputsBusinessLogic.class)).thenReturn(outputsBusinessLogic);
        when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void test_getComponentInstanceOutputs_success() {
        final Either<List<OutputDefinition>, ResponseFormat> listResponseFormatEither = Either.left(new ArrayList<>());
        doReturn(listResponseFormatEither).when(outputsBusinessLogic).getComponentInstanceOutputs(USER_ID, COMPONENT_ID, INSTANCE_ID);

        final ResponseFormat responseFormat = new ResponseFormat(HttpStatus.OK_200.getStatusCode());
        doReturn(responseFormat).when(componentsUtils).getResponseFormat(ActionStatus.OK);

        final OutputsServlet outputsServlet = createTestObject();
        Assertions.assertThat(outputsServlet).isNotNull();

        final Response response = outputsServlet.getComponentInstanceOutputs(RESOURCES, COMPONENT_ID, INSTANCE_ID, COMPONENT_UID, request, USER_ID);
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200.getStatusCode());
        verify(componentsUtils, times(2)).getResponseFormat(ActionStatus.OK);
    }

    @Test
    void test_getComponentInstanceOutputs_isRight() {
        final ResponseFormat responseFormat = new ResponseFormat(HttpStatus.BAD_REQUEST_400.getStatusCode());
        final Either<Object, ResponseFormat> right = Either.right(responseFormat);
        doReturn(right).when(outputsBusinessLogic).getComponentInstanceOutputs(USER_ID, COMPONENT_ID, INSTANCE_ID);

        doReturn(responseFormat).when(componentsUtils).getResponseFormat(ActionStatus.GENERAL_ERROR);

        final OutputsServlet outputsServlet = createTestObject();
        Assertions.assertThat(outputsServlet).isNotNull();

        final Response response = outputsServlet.getComponentInstanceOutputs(RESOURCES, COMPONENT_ID, INSTANCE_ID, COMPONENT_UID, request, USER_ID);
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST_400.getStatusCode());
        verify(componentsUtils, times(0)).getResponseFormat(ActionStatus.GENERAL_ERROR);
    }

    @Test
    void test_getComponentInstanceOutputs_fail() {
        final ResponseFormat responseFormat = new ResponseFormat(HttpStatus.BAD_REQUEST_400.getStatusCode());
        final Either<Object, ResponseFormat> right = Either.right(responseFormat);
        doReturn(right).when(outputsBusinessLogic).getComponentInstanceOutputs(USER_ID, COMPONENT_ID, INSTANCE_ID);

        doThrow(new RuntimeException()).when(componentsUtils).getResponseFormat(ActionStatus.GENERAL_ERROR);

        final OutputsServlet outputsServlet = createTestObject();
        Assertions.assertThat(outputsServlet).isNotNull();

        final Response response = outputsServlet.getComponentInstanceOutputs(RESOURCES, COMPONENT_ID, INSTANCE_ID, COMPONENT_UID, request, USER_ID);
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST_400.getStatusCode());
        verify(componentsUtils, times(0)).getResponseFormat(ActionStatus.GENERAL_ERROR);
    }

    @Test
    void test_createMultipleOutputs_success() {
        final ComponentInstOutputsMap componentInstOutputsMap = new ComponentInstOutputsMap();
        final Either<ComponentInstOutputsMap, ResponseFormat> left = Either.left(componentInstOutputsMap);

        doReturn(left).when(componentsUtils)
            .convertJsonToObjectUsingObjectMapper(any(String.class), any(User.class), eq(ComponentInstOutputsMap.class),
                eq(AuditingActionEnum.CREATE_RESOURCE), eq(RESOURCE));
        final Either<List<OutputDefinition>, ResponseFormat> listResponseFormatEither = Either.left(new ArrayList<>());
        doReturn(listResponseFormatEither).when(outputsBusinessLogic).declareAttributes(USER_ID, COMPONENT_ID, RESOURCE, componentInstOutputsMap);

        final ResponseFormat responseFormat = new ResponseFormat(HttpStatus.OK_200.getStatusCode());
        doReturn(responseFormat).when(componentsUtils).getResponseFormat(ActionStatus.OK);

        final OutputsServlet outputsServlet = createTestObject();
        Assertions.assertThat(outputsServlet).isNotNull();

        final Response response = outputsServlet.createMultipleOutputs(RESOURCES, COMPONENT_ID, request, USER_ID, COMPONENT_INST_OUTPUTS_MAP_OBJ);
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200.getStatusCode());
        verify(componentsUtils, times(3)).getResponseFormat(ActionStatus.OK);
    }

    @Test
    void test_createMultipleOutputs_isRight() {
        final ResponseFormat responseFormat = new ResponseFormat(HttpStatus.BAD_REQUEST_400.getStatusCode());
        final Either<Object, ResponseFormat> right = Either.right(responseFormat);
        doReturn(right).when(outputsBusinessLogic).declareAttributes(USER_ID, COMPONENT_ID, RESOURCE, new ComponentInstOutputsMap());

        doReturn(responseFormat).when(componentsUtils).getResponseFormat(ActionStatus.GENERAL_ERROR);

        final OutputsServlet outputsServlet = createTestObject();
        Assertions.assertThat(outputsServlet).isNotNull();

        final Response response = outputsServlet.createMultipleOutputs(RESOURCES, COMPONENT_ID, request, USER_ID, COMPONENT_INST_OUTPUTS_MAP_OBJ);
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST_400.getStatusCode());
        verify(componentsUtils, times(3)).getResponseFormat(ActionStatus.GENERAL_ERROR);
    }

    @Test
    void test_createMultipleOutputs_fail() {
        final ResponseFormat responseFormat = new ResponseFormat(HttpStatus.BAD_REQUEST_400.getStatusCode());
        doThrow(new RuntimeException()).when(outputsBusinessLogic).declareAttributes(USER_ID, COMPONENT_ID, RESOURCE, new ComponentInstOutputsMap());

        doReturn(responseFormat).when(componentsUtils).getResponseFormat(ActionStatus.GENERAL_ERROR);

        final OutputsServlet outputsServlet = createTestObject();
        Assertions.assertThat(outputsServlet).isNotNull();

        final Response response = outputsServlet.createMultipleOutputs(RESOURCES, COMPONENT_ID, request, USER_ID, COMPONENT_INST_OUTPUTS_MAP_OBJ);
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST_400.getStatusCode());
        verify(componentsUtils, times(2)).getResponseFormat(ActionStatus.GENERAL_ERROR);
    }

    @Test
    void test_deleteOutput_success() {
        doReturn(new OutputDefinition()).when(outputsBusinessLogic).deleteOutput(COMPONENT_ID, USER_ID, OUTPUT_ID);

        final ResponseFormat responseFormat = new ResponseFormat(HttpStatus.OK_200.getStatusCode());
        doReturn(responseFormat).when(componentsUtils).getResponseFormat(ActionStatus.OK);

        final OutputsServlet outputsServlet = createTestObject();
        Assertions.assertThat(outputsServlet).isNotNull();

        final Response response = outputsServlet.deleteOutput(RESOURCES, COMPONENT_ID, OUTPUT_ID, request, USER_ID, COMPONENT_INST_OUTPUTS_MAP_OBJ);
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200.getStatusCode());
        verify(componentsUtils, times(1)).getResponseFormat(ActionStatus.OK);
    }

    @Test
    void test_deleteOutput_fail() {
        final ResponseFormat responseFormat = new ResponseFormat(HttpStatus.BAD_REQUEST_400.getStatusCode());
        doThrow(new ComponentException(responseFormat)).when(outputsBusinessLogic).deleteOutput(COMPONENT_ID, USER_ID, OUTPUT_ID);

        doReturn(responseFormat).when(componentsUtils).getResponseFormat(ActionStatus.GENERAL_ERROR);

        final OutputsServlet outputsServlet = createTestObject();
        Assertions.assertThat(outputsServlet).isNotNull();

        final Response response = outputsServlet.deleteOutput(RESOURCES, COMPONENT_ID, OUTPUT_ID, request, USER_ID, COMPONENT_INST_OUTPUTS_MAP_OBJ);
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST_400.getStatusCode());
        verify(componentsUtils, times(1)).getResponseFormat(ActionStatus.GENERAL_ERROR);
    }

    private OutputsServlet createTestObject() {
        return new OutputsServlet(outputsBusinessLogic, componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
    }

    @Override
    protected Application configure() {
        final OutputsServlet outputsServlet = createTestObject();
        final ResourceConfig resourceConfig = new ResourceConfig()
            .register(outputsServlet)
            .register(new ComponentExceptionMapper(componentsUtils))
            .register(new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(request).to(HttpServletRequest.class);
                }
            });

        resourceConfig.property("contextConfig", new AnnotationConfigApplicationContext(SpringConfig.class));
        return resourceConfig;
    }

}
