package org.openecomp.sdc.be.dao.cassandra.schema.tables;

import com.datastax.driver.core.DataType;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.cassandra.schema.ITableDescription;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;

import java.util.*;


public class OperationalEnvironmentsTableDescription implements ITableDescription {

    private static final String OPERATIONAL_ENVIRONMENT_TABLE = "operationalEnvironment";
    private static final String ENVIRONMENT_ID = "environment_id";

    @Override
    public List<ImmutablePair<String, DataType>> primaryKeys() {
        List<ImmutablePair<String, DataType>> keys = new ArrayList<>();
        keys.add(new ImmutablePair<>(ENVIRONMENT_ID, DataType.varchar()));
        return keys;
    }

    @Override
    public List<ImmutablePair<String, DataType>> clusteringKeys() {
        return new LinkedList<>();
    }

    @Override
    public Map<String, ImmutablePair<DataType, Boolean>> getColumnDescription() {
        Map<String, ImmutablePair<DataType, Boolean>> columns = new HashMap<>();
        Arrays.stream(SdcOperationalEnvironmentFieldsDescription.values())
                .forEach(column -> columns.put(column.getFieldName(), ImmutablePair.of(column.getFieldType(), column.isIndexed())));
        return columns;
    }

    @Override
    public String getKeyspace() {
        return AuditingTypesConstants.REPO_KEYSPACE;
    }

    @Override
    public String getTableName() {
        return OPERATIONAL_ENVIRONMENT_TABLE;
    }

    enum SdcOperationalEnvironmentFieldsDescription {
        //there is also PK field "environmentID"
        TENANT("tenant", DataType.varchar(), false),
        IS_PRODUCTION("is_production", DataType.cboolean(), false),
        ECOMP_WORKLOAD_CONTEXT("ecomp_workload_context", DataType.varchar(), false),
        DMAAP_UEB_ADDRESS("dmaap_ueb_address", DataType.set(DataType.varchar()), false),
        UEB_API_KEY("ueb_api_key",DataType.varchar(), false),
        UEB_SECRET_KEY("ueb_secret_key",DataType.varchar(), false),
        STATUS("status",DataType.varchar() ,true),
        LAST_MODIFIED("last_modified",DataType.timestamp() ,false);

        private String fieldName;
        private boolean isIndexed;
        private DataType fieldType;

        SdcOperationalEnvironmentFieldsDescription(String fieldName, DataType dataType, boolean indexed ) {
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
