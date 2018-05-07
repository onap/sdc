package org.openecomp.sdc.be.datatypes.enums;

import org.junit.Test;

public class NodeTypeEnumTest {

	private NodeTypeEnum createTestSubject() {
		return NodeTypeEnum.AdditionalInfoParameters;
	}

	@Test
	public void testGetName() throws Exception {
		NodeTypeEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test
	public void testGetByName() throws Exception {
		String name = "";
		NodeTypeEnum result;

		// default test
		result = NodeTypeEnum.getByName(name);
		result = NodeTypeEnum.getByName(NodeTypeEnum.AdditionalInfoParameters.getName());
	}

	@Test
	public void testGetByNameIgnoreCase() throws Exception {
		String name = "";
		NodeTypeEnum result;

		// default test
		result = NodeTypeEnum.getByNameIgnoreCase(name);
		result = NodeTypeEnum.getByNameIgnoreCase(NodeTypeEnum.AdditionalInfoParameters.getName());
	}
}