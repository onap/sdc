package org.openecomp.sdc.asdctool.impl.validator.config;

import org.junit.Test;

import java.util.Properties;


public class ValidationConfigManagerTest {

	private ValidationConfigManager createTestSubject() {
		return new ValidationConfigManager();
	}

	
	@Test
	public void testGetOutputFilePath() {
		String result;

		// default test
		result = ValidationConfigManager.getOutputFilePath();
	}
	
	@Test
	public void testGetCsvReportFilePath() {
		String result;

		// default test
		result = ValidationConfigManager.getCsvReportFilePath();
	}

	
	@Test
	public void testSetCsvReportFilePath() {
		String outputPath = "";

		// default test
		ValidationConfigManager.setCsvReportFilePath(outputPath);
	}

	
	@Test
	public void testSetValidationConfiguration() {
		String path = "";
		Properties result;

		// default test
		result = ValidationConfigManager.setValidationConfiguration(path);
	}

	
	@Test
	public void testGetValidationConfiguration() {
		Properties result;

		// default test
		result = ValidationConfigManager.getValidationConfiguration();
	}


	@Test
	public void testGetOutputFullFilePath() throws Exception {
	String result;
	
	// default test
	result=ValidationConfigManager.getOutputFullFilePath();
	}


	@Test
	public void testSetOutputFullFilePath() throws Exception {
	String outputPath = "";
	
	
	// default test
	ValidationConfigManager.setOutputFullFilePath(outputPath);
	}
}