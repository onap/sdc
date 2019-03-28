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

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.RemoteEndpointAwareJdkSSLOptions;
import com.datastax.driver.core.SSLOptions;
import com.datastax.driver.core.Session;


import com.datastax.driver.core.policies.*;
import org.openecomp.core.nosqldb.util.CassandraUtils;
import org.openecomp.sdc.common.errors.SdcConfigurationException;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Optional;

public class CassandraSessionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraSessionFactory.class);

    private CassandraSessionFactory() {
        // static methods, cannot be instantiated
    }

    public static Session getSession() {
        return ReferenceHolder.CASSANDRA;
    }

    /**
     * New cassandra session session.
     *
     * @return the session
     */
    public static Session newCassandraSession() {
        String[] addresses = CassandraUtils.getAddresses();
        int cassandraPort = CassandraUtils.getCassandraPort();
        Long reconnectTimeout = CassandraUtils.getReconnectTimeout();

        Cluster.Builder builder = Cluster.builder();

        if(null != reconnectTimeout) {
            builder.withReconnectionPolicy(new ConstantReconnectionPolicy(reconnectTimeout))
                    .withRetryPolicy(DefaultRetryPolicy.INSTANCE);
        }

        builder.withPort(cassandraPort);

        for (String address : addresses) {
            builder.addContactPoint(address);
        }

        //Check if ssl
        Boolean isSsl = CassandraUtils.isSsl();
        if (isSsl) {
            builder.withSSL(getSslOptions());
        }

        //Check if user/pass
        Boolean isAuthenticate = CassandraUtils.isAuthenticate();
        if (isAuthenticate) {
            builder.withCredentials(CassandraUtils.getUser(), CassandraUtils.getPassword());
        }

        setConsistencyLevel(builder, addresses);

        setLocalDataCenter(builder);

        Cluster cluster = builder.build();
        String keyStore = SessionContextProviderFactory.getInstance().createInterface().get()
            .getTenant();
        LOGGER.info("Cassandra client created hosts: {} port: {} SSL enabled: {} reconnectTimeout",
                addresses, cassandraPort, isSsl, reconnectTimeout);
        return cluster.connect(keyStore);
    }

    private static void setLocalDataCenter(Cluster.Builder builder) {
        String localDataCenter = CassandraUtils.getLocalDataCenter();
        if (Objects.nonNull(localDataCenter)) {
            LOGGER.info("localDatacenter was provided, setting Cassndra client to use datacenter: {} as local.",
                    localDataCenter);

            LoadBalancingPolicy tokenAwarePolicy = new TokenAwarePolicy(
                    DCAwareRoundRobinPolicy.builder().withLocalDc(localDataCenter).build());
            builder.withLoadBalancingPolicy(tokenAwarePolicy);
        } else {
            LOGGER.info(
                    "localDatacenter was provided,  the driver will use the datacenter of the first contact " +
                            "point that was reached at initialization");
        }
    }

    private static void setConsistencyLevel(Cluster.Builder builder, String[] addresses) {
        if (addresses != null && addresses.length > 1) {
            String consistencyLevel = CassandraUtils.getConsistencyLevel();
            if (Objects.nonNull(consistencyLevel)) {
                LOGGER.info(
                        "consistencyLevel was provided, setting Cassandra client to use consistencyLevel: {}" +
                                " as "
                        , consistencyLevel);
                builder.withQueryOptions(new QueryOptions().setConsistencyLevel(ConsistencyLevel.valueOf
                        (consistencyLevel)));
            }
        }
    }

    private static SSLOptions getSslOptions() {

        Optional<String> trustStorePath = Optional.ofNullable(CassandraUtils.getTruststore());
        if (!trustStorePath.isPresent()) {
            throw new SdcConfigurationException("Missing configuration for Cassandra trustStorePath");
        }

        Optional<String> trustStorePassword = Optional.ofNullable(CassandraUtils.getTruststorePassword());
        if (!trustStorePassword.isPresent()) {
            throw new SdcConfigurationException("Missing configuration for Cassandra trustStorePassword");
        }

        SSLContext context = getSslContext(trustStorePath.get(), trustStorePassword.get());
        String[] css = new String[]{"TLS_RSA_WITH_AES_128_CBC_SHA"};
        return RemoteEndpointAwareJdkSSLOptions.builder().withSSLContext(context).withCipherSuites(css).build();
    }

    private static SSLContext getSslContext(String truststorePath, String trustStorePassword) {

        try (FileInputStream tsf = new FileInputStream(truststorePath)) {

            SSLContext ctx = SSLContext.getInstance("SSL");

            KeyStore ts = KeyStore.getInstance("JKS");
            ts.load(tsf, trustStorePassword.toCharArray());
            TrustManagerFactory tmf =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ts);

            ctx.init(null, tmf.getTrustManagers(), new SecureRandom());
            return ctx;

        } catch (Exception exception) {
            throw new SdcConfigurationException("Failed to get SSL Contexts for Cassandra connection", exception);
        }
    }

    private static class ReferenceHolder {
        private static final Session CASSANDRA = newCassandraSession();

        private ReferenceHolder() {
            // prevent instantiation
        }
    }
}
