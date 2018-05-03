package org.openecomp.sdc.asdctool.configuration;

import org.junit.Test;

public class ConfigurationUploaderTest {

	private ConfigurationUploader createTestSubject() {
		return new ConfigurationUploader();
	}

	@Test
	public void testUploadConfigurationFiles() throws Exception {
		String appConfigDir = "src/main/resources/config/";

		// default test
		ConfigurationUploader.uploadConfigurationFiles(appConfigDir);
	}
}