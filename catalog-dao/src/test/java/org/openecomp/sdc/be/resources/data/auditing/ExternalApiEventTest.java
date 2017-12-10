package org.openecomp.sdc.be.resources.data.auditing;

import java.util.Date;
import java.util.UUID;

import org.junit.Test;


public class ExternalApiEventTest {

	private ExternalApiEvent createTestSubject() {
		return new ExternalApiEvent();
	}

	
	@Test
	public void testFillFields() throws Exception {
		ExternalApiEvent testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.fillFields();
	}

	
	@Test
	public void testGetTimebaseduuid() throws Exception {
		ExternalApiEvent testSubject;
		UUID result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimebaseduuid();
	}

	
	@Test
	public void testSetTimebaseduuid() throws Exception {
		ExternalApiEvent testSubject;
		UUID timebaseduuid = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimebaseduuid(timebaseduuid);
	}

	
	@Test
	public void testGetTimestamp1() throws Exception {
		ExternalApiEvent testSubject;
		Date result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimestamp1();
	}

	
	@Test
	public void testSetTimestamp1() throws Exception {
		ExternalApiEvent testSubject;
		Date timestamp1 = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimestamp1(timestamp1);
	}

	
	@Test
	public void testGetAction() throws Exception {
		ExternalApiEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAction();
	}

	
	@Test
	public void testSetAction() throws Exception {
		ExternalApiEvent testSubject;
		String action = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setAction(action);
	}

	
	@Test
	public void testGetStatus() throws Exception {
		ExternalApiEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}

	
	@Test
	public void testSetStatus() throws Exception {
		ExternalApiEvent testSubject;
		String status = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setStatus(status);
	}

	
	@Test
	public void testGetDesc() throws Exception {
		ExternalApiEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDesc();
	}

	
	@Test
	public void testSetDesc() throws Exception {
		ExternalApiEvent testSubject;
		String desc = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDesc(desc);
	}

	
	@Test
	public void testGetConsumerId() throws Exception {
		ExternalApiEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConsumerId();
	}

	
	@Test
	public void testSetConsumerId() throws Exception {
		ExternalApiEvent testSubject;
		String consumerId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setConsumerId(consumerId);
	}

	
	@Test
	public void testGetResourceURL() throws Exception {
		ExternalApiEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceURL();
	}

	
	@Test
	public void testSetResourceURL() throws Exception {
		ExternalApiEvent testSubject;
		String resourceURL = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceURL(resourceURL);
	}

	
	@Test
	public void testGetResourceName() throws Exception {
		ExternalApiEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceName();
	}

	
	@Test
	public void testSetResourceName() throws Exception {
		ExternalApiEvent testSubject;
		String resourceName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceName(resourceName);
	}

	
	@Test
	public void testGetResourceType() throws Exception {
		ExternalApiEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceType();
	}

	
	@Test
	public void testSetResourceType() throws Exception {
		ExternalApiEvent testSubject;
		String resourceType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceType(resourceType);
	}

	
	@Test
	public void testGetServiceInstanceId() throws Exception {
		ExternalApiEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceInstanceId();
	}

	
	@Test
	public void testSetServiceInstanceId() throws Exception {
		ExternalApiEvent testSubject;
		String serviceInstanceId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceInstanceId(serviceInstanceId);
	}

	
	@Test
	public void testGetInvariantUuid() throws Exception {
		ExternalApiEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInvariantUuid();
	}

	
	@Test
	public void testSetInvariantUuid() throws Exception {
		ExternalApiEvent testSubject;
		String invariantUuid = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setInvariantUuid(invariantUuid);
	}

	
	@Test
	public void testGetModifier() throws Exception {
		ExternalApiEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getModifier();
	}

	
	@Test
	public void testSetModifier() throws Exception {
		ExternalApiEvent testSubject;
		String modifier = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setModifier(modifier);
	}

	
	@Test
	public void testGetPrevArtifactUuid() throws Exception {
		ExternalApiEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPrevArtifactUuid();
	}

	
	@Test
	public void testSetPrevArtifactUuid() throws Exception {
		ExternalApiEvent testSubject;
		String prevArtifactUuid = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setPrevArtifactUuid(prevArtifactUuid);
	}

	
	@Test
	public void testGetCurrArtifactUuid() throws Exception {
		ExternalApiEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCurrArtifactUuid();
	}

	
	@Test
	public void testSetCurrArtifactUuid() throws Exception {
		ExternalApiEvent testSubject;
		String currArtifactUuid = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCurrArtifactUuid(currArtifactUuid);
	}

	
	@Test
	public void testGetArtifactData() throws Exception {
		ExternalApiEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactData();
	}

	
	@Test
	public void testSetArtifactData() throws Exception {
		ExternalApiEvent testSubject;
		String artifactData = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactData(artifactData);
	}

	
	@Test
	public void testToString() throws Exception {
		ExternalApiEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testGetPrevVersion() throws Exception {
		ExternalApiEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPrevVersion();
	}

	
	@Test
	public void testSetPrevVersion() throws Exception {
		ExternalApiEvent testSubject;
		String prevVersion = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setPrevVersion(prevVersion);
	}

	
	@Test
	public void testGetCurrVersion() throws Exception {
		ExternalApiEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCurrVersion();
	}

	
	@Test
	public void testSetCurrVersion() throws Exception {
		ExternalApiEvent testSubject;
		String currVersion = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCurrVersion(currVersion);
	}

	
	@Test
	public void testGetPrevState() throws Exception {
		ExternalApiEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPrevState();
	}

	
	@Test
	public void testSetPrevState() throws Exception {
		ExternalApiEvent testSubject;
		String prevState = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setPrevState(prevState);
	}

	
	@Test
	public void testGetCurrState() throws Exception {
		ExternalApiEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCurrState();
	}

	
	@Test
	public void testSetCurrState() throws Exception {
		ExternalApiEvent testSubject;
		String currState = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCurrState(currState);
	}
}