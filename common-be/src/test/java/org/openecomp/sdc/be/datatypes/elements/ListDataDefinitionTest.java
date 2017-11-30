package org.openecomp.sdc.be.datatypes.elements;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;


public class ListDataDefinitionTest {

	private ListDataDefinition createTestSubject() {
		List list = new ArrayList<>();
		return new ListDataDefinition(list);
	}

	
	


	
	@Test
	public void testSetOwnerIdIfEmpty() throws Exception {
		ListDataDefinition testSubject;
		String ownerId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setOwnerIdIfEmpty(ownerId);
	}

	


	
	@Test
	public void testFindUidMatch() throws Exception {
		ListDataDefinition testSubject;
		String uid = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.findUidMatch(uid);
	}
}