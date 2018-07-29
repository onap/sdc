package org.openecomp.sdc.asdctool.impl;

import org.junit.Test;

import java.nio.file.NoSuchFileException;

public class GraphJsonValidatorTest {

	private GraphJsonValidator createTestSubject() {
		return new GraphJsonValidator();
	}

	@Test
	public void testVerifyTitanJson() throws Exception {
		GraphJsonValidator testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.verifyTitanJson("src/test/resources/graph.json");
	}
	
	@Test
	public void testVerifyTitanJsonErrorFile() throws Exception {
		GraphJsonValidator testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.verifyTitanJson("src/test/resources/graphError.json");
	}
	
	@Test(expected=NoSuchFileException.class)
	public void testVerifyTitanJsonNoFile() throws Exception {
		GraphJsonValidator testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.verifyTitanJson("stam");
	}
}