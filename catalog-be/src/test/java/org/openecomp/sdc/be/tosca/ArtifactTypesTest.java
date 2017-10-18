package org.openecomp.sdc.be.tosca;

import java.util.List;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.generator.data.ArtifactType;


public class ArtifactTypesTest {

	private ArtifactTypes createTestSubject() {
		return new ArtifactTypes();
	}

	
	@Test
	public void testGetArtifactTypes() throws Exception {
		ArtifactTypes testSubject;
		List<ArtifactType> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactTypes();
	}

	
	@Test
	public void testSetArtifactTypes() throws Exception {
		ArtifactTypes testSubject;
		List<ArtifactType> artifactTypes = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactTypes(artifactTypes);
	}
}