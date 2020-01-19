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
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SocketOptions;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.util.List;
import java.util.function.Supplier;

public class SdcSchemaUtils {

    private static Logger log = Logger.getLogger(SdcSchemaUtils.class.getName());
    private Cluster cluster;
    private boolean isConnected;  
    
    

    public SdcSchemaUtils() {
        super();
        try {
            isConnected = false;
            cluster =  createCluster();
            isConnected = true;
        } catch (Exception e) {
            log.info("** CassandraClient isn't connected. error is {}", e);
        }

        log.info("** cluster created");
    }

    /**
     * the method creates the cluster object using the supplied cassandra nodes
     * in the configuration
     *
     * @return cluster object our null in case of an invalid configuration
     * 
     * 
     */
    public Cluster createCluster() {
        final Configuration.CassandrConfig config = getCassandraConfig();
        List<String> nodes = config.getCassandraHosts();
        Integer cassandraPort = config.getCassandraPort();
        if (nodes == null || cassandraPort == null) {
            log.info("no nodes or port were supplied in configuration.");
            return null;
        }
        log.info("connecting to node:{} port{}.", nodes, cassandraPort);
        Cluster.Builder clusterBuilder = Cluster.builder();
        nodes.forEach(node -> clusterBuilder.addContactPoint(node).withPort(cassandraPort));

       clusterBuilder.withMaxSchemaAgreementWaitSeconds(60); 
             
       setSocketOptions(clusterBuilder, config);
        if(!enableAuthentication(clusterBuilder, config)){
            return null;
        }
        
        if(!enableSsl(clusterBuilder, config)){
            return null;
        }
        setLocalDc(clusterBuilder, config);
        
        return clusterBuilder.build();
    }
    
    /**
     * 
     * @return
     */
    public Session  connect() {
        Session session = null;
        if (cluster != null) {
            try {
                session = cluster.connect();
               
            } catch (Throwable e) {
                log.debug("Failed to connect cluster, error :",  e);
               
            }
        }
        return session;
    }
    
    public Metadata getMetadata(){
        if (cluster != null){
            return cluster.getMetadata();
        }
        return null;
    }
    
    private void setLocalDc(Cluster.Builder clusterBuilder, Configuration.CassandrConfig config) {
        String localDataCenter = config.getLocalDataCenter();
        if (localDataCenter != null) {
            log.info("localDatacenter was provided, setting Cassndra clint to use datacenter: {} as local.",
                    localDataCenter);
            LoadBalancingPolicy tokenAwarePolicy = new TokenAwarePolicy(
                    DCAwareRoundRobinPolicy.builder().withLocalDc(localDataCenter).build());
            clusterBuilder.withLoadBalancingPolicy(tokenAwarePolicy);
        } else {
            log.info(
                    "localDatacenter was provided,  the driver will use the datacenter of the first contact point that was reached at initialization");
        }
    }
    
    private boolean enableSsl(Cluster.Builder clusterBuilder, Configuration.CassandrConfig config) {
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
                clusterBuilder.withSSL();
            }

        }
        return true;
    }
    
    
    private void setSocketOptions(Cluster.Builder clusterBuilder, Configuration.CassandrConfig config) {
        SocketOptions socketOptions =new SocketOptions();
        Integer socketConnectTimeout = config.getSocketConnectTimeout();
        if( socketConnectTimeout!=null ){
            log.info("SocketConnectTimeout was provided, setting Cassandra client to use SocketConnectTimeout: {} .",socketConnectTimeout);
            socketOptions.setConnectTimeoutMillis(socketConnectTimeout);
        }
        clusterBuilder.withSocketOptions(socketOptions);
    }
    
    private boolean enableAuthentication(Cluster.Builder clusterBuilder, Configuration.CassandrConfig config) {
        boolean authenticate = config.isAuthenticate();
       
        if (authenticate) {
            String username = config.getUsername();
            String password = config.getPassword();
            if (username == null || password == null) {
                log.error("authentication is enabled but username or password were not supplied.");
                return false;
            } else {
                clusterBuilder.withCredentials(username, password);
            }

        }
        return true;
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
    
    
    public void closeCluster() {
        if (isConnected) {
            cluster.close();
        }
        log.info("** CassandraClient cluster closed");
    }

}
