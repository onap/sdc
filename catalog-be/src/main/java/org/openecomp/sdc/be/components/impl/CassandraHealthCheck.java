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
package org.openecomp.sdc.be.components.impl;

import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.cassandra.schema.SdcSchemaUtils;
import org.openecomp.sdc.be.dao.cassandra.schema.Table;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.springframework.stereotype.Component;

@Component("cassandra-health-check")
public class CassandraHealthCheck {

    private static final Logger log = Logger.getLogger(CassandraHealthCheck.class);
    private final Set<String> sdcKeyspaces = new HashSet<>();
    private String localDataCenterName;
    private int HC_FormulaNumber;
    private SdcSchemaUtils sdcSchemaUtils;

    @PostConstruct
    private void init() {
        //Initialize local data center name - this field must be filled by DevOps
        localDataCenterName = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().getLocalDataCenter();
        if (GeneralUtility.isEmptyString(localDataCenterName)) {
            log.error("localDataCenter Name in configuration.yaml is missing.");
            return;
        }
        //Collect all SDC keyspaces
        for (Table table : Table.values()) {
            sdcKeyspaces.add(table.getTableDescription().getKeyspace());
        }
        String janusGraphCfgFile = ConfigurationManager.getConfigurationManager().getConfiguration().getJanusGraphCfgFile();
        Properties prop = new Properties();
        try (final InputStream janusGraphProp = new FileInputStream(janusGraphCfgFile)) {
            //load a properties file
            prop.load(janusGraphProp);
            //Add janusgraph keyspace
            String janusGraphKeyspace = prop.getProperty("storage.cassandra.keyspace");
            if (!GeneralUtility.isEmptyString(janusGraphKeyspace)) {
                sdcKeyspaces.add(janusGraphKeyspace);
            }
        } catch (Exception e) {
            log.error("Failed to open janusGraph.properties file , url is : {}", janusGraphCfgFile, e);
        }
        log.info("All sdc keyspaces are : {}", sdcKeyspaces);
        sdcSchemaUtils = new SdcSchemaUtils();
        //Calculate the Formula of Health Check
        try {
            log.debug("creating cluster for Cassandra Health Check.");
            //Create cluster from nodes in cassandra configuration
            Metadata metadata = sdcSchemaUtils.getMetadata();
            if (metadata == null) {
                log.error("Failure get cassandra metadata.");
                return;
            }
            log.debug("Cluster Metadata: {}", metadata);
            List<KeyspaceMetadata> keyspaces = metadata.getKeyspaces();
            List<Integer> replactionFactorList = new ArrayList<>();
            //Collect the keyspaces Replication Factor of current localDataCenter
            for (KeyspaceMetadata keyspace : keyspaces) {
                if (sdcKeyspaces.contains(keyspace.getName())) {
                    log.debug("keyspace : {} , replication: {}", keyspace.getName(), keyspace.getReplication());
                    Map<String, String> replicationOptions = keyspace.getReplication();
                    //In 1 site with one data center
                    if (replicationOptions.containsKey("replication_factor")) {
                        replactionFactorList.add(Integer.parseInt(replicationOptions.get("replication_factor")));
                    }
                    //In multiple sites with some data center
                    else if (replicationOptions.containsKey(localDataCenterName)) {
                        replactionFactorList.add(Integer.parseInt(replicationOptions.get(localDataCenterName)));
                    }
                }
            }
            if (replactionFactorList.isEmpty()) {
                log.error("Replication factor NOT found in all keyspaces");
                return;
            }
            int maxReplicationFactor = Collections.max(replactionFactorList);
            log.debug("maxReplication Factor is: {}", maxReplicationFactor);
            int localQuorum = maxReplicationFactor / 2 + 1;
            log.debug("localQuorum is: {}", localQuorum);
            HC_FormulaNumber = maxReplicationFactor - localQuorum;
            log.debug("Health Check formula : Replication Factor – Local_Quorum = {}", HC_FormulaNumber);
        } catch (Exception e) {
            log.error("create cassandra cluster failed with exception.", e);
        }
    }

    public boolean getCassandraStatus() {
        if (GeneralUtility.isEmptyString(localDataCenterName)) {
            log.error("localDataCenter Name in configuration.yaml is missing.");
            return false;
        }
        try (final Session session = sdcSchemaUtils.connect()) {
            log.debug("creating cluster for Cassandra for monitoring.");
            log.debug("The cassandra session is {}", session);
            if (session == null) {
                log.error("Failed to connect to cassandra ");
                return false;
            }
            Metadata metadata = sdcSchemaUtils.getMetadata();
            if (metadata == null) {
                log.error("Failure get cassandra metadata.");
                return false;
            }
            log.debug("The number of cassandra nodes is:{}", metadata.getAllHosts().size());
            //Count the number of data center nodes that are down
            Long downHostsNumber = metadata.getAllHosts().stream().filter(x -> x.getDatacenter().equals(localDataCenterName) && !x.isUp()).count();
            if(downHostsNumber > 0) {
                log.warn("{} cassandra nodes are down", downHostsNumber);
            } else {
                log.debug("The cassandra down nodes number is {}", downHostsNumber);
            }
            return HC_FormulaNumber >= downHostsNumber;
        } catch (Exception e) {
            log.error("create cassandra cluster failed with exception.", e);
            return false;
        }
    }

    @PreDestroy
    public void closeClient() {
        if (sdcSchemaUtils != null) {
            sdcSchemaUtils.closeCluster();
        }
        log.info("** sdcSchemaUtils cluster closed");
    }
}
