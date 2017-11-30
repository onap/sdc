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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.cassandra.schema.ITableDescription;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;

import com.datastax.driver.core.DataType;

public class CategoryEventTableDescription implements ITableDescription {
	@Override
	public List<ImmutablePair<String, DataType>> primaryKeys() {
		List<ImmutablePair<String, DataType>> keys = new ArrayList<>();
		keys.add(new ImmutablePair<String, DataType>(TIMEBASED_UUID_FIELD, DataType.timeuuid()));
		return keys;
	}

	@Override
	public List<ImmutablePair<String, DataType>> clusteringKeys() {
		List<ImmutablePair<String, DataType>> keys = new ArrayList<>();
		keys.add(new ImmutablePair<String, DataType>(TIMESTAMP_FIELD, DataType.timestamp()));
		return keys;
	}

	@Override
	public Map<String, ImmutablePair<DataType, Boolean>> getColumnDescription() {
		Map<String, ImmutablePair<DataType, Boolean>> columns = new HashMap<>();

		for (CEFieldsDescription field : CEFieldsDescription.values()) {
			columns.put(field.getName(), new ImmutablePair<DataType, Boolean>(field.type, field.indexed));
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

		ACTION("action", DataType.varchar(), true), 
		STATUS("status", DataType.varchar(), false), 
		DESC("description", DataType.varchar(), false), 
		CATEGORY_NAME("category_Name", DataType.varchar(), false), 
		SUB_CATEGORY_NAME("sub_Category_Name", DataType.varchar(), false), 
		GROUPING_NAME("grouping_name", DataType.varchar(), false), 
		MODIFIER("modifier", DataType.varchar(), false), 
		REQUEST_ID("request_id", DataType.varchar(), false), 
		RESOURCE_TYPE("resource_type", DataType.varchar(), false), 
		SERVICE_INSTANCE_ID("service_instance_id", DataType.varchar(), false);

		private String name;
		private DataType type;
		private boolean indexed;

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
