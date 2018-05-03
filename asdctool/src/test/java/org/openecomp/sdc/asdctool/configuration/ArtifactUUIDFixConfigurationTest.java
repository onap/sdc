package org.openecomp.sdc.asdctool.configuration;

import org.junit.Test;
import org.openecomp.sdc.asdctool.impl.ArtifactUuidFix;
import org.openecomp.sdc.be.components.distribution.engine.ServiceDistributionArtifactsBuilder;
import org.springframework.beans.factory.config.PropertiesFactoryBean;

public class ArtifactUUIDFixConfigurationTest {

	private ArtifactUUIDFixConfiguration createTestSubject() {
		return new ArtifactUUIDFixConfiguration();
	}

	@Test
	public void testArtifactUuidFix() throws Exception {
		ArtifactUUIDFixConfiguration testSubject;
		ArtifactUuidFix result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.artifactUuidFix();
	}

	@Test
	public void testServiceDistributionArtifactsBuilder() throws Exception {
		ArtifactUUIDFixConfiguration testSubject;
		ServiceDistributionArtifactsBuilder result;

		// default test
		testSubject = createTestSubject();
	}

	@Test
	public void testMapper() throws Exception {
		ArtifactUUIDFixConfiguration testSubject;
		PropertiesFactoryBean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.mapper();
	}
}