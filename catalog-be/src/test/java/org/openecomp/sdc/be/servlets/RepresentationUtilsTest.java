package org.openecomp.sdc.be.servlets;

import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Test;
import org.openecomp.sdc.be.model.ArtifactDefinition;

public class RepresentationUtilsTest {

	private RepresentationUtils createTestSubject() {
		return new RepresentationUtils();
	}

	
	@Test
	public void testConvertJsonToArtifactDefinitionForUpdate() throws Exception {
		String content = "";
		Class<ArtifactDefinition> clazz = null;
		ArtifactDefinition result;

		// default test
		result = RepresentationUtils.convertJsonToArtifactDefinitionForUpdate(content, clazz);
	}

	
	@Test
	public void testToRepresentation() throws Exception {
		T elementToRepresent = null;
		Object result;

		// default test
		result = RepresentationUtils.toRepresentation(elementToRepresent);
	}

	

	
	@Test
	public void testConvertJsonToArtifactDefinition() throws Exception {
		String content = "";
		Class<ArtifactDefinition> clazz = null;
		ArtifactDefinition result;

		// default test
		result = RepresentationUtils.convertJsonToArtifactDefinition(content, clazz);
	}
}