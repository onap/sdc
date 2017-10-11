package org.openecomp.sdc.be.model;

import java.util.List;

import javax.annotation.Generated;

import org.junit.Test;


public class ArtifactDefinitionTest {

	private ArtifactDefinition createTestSubject() {
		return new ArtifactDefinition();
	}

	
	@Test
	public void testGetPayloadData() throws Exception {
		ArtifactDefinition testSubject;
		byte[] result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPayloadData();
	}

	
	@Test
	public void testSetPayload() throws Exception {
		ArtifactDefinition testSubject;
		byte[] payloadData = new byte[] { ' ' };

		// default test
		testSubject = createTestSubject();
		testSubject.setPayload(payloadData);
	}

	
	@Test
	public void testSetPayloadData() throws Exception {
		ArtifactDefinition testSubject;
		String payloadData = "";

		// test 1
		testSubject = createTestSubject();
		payloadData = null;
		testSubject.setPayloadData(payloadData);

		// test 2
		testSubject = createTestSubject();
		payloadData = "";
		testSubject.setPayloadData(payloadData);
	}

	
	@Test
	public void testGetListHeatParameters() throws Exception {
		ArtifactDefinition testSubject;
		List<HeatParameterDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getListHeatParameters();
	}

	
	@Test
	public void testSetListHeatParameters() throws Exception {
		ArtifactDefinition testSubject;
		List<HeatParameterDefinition> properties = null;

		// test 1
		testSubject = createTestSubject();
		properties = null;
		testSubject.setListHeatParameters(properties);
	}

	
	@Test
	public void testCheckEsIdExist() throws Exception {
		ArtifactDefinition testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.checkEsIdExist();
	}

	
	@Test
	public void testHashCode() throws Exception {
		ArtifactDefinition testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	
	@Test
	public void testEquals() throws Exception {
		ArtifactDefinition testSubject;
		Object obj = null;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.equals(obj);
	}
}