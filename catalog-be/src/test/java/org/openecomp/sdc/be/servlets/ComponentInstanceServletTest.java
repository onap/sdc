package org.openecomp.sdc.be.servlets;

import fj.data.Either;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Entity;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
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
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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

    @Override
    protected ResourceConfig configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        ApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class);
        return new ResourceConfig(ComponentInstanceServlet.class)
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(request).to(HttpServletRequest.class);
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
