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

package org.openecomp.sdc.asdctool.impl;


import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fj.data.Either;
import org.apache.commons.lang.SystemUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.openecomp.sdc.be.auditing.api.AuditEventFactory;
import org.openecomp.sdc.be.auditing.impl.AuditAuthRequestEventFactory;
import org.openecomp.sdc.be.auditing.impl.AuditConsumerEventFactory;
import org.openecomp.sdc.be.auditing.impl.AuditGetUebClusterEventFactory;
import org.openecomp.sdc.be.auditing.impl.category.AuditCategoryEventFactory;
import org.openecomp.sdc.be.auditing.impl.category.AuditGetCategoryHierarchyEventFactory;
import org.openecomp.sdc.be.auditing.impl.distribution.*;
import org.openecomp.sdc.be.auditing.impl.resourceadmin.AuditResourceAdminEventMigrationFactory;
import org.openecomp.sdc.be.auditing.impl.usersadmin.AuditGetUsersListEventFactory;
import org.openecomp.sdc.be.auditing.impl.usersadmin.AuditUserAccessEventFactory;
import org.openecomp.sdc.be.auditing.impl.usersadmin.AuditUserAdminEventFactory;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.cassandra.schema.Table;
import org.openecomp.sdc.be.dao.es.ElasticSearchClient;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;
import org.openecomp.sdc.be.resources.data.auditing.model.*;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKey;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;

/**
 * Created by mlando on 5/16/2016.
 */
public class DataMigration {

	private ObjectMapper jsonMapper = new ObjectMapper();

	private static Logger log = Logger.getLogger(DataMigration.class.getName());

	private ElasticSearchClient elasticSearchClient;
	@Autowired
	private AuditCassandraDao auditCassandraDao;
	@Autowired
	private ArtifactCassandraDao artifactCassandraDao;

    /**
	 * the method exports and imports the records from ES to cassandra the flow
	 * will check to see if the files are not empty if the files are not empty
	 * the export will be skiped and the flow will use the existing files. the
	 * flow will check if the tables in cassandra are empty, if the tables are
	 * not empty the proces will stop and exit. if the tables are empty the
	 * method will import the records from the files. in case of a fail the flow
	 * will exit and clear all the Cassandra tables.
	 *
	 * @param appConfigDir
	 *            the location of the dir in wich the output files will be
	 *            stored
	 * @param exportFromEs
	 *            should the es be exported again and overwrite the old export
	 * @param importToCassandra
	 *            should we import the data into cassandra
	 * @return true in case the operation was successful.
	 */
	public boolean migrateDataESToCassndra(String appConfigDir, boolean exportFromEs, boolean importToCassandra) {
		if (!initEsClient()) {
			return false;
		}
		Map<Table, File> files = createOutPutFiles(appConfigDir, exportFromEs);
		if (files == null) {
			return false;
		}
		if (exportFromEs && filesEmpty(files)) {
			Map<Table, PrintWriter> printerWritersMap = createWriters(files);
			if (printerWritersMap == null) {
				return false;
			}
			try {
				ImmutableOpenMap<String, IndexMetaData> indexData = getIndexData();
				for (ObjectCursor<String> key : indexData.keys()) {
					if (("resources".equalsIgnoreCase(key.value) || key.value.startsWith("auditingevents"))
                        && !exportArtifacts(key.value, printerWritersMap)) {
                        return false;
                    }
				}
			} finally {
				if (elasticSearchClient != null) {
					elasticSearchClient.close();
				}
				for (PrintWriter writer : printerWritersMap.values()) {
					writer.close();
				}
			}
		}

		return !importToCassandra || importToCassndra(files);
	}

