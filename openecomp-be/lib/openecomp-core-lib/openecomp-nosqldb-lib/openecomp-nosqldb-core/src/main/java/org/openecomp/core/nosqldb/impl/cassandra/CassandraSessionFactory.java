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
package org.openecomp.core.nosqldb.impl.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;


import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Objects;

import org.openecomp.core.nosqldb.util.CassandraUtils;
import org.openecomp.sdc.common.errors.SdcConfigurationException;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

public class CassandraSessionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraSessionFactory.class);

    private CassandraSessionFactory() {
        // static methods, cannot be instantiated
    }

     public static CqlSession getSession() {
        return ReferenceHolder.CASSANDRA;
    }
    /**
     * New cassandra session session.
     *
     * @return the session
     */
       public static CqlSession newCassandraSession() {
        String[] addresses = CassandraUtils.getAddresses();
        int cassandraPort = CassandraUtils.getCassandraPort();

        CqlSessionBuilder builder = CqlSession.builder();

        for (String address : addresses) {
            builder.addContactPoint(new InetSocketAddress(address, cassandraPort));
        }

        // Local DC (required in v4 driver)
        String localDc = CassandraUtils.getLocalDataCenter();
        if (Objects.nonNull(localDc)) {
            LOGGER.info("Setting Cassandra local datacenter: {}", localDc);
            builder.withLocalDatacenter(localDc);
        }

        // SSL
        if (CassandraUtils.isSsl()) {
            builder.withSslContext(getSslContext(
                CassandraUtils.getTruststore(),
                CassandraUtils.getTruststorePassword()
            ));
        }

        // Authentication
        if (CassandraUtils.isAuthenticate()) {
            builder.withAuthCredentials(CassandraUtils.getUser(), CassandraUtils.getPassword());
        }

        // Keyspace (optional, can also be set per query)
        String keyspace = SessionContextProviderFactory.getInstance().createInterface().get().getTenant();
        if (keyspace != null) {
            builder.withKeyspace(keyspace);
        }

        LOGGER.info("Cassandra client created hosts: {} port: {} SSL enabled: {}",
                String.join(",", addresses), cassandraPort, CassandraUtils.isSsl());

        registerCustomCodecs(builder);
        return builder.build();
    }

    private static SSLContext getSslContext(String truststorePath, String trustStorePassword) {
        try (FileInputStream tsf = new FileInputStream(truststorePath)) {
            SSLContext ctx = SSLContext.getInstance("TLS");
            KeyStore ts = KeyStore.getInstance("JKS");
            ts.load(tsf, trustStorePassword.toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ts);
            ctx.init(null, tmf.getTrustManagers(), new SecureRandom());
            return ctx;
        } catch (Exception exception) {
            throw new SdcConfigurationException("Failed to get SSL Contexts for Cassandra connection", exception);
        }
    }

    private static class ReferenceHolder {

        private static final CqlSession CASSANDRA = newCassandraSession();

        private ReferenceHolder() {
            // prevent instantiation
        }
    }

    private static void registerCustomCodecs(CqlSessionBuilder builder) {
    try {
        com.typesafe.config.Config config =
            com.typesafe.config.ConfigFactory.load();

        if (!config.hasPath("datastax-java-driver.basic.custom-codecs")) {
            return;
        }

        var codecConfigs =
            config.getConfigList("datastax-java-driver.basic.custom-codecs");

        for (com.typesafe.config.Config codecConfig : codecConfigs) {
            String className = codecConfig.getString("class");
            LOGGER.info("Registering custom Cassandra codec: {}", className);

            try {
                Class<?> clazz = Class.forName(className);
                Object instance = clazz.getDeclaredConstructor().newInstance();

                if (instance instanceof com.datastax.oss.driver.api.core.type.codec.TypeCodec) {

                    com.datastax.oss.driver.api.core.type.codec.TypeCodec<?> codec =
                        (com.datastax.oss.driver.api.core.type.codec.TypeCodec<?>) instance;

                    builder.addTypeCodecs(codec);

                } else {
                    LOGGER.error("Class {} is not a TypeCodec", className);
                }

            } catch (Exception e) {
                LOGGER.error("Failed to load codec class {}", className, e);
            }
        }

    } catch (Exception e) {
        LOGGER.error("Failed to load codec registry", e);
    }
}
}
