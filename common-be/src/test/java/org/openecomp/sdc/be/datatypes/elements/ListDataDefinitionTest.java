package org.openecomp.sdc.be.datatypes.elements;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

import java.util.List;
import java.util.Set;

public class ListDataDefinitionTest {

	private ListDataDefinition createTestSubject() {
		return new ListDataDefinition<AdditionalInfoParameterDataDefinition>();
	}
	
	@Test
	public void testCopyConstructor() throws Exception {
		ListDataDefinition testSubject;
		String ownerId = "";

		// default test
		testSubject = createTestSubject();
		new ListDataDefinition<AdditionalInfoParameterDataDefinition>(testSubject);
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

	@Test
	public void testGetListToscaDataDefinition() throws Exception {
		ListDataDefinition testSubject;
		List result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getListToscaDataDefinition();
	}

	@Test
	public void testAdd() throws Exception {
		ListDataDefinition testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.add(new AdditionalInfoParameterDataDefinition());
	}

	@Test
	public void testDelete() throws Exception {
		ListDataDefinition testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.delete(new AdditionalInfoParameterDataDefinition());
	}

	@Test
	public void testMergeFunction() throws Exception {
		ListDataDefinition testSubject;
		boolean allowDefaultValueOverride = false;

		// default test
		testSubject = createTestSubject();
		ToscaDataDefinition result = testSubject.mergeFunction(testSubject, allowDefaultValueOverride);
	}

	@Test
	public void testRemoveByOwnerId() throws Exception {
		ListDataDefinition testSubject;
		Set<String> ownerIdList = null;

		// default test
		testSubject = createTestSubject();
		ToscaDataDefinition result = testSubject.removeByOwnerId(ownerIdList);
	}

	@Test
	public void testUpdateIfExist() throws Exception {
		ListDataDefinition testSubject;
		boolean allowDefaultValueOverride = false;

		// default test
		testSubject = createTestSubject();
		ToscaDataDefinition result = testSubject.updateIfExist(testSubject, true);
	}

	@Test
	public void testIsEmpty() throws Exception {
		ListDataDefinition testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isEmpty();
	}
}