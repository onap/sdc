package org.openecomp.sdc.be.datatypes.elements;

import java.util.List;

import javax.annotation.Generated;

import org.junit.Test;


public class AdditionalInfoParameterDataDefinitionTest {

	private AdditionalInfoParameterDataDefinition createTestSubject() {
		return new AdditionalInfoParameterDataDefinition();
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		AdditionalInfoParameterDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testSetUniqueId() throws Exception {
		AdditionalInfoParameterDataDefinition testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	
	@Test
	public void testGetCreationTime() throws Exception {
		AdditionalInfoParameterDataDefinition testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCreationTime();
	}

	
	@Test
	public void testSetCreationTime() throws Exception {
		AdditionalInfoParameterDataDefinition testSubject;
		Long creationTime = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCreationTime(creationTime);
	}

	
	@Test
	public void testGetModificationTime() throws Exception {
		AdditionalInfoParameterDataDefinition testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getModificationTime();
	}

	
	@Test
	public void testSetModificationTime() throws Exception {
		AdditionalInfoParameterDataDefinition testSubject;
		Long modificationTime = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setModificationTime(modificationTime);
	}

	
	@Test
	public void testGetLastCreatedCounter() throws Exception {
		AdditionalInfoParameterDataDefinition testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLastCreatedCounter();
	}

	
	@Test
	public void testSetLastCreatedCounter() throws Exception {
		AdditionalInfoParameterDataDefinition testSubject;
		Integer lastCreatedCounter = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setLastCreatedCounter(lastCreatedCounter);
	}

	
	@Test
	public void testGetParameters() throws Exception {
		AdditionalInfoParameterDataDefinition testSubject;
		List<AdditionalInfoParameterInfo> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getParameters();
	}

	
	@Test
	public void testSetParameters() throws Exception {
		AdditionalInfoParameterDataDefinition testSubject;
		List<AdditionalInfoParameterInfo> parameters = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setParameters(parameters);
	}

	
	@Test
	public void testToString() throws Exception {
		AdditionalInfoParameterDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}