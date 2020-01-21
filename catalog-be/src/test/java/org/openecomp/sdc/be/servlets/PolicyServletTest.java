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

import com.fasterxml.jackson.databind.DeserializationFeature;
import fj.data.Either;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.BaseBusinessLogic;
import org.openecomp.sdc.be.components.impl.PolicyBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.components.impl.aaf.RoleAuthorizationHandler;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.property.PropertyDeclarationOrchestrator;
import org.openecomp.sdc.be.components.utils.PropertyDataDefinitionBuilder;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.GetPolicyValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.PolicyTargetDTO;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.api.FilterDecisionEnum;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.openecomp.sdc.exception.ResponseFormat;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.common.api.Constants.GET_POLICY;



@RunWith(MockitoJUnitRunner.class)
public class PolicyServletTest extends JerseySpringBaseTest{

    private final static String USER_ID = "jh0003";
    private static final String COMPONENT_ID = "componentId";
    private static PolicyBusinessLogic businessLogic;
    private static ComponentsUtils componentsUtils;
    private static ServletUtils servletUtils;
    private static PropertyDeclarationOrchestrator propertyDeclarationOrchestrator;
    private static ToscaOperationFacade toscaOperationFacade;
    private static RoleAuthorizationHandler roleAuthorizationHandler;
    private static ResponseFormat responseFormat;
    @Captor
    private static ArgumentCaptor<PolicyDefinition> policyCaptor;
    @Spy
    private static BaseBusinessLogic baseBusinessLogic;

    private static String validComponentType = "resources";
    private static String unsupportedComponentType = "unsupported";
    private static String componentId = "componentId";
    private static String policyTypeName = "policyTypeName";

    private static final String PROPS_URL = "/v1/catalog/{componentType}/{serviceId}/policies/{policyId}/properties";
    private static final String DECLARE_URL = "v1/catalog/{componentType}/{serviceId}/create/policies";
    private static final String DELETE_URL = "v1/catalog/{containerComponentType}/{componentId}/policies/{policyId}";
    private static final String SERVICE_ID = "serviceId";
    private static final String POLICY_ID = "policyId";
    private static final String PROP_1 = "prop1";

    private static final String UPDATE_TARGETS_URL = "/v1/catalog/{componentType}/{componentId}/policies/{policyId}/targets";
    static ConfigurationSource configurationSource = new FSConfigurationSource(
            ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be");
    static ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

    @BeforeClass
    public static void initClass() {
        ResponseFormatManager.getInstance();
        createMocks();
        when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);
    }

    @Before
    public void beforeMethod() {
        Mockito.reset(businessLogic);
        final JacksonJsonProvider jacksonJsonProvider = new JacksonJaxbJsonProvider().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        setClient(ClientBuilder.newClient(new ClientConfig(jacksonJsonProvider)));
        ThreadLocalsHolder.setApiType(FilterDecisionEnum.EXTERNAL);
        when(request.isUserInRole(anyString())).thenReturn(true);

    }


    @Test
    public void testGetPolicySuccess(){
        String path = "/v1/catalog/" + validComponentType + "/" + componentId + "/policies/" + POLICY_ID;
        PolicyDefinition successResponse = new PolicyDefinition();
        when(businessLogic.getPolicy(eq(ComponentTypeEnum.RESOURCE), eq(componentId), eq(POLICY_ID), eq(USER_ID))).thenReturn(successResponse);
        Response response = target()
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .header("USER_ID", USER_ID)
                .get(Response.class);

        assertEquals(response.getStatus(), HttpStatus.OK_200.getStatusCode());
    }

