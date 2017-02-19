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

package org.openecomp.sdc.asdctool.main;

import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.cassandra.schema.SdcSchemaBuilder;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSchemaMenu {

	private static Logger log = LoggerFactory.getLogger(DataSchemaMenu.class.getName());

	public static void main(String[] args) throws Exception {

		String operation = args[0];

		String appConfigDir = args[1];

		if (args == null || args.length < 2) {
			usageAndExit();
		}

		ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(),
				appConfigDir);
		ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

		try {

			switch (operation.toLowerCase()) {
			case "create-cassandra-structures":
				log.debug("Start create cassandra keyspace, tables and indexes");
				if (SdcSchemaBuilder.createSchema()) {
					log.debug("create cassandra keyspace, tables and indexes successfull");
					System.exit(0);
				} else {
					log.debug("create cassandra keyspace, tables and indexes failed");
					System.exit(2);
				}
			case "create-titan-structures":
				log.debug("Start create titan keyspace, tables and indexes");
				if (SdcSchemaBuilder.createSchema()) {
					log.debug("create cassandra keyspace, tables and indexes successfull");
					System.exit(0);
				} else {
					log.debug("create cassandra keyspace, tables and indexes failed");
					System.exit(2);
				}
			case "clean-cassndra":
				log.debug("Start clean keyspace, tables");
				if (SdcSchemaBuilder.deleteSchema()) {
					log.debug(" successfull");
					System.exit(0);
				} else {
					log.debug(" failed");
					System.exit(2);
				}
			default:
				usageAndExit();
			}
		} catch (Throwable t) {
			t.printStackTrace();
			log.debug("create cassandra keyspace, tables and indexes failed");
			System.exit(3);
		}
	}

	private static void usageAndExit() {
		DataSchemeUsage();
		System.exit(1);
	}

	private static void DataSchemeUsage() {
		System.out.println("Usage: create-cassandra-structures <configuration dir> ");
	}
}
