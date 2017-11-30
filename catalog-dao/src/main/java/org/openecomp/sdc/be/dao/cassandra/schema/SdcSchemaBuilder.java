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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.cassandra.schema.tables.OldExternalApiEventTableDesc;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.schemabuilder.Alter;
import com.datastax.driver.core.schemabuilder.Create;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import com.datastax.driver.core.schemabuilder.SchemaStatement;

public class SdcSchemaBuilder {

	/**
	 * creat key space statment for SimpleStrategy
	 */
	final static String CREATE_KEYSPACE_SIMPLE_STRATEGY = "CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class':'SimpleStrategy', %s};";
	/**
	 * creat key space statment for NetworkTopologyStrategy
	 */
	final static String CREATE_KEYSPACE_NETWORK_TOPOLOGY_STRATEGY = "CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class':'NetworkTopologyStrategy', %s};";

	private static Logger log = LoggerFactory.getLogger(SdcSchemaBuilder.class.getName());

	//TODO remove after 1707_OS migration
	private static void handle1707OSMigration(Map<String, Map<String, List<String>>> cassndraMetadata, Map<String, List<ITableDescription>> schemeData){
		if(cassndraMetadata.containsKey("attaudit")){
			List<ITableDescription> list = new ArrayList<>();
			list.add(new OldExternalApiEventTableDesc());
			schemeData.put("attaudit", list);
		}
		
	}
	/**
	 * the method creates all keyspaces, tables and indexes in case they do not
	 * already exist. the method can be run multiple times. the method uses the
	 * internal enums and external configuration for its operation	 * 
	 * @return true if the create operation was successful
	 */
	public static boolean createSchema() {
		Cluster cluster = null;
		Session session = null;
		try {
			log.info("creating Schema for Cassandra.");
			cluster = SdcSchemaUtils.createCluster();
			if (cluster == null) {
				return false;
			}
			session = cluster.connect();
			List<KeyspaceMetadata> keyspacesMetadateFromCassandra = cluster.getMetadata().getKeyspaces();
			if (keyspacesMetadateFromCassandra == null) {
				log.debug("filed to retrive a list of keyspaces from cassndra");
				return false;
			}
			log.debug("retrived Cassndra metadata.");
			Map<String, Map<String, List<String>>> cassndraMetadata = parseKeyspaceMetadata(keyspacesMetadateFromCassandra);
			Map<String, Map<String, List<String>>> metadataTablesStructure = getMetadataTablesStructure(keyspacesMetadateFromCassandra);
			Map<String, List<ITableDescription>> schemeData = getSchemeData();
			//TODO remove after 1707_OS migration
			handle1707OSMigration(cassndraMetadata, schemeData);
			log.info("creating Keyspaces.");
			for (String keyspace : schemeData.keySet()) {
				if (!createKeyspace(keyspace, cassndraMetadata, session)) {
					return false;
				}
				Map<String, List<String>> keyspaceMetadate = cassndraMetadata.get(keyspace);
				createTables(schemeData.get(keyspace), keyspaceMetadate, session,metadataTablesStructure.get(keyspace));
			}
			return true;
		} catch (Exception e) {
			log.info("createSchema failed with exception.", e);
		} finally {
			if (session != null) {
				session.close();
			}
			if (cluster != null) {
				cluster.close();
			}

		}

		return false;
	}

	public static boolean deleteSchema() {
		Cluster cluster = null;
		Session session = null;
		try {
			log.info("delete Data from Cassandra.");
			cluster = SdcSchemaUtils.createCluster();
			if (cluster == null) {
				return false;
			}
			session = cluster.connect();
			List<KeyspaceMetadata> keyspacesMetadateFromCassandra = cluster.getMetadata().getKeyspaces();
			if (keyspacesMetadateFromCassandra == null) {
				log.debug("filed to retrive a list of keyspaces from cassndra");
				return false;
			}
			log.debug("retrived Cassndra metadata.");
			Map<String, Map<String, List<String>>> cassndraMetadata = parseKeyspaceMetadata(keyspacesMetadateFromCassandra);
			cassndraMetadata.forEach((k, v) -> {
				if (AuditingTypesConstants.TITAN_KEYSPACE.equals(k)) {
					// session.execute("")
				} else if (AuditingTypesConstants.ARTIFACT_KEYSPACE.equals(k)) {

				} else if (AuditingTypesConstants.AUDIT_KEYSPACE.equals(k)) {

				}
			});

			System.out.println(cassndraMetadata);
			return true;
		} catch (Exception e) {
			log.info("deleteSchema failed with exception.", e);
		} finally {
			if (session != null) {
				session.close();
			}
			if (cluster != null) {
				cluster.close();
			}

		}

		return false;
	}

	

