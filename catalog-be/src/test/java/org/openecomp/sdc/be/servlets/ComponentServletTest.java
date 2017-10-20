package org.openecomp.sdc.be.servlets;

import java.util.List;

import javax.annotation.Generated;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.Test;

public class ComponentServletTest {

	private ComponentServlet createTestSubject() {
		return new ComponentServlet();
	}

	
	@Test
	public void testConformanceLevelValidation() throws Exception {
		ComponentServlet testSubject;
		String componentType = "";
		String componentUuid = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetRequirementAndCapabilities() throws Exception {
		ComponentServlet testSubject;
		String componentType = "";
		String componentId = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetLatestVersionNotAbstractCheckoutComponents() throws Exception {
		ComponentServlet testSubject;
		String componentType = "";
		HttpServletRequest request = null;
		String internalComponentType = "";
		List<String> componentUids = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetLatestVersionNotAbstractCheckoutComponentsByBody() throws Exception {
		ComponentServlet testSubject;
		String componentType = "";
		HttpServletRequest request = null;
		String internalComponentType = "";
		String userId = "";
		List<String> data = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetLatestVersionNotAbstractCheckoutComponentsIdesOnly() throws Exception {
		ComponentServlet testSubject;
		String componentType = "";
		HttpServletRequest request = null;
		String internalComponentType = "";
		String userId = "";
		String data = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetComponentInstancesFilteredByPropertiesAndInputs() throws Exception {
		ComponentServlet testSubject;
		String componentType = "";
		String componentId = "";
		HttpServletRequest request = null;
		String searchText = "";
		String userId = "";
		String data = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetComponentDataFilteredByParams() throws Exception {
		ComponentServlet testSubject;
		String componentType = "";
		String componentId = "";
		List<String> dataParamsToReturn = null;
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetFilteredComponentInstanceProperties() throws Exception {
		ComponentServlet testSubject;
		String componentType = "";
		String componentId = "";
		String propertyNameFragment = "";
		List<String> resourceTypes = null;
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}
}