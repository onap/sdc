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

import fj.data.Either;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import javax.ws.rs.client.Entity;
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
import org.openecomp.sdc.be.config.SpringConfig;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.model.User;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;

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
                .header("USER_ID", USER_ID)
                .get( Response.class);

        assertEquals(response.getStatus(), HttpStatus.OK_200);
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
    public void testCopyComponentInstanceSuccess(){

        String componentId = "componentId";
        String componentInstanceId = "componentInstanceId";
        String path = "/v1/catalog/services/" + componentId + "/copyComponentInstance/" + componentInstanceId;

        Either<Map<String, ComponentInstance>, ResponseFormat> successResponse = Either.left(new HashMap<String, ComponentInstance>());
        when(componentInstanceBusinessLogic.copyComponentInstance(any(ComponentInstance.class), eq(componentId), eq(componentInstanceId), eq(USER_ID))).thenReturn(successResponse);
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);

        ComponentInstance c = new ComponentInstance();
        c.setName("comp1");
        c.setUniqueId("comp1");
        c.setComponentUid("comp1");
        c.setPosX("10");
        c.setPosY("10");
        c.setCapabilities(new HashMap<String, List<CapabilityDefinition>>());
        c.setRequirements(new HashMap<String, List<RequirementDefinition>>());

        Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID)
            .post(Entity.json(c));

        assertEquals(response.getStatus(), HttpStatus.OK_200);
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
        Either<RequirementCapabilityRelDef, ResponseFormat> actionResponseEither = Either.left(ref);
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
        UserBusinessLogic userBusinessLogic = Mockito.mock(UserBusinessLogic.class);
        GroupBusinessLogic groupBusinessLogic = Mockito.mock(GroupBusinessLogic.class);
        ResourceImportManager resourceImportManager = Mockito.mock(ResourceImportManager.class);

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
        componentInstanceBusinessLogic = Mockito.mock(ComponentInstanceBusinessLogic.class);
        componentsUtils = Mockito.mock(ComponentsUtils.class);
        servletUtils = Mockito.mock(ServletUtils.class);
        responseFormat = Mockito.mock(ResponseFormat.class);
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
}
