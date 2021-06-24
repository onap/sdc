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

import com.datastax.driver.core.DataType;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;

public class DistribStatusEventTableDesc extends DistribBaseEventTableDesc {

    @Override
    protected void updateColumnDistribDescription(final Map<String, ImmutablePair<DataType, Boolean>> columns) {
        for (final DSEFieldsDescription field : DSEFieldsDescription.values()) {
            columns.put(field.getName(), new ImmutablePair<>(field.type, field.indexed));
        }
    }

    @Override
    public String getTableName() {
        return AuditingTypesConstants.DISTRIBUTION_STATUS_EVENT_TYPE;
    }

    @Getter
    @AllArgsConstructor
    private enum DSEFieldsDescription {
        // @formatter:off
        DID("did", DataType.varchar(), true),
        CONSUMER_ID("consumer_id", DataType.varchar(), false),
        RESOURCE_URL("resoure_URL", DataType.varchar(), false),
        TOPIC_NAME("topic_name", DataType.varchar(), false),
        STATUS_TIME("status_time", DataType.varchar(), false);
        // @formatter:on

        private final String name;
        private final DataType type;
        private final boolean indexed;

    }
}
