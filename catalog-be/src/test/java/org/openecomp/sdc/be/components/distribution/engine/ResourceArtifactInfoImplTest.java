package org.openecomp.sdc.be.components.distribution.engine;

import org.junit.Test;

public class ResourceArtifactInfoImplTest {

	private ResourceArtifactInfoImpl createTestSubject() {
		return new ResourceArtifactInfoImpl();
	}

	@Test
	public void testGetResourceName() throws Exception {
		ResourceArtifactInfoImpl testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceName();
	}

	@Test
	public void testSetResourceName() throws Exception {
		ResourceArtifactInfoImpl testSubject;
		String resourceName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceName(resourceName);
	}

	@Test
	public void testGetResourceVersion() throws Exception {
		ResourceArtifactInfoImpl testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceVersion();
	}

	@Test
	public void testSetResourceVersion() throws Exception {
		ResourceArtifactInfoImpl testSubject;
		String resourceVersion = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceVersion(resourceVersion);
	}

	@Test
	public void testGetResourceUUID() throws Exception {
		ResourceArtifactInfoImpl testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceUUID();
	}

	@Test
	public void testSetResourceUUID() throws Exception {
		ResourceArtifactInfoImpl testSubject;
		String resourceUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceUUID(resourceUUID);
	}

	@Test
	public void testToString() throws Exception {
		ResourceArtifactInfoImpl testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}