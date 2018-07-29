package org.openecomp.sdc.be.tosca.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class ToscaLifecycleOperationDefinitionTest {

	private ToscaLifecycleOperationDefinition createTestSubject() {
		return new ToscaLifecycleOperationDefinition();
	}

	@Test
	public void testGetImplementation() throws Exception {
		ToscaLifecycleOperationDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getImplementation();
	}

	@Test
	public void testSetImplementation() throws Exception {
		ToscaLifecycleOperationDefinition testSubject;
		String implementation = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setImplementation(implementation);
	}

	@Test
	public void testGetInputs() throws Exception {
		ToscaLifecycleOperationDefinition testSubject;
		Map<String, ToscaProperty> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInputs();
	}

	@Test
	public void testSetInputs() throws Exception {
		ToscaLifecycleOperationDefinition testSubject;
		Map<String, ToscaProperty> inputs = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInputs(inputs);
	}

	@Test
	public void testEquals() throws Exception {
		ToscaLifecycleOperationDefinition testSubject;
		Object o = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		o = null;
		result = testSubject.equals(o);
		Assert.assertEquals(false, result);
		result = testSubject.equals(testSubject);
		Assert.assertEquals(true, result);
		result = testSubject.equals(createTestSubject());
		Assert.assertEquals(true, result);
	}

	@Test
	public void testHashCode() throws Exception {
		ToscaLifecycleOperationDefinition testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	@Test
	public void testGetDescription() throws Exception {
		ToscaLifecycleOperationDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	@Test
	public void testSetDescription() throws Exception {
		ToscaLifecycleOperationDefinition testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}
}