package org.openecomp.sdc.be.servlets;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.openecomp.sdc.be.components.impl.InputsBusinessLogic;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.exception.ResponseFormat;

import fj.data.Either;

public class InputsServletTest {

	private InputsServlet createTestSubject() {
		return new InputsServlet();
	}

	
	@Test
	public void testGetComponentInputs() throws Exception {
		InputsServlet testSubject;
		String componentType = "";
		String componentId = "";
		HttpServletRequest request = null;
		String fromName = "";
		int amount = 0;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testUpdateComponentInputs() throws Exception {
		InputsServlet testSubject;
		String containerComponentType = "";
		String componentId = "";
		String data = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetComponentInstanceInputs() throws Exception {
		InputsServlet testSubject;
		String componentType = "";
		String componentId = "";
		String instanceId = "";
		String originComonentUid = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetInputPropertiesForComponentInstance() throws Exception {
		InputsServlet testSubject;
		String componentType = "";
		String componentId = "";
		String instanceId = "";
		String inputId = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetInputsForComponentInput() throws Exception {
		InputsServlet testSubject;
		String componentType = "";
		String componentId = "";
		String inputId = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetInputsAndPropertiesForComponentInput() throws Exception {
		InputsServlet testSubject;
		String componentType = "";
		String componentId = "";
		String inputId = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testParseToComponentInstanceMap() throws Exception {
		InputsServlet testSubject;
		String serviceJson = "";
		User user = null;
		Either<ComponentInstInputsMap, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testCreateMultipleInputs() throws Exception {
		InputsServlet testSubject;
		String componentType = "";
		String componentId = "";
		HttpServletRequest request = null;
		String userId = "";
		String componentInstInputsMapObj = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testDeleteInput() throws Exception {
		InputsServlet testSubject;
		String componentType = "";
		String componentId = "";
		String inputId = "";
		HttpServletRequest request = null;
		String userId = "";
		String componentInstInputsMapObj = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetInputBL() throws Exception {
		InputsServlet testSubject;
		ServletContext context = null;
		InputsBusinessLogic result;

		// default test
		testSubject = createTestSubject();
	}
}