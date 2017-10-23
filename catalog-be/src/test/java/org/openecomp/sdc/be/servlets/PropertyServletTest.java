package org.openecomp.sdc.be.servlets;

import java.util.Map;

import javax.annotation.Generated;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import org.openecomp.sdc.be.components.impl.PropertyBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.PropertyDefinition;

import com.google.common.collect.Multiset.Entry;

import fj.data.Either;

public class PropertyServletTest {

	private PropertyServlet createTestSubject() {
		return new PropertyServlet();
	}

	
	@Test
	public void testCreateProperty() throws Exception {
		PropertyServlet testSubject;
		String resourceId = "";
		String data = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetProperty() throws Exception {
		PropertyServlet testSubject;
		String resourceId = "";
		String propertyId = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testDeleteProperty() throws Exception {
		PropertyServlet testSubject;
		String resourceId = "";
		String propertyId = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testUpdateProperty() throws Exception {
		PropertyServlet testSubject;
		String resourceId = "";
		String propertyId = "";
		String data = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetPropertyModel() throws Exception {
		PropertyServlet testSubject;
		String resourceId = "";
		String data = "";
		Either<Map<String, PropertyDefinition>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
	}

	


	
	@Test
	public void testGetPropertyDefinitionJSONObject() throws Exception {
		PropertyServlet testSubject;
		PropertyDefinition propertyDefinition = null;
		JSONObject result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetPropertyBL() throws Exception {
		PropertyServlet testSubject;
		ServletContext context = null;
		PropertyBusinessLogic result;

		// default test
		testSubject = createTestSubject();
	}
}