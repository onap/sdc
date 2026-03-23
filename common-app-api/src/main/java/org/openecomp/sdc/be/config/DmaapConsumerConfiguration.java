/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import lombok.Getter;
import lombok.Setter;

/**
 * Contains DMAAP Client configuration parameters
 */
@Getter
@Setter
public class DmaapConsumerConfiguration {

    private boolean active;
    private String hosts;
    private String consumerGroup;
    private String consumerId;
    private Integer timeoutMs;
    private Integer limit;
    private Integer pollingInterval;
    private String topic;
    private Double latitude;
    private Double longitude;
    private String version;
    private String serviceName;
    private String environment;
    private String partner;
    private String routeOffer;
    private String protocol;
    private String contenttype;
    private Boolean dme2TraceOn;
    private String aftEnvironment;
    private Integer aftDme2ConnectionTimeoutMs;
    private Integer aftDme2RoundtripTimeoutMs;
    private Integer aftDme2ReadTimeoutMs;
    private String dme2preferredRouterFilePath;
    private Credential credential;
    private Integer timeLimitForNotificationHandleMs;
    private boolean aftDme2SslEnable;
    private boolean aftDme2ClientIgnoreSslConfig;
    private String aftDme2ClientKeystore;
    private String aftDme2ClientKeystorePassword;
    private String aftDme2ClientSslCertAlias;

    @Override
    public String toString() {
        return "DmaapConsumerConfiguration [active=" + active + ", hosts=" + hosts + ", consumerGroup=" + consumerGroup + ", consumerId=" + consumerId
            + ", timeoutMs=" + timeoutMs + ", limit=" + limit + ", pollingInterval=" + pollingInterval + ", topic=" + topic + ", latitude=" + latitude
            + ", longitude=" + longitude + ", version=" + version + ", serviceName=" + serviceName + ", environment=" + environment + ", partner="
            + partner + ", routeOffer=" + routeOffer + ", protocol=" + protocol + ", contenttype=" + contenttype + ", dme2TraceOn=" + dme2TraceOn
            + ", aftEnvironment=" + aftEnvironment + ", aftDme2ConnectionTimeoutMs=" + aftDme2ConnectionTimeoutMs + ", aftDme2RoundtripTimeoutMs="
            + aftDme2RoundtripTimeoutMs + ", aftDme2ReadTimeoutMs=" + aftDme2ReadTimeoutMs + ", dme2preferredRouterFilePath="
            + dme2preferredRouterFilePath + ", credential=" + credential + ", timeLimitForNotificationHandleMs=" + timeLimitForNotificationHandleMs
            + ", aftDme2SslEnable=" + aftDme2SslEnable + ", aftDme2ClientIgnoreSslConfig=" + aftDme2ClientIgnoreSslConfig + ", aftDme2ClientKeystore="
            + aftDme2ClientKeystore + ", aftDme2ClientKeystorePassword=" + aftDme2ClientKeystorePassword + ", aftDme2ClientSslCertAlias="
            + aftDme2ClientSslCertAlias + "]";
    }

    /**
     * Contains Dmaap Client credential parameters: username and password
     */
    @Getter
    @Setter
    public static class Credential {

        private String username;
        private String password;

        @Override
        public String toString() {
            return "Credential [username=" + username + ", password=" + password + "]";
        }
    }
}
