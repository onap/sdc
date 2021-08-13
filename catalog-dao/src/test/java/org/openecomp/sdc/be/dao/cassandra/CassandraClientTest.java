/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.dao.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.utils.CassandraTestHelper;
import org.openecomp.sdc.be.utils.DAOConfDependentTest;

class CassandraClientTest extends DAOConfDependentTest {

    private CassandraClient createTestSubject() {
        return new CassandraClient();
    }

    @BeforeAll
    public static void startServer() {
        CassandraTestHelper.startServer();
    }

    @Test
    void testSetLocalDc() throws Exception {
        CassandraClient testSubject;

        Builder mock = Mockito.mock(Cluster.Builder.class);
        Mockito.when(mock.withLoadBalancingPolicy(Mockito.any())).thenReturn(new Builder());
        // default test
        testSubject = createTestSubject();
        Assertions.assertNotNull(testSubject);

        ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().setLocalDataCenter("mock");

        testSubject = createTestSubject();
        Assertions.assertNotNull(testSubject);
    }

    @Test
    void testEnableSsl() throws Exception {
        CassandraClient testSubject;
        Cluster.Builder clusterBuilder = null;

        Builder mock = Mockito.mock(Cluster.Builder.class);
        Mockito.when(mock.withSSL()).thenReturn(new Builder());

        ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().setSsl(false);
        testSubject = createTestSubject();
        Assertions.assertNotNull(testSubject);
        Assertions.assertNull(System.getProperty("javax.net.ssl.trustStore"));
        Assertions.assertNull(System.getProperty("javax.net.ssl.trustStorePassword"));

        ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().setSsl(true);
        testSubject = createTestSubject();
        Assertions.assertNotNull(testSubject);
        Assertions.assertNotNull(System.getProperty("javax.net.ssl.trustStore"));
        Assertions.assertNotNull(System.getProperty("javax.net.ssl.trustStorePassword"));

        ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().setTruststorePath(null);
        ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().setTruststorePassword(null);
        testSubject = createTestSubject();
        Assertions.assertNotNull(testSubject);
        Assertions.assertNotNull(System.getProperty("javax.net.ssl.trustStore"));
        Assertions.assertNotNull(System.getProperty("javax.net.ssl.trustStorePassword"));
    }

    @Test
    void testEnableAuthentication() throws Exception {
        CassandraClient testSubject;
        Builder mock = Mockito.mock(Cluster.Builder.class);
        Mockito.when(mock.withCredentials(Mockito.any(), Mockito.any())).thenReturn(new Builder());

        testSubject = createTestSubject();
        Assertions.assertNotNull(testSubject);

        ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().setAuthenticate(true);
        testSubject = createTestSubject();
        Assertions.assertNotNull(testSubject);

        ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().setUsername(null);
        ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().setPassword(null);
        testSubject = createTestSubject();
        Assertions.assertNotNull(testSubject);
    }

    @Test
    void testConnect() throws Exception {
        CassandraClient testSubject;
        String keyspace = "";
        Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.connect(keyspace);
        Assertions.assertNotNull(result);
    }

    @Test
    void testSave() throws Exception {
        CassandraClient testSubject;
        T entity = null;
        Class<T> clazz = null;
        MappingManager manager = null;
        CassandraOperationStatus result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.save(entity, clazz, manager);
        Assertions.assertNotNull(result);
    }

    @Test
    void testGetById() throws Exception {
        CassandraClient testSubject;
        String id = "";
        Class<T> clazz = null;
        MappingManager manager = null;
        Either<T, CassandraOperationStatus> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getById(id, clazz, manager);
        Assertions.assertNotNull(result);
    }

    @Test
    void testDelete() throws Exception {
        CassandraClient testSubject;
        String id = "";
        Class<T> clazz = null;
        MappingManager manager = null;
        CassandraOperationStatus result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.delete(id, clazz, manager);
        Assertions.assertNotNull(result);
    }

    @Test
    void testIsConnected() throws Exception {
        CassandraClient testSubject;
        boolean result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.isConnected();
        Assertions.assertTrue(result);
    }

    @Test
    void testCloseClient() throws Exception {
        CassandraClient testSubject;

        // default test
        testSubject = createTestSubject();
        testSubject.closeClient();
        Assertions.assertNotNull(testSubject);
    }
}
