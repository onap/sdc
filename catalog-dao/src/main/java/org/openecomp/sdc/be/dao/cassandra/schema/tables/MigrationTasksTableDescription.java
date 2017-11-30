package org.openecomp.sdc.be.dao.cassandra.schema.tables;

import static org.openecomp.sdc.be.dao.cassandra.schema.tables.MigrationTasksTableDescription.SdcRepoFieldsDescription.MAJOR_VERSION;
import static org.openecomp.sdc.be.dao.cassandra.schema.tables.MigrationTasksTableDescription.SdcRepoFieldsDescription.MINOR_VERSION;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.cassandra.schema.ITableDescription;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;

import com.datastax.driver.core.DataType;

public class MigrationTasksTableDescription implements ITableDescription {

    private static final String MIGRATION_TASKS_TABLE = "migrationTasks";

    @Override
    public List<ImmutablePair<String, DataType>> primaryKeys() {
        return Collections.singletonList(ImmutablePair.of(MAJOR_VERSION.getFieldName(), MAJOR_VERSION.getFieldType()));
    }

    @Override
    public List<ImmutablePair<String, DataType>> clusteringKeys() {
        return Collections.singletonList(ImmutablePair.of(MINOR_VERSION.getFieldName(), MINOR_VERSION.getFieldType()));
    }

    @Override
    public Map<String, ImmutablePair<DataType, Boolean>> getColumnDescription() {
        Map<String, ImmutablePair<DataType, Boolean>> columns = new HashMap<>();
        Arrays.stream(SdcRepoFieldsDescription.values())
                .filter(column -> !column.equals(MAJOR_VERSION) && !column.equals(MINOR_VERSION))
                .forEach(column -> columns.put(column.getFieldName(), ImmutablePair.of(column.getFieldType(), column.isIndexed())));
        return columns;
    }

    @Override
    public String getKeyspace() {
        return AuditingTypesConstants.REPO_KEYSPACE;
    }

    @Override
    public String getTableName() {
        return MIGRATION_TASKS_TABLE;
    }

    enum SdcRepoFieldsDescription {
        MAJOR_VERSION("major_version", DataType.bigint(), true),
        MINOR_VERSION("minor_version", DataType.bigint(), false),
        TIMESTAMP("timestamp", DataType.timestamp(), false),
        NAME("task_name", DataType.varchar(), false),
        STATUS("task_status", DataType.varchar(), false),
        MESSAGE("msg", DataType.varchar(), false),
        DESCRIPTION("description", DataType.varchar(), false),
        EXECUTION_TIME("execution_time", DataType.cdouble(), false);

        private String fieldName;
        private boolean isIndexed;
        private DataType fieldType;

        SdcRepoFieldsDescription(String fieldName, DataType dataType, boolean indexed ) {
            this.fieldName = fieldName;
            this.fieldType = dataType;
            this.isIndexed = indexed;
        }

        public String getFieldName() {
            return fieldName;
        }

        public boolean isIndexed() {
            return isIndexed;
        }

        public DataType getFieldType() {
            return fieldType;
        }
    }
}
