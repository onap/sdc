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

import java.util.Arrays;
import java.util.Optional;

import org.openecomp.sdc.asdctool.impl.PopulateComponentCache;
import org.openecomp.sdc.asdctool.impl.migration.v1604.AppConfig;
import org.openecomp.sdc.asdctool.impl.migration.v1604.DerivedFromAlignment;
import org.openecomp.sdc.asdctool.impl.migration.v1604.GroupsAlignment;
import org.openecomp.sdc.asdctool.impl.migration.v1604.ServiceMigration;
import org.openecomp.sdc.asdctool.impl.migration.v1604.VfcNamingAlignment;
import org.openecomp.sdc.asdctool.impl.migration.v1607.CsarMigration;
import org.openecomp.sdc.asdctool.impl.migration.v1610.TitanFixUtils;
import org.openecomp.sdc.asdctool.impl.migration.v1610.ToscaArtifactsAlignment;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MigrationMenu {

	private static Logger log = LoggerFactory.getLogger(MigrationMenu.class.getName());
	private static final String SERVICE_MIGARTION_BEAN = "serviceMigrationBean";

	private static enum MigrationOperationEnum {
		MIGRATION_1602_1604("migrate-1602-1604", SERVICE_MIGARTION_BEAN), 
		ALIGN_DERIVED_FROM_1604("align-derived-from-1604", "derivedFromAlignment"), 
		MIGRATE_1604_1607("migrate-1604-1607", SERVICE_MIGARTION_BEAN), 
		ALIGN_VFC_NAMES_1604("align-vfc-names-1604", "vfcNamingAlignmentBean"), 
		TEST_REMOVE_HEAT_PLACEHOLDERS("testremoveheatplaceholders",	SERVICE_MIGARTION_BEAN), 
		TEST_ADD_GROUP_UUIDS("testaddgroupuuids", SERVICE_MIGARTION_BEAN), 
		ALIGN_GROUPS("align-groups", "groupsAlignment"), 
		CLEAN_CSAR("clean-csar", "csarMigration"), 
		POPULATE_COMPONENT_CACHE("populate-component-cache", "populateComponentCache"), 
		FIX_PROPERTIES("fix-properties", "titanFixUtils"), 
		ALIGN_TOSCA_ARTIFACTS("align-tosca-artifacts", "toscaArtifactsAlignment"), 
		FIX_ICONS("fix-icons", "titanFixUtils");

		private String value, beanName;

		public static MigrationOperationEnum findByValue(String value) {
			Optional<MigrationOperationEnum> optionalFound = Arrays.asList(MigrationOperationEnum.values()).stream().filter(e -> e.getValue().equalsIgnoreCase(value)).findAny();
			return optionalFound.isPresent() ? optionalFound.get() : null;
		}

		MigrationOperationEnum(String value, String beanName) {
			this.value = value;
			this.beanName = beanName;
		}

		public String getValue() {
			return value;
		}

		public String getBeanName() {
			return beanName;
		}
	};

	public static void main(String[] args) throws Exception {

		if (args == null || args.length < 2) {
			usageAndExit();
		}
		MigrationOperationEnum operationEnum = MigrationOperationEnum.findByValue(args[0]);
		String appConfigDir = args[1];
		String dataInputFileDir = null;
		if (operationEnum == MigrationOperationEnum.ALIGN_DERIVED_FROM_1604) {
			dataInputFileDir = args[2];
		}
		AnnotationConfigApplicationContext context = initContext(appConfigDir);
		try {
			ServiceMigration serviceMigration = (ServiceMigration) context.getBean(SERVICE_MIGARTION_BEAN);
			switch (operationEnum) {
			case MIGRATION_1602_1604:
				log.debug("Start Titan migration from 1602 version to 1604");
				if (serviceMigration.migrate1602to1604(appConfigDir)) {
					log.debug("Titan migration from 1602 version to 1604 was finished successfull");
					System.exit(0);
				} else {
					log.debug("Titan migration from 1602 version to 1604 was failed");
					System.exit(2);
				}
			case MIGRATE_1604_1607:
				log.debug("Start Titan migration from 1604 version to 1607");
				if (serviceMigration.migrate1604to1607(appConfigDir)) {
					log.debug("Titan migration from 1604 version to 1607 was finished successfull");
					System.exit(0);
				} else {
					log.debug("Titan migration from 1604 version to 1607 was failed");
					System.exit(2);
				}
				break;
			case ALIGN_VFC_NAMES_1604:
				VfcNamingAlignment vfcNamingAlignment = (VfcNamingAlignment) context.getBean(operationEnum.getBeanName());
				log.debug("Start VFC naming alignment on 1604");
				if (vfcNamingAlignment.alignVfcNames1604(appConfigDir)) {
					log.debug("VFC naming alignment on 1604 was finished successfull");
					System.exit(0);
				} else {
					log.debug("VFC naming alignment on 1604 was failed");
					System.exit(2);
				}
				break;
			case TEST_REMOVE_HEAT_PLACEHOLDERS:
				boolean check = serviceMigration.testRemoveHeatPlaceHolders(appConfigDir);
				if (check == true) {
					System.exit(0);
				} else {
					System.exit(2);
				}
				break;
			case TEST_ADD_GROUP_UUIDS:
				check = serviceMigration.testAddGroupUuids(appConfigDir);
				if (check == true) {
					System.exit(0);
				} else {
					System.exit(2);
				}
				break;
			case ALIGN_DERIVED_FROM_1604:
				DerivedFromAlignment derivedFromAlignment = (DerivedFromAlignment) context.getBean(operationEnum.getBeanName());
				log.debug("Start derived from alignment on 1604");
				if (derivedFromAlignment.alignDerivedFrom1604(appConfigDir, dataInputFileDir)) {
					log.debug("Derived from alignment on 1604 was finished successfull");
					System.exit(0);
				} else {
					log.debug("Derived from alignment on 1604 was failed");
					System.exit(2);
				}
				break;
			case ALIGN_GROUPS:
				GroupsAlignment groupsAlignment = (GroupsAlignment) context.getBean(operationEnum.getBeanName());
				log.debug("Start derived from alignment on 1604");
				if (groupsAlignment.alignGroups(appConfigDir)) {
					log.debug("Groups alignment was finished successfull");
					System.exit(0);
				} else {
					log.debug("Groups alignment was failed");
					System.exit(2);
				}
				break;
			case CLEAN_CSAR:
				log.debug("Start remove CSAR resources");
				CsarMigration csarMigration = (CsarMigration) context.getBean(operationEnum.getBeanName());
				// TODO Show to Michael L fixed return value
				if (csarMigration.removeCsarResources()) {
					log.debug("Remove CSAR resources finished successfully");
					System.exit(0);
				} else {
					log.debug("Remove CSAR resources failed");
					System.exit(2);
				}
				break;
			case POPULATE_COMPONENT_CACHE:
				PopulateComponentCache populateComponentCache = (PopulateComponentCache) context.getBean(operationEnum.getBeanName());
				// TODO Show to Michael L No return value always returns 0
				populateComponentCache.populateCache();
				System.exit(0);
				break;
			case FIX_PROPERTIES:
				log.debug("Start fix capability properties types");
				TitanFixUtils titanFixUtils = (TitanFixUtils) context.getBean(operationEnum.getBeanName());
				// TODO Show to Michael L fixed return value
				if (titanFixUtils.fixCapabiltyPropertyTypes()) {
					log.debug("Fix capability properties types finished successfully");
					System.exit(0);
				} else {
					log.debug("Fix capability properties types failed");
					System.exit(2);
				}
				break;
			case FIX_ICONS:
				log.debug("Start fix icons of vl and eline");
				titanFixUtils = (TitanFixUtils) context.getBean(operationEnum.getBeanName());
				// TODO Show to Michael L fixed return value
				if (titanFixUtils.fixIconsInNormatives()) {
					log.debug("Fix icons of vl and eline finished successfully");
					System.exit(0);
				} else {
					log.debug("Fix icons of vl and eline failed");
					System.exit(2);
				}
				break;
			case ALIGN_TOSCA_ARTIFACTS:
				log.debug("Start align tosca artifacts");
				ToscaArtifactsAlignment toscaArtifactsAlignment = (ToscaArtifactsAlignment) context.getBean(operationEnum.getBeanName());
				boolean isSuccessful = toscaArtifactsAlignment.alignToscaArtifacts();
				if (isSuccessful) {
					log.debug("Tosca Artifacts alignment was finished successfull");
					System.exit(0);
				} else {
					log.debug("Tosca Artifacts alignment has failed");
					System.exit(2);
				}
				break;
			default:
				usageAndExit();
			}
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(3);
		} finally {
			context.close();
		}
	}

	private static AnnotationConfigApplicationContext initContext(String appConfigDir) {
		ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
		ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		return context;
	}

	private static void usageAndExit() {
		MigrationUsage();
		System.exit(1);
	}

	private static void MigrationUsage() {
		System.out.println("Usage: migrate-1602-1604 <configuration dir>");
		System.out.println("Usage: migrate-1604-1607 <configuration dir>");
		System.out.println("Usage: align-vfc-names-1604 <configuration dir>");
		System.out.println("Usage: align-derived-from-1604 <configuration dir> <data_input_file dir>");
		System.out.println("Usage: align-groups <configuration dir>");
		System.out.println("Usage: fix-properties <configuration dir>");
	}
}
