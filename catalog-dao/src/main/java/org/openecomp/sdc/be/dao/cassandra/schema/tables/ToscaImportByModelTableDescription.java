/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.dao.cassandra.schema.tables;

import com.datastax.driver.core.DataType;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.cassandra.schema.ITableDescription;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;

public class ToscaImportByModelTableDescription implements ITableDescription {

    private static final String MODEL_ID = "model_id";
    private static final String FULL_PATH = "full_path";

    @Override
    public List<ImmutablePair<String, DataType>> primaryKeys() {
        return List.of(
            new ImmutablePair<>(MODEL_ID, DataType.varchar()),
            new ImmutablePair<>(FULL_PATH, DataType.varchar())
        );
    }

    @Override
    public List<ImmutablePair<String, DataType>> clusteringKeys() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, ImmutablePair<DataType, Boolean>> getColumnDescription() {
        return Stream.of(SdcSchemaFilesFieldsDescription.values())
            .collect(Collectors.toMap(SdcSchemaFilesFieldsDescription::getName, field -> new ImmutablePair<>(field.type, field.indexed)));
    }

    @Override
    public String getKeyspace() {
        return AuditingTypesConstants.ARTIFACT_KEYSPACE;
    }

    @Override
    public String getTableName() {
        return "tosca_import_by_model";
    }

    @Getter
    @AllArgsConstructor
    enum SdcSchemaFilesFieldsDescription {
        MODEL_ID("model_id", DataType.varchar(), true),
        CONTENT("content", DataType.varchar(), false);

        private final String name;
        private final DataType type;
        private final boolean indexed;
    }
}
