package org.openecomp.sdc.be.dao.cassandra.schema.tables;

import com.datastax.driver.core.DataType;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.cassandra.schema.ITableDescription;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureToggleEventTableDesc implements ITableDescription {

    private static final String FEATURE_NAME = "feature_name";

    @Override
    public List<ImmutablePair<String, DataType>> primaryKeys() {
        List<ImmutablePair<String, DataType>> keys = new ArrayList<>();
        keys.add(new ImmutablePair<>(FEATURE_NAME, DataType.varchar()));
        return keys;
    }

    @Override
    public List<ImmutablePair<String, DataType>> clusteringKeys() {
        return Lists.newArrayList();
    }

    @Override
    public Map<String, ImmutablePair<DataType, Boolean>> getColumnDescription() {
        Map<String, ImmutablePair<DataType, Boolean>> columns = new HashMap<>();
        Arrays.stream(FeatureToggleEventFieldsDescription.values())
                .forEach(column -> columns.put(column.getName(), ImmutablePair.of(column.getType(), column.isIndexed())));
        return columns;
    }

    @Override
    public String getKeyspace() {
        return AuditingTypesConstants.REPO_KEYSPACE;
    }

    @Override
    public String getTableName() {
        return AuditingTypesConstants.FEATURE_TOGGLE_STATE;
    }

    enum FeatureToggleEventFieldsDescription {
        ENABLED("enabled", DataType.varchar(), false),
        STRATEGY_ID("strategy_id", DataType.varchar(), false),
        PARAMETERS("parameters", DataType.varchar(), false);

        private String name;
        private DataType type;
        private boolean indexed;

        FeatureToggleEventFieldsDescription(String name, DataType type, boolean indexed) {
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
