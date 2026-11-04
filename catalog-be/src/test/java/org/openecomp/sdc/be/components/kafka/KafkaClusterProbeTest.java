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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.internals.KafkaFutureImpl;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.DistributionStatusTopicConfig;

class KafkaClusterProbeTest {

    private static DistributionEngineConfiguration config() {
        DistributionEngineConfiguration de = new DistributionEngineConfiguration();
        de.setKafkaBootStrapServers("kafka:9092");
        DistributionStatusTopicConfig status = new DistributionStatusTopicConfig();
        status.setConsumerId("sdc-id");
        status.setConsumerGroup("sdc");
        de.setDistributionStatusTopic(status);
        return de;
    }

    private static KafkaCommonConfig plaintextConfig() {
        return new KafkaCommonConfig(config(), Map.of("SECURITY_PROTOCOL", "PLAINTEXT"));
    }

    private static KafkaCommonConfig saslWithoutJaasConfig() {
        return new KafkaCommonConfig(config(), Map.of());
    }

    @Test
    void reachableWhenClusterReturnsNodes() {
        Admin admin = mock(Admin.class, RETURNS_DEEP_STUBS);
        when(admin.describeCluster(any()).nodes()).thenReturn(KafkaFuture.completedFuture(List.of(new Node(1, "h", 9092))));
        assertTrue(new KafkaClusterProbe(plaintextConfig(), () -> admin).isClusterReachable(Duration.ofSeconds(1)));
    }

    @Test
    void unreachableWhenClusterReturnsNoNodes() {
        Admin admin = mock(Admin.class, RETURNS_DEEP_STUBS);
        when(admin.describeCluster(any()).nodes()).thenReturn(KafkaFuture.completedFuture(List.of()));
        assertFalse(new KafkaClusterProbe(plaintextConfig(), () -> admin).isClusterReachable(Duration.ofSeconds(1)));
    }

    @Test
    void unreachableOnTimeoutOrMissingJaas() {
        Admin admin = mock(Admin.class, RETURNS_DEEP_STUBS);
        KafkaFutureImpl<Collection<Node>> never = new KafkaFutureImpl<>();
        when(admin.describeCluster(any()).nodes()).thenReturn(never);
        assertFalse(new KafkaClusterProbe(plaintextConfig(), () -> admin).isClusterReachable(Duration.ofMillis(50)));
        Admin unused = mock(Admin.class);
        assertFalse(new KafkaClusterProbe(saslWithoutJaasConfig(), () -> unused).isClusterReachable(Duration.ofMillis(50)));
        verifyNoInteractions(unused);
    }

    @Test
    void failedAdminCreationIsUnreachableAndRetriedOnTheNextProbe() {
        AtomicInteger attempts = new AtomicInteger();
        KafkaClusterProbe probe = new KafkaClusterProbe(plaintextConfig(), () -> {
            attempts.incrementAndGet();
            throw new KafkaException("No resolvable bootstrap urls");
        });
        assertFalse(probe.isClusterReachable(Duration.ofMillis(50)));
        assertFalse(probe.isClusterReachable(Duration.ofMillis(50)));
        assertEquals(2, attempts.get());
    }

    @Test
    void adminIsCreatedOnceAndClosedOnShutdown() {
        Admin admin = mock(Admin.class, RETURNS_DEEP_STUBS);
        when(admin.describeCluster(any()).nodes()).thenReturn(KafkaFuture.completedFuture(List.of(new Node(1, "h", 9092))));
        AtomicInteger created = new AtomicInteger();
        KafkaClusterProbe probe = new KafkaClusterProbe(plaintextConfig(), () -> {
            created.incrementAndGet();
            return admin;
        });
        probe.isClusterReachable(Duration.ofSeconds(1));
        probe.isClusterReachable(Duration.ofSeconds(1));
        probe.close();
        assertEquals(1, created.get());
        verify(admin, times(1)).close(any(Duration.class));
    }
}
