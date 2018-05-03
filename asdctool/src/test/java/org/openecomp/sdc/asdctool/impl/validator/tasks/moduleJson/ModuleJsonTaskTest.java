package org.openecomp.sdc.asdctool.impl.validator.tasks.moduleJson;

import org.junit.Test;
import org.openecomp.sdc.asdctool.impl.validator.utils.VertexResult;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;

public class ModuleJsonTaskTest {

	private ModuleJsonTask createTestSubject() {
		return new ModuleJsonTask();
	}

	@Test(expected=NullPointerException.class)
	public void testValidate() throws Exception {
		ModuleJsonTask testSubject;
		GraphVertex vertex = null;
		VertexResult result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.validate(vertex);
	}
}