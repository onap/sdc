package org.openecomp.sdc.be.model.jsonjanusgraph.utils;

import org.junit.Test;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;

public class IdMapperTest {

	private IdMapper createTestSubject() {
		return new IdMapper();
	}

	
	@Test
	public void testMapComponentNameToUniqueId() throws Exception {
		IdMapper testSubject;
		String componentInstanceName = "";
		GraphVertex serviceVertex = null;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.mapComponentNameToUniqueId(componentInstanceName, serviceVertex);
	}

	
	@Test
	public void testMapUniqueIdToComponentNameTo() throws Exception {
		IdMapper testSubject;
		String compUniqueId = "";
		GraphVertex serviceVertex = null;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.mapUniqueIdToComponentNameTo(compUniqueId, serviceVertex);
	}
}