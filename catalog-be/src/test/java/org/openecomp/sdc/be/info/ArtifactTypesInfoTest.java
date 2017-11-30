package org.openecomp.sdc.be.info;

import java.util.List;

import org.junit.Test;
import org.openecomp.sdc.be.model.ArtifactType;


public class ArtifactTypesInfoTest {

	private ArtifactTypesInfo createTestSubject() {
		return new ArtifactTypesInfo();
	}

	
	@Test
	public void testGetArtifactTypes() throws Exception {
		ArtifactTypesInfo testSubject;
		List<ArtifactType> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactTypes();
	}

	
	@Test
	public void testSetArtifactTypes() throws Exception {
		ArtifactTypesInfo testSubject;
		List<ArtifactType> artifactTypes = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactTypes(artifactTypes);
	}

	
	@Test
	public void testGetHeatDefaultTimeout() throws Exception {
		ArtifactTypesInfo testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getHeatDefaultTimeout();
	}

	
	@Test
	public void testSetHeatDefaultTimeout() throws Exception {
		ArtifactTypesInfo testSubject;
		Integer heatDefaultTimeout = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setHeatDefaultTimeout(heatDefaultTimeout);
	}
}