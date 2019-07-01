package org.openecomp.sdc.asdctool.configuration;

import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.openecomp.sdc.asdctool.impl.ArtifactUuidFix;
import org.openecomp.sdc.be.components.distribution.engine.ServiceDistributionArtifactsBuilder;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;
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
		JanusGraphDao janusGraphDao = mock(JanusGraphDao.class);
		ToscaOperationFacade toscaOperationFacade = mock(ToscaOperationFacade.class);
		ToscaExportHandler toscaExportHandler = mock(ToscaExportHandler.class);
		ArtifactCassandraDao artifactCassandraDao = mock(ArtifactCassandraDao.class);
		CsarUtils csarUtils = mock(CsarUtils.class);

		result = testSubject.artifactUuidFix(janusGraphDao, toscaOperationFacade,
			toscaExportHandler, artifactCassandraDao, csarUtils);
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