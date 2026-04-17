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

public class DistribEngineEventTableDesc extends DistribBaseEventTableDesc {

    @Override
    protected void updateColumnDistribDescription(Map<String, ImmutablePair<DataType, Boolean>> columns) {
        for (DEEFieldsDescription field : DEEFieldsDescription.values()) {
            columns.put(field.getName(), new ImmutablePair<>(field.type, field.indexed));
        }
        //replace the base indexed flag value with the correct one for a given table:
        columns.put(DistFieldsDescription.REQUEST_ID.getName(), new ImmutablePair<>(DistFieldsDescription.REQUEST_ID.getType(), true));
    }

    @Override
    public String getTableName() {
        return AuditingTypesConstants.DISTRIBUTION_ENGINE_EVENT_TYPE;
    }

    @Getter
    @AllArgsConstructor
    enum DEEFieldsDescription {
        // @formatter:off
        CONSUMER_ID("consumer_id", DataTypes.TEXT, false),
		ROLE("role", DataTypes.TEXT, false),
		D_ENV("d_env", DataTypes.TEXT, false),
		API_KEY("api_key", DataTypes.TEXT, false),
		DSTATUS_TOPIC("dstatus_topic", DataTypes.TEXT, false),
		DNOTIF_TOPIC("dnotif_topic",DataTypes.TEXT, false);
        // @formatter:on

        private final String name;
        private final DataType type;
        private final boolean indexed;
    }
}
