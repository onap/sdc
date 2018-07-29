package org.openecomp.sdc.be.components.distribution.engine;

import org.junit.Test;
import org.openecomp.sdc.be.distribution.api.client.CambriaOperationStatus;

import java.util.List;

public class CambriaErrorResponseTest {

	private CambriaErrorResponse createTestSubject() {
		return new CambriaErrorResponse();
	}
	
	@Test
	public void testConstructors() throws Exception {
		CambriaErrorResponse testSubject;
		CambriaOperationStatus result;

		// default test
		new CambriaErrorResponse(CambriaOperationStatus.AUTHENTICATION_ERROR);
		new CambriaErrorResponse(CambriaOperationStatus.CONNNECTION_ERROR, 500);
	}
	
	@Test
	public void testGetOperationStatus() throws Exception {
		CambriaErrorResponse testSubject;
		CambriaOperationStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOperationStatus();
	}

	@Test
	public void testSetOperationStatus() throws Exception {
		CambriaErrorResponse testSubject;
		CambriaOperationStatus operationStatus = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setOperationStatus(operationStatus);
	}

	@Test
	public void testGetHttpCode() throws Exception {
		CambriaErrorResponse testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getHttpCode();
	}

	@Test
	public void testSetHttpCode() throws Exception {
		CambriaErrorResponse testSubject;
		Integer httpCode = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setHttpCode(httpCode);
	}

	@Test
	public void testAddVariable() throws Exception {
		CambriaErrorResponse testSubject;
		String variable = "";

		// default test
		testSubject = createTestSubject();
		testSubject.addVariable(variable);
	}

	@Test
	public void testGetVariables() throws Exception {
		CambriaErrorResponse testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVariables();
	}

	@Test
	public void testSetVariables() throws Exception {
		CambriaErrorResponse testSubject;
		List<String> variables = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setVariables(variables);
	}

	@Test
	public void testToString() throws Exception {
		CambriaErrorResponse testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}