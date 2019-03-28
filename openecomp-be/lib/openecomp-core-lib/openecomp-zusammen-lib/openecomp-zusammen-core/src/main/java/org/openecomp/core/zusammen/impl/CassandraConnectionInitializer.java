/*
 * Copyright © 2016-2017 European Support Limited
 *
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
 */

package org.openecomp.core.zusammen.impl;

import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.core.nosqldb.util.CassandraUtils;

public class CassandraConnectionInitializer {

    private static final String CASSANDRA_PREFIX = "cassandra.";
    private static final String DATA_CENTER_PROPERTY_NAME = CASSANDRA_PREFIX + "datacenter";
    private static final String CONSISTENCY_LEVEL_PROPERTY_NAME =
            CASSANDRA_PREFIX + "consistency.level";
    private static final String CASSANDRA_RECONNECT_TIMEOUT = CASSANDRA_PREFIX + "reconnectTimeout";
    private static final String NODES_PROPERTY_NAME = CASSANDRA_PREFIX + "nodes";
    private static final String CASSANDRA_PORT_PROPERTY_NAME = CASSANDRA_PREFIX + "cassandraPort";
    private static final String AUTHENTICATE_PROPERTY_NAME = CASSANDRA_PREFIX + "authenticate";
    private static final String SSL_PROPERTY_NAME = CASSANDRA_PREFIX + "ssl";
    private static final String TRUSTSTORE_PROPERTY_NAME = CASSANDRA_PREFIX + "truststore";
    private static final String TRUSTSTORE_PASSWORD_PROPERTY_NAME =
            CASSANDRA_PREFIX + "truststore.password";
    private static final String USER_PROPERTY_NAME = CASSANDRA_PREFIX + "user";
    private static final String PASSWORD_PROPERTY_NAME = CASSANDRA_PREFIX + "password";
    private static final String KEYSPACE_PROPERTY_NAME = CASSANDRA_PREFIX + "keyspace";
    private static final String ZUSAMMEN = "zusammen";

    private CassandraConnectionInitializer() {
        // static utility class, prevent instantiation
    }

    public static void setCassandraConnectionPropertiesToSystem() {
        DeferredInitializer.init();
    }

    private static class DeferredInitializer {

        static {
            setSystemProperty(NODES_PROPERTY_NAME, () ->
                                                           StringUtils.join(CassandraUtils.getAddresses(), ','));
            setSystemProperty(CASSANDRA_PORT_PROPERTY_NAME, () -> Integer.toString(CassandraUtils.getCassandraPort()));
            setBooleanSystemProperty(AUTHENTICATE_PROPERTY_NAME, CassandraUtils::isAuthenticate);
            setBooleanSystemProperty(SSL_PROPERTY_NAME, CassandraUtils::isSsl);
            setNullableSystemProperty(TRUSTSTORE_PROPERTY_NAME, CassandraUtils::getTruststore);
            setNullableSystemProperty(TRUSTSTORE_PASSWORD_PROPERTY_NAME, CassandraUtils::getTruststorePassword);
            setSystemProperty(USER_PROPERTY_NAME, CassandraUtils::getUser);
            setSystemProperty(PASSWORD_PROPERTY_NAME, CassandraUtils::getPassword);
            setSystemProperty(KEYSPACE_PROPERTY_NAME, () -> ZUSAMMEN);
            setNullableSystemProperty(DATA_CENTER_PROPERTY_NAME, CassandraUtils::getLocalDataCenter);
            setNullableSystemProperty(CONSISTENCY_LEVEL_PROPERTY_NAME,
                    CassandraUtils::getConsistencyLevel);
            setSystemProperty(CASSANDRA_RECONNECT_TIMEOUT, () -> Long.toString(CassandraUtils.getReconnectTimeout()));
        }

        private DeferredInitializer() { }

        @SuppressWarnings("EmptyMethod")
        static void init() {
            // no-op, just to ensure static initialization
        }

        private static void setSystemProperty(String name, Supplier<String> valueSupplier) {

            if (System.getProperty(name) == null) {
                System.setProperty(name, valueSupplier.get());
            }
        }

        private static void setBooleanSystemProperty(String name, Supplier<Boolean> valueSupplier) {
            setSystemProperty(name, () -> Boolean.toString(valueSupplier.get()));
        }

        private static void setNullableSystemProperty(String name, Supplier<String> valueSupplier) {

            if ((System.getProperty(name) == null) && (valueSupplier.get() != null)) {
                System.setProperty(name, valueSupplier.get());
            }
        }
    }
}
