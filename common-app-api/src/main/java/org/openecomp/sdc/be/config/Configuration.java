/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.config;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.openecomp.sdc.common.api.BasicConfiguration;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;

public class Configuration extends BasicConfiguration {

    private List<String> identificationHeaderFields;
    /**
     * Requests from these Urls will not be logged by
     * org.openecomp.sdc.be.filters.BeServletFilter.<br>
     **/
    private List<String> unLoggedUrls;

    /**
     * backend host
     */
    private String beFqdn;
    /**
     * backend http port
     */
    private Integer beHttpPort;
    /**
     * backend http secured port
     */
    private Integer beSslPort;
    /**
     * be http context
     */
    private String beContext;
    /**
     * backend protocol. http | https
     */
    private String beProtocol = "http";

    private Date released;
    private String version = "1111";
    private String toscaConformanceLevel = "3.0";
    private String minToscaConformanceLevel = "3.0";
    private List<String> protocols;
    private Map<String, String> users;
    private Map<String, Object> neo4j;
    private String janusGraphCfgFile;
    private String janusGraphMigrationKeySpaceCfgFile;
    private Boolean janusGraphInMemoryGraph;
    private int startMigrationFrom;
    private Long janusGraphLockTimeout;
    private Long janusGraphReconnectIntervalInSeconds;
    private List<String> healthStatusExclude;
    private Long janusGraphHealthCheckReadTimeout;
    private Long uebHealthCheckReconnectIntervalInSeconds;
    private Long uebHealthCheckReadTimeout;
    private List<Map<String, Map<String, String>>> defaultImports;

    private List<String> resourceTypes;
    private List<String> excludeResourceCategory;
    private List<String> excludeResourceType;
    private Map<String, Set<String>> excludedPolicyTypesMapping;

    private Map<String, Set<String>> excludedGroupTypesMapping;
    private Map<String, Object> deploymentResourceArtifacts;
    private Map<String, Object> deploymentResourceInstanceArtifacts;
    private Map<String, Object> toscaArtifacts;
    private Map<String, Object> informationalResourceArtifacts;
    private Map<String, Object> informationalServiceArtifacts;
    private Map<String, ArtifactTypeConfig> resourceDeploymentArtifacts;
    private Map<String, ArtifactTypeConfig> serviceDeploymentArtifacts;
    private Map<String, ArtifactTypeConfig> resourceInstanceDeploymentArtifacts;
    private Map<String, ArtifactTypeConfig> resourceInformationalArtifacts;
    private Map<String, ArtifactTypeConfig> resourceInformationalDeployedArtifacts;
    private Map<String, Object> serviceApiArtifacts;
    private List<String> excludeServiceCategory;

    private List<String> artifactTypes;
    private List<String> licenseTypes;

    private Integer additionalInformationMaxNumberOfKeys;
    private HeatDeploymentArtifactTimeout heatArtifactDeploymentTimeout;

    private BeMonitoringConfig systemMonitoring;
    private CleanComponentsConfiguration cleanComponentsConfiguration;

    private String artifactsIndex;

    private String heatEnvArtifactHeader;
    private String heatEnvArtifactFooter;

    private String toscaFilesDir;
    private String heatTranslatorPath;

    private OnboardingConfig onboarding;

    private DcaeConfig dcae;

    private CassandrConfig cassandraConfig;

    private SwitchoverDetectorConfig switchoverDetector;

    private ApplicationL1CacheConfig applicationL1Cache;

    private ApplicationL2CacheConfig applicationL2Cache;

    private ToscaValidatorsConfig toscaValidators;

    private boolean disableAudit;

    private Boolean consumerBusinessLogic;

    private Map<String, VfModuleProperty> vfModuleProperties;

    private Map<String, String> genericAssetNodeTypes;

    private String appVersion;
    private String artifactGeneratorConfig;

    private CadiFilterParams cadiFilterParams;

    private Boolean aafAuthNeeded;

    private String autoHealingOwner;
    private boolean enableAutoHealing;

    private Map<String, List<String>> resourcesForUpgrade;
    private DmaapConsumerConfiguration dmaapConsumerConfiguration;
    private DmaapProducerConfiguration dmaapProducerConfiguration;

    private boolean skipUpgradeFailedVfs;
    private boolean skipUpgradeVSPs;
    private DmeConfiguration dmeConfiguration;

    private boolean supportAllottedResourcesAndProxy;
    private Integer deleteLockTimeoutInSeconds;
    private Integer maxDeleteComponents;
    private CookieConfig authCookie;


    private String aafNamespace;
    private String workloadContext;

    private EnvironmentContext environmentContext;


    public String getAutoHealingOwner() {
        return autoHealingOwner;
    }

    public void setAutoHealingOwner(String autoHealingOwner) {
        this.autoHealingOwner = autoHealingOwner;
    }

    public Integer getMaxDeleteComponents() {
        return maxDeleteComponents;
    }

    public void setMaxDeleteComponents(Integer maxDeleteComponents) {
        this.maxDeleteComponents = maxDeleteComponents;
    }

    public void setEnableAutoHealing(boolean enableAutoHealing) {
        this.enableAutoHealing = enableAutoHealing;
    }

    public boolean isEnableAutoHealing() {
        return enableAutoHealing;
    }

    public Integer getDeleteLockTimeoutInSeconds() {
        return deleteLockTimeoutInSeconds;
    }

    public void setDeleteLockTimeoutInSeconds(Integer deleteLockTimeoutInSeconds) {
        this.deleteLockTimeoutInSeconds = deleteLockTimeoutInSeconds;
    }

    public DmaapConsumerConfiguration getDmaapConsumerConfiguration() {
        return dmaapConsumerConfiguration;
    }

    public void setDmaapConsumerConfiguration(DmaapConsumerConfiguration dmaapConsumerConfiguration) {
        this.dmaapConsumerConfiguration = dmaapConsumerConfiguration;
    }

