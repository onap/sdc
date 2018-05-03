package org.openecomp.sdc.asdctool.impl.validator.executers;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;

public class TopologyTemplateValidatorExecuterTest {

	private TopologyTemplateValidatorExecuter createTestSubject() {
		return new TopologyTemplateValidatorExecuter();
	}

	@Test
	public void testSetName() {
		TopologyTemplateValidatorExecuter testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	@Test
	public void testGetName() {
		TopologyTemplateValidatorExecuter testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test(expected=NullPointerException.class)
	public void testGetVerticesToValidate() throws Exception {
		TopologyTemplateValidatorExecuter testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.getVerticesToValidate(ComponentTypeEnum.PRODUCT);
	}
}