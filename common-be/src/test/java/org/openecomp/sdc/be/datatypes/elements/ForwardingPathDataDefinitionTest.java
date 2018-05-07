package org.openecomp.sdc.be.datatypes.elements;

import org.junit.Test;

public class ForwardingPathDataDefinitionTest {

	private ForwardingPathDataDefinition createTestSubject() {
		return new ForwardingPathDataDefinition();
	}

	@Test
	public void testConstructors() throws Exception {
		ForwardingPathDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		new ForwardingPathDataDefinition("mock");
		new ForwardingPathDataDefinition(testSubject);
	}
	
	@Test
	public void testGetName() throws Exception {
		ForwardingPathDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test
	public void testSetName() throws Exception {
		ForwardingPathDataDefinition testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	@Test
	public void testGetPathElements() throws Exception {
		ForwardingPathDataDefinition testSubject;
		ListDataDefinition<ForwardingPathElementDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPathElements();
	}

	@Test
	public void testSetPathElements() throws Exception {
		ForwardingPathDataDefinition testSubject;
		ListDataDefinition<ForwardingPathElementDataDefinition> pathElements = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setPathElements(pathElements);
	}

	@Test
	public void testGetUniqueId() throws Exception {
		ForwardingPathDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	@Test
	public void testSetUniqueId() throws Exception {
		ForwardingPathDataDefinition testSubject;
		String uid = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uid);
	}

	@Test
	public void testGetProtocol() throws Exception {
		ForwardingPathDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProtocol();
	}

	@Test
	public void testSetProtocol() throws Exception {
		ForwardingPathDataDefinition testSubject;
		String protocol = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setProtocol(protocol);
	}

	@Test
	public void testGetDestinationPortNumber() throws Exception {
		ForwardingPathDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDestinationPortNumber();
	}

	@Test
	public void testSetDestinationPortNumber() throws Exception {
		ForwardingPathDataDefinition testSubject;
		String destinationPortNumber = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDestinationPortNumber(destinationPortNumber);
	}

	@Test
	public void testGetDescription() throws Exception {
		ForwardingPathDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	@Test
	public void testSetDescription() throws Exception {
		ForwardingPathDataDefinition testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}

	@Test
	public void testGetToscaResourceName() throws Exception {
		ForwardingPathDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getToscaResourceName();
	}

	@Test
	public void testSetToscaResourceName() throws Exception {
		ForwardingPathDataDefinition testSubject;
		String toscaResourceName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setToscaResourceName(toscaResourceName);
	}
}