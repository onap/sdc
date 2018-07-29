package org.openecomp.sdc.be.info;

import org.junit.Test;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class NodeTypeInfoToUpdateArtifactsTest {

	private NodeTypeInfoToUpdateArtifacts createTestSubject() {
		return new NodeTypeInfoToUpdateArtifacts("", null);
	}

	@Test
	public void testGetNodeName() throws Exception {
		NodeTypeInfoToUpdateArtifacts testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNodeName();
	}

	@Test
	public void testSetNodeName() throws Exception {
		NodeTypeInfoToUpdateArtifacts testSubject;
		String nodeName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setNodeName(nodeName);
	}

	@Test
	public void testGetNodeTypesArtifactsToHandle() throws Exception {
		NodeTypeInfoToUpdateArtifacts testSubject;
		Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNodeTypesArtifactsToHandle();
	}

	@Test
	public void testSetNodeTypesArtifactsToHandle() throws Exception {
		NodeTypeInfoToUpdateArtifacts testSubject;
		Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setNodeTypesArtifactsToHandle(nodeTypesArtifactsToHandle);
	}
}