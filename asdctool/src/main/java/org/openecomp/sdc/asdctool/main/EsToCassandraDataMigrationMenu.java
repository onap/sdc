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

import org.openecomp.sdc.asdctool.impl.DataMigration;
import org.openecomp.sdc.asdctool.impl.EsToCassandraDataMigrationConfig;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class EsToCassandraDataMigrationMenu {

	private static Logger log = LoggerFactory.getLogger(EsToCassandraDataMigrationMenu.class.getName());

	public static void main(String[] args) throws Exception {

		if (args == null || args.length < 2) {
			usageAndExit();
		}
		String operation = args[0];

		String appConfigDir = args[1];
		System.setProperty("config.home", appConfigDir);
		ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(),
				appConfigDir);
		ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				EsToCassandraDataMigrationConfig.class);
		DataMigration dataMigration = null;
		try {
			switch (operation.toLowerCase()) {
			case "es-to-cassndra-migration":
				dataMigration = (DataMigration) context.getBean("DataMigrationBean");
				log.debug("Start migration from ES to C* ");
				if (dataMigration.migrateDataESToCassndra(appConfigDir, true, true)) {
					log.debug("migration from ES to C* was finished successfull");
					System.exit(0);
				} else {
					log.debug("migration from ES to C* failed");
					System.exit(2);
				}
				break;
			case "es-to-cassndra-migration-export-only":
				dataMigration = (DataMigration) context.getBean("DataMigrationBean");
				log.debug("Start migration export only from ES to C* ");
				if (dataMigration.migrateDataESToCassndra(appConfigDir, true, false)) {
					log.debug("migration export only from ES to C* was finished successfull");
					System.exit(0);
				} else {
					log.debug("migration export only from ES to C* failed");
					System.exit(2);
				}
				break;
			case "es-to-cassndra-migration-import-only":
				dataMigration = (DataMigration) context.getBean("DataMigrationBean");
				log.debug("Start migration import only from ES to C* ");
				if (dataMigration.migrateDataESToCassndra(appConfigDir, false, true)) {
					log.debug("migration import only from ES to C* was finished successfull");
					System.exit(0);
				} else {
					log.debug("migration import only from ES to C* failed");
					System.exit(2);
				}
				break;
			default:
				usageAndExit();
			}
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(3);
		}
	}

	private static void usageAndExit() {
		MigrationUsage();
		System.exit(1);
	}

	private static void MigrationUsage() {
		System.out.println(
				"Usage: es-to-cassndra-migration/es-to-cassndra-migration-import-only/es-to-cassndra-migration-export-only <configuration dir>");
	}
}
