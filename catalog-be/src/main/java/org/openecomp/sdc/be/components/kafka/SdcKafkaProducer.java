package org.openecomp.sdc.be.components.kafka;

import com.google.common.annotations.VisibleForTesting;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.config.SaslConfigs;
import org.openecomp.sdc.be.catalog.api.IStatus;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SdcKafkaProducer {
    private static final Logger log = LoggerFactory.getLogger(SdcKafkaProducer.class.getName());

    private final KafkaProducer<String, String> kafkaProducer;

    public SdcKafkaProducer() {
        log.info("Create SdcKafkaProducer via constructor");
        DistributionEngineConfiguration deConfiguration =
            ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration();
        Properties properties = new Properties();
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,  "org.apache.kafka.common.serialization.StringSerializer");
        //properties.put(ProducerConfig.CLIENT_ID_CONFIG, deConfiguration.getDistributionStatusTopic().getConsumerId());
        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, deConfiguration.getKafkaBootStrapServers());
        properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        properties.put(SaslConfigs.SASL_JAAS_CONFIG, getKafkaSaslJaasConfig());
        properties.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512");
        kafkaProducer = new KafkaProducer<>(properties);
    }

    @VisibleForTesting
    SdcKafkaProducer(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    private static String getKafkaSaslJaasConfig() {
        String saslJaasConfFromEnv = System.getenv("SASL_JAAS_CONFIG");
        if(saslJaasConfFromEnv != null) {
            return saslJaasConfFromEnv;
        } else {
            throw new KafkaException("sasl.jaas.config not set for Kafka Consumer");
        }
    }

    public IStatus send(String message, String topicName) {
        try {
            log.info("before send message . response {}", message);
            ProducerRecord<String, String> kafkaMessagePayload = new ProducerRecord<>(topicName, "PartitionKey", message);
            kafkaProducer.send(kafkaMessagePayload);
            return IStatus.getSuccessStatus();
        }
        catch (KafkaException e) {
            log.error(String.valueOf(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR), "Failed to send message . Exception {}", e.getMessage());
            return IStatus.getFailStatus();
        }
    }

    public void flush() {
        log.info("SdcKafkaProducer - flush");
        try {
            kafkaProducer.flush();
        }
        catch (KafkaException e) {
            log.error("Failed to send data: exc {}", e.getMessage());
        }
    }
}
