/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.core.tools.importinfo;

import static org.openecomp.core.tools.exportinfo.ExportDataCommand.NULL_REPRESENTATION;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.type.DataType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.core.nosqldb.impl.cassandra.CassandraSessionFactory;
import org.openecomp.core.tools.exportinfo.ExportDataCommand;
import org.openecomp.core.tools.model.ColumnDefinition;
import org.openecomp.core.tools.model.TableData;
import org.openecomp.core.tools.util.Utils;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

public class ImportSingleTable {

    public static final ImmutableMap<String, DataType> dataTypesMap;
    private static final Logger logger = LoggerFactory.getLogger(ImportSingleTable.class);
    private static final String INSERT_INTO = "INSERT INTO ";
    private static final String VALUES = " VALUES ";
    private static final Map<String, PreparedStatement> statementsCache = new HashMap<>();

    static {
        Builder<String, DataType> builder = ImmutableMap.builder();
        // dataTypesMap can remain empty, as we store DataType directly in ColumnDefinition
        dataTypesMap = builder.build();
    }

    public void importFile(Path file) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TableData tableData = objectMapper.readValue(file.toFile(), TableData.class);
            try (CqlSession session = CassandraSessionFactory.getSession()) {
                PreparedStatement ps = getPrepareStatement(tableData, session);
                tableData.getRows().forEach(row -> executeQuery(session, ps, tableData.getDefinitions(), row));
            }
        } catch (IOException e) {
            Utils.logError(logger, e);
        }
    }

     private PreparedStatement getPrepareStatement(TableData tableData, CqlSession session) {
        String query = createQuery(tableData);
        if (statementsCache.containsKey(query)) {
            return statementsCache.get(query);
        }
        PreparedStatement preparedStatement = session.prepare(SimpleStatement.newInstance(query));
        statementsCache.put(query, preparedStatement);
        return preparedStatement;
    }

      private void executeQuery(CqlSession session, PreparedStatement ps, List<ColumnDefinition> definitions, List<String> rows) {
        BoundStatement bind = ps.boundStatementBuilder().build();
        for (int i = 0; i < definitions.size(); i++) {
            ColumnDefinition columnDefinition = definitions.get(i);
            String rowData = rows.get(i);
            DataType type = columnDefinition.getDataType();  // get stored DataType
            handleByType(bind, i, rowData, type);
        }
        session.execute(bind);
    }

    private void handleByType(BoundStatement bind, int i, String rowData, DataType type) {
        if (type == null) {
            throw new UnsupportedOperationException("DataType is null at index " + i);
        }

        switch (type.getProtocolCode()) { // use protocol code to identify type
            case com.datastax.oss.protocol.internal.ProtocolConstants.DataType.ASCII:
            case com.datastax.oss.protocol.internal.ProtocolConstants.DataType.VARCHAR:
                String string = new String(Base64.getDecoder().decode(rowData));
                bind = bind.set(i, NULL_REPRESENTATION.equals(string) ? null : string, String.class);
                break;

            case com.datastax.oss.protocol.internal.ProtocolConstants.DataType.BLOB:
                bind = bind.set(i, ByteBuffer.wrap(Base64.getDecoder().decode(rowData.getBytes())), ByteBuffer.class);
                break;

            case com.datastax.oss.protocol.internal.ProtocolConstants.DataType.TIMESTAMP:
                if (StringUtils.isEmpty(rowData)) {
                    bind = bind.set(i, null, Date.class);
                } else {
                    bind = bind.set(i, new Date(Long.parseLong(rowData)), Date.class);
                }
                break;

            case com.datastax.oss.protocol.internal.ProtocolConstants.DataType.BOOLEAN:
                bind = bind.set(i, Boolean.parseBoolean(rowData), Boolean.class);
                break;

            case com.datastax.oss.protocol.internal.ProtocolConstants.DataType.COUNTER:
                bind = bind.set(i, Long.parseLong(rowData), Long.class);
                break;

            case com.datastax.oss.protocol.internal.ProtocolConstants.DataType.INT:
                bind = bind.set(i, Integer.parseInt(rowData), Integer.class);
                break;

            case com.datastax.oss.protocol.internal.ProtocolConstants.DataType.FLOAT:
                bind = bind.set(i, Float.parseFloat(rowData), Float.class);
                break;

            case com.datastax.oss.protocol.internal.ProtocolConstants.DataType.SET:
                byte[] decoded = Base64.getDecoder().decode(rowData);
                String decodedStr = new String(decoded);
                if (!StringUtils.isEmpty(decodedStr)) {
                    String[] splitted = decodedStr.split(ExportDataCommand.JOIN_DELIMITER_SPLITTER);
                    Set<String> set = Sets.newHashSet(splitted);
                    set.remove("");
                    bind = bind.set(i, set, Set.class);
                } else {
                    bind = bind.set(i, null, Set.class);
                }
                break;

            case com.datastax.oss.protocol.internal.ProtocolConstants.DataType.MAP:
                byte[] decodedMap = Base64.getDecoder().decode(rowData);
                String mapStr = new String(decodedMap);
                if (!StringUtils.isEmpty(mapStr)) {
                    String[] splittedMap = mapStr.split(ExportDataCommand.JOIN_DELIMITER_SPLITTER);
                    Map<String, String> map = new HashMap<>();
                    for (String keyValue : splittedMap) {
                        String[] split = keyValue.split(ExportDataCommand.MAP_DELIMITER_SPLITTER);
                        map.put(split[0], split[1]);
                    }
                    bind = bind.set(i, map, Map.class);
                } else {
                    bind = bind.set(i, null, Map.class);
                }
                break;

            default:
                throw new UnsupportedOperationException("DataType not supported: " + type.asCql(false, false));
        }
    }

    private String createQuery(TableData tableData) {
        ColumnDefinition def = tableData.getDefinitions().iterator().next();
        StringBuilder sb = new StringBuilder(1024);
        sb.append(INSERT_INTO).append(def.getKeyspace()).append(".").append(def.getTable());
        sb.append(tableData.getDefinitions().stream().map(ColumnDefinition::getName).collect(Collectors.joining(" , ", " ( ", " ) ")));
        sb.append(VALUES).append(tableData.getDefinitions().stream().map(definition -> "?").collect(Collectors.joining(" , ", " ( ", " ) ")))
            .append(";");
        return sb.toString();
    }
}
