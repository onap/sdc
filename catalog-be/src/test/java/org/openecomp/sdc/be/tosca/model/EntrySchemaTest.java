package org.openecomp.sdc.be.tosca.model;

import javax.annotation.Generated;

import org.junit.Test;


public class EntrySchemaTest {

	private EntrySchema createTestSubject() {
		return new EntrySchema();
	}

	
	@Test
	public void testGetType() throws Exception {
		EntrySchema testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	
	@Test
	public void testSetType() throws Exception {
		EntrySchema testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	
	@Test
	public void testGetDescription() throws Exception {
		EntrySchema testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	
	@Test
	public void testSetDescription() throws Exception {
		EntrySchema testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}
}