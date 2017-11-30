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

import java.util.List;

import org.openecomp.sdc.be.config.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public class SdcSchemaUtils {

    private static Logger log = LoggerFactory.getLogger(SdcSchemaUtils.class.getName());

    /**
     * the method creates the cluster object using the supplied cassandra nodes
     * in the configuration
     *
     * @return cluster object our null in case of an invalid configuration
     */
    public static Cluster createCluster() {
        List<String> nodes = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().getCassandraHosts();
        if (nodes == null) {
            log.info("no nodes were supplied in configuration.");
            return null;
        }
        log.info("connecting to node:{}.", nodes);
        Cluster.Builder clusterBuilder = Cluster.builder();
        nodes.forEach(host -> clusterBuilder.addContactPoint(host));

        clusterBuilder.withMaxSchemaAgreementWaitSeconds(60);

        boolean authenticate = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().isAuthenticate();
        if (authenticate) {
            String username = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().getUsername();
            String password = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().getPassword();
            if (username == null || password == null) {
                log.info("authentication is enabled but username or password were not supplied.");
                return null;
            }
            clusterBuilder.withCredentials(username, password);
        }
        boolean ssl = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().isSsl();
        if (ssl) {
            String truststorePath = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().getTruststorePath();
            String truststorePassword = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().getTruststorePassword();
            if (truststorePath == null || truststorePassword == null) {
                log.info("ssl is enabled but truststorePath or truststorePassword were not supplied.");
                return null;
            }
            System.setProperty("javax.net.ssl.trustStore", truststorePath);
            System.setProperty("javax.net.ssl.trustStorePassword", truststorePassword);
            clusterBuilder.withSSL();
        }
        return clusterBuilder.build();
    }

    public static boolean executeStatement(String statement) {
        return executeStatements(statement);
    }

    public static boolean executeStatements(String ... statements) {
        Cluster cluster = null;
        Session session = null;
        try {
            cluster = createCluster();
            if (cluster == null) {
                return false;
            }
            session = cluster.connect();
            for (String statement : statements) {
                session.execute(statement);
            }
            return true;
        } catch (RuntimeException e) {
            log.error(String.format("could not execute statements"), e);
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
            if (cluster != null) {
                cluster.close();
            }

        }
    }



}
