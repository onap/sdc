package org.openecomp.sdc.be.servlets;

import javax.annotation.Generated;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;


public class ArtifactServletTest {

	private ArtifactServlet createTestSubject() {
		return new ArtifactServlet();
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