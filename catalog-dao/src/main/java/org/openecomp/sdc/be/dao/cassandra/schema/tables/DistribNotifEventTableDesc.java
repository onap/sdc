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
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;

public class DistribNotifEventTableDesc extends DistribBaseEventTableDesc {

    @Override
    protected void updateColumnDistribDescription(Map<String, ImmutablePair<DataType, Boolean>> columns) {
        for (DNEFieldsDescription field : DNEFieldsDescription.values()) {
            columns.put(field.getName(), new ImmutablePair<>(field.type, field.indexed));
        }
        //replace the base indexed flag value with the correct one for a given table:
        columns.put(DistFieldsDescription.SERVICE_INST_ID.getName(), new ImmutablePair<>(DistFieldsDescription.SERVICE_INST_ID.getType(), true));
    }

    @Override
    public String getTableName() {
        return AuditingTypesConstants.DISTRIBUTION_NOTIFICATION_EVENT_TYPE;
    }

    @Getter
    @AllArgsConstructor
    enum DNEFieldsDescription {
        // @formatter:off
        TOPIC_NAME("topic_name", DataTypes.TEXT, false),
		MODIFIER("modifier", DataTypes.TEXT, false),
		CURR_STATE("curr_state", DataTypes.TEXT, false),
		CURR_VERSION("curr_version", DataTypes.TEXT, false),
		DID("did", DataTypes.TEXT, true),
		RESOURCE_NAME("resource_name", DataTypes.TEXT, false),
		RESOURCE_TYPE("resource_type", DataTypes.TEXT, false),
		ENV_ID("env_id", DataTypes.TEXT, false),
		VNF_WORKLOAD_CONTEXT("vnf_workload_context", DataTypes.TEXT, false),
		TENANT("tenant", DataTypes.TEXT, false);
        // @formatter:on

        private final String name;
        private final DataType type;
        private final boolean indexed;
    }
}
