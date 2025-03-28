/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2025 Deutsche Telekom Intellectual Property. All rights reserved.
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
package org.openecomp.server;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.output.WaitingConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import lombok.extern.slf4j.Slf4j;

// this test is working when launched via the IDE
// but not via mvn due to the system properties not
// being propagated to the java process
@Slf4j
@Disabled
@SpringBootTest
public class SdcOnboardingBackendTest {

  static final CassandraContainer<?> cassandraContainer = new CassandraContainer<>(DockerImageName.parse("cassandra:3.11.2"))
    .withExposedPorts(9042)
    .withInitScript("init_keyspaces_and_schema.cql")
    .waitingFor(Wait.forLogMessage(".*Starting listening for CQL clients.*\\n", 1));

  static {
    cassandraContainer.start();
  }

  // @DynamicPropertySource makes sure that this method is only invoked
  // when the cassandraContainer static variable is actually initialized
  // and the container is ready (based on the waitingFor() condition)
  @DynamicPropertySource
  static void datasourceProperties(DynamicPropertyRegistry registry) {
    System.setProperty("cassandra.createKeyspaceIfNotExists", "true");
    System.setProperty("cassandra.replicationFactor", "1");
    int port = cassandraContainer.getFirstMappedPort();
    System.setProperty("cassandra.cassandraPort", String.valueOf(port));
  }

  @Test
  void contextLoads() {
    assertTrue(true);
  }

  @AfterAll
  static void cleanup() {
    cassandraContainer.stop();
  }

  private static void waitForInitScript() {
    WaitingConsumer consumer = new WaitingConsumer();
    cassandraContainer.followOutput(consumer);

    // String lastLogLineInInitSchemas = "Initializing zusammen_dox.version_stage";
    String lastLogLineInInitSchemas = "Executed database script from init_keyspaces_and_schema.cql";
    try {
      consumer.waitUntil(
          frame -> {
            return frame.getUtf8String().contains(lastLogLineInInitSchemas);
          },
          60,
          TimeUnit.SECONDS);
    } catch (Exception e) {
      log.error(cassandraContainer.getLogs());
    }
  }
}
