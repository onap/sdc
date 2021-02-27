/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.dao.cassandra.schema.tables;

import com.datastax.driver.core.DataType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.cassandra.schema.ITableDescription;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;

import java.util.*;

import static org.openecomp.sdc.be.dao.cassandra.schema.tables.MigrationTasksTableDescription.SdcRepoFieldsDescription.MAJOR_VERSION;
import static org.openecomp.sdc.be.dao.cassandra.schema.tables.MigrationTasksTableDescription.SdcRepoFieldsDescription.MINOR_VERSION;

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

    @Getter
    @AllArgsConstructor
    enum SdcRepoFieldsDescription {
        MAJOR_VERSION("major_version", DataType.bigint(), true),
        MINOR_VERSION("minor_version", DataType.bigint(), false),
        TIMESTAMP("timestamp", DataType.timestamp(), false),
        NAME("task_name", DataType.varchar(), false),
        STATUS("task_status", DataType.varchar(), false),
        MESSAGE("msg", DataType.varchar(), false),
        DESCRIPTION("description", DataType.varchar(), false),
        EXECUTION_TIME("execution_time", DataType.cdouble(), false);

        private final String fieldName;
        private final DataType fieldType;
        private final boolean isIndexed;

    }
}
