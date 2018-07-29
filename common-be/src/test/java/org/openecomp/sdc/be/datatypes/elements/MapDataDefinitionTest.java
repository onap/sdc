package org.openecomp.sdc.be.datatypes.elements;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

import java.util.HashSet;
import java.util.Map;

public class MapDataDefinitionTest {

	private MapDataDefinition createTestSubject() {
		return new MapDataDefinition();
	}
	
	@Test
	public void testCopyConstructor() throws Exception {
		new MapDataDefinition(createTestSubject());
	}
	
	@Test
	public void testDelete() throws Exception {
		MapDataDefinition testSubject;
		String key = "";

		// default test
		testSubject = createTestSubject();
		testSubject.delete(key);
	}

	@Test
	public void testSetOwnerIdIfEmpty() throws Exception {
		MapDataDefinition testSubject;
		String ownerId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setOwnerIdIfEmpty(ownerId);
	}

	@Test
	public void testFindKeyByItemUidMatch() throws Exception {
		MapDataDefinition testSubject;
		String uid = "";
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.findKeyByItemUidMatch(uid);
	}

	@Test
	public void testGetMapToscaDataDefinition() throws Exception {
		MapDataDefinition testSubject;
		Map<String, MapDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMapToscaDataDefinition();
	}

	@Test
	public void testPut() throws Exception {
		MapDataDefinition testSubject;
		String key = "";
		ToscaDataDefinition value = null;

		// default test
		testSubject = createTestSubject();
		testSubject.put(key, value);
	}

	@Test
	public void testFindByKey() throws Exception {
		MapDataDefinition testSubject;
		String key = "";
		ToscaDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.findByKey(key);
	}

	@Test
	public void testRemoveByOwnerId() throws Exception {
		MapDataDefinition testSubject;
		ToscaDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.removeByOwnerId(new HashSet<>());
	}

	@Test
	public void testUpdateIfExist() throws Exception {
		MapDataDefinition testSubject;
		ToscaDataDefinition other = null;
		boolean allowDefaultValueOverride = true;
		ToscaDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.updateIfExist(testSubject, allowDefaultValueOverride);
	}

	@Test
	public void testIsEmpty() throws Exception {
		MapDataDefinition testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isEmpty();
	}
}