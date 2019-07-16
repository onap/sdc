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

package org.openecomp.sdc.fe.config;

import org.openecomp.sdc.common.api.BasicConfiguration;

import java.util.Date;
import java.util.List;

import static java.lang.String.format;

public class Configuration extends BasicConfiguration {
    /**
     * fe FQDN
     */
    private String feFqdn;
    /**
     * backend host
     */
    private String beHost;
    /**
     * backend http port
     */
    private Integer beHttpPort;
    /**
     * backend http secured port
     */
    private Integer beSslPort;

    private Integer healthCheckSocketTimeoutInMs;

    private Integer healthCheckIntervalInSeconds;

    private List<String> healthStatusExclude;

    private FeMonitoringConfig systemMonitoring;

    private String kibanaHost;

    private Integer kibanaPort;

    private String kibanaProtocol;

    private String onboardingForwardContext;

    private OnboardingConfig onboarding;

    private DcaeConfig dcae;
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
    private Connection connection;
    private List<String> protocols;
    private int threadpoolSize;
    private int requestTimeout;
    private List<List<String>> identificationHeaderFields;
    private List<List<String>> optionalHeaderFields;
    private List<String> forwardHeaderFields;

    public String getKibanaProtocol() {
        return kibanaProtocol;
    }

    public void setKibanaProtocol(String kibanaProtocol) {
        this.kibanaProtocol = kibanaProtocol;
    }

    public String getKibanaHost() {
        return kibanaHost;
    }

    public void setKibanaHost(String kibanaHost) {
        this.kibanaHost = kibanaHost;
    }

    public Integer getKibanaPort() {
        return kibanaPort;
    }

    public void setKibanaPort(Integer kibanaPort) {
        this.kibanaPort = kibanaPort;
    }

    public FeMonitoringConfig getSystemMonitoring() {
        return systemMonitoring;
    }

    public void setSystemMonitoring(FeMonitoringConfig systemMonitoring) {
        this.systemMonitoring = systemMonitoring;
    }

    public Integer getHealthCheckSocketTimeoutInMs() {
        return healthCheckSocketTimeoutInMs;
    }

    public void setHealthCheckSocketTimeoutInMs(Integer healthCheckSocketTimeout) {
        this.healthCheckSocketTimeoutInMs = healthCheckSocketTimeout;
    }

    public Integer getHealthCheckSocketTimeoutInMs(int defaultVal) {
        return healthCheckSocketTimeoutInMs == null ? defaultVal : healthCheckSocketTimeoutInMs;
    }

    public Integer getHealthCheckIntervalInSeconds() {
        return healthCheckIntervalInSeconds;
    }

    public void setHealthCheckIntervalInSeconds(Integer healthCheckInterval) {
        this.healthCheckIntervalInSeconds = healthCheckInterval;
    }

    public Integer getHealthCheckIntervalInSeconds(int defaultVal) {
        return healthCheckIntervalInSeconds == null ? defaultVal : healthCheckIntervalInSeconds;
    }

    public Date getReleased() {
        return released;
    }

    public void setReleased(Date released) {
        this.released = released;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public List<String> getProtocols() {
        return protocols;
    }

    public void setProtocols(List<String> protocols) {
        this.protocols = protocols;
    }

    public String getBeHost() {
        return beHost;
    }

    public void setBeHost(String beHost) {
        this.beHost = beHost;
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

    public int getThreadpoolSize() {
        return threadpoolSize;
    }

    public void setThreadpoolSize(int threadpoolSize) {
        this.threadpoolSize = threadpoolSize;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public List<List<String>> getIdentificationHeaderFields() {
        return identificationHeaderFields;
    }

    public void setIdentificationHeaderFields(List<List<String>> identificationHeaderFields) {
        this.identificationHeaderFields = identificationHeaderFields;
    }

    public List<List<String>> getOptionalHeaderFields() {
        return optionalHeaderFields;
    }

    public void setOptionalHeaderFields(List<List<String>> optionalHeaderFields) {
        this.optionalHeaderFields = optionalHeaderFields;
    }

    public List<String> getForwardHeaderFields() {
        return forwardHeaderFields;
    }

    public void setForwardHeaderFields(List<String> forwardHeaderFields) {
        this.forwardHeaderFields = forwardHeaderFields;
    }

    public String getFeFqdn() {
        return feFqdn;
    }

    public void setFeFqdn(String feFqdn) {
        this.feFqdn = feFqdn;
    }

    public OnboardingConfig getOnboarding() {
        return onboarding;
    }

    public void setOnboarding(OnboardingConfig onboarding) {
        this.onboarding = onboarding;
    }

    public DcaeConfig getDcae() {
        return dcae;
    }

    public void setDcae(DcaeConfig dcae) {
        this.dcae = dcae;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(format("backend host: %s%n", beHost))
                .append(format("backend http port: %s%n", beHttpPort))
                .append(format("backend ssl port: %s%n", beSslPort)).append(format("backend context: %s%n", beContext))
                .append(format("backend protocol: %s%n", beProtocol))
                .append(format("onboarding forward context: %s%n", onboardingForwardContext))
                .append(format("Version: %s%n", version)).append(format("Released: %s%n", released))
                .append(format("Connecting to database: %s%n", connection))
                .append(format("Supported protocols: %s%n", protocols)).toString();
    }

    public static class FeMonitoringConfig {

        private Boolean enabled;
        private Boolean isProxy;
        private Integer probeIntervalInSeconds;

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

        public void setProbeIntervalInSeconds(Integer probeIntervalInSeconds) {
            this.probeIntervalInSeconds = probeIntervalInSeconds;
        }

        public Integer getProbeIntervalInSeconds(int defaultVal) {
            return probeIntervalInSeconds == null ? defaultVal : probeIntervalInSeconds;
        }
    }

    public static class OnboardingConfig {
        private String protocolFe = "http";
        private String hostFe;
        private Integer portFe;
        private String protocolBe = "http";
        private String hostBe;
        private Integer portBe;
        private String healthCheckUriFe;

        public String getProtocolFe() {
            return protocolFe;
        }

        public void setProtocolFe(String protocolFe) {
            this.protocolFe = protocolFe;
        }

        public String getProtocolBe() {
            return protocolBe;
        }

        public void setProtocolBe(String protocolBe) {
            this.protocolBe = protocolBe;
        }

        public String getHostFe() {
            return hostFe;
        }

        public void setHostFe(String hostFe) {
            this.hostFe = hostFe;
        }

        public String getHostBe() {
            return hostBe;
        }

        public void setHostBe(String hostBe) {
            this.hostBe = hostBe;
        }

        public Integer getPortFe() {
            return portFe;
        }

        public void setPortFe(Integer portFe) {
            this.portFe = portFe;
        }

        public Integer getPortBe() {
            return portBe;
        }

        public void setPortBe(Integer portBe) {
            this.portBe = portBe;
        }

        public String getHealthCheckUriFe() {
            return healthCheckUriFe;
        }

        public void setHealthCheckUriFe(String healthCheckUriFe) {
            this.healthCheckUriFe = healthCheckUriFe;
        }
    }

    public static class DcaeConfig {

        private String protocol = "http";
        private String host;
        private Integer port;
        private String healthCheckUri;

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

    public List<String> getHealthStatusExclude() {
        return healthStatusExclude;
    }

    public void setHealthStatusExclude(List<String> healthStatusExclude) {
        this.healthStatusExclude = healthStatusExclude;
    }

}
