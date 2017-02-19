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

import com.datastax.driver.core.*;
import com.datastax.driver.core.schemabuilder.Alter;
import com.datastax.driver.core.schemabuilder.Create;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import com.datastax.driver.core.schemabuilder.SchemaStatement;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.config.*;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

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

	/**
	 * the method creates all keyspaces, tables and indexes in case they do not
	 * already exist. the method can be run multiple times. the method uses the
	 * internal enums and external configuration for its operation
	 * 
	 * @return true if the create operation was successful
	 */
	public static boolean createSchema() {
		Cluster cluster = null;
		Session session = null;
		try {
			log.info("creating Schema for Cassandra.");
			cluster = createCluster();
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
			Map<String, Map<String, List<String>>> cassndraMetadata = parseKeyspaceMetadata(
					keyspacesMetadateFromCassandra);
			Map<String, List<ITableDescription>> schemeData = getSchemeData();
			log.info("creating Keyspaces.");
			for (String keyspace : schemeData.keySet()) {
				if (!createKeyspace(keyspace, cassndraMetadata, session)) {
					return false;
				}
				Map<String, List<String>> keyspaceMetadate = cassndraMetadata.get(keyspace);
				createTables(schemeData.get(keyspace), keyspaceMetadate, session);

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
			cluster = createCluster();
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
			Map<String, Map<String, List<String>>> cassndraMetadata = parseKeyspaceMetadata(
					keyspacesMetadateFromCassandra);
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
	 * the method creates the cluster object using the supplied cassandra nodes
	 * in the configuration
	 * 
	 * @return cluster object our null in case of an invalid configuration
	 */
	private static Cluster createCluster() {
		List<String> nodes = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig()
				.getCassandraHosts();
		if (nodes == null) {
			log.info("no nodes were supplied in configuration.");
			return null;
		}
		log.info("connecting to node:{}.", nodes);
		Cluster.Builder clusterBuilder = Cluster.builder();
		nodes.forEach(host -> clusterBuilder.addContactPoint(host));

		clusterBuilder.withMaxSchemaAgreementWaitSeconds(60);

		boolean authenticate = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig()
				.isAuthenticate();
		if (authenticate) {
			String username = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig()
					.getUsername();
			String password = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig()
					.getPassword();
			if (username == null || password == null) {
				log.info("authentication is enabled but username or password were not supplied.");
				return null;
			}
			clusterBuilder.withCredentials(username, password);
		}
		boolean ssl = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().isSsl();
		if (ssl) {
			String truststorePath = ConfigurationManager.getConfigurationManager().getConfiguration()
					.getCassandraConfig().getTruststorePath();
			String truststorePassword = ConfigurationManager.getConfigurationManager().getConfiguration()
					.getCassandraConfig().getTruststorePassword();
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
	private static Map<String, Map<String, List<String>>> parseKeyspaceMetadata(
			List<KeyspaceMetadata> keyspacesMetadata) {
		Map<String, Map<String, List<String>>> cassndraMetadata = keyspacesMetadata.stream()
				.collect(Collectors.toMap(keyspaceMetadata -> keyspaceMetadata.getName(),
						keyspaceMetadata -> keyspaceMetadata.getTables().stream()
								.collect(Collectors.toMap(tableMetadata -> tableMetadata.getName(),
										tableMetadata -> tableMetadata.getIndexes().stream()
												.map(indexMetadata -> indexMetadata.getName())
												.collect(Collectors.toList())))));
		return cassndraMetadata;
	}

	/**
	 * the method builds an index name according to a defined logic
	 * <table>
	 * _<column>_idx
	 * 
	 * @param table
	 *            table name
	 * @param column
	 *            column name
	 * @return string name of the index
	 */
	private static String createIndexName(String table, String column) {
		return new StringBuilder().append(table).append("_").append(column).append("_idx").toString();
	}

	/**
	 * the method creats all the tables and indexes thet do not already exist
	 *
	 * @param iTableDescriptions
	 *            a list of table description we want to create
	 * @param keyspaceMetadate
	 *            the current tables that exist in the cassandra under this
	 *            keyspace
	 * @param session
	 *            the session object used for the execution of the query.
	 */
	private static void createTables(List<ITableDescription> iTableDescriptions,
			Map<String, List<String>> keyspaceMetadate, Session session) {

		for (ITableDescription tableDescription : iTableDescriptions) {
			String tableName = tableDescription.getTableName().toLowerCase();
			Map<String, ImmutablePair<DataType, Boolean>> columnDescription = tableDescription.getColumnDescription();
			log.info("creating tables:{}.", tableName);
			if (keyspaceMetadate == null || !keyspaceMetadate.keySet().contains(tableName)) {
				Create create = SchemaBuilder.createTable(tableDescription.getKeyspace(),
						tableDescription.getTableName());
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
				ResultSet result = session.execute(create);
				log.info("table:{} created succsesfully.", tableName);
			} else {
				log.info("table:{} already exists skiping.", tableName);
			}
			List<String> indexNames = (keyspaceMetadate != null ? keyspaceMetadate.get(tableName) : new ArrayList<>());
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
	 * the method create the keyspace in case it does not already exists the
	 * method uses configurtion to select the needed replication strategy
	 * 
	 * @param keyspace
	 *            name of the keyspace we want to create
	 * @param cassndraMetadata
	 *            cassndra metadata
	 * @param session
	 *            the session object used for the execution of the query.
	 * @return true in case the operation was successful
	 */
	private static boolean createKeyspace(String keyspace, Map<String, Map<String, List<String>>> cassndraMetadata,
			Session session) {
		List<Configuration.CassandrConfig.KeyspaceConfig> keyspaceConfigList = ConfigurationManager
				.getConfigurationManager().getConfiguration().getCassandraConfig().getKeySpaces();
		log.info("creating keyspace:{}.", keyspace);
		if (!cassndraMetadata.keySet().contains(keyspace)) {
			Optional<Configuration.CassandrConfig.KeyspaceConfig> keyspaceConfig = keyspaceConfigList.stream()
					.filter(keyspaceInfo -> keyspace.equalsIgnoreCase(keyspaceInfo.getName())).findFirst();
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
	private static String createKeyspaceQuereyString(String keyspace,
			Configuration.CassandrConfig.KeyspaceConfig keyspaceInfo) {
		String query = null;
		if (ReplicationStrategy.NETWORK_TOPOLOGY_STRATEGY.getName()
				.equalsIgnoreCase(keyspaceInfo.getReplicationStrategy())) {
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
		} else if (ReplicationStrategy.SIMPLE_STRATEGY.getName()
				.equalsIgnoreCase(keyspaceInfo.getReplicationStrategy())) {
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
