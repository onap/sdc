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
package org.openecomp.sdc.be.utils;

import com.datastax.driver.core.Cluster;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;

import java.io.IOException;

public class CassandraTestHelper {
    public static final String SERVER = "localhost";
    public static final int BINARY_PORT = 9142;

    public CassandraTestHelper() {
    }

    public static void startServer() {
        try {
            EmbeddedCassandraServerHelper.startEmbeddedCassandra(80000);
        } catch(TTransportException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Cluster createCluster() {
        return Cluster.builder().addContactPoint(SERVER).withPort(BINARY_PORT).build();
    }

    public static Cluster createClusterWithNoSession() {
        return Cluster.builder().addContactPoint(SERVER).build();
    }
}
