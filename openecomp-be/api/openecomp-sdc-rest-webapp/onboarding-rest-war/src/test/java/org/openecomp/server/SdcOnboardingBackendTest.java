package org.openecomp.server;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.CassandraContainer;
// import org.testcontainers.cassandra.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
public class SdcOnboardingBackendTest {

  @Container
  // CassandraContainer<?> cassandraContainer = new CassandraContainer<>("cassandra:3.11.2");
  static final CassandraContainer<?> cassandraContainer = new CassandraContainer<>(DockerImageName.parse("cassandra:3.11.2"))
          .withExposedPorts(9042);

  static {
      cassandraContainer.start(); // Ensure the container is started before running any test
  }

  @Test
  void contextLoads() {
  }

  @AfterAll
  static void cleanup() {
    cassandraContainer.stop();
  }

}
