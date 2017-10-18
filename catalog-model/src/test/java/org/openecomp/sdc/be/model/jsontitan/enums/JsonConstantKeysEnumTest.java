package org.openecomp.sdc.be.model.jsontitan.enums;

import javax.annotation.Generated;

import org.junit.Test;


public class JsonConstantKeysEnumTest {

	private JsonConstantKeysEnum createTestSubject() {
		return  JsonConstantKeysEnum.COMPOSITION;
	}

	
	@Test
	public void testGetValue() throws Exception {
		JsonConstantKeysEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValue();
	}
}