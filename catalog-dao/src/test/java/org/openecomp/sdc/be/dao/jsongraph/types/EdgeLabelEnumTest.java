package org.openecomp.sdc.be.dao.jsongraph.types;

import org.junit.Test;


public class EdgeLabelEnumTest {

	private EdgeLabelEnum createTestSubject() {
		return EdgeLabelEnum.ARTIFACTS;
	}

	
	@Test
	public void testGetEdgeLabelEnum() throws Exception {
		String name = "";
		EdgeLabelEnum result;

		// default test
		result = EdgeLabelEnum.getEdgeLabelEnum(name);
	}

	
	@Test
	public void testIsInstanceArtifactsLabel() throws Exception {
		EdgeLabelEnum testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isInstanceArtifactsLabel();
	}
}