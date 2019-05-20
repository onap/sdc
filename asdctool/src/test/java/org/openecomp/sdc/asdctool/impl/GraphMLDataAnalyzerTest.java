package org.openecomp.sdc.asdctool.impl;

import org.junit.Test;

public class GraphMLDataAnalyzerTest {

	private GraphMLDataAnalyzer createTestSubject() {
		return new GraphMLDataAnalyzer();
	}

	@Test
	public void testAnalyzeGraphMLData() throws Exception {
		GraphMLDataAnalyzer testSubject;
		String[] args = new String[] { "export", "src/main/resources/config/janusgraph.properties", "./" };
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.analyzeGraphMLData(args);
	}
}