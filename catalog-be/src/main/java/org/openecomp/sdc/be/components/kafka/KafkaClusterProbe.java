/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.annotation.PreDestroy;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Asks the Kafka cluster for its brokers. The listener container's consumer keeps polling without an error while
 * the brokers are unreachable, so the consumer cannot tell whether Kafka is up.
 */
@Component
public class KafkaClusterProbe {

    private static final Logger log = Logger.getLogger(KafkaClusterProbe.class.getName());
    private final KafkaCommonConfig kafkaCommonConfig;
    private final Supplier<Admin> adminFactory;
    private Admin admin;

    @Autowired
    public KafkaClusterProbe(KafkaCommonConfig kafkaCommonConfig) {
        this(kafkaCommonConfig, () -> Admin.create(kafkaCommonConfig.getAdminProperties()));
    }

    KafkaClusterProbe(KafkaCommonConfig kafkaCommonConfig, Supplier<Admin> adminFactory) {
        this.kafkaCommonConfig = kafkaCommonConfig;
        this.adminFactory = adminFactory;
    }

    public boolean isClusterReachable(Duration timeout) {
        if (kafkaCommonConfig.configurationError().isPresent()) {
            return false;
        }
        try {
            DescribeClusterOptions options = new DescribeClusterOptions().timeoutMs((int) timeout.toMillis());
            return !admin().describeCluster(options).nodes().get(timeout.toMillis(), TimeUnit.MILLISECONDS).isEmpty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.debug("Kafka cluster probe failed", e);
            return false;
        }
    }

    private synchronized Admin admin() {
        if (admin == null) {
            admin = adminFactory.get();
        }
        return admin;
    }

    @PreDestroy
    public synchronized void close() {
        if (admin != null) {
            admin.close(Duration.ofSeconds(5));
            admin = null;
        }
    }
}
