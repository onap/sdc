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
import com.datastax.driver.core.Session;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.utils.CassandraTestHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

public class SdcSchemaBuilderTest {

    @BeforeClass
    public static void startServer() {
        CassandraTestHelper.startServer();
    }

    @Test
    public void testCreateSchema() {
        SdcSchemaUtils utils = mock(SdcSchemaUtils.class);
        when(utils.createCluster()).thenReturn(CassandraTestHelper.createCluster());
        SdcSchemaBuilder sdcSchemaBuilder = new SdcSchemaBuilder(utils, SdcSchemaBuilderTest::createCassandraConfig);
        final boolean result = sdcSchemaBuilder.createSchema();
        Assert.assertTrue(result);
    }

    @Test
    public void testDeleteSchemaNoKeyspaces() {
        SdcSchemaUtils utils = mock(SdcSchemaUtils.class);
        when(utils.createCluster()).thenReturn(CassandraTestHelper.createCluster());
        SdcSchemaBuilder sdcSchemaBuilder = new SdcSchemaBuilder(utils, SdcSchemaBuilderTest::createCassandraConfig);
        final boolean result = sdcSchemaBuilder.deleteSchema();
        Assert.assertTrue(result);
    }

    @Test
    public void testDeleteSchemaWithKeyspacesExisting() {
        Configuration.CassandrConfig throwAwayConfig = createCassandraConfig();
        Cluster cluster = CassandraTestHelper.createCluster();
        createTestKeyspaces(cluster, throwAwayConfig.getKeySpaces());
        SdcSchemaUtils utils = mock(SdcSchemaUtils.class);
        when(utils.createCluster()).thenReturn(cluster);
        SdcSchemaBuilder sdcSchemaBuilder = new SdcSchemaBuilder(utils, SdcSchemaBuilderTest::createCassandraConfig);
        final boolean result = sdcSchemaBuilder.deleteSchema();
        Assert.assertTrue(result);
    }

    private static Configuration.CassandrConfig createCassandraConfig() {
        Configuration.CassandrConfig cfg = new Configuration.CassandrConfig();
        Set<String> requiredKeyspaces = Arrays.stream(Table.values())
                .map(t -> t.getTableDescription().getKeyspace().toLowerCase())
                .collect(Collectors.toSet());
        List<Configuration.CassandrConfig.KeyspaceConfig> createdKeyspaces = requiredKeyspaces.stream()
                .map(k -> {
                    Configuration.CassandrConfig.KeyspaceConfig keyspace = new Configuration.CassandrConfig.KeyspaceConfig();
                    keyspace.setName(k);
                    keyspace.setReplicationInfo(Collections.singletonList("1"));
                    keyspace.setReplicationStrategy(SdcSchemaBuilder.ReplicationStrategy.SIMPLE_STRATEGY.getStrategyName());
                    return keyspace;})
                .collect(Collectors.toList());
        cfg.setKeySpaces(createdKeyspaces);
        return cfg;
    }

    private static void createTestKeyspaces(Cluster cluster, List<Configuration.CassandrConfig.KeyspaceConfig> keyspaceConfig) {
        try(Session session = cluster.connect()) {
            keyspaceConfig.forEach(keyspace -> {
                String query = String
                        .format("CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class':'%s', 'replication_factor' : %s};",
                                keyspace.getName(), keyspace.getReplicationStrategy(), keyspace.getReplicationInfo().get(0));
                session.execute(query);
            });
        }
    }
}
