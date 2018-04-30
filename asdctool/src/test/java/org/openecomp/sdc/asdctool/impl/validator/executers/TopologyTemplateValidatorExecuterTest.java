package org.openecomp.sdc.asdctool.impl.validator.executers;

import org.junit.Test;


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

	


	

}