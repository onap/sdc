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

public class DistribDeployEventTableDesc extends DistribBaseEventTableDesc {


	@Override
	public String getTableName() {
		return AuditingTypesConstants.DISTRIBUTION_DEPLOY_EVENT_TYPE;
	}

	@Override
	protected void updateColumnDistribDescription(Map<String, ImmutablePair<DataType, Boolean>> columns) {
		for (DSEFieldsDescription field : DSEFieldsDescription.values()) {
			columns.put(field.getName(), new ImmutablePair<>(field.type, field.indexed));
		}
		//replace the base indexed flag value with the correct one for a given table:
		columns.put(DistFieldsDescription.STATUS.getName(),
                new ImmutablePair<>(DistFieldsDescription.STATUS.getType(), true));
        columns.put(DistFieldsDescription.SERVICE_INST_ID.getName(),
                new ImmutablePair<>(DistFieldsDescription.SERVICE_INST_ID.getType(), true));

    }

	enum DSEFieldsDescription {
		MODIFIER("modifier", DataType.varchar(), false),
		CURR_VERSION("curr_version", DataType.varchar(), false), 
		DID("did", DataType.varchar(), true), 
		RESOURCE_NAME("resource_name", DataType.varchar(), false), 
		RESOURCE_TYPE("resource_type", DataType.varchar(), false);

		private String name;
		private DataType type;
		private boolean indexed;

		DSEFieldsDescription(String name, DataType type, boolean indexed) {
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
