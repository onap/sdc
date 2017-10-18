package org.openecomp.sdc.be.model.jsontitan.datamodel;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;


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