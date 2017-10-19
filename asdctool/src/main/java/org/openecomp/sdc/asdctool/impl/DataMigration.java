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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang.SystemUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.cassandra.schema.Table;
import org.openecomp.sdc.be.dao.es.ElasticSearchClient;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGetUebClusterEvent;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;
import org.openecomp.sdc.be.resources.data.auditing.AuthEvent;
import org.openecomp.sdc.be.resources.data.auditing.CategoryEvent;
import org.openecomp.sdc.be.resources.data.auditing.ConsumerEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionDeployEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionDownloadEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionEngineEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionNotificationEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionStatusEvent;
import org.openecomp.sdc.be.resources.data.auditing.GetCategoryHierarchyEvent;
import org.openecomp.sdc.be.resources.data.auditing.GetUsersListEvent;
import org.openecomp.sdc.be.resources.data.auditing.ResourceAdminEvent;
import org.openecomp.sdc.be.resources.data.auditing.UserAccessEvent;
import org.openecomp.sdc.be.resources.data.auditing.UserAdminEvent;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import fj.data.Either;

/**
 * Created by mlando on 5/16/2016.
 */
public class DataMigration {

	private Gson gson = new Gson();

	private ObjectMapper jsonMapper = new ObjectMapper();

	private static Logger log = LoggerFactory.getLogger(DataMigration.class.getName());

	protected ElasticSearchClient elasticSearchClient;
	@Autowired
	protected AuditCassandraDao auditCassandraDao;
	@Autowired
	protected ArtifactCassandraDao artifactCassandraDao;
	private static final String dateFormatPattern = "yyyy-MM-dd HH:mm:ss.SSS z";
	private static SimpleDateFormat simpleDateFormat;

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
		initFormater();
		if (!initEsClient())
			return false;
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
					if ("resources".equalsIgnoreCase(key.value)) {
						if (!exportArtifacts(key.value, printerWritersMap)) {
							return false;
						}
					} else if (key.value.startsWith("auditingevents")) {
						if (!exportAudit(key.value, printerWritersMap)) {
							return false;
						}
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
		if (importToCassandra && !importToCassndra(files)) {
			return false;
		}

		return true;
	}

