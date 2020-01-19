/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fj.data.Either;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.GroupBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.config.SpringConfig;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum.SERVICE_PARAM_NAME;

/**
 * The test suite designed for test functionality of ComponentInstanceServlet class
 */
public class ComponentInstanceServletTest extends JerseyTest {

    private final static String USER_ID = "jh0003";
    private static HttpServletRequest request;
    private static HttpSession session;
    private static ServletContext servletContext;
    private static WebAppContextWrapper webAppContextWrapper;
    private static WebApplicationContext webApplicationContext;
    private static ComponentInstanceBusinessLogic componentInstanceBusinessLogic;
    private static ComponentsUtils componentsUtils;
    private static ServletUtils servletUtils;
    private static ResponseFormat responseFormat;
    private static UserBusinessLogic userBusinessLogic;
    private static GroupBusinessLogic groupBusinessLogic;
    private static ResourceImportManager resourceImportManager;
    private static ServiceBusinessLogic serviceBusinessLogic;

    @BeforeClass
    public static void setup() {
        createMocks();
        stubMethods();
    }

    @Test
    public void testGetRelationByIdSuccess(){

        String containerComponentType = "resources";
        String componentId = "componentId";
        String relationId = "relationId";
        String path = "/v1/catalog/" + containerComponentType + "/" + componentId + "/" + relationId + "/relationId";
        Either<RequirementCapabilityRelDef, ResponseFormat> successResponse = Either.left(new RequirementCapabilityRelDef());
        when(componentInstanceBusinessLogic.getRelationById(eq(componentId), eq(relationId), eq(USER_ID), eq(ComponentTypeEnum.RESOURCE))).thenReturn(successResponse);
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
        Response response = target()
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("USER_ID", USER_ID)
                .get( Response.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    public void testGetRelationByIdFailure(){

        String containerComponentType = "unknown_type";
        String componentId = "componentId";
        String relationId = "relationId";
        String path = "/v1/catalog/" + containerComponentType + "/" + componentId + "/" + relationId + "/relationId";
        when(responseFormat.getStatus()).thenReturn(HttpStatus.BAD_REQUEST_400);
        when(componentsUtils.getResponseFormat(eq(ActionStatus.UNSUPPORTED_ERROR), eq(containerComponentType))).thenReturn(responseFormat);
        Response response = target()
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .header("USER_ID", USER_ID)
                .get( Response.class);

        assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST_400);
    }

    @Test
    public void testBatchDeleteResourceInstancesSuccess() {

        String componentId = "componentId";
        String containerComponentType = ComponentTypeEnum.SERVICE_PARAM_NAME;
        String compId1 = "compId1";
        String[] delCompIds = new String[1];
        delCompIds[0] = compId1;
        List<ComponentInstance> compInsts = new ArrayList<ComponentInstance>();
        String path = "/v1/catalog/" + containerComponentType + "/" + componentId + "/batchDeleteResourceInstances";

        ComponentInstance compInst = new ComponentInstance();
        compInst.setName(compId1);
        compInst.setUniqueId(compId1);
        compInst.setComponentUid(compId1);
        compInst.setInvariantName(compId1);
        compInsts.add(compInst);

        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
        Either<String[], ResponseFormat> convertStatusEither = Either.left(delCompIds);
        when(componentsUtils
                .convertJsonToObjectUsingObjectMapper(anyString(), any(User.class), ArgumentMatchers.<Class<String[]>>any(),
                        nullable(AuditingActionEnum.class), nullable(ComponentTypeEnum.class))).thenReturn(convertStatusEither);
        when(componentInstanceBusinessLogic
                .batchDeleteComponentInstance(eq(containerComponentType), eq(componentId), any(List.class),
                        eq(USER_ID))).thenReturn(Mockito.mock(Map.class));

        Response response = target()
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .header("USER_ID", USER_ID)
                .post(Entity.json(compInsts));

        assertEquals(HttpStatus.OK_200, response.getStatus());
    }

    @Test
    public void testBatchDeleteResourceInstancesFailure() {

        String componentId = "componentId";
        String containerComponentType = ComponentTypeEnum.SERVICE_PARAM_NAME;
        String path = "/v1/catalog/" + containerComponentType + "/" + componentId + "/batchDeleteResourceInstances";

        when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT)).thenReturn(responseFormat);

