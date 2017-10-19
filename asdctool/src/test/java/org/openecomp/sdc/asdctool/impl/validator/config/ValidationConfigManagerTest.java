package org.openecomp.sdc.asdctool.impl.validator.config;

import java.util.Properties;

import javax.annotation.Generated;

import org.junit.Test;


public class ValidationConfigManagerTest {

	private ValidationConfigManager createTestSubject() {
		return new ValidationConfigManager();
	}

	
	@Test
	public void testGetOutputFilePath() throws Exception {
		String result;

		// default test
		result = ValidationConfigManager.getOutputFilePath();
	}

	
	@Test
	public void testSetOutputFilePath() throws Exception {
		String outputPath = "";

		// default test
		ValidationConfigManager.setOutputFilePath(outputPath);
	}

	
	@Test
	public void testGetCsvReportFilePath() throws Exception {
		String result;

		// default test
		result = ValidationConfigManager.getCsvReportFilePath();
	}

	
	@Test
	public void testSetCsvReportFilePath() throws Exception {
		String outputPath = "";

		// default test
		ValidationConfigManager.setCsvReportFilePath(outputPath);
	}

	
	@Test
	public void testSetValidationConfiguration() throws Exception {
		String path = "";
		Properties result;

		// default test
		result = ValidationConfigManager.setValidationConfiguration(path);
	}

	
	@Test
	public void testGetValidationConfiguration() throws Exception {
		Properties result;

		// default test
		result = ValidationConfigManager.getValidationConfiguration();
	}
}