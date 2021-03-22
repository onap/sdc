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

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.DataType.Name;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
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

    public static final ImmutableMap<String, Name> dataTypesMap;
    private static final Logger logger = LoggerFactory.getLogger(ImportSingleTable.class);
    private static final String INSERT_INTO = "INSERT INTO ";
    private static final String VALUES = " VALUES ";
    private static final Map<String, PreparedStatement> statementsCache = new HashMap<>();

    static {
        Builder<String, Name> builder = ImmutableMap.builder();
        Name[] values = Name.values();
        for (Name name : values) {
            builder.put(name.name().toLowerCase(), name);
        }
        dataTypesMap = builder.build();
    }

    public void importFile(Path file) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TableData tableData = objectMapper.readValue(file.toFile(), TableData.class);
            Session session = CassandraSessionFactory.getSession();
            PreparedStatement ps = getPrepareStatement(tableData, session);
            tableData.getRows().forEach(row -> executeQuery(session, ps, tableData.getDefinitions(), row));
        } catch (IOException e) {
            Utils.logError(logger, e);
        }
    }

    private PreparedStatement getPrepareStatement(TableData tableData, Session session) {
        String query = createQuery(tableData);
        if (statementsCache.containsKey(query)) {
            return statementsCache.get(query);
        }
        PreparedStatement preparedStatement = session.prepare(query);
        statementsCache.put(query, preparedStatement);
        return preparedStatement;
    }

    private void executeQuery(Session session, PreparedStatement ps, List<ColumnDefinition> definitions, List<String> rows) {
        BoundStatement bind = ps.bind();
        for (int i = 0; i < definitions.size(); i++) {
            ColumnDefinition columnDefinition = definitions.get(i);
            String rowData = rows.get(i);
            Name name = dataTypesMap.get(columnDefinition.getType());
            handleByType(bind, i, rowData, name);
        }
        session.execute(bind);
    }

    private void handleByType(BoundStatement bind, int i, String rowData, Name name) {
        switch (name) {
            case VARCHAR:
            case TEXT:
            case ASCII:
                String string = new String(Base64.getDecoder().decode(rowData));
                bind.setString(i, NULL_REPRESENTATION.equals(string) ? null : string);
                break;
            case BLOB:
                bind.setBytes(i, ByteBuffer.wrap(Base64.getDecoder().decode(rowData.getBytes())));
                break;
            case TIMESTAMP:
                if (StringUtils.isEmpty(rowData)) {
                    bind.setTimestamp(i, null);
                } else {
                    bind.setTimestamp(i, new Date(Long.parseLong(rowData)));
                }
                break;
            case BOOLEAN:
                bind.setBool(i, Boolean.parseBoolean(rowData));
                break;
            case COUNTER:
                bind.setLong(i, Long.parseLong(rowData));
                break;
            case INT:
                bind.setInt(i, Integer.parseInt(rowData));
                break;
            case FLOAT:
                bind.setFloat(i, Float.parseFloat(rowData));
                break;
            case SET:
                byte[] decoded = Base64.getDecoder().decode(rowData);
                String decodedStr = new String(decoded);
                if (!StringUtils.isEmpty(decodedStr)) {
                    String[] splitted = decodedStr.split(ExportDataCommand.JOIN_DELIMITER_SPLITTER);
                    Set set = Sets.newHashSet(splitted);
                    set.remove("");
                    bind.setSet(i, set);
                } else {
                    bind.setSet(i, null);
                }
                break;
            case MAP:
                byte[] decodedMap = Base64.getDecoder().decode(rowData);
                String mapStr = new String(decodedMap);
                if (!StringUtils.isEmpty(mapStr)) {
                    String[] splittedMap = mapStr.split(ExportDataCommand.JOIN_DELIMITER_SPLITTER);
                    Map<String, String> map = new HashMap<>();
                    for (String keyValue : splittedMap) {
                        String[] split = keyValue.split(ExportDataCommand.MAP_DELIMITER_SPLITTER);
                        map.put(split[0], split[1]);
                    }
                    bind.setMap(i, map);
                } else {
                    bind.setMap(i, null);
                }
                break;
            default:
                throw new UnsupportedOperationException("Name is not supported :" + name);
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
