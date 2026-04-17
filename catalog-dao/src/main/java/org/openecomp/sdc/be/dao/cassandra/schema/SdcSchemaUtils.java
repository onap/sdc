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

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.metadata.Metadata;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.config.ProgrammaticDriverConfigLoaderBuilder;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import java.time.Duration;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.function.Supplier;

import javax.net.ssl.SSLContext;

import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.common.log.wrappers.Logger;

public class SdcSchemaUtils {

    private static Logger log = Logger.getLogger(SdcSchemaUtils.class.getName());
    private CqlSession session;

    private boolean isConnected;

    public SdcSchemaUtils() {
        super();
        try {
            isConnected = false;
            session = createSession();
            isConnected = true;
        } catch (Exception e) {
            log.info("** CassandraClient isn't connected. error is", e);
        }
        log.info("** session created");
    }

    /**
     * the method creates the session object using the supplied cassandra nodes in the configuration
     *
     * @return session object our null in case of an invalid configuration
     */
    public CqlSession createSession() {
        final Configuration.CassandrConfig config = getCassandraConfig();
        List<String> nodes = config.getCassandraHosts();
        Integer cassandraPort = config.getCassandraPort();
        if (nodes == null || cassandraPort == null) {
            log.info("No nodes or port were supplied in configuration.");
            return null;
        }
        log.info("Connecting to node: {} port: {}.", nodes, cassandraPort);
        CqlSessionBuilder  sessionBuilder = CqlSession.builder();
        nodes.forEach(node -> sessionBuilder.addContactPoint(new InetSocketAddress(node, cassandraPort)));
        log.info("Connection timeout in seconds : {}", config.getMaxWaitSeconds());
        DriverConfigLoader loader = DriverConfigLoader.programmaticBuilder()
        .withDuration(
            DefaultDriverOption.REQUEST_TIMEOUT,
            Duration.ofSeconds(config.getMaxWaitSeconds())
        )
        .build();
        sessionBuilder.withConfigLoader(loader);
        config.getCassandraHosts().forEach(node ->
        sessionBuilder.addContactPoint(new InetSocketAddress(node, config.getCassandraPort()))
    );
        setSocketOptions(sessionBuilder, config);
        if (!enableAuthentication(sessionBuilder, config)) {
            return null;
        }
        if (!enableSsl(sessionBuilder, config)) {
            return null;
        }
        setLocalDc(sessionBuilder, config);
        return sessionBuilder.build();
    }

    /**
     * @return
     */
  public CqlSession connect() {
    try {
        return createSession();
    } catch (Throwable e) {
        log.error("Failed to connect to Cassandra health check, error: ", e);
        return null;
    }
}

    public Metadata getMetadata() {
        if (session != null) {
            return session.getMetadata();
        }
        return null;
    }

    private void setLocalDc(CqlSessionBuilder builder, Configuration.CassandrConfig config) {
    String localDataCenter = config.getLocalDataCenter();
    if (localDataCenter != null) {
        log.info("localDatacenter was provided, setting Cassandra client to use datacenter: {} as local.", localDataCenter);
        builder.withLocalDatacenter(localDataCenter);
    } else {
        log.info("localDatacenter was not provided, the driver will use the datacenter of the first contact point that was reached at initialization.");
    }
}

    private boolean enableSsl(CqlSessionBuilder sessionBuilder, Configuration.CassandrConfig config) {
        boolean ssl = config.isSsl();
        if (ssl) {
            String truststorePath = config.getTruststorePath();
            String truststorePassword = config.getTruststorePassword();
            if (truststorePath == null || truststorePassword == null) {
                log.error("ssl is enabled but truststorePath or truststorePassword were not supplied.");
                return false;
            } else {
                System.setProperty("javax.net.ssl.trustStore", truststorePath);
                System.setProperty("javax.net.ssl.trustStorePassword", truststorePassword);
                try {
                SSLContext sslContext = SSLContext.getDefault();
                sessionBuilder.withSslContext(sslContext); 
            } catch (Exception e) {
                log.error("Failed to enable SSL for Cassandra connection", e);
                return false;
            }
          }
        }
        return true;
    }

    private void setSocketOptions(CqlSessionBuilder sessionBuilder, Configuration.CassandrConfig config) {
        ProgrammaticDriverConfigLoaderBuilder configBuilder = DriverConfigLoader.programmaticBuilder();

        Integer socketConnectTimeout = config.getSocketConnectTimeout();
        if (socketConnectTimeout != null) {
            log.info("SocketConnectTimeout was provided, setting Cassandra client to use SocketConnectTimeout: {} .", socketConnectTimeout);
            configBuilder.withDuration(DefaultDriverOption.CONNECTION_CONNECT_TIMEOUT, Duration.ofMillis(socketConnectTimeout));
        }
        Integer socketReadTimeout = config.getSocketReadTimeout();
        if (socketReadTimeout != null) {
            log.info("SocketReadTimeout was provided, setting Cassandra client to use SocketReadTimeout: {} .", socketReadTimeout);
            configBuilder.withDuration(DefaultDriverOption.REQUEST_TIMEOUT, Duration.ofMillis(socketReadTimeout));
        }
        sessionBuilder.withConfigLoader(configBuilder.build());
    }

    private boolean enableAuthentication(CqlSessionBuilder sessionBuilder, Configuration.CassandrConfig config) {
        boolean authenticate = config.isAuthenticate();
        if (authenticate) {
            String username = config.getUsername();
            String password = config.getPassword();
            if (username == null || password == null) {
                log.error("authentication is enabled but username or password were not supplied.");
                return false;
            } else {
                sessionBuilder.withCredentials(username, password);
            }
        }
        return true;
    }

    public boolean executeStatement(String statement) {
        return executeStatement(this::createSession, statement);
    }

    public boolean executeStatements(String... statements) {
        return executeStatements(this::createSession, statements);
    }

    boolean executeStatement(Supplier<CqlSession> sessionSupplier, String statement) {
        return executeStatements(sessionSupplier, statement);
    }

    boolean executeStatements(Supplier<CqlSession> sessionSupplier, String... statements) {
        try (CqlSession session = sessionSupplier.get()) {
            for (String statement : statements) {
                session.execute(statement);
            }
            return true;
        } catch (RuntimeException e) {
            log.error("could not execute statements", e);
        }
        return false;
    }

    Configuration.CassandrConfig getCassandraConfig() {
        return ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig();
    }

    public void closeSession() {
        if (isConnected) {
            session.close();
        }
        log.info("** CassandraClient session closed");
    }
}
