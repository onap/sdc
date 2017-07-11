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

public class SdcSchemaFilesTableDescription implements ITableDescription {
	
	private static final String SDC_RELEASE_NUM = "sdcReleaseNum";
	private static final String TIMESTAMP = "timestamp";
	private static final String CONFORMANCE_LEVEL = "conformanceLevel";
	
	@Override
	public List<ImmutablePair<String, DataType>> primaryKeys() {
		List<ImmutablePair<String, DataType>> keys = new ArrayList<>();
		keys.add(new ImmutablePair<String, DataType>(SDC_RELEASE_NUM, DataType.varchar()));
		keys.add(new ImmutablePair<String, DataType>(CONFORMANCE_LEVEL, DataType.varchar()));
		return keys;
	}
	
	@Override
	public List<ImmutablePair<String, DataType>> clusteringKeys() {
		List<ImmutablePair<String, DataType>> keys = new ArrayList<>();
		keys.add(new ImmutablePair<String, DataType>(TIMESTAMP, DataType.timestamp()));
		return keys;
	}
	
	@Override
	public Map<String, ImmutablePair<DataType, Boolean>> getColumnDescription() {
		Map<String, ImmutablePair<DataType, Boolean>> columns = new HashMap<>();

		for (SdcSchemaFilesFieldsDescription field : SdcSchemaFilesFieldsDescription.values()) {
			columns.put(field.getName(), new ImmutablePair<DataType, Boolean>(field.type, field.indexed));
		}

		return columns;
	}

	@Override
	public String getKeyspace() {
		return AuditingTypesConstants.ARTIFACT_KEYSPACE;
	}

	@Override
	public String getTableName() {
		return "sdcSchemaFiles";  
	}
	
	enum SdcSchemaFilesFieldsDescription {
		FILE_NAME("fileName", DataType.varchar(), false),
		PAYLOAD("payload", DataType.blob(), false),
		CHECKSUM("checksum", DataType.varchar(), false);

		private String name;
		private DataType type;
		private boolean indexed;

		SdcSchemaFilesFieldsDescription(String name, DataType type, boolean indexed) {
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
