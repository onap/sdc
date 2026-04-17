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
package org.openecomp.sdc.be.dao.cassandra.schema;

import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata;
import com.datastax.oss.driver.api.core.session.Session;
import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.type.DataType;              
import com.datastax.oss.driver.api.core.metadata.schema.IndexMetadata;    
import com.datastax.oss.driver.api.core.metadata.schema.KeyspaceMetadata;    
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder;
import com.datastax.oss.driver.api.querybuilder.schema.AlterTableAddColumn;
import com.datastax.oss.driver.api.querybuilder.schema.AlterTableStart;
import com.datastax.oss.driver.api.querybuilder.schema.CreateTable;
import com.datastax.oss.driver.api.querybuilder.schema.CreateTableStart;
import com.datastax.oss.driver.shaded.guava.common.base.Function;
import com.datastax.oss.driver.api.core.metadata.Metadata;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.dao.cassandra.schema.tables.OldExternalApiEventTableDesc;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;

public class SdcSchemaBuilder {

    /**
     * creat key space statment for SimpleStrategy
     */
    private static final String CREATE_KEYSPACE_SIMPLE_STRATEGY = "CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class':'SimpleStrategy', %s};";
    /**
     * creat key space statment for NetworkTopologyStrategy
     */
    private static final String CREATE_KEYSPACE_NETWORK_TOPOLOGY_STRATEGY = "CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class':'NetworkTopologyStrategy', %s};";
    private static Logger log = Logger.getLogger(SdcSchemaBuilder.class.getName());
    private SdcSchemaUtils sdcSchemaUtils;
    private Supplier<Configuration.CassandrConfig> cassandraConfigSupplier;
    public SdcSchemaBuilder(SdcSchemaUtils sdcSchemaUtils, Supplier<Configuration.CassandrConfig> cassandraConfigSupplier) {
        this.sdcSchemaUtils = sdcSchemaUtils;
        this.cassandraConfigSupplier = cassandraConfigSupplier;
    }

    //TODO remove after 1707_OS migration
    private static void handle1707OSMigration(Map<String, Map<String, List<String>>> cassndraMetadata,
                                              Map<String, List<ITableDescription>> schemeData) {
        if (cassndraMetadata.containsKey("attaudit")) {
            List<ITableDescription> list = new ArrayList<>();
            list.add(new OldExternalApiEventTableDesc());
            schemeData.put("attaudit", list);
        }
    }

    /**
     * the method prcess the metadata retrieved from the cassandra for the creation of a map conting the names of keyspaces tabls and indexes already
     * defined in the cassandra keyspacename -> tablename -> list of indexes info
     *
     * @param keyspacesMetadata cassndra mmetadata
     * @return a map of maps of lists holding parsed info
     */
    private static Map<String, Map<String, List<String>>> parseKeyspaceMetadata(List<KeyspaceMetadata> keyspacesMetadata) {
    return keyspacesMetadata.stream()
        .collect(Collectors.toMap(
            ks -> ks.getName().asInternal(),  // convert Keyspace name to String
            ks -> ks.getTables().entrySet().stream() // Stream<Map.Entry<CqlIdentifier, TableMetadata>>
                .collect(Collectors.toMap(
                    entry -> entry.getKey().asInternal(), // table name as String
                    entry -> entry.getValue().getIndexes().values().stream()
                        .map(index -> index.getName().asInternal())
                        .collect(Collectors.toList())
                ))
        ));
}


    private static Map<String, Map<String, List<String>>> getMetadataTablesStructure(List<KeyspaceMetadata> keyspacesMetadata) {
    return keyspacesMetadata.stream()
        .collect(Collectors.toMap(
            ks -> ks.getName().asInternal(), // Keyspace name as String
            ks -> ks.getTables().entrySet().stream() // Table entries
                .collect(Collectors.toMap(
                    entry -> entry.getKey().asInternal(), // Table name as String
                    entry -> entry.getValue().getColumns().entrySet().stream()
                        .map(colEntry -> colEntry.getKey().asInternal().toLowerCase()) // Column names as String
                        .collect(Collectors.toList())
                ))
        ));
}


