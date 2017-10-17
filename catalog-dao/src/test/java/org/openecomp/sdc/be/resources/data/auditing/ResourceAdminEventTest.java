package org.openecomp.sdc.be.resources.data.auditing;

import java.util.Date;
import java.util.UUID;

import javax.annotation.Generated;

import org.junit.Test;


public class ResourceAdminEventTest {

	private ResourceAdminEvent createTestSubject() {
		return new ResourceAdminEvent();
	}

	
	@Test
	public void testFillFields() throws Exception {
		ResourceAdminEvent testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.fillFields();
	}

	
	@Test
	public void testGetResourceName() throws Exception {
		ResourceAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceName();
	}

	
	@Test
	public void testSetResourceName() throws Exception {
		ResourceAdminEvent testSubject;
		String resourceName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceName(resourceName);
	}

	
	@Test
	public void testGetResourceType() throws Exception {
		ResourceAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceType();
	}

	
	@Test
	public void testSetResourceType() throws Exception {
		ResourceAdminEvent testSubject;
		String resourceType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceType(resourceType);
	}

	
	@Test
	public void testGetPrevVersion() throws Exception {
		ResourceAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPrevVersion();
	}

	
	@Test
	public void testSetPrevVersion() throws Exception {
		ResourceAdminEvent testSubject;
		String prevVersion = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setPrevVersion(prevVersion);
	}

	
	@Test
	public void testGetCurrVersion() throws Exception {
		ResourceAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCurrVersion();
	}

	
	@Test
	public void testSetCurrVersion() throws Exception {
		ResourceAdminEvent testSubject;
		String currVersion = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCurrVersion(currVersion);
	}

	
	@Test
	public void testGetPrevState() throws Exception {
		ResourceAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPrevState();
	}

	
	@Test
	public void testSetPrevState() throws Exception {
		ResourceAdminEvent testSubject;
		String prevState = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setPrevState(prevState);
	}

	
	@Test
	public void testGetCurrState() throws Exception {
		ResourceAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCurrState();
	}

	
	@Test
	public void testSetCurrState() throws Exception {
		ResourceAdminEvent testSubject;
		String currState = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCurrState(currState);
	}

	
	@Test
	public void testGetTimebaseduuid() throws Exception {
		ResourceAdminEvent testSubject;
		UUID result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimebaseduuid();
	}

	
	@Test
	public void testSetTimebaseduuid() throws Exception {
		ResourceAdminEvent testSubject;
		UUID timebaseduuid = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimebaseduuid(timebaseduuid);
	}

	
	@Test
	public void testGetTimestamp1() throws Exception {
		ResourceAdminEvent testSubject;
		Date result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimestamp1();
	}

	
	@Test
	public void testSetTimestamp1() throws Exception {
		ResourceAdminEvent testSubject;
		Date timestamp1 = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimestamp1(timestamp1);
	}

	
	@Test
	public void testGetAction() throws Exception {
		ResourceAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAction();
	}

	
	@Test
	public void testSetAction() throws Exception {
		ResourceAdminEvent testSubject;
		String action = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setAction(action);
	}

	
	@Test
	public void testGetRequestId() throws Exception {
		ResourceAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequestId();
	}

	
	@Test
	public void testSetRequestId() throws Exception {
		ResourceAdminEvent testSubject;
		String requestId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRequestId(requestId);
	}

	
	@Test
	public void testGetServiceInstanceId() throws Exception {
		ResourceAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceInstanceId();
	}

	
	@Test
	public void testSetServiceInstanceId() throws Exception {
		ResourceAdminEvent testSubject;
		String serviceInstanceId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceInstanceId(serviceInstanceId);
	}

	
	@Test
	public void testGetStatus() throws Exception {
		ResourceAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}

	
	@Test
	public void testSetStatus() throws Exception {
		ResourceAdminEvent testSubject;
		String status = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setStatus(status);
	}

	
	@Test
	public void testGetDesc() throws Exception {
		ResourceAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDesc();
	}

	
	@Test
	public void testSetDesc() throws Exception {
		ResourceAdminEvent testSubject;
		String desc = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDesc(desc);
	}

	
	@Test
	public void testGetModifier() throws Exception {
		ResourceAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getModifier();
	}

	
	@Test
	public void testSetModifier() throws Exception {
		ResourceAdminEvent testSubject;
		String modifier = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setModifier(modifier);
	}

	
	@Test
	public void testGetPrevArtifactUUID() throws Exception {
		ResourceAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPrevArtifactUUID();
	}

	
	@Test
	public void testSetPrevArtifactUUID() throws Exception {
		ResourceAdminEvent testSubject;
		String prevArtifactUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setPrevArtifactUUID(prevArtifactUUID);
	}

	
	@Test
	public void testGetCurrArtifactUUID() throws Exception {
		ResourceAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCurrArtifactUUID();
	}

	
	@Test
	public void testSetCurrArtifactUUID() throws Exception {
		ResourceAdminEvent testSubject;
		String currArtifactUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCurrArtifactUUID(currArtifactUUID);
	}

	
	@Test
	public void testGetArtifactData() throws Exception {
		ResourceAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactData();
	}

	
	@Test
	public void testSetArtifactData() throws Exception {
		ResourceAdminEvent testSubject;
		String artifactData = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactData(artifactData);
	}

	
	@Test
	public void testGetDid() throws Exception {
		ResourceAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDid();
	}

	
	@Test
	public void testSetDid() throws Exception {
		ResourceAdminEvent testSubject;
		String did = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDid(did);
	}

	
	@Test
	public void testGetDprevStatus() throws Exception {
		ResourceAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDprevStatus();
	}

	
	@Test
	public void testSetDprevStatus() throws Exception {
		ResourceAdminEvent testSubject;
		String dprevStatus = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDprevStatus(dprevStatus);
	}

	
	@Test
	public void testGetDcurrStatus() throws Exception {
		ResourceAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDcurrStatus();
	}

	
	@Test
	public void testSetDcurrStatus() throws Exception {
		ResourceAdminEvent testSubject;
		String dcurrStatus = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDcurrStatus(dcurrStatus);
	}

	
	@Test
	public void testGetToscaNodeType() throws Exception {
		ResourceAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getToscaNodeType();
	}

	
	@Test
	public void testSetToscaNodeType() throws Exception {
		ResourceAdminEvent testSubject;
		String toscaNodeType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setToscaNodeType(toscaNodeType);
	}

	
	@Test
	public void testGetComment() throws Exception {
		ResourceAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComment();
	}

	
	@Test
	public void testSetComment() throws Exception {
		ResourceAdminEvent testSubject;
		String comment = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setComment(comment);
	}

	
	@Test
	public void testGetInvariantUUID() throws Exception {
		ResourceAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInvariantUUID();
	}

	
	@Test
	public void testSetInvariantUUID() throws Exception {
		ResourceAdminEvent testSubject;
		String invariantUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setInvariantUUID(invariantUUID);
	}

	
	@Test
	public void testToString() throws Exception {
		ResourceAdminEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}