	/**
	 * the method prcess the metadata retrieved from the cassandra for the
	 * creation of a map conting the names of keyspaces tabls and indexes
	 * already defined in the cassandra keyspacename -> tablename -> list of
	 * indexes info
	 * 
	 * @param keyspacesMetadata
	 *            cassndra mmetadata
	 * @return a map of maps of lists holding parsed info
	 */
	private static Map<String, Map<String, List<String>>> parseKeyspaceMetadata(List<KeyspaceMetadata> keyspacesMetadata) {
		Map<String, Map<String, List<String>>> cassndraMetadata = keyspacesMetadata.stream()
				.collect(Collectors.toMap(keyspaceMetadata -> keyspaceMetadata.getName(),
						keyspaceMetadata -> keyspaceMetadata.getTables().stream()
								.collect(Collectors.toMap(tableMetadata -> tableMetadata.getName(),
										tableMetadata -> tableMetadata.getIndexes().stream()
												.map(indexMetadata -> indexMetadata.getName())
												.collect(Collectors.toList())))));
		return cassndraMetadata;
	}
	
	private static Map<String, Map<String, List<String>>> getMetadataTablesStructure(
			List<KeyspaceMetadata> keyspacesMetadata) {
		return keyspacesMetadata.stream().collect(
				Collectors.toMap(keyspaceMetadata -> keyspaceMetadata.getName(),
								 keyspaceMetadata -> keyspaceMetadata.getTables().stream().collect(
										 Collectors.toMap(tableMetadata -> tableMetadata.getName(), 
												 		  tableMetadata -> tableMetadata.getColumns().stream().map(
												 				  columnMetadata -> columnMetadata.getName().toLowerCase()).collect(
												 						  Collectors.toList())))));		
	}

	/**
	 * the method builds an index name according to a defined logic
	 * <table>
	 * _<column>_idx
	 * 
	 * @param table: table name
	 * @param column: column name
	 * @return string name of the index
	 */
	private static String createIndexName(String table, String column) {
		return new StringBuilder().append(table).append("_").append(column).append("_idx").toString();
	}

