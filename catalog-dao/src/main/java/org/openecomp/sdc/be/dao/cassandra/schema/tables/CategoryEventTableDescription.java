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
package org.openecomp.sdc.be.dao.cassandra.schema.tables;

import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.DataTypes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.cassandra.schema.ITableDescription;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;

public class CategoryEventTableDescription implements ITableDescription {

    @Override
    public List<ImmutablePair<String, DataType>> primaryKeys() {
        List<ImmutablePair<String, DataType>> keys = new ArrayList<>();
        keys.add(new ImmutablePair<>(TIMEBASED_UUID_FIELD, DataTypes.TIMEUUID));
        return keys;
    }

    @Override
    public List<ImmutablePair<String, DataType>> clusteringKeys() {
        List<ImmutablePair<String, DataType>> keys = new ArrayList<>();
        keys.add(new ImmutablePair<>(TIMESTAMP_FIELD, DataTypes.TIMESTAMP));
        return keys;
    }

    @Override
    public Map<String, ImmutablePair<DataType, Boolean>> getColumnDescription() {
        Map<String, ImmutablePair<DataType, Boolean>> columns = new HashMap<>();
        for (CEFieldsDescription field : CEFieldsDescription.values()) {
            columns.put(field.getName(), new ImmutablePair<>(field.getType(), field.isIndexed()));
        }
        return columns;
    }

    @Override
    public String getKeyspace() {
        return AuditingTypesConstants.AUDIT_KEYSPACE;
    }

    @Override
    public String getTableName() {
        return AuditingTypesConstants.CATEGORY_EVENT_TYPE;
    }

    enum CEFieldsDescription {
        // @formatter:off
        ACTION("action", DataTypes.TEXT, true),
        STATUS("status", DataTypes.TEXT, false),
        DESC("description", DataTypes.TEXT, false),
        CATEGORY_NAME("category_Name", DataTypes.TEXT, false),
        SUB_CATEGORY_NAME("sub_Category_Name", DataTypes.TEXT, false),
        GROUPING_NAME("grouping_name", DataTypes.TEXT, false),
        MODIFIER("modifier", DataTypes.TEXT, false),
        REQUEST_ID("request_id", DataTypes.TEXT, false),
        RESOURCE_TYPE("resource_type", DataTypes.TEXT, false),
        SERVICE_INSTANCE_ID("service_instance_id", DataTypes.TEXT, false);
        // @formatter:on

        private final String name;
        private final DataType type;
        private final boolean indexed;

        CEFieldsDescription(String name, DataType type, boolean indexed) {
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