        Response response = target()
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .header("USER_ID", USER_ID)
                .post(Entity.json(""));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500, response.getStatus());
    }

    @Test
    public void testBatchDissociateRIFromRISuccess() {

        String componentId = "componentId";
        String containerComponentType = ComponentTypeEnum.SERVICE_PARAM_NAME;
        String path = "/v1/catalog/" + containerComponentType + "/" + componentId + "/resourceInstance/batchDissociate";
        RequirementCapabilityRelDef[] refs = new RequirementCapabilityRelDef[1];
        RequirementCapabilityRelDef ref = new RequirementCapabilityRelDef();
        refs[0] = ref;

        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
        Either<RequirementCapabilityRelDef[], ResponseFormat> convertReqEither = Either.left(refs);
        when(componentsUtils.convertJsonToObjectUsingObjectMapper(anyString(), any(User.class),
                ArgumentMatchers.<Class<RequirementCapabilityRelDef[]>>any(),
                nullable(AuditingActionEnum.class), nullable(ComponentTypeEnum.class))).thenReturn(convertReqEither);
        RequirementCapabilityRelDef actionResponseEither = ref;
        when(componentInstanceBusinessLogic
                .dissociateRIFromRI(componentId, USER_ID, ref, ComponentTypeEnum.findByParamName(containerComponentType)))
                .thenReturn(actionResponseEither);

        Response response = target()
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .header("USER_ID", USER_ID)
                .put(Entity.json(refs));

        assertEquals(HttpStatus.OK_200, response.getStatus());
    }

    @Test
    public void testBatchDissociateRIFromRIFailure() {

        String componentId = "componentId";
        String containerComponentType = ComponentTypeEnum.SERVICE_PARAM_NAME;
        String path = "/v1/catalog/" + containerComponentType + "/" + componentId + "/resourceInstance/batchDissociate";

        when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT)).thenReturn(responseFormat);

        Response response = target()
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .header("USER_ID", USER_ID)
                .put(Entity.json(""));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500, response.getStatus());
    }

    @Override
    protected ResourceConfig configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        ApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class);
        return new ResourceConfig(ComponentInstanceServlet.class)
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(request).to(HttpServletRequest.class);
                        bind(userBusinessLogic).to(UserBusinessLogic.class);
                        bind(groupBusinessLogic).to(GroupBusinessLogic.class);
                        bind(componentInstanceBusinessLogic).to(ComponentInstanceBusinessLogic.class);
                        bind(componentsUtils).to(ComponentsUtils.class);
                        bind(servletUtils).to(ServletUtils.class);
                        bind(resourceImportManager).to(ResourceImportManager.class);
                        bind(serviceBusinessLogic).to(ServiceBusinessLogic.class);
                    }
                })
                .property("contextConfig", context);
    }

    private static void createMocks() {
        request = Mockito.mock(HttpServletRequest.class);
        session = Mockito.mock(HttpSession.class);
        servletContext = Mockito.mock(ServletContext.class);
        webAppContextWrapper = Mockito.mock(WebAppContextWrapper.class);
        webApplicationContext = Mockito.mock(WebApplicationContext.class);
        userBusinessLogic = Mockito.mock(UserBusinessLogic.class);
        groupBusinessLogic = Mockito.mock(GroupBusinessLogic.class);
        resourceImportManager = Mockito.mock(ResourceImportManager.class);
        componentInstanceBusinessLogic = Mockito.mock(ComponentInstanceBusinessLogic.class);
        componentsUtils = Mockito.mock(ComponentsUtils.class);
        servletUtils = Mockito.mock(ServletUtils.class);
        responseFormat = Mockito.mock(ResponseFormat.class);
        serviceBusinessLogic = Mockito.mock(ServiceBusinessLogic.class);
    }

    private static void stubMethods() {
        when(request.getSession()).thenReturn(session);
        when(session.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webApplicationContext);
        when(webApplicationContext.getBean(ComponentInstanceBusinessLogic.class)).thenReturn(componentInstanceBusinessLogic);
        when(request.getHeader("USER_ID")).thenReturn(USER_ID);
        when(webApplicationContext.getBean(ServletUtils.class)).thenReturn(servletUtils);
        when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);
    }

    @Test
    public void testUpdateResourceInstancePropertiesSuccess(){

        String containerComponentType = "services";
        String componentId = "componentId";
        String resourceInstanceId = "resourceInstanceId";
        ComponentInstanceProperty [] properties = new ComponentInstanceProperty[1];
        ComponentInstanceProperty property = new ComponentInstanceProperty();
        property.setName("property");
        property.setValue("value");
        property.setType("String");
        properties[0] = (property);
        ObjectMapper mapper = new ObjectMapper();
        String propertyJson = null;
        try {
            propertyJson = mapper.writeValueAsString(properties);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        String path = "/v1/catalog/" + containerComponentType + "/" + componentId + "/resourceInstance" + "/" +
                resourceInstanceId + "/properties";
        when(componentsUtils.convertJsonToObjectUsingObjectMapper(propertyJson, new User(), ComponentInstanceProperty[].class,
                null, ComponentTypeEnum.RESOURCE_INSTANCE)).thenReturn(Either.left(properties));
        when(componentInstanceBusinessLogic.createOrUpdatePropertiesValues(eq(ComponentTypeEnum.findByParamName(SERVICE_PARAM_NAME)),
                eq(componentId), eq(resourceInstanceId), eq(Arrays.asList(properties)), eq(USER_ID))).thenReturn(Either.left(Arrays.asList(properties)));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
        Response response = target()
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .header("USER_ID", USER_ID).post(Entity.entity(properties, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    public void testUpdateResourceInstanceInputsSuccess(){

        String containerComponentType = "services";
        String componentId = "componentId";
        String resourceInstanceId = "resourceInstanceId";
        ComponentInstanceInput[] inputs = new ComponentInstanceInput[1];
        ComponentInstanceInput input = new ComponentInstanceInput();
        input.setName("input");
        input.setValue("value");
        input.setType("String");
        inputs[0] = (input);
        ObjectMapper mapper = new ObjectMapper();
        String inputJson = null;
        try {
            inputJson = mapper.writeValueAsString(inputs);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        String path = "/v1/catalog/" + containerComponentType + "/" + componentId + "/resourceInstance" + "/" +
                resourceInstanceId + "/inputs";
        when(componentsUtils.convertJsonToObjectUsingObjectMapper(inputJson, new User(), ComponentInstanceInput[].class,
                null, ComponentTypeEnum.RESOURCE_INSTANCE)).thenReturn(Either.left(inputs));
        when(componentInstanceBusinessLogic.createOrUpdateInstanceInputValues(eq(ComponentTypeEnum.findByParamName(SERVICE_PARAM_NAME)),
                eq(componentId), eq(resourceInstanceId), eq(Arrays.asList(inputs)), eq(USER_ID))).thenReturn(Either.left(Arrays.asList(inputs)));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
        Response response = target()
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .header("USER_ID", USER_ID).post(Entity.entity(inputs, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    public void testUpdateResourceInstancePropertiesFailure(){

        String containerComponentType = "services";
        String componentId = "componentId";
        String resourceInstanceId = "resourceInstanceId";
        ComponentInstanceProperty [] properties = new ComponentInstanceProperty[1];
        ComponentInstanceProperty property = new ComponentInstanceProperty();
        property.setName("property");
        property.setValue("value");
        property.setType("String");
        properties[0] = (property);
        ObjectMapper mapper = new ObjectMapper();
        String propertyJson = null;
        try {
            propertyJson = mapper.writeValueAsString(properties);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        String path = "/v1/catalog/" + containerComponentType + "/" + componentId + "/resourceInstance" + "/" +
                resourceInstanceId + "/properties";
        when(componentsUtils.convertJsonToObjectUsingObjectMapper(propertyJson, new User(), ComponentInstanceProperty[].class,
                null, ComponentTypeEnum.RESOURCE_INSTANCE)).thenReturn(Either.left(properties));
        when(componentInstanceBusinessLogic.createOrUpdatePropertiesValues(eq(ComponentTypeEnum.findByParamName(SERVICE_PARAM_NAME)),
                eq(componentId), eq(resourceInstanceId), eq(Arrays.asList(properties)), eq(USER_ID)))
                .thenReturn(Either.right(new ResponseFormat(404)));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.NOT_FOUND_404);
        when(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND)).thenReturn(responseFormat);
        Response response = target()
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .header("USER_ID", USER_ID).post(Entity.entity(properties, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND_404);
    }

    @Test
    public void testUpdateResourceInstanceInputsFailure(){

        String containerComponentType = "services";
        String componentId = "componentId";
        String resourceInstanceId = "resourceInstanceId";
        ComponentInstanceInput [] inputs = new ComponentInstanceInput[1];
        ComponentInstanceInput input = new ComponentInstanceInput();
        input.setName("input");
        input.setValue("value");
        input.setType("String");
        inputs[0] = (input);
        ObjectMapper mapper = new ObjectMapper();
        String inputJson = null;
        try {
            inputJson = mapper.writeValueAsString(inputs);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        String path = "/v1/catalog/" + containerComponentType + "/" + componentId + "/resourceInstance" + "/" +
                resourceInstanceId + "/inputs";
        when(componentsUtils.convertJsonToObjectUsingObjectMapper(inputJson, new User(), ComponentInstanceInput[].class,
                null, ComponentTypeEnum.RESOURCE_INSTANCE)).thenReturn(Either.left(inputs));
        when(componentInstanceBusinessLogic.createOrUpdateInstanceInputValues(eq(ComponentTypeEnum.findByParamName(SERVICE_PARAM_NAME)),
                eq(componentId), eq(resourceInstanceId), eq(Arrays.asList(inputs)), eq(USER_ID)))
                .thenReturn(Either.right(new ResponseFormat(404)));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.NOT_FOUND_404);
        when(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND)).thenReturn(responseFormat);
        Response response = target()
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .header("USER_ID", USER_ID).post(Entity.entity(inputs, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND_404);
    }
}
