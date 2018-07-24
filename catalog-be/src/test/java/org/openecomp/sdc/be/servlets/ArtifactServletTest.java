package org.openecomp.sdc.be.servlets;

import fj.data.Either;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.exception.ResponseFormat;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class ArtifactServletTest extends JerseySpringBaseTest {

	private static ArtifactsBusinessLogic businessLogic;
	private static ServletUtils servletUtils;
	private static ResourceImportManager resourceImportManager;
	private static ComponentsUtils componentsUtils;
	private static ResponseFormat responseFormat;
	private final static String USER_ID = "cs0008";

	private ArtifactServlet createTestSubject() {
		return new ArtifactServlet(businessLogic, servletUtils, resourceImportManager, componentsUtils);
	}

  @BeforeClass
	public static void initClass() {
		businessLogic = Mockito.mock(ArtifactsBusinessLogic.class);
		servletUtils = Mockito.mock(ServletUtils.class);
		resourceImportManager = Mockito.mock(ResourceImportManager.class);
		componentsUtils = Mockito.mock(ComponentsUtils.class);
		responseFormat = Mockito.mock(ResponseFormat.class);
	}

	@Override
	protected ResourceConfig configure() {
		return super.configure().register(new ArtifactServlet(businessLogic, servletUtils,
				resourceImportManager, componentsUtils));
	}

	@Test
	public void testPutWorkflowArtifactSuccess(){
		String path = "v1/catalog/resources/resourceId/interfaces/operationId/artifacts/artifactId";

		ArtifactDefinition artifact = new ArtifactDefinition();
		Either<ArtifactDefinition, ResponseFormat> successResponse = Either.left(artifact);

		when(businessLogic.updateArtifactOnInterfaceOperationByResourceUUID(any(), any(),
				any(), any(), any(), any(), any(), any())).thenReturn(successResponse);
		when(responseFormat.getStatus()).thenReturn(HttpStatus.OK_200.getStatusCode());
		when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);
		Response response = target()
				.path(path)
				.request(MediaType.APPLICATION_JSON)
				.header("USER_ID", USER_ID)
				.put(Entity.entity(artifact, MediaType.APPLICATION_JSON),Response.class);

		assertEquals(HttpStatus.OK_200.getStatusCode(), response.getStatus());
	}

	@Test
	public void testPutWorkflowArtifactFailure(){
		String path = "v1/catalog/resourcesTest/resourceId/interfaces/operationId/artifacts/artifactId";

		ArtifactDefinition artifact = new ArtifactDefinition();
		Either<ArtifactDefinition, ResponseFormat> successResponse = Either.left(artifact);

		when(businessLogic.updateArtifactOnInterfaceOperationByResourceUUID(any(), any(),
				eq(ComponentTypeEnum.RESOURCE), any(), any(), any(), any(), any())).thenReturn(successResponse);
		when(responseFormat.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500.getStatusCode());
		when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);
		when(componentsUtils.getResponseFormat(eq(ActionStatus.GENERAL_ERROR), any())).thenReturn(responseFormat);

		Response response = target()
				.path(path)
				.request(MediaType.APPLICATION_JSON)
				.header("USER_ID", USER_ID)
				.put(Entity.entity(artifact, MediaType.APPLICATION_JSON),Response.class);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500.getStatusCode(), response.getStatus());
	}

	@Test
	public void testLoadArtifact() throws Exception {
		ArtifactServlet testSubject;
		String resourceId = "";
		String data = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testUpdateArtifact() throws Exception {
		ArtifactServlet testSubject;
		String resourceId = "";
		String artifactId = "";
		String data = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testDeleteArtifact() throws Exception {
		ArtifactServlet testSubject;
		String resourceId = "";
		String artifactId = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testLoadInformationArtifact() throws Exception {
		ArtifactServlet testSubject;
		String serviceId = "";
		String data = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testUpdateInformationArtifact() throws Exception {
		ArtifactServlet testSubject;
		String serviceId = "";
		String artifactId = "";
		String data = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testUpdateApiArtifact() throws Exception {
		ArtifactServlet testSubject;
		String serviceId = "";
		String artifactId = "";
		String data = "";
		HttpServletRequest request = null;
		String userId = "";
		String origMd5 = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testDeleteApiArtifact() throws Exception {
		ArtifactServlet testSubject;
		String serviceId = "";
		String artifactId = "";
		HttpServletRequest request = null;
		String userId = "";
		String origMd5 = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testDeleteInformationalArtifact() throws Exception {
		ArtifactServlet testSubject;
		String serviceId = "";
		String artifactId = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testDownloadServiceArtifactBase64() throws Exception {
		ArtifactServlet testSubject;
		String serviceId = "";
		String artifactId = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testDownloadResourceArtifactBase64() throws Exception {
		ArtifactServlet testSubject;
		String resourceId = "";
		String artifactId = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testDownloadResourceInstanceArtifactBase64() throws Exception {
		ArtifactServlet testSubject;
		String containerComponentType = "";
		String componentId = "";
		String componentInstanceId = "";
		String artifactId = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testLoadArtifactToInterface() throws Exception {
		ArtifactServlet testSubject;
		String resourceId = "";
		String interfaceType = "";
		String operation = "";
		String userId = "";
		String origMd5 = "";
		String data = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testDeleteArtifactToInterface() throws Exception {
		ArtifactServlet testSubject;
		String resourceId = "";
		String interfaceType = "";
		String operation = "";
		String artifactId = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testUpdateArtifactToInterface() throws Exception {
		ArtifactServlet testSubject;
		String resourceId = "";
		String interfaceType = "";
		String operation = "";
		String artifactId = "";
		String userId = "";
		String origMd5 = "";
		HttpServletRequest request = null;
		String data = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testUpdateRIArtifact() throws Exception {
		ArtifactServlet testSubject;
		String containerComponentType = "";
		String componentId = "";
		String componentInstanceId = "";
		String artifactId = "";
		String data = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testUpdateComponentInstanceArtifact() throws Exception {
		ArtifactServlet testSubject;
		String userId = "";
		String origMd5 = "";
		String containerComponentType = "";
		String componentId = "";
		String componentInstanceId = "";
		String artifactId = "";
		String data = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testLoadComponentInstanceArtifact() throws Exception {
		ArtifactServlet testSubject;
		String userId = "";
		String origMd5 = "";
		String containerComponentType = "";
		String componentId = "";
		String componentInstanceId = "";
		String data = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testDeleteComponentInstanceArtifact() throws Exception {
		ArtifactServlet testSubject;
		String userId = "";
		String origMd5 = "";
		String containerComponentType = "";
		String componentId = "";
		String componentInstanceId = "";
		String artifactId = "";
		String data = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetComponentArtifacts() throws Exception {
		ArtifactServlet testSubject;
		String containerComponentType = "";
		String componentId = "";
		String artifactGroupType = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetComponentInstanceArtifacts() throws Exception {
		ArtifactServlet testSubject;
		String containerComponentType = "";
		String componentId = "";
		String componentInstanceId = "";
		String artifactGroupType = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	

}