    /**
     * the method builds an index name according to a defined logic
     * <table>
     * _<column>_idx
     *
     * @param table:  table name
     * @param column: column name
     * @return string name of the index
     */
    private static String createIndexName(String table, String column) {
        return table + "_" + column + "_idx";
    }

    /**
     * the method creats all the tables and indexes thet do not already exist
     *
     * @param iTableDescriptions:    a list of table description we want to create
     * @param keyspaceMetadata:      the current tables that exist in the cassandra under this keyspace
     * @param session:               the session object used for the execution of the query.
     * @param existingTablesMetadata the current tables columns that exist in the cassandra under this keyspace
     */
   private static void createTables(List<ITableDescription> iTableDescriptions,
                                 Map<String, List<String>> keyspaceMetadata,
                                 CqlSession session,
                                 Map<String, List<String>> existingTablesMetadata) {
    for (ITableDescription tableDescription : iTableDescriptions) {
        String tableName = tableDescription.getTableName().toLowerCase();
        Map<String, ImmutablePair<DataType, Boolean>> columnDescription = tableDescription.getColumnDescription();
        log.info("creating tables:{}.", tableName);

        if (keyspaceMetadata == null || !keyspaceMetadata.containsKey(tableName)) {

            // Build column -> cql-type map preserving insertion order
            Map<String, String> columnsToType = new LinkedHashMap<>();

            // 1) Partition keys
            List<String> pkNames = new ArrayList<>();
            if (tableDescription.primaryKeys() != null) {
                for (ImmutablePair<String, DataType> pk : tableDescription.primaryKeys()) {
                    String name = pk.getLeft();
                    String typeCql = pk.getRight().asCql(false, false);
                    columnsToType.put(name, typeCql);
                    pkNames.add(name);
                }
            }

            // 2) Clustering keys
            List<String> ckNames = new ArrayList<>();
            if (tableDescription.clusteringKeys() != null) {
                for (ImmutablePair<String, DataType> ck : tableDescription.clusteringKeys()) {
                    String name = ck.getLeft();
                    String typeCql = ck.getRight().asCql(false, false);
                    // avoid overwriting if already present
                    columnsToType.putIfAbsent(name, typeCql);
                    ckNames.add(name);
                }
            }

            // 3) Other columns from columnDescription (don't overwrite PK/CK types)
            if (columnDescription != null) {
                for (Map.Entry<String, ImmutablePair<DataType, Boolean>> entry : columnDescription.entrySet()) {
                    String name = entry.getKey();
                    String typeCql = entry.getValue().getLeft().asCql(false, false);
                    columnsToType.putIfAbsent(name, typeCql);
                }
            }

            // Build CREATE TABLE CQL
            StringBuilder cql = new StringBuilder();
            cql.append("CREATE TABLE IF NOT EXISTS ")
               .append(tableDescription.getKeyspace()).append(".").append(tableDescription.getTableName())
               .append(" (");

            // column definitions
            boolean first = true;
            for (Map.Entry<String, String> col : columnsToType.entrySet()) {
                if (!first) {
                    cql.append(", ");
                }
                first = false;
                cql.append(col.getKey()).append(" ").append(col.getValue());
            }

            // primary key clause
            // partition part
            String partitionPart;
            if (pkNames.isEmpty()) {
                // fallback - although old code expected at least one PK
                partitionPart = "id";
                if (!columnsToType.containsKey(partitionPart)) {
                    // if id not present, pick first column as pk (defensive)
                    if (!columnsToType.isEmpty()) {
                        partitionPart = columnsToType.keySet().iterator().next();
                    }
                }
            } else if (pkNames.size() == 1) {
                partitionPart = pkNames.get(0);
            } else {
                partitionPart = "(" + String.join(", ", pkNames) + ")";
            }

            String pkClause;
            if (ckNames.isEmpty()) {
                pkClause = partitionPart;
            } else {
                pkClause = partitionPart + ", " + String.join(", ", ckNames);
            }

            cql.append(", PRIMARY KEY (").append(pkClause).append(")");

            cql.append(");");

            // execute create table
            log.trace("executing : {}", cql.toString());
            session.execute(com.datastax.oss.driver.api.core.cql.SimpleStatement.newInstance(cql.toString()));
            log.info("table:{} created successfully.", tableName);

        } else {
            log.info("table:{} already exists, skipping.", tableName);
            alterTable(session, existingTablesMetadata, tableDescription, tableName, columnDescription);
        }

        log.info("keyspacemetadata:{}", keyspaceMetadata);

        // indexes
        List<String> indexNames = (keyspaceMetadata != null && keyspaceMetadata.get(tableName) != null
                ? keyspaceMetadata.get(tableName) : new ArrayList<>());

        log.info("table:{} creating indexes.", tableName);
        for (Map.Entry<String, ImmutablePair<DataType, Boolean>> description : columnDescription.entrySet()) {
            String indexName = createIndexName(tableName, description.getKey()).toLowerCase();
            if (description.getValue().getRight()) {
                if (!indexNames.contains(indexName)) {
                    var createIndex = SchemaBuilder.createIndex(indexName)
                            .ifNotExists()
                            .onTable(CqlIdentifier.fromCql(tableDescription.getKeyspace()), CqlIdentifier.fromCql(tableDescription.getTableName()))
                            .andColumn(CqlIdentifier.fromCql(description.getKey()));
                    log.info("executing :{}", createIndex);
                    session.execute(createIndex.build());
                    log.info("index:{} created successfully.", indexName);
                } else {
                    log.info("index:{} already exists, skipping.", indexName);
                }
            }
        }
    }
}



