package org.openecomp.sdc.be.datatypes.elements;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;


public class MapDataDefinitionTest {

	private MapDataDefinition createTestSubject() {
		Map myMap = new HashMap<>();
		return new MapDataDefinition(myMap);
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
}