	/**
	 * the method creats all the tables and indexes thet do not already exist
	 *
	 * @param iTableDescriptions: a list of table description we want to create
	 * @param keyspaceMetadate: the current tables that exist in the cassandra under this keyspace
	 * @param session: the session object used for the execution of the query.
	 * @param existingTablesMetadata 
	 *			the current tables columns that exist in the cassandra under this
	 *            keyspace
	 */
	private static void createTables(List<ITableDescription> iTableDescriptions, Map<String, List<String>> keyspaceMetadate, Session session, 
			Map<String, List<String>> existingTablesMetadata) {
		for (ITableDescription tableDescription : iTableDescriptions) {
			String tableName = tableDescription.getTableName().toLowerCase();
			Map<String, ImmutablePair<DataType, Boolean>> columnDescription = tableDescription.getColumnDescription();
			log.info("creating tables:{}.", tableName);
			if (keyspaceMetadate == null || !keyspaceMetadate.keySet().contains(tableName)) {
				Create create = SchemaBuilder.createTable(tableDescription.getKeyspace(),tableDescription.getTableName());
				for (ImmutablePair<String, DataType> key : tableDescription.primaryKeys()) {
					create.addPartitionKey(key.getLeft(), key.getRight());
				}
				if (tableDescription.clusteringKeys() != null) {
					for (ImmutablePair<String, DataType> key : tableDescription.clusteringKeys()) {
						create.addClusteringColumn(key.getLeft(), key.getRight());
					}
				}

				for (String columnName : columnDescription.keySet()) {
					create.addColumn(columnName, columnDescription.get(columnName).getLeft());
				}
				log.trace("exacuting :{}", create.toString());
				session.execute(create);
				log.info("table:{} created succsesfully.", tableName);
			} else {
				log.info("table:{} already exists skiping.", tableName);
				alterTable(session, existingTablesMetadata, tableDescription, tableName, columnDescription);
			}
			log.info("keyspacemetdata{}",keyspaceMetadate);
			List<String> indexNames = (keyspaceMetadate != null && keyspaceMetadate.get(tableName) != null ? keyspaceMetadate.get(tableName) : new ArrayList<>());
			log.info("table:{} creating indexes.", tableName);
			for (String columnName : columnDescription.keySet()) {
				String indexName = createIndexName(tableName, columnName).toLowerCase();
				if (columnDescription.get(columnName).getRight()) {
					if (!indexNames.contains(indexName)) {
						SchemaStatement creatIndex = SchemaBuilder.createIndex(indexName)
								.onTable(tableDescription.getKeyspace(), tableName).andColumn(columnName);
						log.info("executing :{}", creatIndex.toString());
						session.execute(creatIndex);
						log.info("index:{} created succsesfully.", indexName);
					} else {
						log.info("index:{} already exists skiping.", indexName);
					}
				}
			}

		}
	}

	/**
	 * check if there are new columns that were added to definition but don't exist in DB
	 * @param session
	 * @param existingTablesMetadata
	 * @param tableDescription
	 * @param tableName
	 * @param columnDescription
	 */
	private static void alterTable(Session session, Map<String, List<String>> existingTablesMetadata,
			ITableDescription tableDescription, String tableName,
			Map<String, ImmutablePair<DataType, Boolean>> columnDescription) {
		List<String> definedTableColumns = existingTablesMetadata.get(tableName);
		//add column to casandra if was added to table definition
		for (Map.Entry<String, ImmutablePair<DataType, Boolean>> column : columnDescription.entrySet()) {
			String columnName = column.getKey();
			if (!definedTableColumns.contains(columnName.toLowerCase())){
				log.info("Adding new column {} to the table {}", columnName,tableName);
				Alter alter = SchemaBuilder.alterTable(tableDescription.getKeyspace(),tableDescription.getTableName());
				SchemaStatement addColumn = alter.addColumn(columnName).type(column.getValue().getLeft());
				log.trace("exacuting :{}", addColumn.toString());
				session.execute(addColumn);						
			}
		}
	}

	/**
	 * the method create the keyspace in case it does not already exists the
	 * method uses configurtion to select the needed replication strategy
	 * 
	 * @param keyspace: name of the keyspace we want to create
	 * @param cassndraMetadata: cassndra metadata
	 * @param session: the session object used for the execution of the query.
	 * @return true in case the operation was successful
	 */
	private static boolean createKeyspace(String keyspace, Map<String, Map<String, List<String>>> cassndraMetadata, Session session) {
		List<Configuration.CassandrConfig.KeyspaceConfig> keyspaceConfigList = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().getKeySpaces();
		log.info("creating keyspace:{}.", keyspace);
		if (!cassndraMetadata.keySet().contains(keyspace)) {
			Optional<Configuration.CassandrConfig.KeyspaceConfig> keyspaceConfig = keyspaceConfigList.stream().filter(keyspaceInfo -> keyspace.equalsIgnoreCase(keyspaceInfo.getName())).findFirst();
			if (keyspaceConfig.isPresent()) {
				Configuration.CassandrConfig.KeyspaceConfig keyspaceInfo = keyspaceConfig.get();
				String createKeyspaceQuery = createKeyspaceQuereyString(keyspace, keyspaceInfo);
				if (createKeyspaceQuery != null) {
					log.trace("exacuting: {}", createKeyspaceQuery);
					session.execute(createKeyspaceQuery);
					log.info("keyspace:{} created.", keyspace);
					return true;
				} else {
					return false;
				}
			} else {
				log.info(
						"keyspace:{} not present in configuration, no info on replications is available. operation failed.",
						keyspace);
				return false;
			}
		} else {
			log.info("keyspace:{} already exists skipping.", keyspace);
			return true;
		}
	}