    /**
     * check if there are new columns that were added to definition but don't exist in DB
     *
     * @param session
     * @param existingTablesMetadata
     * @param tableDescription
     * @param tableName
     * @param columnDescription
     */
private static void alterTable(CqlSession session,
                               Map<String, List<String>> existingTablesMetadata,
                               ITableDescription tableDescription,
                               String tableName,
                               Map<String, ImmutablePair<DataType, Boolean>> columnDescription) {

    List<String> definedTableColumns = existingTablesMetadata.get(tableName);

    for (Map.Entry<String, ImmutablePair<DataType, Boolean>> column : columnDescription.entrySet()) {
        String columnName = column.getKey();

        if (!definedTableColumns.contains(columnName.toLowerCase())) {
            log.info("Adding new column {} to the table {}", columnName, tableName);

            SimpleStatement addColumnStmt = SchemaBuilder
                    .alterTable(tableDescription.getKeyspace(), tableDescription.getTableName())
                    .addColumn(columnName, column.getValue().getLeft())
                    .build();

            log.trace("executing :{}", addColumnStmt.getQuery());
            session.execute(addColumnStmt);

            log.info("Column {} added successfully to table {}", columnName, tableName);
        }
    }
}




    private static boolean createKeyspaceIfNotExists(String keyspace, CqlSession session,
                                                     List<Configuration.CassandrConfig.KeyspaceConfig> keyspaceConfigList) {
        Optional<Configuration.CassandrConfig.KeyspaceConfig> keyspaceConfig = keyspaceConfigList.stream()
            .filter(keyspaceInfo -> keyspace.equalsIgnoreCase(keyspaceInfo.getName())).findFirst();
        if (keyspaceConfig.isPresent()) {
            return createKeyspaceWhenConfigExists(keyspace, session, keyspaceConfig.get());
        }
        log.info("keyspace:{} not present in configuration, no info on replications is available. Operation failed.", keyspace);
        return false;
    }

    private static boolean createKeyspaceWhenConfigExists(String keyspace, CqlSession session,
                                                          Configuration.CassandrConfig.KeyspaceConfig keyspaceConfig) {
        String createKeyspaceQuery = createKeyspaceQuereyString(keyspace, keyspaceConfig);
        if (createKeyspaceQuery != null) {
            log.trace("executing: {}", createKeyspaceQuery);
            session.execute(createKeyspaceQuery);
            log.info("keyspace:{} created.", keyspace);
            return true;
        }
        return false;
    }

