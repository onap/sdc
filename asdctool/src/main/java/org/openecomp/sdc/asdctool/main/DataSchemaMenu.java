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

package org.openecomp.sdc.asdctool.main;

import org.openecomp.sdc.asdctool.impl.JanusGraphInitializer;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.cassandra.schema.SdcSchemaBuilder;
import org.openecomp.sdc.be.dao.cassandra.schema.SdcSchemaUtils;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.log.wrappers.Logger;

public class DataSchemaMenu {

	private static Logger log = Logger.getLogger(DataSchemaMenu.class.getName());

    public static void main(String[] args) {

		String operation = args[0];

        String appConfigDir = args[1];

        if (args == null || args.length < 2) {
            usageAndExit();
        }

        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

        SdcSchemaBuilder sdcSchemaBuilder = new SdcSchemaBuilder(new SdcSchemaUtils(),
            ConfigurationManager.getConfigurationManager().getConfiguration()::getCassandraConfig);

        switch (operation.toLowerCase()) {
            case "create-cassandra-structures":
                log.debug("Start create cassandra keyspace, tables and indexes");
                if (sdcSchemaBuilder.createSchema()) {
                    log.debug("create cassandra keyspace, tables and indexes successfull");
                    System.exit(0);
                } else {
                    log.debug("create cassandra keyspace, tables and indexes failed");
                    System.exit(2);
                }
                break;
            case "create-janusgraph-structures":
                log.debug("Start create janusgraph keyspace");
                String janusGraphCfg = 2 == args.length ? configurationManager.getConfiguration().getTitanCfgFile() : args[2];
                if (JanusGraphInitializer.createGraph(janusGraphCfg)) {
                    log.debug("create janusgraph keyspace successfull");
                    System.exit(0);
                } else {
                    log.debug("create janusgraph keyspace failed");
                    System.exit(2);
                }
                break;
            case "clean-cassndra":
                log.debug("Start clean keyspace, tables");
                if (sdcSchemaBuilder.deleteSchema()) {
                    log.debug(" successfull");
                    System.exit(0);
                } else {
                    log.debug(" failed");
                    System.exit(2);
                }
                break;
            default:
                usageAndExit();
                break;
        }
    }

    private static void usageAndExit() {
        DataSchemeUsage();
        System.exit(1);
    }

    private static void DataSchemeUsage() {
        System.out.println("Usage: create-cassandra-structures <configuration dir> ");
        System.out.println("Usage: create-janusgraph-structures <configuration dir> ");
    }
}