	private void initFormater() {
		simpleDateFormat = new SimpleDateFormat(dateFormatPattern);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
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
			e.printStackTrace();
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
	 * the method retrieves the fields from the given map and praprs them for
	 * storage as an audit according to the table name
	 * 
	 * @param map
	 *            the map from which we will retrive the fields enum values
	 * @param table
	 *            the table we are going to store the record in.
	 * @return a enummap representing the audit record that is going to be
	 *         created.
	 */
	private EnumMap<AuditingFieldsKeysEnum, Object> createAuditMap(Map<String, String> map, Table table) {
		EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<>(AuditingFieldsKeysEnum.class);
		switch (table) {
		case USER_ADMIN_EVENT:
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP, map.get("TIMESTAMP"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID, map.get("REQUEST_ID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, map.get("SERVICE_INSTANCE_ID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, map.get("ACTION"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, map.get("DESC"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, map.get("STATUS"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_USER_AFTER, map.get("USER_AFTER"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_USER_BEFORE, map.get("USER_BEFORE"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, map.get("MODIFIER"));
			break;
		case USER_ACCESS_EVENT:
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP, map.get("TIMESTAMP"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID, map.get("REQUEST_ID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, map.get("SERVICE_INSTANCE_ID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, map.get("ACTION"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, map.get("DESC"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, map.get("STATUS"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_USER_UID, map.get("USER"));
			break;
		case RESOURCE_ADMIN_EVENT:
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP, map.get("TIMESTAMP"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID, map.get("REQUEST_ID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, map.get("SERVICE_INSTANCE_ID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_INVARIANT_UUID, map.get("INVARIANT_UUID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, map.get("ACTION"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, map.get("DESC"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, map.get("STATUS"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION, map.get("CURR_VERSION"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_STATE, map.get("CURR_STATE"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ID, map.get("DID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, map.get("MODIFIER"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_PREV_VERSION, map.get("PREV_VERSION"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_PREV_STATE, map.get("PREV_STATE"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, map.get("RESOURCE_NAME"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE, map.get("RESOURCE_TYPE"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_DPREV_STATUS, map.get("DPREV_STATUS"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_DCURR_STATUS, map.get("DCURR_STATUS"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TOSCA_NODE_TYPE, map.get("TOSCA_NODE_TYPE"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_COMMENT, map.get("COMMENT"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ARTIFACT_DATA, map.get("ARTIFACT_DATA"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_PREV_ARTIFACT_UUID, map.get("PREV_ARTIFACT_UUID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_CURR_ARTIFACT_UUID, map.get("CURR_ARTIFACT_UUID"));
			break;
		case DISTRIBUTION_DOWNLOAD_EVENT:
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP, map.get("TIMESTAMP"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID, map.get("REQUEST_ID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, map.get("SERVICE_INSTANCE_ID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, map.get("ACTION"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, map.get("DESC"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, map.get("STATUS"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL, map.get("RESOURCE_URL"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID, map.get("CONSUMER_ID"));
			break;
		case DISTRIBUTION_ENGINE_EVENT:
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP, map.get("TIMESTAMP"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID, map.get("REQUEST_ID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, map.get("SERVICE_INSTANCE_ID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, map.get("ACTION"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, map.get("DESC"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, map.get("STATUS"));
			if (map.get("TOPIC_NAME") != null) {
				if (map.get("TOPIC_NAME").contains("-STATUS-")) {
					auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_STATUS_TOPIC_NAME,
							map.get("TOPIC_NAME"));
				} else {
					auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_NOTIFICATION_TOPIC_NAME,
							map.get("TOPIC_NAME"));
				}
			} else {
				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_STATUS_TOPIC_NAME,
						map.get("DSTATUS_TOPIC"));
				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_NOTIFICATION_TOPIC_NAME,
						map.get("DNOTIF_TOPIC"));
			}
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_TOPIC_NAME, map.get("TOPIC_NAME"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ROLE, map.get("ROLE"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_API_KEY, map.get("API_KEY"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ENVRIONMENT_NAME, map.get("D_ENV"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID, map.get("CONSUMER_ID"));
			break;
		case DISTRIBUTION_NOTIFICATION_EVENT:
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP, map.get("TIMESTAMP"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID, map.get("REQUEST_ID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, map.get("SERVICE_INSTANCE_ID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, map.get("ACTION"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, map.get("DESC"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, map.get("STATUS"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_STATE, map.get("CURR_STATE"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION, map.get("CURR_VERSION"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ID, map.get("DID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, map.get("MODIFIER"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, map.get("RESOURCE_NAME"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE, map.get("RESOURCE_TYPE"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_TOPIC_NAME, map.get("TOPIC_NAME"));
			break;
		case DISTRIBUTION_STATUS_EVENT:
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP, map.get("TIMESTAMP"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID, map.get("REQUEST_ID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, map.get("SERVICE_INSTANCE_ID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, map.get("ACTION"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, map.get("DESC"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, map.get("STATUS"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL, map.get("RESOURCE_URL"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ID, map.get("DID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_TOPIC_NAME, map.get("TOPIC_NAME"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID, map.get("CONSUMER_ID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_STATUS_TIME, map.get("STATUS_TIME"));
			break;
		case DISTRIBUTION_DEPLOY_EVENT:
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP, map.get("TIMESTAMP"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID, map.get("REQUEST_ID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, map.get("SERVICE_INSTANCE_ID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, map.get("ACTION"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, map.get("DESC"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, map.get("STATUS"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ID, map.get("DID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, map.get("RESOURCE_NAME"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE, map.get("RESOURCE_TYPE"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, map.get("MODIFIER"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION, map.get("CURR_VERSION"));
			break;
		case DISTRIBUTION_GET_UEB_CLUSTER_EVENT:
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP, map.get("TIMESTAMP"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID, map.get("REQUEST_ID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, map.get("SERVICE_INSTANCE_ID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, map.get("ACTION"));
			if (map.get("STATUS_DESC") != null) {
				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, map.get("STATUS_DESC"));
			} else {
				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, map.get("DESC"));
			}
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, map.get("STATUS"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID, map.get("CONSUMER_ID"));
			break;
		case AUTH_EVENT:
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP, map.get("TIMESTAMP"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, map.get("ACTION"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, map.get("DESC"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID, map.get("REQUEST_ID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, map.get("STATUS"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_AUTH_USER, map.get("USER"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_AUTH_URL, map.get("URL"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_AUTH_STATUS, map.get("AUTH_STATUS"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_AUTH_REALM, map.get("REALM"));
			break;
		case CONSUMER_EVENT:
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP, map.get("TIMESTAMP"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, map.get("ACTION"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, map.get("DESC"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, map.get("STATUS"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, map.get("MODIFIER"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID, map.get("REQUEST_ID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ECOMP_USER, map.get("ECOMP_USER"));
			break;
		case CATEGORY_EVENT:
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP, map.get("TIMESTAMP"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, map.get("ACTION"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, map.get("DESC"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, map.get("STATUS"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, map.get("MODIFIER"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID, map.get("REQUEST_ID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, map.get("SERVICE_INSTANCE_ID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_CATEGORY_NAME, map.get("CATEGORY_NAME"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_SUB_CATEGORY_NAME, map.get("SUB_CATEGORY_NAME"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_GROUPING_NAME, map.get("GROUPING_NAME"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE, map.get("RESOURCE_TYPE"));
			break;
		case GET_USERS_LIST_EVENT:
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP, map.get("TIMESTAMP"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, map.get("ACTION"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, map.get("DESC"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, map.get("STATUS"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, map.get("MODIFIER"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID, map.get("REQUEST_ID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DETAILS, map.get("DETAILS"));
			break;
		case GET_CATEGORY_HIERARCHY_EVENT:
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP, map.get("TIMESTAMP"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, map.get("ACTION"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, map.get("DESC"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, map.get("STATUS"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, map.get("MODIFIER"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID, map.get("REQUEST_ID"));
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DETAILS, map.get("DETAILS"));
			break;
		default:
			auditingFields = null;
			break;
		}
		return auditingFields;
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
				CassandraOperationStatus res = null;
				if (Table.ARTIFACT.equals(table)) {
					res = artifactCassandraDao.saveArtifact(jsonMapper.readValue(line, ESArtifactData.class));
				} else {
					Type type = new TypeToken<Map<String, String>>() {
					}.getType();
					Map<String, String> map = gson.fromJson(line, type);
					EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = createAuditMap(map, table);
					AuditingGenericEvent recordForCassandra = null;
					try {
						recordForCassandra = createAuditRecord(auditingFields);
					} catch (ParseException e) {
						log.error("filed to parse time stemp in recored {}", auditingFields);
						return false;
					}

					res = auditCassandraDao.saveRecord(recordForCassandra);
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
		try {
			for (Table table : files.keySet()) {
				log.info("creating writer for {}", table);
				File file = files.get(table);
				FileWriter fw = new FileWriter(file, true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw);
				printerWritersMap.put(table, out);
				log.info("creating writer for {} was successful", table);
			}
		} catch (IOException e) {
			log.error("create writer to file failed", e);
			return null;
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

	public static AuditingGenericEvent createAuditRecord(EnumMap<AuditingFieldsKeysEnum, Object> auditingFields)
			throws ParseException {
		AuditingActionEnum actionEnum = AuditingActionEnum
				.getActionByName((String) auditingFields.get(AuditingFieldsKeysEnum.AUDIT_ACTION));
		String tableName = actionEnum.getAuditingEsType();
		AuditingGenericEvent event = null;
		Date date = null;
		switch (tableName) {
		case AuditingTypesConstants.USER_ADMIN_EVENT_TYPE:
			UserAdminEvent userAdminEvent = new UserAdminEvent(auditingFields);
			date = simpleDateFormat.parse((String) auditingFields.get(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP));
			userAdminEvent.setTimestamp1(date);
			event = userAdminEvent;
			break;
		case AuditingTypesConstants.AUTH_EVENT_TYPE:
			AuthEvent authEvent = new AuthEvent(auditingFields);
			date = simpleDateFormat.parse((String) auditingFields.get(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP));
			authEvent.setTimestamp1(date);
			event = authEvent;
			break;
		case AuditingTypesConstants.CATEGORY_EVENT_TYPE:
			CategoryEvent categoryEvent = new CategoryEvent(auditingFields);
			date = simpleDateFormat.parse((String) auditingFields.get(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP));
			categoryEvent.setTimestamp1(date);
			event = categoryEvent;
			break;
		case AuditingTypesConstants.RESOURCE_ADMIN_EVENT_TYPE:
			ResourceAdminEvent resourceAdminEvent = new ResourceAdminEvent(auditingFields);
			date = simpleDateFormat.parse((String) auditingFields.get(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP));
			resourceAdminEvent.setTimestamp1(date);
			event = resourceAdminEvent;
			break;
		case AuditingTypesConstants.USER_ACCESS_EVENT_TYPE:
			event = new UserAccessEvent(auditingFields);
			UserAccessEvent userAccessEvent = new UserAccessEvent(auditingFields);
			date = simpleDateFormat.parse((String) auditingFields.get(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP));
			userAccessEvent.setTimestamp1(date);
			event = userAccessEvent;
			break;
		case AuditingTypesConstants.DISTRIBUTION_STATUS_EVENT_TYPE:
			DistributionStatusEvent distributionStatusEvent = new DistributionStatusEvent(auditingFields);
			date = simpleDateFormat.parse((String) auditingFields.get(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP));
			distributionStatusEvent.setTimestamp1(date);
			event = distributionStatusEvent;
			break;
		case AuditingTypesConstants.DISTRIBUTION_DOWNLOAD_EVENT_TYPE:
			DistributionDownloadEvent distributionDownloadEvent = new DistributionDownloadEvent(auditingFields);
			date = simpleDateFormat.parse((String) auditingFields.get(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP));
			distributionDownloadEvent.setTimestamp1(date);
			event = distributionDownloadEvent;
			break;
		case AuditingTypesConstants.DISTRIBUTION_ENGINE_EVENT_TYPE:
			DistributionEngineEvent distributionEngineEvent = new DistributionEngineEvent(auditingFields);
			date = simpleDateFormat.parse((String) auditingFields.get(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP));
			distributionEngineEvent.setTimestamp1(date);
			event = distributionEngineEvent;
			break;
		case AuditingTypesConstants.DISTRIBUTION_NOTIFICATION_EVENT_TYPE:
			DistributionNotificationEvent distributionNotificationEvent = new DistributionNotificationEvent(
					auditingFields);
			date = simpleDateFormat.parse((String) auditingFields.get(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP));
			distributionNotificationEvent.setTimestamp1(date);
			event = distributionNotificationEvent;
			break;
		case AuditingTypesConstants.DISTRIBUTION_DEPLOY_EVENT_TYPE:
			DistributionDeployEvent distributionDeployEvent = new DistributionDeployEvent(auditingFields);
			date = simpleDateFormat.parse((String) auditingFields.get(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP));
			distributionDeployEvent.setTimestamp1(date);
			event = distributionDeployEvent;
			break;
		case AuditingTypesConstants.DISTRIBUTION_GET_UEB_CLUSTER_EVENT_TYPE:
			AuditingGetUebClusterEvent auditingGetUebClusterEvent = new AuditingGetUebClusterEvent(auditingFields);
			date = simpleDateFormat.parse((String) auditingFields.get(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP));
			auditingGetUebClusterEvent.setTimestamp1(date);
			event = auditingGetUebClusterEvent;
			break;
		case AuditingTypesConstants.CONSUMER_EVENT_TYPE:
			ConsumerEvent consumerEvent = new ConsumerEvent(auditingFields);
			date = simpleDateFormat.parse((String) auditingFields.get(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP));
			consumerEvent.setTimestamp1(date);
			event = consumerEvent;
			break;
		case AuditingTypesConstants.GET_USERS_LIST_EVENT_TYPE:
			GetUsersListEvent getUsersListEvent = new GetUsersListEvent(auditingFields);
			date = simpleDateFormat.parse((String) auditingFields.get(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP));
			getUsersListEvent.setTimestamp1(date);
			event = getUsersListEvent;
			break;
		case AuditingTypesConstants.GET_CATEGORY_HIERARCHY_EVENT_TYPE:
			GetCategoryHierarchyEvent getCategoryHierarchyEvent = new GetCategoryHierarchyEvent(auditingFields);
			date = simpleDateFormat.parse((String) auditingFields.get(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP));
			getCategoryHierarchyEvent.setTimestamp1(date);
			event = getCategoryHierarchyEvent;
			break;

		}
		return event;
	}

}