    /**
     * the method retries the schem info from the enums describing the tables
     *
     * @return a map of keyspaces to there table info
     */
    private static Map<String, List<ITableDescription>> getSchemeData() {
        Map<String, List<ITableDescription>> tablesByKeyspace = new HashMap<>();
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
     * the methoed creates the query string for the given keyspace the methoed valides the given data according the the requirments of the replication
     * strategy SimpleStrategy: "CREATE KEYSPACE IF NOT EXISTS
     * <keyspaceName></keyspaceName> WITH replication =
     * {'class':'SimpleStrategy', 'replication_factor':2};" SimpleStrategy: "CREATE KEYSPACE IF NOT EXISTS <keyspaceName></keyspaceName> WITH
     * replication = {'class':'NetworkTopologyStrategy', 'dc1' : 2 ,dc2 : 2 };"
     *
     * @param keyspace     name of the keyspace we want to create
     * @param keyspaceInfo configuration info regurding the replication of the keyspace
     * @return a querey string for the creation of the keyspace
     */
    private static String createKeyspaceQuereyString(String keyspace, Configuration.CassandrConfig.KeyspaceConfig keyspaceInfo) {
        String query = null;
        if (ReplicationStrategy.NETWORK_TOPOLOGY_STRATEGY.getStrategyName().equalsIgnoreCase(keyspaceInfo.getReplicationStrategy())) {
            query = createNetworkTopologyStrategy(keyspaceInfo, keyspace);
        } else if (ReplicationStrategy.SIMPLE_STRATEGY.getStrategyName().equalsIgnoreCase(keyspaceInfo.getReplicationStrategy())) {
            query = createSimpleStrategyQuery(keyspaceInfo, keyspace);
        } else {
            log.error("the supplied replication Strategy is invalid; expected {}/{}, received:{}",
                ReplicationStrategy.NETWORK_TOPOLOGY_STRATEGY.getStrategyName(), ReplicationStrategy.SIMPLE_STRATEGY.getStrategyName(),
                keyspaceInfo.getReplicationStrategy());
        }
        return query;
    }

    private static String createNetworkTopologyStrategy(Configuration.CassandrConfig.KeyspaceConfig keyspaceInfo, String keyspace) {
        String query = null;
        List<String> dcList = keyspaceInfo.getReplicationInfo();
        if (dcList.size() % 2 != 0) {
            log.error("the supplied replication info is invalid; expected dc1,2,dc2,2 etc, received:{}", dcList);
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < dcList.size(); i = i + 2) {
                sb.append("'").append(dcList.get(i)).append("'").append(" : ").append(dcList.get(i + 1));
                if (i + 2 < dcList.size()) {
                    sb.append(",");
                }
            }
            query = String.format(CREATE_KEYSPACE_NETWORK_TOPOLOGY_STRATEGY, keyspace, sb.toString());
        }
        return query;
    }

    private static String createSimpleStrategyQuery(Configuration.CassandrConfig.KeyspaceConfig keyspaceInfo, String keyspace) {
        String query = null;
        List<String> dcList = keyspaceInfo.getReplicationInfo();
        if (dcList.size() != 1) {
            log.error("the supplied replication info is invalid; expected <number>, received:{}", dcList);
        } else {
            query = String.format(CREATE_KEYSPACE_SIMPLE_STRATEGY, keyspace, "'replication_factor'" + " : " + dcList.get(0));
        }
        return query;
    }

