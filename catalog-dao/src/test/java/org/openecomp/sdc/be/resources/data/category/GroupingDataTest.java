package org.openecomp.sdc.be.resources.data.category;

import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.category.GroupingDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public class GroupingDataTest {

	private GroupingData createTestSubject() {
		return new GroupingData(NodeTypeEnum.AdditionalInfoParameters);
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