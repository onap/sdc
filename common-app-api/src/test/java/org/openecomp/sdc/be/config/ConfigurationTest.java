package org.openecomp.sdc.be.config;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.openecomp.sdc.be.config.Configuration.ApplicationL1CacheConfig;
import org.openecomp.sdc.be.config.Configuration.ApplicationL2CacheConfig;
import org.openecomp.sdc.be.config.Configuration.ArtifactTypeConfig;
import org.openecomp.sdc.be.config.Configuration.BeMonitoringConfig;
import org.openecomp.sdc.be.config.Configuration.CassandrConfig;
import org.openecomp.sdc.be.config.Configuration.EcompPortalConfig;
import org.openecomp.sdc.be.config.Configuration.ElasticSearchConfig;
import org.openecomp.sdc.be.config.Configuration.OnboardingConfig;
import org.openecomp.sdc.be.config.Configuration.SwitchoverDetectorConfig;
import org.openecomp.sdc.be.config.Configuration.ToscaValidatorsConfig;
import org.openecomp.sdc.be.config.Configuration.VfModuleProperty;


public class ConfigurationTest {

	private Configuration createTestSubject() {
		return new Configuration();
	}

	
	@Test
	public void testGetGenericAssetNodeTypes() throws Exception {
		Configuration testSubject;
		Map<String, String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGenericAssetNodeTypes();
	}

	
	@Test
	public void testSetGenericAssetNodeTypes() throws Exception {
		Configuration testSubject;
		Map<String, String> genericAssetNodeTypes = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setGenericAssetNodeTypes(genericAssetNodeTypes);
	}

	
	@Test
	public void testGetSwitchoverDetector() throws Exception {
		Configuration testSubject;
		SwitchoverDetectorConfig result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSwitchoverDetector();
	}

	
	@Test
	public void testSetSwitchoverDetector() throws Exception {
		Configuration testSubject;
		SwitchoverDetectorConfig switchoverDetector = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setSwitchoverDetector(switchoverDetector);
	}

	
	@Test
	public void testGetApplicationL1Cache() throws Exception {
		Configuration testSubject;
		ApplicationL1CacheConfig result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getApplicationL1Cache();
	}

	
	@Test
	public void testSetApplicationL1Cache() throws Exception {
		Configuration testSubject;
		ApplicationL1CacheConfig applicationL1Cache = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setApplicationL1Cache(applicationL1Cache);
	}

	
	@Test
	public void testGetApplicationL2Cache() throws Exception {
		Configuration testSubject;
		ApplicationL2CacheConfig result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getApplicationL2Cache();
	}

	
	@Test
	public void testSetApplicationL2Cache() throws Exception {
		Configuration testSubject;
		ApplicationL2CacheConfig applicationL2Cache = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setApplicationL2Cache(applicationL2Cache);
	}

	
	@Test
	public void testGetCassandraConfig() throws Exception {
		Configuration testSubject;
		CassandrConfig result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCassandraConfig();
	}

	
	@Test
	public void testSetCassandraConfig() throws Exception {
		Configuration testSubject;
		CassandrConfig cassandraKeySpace = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCassandraConfig(cassandraKeySpace);
	}

	
	@Test
	public void testGetIdentificationHeaderFields() throws Exception {
		Configuration testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getIdentificationHeaderFields();
	}

	
	@Test
	public void testSetIdentificationHeaderFields() throws Exception {
		Configuration testSubject;
		List<String> identificationHeaderFields = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setIdentificationHeaderFields(identificationHeaderFields);
	}

	
	@Test
	public void testGetReleased() throws Exception {
		Configuration testSubject;
		Date result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getReleased();
	}

	
	@Test
	public void testGetVersion() throws Exception {
		Configuration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVersion();
	}

	
	@Test
	public void testSetReleased() throws Exception {
		Configuration testSubject;
		Date released = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setReleased(released);
	}

	
	@Test
	public void testSetVersion() throws Exception {
		Configuration testSubject;
		String version = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVersion(version);
	}

	
	@Test
	public void testGetProtocols() throws Exception {
		Configuration testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProtocols();
	}

	
	@Test
	public void testSetProtocols() throws Exception {
		Configuration testSubject;
		List<String> protocols = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProtocols(protocols);
	}

	
	@Test
	public void testGetUsers() throws Exception {
		Configuration testSubject;
		Map<String, String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUsers();
	}

	
	@Test
	public void testSetUsers() throws Exception {
		Configuration testSubject;
		Map<String, String> users = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setUsers(users);
	}

	
	@Test
	public void testGetBeFqdn() throws Exception {
		Configuration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getBeFqdn();
	}

	
	@Test
	public void testSetBeFqdn() throws Exception {
		Configuration testSubject;
		String beHost = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setBeFqdn(beHost);
	}

	
	@Test
	public void testGetBeHttpPort() throws Exception {
		Configuration testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getBeHttpPort();
	}

	
	@Test
	public void testSetBeHttpPort() throws Exception {
		Configuration testSubject;
		Integer beHttpPort = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setBeHttpPort(beHttpPort);
	}

	
	@Test
	public void testGetBeSslPort() throws Exception {
		Configuration testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getBeSslPort();
	}

	
	@Test
	public void testSetBeSslPort() throws Exception {
		Configuration testSubject;
		Integer beSslPort = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setBeSslPort(beSslPort);
	}

	
	@Test
	public void testGetBeContext() throws Exception {
		Configuration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getBeContext();
	}

	
	@Test
	public void testSetBeContext() throws Exception {
		Configuration testSubject;
		String beContext = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setBeContext(beContext);
	}

	
	@Test
	public void testGetBeProtocol() throws Exception {
		Configuration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getBeProtocol();
	}

	
	@Test
	public void testSetBeProtocol() throws Exception {
		Configuration testSubject;
		String beProtocol = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setBeProtocol(beProtocol);
	}

	
	@Test
	public void testGetNeo4j() throws Exception {
		Configuration testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNeo4j();
	}

	
	@Test
	public void testSetNeo4j() throws Exception {
		Configuration testSubject;
		Map<String, Object> neo4j = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setNeo4j(neo4j);
	}

	
	@Test
	public void testGetElasticSearch() throws Exception {
		Configuration testSubject;
		ElasticSearchConfig result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getElasticSearch();
	}

	
	@Test
	public void testSetElasticSearch() throws Exception {
		Configuration testSubject;
		ElasticSearchConfig elasticSearch = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setElasticSearch(elasticSearch);
	}

	
	@Test
	public void testGetTitanCfgFile() throws Exception {
		Configuration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTitanCfgFile();
	}

	
	@Test
	public void testSetTitanCfgFile() throws Exception {
		Configuration testSubject;
		String titanCfgFile = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setTitanCfgFile(titanCfgFile);
	}

	
	@Test
	public void testGetTitanMigrationKeySpaceCfgFile() throws Exception {
		Configuration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTitanMigrationKeySpaceCfgFile();
	}

	
	@Test
	public void testSetTitanMigrationKeySpaceCfgFile() throws Exception {
		Configuration testSubject;
		String titanMigrationKeySpaceCfgFile = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setTitanMigrationKeySpaceCfgFile(titanMigrationKeySpaceCfgFile);
	}

	
	@Test
	public void testGetTitanInMemoryGraph() throws Exception {
		Configuration testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTitanInMemoryGraph();
	}

	
	@Test
	public void testSetTitanInMemoryGraph() throws Exception {
		Configuration testSubject;
		Boolean titanInMemoryGraph = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTitanInMemoryGraph(titanInMemoryGraph);
	}

	
	@Test
	public void testGetStartMigrationFrom() throws Exception {
		Configuration testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStartMigrationFrom();
	}

	
	@Test
	public void testSetStartMigrationFrom() throws Exception {
		Configuration testSubject;
		int startMigrationFrom = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setStartMigrationFrom(startMigrationFrom);
	}

	
	@Test
	public void testGetTitanLockTimeout() throws Exception {
		Configuration testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTitanLockTimeout();
	}

	
	@Test
	public void testSetTitanLockTimeout() throws Exception {
		Configuration testSubject;
		Long titanLockTimeout = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTitanLockTimeout(titanLockTimeout);
	}

	
	@Test
	public void testGetTitanHealthCheckReadTimeout() throws Exception {
		Configuration testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTitanHealthCheckReadTimeout();
	}

	

	
	@Test
	public void testSetTitanHealthCheckReadTimeout() throws Exception {
		Configuration testSubject;
		Long titanHealthCheckReadTimeout = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTitanHealthCheckReadTimeout(titanHealthCheckReadTimeout);
	}

	
	@Test
	public void testGetTitanReconnectIntervalInSeconds() throws Exception {
		Configuration testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTitanReconnectIntervalInSeconds();
	}

	

	
	@Test
	public void testSetTitanReconnectIntervalInSeconds() throws Exception {
		Configuration testSubject;
		Long titanReconnectIntervalInSeconds = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTitanReconnectIntervalInSeconds(titanReconnectIntervalInSeconds);
	}

	
	@Test
	public void testGetEsReconnectIntervalInSeconds() throws Exception {
		Configuration testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getEsReconnectIntervalInSeconds();
	}



	
	@Test
	public void testSetEsReconnectIntervalInSeconds() throws Exception {
		Configuration testSubject;
		Long esReconnectIntervalInSeconds = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setEsReconnectIntervalInSeconds(esReconnectIntervalInSeconds);
	}

	
	@Test
	public void testGetArtifactTypes() throws Exception {
		Configuration testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactTypes();
	}

	
	@Test
	public void testSetArtifactTypes() throws Exception {
		Configuration testSubject;
		List<String> artifactTypes = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactTypes(artifactTypes);
	}

	
	@Test
	public void testGetExcludeResourceCategory() throws Exception {
		Configuration testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getExcludeResourceCategory();
	}

	
	@Test
	public void testSetExcludeResourceCategory() throws Exception {
		Configuration testSubject;
		List<String> excludeResourceCategory = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setExcludeResourceCategory(excludeResourceCategory);
	}

	
	@Test
	public void testGetExcludeResourceType() throws Exception {
		Configuration testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getExcludeResourceType();
	}

	
	@Test
	public void testSetExcludeResourceType() throws Exception {
		Configuration testSubject;
		List<String> excludeResourceType = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setExcludeResourceType(excludeResourceType);
	}

	
	@Test
	public void testGetToscaArtifacts() throws Exception {
		Configuration testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getToscaArtifacts();
	}

	
	@Test
	public void testSetToscaArtifacts() throws Exception {
		Configuration testSubject;
		Map<String, Object> toscaArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setToscaArtifacts(toscaArtifacts);
	}

	
	@Test
	public void testGetInformationalResourceArtifacts() throws Exception {
		Configuration testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInformationalResourceArtifacts();
	}

	
	@Test
	public void testSetInformationalResourceArtifacts() throws Exception {
		Configuration testSubject;
		Map<String, Object> informationalResourceArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInformationalResourceArtifacts(informationalResourceArtifacts);
	}

	
	@Test
	public void testGetInformationalServiceArtifacts() throws Exception {
		Configuration testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInformationalServiceArtifacts();
	}

	
	@Test
	public void testSetInformationalServiceArtifacts() throws Exception {
		Configuration testSubject;
		Map<String, Object> informationalServiceArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInformationalServiceArtifacts(informationalServiceArtifacts);
	}

	
	@Test
	public void testGetServiceApiArtifacts() throws Exception {
		Configuration testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceApiArtifacts();
	}

	
	@Test
	public void testSetServiceApiArtifacts() throws Exception {
		Configuration testSubject;
		Map<String, Object> serviceApiArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceApiArtifacts(serviceApiArtifacts);
	}

	
	@Test
	public void testGetServiceDeploymentArtifacts() throws Exception {
		Configuration testSubject;
		Map<String, ArtifactTypeConfig> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceDeploymentArtifacts();
	}

	
	@Test
	public void testSetServiceDeploymentArtifacts() throws Exception {
		Configuration testSubject;
		Map<String, ArtifactTypeConfig> serviceDeploymentArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceDeploymentArtifacts(serviceDeploymentArtifacts);
	}

	
	@Test
	public void testGetResourceDeploymentArtifacts() throws Exception {
		Configuration testSubject;
		Map<String, ArtifactTypeConfig> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceDeploymentArtifacts();
	}

	
	@Test
	public void testSetResourceDeploymentArtifacts() throws Exception {
		Configuration testSubject;
		Map<String, ArtifactTypeConfig> resourceDeploymentArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceDeploymentArtifacts(resourceDeploymentArtifacts);
	}

	
	@Test
	public void testSetResourceInstanceDeploymentArtifacts() throws Exception {
		Configuration testSubject;
		Map<String, ArtifactTypeConfig> resourceInstanceDeploymentArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceInstanceDeploymentArtifacts(resourceInstanceDeploymentArtifacts);
	}

	
	@Test
	public void testGetResourceInstanceDeploymentArtifacts() throws Exception {
		Configuration testSubject;
		Map<String, ArtifactTypeConfig> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceInstanceDeploymentArtifacts();
	}

	
	@Test
	public void testGetExcludeServiceCategory() throws Exception {
		Configuration testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getExcludeServiceCategory();
	}

	
	@Test
	public void testSetExcludeServiceCategory() throws Exception {
		Configuration testSubject;
		List<String> excludeServiceCategory = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setExcludeServiceCategory(excludeServiceCategory);
	}

	
	@Test
	public void testGetLicenseTypes() throws Exception {
		Configuration testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLicenseTypes();
	}

	
	@Test
	public void testSetLicenseTypes() throws Exception {
		Configuration testSubject;
		List<String> licenseTypes = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setLicenseTypes(licenseTypes);
	}

	
	@Test
	public void testGetAdditionalInformationMaxNumberOfKeys() throws Exception {
		Configuration testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAdditionalInformationMaxNumberOfKeys();
	}

	
	@Test
	public void testSetAdditionalInformationMaxNumberOfKeys() throws Exception {
		Configuration testSubject;
		Integer additionalInformationMaxNumberOfKeys = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setAdditionalInformationMaxNumberOfKeys(additionalInformationMaxNumberOfKeys);
	}

	
	@Test
	public void testGetSystemMonitoring() throws Exception {
		Configuration testSubject;
		BeMonitoringConfig result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSystemMonitoring();
	}

	
	@Test
	public void testSetSystemMonitoring() throws Exception {
		Configuration testSubject;
		BeMonitoringConfig systemMonitoring = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setSystemMonitoring(systemMonitoring);
	}

	
	@Test
	public void testGetDefaultHeatArtifactTimeoutMinutes() throws Exception {
		Configuration testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDefaultHeatArtifactTimeoutMinutes();
	}

	
	@Test
	public void testSetDefaultHeatArtifactTimeoutMinutes() throws Exception {
		Configuration testSubject;
		Integer defaultHeatArtifactTimeoutMinutes = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setDefaultHeatArtifactTimeoutMinutes(defaultHeatArtifactTimeoutMinutes);
	}

	
	@Test
	public void testGetUebHealthCheckReconnectIntervalInSeconds() throws Exception {
		Configuration testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUebHealthCheckReconnectIntervalInSeconds();
	}

	
	@Test
	public void testSetUebHealthCheckReconnectIntervalInSeconds() throws Exception {
		Configuration testSubject;
		Long uebHealthCheckReconnectIntervalInSeconds = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setUebHealthCheckReconnectIntervalInSeconds(uebHealthCheckReconnectIntervalInSeconds);
	}

	
	@Test
	public void testGetUebHealthCheckReadTimeout() throws Exception {
		Configuration testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUebHealthCheckReadTimeout();
	}

	
	@Test
	public void testSetUebHealthCheckReadTimeout() throws Exception {
		Configuration testSubject;
		Long uebHealthCheckReadTimeout = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setUebHealthCheckReadTimeout(uebHealthCheckReadTimeout);
	}

	
	@Test
	public void testGetCleanComponentsConfiguration() throws Exception {
		Configuration testSubject;
		CleanComponentsConfiguration result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCleanComponentsConfiguration();
	}

	
	@Test
	public void testSetCleanComponentsConfiguration() throws Exception {
		Configuration testSubject;
		CleanComponentsConfiguration cleanComponentsConfiguration = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCleanComponentsConfiguration(cleanComponentsConfiguration);
	}

	
	@Test
	public void testToString() throws Exception {
		Configuration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testGetUnLoggedUrls() throws Exception {
		Configuration testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUnLoggedUrls();
	}

	
	@Test
	public void testSetUnLoggedUrls() throws Exception {
		Configuration testSubject;
		List<String> unLoggedUrls = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setUnLoggedUrls(unLoggedUrls);
	}

	
	@Test
	public void testGetDeploymentResourceArtifacts() throws Exception {
		Configuration testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDeploymentResourceArtifacts();
	}

	
	@Test
	public void testSetDeploymentResourceArtifacts() throws Exception {
		Configuration testSubject;
		Map<String, Object> deploymentResourceArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDeploymentResourceArtifacts(deploymentResourceArtifacts);
	}

	
	@Test
	public void testGetHeatEnvArtifactHeader() throws Exception {
		Configuration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getHeatEnvArtifactHeader();
	}

	
	@Test
	public void testSetHeatEnvArtifactHeader() throws Exception {
		Configuration testSubject;
		String heatEnvArtifactHeader = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setHeatEnvArtifactHeader(heatEnvArtifactHeader);
	}

	
	@Test
	public void testGetHeatEnvArtifactFooter() throws Exception {
		Configuration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getHeatEnvArtifactFooter();
	}

	
	@Test
	public void testSetHeatEnvArtifactFooter() throws Exception {
		Configuration testSubject;
		String heatEnvArtifactFooter = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setHeatEnvArtifactFooter(heatEnvArtifactFooter);
	}

	
	@Test
	public void testGetDeploymentResourceInstanceArtifacts() throws Exception {
		Configuration testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDeploymentResourceInstanceArtifacts();
	}

	
	@Test
	public void testSetDeploymentResourceInstanceArtifacts() throws Exception {
		Configuration testSubject;
		Map<String, Object> deploymentResourceInstanceArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDeploymentResourceInstanceArtifacts(deploymentResourceInstanceArtifacts);
	}

	
	@Test
	public void testGetArtifactsIndex() throws Exception {
		Configuration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactsIndex();
	}

	
	@Test
	public void testSetArtifactsIndex() throws Exception {
		Configuration testSubject;
		String artifactsIndex = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactsIndex(artifactsIndex);
	}

	
	@Test
	public void testGetResourceInformationalDeployedArtifacts() throws Exception {
		Configuration testSubject;
		Map<String, ArtifactTypeConfig> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceInformationalDeployedArtifacts();
	}

	
	@Test
	public void testSetResourceInformationalDeployedArtifacts() throws Exception {
		Configuration testSubject;
		Map<String, ArtifactTypeConfig> resourceInformationalDeployedArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceInformationalDeployedArtifacts(resourceInformationalDeployedArtifacts);
	}

	
	@Test
	public void testGetResourceTypes() throws Exception {
		Configuration testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceTypes();
	}

	
	@Test
	public void testSetResourceTypes() throws Exception {
		Configuration testSubject;
		List<String> resourceTypes = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceTypes(resourceTypes);
	}

	
	@Test
	public void testGetToscaFilesDir() throws Exception {
		Configuration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getToscaFilesDir();
	}

	
	@Test
	public void testSetToscaFilesDir() throws Exception {
		Configuration testSubject;
		String toscaFilesDir = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setToscaFilesDir(toscaFilesDir);
	}

	
	@Test
	public void testGetHeatTranslatorPath() throws Exception {
		Configuration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getHeatTranslatorPath();
	}

	
	@Test
	public void testSetHeatTranslatorPath() throws Exception {
		Configuration testSubject;
		String heatTranslatorPath = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setHeatTranslatorPath(heatTranslatorPath);
	}

	
	@Test
	public void testGetRequirementsToFulfillBeforeCert() throws Exception {
		Configuration testSubject;
		Map<String, Set<String>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequirementsToFulfillBeforeCert();
	}

	
	@Test
	public void testSetRequirementsToFulfillBeforeCert() throws Exception {
		Configuration testSubject;
		Map<String, Set<String>> requirementsToFulfillBeforeCert = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRequirementsToFulfillBeforeCert(requirementsToFulfillBeforeCert);
	}

	
	@Test
	public void testGetCapabilitiesToConsumeBeforeCert() throws Exception {
		Configuration testSubject;
		Map<String, Set<String>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapabilitiesToConsumeBeforeCert();
	}

	
	@Test
	public void testSetCapabilitiesToConsumeBeforeCert() throws Exception {
		Configuration testSubject;
		Map<String, Set<String>> capabilitiesToConsumeBeforeCert = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCapabilitiesToConsumeBeforeCert(capabilitiesToConsumeBeforeCert);
	}

	
	@Test
	public void testGetOnboarding() throws Exception {
		Configuration testSubject;
		OnboardingConfig result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOnboarding();
	}

	
	@Test
	public void testSetOnboarding() throws Exception {
		Configuration testSubject;
		OnboardingConfig onboarding = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setOnboarding(onboarding);
	}

	
	@Test
	public void testGetEcompPortal() throws Exception {
		Configuration testSubject;
		EcompPortalConfig result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getEcompPortal();
	}

	
	@Test
	public void testSetEcompPortal() throws Exception {
		Configuration testSubject;
		EcompPortalConfig ecompPortal = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setEcompPortal(ecompPortal);
	}

	
	@Test
	public void testGetToscaValidators() throws Exception {
		Configuration testSubject;
		ToscaValidatorsConfig result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getToscaValidators();
	}

	
	@Test
	public void testSetToscaValidators() throws Exception {
		Configuration testSubject;
		ToscaValidatorsConfig toscaValidators = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setToscaValidators(toscaValidators);
	}

	
	@Test
	public void testIsDisableAudit() throws Exception {
		Configuration testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isDisableAudit();
	}

	
	@Test
	public void testSetDisableAudit() throws Exception {
		Configuration testSubject;
		boolean enableAudit = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setDisableAudit(enableAudit);
	}

	
	@Test
	public void testGetResourceInformationalArtifacts() throws Exception {
		Configuration testSubject;
		Map<String, ArtifactTypeConfig> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceInformationalArtifacts();
	}

	
	@Test
	public void testSetResourceInformationalArtifacts() throws Exception {
		Configuration testSubject;
		Map<String, ArtifactTypeConfig> resourceInformationalArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceInformationalArtifacts(resourceInformationalArtifacts);
	}

	
	@Test
	public void testGetVfModuleProperties() throws Exception {
		Configuration testSubject;
		Map<String, VfModuleProperty> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVfModuleProperties();
	}

	
	@Test
	public void testSetVfModuleProperties() throws Exception {
		Configuration testSubject;
		Map<String, VfModuleProperty> vfModuleProperties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setVfModuleProperties(vfModuleProperties);
	}

	
	@Test
	public void testGetToscaConformanceLevel() throws Exception {
		Configuration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getToscaConformanceLevel();
	}

	
	@Test
	public void testSetToscaConformanceLevel() throws Exception {
		Configuration testSubject;
		String toscaConformanceLevel = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setToscaConformanceLevel(toscaConformanceLevel);
	}

	
	@Test
	public void testGetMinToscaConformanceLevel() throws Exception {
		Configuration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMinToscaConformanceLevel();
	}

	
	@Test
	public void testSetMinToscaConformanceLevel() throws Exception {
		Configuration testSubject;
		String toscaConformanceLevel = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setMinToscaConformanceLevel(toscaConformanceLevel);
	}

	
	@Test
	public void testGetDefaultImports() throws Exception {
		Configuration testSubject;
		LinkedList<Map<String, Map<String, String>>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDefaultImports();
	}

	
	@Test
	public void testSetDefaultImports() throws Exception {
		Configuration testSubject;
		LinkedList<Map<String, Map<String, String>>> defaultImports = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDefaultImports(defaultImports);
	}
}