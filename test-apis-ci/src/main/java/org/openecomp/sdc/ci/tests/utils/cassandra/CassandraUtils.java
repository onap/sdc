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

package org.openecomp.sdc.ci.tests.utils.cassandra;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.javatuples.Pair;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Select.Where;

public final class CassandraUtils {
	private static Logger logger = LoggerFactory.getLogger(CassandraUtils.class.getName());

	protected static Cluster cluster = null;
	protected static Session session;

	protected static void initConnection(String keyspace) throws FileNotFoundException {

		String cassandraHost = Utils.getConfig().getCassandraHost();
		Boolean cassandraAuthenticate = Utils.getConfig().getCassandraAuthenticate();
		String cassandraUsername = Utils.getConfig().getCassandraUsername();
		String cassandraPassword = Utils.getConfig().getCassandraPassword();
		Boolean cassandraSsl = Utils.getConfig().getCassandraSsl();
		String cassandraTruststorePath = Utils.getConfig().getCassandraTruststorePath();
		String cassandraTruststorePassword = Utils.getConfig().getCassandraTruststorePassword();
		/*
		 * String cassandraAuditKeySpace=
		 * Utils.getConfig().getCassandraAuditKeySpace(); String
		 * cassandraArtifactKeySpace=
		 * Utils.getConfig().getCassandraArtifactKeySpace();
		 */

		Cluster.Builder clusterBuilder = Cluster.builder().addContactPoint(cassandraHost);
		if (cassandraAuthenticate) {
			// authantication
			clusterBuilder.withCredentials(cassandraUsername, cassandraPassword);
		}

		if (cassandraSsl) {
			// ssl
			System.setProperty("javax.net.ssl.trustStore", cassandraTruststorePath);
			System.setProperty("javax.net.ssl.trustStorePassword", cassandraTruststorePassword);
			clusterBuilder.withSSL();
		}

		cluster = clusterBuilder.build();
		session = cluster.connect(keyspace);

	}

	public static void truncateTable(String keyspace, String tableName) throws FileNotFoundException {

		if (session == null || session.isClosed()) {
			initConnection(keyspace);
		}

		try {

			if (session != null) {
				session.execute(QueryBuilder.truncate(keyspace, tableName));
				logger.debug("The table {}.{} was cleaned",keyspace,tableName);
			} else {
				throw new RuntimeException("Keyspace " + keyspace + " not connected");
			}
		} finally {
			// if (cluster != null) {
			// cluster.close();
			// }
		}
	}

	public static void close() {
		if (cluster != null) {
			cluster.close();
		}
	}

	public static void truncateAllKeyspaces() throws FileNotFoundException {
		// truncateAllTables(AuditingTypesConstants.ARTIFACT_KEYSPACE);
		truncateAllTables(AuditingTypesConstants.AUDIT_KEYSPACE);
	}

	public static void truncateAllTables(String keyspace) throws FileNotFoundException {

		if (session == null || session.isClosed()) {
			initConnection(keyspace);
		}
		try {

			if (session != null) {
				Metadata metadata = cluster.getMetadata();
				KeyspaceMetadata keyspaceMetadata = metadata.getKeyspace(keyspace);
				if (keyspaceMetadata != null) {
					Collection<TableMetadata> tables = keyspaceMetadata.getTables();
					tables.forEach(table -> {
						session.execute(QueryBuilder.truncate(table));
						logger.debug("Table trunceted - {}", table.getName());
					});
				}
			} else {
				throw new RuntimeException("Keyspace " + keyspace + " not connected");
			}

		} finally {
			// if (cluster != null) {
			// cluster.close();
			// }
		}
	}

	public static List<Row> fetchFromTable(String keyspace, String tableName,
			List<Pair<AuditingFieldsKeysEnum, String>> fields) throws FileNotFoundException {

		List<Pair<String, String>> fieldsConverted = new ArrayList<>();

//		fields.forEach(pair -> {
//			Pair<String, String> newPair = new Pair(pair.getValue0().getDisplayName(), pair.getValue1());
//			fieldsConverted.add(newPair);
//		});
		
		fields.forEach(pair ->{
			Pair<String, String> newPair;
			if(pair.getValue0() == AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL ){
			newPair = new Pair<String, String>("RESOURE_URL", pair.getValue1());
			
		}else{
			newPair = new Pair<String, String>(pair.getValue0().getDisplayName(), pair.getValue1());
		}
		fieldsConverted.add(newPair);
			
		});

		return fetchFromTableQuery(keyspace, tableName, fieldsConverted);
	}

	public static List<Row> fetchFromTableQuery(String keyspace, String tableName, List<Pair<String, String>> fields)
			throws FileNotFoundException {

		if (session == null || session.isClosed()) {
			initConnection(keyspace);
		}
		try {

			if (session != null) {
				Select select = QueryBuilder.select().all().from(keyspace, tableName);
				if (fields != null) {
					// Set<Entry<AuditingFieldsKeysEnum, String>> entrySet =
					// fields.entrySet();
					// fields.
					boolean multiple = (fields.size() > 1) ? true : false;
					Where where = null;
					int size = 0;

					for (Pair<String, String> pair : fields) {
						++size;
						if (size == 1) {
							where = select.where(QueryBuilder.eq(pair.getValue0(), pair.getValue1()));
						} else {
							where.and(QueryBuilder.eq(pair.getValue0(), pair.getValue1()));
						}
					}
					if (multiple) {
						select.allowFiltering();
					}

				}

				List<Row> rows = session.execute(select).all();
				for (Row row : rows) {
					logger.debug("{}", row);
				}
				return rows;
			}
		} finally {
			// if (cluster != null) {
			// cluster.close();
			// }
		}
		return null;
	}
	//
	// public static void main(String[] args) throws FileNotFoundException {
	// Map<AuditingFieldsKeysEnum, String> map = new HashMap<>();
	// map.put(AuditingFieldsKeysEnum.AUDIT_ACTION, "Access");
	// map.put(AuditingFieldsKeysEnum.AUDIT_STATUS, "200");
	// // CassandraUtils.truncateTable("sdcartifact", "resources");
	//// CassandraUtils.truncateAllTables("sdcaudit");
	// CassandraUtils.fetchFromTable("sdcaudit", "useraccessevent", map );
	// }

}
