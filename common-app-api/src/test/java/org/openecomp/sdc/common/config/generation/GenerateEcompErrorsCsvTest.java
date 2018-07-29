package org.openecomp.sdc.common.config.generation;

import org.junit.Test;

public class GenerateEcompErrorsCsvTest {

	private GenerateEcompErrorsCsv createTestSubject() {
		return new GenerateEcompErrorsCsv();
	}

	
	@Test
	public void testGenerateEcompErrorsCsvFile() throws Exception {
		GenerateEcompErrorsCsv testSubject;
		String targetFolder = "";
		boolean addTimeToFileName = false;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.generateEcompErrorsCsvFile(targetFolder, addTimeToFileName);
	}

	
	@Test
	public void testMain() throws Exception {
		String[] args = new String[] { "" };

		// default test
		GenerateEcompErrorsCsv.main(args);
	}
}