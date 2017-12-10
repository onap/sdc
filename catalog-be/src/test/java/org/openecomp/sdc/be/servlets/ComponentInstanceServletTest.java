package org.openecomp.sdc.be.servlets;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.impl.ServiceComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.VFComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.web.context.WebApplicationContext;

import fj.data.Either;

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
	private static VFComponentInstanceBusinessLogic vfBusinessLogic;
	private static ServiceComponentInstanceBusinessLogic serviceBusinessLogic;
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
		when(vfBusinessLogic.getRelationById(eq(componentId), eq(relationId), eq(USER_ID), eq(ComponentTypeEnum.RESOURCE))).thenReturn(successResponse);
		when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200.getStatusCode());
		when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
		Response response = target()
				.path(path)
				.request(MediaType.APPLICATION_JSON)
				.header("USER_ID", USER_ID)
				.get( Response.class);

		assertTrue(response.getStatus() == HttpStatus.OK_200.getStatusCode());
	}
	
	@Test
	public void testGetRelationByIdFailure(){
		
		String containerComponentType = "unknown_type";
		String componentId = "componentId";
		String relationId = "relationId";
		String path = "/v1/catalog/" + containerComponentType + "/" + componentId + "/" + relationId + "/relationId";
		when(responseFormat.getStatus()).thenReturn(HttpStatus.BAD_REQUEST_400.getStatusCode());
		when(componentsUtils.getResponseFormat(eq(ActionStatus.UNSUPPORTED_ERROR), eq(containerComponentType))).thenReturn(responseFormat);
		Response response = target()
				.path(path)
				.request(MediaType.APPLICATION_JSON)
				.header("USER_ID", USER_ID)
				.get( Response.class);

		assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST_400.getStatusCode());
	}

	@Override
	protected Application configure() {

		ResourceConfig resourceConfig = new ResourceConfig(ComponentInstanceServlet.class);
		forceSet(TestProperties.CONTAINER_PORT, "0");
		
		resourceConfig.register(new AbstractBinder() {
			@Override
			protected void configure() {
				bind(request).to(HttpServletRequest.class);
			}
		});
		return resourceConfig;
	}
	
	private static void createMocks() {
		request = Mockito.mock(HttpServletRequest.class);
		session = Mockito.mock(HttpSession.class);
		servletContext = Mockito.mock(ServletContext.class);
		webAppContextWrapper = Mockito.mock(WebAppContextWrapper.class);
		webApplicationContext = Mockito.mock(WebApplicationContext.class);
		vfBusinessLogic = Mockito.mock(VFComponentInstanceBusinessLogic.class);
		serviceBusinessLogic = Mockito.mock(ServiceComponentInstanceBusinessLogic.class);
		componentsUtils = Mockito.mock(ComponentsUtils.class);
		servletUtils = Mockito.mock(ServletUtils.class);
		responseFormat = Mockito.mock(ResponseFormat.class);
	}
	
	private static void stubMethods() {
		when(request.getSession()).thenReturn(session);
		when(session.getServletContext()).thenReturn(servletContext);
		when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
		when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webApplicationContext);
		when(webApplicationContext.getBean(VFComponentInstanceBusinessLogic.class)).thenReturn(vfBusinessLogic);
		when(webApplicationContext.getBean(ServiceComponentInstanceBusinessLogic.class)).thenReturn(serviceBusinessLogic);
		when(request.getHeader("USER_ID")).thenReturn(USER_ID);
		when(webApplicationContext.getBean(ServletUtils.class)).thenReturn(servletUtils);
		when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);
	}
}