	/**
	 * the method retries the schem info from the enums describing the tables
	 * 
	 * @return a map of keyspaces to there table info
	 */
	private static Map<String, List<ITableDescription>> getSchemeData() {
		Map<String, List<ITableDescription>> tablesByKeyspace = new HashMap<String, List<ITableDescription>>();
		Table[] tables = Table.values();
		for (Table table : tables) {
			String keyspace = table.getTableDescription().getKeyspace().toLowerCase();
			List<ITableDescription> list = tablesByKeyspace.get(keyspace);
			if (list == null) {
				list = new ArrayList<>();
			}
			list.add(table.getTableDescription());
			tablesByKeyspace.put(keyspace, list);
		}
		return tablesByKeyspace;
	}

	/**
 	 * the methoed creates the query string for the given keyspace the methoed
	 * valides the given data according the the requirments of the replication
	 * strategy SimpleStrategy: "CREATE KEYSPACE IF NOT EXISTS
	 * <keyspaceName></keyspaceName> WITH replication =
	 * {'class':'SimpleStrategy', 'replication_factor':2};" SimpleStrategy:
	 * "CREATE KEYSPACE IF NOT EXISTS <keyspaceName></keyspaceName> WITH
	 * replication = {'class':'NetworkTopologyStrategy', 'dc1' : 2 ,dc2 : 2 };"
	 * 
	 * @param keyspace
	 *            name of the keyspace we want to create
	 * @param keyspaceInfo
	 *            configuration info regurding the replication of the keyspace
	 * @return a querey string for the creation of the keyspace
	 */
	private static String createKeyspaceQuereyString(String keyspace, Configuration.CassandrConfig.KeyspaceConfig keyspaceInfo) {
		String query = null;
		if (ReplicationStrategy.NETWORK_TOPOLOGY_STRATEGY.getName().equalsIgnoreCase(keyspaceInfo.getReplicationStrategy())) {
			List<String> dcList = keyspaceInfo.getReplicationInfo();
			if (dcList.size() % 2 != 0) {
				log.error("the supplied replication info is in valid expected dc1,2,dc2,2 etc received:{}", dcList);
				return query;
			}
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < dcList.size(); i = i + 2) {
				sb.append("'").append(dcList.get(i)).append("'").append(" : ").append(dcList.get(i + 1));
				if (i + 2 < dcList.size()) {
					sb.append(",");
				}
			}

			query = String.format(CREATE_KEYSPACE_NETWORK_TOPOLOGY_STRATEGY, keyspace, sb.toString());
		} else if (ReplicationStrategy.SIMPLE_STRATEGY.getName().equalsIgnoreCase(keyspaceInfo.getReplicationStrategy())) {
			List<String> dcList = keyspaceInfo.getReplicationInfo();
			if (dcList.size() != 1) {
				log.error("the supplied replication info is in valid expected <number> etc received:{}", dcList);
				return query;
			}
			StringBuilder sb = new StringBuilder();
			sb.append("'replication_factor'").append(" : ").append(dcList.get(0));
			query = String.format(CREATE_KEYSPACE_SIMPLE_STRATEGY, keyspace, sb.toString());

		} else {
			log.error("the suplied replication Strategy  is in valide expacted {}/{} etc recived:{}",
					ReplicationStrategy.NETWORK_TOPOLOGY_STRATEGY.getName(),
					ReplicationStrategy.SIMPLE_STRATEGY.getName(), keyspaceInfo.getReplicationStrategy());
		}
		return query;
	}

	public enum ReplicationStrategy {
		NETWORK_TOPOLOGY_STRATEGY("NetworkTopologyStrategy"), SIMPLE_STRATEGY("SimpleStrategy");

		public String name;

		private ReplicationStrategy(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

}
