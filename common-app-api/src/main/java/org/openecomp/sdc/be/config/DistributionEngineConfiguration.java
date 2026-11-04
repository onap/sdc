/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.openecomp.sdc.common.api.BasicConfiguration;
import org.openecomp.sdc.common.http.config.ExternalServiceConfig;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class DistributionEngineConfiguration extends BasicConfiguration {

    public static final int DEFAULT_KAFKA_HEALTH_CHECK_INTERVAL_SEC = 30;
    public static final int DEFAULT_KAFKA_HEALTH_CHECK_TIMEOUT_SEC = 10;

    /** Ignored; kept until OOM stops rendering the key (YamlToObjectConverter rejects unknown keys). */
    @Deprecated
    private List<String> uebServers;
    private String distributionNotifTopicName;
    private String distributionStatusTopicName;
    private String kafkaBootStrapServers;
    /** Ignored; kept until OOM stops rendering the key (YamlToObjectConverter rejects unknown keys). */
    @Deprecated
    private Integer initRetryIntervalSec;
    /** Ignored; kept until OOM stops rendering the key (YamlToObjectConverter rejects unknown keys). */
    @Deprecated
    private Integer initMaxIntervalSec;
    private ComponentArtifactTypesConfig distribNotifServiceArtifactTypes;
    private ComponentArtifactTypesConfig distribNotifResourceArtifactTypes;
    /** Ignored; kept until OOM stops rendering the key (YamlToObjectConverter rejects unknown keys). */
    @Deprecated
    private String uebPublicKey;
    /** Ignored; kept until OOM stops rendering the key (YamlToObjectConverter rejects unknown keys). */
    @Deprecated
    private String uebSecretKey;
    private List<String> environments;
    private DistributionStatusTopicConfig distributionStatusTopic;
    /** Ignored; kept until OOM stops rendering the key (YamlToObjectConverter rejects unknown keys). */
    @Deprecated
    private CreateTopicConfig createTopic;
    private boolean startDistributionEngine;
    private DistributionNotificationTopicConfig distributionNotificationTopic;
    private Integer defaultArtifactInstallationTimeout = 60;
    private Integer currentArtifactInstallationTimeout = 120;
    /** Ignored; kept until OOM stops rendering the key (YamlToObjectConverter rejects unknown keys). */
    @Deprecated
    private boolean useHttpsWithDmaap;
    /** Ignored; kept until OOM stops rendering the key (YamlToObjectConverter rejects unknown keys). */
    @Deprecated
    private ExternalServiceConfig aaiConfig;
    private ExternalServiceConfig msoConfig;
    private Integer opEnvRecoveryIntervalSec;
    /** Ignored; kept until OOM stops rendering the key (YamlToObjectConverter rejects unknown keys). */
    @Deprecated
    private Integer allowedTimeBeforeStaleSec;
    private String distributionDeleteTopicName;
    private DistributionDeleteTopicConfig distributionDeleteTopic;
    private Integer kafkaHealthCheckIntervalSec = DEFAULT_KAFKA_HEALTH_CHECK_INTERVAL_SEC;
    private Integer kafkaHealthCheckTimeoutSec = DEFAULT_KAFKA_HEALTH_CHECK_TIMEOUT_SEC;

    public void setEnvironments(List<String> environments) {
        Set<String> set = new LinkedHashSet<>();
        if (environments != null) {
            set.addAll(environments);
            this.environments = new ArrayList<>(set);
        } else {
            this.environments = null;
        }
    }

    @Getter
    @AllArgsConstructor
    public enum ArtifcatTypeEnum {
        MURANO_PKG("MURANO-PKG"),
        HEAT("HEAT"),
        DG_XML("DG_XML");
        private final String value;

    }

    @Getter
    @Setter
    public static class DistribNotifServiceArtifacts {

        private Map<String, Object> service;
        private Map<String, Object> resource;

    }

    @Getter
    @Setter
    public static class NotifArtifactTypes {

        private List<String> info;
        private List<String> lifecycle;

    }

    @Getter
    @Setter
    public static class NotifArtifactTypesResource {

        private List<ArtifcatTypeEnum> lifecycle;
    }

    /** Ignored; kept until OOM stops rendering the key (YamlToObjectConverter rejects unknown keys). */
    @Deprecated
    @Getter
    @Setter
    @ToString
    public static class CreateTopicConfig {

        private Integer partitionCount;
        private Integer replicationCount;

    }

    @Getter
    @Setter
    @ToString
    public static class DistributionStatusTopicConfig {

        private Integer pollingIntervalSec;
        private Integer fetchTimeSec;
        private String consumerGroup;
        private String consumerId;

    }

    @Getter
    @Setter
    @ToString
    public static class DistributionNotificationTopicConfig {

        private Integer maxWaitingAfterSendingSeconds;
        private Integer maxThreadPoolSize;
        private Integer minThreadPoolSize;

    }

    @Getter
    @Setter
    @ToString
    public static class ComponentArtifactTypesConfig {

        private List<String> info;
        private List<String> lifecycle;

    }

    @Getter
    @Setter
    @ToString
    public static class DistributionDeleteTopicConfig {

        private Integer maxWaitingAfterSendingSeconds;
        private Integer maxThreadPoolSize;
        private Integer minThreadPoolSize;
    }
}
