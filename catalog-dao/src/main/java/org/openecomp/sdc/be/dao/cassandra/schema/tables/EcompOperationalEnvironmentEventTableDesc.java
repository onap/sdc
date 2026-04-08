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

import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.DataTypes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.cassandra.schema.ITableDescription;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;

public class EcompOperationalEnvironmentEventTableDesc implements ITableDescription {

    private static final String OPERATIONAL_ENVIRONMENT_ID = "operational_environment_id";

    @Override
    public List<ImmutablePair<String, DataType>> primaryKeys() {
        List<ImmutablePair<String, DataType>> keys = new ArrayList<>();
        keys.add(new ImmutablePair<>(OPERATIONAL_ENVIRONMENT_ID, DataTypes.TEXT));
        return keys;
    }

    @Override
    public List<ImmutablePair<String, DataType>> clusteringKeys() {
        List<ImmutablePair<String, DataType>> keys = new ArrayList<>();
        keys.add(new ImmutablePair<>(TIMESTAMP_FIELD, DataTypes.TIMESTAMP));
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

    @Getter
    @AllArgsConstructor
    enum EcompOpEnvFieldsDescription {
        // @formatter:off
        ACTION("action", DataTypes.TEXT, false),
        OPERATIONAL_ENVIRONMENT_NAME("operational_environment_name", DataTypes.TEXT, false),
        OPERATIONAL_ENVIRONMENT_TYPE("operational_environment_type", DataTypes.TEXT, false),
        OPERATIONAL_ENVIRONMENT_ACTION("operational_environment_action", DataTypes.TEXT, false),
        TENANT_CONTEXT("tenant_context", DataTypes.TEXT, false);
        // @formatter:on

        private final String name;
        private final DataType type;
        private final boolean indexed;

    }
}
