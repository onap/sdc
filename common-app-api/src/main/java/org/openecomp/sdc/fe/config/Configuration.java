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

import static java.lang.String.format;

import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openecomp.sdc.common.api.BasicConfiguration;

@Getter
@Setter
@NoArgsConstructor
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
    private String onboardingForwardContext;
    private OnboardingConfig onboarding;
    private CookieConfig authCookie;
    private BasicAuthConfig basicAuth;
    private CatalogFacadeMsConfig catalogFacadeMs;
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
    private String permittedAncestors; // Space separated list of permitted ancestors

    public Integer getHealthCheckSocketTimeoutInMs(int defaultVal) {
        return healthCheckSocketTimeoutInMs == null ? defaultVal : healthCheckSocketTimeoutInMs;
    }

    public Integer getHealthCheckIntervalInSeconds(int defaultVal) {
        return healthCheckIntervalInSeconds == null ? defaultVal : healthCheckIntervalInSeconds;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(format("backend host: %s%n", beHost)).append(format("backend http port: %s%n", beHttpPort))
            .append(format("backend ssl port: %s%n", beSslPort)).append(format("backend context: %s%n", beContext))
            .append(format("backend protocol: %s%n", beProtocol)).append(format("onboarding forward context: %s%n", onboardingForwardContext))
            .append(format("Version: %s%n", version)).append(format("Released: %s%n", released))
            .append(format("Connecting to database: %s%n", connection)).append(format("Supported protocols: %s%n", protocols)).toString();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class FeMonitoringConfig {

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
    public static class OnboardingConfig {

        private String protocolFe = "http";
        private String hostFe;
        private Integer portFe;
        private String protocolBe = "http";
        private String hostBe;
        private Integer portBe;
        private String healthCheckUriFe;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CookieConfig {

        private String cookieName = "AuthenticationCookie";
        private String path = "";
        private String domain = "";
        private String securityKey = "";
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class BasicAuthConfig {

        private boolean enabled = false;
        private String userName = "";
        private String userPass = "";
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CatalogFacadeMsConfig {

        private String protocol;
        private String host;
        private Integer port;
        private String healthCheckUri;
        private String path;
    }
}
