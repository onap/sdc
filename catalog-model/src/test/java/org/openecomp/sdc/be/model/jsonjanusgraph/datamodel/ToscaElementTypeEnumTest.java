package org.openecomp.sdc.be.model.jsonjanusgraph.datamodel;

import org.junit.Test;


public class ToscaElementTypeEnumTest {

	private ToscaElementTypeEnum createTestSubject() {
		return  ToscaElementTypeEnum.TOPOLOGY_TEMPLATE;
	}

	


	
	@Test
	public void testGetValue() throws Exception {
		ToscaElementTypeEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValue();
	}
}