    /**
     * the method creates all keyspaces, tables and indexes in case they do not already exist. the method can be run multiple times. the method uses
     * the internal enums and external configuration for its operation	 *
     *
     * @return true if the create operation was successful
     */
    public boolean createSchema() {
    try (CqlSession session = sdcSchemaUtils.createSession()) {
        log.info("Creating Schema for Cassandra.");

        // In driver 4.x, metadata is a Map<CqlIdentifier, KeyspaceMetadata>
        Map<CqlIdentifier, KeyspaceMetadata> keyspacesMetadataFromCassandra = session.getMetadata().getKeyspaces();

        if (keyspacesMetadataFromCassandra == null || keyspacesMetadataFromCassandra.isEmpty()) {
            log.debug("Failed to retrieve a list of keyspaces from Cassandra");
            return false;
        }
        log.debug("Retrieved Cassandra metadata.");

        // Convert Map<CqlIdentifier, KeyspaceMetadata> to your expected structure
        Map<String, Map<String, List<String>>> cassandraMetadata =
                parseKeyspaceMetadata(new ArrayList<>(keyspacesMetadataFromCassandra.values()));

        Map<String, Map<String, List<String>>> metadataTablesStructure =
                getMetadataTablesStructure(new ArrayList<>(keyspacesMetadataFromCassandra.values()));

        Map<String, List<ITableDescription>> schemeData = getSchemeData();

        // TODO remove after 1707_OS migration
        handle1707OSMigration(cassandraMetadata, schemeData);

        log.info("Creating Keyspaces.");
        for (Map.Entry<String, List<ITableDescription>> keyspace : schemeData.entrySet()) {
            if (!createKeyspace(keyspace.getKey(), cassandraMetadata, session)) {
                return false;
            }
            Map<String, List<String>> keyspaceMetadata = cassandraMetadata.get(keyspace.getKey());
            createTables(
                keyspace.getValue(),
                keyspaceMetadata,
                session,
                metadataTablesStructure.get(keyspace.getKey())
            );
        }
        return true;
    } catch (Exception e) {
        log.error(EcompLoggerErrorCode.SCHEMA_ERROR,
                  "Creating Schema for Cassandra",
                  "Cassandra",
                  e.getLocalizedMessage(), e);
        return false;
    }
}


   public boolean deleteSchema() {
    boolean res = false;

    try (CqlSession session = sdcSchemaUtils.createSession()) {
        log.info("delete Data from Cassandra.");

        Metadata metadata = session.getMetadata();

        // Iterate over keyspaces
        for (Map.Entry<CqlIdentifier, KeyspaceMetadata> entry : metadata.getKeyspaces().entrySet()) {
            String keyspaceName = entry.getKey().asInternal();
            KeyspaceMetadata keyspaceMetadata = entry.getValue();

            log.debug("Found keyspace: {}", keyspaceName);

            if (AuditingTypesConstants.janusGraph_KEYSPACE.equals(keyspaceName)) {
                log.info("Deleting JanusGraph keyspace: {}", keyspaceName);
                session.execute(SimpleStatement.builder("DROP KEYSPACE IF EXISTS " + keyspaceName).build());
            } else if (AuditingTypesConstants.ARTIFACT_KEYSPACE.equals(keyspaceName)) {
                log.info("Deleting Artifact keyspace: {}", keyspaceName);
                session.execute(SimpleStatement.builder("DROP KEYSPACE IF EXISTS " + keyspaceName).build());
            } else if (AuditingTypesConstants.AUDIT_KEYSPACE.equals(keyspaceName)) {
                log.info("Deleting Audit keyspace: {}", keyspaceName);
                session.execute(SimpleStatement.builder("DROP KEYSPACE IF EXISTS " + keyspaceName).build());
            }
        }

        res = true;
    } catch (Exception e) {
        log.error(EcompLoggerErrorCode.SCHEMA_ERROR, "deleting Schema for Cassandra", "Cassandra", e.getLocalizedMessage(), e);
    }

    return res;
}

    /**
     * the method create the keyspace in case it does not already exists the method uses configurtion to select the needed replication strategy
     *
     * @param keyspace:         name of the keyspace we want to create
     * @param cassndraMetadata: cassndra metadata
     * @param session:          the session object used for the execution of the query.
     * @return true in case the operation was successful
     */
    private boolean createKeyspace(String keyspace,
                               Map<String, Map<String, List<String>>> cassndraMetadata,
                               CqlSession session) { 
    List<Configuration.CassandrConfig.KeyspaceConfig> keyspaceConfigList =
            cassandraConfigSupplier.get().getKeySpaces();

    log.info("creating keyspace:{}.", keyspace);
    if (!cassndraMetadata.containsKey(keyspace)) {
        return createKeyspaceIfNotExists(keyspace, session, keyspaceConfigList); 
    }
    log.info("keyspace:{} already exists, skipping.", keyspace);
    return true;
}

    @AllArgsConstructor
    public enum ReplicationStrategy {
        NETWORK_TOPOLOGY_STRATEGY("NetworkTopologyStrategy"), SIMPLE_STRATEGY("SimpleStrategy");
        @Getter
        private final String strategyName;
    }
}
