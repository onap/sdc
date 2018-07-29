package org.openecomp.sdc.be.servlets;

import com.fasterxml.jackson.databind.DeserializationFeature;
import fj.data.Either;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.impl.PolicyBusinessLogic;
import org.openecomp.sdc.be.components.utils.PropertyDataDefinitionBuilder;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.PolicyTargetDTO;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class PolicyServletTest extends JerseySpringBaseTest{

    private final static String USER_ID = "jh0003";
    private static final String COMPONENT_ID = "componentId";
    private static PolicyBusinessLogic businessLogic;
    private static ComponentsUtils componentsUtils;
    private static ServletUtils servletUtils;
    private static ResponseFormat responseFormat;

    private static String validComponentType = "resources";
    private static String unsupportedComponentType = "unsupported";
    private static String componentId = "componentId";
    private static String policyTypeName = "policyTypeName";

    private static final String PROPS_URL = "/v1/catalog/{componentType}/{serviceId}/policies/{policyId}/properties";
    private static final String SERVICE_ID = "serviceId";
    private static final String POLICY_ID = "policyId";

    private static final String UPDATE_TARGETS_URL = "/v1/catalog/{componentType}/{componentId}/policies/{policyId}/targets";

    @BeforeClass
    public static void initClass() {
        createMocks();
        when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);
    }
    
    @Before
    public void beforeMethod() {
        final JacksonJsonProvider jacksonJsonProvider = new JacksonJaxbJsonProvider().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        setClient(ClientBuilder.newClient(new ClientConfig(jacksonJsonProvider)));
    }

    @Test
    public void testGetPolicySuccess(){
        String path = "/v1/catalog/" + validComponentType + "/" + componentId + "/policies/" + POLICY_ID;
        Either<PolicyDefinition, ResponseFormat> successResponse = Either.left(new PolicyDefinition());
        when(businessLogic.getPolicy(eq(ComponentTypeEnum.RESOURCE), eq(componentId), eq(POLICY_ID), eq(USER_ID))).thenReturn(successResponse);
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200.getStatusCode());
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
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
        when(responseFormat.getStatus()).thenReturn(HttpStatus.BAD_REQUEST_400.getStatusCode());
        when(componentsUtils.getResponseFormat(eq(ActionStatus.UNSUPPORTED_ERROR), eq(unsupportedComponentType))).thenReturn(responseFormat);
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
        Either<PolicyDefinition, ResponseFormat> successResponse = Either.left(policy);
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
        when(responseFormat.getStatus()).thenReturn(HttpStatus.BAD_REQUEST_400.getStatusCode());
        when(componentsUtils.getResponseFormat(eq(ActionStatus.UNSUPPORTED_ERROR), eq(unsupportedComponentType))).thenReturn(responseFormat);
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
        Either<PolicyDefinition, ResponseFormat> successResponse = Either.left(policy);
        when(businessLogic.updatePolicy(eq(ComponentTypeEnum.RESOURCE), eq(componentId), any(PolicyDefinition.class), eq(USER_ID), eq(true))).thenReturn(successResponse);
        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200.getStatusCode());
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
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
        when(responseFormat.getStatus()).thenReturn(HttpStatus.BAD_REQUEST_400.getStatusCode());
        when(componentsUtils.getResponseFormat(eq(ActionStatus.UNSUPPORTED_ERROR), eq(unsupportedComponentType))).thenReturn(responseFormat);
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
        Either<PolicyDefinition, ResponseFormat> successResponse = Either.left(new PolicyDefinition());
        when(businessLogic.deletePolicy(eq(ComponentTypeEnum.RESOURCE), eq(componentId), eq(POLICY_ID), eq(USER_ID), eq(true))).thenReturn(successResponse);
        when(responseFormat.getStatus()).thenReturn(HttpStatus.NO_CONTENT_204.getStatusCode());
        when(componentsUtils.getResponseFormat(ActionStatus.NO_CONTENT)).thenReturn(responseFormat);
        Response response = target()
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .header("USER_ID", USER_ID)
                .delete(Response.class);

        assertEquals(response.getStatus(), HttpStatus.NO_CONTENT_204.getStatusCode());
    }

    @Test
    public void testDeletePolicyFailure(){
        String path = "/v1/catalog/" + unsupportedComponentType + "/" + componentId + "/policies/" + POLICY_ID;
        when(responseFormat.getStatus()).thenReturn(HttpStatus.BAD_REQUEST_400.getStatusCode());
        when(componentsUtils.getResponseFormat(eq(ActionStatus.UNSUPPORTED_ERROR), eq(unsupportedComponentType))).thenReturn(responseFormat);
        Response response = target()
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .header("USER_ID", USER_ID)
                .delete(Response.class);

        assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST_400.getStatusCode());
    }

    @Test
    public void getPolicyProperties_operationForbidden() {
        when(businessLogic.getPolicyProperties(ComponentTypeEnum.SERVICE, SERVICE_ID, POLICY_ID, USER_ID)).thenReturn(Either.right(new ResponseFormat(Response.Status.FORBIDDEN.getStatusCode())));
        Response response = buildGetPropertiesRequest().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void getPolicyProperties_unHandledError_returnGeneralError() {
        when(businessLogic.getPolicyProperties(ComponentTypeEnum.SERVICE, SERVICE_ID, POLICY_ID, USER_ID)).thenThrow(new RuntimeException());
        Response response = buildGetPropertiesRequest().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void getPolicyProperties_wrongComponentType() {
        Response response = buildGetPropertiesRequest("unknownType").get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        //verifyZeroInteractions(businessLogic);
    }

    @Test
    public void getPolicyProperties() {
        List<PropertyDataDefinition> properties = getPropertiesList();
        when(businessLogic.getPolicyProperties(ComponentTypeEnum.SERVICE, SERVICE_ID, POLICY_ID, USER_ID)).thenReturn(Either.left(properties));
        List<PropertyDataDefinition> policyProps = buildGetPropertiesRequest().get(new GenericType<List<PropertyDataDefinition>>() {});
        assertThat(policyProps)
                .usingElementComparatorOnFields("uniqueId")
                .containsExactlyInAnyOrder(properties.get(0), properties.get(1));
    }
    
    @Test
    public void updatePolicyPropertiesSuccess() {
        List<PropertyDataDefinition> properties = getPropertiesList();
        when(businessLogic.updatePolicyProperties(eq(ComponentTypeEnum.SERVICE), eq(SERVICE_ID), eq(POLICY_ID), any(PropertyDataDefinition[].class), eq(USER_ID), eq(true))).thenReturn(Either.left(properties));
        List<PropertyDataDefinition> policyProps = buildUpdatePropertiesRequest(ComponentTypeEnum.SERVICE_PARAM_NAME, properties).invoke(new GenericType<List<PropertyDataDefinition>>() {});
        assertThat(policyProps)
                .usingElementComparatorOnFields("uniqueId")
                .containsExactlyInAnyOrder(properties.get(0), properties.get(1));
    }

    @Test
    public void updatePolicyTargetsSuccess() {
        List<PolicyTargetDTO> targets = getTargetDTOList();
        when(businessLogic.updatePolicyTargets(eq(ComponentTypeEnum.RESOURCE), eq(COMPONENT_ID), eq(POLICY_ID), anyMap(), eq(USER_ID))).thenReturn(Either.left(new PolicyDefinition()));
        Response policyTargets = buildUpdateTargetsRequest(ComponentTypeEnum.RESOURCE_PARAM_NAME, targets).invoke();
        assertThat(policyTargets.getStatus()).isEqualTo(200);
    }

    @Test
    public void updatePolicyPropertiesFailure() {
        List<PropertyDataDefinition> properties = getPropertiesList();
        ResponseFormat notFoundResponse = new ResponseFormat(HttpStatus.NOT_FOUND_404.getStatusCode());
        when(businessLogic.updatePolicyProperties(eq(ComponentTypeEnum.SERVICE), eq(SERVICE_ID), eq(POLICY_ID), any(PropertyDataDefinition[].class), eq(USER_ID), eq(true))).thenReturn(Either.right(notFoundResponse));
        Response policyProps = buildUpdatePropertiesRequest(ComponentTypeEnum.SERVICE_PARAM_NAME, properties).invoke();
        assertEquals(HttpStatus.NOT_FOUND_404.getStatusCode(), policyProps.getStatus());
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
    
    @Override
    protected ResourceConfig configure() {
        return super.configure()
                .register(new PolicyServlet(businessLogic, servletUtils, null, componentsUtils));
    }

    private static void createMocks() {
        businessLogic = Mockito.mock(PolicyBusinessLogic.class);
        componentsUtils = Mockito.mock(ComponentsUtils.class);
        servletUtils = Mockito.mock(ServletUtils.class);
        responseFormat = Mockito.mock(ResponseFormat.class);
    }
    
}
