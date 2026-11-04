/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2023 Nordix Foundation. All rights reserved.
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
package org.openecomp.sdc.be.components.kafka;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Stream;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.onap.config.api.JettySSLUtils;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.DistributionDeleteTopicConfig;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.DistributionNotificationTopicConfig;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.DistributionStatusTopicConfig;

public class KafkaCommonConfig {

    private final DistributionEngineConfiguration deConfiguration;
    private final Map<String, String> environment;

    public KafkaCommonConfig(DistributionEngineConfiguration config, Map<String, String> environment){
        this.deConfiguration = config;
        this.environment = environment;
    }

    public Properties getConsumerProperties(){
        Properties props = new Properties();
        setCommonProperties(props);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,  "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId("consumer"));
        statusTopic().map(DistributionStatusTopicConfig::getConsumerGroup).ifPresent(group -> props.put(ConsumerConfig.GROUP_ID_CONFIG, group));
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        return props;
    }

    public Properties getProducerProperties(){
        Properties props = new Properties();
        setCommonProperties(props);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,  "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId("producer"));
        // send() waits up to max.block.ms (default 60 s) for broker metadata before the caller's send timeout even starts
        maxSendTimeoutSeconds().ifPresent(seconds -> props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, String.valueOf(seconds * 1000L)));
        return props;
    }

    public Properties getAdminProperties() {
        Properties props = new Properties();
        setCommonProperties(props);
        props.put(CommonClientConfigs.CLIENT_ID_CONFIG, clientId("admin"));
        return props;
    }

    /**
     * @return why the Kafka clients cannot authenticate with this configuration, if they cannot
     */
    public Optional<String> configurationError() {
        if (deConfiguration == null || deConfiguration.getKafkaBootStrapServers() == null) {
            return Optional.of("kafkaBootStrapServers not configured");
        }
        if (usesSasl() && saslJaasConfig() == null) {
            return Optional.of("SASL_JAAS_CONFIG not set for " + securityProtocol());
        }
        return Optional.empty();
    }

    private Optional<DistributionStatusTopicConfig> statusTopic() {
        return Optional.ofNullable(deConfiguration).map(DistributionEngineConfiguration::getDistributionStatusTopic);
    }

    private String clientId(String role) {
        return statusTopic().map(DistributionStatusTopicConfig::getConsumerId).orElse("sdc") + "-" + role + "-" + UUID.randomUUID();
    }

    private Optional<Integer> maxSendTimeoutSeconds() {
        if (deConfiguration == null) {
            return Optional.empty();
        }
        DistributionNotificationTopicConfig notification = deConfiguration.getDistributionNotificationTopic();
        DistributionDeleteTopicConfig delete = deConfiguration.getDistributionDeleteTopic();
        return Stream.of(notification == null ? null : notification.getMaxWaitingAfterSendingSeconds(),
                delete == null ? null : delete.getMaxWaitingAfterSendingSeconds())
            .filter(Objects::nonNull)
            .max(Integer::compare);
    }

    private void setCommonProperties(Properties props) {
        String securityProtocolConfig = securityProtocol();
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocolConfig);
        if (deConfiguration != null && deConfiguration.getKafkaBootStrapServers() != null) {
            props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, deConfiguration.getKafkaBootStrapServers());
        }

        if("SSL".equals(securityProtocolConfig)) {
            final JettySSLUtils.JettySslConfig sslConfig = JettySSLUtils.getSSLConfig();
            props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, sslConfig.getKeystorePath());
            props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, sslConfig.getKeystorePass());
            props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, sslConfig.getKeyStoreManager());
            props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
            props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, sslConfig.getTruststorePath());
            props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, sslConfig.getTruststorePass());
        } else if (usesSasl()) {
            String jaasConfig = saslJaasConfig();
            if (jaasConfig != null) {
                props.put(SaslConfigs.SASL_JAAS_CONFIG, jaasConfig);
            }
            props.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512");
        }
    }

    private String securityProtocol() {
        return environment.getOrDefault("SECURITY_PROTOCOL", "SASL_PLAINTEXT");
    }

    private boolean usesSasl() {
        return securityProtocol().startsWith("SASL_");
    }

    private String saslJaasConfig() {
        return environment.get("SASL_JAAS_CONFIG");
    }

}
