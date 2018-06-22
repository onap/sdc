package org.openecomp.sdc.be.model;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;

public class OperationTest {

	private Operation createTestSubject() {
		return new Operation();
	}

	@Test
	public void testCtor() throws Exception {
		new Operation(new OperationDataDefinition());
		new Operation(new ArtifactDataDefinition(), "mock", new ListDataDefinition<>(), new ListDataDefinition<>());
	}
	
	@Test
	public void testIsDefinition() throws Exception {
		Operation testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isDefinition();
	}

	@Test
	public void testSetDefinition() throws Exception {
		Operation testSubject;
		boolean definition = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setDefinition(definition);
	}

	@Test
	public void testToString() throws Exception {
		Operation testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	@Test
	public void testGetImplementationArtifact() throws Exception {
		Operation testSubject;
		ArtifactDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getImplementationArtifact();
		testSubject.setImplementation(new ArtifactDataDefinition());
		result = testSubject.getImplementationArtifact();
	}
}