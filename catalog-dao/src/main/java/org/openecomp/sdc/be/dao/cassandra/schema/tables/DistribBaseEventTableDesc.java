package org.openecomp.sdc.be.dao.cassandra.schema.tables;

import com.datastax.driver.core.DataType;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.cassandra.schema.ITableDescription;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DistribBaseEventTableDesc implements ITableDescription {
    @Override
    public List<ImmutablePair<String, DataType>> primaryKeys() {
        List<ImmutablePair<String, DataType>> keys = new ArrayList<>();
        keys.add(new ImmutablePair<>(TIMEBASED_UUID_FIELD, DataType.timeuuid()));
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

        for (DistFieldsDescription field : DistFieldsDescription.values()) {
            columns.put(field.getName(), new ImmutablePair<>(field.type, field.indexed));
        }
        updateColumnDistribDescription(columns);
        return columns;
    }

    protected abstract void updateColumnDistribDescription(final Map<String, ImmutablePair<DataType, Boolean>> columns);


    enum DistFieldsDescription {
        ACTION("action", DataType.varchar(), true),
        STATUS("status", DataType.varchar(), false),
        DESCRIPTION("description", DataType.varchar(), false),
        REQUEST_ID("request_id", DataType.varchar(), false),
        SERVICE_INST_ID("service_instance_id", DataType.varchar(), false);

        private String name;
        private DataType type;
        private boolean indexed;

        DistFieldsDescription(String name, DataType type, boolean indexed) {
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
