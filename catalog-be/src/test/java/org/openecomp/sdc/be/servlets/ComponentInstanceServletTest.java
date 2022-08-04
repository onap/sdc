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

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum.SERVICE_PARAM_NAME;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import fj.data.Either;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.ReadListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
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
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogicProvider;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentNodeFilterBusinessLogic;
import org.openecomp.sdc.be.components.impl.GroupBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.components.impl.exceptions.BusinessLogicException;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.SpringConfig;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.info.CreateAndAssotiateInfo;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
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


/**
 * The test suite designed for test functionality of ComponentInstanceServlet class
 */
@TestInstance(Lifecycle.PER_CLASS)
class ComponentInstanceServletTest extends JerseyTest {

    private static final String USER_ID = "jh0003";
    private static final String componentId = "componentId";
    private static final String componentInstanceId = "componentInstanceIdInstanceId";
    public static final String INVALID_CONTENT = "InvalidContent";
    private HttpServletRequest request;
    private HttpSession session;
    private ServletContext servletContext;
    private WebAppContextWrapper webAppContextWrapper;
    private WebApplicationContext webApplicationContext;
    private ComponentInstanceBusinessLogic componentInstanceBusinessLogic;
    private ComponentsUtils componentsUtils;
    private ServletUtils servletUtils;
    private ResponseFormat responseFormat;
    private UserBusinessLogic userBusinessLogic;
    private UserValidations userValidations;
    private GroupBusinessLogic groupBusinessLogic;
    private ResourceImportManager resourceImportManager;
    private ServiceBusinessLogic serviceBusinessLogic;
    private ComponentNodeFilterBusinessLogic componentNodeFilterBusinessLogic;
    private ComponentBusinessLogicProvider componentBusinessLogicProvider;
    private ConfigurationManager configurationManager;
    private User user;
    private String inputData;
    private ComponentInstance componentInstance;
    private CINodeFilterDataDefinition ciNodeFilterDataDefinition;
    private RequirementCapabilityRelDef requirementCapabilityRelDef;

