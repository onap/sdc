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

import static java.lang.String.format;
import static java.util.Collections.emptyMap;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.openecomp.sdc.common.api.BasicConfiguration;

@Getter
@Setter
@NoArgsConstructor
public class Configuration extends BasicConfiguration {

    private List<String> identificationHeaderFields;
    /**
     * Requests from these Urls will not be logged by org.openecomp.sdc.be.filters.BeServletFilter.<br>
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
    private List<String> globalCsarImports;
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
    private Map<String, Object> serviceApiArtifacts;
    private List<String> excludeServiceCategory;
    private List<String> licenseTypes;
    private List<String> definedResourceNamespace;
    private List<String> directives;
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
    private BasicAuthConfig basicAuth;
    private CassandrConfig cassandraConfig;
    private SwitchoverDetectorConfig switchoverDetector;
    private ApplicationL1CacheConfig applicationL1Cache;
    private ApplicationL2CacheConfig applicationL2Cache;
    private ToscaValidatorsConfig toscaValidators;
    private boolean disableAudit;
    private Boolean consumerBusinessLogic;
    private Map<String, VfModuleProperty> vfModuleProperties;
    private Map<String, String> genericAssetNodeTypes;
    private Map<String, CategoryBaseTypeConfig> serviceBaseNodeTypes;
    private Map<String, Map<String, String>> resourceNodeTypes;
    private String appVersion;
    private String artifactGeneratorConfig;
    private CadiFilterParams cadiFilterParams;
    private Boolean aafAuthNeeded = false;
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
    private List<GabConfig> gabConfig;
    private EcompPortalConfig ecompPortal;
    private List<ArtifactConfiguration> artifacts;
    private Map<String, Map<String, List<String>>> componentAllowedInstanceTypes;
    private ExternalCsarStore externalCsarStore;
    private CsarFormat csarFormat;
    private String componentInstanceCounterDelimiter;

    @SuppressWarnings("unchecked")
    private <K, V> Map<K, V> safeGetCapsInsensitiveMap(Map<K, V> map) {
        return map == null ? emptyMap() : new CaseInsensitiveMap(map);
    }

    public Long getJanusGraphHealthCheckReadTimeout(long defaultVal) {
        return janusGraphHealthCheckReadTimeout == null ? defaultVal : janusGraphHealthCheckReadTimeout;
    }

    public Long getJanusGraphReconnectIntervalInSeconds(long defaultVal) {
        return janusGraphReconnectIntervalInSeconds == null ? defaultVal : janusGraphReconnectIntervalInSeconds;
    }

    public Map<String, Set<String>> getExcludedPolicyTypesMapping() {
        return safeGetCapsInsensitiveMap(excludedPolicyTypesMapping);
    }

    public Map<String, Set<String>> getExcludedGroupTypesMapping() {
        return safeGetCapsInsensitiveMap(excludedGroupTypesMapping);
    }

    public List<Map<String, Map<String, String>>> getDefaultImports() {
        return Collections.unmodifiableList(defaultImports);
    }

    @Override
    public String toString() {
        return new StringBuilder().append(format("backend host: %s%n", beFqdn)).append(format("backend http port: %s%n", beHttpPort))
            .append(format("backend ssl port: %s%n", beSslPort)).append(format("backend context: %s%n", beContext))
            .append(format("backend protocol: %s%n", beProtocol)).append(format("Version: %s%n", version)).append(format("Released: %s%n", released))
            .append(format("Supported protocols: %s%n", protocols)).append(format("Users: %s%n", users)).append(format("Neo4j: %s%n", neo4j))
            .append(format("JanusGraph Cfg File: %s%n", janusGraphCfgFile)).append(format("JanusGraph In memory: %s%n", janusGraphInMemoryGraph))
            .append(format("JanusGraph lock timeout: %s%n", janusGraphLockTimeout))
            .append(format("JanusGraph reconnect interval seconds: %s%n", janusGraphReconnectIntervalInSeconds))
            .append(format("excludeResourceCategory: %s%n", excludeResourceCategory))
            .append(format("informationalResourceArtifacts: %s%n", informationalResourceArtifacts))
            .append(format("deploymentResourceArtifacts: %s%n", deploymentResourceArtifacts))
            .append(format("informationalServiceArtifacts: %s%n", informationalServiceArtifacts))
            .append(format("Supported artifacts types: %s%n", artifacts)).append(format("Supported license types: %s%n", licenseTypes))
            .append(format("Additional information Maximum number of preoperties: %s%n", additionalInformationMaxNumberOfKeys))
            .append(format("Heat Artifact Timeout in Minutes: %s%n", heatArtifactDeploymentTimeout))
            .append(format("URLs For HTTP Requests that will not be automatically logged : %s%n", unLoggedUrls))
            .append(format("Service Api Artifacts: %s%n", serviceApiArtifacts))
            .append(format("heat env artifact header: %s%n", heatEnvArtifactHeader))
            .append(format("heat env artifact footer: %s%n", heatEnvArtifactFooter)).append(format("onboarding: %s%n", onboarding)).toString();
    }

    public List<String> getGlobalCsarImports() {
        if (CollectionUtils.isEmpty(globalCsarImports)) {
            return Collections.emptyList();
        }
        return globalCsarImports;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CookieConfig {

        private String securityKey = "";
        private long maxSessionTimeOut = 600 * 1000L;
        private long sessionIdleTimeOut = 30 * 1000L;
        private String cookieName = "AuthenticationCookie";
        private String redirectURL = "https://www.e-access.att.com/ecomp_portal_ist/ecompportal/process_csp";
        private List<String> excludedUrls;
        private List<String> onboardingExcludedUrls;
        private String domain = "";
        private String path = "";
        private boolean isHttpOnly = true;

        public boolean isHttpOnly() {
            return isHttpOnly;
        }

        public void setIsHttpOnly(final boolean isHttpOnly) {
            this.isHttpOnly = isHttpOnly;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CassandrConfig {

        private static final Integer CASSANDRA_DEFAULT_PORT = 9042;
        private List<String> cassandraHosts;
        private Integer cassandraPort;
        private String localDataCenter;
        private Long reconnectTimeout;
        private Integer socketReadTimeout;
        private Integer socketConnectTimeout;
        private List<KeyspaceConfig> keySpaces;
        private boolean authenticate;
        private String username;
        private String password;
        private boolean ssl;
        private String truststorePath;
        private String truststorePassword;
        private int maxWaitSeconds = 120;

        public Integer getCassandraPort() {
            return cassandraPort != null ? cassandraPort : Configuration.CassandrConfig.CASSANDRA_DEFAULT_PORT;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        public static class KeyspaceConfig {

            private String name;
            private String replicationStrategy;
            private List<String> replicationInfo;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SwitchoverDetectorConfig {

        private String gBeFqdn;
        private String gFeFqdn;
        private String beVip;
        private String feVip;
        private int beResolveAttempts;
        private int feResolveAttempts;
        private Boolean enabled;
        private long interval;
        private String changePriorityUser;
        private String changePriorityPassword;
        private String publishNetworkUrl;
        private String publishNetworkBody;
        private Map<String, GroupInfo> groups;

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

        @Getter
        @Setter
        @NoArgsConstructor
        public static class GroupInfo {

            String changePriorityUrl;
            String changePriorityBody;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class HeatDeploymentArtifactTimeout {

        private Integer defaultMinutes;
        private Integer minMinutes;
        private Integer maxMinutes;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class BeMonitoringConfig {

        private Boolean enabled;
        private Boolean isProxy;
        private Integer probeIntervalInSeconds;

        public Integer getProbeIntervalInSeconds(int defaultVal) {
            return probeIntervalInSeconds == null ? defaultVal : probeIntervalInSeconds;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ArtifactTypeConfig {

        private List<String> acceptedTypes;
        private List<String> validForResourceTypes;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class OnboardingConfig {

        private String protocol = "http";
        private String host;
        private Integer port;
        private String getLatestVspPackageUri;
        private String getVspPackageUri;
        private String getVspUri;
        private String getLatestVspUri;
        @ToString.Exclude
        private String healthCheckUri;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class BasicAuthConfig {

        private boolean enabled;
        private String userName;
        private String userPass;
        private String excludedUrls;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString(onlyExplicitlyIncluded = true)
    public static class EcompPortalConfig {

        private String protocol = "https";
        private String host;
        private Integer port;
        private String healthCheckUri;
        @ToString.Include
        private String defaultFunctionalMenu;
        private Integer pollingInterval;
        private Integer timeoutMs;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class ApplicationL1CacheConfig {

        private ApplicationL1CacheInfo datatypes;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class ApplicationL2CacheConfig {

        private boolean enabled;
        private ApplicationL1CacheCatalogInfo catalogL1Cache;
        @ToString.Exclude
        private QueueInfo queue;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class ToscaValidatorsConfig {

        private Integer stringMaxLength;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class ApplicationL1CacheInfo {

        private Boolean enabled;
        private Integer firstRunDelay;
        private Integer pollIntervalInSec;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class ApplicationL1CacheCatalogInfo {

        private Boolean enabled;
        private Integer resourcesSizeInCache;
        private Integer servicesSizeInCache;
        private Integer productsSizeInCache;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class QueueInfo {

        private Integer waitOnShutDownInMinutes;
        private Integer syncIntervalInSecondes;
        private Integer numberOfCacheWorkers;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class EnvironmentContext {

        private String defaultValue;
        private List<String> validValues;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class VfModuleProperty {

        private String forBaseModule;
        private String forNonBaseModule;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PathsAndNamesDefinition {

        private String friendlyName;
        private String path;
        private Boolean searchable;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class GabConfig {

        private String artifactType;
        private List<PathsAndNamesDefinition> pathsAndNamesDefinitions;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ExternalCsarStore {

        private String storageType;
        private Endpoint endpoint;
        private Credentials credentials;
        private String tempPath;
        private int uploadPartSize;

        @Getter
        @Setter
        @NoArgsConstructor
        public static class Endpoint {

            private String host;
            private int port;
            private boolean secure;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        public static class Credentials {

            private String accessKey;
            private String secretKey;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CsarFormat {

        private String defaultFormat;
    }

}
