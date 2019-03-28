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
 * Modifications copyright (c) 2018 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.dao.cassandra.schema;
import com.datastax.driver.core.Cluster;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.utils.CassandraTestHelper;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;

public class SdcSchemaUtilsTest {
	private static final String SINGLE_STATEMENT = "SELECT COUNT(*) FROM system.peers";
	private static final String[] MULTIPLE_STATEMENTS = new String[] {SINGLE_STATEMENT, SINGLE_STATEMENT};
	private static final List<String> CASSANDRA_HOSTS = Collections.singletonList(CassandraTestHelper.SERVER);
	private static final Integer CASSANDRA_PORT = 9042;
	private static final String CASSANDRA_USERNAME = "username";
	private static final String CASSANDRA_PASSWORD = "password";
	private static final String TRUSTSTORE_PATH = "pathToTruststore";
	private static final String TRUSTSTORE_PASSWORD = "passwordToTruststore";

	@BeforeClass
	public static void startServer() {
		CassandraTestHelper.startServer();
	}

	@Test
	public void testExecuteSingleStatement() throws Exception {
		SdcSchemaUtils sdcSchemaUtils = new SdcSchemaUtils();
		final boolean result = sdcSchemaUtils.executeStatement(CassandraTestHelper::createCluster, SINGLE_STATEMENT);
		Assert.assertTrue(result);
	}


	@Test
	public void testExecuteStatementsSuccessfullScenario() throws Exception {
		SdcSchemaUtils sdcSchemaUtils = new SdcSchemaUtils();
		final boolean result = sdcSchemaUtils.executeStatements(CassandraTestHelper::createCluster, MULTIPLE_STATEMENTS);
		Assert.assertTrue(result);
	}

	@Test
	public void testExecuteStatementsClusterFail() throws Exception {
		SdcSchemaUtils sdcSchemaUtils = new SdcSchemaUtils();
		final boolean result = sdcSchemaUtils.executeStatements(() -> null, MULTIPLE_STATEMENTS);
		Assert.assertFalse(result);
	}

	@Test
	public void testExecuteStatementsSessionFail() throws Exception {
		SdcSchemaUtils sdcSchemaUtils = new SdcSchemaUtils();
		final boolean result = sdcSchemaUtils.executeStatements(CassandraTestHelper::createClusterWithNoSession, MULTIPLE_STATEMENTS);
		Assert.assertFalse(result);
	}

	@Test
	public void testCreateClusterNoAuthNoSsl() {
		Configuration.CassandrConfig cfg = new Configuration.CassandrConfig();
		cfg.setCassandraHosts(CASSANDRA_HOSTS);
		cfg.setCassandraPort(CASSANDRA_PORT);

		SdcSchemaUtils sdcSchemaUtils = Mockito.mock(SdcSchemaUtils.class);
		when(sdcSchemaUtils.getCassandraConfig()).thenReturn(cfg);
		when(sdcSchemaUtils.createCluster()).thenCallRealMethod();

		try(Cluster cluster = sdcSchemaUtils.createCluster()) {
			Assert.assertNotNull(cluster);
		}
	}

	@Test
	public void testCreateClusterFailOnLackOfCassandraNodes() {
		Configuration.CassandrConfig cfg = new Configuration.CassandrConfig();
		cfg.setCassandraHosts(null);

		SdcSchemaUtils sdcSchemaUtils = Mockito.mock(SdcSchemaUtils.class);
		when(sdcSchemaUtils.getCassandraConfig()).thenReturn(cfg);
		when(sdcSchemaUtils.createCluster()).thenCallRealMethod();

		try(Cluster cluster = sdcSchemaUtils.createCluster()) {
			Assert.assertNull(cluster);
		}
	}

	@Test
	public void testCreateClusterWithDefaultOnLackOfCassandraPort() {
		Configuration.CassandrConfig cfg = new Configuration.CassandrConfig();
		cfg.setCassandraHosts(CASSANDRA_HOSTS);
		cfg.setCassandraPort(null);

		SdcSchemaUtils sdcSchemaUtils = Mockito.mock(SdcSchemaUtils.class);
		when(sdcSchemaUtils.getCassandraConfig()).thenReturn(cfg);
		when(sdcSchemaUtils.createCluster()).thenCallRealMethod();

		try(Cluster cluster = sdcSchemaUtils.createCluster()) {
			Assert.assertNotNull(cluster);
		}
	}

	@Test
	public void testCreateClusterFailOnAuthEnabledWithNoCredentials() {
		Configuration.CassandrConfig cfg = new Configuration.CassandrConfig();
		cfg.setAuthenticate(true);
		cfg.setCassandraHosts(CASSANDRA_HOSTS);
		cfg.setCassandraPort(CASSANDRA_PORT);
		cfg.setUsername(null);
		cfg.setPassword(null);

		SdcSchemaUtils sdcSchemaUtils = Mockito.mock(SdcSchemaUtils.class);
		when(sdcSchemaUtils.getCassandraConfig()).thenReturn(cfg);
		when(sdcSchemaUtils.createCluster()).thenCallRealMethod();

		try(Cluster cluster = sdcSchemaUtils.createCluster()) {
			Assert.assertNull(cluster);
		}
	}

	@Test
	public void testCreateClusterFailOnSSLWithNoCredentials() {
		Configuration.CassandrConfig cfg = new Configuration.CassandrConfig();
		cfg.setCassandraHosts(CASSANDRA_HOSTS);
		cfg.setCassandraPort(CASSANDRA_PORT);
		cfg.setSsl(true);
		cfg.setTruststorePath(null);
		cfg.setTruststorePassword(null);

		SdcSchemaUtils sdcSchemaUtils = Mockito.mock(SdcSchemaUtils.class);
		when(sdcSchemaUtils.getCassandraConfig()).thenReturn(cfg);
		when(sdcSchemaUtils.createCluster()).thenCallRealMethod();

		try(Cluster cluster = sdcSchemaUtils.createCluster()) {
			Assert.assertNull(cluster);
		}
	}

	@Test
	public void testCreateClusterWithAuthSsl() {
		Configuration.CassandrConfig cfg = new Configuration.CassandrConfig();
		cfg.setAuthenticate(true);
		cfg.setCassandraHosts(CASSANDRA_HOSTS);
		cfg.setCassandraPort(CASSANDRA_PORT);
		cfg.setUsername(CASSANDRA_USERNAME);
		cfg.setPassword(CASSANDRA_PASSWORD);
		cfg.setSsl(true);
		cfg.setTruststorePath(TRUSTSTORE_PATH);
		cfg.setTruststorePassword(TRUSTSTORE_PASSWORD);

		SdcSchemaUtils sdcSchemaUtils = Mockito.mock(SdcSchemaUtils.class);
		when(sdcSchemaUtils.getCassandraConfig()).thenReturn(cfg);
		when(sdcSchemaUtils.createCluster()).thenCallRealMethod();

		try(Cluster cluster = sdcSchemaUtils.createCluster()) {
			Assert.assertNotNull(cluster);
			Assert.assertEquals(System.getProperty("javax.net.ssl.trustStore"), TRUSTSTORE_PATH);
			Assert.assertEquals(System.getProperty("javax.net.ssl.trustStorePassword"), TRUSTSTORE_PASSWORD);
		}
	}
}