package org.openecomp.sdc.be.servlets;

import java.util.List;

import javax.annotation.Generated;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Test;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.exception.ResponseFormat;

import fj.data.Either;

public class ComponentInstanceServletTest {

	private ComponentInstanceServlet createTestSubject() {
		return new ComponentInstanceServlet();
	}

	
	@Test
	public void testCreateComponentInstance() throws Exception {
		ComponentInstanceServlet testSubject;
		String data = "";
		String containerComponentId = "";
		String containerComponentType = "";
		String userId = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testUpdateComponentInstanceMetadata() throws Exception {
		ComponentInstanceServlet testSubject;
		String componentId = "";
		String componentInstanceId = "";
		String containerComponentType = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testUpdateMultipleComponentInstance() throws Exception {
		ComponentInstanceServlet testSubject;
		String componentId = "";
		String containerComponentType = "";
		HttpServletRequest request = null;
		String componentInstanceJsonArray = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testDeleteResourceInstance() throws Exception {
		ComponentInstanceServlet testSubject;
		String componentId = "";
		String resourceInstanceId = "";
		String containerComponentType = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testAssociateRIToRI() throws Exception {
		ComponentInstanceServlet testSubject;
		String componentId = "";
		String containerComponentType = "";
		String userId = "";
		String data = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testDissociateRIFromRI() throws Exception {
		ComponentInstanceServlet testSubject;
		String containerComponentType = "";
		String componentId = "";
		String userId = "";
		String data = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testCreateAndAssociateRIToRI() throws Exception {
		ComponentInstanceServlet testSubject;
		String componentId = "";
		String containerComponentType = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testUpdateResourceInstanceProperty() throws Exception {
		ComponentInstanceServlet testSubject;
		String componentId = "";
		String containerComponentType = "";
		String componentInstanceId = "";
		String userId = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testUpdateResourceInstanceInput() throws Exception {
		ComponentInstanceServlet testSubject;
		String componentId = "";
		String containerComponentType = "";
		String componentInstanceId = "";
		String userId = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testUpdateResourceInstanceAttribute() throws Exception {
		ComponentInstanceServlet testSubject;
		String componentId = "";
		String containerComponentType = "";
		String componentInstanceId = "";
		String userId = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testDeleteResourceInstanceProperty() throws Exception {
		ComponentInstanceServlet testSubject;
		String componentId = "";
		String containerComponentType = "";
		String componentInstanceId = "";
		String propertyId = "";
		String userId = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testChangeResourceInstanceVersion() throws Exception {
		ComponentInstanceServlet testSubject;
		String componentId = "";
		String componentInstanceId = "";
		String containerComponentType = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testUpdateGroupInstanceProperty() throws Exception {
		ComponentInstanceServlet testSubject;
		String componentId = "";
		String containerComponentType = "";
		String componentInstanceId = "";
		String groupInstanceId = "";
		String userId = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetGroupArtifactById() throws Exception {
		ComponentInstanceServlet testSubject;
		String containerComponentType = "";
		String componentId = "";
		String componentInstanceId = "";
		String groupInstId = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetInstancePropertiesById() throws Exception {
		ComponentInstanceServlet testSubject;
		String containerComponentType = "";
		String containerComponentId = "";
		String componentInstanceUniqueId = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testCreateServiceProxy() throws Exception {
		ComponentInstanceServlet testSubject;
		String data = "";
		String containerComponentId = "";
		String containerComponentType = "";
		String userId = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testDeleteServiceProxy() throws Exception {
		ComponentInstanceServlet testSubject;
		String containerComponentId = "";
		String serviceProxyId = "";
		String containerComponentType = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testChangeServiceProxyVersion() throws Exception {
		ComponentInstanceServlet testSubject;
		String containerComponentId = "";
		String serviceProxyId = "";
		String containerComponentType = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	

}