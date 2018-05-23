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
 */

package org.openecomp.sdc.be.dao.cassandra.schema;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SocketOptions;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;

public class SdcSchemaUtils {

    private static Logger log = LoggerFactory.getLogger(SdcSchemaUtils.class.getName());

    public SdcSchemaUtils() {
    }

    /**
     * the method creates the cluster object using the supplied cassandra nodes
     * in the configuration
     *
     * @return cluster object our null in case of an invalid configuration
     */
    public Cluster createCluster() {
        final Configuration.CassandrConfig config = getCassandraConfig();
        List<String> nodes = config.getCassandraHosts();
        if (nodes == null) {
            log.info("no nodes were supplied in configuration.");
            return null;
        }
        log.info("connecting to node:{}.", nodes);
        Cluster.Builder clusterBuilder = Cluster.builder();
        nodes.forEach(clusterBuilder::addContactPoint);

        clusterBuilder.withMaxSchemaAgreementWaitSeconds(60);

        if (config.isAuthenticate()) {
            String username = config.getUsername();
            String password = config.getPassword();
            if (username == null || password == null) {
                log.info("authentication is enabled but username or password were not supplied.");
                return null;
            }
            clusterBuilder.withCredentials(username, password);
        }
        if (config.isSsl()) {
            String truststorePath = config.getTruststorePath();
            String truststorePassword = config.getTruststorePassword();
            if (truststorePath == null || truststorePassword == null) {
                log.info("ssl is enabled but truststorePath or truststorePassword were not supplied.");
                return null;
            }
            System.setProperty("javax.net.ssl.trustStore", truststorePath);
            System.setProperty("javax.net.ssl.trustStorePassword", truststorePassword);
            clusterBuilder.withSSL();
        }
        SocketOptions socketOptions =new SocketOptions();
        Integer socketConnectTimeout = config.getSocketConnectTimeout();
        if( socketConnectTimeout!=null ){
            log.info("SocketConnectTimeout was provided, setting Cassandra client to use SocketConnectTimeout: {} .",socketConnectTimeout);
            socketOptions.setConnectTimeoutMillis(socketConnectTimeout);
        }
        Integer socketReadTimeout = config.getSocketReadTimeout();
        if( socketReadTimeout != null ){
            log.info("SocketReadTimeout was provided, setting Cassandra client to use SocketReadTimeout: {} .",socketReadTimeout);
            socketOptions.setReadTimeoutMillis(socketReadTimeout);
        }
        clusterBuilder.withSocketOptions(socketOptions);
        return clusterBuilder.build();
    }

    public boolean executeStatement(String statement) {
        return executeStatement(this::createCluster, statement);
    }

    public boolean executeStatements(String ... statements) {
        return executeStatements(this::createCluster, statements);
    }

    boolean executeStatement(Supplier<Cluster> clusterSupplier, String statement) {
        return executeStatements(clusterSupplier, statement);
    }

    boolean executeStatements(Supplier<Cluster> clusterSupplier, String ... statements) {
        try(Cluster cluster = clusterSupplier.get();
                Session session = cluster.connect()) {
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

}