    @Test
    public void testGetPolicyFailure(){
        String path = "/v1/catalog/" + unsupportedComponentType + "/" + componentId + "/policies/" + POLICY_ID;
        Response response = target()
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .header("USER_ID", USER_ID)
                .get(Response.class);

        assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST_400.getStatusCode());
    }
    
    @Test
    public void testPostPolicySuccess(){
        String path = "/v1/catalog/" + validComponentType + "/" + componentId + "/policies/" + policyTypeName;
        PolicyDefinition policy = new PolicyDefinition();
        PolicyDefinition successResponse = policy;
        when(businessLogic.createPolicy(eq(ComponentTypeEnum.RESOURCE), eq(componentId), eq(policyTypeName), eq(USER_ID), eq(true))).thenReturn(successResponse);
        when(responseFormat.getStatus()).thenReturn(HttpStatus.CREATED_201.getStatusCode());
        when(componentsUtils.getResponseFormat(ActionStatus.CREATED)).thenReturn(responseFormat);
        Response response = target()
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .header("USER_ID", USER_ID)
                .post(Entity.entity(policy, MediaType.APPLICATION_JSON),Response.class);

        assertEquals(response.getStatus(), HttpStatus.CREATED_201.getStatusCode());
    }
    
    @Test
    public void testPostPolicyFailure(){
        String path = "/v1/catalog/" + unsupportedComponentType + "/" + componentId + "/policies/" + policyTypeName;
        PolicyDefinition policy = new PolicyDefinition();
        Response response = target()
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .header("USER_ID", USER_ID)
                .post(Entity.entity(policy, MediaType.APPLICATION_JSON),Response.class);

        assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST_400.getStatusCode());
    }
    
    @Test
    public void testPutPolicySuccess(){
        String path = "/v1/catalog/" + validComponentType + "/" + componentId + "/policies/" + POLICY_ID;
        PolicyDefinition policy = new PolicyDefinition();
        policy.setUniqueId(POLICY_ID);
        PolicyDefinition successResponse = policy;
        when(businessLogic.updatePolicy(eq(ComponentTypeEnum.RESOURCE), eq(componentId), any(PolicyDefinition.class), eq(USER_ID), eq(true))).thenReturn(successResponse);
        Response response = target()
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .header("USER_ID", USER_ID)
                .put(Entity.entity(policy, MediaType.APPLICATION_JSON),Response.class);

        assertEquals(response.getStatus(), HttpStatus.OK_200.getStatusCode());
    }
    
    @Test
    public void testPutPolicyFailure(){
        String path = "/v1/catalog/" + unsupportedComponentType + "/" + componentId + "/policies/" + POLICY_ID;
        PolicyDefinition policy = new PolicyDefinition();
        Response response = target()
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .header("USER_ID", USER_ID)
                .put(Entity.entity(policy, MediaType.APPLICATION_JSON),Response.class);

        assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST_400.getStatusCode());
    }
    
    @Test
    public void testDeletePolicySuccess(){
        String path = "/v1/catalog/" + validComponentType + "/" + componentId + "/policies/" + POLICY_ID;
        PolicyDefinition successResponse = new PolicyDefinition();
        when(businessLogic.deletePolicy(eq(ComponentTypeEnum.RESOURCE), eq(componentId), eq(POLICY_ID), eq(USER_ID), eq(true))).thenReturn(successResponse);
        Response response = target()
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .header("USER_ID", USER_ID)
                .delete(Response.class);

        assertEquals(response.getStatus(), HttpStatus.OK_200.getStatusCode());
    }

    @Test
    public void testDeletePolicyFailure(){
        String path = "/v1/catalog/" + unsupportedComponentType + "/" + componentId + "/policies/" + POLICY_ID;
        Response response = target()
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .header("USER_ID", USER_ID)
                .delete(Response.class);

        assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST_400.getStatusCode());
    }

    @Test
    public void getPolicyProperties_operationForbidden() {
       // doThrow(new ComponentException(ActionStatus.GENERAL_ERROR)).when(businessLogic).getPolicyProperties(ComponentTypeEnum.SERVICE, SERVICE_ID, POLICY_ID, USER_ID);
        when(businessLogic.getPolicyProperties(ComponentTypeEnum.SERVICE, SERVICE_ID, POLICY_ID, USER_ID))
                .thenThrow(new ByActionStatusComponentException(ActionStatus.AUTH_FAILED, USER_ID));
        Response response = buildGetPropertiesRequest().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test//(expected = ComponentException.class)
    public void getPolicyProperties_unHandledError_returnGeneralError() {
        when(businessLogic.getPolicyProperties(ComponentTypeEnum.SERVICE, SERVICE_ID, POLICY_ID, USER_ID)).thenThrow(new RuntimeException());
        Response response = buildGetPropertiesRequest().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void getPolicyProperties_wrongComponentType() {
        Response response = buildGetPropertiesRequest("unknownType").get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        //verifyZeroInteractions(businessLogic);
    }

    @Test
    public void getPolicyProperties() {
        List<PropertyDataDefinition> properties = getPropertiesList();
        when(businessLogic.getPolicyProperties(ComponentTypeEnum.SERVICE, SERVICE_ID, POLICY_ID, USER_ID)).thenReturn(properties);
        List<PropertyDataDefinition> policyProps = buildGetPropertiesRequest().get(new GenericType<List<PropertyDataDefinition>>() {});
        assertThat(policyProps)
                .usingElementComparatorOnFields("uniqueId")
                .containsExactlyInAnyOrder(properties.get(0), properties.get(1));
    }
    
    @Test
    public void updatePolicyPropertiesSuccess() {
        List<PropertyDataDefinition> properties = getPropertiesList();
        when(businessLogic.updatePolicyProperties(eq(ComponentTypeEnum.SERVICE), eq(SERVICE_ID), eq(POLICY_ID),
                any(PropertyDataDefinition[].class), eq(USER_ID), eq(true))).thenReturn(properties);
        List<PropertyDataDefinition> policyProps = buildUpdatePropertiesRequest(ComponentTypeEnum.SERVICE_PARAM_NAME, properties).invoke(new GenericType<List<PropertyDataDefinition>>() {});
        assertThat(policyProps)
                .usingElementComparatorOnFields("uniqueId")
                .containsExactlyInAnyOrder(properties.get(0), properties.get(1));
    }

    @Test
    public void updatePolicyTargetsSuccess() {
        List<PolicyTargetDTO> targets = getTargetDTOList();
        when(businessLogic.updatePolicyTargets(eq(ComponentTypeEnum.RESOURCE), eq(COMPONENT_ID), eq(POLICY_ID), anyMap(), eq(USER_ID))).thenReturn(new PolicyDefinition());
        Response policyTargets = buildUpdateTargetsRequest(ComponentTypeEnum.RESOURCE_PARAM_NAME, targets).invoke();
        assertThat(policyTargets.getStatus()).isEqualTo(200);
    }

    @Test
    public void updatePolicyPropertiesFailure() {
        List<PropertyDataDefinition> properties = getPropertiesList();
        ResponseFormat notFoundResponse = new ResponseFormat(HttpStatus.NOT_FOUND_404.getStatusCode());
        when(businessLogic.updatePolicyProperties(eq(ComponentTypeEnum.SERVICE), eq(SERVICE_ID), eq(POLICY_ID), any(PropertyDataDefinition[].class), eq(USER_ID), eq(true))).thenThrow(new ByResponseFormatComponentException(notFoundResponse));
        Response policyProps = buildUpdatePropertiesRequest(ComponentTypeEnum.SERVICE_PARAM_NAME, properties).invoke();
        assertEquals(HttpStatus.NOT_FOUND_404.getStatusCode(), policyProps.getStatus());
    }

    @Test
    public void testDeclarePropertyToPolicySuccess() {
        Service service = new Service();
        service.setUniqueId(SERVICE_ID);
        service.addProperty(new PropertyDataDefinitionBuilder().setUniqueId(PROP_1).build());

        PolicyDefinition policyDefinition = new PolicyDefinition();
        policyDefinition.setUniqueId(UniqueIdBuilder.buildPolicyUniqueId(SERVICE_ID, PROP_1));

        setMocksForPropertyDeclaration(policyDefinition);

        when(componentsUtils.getResponseFormat(eq(ActionStatus.OK))).thenReturn(new ResponseFormat(HttpStatus.OK_200.getStatusCode()));

        Response declareResponse = buildDeclarePropertiesRequest(PROP_1).invoke();
        assertEquals(HttpStatus.OK_200.getStatusCode(), declareResponse.getStatus());
    }

    @Test
    public void testUndeclarePolicySuccess() {
        Service service = new Service();
        service.setUniqueId(SERVICE_ID);
        PropertyDefinition origProperty = new PropertyDataDefinitionBuilder().setUniqueId(PROP_1).build();

        service.addProperty(origProperty);

        PolicyDefinition policyDefinition = new PolicyDefinition();
        policyDefinition.setUniqueId(UniqueIdBuilder.buildPolicyUniqueId(SERVICE_ID, PROP_1));
        service.addPolicy(policyDefinition);

        addGetPolicyValueToProperty(origProperty, policyDefinition);

        when(businessLogic.deletePolicy(eq(ComponentTypeEnum.SERVICE), eq(SERVICE_ID), eq(policyDefinition.getUniqueId()), eq(USER_ID), eq(true))).thenReturn(policyDefinition);

        Response deleteResponse = buildDeletePolicyRequest(policyDefinition).invoke();
        assertEquals(HttpStatus.OK_200.getStatusCode(), deleteResponse.getStatus());
    }

    private void addGetPolicyValueToProperty(PropertyDefinition propertyDefinition,
                                             PolicyDefinition policyDefinition) {
        JSONObject jobject = new JSONObject();
        String origValue = Objects.isNull(propertyDefinition.getValue()) ? propertyDefinition.getDefaultValue() : propertyDefinition.getValue();
        jobject.put(GET_POLICY, null);
        propertyDefinition.setValue(jobject.toJSONString());

        GetPolicyValueDataDefinition getPolicyValueDataDefinition = new GetPolicyValueDataDefinition();
        getPolicyValueDataDefinition.setPolicyId(policyDefinition.getUniqueId());
        getPolicyValueDataDefinition.setPropertyName(propertyDefinition.getName());

        getPolicyValueDataDefinition.setOrigPropertyValue(origValue);

        propertyDefinition.setGetPolicyValues(Collections.singletonList(getPolicyValueDataDefinition));
    }

    private void setMocksForPropertyDeclaration(PolicyDefinition policyDefinition) {
        when(request.getSession()).thenReturn(session);
        when(session.getServletContext()).thenReturn(context);
        when(context.getAttribute(eq(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR))).thenReturn(contextWrapper);
        when(contextWrapper.getWebAppContext(any())).thenReturn(applicationContext);
        when(applicationContext.getBean(eq(PolicyBusinessLogic.class))).thenReturn(businessLogic);
        when(businessLogic.declareProperties(eq(USER_ID), eq(SERVICE_ID), any(), any())).thenReturn(
                Either.left(Collections.singletonList(policyDefinition)));
        when(componentsUtils
                     .convertJsonToObjectUsingObjectMapper(any(), any(), eq(ComponentInstInputsMap.class), eq(
                             AuditingActionEnum.CREATE_RESOURCE), eq(ComponentTypeEnum.SERVICE)))
                        .thenReturn(Either.left(getDeclarationBodyForProperty(PROP_1)));
    }

    private List<PropertyDataDefinition> getPropertiesList() {
        PropertyDefinition prop1 = new PropertyDataDefinitionBuilder()
                .setUniqueId("prop1")
                .build();

        PropertyDefinition prop2 = new PropertyDataDefinitionBuilder()
                .setUniqueId("prop2")
                .build();
        return Arrays.asList(prop1, prop2);
    }

    private List<PolicyTargetDTO> getTargetDTOList() {
        PolicyTargetDTO target1 = new PolicyTargetDTO();
        target1.setUniqueIds(Collections.singletonList("uniqueId"));
        target1.setType("GROUPS");

        PolicyTargetDTO target2 = new PolicyTargetDTO();
        target2.setUniqueIds(Collections.singletonList("uniqueId"));
        target2.setType("component_Instances");

        return Arrays.asList(target1, target2);
    }

    private Invocation.Builder buildGetPropertiesRequest(String componentType) {
        return target(PROPS_URL)
                .resolveTemplate("componentType", componentType)
                .resolveTemplate("serviceId", SERVICE_ID)
                .resolveTemplate("policyId", POLICY_ID)
                .request(MediaType.APPLICATION_JSON)
                .header(Constants.USER_ID_HEADER, USER_ID);

    }
    
    private Invocation buildUpdatePropertiesRequest(String componentType, List<PropertyDataDefinition> properties) {
        return target(PROPS_URL)
                .resolveTemplate("componentType", componentType)
                .resolveTemplate("serviceId", SERVICE_ID)
                .resolveTemplate("policyId", POLICY_ID)
                .request(MediaType.APPLICATION_JSON)
                .header(Constants.USER_ID_HEADER, USER_ID)
                .buildPut(Entity.entity(properties, MediaType.APPLICATION_JSON));
    }

    private Invocation buildUpdateTargetsRequest(String componentType, List<PolicyTargetDTO> targets) {
        return target(UPDATE_TARGETS_URL)
                .resolveTemplate("componentType", componentType)
                .resolveTemplate("componentId", COMPONENT_ID)
                .resolveTemplate("policyId", POLICY_ID)
                .request(MediaType.APPLICATION_JSON)
                .header(Constants.USER_ID_HEADER, USER_ID)
                .buildPost(Entity.entity(targets, MediaType.APPLICATION_JSON));
    }

    private Invocation.Builder buildGetPropertiesRequest() {
        return target(PROPS_URL)
                .resolveTemplate("componentType", "services")
                .resolveTemplate("serviceId", SERVICE_ID)
                .resolveTemplate("policyId", POLICY_ID)
                .request(MediaType.APPLICATION_JSON)
                .header(Constants.USER_ID_HEADER, USER_ID);
    }

    private Invocation buildDeletePolicyRequest(PolicyDefinition policyDefinition) {
        return target(DELETE_URL)
                       .resolveTemplate("containerComponentType", "services")
                       .resolveTemplate("componentId", SERVICE_ID)
                       .resolveTemplate("policyId", policyDefinition.getUniqueId())
                       .request(MediaType.APPLICATION_JSON)
                       .header(Constants.USER_ID_HEADER, USER_ID)
                        .buildDelete();
    }

    private Invocation buildDeclarePropertiesRequest(String propertyId) {
        return target(DECLARE_URL)
                       .resolveTemplate("componentType", "services")
                       .resolveTemplate("serviceId", SERVICE_ID)
                       .request(MediaType.APPLICATION_JSON)
                       .header(Constants.USER_ID_HEADER, USER_ID)
                       .buildPost(Entity.entity(getDeclarationBodyForProperty(propertyId), MediaType.APPLICATION_JSON));
    }

    private ComponentInstInputsMap getDeclarationBodyForProperty(String propertyId) {
        ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
        ComponentInstancePropInput propInput = new ComponentInstancePropInput();
        PropertyDefinition propertyDefinition = new PropertyDefinition();

        propertyDefinition.setType("string");
        propertyDefinition.setUniqueId(SERVICE_ID + "." + propertyId);
        propInput.setInput(propertyDefinition);
        propInput.setPropertiesName(propertyId);

        componentInstInputsMap.setComponentInstancePropertiesToPolicies(new HashMap<>());
        componentInstInputsMap.getComponentInstancePropertiesToPolicies().put("componentInstancePropertiesToPolicies", Collections.singletonList(propInput));

        return componentInstInputsMap;
    }

    @Override
    protected ResourceConfig configure() {
        return super.configure()
                .register(new PolicyServlet(null, null, componentsUtils,
                    servletUtils, null, businessLogic));
    }

    private static void createMocks() {
        propertyDeclarationOrchestrator = Mockito.mock(PropertyDeclarationOrchestrator.class);
        toscaOperationFacade = Mockito.mock(ToscaOperationFacade.class);
        businessLogic = Mockito.mock(PolicyBusinessLogic.class);
        businessLogic.setPropertyDeclarationOrchestrator(propertyDeclarationOrchestrator);
        businessLogic.setToscaOperationFacade(toscaOperationFacade);
        BaseBusinessLogic bbl = new BaseBusinessLogicTest(Mockito.mock(IElementOperation.class),
            Mockito.mock(IGroupOperation.class),
            Mockito.mock(IGroupInstanceOperation.class), Mockito.mock(IGroupTypeOperation.class),
            Mockito.mock(InterfaceOperation.class), Mockito.mock(InterfaceLifecycleOperation.class), Mockito.mock(
            ArtifactsOperations.class));
        PolicyServletTest.baseBusinessLogic = Mockito.spy(bbl);
        PolicyServletTest.baseBusinessLogic.setToscaOperationFacade(toscaOperationFacade);

        componentsUtils = Mockito.mock(ComponentsUtils.class);
        servletUtils = Mockito.mock(ServletUtils.class);
        responseFormat = Mockito.mock(ResponseFormat.class);
        roleAuthorizationHandler = Mockito.mock(RoleAuthorizationHandler.class);
    }

    private static class BaseBusinessLogicTest extends BaseBusinessLogic {

        BaseBusinessLogicTest(IElementOperation elementDao,
            IGroupOperation groupOperation,
            IGroupInstanceOperation groupInstanceOperation,
            IGroupTypeOperation groupTypeOperation,
            InterfaceOperation interfaceOperation,
            InterfaceLifecycleOperation interfaceLifecycleTypeOperation,
            ArtifactsOperations artifactToscaOperation) {
            super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation, interfaceOperation,
                interfaceLifecycleTypeOperation, artifactToscaOperation);
        }
    }
    
}
