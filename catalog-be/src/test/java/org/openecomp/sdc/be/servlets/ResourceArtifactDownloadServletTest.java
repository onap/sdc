package org.openecomp.sdc.be.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.Test;

import ch.qos.logback.classic.Logger;

public class ResourceArtifactDownloadServletTest {

	private ResourceArtifactDownloadServlet createTestSubject() {
		return new ResourceArtifactDownloadServlet();
	}

	
	@Test
	public void testGetResourceArtifactByName() throws Exception {
		ResourceArtifactDownloadServlet testSubject;
		String resourceName = "";
		String resourceVersion = "";
		String artifactName = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetResourceArtifactMetadata() throws Exception {
		ResourceArtifactDownloadServlet testSubject;
		String resourceName = "";
		String resourceVersion = "";
		String artifactName = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetLogger() throws Exception {
		ResourceArtifactDownloadServlet testSubject;
		Logger result;

		// default test
		testSubject = createTestSubject();
	}
}