    @BeforeAll
    public void setup() {
        createMocks();
        stubMethods();
        initTestData();
        final String appConfigDir = "src/test/resources/config/catalog-be";
        final ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(),
            appConfigDir);
        configurationManager = new ConfigurationManager(configurationSource);
        org.openecomp.sdc.be.config.Configuration configuration = new org.openecomp.sdc.be.config.Configuration();
        configuration.setJanusGraphInMemoryGraph(true);
        configurationManager.setConfiguration(configuration);
        ExternalConfiguration.setAppName("catalog-be");
    }

    @BeforeEach
    public void before() throws Exception {
        super.setUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    private void initTestData() {
        componentInstance = getComponentInstance();
        inputData = getInputData(componentInstance);
        ciNodeFilterDataDefinition = getCiNodeFilterDataDefinition();

        user = new User();
        user.setUserId(USER_ID);
        user.setRole(Role.ADMIN.name());

        requirementCapabilityRelDef = new RequirementCapabilityRelDef();
        final CapabilityRequirementRelationship capabilityRequirementRelationship = new CapabilityRequirementRelationship();
        final RelationshipInfo relationInfo = new RelationshipInfo();
        relationInfo.setId("RELATION_ID");
        capabilityRequirementRelationship.setRelation(relationInfo);
        requirementCapabilityRelDef.setRelationships(Lists.newArrayList(capabilityRequirementRelationship));
        requirementCapabilityRelDef.setToNode("TO_INSTANCE_ID");
        requirementCapabilityRelDef.setFromNode("FROM_INSTANCE_ID");
    }

    @Test
    void testGetRelationByIdSuccess() {

        String containerComponentType = "resources";
        String componentId = "componentId";
        String relationId = "relationId";
        String path = "/v1/catalog/" + containerComponentType + "/" + componentId + "/" + relationId + "/relationId";
        Either<RequirementCapabilityRelDef, ResponseFormat> successResponse = Either
            .left(new RequirementCapabilityRelDef());
        when(componentInstanceBusinessLogic
            .getRelationById(eq(componentId), eq(relationId), eq(USER_ID), eq(ComponentTypeEnum.RESOURCE)))
            .thenReturn(successResponse);
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
        Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID)
            .get(Response.class);
        
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    void testGetRelationByIdFailure() {

        String containerComponentType = "unknown_type";
        String relationId = "relationId";
        String path = "/v1/catalog/" + containerComponentType + "/" + componentId + "/" + relationId + "/relationId";
        when(responseFormat.getStatus()).thenReturn(HttpStatus.BAD_REQUEST_400);
        when(componentsUtils.getResponseFormat(eq(ActionStatus.UNSUPPORTED_ERROR), eq(containerComponentType)))
            .thenReturn(responseFormat);
        Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID)
            .get(Response.class);

        assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    }

    @Test
    void testBatchDeleteResourceInstancesSuccess() {

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
    void testBatchDeleteResourceInstancesFailure() {

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
    void testBatchDissociateRIFromRISuccess() {

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
    void testBatchDissociateRIFromRIFailure() {

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
                    bind(componentNodeFilterBusinessLogic).to(ComponentNodeFilterBusinessLogic.class);
                    bind(componentBusinessLogicProvider).to(ComponentBusinessLogicProvider.class);
                }
            })
            .property("contextConfig", context);
    }

    private void createMocks() {
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
        componentNodeFilterBusinessLogic = Mockito.mock(ComponentNodeFilterBusinessLogic.class);
        componentBusinessLogicProvider = Mockito.mock(ComponentBusinessLogicProvider.class);
        userValidations = Mockito.mock(UserValidations.class);
    }

    private void stubMethods() {
        when(request.getSession()).thenReturn(session);
        when(session.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR))
            .thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webApplicationContext);
        when(webApplicationContext.getBean(ComponentInstanceBusinessLogic.class))
            .thenReturn(componentInstanceBusinessLogic);
        when(request.getHeader("USER_ID")).thenReturn(USER_ID);
        when(webApplicationContext.getBean(ServletUtils.class)).thenReturn(servletUtils);
        when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);
    }

    @Test
    void testUpdateResourceInstancePropertiesSuccess() {

        String containerComponentType = "services";
        String resourceInstanceId = "resourceInstanceId";
        ComponentInstanceProperty[] properties = new ComponentInstanceProperty[1];
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
        when(componentsUtils
            .convertJsonToObjectUsingObjectMapper(propertyJson, new User(), ComponentInstanceProperty[].class,
                null, ComponentTypeEnum.RESOURCE_INSTANCE)).thenReturn(Either.left(properties));
        when(componentInstanceBusinessLogic
            .createOrUpdatePropertiesValues(eq(ComponentTypeEnum.findByParamName(SERVICE_PARAM_NAME)),
                eq(componentId), eq(resourceInstanceId), eq(Arrays.asList(properties)), eq(USER_ID)))
            .thenReturn(Either.left(Arrays.asList(properties)));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
        Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID).post(Entity.entity(properties, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    void testUpdateResourceInstanceInputsSuccess() {

        String containerComponentType = "services";
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
        when(componentInstanceBusinessLogic
            .createOrUpdateInstanceInputValues(eq(ComponentTypeEnum.findByParamName(SERVICE_PARAM_NAME)),
                eq(componentId), eq(resourceInstanceId), eq(Arrays.asList(inputs)), eq(USER_ID)))
            .thenReturn(Either.left(Arrays.asList(inputs)));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
        Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID).post(Entity.entity(inputs, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    void testUpdateResourceInstancePropertiesFailure() {

        String containerComponentType = "services";
        String resourceInstanceId = "resourceInstanceId";
        ComponentInstanceProperty[] properties = new ComponentInstanceProperty[1];
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
        when(componentsUtils
            .convertJsonToObjectUsingObjectMapper(propertyJson, new User(), ComponentInstanceProperty[].class,
                null, ComponentTypeEnum.RESOURCE_INSTANCE)).thenReturn(Either.left(properties));
        when(componentInstanceBusinessLogic
            .createOrUpdatePropertiesValues(eq(ComponentTypeEnum.findByParamName(SERVICE_PARAM_NAME)),
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
    void testUpdateResourceInstanceInputsFailure() {

        String containerComponentType = "services";
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
        when(componentInstanceBusinessLogic
            .createOrUpdateInstanceInputValues(eq(ComponentTypeEnum.findByParamName(SERVICE_PARAM_NAME)),
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

    @Test
    void testUpdateInstanceRequirement() {
        String containerComponentType = "services";
        String capabilityType = "capabilityType";
        String requirementName = "requirementName";
        RequirementDefinition requirementDefinition = new RequirementDefinition();
        ObjectMapper mapper = new ObjectMapper();
        String requirementJson = null;
        try {
            requirementJson = mapper.writeValueAsString(requirementDefinition);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        String path = "/v1/catalog/" + containerComponentType + "/" + componentId + "/componentInstances/" +
            componentInstanceId + "/requirement/" + capabilityType + "/requirementName/" + requirementName;
        when(componentsUtils
            .convertJsonToObjectUsingObjectMapper(eq(requirementJson), any(User.class), eq(RequirementDefinition.class),
                eq(AuditingActionEnum.GET_TOSCA_MODEL), eq(ComponentTypeEnum.SERVICE)))
            .thenReturn(Either.left(requirementDefinition));
        when(componentInstanceBusinessLogic.updateInstanceRequirement(ComponentTypeEnum.SERVICE,
            componentId, componentInstanceId, requirementDefinition, USER_ID))
            .thenReturn(Either.left(requirementDefinition));
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);

        Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID).put(Entity.entity(requirementDefinition, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    void createComponentInstanceSuccessTest() {
        final String pathFormat = "/v1/catalog/%s/%s/resourceInstance";
        final String path = String.format(pathFormat, SERVICE_PARAM_NAME, componentId);
        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(userBusinessLogic.getUser(USER_ID)).thenReturn(user);
        when(componentInstanceBusinessLogic.
            createComponentInstance(anyString(), anyString(), anyString(), any(ComponentInstance.class)))
            .thenReturn(componentInstance);
        when(responseFormat.getStatus()).thenReturn(HttpStatus.CREATED_201);
        when(componentsUtils.getResponseFormat(ActionStatus.CREATED)).thenReturn(responseFormat);
        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID).post(Entity.entity(inputData, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED_201);
    }

    @Test
    void createComponentInstanceFailWithEmptyContentTest() {
        final String pathFormat = "/v1/catalog/%s/%s/resourceInstance";
        final String path = String.format(pathFormat, SERVICE_PARAM_NAME, componentId);
        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(userBusinessLogic.getUser(USER_ID)).thenReturn(user);
        when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(componentsUtils.getResponseFormat(ActionStatus.MISSING_BODY)).thenReturn(responseFormat);
        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID).post(Entity.entity("", MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    void createComponentInstanceFailWithInvalidContentTest() {
        final String pathFormat = "/v1/catalog/%s/%s/resourceInstance";
        final String path = String.format(pathFormat, SERVICE_PARAM_NAME, componentId);
        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(userBusinessLogic.getUser(USER_ID)).thenReturn(user);
        when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT)).thenReturn(responseFormat);
        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID).post(Entity.entity(INVALID_CONTENT, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    void updateComponentInstanceMetadataSuccessTest() throws IOException {
        final String pathFormat = "/v1/catalog/%s/%s/resourceInstance/%s";
        final String path = String.format(pathFormat, SERVICE_PARAM_NAME, componentId, componentInstanceId);
        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(userBusinessLogic.getUser(USER_ID)).thenReturn(user);
        when(request.getInputStream()).thenReturn(new TestServletInputStream(inputData));
        when(componentsUtils
            .convertJsonToObjectUsingObjectMapper(inputData, new User(), ComponentInstance.class,
                null, ComponentTypeEnum.RESOURCE_INSTANCE)).thenReturn(Either.left(componentInstance));
        when(componentInstanceBusinessLogic
            .updateComponentInstanceMetadata(anyString(), anyString(), anyString(), anyString(),
                any(ComponentInstance.class))).thenReturn(Either.left(componentInstance));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID).post(Entity.entity(inputData, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    void updateComponentInstanceMetadataFailWithInvalidContentTest() throws IOException {
        final String pathFormat = "/v1/catalog/%s/%s/resourceInstance/%s";
        final String path = String.format(pathFormat, SERVICE_PARAM_NAME, componentId, componentInstanceId);
        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(userBusinessLogic.getUser(USER_ID)).thenReturn(user);
        when(request.getInputStream()).thenReturn(new TestServletInputStream(""));
        when(componentsUtils
            .convertJsonToObjectUsingObjectMapper(INVALID_CONTENT, new User(), ComponentInstance.class,
                null, ComponentTypeEnum.RESOURCE_INSTANCE)).thenReturn(Either.left(componentInstance));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.NO_CONTENT_204);
        when(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT)).thenReturn(responseFormat);
        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID).post(Entity.entity(INVALID_CONTENT, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
    }

    @Test
    void updateComponentInstanceMetadataFailConvertJsonDataTest() throws IOException {
        final String pathFormat = "/v1/catalog/%s/%s/resourceInstance/%s";
        final String path = String.format(pathFormat, SERVICE_PARAM_NAME, componentId, componentInstanceId);
        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(userBusinessLogic.getUser(USER_ID)).thenReturn(user);
        when(request.getInputStream()).thenReturn(new TestServletInputStream(inputData));
        when(componentsUtils
            .convertJsonToObjectUsingObjectMapper(inputData, new User(), ComponentInstance.class,
                null, ComponentTypeEnum.RESOURCE_INSTANCE)).thenReturn(Either.right(new ResponseFormat()));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(responseFormat);
        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID).post(Entity.entity(inputData, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    void updateComponentInstanceMetadataAndCreateNodeFilterSuccessTest() throws IOException, BusinessLogicException {
        final String pathFormat = "/v1/catalog/%s/%s/resourceInstance/%s";
        final String path = String.format(pathFormat, SERVICE_PARAM_NAME, componentId, componentInstanceId);
        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(userBusinessLogic.getUser(USER_ID)).thenReturn(user);
        componentInstance.setDirectives(singletonList("substitutable"));
        when(request.getInputStream()).thenReturn(new TestServletInputStream(inputData));
        when(componentsUtils
            .convertJsonToObjectUsingObjectMapper(inputData, new User(), ComponentInstance.class,
                null, ComponentTypeEnum.RESOURCE_INSTANCE)).thenReturn(Either.left(componentInstance));
        when(componentInstanceBusinessLogic
            .updateComponentInstanceMetadata(anyString(), anyString(), anyString(), anyString(),
                any(ComponentInstance.class))).thenReturn(Either.left(componentInstance));

        when(componentNodeFilterBusinessLogic.createNodeFilterIfNotExist(componentId, componentInstanceId,
            true, ComponentTypeEnum.SERVICE)).thenReturn(Optional.of(ciNodeFilterDataDefinition));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID).post(Entity.entity(inputData, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    void updateComponentInstanceMetadataAndCreateNodeFilterFailTest() throws IOException, BusinessLogicException {
        final String pathFormat = "/v1/catalog/%s/%s/resourceInstance/%s";
        final String path = String.format(pathFormat, SERVICE_PARAM_NAME, componentId, componentInstanceId);
        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(userBusinessLogic.getUser(USER_ID)).thenReturn(user);
        componentInstance.setDirectives(singletonList("substitutable"));
        when(request.getInputStream()).thenReturn(new TestServletInputStream(inputData));
        when(componentsUtils
            .convertJsonToObjectUsingObjectMapper(inputData, new User(), ComponentInstance.class,
                null, ComponentTypeEnum.RESOURCE_INSTANCE)).thenReturn(Either.left(componentInstance));
        when(componentInstanceBusinessLogic
            .updateComponentInstanceMetadata(anyString(), anyString(), anyString(), anyString(),
                any(ComponentInstance.class))).thenReturn(Either.left(componentInstance));
        when(componentNodeFilterBusinessLogic.createNodeFilterIfNotExist(componentId, componentInstanceId,
            true, ComponentTypeEnum.SERVICE)).thenReturn(Optional.empty());
        when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(responseFormat);
        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID).post(Entity.entity(inputData, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    void updateComponentInstanceMetadataAndDeleteNodeFilterSuccessTest() throws IOException, BusinessLogicException {
        final String pathFormat = "/v1/catalog/%s/%s/resourceInstance/%s";
        final String path = String.format(pathFormat, SERVICE_PARAM_NAME, componentId, componentInstanceId);
        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(userBusinessLogic.getUser(USER_ID)).thenReturn(user);
        when(request.getInputStream()).thenReturn(new TestServletInputStream(inputData));
        when(componentsUtils
            .convertJsonToObjectUsingObjectMapper(inputData, new User(), ComponentInstance.class,
                null, ComponentTypeEnum.RESOURCE_INSTANCE)).thenReturn(Either.left(componentInstance));
        when(componentInstanceBusinessLogic
            .updateComponentInstanceMetadata(anyString(), anyString(), anyString(), anyString(),
                any(ComponentInstance.class))).thenReturn(Either.left(componentInstance));
        when(componentNodeFilterBusinessLogic
            .deleteNodeFilterIfExists(componentId, componentInstanceId, true, ComponentTypeEnum.SERVICE))
            .thenReturn(Optional.of(ciNodeFilterDataDefinition.getName()));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID).post(Entity.entity(inputData, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    void updateComponentInstanceMetadataAndDeleteNodeFilterFailTest() throws IOException, BusinessLogicException {
        final String pathFormat = "/v1/catalog/%s/%s/resourceInstance/%s";
        final String path = String.format(pathFormat, SERVICE_PARAM_NAME, componentId, componentInstanceId);
        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(userBusinessLogic.getUser(USER_ID)).thenReturn(user);
        when(request.getInputStream()).thenReturn(new TestServletInputStream(inputData));
        when(componentsUtils
            .convertJsonToObjectUsingObjectMapper(inputData, new User(), ComponentInstance.class,
                null, ComponentTypeEnum.RESOURCE_INSTANCE)).thenReturn(Either.left(componentInstance));
        when(componentInstanceBusinessLogic
            .updateComponentInstanceMetadata(anyString(), anyString(), anyString(), anyString(),
                any(ComponentInstance.class))).thenReturn(Either.left(componentInstance));
        when(componentNodeFilterBusinessLogic
            .deleteNodeFilterIfExists(componentId, componentInstanceId, true, ComponentTypeEnum.SERVICE))
            .thenReturn(Optional.empty());
        when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(responseFormat);
        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID).post(Entity.entity(inputData, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    void updateMultipleComponentInstanceSuccessTest() throws IOException {
        final String pathFormat = "/v1/catalog/%s/%s/resourceInstance/multipleComponentInstance";
        final String path = String.format(pathFormat, SERVICE_PARAM_NAME, componentId);
        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(userBusinessLogic.getUser(USER_ID)).thenReturn(user);
        final ComponentInstance[] componentInstances = new ComponentInstance[1];
        componentInstances[0] = componentInstance;
        when(request.getInputStream()).thenReturn(new TestServletInputStream(inputData));
        when(componentsUtils
            .convertJsonToObjectUsingObjectMapper(inputData, new User(), ComponentInstance[].class,
                null, ComponentTypeEnum.RESOURCE_INSTANCE)).thenReturn(Either.left(componentInstances));
        when(componentInstanceBusinessLogic
            .updateComponentInstance(anyString(), any(Component.class), anyString(), anyString(), anyList(),
                anyBoolean())).thenReturn(
            singletonList(componentInstance));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID).post(Entity.entity(inputData, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    void updateMultipleComponentInstanceFailTest() throws IOException {
        final String pathFormat = "/v1/catalog/%s/%s/resourceInstance/multipleComponentInstance";
        final String path = String.format(pathFormat, SERVICE_PARAM_NAME, componentId);
        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(userBusinessLogic.getUser(USER_ID)).thenReturn(user);
        when(request.getInputStream()).thenReturn(new TestServletInputStream(inputData));
        when(componentsUtils
            .convertJsonToObjectUsingObjectMapper(inputData, new User(), ComponentInstance[].class,
                null, ComponentTypeEnum.RESOURCE_INSTANCE)).thenReturn(Either.right(new ResponseFormat()));
        when(componentInstanceBusinessLogic
            .updateComponentInstance(anyString(), any(Component.class), anyString(), anyString(), anyList(),
                anyBoolean())).thenReturn(
            singletonList(componentInstance));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(responseFormat);
        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID).post(Entity.entity(inputData, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    void deleteResourceInstanceSuccessTest() throws BusinessLogicException {
        final String pathFormat = "/v1/catalog/%s/%s/resourceInstance/%s";
        final String path = String.format(pathFormat, SERVICE_PARAM_NAME, componentId, componentInstanceId);
        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(userBusinessLogic.getUser(USER_ID)).thenReturn(user);
        when(componentInstanceBusinessLogic.deleteComponentInstance(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(componentInstance);
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID).delete();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    void associateRIToRISuccessTest() {
        final String pathFormat = "/v1/catalog/%s/%s/resourceInstance/associate";
        final String path = String.format(pathFormat, SERVICE_PARAM_NAME, componentId);
        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(userBusinessLogic.getUser(USER_ID)).thenReturn(user);
        when(componentInstanceBusinessLogic
            .associateRIToRI(anyString(), anyString(), any(RequirementCapabilityRelDef.class),
                any(ComponentTypeEnum.class))).thenReturn(requirementCapabilityRelDef);
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID).post(Entity.entity(getInputData(requirementCapabilityRelDef), MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    void associateRIToRIFailWithInvalidContentTest() {
        final String pathFormat = "/v1/catalog/%s/%s/resourceInstance/associate";
        final String path = String.format(pathFormat, SERVICE_PARAM_NAME, componentId);
        when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(responseFormat);

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID).post(Entity.entity(inputData, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    void dissociateRIFromRISuccessTest() {
        final String pathFormat = "/v1/catalog/%s/%s/resourceInstance/dissociate";
        final String path = String.format(pathFormat, SERVICE_PARAM_NAME, componentId);
        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(userBusinessLogic.getUser(USER_ID)).thenReturn(user);
        when(componentInstanceBusinessLogic
            .dissociateRIFromRI(anyString(), anyString(), any(RequirementCapabilityRelDef.class),
                any(ComponentTypeEnum.class))).thenReturn(requirementCapabilityRelDef);
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID).put(Entity.entity(getInputData(requirementCapabilityRelDef), MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    void dissociateRIFromRIFailWithInvalidContentTest() {
        final String pathFormat = "/v1/catalog/%s/%s/resourceInstance/dissociate";
        final String path = String.format(pathFormat, SERVICE_PARAM_NAME, componentId);
        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(userBusinessLogic.getUser(USER_ID)).thenReturn(user);
        when(componentInstanceBusinessLogic
            .dissociateRIFromRI(anyString(), anyString(), any(RequirementCapabilityRelDef.class),
                any(ComponentTypeEnum.class))).thenReturn(requirementCapabilityRelDef);
        when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(responseFormat);

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID).put(Entity.entity(inputData, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    void createAndAssociateRIToRISuccessTest() throws IOException {
        final String pathFormat = "/v1/catalog/%s/%s/resourceInstance/createAndAssociate";
        final String path = String.format(pathFormat, SERVICE_PARAM_NAME, componentId);
        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(userBusinessLogic.getUser(USER_ID)).thenReturn(user);
        final CreateAndAssotiateInfo createAndAssociateInfo = new CreateAndAssotiateInfo(componentInstance, requirementCapabilityRelDef);
        final String data = getInputData(createAndAssociateInfo);
        when(request.getInputStream()).thenReturn(new TestServletInputStream(data));
        when(componentInstanceBusinessLogic.createAndAssociateRIToRI(anyString(), anyString(), anyString(), any(
            CreateAndAssotiateInfo.class))).thenReturn(Either.left(createAndAssociateInfo));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.CREATED_201);
        when(componentsUtils.getResponseFormat(ActionStatus.CREATED)).thenReturn(responseFormat);

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID).post(Entity.entity(data, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED_201);
    }

    @Test
    void createAndAssociateRIToRIFailTest() throws IOException {
        final String pathFormat = "/v1/catalog/%s/%s/resourceInstance/createAndAssociate";
        final String path = String.format(pathFormat, SERVICE_PARAM_NAME, componentId);
        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(userBusinessLogic.getUser(USER_ID)).thenReturn(user);
        final String data = getInputData(new CreateAndAssotiateInfo(componentInstance, requirementCapabilityRelDef));
        when(request.getInputStream()).thenReturn(new TestServletInputStream(data));
        when(componentInstanceBusinessLogic.createAndAssociateRIToRI(anyString(), anyString(), anyString(), any(
            CreateAndAssotiateInfo.class))).thenReturn(Either.right(new ResponseFormat()));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(responseFormat);

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID).post(Entity.entity(data, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    void createAndAssociateRIToRIFailWithInvalidContentTest() throws IOException {
        final String pathFormat = "/v1/catalog/%s/%s/resourceInstance/createAndAssociate";
        final String path = String.format(pathFormat, SERVICE_PARAM_NAME, componentId);
        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(userBusinessLogic.getUser(USER_ID)).thenReturn(user);
        when(request.getInputStream()).thenReturn(new TestServletInputStream(INVALID_CONTENT));
        when(componentInstanceBusinessLogic.createAndAssociateRIToRI(anyString(), anyString(), anyString(), any(
            CreateAndAssotiateInfo.class))).thenReturn(Either.right(new ResponseFormat()));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(responseFormat);

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID).post(Entity.entity(INVALID_CONTENT, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    void createAndAssociateRIToRIFailWithEmptyContentTest() throws IOException {
        final String pathFormat = "/v1/catalog/%s/%s/resourceInstance/createAndAssociate";
        final String path = String.format(pathFormat, SERVICE_PARAM_NAME, componentId);
        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(userBusinessLogic.getUser(USER_ID)).thenReturn(user);
        final String data = StringUtils.EMPTY;
        when(request.getInputStream()).thenReturn(new TestServletInputStream(data));
        when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT)).thenReturn(responseFormat);

        final Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID).post(Entity.entity(data, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    private CINodeFilterDataDefinition getCiNodeFilterDataDefinition() {
        final CINodeFilterDataDefinition ciNodeFilterDataDefinition = new CINodeFilterDataDefinition();
        ciNodeFilterDataDefinition.setProperties(new ListDataDefinition<>());
        ciNodeFilterDataDefinition.setID("MyNodeFilter");
        ciNodeFilterDataDefinition.setName("NODE_FILTER_UID");
        return ciNodeFilterDataDefinition;
    }

    private ComponentInstance getComponentInstance() {
        final ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setName("InstanceName");
        componentInstance.setUniqueId(componentInstanceId);
        componentInstance.setComponentUid("ComponentInstanceUUID");
        componentInstance.setInvariantName("ComponentInstanceInvariantName");
        return componentInstance;
    }

    private <T> String getInputData(final T elementToRepresent) {
        try {
            return new ObjectMapper().writeValueAsString(elementToRepresent);
        } catch (final JsonProcessingException e) {
            e.printStackTrace();
        }
        return StringUtils.EMPTY;
    }

    class TestServletInputStream extends ServletInputStream {

        private InputStream inputStream;

        TestServletInputStream(String testJson) {
            inputStream = new ByteArrayInputStream(testJson.getBytes());
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }
    }
}
