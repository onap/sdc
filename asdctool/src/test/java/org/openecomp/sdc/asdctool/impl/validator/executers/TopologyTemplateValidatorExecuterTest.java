package org.openecomp.sdc.asdctool.impl.validator.executers;

import org.junit.Test;


public class TopologyTemplateValidatorExecuterTest {

	private TopologyTemplateValidatorExecuter createTestSubject() {
		return new TopologyTemplateValidatorExecuter();
	}

	
	@Test
	public void testSetName() throws Exception {
		TopologyTemplateValidatorExecuter testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	
	@Test
	public void testGetName() throws Exception {
		TopologyTemplateValidatorExecuter testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	


	

}