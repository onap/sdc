package org.openecomp.sdc.be.model.jsontitan.datamodel;

import org.junit.Test;


public class ToscaElementTypeEnumTest {

	private ToscaElementTypeEnum createTestSubject() {
		return  ToscaElementTypeEnum.TopologyTemplate;
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