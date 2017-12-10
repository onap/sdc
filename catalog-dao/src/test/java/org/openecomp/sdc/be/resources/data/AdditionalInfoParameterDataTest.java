package org.openecomp.sdc.be.resources.data;

import java.util.Map;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterDataDefinition;


public class AdditionalInfoParameterDataTest {

	private AdditionalInfoParameterData createTestSubject() {
		return new AdditionalInfoParameterData();
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		AdditionalInfoParameterData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		AdditionalInfoParameterData testSubject;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testGetAdditionalInfoParameterDataDefinition() throws Exception {
		AdditionalInfoParameterData testSubject;
		AdditionalInfoParameterDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAdditionalInfoParameterDataDefinition();
	}

	
	@Test
	public void testSetAdditionalInfoParameterDataDefinition() throws Exception {
		AdditionalInfoParameterData testSubject;
		AdditionalInfoParameterDataDefinition additionalInfoParameterDataDefinition = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setAdditionalInfoParameterDataDefinition(additionalInfoParameterDataDefinition);
	}

	
	@Test
	public void testGetParameters() throws Exception {
		AdditionalInfoParameterData testSubject;
		Map<String, String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getParameters();
	}

	
	@Test
	public void testSetParameters() throws Exception {
		AdditionalInfoParameterData testSubject;
		Map<String, String> parameters = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setParameters(parameters);
	}

	
	@Test
	public void testGetIdToKey() throws Exception {
		AdditionalInfoParameterData testSubject;
		Map<String, String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getIdToKey();
	}

	
	@Test
	public void testSetIdToKey() throws Exception {
		AdditionalInfoParameterData testSubject;
		Map<String, String> idToKey = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setIdToKey(idToKey);
	}

	
	@Test
	public void testToString() throws Exception {
		AdditionalInfoParameterData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}