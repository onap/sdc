package org.openecomp.sdc.be.servlets;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.exception.ResponseFormat;

import com.google.common.base.Equivalence.Wrapper;

import fj.data.Either;

public class ResourcesServletTest {

	private ResourcesServlet createTestSubject() {
		return new ResourcesServlet();
	}

	
	@Test
	public void testCreateResource() throws Exception {
		ResourcesServlet testSubject;
		String data = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testIsUIImport() throws Exception {
		ResourcesServlet testSubject;
		String data = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testPerformUIImport() throws Exception {
	ResourcesServlet testSubject;Wrapper<Response> responseWrapper = null;
	String data = "";
	HttpServletRequest request = null;
	String userId = "";
	String resourceUniqueId = "";
	
	
	// default test
	}

	
	@Test
	public void testParseToResource() throws Exception {
		ResourcesServlet testSubject;
		String resourceJson = "";
		User user = null;
		Either<Resource, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testParseToLightResource() throws Exception {
		ResourcesServlet testSubject;
		String resourceJson = "";
		User user = null;
		Either<Resource, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testDeleteResource() throws Exception {
		ResourcesServlet testSubject;
		String resourceId = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testDeleteResourceByNameAndVersion() throws Exception {
		ResourcesServlet testSubject;
		String resourceName = "";
		String version = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetResourceById() throws Exception {
		ResourcesServlet testSubject;
		String resourceId = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetResourceByNameAndVersion() throws Exception {
		ResourcesServlet testSubject;
		String resourceName = "";
		String resourceVersion = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testValidateResourceName() throws Exception {
		ResourcesServlet testSubject;
		String resourceName = "";
		String resourceType = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetCertifiedAbstractResources() throws Exception {
		ResourcesServlet testSubject;
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetCertifiedNotAbstractResources() throws Exception {
		ResourcesServlet testSubject;
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testUpdateResourceMetadata() throws Exception {
		ResourcesServlet testSubject;
		String resourceId = "";
		String data = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testUpdateResource() throws Exception {
		ResourcesServlet testSubject;
		String data = "";
		HttpServletRequest request = null;
		String userId = "";
		String resourceId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testConvertMapToList() throws Exception {
		Map<String, PropertyDefinition> properties = null;
		List<PropertyDefinition> result;

		// test 1
		properties = null;
		
	}

	
	@Test
	public void testGetResourceFromCsar() throws Exception {
		ResourcesServlet testSubject;
		HttpServletRequest request = null;
		String userId = "";
		String csarUUID = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}
}