	private boolean initEsClient() {
		String configHome = System.getProperty("config.home");
		URL url = null;
		Settings settings = null;
		try {
			if (SystemUtils.IS_OS_WINDOWS) {
				url = new URL("file:///" + configHome + "/elasticsearch.yml");
			} else {
				url = new URL("file:" + configHome + "/elasticsearch.yml");
			}
			log.debug("URL {}", url);
			settings = Settings.settingsBuilder().loadFromPath(Paths.get(url.toURI())).build();
		} catch (MalformedURLException | URISyntaxException e1) {
			log.error("Failed to create URL in order to load elasticsearch yml", e1);
			return true;
		}

		this.elasticSearchClient = new ElasticSearchClient();
		this.elasticSearchClient.setClusterName(settings.get("cluster.name"));
		this.elasticSearchClient.setLocal(settings.get("elasticSearch.local"));
		this.elasticSearchClient.setTransportClient(settings.get("elasticSearch.transportclient"));
		try {
			elasticSearchClient.initialize();
		} catch (URISyntaxException e) {
		    log.error(e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * the method clears all the cassandra tables
	 */
	private void truncateCassandraTable() {
		log.info("import failed. truncating Cassandra tables.");
		artifactCassandraDao.deleteAllArtifacts();
		auditCassandraDao.deleteAllAudit();
	}

	/**
	 * the method imports the records from the files into cassandra
	 * 
	 * @param files
	 *            a map of files holding
	 * @return true if the operation was successful
	 */
	private boolean importToCassndra(Map<Table, File> files) {
		log.info("starting to import date into Cassandra.");
		if (!validtaTablsNotEmpty(files))
			return true;
		for (Table table : files.keySet()) {
			log.info("importing recordes into {}", table.getTableDescription().getTableName());
			if (!handleImport(files, table)) {
				truncateCassandraTable();
				return false;
			}
		}
		log.info("finished to import date into Cassandra.");
		return true;
	}

	private boolean validtaTablsNotEmpty(Map<Table, File> files) {
		for (Table table : files.keySet()) {
			Either<Boolean, CassandraOperationStatus> isTableEmptyRes = checkIfTableIsEmpty(table);
			if (isTableEmptyRes.isRight() || !isTableEmptyRes.left().value()) {
				log.error("Cassandra table {} is not empty operation aborted.",
						table.getTableDescription().getTableName());
				return false;
			}
		}
		return true;
	}

	/**
	 * the method retrieves the fields from the given map and generates
     * corresponding audit event according to the table name
	 * 
	 * @param map
	 *            the map from which we will retrieve the fields enum values
	 * @param table
	 *            the table we are going to store the record in.
	 * @return an AuditingGenericEvent event representing the audit record that is going to be
	 *         created.
	 */
	AuditingGenericEvent createAuditEvent(Map<AuditingFieldsKey, String> map, Table table) {
		AuditEventFactory factory = null;
		switch (table) {
			case USER_ADMIN_EVENT:
				factory = getAuditUserAdminEventFactory(map);
				break;
			case USER_ACCESS_EVENT:
				factory = getAuditUserAccessEventFactory(map);
				break;
			case RESOURCE_ADMIN_EVENT:
				factory = getAuditResourceAdminEventMigrationFactory(map);
				break;
			case DISTRIBUTION_DOWNLOAD_EVENT:
				factory = getAuditDistributionDownloadEventFactory(map);
				break;
			case DISTRIBUTION_ENGINE_EVENT:
				factory = getAuditDistributionEngineEventMigrationFactory(map);
				break;
			case DISTRIBUTION_NOTIFICATION_EVENT:
				factory = getAuditDistributionNotificationEventFactory(map);
				break;
			case DISTRIBUTION_STATUS_EVENT:
				factory = getAuditDistributionStatusEventFactory(map);
				break;
			case DISTRIBUTION_DEPLOY_EVENT:
				factory = getAuditDistributionDeployEventFactory(map);
				break;
			case DISTRIBUTION_GET_UEB_CLUSTER_EVENT:
				factory = getAuditGetUebClusterEventFactory(map);
				break;
			case AUTH_EVENT:
				factory = getAuditAuthRequestEventFactory(map);
				break;
			case CONSUMER_EVENT:
				factory = getAuditConsumerEventFactory(map);
				break;
			case CATEGORY_EVENT:
				factory = getAuditCategoryEventFactory(map);
				break;
			case GET_USERS_LIST_EVENT:
				factory = getAuditGetUsersListEventFactory(map);
				break;
			case GET_CATEGORY_HIERARCHY_EVENT:
				factory = getAuditGetCategoryHierarchyEventFactory(map);
				break;
			default:
				break;
		}
		return factory != null ? factory.getDbEvent() : null;
	}

	private AuditEventFactory getAuditGetCategoryHierarchyEventFactory(Map<AuditingFieldsKey, String> map) {
		return new AuditGetCategoryHierarchyEventFactory(
			CommonAuditData.newBuilder()
					.description(map.get(AuditingFieldsKey.AUDIT_DESC))
					.status(map.get(AuditingFieldsKey.AUDIT_STATUS))
					.requestId(map.get(AuditingFieldsKey.AUDIT_REQUEST_ID))
					.serviceInstanceId(map.get(AuditingFieldsKey.AUDIT_SERVICE_INSTANCE_ID))
					.build(),
			map.get(AuditingFieldsKey.AUDIT_MODIFIER_UID),
			map.get(AuditingFieldsKey.AUDIT_DETAILS),
			map.get(AuditingFieldsKey.AUDIT_TIMESTAMP));
	}

	private AuditEventFactory getAuditGetUsersListEventFactory(Map<AuditingFieldsKey, String> map) {
		return new AuditGetUsersListEventFactory(
			CommonAuditData.newBuilder()
					.description(map.get(AuditingFieldsKey.AUDIT_DESC))
					.status(map.get(AuditingFieldsKey.AUDIT_STATUS))
					.requestId(map.get(AuditingFieldsKey.AUDIT_REQUEST_ID))
					.serviceInstanceId(map.get(AuditingFieldsKey.AUDIT_SERVICE_INSTANCE_ID))
					.build(),
			map.get(AuditingFieldsKey.AUDIT_MODIFIER_UID),
			map.get(AuditingFieldsKey.AUDIT_USER_DETAILS),
			map.get(AuditingFieldsKey.AUDIT_TIMESTAMP));
	}

	private AuditEventFactory getAuditCategoryEventFactory(Map<AuditingFieldsKey, String> map) {
		return new AuditCategoryEventFactory(
			AuditingActionEnum.fromName(map.get(AuditingFieldsKey.AUDIT_ACTION)),
			CommonAuditData.newBuilder()
					.description(map.get(AuditingFieldsKey.AUDIT_DESC))
					.status(map.get(AuditingFieldsKey.AUDIT_STATUS))
					.requestId(map.get(AuditingFieldsKey.AUDIT_REQUEST_ID))
					.serviceInstanceId(map.get(AuditingFieldsKey.AUDIT_SERVICE_INSTANCE_ID))
					.build(),
			map.get(AuditingFieldsKey.AUDIT_MODIFIER_UID),
			map.get(AuditingFieldsKey.AUDIT_CATEGORY_NAME),
			map.get(AuditingFieldsKey.AUDIT_SUB_CATEGORY_NAME),
			map.get(AuditingFieldsKey.AUDIT_GROUPING_NAME),
			map.get(AuditingFieldsKey.AUDIT_RESOURCE_TYPE),
			map.get(AuditingFieldsKey.AUDIT_TIMESTAMP));
	}

	private AuditEventFactory getAuditUserAccessEventFactory(Map<AuditingFieldsKey, String> map) {
		return new AuditUserAccessEventFactory(
			CommonAuditData.newBuilder()
					.description(map.get(AuditingFieldsKey.AUDIT_DESC))
					.status(map.get(AuditingFieldsKey.AUDIT_STATUS))
					.requestId(map.get(AuditingFieldsKey.AUDIT_REQUEST_ID))
					.serviceInstanceId(map.get(AuditingFieldsKey.AUDIT_SERVICE_INSTANCE_ID))
					.build(),
			map.get(AuditingFieldsKey.AUDIT_USER_UID),
			map.get(AuditingFieldsKey.AUDIT_TIMESTAMP));
	}

	private AuditEventFactory getAuditUserAdminEventFactory(Map<AuditingFieldsKey, String> map) {
		return new AuditUserAdminEventFactory(
            AuditingActionEnum.fromName(map.get(AuditingFieldsKey.AUDIT_ACTION)),
			CommonAuditData.newBuilder()
					.description(map.get(AuditingFieldsKey.AUDIT_DESC))
					.status(map.get(AuditingFieldsKey.AUDIT_STATUS))
					.requestId(map.get(AuditingFieldsKey.AUDIT_REQUEST_ID))
					.serviceInstanceId(map.get(AuditingFieldsKey.AUDIT_SERVICE_INSTANCE_ID))
					.build(),
			map.get(AuditingFieldsKey.AUDIT_MODIFIER_UID),
			map.get(AuditingFieldsKey.AUDIT_USER_BEFORE),
			map.get(AuditingFieldsKey.AUDIT_USER_AFTER),
			map.get(AuditingFieldsKey.AUDIT_TIMESTAMP));
	}

	private AuditEventFactory getAuditConsumerEventFactory(Map<AuditingFieldsKey, String> map) {
		return new AuditConsumerEventFactory(
		    AuditingActionEnum.fromName(map.get(AuditingFieldsKey.AUDIT_ACTION)),
			CommonAuditData.newBuilder()
					.description(map.get(AuditingFieldsKey.AUDIT_DESC))
					.status(map.get(AuditingFieldsKey.AUDIT_STATUS))
					.requestId(map.get(AuditingFieldsKey.AUDIT_REQUEST_ID))
					.serviceInstanceId(map.get(AuditingFieldsKey.AUDIT_SERVICE_INSTANCE_ID))
					.build(),
			map.get(AuditingFieldsKey.AUDIT_MODIFIER_UID),
			map.get(AuditingFieldsKey.AUDIT_ECOMP_USER),
			map.get(AuditingFieldsKey.AUDIT_TIMESTAMP));
	}

	private AuditEventFactory getAuditAuthRequestEventFactory(Map<AuditingFieldsKey, String> map) {
		return new AuditAuthRequestEventFactory(
			CommonAuditData.newBuilder()
					.description(map.get(AuditingFieldsKey.AUDIT_DESC))
					.status(map.get(AuditingFieldsKey.AUDIT_STATUS))
					.requestId(map.get(AuditingFieldsKey.AUDIT_REQUEST_ID))
					.serviceInstanceId(map.get(AuditingFieldsKey.AUDIT_SERVICE_INSTANCE_ID))
					.build(),
			map.get(AuditingFieldsKey.AUDIT_USER_UID),
			map.get(AuditingFieldsKey.AUDIT_AUTH_URL),
			map.get(AuditingFieldsKey.AUDIT_AUTH_REALM),
			map.get(AuditingFieldsKey.AUDIT_AUTH_STATUS),
			map.get(AuditingFieldsKey.AUDIT_TIMESTAMP));
	}

	private AuditEventFactory getAuditGetUebClusterEventFactory(Map<AuditingFieldsKey, String> map) {
		return new AuditGetUebClusterEventFactory(
			CommonAuditData.newBuilder()
					.description(map.get(AuditingFieldsKey.AUDIT_DESC))
					.status(map.get(AuditingFieldsKey.AUDIT_STATUS))
					.requestId(map.get(AuditingFieldsKey.AUDIT_REQUEST_ID))
					.serviceInstanceId(map.get(AuditingFieldsKey.AUDIT_SERVICE_INSTANCE_ID))
					.build(),
			map.get(AuditingFieldsKey.AUDIT_DISTRIBUTION_CONSUMER_ID),
			map.get(AuditingFieldsKey.AUDIT_TIMESTAMP));
	}

	private AuditEventFactory getAuditResourceAdminEventMigrationFactory(Map<AuditingFieldsKey, String> map) {
		return new AuditResourceAdminEventMigrationFactory(
                AuditingActionEnum.fromName(map.get(AuditingFieldsKey.AUDIT_ACTION)),
			CommonAuditData.newBuilder()
					.description(map.get(AuditingFieldsKey.AUDIT_DESC))
					.status(map.get(AuditingFieldsKey.AUDIT_STATUS))
					.requestId(map.get(AuditingFieldsKey.AUDIT_REQUEST_ID))
					.serviceInstanceId(map.get(AuditingFieldsKey.AUDIT_SERVICE_INSTANCE_ID))
					.build(),
			new ResourceCommonInfo(map.get(AuditingFieldsKey.AUDIT_RESOURCE_NAME),
					map.get(AuditingFieldsKey.AUDIT_RESOURCE_TYPE)),
			ResourceVersionInfo.newBuilder()
					.artifactUuid(map.get(AuditingFieldsKey.AUDIT_PREV_ARTIFACT_UUID))
					.state(map.get(AuditingFieldsKey.AUDIT_RESOURCE_PREV_STATE))
					.version(map.get(AuditingFieldsKey.AUDIT_RESOURCE_PREV_VERSION))
					.distributionStatus(map.get(AuditingFieldsKey.AUDIT_RESOURCE_DPREV_STATUS))
					.build(),
			ResourceVersionInfo.newBuilder()
					.artifactUuid(map.get(AuditingFieldsKey.AUDIT_CURR_ARTIFACT_UUID))
					.state(map.get(AuditingFieldsKey.AUDIT_RESOURCE_CURR_STATE))
					.version(map.get(AuditingFieldsKey.AUDIT_RESOURCE_CURR_VERSION))
					.distributionStatus(map.get(AuditingFieldsKey.AUDIT_RESOURCE_DCURR_STATUS))
					.build(),
			map.get(AuditingFieldsKey.AUDIT_INVARIANT_UUID),
			map.get(AuditingFieldsKey.AUDIT_MODIFIER_UID),
			map.get(AuditingFieldsKey.AUDIT_ARTIFACT_DATA),
			map.get(AuditingFieldsKey.AUDIT_RESOURCE_COMMENT),
			map.get(AuditingFieldsKey.AUDIT_DISTRIBUTION_ID),
			map.get(AuditingFieldsKey.AUDIT_RESOURCE_TOSCA_NODE_TYPE),
			map.get(AuditingFieldsKey.AUDIT_TIMESTAMP));
	}

	private AuditEventFactory getAuditDistributionDownloadEventFactory(Map<AuditingFieldsKey, String> map) {
		return new AuditDistributionDownloadEventFactory(
			CommonAuditData.newBuilder()
					.description(map.get(AuditingFieldsKey.AUDIT_DESC))
					.status(map.get(AuditingFieldsKey.AUDIT_STATUS))
					.requestId(map.get(AuditingFieldsKey.AUDIT_REQUEST_ID))
					.serviceInstanceId(map.get(AuditingFieldsKey.AUDIT_SERVICE_INSTANCE_ID))
					.build(),
			new DistributionData(map.get(AuditingFieldsKey.AUDIT_DISTRIBUTION_CONSUMER_ID),
					map.get(AuditingFieldsKey.AUDIT_RESOURCE_URL)),
			        map.get(AuditingFieldsKey.AUDIT_TIMESTAMP));
	}

	private AuditEventFactory getAuditDistributionEngineEventMigrationFactory(Map<AuditingFieldsKey, String> map) {
		return new AuditDistributionEngineEventMigrationFactory(
		    AuditingActionEnum.fromName(map.get(AuditingFieldsKey.AUDIT_ACTION)),
			CommonAuditData.newBuilder()
					.description(map.get(AuditingFieldsKey.AUDIT_DESC))
					.status(map.get(AuditingFieldsKey.AUDIT_STATUS))
					.requestId(map.get(AuditingFieldsKey.AUDIT_REQUEST_ID))
					.serviceInstanceId(map.get(AuditingFieldsKey.AUDIT_SERVICE_INSTANCE_ID))
					.build(),
			DistributionTopicData.newBuilder()
					.notificationTopic(map.get(AuditingFieldsKey.AUDIT_DISTRIBUTION_NOTIFICATION_TOPIC_NAME))
					.statusTopic(map.get(AuditingFieldsKey.AUDIT_DISTRIBUTION_STATUS_TOPIC_NAME))
					.build(),
			map.get(AuditingFieldsKey.AUDIT_DISTRIBUTION_CONSUMER_ID),
			map.get(AuditingFieldsKey.AUDIT_DISTRIBUTION_API_KEY),
			map.get(AuditingFieldsKey.AUDIT_DISTRIBUTION_ENVRIONMENT_NAME),
			map.get(AuditingFieldsKey.AUDIT_DISTRIBUTION_ROLE),
			map.get(AuditingFieldsKey.AUDIT_TIMESTAMP));
	}

	private AuditEventFactory getAuditDistributionDeployEventFactory(Map<AuditingFieldsKey, String> map) {
		return new AuditDistributionDeployEventFactory(
			CommonAuditData.newBuilder()
					.description(map.get(AuditingFieldsKey.AUDIT_DESC))
					.status(map.get(AuditingFieldsKey.AUDIT_STATUS))
					.requestId(map.get(AuditingFieldsKey.AUDIT_REQUEST_ID))
					.serviceInstanceId(map.get(AuditingFieldsKey.AUDIT_SERVICE_INSTANCE_ID))
					.build(),
			new ResourceCommonInfo(map.get(AuditingFieldsKey.AUDIT_RESOURCE_NAME),
					map.get(AuditingFieldsKey.AUDIT_RESOURCE_TYPE)),
			map.get(AuditingFieldsKey.AUDIT_RESOURCE_CURR_VERSION),
			map.get(AuditingFieldsKey.AUDIT_DISTRIBUTION_ID),
			map.get(AuditingFieldsKey.AUDIT_MODIFIER_UID),
			map.get(AuditingFieldsKey.AUDIT_TIMESTAMP));
	}

	private AuditEventFactory getAuditDistributionStatusEventFactory(Map<AuditingFieldsKey, String> map) {
		return new AuditDistributionStatusEventFactory(
			CommonAuditData.newBuilder()
					.description(map.get(AuditingFieldsKey.AUDIT_DESC))
					.status(map.get(AuditingFieldsKey.AUDIT_STATUS))
					.requestId(map.get(AuditingFieldsKey.AUDIT_REQUEST_ID))
					.serviceInstanceId(map.get(AuditingFieldsKey.AUDIT_SERVICE_INSTANCE_ID))
					.build(),
			new DistributionData(map.get(AuditingFieldsKey.AUDIT_DISTRIBUTION_CONSUMER_ID),
					map.get(AuditingFieldsKey.AUDIT_RESOURCE_URL)),
			map.get(AuditingFieldsKey.AUDIT_DISTRIBUTION_ID),
			map.get(AuditingFieldsKey.AUDIT_DISTRIBUTION_TOPIC_NAME),
			map.get(AuditingFieldsKey.AUDIT_DISTRIBUTION_STATUS_TIME),
			map.get(AuditingFieldsKey.AUDIT_TIMESTAMP));
	}

	private AuditEventFactory getAuditDistributionNotificationEventFactory(Map<AuditingFieldsKey, String> map) {
		return new AuditDistributionNotificationEventFactory(
			CommonAuditData.newBuilder()
					.description(map.get(AuditingFieldsKey.AUDIT_DESC))
					.status(map.get(AuditingFieldsKey.AUDIT_STATUS))
					.requestId(map.get(AuditingFieldsKey.AUDIT_REQUEST_ID))
					.serviceInstanceId(map.get(AuditingFieldsKey.AUDIT_SERVICE_INSTANCE_ID))
					.build(),
			new ResourceCommonInfo(map.get(AuditingFieldsKey.AUDIT_RESOURCE_NAME),
					map.get(AuditingFieldsKey.AUDIT_RESOURCE_TYPE)),
			ResourceVersionInfo.newBuilder()
					.state(map.get(AuditingFieldsKey.AUDIT_RESOURCE_CURR_STATE))
					.version(map.get(AuditingFieldsKey.AUDIT_RESOURCE_CURR_VERSION))
					.build(),
			map.get(AuditingFieldsKey.AUDIT_DISTRIBUTION_ID),
			map.get(AuditingFieldsKey.AUDIT_MODIFIER_UID),
			map.get(AuditingFieldsKey.AUDIT_DISTRIBUTION_TOPIC_NAME),
			new OperationalEnvAuditData(map.get(AuditingFieldsKey.AUDIT_DISTRIBUTION_ENVIRONMENT_ID),
					map.get(AuditingFieldsKey.AUDIT_DISTRIBUTION_VNF_WORKLOAD_CONTEXT),
					map.get(AuditingFieldsKey.AUDIT_DISTRIBUTION_TENANT)),
			map.get(AuditingFieldsKey.AUDIT_TIMESTAMP));
	}



	/**
	 * the method reads the content of the file intended for a given table, and
	 * sores them in cassandra
	 * 
	 * @param files
	 *            a map of files from which the recordes will be retrieved.
	 * @param table
	 *            the name of the table we want to look up in the files and sore
	 *            in Cassandra // * @param store the function to call when
	 *            storing recordes in cassndra
	 * @return true if the operation was successful
	 */
	private boolean handleImport(Map<Table, File> files, Table table) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(files.get(table)));
			String line = null;
			while ((line = br.readLine()) != null) {
				CassandraOperationStatus res = CassandraOperationStatus.GENERAL_ERROR;
				if (Table.ARTIFACT.equals(table)) {
					res = artifactCassandraDao.saveArtifact(jsonMapper.readValue(line, ESArtifactData.class));
				}
				else {
                    AuditingGenericEvent recordForCassandra = createAuditRecordForCassandra(line, table);
					if (recordForCassandra != null) {
                        res = auditCassandraDao.saveRecord(recordForCassandra);
                    }
				}
				if (!res.equals(CassandraOperationStatus.OK)) {
					log.error("save recored to cassndra {} failed with status {} aborting.",
							table.getTableDescription().getTableName(), res);
					return false;
				}
			}
			return true;
		} catch (IOException e) {
			log.error("failed to read file", e);
			return false;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					log.error("failed to close file reader", e);
				}
			}
		}
	}

    AuditingGenericEvent createAuditRecordForCassandra(String json, Table table) throws IOException{
        return createAuditEvent(parseToMap(json), table);
    }

	private Map<AuditingFieldsKey, String> parseToMap(String json) throws IOException {
		return jsonMapper.readValue(json, new TypeReference<Map<AuditingFieldsKey, String>>(){});
	}

	/**
	 * the method checks if the given table is empty
	 * 
	 * @param table
	 *            the name of the table we want to check
	 * @return true if the table is empty
	 */
	private Either<Boolean, CassandraOperationStatus> checkIfTableIsEmpty(Table table) {
		if (Table.ARTIFACT.equals(table)) {
			return artifactCassandraDao.isTableEmpty(table.getTableDescription().getTableName());
		} else {
			return auditCassandraDao.isTableEmpty(table.getTableDescription().getTableName());
		}
	}

	private boolean filesEmpty(Map<Table, File> files) {
		for (Table table : files.keySet()) {
			File file = files.get(table);
			if (file.length() != 0) {
				log.info("file:{} is not empty skipping export", table.getTableDescription().getTableName());
				return false;
			}
		}
		return true;
	}

	/**
	 * the method reads the records from es index of audit's into a file as
	 * json's.
	 * 
	 * @param value
	 *            the name of the index we want
	 * @param printerWritersMap
	 *            a map of the writers we use to write to a file.
	 * @return true in case the export was successful.
	 */
	private boolean exportAudit(String value, Map<Table, PrintWriter> printerWritersMap) {
		log.info("stratng to export audit data from es index{} to file.", value);
		QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
		SearchResponse scrollResp = elasticSearchClient.getClient().prepareSearch(value).setScroll(new TimeValue(60000))
				.setQuery(queryBuilder).setSize(100).execute().actionGet();
		while (true) {
			for (SearchHit hit : scrollResp.getHits().getHits()) {
				PrintWriter out = printerWritersMap.get(TypeToTableMapping.getTableByType(hit.getType()));
				out.println(hit.getSourceAsString());
			}
			scrollResp = elasticSearchClient.getClient().prepareSearchScroll(scrollResp.getScrollId())
					.setScroll(new TimeValue(60000)).execute().actionGet();
			if (scrollResp.getHits().getHits().length == 0) {
				break;

			}
		}

		log.info("export audit data from es to file. finished succsesfully");
		return true;
	}

	/**
	 * the method reads the records from es index of resources into a file as
	 * json's.
	 *
	 * @param index
	 *            the name of the index we want to read
	 * @param printerWritersMap
	 *            a map of the writers we use to write to a file.
	 * @return true in case the export was successful.
	 */
	private boolean exportArtifacts(String index, Map<Table, PrintWriter> printerWritersMap) {
		log.info("stratng to export artifact data from es to file.");
		PrintWriter out = printerWritersMap.get(Table.ARTIFACT);
		QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
		SearchResponse scrollResp = elasticSearchClient.getClient().prepareSearch(index).setScroll(new TimeValue(60000))
				.setQuery(queryBuilder).setSize(100).execute().actionGet();
		while (true) {
			for (SearchHit hit : scrollResp.getHits().getHits()) {
				;
				out.println(hit.getSourceAsString());
			}
			scrollResp = elasticSearchClient.getClient().prepareSearchScroll(scrollResp.getScrollId())
					.setScroll(new TimeValue(60000)).execute().actionGet();
			if (scrollResp.getHits().getHits().length == 0) {
				break;

			}
		}

		log.info("export artifact data from es to file. finished succsesfully");
		return true;
	}

	/**
	 * the method retrieves all the indexes from elasticsearch
	 * 
	 * @return a map of indexes and there metadata
	 */
	private ImmutableOpenMap<String, IndexMetaData> getIndexData() {
		return elasticSearchClient.getClient().admin().cluster().prepareState().get().getState().getMetaData()
				.getIndices();
	}

	/**
	 * the method creates all the files and dir which holds them. in case the
	 * files exist they will not be created again.
	 * 
	 * @param appConfigDir
	 *            the base path under which the output dir will be created and
	 *            the export result files the created filesa are named according
	 *            to the name of the table into which it will be imported.
	 * @param exportToEs
	 *            if true all the export files will be recreated
	 * @returnthe returns a map of tables and the files representing them them
	 */
	private Map<Table, File> createOutPutFiles(String appConfigDir, boolean exportToEs) {
		Map<Table, File> result = new EnumMap<Table, File>(Table.class);
		File outputDir = new File(appConfigDir + "/output/");
		if (!createOutPutFolder(outputDir)) {
			return null;
		}
		for (Table table : Table.values()) {
			File file = new File(outputDir + "/" + table.getTableDescription().getTableName());
			if (exportToEs) {
				try {
					if (file.exists()) {
						Files.delete(file.toPath());
					}
				} catch (IOException e) {
					log.error("failed to delete output file {}", file.getAbsolutePath(), e);
					return null;
				}
				file = new File(outputDir + "/" + table.getTableDescription().getTableName());
			}
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					log.error("failed to create output file {}", file.getAbsolutePath(), e);
					return null;
				}
			}
			result.put(table, file);

		}
		return result;
	}

	/**
	 * the method create the writers to each file
	 * 
	 * @param files
	 *            a map of the files according to table
	 * @return returns a map of writers according to table.
	 */
	private Map<Table, PrintWriter> createWriters(Map<Table, File> files) {
		Map<Table, PrintWriter> printerWritersMap = new EnumMap<>(Table.class);
      
			for (Table table : files.keySet()) {
				log.info("creating writer for {}", table);
				File file = files.get(table);
                try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)) )){
				printerWritersMap.put(table, out);
				log.info("creating writer for {} was successful", table);
            } catch (IOException e) {
            	log.error("create writer to file failed",e);
            	return null;
			} 
        } 
		return printerWritersMap;
	}

	/**
	 * the method creates the output dir in case it does not exist
	 * 
	 * @param outputDir
	 *            the path under wich the directory will be created.
	 * @return true in case the create was succsesful or the dir already exists
	 */
	private boolean createOutPutFolder(File outputDir) {
		if (!outputDir.exists()) {
			log.info("creating output dir {}", outputDir.getAbsolutePath());
			try {
				Files.createDirectories(outputDir.toPath());
			} catch (IOException e) {
				log.error("failed to create output dir {}", outputDir.getAbsolutePath(), e);
				return false;
			}
		}
		return true;
	}

	public enum TypeToTableMapping {
		USER_ADMIN_EVENT_TYPE(AuditingTypesConstants.USER_ADMIN_EVENT_TYPE,
				Table.USER_ADMIN_EVENT), USER_ACCESS_EVENT_TYPE(AuditingTypesConstants.USER_ACCESS_EVENT_TYPE,
						Table.USER_ACCESS_EVENT), RESOURCE_ADMIN_EVENT_TYPE(
								AuditingTypesConstants.RESOURCE_ADMIN_EVENT_TYPE,
								Table.RESOURCE_ADMIN_EVENT), DISTRIBUTION_DOWNLOAD_EVENT_TYPE(
										AuditingTypesConstants.DISTRIBUTION_DOWNLOAD_EVENT_TYPE,
										Table.DISTRIBUTION_DOWNLOAD_EVENT), DISTRIBUTION_ENGINE_EVENT_TYPE(
												AuditingTypesConstants.DISTRIBUTION_ENGINE_EVENT_TYPE,
												Table.DISTRIBUTION_ENGINE_EVENT), DISTRIBUTION_NOTIFICATION_EVENT_TYPE(
														AuditingTypesConstants.DISTRIBUTION_NOTIFICATION_EVENT_TYPE,
														Table.DISTRIBUTION_NOTIFICATION_EVENT), DISTRIBUTION_STATUS_EVENT_TYPE(
																AuditingTypesConstants.DISTRIBUTION_STATUS_EVENT_TYPE,
																Table.DISTRIBUTION_STATUS_EVENT), DISTRIBUTION_DEPLOY_EVENT_TYPE(
																		AuditingTypesConstants.DISTRIBUTION_DEPLOY_EVENT_TYPE,
																		Table.DISTRIBUTION_DEPLOY_EVENT), DISTRIBUTION_GET_UEB_CLUSTER_EVENT_TYPE(
																				AuditingTypesConstants.DISTRIBUTION_GET_UEB_CLUSTER_EVENT_TYPE,
																				Table.DISTRIBUTION_GET_UEB_CLUSTER_EVENT), AUTH_EVENT_TYPE(
																						AuditingTypesConstants.AUTH_EVENT_TYPE,
																						Table.AUTH_EVENT), CONSUMER_EVENT_TYPE(
																								AuditingTypesConstants.CONSUMER_EVENT_TYPE,
																								Table.CONSUMER_EVENT), CATEGORY_EVENT_TYPE(
																										AuditingTypesConstants.CATEGORY_EVENT_TYPE,
																										Table.CATEGORY_EVENT), GET_USERS_LIST_EVENT_TYPE(
																												AuditingTypesConstants.GET_USERS_LIST_EVENT_TYPE,
																												Table.GET_USERS_LIST_EVENT), GET_CATEGORY_HIERARCHY_EVENT_TYPE(
																														AuditingTypesConstants.GET_CATEGORY_HIERARCHY_EVENT_TYPE,
																														Table.GET_CATEGORY_HIERARCHY_EVENT);

		String typeName;
		Table table;

		TypeToTableMapping(String typeName, Table table) {
			this.typeName = typeName;
			this.table = table;
		}

		public String getTypeName() {
			return typeName;
		}

		public Table getTable() {
			return table;
		}

		public static Table getTableByType(String type) {
			for (TypeToTableMapping mapping : TypeToTableMapping.values()) {
				if (mapping.getTypeName().equalsIgnoreCase(type)) {
					return mapping.getTable();
				}
			}
			return null;
		}
	}

}
