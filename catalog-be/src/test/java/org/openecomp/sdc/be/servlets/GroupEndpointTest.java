//package org.openecomp.sdc.be.servlets;
//
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
//import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
//import fj.data.Either;
//import org.eclipse.jetty.http.HttpStatus;
//import org.glassfish.hk2.utilities.binding.AbstractBinder;
//import org.glassfish.jersey.client.ClientConfig;
//import org.glassfish.jersey.server.ResourceConfig;
//import org.glassfish.jersey.test.JerseyTest;
//import org.glassfish.jersey.test.TestProperties;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.mockito.junit.MockitoJUnitRunner;
//import org.openecomp.sdc.be.DummyConfigurationManager;
//import org.openecomp.sdc.be.components.impl.GroupBusinessLogic;
//import org.openecomp.sdc.be.dao.api.ActionStatus;
//import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
//import org.openecomp.sdc.be.impl.ComponentsUtils;
//import org.openecomp.sdc.be.impl.ServletUtils;
//import org.openecomp.sdc.be.impl.WebAppContextWrapper;
//import org.openecomp.sdc.be.info.GroupDefinitionInfo;
//import org.openecomp.sdc.be.model.GroupDefinition;
//import org.openecomp.sdc.common.api.Constants;
//import org.openecomp.sdc.exception.ResponseFormat;
//import org.springframework.web.context.WebApplicationContext;
//
//import javax.servlet.ServletContext;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpSession;
//import javax.ws.rs.client.ClientBuilder;
//import javax.ws.rs.client.Invocation;
//import javax.ws.rs.core.Application;
//import javax.ws.rs.core.MediaType;
//import javax.ws.rs.core.Response;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.Assert.assertEquals;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.when;
//
//@RunWith(MockitoJUnitRunner.class)
//public class GroupEndpointTest extends JerseyTest {
//
//    public static final String USER_ID = "jh0003";
//    public static final String INVALID_USER_ID = "jh0001";
//    final static String RESOURCE_TYPE = "resources";
//    private static final String COMPONENT_ID = "1234";
//    private static final String VALID_GROUP_ID = "1";
//    private static final String INVALID_GROUP_ID = "2";
//    private static final String NEW_GROUP_NAME = "new group";
//    private static final String VALID_GROUP_TYPE = "networkConnection";
//
//    @Mock
//    private GroupBusinessLogic groupBusinessLogic;
//    @Mock
//    private ServletContext servletContext;
//    @Mock
//    private WebAppContextWrapper webAppContextWrapper;
//    @Mock
//    private WebApplicationContext webApplicationContext;
//    @Mock
//    private HttpServletRequest request;
//    @Mock
//    private HttpSession session;
//    @Mock
//    private ComponentsUtils componentUtils;
//    @Mock
//    private ServletUtils servletUtils;
//    @Mock
//    private ResponseFormat responseFormat;
//
//    @Override
//    protected Application configure() {
//        ResourceConfig resourceConfig = new ResourceConfig()
//                .register(GroupServlet.class)
//                //.register(mapper)
//                ;
//        forceSet(TestProperties.CONTAINER_PORT, "0");
//        resourceConfig.register(new AbstractBinder() {
//            @Override
//            protected void configure() {
//                bind(request).to(HttpServletRequest.class);
//            }
//        });
//        return resourceConfig;
//    }
//
//    @Before
//    public void before() {
//        when(request.getSession()).thenReturn(session);
//        when(session.getServletContext()).thenReturn(servletContext);
//        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
//        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webApplicationContext);
//        when(webApplicationContext.getBean(GroupBusinessLogic.class)).thenReturn(groupBusinessLogic);
////        when(webApplicationContext.getBean(ComponentsUtils.class)).thenReturn(componentUtils);
//        when(webApplicationContext.getBean(ServletUtils.class)).thenReturn(servletUtils);
//        when(servletUtils.getComponentsUtils()).thenReturn(componentUtils);
////        when(request.getHeader("USER_ID")).thenReturn(USER_ID);
//        final JacksonJsonProvider jacksonJsonProvider = new JacksonJaxbJsonProvider().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        setClient(ClientBuilder.newClient(new ClientConfig(jacksonJsonProvider)));
//        new DummyConfigurationManager();
////        when(ConfigurationManager.getConfigurationManager().getConfiguration().getExcludedGroupTypesMapping()).thenReturn(buildExcludePolicyTypesMap());
//    }
//
//    //@Test
//    public void createGroup() {
//        GroupDefinition groupDefinition = new GroupDefinition();
//        groupDefinition.setName(NEW_GROUP_NAME);
//        groupDefinition.setType(VALID_GROUP_TYPE);
//        when(groupBusinessLogic.createGroup(eq(NEW_GROUP_NAME), eq(ComponentTypeEnum.RESOURCE), eq(COMPONENT_ID), eq(USER_ID)))
//                .thenReturn(groupDefinition);
//        //List<GroupDefinition> gdList = buildCreateGroupCall().post(Response.class);
//
//    }
//
//    //@Test
//    public void deleteGroup_withInvalidUser_shouldReturn_401() {
//        int unauthorized401 = HttpStatus.UNAUTHORIZED_401;
//        int unauthorized403 = HttpStatus.FORBIDDEN_403;
//        when(groupBusinessLogic.validateUserExists(eq(INVALID_USER_ID), anyString(), anyBoolean()))
//                .thenReturn(Either.right(new ResponseFormat(unauthorized401)));
//
//        Response response = buildDeleteGroupCall(INVALID_USER_ID, VALID_GROUP_ID).delete();
//        assertThat(response.getStatus()).isEqualTo(unauthorized401);
//    }
//
//    @Test
//    public void getGroupById_internalServerError() {
//        when(groupBusinessLogic.getGroupWithArtifactsById(eq(ComponentTypeEnum.RESOURCE), eq(COMPONENT_ID), eq(VALID_GROUP_ID), eq(USER_ID), eq(false) ))
//                .thenReturn(Either.right(new ResponseFormat(HttpStatus.INTERNAL_SERVER_ERROR_500)));
//        Response response = buildGetGroupCall(VALID_GROUP_ID).get();
//        assertEquals(response.getStatus(), 500);
//    }
//
//    @Test
//    public void getGroupById_Success() {
//        GroupDefinitionInfo groupDefinitionInfo = new GroupDefinitionInfo();
//        groupDefinitionInfo.setUniqueId(VALID_GROUP_ID);
//        when(groupBusinessLogic.getGroupWithArtifactsById(eq(ComponentTypeEnum.RESOURCE), eq(COMPONENT_ID), eq(VALID_GROUP_ID), eq(USER_ID), eq(false) ))
//                .thenReturn(Either.left(groupDefinitionInfo));
//        when(componentUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
//        when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200);
//        GroupDefinitionInfo gdi = buildGetGroupCall(VALID_GROUP_ID).get(GroupDefinitionInfo.class);
//        assertEquals(gdi.getUniqueId(), VALID_GROUP_ID);
//    }
//
//    @Test
//    public void getGroupById_Failure() {
//        when(groupBusinessLogic.getGroupWithArtifactsById(eq(ComponentTypeEnum.RESOURCE), eq(COMPONENT_ID), eq(INVALID_GROUP_ID), eq(USER_ID), eq(false) ))
//                .thenReturn(Either.right(new ResponseFormat(HttpStatus.NOT_FOUND_404)));
//        Response response = buildGetGroupCall(INVALID_GROUP_ID).get();
//        assertEquals(response.getStatus(), 404);
//    }
//
//    private Invocation.Builder buildGetGroupCall(String groupId) {
//        String path = "/v1/catalog/" + RESOURCE_TYPE + "/" + COMPONENT_ID + "/groups/" + groupId;
//        return target(path)
//                .request(MediaType.APPLICATION_JSON)
//                .header(Constants.USER_ID_HEADER, USER_ID);
//    }
//
//    private Invocation.Builder buildDeleteGroupCall(String userId, String groupId) {
//        String path = "/v1/catalog/" + RESOURCE_TYPE + "/" + COMPONENT_ID + "/groups/" + groupId;
//        return target(path)
//                .request(MediaType.APPLICATION_JSON)
//                .header(Constants.USER_ID_HEADER, userId);
//    }
//
//    private Invocation.Builder buildCreateGroupCall() {
//        String path = "/v1/catalog/" + RESOURCE_TYPE + "/" + COMPONENT_ID + "/groups";
//        return target(path)
//                .request(MediaType.APPLICATION_JSON)
//                .header(Constants.USER_ID_HEADER, USER_ID);
//    }
//}
