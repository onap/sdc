package org.openecomp.sdc.be.info;

import java.util.List;

import org.junit.Test;


public class ArtifactAccessListTest {

	private ArtifactAccessList createTestSubject() {
		return new ArtifactAccessList(null);
	}

	
	@Test
	public void testGetArtifacts() throws Exception {
		ArtifactAccessList testSubject;
		List<ArtifactAccessInfo> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifacts();
	}

	
	@Test
	public void testSetArtifacts() throws Exception {
		ArtifactAccessList testSubject;
		List<ArtifactAccessInfo> artifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifacts(artifacts);
	}
}