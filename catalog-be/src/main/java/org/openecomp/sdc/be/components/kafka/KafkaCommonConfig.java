/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2023 Nordix Foundation. All rights reserved.
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

import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaCommonConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaCommonConfig.class.getName());

    private final DistributionEngineConfiguration deConfiguration;

    public KafkaCommonConfig(DistributionEngineConfiguration config){
        this.deConfiguration = config;
    }

    public Properties getConsumerProperties(){
        Properties props = new Properties();
        setCommonProperties(props);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,  "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, deConfiguration.getDistributionStatusTopic().getConsumerId() + "-consumer-" + UUID.randomUUID());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, deConfiguration.getDistributionStatusTopic().getConsumerGroup());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        return props;
    }

    public Properties getProducerProperties(){
        Properties props = new Properties();
        setCommonProperties(props);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,  "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, deConfiguration.getDistributionStatusTopic().getConsumerId() + "-producer-" + UUID.randomUUID());

        return props;
    }

    private void setCommonProperties(Properties props) {
        String securityProtocolConfig = System.getenv().getOrDefault("SECURITY_PROTOCOL", "SASL_PLAINTEXT");
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocolConfig);
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, deConfiguration.getKafkaBootStrapServers());
        if("SSL".equals(securityProtocolConfig)) {
            props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, deConfiguration.getSslConfig().getKeyStorePath());
            props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, deConfiguration.getSslConfig().getKeyStorePassword());
            props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, deConfiguration.getSslConfig().getKeyPassword());
            props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
            props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, deConfiguration.getSslConfig().getTrustStorePath());
            props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, deConfiguration.getSslConfig().getTrustStorePassword());
        } else {
            props.put(SaslConfigs.SASL_JAAS_CONFIG, getKafkaSaslJaasConfig());
            props.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512");
        }
    }

    /**
     * @return The Sasl Jaas Configuration
     */
    private String getKafkaSaslJaasConfig() throws KafkaException {
        String saslJaasConfFromEnv = System.getenv("SASL_JAAS_CONFIG");
        if(saslJaasConfFromEnv != null) {
            return saslJaasConfFromEnv;
        } else {
            throw new KafkaException("sasl.jaas.config not set for Kafka Consumer");
        }
    }

}
