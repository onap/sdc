package org.openecomp.sdc.asdctool.impl;

import org.junit.Test;

import java.nio.file.NoSuchFileException;

public class GraphJsonValidatorTest {

	private GraphJsonValidator createTestSubject() {
		return new GraphJsonValidator();
	}

	@Test
	public void testVerifyJanusGraphJson() throws Exception {
		GraphJsonValidator testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.verifyJanusGraphJson("src/test/resources/graph.json");
	}
	
	@Test
	public void testVerifyJanusGraphJsonErrorFile() throws Exception {
		GraphJsonValidator testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.verifyJanusGraphJson("src/test/resources/graphError.json");
	}
	
	@Test(expected=NoSuchFileException.class)
	public void testVerifyJanusGraphJsonNoFile() throws Exception {
		GraphJsonValidator testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.verifyJanusGraphJson("stam");
	}
}