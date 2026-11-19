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
 * Modifications Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
 * ================================================================================
 */
package org.openecomp.sdc.be.utils;

import com.datastax.driver.core.Cluster;
import java.net.InetSocketAddress;
import org.testcontainers.cassandra.CassandraContainer;

public class CassandraTestHelper {

    public static final String SERVER = "localhost";
    private static final CassandraContainer CASSANDRA = new CassandraContainer("cassandra:3.11.19");

    private CassandraTestHelper() {
    }

    public static synchronized void startServer() {
        if (!CASSANDRA.isRunning()) {
            CASSANDRA.start();
        }
    }

    public static Cluster createCluster() {
        InetSocketAddress contactPoint = CASSANDRA.getContactPoint();
        return Cluster.builder().addContactPoint(contactPoint.getHostString()).withPort(contactPoint.getPort()).build();
    }
}