    public DmeConfiguration getDmeConfiguration() {
        return dmeConfiguration;
    }

    public void setDmeConfiguration(DmeConfiguration dmeConfiguration) {
        this.dmeConfiguration = dmeConfiguration;
    }
    public void setSkipUpgradeVSPs(boolean skipUpgradeVSPs) { this.skipUpgradeVSPs = skipUpgradeVSPs; }

    public boolean getSkipUpgradeVSPsFlag() { return skipUpgradeVSPs; }

    public boolean getSkipUpgradeFailedVfs() {
        return skipUpgradeFailedVfs;
    }

    public boolean getSupportAllottedResourcesAndProxyFlag() {
        return supportAllottedResourcesAndProxy;
    }

    public void setSupportAllottedResourcesAndProxy(boolean supportAllottedResourcesAndProxy) {
        this.supportAllottedResourcesAndProxy = supportAllottedResourcesAndProxy;
    }

    public void setSkipUpgradeFailedVfs(boolean skipUpgradeFailedVfs) {
        this.skipUpgradeFailedVfs = skipUpgradeFailedVfs;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getArtifactGeneratorConfig() {
        return artifactGeneratorConfig;
    }

    public void setArtifactGeneratorConfig(String artifactGeneratorConfig) {
        this.artifactGeneratorConfig = artifactGeneratorConfig;
    }

    private List<GabConfig> gabConfig;

    public Map<String, String> getGenericAssetNodeTypes() {
        return genericAssetNodeTypes;
    }

    public void setGenericAssetNodeTypes(Map<String, String> genericAssetNodeTypes) {
        this.genericAssetNodeTypes = genericAssetNodeTypes;
    }

    public SwitchoverDetectorConfig getSwitchoverDetector() {
        return switchoverDetector;
    }

    public void setSwitchoverDetector(SwitchoverDetectorConfig switchoverDetector) {
        this.switchoverDetector = switchoverDetector;
    }

    public ApplicationL1CacheConfig getApplicationL1Cache() {
        return applicationL1Cache;
    }

    public void setApplicationL1Cache(ApplicationL1CacheConfig applicationL1Cache) {
        this.applicationL1Cache = applicationL1Cache;
    }

    public ApplicationL2CacheConfig getApplicationL2Cache() {
        return applicationL2Cache;
    }

    public void setApplicationL2Cache(ApplicationL2CacheConfig applicationL2Cache) {
        this.applicationL2Cache = applicationL2Cache;
    }

    private EcompPortalConfig ecompPortal;

    public CassandrConfig getCassandraConfig() {
        return cassandraConfig;
    }

    public void setCassandraConfig(CassandrConfig cassandraKeySpace) {
        this.cassandraConfig = cassandraKeySpace;
    }

    public List<String> getIdentificationHeaderFields() {
        return identificationHeaderFields;
    }

    public void setIdentificationHeaderFields(List<String> identificationHeaderFields) {
        this.identificationHeaderFields = identificationHeaderFields;
    }

    public Date getReleased() {
        return released;
    }

    public String getVersion() {
        return version;
    }

    public void setReleased(Date released) {
        this.released = released;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<String> getProtocols() {
        return protocols;
    }

    public void setProtocols(List<String> protocols) {
        this.protocols = protocols;
    }

    public Map<String, String> getUsers() {
        return users;
    }

    public void setUsers(Map<String, String> users) {
        this.users = users;
    }

    public String getBeFqdn() {
        return beFqdn;
    }

    public void setBeFqdn(String beHost) {
        this.beFqdn = beHost;
    }

    public Integer getBeHttpPort() {
        return beHttpPort;
    }

    public void setBeHttpPort(Integer beHttpPort) {
        this.beHttpPort = beHttpPort;
    }

    public Integer getBeSslPort() {
        return beSslPort;
    }

    public void setBeSslPort(Integer beSslPort) {
        this.beSslPort = beSslPort;
    }

    public String getBeContext() {
        return beContext;
    }

    public void setBeContext(String beContext) {
        this.beContext = beContext;
    }

    public String getBeProtocol() {
        return beProtocol;
    }

    public void setBeProtocol(String beProtocol) {
        this.beProtocol = beProtocol;
    }

    public Map<String, Object> getNeo4j() {
        return neo4j;
    }

    public void setNeo4j(Map<String, Object> neo4j) {
        this.neo4j = neo4j;
    }

    public String getJanusGraphCfgFile() {
        return janusGraphCfgFile;
    }

    public void setJanusGraphCfgFile(String janusGraphCfgFile) {
        this.janusGraphCfgFile = janusGraphCfgFile;
    }

    public String getJanusGraphMigrationKeySpaceCfgFile() {
        return janusGraphMigrationKeySpaceCfgFile;
    }

    public void setJanusGraphMigrationKeySpaceCfgFile(String janusGraphMigrationKeySpaceCfgFile) {
        this.janusGraphMigrationKeySpaceCfgFile = janusGraphMigrationKeySpaceCfgFile;
    }

    public Boolean getJanusGraphInMemoryGraph() {
        return janusGraphInMemoryGraph;
    }

    public void setJanusGraphInMemoryGraph(Boolean janusGraphInMemoryGraph) {
        this.janusGraphInMemoryGraph = janusGraphInMemoryGraph;
    }

    public int getStartMigrationFrom() {
        return startMigrationFrom;
    }

    public void setStartMigrationFrom(int startMigrationFrom) {
        this.startMigrationFrom = startMigrationFrom;
    }

    public Long getJanusGraphLockTimeout() {
        return janusGraphLockTimeout;
    }

    public void setJanusGraphLockTimeout(Long janusGraphLockTimeout) {
        this.janusGraphLockTimeout = janusGraphLockTimeout;
    }

    public Long getJanusGraphHealthCheckReadTimeout() {
        return janusGraphHealthCheckReadTimeout;
    }

    public Long getJanusGraphHealthCheckReadTimeout(long defaultVal) {
        return janusGraphHealthCheckReadTimeout == null ? defaultVal : janusGraphHealthCheckReadTimeout;
    }

    public void setJanusGraphHealthCheckReadTimeout(Long janusGraphHealthCheckReadTimeout) {
        this.janusGraphHealthCheckReadTimeout = janusGraphHealthCheckReadTimeout;
    }

    public Long getJanusGraphReconnectIntervalInSeconds() {
        return janusGraphReconnectIntervalInSeconds;
    }

    public Long getJanusGraphReconnectIntervalInSeconds(long defaultVal) {
        return janusGraphReconnectIntervalInSeconds == null ? defaultVal : janusGraphReconnectIntervalInSeconds;
    }

    public void setJanusGraphReconnectIntervalInSeconds(Long janusGraphReconnectIntervalInSeconds) {
        this.janusGraphReconnectIntervalInSeconds = janusGraphReconnectIntervalInSeconds;
    }

    public List<String> getArtifactTypes() {
        return artifactTypes;
    }

    public void setArtifactTypes(List<String> artifactTypes) {
        this.artifactTypes = artifactTypes;
    }

    public List<String> getExcludeResourceCategory() {
        return excludeResourceCategory;
    }

    public void setExcludeResourceCategory(List<String> excludeResourceCategory) {
        this.excludeResourceCategory = excludeResourceCategory;
    }

    public List<String> getExcludeResourceType() {
        return excludeResourceType;
    }

    public void setExcludeResourceType(List<String> excludeResourceType) {
        this.excludeResourceType = excludeResourceType;
    }

    public Map<String, Set<String>> getExcludedPolicyTypesMapping() {
        return safeGetCapsInsensitiveMap(excludedPolicyTypesMapping);
    }

    public void setExcludedPolicyTypesMapping(Map<String, Set<String>> excludedPolicyTypesMapping) {
        this.excludedPolicyTypesMapping = excludedPolicyTypesMapping;
    }

    public Map<String, Set<String>> getExcludedGroupTypesMapping() {
        return safeGetCapsInsensitiveMap(excludedGroupTypesMapping);
    }

    public void setExcludedGroupTypesMapping(Map<String, Set<String>> excludedGroupTypesMapping) {
        this.excludedGroupTypesMapping = excludedGroupTypesMapping;
    }

    public Map<String, Object> getToscaArtifacts() {
        return toscaArtifacts;
    }

    public void setToscaArtifacts(Map<String, Object> toscaArtifacts) {
        this.toscaArtifacts = toscaArtifacts;
    }

    public Map<String, Object> getInformationalResourceArtifacts() {
        return informationalResourceArtifacts;
    }

    public void setInformationalResourceArtifacts(Map<String, Object> informationalResourceArtifacts) {
        this.informationalResourceArtifacts = informationalResourceArtifacts;
    }

    public Map<String, Object> getInformationalServiceArtifacts() {
        return informationalServiceArtifacts;
    }

    public void setInformationalServiceArtifacts(Map<String, Object> informationalServiceArtifacts) {
        this.informationalServiceArtifacts = informationalServiceArtifacts;
    }

    public Map<String, Object> getServiceApiArtifacts() {
        return serviceApiArtifacts;
    }

    public void setServiceApiArtifacts(Map<String, Object> serviceApiArtifacts) {
        this.serviceApiArtifacts = serviceApiArtifacts;
    }

    public Map<String, ArtifactTypeConfig> getServiceDeploymentArtifacts() {
        return serviceDeploymentArtifacts;
    }

    public void setServiceDeploymentArtifacts(Map<String, ArtifactTypeConfig> serviceDeploymentArtifacts) {
        this.serviceDeploymentArtifacts = serviceDeploymentArtifacts;
    }

    public Map<String, ArtifactTypeConfig> getResourceDeploymentArtifacts() {
        return resourceDeploymentArtifacts;
    }

    public void setResourceDeploymentArtifacts(Map<String, ArtifactTypeConfig> resourceDeploymentArtifacts) {
        this.resourceDeploymentArtifacts = resourceDeploymentArtifacts;
    }

    public void setResourceInstanceDeploymentArtifacts(
            Map<String, ArtifactTypeConfig> resourceInstanceDeploymentArtifacts) {
        this.resourceInstanceDeploymentArtifacts = resourceInstanceDeploymentArtifacts;
    }

    public Map<String, ArtifactTypeConfig> getResourceInstanceDeploymentArtifacts() {
        return resourceInstanceDeploymentArtifacts;
    }

    public List<String> getExcludeServiceCategory() {
        return excludeServiceCategory;
    }

    public void setExcludeServiceCategory(List<String> excludeServiceCategory) {
        this.excludeServiceCategory = excludeServiceCategory;
    }

    public List<String> getLicenseTypes() {
        return licenseTypes;
    }

    public void setLicenseTypes(List<String> licenseTypes) {
        this.licenseTypes = licenseTypes;
    }

    public Integer getAdditionalInformationMaxNumberOfKeys() {
        return additionalInformationMaxNumberOfKeys;
    }

    public void setAdditionalInformationMaxNumberOfKeys(Integer additionalInformationMaxNumberOfKeys) {
        this.additionalInformationMaxNumberOfKeys = additionalInformationMaxNumberOfKeys;
    }

    public HeatDeploymentArtifactTimeout getHeatArtifactDeploymentTimeout() {
        return heatArtifactDeploymentTimeout;
    }

    public void setHeatArtifactDeploymentTimeout(HeatDeploymentArtifactTimeout heatArtifactDeploymentTimeout) {
        this.heatArtifactDeploymentTimeout = heatArtifactDeploymentTimeout;
    }

    public BeMonitoringConfig getSystemMonitoring() {
        return systemMonitoring;
    }

    public void setSystemMonitoring(BeMonitoringConfig systemMonitoring) {
        this.systemMonitoring = systemMonitoring;
    }

    public Long getUebHealthCheckReconnectIntervalInSeconds() {
        return uebHealthCheckReconnectIntervalInSeconds;
    }

    public void setUebHealthCheckReconnectIntervalInSeconds(Long uebHealthCheckReconnectIntervalInSeconds) {
        this.uebHealthCheckReconnectIntervalInSeconds = uebHealthCheckReconnectIntervalInSeconds;
    }

    public Long getUebHealthCheckReadTimeout() {
        return uebHealthCheckReadTimeout;
    }

    public void setUebHealthCheckReadTimeout(Long uebHealthCheckReadTimeout) {
        this.uebHealthCheckReadTimeout = uebHealthCheckReadTimeout;
    }

    public String getWorkloadContext() {
        return workloadContext;
    }

    public void setWorkloadContext(String workloadContext) {
        this.workloadContext = workloadContext;
    }

    public EnvironmentContext getEnvironmentContext() {
        return environmentContext;
    }

    public void setEnvironmentContext(EnvironmentContext environmentContext) {
        this.environmentContext = environmentContext;
    }

    public List<GabConfig> getGabConfig() {
        return gabConfig;
    }

    public void setGabConfig(List<GabConfig> gabConfig) {
        this.gabConfig = gabConfig;
    }


    public static class CookieConfig {
        String securityKey = "";
        long maxSessionTimeOut = 600*1000;
        long sessionIdleTimeOut = 30*1000;
        String cookieName = "AuthenticationCookie";
        String redirectURL = "https://www.e-access.att.com/ecomp_portal_ist/ecompportal/process_csp";
        List<String> excludedUrls;
        List<String> onboardingExcludedUrls;
        String domain = "";
        String path = "";
        boolean isHttpOnly = true;

        public String getSecurityKey() {
            return securityKey;
        }

        public void setSecurityKey(String securityKey) {
            this.securityKey = securityKey;
        }

        public long getMaxSessionTimeOut() {
            return maxSessionTimeOut;
        }

        public void setMaxSessionTimeOut(long maxSessionTimeOut) {
            this.maxSessionTimeOut = maxSessionTimeOut;
        }

        public long getSessionIdleTimeOut() {
            return sessionIdleTimeOut;
        }

        public void setSessionIdleTimeOut(long sessionIdleTimeOut) {
            this.sessionIdleTimeOut = sessionIdleTimeOut;
        }

        public String getCookieName() {
            return cookieName;
        }

        public void setCookieName(String cookieName) {
            this.cookieName = cookieName;
        }

        public String getRedirectURL() {
            return redirectURL;
        }

        public void setRedirectURL(String redirectURL) {
            this.redirectURL = redirectURL;
        }

        public List<String> getExcludedUrls() {
            return excludedUrls;
        }

        public void setExcludedUrls(List<String> excludedUrls) {
            this.excludedUrls = excludedUrls;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isHttpOnly() {
            return isHttpOnly;
        }

        public void setIsHttpOnly(boolean isHttpOnly) {
            this.isHttpOnly = isHttpOnly;
        }

        public List<String> getOnboardingExcludedUrls() {
            return onboardingExcludedUrls;
        }

        public void setOnboardingExcludedUrls(List<String> onboardingExcludedUrls) {
            this.onboardingExcludedUrls = onboardingExcludedUrls;
        }
    }

    public CookieConfig getAuthCookie() {
        return authCookie;
    }

    public void setAuthCookie(CookieConfig authCookie) {
        this.authCookie = authCookie;
    }

    public static class CassandrConfig {
        private static final Integer CASSANDRA_DEFAULT_PORT = 9042;
        List<String> cassandraHosts;
        Integer cassandraPort;
        String localDataCenter;
        Long reconnectTimeout;
        Integer socketReadTimeout;
        Integer socketConnectTimeout;
        List<KeyspaceConfig> keySpaces;
        boolean authenticate;
        String username;
        String password;
        boolean ssl;
        String truststorePath;
        String truststorePassword;

        public Integer getCassandraPort() { return cassandraPort != null ? cassandraPort : Configuration.CassandrConfig.CASSANDRA_DEFAULT_PORT; }

        public void setCassandraPort(Integer cassandraPort) { this.cassandraPort = cassandraPort; }

        public String getLocalDataCenter() {
            return localDataCenter;
        }

        public void setLocalDataCenter(String localDataCenter) {
            this.localDataCenter = localDataCenter;
        }

        public boolean isAuthenticate() {
            return authenticate;
        }

        public void setAuthenticate(boolean authenticate) {
            this.authenticate = authenticate;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isSsl() {
            return ssl;
        }

        public void setSsl(boolean ssl) {
            this.ssl = ssl;
        }

        public String getTruststorePath() {
            return truststorePath;
        }

        public void setTruststorePath(String truststorePath) {
            this.truststorePath = truststorePath;
        }

        public String getTruststorePassword() {
            return truststorePassword;
        }

        public void setTruststorePassword(String truststorePassword) {
            this.truststorePassword = truststorePassword;
        }

        public Long getReconnectTimeout() {
            return reconnectTimeout;
        }

        public void setReconnectTimeout(Long reconnectTimeout) {
            this.reconnectTimeout = reconnectTimeout;
        }

        public Integer getSocketReadTimeout() { return socketReadTimeout; }

        public void setSocketReadTimeout(Integer socketReadTimeout) { this.socketReadTimeout = socketReadTimeout;}

        public Integer getSocketConnectTimeout() {	return socketConnectTimeout;}

        public void setSocketConnectTimeout(Integer socketConnectTimeout) { this.socketConnectTimeout = socketConnectTimeout; 	}

        public List<String> getCassandraHosts() {
            return cassandraHosts;
        }

        public void setCassandraHosts(List<String> cassandraHosts) {
            this.cassandraHosts = cassandraHosts;
        }

        public List<KeyspaceConfig> getKeySpaces() {
            return keySpaces;
        }

        public void setKeySpaces(List<KeyspaceConfig> cassandraConfig) {
            this.keySpaces = cassandraConfig;
        }

        public static class KeyspaceConfig {

            String name;
            String replicationStrategy;
            List<String> replicationInfo;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getReplicationStrategy() {
                return replicationStrategy;
            }

            public void setReplicationStrategy(String replicationStrategy) {
                this.replicationStrategy = replicationStrategy;
            }

            public List<String> getReplicationInfo() {
                return replicationInfo;
            }

            public void setReplicationInfo(List<String> replicationInfo) {
                this.replicationInfo = replicationInfo;
            }
        }
    }

    public static class SwitchoverDetectorConfig {

        String gBeFqdn;
        String gFeFqdn;
        String beVip;
        String feVip;
        int beResolveAttempts;
        int feResolveAttempts;
        Boolean enabled;
        long interval;
        String changePriorityUser;
        String changePriorityPassword;
        String publishNetworkUrl;
        String publishNetworkBody;
        Map<String, GroupInfo> groups;

        public String getgBeFqdn() {
            return gBeFqdn;
        }

        public void setgBeFqdn(String gBeFqdn) {
            this.gBeFqdn = gBeFqdn;
        }

        public String getgFeFqdn() {
            return gFeFqdn;
        }

        public void setgFeFqdn(String gFeFqdn) {
            this.gFeFqdn = gFeFqdn;
        }

        public String getBeVip() {
            return beVip;
        }

        public void setBeVip(String beVip) {
            this.beVip = beVip;
        }

        public String getFeVip() {
            return feVip;
        }

        public void setFeVip(String feVip) {
            this.feVip = feVip;
        }

        public int getBeResolveAttempts() {
            return beResolveAttempts;
        }

        public void setBeResolveAttempts(int beResolveAttempts) {
            this.beResolveAttempts = beResolveAttempts;
        }

        public int getFeResolveAttempts() {
            return feResolveAttempts;
        }

        public void setFeResolveAttempts(int feResolveAttempts) {
            this.feResolveAttempts = feResolveAttempts;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public long getInterval() {
            return interval;
        }

        public void setInterval(long interval) {
            this.interval = interval;
        }

        public String getChangePriorityUser() {
            return changePriorityUser;
        }

        public void setChangePriorityUser(String changePriorityUser) {
            this.changePriorityUser = changePriorityUser;
        }

        public String getChangePriorityPassword() {
            return changePriorityPassword;
        }

        public void setChangePriorityPassword(String changePriorityPassword) {
            this.changePriorityPassword = changePriorityPassword;
        }

        public String getPublishNetworkUrl() {
            return publishNetworkUrl;
        }

        public void setPublishNetworkUrl(String publishNetworkUrl) {
            this.publishNetworkUrl = publishNetworkUrl;
        }

        public String getPublishNetworkBody() {
            return publishNetworkBody;
        }

        public void setPublishNetworkBody(String publishNetworkBody) {
            this.publishNetworkBody = publishNetworkBody;
        }

        public Map<String, GroupInfo> getGroups() {
            return groups;
        }

        public void setGroups(Map<String, GroupInfo> groups) {
            this.groups = groups;
        }

        public static class GroupInfo {

            String changePriorityUrl;
            String changePriorityBody;

            public String getChangePriorityUrl() {
                return changePriorityUrl;
            }

            public void setChangePriorityUrl(String changePriorityUrl) {
                this.changePriorityUrl = changePriorityUrl;
            }

            public String getChangePriorityBody() {
                return changePriorityBody;
            }

            public void setChangePriorityBody(String changePriorityBody) {
                this.changePriorityBody = changePriorityBody;
            }
        }

    }

    public static class HeatDeploymentArtifactTimeout {
        Integer defaultMinutes;
        Integer minMinutes;
        Integer maxMinutes;

        public Integer getDefaultMinutes() {
            return defaultMinutes;
        }

        public void setDefaultMinutes(Integer defaultMinutes) {
            this.defaultMinutes = defaultMinutes;
        }

        public Integer getMinMinutes() {
            return minMinutes;
        }

        public void setMinMinutes(Integer minMinutes) {
            this.minMinutes = minMinutes;
        }

        public Integer getMaxMinutes() {
            return maxMinutes;
        }

        public void setMaxMinutes(Integer maxMinutes) {
            this.maxMinutes = maxMinutes;
        }

        @Override
        public String toString() {
            return "HeatDeploymentArtifactTimeout config [default=" + defaultMinutes + ", min=" + minMinutes + ", max=" + maxMinutes + "]";
        }
    }

    public static class BeMonitoringConfig {

        Boolean enabled;
        Boolean isProxy;
        Integer probeIntervalInSeconds;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public Boolean getIsProxy() {
            return isProxy;
        }

        public void setIsProxy(Boolean isProxy) {
            this.isProxy = isProxy;
        }

        public Integer getProbeIntervalInSeconds() {
            return probeIntervalInSeconds;
        }

        public Integer getProbeIntervalInSeconds(int defaultVal) {
            return probeIntervalInSeconds == null ? defaultVal : probeIntervalInSeconds;
        }

        public void setProbeIntervalInSeconds(Integer probeIntervalInSeconds) {
            this.probeIntervalInSeconds = probeIntervalInSeconds;
        }
    }

    public static class ArtifactTypeConfig {

        List<String> acceptedTypes;
        List<String> validForResourceTypes;

        public List<String> getValidForResourceTypes() {
            return validForResourceTypes;
        }

        public void setValidForResourceTypes(List<String> validForResourceTypes) {
            this.validForResourceTypes = validForResourceTypes;
        }

        public List<String> getAcceptedTypes() {
            return acceptedTypes;
        }

        public void setAcceptedTypes(List<String> acceptedTypes) {
            this.acceptedTypes = acceptedTypes;
        }
    }

    public static class OnboardingConfig {

        String protocol = "http";
        String host;
        Integer port;
        String downloadCsarUri;
        String healthCheckUri;

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getDownloadCsarUri() {
            return downloadCsarUri;
        }

        public void setDownloadCsarUri(String downloadCsarUri) {
            this.downloadCsarUri = downloadCsarUri;
        }

        public String getHealthCheckUri() {
            return healthCheckUri;
        }

        public void setHealthCheckUri(String healthCheckUri) {
            this.healthCheckUri = healthCheckUri;
        }

        @Override
        public String toString() {
            return "OnboardingConfig [protocol=" + protocol + ", host=" + host + ", port=" + port + ", downloadCsarUri="
                    + downloadCsarUri + "]";
        }

    }

    public DcaeConfig getDcae() {
        return dcae;
    }

    public void setDcae(DcaeConfig dcae) {
        this.dcae = dcae;
    }

    public static class DcaeConfig {

        String protocol = "http";
        String host;
        Integer port;
        String healthCheckUri;

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getHealthCheckUri() {
            return healthCheckUri;
        }

        public void setHealthCheckUri(String healthCheckUri) {
            this.healthCheckUri = healthCheckUri;
        }
    }

    public static class EcompPortalConfig {
        private String protocol = "https";
        private String host;
        private Integer port;
        private String healthCheckUri;
        private String defaultFunctionalMenu;

        public void setPollingInterval(Integer pollingInterval) {
            this.pollingInterval = pollingInterval;
        }

        public void setTimeoutMs(Integer timeoutMs) {
            this.timeoutMs = timeoutMs;
        }

        private Integer pollingInterval;
        private Integer timeoutMs;

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getHealthCheckUri() {
            return healthCheckUri;
        }

        public void setHealthCheckUri(String healthCheckUri) {
            this.healthCheckUri = healthCheckUri;
        }

        public String getDefaultFunctionalMenu() {
            return defaultFunctionalMenu;
        }

        public void setDefaultFunctionalMenu(String defaultFunctionalMenu) {
            this.defaultFunctionalMenu = defaultFunctionalMenu;
        }

        public Integer getPollingInterval() {
            return pollingInterval;
        }

        public Integer getTimeoutMs() {
            return timeoutMs;
        }

        @Override
        public String toString() {
            return "EcompPortalConfig [defaultFunctionalMenu=" + defaultFunctionalMenu + "]";
        }

    }

    public static class ApplicationL1CacheConfig {

        ApplicationL1CacheInfo datatypes;

        public ApplicationL1CacheInfo getDatatypes() {
            return datatypes;
        }

        public void setDatatypes(ApplicationL1CacheInfo datatypes) {
            this.datatypes = datatypes;
        }

        @Override
        public String toString() {
            return "ApplicationL1CacheConfig [datatypes=" + datatypes + "]";
        }

    }

    public static class ApplicationL2CacheConfig {

        boolean enabled;
        ApplicationL1CacheCatalogInfo catalogL1Cache;

        QueueInfo queue;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public ApplicationL1CacheCatalogInfo getCatalogL1Cache() {
            return catalogL1Cache;
        }

        public void setCatalogL1Cache(ApplicationL1CacheCatalogInfo catalogL1Cache) {
            this.catalogL1Cache = catalogL1Cache;
        }

        public QueueInfo getQueue() {
            return queue;
        }

        public void setQueue(QueueInfo queue) {
            this.queue = queue;
        }

        @Override
        public String toString() {
            return "ApplicationL2CacheConfig [enabled=" + enabled + ", catalogL1Cache=" + catalogL1Cache + "]";
        }

    }

    public static class ToscaValidatorsConfig {

        private Integer stringMaxLength;

        public Integer getStringMaxLength() {
            return stringMaxLength;
        }

        public void setStringMaxLength(Integer stringMaxLength) {
            this.stringMaxLength = stringMaxLength;
        }

        @Override
        public String toString() {
            return "ToscaValidatorsConfig [stringMaxLength=" + stringMaxLength + "]";
        }

    }

    public static class ApplicationL1CacheInfo {

        Boolean enabled;
        Integer firstRunDelay;
        Integer pollIntervalInSec;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public Integer getFirstRunDelay() {
            return firstRunDelay;
        }

        public void setFirstRunDelay(Integer firstRunDelay) {
            this.firstRunDelay = firstRunDelay;
        }

        public Integer getPollIntervalInSec() {
            return pollIntervalInSec;
        }

        public void setPollIntervalInSec(Integer pollIntervalInSec) {
            this.pollIntervalInSec = pollIntervalInSec;
        }

        @Override
        public String toString() {
            return "ApplicationL1CacheInfo [enabled=" + enabled + ", firstRunDelay=" + firstRunDelay
                    + ", pollIntervalInSec=" + pollIntervalInSec + "]";
        }
    }

    public static class ApplicationL1CacheCatalogInfo {

        Boolean enabled;
        Integer resourcesSizeInCache;
        Integer servicesSizeInCache;
        Integer productsSizeInCache;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public Integer getResourcesSizeInCache() {
            return resourcesSizeInCache;
        }

        public void setResourcesSizeInCache(Integer resourcesSizeInCache) {
            this.resourcesSizeInCache = resourcesSizeInCache;
        }

        public Integer getServicesSizeInCache() {
            return servicesSizeInCache;
        }

        public void setServicesSizeInCache(Integer servicesSizeInCache) {
            this.servicesSizeInCache = servicesSizeInCache;
        }

        public Integer getProductsSizeInCache() {
            return productsSizeInCache;
        }

        public void setProductsSizeInCache(Integer productsSizeInCache) {
            this.productsSizeInCache = productsSizeInCache;
        }

        @Override
        public String toString() {
            return "ApplicationL1CacheCatalogInfo [enabled=" + enabled + ", resourcesSizeInCache="
                    + resourcesSizeInCache + ", servicesSizeInCache=" + servicesSizeInCache + ", productsSizeInCache="
                    + productsSizeInCache + "]";
        }

    }

    public static class QueueInfo {
        Integer numberOfCacheWorkers;
        Integer waitOnShutDownInMinutes;
        Integer syncIntervalInSecondes;

        public Integer getWaitOnShutDownInMinutes() {
            return waitOnShutDownInMinutes;
        }

        public void setWaitOnShutDownInMinutes(Integer waitOnShutDownInMinutes) {
            this.waitOnShutDownInMinutes = waitOnShutDownInMinutes;
        }

        public Integer getSyncIntervalInSecondes() {
            return syncIntervalInSecondes;
        }

        public void setSyncIntervalInSecondes(Integer syncIntervalInSecondes) {
            this.syncIntervalInSecondes = syncIntervalInSecondes;
        }

        public Integer getNumberOfCacheWorkers() {
            return numberOfCacheWorkers;
        }

        public void setNumberOfCacheWorkers(Integer numberOfCacheWorkers) {
            this.numberOfCacheWorkers = numberOfCacheWorkers;
        }

        @Override
        public String toString() {
            return "QueueInfo[" + "waitOnShutDownInMinutes=" + waitOnShutDownInMinutes + ", syncIntervalInSecondes="
                    + syncIntervalInSecondes + ", numberOfCacheWorkers=" + this.numberOfCacheWorkers + ']';
        }
    }

    public static class EnvironmentContext {

        String defaultValue;
        List<String> validValues;

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public List<String> getValidValues() {
            return validValues;
        }

        public void setValidValues(List<String> validValues) {
            this.validValues = validValues;
        }
    }


    public CleanComponentsConfiguration getCleanComponentsConfiguration() {
        return cleanComponentsConfiguration;
    }

    public void setCleanComponentsConfiguration(CleanComponentsConfiguration cleanComponentsConfiguration) {
        this.cleanComponentsConfiguration = cleanComponentsConfiguration;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(format("backend host: %s%n", beFqdn))
                .append(format("backend http port: %s%n", beHttpPort))
                .append(format("backend ssl port: %s%n", beSslPort)).append(format("backend context: %s%n", beContext))
                .append(format("backend protocol: %s%n", beProtocol)).append(format("Version: %s%n", version))
                .append(format("Released: %s%n", released)).append(format("Supported protocols: %s%n", protocols))
                .append(format("Users: %s%n", users)).append(format("Neo4j: %s%n", neo4j))
                .append(format("JanusGraph Cfg File: %s%n", janusGraphCfgFile))
                .append(format("JanusGraph In memory: %s%n", janusGraphInMemoryGraph))
                .append(format("JanusGraph lock timeout: %s%n", janusGraphLockTimeout))
                .append(format("JanusGraph reconnect interval seconds: %s%n", janusGraphReconnectIntervalInSeconds))
                .append(format("excludeResourceCategory: %s%n", excludeResourceCategory))
                .append(format("informationalResourceArtifacts: %s%n", informationalResourceArtifacts))
                .append(format("deploymentResourceArtifacts: %s%n", deploymentResourceArtifacts))
                .append(format("informationalServiceArtifacts: %s%n", informationalServiceArtifacts))
                .append(format("Supported artifacts types: %s%n", artifactTypes))
                .append(format("Supported license types: %s%n", licenseTypes))
                .append(format("Additional information Maximum number of preoperties: %s%n",
                        additionalInformationMaxNumberOfKeys))
                .append(format("Heat Artifact Timeout in Minutes: %s%n", heatArtifactDeploymentTimeout))
                .append(format("URLs For HTTP Requests that will not be automatically logged : %s%n", unLoggedUrls))
                .append(format("Service Api Artifacts: %s%n", serviceApiArtifacts))
                .append(format("heat env artifact header: %s%n", heatEnvArtifactHeader))
                .append(format("heat env artifact footer: %s%n", heatEnvArtifactFooter))
                .append(format("onboarding: %s%n", onboarding)).toString();
    }

    public List<String> getUnLoggedUrls() {
        return unLoggedUrls;
    }

    public void setUnLoggedUrls(List<String> unLoggedUrls) {
        this.unLoggedUrls = unLoggedUrls;
    }

    public Map<String, Object> getDeploymentResourceArtifacts() {
        return deploymentResourceArtifacts;
    }

    public void setDeploymentResourceArtifacts(Map<String, Object> deploymentResourceArtifacts) {
        this.deploymentResourceArtifacts = deploymentResourceArtifacts;
    }

    public String getHeatEnvArtifactHeader() {
        return heatEnvArtifactHeader;
    }

    public void setHeatEnvArtifactHeader(String heatEnvArtifactHeader) {
        this.heatEnvArtifactHeader = heatEnvArtifactHeader;
    }

    public String getHeatEnvArtifactFooter() {
        return heatEnvArtifactFooter;
    }

    public void setHeatEnvArtifactFooter(String heatEnvArtifactFooter) {
        this.heatEnvArtifactFooter = heatEnvArtifactFooter;
    }

    public Map<String, Object> getDeploymentResourceInstanceArtifacts() {
        return deploymentResourceInstanceArtifacts;
    }

    public void setDeploymentResourceInstanceArtifacts(Map<String, Object> deploymentResourceInstanceArtifacts) {
        this.deploymentResourceInstanceArtifacts = deploymentResourceInstanceArtifacts;
    }

    public String getArtifactsIndex() {
        return artifactsIndex;
    }

    public void setArtifactsIndex(String artifactsIndex) {
        this.artifactsIndex = artifactsIndex;
    }

    public Map<String, ArtifactTypeConfig> getResourceInformationalDeployedArtifacts() {
        return resourceInformationalDeployedArtifacts;
    }

    public void setResourceInformationalDeployedArtifacts(
            Map<String, ArtifactTypeConfig> resourceInformationalDeployedArtifacts) {
        this.resourceInformationalDeployedArtifacts = resourceInformationalDeployedArtifacts;
    }

    public List<String> getResourceTypes() {
        return resourceTypes;
    }

    public void setResourceTypes(List<String> resourceTypes) {
        this.resourceTypes = resourceTypes;
    }

    public String getToscaFilesDir() {
        return toscaFilesDir;
    }

    public void setToscaFilesDir(String toscaFilesDir) {
        this.toscaFilesDir = toscaFilesDir;
    }

    public String getHeatTranslatorPath() {
        return heatTranslatorPath;
    }

    public void setHeatTranslatorPath(String heatTranslatorPath) {
        this.heatTranslatorPath = heatTranslatorPath;
    }

    public OnboardingConfig getOnboarding() {
        return onboarding;
    }

    public void setOnboarding(OnboardingConfig onboarding) {
        this.onboarding = onboarding;
    }

    public EcompPortalConfig getEcompPortal() {
        return ecompPortal;
    }

    public void setEcompPortal(EcompPortalConfig ecompPortal) {
        this.ecompPortal = ecompPortal;
    }

    public ToscaValidatorsConfig getToscaValidators() {
        return toscaValidators;
    }

    public void setToscaValidators(ToscaValidatorsConfig toscaValidators) {
        this.toscaValidators = toscaValidators;
    }

    public boolean isDisableAudit() {
        return disableAudit;
    }

    public Boolean getConsumerBusinessLogic() {
        return consumerBusinessLogic;
    }

    public void setConsumerBusinessLogic(Boolean consumerBusinessLogic) {
        this.consumerBusinessLogic = consumerBusinessLogic;
    }

    public void setDisableAudit(boolean enableAudit) {
        this.disableAudit = enableAudit;
    }

    public Map<String, ArtifactTypeConfig> getResourceInformationalArtifacts() {
        return resourceInformationalArtifacts;
    }

    public void setResourceInformationalArtifacts(Map<String, ArtifactTypeConfig> resourceInformationalArtifacts) {
        this.resourceInformationalArtifacts = resourceInformationalArtifacts;
    }

    public Map<String, VfModuleProperty> getVfModuleProperties() {
        return vfModuleProperties;
    }

    public void setVfModuleProperties(Map<String, VfModuleProperty> vfModuleProperties) {
        this.vfModuleProperties = vfModuleProperties;
    }

    public String getToscaConformanceLevel() {
        return toscaConformanceLevel;
    }

    public void setToscaConformanceLevel(String toscaConformanceLevel) {
        this.toscaConformanceLevel = toscaConformanceLevel;
    }

    public String getMinToscaConformanceLevel() {
        return minToscaConformanceLevel;
    }

    public void setMinToscaConformanceLevel(String toscaConformanceLevel) {
        this.minToscaConformanceLevel = toscaConformanceLevel;
    }

    public static class VfModuleProperty {
        private String forBaseModule;
        private String forNonBaseModule;
        public String getForBaseModule() {
            return forBaseModule;
        }
        public void setForBaseModule(String forBaseModule) {
            this.forBaseModule = forBaseModule;
        }
        public String getForNonBaseModule() {
            return forNonBaseModule;
        }
        public void setForNonBaseModule(String forNonBaseModule) {
            this.forNonBaseModule = forNonBaseModule;
        }
    }

    public List<Map<String, Map<String, String>>> getDefaultImports() {
        return defaultImports;
    }

    public void setDefaultImports(List<Map<String, Map<String, String>>> defaultImports) {
        this.defaultImports = defaultImports;
    }

    public Map<String, List<String>> getResourcesForUpgrade() {
        return resourcesForUpgrade;
    }

    public void setResourcesForUpgrade(Map<String, List<String>> resourcesForUpgrade) {
        this.resourcesForUpgrade = resourcesForUpgrade;
    }

    @SuppressWarnings("unchecked")
    public static <K,V> Map<K,V> safeGetCapsInsensitiveMap(Map<K,V> map) {
        return map == null ? emptyMap() : new CaseInsensitiveMap(map);
    }


    public List<String> getHealthStatusExclude() {
        return healthStatusExclude;
    }

    public void setHealthStatusExclude(List<String> healthStatusExclude) {
        this.healthStatusExclude = healthStatusExclude;
    }

    public DmaapProducerConfiguration getDmaapProducerConfiguration() {
        return dmaapProducerConfiguration;
    }

    public void setDmaapProducerConfiguration(DmaapProducerConfiguration dmaapProducerConfiguration) {
        this.dmaapProducerConfiguration = dmaapProducerConfiguration;
    }

    public String getAafNamespace() {
        return aafNamespace;
    }

    public void setAafNamespace(String aafNamespace) {
        this.aafNamespace = aafNamespace;
    }

    public Boolean getAafAuthNeeded(){
        return aafAuthNeeded;
    }

    public void setAafAuthNeeded(Boolean aafAuthNeeded){
        this.aafAuthNeeded = aafAuthNeeded;
    }

    public CadiFilterParams getCadiFilterParams() {
        return cadiFilterParams;
    }

    public void setCadiFilterParams(CadiFilterParams cadiFilterParams) {
        this.cadiFilterParams = cadiFilterParams;
    }


    public static class PathsAndNamesDefinition {
        private String friendlyName;
        private String path;
        private String searchable;

        public String getFriendlyName() {
            return friendlyName;
        }

        public String getPath() {
            return path;
        }

        public String getSearchable() {
            return searchable;
        }

        public void setFriendlyName(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public void setSearchable(String searchable) {
            this.searchable = searchable;
        }
    }

    public static class GabConfig {
        private String artifactType;
        private List<PathsAndNamesDefinition> pathsAndNamesDefinitions;

        public String getArtifactType() {
            return artifactType;
        }

        public List<PathsAndNamesDefinition> getPathsAndNamesDefinitions() {
            return pathsAndNamesDefinitions;
        }

        public void setArtifactType(String artifactType) {
            this.artifactType = artifactType;
        }

        public void setPathsAndNamesDefinitions(List<PathsAndNamesDefinition> pathsAndNamesDefinitions) {
            this.pathsAndNamesDefinitions = pathsAndNamesDefinitions;
        }
    }

}
