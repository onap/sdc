package org.openecomp.sdc.be.resources.data.category;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.category.GroupingDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

import java.util.HashMap;
import java.util.Map;

public class GroupingDataTest {

	private GroupingData createTestSubject() {
		return new GroupingData(NodeTypeEnum.AdditionalInfoParameters);
	}

	@Test
	public void testCtor() throws Exception {
		new GroupingData(new HashMap<>());
		new GroupingData(NodeTypeEnum.AdditionalInfoParameters, new GroupingDataDefinition());
	}
	
	@Test
	public void testGetGroupingDataDefinition() throws Exception {
		GroupingData testSubject;
		GroupingDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGroupingDataDefinition();
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		GroupingData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		GroupingData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}
}