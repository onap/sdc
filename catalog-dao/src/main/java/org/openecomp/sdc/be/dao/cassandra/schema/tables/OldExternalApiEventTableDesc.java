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
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.cassandra.schema.ITableDescription;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;

public class OldExternalApiEventTableDesc implements ITableDescription {

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
        for (EGAEFieldsDescription field : EGAEFieldsDescription.values()) {
            columns.put(field.getName(), new ImmutablePair<>(field.type, field.indexed));
        }
        return columns;
    }

    @Override
    public String getKeyspace() {
        return AuditingTypesConstants.AUDIT_KEYSPACE.replace("sdc", "att");
    }

    @Override
    public String getTableName() {
        return AuditingTypesConstants.EXTERNAL_API_EVENT_TYPE;
    }

    @Getter
    @AllArgsConstructor
    enum EGAEFieldsDescription {
        // @formatter:off
		ACTION("action", DataTypes.TEXT, true),
		STATUS("status", DataTypes.TEXT, false),
		DESCRIPTION( "description", DataTypes.TEXT, false),
		CONSUMER_ID("consumer_id", DataTypes.TEXT, false),
		RESOURCE_URL("resource_URL", DataTypes.TEXT, false),
		RESOURCE_NAME("resource_name", DataTypes.TEXT, false),
		RESOURCE_TYPE("resource_type", DataTypes.TEXT, false),
		SERVICE_INST_ID( "service_instance_id", DataTypes.TEXT, true),
		INVARIANT_UUID("invariant_uuid", DataTypes.TEXT, true),
		MODIFIER("modifier", DataTypes.TEXT, false),
		PREV_VERSION( "prev_version", DataTypes.TEXT, false),
		CURR_VERSION("curr_version", DataTypes.TEXT, false),
		PREV_STATE("prev_state", DataTypes.TEXT, false),
		CURR_STATE( "curr_state", DataTypes.TEXT, false),
		PREV_ARTIFACT_UUID( "prev_artifact_uuid", DataTypes.TEXT, false),
		CURR_ARTIFACT_UUID( "curr_artifact_uuid", DataTypes.TEXT, false),
		ARTIFACT_DATA( "artifact_data", DataTypes.TEXT, false);
        // @formatter:on

        private final String name;
        private final DataType type;
        private final boolean indexed;
    }
}
