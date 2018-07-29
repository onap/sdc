package org.openecomp.sdc.be.dao.cassandra.schema.tables;

import com.datastax.driver.core.DataType;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.cassandra.schema.ITableDescription;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;

import java.util.*;

public class EcompOperationalEnvironmentEventTableDesc implements ITableDescription {

    private static final String OPERATIONAL_ENVIRONMENT_ID = "operational_environment_id";

    @Override
    public List<ImmutablePair<String, DataType>> primaryKeys() {
        List<ImmutablePair<String, DataType>> keys = new ArrayList<>();
        keys.add(new ImmutablePair<>(OPERATIONAL_ENVIRONMENT_ID, DataType.varchar()));
        return keys;
    }

    @Override
    public List<ImmutablePair<String, DataType>> clusteringKeys() {
        List<ImmutablePair<String, DataType>> keys = new ArrayList<>();
        keys.add(new ImmutablePair<>(TIMESTAMP_FIELD, DataType.timestamp()));
        return keys;
    }

    @Override
    public String getKeyspace() {
        return AuditingTypesConstants.AUDIT_KEYSPACE;
    }

    @Override
    public Map<String, ImmutablePair<DataType, Boolean>> getColumnDescription() {
        Map<String, ImmutablePair<DataType, Boolean>> columns = new HashMap<>();

        Arrays.stream(EcompOpEnvFieldsDescription.values())
                .forEach(column -> columns.put(column.getName(), ImmutablePair.of(column.getType(), column.isIndexed())));
        return columns;
    }

    @Override
    public String getTableName() {
        return AuditingTypesConstants.ECOMP_OPERATIONAL_ENV_EVENT_TYPE;
    }

    enum EcompOpEnvFieldsDescription {
        ACTION("action", DataType.varchar(), false),
        OPERATIONAL_ENVIRONMENT_NAME("operational_environment_name", DataType.varchar(), false),
        OPERATIONAL_ENVIRONMENT_TYPE("operational_environment_type", DataType.varchar(), false),
        OPERATIONAL_ENVIRONMENT_ACTION("operational_environment_action", DataType.varchar(), false),
        TENANT_CONTEXT("tenant_context", DataType.varchar(), false);

        private String name;
        private DataType type;
        private boolean indexed;

        EcompOpEnvFieldsDescription(String name, DataType type, boolean indexed) {
            this.name = name;
            this.type = type;
            this.indexed = indexed;
        }

        public String getName() {
            return name;
        }

        public DataType getType() {
            return type;
        }

        public boolean isIndexed() {
            return indexed;
        }
    }
}
