package org.openecomp.sdc.be.datatypes.elements;

import java.util.List;

import org.junit.Test;


public class ProductMetadataDataDefinitionTest {

	private ProductMetadataDataDefinition createTestSubject() {
		return new ProductMetadataDataDefinition();
	}

	
	@Test
	public void testGetIsActive() throws Exception {
		ProductMetadataDataDefinition testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getIsActive();
	}

	
	@Test
	public void testSetIsActive() throws Exception {
		ProductMetadataDataDefinition testSubject;
		Boolean active = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setIsActive(active);
	}

	
	@Test
	public void testGetContacts() throws Exception {
		ProductMetadataDataDefinition testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getContacts();
	}

	
	@Test
	public void testSetContacts() throws Exception {
		ProductMetadataDataDefinition testSubject;
		List<String> contacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setContacts(contacts);
	}

	
	@Test
	public void testAddContact() throws Exception {
		ProductMetadataDataDefinition testSubject;
		String contact = "";

		// test 1
		testSubject = createTestSubject();
		contact = null;
		testSubject.addContact(contact);

		// test 2
		testSubject = createTestSubject();
		contact = "";
		testSubject.addContact(contact);
	}

	
	@Test
	public void testGetFullName() throws Exception {
		ProductMetadataDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getFullName();
	}

	
	@Test
	public void testSetFullName() throws Exception {
		ProductMetadataDataDefinition testSubject;
		String fullName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setFullName(fullName);
	}

	
	@Test
	public void testToString() throws Exception {
		ProductMetadataDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testHashCode() throws Exception {
		ProductMetadataDataDefinition testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	
	@Test
	public void testEquals() throws Exception {
		ProductMetadataDataDefinition testSubject;
		Object obj = null;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.equals(obj);
	}
}