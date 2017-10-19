package org.openecomp.sdc.asdctool.impl.validator.executers;

import java.util.List;

import org.junit.Test;
import org.openecomp.sdc.asdctool.impl.validator.tasks.TopologyTemplateValidationTask;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;


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