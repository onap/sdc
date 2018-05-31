package org.openecomp.sdc.be.resources.data;

import java.util.Map;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public class UniqueIdDataTest {

	private UniqueIdData createTestSubject() {
		return new UniqueIdData(NodeTypeEnum.AdditionalInfoParameters, "mock");
	}

	@Test
	public void testGetUniqueId() throws Exception {
		UniqueIdData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	@Test
	public void testToGraphMap() throws Exception {
		UniqueIdData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}
}