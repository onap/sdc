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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;

import java.util.Map;

public class DistribNotifEventTableDesc extends DistribBaseEventTableDesc {

	@Override
	protected void updateColumnDistribDescription(Map<String, ImmutablePair<DataType, Boolean>> columns) {
		for (DNEFieldsDescription field : DNEFieldsDescription.values()) {
			columns.put(field.getName(), new ImmutablePair<>(field.type, field.indexed));
		}
        //replace the base indexed flag value with the correct one for a given table:
        columns.put(DistFieldsDescription.SERVICE_INST_ID.getName(),
                new ImmutablePair<>(DistFieldsDescription.SERVICE_INST_ID.getType(), true));
	}

	@Override
	public String getTableName() {
		return AuditingTypesConstants.DISTRIBUTION_NOTIFICATION_EVENT_TYPE;
	}

	enum DNEFieldsDescription {
		TOPIC_NAME("topic_name", DataType.varchar(), false),
		MODIFIER("modifier", DataType.varchar(), false), 
		CURR_STATE("curr_state", DataType.varchar(), false), 
		CURR_VERSION("curr_version", DataType.varchar(), false), 
		DID("did", DataType.varchar(), true), 
		RESOURCE_NAME("resource_name", DataType.varchar(), false), 
		RESOURCE_TYPE("resource_type", DataType.varchar(), false),
		ENV_ID("env_id", DataType.varchar(), false),
		VNF_WORKLOAD_CONTEXT("vnf_workload_context", DataType.varchar(), false),
		TENANT("tenant", DataType.varchar(), false);

		private String name;
		private DataType type;
		private boolean indexed;

		DNEFieldsDescription(String name, DataType type, boolean indexed) {
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
