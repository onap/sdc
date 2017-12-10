package org.openecomp.sdc.common.datastructure;

import org.junit.Test;


public class WrapperTest {

	private Wrapper createTestSubject() {
		return new Wrapper(null);
	}



	
	@Test
	public void testIsEmpty() throws Exception {
		Wrapper testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isEmpty();
	}
}