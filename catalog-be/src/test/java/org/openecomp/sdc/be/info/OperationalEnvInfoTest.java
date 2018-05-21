package org.openecomp.sdc.be.info;

import org.junit.Test;

public class OperationalEnvInfoTest {

	private OperationalEnvInfo createTestSubject() {
		return new OperationalEnvInfo();
	}

	@Test
	public void testGetOperationalEnvId() throws Exception {
		OperationalEnvInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOperationalEnvId();
	}

	@Test
	public void testSetOperationalEnvId() throws Exception {
		OperationalEnvInfo testSubject;
		String operationalEnvId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setOperationalEnvId(operationalEnvId);
	}

	@Test
	public void testGetOperationalEnvName() throws Exception {
		OperationalEnvInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOperationalEnvName();
	}

	@Test
	public void testSetOperationalEnvName() throws Exception {
		OperationalEnvInfo testSubject;
		String operationalEnvName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setOperationalEnvName(operationalEnvName);
	}

	@Test
	public void testGetOperationalEnvType() throws Exception {
		OperationalEnvInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOperationalEnvType();
	}

	@Test
	public void testSetOperationalEnvType() throws Exception {
		OperationalEnvInfo testSubject;
		String operationalEnvType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setOperationalEnvType(operationalEnvType);
	}

	@Test
	public void testGetOperationalEnvStatus() throws Exception {
		OperationalEnvInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOperationalEnvStatus();
	}

	@Test
	public void testSetOperationalEnvStatus() throws Exception {
		OperationalEnvInfo testSubject;
		String operationalEnvStatus = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setOperationalEnvStatus(operationalEnvStatus);
	}

	@Test
	public void testGetTenantContext() throws Exception {
		OperationalEnvInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTenantContext();
	}

	@Test
	public void testSetTenantContext() throws Exception {
		OperationalEnvInfo testSubject;
		String tenantContext = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setTenantContext(tenantContext);
	}

	@Test
	public void testGetWorkloadContext() throws Exception {
		OperationalEnvInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getWorkloadContext();
	}

	@Test
	public void testSetWorkloadContext() throws Exception {
		OperationalEnvInfo testSubject;
		String workloadContext = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setWorkloadContext(workloadContext);
	}

	@Test
	public void testGetResourceVersion() throws Exception {
		OperationalEnvInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceVersion();
	}

	@Test
	public void testSetResourceVersion() throws Exception {
		OperationalEnvInfo testSubject;
		String resourceVersion = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceVersion(resourceVersion);
	}

	@Test
	public void testGetRelationships() throws Exception {
		OperationalEnvInfo testSubject;
		RelationshipList result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRelationships();
	}

	@Test
	public void testSetRelationships() throws Exception {
		OperationalEnvInfo testSubject;
		RelationshipList relationships = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRelationships(relationships);
	}

	@Test
	public void testToString() throws Exception {
		OperationalEnvInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	@Test
	public void testCreateFromJson() throws Exception {
		String json = "{}";
		OperationalEnvInfo result;

		// default test
		result = OperationalEnvInfo.createFromJson(json);
	}
}